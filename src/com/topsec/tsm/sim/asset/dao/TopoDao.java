package com.topsec.tsm.sim.asset.dao;

import java.util.List;

import com.topsec.tsm.asset.AssTopo;
import com.topsec.tsm.sim.common.dao.BaseDao;

public interface TopoDao extends BaseDao<AssTopo, Integer>{
	
	public void saveOrUpdate(AssTopo topo) ;
	
	public AssTopo getByName(String name) ;

	public List<AssTopo> getUserTopoList(String userName);
}
