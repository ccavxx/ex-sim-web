package com.topsec.tsm.sim.common.tree;

import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class FastJsonResult extends JSONObject implements FieldVisitResult{

	private static final long serialVersionUID = 1L;
	private Map<String,String> fieldMapper ;

	public FastJsonResult(Map<String, String> fieldMapper) {
		super();
		this.fieldMapper = fieldMapper;
	}

	@Override
	public void addChildResult(VisitResult result) {
		JSONArray child = (JSONArray) get("children") ;
		if(child == null){
			child = new JSONArray() ;
			put("children",child) ;
		}
		child.add(result) ;
	}

	public void visitField(String field,Tree treeNode){
		try {
			Object value =  PropertyUtils.getNestedProperty(treeNode, field) ;
			String key = fieldMapper.get(field) ;//如果有需要映射到其它键值，此值不会为null
			if(key == null){
				key = field ;
			}
			put(key, value) ;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
