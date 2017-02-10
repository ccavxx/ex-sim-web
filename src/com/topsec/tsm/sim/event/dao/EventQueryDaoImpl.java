package com.topsec.tsm.sim.event.dao;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.ibatis.sqlmap.client.SqlMapClient;
import com.topsec.tsm.sim.event.bean.Condition;

public class EventQueryDaoImpl implements EventQueryDao{
	
 

	private SqlMapClient sqlMapClient;

	public EventQueryDaoImpl() {
	/*	super();
		
		String beanStr = "sqlMap_" + ModuleDaoFactory.getDefaultDbType();
		ApplicationContext ctx = new ClassPathXmlApplicationContext("app-eventQuery.xml");
		sqlMapClient = (SqlMapClient) ctx.getBean(beanStr);*/
	}

	public SqlMapClient getSqlMapClient() {
		return sqlMapClient;
	}

	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}

	public List getAdvancedEvents(Condition searchParameter) throws SQLException {
		List list=sqlMapClient.queryForList("selectListByAdvanced", searchParameter);
		return list;
	}
	
	public List selectEventForPie(Condition searchParameter) throws SQLException {
		List list=sqlMapClient.queryForList("selectEventForPie", searchParameter);
		return list;
	}

	public List getEvents(Condition params)throws SQLException {
//		params.setStart_time("'"+params.getStart_time()+"'");
//		params.setEnd_time("'"+params.getEnd_time()+"'");
		List list=sqlMapClient.queryForList("selectListByCondition", params);
		return list;
	}
	public List getEventsForFlex(Condition params)throws SQLException {
		List list=sqlMapClient.queryForList("selectEventByConditionForFlex", params);
		return list;
	}
	
	public List getEventsTimeChart(Condition params)throws SQLException {
		List list=sqlMapClient.queryForList("selectEventTimeChart", params);
		return list;
	}
	public Integer getEventsTotalForFlex(Condition params)throws SQLException {
		Integer total = (Integer)sqlMapClient.queryForObject("selectEventTotalByConditionForFlex", params);
		return total;
	}
	
	/* modify by yangxuanjia at 2011-01-27 start */
    @Override
    public List<Map> getCorrelatorData(Condition searchParameter)
            throws SQLException {
        List<Map> list=sqlMapClient.queryForList("getCorrelatorData", searchParameter);
        return list;
    }
    /**
     * 按照事件名称统计事件发生次数
     * @return
     * @throws SQLException 
     */
    public List getEventStatistics() throws SQLException{
    	List list=sqlMapClient.queryForList("getEventStatistics", null);
		return list;
    }
    /* modify by yangxuanjia at 2011-01-27 end */

	@Override
	public Integer getEventStatisticsByCategory(Map<String, String> categoryMap) throws SQLException {
		Integer total =  (Integer) sqlMapClient.queryForObject("getEventStatisticsByCategory", categoryMap);
		return total;
	}

	@Override
	public List<Map<String, Object>> getAllEventStatistics() throws SQLException {
		return sqlMapClient.queryForList("getAllEventStatistics");
	}

	@Override
	public List<Map<String, Object>> getExistedEventNames(Map<String, Object> categoryMap) {
		try {
			return sqlMapClient.queryForList("getExistedEvents", categoryMap);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	@Override
	public List<Map<String, Object>> getEventLogsByEvtId(Integer id) {
	    Map<String,Object> pmap=new HashMap<String, Object>();
	    pmap.put("event_id", id);
		try {
			return sqlMapClient.queryForList("getEventRelaLogs", pmap);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	@Override
	public List<Map<String, Object>> getLogsByUUID(String uuid) {
		try {
			return sqlMapClient.queryForList("getLogsByUUID", uuid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	@Override
	public List<Map<String, Object>> getEventMoreStatisticsByCategory(Map<String, String> categoryMap) {
		try {
			return  sqlMapClient.queryForList("getEventMoreStatisticsByCategory", categoryMap);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	@Override
	public List<Map<String, Object>> getEventsTotalForMoreResult(Condition params) {
		
		try {
			return sqlMapClient.queryForList("selectEventTotalByConditionForMoreResult", params);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	@Override
	public List<Map<String, Object>> cat1Statistic() {
		try {
			return sqlMapClient.queryForList("cat1Statistic");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return Collections.emptyList() ;
	}

	@Override
	public List<Map<String, Object>> cat2Statistic(String cat1) {
		try {
			Condition con = new Condition() ;
			con.setCat1_id(cat1) ;
			return sqlMapClient.queryForList("cat2Statistic", con);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return Collections.emptyList() ;
	}

	@Override
	public List<Map<String, Object>> nameStatBaseOnCat(String cat1, String cat2) {
		try {
			Condition con = new Condition() ;
			con.setCat1_id(cat1) ;
			con.setCat2_id(cat2) ;
			return sqlMapClient.queryForList("nameStatBaseOnCat", con);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return Collections.emptyList() ;
	}

	@Override
	public void update(String event_id, Integer confirm, String confirm_person) {
		Map<String,Object> param = new HashMap<String, Object>(3) ;
		param.put("event_id", event_id) ;
		param.put("confirm", confirm) ;
		param.put("confirm_person", confirm_person) ;
		try {
			sqlMapClient.update("updateEvent", param) ;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}