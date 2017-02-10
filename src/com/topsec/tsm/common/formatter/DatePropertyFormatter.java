package com.topsec.tsm.common.formatter;

import java.util.Date;

import com.topsec.tal.base.util.StringUtil;

public class DatePropertyFormatter implements PropertyFormatter {

	@Override
	public Object format(Object value) {
		if(value instanceof Date){
			return StringUtil.longDateString((Date)value);
		}
		return value ;
	}
}
