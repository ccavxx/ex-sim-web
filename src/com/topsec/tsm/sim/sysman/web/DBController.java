package com.topsec.tsm.sim.sysman.web;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.util.config.Block;
import com.topsec.tal.base.util.config.Config;
import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.framework.exceptions.I18NException;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.node.component.service.DbManagerObject;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.resource.SystemDefinition;
import com.topsec.tsm.sim.database.def.DBPartition;
import com.topsec.tsm.sim.database.def.DBPartitionDetail;
import com.topsec.tsm.sim.database.util.DBConstant;
import com.topsec.tsm.sim.resource.object.OrderAttrib;
import com.topsec.tsm.sim.resource.object.PageAttrib;
import com.topsec.tsm.sim.response.persistence.Response;
import com.topsec.tsm.sim.util.NodeUtil;
import com.topsec.tsm.sim.util.RouteUtils;
import com.topsec.tsm.tal.response.base.RespCfgHelper;
import com.topsec.tsm.tal.service.EventResponseService;
import com.topsec.tsm.util.net.FtpUploadUtil;

@Controller
@RequestMapping("database")
public class DBController {
	protected static Logger log= LoggerFactory.getLogger(DBController.class);
	
	
	
	protected final static String MONTH="month";
	protected final static String YEAR="year";
	protected final static String WEEK="week";
	protected final static String DAY="day";
	protected final static String WHOLE_YEAR="wyear";
	
	
	@Autowired
	private EventResponseService eventResponseService;
	
	
	@RequestMapping("partitionsByTime")
	@ResponseBody
	public Object getPartitionsByTime(
			 @RequestParam(value="date")@DateTimeFormat(pattern="yyyyMMdd")Date datetime,
			 @RequestParam(value="scope")String scopeType){
		List<Object> partitions = new ArrayList<Object>();
		
		DbManagerObject managerObject = new DbManagerObject();
		Date beginDate ;
		if(scopeType.equals(MONTH)){//月
			beginDate = ObjectUtils.addMonths(datetime,-1) ;
		}else if(scopeType.equals(WEEK)){//周  一周时间长度范围
			beginDate = ObjectUtils.addDays(datetime, -7) ;
		}else if(scopeType.equals(DAY)){//天
			beginDate = ObjectUtils.addDays(datetime,-1) ;
		}else{
			return partitions ;
		}
		
		String beginDateString = StringUtil.dateToString(beginDate,"yyyyMMdd") ;
		String endDateString = StringUtil.dateToString(datetime,"yyyyMMdd") ;
		managerObject.setStartdate(beginDateString);
		managerObject.setEnddate(endDateString);
		OrderAttrib orderattrib = new OrderAttrib();
		orderattrib.setOrderAsc(OrderAttrib.ASC);
		managerObject.setOrderAttrib(orderattrib);
		List<DBPartition> resultList = queryDBPartions(managerObject); 
		//从开始日期一直累加，直到与结束日期相等
		while(!endDateString.equals(beginDateString)){
			DBPartitionDetail detail = new DBPartitionDetail();
			beginDate = ObjectUtils.addDay(beginDate) ;
			beginDateString = StringUtil.dateToString(beginDate,"yyyyMMdd");
			for(DBPartition dbp: resultList){
				if(dbp.getPartitionName().endsWith(beginDateString)){
					detail.setPartitionName(beginDateString);
					detail.setEventCount(dbp.getEventCount());
					break;
				}
			}
			if(detail.getPartitionName() == null){
				detail.setPartitionName(beginDateString);
				detail.setEventCount(0);
			}
			partitions.add(detail);
		}
		return partitions;
	}



	private List<DBPartition> queryDBPartions(DbManagerObject managerObject) {
		String[] route= RouteUtils.getRoute();
		Map<String, Object> map=null;
		try {
			map = (Map<String, Object>) NodeUtil.getCommandDispatcher().dispatchCommand(route, MessageDefinition.CMD_SEARCH_EVENTNUM,  (Serializable)managerObject, 2*60*1000);
			List<DBPartition> resultList = (List<DBPartition>) map.get(DBConstant.KEY_LIST);
			return resultList;
		} catch (CommunicationException e) {
			log.warn("事件分区查询超时!") ;
			return null ;
		}
	}
	
	
	protected final static String THIS_MONTH="this_month";
	protected final static String THIS_WEEK="this_week";
	protected final static String LAST_WEEK="last_week";
	protected final static String ALL="all";
	
	
	@RequestMapping(value="partitionPage")
	@ResponseBody
	public Object  getPartitionPages(
					 @RequestParam(value="manageScope",required=false,defaultValue=THIS_WEEK)String mScopeType,
					 @RequestParam(required=false,defaultValue="1",value="page")int pageIndex,
					 @RequestParam(required=false,defaultValue="10",value="rows")int pageSize
			){
		
			String minTime="";
			String maxTime="";
			String dataPattern="yyyyMMdd";
			
			
			Calendar cal = Calendar.getInstance();
			if(mScopeType.equals(THIS_WEEK)){//本周
			
				int day = cal.get(Calendar.DAY_OF_WEEK) - 1;
				if (day == 0)
					day = 7;
				cal.add(Calendar.DAY_OF_WEEK, 1 - day);
				minTime=DateFormatUtils.format(cal, dataPattern);
				maxTime=DateFormatUtils.format(System.currentTimeMillis(), dataPattern);
			}
			
			if(mScopeType.equals(LAST_WEEK)){//上周
 
				int day = cal.get(Calendar.DAY_OF_WEEK) - 1;
				if (day == 0)
					day = 7;
				cal.add(Calendar.DAY_OF_WEEK, 1 - day);
				cal.add(Calendar.DAY_OF_WEEK, -7);
				minTime=DateFormatUtils.format(cal, dataPattern);
				cal.add(Calendar.DAY_OF_WEEK, 6);
				maxTime=DateFormatUtils.format(cal, dataPattern);
			}
			
			
			if (mScopeType.equals(THIS_MONTH)) {//本月
				int day = cal.get(Calendar.DAY_OF_MONTH);
				cal.add(Calendar.DAY_OF_MONTH, -day + 1);
				minTime=DateFormatUtils.format(cal, dataPattern);
				cal.add(Calendar.MONTH, 1);
				maxTime=DateFormatUtils.format(System.currentTimeMillis(), dataPattern);
			}
			
			
			if(mScopeType.equals(ALL)){
				maxTime=DateFormatUtils.format(System.currentTimeMillis(), dataPattern);
			}//全部
			DbManagerObject managerObject = new DbManagerObject();
		
			managerObject.setStartdate(minTime);
			
			managerObject.setEnddate(maxTime);
			PageAttrib pageAttrib = new PageAttrib();
			pageAttrib.setPageSize(pageSize);
			pageAttrib.setPageNo(pageIndex);
			OrderAttrib orAttrib = new OrderAttrib();
			orAttrib.setOrderAsc(OrderAttrib.DESC);
			managerObject.setOrderAttrib(orAttrib);
			managerObject.setPageAttrib(pageAttrib);

			
			Map<String, Object> mapResult = searchPartations(managerObject);
			
			
			String resultTotal =   mapResult.get(DBConstant.RESULT_TATAL).toString();
			
			int recordTotal=0;
			if(NumberUtils.isNumber(resultTotal)){
				recordTotal=NumberUtils.toInt(resultTotal);
			}
			List<DBPartition> listResult =new ArrayList<DBPartition>();
			if(recordTotal!=0){
			   listResult = (List<DBPartition>) mapResult.get(DBConstant.KEY_LIST);
			}
			
			
			Map<String,Object> packPtMap=new HashMap<String,Object>();
			
			
			List<Map<String,Object>>  ptMapList=new ArrayList<Map<String,Object>>();
			for (DBPartition partition : listResult) {
				
				Map<String,Object> ptMap=new HashMap<String, Object>();
				ptMap.put("partitionName", partition.getPartitionName().replace("P", ""));
				ptMap.put("eventCount", partition.getEventCount());
				ptMap.put("importFlag", partition.getImportFlag());
				ptMap.put("exportFlag", partition.getExportFlag());
				ptMapList.add(ptMap);
			}
			
			
			packPtMap.put("total", recordTotal);
			packPtMap.put("rows", ptMapList);
			return packPtMap;
		
	}



	private Map<String, Object> searchPartations(DbManagerObject managerObject) {
		Map<String, Object> mapResult =null;
		String[] route = RouteUtils.getRoute();
		try {
			mapResult = (Map<String, Object>) NodeUtil.getCommandDispatcher().dispatchCommand(route, MessageDefinition.CMD_SEARCH_PARTATION,  (Serializable)managerObject, 2*60*1000);
		} catch(Exception e) {
			if (e instanceof com.topsec.tsm.comm.CommunicationExpirationException){
				//request.setAttribute("exceptionInfo", "请求超时!");
			}
			log.debug("Send message error!");
			e.printStackTrace();
		}
		
		
		return mapResult;
	}


	
	
	@RequestMapping(value="exportPartitions",method=RequestMethod.POST)
	@ResponseBody
	public String getExportPartations(@RequestParam(value="pnames[]")String[]p){
		
		System.out.println(Arrays.toString(p));
		System.out.println(/*partitionsHolder.getPartitions()*/);
		JSONObject infoJson=new JSONObject();
		if(p!=null){
			List<String> partitionName = new ArrayList<String>();
			for (int i = 0; i < p.length; i++) {
				partitionName.add(p[i]);
			}
			
			DbManagerObject managerObject = new DbManagerObject();
			managerObject.setDbpartionnames(partitionName);
			String[] route=RouteUtils.getRoute();
			try {
				NodeUtil.getCommandDispatcher().sendCommand(route, MessageDefinition.CMD_EXPORT_PARTATION,  (Serializable)managerObject, 2*60*1000);
			} catch (CommunicationException e) {
				log.error(e.getMessage());
				infoJson.put("result", "error");
			}
			infoJson.put("result", "success");
		}
		
		
		
		return infoJson.toJSONString() /*JSONObject.toJSONString(p)*/;
	}
	
	
	@RequestMapping(value="deletePartitions" ,method=RequestMethod.POST)
	@ResponseBody
	public Object deletePartitions(@RequestParam(value="pnames[]")String[]pnames,HttpServletRequest request){
		
		JSONObject  resultJson=new JSONObject();
		resultJson.put("result", -1);
		if(pnames!=null&&pnames.length!=0){
			List<String> partitionNames = new ArrayList<String>();
			for (int i = 0; i < pnames.length; i++) {
				partitionNames.add(pnames[i]);
			}
			log.info("delete-->"+partitionNames);
			resultJson.put("result", 1);
			 
			try {
				DbManagerObject managerObject = new DbManagerObject();
				managerObject.setDbpartionnames(partitionNames);
				NodeUtil.getCommandDispatcher().sendCommand(RouteUtils.getRoute(), MessageDefinition.CMD_DELETE_PARTATION,  (Serializable)managerObject, 2*60*1000);
				Thread.sleep(3000);
			} catch (CommunicationException e) {
				log.error(e.getMessage());
				resultJson.put("result", -1);
			} catch (InterruptedException e) {
				log.error(e.getMessage());
				resultJson.put("result", -1);
			}	
				 
		}
		return resultJson.toJSONString();
	}
	
	/**
	 * 分区备份
	 * @author zhaojun 2014-3-20下午6:27:30
	 * @return
	 */
	@RequestMapping(value="getLocalFiles" ,method=RequestMethod.GET)
	@ResponseBody
	public Object getLocalFiles(){
		return getAllFiles();
	}

	private List<String> getAllFiles() {
		List<Response> responses = eventResponseService.getResponsesByNodeId(NodeDefinition.NODE_TYPE_SMP);
		Config config = null;
		List<String> fileNameList= new ArrayList<String>();
		for(Response res : responses){
			if("sys_cfg_backup".equals(res.getCfgKey())){
				try {
					config =RespCfgHelper.getConfig(res);
				} catch (I18NException e) {
					e.printStackTrace();
				}
			}
		}
		Block backupPath  = config.getBlockbyGroup("backuppath");
		String path ="";
//		boolean isShow = false;
		if("local".equalsIgnoreCase(backupPath.getKey())){
	 			path = backupPath.getItemValue("path")+File.separator+"events"+File.separator;
			if (StringUtil.isNotBlank(path)) {
				if (path.endsWith(":"))
					path = path + File.separator;
				File base = new File(path);
				if (base.exists()&&base.isDirectory()) {
					for (File son : base.listFiles()) {
						if (son.isFile() && son.getName().endsWith(".zip")) {
							fileNameList.add(son.getName());
						}
					}
				}
			}
		}else if("ftp".equalsIgnoreCase(backupPath.getKey())){
			String serverIp = backupPath.getItemValue("serverip");
        	String user = backupPath.getItemValue("user");
        	String password =backupPath.getItemValue("password");
        	String encoding = backupPath.getItemValue("encoding");
        	fileNameList = FtpUploadUtil.getFileNames(serverIp, 21, user, password,encoding, "events",".zip");
        
		}
		
		return fileNameList;
	}
	
	 
	protected static final String ALL_PARTITIONS="all_partitions";
	
	/**
	 * 分区导入
	 * @author zhaojun 2014-3-20下午6:27:08
	 * @param pt
	 * @return
	 */
	@RequestMapping(value="importPartions" ,method=RequestMethod.POST)
	@ResponseBody
	public Object importPartitions(@RequestParam(value="partitions[]",required=true)String[] pt){
		
		List<String> listPartitions= new ArrayList<String>();
		for (int i = 0; i < pt.length; i++) {
			if(pt[i].equals(ALL_PARTITIONS)){
				listPartitions.clear();
				listPartitions.addAll(getAllFiles());
				break;
			}
			listPartitions.add(pt[i]);
		}
		if(log.isDebugEnabled()){
			log.debug(StringUtil.join(listPartitions)); 
		}
	    String[] route=RouteUtils.getRoute();
		DbManagerObject DBObject =new  DbManagerObject();
		DBObject.setDbpartionnames(listPartitions);
	    DBObject.setCommand(MessageDefinition.CMD_IMPORT_PARTATION);
	    DBObject.setBackpath("");
	    JSONObject infoJson=new JSONObject();
	    infoJson.put("result", "success");
	    try {
		    NodeUtil.getCommandDispatcher().sendCommand(route, MessageDefinition.CMD_IMPORT_PARTATION,  (Serializable)DBObject, 2*60*1000);
		} catch(Exception e) {
			infoJson.put("result", "fault");
			e.printStackTrace();
		}
		
		return infoJson.toJSONString();
	}
	
	/**
	 * 数据测试
	 * @author zhaojun 2014-2-18下午6:24:58
	 * @param pageIndex
	 * @param rows
	 * @param mScopeType
	 * @return
	 */
	
	@RequestMapping(value="dbtest",produces="text/javascript;charset=UTF-8")
	@ResponseBody
	public String getPageData(@RequestParam(required=true,defaultValue="1",value="page")int pageIndex,
							  @RequestParam(required=true,defaultValue="10",value="rows")int rows,
							  @RequestParam(value="manageScope",required=false,defaultValue=THIS_WEEK)String mScopeType){
		
		System.out.println("index-->"+pageIndex);
		System.out.println("rows-->"+rows);
		System.out.println("manageScope-->"+mScopeType);
		String json =null;
		try {
			  json = FileUtils.readFileToString(new File(SystemDefinition.DEFAULT_CONF_DIR+"event-test.json"),"utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return json;
	}
	
	
	 
 



	public void setEventResponseService(EventResponseService eventResponseService) {
		this.eventResponseService = eventResponseService;
	}



	public static void main(String[] args) {
		// getPartitions(new Date());
	
		Calendar cl=Calendar.getInstance();
		
		int maxDay=cl.getMaximum(Calendar.DAY_OF_MONTH);
		System.out.println(cl.get(Calendar.DAY_OF_YEAR));	
		int day=cl.get(Calendar.DAY_OF_YEAR);
		cl.set(Calendar.DAY_OF_YEAR, day-maxDay+1);
		
	 
		System.out.println(DateFormatUtils.format(cl, "yyyyMMdd"));
	}
}
