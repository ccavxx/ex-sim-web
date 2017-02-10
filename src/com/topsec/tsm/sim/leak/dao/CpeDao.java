package com.topsec.tsm.sim.leak.dao;

import java.util.List;

import com.topsec.tsm.sim.common.dao.BaseDao;
import com.topsec.tsm.sim.kb.CpeBean;

public interface CpeDao extends BaseDao<CpeBean, Integer>{
	public List<CpeBean> getByName(String cpeName);
}
