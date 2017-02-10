package com.topsec.tsm.sim.leak.service;

import java.util.List;

import com.topsec.tsm.sim.kb.CpeBean;

public interface CpeService {
	
	public void save(CpeBean cpe);
	
	public List<CpeBean> getByName(String cpeName);
	
}
