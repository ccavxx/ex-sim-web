package com.topsec.tsm.sim.event.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.topsec.tsm.sim.event.bean.Condition;




public interface EventQueryDao {
	public List getEvents(Condition searchParameter)throws SQLException;
	public List getAdvancedEvents(Condition searchParameter)throws SQLException;
	public List selectEventForPie(Condition searchParameter)throws SQLException;
	/* modify by yangxuanjia at 2011-01-27 start */
    public List<Map> getCorrelatorData(Condition searchParameter)throws SQLException;
    /* modify by yangxuanjia at 2011-01-27 end */
    public List getEventsForFlex(Condition searchParameter)throws SQLException;
    
    public List getEventsTimeChart(Condition searchParameter)throws SQLException;
    /**
     * 根据条件查询事件集合总数
     * @param searchParameters
     * @return
     */
   public Integer getEventsTotalForFlex(Condition params)throws SQLException;
   
   /**
    * 按照事件名称统计事件发生次数
    * @return
 * @throws SQLException 
    */
   public List getEventStatistics() throws SQLException;
   
   public Integer getEventStatisticsByCategory(Map<String,String> categoryMap)throws SQLException;
   
   public List<Map<String, Object>> getAllEventStatistics()throws SQLException;
   
   public List<Map<String, Object>> getExistedEventNames(Map<String, Object> categoryMap);
   
   public List<Map<String, Object>> getEventLogsByEvtId(Integer id);
   
   public List<Map<String, Object>> getLogsByUUID(String uuid);
   
   public List<Map<String, Object> >getEventMoreStatisticsByCategory(Map<String, String> categoryMap);
   
   public List<Map<String, Object>> getEventsTotalForMoreResult(Condition params);
   
	public List<Map<String,Object>> cat1Statistic() ;
	
	public List<Map<String,Object>> cat2Statistic(String cat1) ;
	
	public List<Map<String, Object>> nameStatBaseOnCat(String cat1, String cat2);
	
	public void update(String event_id, Integer confirm, String confirm_person);
	
}
