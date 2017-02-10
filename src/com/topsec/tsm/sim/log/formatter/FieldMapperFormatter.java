package com.topsec.tsm.sim.log.formatter;

import java.util.Map;
/**
 * 根据指定的字段，格式化相应的列
 * @author hp
 *
 */
public class FieldMapperFormatter implements FieldFormatter {

	private String field ;
	private FieldFormatter formatter ;
	
	public FieldMapperFormatter(String field,FieldFormatter formatter) {
		super();
		this.field = field;
		this.formatter = formatter;
	}


	@Override
	public Object format(Object value, Map<String, Object> row) {
		Object fieldValue = row.get(field) ;
		return formatter.format(fieldValue, row) ;
	}
}
