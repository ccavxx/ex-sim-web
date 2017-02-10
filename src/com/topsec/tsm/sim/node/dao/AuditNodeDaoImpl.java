/*
 * 版权声明:北京天融信科技有限公司，版权所有违者必究
 * Copyright：Copyright (c) 2011
 * Company：北京天融信科技有限公司
 * @author liuzhan 
 * 2011-6-15
 * @version 1.0
 */
package com.topsec.tsm.sim.node.dao;

import java.sql.SQLException;
import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import com.topsec.tsm.sim.auditnode.AuditNode;

/*
 * 功能描述：dao层审计对象实现类
 */

public class AuditNodeDaoImpl extends HibernateDaoSupport implements AuditNodeDao {
	/**
	 * 保存
	 * 
	 * @param entity
	 *           要保存的实体
	 */
	public void save(Object entity) {

		this.getHibernateTemplate().save(entity);
	}

	/**
	 * 删除
	 * 
	 * @param id
	 *           要删除实体的id
	 */
	@Override
	public void delete(Long id) {
		Object entity = getById(id);
		getHibernateTemplate().delete(entity);
	}

	/**
	 * 修改
	 * 
	 * @param entity
	 *           要修改的实体
	 */
	public void update(Object entity) {

		this.getHibernateTemplate().update(entity);
	}

	/**
	 * 全部记录
	 * 
	 * @return 全部实体
	 */

	@Override
	public List list() {
		return getHibernateTemplate().find("FROM AuditNode");
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
		return getHibernateTemplate().get(AuditNode.class, id);
	}

	/**
	 * 通过ip获取实体
	 * 
	 * @param ip
	 *           查询条件ip
	 * @return 实体
	 */
	@Override
	public Object getAuditNodeByIp(final String ip) {
		Object obj = getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				return session.createQuery(" FROM AuditNode au WHERE au.ip=? ")//
						.setParameter(0, ip)//
						.uniqueResult();//
			}
		});
		return obj;
	}

	/**
	 * 通过name获取实体
	 * 
	 * @param name
	 *           查询条件name
	 * @return 实体
	 */
	@Override
	public Object getAuditNodeByName(final String name) {
		Object obj = getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				return session.createQuery(" FROM AuditNode au WHERE au.name=? ")//
						.setParameter(0, name)//
						.uniqueResult();//
			}
		});
		return obj;
	}

	/**
	 * 全部记录的数量
	 * 
	 * @return 全部实体记录数量
	 */
	@Override
	public Long getRecordCount() {
		Object obj = getHibernateTemplate().execute(new HibernateCallback() {

			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				return session//
						.createQuery("SELECT COUNT(*) FROM AuditNode")//
						.uniqueResult();//
			}
		});
		return (Long) obj;
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
	public List getRecordList(final int pageNum, final int pageSize) {

		Object obj = getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				return session.createQuery(" FROM AuditNode au ORDER BY au.createTime DESC ")// 按创建时间倒序
						.setFirstResult((pageNum - 1) * pageSize)// 计算当前页的索引首项
						.setMaxResults(pageSize)//
						.list();//
			}
		});
		return (List) obj;
	}

}
