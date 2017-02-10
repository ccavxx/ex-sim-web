package com.topsec.tsm.rest.server.log;

import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.alibaba.fastjson.JSON;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.rest.server.common.RestSecurityAuth;
import com.topsec.tsm.rest.server.common.RestUtil;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.log.bean.LogSearchObject;
import com.topsec.tsm.sim.log.util.LogRecordList;
import com.topsec.tsm.sim.log.web.LogSearchController;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
@Path("/")
public class LogRest {
	@Produces("application/xml;charset=UTF-8")
	@POST
	@Path("/log/getGourpTree")
	public Response getGroupTree(@Context HttpServletRequest request,@CookieParam("sessionid") String id){
		try {
			ResponseBuilder build=Response.status(200);
			build.header("Content-Type","text/xml;charset=UTF-8");
			boolean isLogin = RestSecurityAuth.getInstance().isLogin(id);
			if(!isLogin){
				String msg = RestUtil.getInstance().errorMsg("Auth","访问 任务启动  接口前未登陆！");
	           return build.entity(msg).build();
			}
			SID sid = new SID();
			sid.setUserName("operator");
			sid.setUserDevice(new HashSet());
			Object obj = getInstance().getTreeForGroup(sid, request);
			return build.entity(JSON.toJSONString(obj)).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Path("/log/search")
	@POST
	@Produces("application/xml;charset=UTF-8")
	/**
	 * 日志查询
	 */
	public Response search(@Context HttpServletRequest request,
			@CookieParam("sessionid") String id) {
		ResponseBuilder build=Response.status(200);
		build.header("Content-Type","text/xml;charset=UTF-8");
		boolean isLogin = RestSecurityAuth.getInstance().isLogin(id);
		if(!isLogin){
			String msg = RestUtil.getInstance().errorMsg("Auth","访问 任务启动  接口前未登陆！");
           return build.entity(msg).build();
		}
		SID sid = new SID();
		sid.setUserName("operator");
		sid.setUserDevice(new HashSet());
		LogRecordList logRecordList = new LogRecordList();
		try {
			String xml = RestUtil.getStrFromInputStream(request.getInputStream());
			if(xml!=null){
				Document document = DocumentHelper.parseText(xml);
				Element root = document.getRootElement();
					//查询日志
				LogSearchObject logSearchObject = parsLogSearchObject(root);
				while(true){
					logRecordList = (LogRecordList)getInstance().doLogSearch(sid, logSearchObject,request);
					if(logRecordList.getExceptionInfo() != null ||logRecordList.isFinished() || (logRecordList.getMaps() != null && logRecordList.getMaps().size()>0)){
						break;
					}
					Thread.sleep(1000);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return build.entity(JSON.toJSONString(logRecordList)).build();
	}
	
	private LogSearchObject parsLogSearchObject(Element root){
		LogSearchObject logSearchObject =new LogSearchObject();
		logSearchObject.setHost(root.element("Host").getTextTrim());
		logSearchObject.setDeviceType(root.element("DeviceType").getTextTrim());
		logSearchObject.setNodeId(root.element("NodeId").getTextTrim());
		logSearchObject.setPageNo(Integer.parseInt(root.element("PageNo").getTextTrim()));
		logSearchObject.setPageSize( Integer.parseInt(root.element("PageSize").getTextTrim()));
		logSearchObject.setConditionName( root.element("ConditionName").getTextTrim());
		logSearchObject.setOperator( root.element("Operator").getTextTrim());
		logSearchObject.setQueryContent( root.element("QueryContent").getTextTrim().replace(" ","&nbsp;"));
		logSearchObject.setQueryStartDate(root.element("QueryStartDate").getTextTrim());
		logSearchObject.setQueryEndDate(root.element("QueryEndDate").getTextTrim());
		logSearchObject.setGroup(root.element("Group").getTextTrim());
		logSearchObject.setQueryType(root.element("QueryType").getTextTrim());
		logSearchObject.setCancel(Boolean.parseBoolean(root.element("Cancel").getTextTrim()));
		return logSearchObject;
		
	}
	
	public static LogSearchController instance = null;

	private static LogSearchController getInstance() {
		if (instance == null) {
			instance = new LogSearchController();
			instance.setNodeMgr((NodeMgrFacade)SpringContextServlet.springCtx.getBean("nodeMgrFacade")) ;
			instance.setDataSourceService((DataSourceService) SpringContextServlet.springCtx.getBean("dataSourceService")) ;
		}
		return instance;
	}
	
}
