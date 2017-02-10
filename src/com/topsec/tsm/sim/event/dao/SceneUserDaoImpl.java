package com.topsec.tsm.sim.event.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.topsec.tsm.sim.sceneUser.persistence.SceneUser;
import com.topsec.tsm.sim.sceneUser.persistence.SimBlacks;
import com.topsec.tsm.sim.sceneUser.persistence.SimWhites;

public class SceneUserDaoImpl extends HibernateDaoSupport implements SceneUserDao {

	@Override
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
	public void delete(final Integer id) {
		Object entity = getById(id);
		getHibernateTemplate().execute(new HibernateCallback() {

			@Override
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query=session.createQuery("delete Column tc where tc.sceneUser.id = ?");
				query.setInteger(0, id);
				query.executeUpdate();
				return null;
			}
		});
		getHibernateTemplate().delete(entity);
	}

	@Override
	public void delete(Integer id, Class clazz) {
		Object entity = getById(clazz, id);
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
		return getHibernateTemplate().find("FROM SceneUser order by createTime desc");
	}

	@Override
	public List list(Class clazz) {
		return getHibernateTemplate().find("FROM " + clazz.getSimpleName());
	}

	/**
	 * 通过id获取实体
	 * 
	 * @param id
	 *           要查询实体的id
	 * @return 实体
	 */
	@Override
	public Object getById(Integer id) {
		SceneUser su = (SceneUser) getHibernateTemplate().get(SceneUser.class, id);
		return su ;
	}

	@Override
	public Object getById(Class clazz, Integer id) {
		Object object = getHibernateTemplate().get(clazz, id);

		if (object instanceof SimWhites) {// 解决懒加载
			SimWhites sw = (SimWhites) object;
			Set<SceneUser> sceneUsers = sw.getSceneUsers();
			for (SceneUser sceneUser : sceneUsers) {
				sceneUser.getId();
			}
		}
		if (object instanceof SimBlacks) {
			List list =  getSimBlacksBySceneId( id);
			
			SimBlacks sb = (SimBlacks) object;
			List<SceneUser> sceneUsers = getSceneUserByBlackId( sb.getId());
//			Set<SceneUser> sceneUsers = sb.getSceneUsers();
			for (SceneUser sceneUser : sceneUsers) {
				sceneUser.getId();
			}
		}
		return object;
	}

	public List<SceneUser> getSceneUserByBlackId( Integer id){
		return getHibernateTemplate()
				.find("from SceneUser su where su.id in (select distinct sceneUser.id from Column sc WHERE sc.func='BlacksOp' AND sc.param = ?)",String.valueOf(id));	
	}
	
	/**
	 * @method: getSimBlacksBySceneId
     * 			根据 场景ID获取黑名单
     * @author: 刘根祥(liu_genxiang@topse.com.cn)
	 * @param id 场景ID
	 * @return
	 */
	public List<SimBlacks> getSimBlacksBySceneId(Integer id){
		return getHibernateTemplate()
				.find("from SimBlacks sb where sb.id in (select distinct sc.param from Column sc WHERE sc.func='BlacksOp' AND sc.sceneUser.id= ?)",id);
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
						.createQuery("SELECT COUNT(*) FROM SceneUser")//
						.uniqueResult();//
			}
		});
		return (Long) obj;
	}

	@Override
	public Long getRecordCount(final Class clazz) {
		Object obj = getHibernateTemplate().execute(new HibernateCallback() {

			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				return session//
						.createQuery("SELECT COUNT(*) FROM " + clazz.getSimpleName())//
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
				return session.createQuery(" FROM SceneUser su ORDER BY su.createTime DESC ")// 按创建时间倒序
							  .setFirstResult((pageNum - 1) * pageSize)// 计算当前页的索引首项
							  .setMaxResults(pageSize)//
							  .list();//
			}
		});
		return (List) obj;
	}

	@Override
	public List getRecordList(final int pageNum, final int pageSize, final Class clazz) {

		Object obj = getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				return session.createQuery(" FROM " + clazz.getSimpleName())// 按创建时间倒序
						.setFirstResult((pageNum - 1) * pageSize)// 计算当前页的索引首项
						.setMaxResults(pageSize)//
						.list();//
			}
		});
		List list = (List) obj;
		//解决Bug 70879  
		for (Object _obj : list) {
			if (_obj instanceof SimBlacks) {
				SimBlacks sb = (SimBlacks) _obj;
				List<SceneUser> sceneUsers = getSceneUserByBlackId( sb.getId());
//				Set<SceneUser> sceneUsers = sb.getSceneUsers();
				for (SceneUser sceneUser : sceneUsers) {
					sceneUser.getId();
				}
//				if (sb.getSceneUsers().isEmpty()) {
//					sb.setDelete(true);
//				}
			}
			if (_obj instanceof SimWhites) {
				SimWhites sw = (SimWhites) _obj;
				Set<SceneUser> sceneUsers = sw.getSceneUsers();
				for (SceneUser sceneUser : sceneUsers) {
					sceneUser.getId();
				}
				if (sw.getSceneUsers().isEmpty()) {
					sw.setDelete(true);
				}
			}

		}
		return list;
	}

	@Override
	public Object getByIp(String ip, Class clazz) {
		String hql = " FROM " + clazz.getSimpleName() + " o WHERE o.ip=:ip";
		Query query = getSession().createQuery(hql);
		query.setParameter("ip", ip);
		return query.uniqueResult();
	}

	@Override
	public Object getByName(String name, Class clazz) {
		String hql = " FROM " + clazz.getSimpleName() + " o WHERE o.name=:name";
		Query query = getSession().createQuery(hql);
		query.setParameter("name", name);
		query.setMaxResults(1) ;
		return query.uniqueResult();
	}

	@Override
	public void deleteBlackColumn(Integer sceneId,Integer blacklistId) {
		String sql = "delete from Column c where c.sceneUser.id=? and c.func='BlacksOp' and c.param=?" ;
		Query query = getSession().createQuery(sql) ;
		
		query.executeUpdate() ;
	}

	@Override
	public void deleteColumns(Integer sceneId) {
		String sql = "delete from Column c where c.sceneUser.id=:id " ;
		Query query = getSession().createQuery(sql) ;
		query.setParameter("id", sceneId) ;
		query.executeUpdate() ;
	}
}
