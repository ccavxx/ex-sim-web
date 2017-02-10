package com.topsec.tsm.sim.common.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

public class MyPostProcessor implements BeanPostProcessor {
	private boolean argumentResolverOrderChanged ; 
	@Override
	public Object postProcessAfterInitialization(Object arg0, String arg1) throws BeansException {
		if(argumentResolverOrderChanged){
			return arg0 ;
		}
		if(arg0 instanceof RequestMappingHandlerAdapter){
			RequestMappingHandlerAdapter adapter = (RequestMappingHandlerAdapter)arg0 ;
			List<HandlerMethodArgumentResolver> customArgumentResolvers = new ArrayList<HandlerMethodArgumentResolver>() ;
			List<HandlerMethodArgumentResolver> defaultArugmentReolvers = new ArrayList<HandlerMethodArgumentResolver>() ;
			//将自定义的参数解析器放置在系统内置的参数解析器之前
			for(HandlerMethodArgumentResolver resolver:adapter.getArgumentResolvers().getResolvers()){
				if(resolver instanceof CustomArgumentResolver){
					customArgumentResolvers.add(resolver) ;
				}else{
					defaultArugmentReolvers.add(resolver) ;
				}
			}
			customArgumentResolvers.addAll(defaultArugmentReolvers) ;//将系统内置的参数解析器追加到自定义解析器后面
			adapter.setArgumentResolvers(customArgumentResolvers) ;
			argumentResolverOrderChanged = true ;
		}
		return arg0;
	}

	@Override
	public Object postProcessBeforeInitialization(Object arg0, String arg1) throws BeansException {
		return arg0;
	}

}
