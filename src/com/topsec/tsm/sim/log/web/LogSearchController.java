package com.topsec.tsm.sim.log.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.hyperic.sigar.Sigar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.topsec.tal.base.index.IndexTemplateUtil;
import com.topsec.tal.base.index.template.GroupCollection;
import com.topsec.tal.base.index.template.IndexTemplate;
import com.topsec.tal.base.index.template.LogField;
import com.topsec.tal.base.report.poi.SimXWPFDocument;
import com.topsec.tal.base.report.poi.XWPFUtil;
import com.topsec.tal.base.search.LogCountSet;
import com.topsec.tal.base.search.LogExportObject;
import com.topsec.tal.base.search.LogExportResult;
import com.topsec.tal.base.search.LogRecordSet;
import com.topsec.tal.base.search.LogSearchException;
import com.topsec.tal.base.search.SearchObject;
import com.topsec.tal.base.util.ChainMap;
import com.topsec.tal.base.util.LogKeyInfo;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.SpringWebUtil;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.ass.persistence.Device;
import com.topsec.tsm.auth.manage.ColumnConfigId;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.comm.CommunicationExpirationException;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.AssetObject;
import com.topsec.tsm.sim.asset.DataSourceUtil;
import com.topsec.tsm.sim.asset.group.AssetGroup;
import com.topsec.tsm.sim.asset.group.GroupByAssetCategory;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.auth.service.UserService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.common.bean.Result;
import com.topsec.tsm.sim.common.model.TreeModel;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.log.LogCountStatTask;
import com.topsec.tsm.sim.log.LogStatsUtils;
import com.topsec.tsm.sim.log.bean.LogSearchObject;
import com.topsec.tsm.sim.log.formatter.FieldFormatter;
import com.topsec.tsm.sim.log.formatter.FormatterFactory;
import com.topsec.tsm.sim.log.formatter.Ip2NameFormatter;
import com.topsec.tsm.sim.log.util.LogRecordList;
import com.topsec.tsm.sim.log.util.LogSearchTimerTask;
import com.topsec.tsm.sim.log.util.LogUtil;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.node.util.NodeStatusQueueCache;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.util.CommonUtils;
import com.topsec.tsm.sim.util.DeviceTypeNameUtil;
import com.topsec.tsm.sim.util.FacadeUtil;
import com.topsec.tsm.sim.util.FastJsonUtil;
import com.topsec.tsm.sim.util.FtpConfigUtil;
import com.topsec.tsm.sim.util.NodeUtil;
import com.topsec.tsm.sim.util.RouteUtils;

@Controller
@RequestMapping("logSearch")
public class LogSearchController {
	private static final Logger log = LoggerFactory.getLogger(LogSearchController.class);
	
	private static final String CMD = MessageDefinition.CMD_SEARCH_LOG; 
	private NodeMgrFacade nodeMgrFacade;
	public static Map<String, LogUtil> queryMap = new ConcurrentHashMap<String, LogUtil>();//key:userName
	private static LogSearchTimerTask myTimeTask = null; 
	private DataSourceService dataSourceService;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private boolean groupFilterEnable=StringUtil.booleanVal(System.getProperty("ENABLE.GROUP.FILTER")) ;
	@Autowired
	@Qualifier("dataSourceService")
	public void setDataSourceService(DataSourceService dataSourceService) {
		this.dataSourceService = dataSourceService;
	}
	@Autowired
	public void setNodeMgr(NodeMgrFacade nodeMgr) {
		this.nodeMgrFacade = nodeMgr;
	}
	/**
	 * @method 获取日志列集树
	 * @author zhou_xiaohu
	 * @param HttpServletRequest
	 * @return Object
	 */
	@RequestMapping("getTreeForGroup")
	@ResponseBody
	public Object getTreeForGroup(SID sid,HttpServletRequest request) {
			List<TreeModel> listModel = new ArrayList<TreeModel>();
			try {
				if(sid.hasAuditorRole()){
					TreeModel deviceModel = new TreeModel(LogKeyInfo.LOG_SYSTEM_TYPE,DeviceTypeNameUtil.getDeviceTypeName(LogKeyInfo.LOG_SYSTEM_TYPE));
					List<Map<String,Object>>  listGroup = IndexTemplateUtil.getInstance().getGroupByDeviceType(LogKeyInfo.LOG_SYSTEM_TYPE);
					for(Map<String,Object> map : listGroup){
						TreeModel model = new TreeModel();
						model.setText(map.get("name").toString());
						model.setAttributes(map);
						deviceModel.addChild(model);
					}
					listModel.add(deviceModel);
					return listModel;
				}
				if(dataSourceService == null){
					dataSourceService = (DataSourceService)FacadeUtil.getFacadeBean(request, null, "dataSourceService");
				}
				Map<String,String[]> group= sid.getGroupId();
				if(groupFilterEnable && !sid.isOperator() && ObjectUtils.isEmpty(group)){
					return null;
				}
				
				List<Map<String, Object>> list = dataSourceService.getDataSourceTreeWithNodeList(null);
				if (list != null && list.size() > 0) {
					List<Map<String, Object>> nodes = getDistinctNodesByNodeIp(list);
					if (nodes != null) {
						for (Map<String, Object> node : nodes) {
							List<String> deviceTypes = getDistinctDeviceTypeByNodeIpAndDataSourceType(list, node,sid,"0");
							if (deviceTypes != null && deviceTypes.size()>0) {
								if(group == null || !groupFilterEnable){
									TreeModel modelAll = new TreeModel();
									modelAll.setText("所有设备");
									Map<String,Object>  allType = new HashMap<String,Object>();
									allType.put("deviceType", SimDatasource.DATASOURCE_ALL);
									allType.put("nodeId", node.get("auditorNodeId"));
									List<Map<String,Object>>  listGroupAll = IndexTemplateUtil.getInstance().getGroupByDeviceType(SimDatasource.DATASOURCE_ALL);
									for(Map<String,Object> map : listGroupAll){
										TreeModel model = new TreeModel(SimDatasource.DATASOURCE_ALL+"/"+map.get("groupId"),(String) map.get("name"));
										model.setAttributes(map);
										modelAll.addChild(model);
									}
									modelAll.setAttributes(allType);
									listModel.add(modelAll);
								}
								for (String deviceType : deviceTypes) {
									if (LogKeyInfo.LOG_SYSTEM_TYPE.equals(deviceType))
										continue;
									TreeModel deviceModel = new TreeModel(deviceType,DeviceTypeNameUtil.getDeviceTypeName(deviceType));
									deviceModel.putAttribute("deviceType", deviceType);
									deviceModel.putAttribute("nodeId", node.get("auditorNodeId"));
									if((group != null && group.containsKey(deviceType)) || group == null){
										listModel.add(deviceModel);
										List<Map<String,Object>>  listGroup = IndexTemplateUtil.getInstance().getGroupByDeviceType(deviceType);
										if(ObjectUtils.isEmpty(listGroup)){
											Map<String,Object> baseGroup = IndexTemplateUtil.getInstance().getGroupByDeviceType(IndexTemplateUtil.defaultType).get(0);
											baseGroup.put("deviceType", deviceType) ;
											TreeModel model = new TreeModel("基本信息",baseGroup);
											deviceModel.addChild(model);
										}else{
											for(Map<String,Object> map : listGroup){
												if(!groupFilterEnable){//没有启用列集过滤功能
													TreeModel model = new TreeModel((String)map.get("name"),map);
													model.setId(deviceType+"/"+map.get("groupId")) ;
													deviceModel.addChild(model);
												}else{
													if(group != null && group.containsKey(deviceType)){
														for(String groupId : group.get(deviceType)){
															if(groupId.equals(map.get("groupId").toString())){
																TreeModel model = new TreeModel((String)map.get("name"),map);
																deviceModel.addChild(model);
															}
														}
													}else if(group == null && sid.isOperator()){
														TreeModel model = new TreeModel((String)map.get("name"),map);
														deviceModel.addChild(model);
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return listModel;
	}
	
	/**
	 * @method 获取日志源树
	 * @author Meteor
	 * @param HttpServletRequest
	 * @return Object
	 */
	@RequestMapping("getTree")
	@ResponseBody
	public Object getTree(SID sid,HttpServletRequest request) {
		List<Map<String, Object>> list = null;
		String flagMonitor = request.getParameter("flagMonitor");
		JSONArray nodeJsons = new JSONArray();
		Map<String,String[]> group= sid.getGroupId();
		if(groupFilterEnable){
		  if(!sid.isOperator() && !sid.isAuditor() && group == null)
			return null;
		}
		try {
			list = dataSourceService.getDataSourceTreeWithNodeList(null);
			if (list != null && list.size() > 0) {
				List<Map<String, Object>> nodes = getDistinctNodesByNodeIp(list);
				if (nodes != null) {
					for (Map<String, Object> node : nodes) {
						String flag = request.getParameter("flag");
						List<String> deviceTypes = getDistinctDeviceTypeByNodeIpAndDataSourceType(list, node,sid,flag);
						Locale locale = request.getLocale();
						if (deviceTypes != null && deviceTypes.size()>0) {
							JSONArray deviceTypeJsons = new JSONArray(); 
							
							if(group == null  && (sid.hasOperatorRole() && !"0".equals(flag)) || (!groupFilterEnable && !"0".equals(flag))){
								JSONObject children = new JSONObject();
								children.put("text", "所有设备");
								Map<String,String>  allType = new HashMap<String,String>();
								allType.put("deviceType", SimDatasource.DATASOURCE_ALL);
								allType.put("nodeId", node.get("auditorNodeId").toString());
								children.put("attributes", allType);
								children.put("enable", "true");
								children.put("iconCls", "icon-tree-leaf-all");
								deviceTypeJsons.add(children);
							}
							for (String deviceType : deviceTypes) {
								if (DataSourceUtil.isSysDataSource(deviceType) && "true".equals(flagMonitor))
									continue;
								JSONObject typeJson = new JSONObject();
								String deviceTypeZH = DeviceTypeNameUtil.getDeviceTypeName(deviceType, locale);
								typeJson.put("text", deviceTypeZH);
								Map<String,String>  deviceTypeMap = new HashMap<String,String>();
								deviceTypeMap.put("deviceType", deviceType);
								deviceTypeMap.put("nodeId", node.get("auditorNodeId").toString());
								typeJson.put("attributes", deviceTypeMap);
								List<Map<String, Object>> dataSources = getDataSourcesByNodeIpAndDataSourceType(list, node, deviceType);
								if (dataSources == null) {
									continue ;
								}
								JSONArray deviceTypeChildJsons = new JSONArray();
								
								BeanToPropertyValueTransformer trans = new BeanToPropertyValueTransformer("ip") ;
								Collection<String> userDeviceIPs = (Collection<String>) CollectionUtils.collect(sid.getUserDevice(),trans) ;
								for (Map<String, Object> dataSource : dataSources) {
									if(sid.isOperator() || sid.hasAuditorRole() ||(dataSource.get("dataSourceIp") !=null && userDeviceIPs.contains(dataSource.get("dataSourceIp")))){
										JSONObject typeChildJson = new JSONObject();
//										typeChildJson.put("text", deviceTypeZH);
										typeChildJson.put("text", dataSource.get("dataSourceName"));
										typeChildJson.put("id", dataSource.get("id")) ;
										Map<String,Object> attributes = new HashMap<String, Object>();
										attributes.put("host",dataSource.get("dataSourceIp"));
										attributes.put("nodeId", dataSource.get("auditorNodeId"));
										attributes.put("deviceType",dataSource.get("securityObjectType"));
//										attributes.put("enable", "true");
										Date date = NodeStatusQueueCache.getInstance().getLastUpdateDate((String)dataSource.get("auditorNodeId"));
										if (date == null) {
											attributes.put("state", "1");
										} else {
											long duration = GregorianCalendar.getInstance().getTimeInMillis() - date.getTime();
											if (duration > NodeStatusQueueCache.nodeTimeout) {
												attributes.put("state", "1");
											}
										}
										typeChildJson.put("attributes", attributes);
										typeChildJson.put("iconCls", "icon-tree-leaf-single");
										deviceTypeChildJsons.add(typeChildJson);
									}
									
								}
								typeJson.put("children", deviceTypeChildJsons);
								if (ObjectUtils.isNotEmpty(deviceTypeChildJsons)) {
									deviceTypeJsons.add(typeJson);
								}
							}
							if("0".equals(flag)){
								if(deviceTypeJsons.size()>0){
									JSONObject jsonObject=new JSONObject();
									jsonObject.put("text","日志源");
									jsonObject.put("children", deviceTypeJsons);
									JSONArray array=new JSONArray();
									array.add(jsonObject);
									return array;
								}
							}
							return deviceTypeJsons;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nodeJsons;
	}

	/**
	 * doLogSearch 获取日志查询结果
	 * @author zhou_xiaohu@topsec.com.cn
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("doLogSearch")
	@ResponseBody
	public Object doLogSearch(SID sid,@RequestBody LogSearchObject logSearchObject,HttpServletRequest request) {
		LogRecordList recordList = new LogRecordList();
		List<JSONObject> columnHeaders = new ArrayList<JSONObject>();
		String[] route = null;
		String userName = sid.getUserName();
		SearchObject searchObject = new SearchObject();
		try {
			if (StringUtil.isBlank(logSearchObject.getNodeId())) {
				Node node = nodeMgrFacade.getKernelAuditor(false);
				logSearchObject.setNodeId(node.getNodeId());
			}
			if (NodeStatusQueueCache.offline(logSearchObject.getNodeId())) {
				recordList.setExceptionInfo("日志源所属的审计节点不在线!");
				return recordList;
			}
			logSearchObject.fillSearchObject(searchObject) ;
			searchObject.setUserDevice(dataSourceService.getUserDataSourceAsString(sid,false));//设置查询权限
			// 创建定时任务并启动
			if (myTimeTask == null) {
				myTimeTask = new LogSearchTimerTask();
				new Thread(myTimeTask).start();
			}
			if (queryMap.containsKey(userName) && queryMap.get(userName) != null) {
				if (!searchObject.equals(queryMap.get(userName).getSearchObject())) {
					LogUtil lu = queryMap.get(userName);
					lu.getSearchObject().setCancel(true);
					stopSearch(lu);
					queryMap.remove(userName);
				} else {
					if (logSearchObject.isCancel()) {
						queryMap.remove(userName);
					} else {
						LogUtil util = queryMap.get(userName);
						route = util.getRoute();
						util.setCounts(util.getCounts() - 1);
					}
				}
			}

			if (logSearchObject.isCancel() != true && !queryMap.containsKey(userName)) {
				LogUtil logUtil = new LogUtil();
				logUtil.setNodeId(logSearchObject.getNodeId());
				logUtil.setSearchObject(searchObject);
				logUtil.setCounts(1);
				
				Node node = nodeMgrFacade.getNodeByNodeId(logSearchObject.getNodeId(), false, true, false, false);
				if (node == null) {
					recordList.setExceptionInfo("当前节点不存在!");
					return recordList;
				}
				route = NodeUtil.getRoute(node);
				logUtil.setRoute(route);
				// 如果有独立service节点，则修改节点路由
				Set<Node> children = node.getChildren();
				for (Node child : children) {
					String type = child.getType();
					if (NodeDefinition.NODE_TYPE_QUERYSERVICE.equals(type)) {
						logUtil.setNodeId(child.getNodeId());
						route = NodeUtil.getRoute(child);
						logUtil.setRoute(route);
						break;
					}
				}
				// 将用户信息及查询条件保存至map中
				queryMap.put(userName, logUtil);
			} else {
				Node node = nodeMgrFacade.getNodeByNodeId(logSearchObject.getNodeId(), false, true, false, false);
				if (node == null) {
					recordList.setExceptionInfo("当前节点不存在!");
					return recordList;
				} else {
					route = RouteUtils.getQueryServiceRoutes(node) ;
				}

			}

		} catch (Exception e1) {
			e1.printStackTrace();
			recordList.setExceptionInfo("查询请求失败，" + e1.getMessage());
			return recordList;
		}

		LogRecordSet resultSet = new LogRecordSet();
		try {
			resultSet = (LogRecordSet) NodeUtil.dispatchCommand(route, CMD, searchObject, 5 * 60 * 1000);
		} catch (Exception e) {
			if (e instanceof CommunicationExpirationException) {
				recordList.setExceptionInfo("数据请求超时!");
			} else {
				recordList.setExceptionInfo(e.getMessage());
				log.warn(e.getMessage());
			}
			recordList.setFinished(true);
			return recordList;
		}
		if (resultSet.getException() != null) {
			LogSearchException exception = (LogSearchException) resultSet.getException();
			if (exception.get_type() == 3) {
				recordList.setExceptionInfo("您选择的查询条件超出系统处理能力,\n请缩小时间范围和精确查询条件!");
			} else if (exception.get_type() == 1) {
				recordList.setExceptionInfo("您指定的时间范围内没有日志!");
			} else if (exception.get_type() == 2) {
				recordList.setExceptionInfo("系统忙,请稍候查询!");
			} else if (exception.get_type() == 4) {
				recordList.setExceptionInfo("正在激活索引,请稍候查询!");
			} else if (exception.get_type() == 7) {
				recordList.setExceptionInfo("正在更新缓存,请稍候查询!");
			} else if (exception.get_type() == 8) {
				recordList.setExceptionInfo("存储空间不足,无法查询!");
			} else {
				recordList.setExceptionInfo("没有符合查询条件的记录!");
			}
			recordList.setType(exception.get_type());
			recordList.setFinished(true);
			queryMap.remove(userName);
			return recordList;
		}
		if (resultSet.isFinished()) {
			queryMap.remove(userName);
			if (resultSet.getRecords().size() == 0) {
				recordList.setType(1);
				recordList.setExceptionInfo("没有符合查询条件的记录!");
				recordList.setFinished(true);
				//return recordList;
			}
		}
		GroupCollection collection = IndexTemplate.getTemplate(logSearchObject.getDeviceType()).getGroup(logSearchObject.getGroup()) ;
		UserService userService = (UserService) SpringWebUtil.getBean("userService", request) ;
		String module = "/sim/log/logQuery/" + logSearchObject.getDeviceType() + "/" + logSearchObject.getGroup() ;
		Map<String,JSONObject> columnConfigMap = userService.getColumnConfigMap(new ColumnConfigId(sid.getAccountID(), module)) ;
		for(LogField field:collection.getVisibleFields()){
			JSONObject fieldJSON = FastJsonUtil.toJSON(field, "alias=headerText","hidden","name=dataField","type") ;
			JSONObject userConfig = columnConfigMap.get(field.getName()) ;
			if(userConfig != null){
				fieldJSON.put("width", Math.max(userConfig.getIntValue("width"),70)) ;
				fieldJSON.put("hidden", userConfig.getBoolean("hidden")) ;
			}
			columnHeaders.add(fieldJSON);
		}
		formatRecords(resultSet.getMaps(),collection);
		// 库中总日志条数
		recordList.setTotalLogs(resultSet.getTotalLogs());
		//格式化之后的日志总条数
		DecimalFormat decimalFormat=new DecimalFormat();
		recordList.setTotalCount(decimalFormat.format(resultSet.getTotalLogs()));
		// 查询结果命中总数
		recordList.setTotalRecords(resultSet.getTotalRecords());
		// 可显示数
		recordList.setDisplayCount(Math.min(resultSet.getTotalRecords(),resultSet.getDisplayLimit()));
		// 超过查询结果上限
		recordList.setReachLimit(resultSet.isReachLimit());
        recordList.setSearchLimit(resultSet.getSearchLimit());
        recordList.setDisplayLimit(resultSet.getDisplayLimit()) ;
		recordList.setColumns(columnHeaders) ;
		recordList.setMaps(resultSet.getMaps());
		recordList.setFinished(resultSet.isFinished());
		recordList.setLapTime((int)resultSet.getLapTime());
		recordList.setLogType(logSearchObject.getDeviceType());
		recordList.setGroup(logSearchObject.getGroup());
		//日志源列表
		if(dataSourceService == null){
			dataSourceService = (DataSourceService)FacadeUtil.getFacadeBean(request, null, "dataSourceService");
		}
		List<SimDatasource> list = dataSourceService.getDataSourceByDvcType(logSearchObject.getDeviceType());
		List<Map<String, Object>> dataSource = new ArrayList<Map<String,Object>>();
		BeanToPropertyValueTransformer trans = new BeanToPropertyValueTransformer("ip") ;
		Collection<String> userDeviceIPs = (Collection<String>) CollectionUtils.collect(sid.getUserDevice(),trans) ;
		for(SimDatasource data : list){
			Device device = AssetFacade.getInstance().getAssetByIp(data.getDeviceIp()) ;
			if(device != null && (sid.isOperator() || sid.hasAuditorRole() || userDeviceIPs.contains(data.getDeviceIp()))){
				Map<String,Object> map = new HashMap<String, Object>();
				map.put("ip", data.getDeviceIp());
				if(DataSourceUtil.isSysDataSource(data.getSecurityObjectType())){
					map.put("name",data.getDeviceIp());
				}else{
					map.put("name",device.getName());
				}
				map.put("nodeId", data.getAuditorNodeId());
				boolean selected = data.getAuditorNodeId().equals(logSearchObject.getNodeId()) && 
						           data.getDeviceIp().equals(logSearchObject.getHost()) ; 
				map.put("selected",selected) ;
				if(!dataSource.contains(map)){
					dataSource.add(map);
				}
			}
		}
		recordList.setSeq(logSearchObject.getSeq()) ;
		recordList.setDataSource(dataSource);
		recordList.setFilters((List)FastJsonUtil.toJSONArray(collection.getSearchableFields(), "name","alias","values","type","operators"));
		//雷达图数据组装
		recordList.setTimeline(LogStatsUtils.formatLogChart(resultSet.getStats(),searchObject.getStart(),searchObject.getEnd()));
		return recordList;
	}

	/**
	 * 对返回对象做处理， Date对象就转成yyyy-MM-dd HH:mm:ss格式的字符串，
	 * 其余的对象都调用toString()转成字符串，PRIORITY的值转成相应的级别
	 * 
	 * @标题:
	 * @作者:chenpx
	 * @参数:
	 * @返回值:void
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void formatRecords(List<Map<String,Object>> formatResult,GroupCollection collection) {
		if(ObjectUtils.isEmpty(formatResult)){
			return ;
		}
		List<Map<String,Object>> copy = new ArrayList<Map<String,Object>>(formatResult.size()) ;
		for(Map record:formatResult){
			copy.add(new HashMap<String,Object>(record)) ;
		}
		FormatterFactory formatterFactory = (FormatterFactory) SpringContextServlet.springCtx.getBean("formatterFactory") ;
		Ip2NameFormatter ip2NameFormatter = (Ip2NameFormatter) formatterFactory.getFormatter("Ip2NameFormatter") ;
		for(LogField field:collection.getFields()){
			FieldFormatter formatter = formatterFactory.getFormatter(field.getFormatter()) ;
			String fieldName = field.getName() ;
			boolean isIpField = field.getType().equals("ip") ;
			for(int i=0;i<copy.size();i++){
				Map<String,Object> copyRecord = copy.get(i);
				Object fieldValue = copyRecord.get(fieldName);
				if(formatter != null){
					Map<String,Object> record = formatResult.get(i) ;
					record.put(fieldName, formatter.format(fieldValue, copyRecord)) ;
				}
				if(isIpField && fieldValue instanceof IpAddress){
					Map<String,Object> record = formatResult.get(i) ;
					record.put(fieldName+"$ASSET_NAME", ip2NameFormatter.format(fieldValue, copyRecord)) ;
				}
			}
		}
	}
	/**
	 * @method stopSearch()发送命令，用于停止日志查询
	 * @param HistoryLogUtil
	 * @author 周小虎
	 * @date 2012-03-05
	 */
	public static void stopSearch(LogUtil logUtil){
		try {
			String[] route = null;
			route = logUtil.getRoute();
		    NodeUtil.getCommandDispatcher().dispatchCommand(route, CMD, logUtil.getSearchObject(), 30 * 1000);
		} catch (Exception e) {
			log.warn(e.getMessage());
		}
		
	}
	private List<Map<String, Object>> getDistinctNodesByNodeIp(List<Map<String, Object>> list) {
		List<Map<String, Object>> nodes = new ArrayList<Map<String, Object>>();
		List<String> ips = new ArrayList<String>();
		if (list != null) {
			for (Map<String, Object> o : list) {
				if (!ips.contains((String) o.get("nodeIp"))) {
					ips.add((String) o.get("nodeIp"));
					nodes.add(o);
				}
			}
		}
		return nodes;
	}

	private List<String> getDistinctDeviceTypeByNodeIpAndDataSourceType(
			List<Map<String, Object>> list, Map<String, Object> object,SID sid,String flag) {
		List<String> keys = new ArrayList<String>();
		BeanToPropertyValueTransformer trans = new BeanToPropertyValueTransformer("ip") ;
		Collection<String> userDeviceIPs = (Collection<String>) CollectionUtils.collect(sid.getUserDevice(),trans) ;
		if(sid.hasAuditorRole()){
			keys.add(0, LogKeyInfo.LOG_SYSTEM_TYPE);
			return keys;
		}
		for (Map<String, Object> o : list) {
			if(!"1".equals(flag) && !sid.isOperator()){
				if(!userDeviceIPs.contains(o.get("dataSourceIp").toString())){
					continue;
				}
			}
			if (o.get("nodeIp").equals(object.get("nodeIp")) && !keys.contains(o.get("securityObjectType"))) {
				if (((String) o.get("securityObjectType")).equals(LogKeyInfo.LOG_SYSTEM_RUN_TYPE)) {
					keys.add(0, (String) o.get("securityObjectType"));
				} else if(!(o.get("securityObjectType")).equals(LogKeyInfo.LOG_SYSTEM_TYPE)) {
					keys.add((String) o.get("securityObjectType"));
				}
			}
		}
		return keys;
	}

	private List<Map<String, Object>> getDataSourcesByNodeIpAndDataSourceType(
			List<Map<String, Object>> list, Map<String, Object> object,
			String deviceType) {
		List<Map<String, Object>> dataSources = new ArrayList<Map<String, Object>>();
		if (list != null) {
			for (Map<String, Object> o : list) {
				if (((String) o.get("nodeIp")).equals(object.get("nodeIp"))
						&& ((String) o.get("securityObjectType"))
								.equals(deviceType)) {
					dataSources.add(o);
				}
			}
		}
		return dataSources;
	}

	/**
	 * @method getLogsAmount()获取各种日志类型的数量统计
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping("getLogsAmount")
	@ResponseBody
	public Object getLogsAmount(@RequestParam(value = "nodeId",defaultValue ="") String auditorId,SID sid)throws Exception {
		LogCountStatTask lcst = (LogCountStatTask) SpringContextServlet.springCtx.getBean("logCountStatTask") ;
		List<Map<String,Object>> deviceTypeAndIp = lcst.getDeivceTypeLogCount() ; 
		if(deviceTypeAndIp == null){
			return null ;
		}
		BeanToPropertyValueTransformer trans = new BeanToPropertyValueTransformer("ip") ;
		 Collection<String> userDeviceIPs = (Collection<String>) CollectionUtils.collect(sid.getUserDevice(),trans) ;
		JSONArray logCountJson = new JSONArray();
		List<String> categories = new ArrayList<String>() ;
		for(Map<String,Object> item:deviceTypeAndIp){
			String cnType = DeviceTypeNameUtil.getDeviceTypeName((String) item.get("type")) ;
			if(sid.hasAuditorRole()&&!cnType.equals(DeviceTypeNameUtil.getDeviceTypeName("Esm/Topsec/SystemLog"))){
				continue;
			}else if(sid.isOperator()&&cnType.equals(DeviceTypeNameUtil.getDeviceTypeName("Esm/Topsec/SystemLog"))){
				continue;
			}else if((sid.hasOperatorRole()&&!sid.isOperator())
					&&(cnType.equals(DeviceTypeNameUtil.getDeviceTypeName("Esm/Topsec/SystemLog"))
							||cnType.equals(DeviceTypeNameUtil.getDeviceTypeName("Esm/Topsec/SystemRunLog"))
							||!userDeviceIPs.contains(item.get("ip")))){
				continue;
			}
			item.put("type", cnType) ;
			if(!categories.contains(cnType)){
				categories.add(cnType) ;
			}
		}
		long[] countDatas = new long[categories.size()] ;
		ChainMap<String,Object> countMap = new ChainMap<String,Object>();
		ChainMap<String, List> countDataSources = new ChainMap<String, List>() ;
		countMap.push("name","数量").push("data", countDatas).put("dataSources", countDataSources);
		long[] sizeDatas = new long[categories.size()] ;
		ChainMap<String,Object> sizeMap = new ChainMap<String,Object>();
		ChainMap<String, List> sizeDataSources = new ChainMap<String, List>() ;
		sizeMap.push("name","大小").push("data", sizeDatas).put("dataSources", sizeDataSources) ;
		long countTotal = 0;
		long sizeTotal = 0;
		for(Map<String,Object> item:deviceTypeAndIp){
			if(!sid.isOperator()&&sid.hasOperatorRole()&&!userDeviceIPs.contains(item.get("ip"))){
				continue;
			}
			String type = (String)item.get("type") ;
			int catIndex = categories.indexOf(type) ;
			if(catIndex > -1){
				long[] countData = (long[]) countMap.get("data");
				long[] sizeData = (long[]) sizeMap.get("data") ;
				Number count = (Number) item.get("counts") ;
				Number size = (Number) item.get("size") ;
				countData[catIndex] = countData[catIndex] + count.longValue() ;
				sizeData[catIndex] = sizeData[catIndex] + size.longValue() ;
				countTotal = countTotal + count.longValue();
				sizeTotal = sizeTotal + size.longValue();
				countDataSources.getOrNew(type, ArrayList.class).add(item) ;
				sizeDataSources.getOrNew(type, ArrayList.class).add(item) ;
				JSONObject json = new JSONObject();
				json.put("name",type);
				json.put("ip",item.get("ip"));
				json.put("value", count);
				json.put("logSize", size);
				logCountJson.add(json);
			}
		}
		Map<String,Object> result = new HashMap<String,Object>(4,1.0F) ;
		result.put("categories", categories);
		result.put("countData", countMap) ;
		result.put("sizeData", sizeMap) ;
		result.put("logCountJson", logCountJson);
		result.put("sizeTotal", sizeTotal) ;
		result.put("countTotal", countTotal) ;
		return result ;
	}
	
	/**
	 * downloadFile下载
	 * @param request、response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("downloadFile")
	@ResponseBody
	 public Object downloadFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
			String filename = request.getParameter("filename");
            CommonUtils.setDownloadHeaders(request, response, filename) ;
	        String serverHome = System.getProperty("jboss.server.home.dir");
	        String savaLogPathfile = new StringBuilder(serverHome).append(File.separator).append("ftphome").append(File.separator).append("log").append(File.separator).append(filename).toString();
	        File file = new File(savaLogPathfile);
	        InputStream bis = null;
	        OutputStream bos = null;
	        try {
	            IOUtils.copy((bis = new FileInputStream(file)), (bos = response.getOutputStream())) ;
	        }catch (Exception e) {
	            log.error("下载文件出错....", e);
	        } finally {
	        	ObjectUtils.close(bis) ;
	        	ObjectUtils.close(bos) ;
	        }
	      
	        return null;
	    }

	/**
	 * exportCurPageLogs 到出当前页日志
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("exportCurPageLogs")
	@ResponseBody
	public Object exportCurPageLogs(SID sid,@RequestBody LogSearchObject logSearchObject)throws Exception {
		logSearchObject.setPageNo(Math.max(logSearchObject.getPageNo(), 1)) ;
		logSearchObject.setPageSize(Math.max(logSearchObject.getPageSize(), 10)) ;
		return doExportLog(sid, logSearchObject) ;
	}
	/**
	 * 导出所有日志
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("exportLogs")
	@ResponseBody
	public Object exportLogs(SID sid,@RequestBody LogSearchObject logSearchObject) throws Exception{
		logSearchObject.setPageNo(-1) ;
		logSearchObject.setPageSize(-1) ;
		return doExportLog(sid, logSearchObject) ;
	}
	
	private JSONObject doExportLog(SID sid,LogSearchObject logSearchObject){
		JSONObject json=new JSONObject();
		try {
			List<Node> nodes = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_QUERYSERVICE, false, false, false, false) ;
			if(ObjectUtils.isEmpty(nodes)){
				json.put("exception", "当前节点不存在!");
			}else if(NodeStatusQueueCache.offline(nodes.get(0).getNodeId())){
				json.put("exception", "查询节点掉线！");
			}else{
				String[] route = RouteUtils.getQueryServiceRoutes() ;
				LogExportObject logExportObject = new LogExportObject();
				logSearchObject.fillSearchObject(logExportObject) ;
				logExportObject.setSFromat(logSearchObject.getIsFormate()==1||logSearchObject.getIsFormate()==0);
				logExportObject.setSplitDate(false);
				logExportObject.setZipFile(StringUtil.currentDateToString("yyyyMMdd_HHmmss"));
				logExportObject.setUserDevice(dataSourceService.getUserDataSourceAsString(sid,false));
				//FTP 信息
				setFtpInfo(logExportObject) ;
				try {
                    LogExportResult logExportResult = (LogExportResult)NodeUtil.getCommandDispatcher().dispatchCommand(route, MessageDefinition.CMD_SEARCH_LOG_EXPORTALL,  logExportObject, 15*60*1000);
                    json.put("ftpfilepath", logExportResult.getFtpPathFile());
				} catch (Exception e) {
                    e.printStackTrace();
                    json.put("exception", "发送请求处理错误:" + e.getMessage());
                }

			}
			
		} catch (Exception e) {
			e.printStackTrace();
			json.put("exception", "发送请求处理错误:" + e.getMessage());
		}
		return json;
	}
	
	private static void setFtpInfo(LogExportObject logExportObject){
		Map<String,Object> map=FtpConfigUtil.getInstance().getFTPConfigByKey("log");
		logExportObject.setFtpHost((String)map.get("host"));
		logExportObject.setFtpPort((String)map.get("port"));
		logExportObject.setFtpUser((String)map.get("user"));
		logExportObject.setFtpPassword((String)map.get("password"));
		logExportObject.setFtpEncoding((String)map.get("encoding"));
	}
	
	/**
	 * 日志趋势图
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("getLogChart")
	@ResponseBody
	public Object getLogChart(SID sid,HttpServletRequest request) throws Exception {
		String date = request.getParameter("date");
		String host = request.getParameter("host");
		String deviceType = request.getParameter("deviceType");
		String nodeId  = request.getParameter("nodeId");
		if (StringUtil.isBlank(nodeId)) {
			nodeId = nodeMgrFacade.getKernelAuditor(false).getNodeId() ;
		}
		Date startDate ;
		Date endDate ;
		if(date.length() == 4){
			startDate = StringUtil.toDate(date, "yyyy") ;
			endDate = ObjectUtils.addSeconds(ObjectUtils.addYears(startDate, 1),-1) ;//年末12.31 23:59:59
		}else if(date.length()==7){
			startDate = StringUtil.toDate(date,"yyyy-MM") ;
			endDate = ObjectUtils.addSeconds(ObjectUtils.addMonth(startDate),-1) ;//月末23:59:59
		}else{
			startDate = StringUtil.toDate(date+" 00:00:00","yyyy-MM-dd HH:mm:ss") ;
			endDate = StringUtil.toDate(date+" 23:59:59","yyyy-MM-dd HH:mm:ss");
		}
		String[] route= null;
		JSONArray partitions = new JSONArray();
		JSONObject logCountInfo=new JSONObject();//存放日志概要信息和日志摘要信息
		// map
		SearchObject searchObject = new SearchObject();
		searchObject.setStart(startDate);
		searchObject.setEnd(endDate);
		searchObject.setHost(host);
		searchObject.setType(deviceType);
		if(sid.hasAuditorRole()){
			searchObject.setAuditor(true);
			searchObject.setType(LogKeyInfo.LOG_SYSTEM_TYPE);
		}
		try {
			Node node=null;
			node = nodeMgrFacade.getNodeByNodeId(nodeId,false,true,false,false);
			if(node != null){
				route=NodeUtil.getRoute(node);
			}
			//如果有独立service节点，则修改节点路由
			Set<Node> children = node.getChildren();
			for(Node child:children){
				String type = child.getType();
				if(NodeDefinition.NODE_TYPE_QUERYSERVICE.equals(type)){
					route = NodeUtil.getRoute(child);
					break;
				}
			}
			LogRecordSet recordSet = (LogRecordSet)NodeUtil.getCommandDispatcher().dispatchCommand(route, MessageDefinition.CMD_SEARCH_LOGCOUNT_PARTITION, searchObject, 2*60*1000);
			//趋势图概要统计
			LogCountSet logCountSet = (LogCountSet)NodeUtil.getCommandDispatcher().dispatchCommand(route, MessageDefinition.CMD_SEARCH_LOGCOUNT_TOTAL, searchObject,2*60*1000);
			List<Object[]> logCountList =logCountSet.get_result();
			 JSONArray logCountJson = new JSONArray();
			 DecimalFormat decimalFormat=new DecimalFormat();
			 BeanToPropertyValueTransformer trans = new BeanToPropertyValueTransformer("ip") ;
			 Collection<String> userDeviceIPs = (Collection<String>) CollectionUtils.collect(sid.getUserDevice(),trans) ;
			 boolean flag=false;
			 if(logCountList !=null && logCountList.size()>0){
				for(Object[] logCount : logCountList){
					JSONObject json = new JSONObject();
					if(logCount[1]!=null){
						if(sid.hasOperatorRole() && LogKeyInfo.LOG_SYSTEM_TYPE.equals((String) logCount[1]))
							flag=false;
						else if(sid.hasOperatorRole() && !sid.isOperator() && LogKeyInfo.LOG_SYSTEM_RUN_TYPE.equals((String)logCount[1]))
							flag=false;
						else if(sid.hasOperatorRole() && !sid.isOperator() && userDeviceIPs.contains(logCount[1]))
							flag=true;
						else if(sid.hasOperatorRole() && sid.isOperator()){
							flag=true;
						}else if(sid.hasAuditorRole()){
							flag=true;
						}
						else if(deviceType.equals(SimDatasource.DATASOURCE_ALL))
							flag=true;
						if(flag){
							JSONArray childrenArray = new JSONArray();
							String deviceTypeZH=DeviceTypeNameUtil.getDeviceTypeName((String) logCount[1],Locale.getDefault());
							json.put("deviceTypeName",deviceTypeZH);
							json.put("logCount", (Long)logCount[0]);
							json.put("formatcount", decimalFormat.format((Long)logCount[0]));
							json.put("logSize",  Sigar.formatSize((Long)logCount[2]+(Long)logCount[3]));
							json.put("logSize2",  (Long)logCount[2]+(Long)logCount[3]);
							json.put("children",childrenArray);
							logCountJson.add(json);
							flag=false;
							searchObject.setType((String) logCount[1]);
							searchObject.setHost("");
								LogCountSet logCountSet2 = (LogCountSet)NodeUtil.getCommandDispatcher().dispatchCommand(route, MessageDefinition.CMD_SEARCH_LOGCOUNT_TOTAL, searchObject,2*60*1000);
								List<Object[]> logCountList2 =logCountSet2.get_result();
								if(logCountList2 !=null && logCountList2.size()>0){
									for(Object[] logCount2 : logCountList2){
										JSONObject json2 = new JSONObject();
										if(logCount2[1]!=null){
											String deviceTypeZH2=DeviceTypeNameUtil.getDeviceTypeName((String) logCount2[1],Locale.getDefault());
											json2.put("deviceTypeName",deviceTypeZH2);
											json2.put("logCount", (Long)logCount2[0]);
											json2.put("formatcount", decimalFormat.format((Long)logCount2[0]));
											json2.put("logSize",  Sigar.formatSize((Long)logCount2[2]+(Long)logCount2[3]));
											json2.put("logSize2",  (Long)logCount2[2]+(Long)logCount2[3]);
											childrenArray.add(json2);
										}
									}
							}
						}
						
					}
				}
				logCountInfo.put("logCountJson",logCountJson);
			}
			List<Map<String,Object>> resultList = recordSet.getMaps();
			if(date.length() ==4){
				for(int i=1;i<13;i++){
					JSONObject detaiPartition = new JSONObject();
					String month = i< 10 ? "0"+i : String.valueOf(i) ;
					long logCount =0;
					long logSize =0;
					long indexSize=0;
					for(Map result: resultList){
						if(result.get("month").equals(i)){
							logCount += (Long)result.get("count");
							logSize += (Long)result.get("logsize");
							indexSize += (Long)result.get("indexsize");
						}
					}
					detaiPartition.put("name",month+"月");
					detaiPartition.put("count",logCount);
					detaiPartition.put("logSize",logSize+indexSize);
					detaiPartition.put("formatLogSize",Sigar.formatSize(logSize+indexSize));
					partitions.add(detaiPartition);
				}
			}else if(date.length()==7){
				Calendar c = Calendar.getInstance();
				 //设置为该月，例如11年1月，日期随意
//				GregorianCalendar gregorianCalendar =  new GregorianCalendar();
//				gregorianCalendar.get(Calendar.YEAR);
		        //获得该月的日期
				c.set(Integer.parseInt(date.substring(0,4)),Integer.parseInt(date.substring(date.indexOf("-")+1))-1,1);
				int maxDate = c.getActualMaximum(Calendar.DAY_OF_MONTH);
				for(int i=1;i<= maxDate;i++){
					long logCount =0;
					long logSize =0;
					long indexSize=0;
					String day = i< 10 ? ("0"+i+"日") : String.valueOf(i)+"日";
					JSONObject detail = new JSONObject();
					for(Map result: resultList){	
						if(result.get("day").equals(i)){
							logCount += (Long)result.get("count");
							logSize += (Long)result.get("logsize");
							indexSize += (Long)result.get("indexsize");
						}
					}
					detail.put("name",day);
					detail.put("count",logCount);
					detail.put("logSize",logSize+indexSize);
					detail.put("formatLogSize",Sigar.formatSize(logSize+indexSize));
					partitions.add(detail);
				}
			}
			else{
				for(int i=0;i< 24;i++){
					String hour = i<10 ?("0"+i+"时"):(i+"时");
					JSONObject detail = new JSONObject();
					for(Map result: resultList){	
						if(result.get("hour").equals(i)){
							detail.put("name",hour);
							detail.put("count",(Long)result.get("count"));
							detail.put("logSize",(Long)result.get("logsize")+(Long)result.get("indexsize"));
							detail.put("formatLogSize",Sigar.formatSize((Long)result.get("logsize")+(Long)result.get("indexsize")));
							break;
						}
					}
					if(detail.get("name") == null){
						detail.put("name",hour);
						detail.put("count",0);
						detail.put("logSize",0);
						detail.put("indexSize",0);
						detail.put("formatLogSize",0);
					}
					partitions.add(detail);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		logCountInfo.put("partitions",partitions);
		return logCountInfo;
	}
	
	@RequestMapping(value="exportExcelLogData", produces="text/html;charset=utf-8")
	public void exportExcelLogData(String auditorId,SID sid,HttpServletRequest request,HttpServletResponse response){
		try {
			response.setContentType("application/vnd.ms-excel");
			String fileName=URLEncoder.encode("", "UTF-8") + "日志数量大小统计" +StringUtil.currentDateToString("yyyy-MM-dd")+"-"+(new Date()).getTime()+".xlsx";
			CommonUtils.setDownloadHeaders(request, response, fileName) ;
			XSSFWorkbook  workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("日志数量大小统计");
			XSSFCellStyle cellStyle=workbook.createCellStyle();
			cellStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
			cellStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
			sheet.addMergedRegion(new CellRangeAddress(0,1,0,2));
			XSSFCell cell = sheet.createRow(0).createCell(0);
			XSSFFont  fontStyle=workbook.createFont();
			fontStyle.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
			cellStyle.setFont(fontStyle);
			cell.setCellValue("日志数量大小统计");
			cell.setCellStyle(cellStyle);
			XSSFCellStyle cellStyle2 = workbook.createCellStyle();
			cellStyle2.setFont(fontStyle);
		    XSSFRow row = sheet.createRow(2);    
		    XSSFCell cell0 = row.createCell(0);
		    XSSFCell cell1 = row.createCell(1);   
		    XSSFCell cell2 = row.createCell(2); 
		    cell0.setCellStyle(cellStyle2);
		    cell1.setCellStyle(cellStyle2);
		    cell2.setCellStyle(cellStyle2);
		    sheet.setColumnWidth(0, 5000);
			sheet.setColumnWidth(1, 5000);
			sheet.setColumnWidth(2, 5000);
	        cell0.setCellValue("名称");    
	        cell1.setCellValue("大小");
	        cell2.setCellValue("数量");
			Map<String,Object> result = (Map<String, Object>) getLogsAmount(auditorId,sid);
			if(result!=null){
				List<String> categories = (List<String>) result.get("categories");
				ChainMap<String,Object> countMap = (ChainMap<String, Object>) result.get("countData");
				long[] countData = (long[]) countMap.get("data");
				ChainMap<String,Object> sizeMap = (ChainMap<String, Object>) result.get("sizeData");
				long[] sizeData = (long[]) sizeMap.get("data");
				JSONArray logCountJson = (JSONArray) result.get("logCountJson");
				long countTotal = (Long) result.get("countTotal");
				long sizeTotal = (Long) result.get("sizeTotal");
				int rowsNum = 3;
				for(int i = 0;i < categories.size();i++){
					XSSFRow rows = sheet.createRow(rowsNum);
					rowsNum++;
					cell0 = rows.createCell(0);
					cell1 = rows.createCell(1);
					cell2 = rows.createCell(2);
					cell0.setCellValue(categories.get(i));
					cell1.setCellValue(CommonUtils.formatBytes(sizeData[i],1));
					cell2.setCellValue(CommonUtils.formatCount(countData[i],1));
					for(int j = 0;j < logCountJson.size();j++){
						if(categories.get(i).equals((String)logCountJson.getJSONObject(j).get("name"))){
							rows = sheet.createRow(rowsNum);
							rowsNum++;
							cell0 = rows.createCell(0);
							cell1 = rows.createCell(1);
							cell2 = rows.createCell(2);	
							cell0.setCellValue("    "+logCountJson.getJSONObject(j).get("ip")); 
						    cell1.setCellValue(CommonUtils.formatBytes((Long)logCountJson.getJSONObject(j).get("logSize"),1));
						    cell2.setCellValue(CommonUtils.formatCount((Long)logCountJson.getJSONObject(j).get("value"),1));
						}
					}
				}
				XSSFRow rows = sheet.createRow(rowsNum);
				rowsNum++;
				cell0 = rows.createCell(0);
				cell1 = rows.createCell(1);
				cell2 = rows.createCell(2);	
				cell0.setCellValue("总计");    
			    cell1.setCellValue(CommonUtils.formatBytes(sizeTotal,1));
			    cell2.setCellValue(CommonUtils.formatCount(countTotal,1));
			}
	        workbook.write(response.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	@RequestMapping("getLogSearchFieldset")
	public String getLogSearchFieldset(String deviceType,String host,String nodeId,String maxDate,String minDate,HttpServletRequest request){
		Map<String,Object> deviceTypeMap = IndexTemplateUtil.getInstance().getTemplateByDeviceType(deviceType);
		request.setAttribute("fieldset", deviceTypeMap.get("fieldList"));
		request.setAttribute("groups", deviceTypeMap.get("groupList"));
		request.setAttribute("deviceType", deviceType);
		request.setAttribute("host", host);
		request.setAttribute("nodeId", nodeId);
		request.setAttribute("minDate", minDate);
		request.setAttribute("maxDate", maxDate);
		return "page/log/log_search";
	}
	
	/**
	 * @method queryTodayLogCount()获取当天总日志数（自审计日志除外）
	 */
	@RequestMapping("queryTodayLogCount")
	@ResponseBody
	public Object queryTodayLogCount(HttpServletRequest request){
		Result result = new Result() ;
		try {
			String[] routes = RouteUtils.getQueryServiceRoutes() ;
			LogCountSet logCountSet = (LogCountSet)NodeUtil.dispatchCommand(routes, MessageDefinition.CMD_SEARCH_LOGCOUNT_TOTAL_CURDAY,  null, 30*1000);
			List<Object[]> logCountList = logCountSet.get_result();
			int counts = 0;
			if(ObjectUtils.isNotEmpty(logCountList)){
				for(Object[] logCount : logCountList){
					counts+= (Long)logCount[0];
				}
			}
			return result.buildSuccess(counts);
		}catch(CommunicationException e){
			log.warn("查询当天日志数量超时") ;
			return result.buildError("查询当天日志数量超时") ;
		} catch (Exception e) {
			log.error("查询当天日志数量出错",e) ;
			return result.buildError("获取日志信息失败！") ;
		}
	}

	//导出pdf文件
	public void exportPDF(HttpServletResponse response,String title,String pieImg,String columnImg){
		   try {
			   BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
				Document document = new Document();
				document.setPageSize(PageSize.A4);
				OutputStream ouputStream =response.getOutputStream();
				PdfWriter.getInstance(document, ouputStream);
				
				// 打开文件
				document.open();
				Font headFont = new Font(bfChinese, 20, Font.BOLD);// 设置字体大小
				document.add(new Paragraph(title,headFont));
				//创建表格
				PdfPTable table = new PdfPTable(1);
				table.getRowHeight(20);
				//table.setBorderWidth(0);
				//table.setWidthPercentage(100);
				table.setSpacingBefore(20f);// 设置表格上面空白宽度
//				table.setTotalWidth(535);// 设置表格的宽度
//				table.setLockedWidth(true);// 设置表格的宽度固定
			    table.getDefaultCell().setBorder(0);//设置表格默认为边框1
				PdfPTable titleTable = new PdfPTable(1);
//				Paragraph mess = new Paragraph(title, new com.lowagie.text.Font(bfChinese,11));  
				
				
//				PdfPCell cell = new PdfPCell(new Paragraph("Taony125 testPdf 中文字体",new com.lowagie.text.Font(bfChinese,11)));// 建立一个单元格
//			    cell.setBorder(0);//设置单元格无边框
				//cell.setColspan(7);// 设置合并单元格的列数

//				table.addCell(cell);// 增加单元格
				table.addCell(titleTable);
				//饼图
				PdfPTable pieImageTable=new PdfPTable(1);
				pieImageTable.getDefaultCell().setBorder(0);
				Image pieImage = Image.getInstance(pieImg);
				pieImageTable.addCell(pieImage);
				pieImageTable.setWidthPercentage(80);
				table.addCell(pieImageTable);
				//柱状图
				PdfPTable columnImageTable=new PdfPTable(1);
				columnImageTable.getDefaultCell().setBorder(0);
				Image columnImage = Image.getInstance(columnImg);
				columnImageTable.addCell(columnImage);
				columnImageTable.setWidthPercentage(80);
				table.addCell(columnImageTable);
				
				//表格
				PdfPTable tableData=new PdfPTable(1);
				//tableData.getDefaultCell().setBorder(0);
				tableData.addCell(columnImage);
				tableData.setWidthPercentage(80);
				table.addCell(tableData);
				
				
				
				document.add(table);
				document.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//日志/日志摘要/导出word文档
	@RequestMapping("exportQueryResultWord")
	public void exportQueryResultWord(SID sid,HttpServletRequest request,HttpServletResponse response){
		try {
			response.setContentType("application/msword");
			String fileName=URLEncoder.encode("", "UTF-8") + StringUtil.currentDateToString("yyyy-MM-dd")+"-"+(new Date()).getTime()+".docx";
			CommonUtils.setDownloadHeaders(request,response,fileName) ;
			String date =request.getParameter("date");
			String selectNodeText = StringUtil.recode(request.getParameter("selectNodeText"));
			selectNodeText = selectNodeText.equals("所有设备") ? "" : selectNodeText;
			JSONObject logCountInfo = (JSONObject) getLogChart(sid,request);
			JSONArray logCountJson = ObjectUtils.nvl(logCountInfo.getJSONArray("logCountJson"),new JSONArray());
			JSONArray partitions = ObjectUtils.nvl(logCountInfo.getJSONArray("partitions"),new JSONArray());
			SimXWPFDocument doc = new SimXWPFDocument();
			XWPFParagraph para = doc.createParagraph();
			formatTitleOfWord(para,selectNodeText+"日志概要统计列表"+date);
			XWPFTable deviceTypeTable = doc.createTable(logCountJson.size()+1,3);
			XWPFUtil.setCellWidths(deviceTypeTable, 3000) ;
			XWPFTableRow deviceTypeRow = deviceTypeTable.getRow(0) ;
			deviceTypeRow.getCell(0).setText("类型") ;
			deviceTypeRow.getCell(1).setText("日志数量") ;
			deviceTypeRow.getCell(2).setText("日志大小") ;
			//日志概要统计图
			XWPFParagraph chartPara = doc.createParagraph();
			formatTitleOfWord(chartPara,selectNodeText+"日志概要数量统计图"+date);
			List<List<Object>> deviceTypeChartData = new ArrayList<List<Object>>() ;
			List<Object> chartHeaderData = new ArrayList<Object>(2) ;
			chartHeaderData.add("无");
			chartHeaderData.add("类型") ;
			deviceTypeChartData.add(chartHeaderData);
			String deviceTypeName = "";
			String dataSourceName = "" ;
			Object formatcount = null;
			Object logCount = null;
			Object logSize = null;
			Object logSize2 = null;
			long maxLogCount = 0L;
			long maxLogSize2 = 0L;
			for(int i = 0;i< logCountJson.size();i++){
				JSONObject json = (JSONObject) logCountJson.get(i);
				deviceTypeRow = deviceTypeTable.getRow(i+1) ;
				for(int j = 0;j < json.size();j++){
					dataSourceName = (String) json.get("dataSourceName");
					deviceTypeName = (String) json.get("deviceTypeName");
					deviceTypeName = dataSourceName == null ? deviceTypeName : dataSourceName + "(" + deviceTypeName + ")";
					formatcount = json.get("formatcount");
					logCount = json.get("logCount");
					logSize = json.get("logSize");
					logSize2 = json.get("logSize2");
				}
				deviceTypeRow.getCell(0).setText(deviceTypeName) ;
				deviceTypeRow.getCell(1).setText(formatcount.toString()) ;
				deviceTypeRow.getCell(2).setText(logSize.toString()) ;
				maxLogCount = Math.max(maxLogCount, ((Number)logCount).longValue());
				maxLogSize2 = Math.max(maxLogSize2, ((Number)logSize2).longValue());
			}
			WordChartCreator chartCreator = new WordChartCreator(doc) ;
			String unit = "";
			long base = 1L;
			Map<String, Object> mapCount = formatUnitOfCount(maxLogCount);
			unit = (String) mapCount.get("unit");
			base = (Long)(mapCount.get("base"));
			formatLogDigest(logCountJson, deviceTypeChartData,base,"logCount");
			chartCreator.createChart(1, "", deviceTypeChartData, unit) ;
			XWPFParagraph chartParaSize = doc.createParagraph();
			formatTitleOfWord(chartParaSize,selectNodeText+"日志概要大小统计图"+date);
			List<List<Object>> deviceTypeChartDataSize = new ArrayList<List<Object>>() ;
			List<Object> chartHeaderDataSize = new ArrayList<Object>(2) ;
			chartHeaderDataSize.add("无");
			chartHeaderDataSize.add("类型") ;
			deviceTypeChartDataSize.add(chartHeaderDataSize);
			String sizeUnit = "";
			long sizeBase = 1L;
			Map<String, Object> sizeMap = formatUnitOfSize(maxLogSize2);
			sizeUnit = (String) sizeMap.get("unit");
			sizeBase = (Long)(sizeMap.get("base"));
			formatLogDigest(logCountJson, deviceTypeChartDataSize,sizeBase,"logSize2");
			chartCreator.createChart(1, "", deviceTypeChartDataSize, sizeUnit) ;
			//日志统计列表
			XWPFParagraph nextPagePara = doc.createParagraph();
			nextPagePara.setPageBreak(true);//下一页
			XWPFParagraph LogPara = doc.createParagraph();
			formatTitleOfWord(LogPara,selectNodeText+"日志统计列表"+date);
			XWPFTable table = doc.createTable(partitions.size()+1,3);
			XWPFUtil.setCellWidths(table, 3000) ;
			XWPFTableRow row = table.getRow(0) ;
			row.getCell(0).setText("日期") ;
			row.getCell(1).setText("日志数量") ;
			row.getCell(2).setText("日志大小") ;
			XWPFParagraph nextPage = doc.createParagraph();
			nextPage.setPageBreak(true);//下一页
			XWPFParagraph logCountPara = doc.createParagraph();
			formatTitleOfWord(logCountPara, selectNodeText+"日志数量统计图"+date);
			List<List<Object>> countChartData = new ArrayList<List<Object>>() ;
			List<Object> countHeader = new ArrayList<Object>(2) ;
			countHeader.add("无") ;
			countHeader.add("数量") ;
			countChartData.add(countHeader) ;
			List<List<Object>> bytesChartData = new ArrayList<List<Object>>() ;
			List<Object> bytesHeader = new ArrayList<Object>(2) ;
			bytesHeader.add("无") ;
			bytesHeader.add("大小") ;
			bytesChartData.add(bytesHeader) ;
			String name = "";
			Object count = null;
			Object formatLogSize = null;
			long maxCount = 0L;
			long maxLogSize = 0L;
			for(int i = 0;i < partitions.size();i++){
				JSONObject detaiPartition = partitions.getJSONObject(i);
				row = table.getRow(i+1) ;
				for(int j = 0;j < detaiPartition.size();j++){
					name = detaiPartition.getString("name");
					count = detaiPartition.get("count");
					logSize = detaiPartition.get("logSize");
					formatLogSize = detaiPartition.getString("formatLogSize");
				}
				row.getCell(0).setText(name) ;
				row.getCell(1).setText(count.toString()) ;
				row.getCell(2).setText(formatLogSize.toString()) ;
				maxCount = Math.max(maxCount, ((Number)count).longValue());
				maxLogSize = Math.max(maxLogSize, ((Number)logSize).longValue());
			}
			base = 1L ;
			unit = "" ;
			Map<String, Object> mapCount2 = formatUnitOfCount(maxCount);
			unit = (String) mapCount2.get("unit");
			base = (Long)(mapCount2.get("base"));
			formatLog(partitions, countChartData, base, "count");
			chartCreator.createChart(1, "日志数量统计", countChartData, unit) ;
			XWPFParagraph logCountPara2 = doc.createParagraph();
			formatTitleOfWord(logCountPara2,selectNodeText+"日志大小统计图"+date);
			unit = "";
			base = 1L;
			Map<String, Object> mapSize = formatUnitOfSize(maxLogSize);
			unit = (String) mapSize.get("unit");
			base = (Long)(mapSize.get("base"));
			formatLog(partitions, bytesChartData, base, "logSize");
			chartCreator.createChart(1, "日志大小统计图", bytesChartData, unit) ;
			doc.write(response.getOutputStream());
			}catch (Exception e) {
			e.printStackTrace();
		}
	}
	//为word文档增加段落标题
	public static void formatTitleOfWord(XWPFParagraph para,String title){
		para.setAlignment(ParagraphAlignment.CENTER);
		XWPFRun run = para.createRun();
		run = para.createRun();  
		run.setFontSize(20);
		run.setBold(true);
		run.setText(title); 
	}
//	日志概要图格式处理
	public static void formatLogDigest(JSONArray logCountJson,List<List<Object>> deviceTypeChartData,long baseNum,String dataKey){
		String deviceTypeName = "";
		String dataSourceName = "" ;
		Object logCount = null;
		for(int i = 0;i< logCountJson.size();i++){
			JSONObject json = (JSONObject) logCountJson.get(i);
			for(int j = 0;j < json.size();j++){
				dataSourceName = (String) json.get("dataSourceName");
				deviceTypeName = (String) json.get("deviceTypeName");
				deviceTypeName = dataSourceName == null ? deviceTypeName : dataSourceName + "(" + deviceTypeName + ")";
				if(baseNum != 1){
					logCount = ObjectUtils.round(((Number)json.get(dataKey)).doubleValue()/baseNum,1);
				}else{
					logCount = json.get(dataKey) ;
				}
			}
			List<Object> chartData = new ArrayList<Object>(2) ;
			chartData.add(deviceTypeName) ;
			chartData.add(logCount) ;
			deviceTypeChartData.add(chartData) ;
		}
	}
//	日志大小、数量统计图格式处理
	public static void formatLog(JSONArray partitions,List<List<Object>> chartData,long baseNum,String dataKey){
		String name = "";
		Object data = null;
		for(int i = 0;i < partitions.size();i++){
			JSONObject detaiPartition = partitions.getJSONObject(i);
			for(int j = 0;j < detaiPartition.size();j++){
				name = detaiPartition.getString("name");
				if(baseNum != 1){
					data = ObjectUtils.round(((Number)detaiPartition.get(dataKey)).doubleValue()/baseNum,1);
				}else{
					data = detaiPartition.get(dataKey) ;
				}
			}
			List<Object> logData = new ArrayList<Object>(2) ;
			logData.add(name) ;
			logData.add(data) ;
			chartData.add(logData) ;
		}
	}

//	日志大小格式化
	public Map<String, Object> formatUnitOfSize(long value){
		Map<String, Object> map = new HashMap<String, Object>();
		String unit = "";
		long base = 1L;
		if(value/2 > 1024L*1024*1024*1024){
			unit = "T";
			base = 1024L*1024*1024*1024;
		}else if(value/2 > 1024L*1024*1024){
			unit = "G";
			base = 1024L*1024*1024;
		}else if(value/2 > 1024L*1024){
			unit = "M";
			base = 1024L*1024;
		}else if(value/2 > 1024L){
			unit = "K";
			base = 1024L;
		}
		map.put("unit", unit);
		map.put("base", base);
		return map;
	}
//	日志数量格式化
	public Map<String, Object> formatUnitOfCount(long value){
		Map<String, Object> map = new HashMap<String, Object>();
		String unit = "";
		long base = 1L;
		if(value/2 > 100000000L){
			unit = "亿";
			base = 100000000L;
		}else if(value/2 > 10000L){
			unit = "万";
			base = 10000L;
		}
		map.put("unit", unit);
		map.put("base", base);
		return map;
	}

	@RequestMapping("contextMenuTree")
	@ResponseBody
	public Object contextMenuTree(SID sid,HttpServletRequest request) {
     	List<TreeModel> listModel = new ArrayList<TreeModel>();
		try {
			if(sid.hasAuditorRole()){
				TreeModel deviceModel = new TreeModel(LogKeyInfo.LOG_SYSTEM_TYPE,DeviceTypeNameUtil.getDeviceTypeName(LogKeyInfo.LOG_SYSTEM_TYPE));
				listModel.add(deviceModel);
				return listModel;
			}
			if(dataSourceService == null){
				dataSourceService = (DataSourceService)FacadeUtil.getFacadeBean(request, null, "dataSourceService");
			}
			String ip = request.getParameter("ip");
			List<Map<String, Object>> list = dataSourceService.getDataSourceTreeWithNodeList(null);
			if (list != null && list.size() > 0) {
				List<Map<String, Object>> nodes = getDistinctNodesByNodeIp(list);
				if (nodes != null) {
					for (Map<String, Object> node : nodes) {
						List<String> deviceTypes = getDistinctDeviceTypeByNodeIpAndDataSourceType(list, node,sid,"0");
						if (deviceTypes != null && deviceTypes.size()>0) {
								TreeModel modelAll = new TreeModel();
								modelAll.setText("所有设备");
								Map<String,Object>  allType = new HashMap<String,Object>();
								allType.put("deviceType", SimDatasource.DATASOURCE_ALL);
								allType.put("nodeId", node.get("auditorNodeId"));
								modelAll.setAttributes(allType);
								listModel.add(modelAll);
								
							for (String deviceType : deviceTypes) {
								if (LogKeyInfo.LOG_SYSTEM_TYPE.equals(deviceType))
									continue;
								
								List<LogField> fields = IndexTemplate.getTemplate(deviceType).getFields();
								for(LogField field:fields){
									if(field.getName().equals(ip)){
										TreeModel deviceModel = new TreeModel(deviceType,DeviceTypeNameUtil.getDeviceTypeName(deviceType));
										deviceModel.putAttribute("deviceType", deviceType);
										deviceModel.putAttribute("nodeId", node.get("auditorNodeId"));
										listModel.add(deviceModel);
									}
								}
								
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return listModel;
  }
	/**
	 * 修改日志查询上限
	 * @param request
	 * @return
	 */
	@RequestMapping("changeLimit")
	@ResponseBody
	public Object changeLimit(HttpServletRequest request){
		Integer searchLimit = StringUtil.toInteger(request.getParameter("searchLimit")) ;
		Integer displayLimit = StringUtil.toInteger(request.getParameter("displayLimit")) ;
		if(searchLimit != null && (searchLimit > 10000000 || searchLimit < 10000)){
			return new Result(false, "非法的查询上限(10000-10000000)") ;
		}
		if(displayLimit != null && (displayLimit > 1000000 || displayLimit < 10000)){
			return new Result(false,"非法的显示上限(10000-1000000)");
		}
		ChainMap<String,Object> params = new ChainMap<String,Object>("searchLimit", searchLimit).push("displayLimit", displayLimit) ;
		try {
			NodeUtil.sendCommand(RouteUtils.getQueryServiceRoutes(), MessageDefinition.CMD_SEARCH_SET_LIMIT, params,5000) ;
			return new Result(true,null) ;
		} catch (CommunicationException e) {
			e.printStackTrace();
			return new Result(false,"修改日志上限失败！") ;
		}
	}
}