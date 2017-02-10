package com.topsec.tsm.sim.event.service;

import java.util.List;

public interface SceneUserService {
	/**
	 * 保存
	 * 
	 * @param entity
	 *           要保存的实体
	 */
	public void save(Object entity);

	/**
	 * 删除
	 * 
	 * @param id
	 *           要删除实体的id
	 */
	public void delete(Integer id);

	public void delete(Integer id, Class clazz);

	/**
	 * 全部记录
	 * 
	 * @return 全部记录
	 */
	public List list();

	/**
	 * 修改
	 * 
	 * @param entity
	 *           要修改的实体
	 */
	public void update(Object entity);

	/**
	 * 通过id获取实体
	 * 
	 * @param id
	 *           要查询实体的id
	 * @return 实体
	 */
	public Object getById(Integer id);

	/**
	 * 全部记录的数量
	 * 
	 * @return 全部实体记录数量
	 */
	public Long getRecordCount();

	/**
	 * 分页查询列表
	 * 
	 * @param pageNum
	 *           当前页
	 * @param pageSize
	 *           每页显示数量
	 * @return 分页列表实体对象
	 */
	public List getRecordList(int pageNum, int pageSize);

	public List list(Class clazz);

	public Object getById(Class clazz, Integer id);

	public Long getRecordCount(Class clazz);

	public List getRecordList(int pageNum, int pageSize, Class clazz);

	public Object getByName(String name, Class clazz);

	public Object getByIp(String ip, Class clazz);
	/**
	 * 更改事件状态
	 * @param status
	 * @return
	 */
	public boolean modifyStatus(int sceneId,int status) ;
}
