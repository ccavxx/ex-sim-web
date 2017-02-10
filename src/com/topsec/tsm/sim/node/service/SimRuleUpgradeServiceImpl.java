package com.topsec.tsm.sim.node.service;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.sim.node.dao.SimRuleUpgradeDao;
import com.topsec.tsm.sim.resource.persistence.SimRuleUpgrade;

/*
 * 功能描述：service层事件规则库升级对象接口实现类
 */
public class SimRuleUpgradeServiceImpl implements SimRuleUpgradeService {
	private SimRuleUpgradeDao SimRuleUpgradeDao;

	public SimRuleUpgradeDao getSimRuleUpgradeDao() {
		return SimRuleUpgradeDao;
	}

	public void setSimRuleUpgradeDao(SimRuleUpgradeDao SimRuleUpgradeDao) {
		this.SimRuleUpgradeDao = SimRuleUpgradeDao;
	}

	@Override
	public List<SimRuleUpgrade> list() {
		return SimRuleUpgradeDao.list();
	}

	@Override
	public List<SimRuleUpgrade> getRecordList(int pageNum, int pageSize) {
		return SimRuleUpgradeDao.getRecordList(pageNum, pageSize);
	}

	@Override
	public Long getRecordCount() {
		return SimRuleUpgradeDao.getRecordCount();
	}

	@Override
	public void save(SimRuleUpgrade SimRuleUpgrade) {
		SimRuleUpgradeDao.save(SimRuleUpgrade);
	}

	@Override
	public void delete(SimRuleUpgrade SimRuleUpgrade) {
		SimRuleUpgradeDao.delete(SimRuleUpgrade);
	}

	@Override
	public Map<String, String> getMaxVersion() {
		return SimRuleUpgradeDao.getMaxVersion();
	}

	@Override
	public SimRuleUpgrade getSimRuleUpgradeByName(String fileName) {
		return SimRuleUpgradeDao.getSimRuleUpgradeByName(fileName);
	}

}
