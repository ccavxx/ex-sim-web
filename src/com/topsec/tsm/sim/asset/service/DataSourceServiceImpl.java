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
package com.topsec.tsm.sim.asset.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.auth.manage.AuthAccount;
import com.topsec.tsm.auth.manage.AuthUserDevice;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.sim.aggregator.AggregatorScene;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.AssetObject;
import com.topsec.tsm.sim.asset.DataSourceUtil;
import com.topsec.tsm.sim.asset.dao.DataSourceDao;
import com.topsec.tsm.sim.asset.web.DataSourceStatus;
import com.topsec.tsm.sim.auth.dao.UserDao;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.common.dao.SimOrder;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.newreport.util.ReportDataManagerUtil;
import com.topsec.tsm.sim.node.exception.ComponentNotFoundException;
import com.topsec.tsm.sim.node.exception.DataSourceException;
import com.topsec.tsm.sim.node.service.NodeDeployService;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.node.util.CollectorType;
import com.topsec.tsm.sim.node.util.CollectorTypeUtil;
import com.topsec.tsm.sim.resource.exception.ResourceNameExistException;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.rule.SimRule;
import com.topsec.tsm.sim.sysconfig.dao.AggregatorRuleDao;
import com.topsec.tsm.sim.sysconfig.dao.LogFilterRuleDao;
import com.topsec.tsm.sim.sysconfig.service.AggregatorSend;
import com.topsec.tsm.sim.sysconfig.service.FilterConfigManager;
import com.topsec.tsm.sim.util.NodeUtil;

/**
* 功能描述: 日志源Service层接口
*/
public class DataSourceServiceImpl implements DataSourceService {
	

	private DataSourceDao dataSourceDao;
	private LogFilterRuleDao logFilterRuleDao ;
	private AggregatorRuleDao aggregatorRuleDao;
	private NodeDeployService deployService ;
	private UserDao userDao ;
	public void setDataSourceDao(DataSourceDao dataSourceDao) {
		this.dataSourceDao = dataSourceDao;
	}

	public void setLogFilterRuleDao(LogFilterRuleDao logFilterRuleDao) {
		this.logFilterRuleDao = logFilterRuleDao;
	}
	
	public void setAggregatorRuleDao(AggregatorRuleDao aggregatorRuleDao) {
		this.aggregatorRuleDao = aggregatorRuleDao;
	}

	public void setDeployService(NodeDeployService deployService) {
		this.deployService = deployService;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public SimDatasource get(Long id) throws Exception{
		return dataSourceDao.findById(id);
	}
	
	/**
	* @method: getDataSourceTreeWithNodeList 
	* 		       得到日志源树集合
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:   type: 日志源类型 type="system"为系统日志源;  type="other"为非系统日志源; null为所有日志源
	* @return:  List<Map<String,Object>>:日志源集合   , dataSourceIp,securityObjectType,auditorNodeId,nodeIp,nodeAlias,dataSourceName
	* @exception: Exception
	*/
	@Override
	public List<Map<String,Object>> getDataSourceTreeWithNodeList(String type) throws Exception{
		List<Map<String,Object>> list=dataSourceDao.getDataSourceTreeWithNodeList(type);
		return list;
	}

	@Override
	public List getBlackList() {
		return null;
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
		return dataSourceDao.getDataSource(cmd);
	}

	@Override
	public SimDatasource getFirstByIp(String ip) {
		return dataSourceDao.getFirstByIp(ip,DataSourceStatus.ALL);
	}
	@Override
	public SimDatasource getFirstByIp(String ip, DataSourceStatus status) {
		return dataSourceDao.getFirstByIp(ip,status);
	}

	@Override
	public List<SimDatasource> getByIp(String ip) {
		return dataSourceDao.getByIp(ip);
	}

	@Override
	public void save(SimDatasource datasource)throws ResourceNameExistException,ComponentNotFoundException {
		if(dataSourceDao.isNameExist(datasource.getResourceName())){
			throw new ResourceNameExistException(datasource.getResourceName()) ;
		}
		CollectorType collectType = CollectorTypeUtil.getInstance().getCollectorType(datasource.getCollectMethod()) ;
		if(collectType == null){
			throw new DataSourceException("不支持的收集方式:"+datasource.getCollectMethod()) ;
		}
		if(!collectType.isAllowDupIp() && dataSourceDao.exist(datasource,collectType)){
			throw new DataSourceException("相同IP地址"+collectType.getType()+"收集方式日志源只允许一个！") ;
		}
		dataSourceDao.save(datasource) ;
		sendFilterAndAggregator(datasource) ;
		deployService.sendDataSource(datasource) ;
	}
	
	@Override
	public boolean isResourceNameExist(String name){
		if(dataSourceDao.isNameExist(name)){
			return true;
		}
		return false;
	}
	
	private void sendFilterAndAggregator(SimDatasource dataSource){
		if(dataSource.getRuleId() != null){
			 SimRule rule =  logFilterRuleDao.findById(dataSource.getRuleId());
			 if(rule != null && rule.getAvailable() == 1){
				 FilterConfigManager manager = new FilterConfigManager(dataSource.getAuditorNodeId()) ;
				 manager.enableFilter(rule, dataSource) ;
				 manager.sendFilter() ;
			 }
		}
		if(dataSource.getAggregatorId() != null){
			AggregatorScene aggregatorScene =  aggregatorRuleDao.findById(dataSource.getAggregatorId());
			if(aggregatorScene != null && aggregatorScene.getAvailable() == 1){
				AggregatorSend sender = new AggregatorSend(dataSource.getAuditorNodeId()) ;
	    		sender.enableAggregator(aggregatorScene, dataSource);
	    		sender.sendAggregator() ;
			}
		}
	}
	@Override
	public void update(SimDatasource dataSource)throws ResourceNameExistException, ComponentNotFoundException {
		if(dataSourceDao.isNameExist(dataSource)){
			throw new ResourceNameExistException(dataSource.getResourceName()) ;
		}
		SimDatasource dbDataSource = dataSourceDao.getTransient(dataSource.getResourceId()) ;
		dataSourceDao.update(dataSource) ;
		if(SimDatasource.DATASOURCE_TYPE_LOG.equals(dataSource.getOwnGroup())){
			updateFilter(dataSource, dbDataSource) ;
			updateAggregator(dataSource, dbDataSource) ;
		}
		try {
			deployService.updateDataSource(dataSource) ;
		}catch (ComponentNotFoundException e) {
			Node collectNode = NodeUtil.getCollectNode(dataSource.getNodeId(), (NodeMgrFacade) SpringContextServlet.springCtx.getBean("nodeMgrFacade")) ;
			if(collectNode == null){
				return ;
			}
			Map<String,Object> component = NodeUtil.getComponentByCollectMethod(collectNode, dataSource.getCollectMethod()) ;
			dataSource.setComponentId((Long)component.get("componentId")) ;
			dataSourceDao.update(dataSource) ;
			deployService.updateDataSource(dataSource) ;
		}
	}
	/**
	 * 更新过滤规则
	 * @param dataSource
	 * @param dbDataSource
	 */
	private void updateFilter(SimDatasource dataSource,SimDatasource dbDataSource){
		FilterConfigManager manager = new FilterConfigManager(dataSource.getAuditorNodeId()) ;
		SimRule oldRule = logFilterRuleDao.findById(dbDataSource.getRuleId()) ; 
		SimRule newRule = logFilterRuleDao.findById(dataSource.getRuleId()) ;
		if(oldRule == null && newRule != null){
			manager.enableFilter(newRule, dataSource) ;
		}else if(oldRule != null && newRule == null){
			manager.disableFilter(dataSource) ;
		}else if(oldRule != null && newRule !=null && !oldRule.equals(newRule)){
			manager.disableFilter(dataSource) ;
			manager.enableFilter(newRule, dataSource) ;
		}
		manager.sendFilter() ;
	}
	/**
	 * 更新归并场景
	 * @param dataSource
	 * @param dbDataSource
	 */
	private void updateAggregator(SimDatasource dataSource,SimDatasource dbDataSource){
		AggregatorSend manager = new AggregatorSend(dataSource.getAuditorNodeId()) ;
		AggregatorScene oldAggregator = aggregatorRuleDao.findById(dbDataSource.getAggregatorId()) ; 
		AggregatorScene newAggregator = aggregatorRuleDao.findById(dataSource.getAggregatorId()) ;
		if(oldAggregator == null && newAggregator != null){
			manager.enableAggregator(newAggregator, dataSource) ;
		}else if(oldAggregator != null && newAggregator == null){
			manager.disableAggregator(dataSource) ;
		}else if(oldAggregator !=null && newAggregator != null && !oldAggregator.equals(newAggregator)){
			manager.disableAggregator(dataSource) ;
			manager.enableAggregator(newAggregator,dataSource) ;
		}
		manager.sendAggregator() ;
	}
	
	@Override
	public SimDatasource delete(long id) {
		try {
			SimDatasource dataSource = dataSourceDao.findById(id) ;
			/*if(dataSource.getOwnGroup().equals(SimDatasource.DATASOURCE_TYPE_MONITOR)){
				AlarmPolicyManager apm = new AlarmPolicyManager() ;
				apm.deleteAll(dataSource) ;
			}*/
			if(dataSource == null){
				return null ;
			}
			deployService.deleteDataSource(dataSource) ;
			dataSourceDao.delete(dataSource) ;
			ReportDataManagerUtil.deleteReportData(dataSource) ;
			if(dataSource.getRuleId() != null){
				FilterConfigManager manager = new FilterConfigManager(dataSource.getAuditorNodeId()) ;
				manager.disableFilter(dataSource) ;
				manager.sendFilter() ;
			}
			if(dataSource.getAggregatorId() != null){
				AggregatorSend manager = new AggregatorSend(dataSource.getAuditorNodeId()) ;
				manager.disableAggregator(dataSource) ;
				manager.sendAggregator() ;
			}
			return dataSource ;
		} catch (Exception e) {
			throw new RuntimeException("删除日志源失败",e) ;
		}
	}

	@Override
	public void deleteByIp(String ip) {
		List<SimDatasource> dataSource = dataSourceDao.getByIp(ip) ;
		if (dataSource != null) {
			for(SimDatasource ds:dataSource){
				delete(ds.getResourceId()) ;
			}
		}
	}
	/**
	 * 根据设备类型获取相应的日志源
	 * @param deviceType
	 * @return List<SimDatasource>
	 */
	public List<SimDatasource> getDataSourceByDvcType(String deviceType){
		return dataSourceDao.getByDvcType(deviceType);
	}

	@Override
	public List<String> getDataSourceTypeList() {
		return dataSourceDao.getDataSourceTypeList();
	}

	@Override
	public void switchState(Long id, Integer available){
		SimDatasource dataSource = dataSourceDao.updateState(id,available) ;
		try {
			deployService.updateDataSource(dataSource) ;
		}catch (ComponentNotFoundException e) {
			Node collectNode = NodeUtil.getCollectNode(dataSource.getNodeId(), (NodeMgrFacade) SpringContextServlet.springCtx.getBean("nodeMgrFacade")) ;
			if(collectNode == null){
				return ;
			}
			Map<String,Object> component = NodeUtil.getComponentByCollectMethod(collectNode, dataSource.getCollectMethod()) ;
			dataSource.setComponentId((Long)component.get("componentId")) ;
			dataSourceDao.update(dataSource) ;
			try {
				deployService.updateDataSource(dataSource) ;
			} catch (ComponentNotFoundException e1) {
				e1.printStackTrace() ;
			}
		}
	}

	@Override
	public SimDatasource getById(Long id) {
		return dataSourceDao.findById(id);
	}

	@Override
	public List<SimDatasource> getByAggregatorId(Long aggId) {
		return dataSourceDao.getByAggregatorId(aggId);
	}

	@Override
	public List<String> getDistinctDvcType(String cmd) {
		return dataSourceDao.getDataSourceTypeList();
	}

	@Override
	public List<SimDatasource> getByIPs(String[] ipArray) {
		return dataSourceDao.getByIPs(ipArray);
	}

	@Override
	public SimDatasource findByDeviceTypeAndIp(String securityObjectType,
			String ip) {
		return dataSourceDao.findByDeviceTypeAndIp(securityObjectType, ip);
	}

	@Override
	public List<SimDatasource> getAll(boolean includeMointor,boolean includeAuditLog,boolean includeSystemLog) {
		return dataSourceDao.getAll(includeMointor,includeAuditLog,includeSystemLog);
	}
	
	@Override
	public List<SimDatasource> getUserDataSource(SID sid,boolean operatorExcludeSystemLog) {
		List<SimDatasource> datasourceGroups ;
		if(sid.isOperator()){
			datasourceGroups = dataSourceDao.getAllDataSource(false,!operatorExcludeSystemLog) ;
		}else if(sid.hasOperatorRole()){
			datasourceGroups = dataSourceDao.getUserDataSource(sid);
		}else if(sid.hasAdminRole()){
			datasourceGroups = Collections.emptyList() ;
		}else{
			datasourceGroups = dataSourceDao.getByDvcType(DataSourceUtil.SYSTEM_LOG) ;
		}
		return datasourceGroups ;
	}

	@Override
	public List<SimDatasource> getUserDataSource(SID sid) {
		return getUserDataSource(sid,false);
	}

	@Override
	public List<SimDatasource> getUserDataSource(String userName, boolean operatorExcludeSystemLog) {
		AuthAccount account = userDao.getByName(userName) ;
		if(account == null){
			return Collections.emptyList() ;
		}
		SID sid = new SID(IpAddress.getLocalIp().getLocalhostAddress(), account) ;
		return getUserDataSource(sid,operatorExcludeSystemLog);
	}
	
	@Override
	public List<String> getUserDataSourceAsString(SID sid, boolean operatorExcludeSystemLog) {
		 List<SimDatasource> datasource = getUserDataSource(sid,operatorExcludeSystemLog);
		 List<String> result = new ArrayList<String>() ;
         for(SimDatasource data : datasource){
         	result.add(data.getDeviceIp()+","+data.getSecurityObjectType());
         }
		return result;
	}

	@Override
	public List<SimDatasource> getUserDataSource(String userName) {
		return getUserDataSource(userName, false);
	}
	
	@Override
	public List<SimDatasource> getByNodeId(String nodeId){
		return dataSourceDao.getByNodeId(nodeId) ;
	}
	
	@Override
	public PageBean<SimDatasource> getList(SID sid, int pageIndex, int pageSize, Map<String, Object> searchCondition, SimOrder... orders){
		List<SimDatasource> list = new ArrayList<SimDatasource>();
		List<SimDatasource> templist = new ArrayList<SimDatasource>();
		List<String> authIpList = new ArrayList<String>();
		PageBean<SimDatasource> page = dataSourceDao.search(pageIndex, pageSize, searchCondition, orders);
		String name = (String) searchCondition.get("name");
		String creater = sid.getUserName();
		
		//获取当前用户权限资产
		Iterator<?> resIterator = sid.getUserDevice().iterator();
		while (resIterator.hasNext()) {
			AuthUserDevice authUserDevice=(AuthUserDevice)resIterator.next();
			AssetObject assetObject = AssetFacade.getInstance().getById(authUserDevice.getDeviceId());
			authIpList.add(assetObject.getIp());
		}
		//增加权限
		if ("operator".equals(creater)) {
			templist = page.getData();
		} else {
			for (SimDatasource sd : page.getData()) {
				if (authIpList.contains(sd.getDeviceIp())) {
					templist.add(sd);
				}
			}
		}
		//当根据日志源或者监视对象名称查询时，过滤符合条件的数据
		if (StringUtil.isNotBlank(name)) {
			for (SimDatasource sd : templist) {
				if (sd.getResourceName().contains(name)) {
					list.add(sd);
				}
			}
			page.setData(list);
			page.setTotal(list.size());
		} else {
			page.setData(templist);
			page.setTotal(templist.size());
		}
		return page;
	}
}
