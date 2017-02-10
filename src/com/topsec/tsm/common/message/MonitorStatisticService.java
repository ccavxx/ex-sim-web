package com.topsec.tsm.common.message;

import java.util.List;
import java.util.Map;

/**
 * 内存统计处理接口
 * @author wangxinbing
 * @since 2012.05.15
 */
public interface MonitorStatisticService {

	public void statisticHandler(Map<String, Object> result);
	
	public void statisticHandler(List<Map<String, Object>> result);
}
