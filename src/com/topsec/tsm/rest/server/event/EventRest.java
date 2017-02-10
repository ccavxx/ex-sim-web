package com.topsec.tsm.rest.server.event;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.SpringWebUtil;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.rest.server.common.RestSecurityAuth;
import com.topsec.tsm.rest.server.common.RestUtil;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.event.bean.BasicQueryCriteria;
import com.topsec.tsm.sim.event.bean.CategoryLevelParam;
import com.topsec.tsm.sim.event.service.EventCategoryService;
import com.topsec.tsm.sim.event.service.EventQueryService;
import com.topsec.tsm.sim.event.web.EventQueryController;
import com.topsec.tsm.sim.kb.service.KnowledgeService;
import com.topsec.tsm.sim.sysconfig.service.EventRuleService;
import com.topsec.tsm.tal.service.EventResponseService;

@Path("/")
public class EventRest {
	/**
	 * 事件查询
	 * @param request
	 * @param id
	 * @return
	 */
	@Produces("application/xml;charset=UTF-8")
	@POST
	@Path("/event/getEventByBasicCondition")
	public Response getEventByBasicCondition(@Context HttpServletRequest request,@CookieParam("sessionid") String id){
		ResponseBuilder build=Response.status(200);
		build.header("Content-Type","text/xml;charset=UTF-8");
		boolean isLogin = RestSecurityAuth.getInstance().isLogin(id);
		if(!isLogin){
			String msg = RestUtil.getInstance().errorMsg("Auth","访问 任务启动  接口前未登陆！");
           return build.entity(msg).build();
		}
		Object obj = getInstance().getEventByBasicCondition(parseBasicQueryCriteria(request),request);
		return build.entity(JSON.toJSONString(obj)).build();
	}
	/**
	 * 时间轴
	 * @param request
	 * @param id
	 * @return
	 */
	@Produces("application/xml;charset=UTF-8")
	@POST
	@Path("/event/expandTimelineByBasicCondition")
	public Response expandTimelineByBasicCondition(@Context HttpServletRequest request,@CookieParam("sessionid") String id){
		ResponseBuilder build=Response.status(200);
		build.header("Content-Type","text/xml;charset=UTF-8");
		boolean isLogin = RestSecurityAuth.getInstance().isLogin(id);
		if(!isLogin){
			String msg = RestUtil.getInstance().errorMsg("Auth","访问 任务启动  接口前未登陆！");
           return build.entity(msg).build();
		}
		Object obj = getInstance().expandTimelineByBasicCondition(parseBasicQueryCriteria(request),request);
		return build.entity(JSON.toJSONString(obj)).build();
	}
	/**
	 * 事件级别信息统计
	 * @param request
	 * @param id
	 * @return
	 */
	@Produces("application/xml;charset=UTF-8")
	@POST
	@Path("/event/getLevelEventStatistic")
	public Response getLevelEventStatistic(@Context HttpServletRequest request,@CookieParam("sessionid") String id){
		ResponseBuilder build=Response.status(200);
		build.header("Content-Type","text/xml;charset=UTF-8");
		boolean isLogin = RestSecurityAuth.getInstance().isLogin(id);
		if(!isLogin){
			String msg = RestUtil.getInstance().errorMsg("Auth","访问 任务启动  接口前未登陆！");
           return build.entity(msg).build();
		}
		Object obj = getInstance().getLevelEventStatistic(parseParam(request),request);
		return build.entity(JSON.toJSONString(obj)).build();
	}
	/**
	 * 获取事件规则
	 * @param request
	 * @param id
	 * @return
	 */
	@Produces("application/xml;charset=UTF-8")
	@POST
	@Path("/event/getEventRule")
	public Response getEventRule(@Context HttpServletRequest request,@CookieParam("sessionid") String id){
		ResponseBuilder build=Response.status(200);
		build.header("Content-Type","text/xml;charset=UTF-8");
		boolean isLogin = RestSecurityAuth.getInstance().isLogin(id);
		if(!isLogin){
			String msg = RestUtil.getInstance().errorMsg("Auth","访问 任务启动  接口前未登陆！");
           return build.entity(msg).build();
		}
		Object obj = getInstance().getEventRule();
		return build.entity(JSON.toJSONString(obj)).build();
	}
	
	/**
	 * 事件回溯
	 * @param request
	 * @param id
	 * @return
	 */
	@Produces("application/xml;charset=UTF-8")
	@POST
	@Path("/event/correlatorData")
	public Response correlatorData(@Context HttpServletRequest request,@CookieParam("sessionid") String id){
		ResponseBuilder build=Response.status(200);
		build.header("Content-Type","text/xml;charset=UTF-8");
		boolean isLogin = RestSecurityAuth.getInstance().isLogin(id);
		if(!isLogin){
			String msg = RestUtil.getInstance().errorMsg("Auth","访问 任务启动  接口前未登陆！");
           return build.entity(msg).build();
		}
		Object obj = getInstance().correlatorData(Integer.parseInt(request.getParameter("evtId")));
		return build.entity(JSON.toJSONString(obj)).build();
	}
	
	
	/**
	 * 事件回溯
	 * @param request
	 * @param id
	 * @return
	 */
	@Produces("application/xml;charset=UTF-8")
	@POST
	@Path("/event/getEventCategory")
	public Response getEventCategory(@Context HttpServletRequest request,@CookieParam("sessionid") String id){
		ResponseBuilder build=Response.status(200);
		build.header("Content-Type","text/xml;charset=UTF-8");
		boolean isLogin = RestSecurityAuth.getInstance().isLogin(id);
		if(!isLogin){
			String msg = RestUtil.getInstance().errorMsg("Auth","访问 任务启动  接口前未登陆！");
           return build.entity(msg).build();
		}
		Integer eid =null;
		if(request.getParameter("eid") != null && !"null".equals(request.getParameter("eid")) ){
			eid = Integer.parseInt(request.getParameter("eid"));
		}
		Object obj = getInstance().getEventCategory(eid,request);
		return build.entity(obj).build();
	}
	
	@Produces("application/xml;charset=UTF-8")
	@POST
	@Path("/event/getAssociatedKnowledgeByEvtId")
	public Response getAssociatedKnowledgeByEvtId(@Context HttpServletRequest request,@CookieParam("sessionid") String id){
		ResponseBuilder build=Response.status(200);
		build.header("Content-Type","text/xml;charset=UTF-8");
		boolean isLogin = RestSecurityAuth.getInstance().isLogin(id);
		if(!isLogin){
			String msg = RestUtil.getInstance().errorMsg("Auth","访问 任务启动  接口前未登陆！");
           return build.entity(msg).build();
		}
		Float eid =null;
		if(request.getParameter("eid") != null && !"null".equals(request.getParameter("eid")) ){
			eid = Float.parseFloat(request.getParameter("eid"));
		}
		Object obj = getInstance().getAssociatedKnowledgeByEvtId(eid,request);
		return build.entity(JSON.toJSONString(obj)).build();
	}
	
	@Produces("application/xml;charset=UTF-8")
	@POST
	@Path("/event/cat1Statistic")
	public Response cat1Statistic(@Context HttpServletRequest request,@CookieParam("sessionid") String id){
		ResponseBuilder build=Response.status(200);
		build.header("Content-Type","text/xml;charset=UTF-8");
		boolean isLogin = RestSecurityAuth.getInstance().isLogin(id);
		if(!isLogin){
			String msg = RestUtil.getInstance().errorMsg("Auth","访问 任务启动  接口前未登陆！");
           return build.entity(msg).build();
		}
		Object obj = getInstance().cat1Statistic();
		return build.entity(JSON.toJSONString(obj)).build();
	}
	
	@Produces("application/xml;charset=UTF-8")
	@POST
	@Path("/event/cat2Statistic")
	public Response cat2Statistic(@Context HttpServletRequest request,@CookieParam("sessionid") String id){
		ResponseBuilder build=Response.status(200);
		build.header("Content-Type","text/xml;charset=UTF-8");
		boolean isLogin = RestSecurityAuth.getInstance().isLogin(id);
		if(!isLogin){
			String msg = RestUtil.getInstance().errorMsg("Auth","访问 任务启动  接口前未登陆！");
           return build.entity(msg).build();
		}
		String cat1 =null;
		if(request.getParameter("cat1") != null && !"null".equals(request.getParameter("cat1")) ){
			cat1 = request.getParameter("cat1");
		}
		Object obj = getInstance().cat2Statistic(cat1);
		return build.entity(JSON.toJSONString(obj)).build();
	}
	
	private CategoryLevelParam parseParam( HttpServletRequest request){
		CategoryLevelParam param = new CategoryLevelParam();
		if(request.getParameter("alarmState")!= null)
			param.setAlarmState(Integer.parseInt(request.getParameter("alarmState")));
		if(request.getParameter("category")!= null){
			JSONObject  jasonObject = JSONObject.parseObject(request.getParameter("category"));
			param.setCategory( (Map)jasonObject);
		}
		if(request.getParameter("level")!= null)
			param.setLevel(Integer.parseInt(request.getParameter("level")));
		if(request.getParameter("name")!= null)
			param.setName(request.getParameter("name"));
		if(request.getParameter("requestIp")!= null)
			param.setRequestIp(request.getParameter("requestIp"));
		return param;
	}
	
	/**
	 * 格式化查询条件信息
	 * @param request
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	private BasicQueryCriteria parseBasicQueryCriteria( HttpServletRequest request){
		BasicQueryCriteria param = new BasicQueryCriteria();
		param.setIp(request.getParameter("ip"));
		param.setDeviceIp(request.getParameter("deviceIp"));
		param.setDeviceType(request.getParameter("deviceType"));
		param.setEventType(request.getParameter("eventType"));
		if(request.getParameter("eventName")!= null)
			param.setEventName(request.getParameter("eventName"));
		if(request.getParameter("category1")!= null){
		   param.setCategory1(request.getParameter("category1"));
		}
		if(request.getParameter("category2")!= null){
		  param.setCategory2(request.getParameter("category2"));
		}
		if(request.getParameter("ruleName")!=null){
			param.setRuleName(request.getParameter("ruleName"));
		}
		param.setSrcIp(request.getParameter("srcIp"));
		param.setDestIp(request.getParameter("destIp"));
		param.setSrcPort(request.getParameter("srcPort"));
		param.setDestPort(request.getParameter("destPort"));
		param.setPage(StringUtil.toInt(request.getParameter("page"),1));
		param.setRows(StringUtil.toInt(request.getParameter("rows"),10));
		param.setFields(request.getParameter("fields"));
		param.setHeader(request.getParameter("header"));
		param.setProtocol(request.getParameter("protocol"));
		param.setStartTime(request.getParameter("startTime"));
		param.setEndTime(request.getParameter("endTime"));
		param.setPriority(request.getParameter("priority"));
		return param;
	}
	 
	public static EventQueryController instance = null;

	private static EventQueryController getInstance() {
		if (instance == null) {
			instance = new EventQueryController();
			SpringWebUtil util = SpringContextServlet.springCtx ;
			instance.setEventQueryService(util.getBeanByClass(EventQueryService.class)) ;
			instance.setDataSourceService((DataSourceService) util.getBean("dataSourceService")) ;
			instance.setEventCategoryService(util.getBeanByClass(EventCategoryService.class)) ;
			instance.setEventResponseService(util.getBeanByClass(EventResponseService.class)) ;
			instance.setEventRuleService(util.getBeanByClass(EventRuleService.class)) ;
			instance.setKnowledgeService(util.getBeanByClass(KnowledgeService.class)) ;
		} 
		return instance;
	}
}
