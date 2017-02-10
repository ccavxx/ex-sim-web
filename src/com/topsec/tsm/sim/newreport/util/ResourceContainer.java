package com.topsec.tsm.sim.newreport.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sun.tools.javac.util.ByteBuffer;
import com.topsec.tsm.sim.access.util.GlobalUtil;

/**
 * @ClassName: ResourceContainer
 * @Declaration: ToDO
 * 
 * @author: WangZhiai create on2015年9月22日上午9:29:25
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class ResourceContainer {
	private static Logger logger = LoggerFactory.getLogger(ResourceContainer.class) ;
	private static final String WINDOWS_OS_EVENT_MAPPING="/resource/newreport/windows_eventid.properties";
	private static final String WINDOWS_OS_EVENT_OTHER_MAPPING="/resource/newreport/windows_eventid_other.properties";
	private static final String IP_MAPPING="/resource/newreport/ip_mapper.properties";
	private static final String PROPERTY_MAPPING="/resource/newreport/property_mapper.properties";
	private static final String OTHER_MAPPING="/resource/newreport/other_mapper.properties";
	private static final String EN_TO_CN_MAPPING="/resource/newreport/en_to_cn.properties";
	private static final String EVERY_MAPPING_PATH="/resource/newreport/everyoneload/";
	private static final String HTML_TEMPLATE="/resource/newreport/exportTemplate/htmlLoyout.html";
	private static final String REPORT_XML="/resource/newreport/windows_report.xml";
	
	private static final String START_END_TIME=" AND START_TIME >= ? AND START_TIME< ? ";
	private static final String RESOURCE_ID_QUERY=" AND RESOURCE_ID= ?"+START_END_TIME;
	private static final String [] DEVICE_TYPES={"OS/Microsoft/WindowsEventLog","Web/Microsoft/IIS FTP","Web/Apache/Apache Access"};
	private static AtomicInteger startId=new AtomicInteger(100000);
	private static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	private static Document document = null;
	private static Element rootElement ;
	private static InputStream inStream;
	
	private static final Map<String, String> FIELD_MAPPING = new HashMap<String, String>(7);
	private static Map<String, List<String>> osTableFiledMap;
	private static Map<String, List<Map<String, Object>>> keyIsNameMap;
	private static Map<Integer, List<Map<String, Object>>> keyIsIdMap;
	private static Map<Integer, List<Map<String, Object>>> keyIsIdParentSubMap;
	private static Map<Integer, Map<String, Object>> keyIsParentSubIdMap;
	private static Map<Integer, List<Map<String, Object>>> keyIsIdDetailParentSubMap;
	private static Map<String, List<Map<String, Object>>> deviceReportMap;
	private static Map<String, List<Map<String, Object>>> reGroupdeviceReportMap;
	private static Map<String, String> groupIdAndNameMap ;
	private static Map<String,List<Integer>> reportIsGroupMap ;
	
	public static final Properties WINOS_MAPPING_PROP;
	public static final Properties WINOS_OTHER_MAPPING_PROP;
	public static final Properties IP_MAPPING_PROP;
	public static final Properties PROPERTY_MAPPING_PROP;
	public static final Properties OTHER_MAPPING_PROP;
	public static final Properties EN_TO_CN_PROP;
	public static final Properties EVERY_MAPPING_PROP;
	
	public static String htmlTemplate=null;
	static{
		WINOS_MAPPING_PROP = new Properties();
		WINOS_OTHER_MAPPING_PROP = new Properties();
		IP_MAPPING_PROP = new Properties();
		PROPERTY_MAPPING_PROP = new Properties();
		OTHER_MAPPING_PROP = new Properties();
		EN_TO_CN_PROP = new Properties();
		EVERY_MAPPING_PROP = new Properties();
		
		try {
			inStream = ResourceContainer.class.getResourceAsStream(WINDOWS_OS_EVENT_MAPPING);
			WINOS_MAPPING_PROP.load(inStream);
			inStream.close();
		} catch (IOException e) {
			logger.info("...WINDOWS_OS_EVENT_MAPPING 加载失败!");
			e.printStackTrace();
		}
		
		try {
			inStream = ResourceContainer.class.getResourceAsStream(WINDOWS_OS_EVENT_OTHER_MAPPING);
			WINOS_OTHER_MAPPING_PROP.load(inStream);
			inStream.close();
		} catch (IOException e) {
			logger.info("...WINDOWS_OS_EVENT_OTHER_MAPPING 加载失败!");
			e.printStackTrace();
		}
		
		try {
			inStream = ResourceContainer.class.getResourceAsStream(IP_MAPPING);
			IP_MAPPING_PROP.load(inStream);
			inStream.close();
		} catch (IOException e) {
			logger.info("...IP_MAPPING 加载失败!");
			e.printStackTrace();
		}
		
		try {
			inStream = ResourceContainer.class.getResourceAsStream(PROPERTY_MAPPING);
			PROPERTY_MAPPING_PROP.load(inStream);
			inStream.close();
		} catch (IOException e) {
			logger.info("...NAME_MAPPING 加载失败!");
			e.printStackTrace();
		}
		
		try {
			inStream = ResourceContainer.class.getResourceAsStream(OTHER_MAPPING);
			OTHER_MAPPING_PROP.load(inStream);
			inStream.close();
		} catch (IOException e) {
			logger.info("...OTHER_MAPPING 加载失败!");
			e.printStackTrace();
		}
		
		try {
			EN_TO_CN_PROP.load(new InputStreamReader(ResourceContainer.class.getResourceAsStream(EN_TO_CN_MAPPING),"UTF-8"));
			inStream.close();
		} catch (IOException e) {
			logger.info("...EN_TO_CN_MAPPING 加载失败!");
			e.printStackTrace();
		}
		
		try {
			File file=new File(ResourceContainer.class.getResource(EVERY_MAPPING_PATH).getFile());
			if (file.isDirectory()) {
				FileFilter fileFilter=new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						/** 过滤掉非文件、或者大于10MB的文件，防止恶意程序加入大文件导致系统危险 */
						if (!pathname.isFile() || pathname.length()>10*1024*1024) {
							return false;
						}
						if (pathname.getPath().endsWith(".properties")) {
							return true;
						}
						return false;
					}
				};
				File[] files=file.listFiles(fileFilter);
				for (File subFile : files) {
					
					inStream=new FileInputStream(subFile);
					EVERY_MAPPING_PROP.load(inStream);
				}
			}else if (file.isFile() && file.exists()) {
				inStream=new FileInputStream(file);
				EVERY_MAPPING_PROP.load(inStream);
			}
		} catch (IOException e) {
			logger.info("...EVERY_MAPPING 加载失败!");
			e.printStackTrace();
		}
		
		try {
			inStream = ResourceContainer.class.getResourceAsStream(HTML_TEMPLATE);
			ByteBuffer buffer=new ByteBuffer();
			byte []b=new byte[1024];
			int len=-1;
			while((len=inStream.read(b))!=-1){
				buffer.appendBytes(b, 0, len);
			}
			htmlTemplate = new String(buffer.elems,"UTF-8");
			inStream.close();
		} catch (IOException e) {
			logger.info("...HTML_TEMPLATE 加载失败!");
			e.printStackTrace();
		}
		
		try {
			inStream = ResourceContainer.class.getResourceAsStream(REPORT_XML);
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(inStream);
			rootElement = document.getDocumentElement();
			inStream.close();
		} catch (Exception e) {
			logger.info("...REPORT_XML 加载失败!");
			e.printStackTrace();
		}
		
		FIELD_MAPPING.put("EVENTID", "MSG_ID");
		FIELD_MAPPING.put("LOGONTYPE", "LOGON_TYPE");
		FIELD_MAPPING.put("SOURCE", "DVC_EVENT_CATEGORY");
		FIELD_MAPPING.put("CATEGORY", "CAT1_ID");
		FIELD_MAPPING.put("SUBCATEGORY", "CAT2_ID");
//		FIELD_MAPPING.put("SEVERITY", "SEVERITY");
		FIELD_MAPPING.put("apache_statuscode", "REQUEST_STATUS");
		
		reportIsGroupMap = new HashMap<String, List<Integer>>();
		osTableFiledMap=new LinkedHashMap<String, List<String>>(6);
		initAndSetTableFiledMap(osTableFiledMap);
		initOtherReportParents();
		initReportGroupParents();
		deviceReportMap=new HashMap<String, List<Map<String,Object>>>(DEVICE_TYPES.length);
		reGroupdeviceReportMap=new HashMap<String, List<Map<String,Object>>>(DEVICE_TYPES.length);
		for(String deviceType : DEVICE_TYPES)
			initOtherReportParents(deviceType);
		setkeyIsIdParentSubMap();
		reGroupdeviceReportMap.putAll(deviceReportMap);
		rebuildParent(groupIdAndNameMap,reportIsGroupMap, keyIsIdParentSubMap, keyIsIdDetailParentSubMap,reGroupdeviceReportMap);
	}
	
	public static List<Map<String,Object>> getParentThemesByDeviceType(String deviceType){
		return reGroupdeviceReportMap.get(deviceType);
	}
	
	public static List<Map<String,Object>> getParentSubs(Integer parentId){
		return keyIsIdParentSubMap.get(parentId);
	}
	public static List<Map<String,Object>> getDetailParentSubs(Integer parentId){
		return keyIsIdDetailParentSubMap.get(parentId);
	}
	public static Map<String,Object> getSigleSubThemes(Integer parentSubId){
		
		List<Map<String, Object>> criterias=keyIsIdMap.get(parentSubId);
		String []strings = unionhandler(criterias);
		String dataStructure="{categorys:{EVENT_NAME~名称~TEXT},series:{EVENT_NAME~名称~TEXT},statistical:{OPCOUNT~数~COUNT_NO}}";
		return createAndSetSubThemeMap(strings[0],strings[1],strings[3],dataStructure,"");
	}
	public static Map<String,Object> getSubThemes(Integer parentSubId){
		
		return keyIsParentSubIdMap.get(parentSubId);
	}
	private static void rebuildParent(Map<String, String> groupIdAndNameMap,Map<String, List<Integer>>reportIsGroupMap,
			Map<Integer, List<Map<String, Object>>> keyIsIdParentSubMap,Map<Integer, List<Map<String, Object>>> keyIsIdDetailParentSubMap,
			Map<String, List<Map<String, Object>>> reGroupdeviceReportMap){
		for(Map.Entry<String, String> entry:groupIdAndNameMap.entrySet()){
			String id = entry.getKey();
			
			List<Integer>idlist=reportIsGroupMap.get(id);
			if (null == idlist || idlist.size() == 0) 
				continue;
			
			List<Map<String, Object>>sampleList = new ArrayList<Map<String,Object>>();
			List<Map<String, Object>>detailList = new ArrayList<Map<String,Object>>();
			for (int parentId : idlist) {
				List<Map<String, Object>>sList=keyIsIdParentSubMap.get(parentId);
				List<Map<String, Object>>dList=keyIsIdDetailParentSubMap.get(parentId);
				sampleList.addAll(sList);
				detailList.addAll(dList);
				keyIsIdParentSubMap.remove(parentId);
				keyIsIdDetailParentSubMap.remove(parentId);
			}
			int parentId = idlist.get(0);
			keyIsIdParentSubMap.put(parentId, sampleList);
			keyIsIdDetailParentSubMap.put(parentId, detailList);
		}
		for(Map.Entry<String, List<Map<String, Object>>> entry : reGroupdeviceReportMap.entrySet()){
			String deviceType = entry.getKey();
			List<Map<String, Object>> maps = entry.getValue();
			if(null == maps || maps.size() ==0)
				continue;
			List<Map<String, Object>> removemap=new ArrayList<Map<String,Object>>();
			
			for(Map.Entry<String, String> groupentry:groupIdAndNameMap.entrySet()){
				String id = groupentry.getKey();
				String value = groupentry.getValue();
				Map<String, Object> reMap=new HashMap<String, Object>();
				List<Integer>idlist=reportIsGroupMap.get(id);
				if (null == idlist || idlist.size() == 0) 
					continue;
				int parentId = idlist.get(0);
				
				for (Map<String, Object> map : maps) {
					if(idlist.contains(map.get("id"))){
						removemap.add(map);
						if (reMap.size() == 0) {
							reMap.putAll(map);
						}
					}
				}
				if (removemap.size()>0) 
					maps.removeAll(removemap);
				if (reMap.size()>0) {
					reMap.put("id", parentId);
					reMap.put("reportName", value);
					maps.add(reMap);
				}
				
			}
			
			
			reGroupdeviceReportMap.put(deviceType, maps);
		}
	}
	private static Map<String,Object> createAndSetSubThemeMap(String sql,String paramCdt,String tableName,String dataStructure,String describe){
		Map<String,Object> subTheme = new HashMap<String, Object>();
		subTheme.put("sortCondition", "");
		subTheme.put("groupCondition", "");
		subTheme.put("specialCondition", "");
		subTheme.put("queryType", "NO_QUERY");
		subTheme.put("sql", sql);
		subTheme.put("reportType", "NOT_DRILL");
		subTheme.put("special", "NO_RE_GROUP");
		subTheme.put("allPurposeCondition", paramCdt);
		subTheme.put("dataStructureDesc", dataStructure);
		subTheme.put("queryCondition", "");
		subTheme.put("linkCondition", "");
		subTheme.put("showType", "NOT_TREND");
		subTheme.put("describe", describe);
		subTheme.put("tableName", tableName);
		subTheme.put("isUnion", true);
		return subTheme;
	}
	
	private static String[] unionhandler(List<Map<String, Object>> criterias){
		StringBuffer sqlBuffer = new StringBuffer();
		StringBuffer paramCdt = new StringBuffer();
		
		int len = criterias.size();
		for (int i = 0; i < len-1; i++) {
			Map<String, Object> unitMap=criterias.get(i);
			sqlBuffer.append(unitMap.get("sql")).append(" WHERE ").append(unitMap.get("condition"))
			.append(unitMap.get("otherCondition")).append(" UNION ");
			paramCdt.append(unitMap.get("otherCondition")).append(" UNION ");
		}
		Map<String, Object> lastMap = criterias.get(len-1);
		sqlBuffer.append(lastMap.get("sql")).append(" WHERE ").append(lastMap.get("condition"))
		.append(lastMap.get("otherCondition"));
		paramCdt.append(lastMap.get("otherCondition"));
		String tableName = (String)lastMap.get("tableName");
		String []strings={sqlBuffer.toString(),paramCdt.toString(),"",tableName};
		return strings;
	}
	private static void initOtherReportParents() {
		
		NodeList groups = rootElement.getElementsByTagName("ImportantReportsGroup");
		int groupLength=groups.getLength();
		keyIsNameMap=new HashMap<String, List<Map<String,Object>>>(groupLength);
		
		for (int i = 0; i < groupLength; i++) {
			Element element=(Element)groups.item(i);

			String groupName = element.getAttribute("imp_group_name");
			List<Map<String, Object>> criterias=initReportCriteriaByName(groupName);
			if(null != criterias && criterias.size() > 0 )
				keyIsNameMap.put(groupName, criterias);
			
		}
		
	}
	private static void setkeyIsIdParentSubMap(){
		keyIsIdParentSubMap = new HashMap<Integer, List<Map<String,Object>>>();
		keyIsIdDetailParentSubMap = new HashMap<Integer, List<Map<String,Object>>>();
		keyIsParentSubIdMap = new HashMap<Integer, Map<String,Object>>();
		keyIsIdMap = new HashMap<Integer, List<Map<String,Object>>>();
		for (Map.Entry<String, List<Map<String, Object>>> entry : deviceReportMap.entrySet()) {

			List<Map<String, Object>> mapList =entry.getValue();
			for (Map<String, Object> map : mapList) {
				
				hasMoreSql(keyIsIdParentSubMap,keyIsParentSubIdMap,keyIsIdDetailParentSubMap,keyIsIdMap,map);
			}
		}
		
	}
	private static Map<String,Object> createAndSetParentSubMap(Integer parentSubId,Integer subId,String subReportName){
		Map<String,Object> map = new HashMap<String, Object>(6);
		map.put("parentSubId", parentSubId);
		map.put("subId", subId);
		map.put("subReportName", subReportName);
		map.put("showType", "NOT_TREND:(standardbar)");
		map.put("showOrder", 1);
		map.put("userShow", "BIG_IMAGE");

		return map;
	}
	private static boolean hasMoreSql(Map<Integer, List<Map<String, Object>>> parentSubMaps,
			Map<Integer, Map<String, Object>> parentSubIdMaps,Map<Integer, List<Map<String, Object>>> detailParentSubMaps,
			Map<Integer, List<Map<String, Object>>> keyIdMap,Map<String, Object> mapUnit){
		String name=mapUnit.get("reportEnName").toString();
		List<Map<String, Object>> criterias=keyIsNameMap.get(name);
		int id=Integer.valueOf(mapUnit.get("id").toString());
		keyIdMap.put(id, criterias);
		
		List<Map<String,Object>> maps = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> detailPSMs = new ArrayList<Map<String,Object>>();
		
		List<String[]> list=unionSqlhandler(criterias);
		int listLen = list.size();
		for (int i=0;i<listLen;i++) {
			String[] strings=list.get(i);
			String cnName = en2Cn(name);
			String dataStructure=null;
			if("EVENT_NAME".equals(strings[2])){
				dataStructure="{categorys:{EVENT_NAME~名称~TEXT},series:{EVENT_NAME~名称~TEXT},statistical:{OPCOUNT~数~COUNT_NO}}";
			}else if("SRC_USER_NAME".equals(strings[2])){
				dataStructure="{categorys:{SRC_USER_NAME~用户~TEXT},series:{SRC_USER_NAME~用户~TEXT},statistical:{OPCOUNT~数~COUNT_NO}}";
				cnName = cnName+"用户排行";
			}
			int parentSubId = id*10+i;
			
			Map<String,Object> parentSubMap = createAndSetParentSubMap(parentSubId,id,cnName);
			maps.add(parentSubMap);
			
			Map<String, Object>mapSub = createAndSetSubThemeMap(strings[0],strings[1],strings[3],dataStructure,"");
			parentSubIdMaps.put(parentSubId, mapSub);
			
			Map<String,Object> detailParentSubMap = createAndSetDetailParentSubs(parentSubMap,mapSub,mapUnit);
			detailPSMs.add(detailParentSubMap);
			
		}
		parentSubMaps.put(id, maps);
		detailParentSubMaps.put(id, detailPSMs);
		
		if(listLen > 1)
			return true;
		return false;
	}
	
	private static List<String[]> unionSqlhandler(List<Map<String, Object>> criterias){
		List<String[]> list = new ArrayList<String[]>(2);
		
		StringBuffer sqlBuffer = new StringBuffer();
		StringBuffer paramCdt = new StringBuffer();
		StringBuffer otherSql = new StringBuffer();
		String[]categories = null;
		String[]tableNames = null;
		int len = criterias.size();
		List<String>sigsqls=new ArrayList<String>(len);
		List<String>sigCdts=new ArrayList<String>(len);
		boolean firstAndsame = true;
		
		for (int i = 0; i < len-1; i++) {
			Map<String, Object> unitMap=criterias.get(i);
			String[]sqls = (String[])unitMap.get("sqls");
			if(firstAndsame){
				categories = (String[])unitMap.get("categories");
				tableNames = (String[])unitMap.get("tableNames");
			}
			String cdt = (String)unitMap.get("condition");
			String otherCdt = (String)unitMap.get("otherCondition");
			sqlBuffer.append(sqls[0]).append(" WHERE ").append(cdt)
			.append(otherCdt).append(" UNION ");
			paramCdt.append(otherCdt).append(" UNION ");
			
			if (sqls.length == 2) {
				if(firstAndsame){
					otherSql.append(sqls[1]).append(" WHERE (");
					firstAndsame = false;
					categories = (String[])unitMap.get("categories");
					tableNames = (String[])unitMap.get("tableNames");
				}
				if(! sigsqls.contains(sqls[1])){
					sigsqls.add(sqls[1]);
				}
				if(! sigCdts.contains(cdt)){
					sigCdts.add(cdt);
					otherSql.append(cdt).append(" OR ");
				}
			}
			
		}
		Map<String, Object> lastMap = criterias.get(len-1);
		String[]sqls = (String[])lastMap.get("sqls");
		String cdt = (String)lastMap.get("condition");
		String otherCdt = (String)lastMap.get("otherCondition");
		
		sqlBuffer.append(sqls[0]).append(" WHERE ").append(cdt)
		.append(otherCdt);
		paramCdt.append(otherCdt);
		
		String []strings={sqlBuffer.toString(),paramCdt.toString(),categories[0],tableNames[0]};
		list.add(strings);
		
		String otherString=null;
		if (otherSql.length() > 3) {
			if(! sigCdts.contains(cdt)){
				sigCdts.add(cdt);
				otherSql.append(cdt).append(") AND ").append(categories[1])
				.append(" IS NOT NULL ").append(otherCdt);
				otherString = otherSql.toString();
			}else {
				otherString = otherSql.substring(0, otherSql.length()-3)+ ") AND " +categories[1]+" IS NOT NULL "+ otherCdt;
			}
			String []string1s={otherString,otherCdt,categories[1],tableNames[1]};
			list.add(string1s);
		}
		
		return list;
	}
	private static Map<String,Object> createAndSetDetailParentSubs(Map<String,Object> mapPS,Map<String,Object> mapS,Map<String, Object> mapP){
		Map<String,Object> map = new HashMap<String, Object>(23);
		
		map.put("sortCondition", mapS.get("sortCondition"));
		map.put("subDescribe", "");
		map.put("groupCondition", mapS.get("groupCondition"));
		map.put("parentReportName", mapP.get("reportName"));
		map.put("parentDescribe", "");
		map.put("showUnits", mapP.get("showUnits"));
		map.put("specialCondition", mapS.get("specialCondition"));
		map.put("queryType", mapS.get("queryType"));
		map.put("sql", mapS.get("sql"));
		map.put("reportType", mapS.get("reportType"));
		map.put("special", mapS.get("special"));
		map.put("allPurposeCondition", mapS.get("allPurposeCondition"));
		map.put("dataStructureDesc", mapS.get("dataStructureDesc"));
		map.put("site", "");
		map.put("queryCondition", mapS.get("queryCondition"));
		map.put("userShow", mapPS.get("userShow"));
		map.put("linkCondition", mapS.get("linkCondition"));
		map.put("tableName", mapS.get("tableName"));
		map.put("subReportName", mapPS.get("subReportName"));
		map.put("showType", mapPS.get("showType"));
		map.put("showOrder", mapPS.get("showOrder"));
		map.put("formatStyle", "");
		map.put("describe", mapS.get("describe"));
		map.put("isUnion", mapS.get("isUnion"));
		return map;
	}
	private static void initOtherReportParents(String deviceType) {
		
		NodeList groups = rootElement.getElementsByTagName("ImportantReportsGroup");
		
		if ("OS/Microsoft/WindowsEventLog".equalsIgnoreCase(deviceType)){
			startId.set(setParentThemes(deviceType,groups,deviceReportMap,"Windows","Windows",startId.get()) );
		}else if ("Web/Microsoft/IIS FTP".equalsIgnoreCase(deviceType)) {
			startId.set(setParentThemes(deviceType,groups,deviceReportMap,"iis_w3cweb","Applications",startId.get()) );
		}else if ("Web/Apache/Apache Access".equalsIgnoreCase(deviceType)) {
			startId.set(setParentThemes(deviceType,groups,deviceReportMap,"apache_logs","Applications",startId.get()) );
		}
		
	}
	
	private static int setParentThemes(String deviceType,NodeList groups,Map<String, List<Map<String,Object>>> deviceReports,String groupLogType,String groupType,int id){
		int groupLength=groups.getLength();
		List<Map<String,Object>> parentMaps = new ArrayList<Map<String,Object>>(groupLength);
		for (int i = 0; i < groupLength; i++) {
			Element element=(Element)groups.item(i);
			if (groupLogType.equals(element.getAttribute("group_logtype"))
					&& groupType.equals(element.getAttribute("group_type"))){
				String groupid =element.getAttribute("impgroupid");
				List<Integer> idlist = reportIsGroupMap.get(groupid);
				if(null == idlist)
					idlist = new ArrayList<Integer>();
				String groupName = element.getAttribute("imp_group_name");
				List<Map<String, Object>> criterias=keyIsNameMap.get(groupName);
				if(null != criterias && criterias.size() > 0 ){
					Map<String, Object> parentTheme=new HashMap<String, Object>();
					id+=i;
					
					parentTheme.put("id", id);
					parentTheme.put("reportEnName", groupName);
					parentTheme.put("reportName", en2Cn(groupName));
					parentTheme.put("showUnits", "");
					if (! parentMaps.contains(parentTheme)) 
						parentMaps.add(parentTheme);
					if(!idlist.contains(id)){
						idlist.add(id);
						reportIsGroupMap.put(groupid, idlist);
					}
				}
				
			}
		}
		deviceReports.put(deviceType, parentMaps);
		return id;
	}
	private static String en2Cn(String en){
		if(GlobalUtil.isNullOrEmpty(en))
			return en;
		String cn=EN_TO_CN_PROP.getProperty(en);
		if(GlobalUtil.isNullOrEmpty(cn))
			cn=EN_TO_CN_PROP.getProperty(en.toUpperCase());
		if(GlobalUtil.isNullOrEmpty(cn))
			return en;
		return cn;
	}
	private static List<Map<String, Object>> initReportCriteriaByName(String name){
		NodeList groups = rootElement.getElementsByTagName("ImportantReportsGroup");
		int groupLength=groups.getLength();
		List<Map<String, Object>> criterias = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < groupLength; i++) {
			Element element=(Element)groups.item(i);
			if (name.equalsIgnoreCase(element.getAttribute("imp_group_name"))){
				NodeList reportCriterias=element.getElementsByTagName("ImportantReports");
				int criteriaLength=reportCriterias.getLength();
				for (int j = 0; j < criteriaLength; j++) {
					Element criteriasElement=(Element)reportCriterias.item(j);
					String criteria=criteriasElement.getAttribute("reportcriteria");
					String [] fieldStrings = criteria.split(":");
					List<String> fieldsList=new ArrayList<String>(fieldStrings.length);
					for (String string : fieldStrings) {
						int pos=string.lastIndexOf("(");
						if (-1 != pos) {
							String fString=string.substring(pos+1);
							if (! fieldsList.contains(fString)) 
								fieldsList.add(fString);
						}
					}
					/**
					 * 暂时从这里控制报表主题
					 */
					if (fieldsList.size() > FIELD_MAPPING.size() 
							|| ! isMapContainsKeyByList(FIELD_MAPPING,fieldsList)) 
						continue;
					formatFields(fieldsList,FIELD_MAPPING);
					String []tableNames;
					String[]sqls;
					String[]categories;
					String tableName=tableNameByFileds(osTableFiledMap,fieldsList);
					String tableOther=tableOther(tableName);
					String condition=formatCriteria(criteria,FIELD_MAPPING);
					String category=criteriasElement.getAttribute("imp_reportname");
					
					String sql=selectColSql(en2Cn(category),"EVENT_NAME",tableName,condition);
					
					if(null != tableOther){
						String sqlOther = selectColSql("SRC_USER_NAME",tableOther);
						tableNames = new String[]{tableName,tableOther};
						sqls = new String[]{sql,sqlOther};
						categories = new String[]{"EVENT_NAME","SRC_USER_NAME"};
					}else {
						tableNames =new String[]{tableName};
						sqls = new String[]{sql};
						categories = new String[]{"EVENT_NAME"};
					}
						
					Map<String, Object> categoryMap = new HashMap<String, Object>(10);
					categoryMap.put("category", category);
					categoryMap.put("criteria", criteria);
					categoryMap.put("fields", fieldsList);
					categoryMap.put("condition", condition);
					categoryMap.put("tableNames", tableNames);
					categoryMap.put("sqls", sqls);
					categoryMap.put("categories", categories);
					categoryMap.put("tableName", tableName);
					categoryMap.put("sql", sql);
					categoryMap.put("otherCondition", RESOURCE_ID_QUERY);
					criterias.add(categoryMap);
				}
			}
				
		}
		return criterias;
	}
	
	private static void initReportGroupParents() {
		
		NodeList groups = rootElement.getElementsByTagName("ImpReportsConstants");
		int groupLength=groups.getLength();
		groupIdAndNameMap = new HashMap<String, String>(groupLength);
		
		for (int i = 0; i < groupLength; i++) {
			Element element=(Element)groups.item(i);

			String reportName = element.getAttribute("imp_reportname");
			String groupId = element.getAttribute("impgroupid");
			if(null != reportName && null != groupId )
				groupIdAndNameMap.put(groupId, reportName);
			
		}
		
	}
	private static String selectColSql(String category,String cateName,String tableName,String condition){
		StringBuffer sql=new StringBuffer("SELECT ");
		sql.append("'").append(category).append("' ").append(cateName)
		.append(",").append(" RESOURCE_ID,")
		.append("SUM(OPCOUNT) OPCOUNT FROM ").append(tableName);
		
		return sql.toString();
	}
	private static String selectColSql(String colName,String tableName){
		StringBuffer sql=new StringBuffer("SELECT ");
		sql.append(colName).append(",").append(" RESOURCE_ID,")
		.append("SUM(OPCOUNT) OPCOUNT FROM ")
		.append(tableName).append(" ");//.append(" WHERE ").append(condition)
		return sql.toString();
	}
	private static void formatFields(List<String> fieldList,Map<String, String> map){
		for (int i = 0; i < fieldList.size(); i++) {
			fieldList.set(i, map.get(fieldList.get(i)));
		}
	}
	private static String formatCriteria(String criteria,Map<String, String> map){
		String condition=criteria.replace(":", "=");
		condition=condition.replace("\"", "");
		for (Map.Entry<String, String> entry : map.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			condition=condition.replace("("+key, "("+value);
			
		}
		String patternOther="(\\s*=\\s*)([^()]*)(\\))";
		Pattern ptn=Pattern.compile(patternOther) ;
		if ( ptn.matcher(condition).find()) {
			String repalceString="$1'$2'$3";
			condition=condition.replaceAll(patternOther, repalceString);
		}
		
		return condition;
	}
	private static String tableNameByFileds(Map<String, List<String>> filedMap,List<String> fileds){
		
		for (Map.Entry<String, List<String>> entry : filedMap.entrySet()) {
			String key=entry.getKey();
			List<String> fieldList=entry.getValue();
			if (fileds.size() > fieldList.size()) 
				continue;
			
			if(fieldList.containsAll(fileds))
				return key;
		}
		return null;
	}
	private static boolean isMapContainsKeyByList(Map<?, ?>map,List<?>list){
		for (Object object : list) {
			if (! map.containsKey(object)) {
				return false;
			}
		}
		return true;
	}
	/*private static String filedMapping(String filed){
		if ("CATEGORY".equals(filed)) {
			"CAT1_ID"
		}else if ("SUBCATEGORY".equals(filed)) {
			"CAT2_ID"
		}else if ("USERACCOUNTCONTROL".equals(filed)) {
			
		}else if ("ACCOUNTEXPIRES".equals(filed)) {
			
		}else if ("LOGONHOURS".equals(filed)) {
			
		}else if ("WORKSTATION_NAME".equals(filed)) {
			
		}else if ("ERRORCODE".equals(filed)) {
			
		}else if ("OBJECTTYPE".equals(filed)) {
			
		}else if ("ACCESSES".equals(filed)) {
			
		}else if ("IENAME".equals(filed)) {
			
		}else if ("RISK_LEVEL".equals(filed)) {
			
		}else if ("MESSAGE".equals(filed)) {
			
		}
		return filed;
	}*/
	private static String tableOther(String tableName){
		if(GlobalUtil.isNullOrEmpty(tableName))
			return null;
		if("MSG_ID_LOGON_TYPE_HOUR".equalsIgnoreCase(tableName))
			return "MSG_ID_LOGON_USER_HOUR";
		else if("MSG_ID_CATEGORY_HOUR".equalsIgnoreCase(tableName))
			return "MSG_ID_CATEGORY_USER_HOUR";
		return null;
	}
	private static void initAndSetTableFiledMap(Map<String, List<String>> map){
		
		List<String> list = new ArrayList<String>(1);
		list.add("MSG_ID");
		map.put("MSG_ID_HOUR", list);
		
		list=new ArrayList<String>(2);
		list.add("MSG_ID");
		list.add("LOGON_TYPE");
		map.put("MSG_ID_LOGON_TYPE_HOUR", list);
		
		list=new ArrayList<String>(2);
		list.add("MSG_ID");
		list.add("DVC_EVENT_CATEGORY");
		map.put("MSG_ID_CATEGORY_HOUR", list);
		
		list=new ArrayList<String>(2);
		list.add("REQUEST_STATUS");
		map.put("REQUEST_STATUS_HOUR", list);
		
		list=new ArrayList<String>(3);
		list.add("MSG_ID");
		list.add("CAT1_ID");
		list.add("CAT2_ID");
		map.put("MSG_ID_STATUS_HOUR", list);
		
	}
}
