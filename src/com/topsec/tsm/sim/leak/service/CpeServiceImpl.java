package com.topsec.tsm.sim.leak.service;

import java.util.List;

import com.topsec.tsm.sim.kb.CpeBean;
import com.topsec.tsm.sim.leak.dao.CpeDao;

public class CpeServiceImpl implements CpeService{
	private CpeDao cpeDao;
	public CpeDao getCpeDao() {
		return cpeDao;
	}
	public void setCpeDao(CpeDao cpeDao) {
		this.cpeDao = cpeDao;
	}
	@Override
	public void save(CpeBean cpe) {
		cpeDao.save(cpe);
	}
	
	@Override
	public List<CpeBean> getByName(String cpeName) {
		return cpeDao.getByName(cpeName);
	}

}
