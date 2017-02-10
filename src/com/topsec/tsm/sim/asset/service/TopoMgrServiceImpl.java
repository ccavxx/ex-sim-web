package com.topsec.tsm.sim.asset.service;

import java.util.List;

import com.topsec.tsm.asset.AssTopology;
import com.topsec.tsm.sim.asset.dao.TopoMgrDao;

public class TopoMgrServiceImpl implements TopoMgrService{
	private TopoMgrDao topoMgrDao;

	public void setTopoMgrDao(TopoMgrDao topoMgrDao) {
		this.topoMgrDao = topoMgrDao;
	}

	@Override
	public AssTopology queryAssTopologyById(Integer viewId) throws Exception {
		return this.topoMgrDao.queryAssTopologyById(viewId);
	}

	@Override
	public List<AssTopology> queryAssTopologyList(String id) throws Exception {
		return topoMgrDao.queryAssTopologyList(id);
	}

	@Override
	public void updateAssTopology(Integer id, AssTopology ass) throws Exception {
		topoMgrDao.updateAssTopology(id, ass);
	}

	@Override
	public AssTopology addAssTopology(AssTopology ass) throws Exception {
		return  topoMgrDao.addAssTopology(ass);
	}

	@Override
	public AssTopology queryUniqueAssTopolog() throws Exception {
		return topoMgrDao.queryUniqueAssTopolog();
	}

	@Override
	public void deleteAssTopology(Integer id) throws Exception {
		topoMgrDao.deleteAssTopology(id);
	}
}
