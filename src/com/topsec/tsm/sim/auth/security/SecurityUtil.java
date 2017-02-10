package com.topsec.tsm.sim.auth.security;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tal.base.util.PropertyManager;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.base.audit.AuditRecord;
import com.topsec.tsm.common.SIMConstant;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.util.AuditLogFacade;
import com.topsec.tsm.util.StringFormater;

/**
 * 安全检测工具类
 * @author hp
 *
 */
public final class SecurityUtil {
	
	private static Logger logger = LoggerFactory.getLogger(SecurityUtil.class) ;
	private static String[] illegalContents ;
	private static String[] sqlInjectionKeys ;
	private static char[] ILLEGAL_CHARACTER = new char[]{'\'','"','<','>','&','#',',','%'} ;
	static{
		illegalContents = StringUtil.split(PropertyManager.getResource(SIMConstant.SYS_PROPERTIE_PATH).getProperty("illegal.content")) ;
		sqlInjectionKeys = StringUtil.split(PropertyManager.getResource(SIMConstant.SYS_PROPERTIE_PATH).getProperty("sql.injection")) ;
	}
	
	/**
	 * 判断Map中是否包含非法的内容
	 * @param data
	 * @return
	 */
	public static Attack findAttack(Map<?,?> data,Set<String> uncheckeProperites,String[] allows){
		if(data == null || data.isEmpty()){
			return null ;
		}
		Attack illegalString = null;
		for(Map.Entry<?, ?> entry:data.entrySet()){
			Object key = entry.getKey() ;
			Object value = entry.getValue() ;
			if(uncheckeProperites.contains(key) || ArrayUtils.contains(allows, value)){
				continue ;
			}
			if(value instanceof String){
				illegalString = findAttack((String)value) ;
			}else if(value instanceof Map){
				illegalString = findAttack((Map<?,?>)value,allows) ;
			}else if(value != null && value.getClass().isArray()){
				if(value.getClass() == String[].class){
					illegalString = findAttack((String[])value,allows) ;
				}else{
					illegalString = findAttack((Object[])value,allows) ;
				}
			}
			if(illegalString != null){
				return illegalString ;
			}
		}
		return null ;
	}
	/**
	 * 判断Map中是否包含非法的内容
	 * @param data
	 * @return
	 */
	public static Attack findAttack(Map<?,?> data,String[] allows){
		return findAttack(data, Collections.<String>emptySet(), allows) ;
	}
	
	public static Attack findAttack(Object[] input,String[] allows){
		for(Object obj:input){
			if(!(obj instanceof String)){
				continue ;
			}
			if(ArrayUtils.contains(allows, obj)){
				continue ;
			}
			Attack attack = findAttack((String)obj) ;
			if(attack != null){
				return attack ;
			}
		}
		return null ;
	}
	
	public static Attack findAttack(String[] input,String[] allows){
		for(String str:input){
			if(ArrayUtils.contains(allows, str)){
				continue ;
			}
			Attack illegalString = findAttack(str) ;
			if(illegalString != null){
				return illegalString ;
			}
		}
		return null ;
	}
	
	public static Attack findAttack(String input){
		if(input == null)
			return null ;
		String str = input.toUpperCase().replace(" ", "") ;
		for(String illegalString:illegalContents){
			int index = str.indexOf(illegalString);
			if(index > -1){
				return new Attack(AttackType.XSS,input) ;
			}
		}
		for(String key:sqlInjectionKeys){
			int index = str.indexOf(key);
			if(index > -1){
				return new Attack(AttackType.SQL_INJECTION,input) ;
			}
		}
		return null ;
	}
	
	public static void goBackHome(HttpServletRequest request, HttpServletResponse response, AttackType type)throws IOException{
		goBackHome(request, response, type, null) ;
	}
	
	public static void goBackHome(HttpServletRequest request, HttpServletResponse response, AttackType type,String illegallContent) throws IOException {
		String message = null;
		switch (type) {
			case XSS:
				message = "提交内容包含脚本语言或跨站攻击脚本!可疑内容:"+StringEscapeUtils.escapeHtml(illegallContent); break;
			case CROSS_SCRIPT:
				message = "跨站点请求伪造攻击,禁止访问!"; break;
			case SQL_INJECTION:
				message = "SQL注入攻击!可疑内容:" + StringEscapeUtils.escapeHtml(illegallContent); break;
			case WATCH_DIRECTORY:
				message = "目录猜测攻击!" ;break ;	
			case ERROR:
				message = "异常请求!"; break;
		}
		SID sid = (SID)request.getSession().getAttribute(SIMConstant.SESSION_SID) ;
		String user = sid == null ? "N/A" : sid.getUserName() ;
		String log = StringFormater.format("主机:{},用户:{},在访问{}时包含可疑攻击,攻击类型:{},攻击内容:{}", request.getRemoteHost(),user,request.getRequestURL().toString(),type.name(),illegallContent);
		logger.warn(log) ;
		AuditRecord record = AuditLogFacade.createSystemAuditLog();
		record.sysFailNotify("安全检测", log) ;
		AuditLogFacade.send(record) ;
		String accept = request.getHeader("Accept") ;
		if(accept != null && accept.contains("application/json")){
			response.setContentType("text/html;charset=utf-8");
			response.getWriter().print(message.replaceAll("[\r]?\n", ""));
			response.setStatus(555) ;
		}else{
			response.setStatus(555) ;
			String script = "<script language='javascript'>alert(\"" + message.replaceAll("[\r\n]", "") + "\");window.location='" + SecurityFilter.FORWARD_PATH + "';</script>" ;
			response.setContentType("text/html;charset=utf-8");
			response.getWriter().print(script);
		}
		response.flushBuffer();
	}

	public static Cookie getCookie(HttpServletRequest httpServletRequest, String name) {
		Cookie ck = null;
		Cookie[] cks = httpServletRequest.getCookies();
		if (cks != null) {
			for (int i = 0; i < cks.length; i++) {
				if (cks[i].getName() != null && cks[i].getName().equals(name)) {
					ck = cks[i];
					break;
				}
			}
		}
		return ck;
	}
	/**
	 * 判断字符串中是否包含非法字符<br>
	 * @param str
	 * @return
	 */
	public static boolean hasIllegalChar(String str){
		return StringUtils.containsAny(str, ILLEGAL_CHARACTER) ;
	}
}
