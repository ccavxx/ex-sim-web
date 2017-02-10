package com.topsec.tsm.sim.report.web;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.LogKeyInfo;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.Parameter;
import com.topsec.tal.base.util.SpringWebUtil;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.ass.persistence.Device;
import com.topsec.tsm.auth.manage.AuthUserDevice;
import com.topsec.tsm.base.audit.AuditRecord;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.base.type.Severity;
import com.topsec.tsm.common.SIMConstant;
import com.topsec.tsm.resource.AuditCategoryDefinition;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.access.util.GlobalUtil;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.report.common.ReportException;
import com.topsec.tsm.sim.report.common.ReportFileCreator;
import com.topsec.tsm.sim.report.common.SubjectModel;
import com.topsec.tsm.sim.report.common.SubjectType;
import com.topsec.tsm.sim.report.jasper.JRReportFileCreator;
import com.topsec.tsm.sim.report.persistence.RptMaster;
import com.topsec.tsm.sim.report.persistence.RptMstSub;
import com.topsec.tsm.sim.report.persistence.RptRule;
import com.topsec.tsm.sim.report.persistence.RptRuleValue;
import com.topsec.tsm.sim.report.persistence.RptSub;
import com.topsec.tsm.sim.report.service.ReportService;
import com.topsec.tsm.sim.report.util.ChartTypeUtil;
import com.topsec.tsm.sim.report.util.ReportUiUtil;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.util.AuditLogFacade;
import com.topsec.tsm.sim.util.DeviceTypeNameUtil;


@Controller("customReport")
@RequestMapping("customReport")
public class FlexReportController {
	private static Logger log = LoggerFactory.getLogger(FlexReportController.class) ;
	private NodeMgrFacade nodeMgrFacade;
	private DataSourceService dataSourceService;
	
	@Autowired
	private ReportService reportService ;//= (MyReportDao) SpringContextServlet.springCtx.getBean("myReportDao");//getBean(MyReportDao.class);
	
	public static final String BASE_REPORT = "strandReport" ;//基本报表
	public static final String MY_REPORT = "myReport" ;//自定义报表
	public static final String GLOBAL_LOG_REPORT = "Log/Global/Detail" ;
	public static final String GLOBAL_MONITOR_REPORT = "Monitor/Global/Detail";
	
	public static final String PROTOCOL_COLLECTION_KEY = "Protocol" ;//网络协议集合
	public static final String LOG_DATASOURCE_COLLECTION_KEY = "Log" ;//日志源集合
	public static final String TOP_POLICY_COLLECTION_KEY = "TopPolicy" ;//Top n策略集合
	public static final String SUBJECT_CLASS_DEVICE_TYPE = "deviceType" ;//属于设备类型的主题 
	public static final String SUBJECT_CLASS_SPECIAL = "special" ;//属于特殊分类的主题 
	public static final String SUBJECT_CLASS_DEVICE = "device" ;//属于设备的主题 
	
	/**
	 * 报表主题树形菜单
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="getSubjectTree")
	public void getSubjectTree(SID sid,HttpServletRequest request, HttpServletResponse response) throws Exception{
		JSONArray result = new JSONArray() ;
		dataSourceService = getDataSourceService(request);
		if (sid.isAuditor()||sid.hasAuditorRole()) {
			result.add(getSelfAuditReport());
		}
		if (sid.hasOperatorRole()) {
			result.add(getEventReport());
		}
		result.add(getLogReport(sid)) ;
		//result.add(getMonitorObjectReport()) ;
		writeJSONArray(response,result) ;
	}
	/**
	 * 自审计报表
	 * @return
	 */
	private JSONObject getSelfAuditReport(){
		return createSubjectNode("审计报表","special", LogKeyInfo.LOG_SYSTEM_TYPE, false,null);
	}
	
	/**
	 * 事件报表
	 * @return
	 */
	private JSONObject getEventReport(){
		return createSubjectNode("事件报表","special", LogKeyInfo.LOG_SIM_EVENT, false,null);
	}
	
	/**
	 * 日志报表
	 * @return
	 * @throws Exception 
	 */
	private JSONObject getLogReport(SID sid) throws Exception{
		JSONArray child = new JSONArray() ;
		if (sid.isOperator()) {
			child.add(createSubjectNode("全局报表", "special", GLOBAL_LOG_REPORT,
					false, null));
		}
		List<SimDatasource> simDatasources=dataSourceService.getDataSource(DataSourceService.CMD_ALL);
		Set<?> set=sid.getUserDevice();
		List<String> dvcTypes = null;
		if (GlobalUtil.isNullOrEmpty(set)) {
			if (sid.isOperator()) {
				dvcTypes=dataSourceService.getDistinctDvcType(DataSourceService.CMD_ALL);//日志源禁用时不让显示
				dvcTypes.remove("Esm/Topsec/SystemRunLog");
				//dataSourceService.getDistinctDataSourceType(DataSourceService.TYPE_OTHER) ;
			}			
		}else{
			dvcTypes=new ArrayList<String>();
			BeanToPropertyValueTransformer trans = new BeanToPropertyValueTransformer("ip") ;
			Collection<String> userDeviceIPs = (Collection<String>) CollectionUtils.collect(set,trans) ;
			for (SimDatasource simDatasource : simDatasources) {
				Device device = AssetFacade.getInstance().getAssetByIp(simDatasource.getDeviceIp()) ;
				if (device != null && userDeviceIPs.contains(simDatasource.getDeviceIp())) {
					if(!dvcTypes.contains(simDatasource.getSecurityObjectType())){
						dvcTypes.add(simDatasource.getSecurityObjectType());
					}
				}
			}
		}
		JSONObject subject =null;
		if (!GlobalUtil.isNullOrEmpty(dvcTypes) && sid.hasOperatorRole()) {
			for (String deviceType : dvcTypes) {
				String name = DeviceTypeNameUtil.getDeviceTypeName(deviceType,
						Locale.getDefault());
				child.add(createSubjectNode(name, "deviceType", deviceType,
						false, new JSONArray()));
			}
			subject = createSubjectNode("日志报表", "special", "", true, child,
					true);
		} 
		return subject;
	}
	/**
	 * 监视对象报表
	 * @return
	 * @throws Exception 
	 */
	 JSONObject getMonitorObjectReport() throws Exception{
		JSONArray child = new JSONArray() ;
		child.add(createSubjectNode("全局报表",  "special", GLOBAL_MONITOR_REPORT, false,new JSONArray())) ;
		//error code
		/*DataSourceService monitorService = (DataSourceService)SpringContextServlet.springCtx.getBean("monitorService") ;
		List<String> dataSourceTypes = monitorService.getDistinctDataSourceType(DataSourceService.TYPE_OTHER) ;
		for (String deviceType : dataSourceTypes) {
			String name = DeviceTypeNameUtil.getDeviceTypeName(deviceType,Locale.getDefault());
			child.add(createSubjectNode(name, "deviceType", "Monitor/"+deviceType, false, new JSONArray()));
		}
		JSONObject report = createSubjectNode("监视对象报表", "special", "", true, child);
		return report;
		*/
		return null ;
	}
	/**
	 * 创建主题节点,此方法会自动生成一个用来标识每一个节点的uuid属性，此属性只是用于前台区分每一个节点，没有其他任何意义
	 * @param name 节点名称
	 * @param nodeType 节点类型:special(自定义节点),deivceType(设备类型节点),device(设备节点)
	 * @param deviceType 设备类型
	 * @param isLoaded 数据是否已经加载
	 * @param children 子节点
	 * @param open 如果数据已经加载是否打开
	 * @return
	 */
	private JSONObject createSubjectNode(String name,String nodeType,String deviceType,boolean isLoaded,JSONArray children){
		return createSubjectNode(name, nodeType, deviceType, isLoaded, children, false) ;
	}
	/**
	 * 创建主题节点,此方法会自动生成一个用来标识每一个节点的uuid属性，此属性只是用于前台区分每一个节点，没有其他任何意义
	 * @param name 节点名称
	 * @param nodeType 节点类型:special,deivceType
	 * @param deviceType 设备类型
	 * @param isLoaded 下级数据是否已经加载
	 * @param children 子节点
	 * @param open 如果下级数据已经加载是否打开
	 * @return
	 */
	private JSONObject createSubjectNode(String name,String nodeType,String deviceType,boolean isLoaded,JSONArray children,boolean open){
		JSONObject subjectNode = new JSONObject(new JSONObject()) ;
		subjectNode.put("uuid", StringUtil.getUUIDString()) ;
		subjectNode.put("name",ReportUiUtil.changeRptName2(name,true)) ;
		subjectNode.put("nodeType", nodeType) ;
		subjectNode.put("deviceType", deviceType) ;
		subjectNode.put("isLoaded", isLoaded) ;
		subjectNode.put("open", open) ;
		subjectNode.put("children", children) ;
		return subjectNode ;
	}
	/**
	 * 获得报表下级主题,下级有两类:主题分类,报表主题
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping("getChildSubject")
	public void getChildSubject(SID sid,HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		String deviceType = request.getParameter("deviceType");
		String type = request.getParameter("nodeType") ;
		JSONArray child ;
		if("special".equals(type)||"device".equals(type)){//special：自定义根节点类型,device:设备结点
			child = getSubjectsByDeviceType(deviceType) ;
		}else if("deviceType".equals(type)){//设备类型结点
			child = getDataSourcesByDeviceType(sid,deviceType) ;
		}else{
			child = null ;
		}
		JSONObject result = new JSONObject();
		result.put("children", child) ;
		result.put("deviceType", deviceType) ;
		result.put("uuid", request.getParameter("uuid")) ;//此属性用于确定触发点击事件的树的节点
		writeJSONObject(response,result) ;
	}
	/**
	 * 获取指定deviceType(设备类型)下的数据源列表
	 * @param deviceType 设备类型
	 * @return
	 * @throws Exception 
	 */
	private JSONArray getDataSourcesByDeviceType(SID sid,String deviceType) throws Exception{
		JSONArray result = new JSONArray() ;
		/*List<SimDatasource> dataSources  = dataSourceService.getDataSourcesByDeviceType(DataSourceService.CMD_ALL,deviceType) ;
		Set<?> set=sid.getUserDevice();
		BeanToPropertyValueTransformer trans = new BeanToPropertyValueTransformer("ip") ;
		Collection<String> userDeviceIPs = (Collection<String>) CollectionUtils.collect(set,trans) ;
		if(dataSources!=null){
			
			for(SimDatasource ds: dataSources){
				
				if (userDeviceIPs.contains(ds.getDeviceIp())){
					JSONObject node = createSubjectNode(ds.getResourceName(), "device", deviceType, false,null) ;
					node.put("deviceIp", ds.getDeviceIp()) ;
					result.add(node) ;
				}
			}
		}*/
		return result ;	
	}
	/**
	 * 获取设备分类下的主题信息
	 * @param deviceType
	 * @return
	 */
	private JSONArray getSubjectsByDeviceType(String deviceType){
		List<RptSub> childSubject = reportService.getSubRepByDeviceType(deviceType);
		if (ObjectUtils.isEmpty(childSubject)){
			childSubject = reportService.getSubRepByDeviceType(getFirstDeviceType(deviceType));
		}
		JSONArray child = new JSONArray() ;
		for(RptSub rpt:childSubject){
			boolean isDigSubject = StringUtil.booleanVal(rpt.getSubDig()) ;
			if(isDigSubject){
				continue ;
			}
			JSONObject subject = createSubjectNode(rpt.getSubName(), "deviceType", rpt.getSubSubject(), false, null) ;
			subject.put("id", rpt.getId()) ;
			child.add(subject) ;
		}
		return child ;
	}
	
	/**
	 * 获得主题分类下所有的主题
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping("getSubject")
	public void getSubject(HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		String deviceType = request.getParameter("deviceType");//设备类型
		String subjectClass = request.getParameter("nodeType") ;//主题分类,special：自定义分类节点,device:设备节点,deviceType:设备类型节点
		String subjectClassName = StringUtil.recode(request.getParameter("subjectClassName")) ;//主题分类名称
		String deviceIp = request.getParameter("deviceIp") ;//当subjectClass是设备节点时,对应的设备ip地址
		List<RptSub> childSubject = reportService.getSubRepByDeviceType(deviceType);
		if (ObjectUtils.isEmpty(childSubject)){
			childSubject = reportService.getSubRepByDeviceType(getFirstDeviceType(deviceType));
		}
		JSONObject result = new JSONObject() ;
		JSONArray child = new JSONArray() ;
		for(RptSub rpt:childSubject){
			JSONObject subject = new JSONObject() ;
			boolean isDigSubject = StringUtil.booleanVal(rpt.getSubDig()) ;
			if(isDigSubject || rpt.getSubName().contains("跟踪")){
				continue ;//如果是下探主题,直接过滤掉,如果需要可以将下面代码注释去掉,这样前台就可以选择下探主题
				/*List<RptRule> rules = reportService.getSubjectParams(rpt.getId()) ;
				//下探主题参数
				JSONArray digParams = new JSONArray() ;
				for(RptRule rule:rules){
					digParams.add(createJsonRule(rule)) ;
				}
				subject.put("digParams", digParams) ;
				subject.put("subjectType", "digChart") ;*/
			}else{
				subject.put("subjectType", SubjectType.CHART) ;
			}
			subject.put("id", rpt.getId()) ;
			subject.put("name", ReportUiUtil.changeRptName(rpt.getSubName(),true))  ;
			subject.put("deviceType", deviceType) ;
			subject.put("subjectClass", subjectClass) ;
			subject.put("subjectClassName", subjectClassName) ;
			subject.put("deviceIp", deviceIp) ;
			subject.put("chartType", rpt.getChartType()) ;
			subject.put("fcChartType", ChartTypeUtil.getFCChartType(rpt.getChartType()));
			child.add(subject) ;
		}
		result.put("child", child) ;
		writeJSONObject(response,result) ;
	}
	/**
	 * 显示用户自定义报表左侧树
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("getMyReportTree")
	public void getMyReportTree(SID sid,HttpServletRequest request, HttpServletResponse response)throws Exception{
		 
		dataSourceService = getDataSourceService(request) ;
		List<RptMaster> myReportList = null;
		if (sid.isOperator()) {
			myReportList=reportService.getAllMyReports();
		 }else{
			 myReportList=reportService.showAllMyReportsByUser(sid.getUserName());
		 }
		JSONObject result = new JSONObject() ;
		if (myReportList != null && myReportList.size() > 0) {
			JSONArray arr = new JSONArray() ;
			for (RptMaster rptMaster : myReportList) {
				JSONObject obj = new JSONObject() ;
				obj.put("id", rptMaster.getId()) ;
				obj.put("name", rptMaster.getMstName()) ;
				obj.put("type", rptMaster.getMstType()) ;
				obj.put("layoutString", rptMaster.getLayout()) ;
				arr.add(obj) ;
			}
			result.put("treeData", arr) ;
		}
		result.put("nodeIds", getNodeIds(request)) ;
		writeJSONObject(response,result) ;
	}
	/**
	 * 获取系统各节点id
	 * @return
	 * @throws Exception
	 */
	private String[] getNodeIds(HttpServletRequest request)throws Exception{
		nodeMgrFacade = (NodeMgrFacade) SpringWebUtil.getBean("nodeMgrFacade", request);
		List<Node> nodes = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_AUDIT, false, false, false, false);
		if(ObjectUtils.isNotEmpty(nodes)){
			int nodeLength = nodes.size() ;
			String[] nodeIds = new String[nodeLength] ;
			for(int i=0;i<nodeLength;i++){
				nodeIds[i] = nodes.get(i).getNodeId();
			}
			return nodeIds ;
		}
		return new String[0] ;
	}

	/**
	 * 创建一个新的报表
	 *
	 * SID 也可以通过参数（SID sid）引入
	*/
	@RequestMapping("createReport")
	public void createReport(SID sid,HttpServletRequest request, HttpServletResponse response)throws Exception{
		request.setCharacterEncoding("UTF-8");
		JSONObject result = new JSONObject() ;
		if(sid==null){
			result.put("isSuccess", false) ;
			result.put("errorMessage", "你还没有登录或者登录超时，请重新登录！") ;
			writeJSONObject(response,result) ;
			return ;
		}
		try{
			RptMaster rpt = new RptMaster(null,request.getParameter("mstName"),2,"0") ;
			rpt.setCreTime(new Date()) ;
			rpt.setLastUpdated(rpt.getCreTime()) ;
			rpt.setDelflg(0) ;
			rpt.setCreUser(sid.getUserName()) ;
			rpt.setCreIp(sid.getLoginIP()) ;
			rpt.setLayout(request.getParameter("layout")) ;
			setSubSubject(rpt, rpt.getLayout()) ;
			Integer reportId = reportService.addMyReport(rpt) ;
			if(reportId != null){
				logUserOperation(AuditCategoryDefinition.SYS_ADD, "添加自定义报表", "添加自定义报表,名称:" + rpt.getMstName(), sid.getUserName(), rpt.getCreIp(), IpAddress.getLocalIp().toString(), true, Severity.MEDIUM) ;
				result.put("reportId", reportId) ;
				result.put("isSuccess", true) ;
				result.put("name",rpt.getMstName()) ;
				result.put("uuid", request.getParameter("uuid")) ;
			}else{
				result.put("isSuccess", false) ;
				result.put("errorMessage", "保存失败！") ;
			}
		}catch(ReportException e) {
			log.error(e.getMessage()) ;
			result.put("isSuccess", false) ;
			result.put("errorMessage", e.getMessage()) ;
		}catch (Exception e) {
			e.printStackTrace() ;
			log.error(e.getMessage()) ;
			result.put("isSuccess", false) ;
			result.put("errorMessage", "系统出现异常!!!") ;
		}
		writeJSONObject(response,result);
	}
	/**
	 * 根据xml布局数据，将报表与主题进行关联
	 * @param rpt
	 * @param layout
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void setSubSubject(RptMaster rpt,String layout){
		SAXReader reader = new SAXReader() ;
		try {
			Document doc = reader.read(new StringReader(layout)) ;
			Element root = doc.getRootElement() ;
			List<Element> rows = root.elements("row") ;
			int rowCount = rows.size() ;
			@SuppressWarnings("rawtypes")
			List subList = new ArrayList<RptMstSub>() ;
			for(int i=0;i<rowCount;i++){
				Element row = rows.get(i) ;
				List<Element> columns = row.elements("column") ;
				int columnCount = columns.size() ;
				for(int j=0;j<columnCount;j++){
					Element column = columns.get(j) ;
					Element subject = column.element("Subject") ;
					if(subject==null){
						throw new ReportException(MessageFormat.format("报表第{0}行,第{1}列没有指定主题!!!",i+1,j+1)) ;
					}
					RptMstSub sub = new RptMstSub() ;
					sub.setSubId(StringUtil.toInteger(subject.attributeValue("id"))) ;
					sub.setDeviceType(subject.attributeValue("deviceType")) ;
					sub.setRptRuleValues(getRuleValues(subject,rpt)) ;
					sub.setCreIp(rpt.getCreIp()) ;
					sub.setCreTime(rpt.getCreTime()) ;
					sub.setLastUpdated(new Date()) ;
					sub.setCreUser(rpt.getCreUser()) ;
					sub.setSubRow(i+1) ;
					sub.setSubColumn(j+1) ;
					subList.add(sub) ;
				}
			}
			rpt.setRepMstSubs(subList) ;
		} catch (DocumentException e) {
			throw new RuntimeException(e) ;
		}
	}
	/**
	 * 获得主题对应的规则信息
	 * @param subject
	 * @param rpt
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<RptRuleValue> getRuleValues(Element subject,RptMaster rpt) {
		Integer subjectId = StringUtil.toInteger(subject.attributeValue("id")) ;
		List<RptRule> allRules = reportService.getAllRules(subjectId) ;
		if(ObjectUtils.isEmpty(allRules)){
			return null ;
		}
		//下探主题规则参数信息
		Map<Integer,Element> digRules = new HashMap<Integer,Element>() ;
		List<Element> digRuleElements = subject.selectNodes("DigParams/Rule") ;
		if(ObjectUtils.isNotEmpty(digRuleElements)){
			for(Element e:digRuleElements){
				digRules.put(StringUtil.toInteger(e.attributeValue("id")),e) ;
			}
		}
		List<RptRuleValue> rules = new ArrayList<RptRuleValue>(allRules.size()) ;
		for(RptRule rule:allRules){
			RptRuleValue ruleValue = new RptRuleValue() ;
			ruleValue.setCreIp(rpt.getCreIp());
			ruleValue.setCreTime(rpt.getCreTime());
			ruleValue.setLastUpdated(new Date()) ;
			ruleValue.setCreUser(rpt.getCreUser());
			ruleValue.setRuleId(rule.getId()) ;
			setRuleSqlValue(subject,rule,ruleValue,digRules) ;
			rules.add(ruleValue) ;
		}
		return rules ;
	}

	/**
	 * 设置规则的
	 * @param rule
	 * @param ruleValue
	 * @param digRules
	 */
	private void setRuleSqlValue(Element subject,RptRule rule, RptRuleValue ruleValue,Map<Integer, Element> digRules) {
		String sqlValue ;
		if(digRules.containsKey(rule.getId())){
			Element ruleElement = digRules.get(rule.getId()); 
			sqlValue = ruleElement.attributeValue("sqlValue") ;
		}else{
			sqlValue = rule.getSqlDefValue() ;
		}
		//ruleName==2表示主题的检索范围是设备或者设备类型
		if(rule.getRuleName()==2){
			String subjectClass = subject.attributeValue("subjectClass") ;
			//如果用户选择的设备下的主题,就获取设备的ip作为参数进行检索
			if("device".equals(subjectClass)){
				sqlValue = subject.attributeValue("deviceIp") ;
			}else{//如果是设备类型,就根据设备类型进行检索
				sqlValue = "onlyByDvctype;;;"+subject.attributeValue("deviceType") ;
			}
		}
		ruleValue.setSqlValue(sqlValue) ;
	}

	/**
	 * 修改报表
	 */
	@RequestMapping("modifyReport")
	public void modifyReport(SID sid,HttpServletRequest request, HttpServletResponse response)throws Exception{
		
		
		JSONObject result = new JSONObject() ;
		if(sid==null){
			result.put("isSuccess", false) ;
			result.put("errorMessage", "你还没有登录或者登录超时，请重新登录！") ;
			writeJSONObject(response, result) ;
			return ;
		}
		String rptId = request.getParameter("reportId") ;
		if(StringUtil.isBlank(rptId)){
			result.put("isSuccess", false) ;
			result.put("errorMessage", "无效的记录！") ;
			writeJSONObject(response, result) ;
			return ;
		}
		try{
			RptMaster rpt = reportService.getMyReportById(StringUtil.toInt(rptId, 0)) ;
			rpt.setMstName(StringUtil.trim(request.getParameter("mstName"))) ;
			rpt.setLayout(request.getParameter("layout")) ;
			rpt.setLastUpdated(new Date()) ;
			rpt.setLastUser(sid.getUserName()) ;
			rpt.setLastIp(sid.getLoginIP()) ;
			setSubSubject(rpt, rpt.getLayout()) ;
			reportService.updateMyReport(rpt)  ;
			logUserOperation(AuditCategoryDefinition.SYS_UPDATE, "修改自定义报表", "修改自定义报表,名称:" + rpt.getMstName(), sid.getUserName(), rpt.getCreIp(), IpAddress.getLocalIp().toString(), true,Severity.LOWEST) ;
			result.put("reportId", rptId) ;
			result.put("mstName", rpt.getMstName()) ;
			result.put("name", rpt.getMstName()) ;
			result.put("uuid", request.getParameter("uuid")) ;
			result.put("isSuccess", true) ;
		}catch (ReportException e) {
			e.printStackTrace() ;
			log.error(e.getMessage()) ;
			result.put("isSuccess", false) ;
			result.put("errorMessage", e.getMessage()) ;
		}catch (Exception e) {
			e.printStackTrace() ;
			log.error(e.getMessage()) ;
		}
		writeJSONObject(response, result) ;
	}
	
	/**
	 * 删除报表
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("deleteReport")
	public void deleteReport(SID sid,HttpServletRequest request, HttpServletResponse response)throws Exception{
		
		JSONObject result = new JSONObject() ;
		if(sid==null){
			result.put("errorMessage","你还没有登录或者登录超时，请重新登录！") ;
			result.put("isSuccess", false) ;
			return ;
		}
		String reportId = request.getParameter("reportId") ;
		if(StringUtil.isBlank(reportId)){
			result.put("errorMessage","无效的记录！") ;
			result.put("isSuccess", false) ;
			return ;
		}
		boolean isSuccess = false ;
		String logDesc = "" ;
		try{
			RptMaster rpt = reportService.removeMyReport(StringUtil.toInteger(reportId));
			logDesc = (isSuccess = rpt != null) ? "删除自定义报表,名称:" + rpt.getMstName() : "删除自定义报表失败!";
		}catch (Exception e) {
			e.printStackTrace() ;
			log.error(e.getMessage()) ;
			result.put("errorMessage", "删除失败！") ;
			logDesc = "删除自定义报表失败:"+e.getMessage() ;
		}finally{
			logUserOperation(AuditCategoryDefinition.SYS_DELETE, "删除自定义报表",logDesc, sid.getUserName(), sid.getLoginIP(), IpAddress.getLocalIp().toString(), true, Severity.MEDIUM) ;
		}
		result.put("reportId", reportId) ;
		result.put("isSuccess", isSuccess) ;
		writeJSONObject(response,result) ;
	}	
	/**
	 * 获取主题图表数据
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping("getSubjectData")
	public void getSubjectData(HttpServletRequest request, HttpServletResponse response)throws Exception{
		response.setCharacterEncoding("UTF-8");
		try {
			response.setContentType("text/xml") ;
			PrintWriter writer = response.getWriter() ;
			int subjectId = StringUtil.toInt(request.getParameter("id")) ;
			Parameter parameter = new Parameter(request.getParameterMap()) ;
			SubjectModel model = new SubjectModel(subjectId,parameter) ;
			writer.write(model.getFusionChartXML()) ;
		} catch (Exception e) {
			e.printStackTrace() ;
			log.error(e.getMessage()) ;
		}
	}
	/**
	 * 导出自定义报表
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	
	@RequestMapping("exportReport")
	public void exportReport(HttpServletRequest request, HttpServletResponse response)throws Exception{
		
		long beginTime = System.currentTimeMillis() ;
		String rptId = request.getParameter("reportId") ;
		String fileFormat = request.getParameter("fileFormat") ;//要导出的文件格式
		RptMaster rpt = reportService.getMyReportById(StringUtil.toInt(rptId, 0)) ;
		response.setHeader("Content-Disposition", "attachment; filename=\"" + java.net.URLEncoder.encode(rpt.getMstName(), "UTF-8")+StringUtil.currentDateToString("yyyy-MM-dd HH:mm:ss")+"."+fileFormat+"\"");
		ReportFileCreator exporter = new JRReportFileCreator(rpt, fileFormat, new Parameter(request.getParameterMap())) ;
		OutputStream os = response.getOutputStream() ;
		exporter.exportReportTo(os) ;
		long endTime = System.currentTimeMillis() ;
		ObjectUtils.close(os) ;
		System.out.println("总耗时:"+(endTime-beginTime));
	}

	private DataSourceService getDataSourceService(HttpServletRequest request) {
		if (this.dataSourceService == null) {
			this.dataSourceService = (DataSourceService) SpringWebUtil.getBean("dataSourceService",request);
		}
		return this.dataSourceService;
	}
	/**
	 * 记录用户操作日志
	 * @param cat2　日志分类
	 * @param name　日志名称
	 * @param desc　日志描述
	 * @param subject 动作发起者
	 * @param srcAddress　源地址
	 * @param destAddress　目标地址
	 * @param result　结果
	 * @param serverity　安全级别
	 */
	private void logUserOperation(String cat2,String name,String desc,String subject,String srcAddress,String destAddress,boolean result,Severity serverity){
		AuditRecord _log = AuditLogFacade.createConfigAuditLog();
		_log.setBehavior(cat2);
		_log.setSecurityObjectName(name);
		_log.setDescription(desc);
		_log.setSubject(subject);
		_log.setSubjectAddress(IpAddress.IPV4_LOCALHOST);
		_log.setObjectAddress(IpAddress.IPV4_LOCALHOST);
		_log.setSuccess(true);
		_log.setSeverity(serverity);
		AuditLogFacade.send(_log);// 发送系统自审计日志
	}	
	/**
	 * 返回第一级设备类型,如果设备类型为null,返回""
	 * @param deviceType 设备类型
	 * @return
	 */
	private String getFirstDeviceType(String deviceType){
		if(StringUtil.isBlank(deviceType)){return "";}
		return deviceType.substring(0,deviceType.indexOf('/')) ;
	}
	/**
	 * 输出json数据
	 */
	private void writeJSONObject(HttpServletResponse response,JSONObject json) throws IOException{
		response.setCharacterEncoding("utf-8");
		response.getWriter().write(json.toString()) ;
	}
	/**
	 * 输出JSONArray数据
	 */
	private void writeJSONArray(HttpServletResponse response,JSONArray json) throws IOException{
		response.setCharacterEncoding("utf-8");
		response.getWriter().write(json.toString()) ;
	}

}