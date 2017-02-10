package com.topsec.tsm.sim.report.web;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JExcelApiExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.license.util.ChangePageEncode;
import com.topsec.tal.base.report.service.RptMasterTbService;
import com.topsec.tal.base.util.LogKeyInfo;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.ass.persistence.Device;
import com.topsec.tsm.auth.manage.AuthUserDevice;
import com.topsec.tsm.sim.access.util.GlobalUtil;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.AssetObject;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.common.model.TreeModel;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.log.service.LogReportTaskService;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.report.bean.ReportBean;
import com.topsec.tsm.sim.report.bean.struct.ExpDateStruct;
import com.topsec.tsm.sim.report.bean.struct.ExpMstRpt;
import com.topsec.tsm.sim.report.bean.struct.ExpStruct;
import com.topsec.tsm.sim.report.chart.highchart.CreateChartFactory;
import com.topsec.tsm.sim.report.chart.highchart.model.ChartTable;
import com.topsec.tsm.sim.report.chart.highchart.model.ColumnData;
import com.topsec.tsm.sim.report.model.ReportModel;
import com.topsec.tsm.sim.report.persistence.RptMaster;
import com.topsec.tsm.sim.report.service.ReportService;
import com.topsec.tsm.sim.report.util.HtmlAndFileUtil;
import com.topsec.tsm.sim.report.util.ReportUiConfig;
import com.topsec.tsm.sim.report.util.ReportUiUtil;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.util.DeviceTypeNameUtil;
import com.topsec.tsm.sim.util.FacadeUtil;

@Controller
@RequestMapping("report")
public class ReportController{

	private Logger logger = LoggerFactory.getLogger(ReportController.class) ;
	
	@Autowired
	private DataSourceService dataSourceService;
	@Autowired
	private ReportService reportService; 
	@Autowired
	private LogReportTaskService logReportTaskService ;
	@Autowired
	private NodeMgrFacade nodeMgrFacade;
	private static Node auditor =null;
	/**
	 * 构建基本报表左侧树
	 */
	@RequestMapping("getReportTree")
	@ResponseBody
	public Object getReportTree(SID sid,HttpServletRequest request){
		if (null == auditor) {
			if (null == nodeMgrFacade) {
				nodeMgrFacade=(NodeMgrFacade) SpringContextServlet.springCtx.getBean("nodeMgrFacade");
			}
			auditor = nodeMgrFacade.getKernelAuditor(false);
		}
		TreeModel tree = new TreeModel("basic","基本报表","open");
		tree.putAttribute("type",ReportUiConfig.ReportTreeType.TRUNK);
		Set<AuthUserDevice> devices = sid.getUserDevice() == null ? Collections.<AuthUserDevice>emptySet() : sid.getUserDevice();
		if(dataSourceService == null){
			dataSourceService = (DataSourceService)FacadeUtil.getFacadeBean(request, null, "dataSourceService");
		}
		if(reportService == null){
			reportService = (ReportService)FacadeUtil.getFacadeBean(request, null, "reportService");
		}
		if (sid.isAuditor()||sid.hasAuditorRole()) {
			TreeModel selfAudit = new TreeModel(LogKeyInfo.LOG_SYSTEM_TYPE,
					"审计报表", "closed");
			selfAudit.putAttribute("dvcType", LogKeyInfo.LOG_SYSTEM_TYPE);
			selfAudit.putAttribute("type", ReportUiConfig.ReportTreeType.TRUNK);
			List<Map> rptMapList = reportService.getRptMaster("Comprehensive"+LogKeyInfo.LOG_SYSTEM_TYPE);
			if (!GlobalUtil.isNullOrEmpty(rptMapList)) {
				Map trunkMap=rptMapList.get(0);
				selfAudit.putAttribute("viewItem",trunkMap.get("viewItem"));
			}
			List<TreeModel> schildren = createTreeModel(reportService, LogKeyInfo.LOG_SYSTEM_TYPE);
			selfAudit.setChildren(schildren);
			tree.addChild(selfAudit);
		}
		if (sid.hasOperatorRole() || sid.isOperator()) {
			TreeModel system = new TreeModel(LogKeyInfo.LOG_SYSTEM_RUN_TYPE,
					"系统报表", "closed");
			system.putAttribute("dvcType", LogKeyInfo.LOG_SYSTEM_RUN_TYPE);
			system.putAttribute("type", ReportUiConfig.ReportTreeType.TRUNK);
			List<Map> rptMapList = reportService.getRptMaster("Comprehensive"+LogKeyInfo.LOG_SYSTEM_RUN_TYPE);
			if (!GlobalUtil.isNullOrEmpty(rptMapList)) {
				Map trunkMap=rptMapList.get(0);
				system.putAttribute("viewItem",trunkMap.get("viewItem"));
			}
			List<TreeModel> syschildren = createTreeModel(reportService, LogKeyInfo.LOG_SYSTEM_RUN_TYPE);
			system.setChildren(syschildren);
			tree.addChild(system);
			TreeModel event = new TreeModel(LogKeyInfo.LOG_SIM_EVENT + "",
					"事件报表", "closed");
			event.putAttribute("dvcType", LogKeyInfo.LOG_SIM_EVENT);
			event.putAttribute("type", ReportUiConfig.ReportTreeType.TRUNK);
			List<Map> rptMapEvList = reportService.getRptMaster("Comprehensive"+LogKeyInfo.LOG_SIM_EVENT);
			if (!GlobalUtil.isNullOrEmpty(rptMapEvList)) {
				Map trunkMap=rptMapEvList.get(0);
				event.putAttribute("viewItem",trunkMap.get("viewItem"));
			}
			List<TreeModel> echildren = createTreeModel(reportService, LogKeyInfo.LOG_SIM_EVENT);
			event.setChildren(echildren);
			tree.addChild(event);
		}
		TreeModel  log= new TreeModel(LogKeyInfo.LOG_GLOBAL_DETAIL,"日志报表","open");
		log.putAttribute("dvcType", LogKeyInfo.LOG_GLOBAL_DETAIL);
		log.putAttribute("type", ReportUiConfig.ReportTreeType.TRUNK);
		List<TreeModel> children = createTreeModel(reportService, LogKeyInfo.LOG_GLOBAL_DETAIL);
		log.addChild(children.get(0));
		
		List<SimDatasource> simDatasources=dataSourceService.getDataSource(DataSourceService.CMD_ALL);
		List<String> dvcTypes = null;
		if (sid.isOperator()) {
			dvcTypes = dataSourceService.getDistinctDvcType(DataSourceService.CMD_ALL);
		}else{
			dvcTypes=new ArrayList<String>();
			BeanToPropertyValueTransformer trans = new BeanToPropertyValueTransformer("ip") ;
			Collection<String> userDeviceIPs = (Collection<String>) CollectionUtils.collect(devices,trans) ;
			for (SimDatasource simDatasource : simDatasources) {
				Device device = AssetFacade.getInstance().getAssetByIp(simDatasource.getDeviceIp()) ;
				if (device != null && userDeviceIPs.contains(simDatasource.getDeviceIp())) {
					if(!dvcTypes.contains(simDatasource.getSecurityObjectType())){
						dvcTypes.add(simDatasource.getSecurityObjectType());
					}
				}
			}
		}
		
		if (!GlobalUtil.isNullOrEmpty(dvcTypes)&&(sid.hasOperatorRole() || sid.isOperator())) {
			for (String dvc : dvcTypes) {
				if (!LogKeyInfo.LOG_SYSTEM_TYPE.equals(dvc)
						&& !LogKeyInfo.LOG_SYSTEM_RUN_TYPE.equals(dvc)) {
					String name = DeviceTypeNameUtil.getDeviceTypeName(dvc,
							request.getLocale());
					TreeModel trees = new TreeModel(dvc, name);
					trees.putAttribute("dvcType", dvc);
					trees.putAttribute("type",
							ReportUiConfig.ReportTreeType.TRUNK);
					List<Map> rptMapList = reportService.getRptMaster("Comprehensive"+dvc);
					if (!GlobalUtil.isNullOrEmpty(rptMapList)) {
						Map trunkMap=rptMapList.get(0);
						trees.putAttribute("viewItem",trunkMap.get("viewItem"));
					}
					
					List<TreeModel> bchildren = createTreeModel(reportService, dvc);
					trees.setChildren(bchildren);
					trees.setState(bchildren.isEmpty() ? "open" : "closed");
					log.addChild(trees);
				}
			}
			tree.addChild(log);
		}
		
        JSONArray jsonArray = new JSONArray(1) ;
        jsonArray.add(tree) ;
		return jsonArray;
	}
	private static List<TreeModel> createTreeModel(ReportService rptService,String dvcType){
		List<TreeModel> list = new ArrayList<TreeModel>();
		List<Map> rptList = rptService.getRptMaster(dvcType);
		for(Map map:rptList){
			TreeModel model = new TreeModel(StringUtil.toString(map.get("id")),StringUtil.toString(map.get("mstName")),"open");
			model.putAttribute("viewItem",map.get("viewItem"));
			model.putAttribute("dvcType", dvcType);
			model.putAttribute("type", ReportUiConfig.ReportTreeType.BRANCH);
			list.add(model);
		}
		return list;
	}
	/**
	 * 基本报表查询
	 */
	@RequestMapping("reportQuery")
	@SuppressWarnings("unchecked")
	public String reportQuery(SID sid,HttpServletRequest request,HttpServletResponse response) throws Exception{
		boolean fromRest  = false;
		if( request.getParameter("fromRest") != null){
			 fromRest =Boolean.parseBoolean( request.getParameter("fromRest"));
		}
		JSONObject json = new JSONObject();
		ReportBean bean = new ReportBean();
		bean = ReportUiUtil.tidyFormBean(bean, request);
		String onlyByDvctype = request.getParameter("onlyByDvctype");
		String [] talCategory = bean.getTalCategory();
		ReportModel.setBeanPropery(bean);
		RptMasterTbService rptMasterTbImp = (RptMasterTbService) SpringContextServlet.springCtx.getBean(ReportUiConfig.MstBean);
		List<Map<String,Object>> subResult = rptMasterTbImp.queryTmpList(ReportUiConfig.MstSubSql, new Object[]{StringUtil.toInt(bean.getMstrptid(), StringUtil.toInt(bean.getTalTop(),5))});
		StringBuffer layout = new StringBuffer();
		Map<Integer,Integer> rowColumns = ReportModel.getRowColumns(subResult) ;
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("dvcType", bean.getDvctype());
		params.put("talTop", bean.getTalTop());
		params.put("mstId", bean.getMstrptid());
		params.put("eTime", bean.getTalEndTime());
		if ("Esm/Topsec/SimEvent".equals(bean.getDvctype())
				||"Esm/Topsec/SystemLog".equals(bean.getDvctype())
				||"Esm/Topsec/SystemRunLog".equals(bean.getDvctype())
				||"Log/Global/Detail".equals(bean.getDvctype())) {
			onlyByDvctype="onlyByDvctype";
		}
		String sUrl = null;
		List<SimDatasource> simDatasources=dataSourceService.getDataSourceByDvcType(bean.getDvctype());
		removeRepeatDs(simDatasources);
		Set<AuthUserDevice> devices= ObjectUtils.nvl(sid.getUserDevice(),Collections.<AuthUserDevice>emptySet()) ;
		List<SimDatasource> dslist =new ArrayList<SimDatasource>();
		if (sid.isOperator()) {
			SimDatasource dsource = new SimDatasource();
			dsource.setDeviceIp("全部");
			dsource.setSecurityObjectType(bean.getDvctype());
			dsource.setAuditorNodeId("");
			dslist.add(0, dsource);
			dslist.addAll(simDatasources) ;
		}else{
			BeanToPropertyValueTransformer trans = new BeanToPropertyValueTransformer("ip") ;
			Collection<String> userDeviceIPs = (Collection<String>) CollectionUtils.collect(devices,trans) ;
			for (SimDatasource simDatasource : simDatasources) {
				if (userDeviceIPs.contains(simDatasource.getDeviceIp())) {
					dslist.add(simDatasource);
				}
			}
		}
		
		int screenWidth = StringUtil.toInt(request.getParameter("screenWidth"),1280) - 25- 200 ;
		
		boolean flag= "onlyByDvctype".equals(onlyByDvctype);
		SimDatasource selectDataSource = getSelectDataSource(dslist, bean, flag, request);
		AssetObject assetObject=null==selectDataSource?null:AssetFacade.getInstance().getAssetByIp(selectDataSource.getDeviceIp());
		if(fromRest){
			json.put("selectDataSourceId",selectDataSource == null ? 0 : selectDataSource.getResourceId());
			json.put("selectDataSourceName",selectDataSource == null ? "" : assetObject.getName());
		}
		request.setAttribute("selectDataSourceId",selectDataSource == null ? 0 : selectDataSource.getResourceId());
		request.setAttribute("selectDataSourceName",selectDataSource == null ? "" : assetObject.getName());
		StringBuffer subUrl = new StringBuffer();
		Map layoutValue = new HashMap() ;
		for(int i=0,len =subResult.size();i<len;i++ ){
			params.remove("sTime");
			Map subMap = subResult.get(i);
			if(i==0){
				bean.setViewItem(StringUtil.toString(subMap.get("viewItem"), ""));
			}
			Integer row = (Integer) subMap.get("subRow");
			layout.append(row + ":" + subMap.get("subColumn")+ ",");
			if (GlobalUtil.isNullOrEmpty(subMap)) {
				continue;
			}
			boolean qushi = StringUtil.booleanVal(subMap.get("chartProperty"));
			String tableSql = StringUtil.nvl((String)subMap.get("tableSql"));
			String subName = StringUtil.nvl((String)subMap.get("subName"));
			String mstName = StringUtil.nvl((String)subMap.get("mstName"));
			String pageSql = StringUtil.nvl((String)subMap.get("pagesql"));
			String chartSql = StringUtil.nvl((String)subMap.get("chartSql"));
			String nowTime=ReportUiUtil.getNowTime(ReportUiConfig.dFormat1);
			String talEndTime=bean.getTalEndTime();
			if (qushi && (subName.contains("时趋势")||subName.contains("日趋势")||subName.contains("月趋势")
					||subName.contains("时总趋势")||mstName.contains("时总趋势")
					||mstName.contains("时趋势")||mstName.contains("日趋势")||mstName.contains("月趋势"))	) {
				bean.setTalEndTime(nowTime);
				params.put("eTime", bean.getTalEndTime());
				if (tableSql.indexOf("Hour") > 20
						|| tableSql.indexOf("_hour") > 20) {
					params.put("sTime",ReportUiUtil.toStartTime("hour",bean.getTalEndTime()));
				} else if (tableSql.indexOf("Day") > 20
						|| tableSql.indexOf("_day") > 20) {
					params.put("sTime",ReportUiUtil.toStartTime("day",bean.getTalEndTime()));
				} else if (tableSql.indexOf("Month") > 20
						|| tableSql.indexOf("_month") > 20) {
					params.put("sTime",ReportUiUtil.toStartTime("month",bean.getTalEndTime()));
				}else if (pageSql.indexOf("Hour") > 20
						|| pageSql.indexOf("_hour") > 20
						|| chartSql.indexOf("Hour") > 20) {
					params.put("sTime",ReportUiUtil.toStartTime("hour",bean.getTalEndTime()));
				} else if (pageSql.indexOf("Day") > 20
						|| pageSql.indexOf("_day") > 20
						|| chartSql.indexOf("Day") > 20) {
					params.put("sTime",ReportUiUtil.toStartTime("day",bean.getTalEndTime()));
				} else if (pageSql.indexOf("Month") > 20
						|| pageSql.indexOf("_month") > 20
						|| chartSql.indexOf("Month") > 20) {
					params.put("sTime",ReportUiUtil.toStartTime("month",bean.getTalEndTime()));
				}else {
					params.put("sTime", bean.getTalStartTime());
				}
			} else if(subName.indexOf("分布图") > 1){
				bean.setTalEndTime(nowTime);
				params.put("eTime", bean.getTalEndTime());
				if (tableSql.indexOf("Hour") > 20 || tableSql.indexOf("_hour") > 20) {
					params.put("sTime", ReportUiUtil.toStartTime("undefined", bean.getTalEndTime()));
				}else{
					params.put("sTime", bean.getTalStartTime());
				}
				String startTime=params.get("sTime").toString();
				String endTime=params.get("eTime").toString();
				subName=subName+" （统计时间段："+startTime.substring(5)+" - "+endTime.substring(10)+"）";//endTime.substring(10,endTime.length()-4)+"0:00）";
				subMap.put("subName", subName);
			}else {
				params.put("sTime", bean.getTalStartTime());
			}
			bean.setTalEndTime(talEndTime);
			sUrl=getUrl(ReportUiConfig.subUrl,request, params,bean.getTalCategory()).toString();
			subUrl.replace(0, subUrl.length(),sUrl);
			subUrl.append("&").append(ReportUiConfig.subrptid).append("=").append(subMap.get("subId"));
			subUrl.substring(0, subUrl.length());
			int column = rowColumns.get(row);
			String width = String.valueOf((screenWidth-10*column)/column) ;
			String _column = subMap.get("subColumn").toString();
			layoutValue.put(row+_column, ReportUiUtil.createSubTitle(subMap, width,subUrl.toString(),bean.getTalCategory(),StringUtil.toInt(bean.getTalTop(), 5)));
		}
		if(talCategory!=null){
			if(fromRest)
				json.put("superiorUrl",getSuperiorUrl(request, params,bean.getTalCategory()).toString());
			request.setAttribute("superiorUrl",getSuperiorUrl(request, params,bean.getTalCategory()).toString());
		}

		if(!GlobalUtil.isNullOrEmpty(subResult)&&subResult.size()>0){
			if (!GlobalUtil.isNullOrEmpty(subResult.get(0).get("mstName"))) {
				if(fromRest){
					json.put("title", subResult.get(0).get("mstName"));
				}
				request.setAttribute("title", subResult.get(0).get("mstName"));
			}
	    }
		String htmlLayout = ReportModel.createMstTable(layout.toString(),layoutValue);
		StringBuffer sb = getExportUrl(request, params,talCategory);
		request.setAttribute("expUrl", sb.toString());
		request.setAttribute("layout", htmlLayout);
		request.setAttribute("bean", bean);
		request.setAttribute("dslist", dslist);
		if(fromRest){
			json.put("expUrl", sb.toString());
			json.put("layout", htmlLayout);
			json.put("bean", JSONObject.toJSON(bean));
			json.put("dslist", JSONObject.toJSON(dslist));
			return json.toString();
		}
		
		return "/page/report/base_report_detail";
	}
	
	/**
	 * 小主题查询
	 */
	@RequestMapping("getSubTitle")
	@ResponseBody
	public Object getSubTitle(SID sid,HttpServletRequest request,HttpServletResponse response){
		try {
			SID.setCurrentUser(sid) ;
			String subId = request.getParameter(ReportUiConfig.subrptid);
			String sTime = request.getParameter(ReportUiConfig.sTime);
			String eTime = request.getParameter(ReportUiConfig.eTime);
			int talTop= request.getParameter("talTop")==null?5:Integer.valueOf(request.getParameter("talTop"));
			RptMasterTbService rptMasterTbImp = (RptMasterTbService) SpringContextServlet.springCtx.getBean(ReportUiConfig.MstBean);
			List<Map> subList = rptMasterTbImp.queryTmpList(ReportUiConfig.SubTitleSql, new Object[]{StringUtil.toInt(subId, 0)});
			JSONObject json = null;
			if(subList.size()>0){
				Map subMap = (Map) subList.get(0);
				Map<String,Object> rsMap =null;
				if(dataSourceService == null){
					dataSourceService = (DataSourceService)FacadeUtil.getFacadeBean(request, null, "dataSourceService");
				}
				List<String>deviceTypes = ReportModel.getDeviceTypeList(dataSourceService,SID.currentUser());
				List<String>deviceIps = ReportModel.getDeviceIpList(dataSourceService,SID.currentUser());
				rsMap = ReportModel.getSubTitleData(rptMasterTbImp,deviceTypes,deviceIps,subMap, sTime, eTime, subId, false,request);
				if (null !=rsMap 
						&& (null==subMap.get("subName") || ((String)subMap.get("subName")).indexOf("趋势")<0)
						&& null !=rsMap.get("result") && ((List)rsMap.get("result")).size()>talTop) {
					List result=((List)rsMap.get("result")).subList(0, talTop);
					rsMap.put("result",result);
				}
				Map<Object,Object> data = ReportModel.reformingResult(subMap, rsMap);
				String dvctype =request.getParameter("dvctype");
				/************************************************/
				String logQueryUrl=null;
				String eventQueryUrl=null;
				if (!GlobalUtil.isNullOrEmpty(subMap.get("logQueryCondition"))) {
					String queryCondition=subMap.get("logQueryCondition").toString();
					if (queryCondition.contains("REPORT_QUERY_TYPE=EVENT_QUERY")) {
						eventQueryUrl=ReportUiConfig.reportQueryEventUrl;
					}else {
						logQueryUrl=ReportUiConfig.reportQueryLogUrl;
					}
				}
				if ("Esm/Topsec/SystemRunLog".equals(dvctype) && ! sid.isOperator()) {
					logQueryUrl=null;
				}
				/************************************************/
				Map params = new HashMap();
				params.put("dvcType", dvctype);
				params.put("talTop", talTop);
				params.put("sTime", sTime);
				params.put("eTime", eTime);
				String chartLink = StringUtil.toString(subMap.get("chartLink"),"0");
				int _chartLink = Integer.valueOf(chartLink);
				
				String[] talCategory = null;
				String [] categoryValues = request.getParameterValues(ReportUiConfig.talCategory);
				if(categoryValues!=null){
					talCategory = new String [categoryValues.length];
					for(int i=0,len= categoryValues.length;i<len;i++){
						talCategory[i] =ChangePageEncode.IsoToUtf8(categoryValues[i]);
					}
				}
				
				StringBuffer url = getUrl(ReportUiConfig.reportUrl,request, params,talCategory);
				String surl = url.toString();
				if(_chartLink>0){
					url.append("&superiorId=").append(subList.get(0).get("mstId")).append("&").append(ReportUiConfig.mstrptid).append("=").append(chartLink).append("&drill=true");
				}
				data.put("url", _chartLink>0? url.toString():"");
				url.replace(0, url.length(), surl).replace(0, ReportUiConfig.reportUrl.length(), ReportUiConfig.moreUrl);
				url.append("&").append(ReportUiConfig.mstrptid).append("=").append(subList.get(0).get("mstId"))
				.append("&").append(ReportUiConfig.subrptid).append("=").append(subId);
				data.put("moreUrl", url.toString());
				if (!GlobalUtil.isNullOrEmpty(logQueryUrl) 
						|| !GlobalUtil.isNullOrEmpty(eventQueryUrl) ) {
					if (null !=logQueryUrl) {
						data.put("logQueryUrl", logQueryUrl);
					}
					if (null !=eventQueryUrl) {
						data.put("eventQueryUrl", eventQueryUrl);
					}
					String nodeId=request.getParameter("nodeId");
					if (!GlobalUtil.isNullOrEmpty(nodeId)) {
						params.put("nodeId", nodeId);
					}
					String reportType=request.getParameter("reportType");
					if (!GlobalUtil.isNullOrEmpty(reportType)) {
						params.put("reportType", reportType);
					}
					data.put("frontEndParams", params);
				}
				int type = StringUtil.toInt(StringUtil.toString(subMap.get("chartType"),"1"));
				Map<String,Object> rstMap = CreateChartFactory.getInstance().createChart(type, data);
				
				if(rstMap!=null){
					json = new JSONObject();
					json.put("subID", subId);
					json.put("type",StringUtil.toString(rstMap.get("type"),""));
					json.put("chart", rstMap.get("chart"));
					json.put("table",rstMap.get("table"));
				}
			}
			return json;
		}finally{
			SID.removeCurrentUser() ;
		}
	}
	
	/**
	 *  基本报表更多查询
	 */
	@RequestMapping("moreReport")
	public String moreReport(SID sid,HttpServletRequest request,HttpServletResponse response){
		boolean fromRest  = false;
		if( request.getParameter("fromRest") != null){
			 fromRest =Boolean.parseBoolean( request.getParameter("fromRest"));
		}
		JSONObject jsonObj = new JSONObject();
		ReportBean bean = new ReportBean();
		String onlyByDvctype = request.getParameter("onlyByDvctype");
		bean = ReportUiUtil.tidyFormBean(bean, request);
		String [] talCategory = bean.getTalCategory();
		RptMasterTbService rptMasterTbImp = (RptMasterTbService) SpringContextServlet.springCtx.getBean(ReportUiConfig.MstBean);
		List<Map> subList = rptMasterTbImp.queryTmpList(ReportUiConfig.PaginationSql, new Object[]{StringUtil.toInt(bean.getSubrptid(), 0)});
		JSONObject obj= null;
		StringBuffer url = null;
		String surl ="";
		boolean flag = false;
		
		try {
			SID.setCurrentUser(sid);
			if(subList.size()>0){
				Map subMap = (Map) subList.get(0);
				String subTitle = subMap.get("subName").toString();
				String mstType = request.getParameter("msttype");
				subTitle = ReportUiUtil.viewRptName(subTitle, mstType);
				request.setAttribute("subName", subTitle);
				if(fromRest){
					jsonObj.put("subName", subTitle);
				}
				
				String deviceType = bean.getDvctype();

				// 显示出来的lable
				String paginationViewFiled = null;
				String paginationHtmFiled = null;
				// sql中map的字段
				
				if (!GlobalUtil.isNullOrEmpty(subMap)) {
					if (!GlobalUtil.isNullOrEmpty(subMap.get("paginationViewFiled"))) {
						paginationViewFiled = subMap.get("paginationViewFiled").toString();
					}
					if (!GlobalUtil.isNullOrEmpty(subMap.get("paginationHtmFiled"))) {
						paginationHtmFiled = subMap.get("paginationHtmFiled").toString();
					}
					
				}
				List sqlParam = ReportUiUtil.getPaginationItem(request, paginationHtmFiled);// 前台数值
				
				sqlParam.add(bean.getTalStartTime());// 开始时间 倒数第3个参数
				sqlParam.add(bean.getTalEndTime());// 结束时间倒数第4个参数
				if (!bean.getDvcaddress().equals("")) {
					if (deviceType.equals(LogKeyInfo.LOG_SYSTEM_TYPE) || deviceType.equals("Log/Global/Detail")) {
					} else {
						if (onlyByDvctype != null && onlyByDvctype.equals("onlyByDvctype")) {
						} else {
							sqlParam.add(bean.getDvcaddress()); // dvcaddress 倒数第5个参数
						}
					}
				}
			
				if(!GlobalUtil.isNullOrEmpty(talCategory)){
					for(String str:talCategory){
						if (null!= str) {
							str=str.substring(str.indexOf("***")+3);
							if (str.indexOf("%")>-1) {
								try {
									str = new String(str.getBytes("iso-8859-1"), "UTF-8");
									str = URLDecoder.decode(str, "UTF-8");
								} catch (UnsupportedEncodingException e) {
									e.printStackTrace();
								}
							}
							sqlParam.add(str);
						}
					}
				}

				String pageNum = "1";// 当前页数
				int pageSize = bean.getPagesize() == null ? 10 : Integer.parseInt(bean.getPagesize());
				String pagein = request.getParameter("pagein");
				String pageingo = request.getParameter("pageingo");
				String pageIndex = request.getParameter("pageIndex");

				if (ReportUiUtil.checkNull(pageIndex)) {
					pageNum = pageIndex;
				} else if (ReportUiUtil.checkNull(pagein) && ReportUiUtil.checkNull(pageingo)) {
					pageNum = pagein;
				}
			    String [] nodeId = request.getParameterValues("nodeId");
			    Map<String,Object> rsMap = null;
			    
			    String subId = request.getParameter(ReportUiConfig.subrptid);
				String sTime = bean.getTalStartTime();
				String eTime = bean.getTalEndTime();
				List<Map> subList1 = rptMasterTbImp.queryTmpList(ReportUiConfig.SubTitleSql, new Object[]{StringUtil.toInt(subId, 0)});
				JSONObject json = null;
				Map<String,Object> rsMap1=null;
				if(subList1.size()>0){
					Map subMap1 = (Map) subList1.get(0);
					List<String>deviceTypes=ReportModel.getDeviceTypeList(dataSourceService,SID.currentUser());
					List<String>deviceIps=ReportModel.getDeviceIpList(dataSourceService,SID.currentUser());
					request.setAttribute("moredat", "999");
					rsMap1 = ReportModel.getSubTitleData(rptMasterTbImp,deviceTypes,deviceIps,subMap1, sTime, eTime, subId, false,request);
				}
				rsMap = rsMap1;
				String viewParamItem = ReportModel.createSearchItem(paginationViewFiled, paginationHtmFiled);
				request.setAttribute("viewParamItem", viewParamItem);
				if(fromRest){
					jsonObj.put("viewParamItem", viewParamItem);
				}
				int sumPage= 0;
				if(rsMap!=null){
					sumPage = StringUtil.toInt(StringUtil.toString(rsMap.get("sumPage"), "0"));
				}
				// 总页数
				request.setAttribute("pages", ReportModel.getPages(sumPage, pageSize));
				request.setAttribute("sumdata", sumPage);
				// 当前页
				request.setAttribute("pageIndex", pageNum);
				if(fromRest){
					jsonObj.put("pages", ReportModel.getPages(sumPage, pageSize));
					jsonObj.put("sumdata", sumPage);
					// 当前页
					jsonObj.put("pageIndex", pageNum);
				}
				Map<Object,Object> data = ReportModel.reformingResult(subMap, rsMap);
			    int chartType  = StringUtil.toInt(subMap.get("chartType").toString(),0);       
			   if(chartType>0&&chartType<4){
				   flag = true;
			   }
				Map params = new HashMap();
				params.put("dvcType", request.getParameter("dvctype"));
				params.put("talTop", request.getParameter("talTop"));
				params.put("sTime", bean.getTalStartTime());
				params.put("eTime", bean.getTalEndTime());
				String chartLink = StringUtil.toString(subMap.get("chartLink"),"0");
				int _chartLink = Integer.valueOf(chartLink);
				url = getUrl(ReportUiConfig.reportUrl,request, params,bean.getTalCategory());
				surl = url.toString();
				
				if(_chartLink>0){
					url.append("&superiorId=").append(subList.get(0).get("mstId")).append("&").append(ReportUiConfig.mstrptid).append("=").append(chartLink);
				}
				data.put("url", _chartLink>0? url.toString():"");
				url.replace(0, url.length(), surl).replace(0, ReportUiConfig.reportUrl.length(), ReportUiConfig.moreUrl);
				url.append("&").append(ReportUiConfig.mstrptid).append("=").append(subList.get(0).get("mstId"))
				.append("&").append(ReportUiConfig.subrptid).append("=").append(bean.getSubrptid());
				data.put("moreUrl", url.toString());
				request.setAttribute("moreUrl", url.toString());
				if(fromRest){
					jsonObj.put("moreUrl", url.toString());
				}
				ChartTable table = CreateChartFactory.getInstance ().reformingChartTable(data);
				obj = moreRptAssembleData(table,StringUtil.toInt(pageNum),pageSize);
				url.replace(0, url.length(), surl);
				url.append("&").append(ReportUiConfig.mstrptid).append("=").append(subList.get(0).get("mstId"));
			}
			if(subList!=null){
				request.setAttribute("title",subList.get(0).get("subName"));
				if(fromRest){
					jsonObj.put("title", subList.get(0).get("subName"));
				}
			}
			List<SimDatasource> dslist = new ArrayList<SimDatasource>();
			
			SimDatasource dsource = new SimDatasource();
			if("onlyByDvctype".equals(onlyByDvctype)){
				dsource.setDeviceIp("全部");
				dsource.setNodeId("");
			}else{
				dsource.setDeviceIp(request.getParameter("dvcaddress"));
				dsource.setNodeId(request.getParameter("nodeId"));
			}
			dsource.setSecurityObjectType(bean.getDvctype());
			dslist.add(0, dsource);
			request.setAttribute("dslist", dslist);
			request.setAttribute("flag", flag);
			request.setAttribute("goUrl", url!=null?url.toString():"");
			request.setAttribute("tableOptions", obj);
			request.setAttribute("bean", bean);
			if(fromRest){
				jsonObj.put("dslist", dslist);
				jsonObj.put("flag", flag);
				jsonObj.put("goUrl", url!=null?url.toString():"");
				jsonObj.put("tableOptions", obj);
				jsonObj.put("bean", bean);
				return JSON.toJSONString(jsonObj);
			}
		} finally {
			SID.removeCurrentUser();
		}
	
		return "/page/report/more_report";
	}
	
	private JSONObject moreRptAssembleData(ChartTable table,int pageIndex,int pageSize){
		JSONObject jsonTable = new JSONObject() ;
		String[] fields = table.getFields();
		String[] header = table.getHeader();
		List<JSONObject> columns = new ArrayList<JSONObject>();
		for(int i=0,len=fields.length;i<len;i++){
			JSONObject columnJSON = new JSONObject() ;
			columnJSON.put("field", fields[i]) ;
			columnJSON.put("title", header[i]) ;
			columnJSON.put("width", 200) ;
			columns.add(columnJSON) ;
		}
		jsonTable.put("columns", columns);
		List<JSONObject> data = new ArrayList<JSONObject>();
		List<ColumnData> list = table.getBodyList(); 
		/*jsonTable.put("columns", Math.ceil((list.size()+0.1)/pageSize))
		for(int i=(pageIndex-1)*pageSize,j=0;i<list.size()&&j<pageSize;i++,j++){
			JSONObject dataJSON = (JSONObject) JSON.toJSON(list.get(i).getData()) ;
			data.add(dataJSON);
		}*/
		
		for(ColumnData cdata:list){
			JSONObject dataJSON = (JSONObject) JSON.toJSON(cdata.getData()) ;
			data.add(dataJSON);
		}
		jsonTable.put("data", data);
		return jsonTable;
	}
	/**
	 *  报表导出
	 * @param sid 用户信息
	 */
	@RequestMapping("expMstReport")
	public void expMstReport(SID sid,HttpServletRequest request,HttpServletResponse response,HttpSession session){
		long start = System.currentTimeMillis();
		ReportBean bean = new ReportBean();
		bean = ReportUiUtil.tidyFormBean(bean, request);
		ExpStruct exp = new ExpStruct();
		exp.setMstrptid(bean.getMstrptid());
		exp.setDvc(bean.getDvcaddress());
		exp.setRptTimeS(bean.getTalStartTime());
		exp.setRptTimeE(bean.getTalEndTime());
		exp.setTop(bean.getTalTop());
		String viewItem = StringUtil.toString(request.getParameter("viewItem"), "");
		bean.setViewItem(viewItem);
		// 只有趋势报表没有时间概念
		if (bean.getViewItem().indexOf("2") < 0) {
			exp.setRptType(ReportUiConfig.rptDirection);
			String[] time = ReportUiUtil.getExpTime("month");
			exp.setRptTimeS(time[0]);
			exp.setRptTimeE(time[1]);
		} else {
			exp.setRptType(ReportUiUtil.rptTypeByTime(bean.getTalStartTime(),bean.getTalEndTime()));
		}
		
		if (sid == null) {
			exp.setRptUser("System");
		} else {
			exp.setRptUser(sid.getUserName());
		}
		String expType = request.getParameter("exprpt");
		if (expType.equals("pdf")) {
			response.setContentType("application/pdf");
		} else if (expType.equals("rtf")) {
			response.setContentType("application/msword");
		} else if (expType.equals("excel")) {
			response.setContentType("application/vnd.ms-excel");
		} else if (expType.equals("doc")) {
			response.setContentType("application/msword");
		} else if (expType.equals("docx")) {
			response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		}else if(expType.equals("html")){
			response.setContentType("application/x-javascript;charset=utf-8");
		}
		exp.setFileType(expType);
		javax.servlet.ServletOutputStream out = null;
		ExpMstRpt expMst = new ExpMstRpt() ;
		try {
			SID.setCurrentUser(sid) ;
			long time1 = System.currentTimeMillis();
			RptMasterTbService rptMasterTbImp = (RptMasterTbService) SpringContextServlet.springCtx.getBean(ReportUiConfig.MstBean) ;
			LinkedHashMap<String, List> expmap = ReportModel.expMstReport(rptMasterTbImp,exp, request);
			long time2 = System.currentTimeMillis();
			String title = exp.getRptName();
			//response.setCharacterEncoding("UTF-8");
			
			String filePath=null;
			String htmlName=null;
			String zipFileName=null;
			String tempName=null;
			if (expType.equalsIgnoreCase("html")) {
				StringBuffer html=new StringBuffer();
				exp.setHtml(html);
				filePath=ReportUiUtil.getSysPath();
				filePath=filePath.substring(0, filePath.length()-16)+"htmlExp/";
				ExpMstRpt.setFliePath(filePath+"exphtml/"+"html/");
				HtmlAndFileUtil.createPath(filePath);
				HtmlAndFileUtil.clearPath(filePath);
				HtmlAndFileUtil.createPath(filePath+"exphtml/"+"html/");
				tempName=StringUtil.currentDateToString("yyyyMMddHHmmss");
				htmlName= tempName+ ".htm";
				zipFileName=title+tempName+ReportUiUtil.getFileSuffix(expType);
			}
			List<JasperPrint> jasperPrintList = expMst.creMstRpt(expmap, request, exp);
			long time3 =System.currentTimeMillis();
			
			System.out.println((time1-start)+"  "+(time2-time1)+"  "+(time3-time2));
			
			JRAbstractExporter exporter = ReportUiUtil.getJRExporter(exp.getFileType());
			exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrintList);
	
			if (expType.equalsIgnoreCase("html")) {
				response.setContentType("APPLICATION/OCTET-STREAM");  
				response.setHeader("Content-Disposition","attachment; filename="+java.net.URLEncoder.encode(title, "UTF-8")+tempName +ReportUiUtil.getFileSuffix(expType));
			}else{
				String userAgent = request.getHeader("User-Agent") ;
				String fileName=null;
				if(userAgent.indexOf("Firefox")>0){
					fileName=java.net.URLEncoder.encode(title, "UTF-8") + StringUtil.currentDateToString("yyyyMMddHHmmss") + ReportUiUtil.getFileSuffix(expType);
					response.setHeader("Content-Disposition", "attachment; filename*=\"utf8' '" +fileName + "\"");
				}else{
					fileName = java.net.URLEncoder.encode(title, "UTF-8") + StringUtil.currentDateToString("yyyyMMddHHmmss") + ReportUiUtil.getFileSuffix(expType);
					response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName+"\"");
				}
			}
			out = response.getOutputStream();
			if (expType.equals("excel")) {
				exporter.setParameter(JExcelApiExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.TRUE);
				exporter.setParameter(JExcelApiExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.FALSE);
				exporter.setParameter(JExcelApiExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.TRUE);
			} else if (expType.equals("pdf")) {
				exporter.setParameter(JRPdfExporterParameter.FORCE_LINEBREAK_POLICY, Boolean.TRUE);// 行连接用
			} else if (expType.equals("docx")) {
				
			}
			if (!expType.equalsIgnoreCase("html")) {
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
				exporter.exportReport();
				out.flush();
			}else{
				String html=exp.getHtml().toString();
				HtmlAndFileUtil.writeContent(HtmlAndFileUtil.createFile(filePath+"exphtml/"+"html/"+htmlName),html);
				HtmlAndFileUtil.compressFloderChangeToZip(filePath+"exphtml/", filePath, zipFileName);
				HtmlAndFileUtil.outzipFile(filePath+zipFileName, out);
			}
			
		} catch (Exception e) {
			logger.error("报表导出失败:",e) ;
		} finally {
			clearTmpImage(expMst) ;
			ObjectUtils.close(out) ;
			SID.removeCurrentUser() ;
		}
	}
	private void clearTmpImage(ExpMstRpt expMst){
		if(expMst != null && expMst.reportImages != null){
			for(Map.Entry<ExpDateStruct, String> entry:expMst.reportImages.entrySet()){
				String imageFileName = entry.getValue() ;
				try {
					File f = new File(imageFileName) ;
					if (imageFileName != null && f.exists()) {
						FileUtils.forceDelete(f) ;
					}
				} catch (Exception e) {
					logger.error("删除文件"+imageFileName+"失败",e);
				}
			}
		}
	}
	private StringBuffer getSuperiorUrl(HttpServletRequest request,Map params,String[] talCategory){
		String sid = request.getParameter("superiorId");
		if(sid==null){
			List<Map> list =reportService.getSuperiorId(params.get("mstId").toString());
			if(list!=null&&list.size()>0){
				Map map = list.get(0);
				sid = StringUtil.toString(map.get("mstId"), "0");
			}
		}
		int len = talCategory.length;
		String prefix =ReportUiConfig.reportUrl;
		StringBuffer sb = getConditonUrl(prefix, request, params);
		sb.append("&").append(ReportUiConfig.mstrptid).append("=").append(sid);
		if(len>1){
			for(int i=0;i<len-1;i++){
				sb.append("&").append(ReportUiConfig.talCategory).append("=").append(talCategory[i]);
			}
		}
		return sb;
	}

	private StringBuffer getUrl(String prefix,HttpServletRequest request,Map params,String[] talCategory){
		StringBuffer sb = getConditonUrl(prefix, request, params);
		if(talCategory!=null&&talCategory.length>0){
			for(String str:talCategory){
				if (null == str) {
					continue;
				}
				String valueString="";
				valueString=str;
				if (!ReportUiUtil.checkStringAll16Num(str) && !ReportUiUtil.isEnglishOrNumber(str)) {
					try {
						valueString=URLEncoder.encode(str, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				
				sb.append("&").append(ReportUiConfig.talCategory).append("=").append(valueString);
			}
		}
		return sb;
	}
	
    private StringBuffer getNoConditionUrl(String prefix,HttpServletRequest request,Map params){
    	String type = request.getParameter("type");
		String onlyByDvctype = request.getParameter("onlyByDvctype");
		String reportType = request.getParameter("reportType");
		if ("Esm/Topsec/SimEvent".equals(params.get("dvcType"))
				||"Esm/Topsec/SystemLog".equals(params.get("dvcType"))
				||"Esm/Topsec/SystemRunLog".equals(params.get("dvcType"))
				||"Log/Global/Detail".equals(params.get("dvcType"))) {
			onlyByDvctype="onlyByDvctype";
		}
		String[] nodeIds = request.getParameterValues("nodeId");
		String dvcaddress = request.getParameter(ReportUiConfig.dvcaddress);
		StringBuffer sb = new StringBuffer();
		sb.append(prefix).append(ReportUiConfig.dvctype).append("=").append(params.get("dvcType"));
		if(StringUtil.isNotBlank(onlyByDvctype)){
			sb.append("&onlyByDvctype=onlyByDvctype");
		}
		if(StringUtil.isNotBlank(type)){
			sb.append("&type").append("=").append(type);
		}
		if(StringUtil.isNotBlank(reportType)){
			sb.append("&reportType").append("=").append(reportType);
		}
		if(nodeIds==null&&"onlyByDvctype".equals(onlyByDvctype)){
			String dvcType= params.get("dvcType").toString();
			/**日志报表、事件报表 只有上级节点有，故查询上级节点ID*/
			if(LogKeyInfo.LOG_GLOBAL_DETAIL.equals(dvcType)||LogKeyInfo.LOG_SIM_EVENT.equals(dvcType)){
				dvcType = LogKeyInfo.LOG_SYSTEM_TYPE;
			}
			//此段代码是 可以支持多级。在此改为 单节点 取核心节点，以后如果打开多级格式的话：可以把这代码打开，把下面的代码注掉。
			/*List<String> list = dataSourceService.getAuditorNodeIdByDvcType(dvcType);
			for(String str:list){
				sb.append("&nodeId=").append(str);
			}*/
			sb.append("&nodeId=").append(auditor.getNodeId());
		}else{
			for(String str:nodeIds){
				sb.append("&nodeId=").append(str);
			}
		}
		if(StringUtil.isNotBlank(dvcaddress)){
			sb.append("&").append(ReportUiConfig.dvcaddress).append("=").append(dvcaddress);
		}
		return sb;
    }
	
    private StringBuffer getExportUrl(HttpServletRequest request,Map params,String[] talCategory){
    	StringBuffer sb = getNoConditionUrl(ReportUiConfig.expUrl, request, params);
    	if(talCategory!=null&&talCategory.length>0){
			for(String str:talCategory){
				sb.append("&").append(ReportUiConfig.talCategory).append("=").append(str);
			}
		}
		return sb;
    }
    
	private StringBuffer getConditonUrl(String prefix,HttpServletRequest request,Map params){
		StringBuffer sb = getNoConditionUrl(prefix, request, params);
		sb.append("&").append(ReportUiConfig.TalTop).append("=").append(params.get("talTop"))
		.append("&").append(ReportUiConfig.sTime).append("=").append(params.get("sTime"))
		.append("&").append(ReportUiConfig.eTime).append("=").append(params.get("eTime"));
		return sb;
	}
	private SimDatasource getSelectDataSource(List<SimDatasource> dsList,ReportBean bean,boolean flag,HttpServletRequest request){
		String str = flag||ObjectUtils.isEmpty(bean.getNodeId())  ? null : bean.getNodeId()[0];
		String dvcaddress = request.getParameter(ReportUiConfig.dvcaddress);
		if(!flag&&dvcaddress!=null){
			for(SimDatasource ds:dsList){
				if(ds.getAuditorNodeId()!=null&&ds.getDeviceIp()!=null){
					if(ds.getAuditorNodeId().equals(str)&&ds.getDeviceIp().equals(dvcaddress)){
						return ds;
					}
				}
				
			}
			return ObjectUtils.isEmpty(dsList) ? null : dsList.get(0) ;//如果所有都没有选中的，默认选中第一个日志源
		}
		return null;
	}
	@RequestMapping("hasLogReoprtRole")
	@ResponseBody
	public Object hasLogReoprtRole(SID sid){
		Set<?> devices=GlobalUtil.isNullOrEmpty(sid.getUserDevice())?Collections.emptySet():sid.getUserDevice();
		List<SimDatasource> simDatasources=dataSourceService.getDataSource(DataSourceService.CMD_ALL);
		List<String> dvcTypes = null;
		if (sid.isOperator()) {
			dvcTypes = dataSourceService.getDistinctDvcType(DataSourceService.CMD_ALL);			
		}else{
			dvcTypes=new ArrayList<String>();
			BeanToPropertyValueTransformer trans = new BeanToPropertyValueTransformer("ip") ;
			Collection<String> userDeviceIPs = (Collection<String>) CollectionUtils.collect(devices,trans) ;
			for (SimDatasource simDatasource : simDatasources) {
				Device device = AssetFacade.getInstance().getAssetByIp(simDatasource.getDeviceIp()) ;
				if (device != null && userDeviceIPs.contains(simDatasource.getDeviceIp())) {
					if(!dvcTypes.contains(simDatasource.getSecurityObjectType())){
						dvcTypes.add(simDatasource.getSecurityObjectType());
					}
				}
			}
		}
		Boolean hasLogReoprtRole=null;
		if (GlobalUtil.isNullOrEmpty(dvcTypes)&&sid.hasOperatorRole()) {
			hasLogReoprtRole= false;
		}else if (sid.isAuditor()||sid.hasAuditorRole()) {
			return null;
		}else{
			hasLogReoprtRole= true;
		}
		return hasLogReoprtRole;
	}
	@RequestMapping("reoprtRole")
	@ResponseBody
	public Object reoprtRole(SID sid){
		List<RptMaster> masterReportList = null;
		if (sid.isOperator()) {
			masterReportList=reportService.getAllMyReports();
		}else if (sid.hasOperatorRole()) {
			masterReportList=reportService.showAllMyReportsByUser(sid.getUserName());
		}
		Boolean hasLogReoprtRole=null;
		Boolean hasLogStatisticsRole=false;
		if (sid.isAuditor()||GlobalUtil.isNullOrEmpty(masterReportList)) {
			hasLogReoprtRole= false;
		}else{
			hasLogReoprtRole= true;
		}
		JSONObject result = new JSONObject();
		result.put("hasLogReoprtRole", hasLogReoprtRole);
		result.put("hasLogStatisticsRole", hasLogStatisticsRole);
		return result;
	}
	
	@RequestMapping("userReportRole")
	@ResponseBody
	public Object userReportRole(SID sid){
		JSONObject json = null;
		boolean isAuditor=sid.isAuditor();
		boolean isAdmin=sid.isAdmin();
		boolean isOperator=sid.isOperator();
		boolean hasOpratorRole=sid.hasOperatorRole();
		List<String> deviceIpList = null;
		Set<?> devices=GlobalUtil.isNullOrEmpty(sid.getUserDevice())?Collections.emptySet():sid.getUserDevice();
		List<SimDatasource> simDatasources=dataSourceService.getDataSource(DataSourceService.CMD_ALL);
		if (!isOperator&&hasOpratorRole){
			deviceIpList=new ArrayList<String>();
			BeanToPropertyValueTransformer trans = new BeanToPropertyValueTransformer("ip") ;
			Collection<String> userDeviceIPs = (Collection<String>) CollectionUtils.collect(devices,trans) ;
			for (SimDatasource simDatasource : simDatasources) {
				Device device = AssetFacade.getInstance().getAssetByIp(simDatasource.getDeviceIp()) ;
				if (device != null && userDeviceIPs.contains(simDatasource.getDeviceIp())) {
					if(!deviceIpList.contains(simDatasource.getDeviceIp())){
						deviceIpList.add(simDatasource.getAuditorNodeId()+"AddAuditorNodeID"+simDatasource.getDeviceIp()+"AddAuditorNodeID"+simDatasource.getSecurityObjectType());
					}
				}
			}
		}else if (isOperator) {
			deviceIpList=new ArrayList<String>();
			deviceIpList.add("AUDITORIDAddAuditorNodeIDonlyByDvctypeAddAuditorNodeID全部");
			for (SimDatasource simDatasource : simDatasources) {
				Device device = AssetFacade.getInstance().getAssetByIp(simDatasource.getDeviceIp()) ;
				if(!deviceIpList.contains(simDatasource.getDeviceIp())){
					deviceIpList.add(simDatasource.getAuditorNodeId()+"AddAuditorNodeID"+simDatasource.getDeviceIp()+"AddAuditorNodeID"+simDatasource.getSecurityObjectType());
				}
			}
		}
		json=new JSONObject();
		json.put("isAdmin", isAdmin);
		json.put("isAuditor", isAuditor);
		json.put("isOperator", isOperator);
		json.put("hasOpratorRole", hasOpratorRole);
		json.put("deviceIpList", deviceIpList);
		return json;
	}
	private void removeRepeatDs(List<SimDatasource> simDatasources){
		if (null==simDatasources || simDatasources.size()<1) {
			return;
		}
		List<SimDatasource>removedDatasources=new ArrayList<SimDatasource>();
		for (int i = 0; i < simDatasources.size(); i++) {
			SimDatasource simDatasource=simDatasources.get(i);
			for (int j = i+1; j < simDatasources.size(); j++) {
				SimDatasource simDatasourceOther=simDatasources.get(j);
				if (simDatasource.getDeviceIp().equals(simDatasourceOther.getDeviceIp())
						&& simDatasource.getSecurityObjectType().equals(simDatasourceOther.getSecurityObjectType())) {
					removedDatasources.add(simDatasourceOther);
				}
			}
		}
		if (removedDatasources.size()>0) {
			simDatasources.removeAll(removedDatasources);
		}
	}
}
