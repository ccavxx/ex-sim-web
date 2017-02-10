package com.topsec.tsm.sim.sysconfig.service;

/**
 * @ClassName: ExcelExportPOI
 * @Description: 负责关联规则信息的导出
 * 
 * @author: horizon create on 2014年11月12日12:13:03
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.event.EventRule;
import com.topsec.tsm.sim.event.EventRuleDispatch;
import com.topsec.tsm.sim.event.EventRuleGroup;

public class ExcelExportPOI extends ExcelImportExportBasePOI {
	
	public final String FILENAME_PRENAME = "关联分析规则";
	
	private HSSFCellStyle titleStyle = null;
	
	private String excelFileName = null;
	private EventRuleService eventRuleService = null;
	
	private HSSFWorkbook workbook = null;
	
	/**
	 * 生成要导出的 excel 文件对象、初始化列表头样式
	 */
	private ExcelExportPOI() {
		workbook = new HSSFWorkbook();
		setTitleCellStyles(workbook);
	}
	/**
	 * 生成 ExcelImportExportPOI 实例对象
	 * @return
	 */
	public static ExcelExportPOI newInstance() {
		return new ExcelExportPOI();
	}
	/**
	 * 获得整体数据、定义文件名称、创建books
	 * @param eventRuleService 关联规则 service
	 * @param eventResponseService 告警方式 service
	 * @param knowledgeService 知识库 service
	 */
	public HSSFWorkbook getWorkbook(EventRuleService eventRuleService, SID sid) {
		
		this.eventRuleService = eventRuleService;
		String username = sid.getUserName();
		// 获得整体数据
		List<EventRuleGroup> groupListTemp = new ArrayList<EventRuleGroup>();
		List<EventRuleGroup> groupList = eventRuleService.getAllEventRuleGroups();
		if(sid.isOperator()) {
			groupListTemp = groupList;
		} else {
			for(int i = 0; i < groupList.size(); i++) {
				EventRuleGroup group = groupList.get(i);
				String createrName = group.getCreater();
				if(username.equals(createrName)) {
					groupListTemp.add(groupList.get(i));
				}
			}
		}
		
		// 定义文件名称
		setExcelFileName(groupListTemp.size());
		// 创建sheets
		createHSSFSheets(groupListTemp);
		
		return workbook;
	}
	/**
	 * 获得构建的 excel 文件名称
	 * @return
	 */
	private String getExcelFileName() {
		return excelFileName;
	}
	/**
	 * 创建工作表sheets
	 * @param groupListTemp
	 * @return 工作表集合
	 */
	private void createHSSFSheets(List<EventRuleGroup> groupListTemp) {
		// 创建工作表对象
		HSSFSheet sheet = workbook.createSheet(getExcelFileName());
		setCellWidths(sheet);// 设置列宽
		int rowNum = creatInfRowHead(sheet, 0);
		
		for(int i = 0, lg = groupListTemp.size(); i < lg; i++) {
			rowNum += 1;
			HSSFRow row = sheet.createRow(rowNum);
			EventRuleGroup group = groupListTemp.get(i);

			HSSFCell nameCell = row.createCell(0);
			nameCell.setCellValue(group.getGroupName());

			HSSFCell firstTypeCell = row.createCell(1);
			firstTypeCell.setCellValue(group.getCat1id());

			HSSFCell secondTypeCell = row.createCell(2);
			secondTypeCell.setCellValue(group.getCat2id());

			HSSFCell levelCell = row.createCell(3);
			levelCell.setCellValue(group.getPriority());

			HSSFCell overTimeCell = row.createCell(4);
			overTimeCell.setCellValue(group.getTimeout());

			HSSFCell startCell = row.createCell(5);
			startCell.setCellValue(group.getStatus());

			HSSFCell descCell = row.createCell(6);
			descCell.setCellValue(group.getDesc());
			
			// 规则信息
			JSONArray eventRuleJson = getEventRuleJson(group.getGroupId());
			HSSFCell eventRuleJsonCell = row.createCell(7);
			eventRuleJsonCell.setCellValue(eventRuleJson.toString());
			
			// 关联信息
			JSONArray eventRuleDispatchJson = getEventRuleDispatchJson(group.getGroupId());
		    HSSFCell relasJsonCell = row.createCell(8);
		    relasJsonCell.setCellValue(eventRuleDispatchJson.toString());
			
		}
	}
	/**
	 * 构建excel文件名称（组成：TA_L关联分析规则_时间(规则数量)）
	 * @param count
	 * @return
	 */
	private void setExcelFileName(int count) {
		StringBuilder fileName = new StringBuilder(FILENAME_PRENAME);
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		fileName.append("_");
		fileName.append(df.format(new Date()));
		fileName.append("(");
		fileName.append(count);
		fileName.append("条)");
		this.excelFileName = fileName.toString();
	}
	/**
	 * 列头样式 (背景蓝灰色、字体居中、宋体、自动换行)
	 * @param workbook
	 */
	private void setTitleCellStyles(HSSFWorkbook workbook) {
		
		titleStyle = workbook.createCellStyle();
		
		//设置背景色
		titleStyle.setFillForegroundColor(HSSFColor.BLUE_GREY.index);
		titleStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		//设置居中
		titleStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		//设置字体
		HSSFFont font = workbook.createFont();
		font.setColor(HSSFColor.WHITE.index);
		font.setFontName("宋体");
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		font.setFontHeightInPoints((short) 11); //设置字体大小
		titleStyle.setFont(font);//选择需要用到的字体格式
		//设置自动换行
		titleStyle.setWrapText(true);
	}
	/**
	 * 设置列宽
	 * @param sheet
	 */
	private void setCellWidths(HSSFSheet sheet) {
		// 设置列宽 ,第一个参数代表列id(从0开始)，第2个参数代表宽度值
		sheet.setColumnWidth(0, 180*38);
		sheet.setColumnWidth(1, 150*38);
		sheet.setColumnWidth(2, 150*38);
		sheet.setColumnWidth(3, 80*38);
		sheet.setColumnWidth(4, 80*38);
		sheet.setColumnWidth(5, 100*38);
		sheet.setColumnWidth(6, 300*38);
		sheet.setColumnWidth(7, 200*38);
		sheet.setColumnWidth(8, 300*38);
	}
	/**
	 * 创建信息列头
	 * @param sheet
	 * @param rowNum 最后插入信息的行号
	 * @return 最后插入信息的行号
	 */
	private int creatInfRowHead(HSSFSheet sheet, int rowNum) {
		
		HSSFRow titleRow = sheet.createRow(rowNum);
		
		HSSFCell nameCell = titleRow.createCell(0);
		nameCell.setCellValue(BASE_TITLE_NAME);
		nameCell.setCellStyle(titleStyle);
		
		HSSFCell firstTypeCell = titleRow.createCell(1);
		firstTypeCell.setCellValue(BASE_TITLE_FIRSTTYPE);
		firstTypeCell.setCellStyle(titleStyle);
		
		HSSFCell secondTypeCell = titleRow.createCell(2);
		secondTypeCell.setCellValue(BASE_TITLE_SECONDTYPE);
		secondTypeCell.setCellStyle(titleStyle);
		
		HSSFCell levelCell = titleRow.createCell(3);
		levelCell.setCellValue(BASE_TITLE_LEVEL);
		levelCell.setCellStyle(titleStyle);
		
		HSSFCell overTimeCell = titleRow.createCell(4);
		overTimeCell.setCellValue(BASE_TITLE_OVERTIME);
		overTimeCell.setCellStyle(titleStyle);
		
		HSSFCell startCell = titleRow.createCell(5);
		startCell.setCellValue(BASE_TITLE_START);
		startCell.setCellStyle(titleStyle);
		
		HSSFCell descCell = titleRow.createCell(6);
		descCell.setCellValue(BASE_TITLE_DESC);
		descCell.setCellStyle(titleStyle);
		
		HSSFCell eventRuleCell = titleRow.createCell(7);
		eventRuleCell.setCellValue(EVENTRULE_TITLE);
		eventRuleCell.setCellStyle(titleStyle);
		
		HSSFCell eventruledispatchCell = titleRow.createCell(8);
		eventruledispatchCell.setCellValue(EVENTRULEDISPATCH_TITLE);
		eventruledispatchCell.setCellStyle(titleStyle);
		
		return rowNum;
	}
	/**
	 * 返回 EventRule 的json数据
	 * @param groupId
	 * @return 转化的json数据
	 */
	private JSONArray getEventRuleJson(Integer groupId) {
		List<EventRule> eventRuleList = eventRuleService.getEventRulesByGroupId(groupId);
		JSONArray result = new JSONArray();
		for(EventRule eventRule : eventRuleList) {
			JSONObject eventRuleTemp = new JSONObject();
			
			eventRuleTemp.put(EVENTRULE_TITLE_RULENUM, eventRule.getRuleNum());
			eventRuleTemp.put(EVENTRULE_TITLE_NAME, eventRule.getName());
			eventRuleTemp.put(EVENTRULE_TITLE_RULETEMPLATE, eventRule.getRuleTemplate());
			eventRuleTemp.put(EVENTRULE_TITLE_ALARMSTATE, eventRule.getAlarmState());
			eventRuleTemp.put(EVENTRULE_TITLE_ID, eventRule.getId());
			
			result.add(eventRuleTemp);
		}
		return result;
	}
	/**
	 * 返回 EventRuleDispatch 的json数据
	 * @param groupId
	 * @return 转化的json数据
	 */
	private JSONArray getEventRuleDispatchJson(Integer groupId) {
		
		List<EventRuleDispatch> relasList = eventRuleService.getEventDispatchByGroupId(groupId);
		
		JSONArray result = new JSONArray();
		for(EventRuleDispatch eventRuleDispatch : relasList) {
			
			JSONObject eventRuleDispatchTemp = new JSONObject();
			
			eventRuleDispatchTemp.put(EVENTRULEDISPATCH_TITLE_RULEID, eventRuleDispatch.getRuleId());
			eventRuleDispatchTemp.put(EVENTRULEDISPATCH_TITLE_ORDER, eventRuleDispatch.getOrder());
			eventRuleDispatchTemp.put(EVENTRULEDISPATCH_TITLE_COMPARATORTEMPLATE, eventRuleDispatch.getComparatorTemplate());
			eventRuleDispatchTemp.put(EVENTRULEDISPATCH_TITLE_TIMEOUT, eventRuleDispatch.getTimeout());
			
			result.add(eventRuleDispatchTemp);
		}
		return result;
	}
	
}
