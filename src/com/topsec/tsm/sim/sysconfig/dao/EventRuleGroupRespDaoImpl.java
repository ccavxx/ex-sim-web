/**
 * 
 */
package com.topsec.tsm.sim.sysconfig.dao;


import com.topsec.tsm.sim.common.dao.HibernateDaoImpl;
import com.topsec.tsm.sim.event.EventRuleGroupResp;

/**
 * @author  zhaojun  2014-8-12 上午9:43:45
 *
 */
public class EventRuleGroupRespDaoImpl extends HibernateDaoImpl<EventRuleGroupResp, Integer>  implements EventRuleGroupRespDao {

	@Override
	public int deleteRuleGroupRspByGid(int groupId) {
		//this.getSession().de
		return this.getSession().createQuery("delete from EventRuleGroupResp eg where eg.groupId=:groupId")
						 .setParameter("groupId", groupId)
						 .executeUpdate();
		
		
	}

 

}
