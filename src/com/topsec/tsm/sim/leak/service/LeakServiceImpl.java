package com.topsec.tsm.sim.leak.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.sim.kb.CpeBean;
import com.topsec.tsm.sim.kb.Leak;
import com.topsec.tsm.sim.leak.dao.CpeDao;
import com.topsec.tsm.sim.leak.dao.LeakDao;

public class LeakServiceImpl implements LeakService{
	private LeakDao leakDao;
	private CpeDao cpeDao ;

	public LeakDao getLeakDao() {
		return leakDao;
	}
	
	public void setCpeDao(CpeDao cpeDao) {
		this.cpeDao = cpeDao;
	}
	
	public void setLeakDao(LeakDao leakDao) {
		this.leakDao = leakDao;
	}

	@Override
	public void saveLeaks(Leak leak) {
		leakDao.save(leak);
	}

	@Override
	public PageBean<Leak> getAllLeaks(int pageIndex,int pageSize,Map<String,Object> condition) {
		 return leakDao.search(pageIndex, pageSize, condition);
	}

	@Override
	public Leak getLeakByName(String name) {
		return leakDao.getLeakByName(name);
	}

	@Override
	public List<String> getAllYears() {
		return leakDao.getAllYears();
	}

	@Override
	public Leak getLeakById(Integer id) {
		return leakDao.getLeakById(id);
	}

	@Override
	public List<Leak> getByCpe(String cpeName) {
		List<CpeBean> cpes = cpeDao.getByName(cpeName);
		List<Leak> leaks = new ArrayList<Leak>();
		if(cpes != null){
			for(CpeBean cpeBean : cpes){
				Leak leak = getLeakById(cpeBean.getLeakId());
				leaks.add(leak);
			}
		}
		return leaks;
	}
	
}
