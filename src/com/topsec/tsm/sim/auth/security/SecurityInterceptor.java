package com.topsec.tsm.sim.auth.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.topsec.tsm.sim.common.web.IgnoreSecurityCheck;
import com.topsec.tsm.sim.common.web.NotCheck;

public class SecurityInterceptor extends HandlerInterceptorAdapter{
	
	@Override
	public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
		if(SecurityFilter.ENABLED){
			HandlerMethod method = (HandlerMethod)handler ;
			IgnoreSecurityCheck ignore = method.getMethodAnnotation(IgnoreSecurityCheck.class) ; 
			if(ignore != null){
				return super.preHandle(req, resp, handler) ;
			}
			NotCheck notCheck = method.getMethodAnnotation(NotCheck.class) ;
			Set<String> notCheckParameters = notCheck == null ? Collections.<String>emptySet() : new HashSet<String>(Arrays.asList(notCheck.properties())) ;
			String[] allows = notCheck == null ? new String[0] : notCheck.allows() ;
			Map<String,String[]> params = req.getParameterMap();
			for(Map.Entry<String, String[]> entry:params.entrySet()) {
				if(notCheckParameters.contains(entry.getKey())){
					continue ;
				}
				Attack attack = SecurityUtil.findAttack(entry.getValue(),allows);
				if(attack != null){
					throw new AttackException(attack);
				}
			}
		}
		return super.preHandle(req, resp, handler);
	}

	
}
