/**
 * 
 */
package com.topsec.tsm.sim.sysconfig.dao;

import com.topsec.tsm.sim.common.dao.BaseDao;
import com.topsec.tsm.sim.event.EventRuleGroupResp;

/**
 * @author  zhaojun  2014-8-12 上午9:42:44
 *
 */
public interface EventRuleGroupRespDao  extends BaseDao<EventRuleGroupResp, Integer>{

	int deleteRuleGroupRspByGid(int groupId);
}
