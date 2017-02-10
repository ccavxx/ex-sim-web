package com.topsec.tsm.sim.sysconfig.convert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 规则参数处理
 * @author zhaojun 2014-3-28下午6:55:28
 */
public class XMLRuleColumnHandler implements RuleColumnHandler {
	
	/**
	 * 隔离特殊字符分隔符处理
	 */
	@Override
	public Map<String, Object> separate(String func, String field, String paramText) {
		Map<String,Object> functionMap=new HashMap<String, Object>();
		functionMap.put("function", /*explainVals(func)*/func);	
		functionMap.put("field", /*explainVals(field)*/field);	
		//String param=explainVals(paramText);
		String[] vals=paramText.split("θ");//值分割
		List<String> parameterValues=new ArrayList<String>();
		for (int i = 0; i < vals.length; i++) {
			parameterValues.add(vals[i]);
		}
		functionMap.put("params",parameterValues );	
		return functionMap;
	}
	@Deprecated
	private  String explainVals(String text) {
		StringBuffer sb = new StringBuffer();
		String[] texts = text.split("λ");
		for (int i = 0; i < texts.length; i++) {
			String str = texts[i];
			String[] strs = str.split("θ");
			for (int j = 0; j < strs.length; j++) {
				String s = strs[j];
				if (s.indexOf("μ") != -1) {
					if (j < strs.length - 1) {
						sb.append(s.split("μ")[0] + "θ");
					} else {
						sb.append(s.split("μ")[0]);
					}
				} else {
					sb.append(s.split("α")[0]);
				}
			}

		}
		return sb.toString();
	}	
}
