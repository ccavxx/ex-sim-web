package com.topsec.tsm.sim.sysconfig.convert;

import java.util.Map;

/**
 * 规则参数处理
 * @author zhaojun 2014-3-28下午6:53:37
 */
public interface RuleColumnHandler {
	Map<String, Object> separate(String func, String field, String paramText);
}
