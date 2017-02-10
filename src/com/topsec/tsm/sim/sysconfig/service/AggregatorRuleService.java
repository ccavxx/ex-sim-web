package com.topsec.tsm.sim.sysconfig.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.sim.aggregator.AggregatorScene;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.common.dao.SimOrder;

public interface AggregatorRuleService {

	public PageBean<AggregatorScene> getList(int pageIndex, int pageSize, Map<String, Object> searchCondition, SimOrder... orders);
	
	public AggregatorScene getAggregatorScene(Long id);

	public List<AggregatorScene> getByDeviceObjectType(String deviceType) ;

	public AggregatorScene deleteAggregatorRule( DataSourceService dataSourceService, Long id);

	public Serializable saveAggregatorScene(AggregatorScene aggregatorScene);

	public void updateAggregatorScene( DataSourceService dataSourceService, AggregatorScene aggregatorScene);

	public void updateAggregatorSceneAvailable(	DataSourceService dataSourceService, AggregatorScene aggregatorScene, Integer available);

	public List<AggregatorScene> getAggregatorSceneByName(String name);
}