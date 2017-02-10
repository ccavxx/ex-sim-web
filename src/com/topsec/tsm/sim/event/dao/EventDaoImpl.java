package com.topsec.tsm.sim.event.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.sim.event.bean.Condition;
import com.topsec.tsm.sim.event.bean.EventModel;
import com.topsec.tsm.sim.sceneUser.persistence.EventCategory;

public class EventDaoImpl  implements EventDao   {
	
	private SqlMapClient sqlMapClient;
	
	@Override
	public List<EventCategory> getAllEventCategorys() {
		return null;
	}


	@Override
	public List<Map<String, Integer>> getEventLevelStatistics(Condition condition) throws SQLException {
		return sqlMapClient.queryForList("getEventLevelStatisticsByTime", condition);
	}
	
	@Override
	public List<Map<String, Object>> getEventNameStatistic(String startTime, String endTime) {
		try {
			Condition con = new Condition() ;
			con.setStart_time(startTime) ;
			con.setEnd_time(endTime) ;
			return sqlMapClient.queryForList("nameStatistics",con);
		} catch (SQLException e) {
			e.printStackTrace();
			return Collections.emptyList() ;
		}
	}


	@Override
	public List<Map<String, Integer>> getEventCategoryStatistics(Condition condition) throws SQLException {
		return sqlMapClient.queryForList("getEventCategoryStatisticsByTime", condition);
	}
	
	@Override
	public PageBean<EventModel> getByIp(int pageIndex, int pageSize, String ip,Date startTime, Date endTime) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("startTime", startTime) ;
		params.put("endTime", endTime) ;
		params.put("pageSize", pageSize) ;
		params.put("beginIndex", (pageIndex-1)*pageSize) ;
		params.put("dvc_address", ip) ;
		try {
			Integer totalCount = (Integer) sqlMapClient.queryForObject("getTotalByIP", params) ;
			if (totalCount != null && totalCount == 0) {
				return new PageBean<EventModel>(pageIndex,pageSize,0) ;
			}
			PageBean<EventModel> page = new PageBean<EventModel>(pageIndex, pageSize, totalCount) ;
			List<Map> datas = sqlMapClient.queryForList("getByIP", params) ;
			List<EventModel> events = new ArrayList<EventModel>(datas.size()) ;
			for(Map record:datas){
				events.add(EventModel.createEvent(record)) ;
			}
			page.setData(events) ;
			return page ;
		} catch (SQLException e) {
			e.printStackTrace() ;
			return new PageBean<EventModel>(pageIndex, pageSize, 0);
		}
	}


	public Object getEventAlarmCount(Condition condition){
		try {
			return sqlMapClient.queryForObject("getEventAlarmCount", condition);
		} catch (SQLException e) {
			e.printStackTrace();
			return Collections.EMPTY_MAP; 
		}
	}
	
	public List<Map> getAlarmCount(Condition condition){
		try {
			return sqlMapClient.queryForList("getAlarmCount", condition);
		} catch (SQLException e) {
			e.printStackTrace();
			return Collections.emptyList(); 
		}
	}
	
	public List<Map<String, Object>> getEventStatistic(Date beginDate,Date endDate){
		try {
			Map<String,Object> param = new HashMap<String,Object>(2) ;
			param.put("start_time", beginDate) ;
			param.put("end_time", endDate) ;
			return sqlMapClient.queryForList("getDayEventStatistic",param);
		} catch (SQLException e) {
			e.printStackTrace();
			return Collections.emptyList(); 
		}
	}
	
	public List<Map> getEventCount(Condition condition){
		try {
			return sqlMapClient.queryForList("getEventCount", condition);
		} catch (SQLException e) {
			e.printStackTrace();
			return Collections.emptyList(); 
		}
	}
	
	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}

	@Override
	public List<Map<String, Object>> getEventRiverDataByTime(String startTime, String endTime) {
		try {
			Map<String,Object> param = new HashMap<String,Object>(2) ;
			param.put("start_time", startTime) ;
			param.put("end_time", endTime) ;
			return sqlMapClient.queryForList("getEventRiverDataByTime",param);
		} catch (SQLException e) {
			e.printStackTrace();
			return Collections.emptyList(); 
		}
	}

}
