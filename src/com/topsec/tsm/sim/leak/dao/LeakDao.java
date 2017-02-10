package com.topsec.tsm.sim.leak.dao;


import java.util.List;

import com.topsec.tsm.sim.common.dao.BaseDao;
import com.topsec.tsm.sim.kb.Leak;

public interface LeakDao extends BaseDao<Leak, Integer> {
	public Leak getLeakById(Integer id);
	public Leak getLeakByName(String name);
	public List<String> getAllYears();
}
