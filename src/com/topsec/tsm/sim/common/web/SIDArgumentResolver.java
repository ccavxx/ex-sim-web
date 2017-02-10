package com.topsec.tsm.sim.common.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.topsec.tsm.common.SIMConstant;
import com.topsec.tsm.sim.auth.util.SID;

/**
 * Controller方法请求参数SID类型解析器
 * @author hp
 *
 */
public class SIDArgumentResolver implements HandlerMethodArgumentResolver,CustomArgumentResolver{

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterType() == SID.class;
	}

	@Override
	public Object resolveArgument(MethodParameter parameter,
			ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
			WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		return request.getSession().getAttribute(SIMConstant.SESSION_SID);
	}

}
