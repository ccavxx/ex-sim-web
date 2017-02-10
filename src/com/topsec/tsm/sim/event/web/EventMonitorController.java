package com.topsec.tsm.sim.event.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.springframework.web.util.HtmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.html.HtmlEscapers;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.common.formatter.HtmlEscapeFormatter;
import com.topsec.tsm.filter.bd.FilterMgrBD;
import com.topsec.tsm.framework.BDFactory;
import com.topsec.tsm.framework.interfaces.TypeDef;
import com.topsec.tsm.framework.selector.ParseException;
import com.topsec.tsm.framework.selector.Selector;
import com.topsec.tsm.framework.selector.Selectorable;
import com.topsec.tsm.framework.util.TypeDefFactory;
import com.topsec.tsm.node.collector.DataConstants;
import com.topsec.tsm.resource.SystemDefinition;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.event.EventRule;
import com.topsec.tsm.sim.event.EventRuleGroup;
import com.topsec.tsm.sim.sysconfig.service.EventRuleService;
import com.topsec.tsm.sim.util.SessionCacheImpl;
import com.topsec.tsm.sim.util.SessionListener;
import com.topsec.tsm.sim.util.SessionMrg;

@Controller
@RequestMapping("eventMonitor")
public class EventMonitorController {
	protected static Logger log= LoggerFactory.getLogger(EventMonitorController.class);
	
	@Autowired
	private EventRuleService eventRuleService;
	private SID sid;

	

	@RequestMapping(value="jsondata" ,produces="text/javascript;charset=UTF-8")
	@ResponseBody
	public String getJsonFile(@RequestParam(value="json",required=true)String name){
		String json = null;
		try {
			
		    InputStream jsonStream = this.getClass().getClassLoader().getResourceAsStream("resource/ui/event/"+name+".json");
			json = IOUtils.toString(jsonStream,"utf-8");
			IOUtils.closeQuietly(jsonStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return JSONArray.parseArray(json).toJSONString();
	}
	
	/**
	 * 获取事件规则
	 * @author zhaojun 2014-2-13下午3:18:24
	 * @return
	 */
	@RequestMapping(value="eventRule")
	@ResponseBody
	public Object getEventRule(){
		List<EventRuleGroup> eventRuleGroups= eventRuleService.getAllEventRuleGroups();
		List<Map<String,Object>> rules=new ArrayList<Map<String,Object>>();
		if(eventRuleGroups!=null){
			for (EventRuleGroup eventRuleGroup : eventRuleGroups) {
				Map<String,Object> e=new HashMap<String, Object>();
				e.put("id", eventRuleGroup.getGroupId());
				e.put("text", eventRuleGroup.getGroupName());
				rules.add(e);
			}
		}
		return rules;
	}
	
	/**
	 * 获取事件数据
	 * @author zhaojun 2014-2-13下午3:19:04
	 * @return
	 */
	@RequestMapping(value="eventdata")
	@ResponseBody
	public Object getEventData(@RequestParam(value="filter",required=false) String filter,
							   @RequestParam(value="limit",defaultValue="5")Integer limit,
							   HttpServletRequest request){
		HttpSession session = request.getSession();
		String sessionId = session.getId();
		HashMap<String,Object> sessionMap = SessionMrg.getInstance().getSessionMap();
		if(!sessionMap.containsKey(sessionId)){
			SessionCacheImpl userEventCache = new SessionCacheImpl(null);
			sessionMap.put(sessionId, userEventCache);
		}
		if(StringUtil.isNotBlank(filter)){
			filter = "all".equals(filter) ? "" : changeFilterByType(filter, request) ;
			SessionCacheImpl sessionEventCache = (SessionCacheImpl)sessionMap.get(sessionId);
			sessionEventCache.setFilterStr(filter);
			sessionMap.put(sessionId, sessionEventCache);
		}
		SessionCacheImpl caceImpl = (SessionCacheImpl)sessionMap.get(sessionId);
		String currentFilter = caceImpl.getFilterStr();
		List<Map<String,Object>> msgList = caceImpl.getCach().pop(limit);
		return convert2LMap(msgList,currentFilter);
	}
	
	private List<Map<String,String>> convert2LMap(List<Map<String, Object>> msgList,String filter) {
		
		List<Map<String,String>> eventList=new ArrayList<Map<String,String>>();
		List<Map<String,Object>> events = filterResult(msgList, filter);
		if(ObjectUtils.isEmpty(events)){
			return eventList ;
		}
		for(Map<String,Object> event:events){
			Map<String,String> eventMap=new HashMap<String, String>();
			Set<Entry<String, Object>> eEntrySet = event.entrySet();
			for (Entry<String, Object> entry : eEntrySet) {
				String keyName = entry.getKey();
				Object value = entry.getValue() ;
				if (value!=null) {
//					Element name = ele.addElement(keyName.toUpperCase());//统一使用大写字段，前台解析也要统一使用大写
					String stringValue=(value instanceof Date?StringUtil.dateToString((Date) value, "yyyy-MM-dd HH:mm:ss"): StringUtil.toString(value));
					if ("DESCR".equals(keyName)) {
						eventMap.put(keyName.toUpperCase(), HtmlUtils.htmlEscape((String)value));
					} else {
						eventMap.put(keyName.toUpperCase(), stringValue);
					}
				}
			}
			
			eventMap.put("id", StringUtil.toString(event.get(DataConstants.UUID), ""));
			eventMap.put("type",StringUtil.toString(event.get("EVENT_TYPE"),""));
			eventList.add(eventMap);
		}
		
		return eventList;
	}
	private List<Map<String,Object>> filterResult(List<Map<String,Object>> msgList,String filterStr){
		TypeDef typedef = TypeDefFactory.createInstance(SystemDefinition.DEFAULT_CONF_DIR + "typedef-filter.xml");
		Selectorable s = null;
		List msg = msgList;
		if(StringUtil.isNotBlank(filterStr)){
			try {
				s = new Selector(typedef, null).createSelector(filterStr);
				msg = s.select(msgList);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return msg;
//	
	}
	//按照类型转化为过滤需要的FilterStr
	private String changeFilterByType(String filterStr,HttpServletRequest request){
		String _filterStr = "";
		try{
			String type = filterStr.split(",")[0];
			String value = filterStr.split(",")[1];
			if("PRIORITY".equals(type)){
				_filterStr = " SELECTOR(PRIORITY = "+value+")";
			}else if("CAT".equals(type)){
				String[] cats = value.split(":");
				int length = cats.length;
				_filterStr = "CAT1_ID = '"+cats[0]+"' ";
				if(length>1){
					for(int i=2;i<=length;i++){
						String cat = " AND CAT"+i+"_ID ="+"'"+cats[i-1]+"' ";
						_filterStr += cat;
					}
				}
				_filterStr =  " SELECTOR("+_filterStr+")";
				
			}else if("DVC_TYPE".equals(type)){
				_filterStr = "SELECTOR(DVC_TYPE = "+"'"+value+"' )";
			}else if("SelfDefine".equals(type)){
				_filterStr = value.replaceAll(";", "=");
				BDFactory bdf = BDFactory.getInstance();
				FilterMgrBD service = (FilterMgrBD)bdf.getBD(FilterMgrBD.class, request, sid.toString());
				_filterStr = service.parseFilterSQLStr(_filterStr);

			}else if("NAME".equals(type)){
				_filterStr = " SELECTOR(NAME =  '"+value+"')";
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return _filterStr;
	}
	
}
