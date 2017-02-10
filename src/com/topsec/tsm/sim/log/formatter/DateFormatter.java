package com.topsec.tsm.sim.log.formatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.topsec.tal.base.util.StringUtil;

public class DateFormatter implements FieldFormatter {
	private SimpleDateFormat dateFormatter ;
	public DateFormatter() {
		super();
		dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
	}

	public DateFormatter(String pattern) {
		super();
		dateFormatter = new SimpleDateFormat(pattern) ;
	}

	@Override
	public Object format(Object value, Map<String, Object> row) {
		if(value instanceof Date){
			return dateFormatter.format((Date)value);
		}
		return StringUtil.toString(value) ;
	}

}
