package com.topsec.tsm.sim.sysconfig.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.aggregator.AggregatorScene;
import com.topsec.tsm.sim.common.dao.HibernateDaoImpl;

public class AggregatorRuleDaoImpl extends HibernateDaoImpl<AggregatorScene, Long> implements AggregatorRuleDao {

	@Override
	public List<AggregatorScene> findByDeviceType(String deviceType) {
		return findByCriteria(Restrictions.eq("deviceType", deviceType));
	}

	@Override
	public List<AggregatorScene> getAggregatorSceneByName(String name) {
		return findByCriteria(Restrictions.eq("name", name));
	}

	@Override
	protected Criterion[] getSearchCriterions(Map<String, Object> searchCondition) {
		List<Criterion> criterion = new ArrayList<Criterion>();
		String creater = (String) searchCondition.get("creater");
		if(StringUtil.isNotBlank(creater)) {
			criterion.add(Restrictions.eq("creater", creater));
		}
		return criterion.toArray(new Criterion[]{});
	}
}