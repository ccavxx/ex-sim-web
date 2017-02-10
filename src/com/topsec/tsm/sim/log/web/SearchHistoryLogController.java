package com.topsec.tsm.sim.log.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.biff.RowsExceededException;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jboss.util.StopWatch;
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
import com.topsec.tal.base.archive.ArchiveFileReader;
import com.topsec.tal.base.archive.ArchiveFileReaderHelper;
import com.topsec.tal.base.archive.Event;
import com.topsec.tal.base.index.IndexTemplateUtil;
import com.topsec.tal.base.index.template.GroupCollection;
import com.topsec.tal.base.index.template.IndexTemplate;
import com.topsec.tal.base.index.template.LogField;
import com.topsec.tal.base.index.template.LogFieldPropertyFilter;
import com.topsec.tal.base.search.AbstractCondition;
import com.topsec.tal.base.search.AdvancedCondition;
import com.topsec.tal.base.search.HistorySearchCacheItem;
import com.topsec.tal.base.search.HistorySearchObject;
import com.topsec.tal.base.search.LogExportResult;
import com.topsec.tal.base.search.LogRecordSet;
import com.topsec.tal.base.search.LogSearchException;
import com.topsec.tal.base.search.Record;
import com.topsec.tal.base.search.SearchObject;
import com.topsec.tal.base.search.SearchResult;
import com.topsec.tal.base.search.StatisticObject;
import com.topsec.tal.base.search.UnindexException;
import com.topsec.tal.base.util.ChainMap;
import com.topsec.tal.base.util.SpringWebUtil;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.util.config.Block;
import com.topsec.tal.base.util.config.Config;
import com.topsec.tsm.auth.manage.ColumnConfigId;
import com.topsec.tsm.framework.interfaces.TypeDef;
import com.topsec.tsm.framework.statisticor.exception.StatisticException;
import com.topsec.tsm.framework.statisticor.metadata.FieldMeta;
import com.topsec.tsm.framework.statisticor.metadata.FieldsMeta;
import com.topsec.tsm.framework.statisticor.metadata.GroupsMeta;
import com.topsec.tsm.framework.statisticor.metadata.OrderMeta;
import com.topsec.tsm.framework.statisticor.metadata.OrdersMeta;
import com.topsec.tsm.framework.statisticor.metadata.StatisticMeta;
import com.topsec.tsm.framework.statisticor.result.StatisticResult;
import com.topsec.tsm.framework.statisticor.worker.Statistic;
import com.topsec.tsm.framework.statisticor.worker.StatisticFactory;
import com.topsec.tsm.framework.util.TypeDefFactory;
import com.topsec.tsm.framework.util.dataable.DataBuilder;
import com.topsec.tsm.node.collector.DataConstants;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.auth.service.UserService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.log.IndexComparator;
import com.topsec.tsm.sim.log.IndexingThread;
import com.topsec.tsm.sim.log.LogFilter;
import com.topsec.tsm.sim.log.LogStatsUtils;
import com.topsec.tsm.sim.log.bean.LogSearchObject;
import com.topsec.tsm.sim.log.util.HistoryLogUtil;
import com.topsec.tsm.sim.log.util.LogRecordList;
import com.topsec.tsm.sim.log.util.LogUtil;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.response.persistence.Response;
import com.topsec.tsm.sim.util.CommonUtils;
import com.topsec.tsm.sim.util.DateUtils;
import com.topsec.tsm.sim.util.DeviceTypeNameUtil;
import com.topsec.tsm.sim.util.ExportExcelHandler;
import com.topsec.tsm.sim.util.ExportExcelUtil;
import com.topsec.tsm.sim.util.FastJsonUtil;
import com.topsec.tsm.sim.util.FtpConfigUtil;
import com.topsec.tsm.tal.response.base.RespCfgHelper;
import com.topsec.tsm.tal.service.EventResponseService;
import com.topsec.tsm.util.StringFormater;
import com.topsec.tsm.util.SystemInfoUtil;
import com.topsec.tsm.util.net.FtpUploadUtil;

@Controller
@RequestMapping("searchHistoryLog")
public class SearchHistoryLogController {
	private static final Logger log = LoggerFactory.getLogger(SearchHistoryLogController.class);
	private int MAX_LOG_COUNTS = 20000 * 60 * 5;
	private static Map<String, HistorySearchCacheItem> caches = new ConcurrentHashMap<String, HistorySearchCacheItem>();
	private ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
	private NodeMgrFacade nodeMgrFacade;
	private EventResponseService eventResponseService;
	public static Map<String, HistoryLogUtil> queryMap = new ConcurrentHashMap<String, HistoryLogUtil>();// key:userName
	private SimpleDateFormat formatYMDHMS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private SimpleDateFormat formatYMD = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat formatHMS = new SimpleDateFormat("HH:mm:ss");
	private Thread worker = null;
	private static Timer timer = null;
	private long timestamp = 5 * 1000;
	private SearchTimerTask myTimeTask = null;
	private static int SearchLimit = 10000;
	static {
		String val = System.getProperty("TSM.SEARCH.LIMIT");
		if (val != null) {
			try {
				SearchLimit = Integer.valueOf(val);
			} catch (Exception e) {

			}
		}
	}
	private static TypeDef _typedef = TypeDefFactory.createInstance("typedef.xml");
	/*
	 * @author:丁广富
	 * 
	 * @param logHome
	 * 
	 * @return HistLogInfo
	 */
	public void getIndexInfo() {
		try {
			String path = getDataHome();
			String tempLogPath = new StringBuilder(path)
					.append("/indexes/temp").toString();
			File file = new File(tempLogPath);
			File[] files = file.listFiles();
			List<File> fileList = Arrays.asList(files);
			Collections.sort(fileList, new IndexComparator());
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public String getDataHome() throws Exception {
		Node node = nodeMgrFacade.getKernelAuditor(false, true, false, false);
		String nodeid = node.getNodeId();
		Set<Node> children = node.getChildren();
		for (Node child : children) {
			String type = child.getType();
			if (NodeDefinition.NODE_TYPE_INDEXSERVICE.equals(type)) {
				nodeid = child.getNodeId();
				break;
			}
		}

		List<Response> responses = eventResponseService
				.getResponsesByNodeId(nodeid);
		Config config = null;
		for (Response res : responses) {
			if ("sys_cfg_store".equals(res.getCfgKey())) {
				config = RespCfgHelper.getConfig(res);
			}
		}
		String path = null;
		Block archive_path = config.getBlockbyKey("archive_path");

		if ("archive_path".equalsIgnoreCase(archive_path.getKey())) {
			path = archive_path.getItemValue("archive_path");
		}
		return path;
	}
	@RequestMapping("doIpLocationStat")
	@ResponseBody
	public Object doIpLocationStat(SID sid,@RequestParam(value="type",defaultValue="4")int type,
			LogSearchObject logSearchObject, HttpServletRequest request,HttpSession session){
		List<Map<String, Object>> datas = new ArrayList<Map<String,Object>>();
		try {
			if(type < 0 || type > 5){
				return null ;
			}
			session.setAttribute("condition", logSearchObject);
			datas = getIpLocationData(logSearchObject, type, sid, session);
		} catch (Exception e) {
			e.printStackTrace();
			datas = Collections.emptyList() ;
		}
		return datas ;
	}
	private List<Map<String, Object>> getIpLocationData(LogSearchObject logSearchObject, int type, SID sid,HttpSession session) throws Exception{
		List<Map<String, Object>> datas = new ArrayList<Map<String,Object>>();
		StatisticObject statObject = builtStatisticObject(logSearchObject, type);
		AbstractCondition scon = new AdvancedCondition(statObject.getConditionNames(),
				statObject.getQueryContents(), statObject.getOperators(), statObject.getQueryTypes());
		final StringBuilder cacheKey = new StringBuilder(256).append(scon).append(statObject.getGroup()).append(sid.getAccountID());
		HistorySearchCacheItem cache = getCache(statObject, cacheKey.toString());
		waitQueryComplete(cache, statObject, cacheKey.toString());
		List<Map<String,Object>> statResult = doSearchResultStatistic(_typedef, cache, SearchLimit, statObject);
		if(statResult != null){
			for(Map<String,Object> item:statResult){
				if(StringUtil.isBlank((String)item.get("SRC_LOCATION")) || "未知".equals(item.get("SRC_LOCATION"))){
					continue ;
				}
				datas.add(ChainMap.newMap("name", item.get("SRC_LOCATION")).push("value", item.get("OPCOUNT"))) ;
			}
		}
		return datas;
	}
	private void waitQueryComplete(HistorySearchCacheItem cache, StatisticObject statObject, String cacheKey) throws LogSearchException{
		final AbstractCondition query = new   AdvancedCondition(statObject.getConditionNames(),
				statObject.getQueryContents(), statObject.getOperators(),statObject.getQueryTypes());
		if(caches.get(cacheKey.toString())==null){
			if(statObject.isCancel()){
//				result.setFinished(true);
//				result.setStats(new HashMap<String, Object>());
				log.info("取消查询。。。");
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("收到统计命令[").append(statObject.getType()).append("]");
				String host = statObject.getHost();
				if(host != null){
					sb.append("(").append(host).append(")");
				}
				if(!query.isSearchAll()){
					sb.append(query.toReadableString());
				}
				log.info(sb.toString());
			}
		}
		if(cache != null && !cache.isDone()){
			throw new LogSearchException("查询统计超时！") ;
		}
	}
	private HistorySearchCacheItem getCache(StatisticObject statObject, String cacheKey){
		HistorySearchCacheItem cache = null;
		try {
			cache = (HistorySearchCacheItem) caches.get(cacheKey.toString());
		} catch (Exception e1) {
			e1.printStackTrace();
			return cache;
		}
		if(cache != null){
			int waitCount = 0 ;
			while(!cache.isDone()&&waitCount++<10){
				log.warn("正在等待查询结果结束！");
				try {
					Thread.sleep(1000) ;
				} catch (InterruptedException e) {
					e.printStackTrace();
					waitCount = 100 ;
				}
			}
			//停止查询
			cache.setCancel(statObject.isCancel());
			cache.setVisitTime(System.currentTimeMillis());
		}
		return cache;
	}
	private StatisticObject builtStatisticObject(LogSearchObject logSearchObject, int type){
		StatisticObject searchObject = new StatisticObject();
		logSearchObject.fillSearchObject(searchObject) ;
		String groupFunction = StringFormater.format("IpMapper({},{})", logSearchObject.getStatColumn(),type) ;
		searchObject.putSelect("SRC_LOCATION", groupFunction) ;
		searchObject.putSelect("OPCOUNT", "COUNT("+logSearchObject.getStatColumn()+")") ;
		searchObject.putGroupBy("SRC_LOCATION", groupFunction) ;
		return searchObject;
	}
	public List<Map<String,Object>> doSearchResultStatistic(
			TypeDef typeDef,HistorySearchCacheItem cache,int displayLimit,StatisticObject statObject) throws StatisticException{
		Statistic statistic = StatisticFactory.create(_typedef, null, statObject.getSelectFields(), 
				statObject.getWhereCondition(), statObject.getGroupFields(), statObject.getOrderFields()) ;
		List<Map<String,Object>> datas = doCacheStatistic(statistic, cache, displayLimit);
		
		int top = statObject.getTop() == null || statObject.getTop() < 1 ? Integer.MAX_VALUE : statObject.getTop() ; 
		if(datas.size() > top){
			//此处不能使用subList，因此subList返回的对象不支持序列化
			datas = new ArrayList<Map<String,Object>>(datas.subList(0, top));
		}
		return datas ;
	}
	private List<Map<String,Object>> doCacheStatistic(Statistic statistic,HistorySearchCacheItem cache,int displayLimit){
		try {
			List<Record> records = cache.getRecords() ;
			if(records == null){
				return Collections.emptyList() ;
			}
			int limit = Math.min(records.size(), displayLimit) ;
			for(int i=0;i<limit;i++){
				Event event = ArchiveFileReaderHelper.read(records.get(i));
				if(event == null) {
					continue;
				}
				statistic.work(event.getMap());
			}
			StatisticResult stat = statistic.getResult(false);
			List<Map<String, Object>> data = stat.getMapRecords();
			return data ;
		} catch (Exception e) {
			e.printStackTrace();
			return Collections.emptyList() ;
		}
	}
	
	/**
	 * 日志导入查询
	 * 
	 * @author 周小虎
	 * @param deviceType
	 * @param pageNo
	 * @param pageSize
	 * @param conditionName
	 * @param operator
	 * @param queryContent
	 * @return
	 */
	@RequestMapping("doLogSearch")
	@ResponseBody
	public Object doLogSearch(SID sid,@RequestBody LogSearchObject logSearchObject,HttpServletRequest request) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		LogRecordList recordList = new LogRecordList();
		List<LogUtil> logUitls = new ArrayList<LogUtil>();
		List<LogUtil> sourcelogUitls = new ArrayList<LogUtil>();
		List<String> columnTypes = new ArrayList<String>();
		List<String> columnNames = new ArrayList<String>();
		
		String[] conditionNames = logSearchObject.getConditionName();
		String[] operators = logSearchObject.getOperator();
		String[] queryContents = logSearchObject.getQueryContent();
		String[] types = logSearchObject.getQueryType();
		LogRecordSet resultSet = new LogRecordSet();
		String userName = sid.getUserName();
		int userId =sid.getAccountID();
		HistorySearchObject searchObject = new HistorySearchObject();
		
		searchObject.setType(logSearchObject.getDeviceType());
		searchObject.setPage(logSearchObject.getPageNo());
		searchObject.setPerPage(logSearchObject.getPageSize());

		searchObject.setGroup(logSearchObject.getGroup());
		searchObject.setCancel(logSearchObject.isCancel());

		AbstractCondition scon = new AdvancedCondition(conditionNames,
				queryContents, operators, types);
		searchObject.setCond(scon);
		searchObject.setGroup(logSearchObject.getGroup());

		if (worker == null) {
			worker = new Thread(new CacheManager());
			ses.scheduleAtFixedRate(worker, 30, 30, TimeUnit.SECONDS);
		}

		String cacheKey = new StringBuilder(256).append(scon).append(logSearchObject.getGroup()).append(userId)
				.toString();
		HistorySearchCacheItem cache = (HistorySearchCacheItem) caches
				.get(cacheKey);
		
			try {
				if (cache == null) {
					cache = buildCache(userId, cacheKey, logSearchObject.getDeviceType(), scon, logSearchObject.getGroup(), true);
				}
			} catch (LogSearchException e) {

				resultSet.setException(e);
				// return result;
			}
			cache.setCancel(logSearchObject.isCancel());
			cache.setVisitTime(System.currentTimeMillis());
			if (timer == null) {
				timer = new Timer();
				Date d = new Date(Calendar.getInstance().getTimeInMillis()
						+ timestamp);
				// 创建定时任务并启动
				myTimeTask = new SearchTimerTask();
				timer.schedule(myTimeTask, d, timestamp);
			}
			if (queryMap.containsKey(userName)) {
				if (!searchObject.equals(queryMap.get(userName)
						.getSearchObject())) {
					queryMap.remove(userName);
				} else {
					if (logSearchObject.isCancel()) {
						queryMap.remove(userName);
					} else {
						HistoryLogUtil util = queryMap.get(userName);
						util.setCounts(util.getCounts() - 1);
					}
				}
			}
			if (logSearchObject.isCancel() != true && !queryMap.containsKey(userName)) {
				HistoryLogUtil logUtil = new HistoryLogUtil();
				logUtil.setSearchObject(searchObject);
				logUtil.setCounts(1);
				// 将用户信息及查询条件保存至map中
				queryMap.put(userName, logUtil);
			}
			//更新统计数据
			Map<String, Object> stats = cache.getStats();
			
			if(stats == null){
				try {
					stats = statResult(cache,logSearchObject);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				resultSet.setStats(stats);
				if(cache.isDone())
					cache.setStats(stats);
			}else{
				resultSet.setStats(stats);
			}
			resultSet = (LogRecordSet) searchHistory(cache,
					searchObject.getPerPage(), searchObject.getPage());
			// }
			List<JSONObject> columnHeaders = new ArrayList<JSONObject>();
		if (resultSet != null) {
			for (Map map : resultSet.getColumnNames()) {
				Set set = map.entrySet();
				for (Iterator iter = set.iterator(); iter.hasNext();) {
					Map.Entry entry = (Map.Entry) iter.next();
					LogUtil logUitl = new LogUtil();
					logUitl.setDataField(entry.getKey().toString());
					logUitl.setHeaderText(entry.getValue().toString());
					logUitls.add(logUitl);
					columnNames.add(entry.getKey().toString());
				}
			}

			for (Map type : resultSet.getColumnTypes()) {
				Set set = type.entrySet();
				for (Iterator iter = set.iterator(); iter.hasNext();) {
					Map.Entry entry = (Map.Entry) iter.next();
					columnTypes.add(entry.getValue().toString());
				}
			}
			GroupCollection collection = IndexTemplate.getTemplate(logSearchObject.getDeviceType()).getGroup(logSearchObject.getGroup()) ;
			UserService userService = (UserService) SpringWebUtil.getBean("userService", request) ;
			String module = "/sim/log/logQuery/" + logSearchObject.getDeviceType() + "/" + logSearchObject.getGroup() ;
			Map<String,JSONObject> columnConfigMap = userService.getColumnConfigMap(new ColumnConfigId(sid.getAccountID(), module)) ;
			for(LogField field:collection.getFields(new LogFieldPropertyFilter("visiable", true))){
				JSONObject fieldJSON = FastJsonUtil.toJSON(field, "alias=headerText","hidden","name=dataField","type") ;
				JSONObject userConfig = columnConfigMap.get(field.getName()) ;
				if(userConfig != null){
					fieldJSON.put("width", Math.max(userConfig.getIntValue("width"),70)) ;
					fieldJSON.put("hidden", userConfig.getBoolean("hidden")) ;
				}
				columnHeaders.add(fieldJSON);
			}
			formatRecords(resultSet.getMaps(), columnNames, columnTypes);
			for (SearchResult sresult : resultSet.getRecords()) {
				LogUtil logUtil = new LogUtil();
				logUtil.setIp(sresult.get_ip());
				logUtil.setMsg(sresult.get_msg());
				logUtil.setTime(dateFormat.format(new Date(new Long(sresult
						.get_time()))));
				sourcelogUitls.add(logUtil);
			}

			if (resultSet.getException() != null) {
				log.error(resultSet.getException().getMessage());
				LogSearchException exception = (LogSearchException) resultSet
						.getException();
				if (exception.get_type() == 3) {
					recordList
							.setExceptionInfo("您选择的查询条件超出系统处理能力,\n请缩小时间范围和精确查询条件!");
				} else if (exception.get_type() == 1) {
					recordList.setExceptionInfo("您指定的时间范围内没有可用日志!");
				} else if (exception.get_type() == 2) {
					recordList.setExceptionInfo("系统忙,请稍候查询!");
				} else if (exception.get_type() == 4) {
					recordList.setExceptionInfo("正在激活索引,请稍候查询!");
				} else if (exception.get_type() == 7) {
					recordList.setExceptionInfo("正在更新缓存,请稍候查询!");
				} else {
					recordList.setExceptionInfo("没有符合查询条件的记录!");
				}
				recordList.setType(exception.get_type());
				queryMap.remove(userName);
				recordList.setTotalRecords(resultSet.getTotalRecords());
				recordList.setTotalCount(resultSet.getTotalLogs()+"");
				recordList.setLogUtil(logUitls);
				recordList.setMaps(resultSet.getMaps());
				recordList.setRecords(sourcelogUitls);
				recordList.setFinished(true);
				return recordList;
			}
			if (resultSet.isFinished()) {
				queryMap.remove(userName);
				if (resultSet.getRecords().size() == 0) {
					recordList.setType(1);
					recordList.setExceptionInfo("没有符合查询条件的记录!");
					recordList.setTotalRecords(resultSet.getTotalRecords());
					recordList.setTotalLogs(resultSet.getTotalLogs());
					recordList.setLogUtil(logUitls);
					recordList.setMaps(resultSet.getMaps());
					recordList.setRecords(sourcelogUitls);
					recordList.setFinished(true);
					//return recordList;

				}
			}
			recordList.setDisplayCount(Math.min(resultSet.getTotalRecords(),SearchLimit));
			recordList.setTotalRecords(resultSet.getTotalRecords());
			recordList.setColumns(columnHeaders);
			recordList.setTotalCount(resultSet.getTotalLogs()+"");
			recordList.setLogUtil(logUitls);
			recordList.setMaps(resultSet.getMaps());
			recordList.setRecords(sourcelogUitls);
			recordList.setFinished(resultSet.isFinished());
			recordList.setLapTime(Integer.parseInt(resultSet.getLapTime() + ""));
			recordList.setFilters(IndexTemplateUtil.getInstance().getSearchTemplate(logSearchObject.getDeviceType(), logSearchObject.getGroup()));
			//雷达图数据组装
			try {
				recordList.setTimeline(LogStatsUtils.formatLogChart(
						statResult(cache,logSearchObject),
						DateUtils.parseDatetime(logSearchObject.getQueryStartDate()),DateUtils.parseDatetime(logSearchObject.getQueryEndDate())));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		return recordList;

	}
	/**
	 * 
	 * 根据设备类型获取列集
	 * @param deviceType
	 * @return
	 */
	@RequestMapping("getGroupListByDeviceType")
	@ResponseBody
public Object getGroupListByDeviceType(@RequestParam(value="deviceType",defaultValue="")String deviceType){
	 List<Map<String,Object>>  listGroup = IndexTemplateUtil.getInstance().getGroupByDeviceType(deviceType);
	 JSONArray deviceArray=new JSONArray();
	 JSONArray childArray=new JSONArray();
	 for (Iterator groupIterator= listGroup.iterator(); groupIterator.hasNext();) {
		 Map<String, Object> map = (Map<String, Object>) groupIterator.next();
		 JSONObject group = new JSONObject();
		 group.put("id",map.get("groupId").toString());
		 group.put("text",map.get("name").toString());
		 Map attributes=new HashMap();
		 attributes.put("deviceType",map.get("deviceType").toString());
		 attributes.put("filter", map.get("filter").toString());
		 group.put("attributes", attributes);
		 childArray.add(group);
	 }
	 JSONObject device=new JSONObject();
	 device.put("id",deviceType);
	 device.put("text",DeviceTypeNameUtil.getDeviceTypeName(deviceType,Locale.getDefault()));
	 device.put("children", childArray);
	 deviceArray.add(device);
	return deviceArray;
}
	/**
	 * 对返回对象做处理， Date对象就转成yyyy-MM-dd HH:mm:ss格式的字符串，
	 * 其余的对象都调用toString()转成字符串，PRIORITY的值转成相应的级别
	 */
	@SuppressWarnings("unchecked")
	private synchronized void formatRecords(List<Map<String,Object>> formatResult,
			List<String> names, List<String> types) {
		for (Map map : formatResult) {
			for (int i = 0; i < names.size(); i++) {
				if (map.containsKey(names.get(i))) {
					if (types.get(i).equalsIgnoreCase("ip")) {
						map.put(names.get(i), map.get(names.get(i)).toString());
					} else if (types.get(i).equalsIgnoreCase("mac")) {
						map.put(names.get(i), map.get(names.get(i)).toString());
					} else if (types.get(i).equalsIgnoreCase("Date")) {
						if (map.get(names.get(i)) != null) {
							Object date = map.get(names.get(i));
							if (date != null && date instanceof Date) {
								map.put(names.get(i),StringUtil.longDateString((Date) date));
							}else{
								log.warn("{}不是合法的日期类型",date) ;
								map.put(names.get(i),date.toString());
							}
						}
					} else if (names.get(i).equalsIgnoreCase("PRIORITY")) {
						map.put(names.get(i),CommonUtils.getLevel(map.get(names.get(i)))) ;
					}
				}
			}
		}
	}

	class CacheManager implements Runnable {

		public void run() {

			Thread.currentThread().setName("CacheManager");
			if (SystemInfoUtil.getInstance().isSearchMemoryBusy()) {
				caches.clear();
			}
			try {
				long clearTimes = 300000;
				Set set = caches.entrySet();
				java.util.Map.Entry[] items = new java.util.Map.Entry[caches
						.entrySet().size()];
				set.toArray(items);
				for (java.util.Map.Entry field : items) {
					String name = (String) field.getKey();
					HistorySearchCacheItem cache = (HistorySearchCacheItem) field
							.getValue();
					long now = System.currentTimeMillis();

					if ((now - cache.getVisitTime()) > clearTimes) {
						caches.remove(name);
					}

				}

			} catch (Exception e) {

				log.warn(e.getMessage());
			}

		}

	}

	private HistorySearchCacheItem buildCache(int userId,String cacheKey, String type,
			AbstractCondition query, String group, boolean reverse)
			throws LogSearchException {

		List<Record> records = new ArrayList<Record>();
		HistorySearchCacheItem cache = new HistorySearchCacheItem(cacheKey,
				type, query, reverse);
		List<String> names = IndexTemplateUtil.getInstance()
				.getVisiableGroupFieldParam(type, group, "name");
		List<String> alias = IndexTemplateUtil.getInstance()
				.getVisiableGroupFieldParam(type, group, "alias");
		List<String> types = IndexTemplateUtil.getInstance()
				.getVisiableGroupFieldParam(type, group, "type");
		cache.setColumnNames(alias);
		cache.setColumnKeys(names);
		cache.setColumnTypes(types);
		cache.setRecords(records);

		caches.put(cacheKey, cache);
		cache.setTotal(0);

		if (!IndexingThread.isBusy()) {
			getIndexInfo();

		}
		new DeamonSearcher(cache, userId).start();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
		return cache;
	}

	class DeamonSearcher extends Thread {

		private HistorySearchCacheItem cache;

		int userId;
		DeamonSearcher(HistorySearchCacheItem cache, int userId) {
			this.cache = cache;
			this.userId = userId;
		}

		@Override
		public void run() {
			//计算历史日志总数
			String path = ".";
			try {
				path = getDataHome();
			} catch (Exception e1) {
				e1.printStackTrace();
				cache.setDone(true);
				cache.setTotal(0);
				return;
			}
			String tempLogPath = new StringBuilder(path).append("/events/temp/").append(userId).toString();
			File file = new File(tempLogPath);
			File[] files = file.listFiles(new LogFilter());

			int allLogs = 0;
			try{
				for (File f : files) {
					ArchiveFileReader reader = new ArchiveFileReader(f.getAbsolutePath());
					allLogs += reader.getEvents();
					reader.close();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			cache.setAllLogs(allLogs);
			
			String type = cache.getType();
			boolean reverse = cache.getDocOrder();
			AbstractCondition query = cache.getQuery();
			// long start = System.currentTimeMillis();
			String tempIndexPath = new StringBuilder(path).append("/indexes/temp/").append(userId).toString();
			File lf = new File(tempIndexPath);
			while (!cache.isDone() && !cache.isCancel()) {
				if (caches.get(cache.getCacheKey()) == null)
					break;

				int count = search(cache, lf, query, reverse) ;
				if(count == 0 || count == SearchLimit || count == allLogs){
					cache.setDone(true);
					break ;
				}
				synchronized (cache) {
					String queryCondition = query.isSearchAll() ? "" : "("+query.toReadableString()+")" ;
					if (IndexingThread.isBusy()) {
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (SystemInfoUtil.getInstance().isDataHomeFull()) {
							cache.setDone(true);
							String sb = "由于存储空间不足，查询[" + type + "]" + "日志结束，共有" + cache.getTotal() + "条符合要求" + queryCondition + "的日志。";
							log.info(sb);
							return;
						}						
					}else{
						//如果查询出来的数据与实际数据不相等再重新执行一次查询
						if(cache.getTotal() != allLogs && cache.getTotal() != SearchLimit){
							search(cache, lf, query, reverse) ;
						}
						cache.setDone(true);
						String logInfo = "查询历史日志["+type+"]" + "日志结束，共有" + cache.getTotal() + "条符合要求" + queryCondition + "的日志。";
						log.info(logInfo);
					}
				}

			}
			if (cache.isCancel()) {
				cache.setDone(true);
				log.info("取消查询");
			}
		}
		
		private int search(HistorySearchCacheItem cache,File lf,AbstractCondition query,boolean reverse){
			IndexReader reader = null;
			IndexSearcher searcher = null;
			if (cache.isCancel()) {
				log.info("取消查询");
				cache.setDone(true);
				return 0;
			}
			try {
				reader = DirectoryReader.open(FSDirectory.open(Paths.get(lf.getAbsolutePath())));
				searcher = new IndexSearcher(reader);
				Query queryObj = query.getQueryObj();
				TopDocs topDocs = searcher.search(queryObj, null, SearchLimit, new Sort(new SortField(null, SortField.Type.DOC, reverse)));
				
				if (topDocs.totalHits == 0) {
					cache.setDone(true);
					return 0;
				}
				
				List<Record> records = new ArrayList<Record>();
				ScoreDoc[] hits = topDocs.scoreDocs;
				int total = 0 ;
				for (int i = total; i < hits.length; i++) {
					if (total < SearchLimit) {
						Document doc = searcher.doc(hits[i].doc);
						String time = doc.get("RECV_TIME");
						BigInteger _bi = new BigInteger(time, 36); // System.out.println(_bi.toString(10));
						time = _bi.toString(10);
						String eventFile = doc.get("EVENTFILE");
						String chunkPos = doc.get("MAGICPOSITION");
						records.add(new Record(time, eventFile,chunkPos));
					}
					total++ ;
				}
				synchronized (cache) {
					cache.setRecords(records);
					cache.setTotal(total);
				}
			} catch (Exception e) {
				log.error(e.getMessage());
				return -1 ;
			} finally {
				try {
					if (reader != null)
						reader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return cache.getTotal() ;
		}
	}

	
	class SearchTimerTask extends TimerTask {

		@Override
		public void run() {
			// System.out.println("????");
			try {
				for (String key : LogSearchController.queryMap.keySet()) {
					int count = LogSearchController.queryMap.get(key)
							.getCounts();
					if (count >= 5) {
						LogUtil lu = LogSearchController.queryMap.get(key);
						lu.getSearchObject().setCancel(true);
						// 停止查询
						// String cacheKey = new
						// StringBuilder(256).append(lu.getSearchObject().getCond()).append(lu.getSearchObject().getGroup()).toString();

						String cacheKey = new StringBuilder(256)
								.append(lu.getSearchObject()
										.getConditionNames())
								.append(lu.getSearchObject().getOperators())
								.append(lu.getSearchObject().getQueryTypes())
								.append(lu.getSearchObject().getQueryContents())
								.append(lu.getSearchObject().getGroup())
								.toString();
						HistorySearchCacheItem cache = (HistorySearchCacheItem) caches
								.get(cacheKey);
						if (cache != null)
							cache.setCancel(true);
						LogSearchController.queryMap.remove(key);
					} else {
						LogSearchController.queryMap.get(key).setCounts(
								count + 1);
						// System.out.println("++");
					}
				}
			} catch (Exception e) {

			}
		}
	}

	public LogRecordSet searchHistory(HistorySearchCacheItem cache,
			int hitsPerPage, int fromPage) {
		LogRecordSet result = new LogRecordSet();
		List logs = result.getRecords();
		List maps = result.getMaps();
		result.setType(0);
		result.setLogType(cache.getType());

		StopWatch sw = new StopWatch();
		synchronized (cache) {
			sw.start();
			result.setFinished(cache.isDone());
			List<String> cols = cache.getColumnNames();
			List<String> types = cache.getColumnTypes();
			int size = cols.size();
			List<String> keys = cache.getColumnKeys();

			for (int i = 0; i < size; i++) {
				Map<String, String> column = new HashMap<String, String>();
				column.put(keys.get(i), cols.get(i));
				result.getColumnNames().add(column);
			}
			for (int i = 0; i < size; i++) {
				Map<String, String> column = new HashMap<String, String>();
				column.put(keys.get(i), types.get(i));
				result.getColumnTypes().add(column);
			}
			result.setTotalRecords(cache.getTotal());
			result.setTotalLogs(cache.getAllLogs());
			cache.setVisitTime(System.currentTimeMillis());

			long numTotalHits = cache.getRecords().size();
			// logger.info(numTotalHits + " matching documents");

			int from = (fromPage - 1) * hitsPerPage;

			if (from > numTotalHits) {
				// logger.debug("Skip Log File");
				result.setException(new LogSearchException("out of range",
						LogSearchException.OUTOF_RANGE));

			}

			int end = from + hitsPerPage;

			end = (int) Math.min(numTotalHits, end);
			// int perPage = end - from;

			for (int i = from; i < end; i++) {

				Record record;
				try {
					record = cache.getRecords().get(i);
					String time = record.getTime();
					// BigInteger _bi = new BigInteger(time, 36); //
					// System.out.println(_bi.toString(10));
					//
					// time = _bi.toString(10);

					String eventFile = record.getEventFile();
					String chunkPos = record.getChunkPos();
					String tokens[] = chunkPos.split(",");
					chunkPos = tokens[0];
					String innerPos = tokens[1];
					BigInteger _bichunkPos = new BigInteger(chunkPos, 36); // System.out.println(_bi.toString(10));
					BigInteger _biinnerPos = new BigInteger(innerPos, 36);
					int chunk = Integer.parseInt(_bichunkPos.toString(10));
					int inner = Integer.parseInt(_biinnerPos.toString(10));
					ArchiveFileReader reader = new ArchiveFileReader(eventFile);
					Event event = reader.read(chunk, inner);
					reader.close();

					Map log = event.getMap();

					if (log != null) {
						Map resultLog = new HashMap();
						for (String key : keys) {
							Object obj = log.get(key);
							if (obj != null)
								resultLog.put(key, obj);
							else
								resultLog.put(key, DataConstants.NULL);
						}
						//把 AGT_RECEIPT_TIME信息带上，供计算雷达图使用
						resultLog.put("AGT_RECEIPT_TIME", log.get("AGT_RECEIPT_TIME"));
						resultLog.put(DataConstants.UUID, log.get(DataConstants.UUID));
						resultLog.put("EVENT_FILE", eventFile);
						maps.add(resultLog);
					}
					String msg = event.toOriginalString();
					logs.add(new SearchResult(time, msg, event.getHost()));
					event = null;
				} catch (CorruptIndexException e) {
	
					// e.printStackTrace();
					result.setException(new LogSearchException(e));
					break;
				} catch (IOException e) {
	
					// e.printStackTrace();
					result.setException(new LogSearchException(e));
					break;
				}

			}
			sw.stop();
			result.setLapTime(sw.getLapTime());
		}
		return result;
	}

	/**
	 * ' exportLogs 导出所有日志
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("exportLogs")
	@ResponseBody
	public Object exportLogs(HttpServletRequest request) throws Exception {

		String[] conditionNames = request.getParameter("conditionName").split(
				",");
		String[] operators = new String(request.getParameter("operator")
				.getBytes("ISO8859-1"), "utf-8").split(",");
		String[] queryContents = new String(request
				.getParameter("queryContent").getBytes("ISO8859-1"), "utf-8")
				.split(",");
		String[] types = request.getParameter("queryType").split(",");
		boolean format = false;
		boolean splitDate = false;
		String sformat = request.getParameter("sformat");
		String zipLogName = request.getParameter("zipLogName");
		String dvcIp = request.getParameter("dvcIp");
		String group = new String(request.getParameter("group").getBytes(
				"ISO8859-1"), "utf-8");
		JSONObject json = new JSONObject();
		try {
			if (sformat != null) {
				format = Boolean.valueOf(sformat);
			}
			LogExportResult exportResult = null;
			try {

				AbstractCondition scon = new AdvancedCondition(conditionNames,
						queryContents, operators, types);
				exportResult = exportHistory(zipLogName, scon, group,
						100000, format,
						splitDate, dvcIp);

			} catch (Exception e) {
				e.printStackTrace();
				json.put("exception", "导出信息异常：" + e.getMessage());
			}
			if (exportResult.getException() != null) {
				json.put("exception", "导出信息异常!");
			} else {
				json.put("ftpfilepath", exportResult.getFtpPathFile());
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return json;
	}

	@SuppressWarnings("unchecked")
	private LogExportResult exportHistory(String zipFile,
			AbstractCondition query, String group, int max, boolean format,
			boolean splitDate, String dvcIp) throws LogSearchException,
			UnindexException {
		LogExportResult exportResult = new LogExportResult();
		zipFile = zipFile + ".zip";
		zipFile = System.getProperty("java.io.tmpdir") + File.separator
				+ zipFile;
		File uploadFile = new File(zipFile);
		int maxCount = max;
		if (maxCount <= 0) {
			maxCount = SearchLimit;
		}
		boolean hasHead = false;

		String cacheKey = new StringBuilder(256).append(query).append(group)
				.toString();
		HistorySearchCacheItem cache = (HistorySearchCacheItem) caches
				.get(cacheKey);
		if (cache == null) {
			exportResult.setException(new LogSearchException(
					"There are no logs file between ",
					LogSearchException.NO_LOGS));
			return exportResult;

		}
		cache.setVisitTime(System.currentTimeMillis());

		String delimiter = System.getProperty("line.separator");

		ZipOutputStream zipOut = null;
		WritableWorkbook book = null;
		try {
			zipOut = new ZipOutputStream(new FileOutputStream(uploadFile));
			ZipEntry ze = new ZipEntry(zipFile.replaceFirst(".zip", ".xls")
					.substring(zipFile.lastIndexOf(File.separator) + 1));
			zipOut.putNextEntry(ze);

			// java excel api
			// open file
			book = Workbook.createWorkbook(zipOut);
			WritableSheet front = book.createSheet("封面", 0);
			WritableFont font = new WritableFont(WritableFont.createFont("楷体"));
			font.setPointSize(36);
			font.setBoldStyle(WritableFont.BOLD);
			font.setColour(Colour.RED);
			WritableCellFormat cellFormat = new WritableCellFormat(font);
			cellFormat.setAlignment(Alignment.CENTRE);
			cellFormat.setBackground(Colour.GRAY_25);
			cellFormat.setWrap(true);

			cellFormat.setBorder(Border.ALL, BorderLineStyle.MEDIUM);
			cellFormat.setLocked(true);
			cellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);

			Label title = new Label(0, 0, dvcIp + "\012日志查询结果导出报告", cellFormat);
			front.addCell(title);
			front.mergeCells(0, 0, 10, 20);
			front.getSettings().setHidden(false);
			front.getSettings().setSelected(true);

			// create Sheet with name "导出日志"
			WritableSheet sheet = book.createSheet("1", 1);
			sheet.getSettings().setHidden(false);
			sheet.getSettings().setDefaultColumnWidth(20);

			long numTotalHits = cache.getRecords().size();

			boolean exceed_max = false;
			int end = (int) Math.min(numTotalHits, maxCount);
			if (numTotalHits > maxCount) {
				exceed_max = true;
			}
			if (exceed_max) {
				title.setString(dvcIp + "\012日志查询结果导出报告\012最多允许导出" + end
						+ "条日志");
			} else {
				title.setString(dvcIp + "\012日志查询结果导出报告\012共计" + end + "条日志");

			}
			int count = 1;
			List<String> keys = null;

			int m = 1;
			int page = 1;
			for (int idx = 0; idx < end; idx++, m++) {
				if (page == 1) {
					if (m % 1001 == 0) {
						// book.write();

						// int page = m/10000;
						page++;
						sheet = book.createSheet("" + page, page);
						// sheet.setColumnGroup(0, i-1, false);
						sheet.setColumnView(0, 20);
						// sheet.setName(new
						// StringBuilder("").append(page+1).toString());
						sheet.getSettings().setHidden(false);
						// sheet.getSettings().setSelected(true);
						sheet.getSettings().setDefaultColumnWidth(20);
						m = 0;

					}

				} else {
					if (m % 1000 == 0) {
						// book.write();

						// int page = m/10000;
						page++;
						sheet = book.createSheet("" + page, page);
						// sheet.setColumnGroup(0, i-1, false);
						sheet.setColumnView(0, 20);
						// sheet.setName(new
						// StringBuilder("").append(page+1).toString());
						sheet.getSettings().setHidden(false);
						// sheet.getSettings().setSelected(true);
						sheet.getSettings().setDefaultColumnWidth(20);
						m = 0;

					}
				}
				Record record;
				try {
					record = cache.getRecords().get(idx);

					String eventFile = record.getEventFile();
					String chunkPos = record.getChunkPos();

					String[] tokens = chunkPos.split(",");
					chunkPos = tokens[0];
					String innerPos = tokens[1];
					if (eventFile != null) {
						// int chunk = Integer.parseInt(chunkPos);
						// int inner = Integer.parseInt(innerPos);
						int chunk = Integer.parseInt(new BigInteger(chunkPos,
								36).toString(10));
						int inner = Integer.parseInt(new BigInteger(innerPos,
								36).toString(10));
						if ((chunk > 0) && (inner >= 0)) {
							ArchiveFileReader reader = new ArchiveFileReader(
									eventFile);

							Event event = reader.read(chunk, inner);
							reader.close();
							if ((format) && (!(hasHead))) {
								hasHead = true;
								keys = IndexTemplateUtil.getInstance()
										.getVisiableGroupField(event.getType(),
												group);

								if (keys != null) {
									// StringBuilder sb = new StringBuilder();
									// //
									// sb.append("序号").append(DataConstants.TAB);
									WritableFont fontHeader = new WritableFont(
											WritableFont.createFont("微软雅黑"));
									fontHeader.setBoldStyle(WritableFont.BOLD);
									fontHeader.setColour(Colour.RED);
									WritableCellFormat cellFormatHeader = new WritableCellFormat(
											fontHeader);
									cellFormatHeader
											.setAlignment(Alignment.CENTRE);
									cellFormatHeader
											.setBackground(Colour.BLUE2);

									cellFormatHeader.setBorder(Border.ALL,
											BorderLineStyle.MEDIUM);
									cellFormatHeader.setLocked(true);
									cellFormatHeader
											.setVerticalAlignment(VerticalAlignment.CENTRE);
									int i = 0;
									for (String key : keys) {
										String field = IndexTemplateUtil
												.getInstance().getFieldType(
														event.getType(), key);
										if (field != null) {
											if ((field.equalsIgnoreCase("Date"))
													&& (splitDate)) {
												// sb.append("日期").append(DataConstants.TAB).append("时间").append(DataConstants.TAB);
												Label label = new Label(i++, 0,
														"日期", cellFormatHeader);
												sheet.addCell(label);
												label = new Label(i++, 0, "时间",
														cellFormatHeader);
												sheet.addCell(label);
											} else {
												// sb.append(IndexTemplateUtil.getInstance().getFieldAlias(event.getType(),
												// key)).append(DataConstants.TAB);
												Label label = new Label(
														i++,
														0,
														IndexTemplateUtil
																.getInstance()
																.getFieldAlias(
																		event.getType(),
																		key),
														cellFormatHeader);
												sheet.addCell(label);
											}
										}
									}
									sheet.setColumnView(0, 20);
									// if (sb.length() > 0) {
									// zipOut.write(sb.toString().getBytes());
									// zipOut.write(delimiter.getBytes());
									// }
								}
							}
							if (format) {
								if (keys != null) {
									Map log = event.getMap();
									if (log != null) {
										if (log.get("START_TIME") == null) {
											log.put("START_TIME", new Date(
													event.getTime()));
										}
										// StringBuilder sb = new
										// StringBuilder();
										// //
										// sb.append(count++).append(DataConstants.TAB);
										int n = 0;
										for (String key : keys) {
											Object value = log.get(key);
											if (value != null) {
												if (value instanceof Date) {
													if (splitDate) {
														synchronized (this.formatYMD) {
															// sb.append(this.formatYMD.format(value)).append(DataConstants.TAB);
															Label label = new Label(
																	n++,
																	m,
																	this.formatYMD
																			.format(value));
															sheet.addCell(label);
														}

														synchronized (this.formatHMS) {
															// sb.append(this.formatHMS.format(value)).append(DataConstants.TAB);
															Label label = new Label(
																	n++,
																	m,
																	this.formatHMS
																			.format(value));
															sheet.addCell(label);
														}

													} else {
														synchronized (this.formatYMDHMS) {
															// sb.append(this.formatYMDHMS.format(value)).append(DataConstants.TAB);
															Label label = new Label(
																	n++,
																	m,
																	this.formatYMDHMS
																			.format(value));
															sheet.addCell(label);
														}

													}

												} else if (key
														.equals("PRIORITY")) {
													if ((Integer) value == 4) {
														// sb.append(DataConstants.PRIORITY_FOUR)
														// .append(DataConstants.TAB);
														Label label = new Label(
																n++,
																m,
																DataConstants.PRIORITY_FOUR);
														sheet.addCell(label);
													} else if ((Integer) value == 3) {
														// sb.append(DataConstants.PRIORITY_THREE)
														// .append(DataConstants.TAB);
														Label label = new Label(
																n++,
																m,
																DataConstants.PRIORITY_THREE);
														sheet.addCell(label);
													} else if ((Integer) value == 2) {
														// sb.append(DataConstants.PRIORITY_TWO)
														// .append(DataConstants.TAB);
														Label label = new Label(
																n++,
																m,
																DataConstants.PRIORITY_TWO);
														sheet.addCell(label);
													} else if ((Integer) value == 1) {
														// sb.append(DataConstants.PRIORITY_ONE)
														// .append(DataConstants.TAB);
														Label label = new Label(
																n++,
																m,
																DataConstants.PRIORITY_ONE);
														sheet.addCell(label);
													} else if ((Integer) value == 0) {
														// sb.append(DataConstants.PRIORITY_ZERO)
														// .append(DataConstants.TAB);
														Label label = new Label(
																n++,
																m,
																DataConstants.PRIORITY_ZERO);
														sheet.addCell(label);
													} else {
														// sb.append(DataConstants.PRIORITY_UNKNOWN)
														// .append(DataConstants.TAB);
														Label label = new Label(
																n++,
																m,
																DataConstants.PRIORITY_UNKNOWN);
														sheet.addCell(label);
													}

												} else if (value instanceof String) {
													// sb.append(((String)value).replaceAll("\r",
													// " ").replaceAll("\n",
													// " ").replaceAll(DataConstants.TAB,
													// " ")).append(DataConstants.TAB);
													Label label = new Label(
															n++, m,
															(String) value);
													sheet.addCell(label);
												} else {
													// sb.append(value).append(DataConstants.TAB);
													Label label = new Label(
															n++, m,
															value.toString());
													sheet.addCell(label);
												}
											} else {
												// sb.append(" ").append(DataConstants.TAB);
												Label label = new Label(n++, m,
														"");
												sheet.addCell(label);
											}
										}
										// if (sb.length() > 0) {
										// zipOut.write(sb.toString().getBytes());
										// zipOut.write(delimiter.getBytes());
										// }
									}
								}
							} else {
								// zipOut.write(new StringBuilder()
								// .append(count++).append(DataConstants.TAB)
								// .toString().getBytes());
								// zipOut.write(event.toOriginalString().replaceAll("\r",
								// " ").replaceAll("\n",
								// " ").replaceAll(DataConstants.TAB,
								// " ").getBytes());
								// zipOut.write(delimiter.getBytes());
								Label label = new Label(0, m,
										event.toOriginalString());
								sheet.addCell(label);
							}
						}
					}

				} catch (CorruptIndexException e) {
					e.printStackTrace();
					throw new LogSearchException(e);
				} catch (IOException e) {
					e.printStackTrace();
					throw new LogSearchException(e);
				}
			}

		} catch (RowsExceededException rex) {
			log.warn(rex.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (book != null) {
					try {
						book.write();
						book.setProtected(true);
						book.close();
					} catch (Exception e) {
		
						e.printStackTrace();
					}
				}
				if (zipOut != null)
					zipOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// FTP 信息
		Map<String, Object> ftpmap = FtpConfigUtil.getInstance()
				.getFTPConfigByKey("log");
		try {
			String uploadName = uploadFile.getName();
			boolean result = FtpUploadUtil.uploadFile(
					(String) ftpmap.get("host"),
					Integer.parseInt((String) ftpmap.get("port")),
					(String) ftpmap.get("user"),
					(String) ftpmap.get("password"),
					(String) ftpmap.get("encoding"), ".", uploadName,
					new FileInputStream(uploadFile));

			FileUtils.deleteQuietly(uploadFile);
			if (result) {
				exportResult.setFtpPathFile(uploadName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return exportResult;
	}

	/**
	 * downloadFile 下载文件
	 * 
	 * @author zhou_xiaohu@topsec.com.cn
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("downloadFile")
	@ResponseBody
	public Object downloadFile(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		JSONObject json = new JSONObject();
		String filename = (String) request.getParameter("filename");
		response.setHeader("Content-Type", "application/octet-stream");
		response.setHeader("Content-Type", "text/html;charset=UTF-8");
		response.setContentType("text/html;charset=UTF-8");
		response.setHeader("Content-Disposition", "inline; filename="
				+ filename);
		String serverHome = System.getProperty("jboss.server.home.dir");
		String savaLogPathfile = new StringBuilder(serverHome)
				.append(File.separator).append("ftphome")
				.append(File.separator).append("log").append(File.separator)
				.append(filename).toString();

		File file = new File(savaLogPathfile);
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(file));
			bos = new BufferedOutputStream(response.getOutputStream());
			byte[] buff = new byte[2048];
			int bytesRead;
			while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
				bos.write(buff, 0, bytesRead);
			}
		} catch (Exception e) {
			log.error("下载文件出错....", e);
			System.out.println("下载文件出错....");
			e.printStackTrace();
			json.put("errorInfor", "下载文件出错:" + e.getMessage());
			return json;
		} finally {
			try {
				if (bis != null)
					bis.close();
				if (bos != null)
					bos.close();
			} catch (Exception ex) {
				System.out.println("关闭流失败");
			}
		}
		return json;
	}
	//更新查询结果字段统计
		private LogRecordSet statSearchKey(SearchObject obj,SID sid)
		{
			//测试查询结果统计计算需要的时间
			StopWatch sw = new StopWatch();
			sw.start();
			
			LogRecordSet result = new LogRecordSet();
			
			List<String> statColumns = obj.getStatColumns();
			if(statColumns.size()!=1){
				result.setFinished(true);
				result.setStats(new HashMap<String, Object>());
				System.out.println("错误的统计参数。。。");
				sw.stop();
				return result;
			}
			
			final Date startDate = obj.getStart();
			final String type = obj.getType();
			final String host = obj.getHost();
			final AbstractCondition query = new   AdvancedCondition(obj.getConditionNames(),
					obj.getQueryContents(), obj.getOperators(),obj.getQueryTypes());
			final String group = obj.getGroup();

			Calendar calendar = new GregorianCalendar();
			calendar.setTime(obj.getEnd());
			calendar.set(Calendar.MILLISECOND, 999);
			final Date endDate = calendar.getTime();

			AbstractCondition scon = new AdvancedCondition(obj.getConditionNames(),
					obj.getQueryContents(), obj.getOperators(), obj.getQueryTypes());
			final StringBuilder cacheKey = new StringBuilder(256).append(scon).append(obj.getGroup()).append(sid.getAccountID());
			HistorySearchCacheItem cache = null;

			if(caches.get(cacheKey.toString())==null){
				if(obj.isCancel()){
					result.setFinished(true);
					result.setStats(new HashMap<String, Object>());
					System.out.println("取消查询。。。");
					sw.stop();
					return result;
				}else
				{
					StringBuilder sb = new StringBuilder();
					sb.append("收到统计命令[").append(type).append("]");
					if(host != null){
						sb.append("(").append(host).append(")");
					}
					if(!query.isSearchAll()){
						sb.append(query.toReadableString());
					}
					log.info(sb.toString());

				}
			}

			
			try {
				cache = (HistorySearchCacheItem) caches.get(cacheKey.toString());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				result.setException(e1);
				result.setStats(new HashMap<String, Object>());
				sw.stop();
				return result;
			}
			int waitCount = 0 ;
			while(!cache.isDone()&&waitCount++<10){
				log.warn("正在等待查询结果结束！");
				try {
					Thread.sleep(1000) ;
				} catch (InterruptedException e) {
					e.printStackTrace();
					waitCount = 100 ;
				}
			}
			if(!cache.isDone()){
				result.setException(new LogSearchException("查询统计超时！")) ;
			}
			//停止查询
			cache.setCancel(obj.isCancel());
			cache.setVisitTime(System.currentTimeMillis());
			
			Map<String, Object> stats = new LinkedHashMap<String, Object>();
			
		    StatisticMeta meta = new StatisticMeta();
		    
		    DataBuilder builder;
			Map<String, String> functions = new HashMap<String, String>();
			functions.put("COUNT", "com.topsec.tsm.framework.util.dataable.function.Count");
			builder = new DataBuilder(functions);
		    
			//要统计的字段
		    FieldMeta field1 = new FieldMeta(statColumns.get(0),statColumns.get(0));
		    //次数累积
		    FieldMeta field2 = new FieldMeta("OPCOUNT","COUNT("+statColumns.get(0)+")");
		    
		    FieldsMeta fields = new FieldsMeta();
		    fields.addField(field1);
		    fields.addField(field2);        
		    meta.setFields(fields);
		
		    //归并后的字段
		    FieldMeta fgroup = new FieldMeta(statColumns.get(0),statColumns.get(0));
		    GroupsMeta groups = new GroupsMeta();
		    groups.addField(fgroup);
		    meta.setGroups(groups);
		    
		    //排序字段
		    OrdersMeta orders = new OrdersMeta();
		    OrderMeta order = new OrderMeta("OPCOUNT");
		    order.setOrder("DESC");
		    orders.addField(order);
		    meta.setOrders(orders);
		    
		    Statistic statistic;
		    try {
				statistic = new Statistic(meta, _typedef, builder);
				//归并结果
				int temp = 1;
				String eventFile = null;
				for(Record record: cache.getRecords()){
					try {
						//打开文件，还原记录				
						eventFile = record.getEventFile();
						String chunkPos = record.getChunkPos();
						String tokens[] = chunkPos.split(",");
						chunkPos = tokens[0];
						String innerPos = tokens[1];
						BigInteger _bichunkPos = new BigInteger(chunkPos, 36); // System.out.println(_bi.toString(10));
						BigInteger _biinnerPos = new BigInteger(innerPos, 36);
						int chunk = Integer.parseInt(_bichunkPos.toString(10));
						int inner = Integer.parseInt(_biinnerPos.toString(10));
						//缓存打开，超时关闭
						ArchiveFileReader reader = ArchiveFileReaderHelper.open(eventFile);
						if(reader == null){
							continue;
						}
						Event event = reader.read(chunk, inner);
						if(event == null) {
							log.warn("日志文件"+eventFile+","+chunk+","+inner+"位置数据提取失败");
							continue;
						}
						Map log = event.getMap();
						statistic.work(log);
					}catch(FileNotFoundException e){
						log.warn("日志文件{}已被删除",record.getEventFile()) ;
					} catch (Exception e) {
						e.printStackTrace() ;
					}
				}
				StatisticResult stat = statistic.getResult(false);
				List<Map<String, Object>> data = stat.getMapRecords();
	        	for(Map<String, Object> event:data) {
	        		stats.put(StringUtil.toString(event.get(statColumns.get(0))), event.get("OPCOUNT"));
	        	}
	        	
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				ArchiveFileReaderHelper.clearCache() ;
			}
			result.setStats(stats);
			sw.stop();
			log.info("结果统计耗时："+sw.getLapTime()+"毫秒。");
		    return result;
		}
	
	
	//更新查询结果时间分布
	Map<String, Object> statResult(HistorySearchCacheItem cache,LogSearchObject obj) throws ParseException
	{
		//测试时间轴计算需要的时间
//		StopWatch sw = new StopWatch();
//		sw.start();
		
		Map<String, Object> stats = new HashMap<String, Object>();
		
	    StatisticMeta meta = new StatisticMeta();
	    
	    DataBuilder builder;
		Map<String, String> functions = new HashMap<String, String>();
		functions.put("TIMETRUNCATE", "com.topsec.tsm.framework.util.dataable.function.TimeTruncate");
		functions.put("COUNT", "com.topsec.tsm.framework.util.dataable.function.Count");
		builder = new DataBuilder(functions);
	    
		Date st =DateUtils.parseDate(obj.getQueryStartDate());
		Date et = DateUtils.parseDate(obj.getQueryEndDate());
		//最小1分钟一个刻度
		//与前台计算方法一致，前台图形从右向左显示
		long span = ((et.getTime()-st.getTime())/60000);
		int type = 1;
		if(span<1)
			span = 1;
		if(span/(24*60)>=60){
			span = 1;
			type=3;
		}
		else if(span/(12*60)>=60){
			span = 12;
			type=2;
		}
		else if(span/(6*60)>=60){
			span = 6;
			type=2;
		}
		else if(span/(60)>=60){
			span = 1;
			type=2;
		}
		else if(span/30>=60){
			span = 30;
			type=1;
		}
		else if(span/10>=60){
			span = 10;
			type=1;
		}
		else if(span/5>=60){
			span = 5;
			type=1;
		}
		else{
			span = 1;
			type=1;
		}
		
		StringBuilder sb = new StringBuilder("TIMETRUNCATE(AGT_RECEIPT_TIME,");
		sb.append(type).append(",").append(span).append(")");
	    //归并后的接收时间字段
	    FieldMeta field1 = new FieldMeta("AGT_RECEIPT_TIME",sb.toString());
	    //次数累积
	    FieldMeta field2 = new FieldMeta("OPCOUNT","COUNT(AGT_RECEIPT_TIME)");
	    
	    FieldsMeta fields = new FieldsMeta();
	    fields.addField(field1);
	    fields.addField(field2);        
	    meta.setFields(fields);
	
	    //归并后的接收时间字段
	    FieldMeta fgroup = new FieldMeta("AGT_RECEIPT_TIME",sb.toString());
	    GroupsMeta groups = new GroupsMeta();
	    groups.addField(fgroup);
	    meta.setGroups(groups);
	    
	    Statistic statistic;
	    try {
			statistic = new Statistic(meta, _typedef, builder);
			//归并结果
			Map<String, Object> tmp = new HashMap<String, Object>();
			int temp = 1;
			for(Record record: cache.getRecords()){
				tmp.put("AGT_RECEIPT_TIME", new Date(Long.valueOf(record.getTime())));
				
				statistic.work(tmp);
				tmp.clear();
			}
			StatisticResult stat = statistic.getResult(false);
			List<Map<String, Object>> data = stat.getMapRecords();
        	for(Map<String, Object> event:data) {
        		stats.put(StringUtil.longDateString((Date)event.get("AGT_RECEIPT_TIME")), event.get("OPCOUNT"));
        	}

		} catch (StatisticException e) {

			e.printStackTrace();
		}
//		sw.stop();
//		logger.info("时间轴计算耗时："+sw.getLapTime()+"毫秒。");
	    return stats;
	}
	/**
	 * doLogFieldStatic 获取日志查询结果
	 * @author zhou_xiaohu@topsec.com.cn
	 * @return
	 */
	
	@RequestMapping("doLogFieldStatic")
	@ResponseBody
	public Object doLogFieldStatic(SID sid,@RequestBody LogSearchObject logSearchObject,HttpServletRequest request,HttpSession session) {
		JSONObject jsonObject=new JSONObject();
		try {
			jsonObject=logFieldStatic(logSearchObject,sid);
			session.setAttribute("condition", logSearchObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
	public JSONObject logFieldStatic(LogSearchObject logSearchObject,SID sid){
		SearchObject searchObject = new SearchObject();
		searchObject.setStart(StringUtil.toDate(logSearchObject.getQueryStartDate()));
		searchObject.setEnd(StringUtil.toDate(logSearchObject.getQueryEndDate()));
		searchObject.setHost(logSearchObject.getHost());
		searchObject.setType(logSearchObject.getDeviceType());
		searchObject.setConditionNames(logSearchObject.getConditionName());
		searchObject.setOperators(logSearchObject.getOperator());
		searchObject.setQueryContents(logSearchObject.getQueryContent());
		searchObject.setQueryTypes( logSearchObject.getQueryType());
		searchObject.setGroup(logSearchObject.getGroup());
		searchObject.setCancel(logSearchObject.isCancel());
		List<String> statColumns = new ArrayList<String>();
		statColumns.add(logSearchObject.getStatColumn());
		searchObject.setStatColumns(statColumns);
		LogRecordSet resultSet = new LogRecordSet();
		resultSet =statSearchKey(searchObject,sid);
		JSONObject jsonObject=new JSONObject();
		if(resultSet.getException() != null){
			jsonObject.put("flag", "failure");
			if(resultSet.getException() instanceof LogSearchException){
				jsonObject.put("error",resultSet.getErrorMessage());
			}else{
				jsonObject.put("error","统计失败!");
			}
			
		}else{
			List<Map<String, String>> columnTypes = IndexTemplateUtil.getInstance().getVisiableGroupColumnTypes(searchObject.getType(),searchObject.getGroup());
			String columnType="";
			for (Map<String, String> column : columnTypes) {
				Map.Entry<String, String> entry = column.entrySet().iterator().next();
				String key = entry.getKey();
				String value = entry.getValue();
                if(logSearchObject.getStatColumn().equals(key)){
                	columnType=value;
                	break;
                }				
			}
		    jsonObject.put("flag", "success");
			Map<String,Object> statResult=resultSet.getStats();

			JSONArray tableJsonArr= new JSONArray() ;
			int count = 1 ;
			String statColumn = searchObject.getStatColumns().get(0) ;
			double totalCount=0;
			for(Map.Entry<String, Object> entry:statResult.entrySet()){  
				 if(count++>20){//如果数据超过20条，剩余的数据都合并为其它
			        	break ;
			        }
				String key=entry.getKey();           
				if(StringUtil.isBlank(key)){
					continue ;
				}
		        String value=StringUtil.toString(entry.getValue()); 
		        totalCount+=StringUtil.toDoubleNum(value);
		        if(statColumn.equalsIgnoreCase("PRIORITY")){
		        	key=CommonUtils.getLevel(key);
		        }else if(statColumn.equalsIgnoreCase("DVC_TYPE")){
		        	key=statColumn.substring(0,statColumn.indexOf("/"));
		        }
		       
		        JSONObject tableJson=new JSONObject();
		        tableJson.put(statColumn,key);
		        tableJson.put("result",value);
		        tableJsonArr.add(tableJson);
			}
			DecimalFormat decimalFormat=new DecimalFormat("0.00");
			for(int i=0;i<tableJsonArr.size();i++){
				 JSONObject tempJson=tableJsonArr.getJSONObject(i);
				 double result=StringUtil.toDoubleNum(tempJson.get("result").toString());
				 double percentResult=result/totalCount*100;
				 tempJson.put("percent",decimalFormat.format(percentResult));
			}
			jsonObject.put("tableData", tableJsonArr);
			jsonObject.put("columnType", columnType);
		}
		return jsonObject;
	}
	/**
	 * 
	 * 导出 日志统计字段结果
	 * 
	 */
	@RequestMapping("exportLogField")
	public void exportLogField(HttpServletRequest request,HttpServletResponse response,SID sid,HttpSession session){
		String exportType = (String)request.getParameter("exportType");
		LogSearchObject logSearchObject = (LogSearchObject)session.getAttribute("condition");
		String statColumn = logSearchObject.getStatColumn();
		String statColumnName="";
		List<Map<String, String>> columnNames = IndexTemplateUtil.getInstance().getVisiableGroupColumnNames(logSearchObject.getDeviceType(),logSearchObject.getGroup());
	
		for (Map<String, String> column : columnNames) {
			Map.Entry<String, String> entry = column.entrySet().iterator().next();
		    if(statColumn.equals(entry.getKey())){
		    	statColumnName=entry.getValue();
		    	break;
		    }				
		}
		logSearchObject.setStatColumn(statColumn);
		
		JSONObject tableDatas=logFieldStatic(logSearchObject, sid);
		final List<String> tableHead = new ArrayList<String>();
		tableHead.add(statColumn);
		tableHead.add(statColumnName);
		tableHead.add("结果");
		tableHead.add("百分比");
//		final String[] tableHead={statColumn,statColumnName,"结果","百分比"};
		String title=statColumnName+"统计结果";
		try {
			CommonUtils.setDownloadHeaders(request, response, statColumnName+"统计"+"."+exportType) ;
			if(exportType.equals("xls")){
				ExportExcelUtil.exportExcel(response, tableHead, tableDatas, new ExportExcelHandler<JSONObject>(){
					@Override
					public void createSheetCell(HSSFSheet tableSheet, JSONObject tableDatas) {
						JSONArray rowData=(JSONArray) tableDatas.get("tableData");
				        for (int i=0, len = rowData.size(); i<len; i++) {
				        	JSONObject record=(JSONObject)rowData.get(i);
					    	  HSSFRow tableRowData = tableSheet.createRow(1+i);
					    	  tableRowData.createCell(0).setCellValue(StringUtil.toString(record.get(tableHead.get(0))));
					    	  tableRowData.createCell(1).setCellValue(StringUtil.toString(record.get("result")));
				    		  tableRowData.createCell(2).setCellValue(StringUtil.toString(record.get("percent"))+"%");
						  }
					}
					
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
	   	}
	}
	/**
	 * 
	 * 导出 日志统计字段结果
	 * 
	 */
	@RequestMapping("exportMapLog")
	public void exportMapLog(HttpServletRequest request,HttpServletResponse response,SID sid,HttpSession session){
		String exportType = (String)request.getParameter("exportType");
		int type = StringUtil.toInteger(request.getParameter("type"), 4);
		LogSearchObject logSearchObject = (LogSearchObject)session.getAttribute("condition");
		List<Map<String, Object>> datas = new ArrayList<Map<String,Object>>();
		try {
			datas = getIpLocationData(logSearchObject, type, sid, session);
		} catch (Exception e1) {
			e1.printStackTrace();
			datas = Collections.emptyList() ;
		}
		
		String statColumn = logSearchObject.getStatColumn();
		String statColumnName="";
		List<Map<String, String>> columnNames = IndexTemplateUtil.getInstance().getVisiableGroupColumnNames(logSearchObject.getDeviceType(),logSearchObject.getGroup());
	
		for (Map<String, String> column : columnNames) {
			Map.Entry<String, String> entry = column.entrySet().iterator().next();
		    if(statColumn.equals(entry.getKey())){
		    	statColumnName=entry.getValue();
		    	break;
		    }				
		}
		logSearchObject.setStatColumn(statColumn);
		JSONObject tableDatas= new JSONObject();
		tableDatas.put("tableData", (JSONArray) FastJsonUtil.toJSONArray(datas, "name="+statColumn, "value=result"));
		final List<String> tableHead = new ArrayList<String>();
		tableHead.add(statColumn);
		tableHead.add(statColumnName);
		tableHead.add("数量");
//		final String[] tableHead={statColumn,statColumnName,"数量"};
		String title=statColumnName+"统计结果";
		try {
			CommonUtils.setDownloadHeaders(request, response, "IP数量分布统计"+"."+exportType) ;
			if(exportType.equals("xls")){
				ExportExcelUtil.exportExcel(response, tableHead, tableDatas, new ExportExcelHandler<JSONObject>(){
					@Override
					public void createSheetCell(HSSFSheet tableSheet, JSONObject tableDatas) {
				        JSONArray rowData=(JSONArray) tableDatas.get("tableData");
				        for (int i=0, len = rowData.size(); i<len; i++) {
				        	JSONObject record=(JSONObject)rowData.get(i);
					    	  HSSFRow tableRowData = tableSheet.createRow(1+i);
					    	  tableRowData.createCell(0).setCellValue(StringUtil.toString(record.get(tableHead.get(0))));
					    	  tableRowData.createCell(1).setCellValue(StringUtil.toString(record.get("result")));
						  }
					}
					
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
	   	}
	}
	public static void clearCache() {
		caches.clear();
	}

	@Autowired
	public void setNodeMgr(NodeMgrFacade nodeMgr) {
		this.nodeMgrFacade = nodeMgr;
	}

	@Autowired
	public void setEventResponseService(
			EventResponseService eventResponseService) {
		this.eventResponseService = eventResponseService;
	}
}
