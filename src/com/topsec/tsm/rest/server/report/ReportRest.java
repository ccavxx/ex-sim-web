package com.topsec.tsm.rest.server.report;

import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.topsec.tal.base.util.SpringWebUtil;
import com.topsec.tsm.auth.manage.AuthAccount;
import com.topsec.tsm.rest.server.common.RestSecurityAuth;
import com.topsec.tsm.rest.server.common.RestUtil;
import com.topsec.tsm.sim.auth.service.UserService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.newreport.bean.ReportQueryConditions;
import com.topsec.tsm.sim.newreport.web.BasicReportController;

@Path("/")
public class ReportRest {
	private UserService userService;
	
	@Autowired
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	/**
	 * 多级状态下的  左侧报表树
	 * @param request
	 * @param id
	 * @return
	 */
	@Produces("application/xml;charset=UTF-8")
	@POST
	@Path("/basicreport/reportShowTree")
	public Response getReportTree(@Context HttpServletRequest request,@CookieParam("sessionid") String id){
		try {
			ResponseBuilder build=Response.status(200);
			build.header("Content-Type","text/xml;charset=UTF-8");
			boolean isLogin = RestSecurityAuth.getInstance().isLogin(id);
			if(!isLogin){
				String msg = RestUtil.getInstance().errorMsg("Auth","访问 任务启动  接口前未登陆！");
	           return build.entity(msg).build();
			}
			UserService userService = (UserService) SpringWebUtil.getBean("userService", request);
			AuthAccount account = userService.getUserByUserName("operator");
			SID sid = new SID("127.0.0.1", account);
			Object obj = getInstance().getReportTree(sid, request);
			return build.entity(JSON.toJSONString(obj)).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	@Produces("application/xml;charset=UTF-8")
	@POST
	@Path("/report/userReportRole")
	public Response userReportRole(@Context HttpServletRequest request,@CookieParam("sessionid") String id){
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
			Object obj = getInstance().reportRole(sid);
			return build.entity(JSON.toJSONString(obj)).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 报表查询
	 * @param request
	 * @param id
	 * @return
	 */
	@Produces("application/xml;charset=UTF-8")
	@POST
	@Path("/basicreport/findReport")
	public Response reportQuery(@Context HttpServletRequest request,@CookieParam("sessionid") String id){
		try {
			ResponseBuilder build=Response.status(200);
			build.header("Content-Type","text/xml;charset=UTF-8");
			boolean isLogin = RestSecurityAuth.getInstance().isLogin(id);
			if(!isLogin){
				String msg = RestUtil.getInstance().errorMsg("Auth","访问 任务启动  接口前未登陆！");
	           return build.entity(msg).build();
			}
			UserService userService = (UserService) SpringWebUtil.getBean("userService", request);
			AuthAccount account = userService.getUserByUserName("operator");
			SID sid = new SID("127.0.0.1", account);
			String xml = RestUtil.getStrFromInputStream(request.getInputStream());
			if(xml!=null){
				Document document = DocumentHelper.parseText(xml);
				Element root = document.getRootElement();
				ReportQueryConditions queryConditions = parsReportQueryConditions(root);
				Object obj = getInstance().findReport(sid, queryConditions,request);
				return build.entity(JSON.toJSONString(obj)).build();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 或许小主题信息
	 * @param request
	 * @param id
	 * @return
	 */
	@Produces("application/xml;charset=UTF-8")
	@POST
	@Path("/basicreport/subThemeData")
	public Response getSubTitle(@Context HttpServletRequest request,@CookieParam("sessionid") String id){
		try {
			ResponseBuilder build=Response.status(200);
			build.header("Content-Type","text/xml;charset=UTF-8");
			boolean isLogin = RestSecurityAuth.getInstance().isLogin(id);
			if(!isLogin){
				String msg = RestUtil.getInstance().errorMsg("Auth","访问 任务启动  接口前未登陆！");
	           return build.entity(msg).build();
			}
//			UserService userService = (UserService) SpringWebUtil.getBean("userService", request);
//			AuthAccount account = userService.getUserByUserName("operator");
//			SID sid = new SID("127.0.0.1", account);
			String xml = RestUtil.getStrFromInputStream(request.getInputStream());
			if(xml!=null){
				Document document = DocumentHelper.parseText(xml);
				Element root = document.getRootElement();
				ReportQueryConditions queryConditions = parsReportQueryConditions(root);
				Object obj = getInstance().subThemeData(queryConditions);
				return build.entity(JSON.toJSONString(obj)).build();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static BasicReportController instance = null;

	private static BasicReportController getInstance() {
		if (instance == null) {
			instance = new BasicReportController();
		}
		return instance;
	}
	private ReportQueryConditions parsReportQueryConditions(Element root){
		ReportQueryConditions reportQueryConditions = new ReportQueryConditions();
		reportQueryConditions.setId(root.element("Id").getTextTrim());
		reportQueryConditions.setDvcAddress(root.element("DvcAddress").getTextTrim());
		reportQueryConditions.setSecurityObjectType(root.element("SecurityObjectType").getTextTrim());
		reportQueryConditions.setNodeIds(new String[]{root.element("NodeIds").getText()});
		//ParentIds 默认值 
		int parentId = 0;
		String parentIdStr = root.element("ParentIds").getText();
		if(StringUtils.isNotBlank(parentIdStr))
			parentId = Integer.parseInt(parentIdStr);
		reportQueryConditions.setParentIds(new Integer[]{parentId});
		int parentSubId = 0;
		String parentSubIdStr = root.element("ParentSubId").getText();
		if(StringUtils.isNotBlank(parentSubIdStr) && !"null".equals(parentSubIdStr))
			parentSubId = Integer.parseInt(parentSubIdStr);
		reportQueryConditions.setParentSubId(parentSubId);
		int subId = 0;
		String subIdStr = root.element("SubId").getText();
		if(StringUtils.isNotBlank(subIdStr) && !"null".equals(subIdStr))
			subId = Integer.parseInt(subIdStr);
		reportQueryConditions.setSubId(subId);
		reportQueryConditions.setStime(root.element("Stime").getTextTrim());
		reportQueryConditions.setEndtime(root.element("Endtime").getTextTrim());
		long pageIndex = 0;
		String pageIndexStr = root.element("PageIndex").getText();
		if(StringUtils.isNotBlank(pageIndexStr) && !"null".equals(pageIndexStr))
			pageIndex = Long.parseLong(pageIndexStr);
		reportQueryConditions.setPageIndex(pageIndex);
		long pageSize = 10;
		String pageSizeStr = root.element("PageSize").getText();
		if(StringUtils.isNotBlank(pageSizeStr) && !"null".equals(pageSizeStr))
			pageSize = Long.parseLong(pageSizeStr);
		reportQueryConditions.setPageSize(pageSize);
		reportQueryConditions.setParams(root.element("Params").getTextTrim());
		int topn = 0;
		String topnStr = root.element("Topn").getText();
		if(StringUtils.isNotBlank(topnStr) && !"null".equals(topnStr))
			topn = Integer.parseInt(topnStr);
		reportQueryConditions.setTopn(topn);
		reportQueryConditions.setExportFormat(root.element("ExportFormat").getTextTrim());
		reportQueryConditions.setUsername(root.element("Username").getTextTrim());
		reportQueryConditions.setQueryType(root.element("QueryType").getTextTrim());
		return reportQueryConditions;
		
	}
}
