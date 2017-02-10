package com.topsec.tsm.sim.event.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import org.apache.commons.beanutils.BeanPropertyValueEqualsPredicate;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.index.IndexTemplateUtil;
import com.topsec.tal.base.index.IndexTemplateUtil.IndexField;
import com.topsec.tal.base.search.LogSearchException;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.SpringWebUtil;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.ass.InvalidAssetIdException;
import com.topsec.tsm.asset.AssTopo;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.common.formatter.HtmlEscapeFormatter;
import com.topsec.tsm.node.collector.DataConstants;
import com.topsec.tsm.node.collector.support.Map2String;
import com.topsec.tsm.sim.alarm.persistence.SimAlarm;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.AssetObject;
import com.topsec.tsm.sim.asset.AssetUtil;
import com.topsec.tsm.sim.asset.exception.AssetException;
import com.topsec.tsm.sim.asset.exception.InvalidLicenseException;
import com.topsec.tsm.sim.asset.exception.LimitedNumException;
import com.topsec.tsm.sim.asset.service.AssetService;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.asset.service.TopoService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.common.bean.Result;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.event.EventRuleGroup;
import com.topsec.tsm.sim.event.bean.BasicQueryCriteria;
import com.topsec.tsm.sim.event.bean.CategoryLevelParam;
import com.topsec.tsm.sim.event.bean.Condition;
import com.topsec.tsm.sim.event.bean.QueryConditionAdapter;
import com.topsec.tsm.sim.event.service.EventCategoryService;
import com.topsec.tsm.sim.event.service.EventQueryService;
import com.topsec.tsm.sim.event.util.CategoryOrganizationTemplate;
import com.topsec.tsm.sim.event.util.CategoryOrganizationTemplate.AbstractEndModel;
import com.topsec.tsm.sim.kb.KBEvent;
import com.topsec.tsm.sim.kb.service.KnowledgeService;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.response.persistence.Response;
import com.topsec.tsm.sim.sceneUser.persistence.EventCategory;
import com.topsec.tsm.sim.sysconfig.service.EventRuleService;
import com.topsec.tsm.sim.util.AuditLogFacade;
import com.topsec.tsm.sim.util.CommonUtils;
import com.topsec.tsm.sim.util.DateUtils;
import com.topsec.tsm.sim.util.DeviceTypeNameUtil;
import com.topsec.tsm.sim.util.FacadeUtil;
import com.topsec.tsm.sim.util.FastJsonUtil;
import com.topsec.tsm.sim.util.FtpConfigUtil;
import com.topsec.tsm.sim.util.JSONConverterCallBack;
import com.topsec.tsm.tal.service.EventResponseService;
import com.topsec.tsm.util.net.FtpUploadUtil;

@Controller
@RequestMapping("eventQuery")
public class EventQueryController {
	
	protected static Logger log= LoggerFactory.getLogger(EventQueryController.class);
	
	@Autowired
	private EventQueryService eventQueryService;
	
	@Autowired
	private EventRuleService eventRuleService;
	
	@Autowired
	private KnowledgeService knowledgeService;
	
	@Autowired
	private EventResponseService eventResponseService;
	
	@Autowired
	private EventCategoryService eventCategoryService;
	
	@Autowired
	private TopoService topoServiceService;
	
	@Autowired
	@Qualifier("dataSourceService")
	private DataSourceService dataSourceService ;
	
	private static int SearchLimit = 5000;
	//private final static String DEFUALt_COLUMS_SET="EVENT_ID,PRIORITY,DVC_TYPE,NAME,EVENT_TYPE,SRC_ADDRESS,SRC_PORT,DVC_ADDRESS,TRANS_PROTOCOL,CAT1_ID,CAT2_ID,CAT3_ID,DEST_ADDRESS,DEST_PORT,START_TIME,END_TIME,CUSTOM6,CUSTOM8";
	private final static String DEFUALt_COLUMS_SET="DESCR,CONFIRM,CONFIRM_PERSON,EVENT_ID,PRIORITY,NAME,SRC_ADDRESS,DEST_ADDRESS,DVC_ADDRESS,CAT1_ID,CAT2_ID,CAT3_ID,START_TIME,END_TIME,CUSTOM1,CUSTOM6,CUSTOM8,UUID";
	static {
		String val = System.getProperty("TSM.SEARCH.LIMIT");
		if(val != null){
			try{
				SearchLimit = Integer.valueOf(val);
			}catch(Exception e){
				
			}
		}
		
	}
	
	/**
	 * 事件查询
	 * @author zhaojun 2014-2-25下午3:44:43
	 * @param criteria
	 * @param result
	 * @return
	 */
	
	@RequestMapping(value="basicEventQuery" ,produces="text/javascript;charset=utf-8")
	@ResponseBody
	public Object getEventByBasicCondition(@ModelAttribute("condition") BasicQueryCriteria criteria,HttpServletRequest request){
		QueryConditionAdapter  queryConditionAdapter=new QueryConditionAdapter(criteria);
		Condition condition = queryConditionAdapter.getRequestCondition();
		condition.setColumnsSet(DEFUALt_COLUMS_SET);
		
		Map<String, Object> map = queryProcess(condition,request);
		return map;
	}

	private Map<String, Object> queryProcess(Condition condition,HttpServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
				List<Map<String, Object>> eventResult=eventQueryService.getEventsForFlex(condition);
				for(Map<String, Object> typeName:eventResult){
					String formatLog= (String) typeName.get("CUSTOM8");
					//System.out.println(formatLog);
					if (formatLog != null) {
						JSONObject jsonObject = JSONObject.parseObject(formatLog);
						JSONArray logs=(JSONArray) jsonObject.get("logs");
						typeName.put("LOG_COUNT", logs.size());
					}else{
						typeName.put("LOG_COUNT", 0);
					}
					
					Integer confirm = (Integer)typeName.get("CONFIRM");
					String confirm_person = (String)typeName.get("CONFIRM_PERSON");
					//0：未确认，是数据库中“confirm”的默认值
					if (null == confirm || confirm == 0) {
						typeName.put("CONFIRM", "未确认");
					} else {
						typeName.put("CONFIRM", "已确认");
					}
					if (null == confirm_person) {
						typeName.put("CONFIRM_PERSON", "");
					}
					
					/*System.out.println(logs);
					
					for(int i=0;i<logs.size();i++){
						String encodelog=(String) logs.get(i);
						System.out.println(encodelog);
						byte[] b = Base64.decodeBase64(encodelog);
						System.out.println(new String(b));
					}*/
					typeName.remove("CUSTOM1");
					typeName.put("DESCR", HtmlUtils.htmlEscape((String)typeName.get("DESCR")));
				}
			
			map.put("rows",eventResult);
			List<Map<String, Object>> totalMaps = eventQueryService.getEventsTotalForFlex(condition,false);//总数
			Map<String, Object> totalMap=totalMaps.get(0);
			map.put("total",totalMap.get("value"));
			//封装时间轴数据
			//List<Map<String,Object>> list = formatChart(condition);
			//map.put("chart", list);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return map;
	}
	
	@RequestMapping(value="expandTimeline" ,produces="text/javascript;charset=utf-8")
	@ResponseBody
	public Object expandTimelineByBasicCondition(@ModelAttribute("condition") BasicQueryCriteria criteria,HttpServletRequest request){
		QueryConditionAdapter  queryConditionAdapter=new QueryConditionAdapter(criteria);
		Condition condition = queryConditionAdapter.getRequestCondition();
		try {
			return formatChart(condition,request);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 封装时间轴数据
	 * @param condition
	 * @return
	 * @throws SQLException
	 */
	private List<Map<String,Object>> formatChart(Condition condition,HttpServletRequest request) throws SQLException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		int length =0;
		long maxShowNumber =0;
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		try {
			if(condition.getStart_time() == null)
				return list;
			Date st= sdf.parse(condition.getStart_time());
			Date et  =   sdf.parse(condition.getEnd_time());
			long span = ((et.getTime()-st.getTime())/60000/60);
			Date beginDate =null;
			if(span <= 24*3){
				length = 13;
				maxShowNumber = ((et.getTime()-st.getTime())/(60000*60));
				beginDate= sdf.parse(condition.getStart_time().substring(0, 13)+":00:00");
			}else{
				length = 10;
				maxShowNumber = ((et.getTime()-st.getTime())/(60000* 24*60));
				beginDate= sdf.parse(condition.getStart_time().substring(0, 10)+" 00:00:00");
			}
		//最小1分钟一个刻度
		//与前台计算方法一致，前台图形从右向左显示
		condition.setColumnsSet("SUBSTRING(end_time,1,"+length+")");
		List<Map<String,Object>> eventTimeChart=eventQueryService.getEventsTimeChart(condition);
		while(maxShowNumber+1 >0){
			Map<String,Object> chart = new HashMap<String, Object>();
			if(length ==10){
				String date = sdf.format(beginDate).substring(0,10);
				boolean hasValue = false;
				for(Map<String,Object> map : eventTimeChart){
					if(date.equals(map.get("eTime"))){
						chart.put("y",map.get("count(*)"));
						hasValue = true;
					}
				}
				chart.put("x", beginDate.getTime()+"&"+(beginDate.getTime() +24*60*60*1000-1000));
				if(!hasValue){
					chart.put("y",0);
				}
				beginDate.setTime(beginDate.getTime()+24*60*60*1000);
			}else if(length == 13){
				String date = sdf.format(beginDate).substring(0,13);
				boolean hasValue = false;
				for(Map<String,Object> map : eventTimeChart){
					if(date.equals(map.get("eTime"))){
						chart.put("y",map.get("count(*)"));
						hasValue = true;
					}
				}
				chart.put("x", beginDate.getTime()+"&"+(beginDate.getTime() +60*60*1000-1000));
				if(!hasValue){
					chart.put("y",0);
				}
				beginDate.setTime(beginDate.getTime()+60*60*1000);
			}
			list.add(chart);
			maxShowNumber--;
		}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return list;
	}
	/**
	 * 导出事件
	 * @author zhaojun 2014-2-25下午4:05:26
	 * @param criteria
	 * @param result
	 * @return
	 */
	@RequestMapping(value="exportBasicEvents" ,produces="text/javascript;charset=utf-8")
	@ResponseBody
	public Object exportEventsByBasicCondition(@ModelAttribute("condition") BasicQueryCriteria criteria,HttpServletRequest request){
		QueryConditionAdapter  queryConditionAdapter=new QueryConditionAdapter(criteria);
		Condition condition = queryConditionAdapter.getRequestCondition();
		condition.setColumnsSet(DEFUALt_COLUMS_SET);
		condition.setPageSize(SearchLimit);
		Map<String, Object> resultMap =queryProcess(condition,request);
		Map<String,String>  pathMap=new HashMap<String, String>();
		String path=null;
		try {
			path=exportProcess(resultMap,condition,criteria.getHeader(),criteria.getFields());
		} catch (LogSearchException e) {
			e.printStackTrace();
		}
		pathMap.put("filepath", path);
		return pathMap;
	}
	
	
	@RequestMapping(value="downloadfile")
	public ModelAndView downEventsExportFile(@RequestParam(value="filename")String filename,HttpServletResponse response){
		
        response.setHeader("Content-Type", "application/octet-stream");
        response.setHeader("Content-Type", "text/html;charset=UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename="+ filename);
        String serverHome = System.getProperty("jboss.server.home.dir");
        String savaLogPathfile = new StringBuilder(serverHome)
								        .append(File.separator)
								        .append("ftphome")
								        .append(File.separator)
								        .append("log")
								        .append(File.separator)
								        .append(filename)
								        .toString();

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
        }
        catch (Exception e) {
            log.error("下载文件出错....", e);
        } finally {
            ObjectUtils.close(bis) ;
            ObjectUtils.close(bos) ;
        }
		
		
		return null;
	}
	/**
	 * 下载
	 * @author zhaojun 2014-2-25下午4:05:38
	 * @param resultMap
	 * @param condition
	 * @param header
	 * @return
	 * @throws LogSearchException
	 */
	private String exportProcess(Map<String, Object> resultMap, Condition condition, String header,String fields) throws LogSearchException {
		
 
		List<Map<String, Object>> list=(List<Map<String, Object>>) resultMap.get("rows");
		
		
		String filePath = "";
		String zipFile = System.currentTimeMillis()+".zip";
		zipFile = System.getProperty("java.io.tmpdir") + File.separator + zipFile;
		File uploadFile = new File(zipFile);

		ZipOutputStream zipOut = null;
		WritableWorkbook book = null;
		try {
			zipOut = new ZipOutputStream(new FileOutputStream(uploadFile));
			ZipEntry ze = new ZipEntry(zipFile.replaceFirst(".zip", ".xls")
					.substring(zipFile.lastIndexOf(File.separator) + 1));
			zipOut.putNextEntry(ze);
			String[] strTitle={}; 
			String[] strHeader={}; 
			if(fields != "" && header !=""){
				strTitle = fields.split(",");
				strHeader = header.split(",");
			}
			//java excel api
			//open file
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
			
			Label title = new Label(0,0,"事件查询结果导出报告",cellFormat);
			front.addCell(title);
			front.mergeCells(0, 0, 10, 20);
			front.getSettings().setHidden(false);
			front.getSettings().setSelected(true);
			
			
			//create Sheet with name "导出日志"
			WritableSheet sheet = book.createSheet("1", 1);
			sheet.getSettings().setHidden(false);
			sheet.getSettings().setDefaultColumnWidth(20);
          
			title.setString("事件查询结果导出报告\012共计"+list.size()+"条事件");
			
			WritableFont fontHeader = new WritableFont(WritableFont.createFont("微软雅黑"));
			fontHeader.setBoldStyle(WritableFont.BOLD);
			fontHeader.setColour(Colour.RED);
			WritableCellFormat cellFormatHeader = new WritableCellFormat(fontHeader);
			cellFormatHeader.setAlignment(Alignment.CENTRE);
			cellFormatHeader.setBackground(Colour.BLUE2);
			
			cellFormatHeader.setBorder(Border.ALL, BorderLineStyle.MEDIUM);
			cellFormatHeader.setLocked(true);
			cellFormatHeader.setVerticalAlignment(VerticalAlignment.CENTRE);
			int i=0;
			for(String head:strHeader){
				Label label = new Label(i++,0,head,cellFormatHeader);
				sheet.addCell(label);
			}
			int m=1;
			int page = 1;
			for (Map<String, Object> elem: list) {
				if(page==1){
					if(m%1001 == 0){
						page++;
						sheet = book.createSheet(""+page, page);
						sheet.setColumnView(0, 20);
						sheet.getSettings().setHidden(false);
						sheet.getSettings().setDefaultColumnWidth(20);
						m=0;
						
					}
					
				}else{
					if(m%1000 == 0){
						page++;
						sheet = book.createSheet(""+page, page);
						sheet.setColumnView(0, 20);
						sheet.getSettings().setHidden(false);
						sheet.getSettings().setDefaultColumnWidth(20);
						m=0;
						
					}
				}
				Map<String, Object> record = (Map<String, Object>) elem;
				int n=0;
				for(String tit:strTitle){
					Object value =record.get(tit);
					if(tit.equalsIgnoreCase("PRIORITY")){
						value = CommonUtils.getLevel(value) ;
					}
					if(value != null){
						Label label = new Label(n++,m,value.toString());
						sheet.addCell(label);
					}
					else{
						Label label = new Label(n++,m,"null");
						sheet.addCell(label);
					}
				}
				m++;
			}
		} catch (Exception e) {
			throw new LogSearchException(e);
		} finally {
			if(book != null){
				try {
					book.write();
					book.setProtected(true);
					book.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (zipOut != null)
				try {
					zipOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		
		try {
		  //FTP 信息
		  Map<String,Object> ftpmap=FtpConfigUtil.getInstance().getFTPConfigByKey("log");
          String uploadName = uploadFile.getName();
          boolean result = FtpUploadUtil.uploadFile((String)ftpmap.get("host"), Integer.parseInt((String)ftpmap.get("port")), (String)ftpmap.get("user"), 
        		  (String)ftpmap.get("password"),(String)ftpmap.get("encoding"), ".", uploadName, new FileInputStream(uploadFile));

			FileUtils.deleteQuietly(uploadFile);
			if (result) {
				filePath =uploadName;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return filePath;
	}
	
	
	/**
	 * 事件回溯
	 * @author zhaojun 2014-6-27下午3:11:19
	 * @param id
	 * @return
	 */
	@RequestMapping(value="correlatorData"/*,produces="text/javascript;charset=utf-8"*/)
	@ResponseBody
	public Object correlatorData(@RequestParam(value="evtId")Integer id){
		List<Map<String,Object>> list=eventQueryService.getEventLogsById(id);
		return groupLogs(list) ;
	}

	/**
	 * 事件查询界面鼠标右键查看事件详细信息
	 * @param id
	 * @return
	 */
	@RequestMapping("queryEventDetail")
	@ResponseBody
	public Object eventDetails(SID sid,  @RequestParam("evtId")Integer id, @RequestParam("end_time")String end_time, @RequestParam("uuid")String uuid, @RequestParam("evtName")String evtName){
		
		//返回给页面的集合
		JSONObject result = new JSONObject() ;
		
		//根据事件ID查询关联日志信息
		List<Map<String,Object>> list = eventQueryService.getLogsByUUID(uuid);
		
		//日志信息转码
		List<Map<String,Object>> logs = translateLogs(list) ;
		
		//根据日志信息获取所有IP以获得资产
		Set<AssetObject> assetSet = getRelatedAsset(logs);
		
		//资产IP集合
		Map<String, String> ipMap = new HashMap<String, String>();
		Iterator<AssetObject> assetIt = assetSet.iterator();
		JSONArray ja = new JSONArray();
		while (assetIt.hasNext()) {
			AssetObject ao = assetIt.next();
			ipMap.put(ao.getIp(), ao.getName());
		}
		
		//查询所有业务组
		List<AssTopo> topoList = topoServiceService.getAll();
		
		//根据资产IP查询其所在业务组
		Iterator<String> ipAndName = ipMap.keySet().iterator();
		Set<String> topoNameSet = new HashSet<String>();
		while (ipAndName.hasNext()) {
			String ip = ipAndName.next();
			for (AssTopo assTopo : topoList) {
				String topoName = assTopo.getName();
				String config = assTopo.getConfig();
				if (config.contains(ip)) {
					topoNameSet.add(topoName);
				}
			}
		}
		Iterator<String> topoTemp = topoNameSet.iterator();
		while (topoTemp.hasNext()) {
			JSONObject jo = new JSONObject();
			jo.put("topoName", topoTemp.next());
			ja.add(jo);
		}
		result.put("topo", ja);
		result.put("assets", FastJsonUtil.toJSONArray(assetSet, new JSONConverterCallBack<AssetObject>(){
			@Override
			public void call(JSONObject result, AssetObject obj) {
				//result.put("deviceTypeName", DeviceTypeShortKeyUtil.getInstance().deviceTypeToCN(obj.getDeviceTypeName(), "/")) ;
				result.put("osIconCls", AssetUtil.getIconClsByOS(obj.getOs()));
				result.put("assetIconCls", AssetUtil.getIconClsByDeviceType(obj.getDeviceType()));
				NodeMgrFacade nodeMgrFacade = (NodeMgrFacade) SpringContextServlet.springCtx.getBean("nodeMgrFacade");
				Node node = nodeMgrFacade.getNodeByNodeId(obj.getScanNodeId());
				result.put("nodeName", node != null ? node.getIp() : "");
				FastJsonUtil.mergeToJSON(result, obj,"enabled=available","logCount");
				result.put("deviceTypeIcon", AssetUtil.getIcon48(obj.getDeviceType()));
			}
		}, "name", "ip", "deviceTypeName", "os.osName=osName", "safeRank", "linkman", "state", "status"));
		
		//根据日志信息中的所有资产IP和资产类型以获取日志源
		Set<SimDatasource> logSrcSet = getRelatedDataSource(logs);
		result.put("dataSources", FastJsonUtil.toJSONArray(logSrcSet, new JSONConverterCallBack<SimDatasource>(){
			@Override
			public void call(JSONObject result, SimDatasource obj) {
				result.put("securityObjectTypeName", DeviceTypeNameUtil.getDeviceTypeName(obj.getSecurityObjectType()));
			}
		}, "resourceName", "deviceIp"));
		
		//根据解码后的事件名称查询规则
		EventRuleGroup eventRule = eventRuleService.getEventRuleByName(StringUtil.recode(evtName));
		if(eventRule != null){
			//根据规则ID查询告警信息
			List<Response> eventRuleGroupRespList = eventResponseService.getRespByGroupId(eventRule.getGroupId()) ;
			result.put("alarm", FastJsonUtil.toJSONArray(eventRuleGroupRespList,  "id","name","creater","cfgKey","start","desc"));
		}
		
		//获取关联知识库信息
		List<KBEvent> knowledgeList = knowledgeService.getAssociatedKnowledgeByEvtIdAndEndTime(id, end_time);
		List<Map<String, Object>> kbFormatMaps = convetAk2Maps(knowledgeList);
		result.put("knowledge", FastJsonUtil.toJSONArray(kbFormatMaps, "name", "priority", "description", "solution", "creater", "createTime"));
				
		return result;
	}
	
	private List<Map<String, Object>> convetAk2Maps(List<KBEvent> list) {
		List<Map<String,Object>> kbFormatMaps=new ArrayList<Map<String,Object>>();
		if(list!=null){
			for (KBEvent kbevt : list) {
				Map<String,Object>  formartMap=new HashMap<String, Object>();
				formartMap.put("id", kbevt.getId());
				formartMap.put("name", kbevt.getName());
				formartMap.put("priority", CommonUtils.getLevel(kbevt.getPriority()));
				formartMap.put("description", kbevt.getDescription());
				formartMap.put("solution", kbevt.getSolution());
				formartMap.put("creater", kbevt.getCreater());
				formartMap.put("createTime", DateUtils.formatDatetime(kbevt.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
				kbFormatMaps.add(formartMap);
			}
		}
		return kbFormatMaps;
	}
	
	/**
	 * 获取关联资产
	 * @param logs
	 * @return
	 */
	private Set<AssetObject> getRelatedAsset(List<Map<String,Object>> logs){
		Set<AssetObject> relatedAsset = new HashSet<AssetObject>();
		for(Map<String,Object> log:logs){
			String ip = StringUtil.toString(log.get(DataConstants.SRC_ADDRESS));
			AssetObject asset = AssetFacade.getInstance().getAssetByIp(ip);
			if(asset != null){
				relatedAsset.add(asset);
			}
			ip = StringUtil.toString(log.get(DataConstants.DEST_ADDRESS));
			asset = AssetFacade.getInstance().getAssetByIp(ip);
			if(asset != null){
				relatedAsset.add(asset);
			}
			ip = StringUtil.toString(log.get(DataConstants.DVC_ADDRESS));
			asset = AssetFacade.getInstance().getAssetByIp(ip);
			if(asset != null){
				relatedAsset.add(asset);
			}
		}
		return relatedAsset;
	}
	/**
	 * 获取关联日志源
	 * @param logs
	 * @return
	 */
	private Set<SimDatasource> getRelatedDataSource(List<Map<String,Object>> logs){
		Set<SimDatasource> relatedDataSources = new HashSet<SimDatasource>();
		for(Map<String,Object> log:logs){
			String ip = StringUtil.toString(log.get(DataConstants.SRC_ADDRESS));
			String dvcType = (String) log.get(DataConstants.DVC_TYPE);
			SimDatasource ds = dataSourceService.findByDeviceTypeAndIp(dvcType, ip);
			if(ds != null){
				relatedDataSources.add(ds);
			}
			ip = StringUtil.toString(log.get(DataConstants.DEST_ADDRESS));
			ds = dataSourceService.findByDeviceTypeAndIp(dvcType, ip);
			if(ds != null){
				relatedDataSources.add(ds);
			}
			ip = StringUtil.toString(log.get(DataConstants.DVC_ADDRESS));
			ds = dataSourceService.findByDeviceTypeAndIp(dvcType, ip);
			if(ds != null){
				relatedDataSources.add(ds);
			}
		}
		return relatedDataSources;
	}
	
	/**
	 * 根据事件的uuid进行事件回溯
	 * @param uuid
	 * @return
	 */
	@RequestMapping("correlatorDataByUUID")
	@ResponseBody
	public Object correlatorDataByUUID(@RequestParam("uuid")String uuid) {
		List<Map<String,Object>> logs = eventQueryService.getLogsByUUID(uuid) ;
		return groupLogs(logs) ;
	}
	private List<Map<String,Object>> groupLogs(List<Map<String,Object>> list){
		if(list==null){
			return Collections.emptyList();
		}else{
			List<Map<String,Object>>  clogs=new ArrayList<Map<String,Object>>();
			for (Map<String, Object> logMap : list) {
				String logjsontext = (String) logMap.get("custom8");
				if(logjsontext!=null&&logjsontext.trim().length()>0){
					JSONObject logjson = JSONObject.parseObject(logjsontext);
					JSONArray jsonArray=(JSONArray) logjson.get("logs");
					for (int i = 0; i < jsonArray.size(); i++) {
						String log=(String) jsonArray.get(i);
						byte[] b = Base64.decodeBase64(log);
						String logstr=new String(b);
						Map<String, Object> logMap0 = Map2String.string2Map(logstr);
						String dvcType=(String) logMap0.get("DVC_TYPE");
						addDeviceGroupLogs(dvcType,clogs,Map2String.string2Map(logstr));
					}
				}
			}
			return clogs;
		}
	}
	
	private List<Map<String,Object>> translateLogs(List<Map<String,Object>> list){
		if(list==null){
			return Collections.emptyList();
		}else{
			List<Map<String,Object>>  clogs=new ArrayList<Map<String,Object>>(list.size());
			for (Map<String, Object> logMap : list) {
				String logjsontext = (String) logMap.get("custom8");
				if(logjsontext!=null&&logjsontext.trim().length()>0){
					JSONObject logjson = JSONObject.parseObject(logjsontext);
					JSONArray jsonArray=(JSONArray) logjson.get("logs");
					for (int i = 0; i < jsonArray.size(); i++) {
						String logString=(String) jsonArray.get(i);
						byte[] b = Base64.decodeBase64(logString);
						String logstr=new String(b);
						Map<String, Object> log = Map2String.string2Map(logstr);
						clogs.add(log) ;
					}
				}
			}
			return clogs;
		}
	}
	
	@SuppressWarnings("unchecked")
	private void addDeviceGroupLogs(String dvcType, List<Map<String, Object>> clogs,Map<String, Object> log) {
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy",Locale.US) ;
		Map<String, Object> clog0 = (Map<String, Object>)CollectionUtils.find(clogs,new BeanPropertyValueEqualsPredicate("deviceType",dvcType)) ;
		clog0 = (clog0==null) ? new HashMap<String, Object>() : clog0;
		if(!clog0.containsKey("deviceType")){
			IndexTemplateUtil indexTemplateUtil = IndexTemplateUtil.getInstance();
		    List<IndexField> indexFields = indexTemplateUtil.getIndexFields(dvcType);//设备日志列集
		    List<Map<String,String>> headcolums=new ArrayList<Map<String,String>>();
		    for (IndexField indexField : indexFields) {
				Map<String,String> fieldMap=new HashMap<String, String>();
				fieldMap.put("field", indexField.getKey());
				fieldMap.put("title", indexTemplateUtil.getFieldAlias(dvcType, indexField.getKey()));
				headcolums.add(fieldMap);
			}
		     
		    clog0.put("dvcTypeName", DeviceTypeNameUtil.getDeviceTypeName(dvcType));
			clog0.put("deviceType", dvcType);
			clog0.put("headcolums", headcolums);	
			clogs.add(clog0);
		}
		if(!clog0.containsKey("columsdata")){
			clog0.put("columsdata", new ArrayList<Map<String, Object>>());
		}
		List<Map<String, Object>> columsdata =(List<Map<String, Object>>) clog0.get("columsdata");
		if(log.containsKey("PRIORITY")){
			log.put("PRIORITY", CommonUtils.getLevel(log.get("PRIORITY")));
		}
		if(log.containsKey("START_TIME")){
			try {
				Date date = sdf.parse(log.get("START_TIME").toString()) ;
				log.put("START_TIME", StringUtil.dateToString(date,"yyyy-MM-dd HH:mm:ss"));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		columsdata.add(log);
	}

	/**
	 * 事件名称树
	 * 事件节点带统计数值
	 * @author zhaojun 2014-5-20下午2:25:33
	 * @return
	 */
	@RequestMapping(value="eventRule")
	@ResponseBody
	public Object getEventRule(){//统计每一条规则产生的事件数
		return getEventRule(this.eventQueryService) ;
	}
	
	public static Object getEventRule(EventQueryService eventQueryService){
		List<Map<String,Object>> rules=new ArrayList<Map<String,Object>>();
		try {
			List<Map<String, Object>> evtMaps= eventQueryService.getAllEventStatistics();
			if(evtMaps!=null){
				for (Map<String, Object> eventCountRowMap : evtMaps) {
					Map<String,Object> e=new HashMap<String, Object>();
					String name=(String) eventCountRowMap.get("name");
					e.put("id", name.hashCode());
					Long count=(Long) eventCountRowMap.get("count");
					e.put("text", name+"("+count+")");
					Date startTime=(Date) eventCountRowMap.get("start_time");
					Date endTime=(Date) eventCountRowMap.get("end_time");
					Map<String,Object> attributes=new HashMap<String, Object>();
					attributes.put("startTime", DateUtils.formatDatetime(startTime, "yyyy-MM-dd HH:mm:ss"));
					attributes.put("endTime", DateUtils.formatDatetime(endTime, "yyyy-MM-dd HH:mm:ss"));
					attributes.put("realName", name);
					e.put("attributes", attributes);
					rules.add(e);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rules;
	}
	
	
	/**
	 * 异步统计分级节点事件数
	 * @author zhaojun 2014-6-3下午3:54:30
	 * @param levelParam
	 * @return
	 */
	
	@RequestMapping(value="levelStatistic",produces="text/javascript;charset=UTF-8")
	@ResponseBody
	public Object getLevelEventStatistic(@ModelAttribute("levelParam") CategoryLevelParam levelParam,HttpServletRequest request){
		//System.out.println(JSONObject.toJSONString(levelParam));
		int level = levelParam.getLevel();
		Map<String, String> categoryMap=levelParam.getCategory();
		Map<String,Object> levelEventMap=new HashMap<String, Object>(); 
		levelEventMap.put("count", 0);
		List<Map<String, Object>> totalMaps =null;
		if(eventQueryService == null){
			eventQueryService = (EventQueryService)FacadeUtil.getFacadeBean(request, null, "eventQueryService");
		}
		if(level!=3){//分类统计事件
			
			try {
				Map<String,String> queryParam=new HashMap<String, String>(categoryMap); 
				if(levelParam.getAlarmState()!=null){
					queryParam.put("alarmState", levelParam.getAlarmState().toString());
				}
				totalMaps = eventQueryService.getEventStatisticByCatergory(queryParam,true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}else{//统计具体事件或者告警
			try {
				Condition params=new Condition();
				params.setName(levelParam.getName());
				params.setAlarmState(levelParam.getAlarmState());
				String cat1id=categoryMap.get("cat1id");
				String cat2id=categoryMap.get("cat2id");
				params.setCat1_id(cat1id);
				params.setCat2_id(cat2id);
				totalMaps =eventQueryService.getEventsTotalForFlex(params,true);//告警下次再修改
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		Map< String,  Object> statisticMap=new HashMap<String, Object>();
		if(totalMaps!=null&&totalMaps.size()==1){
			statisticMap.putAll(totalMaps.get(0));
		}
		Date startTime = (Date) statisticMap.get("start_time");
		Date endTime = (Date) statisticMap.get("end_time");
		Long count =   (Long) statisticMap.get("value");
		levelEventMap.put("count", count==null?0:count);
		levelEventMap.put("startTime",startTime==null?null: DateUtils.formatDatetime(startTime, "yyyy-MM-dd HH:mm:ss"));
		levelEventMap.put("endTime",endTime==null? null: DateUtils.formatDatetime(endTime, "yyyy-MM-dd HH:mm:ss"));
		
		return levelEventMap;
	}
	
	/**
	 * 事件分类树
	 * 节点带统计
	 * 
	 * @author zhaojun 2014-3-17下午6:06:56
	 * @return
	 */
	@RequestMapping(value="eventCategory",produces="text/javascript;charset=UTF-8")
	@ResponseBody
	public Object getEventCategory(@RequestParam(value="id",required=false)Integer id,HttpServletRequest request){
		/*一级二级为事件分类,三级节点为具体事件 */
		  final CategoryOrganizationTemplate  cgenTemplate=new CategoryOrganizationTemplate(id){
			@Override
			public void extractor(JSONObject jsonObject) {
				String text=jsonObject.getString("text");
				jsonObject.put("text",text);//设置节点样式
			}
		};
		if(eventCategoryService == null){
			eventCategoryService = (EventCategoryService)FacadeUtil.getFacadeBean(request, null, "eventCategoryService");
		}
		if(eventQueryService == null){
			eventQueryService = (EventQueryService)FacadeUtil.getFacadeBean(request, null, "eventQueryService");
		}
		JSONArray  jsonArray=cgenTemplate.genDynamicCategoryJson(eventCategoryService, new AbstractEndModel() {
			@Override
			public void level3(JSONArray jsonArray, Map<String, Object> categoryMap) {
				EventCategory currCategory = cgenTemplate.getCurrentCategory();
				if(currCategory!=null&&(currCategory.getParentId()!=null&&currCategory.getParentId()>0)){
					EventCategory pCategory = eventCategoryService.get(currCategory.getParentId());
					Map<String,Object> categoryMapCopy=new HashMap<String, Object>();
					categoryMapCopy.put("cat1id", pCategory.getCategoryName());
					categoryMapCopy.put("cat2id", currCategory.getCategoryName());
					List<Map<String,Object>> eventKeyNameMaps=eventQueryService.getExistedEventNames(categoryMapCopy);
					if(eventKeyNameMaps!=null){
						for (Map<String, Object> evtKeyNameMap : eventKeyNameMaps) {
							JSONObject  parentJsonObject=new JSONObject();
							String evtName=(String) evtKeyNameMap.get("name");
							int counts = ((Number)evtKeyNameMap.get("counts")).intValue();
							parentJsonObject.put("text", evtName+"("+counts+")");
							JSONObject attributes=new JSONObject();
							attributes.put("type", "3");//三级事件
							//attributes.put("startTime",DateUtils.formatDatetime(((Date)evtKeyNameMap.get("start_time")), "yyyy-MM-dd HH:mm:ss"));
							//attributes.put("endTime",DateUtils.formatDatetime(((Date)evtKeyNameMap.get("end_time")), "yyyy-MM-dd HH:mm:ss"));
							attributes.put("realName",evtName);
							parentJsonObject.put("attributes", attributes);
							parentJsonObject.put("state","open");
							parentJsonObject.put("id", "3_"+ evtName.hashCode());
							cgenTemplate.extractor(parentJsonObject);
							jsonArray.add(parentJsonObject);
						}
					}
				}
			}
		});
		return jsonArray.toJSONString();
	}
	/**
	 * 按照一级分类进行事件数量统计
	 * @return
	 */
	@RequestMapping("cat1Statistic")
	@ResponseBody
	public Object cat1Statistic() {
		List<Map<String,Object>> result =  eventQueryService.cat1Statistic() ;
		return result ;
	}
	/**
	 * 按照事件二级分类进行事件统计
	 * @param cat1 一级分类
	 * @return
	 */
	@RequestMapping("cat2Statistic")
	@ResponseBody
	public Object cat2Statistic(@RequestParam("cat1")String cat1) {
		List<Map<String,Object>> result =  eventQueryService.cat2Statistic(StringUtil.recode(cat1)) ;
		return result ;
	}
	/**
	 * 基于一级分类二级分类的事件名称统计
	 * @param cat1 一级分类
	 * @return
	 */
	@RequestMapping("nameStatBaseOnCat")
	@ResponseBody
	public Object nameStatBaseOnCat(@RequestParam("cat1")String cat1,@RequestParam("cat2")String cat2) {
		List<Map<String,Object>> result =  eventQueryService.nameStatBaseOnCat(StringUtil.recode(cat1),StringUtil.recode(cat2)) ;
		return result ;
	}	
	/**
	 * 关联知识库
	 * @param id
	 * @param request
	 * @return
	 */
	public Object getAssociatedKnowledgeByEvtId(float id,HttpServletRequest request){
		KnowledgeService 	knowledgeService = (KnowledgeService)FacadeUtil.getFacadeBean(request, null, "knowledgeService");
		List<KBEvent> list = knowledgeService.getAssociatedKnowledgeByEvtId(id);
		List<Map<String, Object>> kbFormatMaps = new ArrayList<Map<String,Object>>();
		if(list!=null){
			for (KBEvent kbevt : list) {
				Map<String,Object>  formartMap=new HashMap<String, Object>();
				formartMap.put("id", kbevt.getId());
				formartMap.put("name", kbevt.getName());
				formartMap.put("priority", CommonUtils.getLevel(kbevt.getPriority()));
				formartMap.put("description", kbevt.getDescription());
				formartMap.put("solution", kbevt.getSolution());
				formartMap.put("creater", kbevt.getCreater());
				formartMap.put("createTime", DateUtils.formatDatetime(kbevt.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
				kbFormatMaps.add(formartMap);
			}
		}
		return kbFormatMaps;
	}

	/**
	 * 更改事件状态 (0：未确认,1：已确认)
	 * @param ids
	 * @param state
	 * @param request
	 */
	@RequestMapping("eventConfirm")
	@ResponseBody
	public Object changeEventState(@RequestParam("ids")String[] ids, @RequestParam("state")Integer state, 
			HttpServletRequest request, SID sid) {
		Result result = new Result(true, null);
		if (ObjectUtils.isEmpty(ids)) {
			return result;
		}
		for (String id:ids) {
			if (StringUtil.isBlank(id)) {
				continue;
			}
			Result changeResult;
			try {
				changeResult = changeConfirmStateById(id, state, request, sid.getUserName());
				if(!changeResult.isSuccess()){
					result.buildError(changeResult.getMessage());
					break;
				}
			} catch (Exception e) {
				result.buildError(e.getMessage());
			}
		}
		return result;
	}
	
	private Result changeConfirmStateById(String event_id, Integer confirm, 
			HttpServletRequest request, String confirm_person) throws Exception{
		if (confirm == 0) {//取消确认，清空确认人
			eventQueryService.updateEvent(event_id, confirm, "");
		} else {//确认后，添加确认人
			eventQueryService.updateEvent(event_id, confirm, confirm_person);
		}
		JSONObject result = new JSONObject();
		result.put("event_id", event_id);
		result.put("confirm", confirm);
		result.put("confirm_person", confirm_person);
		return new Result().buildSuccess(result) ;
	}
	
	/**
	 * 根据事件名称查询规则id，并查询相关联的告警
	 */
	@RequestMapping("queryEventAlarm")
	@ResponseBody
	public Object eventAlarm(SID sid, @RequestParam("name")String evtName){
		//返回给页面的集合
		JSONObject result = new JSONObject() ;
		//根据解码后的事件名称查询规则
		EventRuleGroup eventRule = eventRuleService.getEventRuleByName(StringUtil.recode(evtName));
		if(eventRule != null){
			//根据规则ID查询告警信息
			List<Response> eventRuleGroupRespList = eventResponseService.getRespByGroupId(eventRule.getGroupId()) ;
			result.put("ruleId", eventRule.getGroupId());
			result.put("alarm", FastJsonUtil.toJSONArray(eventRuleGroupRespList,  "id","name","creater","cfgKey","start","desc"));
		}
		return result;
	}

	public EventQueryService getEventQueryService() {
		return eventQueryService;
	}

	public void setEventQueryService(EventQueryService eventQueryService) {
		this.eventQueryService = eventQueryService;
	}

	public EventRuleService getEventRuleService() {
		return eventRuleService;
	}

	public void setEventRuleService(EventRuleService eventRuleService) {
		this.eventRuleService = eventRuleService;
	}

	public EventResponseService getEventResponseService() {
		return eventResponseService;
	}

	public void setEventResponseService(EventResponseService eventResponseService) {
		this.eventResponseService = eventResponseService;
	}

	public EventCategoryService getEventCategoryService() {
		return eventCategoryService;
	}

	public void setEventCategoryService(EventCategoryService eventCategoryService) {
		this.eventCategoryService = eventCategoryService;
	}

	public DataSourceService getDataSourceService() {
		return dataSourceService;
	}

	public void setDataSourceService(DataSourceService dataSourceService) {
		this.dataSourceService = dataSourceService;
	}

	public void setKnowledgeService(KnowledgeService knowledgeService) {
		this.knowledgeService = knowledgeService;
	}
}
