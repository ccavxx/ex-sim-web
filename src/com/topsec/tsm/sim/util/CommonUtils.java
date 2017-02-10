package com.topsec.tsm.sim.util;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.topsec.tal.base.util.EnhanceProperties;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.Parameter;
import com.topsec.tal.base.util.PropertyManager;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.auth.manage.LoginFailed;
import com.topsec.tsm.base.audit.AuditRecord;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.base.type.Severity;
import com.topsec.tsm.common.SIMConstant;
import com.topsec.tsm.node.collector.DataConstants;
import com.topsec.tsm.resource.AuditCategoryDefinition;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.auth.util.SafeMgrConfigUtil;
import com.topsec.tsm.sim.common.exception.TimeExpressionException;
import com.topsec.tsm.sim.response.persistence.TimeExpression;
import com.topsec.tsm.util.encrypt.RSAUtil;

public class CommonUtils {
	/**
	 * 正则表达式--中文
	 */
	public static final String REGEX_ZH = "[\u4e00-\u9fa5]+";
	public static final String COMPANY_PROPERTY_PATH = "resource/company.properties" ;
	private static final String DEFAULT_COMPANY_LOGO = "/img/skin/top/logo.png" ;
	private static final String DEFAULT_COMPANY_NAME = "北京天融信公司" ;
	private static final String DEFAULT_PRODUCT_NAME = "天融信安全管理系统" ;
	//账户或者ip被锁定的最长分钟数
	private static final long TIME_OF_FORBIDDEN = 3L;
	/**
	 * 根据优先级返回级别信息<br>
	 * 事件级别和告警级别均可使用此方法<br>
	 * @param priority 优先级
	 * @return 
	 */
	public static String getLevel(int priority) {
		String level = DataConstants.PRIORITY_UNKNOWN;
		switch (priority) {
		case 0:
			level = DataConstants.PRIORITY_ZERO;
			break;
		case 1:
			level = DataConstants.PRIORITY_ONE;
			break;
		case 2:
			level = DataConstants.PRIORITY_TWO;
			break;
		case 3:
			level = DataConstants.PRIORITY_THREE;
			break;
		case 4:
			level = DataConstants.PRIORITY_FOUR;
			break;
		}
		return level;
	}
	/**
	 * 将数字级别(0,1,2,3,4)翻译成中文名称
	 * @param priority
	 * @return
	 */
	public static String getLevel(Object priority){
		String pr = priority == null ? "-1" : priority.toString() ;
		return getLevel(StringUtil.toInt(pr, -1)) ;
	}
	/**
	 * 计算磁盘大小
	 * @param size
	 * @return
	 */
	public static String formatterDiskSize(long size) {
		return formatBytes(size) ;
	}
	/**
	 * 格式化bytes为KB,MB,GB,TB
	 * @param bytes
	 * @return
	 */
	public static String formatBytes(long bytes){
		return formatBytes(bytes, 0) ;
	}
	
	public static String formatBytes(long value,int scale){
		double bytes = new Long(value).doubleValue() ;
		String formatString ;
		if(scale <= 0){
			formatString = "#" ;
		}else{
			StringBuffer sb = new StringBuffer("#.") ;
			while(scale-- > 0){
				sb.append("0") ;
			}
			formatString = sb.toString() ;
		}
		DecimalFormat format = new DecimalFormat(formatString);
		if (bytes < 1024) {
			return bytes + "Byte";
		} else if (bytes / 1024 < 1024) {
			return format.format(bytes / 1024) + "KB";
		} else if (bytes / (1024 * 1024) < 1024) {
			return format.format(bytes / 1024 / 1024) + "MB";
		} else if (bytes / (1024 * 1024 * 1024) < 1024) {
			return format.format(bytes / 1024 / 1024 / 1024) + "GB";
		} else {
			return format.format(bytes / 1024 / 1024 / 1024 / 1024) + "TB";
		}
	}
	/**
	 * 格式化count单位为万/亿
	 */
	public static String formatCount(long count){
		return formatCount(count, 0) ;
	}
	
	public static String formatCount(long value,int scale){
		double count = new Long(value).doubleValue() ;
		 NumberFormat nf = NumberFormat.getNumberInstance();  
         nf.setMaximumFractionDigits(scale);
		if(count > 100000000){
			return nf.format(count/100000000) + "亿";
		}else if(count > 10000){
			return nf.format(count/10000) + "万";
		}else{
			return nf.format(count)+"";
		}
	}

	public static TimeExpression createTimeExpression(Parameter params) throws TimeExpressionException{
		String timerType = params.getValue("timerType") ;
		TimeExpression timeExpression = new TimeExpression();
		try {
			timeExpression.setType(timerType);
			if (timerType.equals(TimeExpression.TYPE_EVERY_YEAR)) {
				timeExpression.setEveryYear(params.getInt("month"), params.getInt("date"), params.getInt("hour"), params.getInt("min"), 0);
			}else if (timerType.equals(TimeExpression.TYPE_EVERY_MONTH)) {
				timeExpression.setEveryMonth(params.getInt("date"), params.getInt("hour"), params.getInt("min"), 0);
			}else if (timerType.equals(TimeExpression.TYPE_EVERY_WEEK)) {
				timeExpression.setEveryWeek(params.getInt("day"), params.getInt("hour"),params.getInt("min"), 0);
			}else if (timerType.equals(TimeExpression.TYPE_EVERY_DAY)) {
				timeExpression.setEveryDay(params.getInt("hour"), params.getInt("min"),0);
			}else if (timerType.equals(TimeExpression.TYPE_INTERVAL_HOUR)) {
				timeExpression.setIntervalHour(params.getInt("hour"));
			}else if (timerType.equals(TimeExpression.TYPE_INTERVAL_MINUTE)) {
				timeExpression.setIntervalMinute(params.getInt("min"));
			}
		} catch (RuntimeException e) {
			throw new TimeExpressionException("") ;
		}
		return timeExpression ;
	}
    public static TimeExpression createTimeExpression(HttpServletRequest request) throws TimeExpressionException{
    	return createTimeExpression(new Parameter(request.getParameterMap())) ;
    }
    public static String getTimeExpressionText(String timeType){
    	String text="";
		if(StringUtil.isNotBlank(timeType)){
			if(timeType.equals(TimeExpression.TYPE_EVERY_DAY)){
		    	   text = "每日";
		       }else if(timeType.equals(TimeExpression.TYPE_EVERY_MONTH)){
		    	   text = "每月";
		       }else if(timeType.equals(TimeExpression.TYPE_EVERY_WEEK)){
		    	   text ="每周";
		       }else if(timeType.equals(TimeExpression.TYPE_EVERY_YEAR)){
		    	   text = "每年";
		       }else if(timeType.equals(TimeExpression.TYPE_INTERVAL_HOUR)){
		    	   text = "小时间隔";
		       }else if(timeType.equals(TimeExpression.TYPE_INTERVAL_MINUTE)){
		    	   text = "分钟间隔";
		       }
		}
		return text;
    }
    
    /**
     * 判断是否是软件平台
     * @return
     */
    public static boolean isSoftwarePlatform(){
		@SuppressWarnings("rawtypes")
		Map licenseInfo = LicenceServiceUtil.getInstance().getLicenseInfo();
		String talVersion=(String)licenseInfo.get("TAL_VERSION");//版本信息
		return "TAEX-S".equalsIgnoreCase(talVersion)||"TAEX_S".equalsIgnoreCase(talVersion) ;
    }
    /**
     * 判断是否是硬件平台
     * @return
     */
    public static boolean isHardwarePlatform(){
    	return !isSoftwarePlatform() ;
    }
    
	/**
	 * 解决https协议时ie8无法下载文件的bug
	 * @param response
	 */
	public static void setHeaders4Download(HttpServletResponse response){
		response.setHeader("Expires", "0");
		response.setHeader("Pragma","public");
		response.setHeader("Cache-Control", "must-revalidate,post-check=0, pre-check=0");
		response.setHeader("Cache-Control","public");	
	}
	/**
	 * 设置下载文件头
	 * @param request
	 * @param response
	 * @param fileName
	 */
	public static void setDownloadHeaders(HttpServletRequest request,HttpServletResponse response,String fileName){
		try {
			setHeaders4Download(response) ;
			fileName = java.net.URLEncoder.encode(fileName, "UTF-8");
			String userAgent = request.getHeader("User-Agent") ;
			if(userAgent.indexOf("Firefox")>0){
				response.setHeader("Content-Disposition", "attachment; filename*=\"utf8' '" + fileName + "\"");
			}else{
				response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
			}
		} catch (UnsupportedEncodingException e) {}
	}
    
    public static void resetDefaultCompanyInfo(){
    	setCompanyInfo(DEFAULT_COMPANY_LOGO, DEFAULT_COMPANY_NAME,DEFAULT_PRODUCT_NAME) ;
    }
    public static String getProductName(){
    	EnhanceProperties pt = PropertyManager.getResource(COMPANY_PROPERTY_PATH,true) ;
    	return pt.getProperty("productName", DEFAULT_PRODUCT_NAME) ;
    }
    public static String getCompanyLogo(){
    	EnhanceProperties pt = PropertyManager.getResource(COMPANY_PROPERTY_PATH,true) ;
    	return pt.getProperty("companyLogo", DEFAULT_COMPANY_LOGO) ;
    }
    
    public static String getCompanyName(){
    	EnhanceProperties pt = PropertyManager.getResource(COMPANY_PROPERTY_PATH,true) ;
    	return pt.getProperty("companyName", DEFAULT_COMPANY_NAME) ;
    }
    
    public static void setCompanyInfo(String logo,String companyName,String productName){
    	EnhanceProperties pt = PropertyManager.getResource(COMPANY_PROPERTY_PATH,true) ;
    	pt.setProperty("companyLogo", logo) ;
    	pt.setProperty("companyName", companyName) ;
    	pt.setProperty("productName", productName) ;
    	pt.store();
    }
    /**
     * 使用通用加密密钥进行解密，解密失败返回null
     * @param text
     * @return
     */
    public static String decrypt(String text){
    	return RSAUtil.decryptUseCached(SIMConstant.COMMON_KEY_PAIR_CACHE_ID,text) ;
    }
    /**
     * 使用通用加密密钥进行解密，解密失败返回指定的默认值
     * @param text
     * @return
     */
    public static String decrypt(String text,String defaultValue){
    	return StringUtil.nvl(RSAUtil.decryptUseCached(SIMConstant.COMMON_KEY_PAIR_CACHE_ID,text),defaultValue) ;
    }
    /**
     * 根据sid中保存的客户端公钥信息加密字符串，如果sid中公钥为null则返回null
     * @param sid
     * @return
     */
    public static String encrypt(SID sid,String text){
    	if(sid.getClientPublicKey() == null){
    		return text ;
    	}
    	return RSAUtil.encryptString(sid.getClientPublicKey(), text) ;
    }
    
	/**
	 * 是否禁止访问
	 * @param account
	 * @return
	 */
	public static boolean isForbidden(LoginFailed failedInfo){
		if(failedInfo == null){
			return false ;
		}
		long lastLoginTime = failedInfo.getLastLoginTime() ;
		long mills = System.currentTimeMillis() - lastLoginTime ;
		boolean forbiddenTimeOut = mills > TIME_OF_FORBIDDEN*60*1000 ;	
		int allowFailedCount = StringUtil.toInt(SafeMgrConfigUtil.getInstance().getValue("failCount"),Integer.MAX_VALUE) ;
		int failedCount = failedInfo.getFailedTimes();
		if(forbiddenTimeOut){//IP禁用超时
			failedInfo.setFailedTimes(0) ;
		}
		return failedCount >= allowFailedCount && !forbiddenTimeOut ;
	}
	/**
	 * 获取客户端被禁用时间
	 * @param failedInfo
	 * @return
	 */
	public static String getForbidenTime(LoginFailed failedInfo,long min){
		long lastLoginTime = failedInfo.getLastLoginTime();
		long mills = System.currentTimeMillis() - lastLoginTime ;
		Date date = new Date(min*60*1000-mills); 
		SimpleDateFormat aFormat = new SimpleDateFormat("mm:ss"); 
		return aFormat.format(date) ;
	}
	/**
	 * 增加登录失败次数
	 * @param loginName
	 * @param loginIp
	 * @param failedInfo
	 * @param request
	 */
	public static void incrementFailTimes(String loginName,String loginIp,LoginFailed failedIPInfo,LoginFailed failedNameInfo,Map<String,LoginFailed> LOGIN_IP_FAILED_CACHE,Map<String,LoginFailed> LOGIN_NAME_FAILED_CACHE,HttpServletRequest request){
		if(failedIPInfo == null){
			failedIPInfo = new LoginFailed(loginIp) ;
			LOGIN_IP_FAILED_CACHE.put(loginIp, failedIPInfo) ;
		}
		if(failedNameInfo == null){
			failedNameInfo = new LoginFailed(loginName) ;
			LOGIN_NAME_FAILED_CACHE.put(loginName, failedNameInfo) ;
		}
		failedIPInfo.increment() ;
		failedNameInfo.increment() ;
		if(isForbidden(failedIPInfo)){
			request.setAttribute("error","IP地址已被禁用," + TIME_OF_FORBIDDEN + "分钟后方可重试！");
			generateLog(AuditCategoryDefinition.SYS_LOGIN,"IP地址禁用","密码尝试次数已达上限，地址"+loginIp+"已被禁用！",loginName, loginIp,false,Severity.HIGH);
		}else if(isForbidden(failedNameInfo)){
			request.setAttribute("error","账户已被禁用," + TIME_OF_FORBIDDEN + "分钟后方可重试！");
			generateLog(AuditCategoryDefinition.SYS_LOGIN,"账户禁用","密码尝试次数已达上限，地址"+loginName+"已被禁用！",loginName, loginIp,false,Severity.HIGH);
		}else{
			request.setAttribute("error","密码错误！");
		}
		request.setAttribute("pwdError",true);
	}
	public static void generateLog(String cat, String name, String desc, String subject, String remoteAddress, boolean result, Severity severity) {
		AuditRecord _log = AuditLogFacade.createConfigAuditLog();
		_log.setBehavior(cat);
		_log.setSecurityObjectName(name);
		_log.setDescription(desc);
		_log.setSubject((subject==null)?"unknown":subject);
		_log.setSubjectAddress(new IpAddress(remoteAddress));
		_log.setObjectAddress(IpAddress.IPV4_LOCALHOST);
		_log.setSuccess(result);
		_log.setSeverity(severity);
		AuditLogFacade.send(_log);// 发送系统自审计日志
	}
	
	/**
	 * 创建关系树<br>
	 * 数据结构包含:{categoires:[],nodes:[],links:[]}<br>
	 * categories:节点分类，包含了所有的节点分类<br>
	 * nodes:节点列表，节点数据结构：name表示节点id，text表示节点名称，category表示节点所属类型(索引下标)<br>
	 * links:节点关系列表，关系数据结构:source表示源，target表示目的<br>
	 * 实例:<br>
	 * [
	 *  {src:192.168.75.80,dest:192.168.75.81,dest_port:80}<br>
	 *  {src:192.168.75.80,dest:192.168.75.81,dest_port:90},<br>
	 *  {src:192.168.75.100,dest:192.168.75.111,dest_port:91}<br>
	 *]
	 * 最终数据结构:<br>
	 * {
	 *   categories:[80,90,91],//所有dest_port去重结果<br>
	 *   nodes:[
	 *          {text:192.168.75.80,category:0},//src字段生成数据<br>
	 *          {text:192.168.75.81,category:0},//dest字段生成数据<br>
	 *          {text:192.168.75.80,category:1},//src字段生成数据<br>
	 *          {text:192.168.75.81,category:1},//dest字段生成数据<br>
	 *          {text:192.168.75.100,category:2},//src字段生成数据<br>
	 *          {text:192.168.75.111,category:2}<br>
	 *         ],<br>
	 *   links:[//source和target都表示nodes节点中的索引下标<br>
	 *          {source:0,target:1},<br>
	 *          {source:2,target:3},<br>
	 *          {source:4,target:5},<br>
	 *         ]<br>
	 * }<br>
	 * @param fromField 源字段
	 * @param toField 目的字段
	 * @param categoryField 分类字段
	 * @param countField 计数字段
	 * @param datas 数据
	 * @return 包含categories,nodes,links的数据结构
	 */
	public static Map<String,Object> buildRelationTree(String fromField,String toField,String categoryField,String countField,List<Map<String,Object>> datas){
		if(ObjectUtils.isEmpty(datas)){
			return new HashMap<String,Object>(0,1.0F) ;
		}
		List<String> categorys = new ArrayList<String>() ;
		List<Map<String,Object>> nodes = new ArrayList<Map<String,Object>>() ;
		List<Map<String,Object>> links = new ArrayList<Map<String,Object>>() ;
		for(Map<String,Object> record:datas){
			String cat = StringUtil.toString(record.get(categoryField)) ;
			int categoryIndex = categorys.indexOf(cat) ;
			if(categoryIndex == -1){
				categorys.add(cat) ;
				categoryIndex = categorys.size() - 1;
			}
			String fromNodeName = StringUtil.toString(record.get(fromField)) ;
			Map<String,Object> fromNode = findOrCreateNode(nodes, fromNodeName, categoryIndex) ;
			String toNodeName = StringUtil.toString(record.get(toField)) ;
			Map<String,Object> toNode = findOrCreateNode(nodes, toNodeName, categoryIndex) ;
			Number value = (Number) toNode.get("value")  ;
			Number count = (Number) record.get(countField) ;
			toNode.put("value", value == null ? count : value.longValue() + count.longValue() ) ;
			Map<String,Object> link = new HashMap<String, Object>(2,1.0F) ;
			link.put("source", fromNode.get("index")) ;
			link.put("target", toNode.get("index")) ;
			link.put("count", count) ;
			links.add(link) ;
		}
		Map<String,Object> data = new HashMap<String, Object>() ;
		data.put("categories", categorys) ;
		data.put("nodes", nodes) ;
		data.put("links", links) ;
		return data;
	}
	/**
	 * 根据节点名称和分类查找节点，如果不存在则创建一个新的节点
	 * @param nodes
	 * @param name
	 * @param category
	 * @return
	 */
	private static Map<String,Object> findOrCreateNode(List<Map<String,Object>> nodes,String name,int category){
		Map<String,Object> node = null;
		for(int i=0;i<nodes.size();i++){
			Map<String,Object> currentNode = nodes.get(i) ;
			if(currentNode.get("text").equals(name) && currentNode.get("category").equals(category)){
				node = currentNode ;
				break ;
			}
		}
		if(node == null){
			node = new HashMap<String, Object>(4,1.0F) ;
			//此处的name无实际意义
			//如果此属性引用name值，在渲染时，如果源节点和目的节点的name都相同会出现节点重合的现象
			node.put("name", String.valueOf(nodes.size())) ;
			node.put("text", name) ;
			node.put("category", category) ;
			node.put("index", nodes.size()) ;//此处增加一个冗余字段是为了上层代码更容易处理
			nodes.add(node) ;
		}
		return node ;
	}
	
	
}

