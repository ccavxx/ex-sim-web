package com.topsec.tsm.sim.sysconfig.service;

/**
 * @ClassName: ExcelImportExportPOI
 * @Description: 负责关联规则信息的导入、导出
 * 
 * @author: horizon create on 2014年11月12日12:13:03
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.event.EventRule;
import com.topsec.tsm.sim.event.EventRuleDispatch;
import com.topsec.tsm.sim.event.EventRuleGroup;

public class ExcelImportPOI extends ExcelImportExportBasePOI{
	
	private static final Logger log = LoggerFactory.getLogger(ExcelImportPOI.class) ;

	private EventRuleService eventRuleService = null;
	private SID sid = null;
	
	private InputStream instream = null;
	private Workbook workbook = null;
	private boolean isNotOurExcel = false;
	
	private StringBuilder errorMsg = null;
	private StringBuilder errorMsg2 = new StringBuilder();
	private StringBuilder groupNames = null;
	
	public String getErrorMsg() {
		return errorMsg.append(errorMsg2).toString();
	}
	private void setErrorMsg() {
		this.errorMsg = new StringBuilder();
	}
	
	public StringBuilder getGroupNames() {
		return groupNames;
	}
	private void setGroupNames() {
		this.groupNames = new StringBuilder();
	}
	/**
	 * 生成要导出的 excel 文件对象、初始化列表头样式
	 */
	public ExcelImportPOI() {}
	/**
	 * 生成 ExcelImportExportPOI 实例对象
	 * @return
	 */
	public void init(MultipartFile file) {
		
		setErrorMsg();
		setGroupNames();
		try {
			instream = file.getInputStream();
			workbook = Workbook.getWorkbook(instream);
		} catch (BiffException e) {
			errorMsg.append("\\n 读取excel文件错误");
			log.warn(e.getMessage());
		} catch (IOException e) {
			errorMsg.append("\\n 读取excel文件时，文件被删除");
			log.warn(e.getMessage());
		}
	}
    private void setCorrRuleConfig(Sheet readsheet) {
        try {

            // 获取Sheet表中所包含的总行数
        	int rsRows = readsheet.getRows();
    		if(rsRows < 2) {
    			return;
    		}
        	// 验证改文件格式的是否正确
        	setNotOurExcel(readsheet);
        	if(isNotOurExcel()) {
        		return;
        	}

        	for(int i = 1; i < rsRows; i++) {
        		
        		String groupNameContents = readsheet.getCell(0, i).getContents();
        		if(groupNameContents == null) {
        			errorMsg2.append("\\n 关联规则名称为空：第" + i + 1 + "行");
        			continue;
        		}
            	String groupName = groupNameContents.trim();
        		Map<String, Object> condition = new HashMap<String, Object>();
        		condition.put("groupName", groupName);
        		int count = eventRuleService.countEventRuleGroupByConditon(condition);
        		
        		// 验证该名称规则是否存在
        		if(count != 0) {
        			errorMsg2.append("\\n \"" + groupName + "\" 关联规则已存在");
        			continue;
        		}
        		// 关联基本属性
        		EventRuleGroup eventRuleGroup = new EventRuleGroup();
        		
        		eventRuleGroup.setGroupName(groupName);// 关联名称
        		
        		String cat1idNameContents = readsheet.getCell(1, i).getContents();
        		if(cat1idNameContents == null) {
        			errorMsg2.append("\\n 关联规则 " + BASE_TITLE_FIRSTTYPE + " 为空：第" + groupName + "行");
        			continue;
        		}
        		eventRuleGroup.setCat1id(cat1idNameContents.trim());// 一级分类
        		
        		String cat2idNameContents = readsheet.getCell(2, i).getContents();
        		if(cat2idNameContents == null) {
        			errorMsg2.append("\\n 关联规则 " + BASE_TITLE_SECONDTYPE + " 为空：第" + groupName + "行");
        			continue;
        		}
        		eventRuleGroup.setCat2id(cat2idNameContents.trim());// 二级分类
        		
        		String priorityContents = readsheet.getCell(3, i).getContents();
        		if(priorityContents == null) {
        			errorMsg2.append("\\n 关联规则 " + BASE_TITLE_LEVEL + " 为空：第" + groupName + "行");
        			continue;
        		}
        		eventRuleGroup.setPriority(Integer.valueOf(priorityContents.trim()));// 优先级
        		
        		String timeoutContents = readsheet.getCell(4, i).getContents();
        		if(timeoutContents == null) {
        			errorMsg2.append("\\n 关联规则 " + BASE_TITLE_OVERTIME + " 为空：第" + groupName + "行");
        			continue;
        		}
        		eventRuleGroup.setTimeout(Integer.valueOf(timeoutContents.trim()));// 超时
        		
        		String statusContents = readsheet.getCell(5, i).getContents();
        		if(statusContents == null) {
        			errorMsg2.append("\\n 关联规则 " + BASE_TITLE_START + " 为空：第" + groupName + "行");
        			continue;
        		}
        		eventRuleGroup.setStatus(Integer.valueOf(statusContents.trim()));// 启用禁用
        		
        		String descContents = readsheet.getCell(6, i).getContents();
        		if(descContents == null) {
        			errorMsg2.append("\\n 关联规则 " + BASE_TITLE_DESC + " 为空：第" + groupName + "行");
        			continue;
        		}
        		eventRuleGroup.setDesc(descContents.trim());
        		
        		// 读取规则视图模块信息
        		String eventRulesContents = readsheet.getCell(7, i).getContents();
        		if(eventRulesContents == null) {
        			errorMsg2.append("\\n 关联规则 " + EVENTRULE_TITLE + " 为空：第" + groupName + "行");
        			continue;
        		}
        		
        		// 读取规则视图模块关系信息
        		String eventruledispatchsContents = readsheet.getCell(8, i).getContents();
        		if(eventruledispatchsContents == null) {
        			errorMsg2.append("\\n 关联规则 " + EVENTRULEDISPATCH_TITLE + " 为空：第" + groupName + "行");
        			continue;
        		}
        		
        		eventRuleGroup.setCreateTime(new Date());// 添加日期
        		eventRuleGroup.setAlarmState(0);// 告警
        		
        		String username = sid.getUserName();
        		eventRuleGroup.setCreater(username);
    			eventRuleGroup.setIsSystem(sid.isOperator() ? 1 : 0);
    			
        		// 保存关联规则
        		Integer groupId = eventRuleService.addEventRuleGroup(eventRuleGroup);
        		
        		// 保存规则视图模块信息
        		Map<String, String> ruleIdMap = new HashMap<String, String>();// 新旧 ruleId 映射表
        		
        		JSONArray eventRuleArray = JSONArray.parseArray(eventRulesContents.trim());
        		
        		for(Object eventRuleObject : eventRuleArray) {
        			JSONObject eventRuleJSONObject = (JSONObject)eventRuleObject;
        			
                	EventRule eventRule = new EventRule();
                	eventRule.setRuleNum(eventRuleJSONObject.getString(EVENTRULE_TITLE_RULENUM));
                	eventRule.setName(eventRuleJSONObject.getString(EVENTRULE_TITLE_NAME));
                	eventRule.setRuleTemplate(eventRuleJSONObject.getString(EVENTRULE_TITLE_RULETEMPLATE));
                	eventRule.setAlarmState(eventRuleJSONObject.getInteger(EVENTRULE_TITLE_ALARMSTATE));
                	eventRule.setCreateTime(new Date());
                	
                	String ruleIdTemp = eventRuleJSONObject.getString(EVENTRULE_TITLE_ID);
                	
                	Integer ruleId = eventRuleService.saveEventRule(eventRule);
                	ruleIdMap.put(ruleIdTemp, ruleId.toString());
        		}

        		// 保存规则视图模块关系信息·关联多规则关系
        		JSONArray eventruledispatchArray  = JSONArray.parseArray(eventruledispatchsContents.trim());
        		
        		List<EventRuleDispatch> eventRuleDispatchList = new ArrayList<EventRuleDispatch>();
        		
        		for(Object eventRuleDispatchObject : eventruledispatchArray) {
        			
        			JSONObject eventRuleDispatchJSONObject = (JSONObject)eventRuleDispatchObject;
        			
        			EventRuleDispatch eventRuleDispatch = new EventRuleDispatch();

        			eventRuleDispatch.setRuleId(Integer.valueOf(ruleIdMap.get(eventRuleDispatchJSONObject.getString(EVENTRULEDISPATCH_TITLE_RULEID))));
        			eventRuleDispatch.setOrder(eventRuleDispatchJSONObject.getInteger(EVENTRULEDISPATCH_TITLE_ORDER));
        			eventRuleDispatch.setComparatorTemplate(eventRuleDispatchJSONObject.getString(EVENTRULEDISPATCH_TITLE_COMPARATORTEMPLATE));
        			eventRuleDispatch.setTimeout(eventRuleDispatchJSONObject.getInteger(EVENTRULEDISPATCH_TITLE_TIMEOUT));
        			eventRuleDispatch.setGroupId(groupId);
        			
        			eventRuleDispatchList.add(eventRuleDispatch);
                	
        		}
        		
        		EventRuleDispatch[] eventRuleDispatchArray = new EventRuleDispatch[eventRuleDispatchList.size()];
        		eventRuleDispatchList.toArray(eventRuleDispatchArray);
        		eventRuleService.associate2EventRuleGroup(eventRuleDispatchArray);
        		
            	groupNames.append(groupName);
            	if(i != rsRows - 1) {
            		groupNames.append(",");
            	}
        	}

       } catch (Exception e) {
    	   	e.printStackTrace();
			errorMsg.append("\\n 读取" + readsheet.getName() + "发生错误");
			log.warn("读取" + readsheet.getName() + "工作表时，发生错误：" + e.getMessage());
       }
    }
	/**
	 * 解析关联规则 excel 文件
	 * @param file
	 * @param sid
	 * @return
	 */
	public void xlsToCorrRuleConfig(EventRuleService eventRuleService, SID sid) {
        if(errorMsg.length() > 0) {
        	return;
        }
		this.eventRuleService = eventRuleService;
		this.sid = sid;
		
		try {
			Sheet[] sheets = workbook.getSheets();
			for(int i = 0, lg = sheets.length; i < lg; i++) {
				Sheet sheet = sheets[i];
				setCorrRuleConfig(sheet);
				
		        if(errorMsg.length() > 0 ) {
		        	break;
		        }
				if(isNotOurExcel()) {
					errorMsg.append("\\n excel 格式不正确");
					log.warn("构建 导入 数据时发生错误:excel 格式不正确");
					break;
				}
			}
		} catch (Exception e) {
			errorMsg.append("\\n 构建 导入 数据时发生错误");
			log.warn("构建 导入 数据时发生错误:" + e.getMessage());
		} finally {
			closeWorkbook();
			closeInstream();
		}
	}
	/**
	 * 关闭 InputStream
	 * @param instream
	 */
	private void closeInstream() {
		if(instream != null) {
			try {
				instream.close();
			} catch (IOException e) {
				errorMsg.append("\\n instream 关闭失败");
				log.warn(e.getMessage());
			}
		}
	}
	/**
	 * 关闭 workbook
	 * @param workbook
	 */
	private void closeWorkbook() {
		if(workbook != null) {
			workbook.close();
		}
	}
	/**
	 * 验证 excel 格式是否正确
	 * @return
	 */
	private boolean isNotOurExcel() {
		return isNotOurExcel;
	}
	/**
	 * 判断 验证 excel 格式是否正确
	 * @return
	 */
	private void setNotOurExcel(Sheet readsheet) {
		
		String nameTitle = readsheet.getCell(0, 0).getContents();
		if(StringUtil.isBlank(nameTitle) || !BASE_TITLE_NAME.equals(nameTitle.trim())) {
			this.isNotOurExcel = true;
			return;
		}
		String firsttypeTitle = readsheet.getCell(1, 0).getContents();
		if(StringUtil.isBlank(firsttypeTitle) || !BASE_TITLE_FIRSTTYPE.equals(firsttypeTitle.trim())) {
			this.isNotOurExcel = true;
			return;
		}
		String sencondtypeTitle = readsheet.getCell(2, 0).getContents();
		if(StringUtil.isBlank(sencondtypeTitle) || !BASE_TITLE_SECONDTYPE.equals(sencondtypeTitle.trim())) {
			this.isNotOurExcel = true;
			return;
		}
		String levelTitle = readsheet.getCell(3, 0).getContents();
		if(StringUtil.isBlank(levelTitle) || !BASE_TITLE_LEVEL.equals(levelTitle.trim())) {
			this.isNotOurExcel = true;
			return;
		}
		String overtimeTitle = readsheet.getCell(4, 0).getContents();
		if(StringUtil.isBlank(overtimeTitle) || !BASE_TITLE_OVERTIME.equals(overtimeTitle.trim())) {
			this.isNotOurExcel = true;
			return;
		}
		String startTitle = readsheet.getCell(5, 0).getContents();
		if(StringUtil.isBlank(startTitle) || !BASE_TITLE_START.equals(startTitle.trim())) {
			this.isNotOurExcel = true;
			return;
		}
		String descTitle = readsheet.getCell(6, 0).getContents();
		if(StringUtil.isBlank(descTitle) || !BASE_TITLE_DESC.equals(descTitle.trim())) {
			this.isNotOurExcel = true;
			return;
		}
		String eventruleTitle = readsheet.getCell(7, 0).getContents();
		if(StringUtil.isBlank(eventruleTitle) || !EVENTRULE_TITLE.equals(eventruleTitle.trim())) {
			this.isNotOurExcel = true;
			return;
		}
		String eventruledispatchTitle = readsheet.getCell(8, 0).getContents();
		if(StringUtil.isBlank(eventruledispatchTitle) || !EVENTRULEDISPATCH_TITLE.equals(eventruledispatchTitle.trim())) {
			this.isNotOurExcel = true;
			return;
		}
	}
	
}
