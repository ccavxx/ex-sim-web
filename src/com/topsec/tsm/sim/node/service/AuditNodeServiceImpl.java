/*
 * 版权声明:北京天融信科技有限公司，版权所有违者必究
 * Copyright：Copyright (c) 2011
 * Company：北京天融信科技有限公司
 * @author liuzhan 
 * 2011-6-15
 * @version 1.0
 */
package com.topsec.tsm.sim.node.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.topsec.tsm.sim.auditnode.AuditNode;
import com.topsec.tsm.sim.node.dao.AuditNodeDao;
import com.topsec.tsm.sim.resource.persistence.Node;

public class AuditNodeServiceImpl implements AuditNodeService {

	private AuditNodeDao auditNodeDao;// spring注入的对象

	/**
	 * 保存
	 * 
	 * @param entity
	 *           要保存的实体
	 */
	@Override
	public void save(Object entity) {
		auditNodeDao.save(entity);
	}

	/**
	 * 删除
	 * 
	 * @param id
	 *           要删除实体的id
	 */
	@Override
	public void delete(Long id) {
		auditNodeDao.delete(id);
	}

	/**
	 * 修改
	 * 
	 * @param entity
	 *           要修改的实体
	 */
	@Override
	public void update(Object entity) {
		auditNodeDao.update(entity);
	}

	/**
	 * 全部记录
	 * 
	 * @return 全部记录
	 */
	@Override
	public List list() {
		return auditNodeDao.list();
	}

	/**
	 * 通过id获取实体
	 * 
	 * @param id
	 *           要查询实体的id
	 * @return 实体
	 */
	@Override
	public Object getById(long id) {
		AuditNode auditNode = (AuditNode) auditNodeDao.getById(id);
		if (auditNode != null) {
			Set<Node> nodes = auditNode.getNodes();
			if (nodes != null) {
				for (Node node : nodes) {
					node.getNodeId();
				}
			}
		}
		return auditNode;
	}

	/**
	 * 全部记录的数量
	 * 
	 * @return 全部实体记录数量
	 */
	@Override
	public Long getRecordCount() {
		return auditNodeDao.getRecordCount();
	}

	/**
	 * 通过ip获取实体
	 * 
	 * @param ip
	 *           查询条件ip
	 * @return 实体
	 */
	@Override
	public Object getAuditNodeByIp(String ip) {
		return auditNodeDao.getAuditNodeByIp(ip);
	}

	/**
	 * 通过name获取实体
	 * 
	 * @param name
	 *           查询条件name
	 * @return 实体
	 */
	@Override
	public Object getAuditNodeByName(String name) {
		return auditNodeDao.getAuditNodeByName(name);
	}

	/**
	 * 分页查询列表
	 * 
	 * @param pageNum
	 *           当前页
	 * @param pageSize
	 *           每页显示数量
	 * @return 分页列表实体对象
	 */
	@Override
	public List getRecordList(int pageNum, int pageSize) {
		List<AuditNode> list = auditNodeDao.getRecordList(pageNum, pageSize);
		for (AuditNode auditNode : list) {
			Set<Node> nodes = auditNode.getNodes();
			for (Node node : nodes) {
				node.getNodeId();
			}
		}
		return list;
	}
	
	/**
	* @method: getAuditNodeMapping 
	* 		       得到审计对象映射
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:  
	* @return: Map<String,String>:  审计对象映射(key为IP,value为审计对象名称)
	* @exception: Exception
	*/
	public Map<String,String> getAuditNodeMapping() throws Exception{
		List<AuditNode> list = this.list();
		Map<String,String> map=new HashMap<String, String>();
		if(list!=null){
			for (AuditNode auditNode : list) {
				String ip = auditNode.getIp();
				String name=auditNode.getName();
				map.put(ip, name);
			}
		}
		return  map;
	}

	public AuditNodeDao getAuditNodeDao() {
		return auditNodeDao;
	}

	public void setAuditNodeDao(AuditNodeDao auditNodeDao) {
		this.auditNodeDao = auditNodeDao;
	}

}
