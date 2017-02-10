package com.topsec.tsm.sim.sysconfig.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.sim.asset.dao.DataSourceDao;
import com.topsec.tsm.sim.common.dao.SimOrder;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.rule.SimRule;
import com.topsec.tsm.sim.sysconfig.dao.LogFilterRuleDao;

public class LogFilterRuleServiceImpl implements LogFilterRuleService {
	public static final int ENABLE_FILTER = 1 ;
	public static final int DISABLE_FILTER = 0 ;
	public static final int REMOVE_FILTER = -1 ;
	
	private LogFilterRuleDao logFilterRuleDao;
	private DataSourceDao dataSourceDao;

	/* (non-Javadoc)
	 * @see com.topsec.tsm.sim.log.service.LogFilterRuleService#getList(int, int, java.util.Map, com.topsec.tsm.sim.common.dao.SimOrder)
	 */
	public PageBean<SimRule> getList(int pageIndex, int pageSize,Map<String, Object> searchCondition, SimOrder... orders){
		return logFilterRuleDao.search(pageIndex, pageSize, searchCondition, orders);
	}

	public LogFilterRuleDao getLogFilterRuleDao() {
		return logFilterRuleDao;
	}

	public void setLogFilterRuleDao(LogFilterRuleDao logFilterRuleDao) {
		this.logFilterRuleDao = logFilterRuleDao;
	}

	public DataSourceDao getDataSourceDao() {
		return dataSourceDao;
	}

	public void setDataSourceDao(DataSourceDao dataSourceDao) {
		this.dataSourceDao = dataSourceDao;
	}

	@Override
	public void saveSimRule(SimRule rule) {
		logFilterRuleDao.save(rule);
	}

	@Override
	public SimRule deleteSimRule(Long id) {
		SimRule rule = logFilterRuleDao.findById(id);
		logFilterRuleDao.delete(rule);
		Map<String,FilterConfigManager> nodeFilterManager = new HashMap<String,FilterConfigManager>() ;
		try {
			resetNodeFilterManager(rule, REMOVE_FILTER, nodeFilterManager) ;
			for(FilterConfigManager mgr:nodeFilterManager.values()){
				mgr.sendFilter() ;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rule ;
	}

	@Override
	public void updateSimRule(SimRule rule) {
		logFilterRuleDao.update(rule);		
		Map<String,FilterConfigManager> nodeFilterManager = new HashMap<String,FilterConfigManager>() ;
		int operation = rule.getAvailable() == 1 ? ENABLE_FILTER : DISABLE_FILTER ;
		resetNodeFilterManager(rule,operation , nodeFilterManager) ;
		for(FilterConfigManager mgr:nodeFilterManager.values()){
			mgr.sendFilter() ;
		}
	}
	

	@Override
	public SimRule getSimRule(Long id) {
		return logFilterRuleDao.findById(id);
	}
	
	/**
	 * 将过滤器中对应的所有数据源按结点进行分组管理<br/>
	 * 启用、禁用、删除各节点数据源上的过滤器
	 * @param rule 过滤器
	 * @param operation 操作:启用(1)或者禁用(0)删除(-1)
	 * @param nodeFilterManager 所有节点的过滤器管理对象
	 * @throws Exception
	 */
	private void resetNodeFilterManager(SimRule rule,int operation,Map<String,FilterConfigManager> nodeFilterManager){
		List<SimDatasource> ruleDataSources = dataSourceDao.getDatasourcesByRuleId(rule.getId()) ; 
		if(ObjectUtils.isNotEmpty(ruleDataSources)){
			for(SimDatasource dataSource:ruleDataSources){
				FilterConfigManager manager = nodeFilterManager.get(dataSource.getAuditorNodeId()) ;
				if(manager == null){
					manager = new FilterConfigManager(dataSource.getAuditorNodeId()) ;
					nodeFilterManager.put(dataSource.getAuditorNodeId(), manager) ;
				}
				switch(operation){
					case ENABLE_FILTER : manager.enableFilter(rule, dataSource) ; break; 
					case DISABLE_FILTER: manager.disableFilter(dataSource) ; break; 
					case REMOVE_FILTER :{
						manager.disableFilter(dataSource) ;
						dataSource.setRuleId(null) ;
						break; 
					}
					default : throw new RuntimeException("无效的operation值："+operation) ;
				}
			}
		}
	}

	/**
	 * 将过滤器中数据源ID对应的所有数据源以结点为单位进行查询<br/>
	 * 启用、禁用、删除各节点数据源上的过滤器
	 * @param deviceType 数据源ID
	 * @throws Exception
	 */
	@Override
	public List<SimRule> getByDeviceObjectType(String deviceType) {
		return logFilterRuleDao.findByDeviceType(deviceType);
	}

	@Override
	public List<SimRule> getSimRuleByName(String name) {
		return logFilterRuleDao.findSimRuleByName(name);
	}
}
