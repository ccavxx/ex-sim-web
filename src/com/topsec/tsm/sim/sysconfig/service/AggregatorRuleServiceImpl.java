package com.topsec.tsm.sim.sysconfig.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.sim.aggregator.AggregatorScene;
import com.topsec.tsm.sim.asset.dao.DataSourceDao;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.common.dao.SimOrder;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.sysconfig.dao.AggregatorRuleDao;

public class AggregatorRuleServiceImpl implements AggregatorRuleService {

	private AggregatorRuleDao aggregatorRuleDao;
	private DataSourceDao dataSourceDao;

	public PageBean<AggregatorScene> getList(int pageIndex, int pageSize,Map<String, Object> searchCondition, SimOrder... orders){
		return this.getAggregatorRuleDao().search(pageIndex, pageSize, searchCondition, orders);
	}

	public AggregatorRuleDao getAggregatorRuleDao() {
		return aggregatorRuleDao;
	}

	public void setAggregatorRuleDao(AggregatorRuleDao aggregatorRuleDao) {
		this.aggregatorRuleDao = aggregatorRuleDao;
	}

	public DataSourceDao getDataSourceDao() {
		return dataSourceDao;
	}

	public void setDataSourceDao(DataSourceDao dataSourceDao) {
		this.dataSourceDao = dataSourceDao;
	}

	@Override
	public AggregatorScene getAggregatorScene(Long id) {
		return this.getAggregatorRuleDao().findById(id);
	}

	/**
	 * 将归并中数据源ID对应的所有数据源以结点为单位进行查询<br/>
	 * 启用、禁用、删除各节点数据源上的归并规则
	 * @param deviceType 数据源ID
	 * @throws Exception
	 */
	@Override
	public List<AggregatorScene> getByDeviceObjectType(String deviceType) {
		return this.getAggregatorRuleDao().findByDeviceType(deviceType);
	}

	@Override
	public AggregatorScene deleteAggregatorRule( DataSourceService dataSourceService, Long id) {
		AggregatorScene aggregatorScene = aggregatorRuleDao.findById(id);
		//如果归并场景已绑定到相应的日志源上，则重新下发归并场景
		deleteOrUpdate( dataSourceService, aggregatorScene,AggregatorSend.DELETE);
		aggregatorRuleDao.delete(id);
		return aggregatorScene;
	}

	@Override
	public Serializable saveAggregatorScene(AggregatorScene aggregatorScene) {
		return this.getAggregatorRuleDao().save(aggregatorScene);
	}

	@Override
	public void updateAggregatorScene( DataSourceService dataSourceService, AggregatorScene aggregatorScene) {
		//如果归并场景已绑定到相应的日志源上，则重新下发归并场景
		deleteOrUpdate( dataSourceService, aggregatorScene, AggregatorSend.MODIFY);
		this.getAggregatorRuleDao().update(aggregatorScene);
	}
	
	@Override
	public void updateAggregatorSceneAvailable( DataSourceService dataSourceService, AggregatorScene aggregatorScene, Integer available) {
		boolean updateFlag = false;
		if(available==1){
			updateAvailable( dataSourceService, aggregatorScene, AggregatorSend.MODIFY);
			updateFlag = true;
		}else if(available==0){
			updateAvailable( dataSourceService, aggregatorScene,AggregatorSend.DELETE);
			updateFlag = true;
		}
		if(updateFlag){
			this.getAggregatorRuleDao().update(aggregatorScene);
		}
	}

	/**
	 * 规则删除修改下发·如果归并场景已绑定到相应的日志源上，则重新下发归并场景
	 */
	public void deleteOrUpdate(DataSourceService dataSourceService, AggregatorScene aggregatorScene,String operation) {
		List<SimDatasource> sList = dataSourceService.getByAggregatorId(aggregatorScene.getId());
		if(ObjectUtils.isNotEmpty(sList)){
			Map<String,AggregatorSend> nodeAggregatorSenders = new HashMap<String, AggregatorSend>() ;
			for (SimDatasource datasource : sList) {
				if(AggregatorSend.DELETE.equals(operation)){
					datasource.setAggregatorId(null) ;
				}
				String nodeId = datasource.getAuditorNodeId() ;
				AggregatorSend sender = nodeAggregatorSenders.get(nodeId) ;
				if (sender == null) {
					sender = new AggregatorSend(nodeId) ;
					nodeAggregatorSenders.put(nodeId, sender) ;
				}
				if(AggregatorSend.DELETE.equals(operation)){
					sender.disableAggregator(datasource) ;
				}else if(aggregatorScene.getAvailable() == 0){
					sender.disableAggregator(datasource) ;
				}else{
					sender.enableAggregator(aggregatorScene, datasource) ;
				}
			}
			for(AggregatorSend sd:nodeAggregatorSenders.values()){
				sd.sendAggregator() ;
			}
		}
	}
	
	/**
	 * 规则更新状态下发·如果归并场景已绑定到相应的日志源上，则重新下发归并场景
	 */
	public void updateAvailable( DataSourceService dataSourceService, AggregatorScene aggregatorScene,String operation) {
		List<SimDatasource> sList = dataSourceService.getByAggregatorId(aggregatorScene.getId());
		if(ObjectUtils.isNotEmpty(sList)){
			Map<String,AggregatorSend> nodeAggregatorSenders = new HashMap<String, AggregatorSend>() ;
			for (SimDatasource datasource : sList) {
				String nodeId = datasource.getAuditorNodeId() ;
				AggregatorSend sender = nodeAggregatorSenders.get(nodeId) ;
				if (sender == null) {
					sender = new AggregatorSend(nodeId) ;
					nodeAggregatorSenders.put(nodeId, sender) ;
				}
				if(aggregatorScene.getAvailable() == 0){
					sender.disableAggregator(datasource) ;
				}else{
					sender.enableAggregator(aggregatorScene, datasource) ;
				}
			}
			for(AggregatorSend sd:nodeAggregatorSenders.values()){
				sd.sendAggregator() ;
			}
		}
	}

	/**
	 * 通过 name 获得 aggregatorScene 对象
	 */
	public List<AggregatorScene> getAggregatorSceneByName(String name) {
		return this.getAggregatorRuleDao().getAggregatorSceneByName(name);
	}
}
