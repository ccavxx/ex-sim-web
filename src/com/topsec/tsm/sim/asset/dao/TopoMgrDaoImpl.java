package com.topsec.tsm.sim.asset.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.topsec.tsm.asset.AssTopology;
import com.topsec.tsm.sim.common.dao.HibernateDaoImpl;
import com.topsec.tsm.ui.topo.util.TopoUtil;

public class TopoMgrDaoImpl extends HibernateDaoImpl<AssTopology, Integer> implements TopoMgrDao {
	
	public AssTopology queryAssTopologyById(Integer viewId) throws Exception{
		return this.findById(viewId);
	}
	public AssTopology queryUniqueAssTopolog() throws Exception{
		return findFirstByCriteria() ;
	}
	public List<AssTopology> queryAssTopologyList(String id) throws Exception{
		return this.getAll();
	}
	public void updateAssTopology(Integer id,AssTopology ass) throws Exception{
		AssTopology topo = this.findById(id);
		if(topo!=null){
			String viewInfo = ass.getViewInfo();
			String viewName = ass.getViewName();
			String states = ass.getStates();
			if(TopoUtil.isNotEmpty(viewInfo)){
				topo.setViewInfo(viewInfo);
			}
			if(TopoUtil.isNotEmpty(viewName)){
				topo.setViewName(viewName);
			}
			if(TopoUtil.isNotEmpty(states)){
				topo.setStates(states);
			}
			this.update(topo);
		}else{
			this.save(ass);
		}
	}
	public AssTopology addAssTopology(AssTopology ass) throws Exception{
		this.save(ass);
		return findFirstByCriteria(Restrictions.eq("viewId", ass.getViewId()));
	}
	public void deleteAssTopology(Integer id) throws Exception{
		this.delete(id);
	}
	@Override
	public void saveOrUpdate(AssTopology topo) {
		getSession().saveOrUpdate(topo) ;
	}
}
