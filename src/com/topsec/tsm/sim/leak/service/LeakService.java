package com.topsec.tsm.sim.leak.service;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.sim.kb.Leak;

public interface LeakService {
	public void saveLeaks(Leak leak);
	public PageBean<Leak> getAllLeaks(int pageIndex,int pageSize,Map<String,Object> condition);
	public Leak getLeakById(Integer id);
	public Leak getLeakByName(String name);
	public List<String> getAllYears();
	public List<Leak> getByCpe(String cpe) ;
}
