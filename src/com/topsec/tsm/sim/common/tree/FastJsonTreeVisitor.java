package com.topsec.tsm.sim.common.tree;

import java.util.HashMap;
import java.util.Map;

import com.topsec.tal.base.util.StringUtil;

public class FastJsonTreeVisitor extends TreeFieldVisitor {
	
	private Map<String,String> fieldMapper;
	
	public FastJsonTreeVisitor(Map<String, String> fieldMapper) {
		super(fieldMapper.keySet());
		this.fieldMapper = fieldMapper;
	}

	public FastJsonTreeVisitor(String... fieldExpressions){
		fieldMapper = new HashMap<String, String>()  ;
		for(String fieldExp:fieldExpressions){
			String[] fieldArr = StringUtil.split(fieldExp, "=") ;
			if(fieldArr.length==0||fieldArr.length>3){
				continue ;
			}
			if(fieldArr.length==2){
				fieldMapper.put(fieldArr[0], fieldArr[1]) ;
			}
			addField(fieldArr[0]) ;
		}
		
	}
	
	@Override
	public FieldVisitResult createVisitResult() {
		return new FastJsonResult(fieldMapper);
	}
	
}
