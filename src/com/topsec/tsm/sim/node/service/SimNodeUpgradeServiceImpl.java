package com.topsec.tsm.sim.node.service;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.sim.node.dao.SimNodeUpgradeDao;
import com.topsec.tsm.sim.resource.persistence.SimNodeUpgrade;

/*
 * 功能描述：service层升级对象接口实现类
 */
public class SimNodeUpgradeServiceImpl implements SimNodeUpgradeService {
	private SimNodeUpgradeDao simNodeUpgradeDao;

	public SimNodeUpgradeDao getSimNodeUpgradeDao() {
		return simNodeUpgradeDao;
	}

	public void setSimNodeUpgradeDao(SimNodeUpgradeDao simNodeUpgradeDao) {
		this.simNodeUpgradeDao = simNodeUpgradeDao;
	}

	@Override
	public List<SimNodeUpgrade> list() {
		return simNodeUpgradeDao.list();
	}

	@Override
	public List<SimNodeUpgrade> getRecordList(int pageNum, int pageSize) {
		return simNodeUpgradeDao.getRecordList(pageNum, pageSize);
	}

	@Override
	public long getRecordCount() {
		return simNodeUpgradeDao.getRecordCount();
	}

	@Override
	public void save(SimNodeUpgrade simNodeUpgrade) {
		simNodeUpgradeDao.save(simNodeUpgrade);
	}

	@Override
	public void delete(SimNodeUpgrade simNodeUpgrade) {
		simNodeUpgradeDao.delete(simNodeUpgrade);
	}

	@Override
	public Map<String, String> getMaxVersionStrByType(String type, String versionFrom) {
		return simNodeUpgradeDao.getMaxVersionStrByType(type, versionFrom);
	}

	@Override
	public SimNodeUpgrade getSimNodeUpgradeByName(String fileName) {
		return simNodeUpgradeDao.getSimNodeUpgradeByName(fileName);
	}

}
