package com.topsec.tsm.sim.log.formatter;

import java.util.Map;

/**
 * 格式化字段
 * @author hp
 *
 */
public interface FieldFormatter {

	public Object format(Object value,Map<String,Object> row) ;
}
