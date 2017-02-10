package com.topsec.tsm.common.formatter;

import java.util.Map;

import com.topsec.tal.base.web.SpringContextServlet;

public class FormatterFactory {

	private Map<String,PropertyFormatter> formatters ;
	
	public PropertyFormatter getFormatter(String formatter){
		return formatters.get(formatter) ; 
	}

	public Map<String, PropertyFormatter> getFormatters() {
		return formatters;
	}

	public void setFormatters(Map<String, PropertyFormatter> formatters) {
		this.formatters = formatters;
	}

	public static FormatterFactory getInstance(){
		return (FormatterFactory) SpringContextServlet.springCtx.getBean("propertyFormatterFactory") ;
	}
	
}
