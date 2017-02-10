package com.topsec.tsm.sim.common.web;

import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.topsec.tal.base.util.StringUtil;

/**
 * Controller方法请求参数JSONArgument类型解析器
 * @author hp
 *
 */
public class JSONArgumentResolver extends MappingJackson2HttpMessageConverter implements HandlerMethodArgumentResolver,CustomArgumentResolver{

	public JSONArgumentResolver(){
		ObjectMapper mapper = getObjectMapper() ;
		mapper.setDateFormat(new SimpleDateFormat(StringUtil.LONG_DATE)) ;
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) ;
	}
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(JSONArgument.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter,
			ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
			WebDataBinderFactory binderFactory) throws Exception {
		Class<?> instanceClass = parameter.getParameterType() ;
		HttpInputMessage message = new ServletServerHttpRequest((HttpServletRequest)webRequest.getNativeRequest()) ;
		Object obj = readInternal(instanceClass, message) ;
		return obj;
	}
}
