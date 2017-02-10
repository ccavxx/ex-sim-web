package com.topsec.tsm.sim.auth.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.auth.manage.AuthAccount;
import com.topsec.tsm.auth.manage.ColumnConfigId;
import com.topsec.tsm.auth.manage.UserColumnConfig;
import com.topsec.tsm.sim.common.dao.HibernateDaoImpl;

public class UserDaoImpl extends HibernateDaoImpl<AuthAccount, Integer> implements UserDao {
	
	/**
	 * 获取所有用户信息
	 * @author zhou_xiaohu
	 * @return
	 */
	public Collection getAllUsers() {
		return findByCriteria(Restrictions.ne("ID", 5)) ;
	}

	/**
	 * getUsersByRoleName 根据用户角色获取用户列表
	 * @param roleName="" 查询所有
	 * @return
	 * @throws java.rmi.RemoteException
	 */
	public List<AuthAccount> getUsersByRoleName(String roleName){
		List<AuthAccount> users = findByCriteria() ; 
		return users ;
	}

	/**
	 * 获取所有用户
	 * 
	 */
	public PageBean<AuthAccount> getUsersPage(Integer pageIndex,Integer pageSize,Map<String, Object> searchCondition){
		List<Criterion> criterions = new ArrayList<Criterion>() ;
		criterions.add(Restrictions.ne("ID", 5)) ;
		if(searchCondition.get("roleid")!=null &&  !"".equals(searchCondition.get("roleid"))){
			criterions.add(Restrictions.eq("rs.Id",Integer.valueOf(searchCondition.get("roleid").toString())));
		}
		if(searchCondition.get("creater")!=null && !"".equals(searchCondition.get("creater"))){
			criterions.add(Restrictions.or(Restrictions.eq("CreateUser",searchCondition.get("creater").toString()),Restrictions.eq("Name",searchCondition.get("creater").toString())));
		}
		if(searchCondition.get("userName")!=null && !"".equals(searchCondition.get("userName"))){
			criterions.add(Restrictions.ne("Name",searchCondition.get("userName").toString()));
		}
	
		Criteria cri = createCriteria(criterions.toArray(new Criterion[0])) ;
		cri.createAlias("Roles", "rs") ;
		cri.setProjection(Projections.rowCount()) ;
		Number rowCount = (Number) cri.uniqueResult() ;
		cri.setProjection(null) ;
		cri.setResultTransformer(CriteriaSpecification.ROOT_ENTITY) ;
		cri.setFirstResult((pageIndex-1)*pageSize) ;
		cri.setMaxResults(pageSize) ;
		cri.addOrder(Order.desc("ID")) ;
		PageBean<AuthAccount> result = new PageBean<AuthAccount>(pageIndex, pageSize, rowCount.intValue()) ;
		result.setData(cri.list()) ;
		return result ;
	}
	/**
	 * getUserByUserName 根据用户名获取用户
	 * @param userName
	 * @return
	 */
	public AuthAccount getByName(String userName){
		return findUniqueByCriteria(Restrictions.eq("Name", userName)) ;
	}

	@Override
	public void updateFailedCount(AuthAccount aa, Integer count) {
		getSession()
		.createQuery("update AuthAccount account set account.failed = :failed where account.ID = :id")
		.setInteger("failed", count)
		.setInteger("id", aa.getID())
		.executeUpdate();
	}
	
	public void deleteAuthDeivce(String deviceId){
		Query query = getSession().createSQLQuery("delete from auth_user_device where device_id = :deviceId")
								  .setString("deviceId", deviceId) ;
		query.executeUpdate() ;
	}

	@Override
	public void changeDefaultTopo(Integer accountId, Integer topoId) {
		getSession()
		.createQuery("update AuthAccount account set account.defaultTopoId = :defaultTopoId where account.ID = :id")
		.setInteger("defaultTopoId", topoId)
		.setInteger("id", accountId)
		.executeUpdate();
	}
	
	public void deleteUserRelateInfo(String userName){
		String assetSql="update ass_resource set creator =''  where creator = '"+userName+"'";
		getSession().createSQLQuery(assetSql).executeUpdate();
		
        String ruleSql="update  event_rule_group set creater =''  where creater = '"+userName +"'";
        getSession().createSQLQuery(ruleSql).executeUpdate();
        
        String taskSql="update  rpt_report_task set creater =''  where creater = '"+userName+"'";
        getSession().createSQLQuery(taskSql).executeUpdate() ;
        
        String kbSql="update  kb_event set creater='' WHERE creater='"+ userName+"'";
        getSession().createSQLQuery(kbSql).executeUpdate();
	}

	@Override
	public void saveColumnConfig(UserColumnConfig config) {
		UserColumnConfig dbConfig = getColumnConfig(config.getId()) ;
		if(dbConfig == null){
			getSession().save(config) ;
		}else{
			dbConfig.setConfig(config.getConfig()) ;
			getSession().update(dbConfig) ;
		}
	}

	@Override
	public UserColumnConfig getColumnConfig(ColumnConfigId id) {
		return (UserColumnConfig) getSession().get(UserColumnConfig.class, id) ;
	}
}
