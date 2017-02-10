package com.topsec.tsm.sim.newreport.handler;

import java.util.List;
import java.util.Map;

/**
 * @ClassName: QueryResultFormat
 * @Declaration: TODo
 * 
 * @author: WangZhiai create on2015年7月13日上午10:51:37
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public interface QueryResultFormat {
	/**
	 * <p>
	 * 此方法中完成查询结果的 空数据、时间结果的格式化 等预操作
	 * </p>
	 * @param data 查询出来的原始数据
	 * @return
	 */
	public <T>List<T> preprocess(List<T> data,Map<String, Object> structureMap);
	
	/**
	 * 流量数据处理
	 * @param data 查询出来的数据
	 * @param units 单位 B Kb Mb Gb Tb Pb Eb...
	 * @return 处理过后的数据
	 */
	public <T>List<T> showFlowOperater(List<T> data,String units);
	
	/**
	 * 
	 * @param data
	 * @param units 单位 一、万 、亿、万亿、亿亿
	 * @return
	 */
	public <T>List<T> showNumberOperater(List<T> data,String units);
	
	/**
	 * 此方法用于趋势图的展示，亦可以在前端实现，在此处定义是为了导出 doc 、pdf 等时候用
	 * @param data 传过来的流数据
	 * @param showNum 显示点个数
	 * @return
	 */
	public <T>List<T> trendOperater(List<T> data,Integer showNum);
	
	/**
	 * 此方法将查询的某些字段映射到系统想要展示的字段
	 * @param type
	 * @param fromobj
	 * @return
	 */
	public <T extends Object> T mapping(String type,Object fromobj);
	
	/**
	 * 
	 * @param list
	 * @param context 上下文对象
	 * @return
	 */
	public <T extends Object> Map<T, Long> reducing(List<Map<T, Long>>list,Map<T, Long>context);
}
