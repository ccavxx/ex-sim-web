package com.topsec.tsm.sim.log.formatter;

import java.util.Map;

public class ToStringFormatter implements FieldFormatter {

	@Override
	public Object format(Object value, Map<String, Object> row) {
		return value == null ? "" : value.toString();
	}

}
