package com.topsec.tsm.sim.auth.web;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.auth.manage.AuthAccount;
import com.topsec.tsm.auth.manage.LoginFailed;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.base.type.Severity;
import com.topsec.tsm.common.SIMConstant;
import com.topsec.tsm.resource.AuditCategoryDefinition;
import com.topsec.tsm.sim.access.SysTreeMenu;
import com.topsec.tsm.sim.access.service.SysAccessService;
import com.topsec.tsm.sim.access.util.GlobalUtil;
import com.topsec.tsm.sim.auth.service.UserService;
import com.topsec.tsm.sim.auth.util.LoginUserCache;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.auth.util.SafeMgrConfigUtil;
import com.topsec.tsm.sim.auth.util.ThreeAuthority;
import com.topsec.tsm.sim.common.bean.Result;
import com.topsec.tsm.sim.common.exception.CommonUserException;
import com.topsec.tsm.sim.util.CommonUtils;
import com.topsec.tsm.util.UUIDUtils;
import com.topsec.tsm.util.encrypt.RSAUtil;

@Controller
@RequestMapping("userLogin")
public class LoginController {
	
	private static final Logger log = LoggerFactory.getLogger(LoginController.class);
	/**客户端登录失败缓存*/
	private static final Map<String,LoginFailed> LOGIN_IP_FAILED_CACHE = new HashMap<String,LoginFailed>();
	private static final Map<String,LoginFailed> LOGIN_NAME_FAILED_CACHE = new HashMap<String,LoginFailed>();
	//账户或者ip被锁定的最长分钟数
	private static final long TIME_OF_FORBIDDEN = 3L;
	private UserService userService;
	@Autowired
	private SysAccessService sysAccessService;
	
	@Autowired
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	/**
	 * Login 用户登录
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("login")
	public String login(HttpServletRequest request,HttpSession session){
		try {
			String loginName = StringUtil.nvl(request.getParameter("loginName")).toLowerCase();
			String password = request.getParameter("encryptPassword");
			String loginIp = request.getRemoteAddr() ;
			if(!loginInfoValid(request,session, loginName, password)){
				return "/page/login" ;
			}
			LoginFailed failedIPInfo = LOGIN_IP_FAILED_CACHE.get(loginIp) ;
			LoginFailed failedNameInfo = LOGIN_NAME_FAILED_CACHE.get(loginName) ;
			//地址被禁用
			if(CommonUtils.isForbidden(failedIPInfo)){
				request.setAttribute("error", "IP地址已被禁用,请于"+CommonUtils.getForbidenTime(failedIPInfo,TIME_OF_FORBIDDEN)+"后重试！") ;
				return "/page/login" ;
			}
			//账户被禁用
			if(CommonUtils.isForbidden(failedNameInfo)){
				request.setAttribute("error", "账户已被禁用,请于"+CommonUtils.getForbidenTime(failedNameInfo,TIME_OF_FORBIDDEN)+"后重试！") ;
				return "/page/login" ;
			}
			AuthAccount account = userService.getUserByUserName(loginName.toLowerCase());
			if(account == null){
				request.setAttribute("error", "账号不存在！");
				return "/page/login" ;
			}
			//RSAPublicKey clientKey = createClientPublicKey(request) ;
			//是否是MD5的密码，如果是则不需要再解密
			boolean isMD5Password = StringUtil.booleanVal(request.getParameter("md5Pwd")) ;
			String md5Password = isMD5Password ? password : convertPassword(password) ;
			if(!account.getPasswd().equals(md5Password)){
				CommonUtils.incrementFailTimes(loginName, loginIp, failedIPInfo,failedNameInfo,LOGIN_IP_FAILED_CACHE,LOGIN_NAME_FAILED_CACHE,request) ;
				CommonUtils.generateLog(AuditCategoryDefinition.SYS_LOGIN,"登录","密码错误！",loginName, request.getRemoteHost(),false,Severity.HIGH);
				return "/page/login";
			}else if(account.getEnable() == 0){
				request.setAttribute("error","账号已被禁用！");
				return "/page/login";
			}else if(account.isExpire()){
				request.setAttribute("error", "账号已过期！") ;
				return "/page/login" ;
			}else if(!loginAddrValid(account,loginIp)){
				request.setAttribute("error", "无效的客户端IP！") ;
				return "/page/login";
			}else if(!account.isDefaultUser() && isAccountPasswordExpire(account)){
				request.setAttribute("error", "密码已经过期，请修改密码！");
				return "/page/login";
			}
			SID sid = createSID(account, request, loginName, loginIp) ;
			log.info(StringUtil.today("yyyy-MM-dd HH:mm:ss") + "用户:" + loginName + " 从 " + request.getRemoteHost() + " 登录成功。");
			CommonUtils.generateLog(AuditCategoryDefinition.SYS_LOGIN,"登录系统","系统登录时间: " + StringUtil.today("yyyy-MM-dd HH:mm:ss") + " 用户：" + loginName,loginName, request.getRemoteHost(),true,Severity.LOWEST);
			loadUserMenus(sid,session);
			LOGIN_IP_FAILED_CACHE.remove(loginIp) ;
			LOGIN_NAME_FAILED_CACHE.remove(loginName) ; 
			return "redirect:/sim/index/";
		} catch (CommonUserException e) {
			request.setAttribute("error", e.getMessage()) ;
			return "/page/login";
		}
	}
	/**
	 * 判断登录信息是否有效
	 * @param request
	 * @param session
	 * @param loginName
	 * @param password
	 * @return
	 */
	private boolean loginInfoValid(HttpServletRequest request,HttpSession session,String loginName,String password){
		request.setAttribute("loginName", loginName) ;
		if (StringUtils.isBlank(loginName) || StringUtils.isBlank(password)) {
			request.setAttribute("error","账号、密码不能为空！");
			return false ;
		}
		if(request.getParameter("noValidCodeLogin")==null){
			String validCode = request.getParameter("validCode") ;
			if(StringUtil.isBlank(validCode)||!validCode.equalsIgnoreCase((String)session.getAttribute("validCode"))){
				request.setAttribute("error", "验证码错误！") ;
				request.setAttribute("validCodeError", true) ;
				return false ;
			}
		}
		return true ;

	}
	
	/**
	 * 根据客户端提供的公钥信息生成公钥对象
	 * @param request
	 * @return
	 */
	private RSAPublicKey genClientPublicKey(HttpServletRequest request){
		RSAPublicKey key = null ;
		try {
			String[] keys = StringUtil.split(request.getParameter("clientPublicKey"),"|") ;
			key = RSAUtil.getRSAPublidKey(keys[0], keys[1]) ;
		} catch (Exception e) {
			e.printStackTrace() ;
			throw new CommonUserException("无效的客户端密钥！");
		}
		return key ;
	}
	/**
	 * 判断账号密码是否已经过期
	 * @param account
	 * @return
	 */
	private boolean isAccountPasswordExpire(AuthAccount account){
		String pwdModifyCycleStr = SafeMgrConfigUtil.getInstance().getSafeMgrConfigValueByName("pwdModifyCycle");
		long mdfPwdTime = account.getMdfPwdTime();
		long mdfPwdTimeMills = System.currentTimeMillis() - mdfPwdTime;
		long pwdModifyCycle = StringUtil.toLongNum(pwdModifyCycleStr,30);
		boolean mdfPwdTimeOut = mdfPwdTimeMills > pwdModifyCycle*24*60*60*1000;
		return mdfPwdTimeOut ;
	}
	
	/**
	 * 获取密码
	 * @param password
	 * @return
	 */
	private String convertPassword(String password){
		password = CommonUtils.decrypt(password,"") ;
		return StringUtil.MD5(password).toLowerCase() ;
	}
	
    /**
     * 判断登录地址是否合法
     * @param ip
     * @return
     */
    public boolean loginAddrValid(AuthAccount account,String ip){
    	boolean valid = true ;
    	if(ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")){
    		ip = IpAddress.getLocalIp().getLocalhostAddress() ;
    	}
    	IpAddress loginIP = new IpAddress(ip);    	
    	if(account.isAdmin()){
    		List<IpAddress> localIpList = IpAddress.getAllLocalIp();    		
    		if(localIpList.contains(loginIP)||ip.equals("127.0.0.1")||ip.equals("::1")){//保证admin始终可在服务器本机登陆。
    			return valid;
    		}
    	}    	
		BigInteger startIp = account.getValidIPStart() ;
		BigInteger endIp = account.getValidIPEnd() ;		
		BigInteger userIp = BigInteger.valueOf(loginIP.getLowAddress());
		if(startIp != null){
			valid = valid && userIp.compareTo(startIp) >= 0 ;
		}
		if (endIp != null) {
			valid = valid && userIp.compareTo(endIp) <= 0 ;
		}
		return valid ;
    }
    
    private SID createSID(AuthAccount account,HttpServletRequest request,String loginName,String loginIp){
    	HttpSession session = request.getSession() ;
		account.setFailed(0) ;
		account.setLateLoginIP(BigInteger.valueOf(new IpAddress(loginIp).getLowAddress())) ;
		account.setLateLoginTime(BigInteger.valueOf(System.currentTimeMillis())) ;
		userService.modifyInfo(account) ;
		SID sid = new SID(loginIp,account);
		boolean addSuccess = LoginUserCache.getInstance().addUser(sid);
		if(!addSuccess){
			throw new CommonUserException("登录失败，在线用户数已达上限！") ;
		}
		SID sessionSID = (SID) session.getAttribute(SIMConstant.SESSION_SID) ;
		if(sessionSID != null && !sessionSID.getUserName().equalsIgnoreCase(sid.getUserName())){
			LoginUserCache.getInstance().removeUser(sessionSID.getUserName()) ;
		}
		List<SID> onlineUser = LoginUserCache.getInstance().getOnlineUsers();
		for (SID userid : onlineUser) {
			if(!loginName.equals(userid.getUserName()) && loginIp.equals(userid.getLoginIP())){
				LoginUserCache.getInstance().removeUser(userid.getUserName()) ;
				break;
			}
		}
		LOGIN_IP_FAILED_CACHE.remove(loginIp) ;
		session.setAttribute("locked", false) ;
		session.setAttribute(SIMConstant.SESSION_SID, sid);
		session.setAttribute(SIMConstant.SESSION_TOKEN, UUIDUtils.compactUUID()) ;
		return sid ;
    }
    
	/**
	 * /注销操作
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("logout")
	public String logout(HttpServletRequest request,HttpSession session){
		try {
			SID sid = (SID)session.getAttribute(SIMConstant.SESSION_SID);
			if(sid != null){
				String userName =sid.getUserName();
				CommonUtils.generateLog(AuditCategoryDefinition.SYS_LOGOUT,"注销系统","注销系统时间: " + StringUtil.today("yyyy-MM-dd HH:mm:ss") + "   用户：" + userName,userName, request.getRemoteHost(),true,Severity.LOWEST);
				log.info(StringUtil.today("yyyy-MM-dd HH:mm:ss") + " 用户:" + userName + " 从 " + request.getRemoteHost() + " 注销系统。");
			}
			session.invalidate() ;
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		return "redirect:/";
	}
	/**
	 * 检测用户是否已经登陆
	 * @param loginName
	 * @return
	 */
	@RequestMapping("checkLogin")
	@ResponseBody
	public Object checkLogin(@RequestParam("loginName")String loginName,HttpServletRequest request){
		SID sid = loginName == null ? null : LoginUserCache.getInstance().getLoginUserCachByName(loginName.toLowerCase()) ;
		JSONObject result = new JSONObject() ;
		if(sid != null && !request.getRemoteHost().equals(sid.getLoginIP())){
			result.put("exist", true) ;
			result.put("ip",sid.getLoginIP()) ;
		}else{
			result.put("exist", false) ;
		}
		return result ;
	}
	
	@RequestMapping("unlock")
	@ResponseBody
	public Object unlock(@RequestParam("password")String password,SID sid,HttpSession session,HttpServletRequest request){
		Result result = new Result() ;
		AuthAccount aa = userService.getUserByID(sid.getAccountID()) ;
		if(aa == null){
			return result.buildError("账号不存在！");
		}
		String loginIp = request.getRemoteAddr() ;
		LoginFailed failedInfo = LOGIN_IP_FAILED_CACHE.get(loginIp) ;
		if(CommonUtils.isForbidden(failedInfo)){
			return result.buildError("IP地址已被禁用！") ;
		}
		password = convertPassword(password) ;
		if(password.equals(aa.getPasswd())){
			result.buildSuccess(null);
			session.setAttribute("locked", false) ;
		}else{
			if(failedInfo == null){
				failedInfo = new LoginFailed(loginIp) ;
				LOGIN_IP_FAILED_CACHE.put(loginIp, failedInfo) ;
			}
			failedInfo.increment() ;
			if(CommonUtils.isForbidden(failedInfo)){
				return result.buildError("密码错误，IP地址已被禁用！");
			}else{
				return result.buildError("密码错误");
			}
		}
		return result;
	}
	@RequestMapping("lock")
	@ResponseBody
	public Object lock(HttpSession session){
		session.setAttribute("locked", true) ;
		return true ;
	}
	/**
	 * 加载用户菜单
	 * @param sid
	 * @param session
	 */
	private void loadUserMenus(SID sid,HttpSession session){
		Set<SysTreeMenu>setSysTreeMenus=sysAccessService.showTopTreeMenus(sid.getAccountID());
		if (GlobalUtil.isNullOrEmpty(session.getAttribute("setSysTreeMenus"))) {
			session.removeAttribute("setSysTreeMenus");
		}
		sid.setAuthority(new ThreeAuthority()) ;
		session.setAttribute("isDefaultUser", sid.isDefaultUser()) ;
		session.setAttribute("setSysTreeMenus", setSysTreeMenus);
		session.setAttribute("debug", StringUtil.booleanVal(System.getProperty(SIMConstant.DEBUG_PROPERTY_KEY))) ;
		session.setAttribute("hasOperatorRole",sid.hasOperatorRole());
		session.setAttribute("userName",sid.getUserName());
		session.setAttribute("isOperator",sid.isOperator());
	}
}
