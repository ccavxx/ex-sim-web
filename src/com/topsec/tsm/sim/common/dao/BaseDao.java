package com.topsec.tsm.sim.common.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.topsec.tsm.ass.PageBean;


public interface BaseDao<T,ID extends Serializable> {

	/**
	 * 根据id查找实体对象
	 * @param id
	 * @return
	 */
	public T findById(ID id) ;
	/**
	 * 根据id获取一个游离状态的hibernate实体对象
	 * @param id
	 * @return
	 */
	public T getTransient(ID id) ;
	
	/**
	 * 检索所有的实体对象
	 * @return
	 */
	public List<T> getAll() ;
	/**
	 * 根据搜索条件进行分页查询
	 * @param pageIndex
	 * @param pageSize
	 * @param searchCondition
	 * @return
	 */
	public PageBean<T> search(int pageIndex,int pageSize,Map<String,Object> searchCondition) ;
	
	/**
	 * 根据使用指定字段对数据排序后，再根据搜索条件进行分页查询
	 * @param pageIndex
	 * @param pageSize
	 * @param searchCondition
	 * @return
	 */
	public PageBean<T> search(int pageIndex,int pageSize,Map<String,Object> searchCondition,SimOrder... orders) ;

	/**
	 * 
	 * 根据条件检索对象
	 * @param condition
	 * @return
	 */
	public List<T> findByCondition(Map<String,Object> condition) ;
	/**
	 * 保存实体对象 返回Id
	 * @param entity
	 */
	public Serializable save(T entity) ;
	/**
	 * 更新实体对象
	 * @param entity
	 */
	public void update(T entity) ;
	/**
	 * 删除实体对象
	 * @param entity
	 */
	public void delete(T entity) ;
	/**
	 * 删除实体对象
	 *功能描述：
	 *@author: ZhouZhijie
	 *@date： 日期：2014-3-7 时间：下午01:51:35
	 *@param id
	 */
	public void delete(ID id) ;
	
	/**
	 * 批量保存
	 * @author zhaojun 2014-4-25上午10:50:32
	 * @param list
	 */
	public void batchSave(List<T> list);
	/**
	 * 单独更新某一指定属性
	 * @param propertyName
	 * @param value
	 */
	public int updateProperty(ID id,String propertyName,Object value) ;
}
