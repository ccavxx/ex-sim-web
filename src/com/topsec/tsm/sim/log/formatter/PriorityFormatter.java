package com.topsec.tsm.sim.log.formatter;

import java.util.Map;

import com.topsec.tsm.sim.util.CommonUtils;

public class PriorityFormatter implements FieldFormatter {

	@Override
	public Object format(Object value, Map<String, Object> row) {
		return CommonUtils.getLevel(value) ;
	}

}
