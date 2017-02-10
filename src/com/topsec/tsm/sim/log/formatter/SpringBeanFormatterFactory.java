package com.topsec.tsm.sim.log.formatter;

import java.util.Map;

public class SpringBeanFormatterFactory implements FormatterFactory{

	private Map<String,FieldFormatter> formatters ;

	public void setFormatters(Map<String, FieldFormatter> formatters) {
		this.formatters = formatters;
	}

	@Override
	public FieldFormatter getFormatter(String name) {
		return formatters.get(name);
	}
}
