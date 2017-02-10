package com.topsec.tsm.common.formatter;

import org.springframework.web.util.HtmlUtils;

public class HtmlEscapeFormatter implements PropertyFormatter {

	@Override
	public Object format(Object value) {
		if(value instanceof String){
			return HtmlUtils.htmlEscape((String)value) ;
		}
		return value;
	}

}
