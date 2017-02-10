package com.topsec.tsm.sim.newreport.handler;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.sim.newreport.bean.PageVo;

/**
 * @ClassName: QueryConditionsFormat
 * @Declaration: TODo
 * 
 * @author: WangZhiai create on2015年7月13日上午10:43:21
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public interface QueryConditionsFormat {
	
	/**
	 * 
	 * @param queryString 查询的SQL 或者其他语句
	 * @param stime查询的开始时间 (null 不做处理)
	 * @param pageable分页对象 (null 不做处理)
	 * @return 替换过的 sql 或者其他语句，例如 HOUR表改为从 天(DAY)表查询
	 */
	public String changeQueryString(String queryString,Long stime,PageVo pageable);
	
	/**
	 * 根据查询结束时间 和 时间周期报表类型 确定 开始时间
	 * </p>
	 * @param eTime 报表 查询结束时间
	 * @param cycleReportTimeType 代表 天、周、月、年 报表 时间类型
	 * @return 报表查询的开始时间
	 */
	public Long executeStartTime(Long eTime,String cycleReportTimeType);
	
	/**
	 * 查询参数检查并格式化
	 * @param params
	 * @return 格式化后的参数
	 */
	public List<Object> queryParamsFormat(List<Object> params);
	
	/**
	 * 把queryMap中的值组装成查询语句
	 * @param queryMap
	 * @param queryConditions is ReportQueryConditions entity
	 * @return
	 */
	public String assemblingQueryString(Map<String, Object> queryMap,Object queryConditions);
	
	/**
	 * 提取queryMap中的条件语句并赋值
	 * @param queryMap
	 * @param queryConditions is ReportQueryConditions entity
	 * @return
	 */
	public List<Object> assemblingQueryParams(Map<String, Object> queryMap,Object queryConditions);
	
	/**
	 * 从指定查询Map中 查询体参数中 ，生成 该 查询条件 和对应的查询参数
	 * @param queryMap 查询Map
	 * @param queryConditions is ReportQueryConditions entity 传过来的参数
	 * @return
	 */
	public Map<String, List<Object>> assemblingQueryStringAndParams(Map<String, Object> queryMap,Object queryConditions) ;
	/**
	 * 从指定查询Map中 查询体参数中 ，生成 该 查询条件 和对应的查询参数
	 * @param queryMap 查询Map
	 * @param queryConditions is ReportQueryConditions entity
	 * @return
	 */
	public Map.Entry<String, List<Object>> assemblingEntryQueryStringAndParams(Map<String, Object> queryMap,Object queryConditions) ;
	
	/**
	 * 根据查询体生成所有的 (查询语句，查询参数) 的键值对的 List
	 * @param queryConditions is ReportQueryConditions entity 查询体
	 * @return 每一个List 中的元素 是属于同一个 parentId
	 */
	public List<Map<String, List<Object>>> assemblingQueryStringAndParams(Object queryConditions) ;
}
