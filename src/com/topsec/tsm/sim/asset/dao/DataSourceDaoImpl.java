/**
 * 版权声明北京天融信科技有限公司，版权所有违者必究
 *
 * Copyright：Copyright (c) 2011
 * Company：北京天融信科技有限公司
 * @author 杨轩嘉（yang_xuanjia@topsec.com.cn）
 * @since  2011-06-15
 * @version 1.0
 * 
 */
package com.topsec.tsm.sim.asset.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tal.base.util.LogKeyInfo;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.asset.DataSourceUtil;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.asset.web.DataSourceStatus;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.common.dao.HibernateDaoImpl;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.node.util.CollectorType;

/**
 * 功能描述: 日志源Dao层实现类
 */
public class DataSourceDaoImpl extends HibernateDaoImpl<SimDatasource, Long> implements DataSourceDao {

	private String ownerGroup ;
	
	public String getOwnerGroup() {
		return ownerGroup;
	}

	public void setOwnerGroup(String ownerGroup) {
		this.ownerGroup = ownerGroup;
	}

	/**
	 * @method: getDataSourceTreeWithNodeList 得到日志源树集合
	 * @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	 * @param: type: 日志源类型 type="system"为系统日志源; type="other"为非系统日志源; null为所有日志源
	 * @return: List<Map<String,Object>>:日志源集合 , dataSourceIp,securityObjectType,auditorNodeId,nodeIp,nodeAlias,dataSourceName
	 * @exception: Exception
	 */
	public List<Map<String, Object>> getDataSourceTreeWithNodeList(String type) throws Exception {
		String hql = "select new map(d.ResourceId as id,d.deviceIp as dataSourceIp,d.securityObjectType as securityObjectType,d.auditorNodeId as auditorNodeId,n.Ip as nodeIp,n.Alias as nodeAlias,d.ResourceName as dataSourceName) from SimDatasource d, Node n where d.auditorNodeId=n.NodeId and n.Type='Auditor'";
		if ("system".equals(type)) {
			hql += " and d.securityObjectType='" + LogKeyInfo.LOG_SYSTEM_TYPE + "'";
		} else if ("other".equals(type)) {
			hql += " and d.securityObjectType!='" + LogKeyInfo.LOG_SYSTEM_TYPE + "'";
		}
		if ("log".equals(ownerGroup)) {
			hql += " and (d.ownGroup=:ownGroup or d.ownGroup is null)";
		} else if ("monitor".equals(ownerGroup)) {
			hql += " and d.ownGroup=:ownGroup ";
		}

		Session session = this.getSession();
		Query query = session.createQuery(hql);
		query.setString("ownGroup", ownerGroup);
		List<Map<String, Object>> simDatasources = query.list();
		return simDatasources;
	}
	
	@Override
	public SimDatasource getFirstByIp(String ip,DataSourceStatus status) {
		Criteria cri = createCriteria(Restrictions.eq("ownGroup",ownerGroup),Restrictions.eq("deviceIp", ip)) ;
		switch(status){
			case ENABLE : cri.add(Restrictions.eq("available", 1)) ;break ;
			case DISABLE: cri.add(Restrictions.eq("avaible", 0)) ;break ;
			case ALL : cri.add(Restrictions.in("available", new Integer[]{0,1})) ;
		}
		cri.setMaxResults(1) ;
		SimDatasource dataSource = (SimDatasource) cri.uniqueResult() ;
		return dataSource ;
	}

	@Override
	public List<SimDatasource> getByIp(String ip) {
		List<SimDatasource> dataSources = findByCriteria(Restrictions.eq("ownGroup", ownerGroup),
														 Restrictions.eq("deviceIp", ip)) ;
		return dataSources ;
	}
	
	@Override
	public SimDatasource findByDeviceTypeAndIp(String securityObjectType,String ip) {
		SimDatasource dataSource = findFirstByCriteria(Restrictions.eq("ownGroup", ownerGroup),
														Restrictions.eq("securityObjectType", securityObjectType),
														 Restrictions.eq("deviceIp", ip)) ;
		return dataSource ;
	}
	
	@Override
	public List<SimDatasource> getByIPs(String[] ipArray) {
		if(ObjectUtils.isEmpty(ipArray)) return Collections.emptyList() ;
		String hql = "SELECT new SimDatasource(ResourceId,deviceIp,securityObjectType,nodeId) FROM SimDatasource WHERE ownGroup=:ownerGroup AND deviceIp in (:deviceIps)" ;
		Query query = getSession().createQuery(hql) ;
		query.setString("ownerGroup", ownerGroup);
		query.setParameterList("deviceIps",ipArray) ;
		List result = query.list() ;
		return result ;
	}

	/**
	 * @method: getDataSource
	 *          查询启动的所有日志源
	 * @param cmd: 分类, 如果cmd="DataSource"为数据源列表;  cmd="Start"为启用的日志源列表;cmd="Stop"为禁用的日志源列表; 
	 * @param ownGroup
	 * @return
	 * @throws Exception
	 */
	public List<SimDatasource> getDataSource(String cmd){
		Criteria criteria = createCriteria(Restrictions.eq("ownGroup", ownerGroup)) ;
		if("Start".equals(cmd)) {
			criteria.add(Property.forName("available").eq(1));
		}else if ("Stop".equals(cmd)) {
			criteria.add(Property.forName("available").eq(0));
		}
		return criteria.list() ;
	}

	@Override
	public boolean isNameExist(String name) {
		SimDatasource datasource = findFirstByCriteria(Restrictions.eq("ResourceName", name),Restrictions.eq("ownGroup", ownerGroup)) ;
		getSession().evict(datasource) ;
		return datasource != null;
	}
	
	@Override
	public boolean isNameExist(SimDatasource dataSource) {
		SimDatasource sameDS = 
				findFirstByCriteria(Restrictions.eq("ResourceName", dataSource.getResourceName()),
									Restrictions.eq("ownGroup", ownerGroup),
									Restrictions.ne("ResourceId", dataSource.getResourceId())) ;
		return sameDS != null ;
	}

	@Override
	public List<SimDatasource> getDatasourcesByRuleId(Long id) {
		return findByCriteria(Restrictions.eq("ruleId", id),Restrictions.eq("ownGroup", ownerGroup)) ;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getDataSourceTypeList() {
		String hql = "SELECT DISTINCT s.securityObjectType as securityObjectType FROM SimDatasource s WHERE ";
		if ("log".equals(ownerGroup)) {
			hql += "(s.ownGroup=:ownGroup or s.ownGroup is null)";
		} else if ("monitor".equals(ownerGroup)) {
			hql += "s.ownGroup=:ownGroup ";
		}
		Session session = getSession();
		Query query = session.createQuery(hql);
		query.setString("ownGroup", ownerGroup);
		List<String> list = query.list();
		return list;
	}

	/**
	 * 根据设备类型获取相应的日志源
	 * @param deviceType
	 * @return List<SimDatasource>
	 */
	public List<SimDatasource> getByDvcType(String deviceType){
			return findByCriteria(Restrictions.eq("securityObjectType", deviceType),Restrictions.eq("ownGroup", ownerGroup)) ;
	}

	@Override
	public SimDatasource updateState(Long id, Integer available) {
		SimDatasource dataSource = findById(id) ;
		dataSource.setAvailable(available) ;
		update(dataSource) ;
		return dataSource ;
	}

	@Override
	public boolean exist(String ip, String securityObjectType, String nodeId,String group) {
		Criterion groupCondition ;
		if("system".equals(group)){
			groupCondition = Restrictions.isNull("ownGroup") ;
		}else{
			groupCondition = Restrictions.eq("ownGroup", group) ;
		}
		SimDatasource dataSource = findFirstByCriteria(
				Restrictions.eq("deviceIp", ip),
				Restrictions.eq("securityObjectType", securityObjectType),
				Restrictions.eq("nodeId", nodeId),
				groupCondition
				) ;
		
		return dataSource != null;
	}

	@Override
	public List<SimDatasource> getByAggregatorId(Long aggId) {
		return findByCriteria(Restrictions.eq("aggregatorId", aggId),Restrictions.eq("ownGroup", ownerGroup));
	}
	
	public List<SimDatasource> getDataSourceListByDeviceType(String type){
		String hql = "from SimDatasource where securityObjectType='"+type+"' and ownGroup=:ownGroup group by deviceIp ";
		Session session = this.getSession();
		Query query = session.createQuery(hql);
		query.setString("ownGroup", ownerGroup) ;
		List<SimDatasource> simDatasources = query.list();
		return simDatasources;
	}
	
	@Override
	public List<SimDatasource> getAll(boolean includeMonitor,boolean includeAuditLog,boolean includeSystemLog) {
		if(includeMonitor && includeAuditLog && includeSystemLog){
			return getAll() ;
		}else{
			List<Criterion> conditions = new ArrayList<Criterion>(3) ;
			if(!includeMonitor){
				conditions.add(Restrictions.eq("ownGroup", "log")) ;
			}
			if(!includeAuditLog){
				conditions.add(Restrictions.ne("securityObjectType", DataSourceUtil.SYSTEM_LOG)) ;
			}
			if(!includeSystemLog){
				conditions.add(Restrictions.ne("securityObjectType", DataSourceUtil.SYSTEM_RUN_LOG)) ;
			}
			return findByCriteria(conditions.toArray(new Criterion[conditions.size()])) ;
		}
	}
	
	public List<SimDatasource> getAllDataSource(boolean includeAuditLog,boolean includeSystemLog){
		return getAll(false, includeAuditLog, includeSystemLog) ;
	}

	@Override
	public List<SimDatasource> getUserDataSource(SID sid) {
		Set<String> userIps = sid.getUserIp() ;
		return findByCriteria(Restrictions.eq("ownGroup", ownerGroup),Restrictions.in("deviceIp", userIps));
	}

	@Override
	public boolean exist(SimDatasource datasource, CollectorType collectType) {
		SimDatasource dataSource = findFirstByCriteria(
				Restrictions.eq("deviceIp", datasource.getDeviceIp()),
				Restrictions.eq("collectMethod", collectType.getType())) ;
		return dataSource != null ;
	}

	@Override
	public List<SimDatasource> getByNodeId(String nodeId) {
		return findByCriteria(Restrictions.eq("ownGroup", ownerGroup),Restrictions.eq("nodeId", nodeId));
	}

	@Override
	protected Criterion[] getSearchCriterions(Map<String, Object> searchCondition) {
		List<Criterion> conditions = new ArrayList<Criterion>();
		String ip = (String) searchCondition.get("ip");
		String name = (String) searchCondition.get("name");
		String dataSourceType = (String) searchCondition.get("dataSourceType");
		String collectMethod = (String) searchCondition.get("collectMethod");
		String state = (String) searchCondition.get("state");
			if (StringUtil.isNotBlank(ip)){
				conditions.add(Restrictions.like("deviceIp", "%" + ip + "%"));
			}
			/*if (StringUtil.isNotBlank(name)){
				conditions.add(Restrictions.eq("resourceName", name));
			}*/
			if (StringUtil.isNotBlank(dataSourceType)){
				conditions.add(Restrictions.eq("securityObjectType", dataSourceType));
			}
			if(StringUtil.isNotBlank(collectMethod)){
				conditions.add(Restrictions.eq("collectMethod", collectMethod));
			}
			if(StringUtil.isNotBlank(state)){
				conditions.add(Restrictions.eq("available", Integer.valueOf(state)));
			}
		conditions.add(Restrictions.eq("ownGroup", ownerGroup));
		conditions.add(Restrictions.ne("deviceIp", "127.0.0.1"));
		return conditions.toArray(new Criterion[conditions.size()]);
	}
	
	
}
