package com.topsec.tsm.sim.asset.service;

import java.util.List;

import com.topsec.tsm.asset.AssTopology;

public interface TopoMgrService {
	public AssTopology queryAssTopologyById(Integer viewId) throws Exception;
	public List<AssTopology> queryAssTopologyList(String id) throws Exception;
	public void updateAssTopology(Integer id,AssTopology ass) throws Exception;
	public AssTopology addAssTopology(AssTopology ass) throws Exception;
	public AssTopology queryUniqueAssTopolog() throws Exception;
	public void deleteAssTopology(Integer id) throws Exception;
}
