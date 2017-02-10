package com.topsec.tsm.sim.common.web;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tsm.sim.auth.security.Attack;
import com.topsec.tsm.sim.auth.security.AttackChecker;
import com.topsec.tsm.sim.auth.security.AttackException;
import com.topsec.tsm.sim.auth.security.SecurityFilter;

/**
 * SecurityModelAttribute解析器<br>
 * 此解析器从ModelAttribute继承而来，在此基础上增加了安全检测功能
 * @author hp
 *
 */
public class SecurityModelAttributeResolver extends ServletModelAttributeMethodProcessor implements CustomArgumentResolver {
	
	private Map<Class<? extends AttackChecker>,AttackChecker> checkerMap = new HashMap<Class<? extends AttackChecker>, AttackChecker>() ; 
	
	public SecurityModelAttributeResolver() {
		super(true) ;
	}
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(SecurityModelAttribute.class);
	}
	
	@Override
	protected void validateIfApplicable(WebDataBinder binder, MethodParameter parameter) {
		super.validateIfApplicable(binder, parameter) ;
		if(!SecurityFilter.ENABLED){
			return ;
		}
		Object data = binder.getTarget() ;
		SecurityModelAttribute annotation = parameter.getParameterAnnotation(SecurityModelAttribute.class) ;
		Class<? extends AttackChecker> checkerClass = annotation.value() ;
		AttackChecker checker = checkerMap.get(checkerClass) ;
		if(checker == null){
			synchronized (checkerMap) {
				try {
					checkerMap.put(checkerClass, (checker = checkerClass.newInstance())) ;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		Set<String> uncheckProperties = new HashSet<String>() ; 
		if(ObjectUtils.isNotEmpty(annotation.uncheck())){
			uncheckProperties.addAll(Arrays.asList(annotation.uncheck())) ;
		}
		Attack attack = checker.findAttack(data,uncheckProperties,annotation.allows()) ;
		if(attack != null){
			throw new AttackException(attack) ;
		}
	}

}
