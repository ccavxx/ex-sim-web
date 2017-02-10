package com.topsec.tsm.sim.alarm.web;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.node.collector.DataConstants;
import com.topsec.tsm.sim.alarm.bean.AlarmQueryCriteria;
import com.topsec.tsm.sim.alarm.bean.QueryConditionAdapter;
import com.topsec.tsm.sim.alarm.persistence.AlarmCategory;
import com.topsec.tsm.sim.alarm.persistence.SimAlarm;
import com.topsec.tsm.sim.alarm.service.AlarmService;
import com.topsec.tsm.sim.event.EventRule;
import com.topsec.tsm.sim.event.bean.Condition;
import com.topsec.tsm.sim.event.service.EventCategoryService;
import com.topsec.tsm.sim.event.service.EventQueryService;
import com.topsec.tsm.sim.event.util.CategoryOrganizationTemplate;
import com.topsec.tsm.sim.event.util.CategoryOrganizationTemplate.AbstractEndModel;
import com.topsec.tsm.sim.sceneUser.persistence.EventCategory;
import com.topsec.tsm.sim.util.CommonUtils;
import com.topsec.tsm.sim.util.DeviceTypeNameUtil;

@Controller
@RequestMapping("/alarm/*")
public class AlarmController {
	
	protected static final Logger logger=LoggerFactory.getLogger(AlarmController.class);
	//private final static String DEFUALt_COLUMS_SET="EVENT_ID,PRIORITY,DVC_TYPE,NAME,EVENT_TYPE,SRC_ADDRESS,SRC_PORT,DVC_ADDRESS,MESSAGE,TRANS_PROTOCOL,CAT1_ID,CAT2_ID,CAT3_ID,DEST_ADDRESS,DEST_PORT,START_TIME,END_TIME,CUSTOM6,CUSTOM8";‘
	
	protected final static String DEFUALt_COLUMS_SET="PRIORITY,NAME,SRC_ADDRESS,DEST_ADDRESS,DVC_ADDRESS,CAT1_ID,CAT2_ID,CAT3_ID,END_TIME";
	@Autowired
	private AlarmService alarmService;
	
	@Autowired
	private EventCategoryService eventCategoryService;
	
	@Autowired
	private EventQueryService eventQueryService;
	
	@RequestMapping("getRealTimeData")
	@ResponseBody
	public List getRealTimeData(@RequestParam(value = "limit", defaultValue = "10", required = false) Integer limit) throws Exception {
		return alarmService.getDataFromCache(limit);
	}
	
	@RequestMapping("getLevelData")
	@ResponseBody
	public List getLevelData(@RequestParam(value = "unit", defaultValue = "hour", required = false) String timeUnit) throws Exception {
		return alarmService.getLevelData(timeUnit);
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping("todayAlarm")
	@ResponseBody
	public Object todayAlarm(@RequestParam(value="page",defaultValue="1")int page,@RequestParam(value="rows",defaultValue="10")int rows) throws Exception {
		Date now = new Date();
		Date  dayBegin = ObjectUtils.dayBegin(now) ;
		Condition con = new Condition() ;
		con.setColumnsSet(DEFUALt_COLUMS_SET) ;
		con.setStart_time(StringUtil.longDateString(dayBegin)) ;
		con.setEnd_time(StringUtil.longDateString(now)) ;
		con.setAlarmState(1) ;
		con.setPageSize(rows) ;
		con.setSizeStart((page-1)*rows) ;
		List<Map<String,Object>> data = eventQueryService.getEventsForFlex(con) ;
		List<Map<String,Object>> totalList = eventQueryService.getEventsTotalForFlex(con, false) ;
		JSONObject result = new JSONObject() ;
		result.put("total", ((Map<String,Object>)totalList.get(0)).get("value")) ;
		result.put("rows", data) ;
		return result;
	}
	/**
	 * 按优先级统计告警
	 * @author zhaojun 2014-3-11下午5:26:14
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	@RequestMapping("levelStatisticByTime")
	@ResponseBody
	public Object getLevelStatisticByTime(@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") @RequestParam(value="startTime")  Date startTime,
			@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") @RequestParam(value="endTime") Date endTime){
		List<Map<String,Object>> alarmList= alarmService.getAlarmLevelStatisticByTime(startTime,endTime);
		/**
		 * 计算每一种级别的告警
		 */
		String []prioritys=	{DataConstants.PRIORITY_ZERO,DataConstants.PRIORITY_ONE,DataConstants.PRIORITY_TWO,	DataConstants.PRIORITY_THREE,DataConstants.PRIORITY_FOUR};
		List<Map<String, Object>> alarms=new ArrayList<Map<String,Object>>(prioritys.length);
		for (int i = 0; i < prioritys.length; i++) {
			Map<String, Object> alarmMap=new HashMap<String, Object>();
			alarmMap.put("opCount", 0);
			alarmMap.put("priority", i);
			alarmMap.put("p_label", CommonUtils.getLevel(i));
			if(alarmList!=null){
				for (Map<String, Object> map : alarmList) {
					Number priority=(Number) map.get("priority");
					if(priority.intValue()==i){
						alarmMap.put("opCount", map.get("opCount"));
					}
				}
			}
			alarms.add(alarmMap);
		}
		return alarms;
	}
	
	/**
	 * 按设备统计告警
	 * @author zhaojun 2014-3-11下午5:25:39
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	@RequestMapping("devStatisticByTime")
	@ResponseBody
	public Object getDeviceStatisticByTime(@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") @RequestParam(value="startTime")  Date startTime,
			@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") @RequestParam(value="endTime") Date endTime){
			List<Map<String,Object>> alarmList=alarmService.getDevAlarmStatisticByTime(startTime,endTime);
			return alarmList;
		
	}
	
	protected static final String MONTH="month";
	
	
	/**
	 * 告警统计 一个月每天
	 * @author zhaojun 2014-3-11下午5:25:14
	 * @param time
	 * @param scopeType
	 * @return
	 */
	@RequestMapping("scopeStatisticByTime")
	@ResponseBody
	public Object getScopeStatisticByTime(@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") @RequestParam(value="time",required=true) Date time,
			@RequestParam(value="scopeType",defaultValue=MONTH,required=false) String scopeType){
			
			if(scopeType.equals(MONTH)){
				
				/**
				 * 计算一个月时间
				 */
				
				Date endTime=time;
				Date startTime=ObjectUtils.addMonths(endTime, -1);
				List<Map<String,Object>> alarmList=alarmService.getDayStatisticByTime(startTime,endTime);
				/**
				 * 计算一段时间范围
				 */
				Calendar calendar0=DateUtils.toCalendar(startTime);
			    Calendar calendar1=DateUtils.toCalendar(endTime);
			    List<Map<String,Object>> eAlarmList=new ArrayList<Map<String,Object>>();
			    
				int day0=calendar0.get(Calendar.DAY_OF_YEAR);
		    	int day1=calendar1.get(Calendar.DAY_OF_YEAR);
		    	int iday=0;
			    
			    if(calendar0.get(Calendar.YEAR)==calendar1.get(Calendar.YEAR)){//不跨年
			    	iday=day1-day0+1;
			    }else{//跨年
			    	int max0day=calendar0.getActualMaximum(Calendar.DAY_OF_YEAR);
			    	iday=max0day-day0+1+day1;
			    }
			    for (int day =0; day <iday; day++) {
			
					String time1=DateFormatUtils.format(calendar0, "yyyy-MM-dd");
					Map<String, Object> newMap=new HashMap<String, Object>();
					for (Map<String, Object> map : alarmList) {
						if(map.get("time").equals(time1)){
							newMap.putAll(map);
						}
					}
					if(newMap.size()==0){
						newMap.put("time", time1);
						newMap.put("total", 0);
					}
					eAlarmList.add(newMap);
					
					calendar0.set(Calendar.DAY_OF_YEAR, calendar0.get(Calendar.DAY_OF_YEAR)+1);
				}
				return eAlarmList;
			}
		
			/**
			 * 其它类型统计以后再扩充
			 */
			return null;
	}
	
	
	/**
	 * 告警查询
	 * @author zhaojun 2014-3-11下午5:25:01
	 * @param alermq
	 * @return
	 */
	@RequestMapping("queryAlarm")
	@ResponseBody
	public Object getAlarmList(@ModelAttribute AlarmQueryCriteria  alermq){
		QueryConditionAdapter  queryConditionAdapter=new QueryConditionAdapter(alermq);
		Condition condition = queryConditionAdapter.getRequestCondition();
		condition.setColumnsSet(DEFUALt_COLUMS_SET);
		Map<String, Object> pageMap = queryProcess(condition);
		return pageMap;
	}
	
	private Map<String, Object> queryProcess(Condition condition) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			List<Map<String, Object>> eventResult=eventQueryService.getEventsForFlex(condition);
			map.put("rows",eventResult);
			List<Map<String, Object>> totalMaps = eventQueryService.getEventsTotalForFlex(condition,false);//总数
			Map<String, Object> totalMap=totalMaps.get(0);
			map.put("total",totalMap.get("value"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return map;
	}
	/**
	 * 告警规则类型
	 * @author zhaojun 2014-3-17下午4:54:48
	 * @return
	 */
	
	@RequestMapping(value="alarmCategory",produces="text/javascript;charset=UTF-8")
	@ResponseBody
	public Object getAlarmCategory(@RequestParam(value="id",required=false)Integer id){
		
		final CategoryOrganizationTemplate  cgenTemplate=new CategoryOrganizationTemplate(id){
				@Override
				public void extractor(JSONObject jsonObject) {
					String text=jsonObject.getString("text");
					jsonObject.put("text", "<a>"+ text+"</a><img src='/img/icons/loading.gif'>");//设置节点样式
				}
		};
		
		
		JSONArray  jsonArray=cgenTemplate.genDynamicCategoryJson(eventCategoryService, new AbstractEndModel() {
			@Override
			public void level3(JSONArray jsonArray, Map<String, Object> categoryMap) {
				EventCategory currCategory = cgenTemplate.getCurrentCategory();
				if(currCategory!=null&&(currCategory.getParentId()!=null&&currCategory.getParentId()>0)){
					EventCategory pCategory = eventCategoryService.get(currCategory.getParentId());
					Map<String,Object> categoryMapCopy=new HashMap<String, Object>();
					categoryMapCopy.put("cat1id", pCategory.getCategoryName());
					categoryMapCopy.put("cat2id", currCategory.getCategoryName());
					List<Map<String,String>> eventKeyNameMaps=alarmService.getExistedAlarmNames(categoryMapCopy);
					if(eventKeyNameMaps!=null){
						for (Map<String, String> evtKeyNameMap : eventKeyNameMaps) {
							JSONObject  parentJsonObject=new JSONObject();
							String evtName=evtKeyNameMap.get("name");
							parentJsonObject.put("text", evtName);
							JSONObject attributes=new JSONObject();
							attributes.put("type", "3");//三级告警事件
							parentJsonObject.put("attributes", attributes);
							parentJsonObject.put("state","open");
							parentJsonObject.put("id", "3_"+ evtName.hashCode());
							cgenTemplate.extractor(parentJsonObject);
							jsonArray.add(parentJsonObject);
						}
					}
				}
			}
		});
		
		return jsonArray.toJSONString();
	}
}
