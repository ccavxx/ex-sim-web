package com.topsec.tsm.sim.auth.web;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.index.IndexTemplateUtil;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.auth.manage.AuthAccount;
import com.topsec.tsm.auth.manage.AuthRole;
import com.topsec.tsm.auth.manage.AuthUserDevice;
import com.topsec.tsm.auth.manage.LoginFailed;
import com.topsec.tsm.auth.manage.UserColumnConfig;
import com.topsec.tsm.base.type.Severity;
import com.topsec.tsm.resource.AuditCategoryDefinition;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.AssetObject;
import com.topsec.tsm.sim.asset.AssetUtil;
import com.topsec.tsm.sim.asset.group.AssetGroup;
import com.topsec.tsm.sim.asset.group.GroupByAssetCategory;
import com.topsec.tsm.sim.asset.group.GroupByAssetVender;
import com.topsec.tsm.sim.asset.group.GroupStrategy;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.auth.form.UserForm;
import com.topsec.tsm.sim.auth.service.UserService;
import com.topsec.tsm.sim.auth.util.MD5;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.auth.util.SafeMgrConfigUtil;
import com.topsec.tsm.sim.auth.util.Util;
import com.topsec.tsm.sim.common.bean.Result;
import com.topsec.tsm.sim.common.model.TreeModel;
import com.topsec.tsm.sim.common.tree.DefaultTreeIterator;
import com.topsec.tsm.sim.common.tree.FastJsonResult;
import com.topsec.tsm.sim.common.tree.FastJsonTreeVisitor;
import com.topsec.tsm.sim.common.tree.VisitResultListener;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.util.CommonUtils;
import com.topsec.tsm.sim.util.DeviceTypeNameUtil;
import com.topsec.tsm.sim.util.FastJsonUtil;
import com.topsec.tsm.tal.service.EventResponseService;
@Controller
@RequestMapping("authUser")
public class UserController  { 
		private static final Map<String,LoginFailed> LOGIN_IP_FAILED_CACHE = new HashMap<String,LoginFailed>();
		private static final Map<String,LoginFailed> LOGIN_NAME_FAILED_CACHE = new HashMap<String,LoginFailed>();
		//账户或者ip被锁定的最长分钟数
		private static final long TIME_OF_FORBIDDEN = 3L;
		private UserService userService;
		private EventResponseService eventResponseService;
		private DataSourceService dataSourceService;
		@Autowired
		@Qualifier("dataSourceService")
		public void setDataSourceService(DataSourceService dataSourceService) {
			this.dataSourceService = dataSourceService;
		}
		@Autowired
		public void setUserService(UserService userService) {
			this.userService = userService;
		}

		@Autowired
		public void setEventResponseService(EventResponseService eventResponseService) {
			this.eventResponseService = eventResponseService;
		}
		@RequestMapping("getGroupEnable")
		@ResponseBody
		public Object getGroupEnable(){
			boolean groupEnable=StringUtil.booleanVal(System.getProperty("ENABLE.GROUP.FILTER")) ;
			return groupEnable;
		}
		@RequestMapping("getAssetTree")
		@ResponseBody
		public Object getAssetTree(SID sid){
			try {
				SID.setCurrentUser(sid) ;
				GroupStrategy strategy = new GroupByAssetCategory(new GroupByAssetVender()) ;
				AssetGroup root = AssetFacade.getInstance().groupByWithRoot(strategy) ;
				DefaultTreeIterator iterator = new DefaultTreeIterator() ;
				iterator.regist(new VisitResultListener<FastJsonResult,AssetGroup>() {
					@Override
					public void onResult(FastJsonResult result, AssetGroup group) {
						if(group.getLevel() == 1){
							return ;
						}
						if(group.getLevel() == 2){//第一级为虚拟root
							result.put("iconCls", AssetUtil.getIconClsByDeviceType(group.getId())) ;
						}else if(group.getLevel() == 3){
							result.put("iconCls", "icon-none") ;
						}
						if(group.isLeaf()){
							JSONArray child = FastJsonUtil.toJSONArray(group.getAllAssets(), "ip=id","name=text","deviceType=type","id=resid","scanNodeId=nodeid") ;
							JSONObject attr = new JSONObject();
							attr.put("isAsset", true) ;
							FastJsonUtil.put(child, "attributes", attr) ;
							FastJsonUtil.put(child, "iconCls",  AssetUtil.getIconClsByDeviceType(group.getParent().getId())) ;
							JSONArray jsonArray=new JSONArray();
							for (int i = 0; i < child.size(); i++) {
					            JSONObject dataSourceIp = (JSONObject) child.get(i);
					            JSONArray  attributes=new JSONArray();
					            List<SimDatasource> assetDataSources = dataSourceService.getByIp(dataSourceIp.getString("id")) ;
								JSONObject jsonObject=new JSONObject();
								if(assetDataSources.size()>0){
									jsonObject.put("deviceType", assetDataSources.get(0).getSecurityObjectType());
								}
								attributes.add(jsonObject);
					        	dataSourceIp.put("attributes", attributes);
					        	jsonArray.add(dataSourceIp);
							}
							result.put("children", jsonArray) ;
						}else{
							result.put("state", "closed") ;
						}
					}
				}) ;
				FastJsonResult result = (FastJsonResult) iterator.iterate(root, new FastJsonTreeVisitor("id","name=text")) ;
				JSONObject attr = new JSONObject();
				attr.put("text","全选");
				attr.put("children", result.get("children"));
				JSONArray arr=new JSONArray();
				arr.add(attr);
				Object childrenObj = arr ;
				return childrenObj==null ? new JSONArray() : childrenObj ;
			}finally{
				SID.removeCurrentUser() ;
			}
		}

		/**
		 * getView 获取用户列表
		 * @author zhou_xiaohu
		 * @param roleName
		 * @return
		 */
		@RequestMapping("getView")
		@ResponseBody
		public Object getView(SID sid,@RequestParam Map<String,Object> searchCondition,HttpServletRequest request){
			int pageIndex = StringUtil.toInt((String)searchCondition.get("page"),1);
			int pageSize = StringUtil.toInt((String)searchCondition.get("rows"), 20) ;
			JSONArray jsonArray=new JSONArray();
			JSONObject result = new JSONObject();
			SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
			if(!sid.isAdmin()){
				searchCondition.put("creater",sid.getUserName());
			}
//			searchCondition.put("userName",sid.getUserName());
			try {
				PageBean<AuthAccount> page = userService.getUsersPage(pageIndex,pageSize,searchCondition);
				result.put("total", page.getTotal()) ;
				List<AuthAccount> authAccounts = page.getData() ;
				result.put("rows", jsonArray) ;
				for(AuthAccount user: authAccounts){
					JSONObject jsonObj=new JSONObject();
					Set set = user.getRoles();
					Set resource=user.getUserDevice();
					Iterator resIterator=resource.iterator();
					StringBuffer resStr=new StringBuffer();
					StringBuffer text=new StringBuffer();
					int i=0;
					while(resIterator.hasNext()){
						AuthUserDevice authUserDevice=(AuthUserDevice)resIterator.next();
						AssetObject assetObject = AssetFacade.getInstance().getById(authUserDevice.getDeviceId());
						if(assetObject == null){
							continue;
						}
						resStr.append(assetObject.getName()).append(",");
						resStr.append(authUserDevice.getDeviceType()).append(",");
						resStr.append(authUserDevice.getDeviceId()).append(",");
						resStr.append(authUserDevice.getIp()).append(",");
						List<SimDatasource> assetDataSources = dataSourceService.getByIp(authUserDevice.getIp()) ;
						if(assetDataSources.size()>0){
						  resStr.append(assetDataSources.get(0).getSecurityObjectType());
						}
						text.append(assetObject.getName());
						if(i<resource.size()-1){resStr.append(";");text.append(",");}
						i++;
					}
					jsonObj.put("AssetName",text.toString());
					jsonObj.put("RESOURCEID",resStr.toString());
					Iterator iterator = set.iterator();
					while(iterator.hasNext()){
						AuthRole role = (AuthRole)iterator.next();
						jsonObj.put("ROLEID",role.getId());
						jsonObj.put("ROLENAME",role.getName());
					}
					jsonObj.put("USERID", user.getID());
				    if(user.isDefaultUser()){
				    	jsonObj.put("action", "edit") ;
				    }else if(sid.isAdmin() || user.getCreateUser().equalsIgnoreCase(sid.getUserName())){
						jsonObj.put("action", "editAndDel"); 
					}
					jsonObj.put("USERNAME", user.getName());
					jsonObj.put("CREATEUSER", user.getCreateUser());
					jsonObj.put("LASTMODIFYUSER", user.getLateModifyUser());
					jsonObj.put("LASTMODIFYTIME",simpleDateFormat.format(user.getLateModifyTime()));
					jsonObj.put("MINIP", Util.toIpString(user.getValidIPStart()));
					jsonObj.put("MAXIP", Util.toIpString(user.getValidIPEnd()));
					jsonObj.put("STATUS", user.getEnable());
					if(StringUtil.isNotBlank(user.getDescription())){
						jsonObj.put("DESCRIPTION", user.getDescription());
					}
					if(StringUtil.isNotBlank(user.getGroupId())){
						jsonObj.put("groupId", user.getGroupId());
						jsonObj.put("groupMap", getGroupName(user.getGroupId()));
					}
					jsonObj.put("EXPIRETIME",user.isAdmin() ? "永久有效" : simpleDateFormat.format(user.getExpireTime()));
					jsonArray.add(jsonObj);
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			} 
			return result;
		}
		public List<Map<String,Object>> getGroupName(String groupId){
			List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
				String[] groupArr=groupId.split(";");
				for(int temp=0;temp<groupArr.length;temp++){
					String groupResult=groupArr[temp];
					String deviceType=groupResult.split(":")[0];
					String groupTemp=groupResult.split(":")[1];
					String[] groupName=groupTemp.split("#");
					String deviceTypeZHName=DeviceTypeNameUtil.getDeviceTypeName(deviceType,Locale.getDefault());
					 List<Map<String,Object>>  listGroup = IndexTemplateUtil.getInstance().getGroupByDeviceType(deviceType);
		    		
		    			 for(int i=0;i<groupName.length;i++){
		    				 Map<String, Object>  groupMap =new HashMap<String, Object>();
		    				 for (Iterator groupIterator= listGroup.iterator(); groupIterator.hasNext();) {
				    			 Map<String, Object> map = (Map<String, Object>) groupIterator.next();
				    			 if(groupName[i].equals(map.get("groupId"))){
				    				 groupMap.put("name",deviceTypeZHName+"-->"+map.get("name").toString());
				    				 groupMap.put("value", deviceType+":"+groupName[i]);
				    				 list.add(groupMap);
				    			 }
		    				 } 
		    			 }
				}
			return list;
		}
		/**
		 * modifyPswUI 修改密码
		 * @author zhou_xiaohu
		 * @return
		 */
		@RequestMapping("modifyPswUI")
		@ResponseBody
		public Object modifyPswUI(SID sid,HttpServletRequest request){
			JSONObject json = new JSONObject();
			int minCount = 8;// 默认密码长度是6
			int minUpperCount = 0;// 默认包含大写字母最少是0
			int minLowerCount = 0;// 默认包含小写字母是0
			int minNumCount = 0;// 默认包含数字是0
			try {
				minCount = Integer.parseInt(SafeMgrConfigUtil.getInstance().getSafeMgrConfigValueByName("minCount"));
				minUpperCount = Integer.parseInt(SafeMgrConfigUtil.getInstance().getSafeMgrConfigValueByName("minUpperCount"));
				minLowerCount = Integer.parseInt(SafeMgrConfigUtil.getInstance().getSafeMgrConfigValueByName("minLowerCount"));
				minNumCount = Integer.parseInt(SafeMgrConfigUtil.getInstance().getSafeMgrConfigValueByName("minNumCount"));
			} catch (NumberFormatException e) {
				minCount = 8;
				minUpperCount = 0;
				minLowerCount = 0;
				minNumCount = 0;
				e.printStackTrace();
			}
			json.put("minCount", minCount);
			json.put("minUpperCount", minUpperCount);
			json.put("minLowerCount", minLowerCount);
			json.put("minNumCount", minNumCount);
			String userName = sid.getUserName();
			String type = "";
			if ("admin".equals(userName)) {// 账户管理员可以重置密码
				type = "1";
			}
			json.put("type", type);
			return json;
		}
		/**
		 * checkLoginName检查用户名是否已存在
		 * @author zhou_xiaohu
		 * @param loginName
		 * @return
		 */
		@RequestMapping("checkLoginName")
		@ResponseBody
		public Object checkLoginName(@RequestParam(value="loginName")String loginName){
			JSONObject json = new JSONObject();
			try {
				AuthAccount account = userService.getUserByUserName(loginName);
				if(account!=null && !"".equals(account)){
					json.put("result", "false");
				}else{
					json.put("result", "true");
				}
			} catch (Exception e) {
				e.printStackTrace();
				json.put("result", "false");
			} 
			return json;
		}
		/**
		 * checkLoginName 检查用户名是否已存在
		 * @author horizon
		 * @param userName
		 * @return
		 */
		@RequestMapping("checkUserName")
		@ResponseBody
		public Object checkUserName(@RequestParam(value="userName")String userName){
			JSONObject json = new JSONObject();
			try {
				AuthAccount account = userService.getUserByUserName(userName);
				if(account != null && !"".equals(account)) {
					json.put("ok", "");
				} else {
					json.put("error", "该用户名不存在");
				}
			} catch (Exception e) {
				e.printStackTrace();
				json.put("error", "该用户名不存在");
			} 
			return json;
		}
		
		/**
		 * checkLoginName检查用户名是否已存在
		 * @author zhou_xiaohu
		 * @param loginName
		 * @return
		 */
		@RequestMapping("checkLoginPwd")
		@ResponseBody
		public Object checkLoginPwd(@RequestParam(value="pwd") String pwd, @RequestParam(value="userId") String userId){
			JSONObject json = new JSONObject();
			try {
				AuthAccount account = userService.getUserByID(Integer.parseInt(userId));
				String oldPwd = account.getPasswd();
				String newPwd = MD5.MD5(pwd);
				if(newPwd.equals(oldPwd)) {
					json.put("result", "false");
				} else {
					json.put("result", "true");
				}
			} catch (Exception e) {
				e.printStackTrace();
				json.put("result", "false");
			}
			return json;
		}
		@RequestMapping("checkOldPassword")
		@ResponseBody
		public Object checkOldPassword(@RequestParam(value="oldPassword")String oldPassword,@RequestParam(value="userName")String userName,HttpServletRequest request){
			JSONObject json = new JSONObject();
			json.put("ok", "");
			if(StringUtil.isBlank(userName)){
				json.put("error", "请输入用户名！");
			}else{
				AuthAccount account = userService.getUserByUserName(userName);
				if(account==null){
					json.put("error", "请您输入正确的用户名！");
				}else{
					String loginIp = request.getRemoteAddr() ;
					LoginFailed failedIPInfo = LOGIN_IP_FAILED_CACHE.get(loginIp) ;
					LoginFailed failedNameInfo = LOGIN_NAME_FAILED_CACHE.get(userName);
					//地址被禁用
					if(CommonUtils.isForbidden(failedIPInfo)){
						json.put("error","IP地址已被禁用,请于"+CommonUtils.getForbidenTime(failedIPInfo, TIME_OF_FORBIDDEN)+"后重试！") ;
					}else
						//账户被禁用
					if(CommonUtils.isForbidden(failedNameInfo)){
						json.put("error", "该账户已被禁用,请于"+CommonUtils.getForbidenTime(failedNameInfo,TIME_OF_FORBIDDEN)+"后重试！") ;
					}else{
						String oldPwd =  account.getPasswd();
						oldPassword = CommonUtils.decrypt(oldPassword, "") ;
						oldPassword = MD5.MD5(oldPassword);
						if(!oldPassword.equals(oldPwd)){
							if(failedIPInfo == null){
								LOGIN_IP_FAILED_CACHE.put(loginIp, failedIPInfo = new LoginFailed(loginIp)) ;
							}
							if(failedNameInfo == null){
								LOGIN_NAME_FAILED_CACHE.put(userName, failedNameInfo = new LoginFailed(userName)) ;
							}
							failedIPInfo.increment() ;
							failedNameInfo.increment() ;
							if(CommonUtils.isForbidden(failedIPInfo)){
								json.put("error","IP地址已被禁用," + TIME_OF_FORBIDDEN + "分钟后方可重试！");
								CommonUtils.generateLog(AuditCategoryDefinition.SYS_LOGIN,"IP地址禁用","密码尝试次数已达上限，地址"+loginIp+"已被禁用！",userName, loginIp,false,Severity.HIGH);
							}else if(CommonUtils.isForbidden(failedNameInfo)){
								json.put("error","账户已被禁用," + TIME_OF_FORBIDDEN + "分钟后方可重试！");
								CommonUtils.generateLog(AuditCategoryDefinition.SYS_LOGIN,"账户禁用","密码尝试次数已达上限，地址"+userName+"已被禁用！",userName, loginIp,false,Severity.HIGH);
							}else{
								json.put("error","密码错误！");
							}
							json.put("pwdError",true);
							CommonUtils.generateLog(AuditCategoryDefinition.SYS_LOGIN,"登录","密码错误！",userName, request.getRemoteHost(),false,Severity.HIGH);
						}
					}
				}
			}
			if(!json.containsKey("error")){
				LOGIN_IP_FAILED_CACHE.remove(request.getRemoteAddr()) ;
				LOGIN_NAME_FAILED_CACHE.remove(userName) ; 
			}
			return json;
		}
		/**
		 * addUser 添加新用户
		 * @param request
		 * @param account
		 * @return
		 */
		@RequestMapping("addUser")
		@ResponseBody
		public Object addUser(SID sid, HttpServletRequest request,@RequestBody UserForm userForm){
			JSONObject json = new JSONObject();
			try {
				//用户基本信息
				if(sid.hasAdminRole()){
					
				AuthAccount account = new AuthAccount();
				account.setName(userForm.getUserName().trim());
				account.setPasswd(MD5.MD5(CommonUtils.decrypt(userForm.getPassword(),"")));
				account.setEnable(StringUtil.toInteger(userForm.getStatus(), 0)) ;
				Date expireTime = StringUtil.toDate(userForm.getExpireTime(), "yyyy-MM-dd") ;
				account.setExpireTime(BigInteger.valueOf(expireTime.getTime()));
				InetAddress startIP = Inet4Address.getByName(userForm.getMinIp());
				InetAddress endIP = Inet4Address.getByName(userForm.getMaxIp());
				account.setValidIPStart(new BigInteger(Util.ipTohl(startIP).toString()));
				account.setValidIPEnd(new BigInteger(Util.ipTohl(endIP).toString()));
				//设置列集信息
				account.setGroupId(userForm.getDeviceGroup());
				account.setDescription(userForm.getDescription());
				account.setCreateUser(sid.getUserName());
				account.setLateModifyUser(sid.getUserName());
				long nowTime = Calendar.getInstance().getTime().getTime();
				account.setLateModifyTime(BigInteger.valueOf(nowTime));
				// 设置最新更改密码的时间
				account.setMdfPwdTime(nowTime);
				//关联角色信息表
				AuthRole authRole = new AuthRole();
				authRole.setId(Integer.parseInt(userForm.getRoleid()));
				Set set = new HashSet();
				set.add(authRole);
				account.setRoles(set);
				//设置用户权限
				account.setUserDevice(userForm.getUserDevices());
				userService.addUser(account);
				// 以下产生日志信息
				CommonUtils.generateLog(AuditCategoryDefinition.SYS_ADD,"添加新用户","添加新用户名称: " + account.getName(),sid.getUserName(), request.getRemoteHost(), true, Severity.MEDIUM);
				json.put("result","success");
				}else{
					json.put("result","failure");
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				json.put("result","failure");
			} 
			return json;
		}
		
		/**
		 * modifyUserPwd 修改用户密码
		 * @param request
		 * @return
		 */
		@RequestMapping("modifyUserPwd")
		@ResponseBody
		public Object modifyUserPwd(@RequestParam Map<String, String> requestForm,HttpServletRequest request) {
			JSONObject json = new JSONObject();
			try {
				String userName = requestForm.get("userName");
				String oldPassword = CommonUtils.decrypt(requestForm.get("oldPassword"),"");
				String password = CommonUtils.decrypt(requestForm.get("password"),"");
				String passwordAgain = CommonUtils.decrypt(requestForm.get("passwordAgain"),"");
				if(StringUtil.isBlank(userName) || StringUtil.isBlank(oldPassword) || StringUtil.isBlank(password) || StringUtil.isBlank(passwordAgain)) {
					json.put("result","failure");
					json.put("msg","非法的用户名或密码");
					return json;
				}
				if(!password.equals(passwordAgain)) {
					json.put("result","failure");
					json.put("msg","密码与确认密码不一致");
					return json;
				}
				
				AuthAccount account = userService.getUserByUserName(userName);
				account.setLateModifyUser(userName);
				account.setLateModifyTime(BigInteger.valueOf(Calendar.getInstance().getTime().getTime()));
				String oldPwd = account.getPasswd();
				String loginIp = request.getRemoteAddr() ;
				LoginFailed failedIPInfo = LOGIN_IP_FAILED_CACHE.get(loginIp) ;
				LoginFailed failedNameInfo = LOGIN_NAME_FAILED_CACHE.get(userName) ;
				if(CommonUtils.isForbidden(failedIPInfo)){
					json.put("result","failure");
					json.put("msg","IP已被禁用,请于" + CommonUtils.getForbidenTime(failedIPInfo, TIME_OF_FORBIDDEN) + "后重试！");
					return json;
				}
				if(CommonUtils.isForbidden(failedNameInfo)){
					json.put("result","failure");
					json.put("msg","账户已被禁用,请于" + CommonUtils.getForbidenTime(failedIPInfo, TIME_OF_FORBIDDEN) + "后重试！");
					return json;
				}
				String oldPasswordD5 = MD5.MD5(oldPassword);
				if(!oldPwd.equals(oldPasswordD5)){
					if(failedIPInfo == null){
						failedIPInfo = new LoginFailed(loginIp) ;
						LOGIN_IP_FAILED_CACHE.put(loginIp, failedIPInfo) ;
					}
					if(failedNameInfo == null){
						failedNameInfo = new LoginFailed(userName) ;
						LOGIN_NAME_FAILED_CACHE.put(userName, failedNameInfo) ;
					}
					failedIPInfo.increment() ;
					failedNameInfo.increment();
					if(CommonUtils.isForbidden(failedIPInfo)){
						json.put("result","failure");
						json.put("msg","密码错误，IP地址已被禁用," + TIME_OF_FORBIDDEN + "分钟后方可重试！");
						return json;
					}else if(CommonUtils.isForbidden(failedNameInfo)){
						json.put("result","failure");
						json.put("msg","密码错误，账户已被禁用," + TIME_OF_FORBIDDEN + "分钟后方可重试！");
						return json;
					}else{
						json.put("result","failure");
						json.put("msg","密码错误");
						return json;
					}
				}
				String newPwd = MD5.MD5(password);
				if(newPwd.equals(oldPwd)) {
					json.put("result","failure");
					json.put("msg","新密码不能与旧密码相同，请重新设置");
					return json;
				} else {
					account.setPasswd(newPwd);
					account.setMdfPwdTime(System.currentTimeMillis());
				}

				userService.modifyInfo(account);
				// 以下产生日志信息
				CommonUtils.generateLog(AuditCategoryDefinition.SYS_UPDATE, "更改密码", "更改密码的用户名称: " + account.getName(), account.getName(), request.getRemoteHost(), true, Severity.MEDIUM);
				json.put("result","success");
			} catch (Exception e) {
				e.printStackTrace();
				json.put("result","failure");
				json.put("msg","系统内部错误！");
			}
			LOGIN_IP_FAILED_CACHE.remove(request.getRemoteAddr()) ;
			LOGIN_NAME_FAILED_CACHE.remove(requestForm.get("userName")) ; 
			return json;
		}
		/**
		 * modifyUser修改用户
		 * @param request
		 * @param account
		 * @return
		 */
		
		@RequestMapping("modifyUser")
		@ResponseBody
		public Object modifyUser(SID sid,HttpServletRequest request,@RequestBody UserForm userForm){
			JSONObject json = new JSONObject();
			try {
				AuthAccount account = userService.getUserByID(Integer.parseInt(userForm.getUserid()));
				
			   if(sid.getUserName().equals(account.getCreateUser()) || account.getCreateUser().equals("administrator") || sid.isAdmin()){
					if(userForm.getExpireTime() != null && !"".equals(userForm.getExpireTime())){
						Date expireTime = StringUtil.toDate(userForm.getExpireTime(), "yyyy-MM-dd") ;
						account.setExpireTime(BigInteger.valueOf(expireTime.getTime()));
					}
					if(userForm.getPassword()!=null && !"".equals(userForm.getPassword())){
						String oldPwd = account.getPasswd();
						String newPwd = MD5.MD5(CommonUtils.decrypt(userForm.getPassword(),""));
						if(newPwd.equals(oldPwd)) {
							json.put("result","failure");
							json.put("msg","新密码不能与旧密码相同，请重新设置");
							return json;
						} else {
							account.setPasswd(newPwd);
							long nowTime = Calendar.getInstance().getTime().getTime();
							account.setMdfPwdTime(nowTime);
						}
					}
					if(userForm.getStatus()!=null && !"".equals(userForm.getStatus())){
						account.setEnable(Integer.parseInt(userForm.getStatus()));
					}else{
						account.setEnable(0);
					}
					
					InetAddress startIP = Inet4Address.getByName(userForm.getMinIp());
					InetAddress endIP = Inet4Address.getByName(userForm.getMaxIp());
					account.setValidIPStart(new BigInteger(Util.ipTohl(startIP).toString()));
					account.setValidIPEnd(new BigInteger(Util.ipTohl(endIP).toString()));
					account.setDescription(userForm.getDescription());
					account.setLateModifyUser(sid.getUserName());
					account.setLateModifyTime(BigInteger.valueOf(Calendar.getInstance().getTime().getTime()));
					//设置用户权限
					account.setUserDevice(userForm.getUserDevices());
					//设置用户日志源列集
	//				account.setGroupId("OS/Microsoft/WindowsEventLog:1#2;");
					account.setGroupId(userForm.getDeviceGroup());
					userService.modifyInfo(account);
					// 以下产生日志信息
					CommonUtils.generateLog(AuditCategoryDefinition.SYS_UPDATE,"修改用户","修改的用户名称: " + account.getName(),sid.getUserName(), request.getRemoteHost(), true, Severity.MEDIUM);
					json.put("result","success");
			  }else{
				  json.put("result","failure");
			  }
			
			} catch (Exception e) {
				e.printStackTrace();
				json.put("result","failure");
			} 
			return json;
		}
		
		/**
		 * deleteUser 删除用户及批量刪除用戶
		 * @param request
		 * @return
		 */
		@RequestMapping("deleteUser")
		@ResponseBody
		public Object deleteUser(SID sid,HttpServletRequest request){
			JSONObject json = new JSONObject();
			
			try {
				String id = request.getParameter("id");
				String[] ids = id.split(",");
				for (int i = 0; i < ids.length; i++) {
					AuthAccount account = userService.getUserByID(StringUtil.toInt(ids[i]));
						userService.deleteUserRelateInfo(account.getName());
						//清除缓存中跟用户相关的资产
						clearAssetFacade(account.getName());
						//删除对应的计划报表
						this.eventResponseService.deleteResponsesByCreater(account.getName());
						// 以下产生日志信息
						CommonUtils.generateLog(AuditCategoryDefinition.SYS_DELETE,"删除用户","删除用户ID: " + ids[i],sid.getUserName(), request.getRemoteHost(), true, Severity.HIGH);
						userService.delUserByID(Integer.parseInt(ids[i]));
						json.put("result","success");
				}
			} catch (Exception e) {
				e.printStackTrace();
				json.put("result","failure");
			} 
			return json;
		}
		
		@RequestMapping("changeDefaultTopo")
		@ResponseBody
		public Object changeDefaultTopo(@RequestParam("defaultTopoId")Integer defaultTopoId,SID sid) {
			Result result = new Result() ;
			userService.changeDefaultTopo(sid.getAccountID(), defaultTopoId) ;
			sid.setDefaultTopoId(defaultTopoId) ;
			result.buildSuccess(null) ;
			return result ;
		}
		/**
		 * 清除缓存中的资产
		 * @param userName
		 */
		public void clearAssetFacade(String userName){
			List<AssetObject> assetList=AssetFacade.getInstance().getAll();
			for (AssetObject assetObject:assetList) {
				String creator = assetObject.getCreator() ;
				if(creator != null && creator.equalsIgnoreCase(userName)){
					//assetObject.setCreator(null);
				}
			}
		}

  /**
   * 根据设备类型，获取列集
   * @param request
   * @param deviceType
   * @return
   */
  @RequestMapping("getGroupListByDeviceType")
  @ResponseBody
  public Object getGroupListByDeviceType(@RequestParam(value="deviceType",defaultValue="")String deviceType){
	  List<TreeModel> listModel = new ArrayList<TreeModel>();
	  String[] deviceArr=deviceType.split(",");
	    for(int  i=0;i<deviceArr.length;i++){
	    	 List<SimDatasource> assetDataSources = dataSourceService.getByIp(deviceArr[i]);
	    	 for(SimDatasource datasource : assetDataSources){
		    		 TreeModel deviceTypeModel = new TreeModel();
		    		 Map<String, Object> attributes=new HashMap<String, Object>();
		    		 attributes.put("deviceIp",datasource.getDeviceIp());
		    		 deviceTypeModel.setAttributes(attributes);
		    		 deviceTypeModel.setId(datasource.getSecurityObjectType());
		    		 deviceTypeModel.setText(DeviceTypeNameUtil.getDeviceTypeName(datasource.getSecurityObjectType(),Locale.getDefault()));
		    		 List<Map<String,Object>>  listGroup = IndexTemplateUtil.getInstance().getGroupByDeviceType(datasource.getSecurityObjectType());
		    		 for (Iterator groupIterator= listGroup.iterator(); groupIterator.hasNext();) {
						 Map<String, Object> map = (Map<String, Object>) groupIterator.next();
						 TreeModel groupModel = new TreeModel();
						 groupModel.setId(map.get("groupId").toString());
						 groupModel.setText(map.get("name").toString());
						 deviceTypeModel.addChild(groupModel);
					}
		    		 listModel.add(deviceTypeModel);
	    	 }
	    }
	  return listModel;
  }
  /**
   * 保存用户列配置
   * @return
   */
  @RequestMapping("saveColumnConfig")
  @ResponseBody
  public Object saveColumnConfig(SID sid,HttpServletRequest request,
		  						 @RequestParam("module")String module,
		  						 @RequestParam("config")String config){
	  Result result = new Result() ;
	  UserColumnConfig columnConfig = new UserColumnConfig(sid.getAccountID(), module, config) ;
	  try{
		  userService.saveColumnConfig(columnConfig) ;
		  result.buildSuccess(null);
	  }catch(Exception e){
		  result.buildError("列配置信息保存失败！") ;
	  }
	  return result ;
  }
  
}
