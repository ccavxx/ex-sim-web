package com.topsec.tsm.sim.report.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.topsec.tsm.rest.server.common.HttpUtil;
import com.topsec.tsm.rest.server.common.RestUtil;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.newreport.bean.ReportQueryConditions;

@Controller
@RequestMapping("reportRest")
public class ReportRestController{
	/**
	 * 构建基本报表左侧树
	 */
	@RequestMapping(value="getReportTree",  produces="text/html;charset=utf-8")
	@ResponseBody
	public Object getReportTree(SID sid, HttpServletRequest request){
		String treeJson = "[]";
		String ip = request.getParameter("ip");
		try{
			if(!StringUtils.isBlank(ip)){
				String url = "https://"+ip+"/resteasy/basicreport/reportShowTree";
				Map<String, String> cookies = new HashMap<String, String>();
				cookies.put("sessionid",RestUtil.getSessionId(ip));
				Map params = request.getParameterMap() ;
				treeJson = HttpUtil.doPostWithSSLByMap(url, params, cookies, "UTF-8");
			}
				
		}catch(Exception e){
			e.printStackTrace();
		}
		return treeJson;
	}
	
	@RequestMapping("userReportRole")
	@ResponseBody
	public Object userReportRole(HttpServletRequest request){
		String treeJson = null;
		String ip = request.getParameter("ip");
		try{
			if(!StringUtils.isBlank(ip)){
				String url = "https://"+ip+"/resteasy/basicreport/userReportRole";
				Map<String,String> cookies = new HashMap<String,String>();
				cookies.put("sessionid",RestUtil.getSessionId(ip));
				treeJson = HttpUtil.doPostWithSSLByString(url, null, cookies, "UTF-8");
			}
				
		}catch(Exception e){
			e.printStackTrace();
		}
		return treeJson;
	}

	/**
	 * 基本报表查询
	 */
	@RequestMapping(value="reportQuery", produces="text/html;charset=utf-8")
	@ResponseBody
	public String reportQuery(@RequestBody ReportQueryConditions reportQueryConditions,HttpServletRequest request) throws Exception{
		String ip = request.getParameter("ip");
		String recordList = null;
		try{
			if(!StringUtils.isBlank(ip)){
				String param =builtParam(reportQueryConditions);
				Map<String,String> cookies = new HashMap<String,String>();
				cookies.put("sessionid",RestUtil.getSessionId(ip));
				String result = HttpUtil.doPostWithSSLByString("https://"+ip+"/resteasy/basicreport/findReport", param, cookies, "UTF-8") ;
				return result ;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return recordList;
	}
	/**
	 * 小主题查询
	 */
	@RequestMapping(value="subThemeData", produces="text/html;charset=utf-8")
	@ResponseBody
	public String getSubTitle(@RequestBody ReportQueryConditions reportQueryConditions,HttpServletRequest request) {
		String ip = request.getParameter("ip");
		String recordList = null;
		try{
			if(!StringUtils.isBlank(ip)){
				String param =builtParam(reportQueryConditions);
				Map<String,String> cookies = new HashMap<String,String>();
				cookies.put("sessionid",RestUtil.getSessionId(ip));
				String result = HttpUtil.doPostWithSSLByString("https://"+ip+"/resteasy/basicreport/subThemeData", param, cookies, "UTF-8") ;
				return result ;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return recordList;
	}
	/**
	 * 将report 请求转为 reportRest请求
	 *  @param reportUrl
	 *  @return
	 */
	public static String changeTheReportUrl2Rest(Object reportUrl){
		String reportUrlTemp = null;
		if(reportUrl != null){
			reportUrlTemp = (String)reportUrl;
			reportUrlTemp = StringUtils.replace(reportUrlTemp, "/report/", "/reportRest/");
			reportUrlTemp = StringUtils.replace(reportUrlTemp, "\\report\\", "\\reportRest\\");
			reportUrlTemp = StringUtils.replace(reportUrlTemp, "\"report.", "\"simsuperiorReport.");
		}
		return reportUrlTemp;
	}
	private String builtParam(ReportQueryConditions reportQueryConditions){
		String param ="<Report>" +
				"<Id>"+reportQueryConditions.getId()+"</Id>"+
				"<ResourceId>"+reportQueryConditions.getResourceId()+"</ResourceId>"+
				"<DvcAddress>"+reportQueryConditions.getDvcAddress()+"</DvcAddress>"+
				"<SecurityObjectType>"+reportQueryConditions.getSecurityObjectType()+"</SecurityObjectType>"+
				"<NodeIds>"+StringUtils.join(reportQueryConditions.getNodeIds(), ",")+"</NodeIds>"+
				"<ParentIds>"+StringUtils.join(reportQueryConditions.getParentIds(), ",")+"</ParentIds>"+
				"<ParentSubId>"+reportQueryConditions.getParentSubId()+"</ParentSubId>"+
				"<SubId>"+reportQueryConditions.getSubId()+"</SubId>"+
				"<Stime>"+reportQueryConditions.getStime()+"</Stime>"+
				"<Endtime>"+reportQueryConditions.getEndtime()+"</Endtime>"+
				"<PageIndex>"+reportQueryConditions.getPageIndex()+"</PageIndex>"+
				"<PageSize>"+reportQueryConditions.getPageSize()+"</PageSize>"+
				"<Params>"+reportQueryConditions.getParams()+"</Params>"+
				"<Topn>"+reportQueryConditions.getTopn()+"</Topn>"+
				"<ExportFormat>"+reportQueryConditions.getExportFormat()+"</ExportFormat>"+
				"<Username>"+reportQueryConditions.getUsername()+"</Username>"+
				"<QueryType>"+reportQueryConditions.getQueryType()+"</QueryType>"+
				"</Report>";
		return param;
	}
}
