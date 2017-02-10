package com.topsec.tsm.sim.asset.dao;

import java.util.List;

import com.topsec.tsm.asset.AssTopology;
import com.topsec.tsm.sim.common.dao.BaseDao;

public interface TopoMgrDao extends BaseDao<AssTopology, Integer>{
	public AssTopology queryAssTopologyById(Integer viewId) throws Exception;
	public List<AssTopology> queryAssTopologyList(String id) throws Exception;
	public void updateAssTopology(Integer id,AssTopology ass) throws Exception;
	public AssTopology addAssTopology(AssTopology ass) throws Exception;
	public AssTopology queryUniqueAssTopolog() throws Exception;
	public void deleteAssTopology(Integer id) throws Exception;
	
	public void saveOrUpdate(AssTopology topo) ;
}
