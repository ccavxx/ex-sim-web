package com.topsec.tsm.sim.sysconfig.dao;

import java.util.List;

import com.topsec.tsm.sim.aggregator.AggregatorScene;
import com.topsec.tsm.sim.common.dao.BaseDao;

public interface AggregatorRuleDao extends BaseDao<AggregatorScene, Long>{

	List<AggregatorScene> findByDeviceType(String deviceType);

	List<AggregatorScene> getAggregatorSceneByName(String name);

}