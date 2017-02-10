package com.topsec.tsm.sim.report.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.alibaba.fastjson.JSON;
import com.topsec.tal.base.hibernate.ReportTask;
import com.topsec.tal.base.util.LogKeyInfo;
import com.topsec.tsm.ass.persistence.Device;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.common.SIMConstant;
import com.topsec.tsm.sim.access.util.GlobalUtil;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.log.service.LogReportTaskService;
import com.topsec.tsm.sim.report.persistence.RptMaster;
import com.topsec.tsm.sim.report.service.ReportService;
import com.topsec.tsm.sim.report.util.ReportUiUtil;
import com.topsec.tsm.sim.util.DeviceTypeNameUtil;
import com.topsec.tsm.sim.util.FacadeUtil;
import com.topsec.tsm.tal.service.EventResponseService;

@Controller("planReport")
@RequestMapping("planReport")
public class PlanReportController{
	private static final Logger log = LoggerFactory.getLogger(PlanReportController.class);
	@Autowired
	private ReportService reportService ;
	@Autowired
	private EventResponseService eventResponseService ;
	@Autowired
	private LogReportTaskService logReportTaskService ;
	private DataSourceService dataSourceService;
	
	private void setEncode(HttpServletRequest request, HttpServletResponse response) {
		try {
			request.setCharacterEncoding("UTF-8");
			response.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		
	}
	@RequestMapping("openDefReportPage")
	@ResponseBody
	public Object openDefReportPage(SID sid,HttpServletRequest request, 
			HttpServletResponse response){
		 setEncode(request,response);
		 List<RptMaster> masterReportList=null;
		 if (sid.isOperator()) {
			 masterReportList=reportService.getAllMyReports();
		 }else{
			 masterReportList=reportService.showAllMyReportsByUser(sid.getUserName());
		 }
		 String value="";
			try {
				value = URLDecoder.decode(request.getParameter("value"),"utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			if(!value.equals("")&&(value!=null))
			{
				String planSysReportArray=value.split("=:")[0];	 
				request.setAttribute("report",planSysReportArray);
				request.setAttribute("masterReportList", masterReportList);
			}
		 request.setAttribute("masterReportList", masterReportList);
		 Object jsonObject=JSON.toJSON(masterReportList);
		 return jsonObject;
	}
	/*
	@RequestMapping("reportLogStatisticsTask")
	@ResponseBody
	public Object reportLogStatisticsTask(SID sid,HttpServletRequest request, 
			HttpServletResponse response){
		setEncode(request,response);
		List<Map<String, Object>>resultList=new ArrayList<Map<String,Object>>();
		List<ReportTask> reportTaskList=null;
		reportTaskList=logReportTaskService.getAllTask();

		if (!GlobalUtil.isNullOrEmpty(reportTaskList)) {
			for (ReportTask reportTask : reportTaskList) {
				Map<String, Object> map=new HashMap<String, Object>();
				map.put("id", reportTask.getId());
				map.put("taskName", reportTask.getTaskName());
				resultList.add(map);
			}
		}
		 request.setAttribute("resultList", resultList);
		 Object jsonObject=JSON.toJSON(resultList);
		 return jsonObject;
	}
	*/
	@SuppressWarnings("unchecked")
	@RequestMapping("changeReportType")
	public void changeReportType(SID sid,HttpServletRequest request, 
			HttpServletResponse response) throws Exception{
		dataSourceService=getDataSourceService(request);
		
		String reportType=request.getParameter("reportType");
		if(reportType==null||reportType.equals("")){
			log.error("com.topsec.tsm.tal.ui.report.action.PlanReportAction.changeReportType(),reportType==null||reportType.equals('')!!!");
			throw new Exception("com.topsec.tsm.tal.ui.report.action.PlanReportAction.changeReportType(),reportType==null||reportType.equals('')!!!");
		}
		
		response.setContentType("text/xml;charset=utf-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter writer=null;
		try {
			writer = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<String> ips=new ArrayList<String>();
		List<Map> mstRptList=null;
		List<String> types=new ArrayList<String>();
		String result="";
		String localIp = IpAddress.getLocalIp().toString();
		if(localIp.indexOf(':')>0)
			localIp = "::1";
		else 
			localIp = "127.0.0.1";

		if(reportType.equals("1") && (sid.hasAuditorRole())){
			//自审计报表
			ips.add(localIp);
			mstRptList=ReportUiUtil.getMstList(LogKeyInfo.LOG_SYSTEM_TYPE);
			types.add(LogKeyInfo.LOG_SYSTEM_TYPE);
			result=getXMLResult(request,reportType,ips,mstRptList,types,null);
		}else if(reportType.equals("2") && sid.hasOperatorRole()){ 
			//事件报表
			ips.add(localIp);
			mstRptList=ReportUiUtil.getMstList(LogKeyInfo.LOG_SIM_EVENT);
			types.add(LogKeyInfo.LOG_SIM_EVENT);
			result=getXMLResult(request,reportType,ips,mstRptList,types,null);
		}else if(reportType.equals("3") && sid.hasOperatorRole()){
			//审计对象报表
			ips.add(localIp);
			mstRptList=ReportUiUtil.getMstList(LogKeyInfo.LOG_SIM_AUDIT_NODE);
			types.add(LogKeyInfo.LOG_SIM_AUDIT_NODE);
			result=getXMLResult(request,reportType,ips,mstRptList,types,null);
 		}else if(reportType.equals("4") && sid.hasOperatorRole()){
			//日志报表
 			List<SimDatasource> simDatasources=dataSourceService.getDataSource(DataSourceService.CMD_ALL);
			Set set=sid.getUserDevice();
			if (sid.isOperator()) {
				types=dataSourceService.getDistinctDvcType(null);
				types.add(0, "Log/Global/Detail");
			}else{
				BeanToPropertyValueTransformer trans = new BeanToPropertyValueTransformer("ip") ;
				Collection<String> userDeviceIPs = (Collection<String>) CollectionUtils.collect(set,trans) ;
				for (SimDatasource simDatasource : simDatasources) {
					Device device = AssetFacade.getInstance().getAssetByIp(simDatasource.getDeviceIp()) ;
					if (device != null && userDeviceIPs.contains(simDatasource.getDeviceIp())) {
						if(!types.contains(simDatasource.getSecurityObjectType())){
							types.add(simDatasource.getSecurityObjectType());
						}
					}
				}
			}
			result=getXMLResult(request,reportType,null,null,types,null);
		}else if(reportType.equals("6") && sid.hasAdminRole()){ 
			//系统报表
			ips.add(localIp);
			mstRptList=ReportUiUtil.getMstList(LogKeyInfo.LOG_SYSTEM_RUN_TYPE);
			types.add(LogKeyInfo.LOG_SYSTEM_RUN_TYPE);
			result=getXMLResult(request,reportType,ips,mstRptList,types,null);
		}
		writer.write(result);
		writer.flush();
		
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping("changeDeviceType")
	public void changeDeviceType(SID sid,HttpServletRequest request,  HttpServletResponse response){
		dataSourceService=getDataSourceService(request);
		String deviceType=request.getParameter("deviceType");
		List<Map> mstRptList=null;
		List<String> ipList = new ArrayList<String>();
		Map<String,String> keyMap=new HashMap<String, String>();
		try {
			List<SimDatasource> list = null;
			if (deviceType.contains("Monitor/")) {
				//list = dataSourceService.getDataSourcesByDeviceType(DataSourceService.CMD_DATA_SOURCE, deviceType.replace("Monitor/", ""));
			}else{
				list = dataSourceService.getDataSourceByDvcType(deviceType);
			}
			
			if(list!=null){
				Set set=sid.getUserDevice();
				BeanToPropertyValueTransformer trans = new BeanToPropertyValueTransformer("ip") ;
				Collection<String> userDeviceIPs = (Collection<String>) CollectionUtils.collect(set,trans) ;
				for (SimDatasource simDatasource : list) {
					Device device = AssetFacade.getInstance().getAssetByIp(simDatasource.getDeviceIp()) ;
					String deviceIp = simDatasource.getDeviceIp();
					if (device != null && (sid.isOperator() || userDeviceIPs.contains(deviceIp))) {
						if (!keyMap.containsKey(deviceIp)) {
							keyMap.put(deviceIp, simDatasource.getResourceName());
						} else {
							String value = keyMap.get(deviceIp);
							keyMap.put(deviceIp,value + ","+ simDatasource.getResourceName());
						}
					}
				}
				Set<String> keySet = keyMap.keySet();
				if(keySet!=null){
					for (String s : keySet) {
						ipList.add(s);
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		
		String result="";
		mstRptList=ReportUiUtil.getMstList(deviceType);
		response.setContentType("text/xml;charset=utf-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter writer=null;
		try {
			writer = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 4:日志报表
		String type=null;
		
		if("Log/Global/Detail".equals(deviceType)){
			type="5";
			if (sid.isOperator()) {
				ipList.add(0, "Log/Global/Detail");
			}
		}else{
			type="4";
			if (sid.isOperator()) {
				ipList.add(0, "onlyByDvctype");
			}
			
		}
		result=getXMLResult(request,type,ipList,mstRptList,null,keyMap);  
		writer.write(result);
		writer.flush();
		
	}
	private String getXMLResult(HttpServletRequest request,String type,List<String> ipList,List<Map> mstRptList,List<String> deviceTypes,Map<String,String> keyMap){
		Locale locale=request.getLocale();
		
		String xml_start="<?xml version=\"1.0\" encoding=\"utf-8\"?>"+"<selects>";
        String xml_end="</selects>";
        String xml="<type>"+type+"</type>";
        
        //xml+="<deviceType><value>null</value><text>请选择</text></deviceType>";
        if(deviceTypes!=null){
        	for(int i=0;i<deviceTypes.size();i++){
        		String deviceType=deviceTypes.get(i);
        		String deviceTypeZH=null;
        		if("Log/Global/Detail".equals(deviceType) || "Monitor/Global/Detail".equals(deviceType)){
        			deviceTypeZH="全局报表";
        		}else{
        			deviceTypeZH=DeviceTypeNameUtil.getDeviceTypeName(deviceType,locale);
        		}
        		if("5".equals(type)){
        			xml=xml+"<deviceType><value>Monitor/"+deviceType+"</value><text>"+deviceTypeZH+"</text></deviceType>";
        		}else{
        			xml=xml+"<deviceType><value>"+deviceType+"</value><text>"+deviceTypeZH+"</text></deviceType>";
        		}
	    	}
        }
        
        //xml+="<selectIP><value>null</value><text>请选择</text></selectIP>";
        if(ipList!=null){
        	for(int i=0;i<ipList.size();i++){
        		if("onlyByDvctype".equals(ipList.get(i))){
        			xml=xml+"<selectIP><value>"+ipList.get(i)+"</value><text>"+"全部"+"</text></selectIP>";
        		}else if("Log/Global/Detail".equals(ipList.get(i))){
        			xml=xml+"<selectIP><value>"+ipList.get(i)+"</value><text>"+ipList.get(i)+"</text></selectIP>";
        		}else{
        			if(keyMap!=null&&keyMap.get(ipList.get(i))!=null){
        				xml=xml+"<selectIP><value>"+ipList.get(i)+"</value><text>"+keyMap.get(ipList.get(i))+"</text></selectIP>";
        			}else{
        				xml=xml+"<selectIP><value>"+ipList.get(i)+"</value><text>"+ipList.get(i)+"</text></selectIP>";
        			}
        		}
	    	}
        }
        
	   //xml=xml+"<selectReport><value>null</value><text>请选择</text></selectReport>";
       if(mstRptList!=null)
       {
    	   for(Map map:mstRptList){
    		   xml=xml+"<selectReport><value>"+map.get("id")+"</value><text>"+map.get("mstName")+"</text></selectReport>";
    	   }
       } 
       return xml_start+xml+xml_end;
	}
	
	private DataSourceService getDataSourceService(HttpServletRequest request) {
		if(null==dataSourceService){
			this.dataSourceService=(DataSourceService)FacadeUtil.getFacadeBean(request, null, "dataSourceService");
		}
		return this.dataSourceService;
	}
}
