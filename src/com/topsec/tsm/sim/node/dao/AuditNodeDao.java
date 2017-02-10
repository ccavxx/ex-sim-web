/*
 * 版权声明:北京天融信科技有限公司，版权所有违者必究
 * Copyright：Copyright (c) 2011
 * Company：北京天融信科技有限公司
 * @author liuzhan 
 * 2011-6-15
 * @version 1.0
 */
package com.topsec.tsm.sim.node.dao;

import java.util.List;

/*
 * 功能描述：dao层审计对象接口
 */
public interface AuditNodeDao {
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
	public void delete(Long id);

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
	public Object getById(long id);

	/**
	 * 通过ip获取实体
	 * 
	 * @param ip
	 *           查询条件ip
	 * @return 实体
	 */
	public Object getAuditNodeByIp(String ip);

	/**
	 * 通过name获取实体
	 * 
	 * @param name
	 *           查询条件ip
	 * @return 实体
	 */
	public Object getAuditNodeByName(String name);

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

}
