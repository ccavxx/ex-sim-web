package com.topsec.tsm.sim.newreport.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.index.IndexTemplateUtil;
import com.topsec.tal.base.report.poi.SimXWPFDocument;
import com.topsec.tal.base.util.LogKeyInfo;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.SpringWebUtil;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.ass.persistence.Device;
import com.topsec.tsm.ass.service.DeviceService;
import com.topsec.tsm.auth.manage.AuthRole;
import com.topsec.tsm.auth.manage.AuthUserDevice;
import com.topsec.tsm.sim.access.util.GlobalUtil;
import com.topsec.tsm.sim.asset.AssetCategory;
import com.topsec.tsm.sim.asset.AssetCategoryUtil;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.AssetObject;
import com.topsec.tsm.sim.asset.group.AssetGroup;
import com.topsec.tsm.sim.asset.group.GroupByAssetCategory;
import com.topsec.tsm.sim.asset.group.GroupByAssetVender;
import com.topsec.tsm.sim.asset.group.GroupStrategy;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.common.model.TreeModel;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.log.service.LogReportTaskService;
import com.topsec.tsm.sim.newreport.bean.ReportQueryConditions;
import com.topsec.tsm.sim.newreport.bean.SimDsVo;
import com.topsec.tsm.sim.newreport.chart.echart.EChartImageFactory;
import com.topsec.tsm.sim.newreport.handler.QueryConditionsFormat;
import com.topsec.tsm.sim.newreport.model.ReportQuery;
import com.topsec.tsm.sim.newreport.util.ExportDocumentUtil;
import com.topsec.tsm.sim.newreport.util.QueryUtil;
import com.topsec.tsm.sim.newreport.util.ResourceContainer;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.report.service.ReportService;
import com.topsec.tsm.sim.report.util.HtmlAndFileUtil;
import com.topsec.tsm.sim.report.util.ReportUiConfig;
import com.topsec.tsm.sim.report.util.ReportUiUtil;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.util.DeviceTypeNameUtil;

@Controller
@RequestMapping("basicreport")
public class BasicReportController{

	private Logger logger = LoggerFactory.getLogger(BasicReportController.class) ;
	
	@Autowired
	private DataSourceService dataSourceService;
	@Autowired
	private DataSourceService monitorService;
	@Autowired
	private ReportService reportService;
	@Autowired
	private DeviceService deviceService;
	@Autowired
	private LogReportTaskService logReportTaskService ;
	@Autowired
	private NodeMgrFacade nodeMgrFacade;
	
	@Autowired
	private ReportQuery reportQuery;
	
	@Autowired
	private QueryConditionsFormat queryConditionsFormat;
	/**
	 * 构建基本报表左侧树
	 */
	@RequestMapping("reportShowTree")
	@ResponseBody
	public Object getReportTree(SID sid,HttpServletRequest request){
		
		TreeModel tree = new TreeModel("basic","基础信息报表","open");
		tree.putAttribute("type",ReportUiConfig.ReportTreeType.TRUNK);
		tree.putAttribute("username", sid.getUserName());
		String roleIds=roles(sid);
		tree.putAttribute("roleIds", roleIds);
		
		if (sid.hasAuditorRole()) {
			TreeModel selfAudit = new TreeModel(LogKeyInfo.LOG_SYSTEM_TYPE,
					"审计报表", "closed");
			selfAudit.putAttribute("securityObjectType", LogKeyInfo.LOG_SYSTEM_TYPE);
			selfAudit.putAttribute("type", ReportUiConfig.ReportTreeType.TRUNK);
			List<TreeModel> schildren = createTreeModel(LogKeyInfo.LOG_SYSTEM_TYPE, request);
			selfAudit.setChildren(schildren);
			tree.addChild(selfAudit);
		}
		if (sid.isOperator()) {
			TreeModel system = new TreeModel(LogKeyInfo.LOG_SYSTEM_RUN_TYPE,
					"系统报表", "closed");
			system.putAttribute("securityObjectType", LogKeyInfo.LOG_SYSTEM_RUN_TYPE);
			system.putAttribute("type", ReportUiConfig.ReportTreeType.TRUNK);
			List<TreeModel> syschildren = createTreeModel(LogKeyInfo.LOG_SYSTEM_RUN_TYPE, request);
			system.setChildren(syschildren);
			tree.addChild(system);
		}
		if (sid.hasOperatorRole()) {
			TreeModel event = new TreeModel(LogKeyInfo.LOG_SIM_EVENT + "",
					"事件报表", "closed");
			event.putAttribute("securityObjectType", LogKeyInfo.LOG_SIM_EVENT);
			event.putAttribute("type", ReportUiConfig.ReportTreeType.TRUNK);
			List<TreeModel> echildren = createTreeModel(LogKeyInfo.LOG_SIM_EVENT, request);
			event.setChildren(echildren);
			tree.addChild(event);
		}
		TreeModel  log= new TreeModel(LogKeyInfo.LOG_GLOBAL_DETAIL,"日志报表","open");
		log.putAttribute("securityObjectType", LogKeyInfo.LOG_GLOBAL_DETAIL);
		log.putAttribute("type", ReportUiConfig.ReportTreeType.TRUNK);
//		List<TreeModel> children = createTreeModel(LogKeyInfo.LOG_GLOBAL_DETAIL);
//		log.addChild(children.get(0));
		if(dataSourceService == null)
			dataSourceService = (DataSourceService) SpringWebUtil.getBean("dataSourceService", request);
		List<String>securityObjectTypes=roleLogType(sid,dataSourceService);

		if (!GlobalUtil.isNullOrEmpty(securityObjectTypes)&&(sid.hasOperatorRole())) {
			for (String securityObjectType : securityObjectTypes) {
				if (!LogKeyInfo.LOG_SYSTEM_TYPE.equals(securityObjectType)
						&& !LogKeyInfo.LOG_SYSTEM_RUN_TYPE.equals(securityObjectType)) {
					String name = DeviceTypeNameUtil.getDeviceTypeName(securityObjectType,
							request.getLocale());
					TreeModel trees = new TreeModel(securityObjectType, name);
					trees.putAttribute("securityObjectType", securityObjectType);
					trees.putAttribute("type",
							ReportUiConfig.ReportTreeType.TRUNK);
					
					List<TreeModel> bchildren = createTreeModel( securityObjectType, request);
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
	private List<TreeModel> createTreeModel(String securityObjectType, HttpServletRequest request){
		List<TreeModel> list = new ArrayList<TreeModel>();
		if(reportQuery == null)
			reportQuery = (ReportQuery) SpringWebUtil.getBean("reportQuery", request);
		List<Map<String,Object>> parentList = reportQuery.findParentTheme(securityObjectType);
		for(Map<String,Object> map:parentList){
			TreeModel model = new TreeModel(StringUtil.toString(map.get("id")),StringUtil.toString(map.get("reportName")),"open");
			model.putAttribute("showUnits",map.get("showUnits"));
			model.putAttribute("securityObjectType", securityObjectType);
			model.putAttribute("type", ReportUiConfig.ReportTreeType.BRANCH);
			list.add(model);
		}
		return list;
	}
	@RequestMapping("reportTreeByBusiness")
	@ResponseBody
	public Object getTreeByBusiness(SID sid,HttpServletRequest request){
		TreeModel tree = new TreeModel("basic","基础信息报表","open");
		tree.putAttribute("type",ReportUiConfig.ReportTreeType.TRUNK);
		tree.putAttribute("username", sid.getUserName());
		String roleIds=roles(sid);
		tree.putAttribute("roleIds", roleIds);
		JSONArray jsonArray = new JSONArray(1) ;
        jsonArray.add(tree) ;
		return jsonArray;
	}
	@RequestMapping("reportComboTree")
	@ResponseBody
	public Object reportComboTree(SID sid,HttpServletRequest request){
		
		TreeModel tree = new TreeModel("basic","基础信息报表","open");
		tree.putAttribute("type",ReportUiConfig.ReportTreeType.TRUNK);
		tree.putAttribute("username", sid.getUserName());
		String roleIds=roles(sid);
		tree.putAttribute("roleIds", roleIds);
		Node node = nodeMgrFacade.getKernelAuditor(false);
		tree.putAttribute("nodeId", node.getNodeId());
		if (sid.hasAuditorRole()) {
			TreeModel selfAudit = new TreeModel(LogKeyInfo.LOG_SYSTEM_TYPE,
					"审计报表", "closed");
			selfAudit.putAttribute("securityObjectType", LogKeyInfo.LOG_SYSTEM_TYPE);
			selfAudit.putAttribute("type", ReportUiConfig.ReportTreeType.TRUNK);
			List<TreeModel> schildren = createTreeModel(LogKeyInfo.LOG_SYSTEM_TYPE, request);
			selfAudit.setChildren(schildren);
			tree.addChild(selfAudit);
		}
		if (sid.isOperator()) {
			TreeModel system = new TreeModel(LogKeyInfo.LOG_SYSTEM_RUN_TYPE,
					"系统报表", "closed");
			system.putAttribute("securityObjectType", LogKeyInfo.LOG_SYSTEM_RUN_TYPE);
			system.putAttribute("type", ReportUiConfig.ReportTreeType.TRUNK);
			List<TreeModel> syschildren = createTreeModel(LogKeyInfo.LOG_SYSTEM_RUN_TYPE, request);
			system.setChildren(syschildren);
			tree.addChild(system);
		}
		if (sid.hasOperatorRole()) {
			TreeModel event = new TreeModel(LogKeyInfo.LOG_SIM_EVENT + "",
					"事件报表", "closed");
			event.putAttribute("securityObjectType", LogKeyInfo.LOG_SIM_EVENT);
			event.putAttribute("type", ReportUiConfig.ReportTreeType.TRUNK);
			List<TreeModel> echildren = createTreeModel(LogKeyInfo.LOG_SIM_EVENT, request);
			event.setChildren(echildren);
			tree.addChild(event);
		}
		TreeModel  log= new TreeModel(LogKeyInfo.LOG_GLOBAL_DETAIL,"日志报表","closed");
		log.putAttribute("securityObjectType", LogKeyInfo.LOG_GLOBAL_DETAIL);
		log.putAttribute("type", ReportUiConfig.ReportTreeType.TRUNK);
//		List<TreeModel> children = createTreeModel(LogKeyInfo.LOG_GLOBAL_DETAIL);
//		log.addChild(children.get(0));
		List<String>securityObjectTypes=roleLogType(sid,dataSourceService);
		if (!GlobalUtil.isNullOrEmpty(securityObjectTypes)&&(sid.hasOperatorRole())) {
			for (String securityObjectType : securityObjectTypes) {
				if (!LogKeyInfo.LOG_SYSTEM_TYPE.equals(securityObjectType)
						&& !LogKeyInfo.LOG_SYSTEM_RUN_TYPE.equals(securityObjectType)) {
					String name = DeviceTypeNameUtil.getDeviceTypeName(securityObjectType,
							request.getLocale());
					TreeModel trees = new TreeModel(securityObjectType, name);
					trees.putAttribute("securityObjectType", securityObjectType);
					trees.putAttribute("type",
							ReportUiConfig.ReportTreeType.TRUNK);
					
					List<TreeModel> bchildren = createTreeModel(sid,securityObjectType,"LOG",request);
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
	
	@RequestMapping("stateMonitorTree")
	@ResponseBody
	public Object stateMonitorTree(SID sid,HttpServletRequest request){
		try {
			SID.setCurrentUser(sid) ;
			TreeModel tree = new TreeModel("monitor", "状态监视报表", "open");
			tree.putAttribute("username", sid.getUserName());
			String roleIds = roles(sid);
			tree.putAttribute("roleIds", roleIds);
			tree.putAttribute("securityObjectType",
					LogKeyInfo.LOG_GLOBAL_DETAIL);
			tree.putAttribute("type", ReportUiConfig.ReportTreeType.TRUNK);
			
			List<SimDatasource> roleDatasources=allRoleDatasources(sid, monitorService);
			
			GroupStrategy strategy = new GroupByAssetCategory(
					new GroupByAssetVender());
			AssetGroup root = AssetFacade.getInstance().groupByWithRoot(
					strategy);
			List<Map<String,Object>> parentList = reportQuery.findParentTheme("MONITOR");
			for (AssetGroup assetGroup : root.getChildren()) {
				TreeModel trees = new TreeModel("deviceType",assetGroup.getName());
				boolean hasChildren=false;
				for (AssetGroup aGroup :assetGroup.getChildren()){
					TreeModel treesubs = new TreeModel("manufacturerType",aGroup.getName());
					
					for(Map<String,Object> map:parentList){
						TreeModel model = new TreeModel(StringUtil.toString(map.get("id")),StringUtil.toString(map.get("reportName")),"closed");
						model.putAttribute("showUnits",map.get("showUnits"));
						model.putAttribute("securityObjectType", "");
						model.putAttribute("type", ReportUiConfig.ReportTreeType.TRUNK);
						for (AssetObject assetObject:aGroup.getAssets()){
							SimDatasource datasource=QueryUtil.containsIpDatasource(roleDatasources, assetObject.getIp());
							if (null != datasource) {
								
								TreeModel subTree = new TreeModel(assetObject.getIp(),assetObject.getName());
								subTree.putAttribute("resourceId", datasource.getResourceId());
								subTree.putAttribute("securityObjectType", datasource.getSecurityObjectType());
								subTree.putAttribute("type", ReportUiConfig.ReportTreeType.BRANCH);
								assetObject.getDeviceType();
								model.addChild(subTree);
								hasChildren=true;
							}
						}
						if (hasChildren) {
							treesubs.addChild(model);
						}
					}
					if (hasChildren) {
						trees.addChild(treesubs);
					}
				}
				if (hasChildren) {
					tree.addChild(trees);
				}
				
			}
			/*System.out.println("********** =================== *************");
			List<AssetGroup> assetGroups = AssetFacade.getInstance().groupBy(
					new GroupByAssetCategory());
			for (AssetGroup assetGroup : assetGroups) {
				TreeModel trees = new TreeModel("deviceType",assetGroup.getName());
				List<AssetObject>assets=assetGroup.getAssets();
				boolean hasChildren=false;
				for(Map<String,Object> map:parentList){
					TreeModel model = new TreeModel(StringUtil.toString(map.get("id")),StringUtil.toString(map.get("reportName")),"closed");
					model.putAttribute("showUnits",map.get("showUnits"));
					model.putAttribute("securityObjectType", "");
					model.putAttribute("type", ReportUiConfig.ReportTreeType.TRUNK);
					for (AssetObject assetObject : assets) {
						SimDatasource datasource=QueryUtil.containsIpDatasource(roleDatasources, assetObject.getIp());
						if (null != datasource) {
							
							TreeModel subTree = new TreeModel(assetObject.getIp(),assetObject.getName());
							subTree.putAttribute("resourceId", datasource.getResourceId());
							subTree.putAttribute("securityObjectType", datasource.getSecurityObjectType());
							subTree.putAttribute("type", ReportUiConfig.ReportTreeType.BRANCH);
							model.addChild(subTree);
							hasChildren=true;
						}
						
					}
					if (hasChildren) {
						trees.addChild(model);
					}
				}
				
				if (hasChildren) {
					tree.addChild(trees);
				}
				
			}
			
			List<String> securityObjectTypes = roleLogType(sid, monitorService);
			if (!GlobalUtil.isNullOrEmpty(securityObjectTypes)
					&& (sid.hasOperatorRole())) {
				for (String securityObjectType : securityObjectTypes) {
					if (!LogKeyInfo.LOG_SYSTEM_TYPE.equals(securityObjectType)
							&& !LogKeyInfo.LOG_SYSTEM_RUN_TYPE
									.equals(securityObjectType)) {
						String name = DeviceTypeNameUtil.getDeviceTypeName(
								securityObjectType, request.getLocale());
						TreeModel trees = new TreeModel(securityObjectType,
								name);
						trees.putAttribute("securityObjectType",
								securityObjectType);
						trees.putAttribute("type",
								ReportUiConfig.ReportTreeType.TRUNK);

						List<TreeModel> bchildren = createTreeModel(sid,
								securityObjectType, "MONITOR");
						trees.setChildren(bchildren);
						trees.setState(bchildren.isEmpty() ? "open" : "closed");
						tree.addChild(trees);
					}
				}

			}*/
			JSONArray jsonArray = new JSONArray(1);
			jsonArray.add(tree);
			return jsonArray;
		} finally {
			SID.removeCurrentUser() ;
		}
	}
	@RequestMapping("newstateMonitorTree")
	@ResponseBody
	public Object newstateMonitorTree(SID sid,HttpServletRequest request){
		try {
			SID.setCurrentUser(sid) ;
			if(sid.isAuditor())
				return new JSONArray(1).add(new TreeModel("", "", "open"));
			TreeModel tree = new TreeModel("monitor", "状态监视报表", "open");
			tree.putAttribute("username", sid.getUserName());
			String roleIds = roles(sid);
			tree.putAttribute("roleIds", roleIds);
			tree.putAttribute("securityObjectType",
					LogKeyInfo.LOG_GLOBAL_DETAIL);
			tree.putAttribute("type", ReportUiConfig.ReportTreeType.TRUNK);
			
			List<SimDatasource> roleDatasources=allRoleDatasources(sid, monitorService);
			
			GroupStrategy strategy = new GroupByAssetCategory(
					new GroupByAssetVender());
			AssetGroup root = AssetFacade.getInstance().groupByWithRoot(
					strategy);
			List<Map<String,Object>> parentList = reportQuery.findParentTheme("MONITOR");
			for (AssetGroup assetGroup : root.getChildren()) {
				TreeModel trees = new TreeModel("deviceType",assetGroup.getName());
				boolean hasChildren=false;
				for (AssetGroup aGroup :assetGroup.getChildren()){
					TreeModel treesubs = new TreeModel("manufacturerType",aGroup.getName());
					
					for (AssetObject assetObject:aGroup.getAssets()){
						String pathId = assetObject.getDeviceType();
						AssetCategory asset = AssetCategoryUtil.getInstance().getCategoryByPathId(pathId);
						if(null == asset)
							continue;
						SimDatasource datasource = QueryUtil.containsIpDatasource(roleDatasources, assetObject.getIp());
						if (null != datasource){
							
							for(Map<String,Object> map:parentList){
								Object fieldsObject = map.get("specialFields");
								String [] specialFields = null;
								if(null !=fieldsObject)
									specialFields = fieldsObject.toString().split(",");
								boolean needCreate = false;
								if (null != specialFields) {
									for (String string : specialFields) {
										if(null != asset.getAttribute(string)){
											needCreate = true;
											break;
										}
									}
								}
								if (! needCreate) 
									continue;
								
								TreeModel model = new TreeModel(StringUtil.toString(map.get("id")),StringUtil.toString(map.get("reportName")),"closed");
								model.putAttribute("showUnits",map.get("showUnits"));
								model.putAttribute("type", ReportUiConfig.ReportTreeType.BRANCH);
								model.putAttribute("securityObjectType", datasource.getSecurityObjectType());
								model.setState("open");
								treesubs.addChild(model);
							}
							hasChildren=true;
							break;
						}
						
					}
					
					if (hasChildren) {
						treesubs.setState("closed");
						trees.addChild(treesubs);
					}
				}
				if (hasChildren) {
					tree.addChild(trees);
				}
				
			}
			JSONArray jsonArray = new JSONArray(1);
			jsonArray.add(tree);
			return jsonArray;
		} finally {
			SID.removeCurrentUser() ;
		}
	}
	private List<TreeModel> createTreeModel(SID sid,String securityObjectType,final String type,HttpServletRequest request){
		List<TreeModel> list = new ArrayList<TreeModel>();
		List<Map<String,Object>> parentList = null;
		if ("MONITOR".equals(type)) parentList = reportQuery.findParentTheme(type);
		else parentList = reportQuery.findParentTheme(securityObjectType);
		for(Map<String,Object> map:parentList){
			TreeModel model = new TreeModel(StringUtil.toString(map.get("id")),StringUtil.toString(map.get("reportName")),"closed");
			model.putAttribute("showUnits",map.get("showUnits"));
			model.putAttribute("securityObjectType", securityObjectType);
			if ("MONITOR".equals(type)) model.putAttribute("type", ReportUiConfig.ReportTreeType.TRUNK);
			else model.putAttribute("type", ReportUiConfig.ReportTreeType.BRANCH);
			
			List<SimDatasource> hasroleDatasources= roleDsByType(sid,securityObjectType,type,request);
			for (SimDatasource simDatasource : hasroleDatasources) {
				String deviceIp=simDatasource.getDeviceIp();
				String deText=deviceIp;
				if("ONLY_BY_DVCTYPE".equals(deviceIp) || "ALL_ROLE_ADDRESS".equals(deviceIp)){
					if ("MONITOR".equals(type)) continue;
					deText="全部";
				}
				TreeModel subModel=new TreeModel(deviceIp, deText, "open");
				subModel.putAttribute("resourceId", simDatasource.getResourceId());
				subModel.putAttribute("securityObjectType", securityObjectType);
				subModel.putAttribute("type", ReportUiConfig.ReportTreeType.BRANCH);
				model.addChild(subModel);
			}
			if (hasroleDatasources.size() == 0) {
				model.setState("open");
			}
			list.add(model);
		}
		return list;
	}
	/**
	 * 基本报表查询
	 */
	@RequestMapping("findReport")
	@ResponseBody
	public Object findReport(SID sid,@RequestBody ReportQueryConditions queryConditions,HttpServletRequest request) throws Exception{
		JSONObject json = null;
		json = new JSONObject();
		QueryUtil.setQueryTime(queryConditions);
		List<SimDatasource> hasroleDatasources= roleDsByType(sid,queryConditions.getSecurityObjectType(),queryConditions.getQueryType(),request);
		SimDatasource selectDses=chooiseDs(hasroleDatasources,queryConditions);
		List<SimDsVo> roleDs=new ArrayList<SimDsVo>();
		for (SimDatasource simDatasource : hasroleDatasources) {
			roleDs.add(new SimDsVo(simDatasource));
		}
		boolean allRoleDevice = false;
		if (null != selectDses) {
			json.put("selectds", new SimDsVo(selectDses));
			allRoleDevice = "ONLY_BY_DVCTYPE".equals(selectDses.getDeviceIp())
					||"ALL_ROLE_ADDRESS".equals(selectDses.getDeviceIp());
		}
		List<Map<String,Object>> data=new ArrayList<Map<String,Object>>();
		for (Integer integer : queryConditions.getParentIds()) {
			if (null != integer) {
				List<Map<String, Object>> subList=reportQuery.findSimpleSubThemes(integer);
				for (Map<String, Object> map : subList) {
					QueryUtil.aliasMapFiledValue(queryConditions.getSecurityObjectType(), map);
					if (allRoleDevice){ 
						if("ALL_DEVICE_GROUP".equals(map.get("reportGroup"))
								|| !"ONE_DEVICE_GROUP".equals(map.get("reportGroup")))
							data.add(map);
					}else if(!"ALL_DEVICE_GROUP".equals(map.get("reportGroup")))
						data.add(map);
					
					
				}
			}
		}
		json.put("roleDs", roleDs);
		json.put("queryConditions", queryConditions);
		json.put("result", data);
		return json;
	}
	
	/**
	 * 小主题查询
	 */
	@RequestMapping("subThemeData")
	@ResponseBody
	public Object subThemeData(@RequestBody ReportQueryConditions queryConditions){
		JSONObject json = new JSONObject();
		QueryUtil.setQueryTime(queryConditions);
		Map<Object, List<Object>> dataMap =reportQuery.findDataByConditions(queryConditions);
		for (Map.Entry<Object, List<Object>>  entry : dataMap.entrySet()) {
			Map keyMap=(Map)entry.getKey();
			json.put("dataStructureDesc", keyMap.get("dataStructureDesc"));
			json.put("reportType", keyMap.get("reportType"));
			json.put("showType", keyMap.get("showType"));
			json.put("describe", keyMap.get("describe"));
			json.put("queryType", keyMap.get("queryType"));
			json.put("queryCondition", keyMap.get("queryCondition"));
			json.put("needReGroup", keyMap.get("needReGroup"));
			String reGroupCol="RESOURCE_ID";
			json.put("reGroupCol", reGroupCol);
			json.put("queryConditionsObj", queryConditions);
			
			List<Object> value=entry.getValue();
			json.put("data", value);
		}
		
		return json;
	}
	
	@RequestMapping("exportReport")
	public void exportReport(SID sid,HttpServletRequest request,HttpServletResponse response){
		try {
			ReportQueryConditions queryConditions=QueryUtil.getRequestReportQueryConditions(request);
			List<Map>maps=reportQuery.findResultPutInDataStructureDescByConditions(queryConditions);
			logger.info("------------报表导出开始 !");
			
			String headline=ExportDocumentUtil.headlineByConditions(queryConditions,reportQuery);
			String reportDesc=ExportDocumentUtil.reportDescByConditions(queryConditions,reportQuery);
			
			Map<String, Object>exportStructMap=new HashMap<String, Object>();
			exportStructMap.put(QueryUtil.EXECUTE_TIME, QueryUtil.nowTime(QueryUtil.TIME_FORMAT));
			exportStructMap.put(QueryUtil.START_TIME, queryConditions.getStime());
			exportStructMap.put(QueryUtil.END_TIME, queryConditions.getEndtime());
			exportStructMap.put(QueryUtil.AUTHOR, sid.getUserName());
			exportStructMap.put(QueryUtil.EXPORT_CATEGORY, QueryUtil.timeQuantum(queryConditions.getStime(), queryConditions.getEndtime()));
			exportStructMap.put(QueryUtil.EXPORT_HEADLINE, headline);
			exportStructMap.put(QueryUtil.REPORT_DESC, reportDesc);
			exportStructMap.put(QueryUtil.SECURITY_OBJECT_TYPE, QueryUtil.getDeviceTypeName(queryConditions.getSecurityObjectType(), request.getLocale()));
			exportStructMap.put(QueryUtil.DVC_ADDRESS, QueryUtil.getQueryDvcAddresses(queryConditions));
			exportStructMap.put(QueryUtil.REPORT_SUMMARY, ExportDocumentUtil.getReportSummary(maps, exportStructMap,queryConditions.getExportFormat()));
			
			if ("pdf".equalsIgnoreCase(queryConditions.getExportFormat())) {
				response.setContentType("application/pdf");
			} else if ("rtf".equalsIgnoreCase(queryConditions.getExportFormat()) 
					||"doc".equalsIgnoreCase(queryConditions.getExportFormat())) {
				response.setContentType("application/msword");
			} else if ("excel".equalsIgnoreCase(queryConditions.getExportFormat())) {
				response.setContentType("application/vnd.ms-excel");
			} else if ("docx".equalsIgnoreCase(queryConditions.getExportFormat())) {
				response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
			}else if("html".equalsIgnoreCase(queryConditions.getExportFormat())){
//				response.setContentType("application/x-javascript;charset=utf-8");
				response.setContentType("APPLICATION/OCTET-STREAM"); 
			}
			String title=headline;
			String timeTitle=StringUtil.currentDateToString("yyyyMMddHHmmss");
			String fileSuffix=QueryUtil.getFileSuffix(queryConditions.getExportFormat());
			if ("html".equalsIgnoreCase(queryConditions.getExportFormat())) {
				response.setHeader("Content-Disposition","attachment; filename="+java.net.URLEncoder.encode(title, "UTF-8")+timeTitle + fileSuffix);
				
			}else{
				String userAgent = request.getHeader("User-Agent") ;
				String fileName=null; 
				if(userAgent.indexOf("Firefox")>0){
					fileName=java.net.URLEncoder.encode(title, "UTF-8") + timeTitle + QueryUtil.getFileSuffix(queryConditions.getExportFormat());
					response.setHeader("Content-Disposition", "attachment; filename*=\"utf8' '" +fileName + "\"");
				}else{
					fileName = java.net.URLEncoder.encode(title, "UTF-8") + timeTitle + QueryUtil.getFileSuffix(queryConditions.getExportFormat());
					response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName+"\"");
				}
			}
			/***********************************************/
			ServletOutputStream out =null;
			try {
				out = response.getOutputStream();
				String filePath=ReportUiUtil.getSysPath();
				filePath=filePath.substring(0, filePath.length()-16);
				if ("html".equalsIgnoreCase(queryConditions.getExportFormat())) {

					String htmlTemplate=ResourceContainer.htmlTemplate;
					exportStructMap.put(QueryUtil.RESULT_DATA_AND_STRUCTURE, JSONObject.toJSONString(maps));
					htmlTemplate=ExportDocumentUtil.setHtml(htmlTemplate,exportStructMap);
					
					String resourceJsPath=filePath+"exportjs/";
					filePath=filePath+"htmlExp/";
					String exportJsPath=filePath+"exphtml/"+"html/"+"js/";
					
					String htmlName= timeTitle+ ".htm";
					String zipFileName=title+timeTitle+fileSuffix;
					HtmlAndFileUtil.createPath(filePath);
					HtmlAndFileUtil.clearPath(filePath);
					HtmlAndFileUtil.createPath(filePath+"exphtml/"+"html/");
					HtmlAndFileUtil.createPath(exportJsPath);
					HtmlAndFileUtil.copyfromPathToPath(resourceJsPath, exportJsPath);
					HtmlAndFileUtil.writeContent(HtmlAndFileUtil.createFile(filePath+"exphtml/"+"html/"+htmlName),htmlTemplate);

					HtmlAndFileUtil.compressFloderChangeToZip(filePath+"exphtml/", filePath, zipFileName);
					HtmlAndFileUtil.outzipFile(filePath+zipFileName, out);
				}else if ("doc".equalsIgnoreCase(queryConditions.getExportFormat()) 
						|| "docx".equalsIgnoreCase(queryConditions.getExportFormat())){
					SimXWPFDocument doc=ExportDocumentUtil.doc(exportStructMap,maps);
					doc.write(out);
				}else if ("excel".equalsIgnoreCase(queryConditions.getExportFormat())
						||"xls".equalsIgnoreCase(queryConditions.getExportFormat())
						||"xlsx".equalsIgnoreCase(queryConditions.getExportFormat())){
					XSSFWorkbook xlsx=ExportDocumentUtil.xlsx(exportStructMap, maps);
					xlsx.write(out);
				}else if ("pdf".equalsIgnoreCase(queryConditions.getExportFormat())) {
					ExportDocumentUtil.pdf(exportStructMap,maps,out);
				}
				out.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				ObjectUtils.close(out) ;
				EChartImageFactory.clearImageFolder();
				SID.removeCurrentUser() ;
			}
			/***********************************************/
			logger.info("------------报表导出结束 !");
		} catch (Exception e) {
			logger.error("报表导出失败:",e);
		}
	}
	
	@RequestMapping("logQueryColumnLevel")
	@ResponseBody
	public Object logQueryColumnLevel(@RequestParam("securityObjectType") String securityObjectType){
		
		Map<String, Object> template=IndexTemplateUtil.getInstance().getTemplateByDeviceType(securityObjectType);
		
		List<Map<String, Object>> groupList=(List<Map<String, Object>>)template.get("groupList");
		List<Map<String, Object>> fieldList=(List<Map<String, Object>>)template.get("fieldList");
		TreeModel tree = new TreeModel("rootBasic","日志查询字段","open");
		tree.putAttribute("type",ReportUiConfig.ReportTreeType.TRUNK);
		for (Map<String, Object> map : groupList) {
			String id=map.get("id").toString();
			String name=map.get("name").toString();
			String filter=map.get("filter").toString();
			Object filterList=map.get("filterList");
			TreeModel treeGroup = new TreeModel(id,name,"closed");
			treeGroup.putAttribute("filter", filter);
			treeGroup.putAttribute("filterList", filterList);
			
			for (Map<String, Object> filedMap : fieldList) {
				Object visiable=filedMap.get("visiable");
				Object searchable=filedMap.get("searchable");
				if ("true".equals(searchable) && "true".equals(visiable)) {
					String group=filedMap.get("group").toString();
					if (group.indexOf(id)>-1) {
						String fieldId=filedMap.get("name").toString();
						String fieldAlias=filedMap.get("alias").toString();
						TreeModel treeFiled = new TreeModel(fieldId,fieldAlias,"open");
						treeGroup.addChild(treeFiled);
					}
				}
			}
			
			tree.addChild(treeGroup);
		}
		JSONArray jsonArray = new JSONArray(1) ;
        jsonArray.add(tree) ;
		return jsonArray;
	}
	
	@RequestMapping("logColumnTemplet")
	@ResponseBody
	public Object logColumnTemplet(@RequestParam("securityObjectType") String securityObjectType){
		
		Map<String, Object> template=IndexTemplateUtil.getInstance().getTemplateByDeviceType(securityObjectType);
		
		List<Map<String, Object>> fieldList=(List<Map<String, Object>>)template.get("fieldList");
		
		JSONArray jsonArray = new JSONArray(1) ;
        jsonArray.add(fieldList) ;
		return jsonArray;
	}
	@RequestMapping("reportRole")
	@ResponseBody
	public Object reportRole(SID sid){
		List<SimDatasource> roleDatasources=allRoleDatasources(sid, dataSourceService);
		Boolean hasCusReoprtRole=false;
		Boolean hasBasicReoprtRole=null;
		Boolean hasLogStatisticsRole=false;
		if (sid.isAuditor()||GlobalUtil.isNullOrEmpty(roleDatasources)) {
			hasBasicReoprtRole= false;
		}else{
			hasBasicReoprtRole= true;
		}
		JSONObject result = new JSONObject();
		result.put("hasCusReoprtRole", hasCusReoprtRole);
		result.put("hasBasicReoprtRole", hasBasicReoprtRole);
		result.put("hasLogStatisticsRole", hasLogStatisticsRole);
		return result;
	}
	private String roles(SID sid){
		StringBuffer roleIds=new StringBuffer();
		Set<AuthRole> userRoles=sid.getRoles();
		for(AuthRole aur:userRoles){
			Integer roleId=aur.getId();
			if(null !=roleId){
				roleIds.append(roleId).append(",");
			}
		}
		int length=roleIds.length();
		if (1<length) {
			return roleIds.substring(0, length-1);
		}
		return null;
	}
	 
	private List<SimDatasource> roleDsByType(SID sid,String securityObjectType,String type,HttpServletRequest request){
		List<SimDatasource> dslist =null;
		if (LogKeyInfo.LOG_SIM_EVENT.equals(securityObjectType)
				|| LogKeyInfo.LOG_SYSTEM_TYPE.equals(securityObjectType)) {
			dslist =new ArrayList<SimDatasource>();
			if(nodeMgrFacade == null)
				nodeMgrFacade = (NodeMgrFacade) SpringWebUtil.getBean("nodeMgrFacade", request);
			Node auditor=nodeMgrFacade.getKernelAuditor(false);
			SimDatasource dsource = allDatasource(securityObjectType,auditor.getNodeId(),"ONLY_BY_DVCTYPE");
			dslist.add(0, dsource);
			return dslist;
		}
		List<SimDatasource> simDatasources = null;
		if("LOG".equals(type)){
			simDatasources=dataSourceService.getDataSourceByDvcType(securityObjectType);
		}else if("MONITOR".equals(type)){
			simDatasources=monitorService.getDataSourceByDvcType(securityObjectType);
		}
		if (null == simDatasources || 0==simDatasources.size()) {
			return simDatasources;
		}
		String auditorNodeId=simDatasources.get(0).getAuditorNodeId();
		removeRepeatDs(simDatasources);
		Set<AuthUserDevice> devices= sid.getUserDevice() == null ? Collections.<AuthUserDevice>emptySet() : sid.getUserDevice(); ;
		dslist =new ArrayList<SimDatasource>();
		if (sid.isOperator()) {
			SimDatasource dsource = allDatasource(securityObjectType,auditorNodeId,"ONLY_BY_DVCTYPE");
			dslist.add(0, dsource);
			dslist.addAll(simDatasources) ;
		}else{
			BeanToPropertyValueTransformer trans = new BeanToPropertyValueTransformer("ip") ;
			Collection<String> userDeviceIPs = (Collection<String>) CollectionUtils.collect(devices,trans) ;
			for (SimDatasource simDatasource : simDatasources) {
				Device device = AssetFacade.getInstance().getAssetByIp(simDatasource.getDeviceIp()) ;
				if (userDeviceIPs.contains(simDatasource.getDeviceIp()) || (device != null && sid.getUserName().equalsIgnoreCase(device.getCreator()))) {
					dslist.add(simDatasource);
				}
			}
			if (0<dslist.size()) {
				SimDatasource dsource = allDatasource(securityObjectType,auditorNodeId,"ALL_ROLE_ADDRESS");
				dslist.add(0, dsource);
			}
		}
		return dslist;
	}
	
	private SimDatasource allDatasource(String securityObjectType,String auditorNodeId ,String ipType){
		SimDatasource dsource = new SimDatasource();
		dsource.setDeviceIp(ipType);
		dsource.setSecurityObjectType(securityObjectType);
		dsource.setAuditorNodeId(auditorNodeId);
		dsource.setResourceName("全部");
		dsource.setNodeId("");
		dsource.setDeviceType(securityObjectType);
		return dsource;
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
	 
	 private List<String> roleLogType(SID sid,DataSourceService dataSourceService){
		 List<SimDatasource> simDatasources=dataSourceService.getDataSource(DataSourceService.CMD_ALL);
		 Set<AuthUserDevice> devices = sid.getUserDevice() == null ? Collections.<AuthUserDevice>emptySet() : sid.getUserDevice();
		 List<String> securityObjectTypes = null;
			if (sid.isOperator()) {
				securityObjectTypes = dataSourceService.getDistinctDvcType(DataSourceService.CMD_ALL);
			}else{
				securityObjectTypes=new ArrayList<String>();
				BeanToPropertyValueTransformer trans = new BeanToPropertyValueTransformer("ip") ;
				Collection<String> userDeviceIPs = (Collection<String>) CollectionUtils.collect(devices,trans) ;
				for (SimDatasource simDatasource : simDatasources) {
					Device device = AssetFacade.getInstance().getAssetByIp(simDatasource.getDeviceIp()) ;
					if ((device != null && sid.getUserName().equalsIgnoreCase(device.getCreator())) ||
						 userDeviceIPs.contains(simDatasource.getDeviceIp())) {
						if(!securityObjectTypes.contains(simDatasource.getSecurityObjectType())){
							securityObjectTypes.add(simDatasource.getSecurityObjectType());
						}
					}
				}
			}
			return securityObjectTypes;
	 }
	 
	 private List<SimDatasource> allRoleDatasources(SID sid,DataSourceService dataSourceService){
		 List<SimDatasource> simDatasources=dataSourceService.getDataSource(DataSourceService.CMD_ALL);
		 Set<AuthUserDevice> devices = sid.getUserDevice() == null ? Collections.<AuthUserDevice>emptySet() : sid.getUserDevice();
		 List<SimDatasource> roleDatasources = null;
			if (sid.isOperator()) {
				roleDatasources=simDatasources;
			}else{
				roleDatasources=new ArrayList<SimDatasource>();
				BeanToPropertyValueTransformer trans = new BeanToPropertyValueTransformer("ip") ;
				Collection<String> userDeviceIPs = (Collection<String>) CollectionUtils.collect(devices,trans) ;
				for (SimDatasource simDatasource : simDatasources) {
					Device device = AssetFacade.getInstance().getAssetByIp(simDatasource.getDeviceIp()) ;
					if ((device != null && sid.getUserName().equalsIgnoreCase(device.getCreator())) ||
						 userDeviceIPs.contains(simDatasource.getDeviceIp())) {
						if(!roleDatasources.contains(simDatasource)){
							roleDatasources.add(simDatasource);
						}
					}
				}
			}
			return roleDatasources;
	 }
	 
	 private SimDatasource chooiseDs(List<SimDatasource> simDatasources,ReportQueryConditions queryConditions){
		 if (null !=simDatasources && 0<simDatasources.size()) {
			 if (null != queryConditions && null != queryConditions.getDvcAddress()) {
				for (SimDatasource simDatasource : simDatasources) {
					if (simDatasource.getDeviceIp().equals(queryConditions.getDvcAddress())) {
						return simDatasource;
					}
				}
			 }else {
				return simDatasources.get(0);
			}
		}
		return null;
	 }
	 
	 {//此代码可以直接查询sub的数据结果
		 /*Map<String, Object> queryMap=reportQuery.findDetailSubTheme(queryConditions.getParentSubId());
			String queryString=queryConditionsFormat.assemblingQueryString(queryMap,queryConditions);
			List<Object> params=queryConditionsFormat.assemblingQueryParams(queryMap, queryConditions);
			Pageable pageable=null;
			if (0 !=queryConditions.getPageSize()) {
				pageable=new PageVo(queryConditions.getPageIndex(),queryConditions.getPageSize());
			}
			queryString=queryConditionsFormat.changeQueryString(queryString,QueryUtil.stringTimeToLong(queryConditions.getStime()),pageable);
			List result=reportQuery.findByConditions(queryString, params, queryConditions.getNodeIds());
			json.put("result", result);*/
		 /*
		  ReportQueryConditions queryConditions=new ReportQueryConditions();
			queryConditions.setExportFormat("docx");
			queryConditions.setStime("2015-08-03 00:00:00");
			queryConditions.setEndtime("2015-08-04 11:00:00");
			queryConditions.setNodeIds(new String[]{"66202d09-265d-458f-b99b-25b7d51b3c27"});
			queryConditions.setSecurityObjectType("Firewall/TOPSEC/TOS/V005");
			queryConditions.setDvcAddress("ALL_ROLE_ADDRESS");
			queryConditions.setParams("DVC_ADDRESS=192.168.75.40,192.168.75.188&");
			queryConditions.setParentIds(new Integer[]{1});
		  */
//			JSONObject json = new JSONObject();
//			json.put("resultList", maps);
//			HtmlAndFileUtil.writeContent(HtmlAndFileUtil.createFile(filePath+"exphtml/"+"html/dat.json"),JSONObject.toJSONString(maps));
//		 try {
//				Runtime.getRuntime().exec("attrib " + "\"" + file.getAbsolutePath() + "\""+ " +H");
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
	 }
}
