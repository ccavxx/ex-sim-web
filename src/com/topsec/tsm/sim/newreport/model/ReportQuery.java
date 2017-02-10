package com.topsec.tsm.sim.newreport.model;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.sim.newreport.bean.PageVo;
/**
 * @ClassName: ReportQuery
 * @Declaration: TODo
 * 
 * @author: WangZhiai create on2015年7月10日下午5:14:16
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public interface ReportQuery {
	
	/**
	 * 根据日志源类型查询一级主题报表的内容
	 * @param securityObjectType
	 * @return
	 */
	public List<Map<String,Object>> findParentTheme(String securityObjectType);
	
	/**
	 * 根据一级主题报表的内容查询 子主题报表的简单内容
	 * @param parentId
	 * @return
	 */
	public List<Map<String,Object>> findSimpleSubThemes(Integer parentId);
	
	/**
	 * 根据一级主题的内容查询 子报表的详细内容，结构等
	 * @param parentId
	 * @return
	 */
	public List<Map<String,Object>> findDetailSubThemeList(Integer parentId);
	
	/**
	 * 
	 * @param parentSubId 关联表ID主键
	 * @return 此处可返回 Map封装的属性
	 */
	public Map<String,Object> findDetailSubTheme(Integer parentSubId);
	
	/**
	 * 此方法只适合单节点的查询
	 * @param queryString
	 * @param params
	 * @return
	 */
	public <T>List<T> findByConditions(String queryString,Object ... params);
	
	/**
	 * 此方法只适合针对报表单节点的查询
	 * @param queryString
	 * @param params
	 * @return
	 */
	public List<Map<String,Object>> findBySQL(String queryString,Object ... params);
	/**
	 * 返回查询结果
	 * @param queryString 查询字符串
	 * @param params 查询参数
	 * @param nodeIds 节点
	 * @return
	 */
	public <T>List<T> findByConditions(String queryString,List<Object> params,String[] nodeIds);
	
	/**
	 * <p>
	 * 对于mySQL 分页的实现可以  控制查询字符串即可，此方法可以用 findByConditions(String,List,String[])实现
	 * 对于HQL等其他的实现可以相关设置 pageable的相关值
	 * </p>
	 * @param queryString
	 * @param params
	 * @param nodeIds
	 * @param pageable
	 * @return 返回查询结果
	 */
	public <T>List<T> findByConditions(String queryString,List<Object> params,String[] nodeIds,PageVo pageable);
	
	/**
	 * <p>
	 * 此方法前端或者模拟的查询结构体 组装 直接返回查询结果和数据结构，可用于 csoc接口使用
	 * 此方法 返回的结构是 对应的 某个具体的 小主题查询的结果
	 * </p>
	 * @param conditionsObj is ReportQueryConditions entity查询条件对象
	 * @return
	 */
	public <T>Map<Object,List<T>> findDataByConditions(Object conditionsObj);
	
	/**
	 * 根据查询体 查询所有数据，包括数据结构和数据,可以用于 csoc接口使用
	 * @param conditionsObj is ReportQueryConditions entity
	 * @return
	 */
	public <T>Map<Object,List<T>> findAllDataByConditions(Object conditionsObj);
	
	/**
	 * 根据查询体 查询所有数据，包括数据结构和数据,放入数据结构体中，可以用于 csoc接口使用
	 * @param conditionsObj is ReportQueryConditions entity
	 * @return
	 */
	public List<Map> findResultPutInDataStructureDescByConditions(Object conditionsObj);
	
	/**
	 * 返回此一级主题报表引用了哪些数据库的表
	 * @param parentId
	 * @return
	 */
	public List<String> findTableNameList(Integer parentId);
	
	/**
	 * 
	 * @param securityObjectType
	 * @return
	 */
	public List<String> findTableNameListByType(String securityObjectType);
}
