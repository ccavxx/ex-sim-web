package com.topsec.tsm.sim.report.common;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.topsec.tal.base.report.service.RptMasterTbService;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.Parameter;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.report.bean.struct.SqlStruct;
import com.topsec.tsm.sim.report.model.ChartData;
import com.topsec.tsm.sim.report.model.ReportDispatchModel;
import com.topsec.tsm.sim.report.persistence.RptRule;
import com.topsec.tsm.sim.report.service.ReportService;
import com.topsec.tsm.sim.report.util.ChartCategoryFormatter;
import com.topsec.tsm.sim.report.util.ChartConstant;
import com.topsec.tsm.sim.report.util.ReportUiConfig;
import com.topsec.tsm.sim.report.util.ReportUiUtil;
import com.topsec.tsm.sim.report.util.ThreadPoolExecuteDispatchUtil;
import com.topsec.tsm.sim.util.DeviceTypeNameUtil;

@SuppressWarnings("unchecked")
public class SubjectModel {

	/**
	 * 主题id
	 */
	private int subjectId ;

	/**
	 * 参数
	 */
	private Parameter parameter ;
	/**
	 * 主题信息
	 */
	private Map subject ;
	
	public SubjectModel(int subjectId, Parameter parameter) {
		this.subjectId = subjectId;
		this.parameter = parameter;
		subject = getSubjectById(subjectId, parameter.getValue("deviceType")) ;
	}
	/**
	 * 根据主题id获得Map结构的主题
	 * @param subjectId
	 * @param deviceType
	 * @return
	 */
	private Map getSubjectById(int subjectId,String deviceType){
		RptMasterTbService rptMasterTbImp = (RptMasterTbService) SpringContextServlet.springCtx.getBean(ReportUiConfig.MstBean);
		List<Map<Object,Object>> subList = rptMasterTbImp.queryTmpList(ReportUiConfig.GetSub, new Object[]{subjectId}) ;
		if(ObjectUtils.isNotEmpty(subList)){
			Map subject = subList.get(0) ;
			subject.put("mstType", 2) ;//只有自定义报表
			subject.put("deviceType", deviceType) ;
			setAxisLabel(subject) ;
			return subject ;
		}
		return null ;
	}
	/**
	 * 
	 * @param subject
	 */
	private void setAxisLabel(Map subject){
		Object axisLabel = subject.get("tableLable");
		String[] xy = StringUtil.split((String)axisLabel);
		subject.put("categoryAxisLabel", xy.length==2 ? xy[0] : "") ;
		subject.put("valueAxisLabel", xy.length==2 ? xy[1] : "") ;
	}
	/**
	 * 获得主题规则信息
	 * @param subject
	 * @param model
	 * @param rptMasterTbImp
	 * @return
	 */
	public List<RptRule> getSubjectRules(){
		ReportService rptService = (ReportService) SpringContextServlet.springCtx.getBean("reportService");
		List<RptRule> ruleResult =   rptService.getAllRules((Integer)subject.get("subId"));
		boolean isCoreNode = ReportUiUtil.isCoreNodeReport(subject);
		if (isCoreNode) {
			for (int j = 0; j < ruleResult.size(); j++) {
				RptRule rule = ruleResult.get(j);
				String ruleCondition = rule.getSqlParam() ;
				if (ObjectUtils.equalsAny(ruleCondition, "and dvcAddress = ?","and alias.dvcAddress = ?","and fwrisk.dvcAddress = ?")) {
					ruleResult.remove(j);
					break;
				}
			}
		}
		return ruleResult ;
	}		
	/**
	 * 获得主题FusionChart数据结构
	 * @return
	 * @throws Exception 
	 */
	public String getFusionChartXML() throws Exception{
		ArrayList<ChartData> list = getChartData() ;
		String subrptName = subject.get("subName") + "";
		Integer subType = (Integer)subject.get("subType") ;
		if (subType != 2) {
			String type = ReportUiConfig.GraphType.get(subject.get("chartType"));
			StringBuffer dataXml = new StringBuffer();
			if (type.equals(ChartConstant.Type_BarChart)) {
				appendBarChartXML(dataXml, subrptName, getCategoryAxisLabel(), getValueAxisLabel(), list) ;
			} else if (type.equals(ChartConstant.Type_CyliderChart)) {

			} else if (type.equals(ChartConstant.Type_StackedChart)) {

			} else if (type.equals(ChartConstant.Type_LineChart)) {
				appendAreaChartXML(dataXml, list) ;
			} else if (type.equals(ChartConstant.Type_PieChart)) {
				appendPieChartXML(dataXml, list) ;
			}
			return dataXml.toString() ;
		}
		return "" ;
	}

	/**
	 * 根据规则创建SqlStruts结构
	 * @param rules
	 * @param parameter
	 * @return
	 */
	public SqlStruct getSqlStruct(List rules,Parameter parameter){
		SqlStruct sqlStruct = getHqlTerm(rules,parameter);
		if (sqlStruct.getDvcIp() == null){
			sqlStruct.setDvcIp("127.0.0.1");
		} 
		return sqlStruct ;
	}
	/**
	 * 获得主题ChartData结构数据
	 * @return
	 * @throws Exception
	 */
	public ArrayList<ChartData> getChartData()throws ReportException{
		Integer subType = (Integer)subject.get("subType") ;
		try {
			List<RptRule> rules = getSubjectRules() ;
			SqlStruct sqlStruct = getSqlStruct(rules, parameter) ;
			boolean onlyByDvctype="onlyByDvctype".equals(parameter.getValue("onlyByDvctype"));
			String runSql = getRunSql(subType);// 获取sql
			String sql = (String)subject.get(runSql);
			String tableName=(String)subject.get("subTableName");
			String sTime = sqlStruct.getsTime();
			String eTime = sqlStruct.geteTime();
			// 根据时间区间来确定要查询的表
			sql = getTimeSql(sql, sTime, eTime);
			String[] nodeIds=parameter.getValues("nodeId"); 
			sql += sqlStruct.getSql();
			if (sql.indexOf("union") != -1&&onlyByDvctype) {
				sql=sql.replace("DVC_ADDRESS=?", " ("+getDvcIp(parameter.getValue("deviceType"),"DVC_ADDRESS =")+")");
			}
			// 执行获取转换后的Sql
			List resultValue = getList(sql, sqlStruct.getSqlparam(), sqlStruct.getSqlpage(),tableName,nodeIds,parameter);
			// 总记录数 是否显示更多
			int sumPage=ObjectUtils.nvl((Integer)resultValue.get(0), 5) ;
			// DB返回的 详细数据
			List result = (List) resultValue.get(1);
			String chartItem = StringUtil.nvl((String)subject.get("chartItem"));
			String[] chartItems=chartItem.split(",");
			String categorys = (String)subject.get("category");
			/*String category = (String)subMap.get("category");*/
			String category = categorys.indexOf("1") == -1 ? categorys : categorys.split("|")[0];

			boolean qushiFlag = StringUtil.booleanVal(subject.get("chartProperty"));// 趋势报表
			if (qushiFlag)// 针对趋势报表重构数据
			{
				String tableType = ReportUiUtil.getTable(sql);
				String mstType = subject.get("mstType").toString();
				result = changeResultPro(result, sTime, eTime, tableType,category, chartItem, sqlStruct.getDvcIp(), mstType);
				sumPage = result.size();// 趋势报表增加的数据
			}else{
				Map<String,Object> map=null;
				if (sql.indexOf("union") != -1) {
					map=reformingStatisticDataForUnion(result, category, chartItems);
				}else if(subType==5){
					String tmpCate = categorys.split("&")[1];
					String catestr = categorys.split("&")[0];
					map = reformingStatisticDataForSimpleMultidata(result, catestr.split(","), chartItems, tmpCate.split(","),sumPage,false);
				}else {
					map=reformingStatisticDataForTop(result,category,chartItems,sumPage,false);
				}
				
				List reformingResult=(List)map.get("result");
				int reformingSumPage=(Integer)map.get("sumPage");
				result=reformingResult;
				sumPage=reformingSumPage;
			}
			ArrayList<ChartData> list = new ArrayList<ChartData>();
			String picFiled[] = ((String) subject.get("chartItem")).split(",");
			
			int countSign = -1;// 决定显示 kb mb
			double tmpCountSign = 0;
			boolean bCountSing = true;

			for (int i = 0; i < result.size(); i++) {
				Map dataMap = (Map) result.get(i);
				for (int j = 0; j < picFiled.length && subType != 2; j++) {// 图片
																			 
					tmpCountSign = ReportUiUtil.getUnitValue(picFiled[j], dataMap);
					if (bCountSing && tmpCountSign > 0) {
						countSign = ReportUiUtil.getCountCapability(tmpCountSign);
						bCountSing = false;
					}
					list.add(getChartDate(subject, dataMap, countSign, picFiled[j]));
				}
			}
			
			ReportUiUtil.getUnit(subject, countSign);// 获取chart title
			return list ;
		} catch (Exception e) {
			throw new ReportException(e.getMessage()) ;
		}
	}
	
	public List getData(){
		Integer subType = (Integer)subject.get("subType") ;
		try {
			List<RptRule> rules = getSubjectRules() ;
			SqlStruct sqlStruct = getSqlStruct(rules, parameter) ;
			boolean onlyByDvctype="onlyByDvctype".equals(parameter.getValue("onlyByDvctype"));
			String runSql = getRunSql(subType);// 获取sql
			String sql = (String)subject.get(runSql);
			String tableName=(String)subject.get("subTableName");
			String sTime = sqlStruct.getsTime();
			String eTime = sqlStruct.geteTime();
			// 根据时间区间来确定要查询的表
			sql = getTimeSql(sql, sTime, eTime);
			String[] nodeIds=parameter.getValues("nodeId"); 
			sql += sqlStruct.getSql();
			if (sql.indexOf("union") != -1&&onlyByDvctype) {
				sql=sql.replace("DVC_ADDRESS=?", " ("+getDvcIp(parameter.getValue("deviceType"),"DVC_ADDRESS =")+")");
			}
			// 执行获取转换后的Sql
			List resultValue = getList(sql, sqlStruct.getSqlparam(), sqlStruct.getSqlpage(),tableName,nodeIds,parameter);
			List allNodeResult = (List) resultValue.get(1) ;
			if(ObjectUtils.isNotEmpty(allNodeResult)){
				return (List) allNodeResult.get(0) ; 
			}
			return Collections.emptyList() ;
		}catch(Exception e){
			e.printStackTrace() ;
			return Collections.emptyList() ;
		}
	}
	
/*	public String getJFreeChartFile(Map subject,List rules,HttpServletRequest request,Parameter parameter)throws Exception{
		ArrayList<ChartData> chartData = getChartData(subject, rules, (Integer)subject.get("subType"), parameter) ;
		ReportTalChart chart = new ReportTalChart();
		return chart.creChart(request, null, chartData, subject, rules).toString() ;
	}
*/
	/**
	 * 创建柱图
	 * @param dataXml
	 * @param subrptName
	 * @param x
	 * @param y
	 * @param list
	 */
	private void appendBarChartXML(StringBuffer dataXml,String subrptName,String x,String y,List<ChartData> list){
		dataXml.append("<chart showAboutMenuItem='0' chartNoDataText='没有数据' maxColWidth='45' caption='" + subrptName + "' labelDisplay='Stagger' staggerLines='1' useRoundEdges='1' ");
		dataXml.append("xAxisName='" + x + "' ");
		dataXml.append("yAxisName='" + y + "' ");
		dataXml.append("showValues='1' ");
		dataXml.append("decimals='0' ");
		dataXml.append("plotSpacePercent='50' ");
		dataXml.append("formatNumberScale='0'>");
		for (ChartData chartData : list) {
			dataXml.append("<set label='" + chartData.getSerise() + "' value='" + chartData.getValue() + "' />");
		}
		dataXml.append("</chart>");
	}
	/**
	 * 创建趋势图
	 * @param dataXml
	 * @param list
	 */
	private void appendAreaChartXML(StringBuffer dataXml,List<ChartData> list){
		List<String> seriseList = new ArrayList<String>();
		List<String> categoryList = new ArrayList<String>();
		for (ChartData chartData : list) {
			String serise = chartData.getSerise();
			String category = chartData.getCategory();
			if(category.length()>19){
				category = category.substring(0, 19) ;//截断2013-01-01 00:00:00.0后面的.0统一数据格式
			}
			if (!seriseList.contains(serise)) {
				seriseList.add(serise);
			}
			if (!categoryList.contains(category)) {
				categoryList.add(category);
			}
		}
		dataXml.append("<chart showAboutMenuItem='0' caption='' subCaption='' ")
			   .append("numdivlines='8' showZeroPlane='0' showValues='0' numVDivLines='24' ")
			   .append("showAlternateVGridColor='0' plotGradientColor='' ")
			   .append("numVisiblePlot='").append(ChartCategoryFormatter.formatCategoryList(categoryList)).append("'")
			   .append("plotFillAlpha='60' palette='4' canvasPadding='40'>") ;
	    dataXml.append("<categories>");
	    boolean isPrios=false;
	    for (String seri : seriseList) {
			if ("非常危险,高危险,一般危险,低危险,无危险".equals(seri)) {
				isPrios=true;
				break;
			}
		}
	    if (isPrios) {
	    	for (int i = 0; i < 5; i++) {
	    		for (String categoryLabel:categoryList) {
	    			dataXml.append("<category label='").append(categoryLabel).append("'/>");
				}
			}
	    	
		}else {
			for (String categoryLabel:categoryList) {
				dataXml.append("<category label='").append(categoryLabel).append("'/>");
			}	
		}
		

		dataXml.append("</categories>");
		for (String seri : seriseList) {
			dataXml.append("<dataset seriesName='").append(seri).append("'  anchorBorderColor='2AD62A' anchorBgColor='2AD62A'>");
			if (isPrios) {
				for (int j = 0; j < 5; j++) {
					for (int i = 0; i+j < list.size(); i+=5) {
						ChartData chartData=list.get(i+j);
						String serise = chartData.getSerise();
						if (serise.equals(seri)) {
							dataXml.append("<set value='" + chartData.getValue() + "' toolText='"+chartData.getCategory()+","+chartData.getValue()+"'/>");
						}
					}
				}
				
			}else {
				for (ChartData chartData : list) {
					String serise = chartData.getSerise();
					if (serise.equals(seri)) {
						dataXml.append("<set value='" + chartData.getValue() + "' toolText='"+chartData.getCategory()+","+chartData.getValue()+"'/>");
					}
				}
			}
			
			dataXml.append(" </dataset>");
		}
		dataXml.append("<styles>");
		dataXml.append("<definition>");
		dataXml.append("<style name='CaptionFont' type='font' size='12'/>");
		dataXml.append("</definition>");
		dataXml.append("<application>");
		dataXml.append("<apply toObject='CAPTION' styles='CaptionFont' />");
		dataXml.append("<apply toObject='SUBCAPTION' styles='CaptionFont' />");
		dataXml.append("</application>");
		dataXml.append("</styles>");
		dataXml.append("</chart>");
	}
	/**
	 * 创建饼图
	 * @param dataXml
	 * @param list
	 */
	private void appendPieChartXML(StringBuffer dataXml,List<ChartData> list){
		dataXml.append("<chart showAboutMenuItem='0' palette='4' decimals='0' enableSmartLabels='1' enableRotation='0' ")
			   .append("bgAlpha='40,100' bgRatio='0,100' bgAngle='360' startingAngle='70' isSmartLineSlanted='0' labelDistance='10'>") ;
		List<Double> valList = new ArrayList<Double>();
		for (ChartData chartData : list) {
			valList.add(chartData.getValue());
		}
		Collections.sort(valList);
		Collections.reverse(valList);
		for (ChartData chartData : list) {
			String ctg = chartData.getCategory();
			String[] ctgArray = StringUtils.split(ctg, "***");
			double value = chartData.getValue();
			if (value == valList.get(0)) {
				dataXml.append("<set label='" + ctgArray[1] + "' value='" + value + "' isSliced='1' />");
			} else {
				dataXml.append("<set label='" + ctgArray[1] + "' value='" + value + "'  />");
			}
		}
		dataXml.append(" </chart>");
	}
	/**
	 * 根据不同规则条件将主题中所有的规则条件拼接为一条sql语句(只有where部分)，同时加入相应的sql输入参数
	 * @param ruleResult
	 * @param onlyByDeviceType
	 * @param deviceType
	 * @return
	 */
	private SqlStruct getHqlTerm(List<RptRule> ruleResult,Parameter parameter) {
		SqlStruct sqlStruct = new SqlStruct();
		List sqlParam = new ArrayList();
		Integer paramIndex = null; // 前台参数对应名
		StringBuffer queryCondition = new StringBuffer();
		String onlyByDeviceType = parameter.getValue("onlyByDvctype");
		String deviceType = parameter.getValue("deviceType") ;
		for (int i = 0; i < ruleResult.size(); i++) {
			RptRule rule = ruleResult.get(i);
			paramIndex = rule.getHtmlField(); 
			String ruleCondition=rule.getSqlParam();
			boolean onlyDvc = false; //设备类型IP
			if(ObjectUtils.equalsAny(ruleCondition,"and dvcAddress = ?","and alias.dvcAddress = ?","and fwrisk.dvcAddress = ?","")){
				if("onlyByDvctype".equals(onlyByDeviceType)&&deviceType != null){
					String key = ruleCondition.replace("= ?", " = ").replace("and", "");
					rule.setSqlParam(" and ("+getDvcIp(deviceType,key)+")");
					onlyDvc = true;
				}
				
				String sqlValue= rule.getSqlDefValue();
				if(sqlValue!=null&&sqlValue.startsWith("onlyByDvctype")){
					String[] sqlValueArray = sqlValue.split(";;;");
					sqlStruct.setDevTypeName(sqlValueArray[1]);
					sqlStruct.setDvcIp("onlyByDvctype");
					String deviceTypeZh=DeviceTypeNameUtil.getDeviceTypeName(sqlValueArray[1],Locale.getDefault());
					sqlStruct.setOnlyByDvctype(deviceTypeZh);
					String key = ruleCondition.replace("= ?", " = ").replace("and", "");
					rule.setSqlParam(" and ("+getDvcIp(sqlValueArray[1],key)+")");
					onlyDvc = true;
				}
				
			}
			
			// 获取参数值
			String htmlFieldValue = getParameterValue(parameter, paramIndex,ruleCondition);
			
			int ruleName = rule.getRuleName() ;// 规则名称id
			if (ruleName==1) {// 1 top规则
				// 前台不为空获取默认值
				sqlStruct.setSqlpage(StringUtil.toInteger(htmlFieldValue,StringUtil.toInteger(rule.getSqlDefValue()))) ;
				continue;
			}
			if (ruleName==7) {// union
				sqlParam = getUnion(sqlParam, StringUtil.toInt(rule.getSqlDefValue()));
				sqlStruct.setSqlparam(sqlParam);
				return sqlStruct;
			}
			// 显示项才有参数,0、1不显示,有没有可能有默认值或 前台可能有参数来。
			if (ruleName!=0  && !onlyDvc) {
				Object sqlValue = ObjectUtils.nvl(htmlFieldValue, getDefValue(rule));
				if (sqlValue != null){
					sqlParam.add(sqlValue);
				}
				setExpTime(sqlStruct, ruleName, rule, sqlValue);// String类型可能有问题
			}
			Object oSqlParam = rule.getSqlParam();
			if (oSqlParam != null)
				queryCondition.append(" ").append(oSqlParam).append(" ");
		}
		sqlStruct.setSql(queryCondition.toString());
		sqlStruct.setSqlparam(sqlParam);
		return sqlStruct;
	}
	/**
	 * 提取指定的参数索引所对应的客户端传递的参数值
	 * @param paramIndex 参数索引
	 * @param talCategoryKey
	 * @return
	 */
	private String getParameterValue(Parameter parameter, Integer paramIndex,String talCategoryKey) {
		 
		String htmlField=null;
		if(paramIndex==6){
			String[] talCategoryArray=parameter.getValues(ReportUiConfig.Html_Field.get(paramIndex));
			if(talCategoryArray!=null&&talCategoryArray.length>0){
				for (int j = 0; j < talCategoryArray.length; j++) {
					if(talCategoryArray[j]!=null&&!talCategoryArray[j].equals("")&&!talCategoryArray[j].equals("null")){
						String[] talCategoryValueArray=StringUtils.split(talCategoryArray[j],"***");
						String talCategoryKeyConvert=ReportUiConfig.ColumnRuleMap.get(talCategoryValueArray[0]);       
						if(talCategoryKey.indexOf(talCategoryKeyConvert)!=-1){ 
							htmlField=talCategoryValueArray[1];
						}
					}
				}
			}
		}else{
			htmlField = parameter.getValue(ReportUiConfig.Html_Field.get(paramIndex));
		}
		if (ReportUiConfig.NA.equals(htmlField)){
			return "";
		}
		if (StringUtil.isNotBlank(htmlField))
			return htmlField;
		else
			return null;
				
	}
	/**
	 * 获取对应的设备类型下的所有设备的ip地址，拼接为一个用于sql查询的字符串
	 * 返回结果示例:(dvcAddress='192.168.0.1' or dvcAddress='192.168.0.2')
	 * @param dvc 设备类型
	 * @param key
	 * @return
	 */
	private String getDvcIp(String dvc,String key){
		List<Map<String, Object>> list = null;
		try {
			if(dvc.contains("Monitor/")){
				DataSourceService dataSourceService = (DataSourceService)SpringContextServlet.springCtx.getBean("monitorService");
				list = dataSourceService.getDataSourceTreeWithNodeList(null);
			}else{
				DataSourceService dataSourceService = (DataSourceService)SpringContextServlet.springCtx.getBean("dataSourceService");
				list = dataSourceService.getDataSourceTreeWithNodeList(null);	
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		StringBuffer dvcIP = new StringBuffer();
		if (ObjectUtils.isNotEmpty(list)) {
			
			for (Map<String, Object> m : list) {
				if (dvc.replace("Monitor/", "").equals((String) m.get("securityObjectType"))) {
					if(dvcIP.length()<1)
						dvcIP.append(key).append("'").append(m.get("dataSourceIp")).append("'");
					else
						dvcIP.append(" or ").append(key).append("'").append(m.get("dataSourceIp")).append("'");
				}
			}
		}
		return dvcIP.toString();
	}
	private List getUnion(List sqlParam, int num) {
		List reValue = new ArrayList();
		for (int j = 0; j < num; j++) {
			reValue.addAll(sqlParam);
		}
		return reValue;
	}
	private void setExpTime(SqlStruct sqlStruct, int ruleName, RptRule rule,Object tmpO) {
		SimpleDateFormat smpFmt1 = new SimpleDateFormat(ReportUiConfig.dFormat1);
		if (ruleName==2)// 设备IP
			sqlStruct.setDvcIp(tmpO.toString());
		else if (ruleName==3)// 开始时间
			sqlStruct.setsTime(tmpO + "");// String
		else if (ruleName==4)// 结束时间
			sqlStruct.seteTime(tmpO + ""); // String
		else if (ruleName==8) {// addDate规则
			Object defValue = rule.getSqlDefValue() ;
			if ("addNow".equals(defValue)){
				sqlStruct.seteTime(tmpO instanceof Date ? smpFmt1.format(tmpO) : tmpO + "");
			}else{
				sqlStruct.setsTime(tmpO instanceof Date ? smpFmt1.format(tmpO) : tmpO + "");
			}
		}
	}
	private String getRunSql(int subType) {
		String reValue = null;
		switch (subType) {
			case 1: reValue = "chartSql";break;
			case 2: reValue = "tableSql";break;
			case 3:
			case 5:reValue = "tableSql";break;
		}	
		return reValue;

	}
	/**
	 * 根据时间区间替换sql对应的日、月、年表
	 * 
	 * @param String
	 *            sql 原始Sql
	 * @param String
	 *            sTime 开始时间
	 * @param String
	 *            eTime 结束时间
	 * @return String 替换后的Sql
	 * @throws Exception
	 */
	public String getTimeSql(String sql, String sTime, String eTime) {
		String newSql = sql;
		if (sql.indexOf("_month") > 0)
			return sql;
		if (sql.indexOf("_day") > 0)
			return sql;
		if (StringUtil.isNotBlank(sTime) && StringUtil.isNotBlank(eTime)){
			long time = ReportUiUtil.countTime(sTime, eTime);
			if(time >= (86400 * 28)) {
				newSql = sql.replace("hour", "month").replace("Hour", "Month");
			}else if (time >= 2*86400) {
				newSql = sql.replace("hour", "day").replace("Hour", "Day");
			}
			return newSql;
		}else{
			return sql;
		}
	}
	public List getList(String sql, List sqlParam, Integer iPage,String tableName,String[] nodeIds,Parameter parameter) throws Exception {
		List reValue = new ArrayList();
		List result = null;
		if(nodeIds==null){
			throw new Exception("getList(),nodeIds==null!!!");
		}
		try {
			String reportValue = parameter.getValue(ReportUiConfig.reportValue);
			if (StringUtils.isNotBlank(reportValue)) {
				reportValue = new String(reportValue.getBytes("iso-8859-1"), "UTF-8");
			}
			if("1".equals(parameter.getValue("reportType"))){
				sql=sql.replace("startTime <= ?", "startTime <= ? and srcUserName='"+reportValue+"'");
			}else if("2".equals(parameter.getValue("reportType"))){
				sql=sql.replace("startTime" + " <= ?", "startTime <= ? and customer="+reportValue);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		String flag= sql.indexOf("union") != -1 ? "union" : "list";
		
		NodeMgrFacade nodeMgrFacade = (NodeMgrFacade)SpringContextServlet.springCtx.getBean("nodeMgrFacade");
		
		int len=nodeIds.length;
		List<ReportDispatchModel> tList=new ArrayList<ReportDispatchModel>(len);
		for (int i = 0; i < len; i++) {
			ReportDispatchModel reportDispatchModel=new ReportDispatchModel();
			reportDispatchModel.setNodeId(nodeIds[i]);
			reportDispatchModel.setNodeMgrFacade(nodeMgrFacade);
			reportDispatchModel.setCmd(MessageDefinition.CMD_REPORT_GET_RESULT);
			Map<String,Object> map=reportDispatchModel.getMap();
			map.put("sql", sql);
			map.put("sqlParam", sqlParam);
			if(iPage!=null){
				map.put("iPage", iPage*10);
			}
			map.put("tableName", tableName);
			map.put("flag", flag);
			tList.add(reportDispatchModel);
		}
		
		result=reportDispatch(tList,null);
		if (sql.indexOf("union") != -1) {
			//设置几个极大数值,防止数据被过滤掉
			reValue.add(9999);
		} else {
			reValue.add(iPage);
		}
		reValue.add(result);
		return reValue;
	}
	public List reportDispatch(List<ReportDispatchModel> tList,HttpServletRequest request){
		List result=new ArrayList();
		try {
			ThreadPoolExecuteDispatchUtil<ReportDispatchModel> threadPoolExecuteDispatchUtil=new ThreadPoolExecuteDispatchUtil<ReportDispatchModel>(tList);
			ThreadPoolExecutor threadPoolExecutor=null;
			threadPoolExecutor=(ThreadPoolExecutor)SpringContextServlet.springCtx.getBean("commondDispatchThreadPool");
			threadPoolExecuteDispatchUtil.setThreadPool(threadPoolExecutor);
			threadPoolExecuteDispatchUtil.execute();
			 
			while(true){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				Boolean f=true;
				for (ReportDispatchModel rdm : tList) {
					if(!rdm.isQueryComplete()){
						f=false;
						break;
					}
				}
				 
				if(f){
					break;
				}
				
			}
			if(tList!=null){
				for (ReportDispatchModel reportDispatchModel : tList) {
					result.add(reportDispatchModel.getList());
				}
			}
		} catch (Exception e) {
			e.printStackTrace() ;
		}
		return result ;
	}
	private Object getDefValue(RptRule rule) {
		Object sqlValue = rule.getSqlDefValue() ;
		if(sqlValue.equals("talsysdates")) {
			sqlValue = getTimeValue("rick1979");// String
		} else if (sqlValue.equals("talsysdaten")) {
			sqlValue = getTimeValue("addNow");// String
		}else if (rule.getRuleName()!=null&&rule.getRuleName()==8){
			sqlValue = getExpValue(rule);
		}
		return sqlValue;
	}
	private Object getExpValue(RptRule rule) {
		return getTimeValue(rule.getSqlDefValue().toString());
	}
	private Object getTimeValue(String dType) {
		Object reValue = null;
		if (dType.equals("addNow"))
			reValue = ReportUiUtil.getNowTime(ReportUiConfig.dFormat2 + ReportUiConfig.eTimePostfix);
		else
			reValue = ReportUiUtil.getNowTime(ReportUiConfig.dFormat2 + ReportUiConfig.sTimePostfix);
		return reValue;
	}

	private ChartData getChartDate(Map<Object, Object> subMap, Map dataMap,int countSign, String key) {
		ChartData _charData = new ChartData();
		String chartItem = key;// 图的Value
		Double chartItemValue = dataMap.get(chartItem) == null ? 0d : Double.parseDouble(dataMap.get(chartItem) + "");
		if (countSign != -1) // if (chartItem.equals("BYTES")){
			chartItemValue = ReportUiUtil.getCapability(chartItemValue,countSign);
		_charData.setValue(chartItemValue);
		// 如果serise为null则无法组织报表
		String serise = subMap.get("serise") + "";
		if (StringUtil.isNotBlank(serise)) {
			if (serise.indexOf("$") > 0)
				serise = serise.replace("$", "");
			else if (serise.indexOf("*") > 0) {
				String cb = ReportUiConfig.Capability.get(countSign);
				serise = serise.replace("*", cb);
			} else {
				serise = dataMap.get(subMap.get("serise")).toString();
				if (StringUtil.isBlank(serise))
					serise = ReportUiConfig.NA;
			}
		} 
		boolean qushiFlag = StringUtil.booleanVal(subMap.get("chartProperty"));// 趋势报表

		if (qushiFlag && serise.length() > 0){
			serise = serise.replace("(Bytes)", "").replace("(GB)", "").replace("(KB)", "").replace("(MB)", "");
		} 
		//yxj end 
			// else
		// serise = "";
		
		// 在协议、或者事件源有可能为空（非null），这种情况下设置为N/A
		// 该部分应模仿serise
		// String category = dataMap.get(subMap.get("category") + "") + "";
		String category = subMap.get("category") + "";
		if (StringUtil.isNotBlank(category)) {
			if (category.indexOf("$") > 0){
				category = category.replace("$", "");
			}
			else{
				if(qushiFlag){
					category = (StringUtil.isNotBlank(dataMap.get(category) + "") ? dataMap.get(category)+ "" : ReportUiConfig.NA);
				}
			}
		}
		if (subMap.get("tableFiled").toString().startsWith("DVC_TYPE")){
			category = DeviceTypeNameUtil.getDeviceTypeName(category,  Locale.getDefault());
		}
		if (qushiFlag && serise.length() > 0){
			
		}else{
			category=category+"***"+serise;
		}
		_charData.setCategory(category);// 种类
		
		String serise2 = (String)subMap.get("serise");
		boolean  flag = ReportUiUtil.isSystemLog(subMap);
		if(flag){
			if("TYPE".equals(serise2)||"ALLLOGTYPE".equals(serise2)){
				serise=DeviceTypeNameUtil.getDeviceTypeName(serise, Locale.getDefault());
			}
		}
		
		if(subMap.get("transAuditNode")!=null&&"true".equals(subMap.get("transAuditNode"))){
			if("SRC_ADDRESS".equals(serise2)||"DEST_ADDRESS".equals(serise2)){
				Map<String,String> auditNodeMapping=(Map<String,String>)subMap.get("auditNodeMapping"); 
				String auditNodeName=auditNodeMapping.get(serise);
				if(auditNodeName!=null){
					if(auditNodeName.length()>20){
						auditNodeName=auditNodeName.substring(0,20)+"...";
					}
					serise=auditNodeName;
				}
			}
		}
		
		_charData.setSerise(serise);
		return _charData;
	}
	/**
	 * 针对趋势报表重构数据
	 * 
	 * @param List
	 *            result DB中原始数据
	 * @param String
	 *            sql 获取重构类型[hour day month]
	 * @return List 重构后的数据
	 * 
	 */
	public List<Map<String, Object>> changeResultPro(List<Map<String, Object>> result, String sTime, String eTime,String table, String category, String chartItem, String dvcIp, String mstType) {
		if (result.size()==0){
			return result;
		}

		List qushiList=new ArrayList();
		if (sTime!="" && eTime!="" && ReportUiUtil.countTime(sTime, eTime, "qushi")) {
			String[] DateV=new String[]{sTime,eTime};
			List qushiListTemp=new ArrayList();
			
			// 输入的结束时间大于等于DB的开始时间
			if (!ReportUiUtil.countTime(DateV[0], sTime, "qushi")){
				sTime = DateV[0];
			}
			
			if(result!=null&&result.size()>0){
				
				Set<String> priorityKeys=new HashSet<String>();
				for (Object oList : result) {
					List<Map<String, Object>> mapList=(List<Map<String, Object>>)oList;
					if(mapList!=null&&mapList.size()>0){
						if (mapList.get(0).get("PRIORITY")!=null){
							for (Map<String, Object> map : mapList) {
								priorityKeys.add((String)map.get("PRIORITY")) ;
							}
						}
					}
				}
				
				
				for (Object oList : result) {
					List<Map<String, Object>> mapList=(List<Map<String, Object>>)oList;
					if(mapList!=null&&mapList.size()>0){
							if (mapList.get(0).get("PRIORITY")!=null){
								Map<String,List<Map<String, Object>>> maps=new HashMap<String,List<Map<String, Object>>>();
								for (Map<String, Object> map : mapList) {
									String priority=(String)map.get("PRIORITY");
									if(maps.get(priority)==null){
										List<Map<String, Object>> innerMapList=new ArrayList<Map<String,Object>>();
										innerMapList.add(map);
										maps.put(priority, innerMapList);
									}else{
										List<Map<String, Object>> innerMapList = maps.get(priority);
										innerMapList.add(map);
									}
								}
								mapList.clear();
								for (String key : priorityKeys) {
									List<Map<String, Object>> innerMapList = maps.get(key);
									if(innerMapList==null){
										innerMapList=new ArrayList<Map<String,Object>>();
									}
									innerMapList = changeResult(innerMapList, table, sTime, eTime,category, chartItem, mstType, DateV[1]);
									mapList.addAll(innerMapList);
								}
							}else{	
								//时间趋势
								mapList = changeResult(mapList, table, sTime, eTime,category, chartItem, mstType, DateV[1]);			
							}
							qushiListTemp.add(mapList);
					}
				}
				
				reformingStatisticDataForTrend(qushiListTemp,qushiList,category,chartItem);
			}
		}
		return qushiList;
	}
	/**
	* @method: reformingStatisticDataForTop 
	* 		 重新构造排行报表数据
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:  result:从各个Auditor得到的结果result<List>
	*  		   category: 种类key
	*   	   chartItems: 统计项集合
	*    	   sumPage: 每页显示几条
	*    	   isAllResult: true为返回所有值,false为返回topN
	* @return: Map<String,Object>: 用map返回结果
	* @exception: Exception
	*/
	public Map<String,Object> reformingStatisticDataForTop(List result,String category,String[] chartItems,int sumPage,Boolean isAllResult){
		
		//排行报表和统计报表yang_xuanjia
		Map<String,Object> rmap=new HashMap<String, Object>();
		
		int chartItemsLen=chartItems.length;
		
		List topResultTemp=new ArrayList(); 
		List topResult=new ArrayList(); 
		int mFlag=0;
		if(result!=null&&result.size()>0){
			for (Object oList : result) {
				List mapList=(List)oList;
				if(mapList!=null&&mapList.size()>0){
					mFlag++;
					for (Object oMap : mapList) {
						topResultTemp.add((Map)oMap);
					}
				}
			}
		}
		
		int len=topResultTemp.size();
		if(mFlag>=2&&len>=2){
			//mFlag: 在多个Auditor内有数据,如果只有1个Auditor中有数据,就不用排序了,因为sql语句就已经排序了.
			//len:大于等于2条记录才排序
			
			//排序前先把多个Auditor内key相同的值求和.
			List topSumResultList=new ArrayList(); 
			Map<String,Object> topSumResulMap=new HashMap<String, Object>();
			for (int i = 0; i < len; i++) {
				Map<String,Object> m=(Map<String,Object>)topResultTemp.get(i);
				String categoryTemp=m.get(category).toString();
				
				if(topSumResulMap.get(categoryTemp)==null){
					topSumResulMap.put(categoryTemp, m);
				}else{
					Map<String,Object> mOld =(Map<String,Object>)topSumResulMap.get(categoryTemp);
					for (int j = 0; j < chartItemsLen; j++) {
						Object old=mOld.get(chartItems[j]);
						Object o=m.get(chartItems[j]);
						old=ReportUiUtil.sumValue(old, o);
						mOld.put(chartItems[j], old);
					}
					 
					/* yxj
					 * String nodeIdOld=(String)mOld.get("nodeId");
					String nodeId=(String)m.get("nodeId");
					mOld.put("nodeId", nodeIdOld+","+nodeId);*/
					//topSumResulMap.put(category, mOld);
				}
			}
			
			Set<String> keys=topSumResulMap.keySet();
			if(keys!=null&&keys.size()>0){
				 for (String key : keys) {
					 topSumResultList.add(topSumResulMap.get(key));
				}
			}
			
			topResultTemp=topSumResultList;
			
			len=topResultTemp.size();
			ReportDataComparable comparable=new ReportDataComparable(chartItems[0]); 
			for (int i = 0; i < len-1; i++) {
				for (int j = i+1; j < len; j++) {
					if(comparable.compareTo(topResultTemp.get(i), topResultTemp.get(j))<0){
						Object temp=topResultTemp.get(i);
						topResultTemp.remove(i);
						topResultTemp.add(i, topResultTemp.get(j-1));
						topResultTemp.remove(j);
						topResultTemp.add(j,temp);
					}
				}
			}
		}
		
		
		
		int lenTemp=topResultTemp.size();
		if(isAllResult){
			result=topResultTemp;
		}else{
			//lenResult为要显示的记录数
			int lenResult=0;
			if(sumPage>=lenTemp){
				lenResult=lenTemp; 
			}else{
				lenResult=sumPage;
			}
			for (int i = 0; i < lenResult; i++) {
				topResult.add(topResultTemp.get(i));
			}
			result=topResult;
		}
		
		sumPage=lenTemp;
		//sumPage为总记录数
		rmap.put("result", result);
		rmap.put("sumPage", sumPage);
		
		return rmap;
	}
	/**
	 * 多列合并数据，非归并字段暂时只支持String 类型的
	 * @param result 
	 * @param category 非合并字段
	 * @param chartItems 合并字段
	 * @param compositorFields 排序字段  "id|1,opCount|-1"
	 * @return
	 */
	public Map<String,Object> reformingStatisticDataForSimpleMultidata(List result,String[] category,String[] chartItems,String[] compositorFields,int sumPage,boolean isAllResult){
		
		List tmpList = new ArrayList();
	    int nodes = 0;
	     if(result!=null&&result.size()>0){
			for (Object oList : result) {
				if(oList!=null){
					nodes++;
					tmpList.addAll((Collection) oList);
				}
			}
		}
		if(nodes>=2&&tmpList.size()>1){
			for(int i=0,len=tmpList.size();i<len;i++){
				HashMap iMap =(HashMap) tmpList.get(i);
				for(int j=i+1;j<len;j++){
					boolean jflag = true;
					HashMap jMap = (HashMap) tmpList.get(j);
					for(int ic=0,clen=category.length;ic<clen;ic++){
						jflag =jflag&&iMap.get(category[ic]).equals(jMap.get(category[ic])); 
						if(ic==clen-1){
							if(jflag){
								Map tMap = iMap;
								for(int y=0,ylen=chartItems.length;y<ylen;y++){
									Object iobj=iMap.get(chartItems[y]);
									Object jobj=jMap.get(chartItems[y]);
									Object tobj=ReportUiUtil.sumValue(iobj, jobj);
									tMap.remove(chartItems[y]);
									tMap.put(chartItems[y], tobj);
								}
								tmpList.remove(j);
								tmpList.remove(i);
								tmpList.add(i, tMap);
								len = tmpList.size();
							}
						}
					}
				}
			}
			
			if(compositorFields!=null&&compositorFields.length>0){
				for(int k=0,klen= compositorFields.length;k<klen;k++){
					String order[] = compositorFields[k].split("|");
					String field =order[0];
					ReportDataComparable comparable=new ReportDataComparable(field); 
					int ord = Integer.parseInt(order[1]!=null?order[1]:"0");
					for(int i=0,len=tmpList.size();i<len;i++){
						for(int j=i+1;j<len;j++){
							int myOrd = comparable.compareTo(tmpList.get(i),tmpList.get(j));
	                        if(ord<0){
	                        	if(myOrd<0){
	                        		Object temp=tmpList.get(i);
	                        		tmpList.remove(i);
	                        		tmpList.add(i, tmpList.get(j-1));
	                        		tmpList.remove(j);
	                        		tmpList.add(j,temp);
	                        	}
	                        }else if(ord>0){
	                        	if(myOrd>0){
	                        		Object temp=tmpList.get(i);
	                        		tmpList.remove(i);
	                        		tmpList.add(i, tmpList.get(j-1));
	                        		tmpList.remove(j);
	                        		tmpList.add(j,temp);
	                        	}
	                        }
						}
					}
				}
			}
		}
		List topResult=new ArrayList(); 
		int lenTemp=tmpList.size();
		if(isAllResult){
			result=tmpList;
		}else{
			//lenResult为要显示的记录数
			int lenResult=0;
			if(sumPage>=lenTemp){
				lenResult=lenTemp; 
			}else{
				lenResult=sumPage;
			}
			for (int i = 0; i < lenResult; i++) {
				topResult.add(tmpList.get(i));
			}
			result=topResult;
		}
		sumPage=lenTemp;
		
		
		Map<String,Object> rmap=new HashMap<String, Object>();
			rmap.put("result", result);
			rmap.put("sumPage", sumPage);
			return rmap;
	}
	/**
	* @method: reformingStatisticDataForUnion 
	* 		 重新构造概要(Union)报表数据
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:  result:从各个Auditor得到的结果result<List>
	*  		   category: 种类key
	*   	   chartItems: 统计项集合
	* @return: Map<String,Object>: 用map返回结果
	* @exception: Exception
	*/
	public Map<String,Object> reformingStatisticDataForUnion(List result,String category,String[] chartItems){
		
		//概要yang_xuanjia
		Map<String,Object> rmap=new HashMap<String, Object>();
		int chartItemsLen=chartItems.length;
		List resultHaveData=new ArrayList();
		
		if(result!=null){
			
			for (Object object : result) {
				List tempList=(List)object;
				if(tempList!=null&&tempList.size()>0){
					resultHaveData.add(tempList);
				}
			}
			result=resultHaveData;
			
			int resultArrayLen=result.size();
			List<String> keyList = new ArrayList<String>();
			if(resultArrayLen==1){
				result=(List)result.get(0);
			}else if(resultArrayLen>=2){
				 int column=((List)result.get(0)).size();
				 List resultList=new ArrayList();
				 Map topSumResulMap=new HashMap();
				 for (int j = 0; j < column; j++) {
					 for (int i = 0; i < resultArrayLen; i++) {
							Map<String,Object> m=(Map)((List)result.get(i)).get(j);
							String categoryTemp=(String)m.get(category);
							
							if(topSumResulMap.get(categoryTemp)==null){
								topSumResulMap.put(categoryTemp, m);
								keyList.add(categoryTemp);
							}else{
								Map<String,Object> mOld =(Map<String,Object>)topSumResulMap.get(categoryTemp);
								for (int k = 0; k < chartItemsLen; k++) {
									Object old=mOld.get(chartItems[k]);
									Object o=m.get(chartItems[k]);
									old=ReportUiUtil.sumValue(old, o);
									mOld.put(chartItems[k], old);
								}
							}
					 }
				 }
				 
//					if(keys!=null&&keys.size()>0){
						 for (String key : keyList) {
							 resultList.add(topSumResulMap.get(key));
						}
//					}
					result=resultList;
			}
		}else{
			result=resultHaveData;
		}
		
		//sumPage为总记录数
		rmap.put("result", result);
		rmap.put("sumPage", 9999);
		
		return rmap;
	}
	public List<Map<String, Object>> changeResult(List<Map<String, Object>> result, String table, String sTime,String eTime, String category, String chartItem, String mstType, String maxTime) {
		List<Map<String, Object>> reValue = new ArrayList<Map<String, Object>>();
		// 计算开始时间 结束时间Start
		String ruleDate = null;

		if (ReportUiUtil.getSEcount(maxTime, eTime) > 0){
			eTime = maxTime;
		}
		if (sTime.endsWith(".0")){
			sTime = sTime.substring(0, sTime.length() - 2);
		}
		if (eTime.endsWith(".0")){
			eTime = eTime.substring(0, eTime.length() - 2);
		}
		if (table.toLowerCase().indexOf("hour") >= 0) {
			ruleDate = "hour";
			if (!sTime.endsWith("0:00")){
				sTime = ReportUiUtil.getAddDate(countTime(sTime,ruleDate), ruleDate,table);
			}
			eTime = countTime(eTime,ruleDate);
		} else if (table.toLowerCase().indexOf("day") >= 0) {
			ruleDate = "day";
			if (!(sTime.endsWith("00:00")||sTime.endsWith("06:00")||sTime.endsWith("12:00")||sTime.endsWith("18:00"))){
				sTime = ReportUiUtil.getAddDate(countTime(sTime,ruleDate), ruleDate,table);
			}
			eTime = countTime(eTime,ruleDate);

		} else if (table.toLowerCase().indexOf("month") >= 0) {
			ruleDate = "month";
			if (!sTime.endsWith("00:00:00")){
				sTime = ReportUiUtil.getAddDate(sTime.substring(0, 11) + "00:00:00", ruleDate,table);
			}
			eTime = eTime.substring(0, 11) + "00:00:00";
		} 
		
		// 计算开始时间 结束时间End
		// 设定开始时间！
		String tmpTime = sTime;
		int idx = 0; // 计数
		while (ReportUiUtil.countTime(tmpTime, eTime, "qs")) {
			if (idx < result.size()) {
				if (!ReportUiUtil.countTime(tmpTime, result.get(idx).get(category).toString(), ruleDate)) {//两个时间值不等
					Map<String, Object> tmp = getTimeMap(tmpTime, chartItem);
					if (result.size()>0 && result.get(idx).get("PRIORITY") != null){
						tmp.put("PRIORITY", result.get(idx).get("PRIORITY"));
					}
					reValue.add(tmp);
					tmpTime = ReportUiUtil.getAddDate(tmpTime, ruleDate,table);
				} else {
					tmpTime = result.get(idx).get("START_TIME").toString();
					reValue.add(result.get(idx));
					tmpTime = ReportUiUtil.getAddDate(tmpTime, ruleDate,table);
					idx++;
				}
			} else {
				Map<String, Object> tmp = getTimeMap(tmpTime, chartItem);
				if (result.size()>0 && result.get(0).get("PRIORITY") != null){
					tmp.put("PRIORITY", result.get(0).get("PRIORITY"));			
				}
				reValue.add(tmp);
				tmpTime = ReportUiUtil.getAddDate(tmpTime, ruleDate,table);
			}
		}
		// if (reValue != null && reValue.size() > 0
		// && Integer.parseInt(mstType) != 2)// 2是自定义报表
		// reValue.remove(reValue.size() - 1);// 去掉最后一条数据
		return reValue;
	}			
	/**
	* @method: reformingStatisticDataForTrend 
	* 		 重新构造趋势报表数据
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:  qushiListTemp: 列表集合list<List>
	*  		   qushiList: 需要返回的结果
	*  		   chartItem: 统计项  aa,bb
	* @return: void
	* @exception: Exception
	*/
	public void reformingStatisticDataForTrend(List qushiListTemp,List qushiList,String category,String chartItem){ 
		
		int size=qushiListTemp.size();
		if(size>0){
			if(size<2){
				List qushiListTemp2=(List)qushiListTemp.get(0);
				if(qushiListTemp2!=null&&qushiListTemp2.size()>0){
					qushiList.addAll(qushiListTemp2);
				}
			}else{
				String[] chartItems=chartItem.split(",");
				int lenChartItems=chartItems.length;
				List<Map<String, Object>> mapListTemp=(List<Map<String, Object>>)qushiListTemp.get(0);
				int len=mapListTemp.size();
				if(len>0){
					for (int i = 0; i < len; i++) {
						 
						
						Map<String,Object> mapTemp=new HashMap<String, Object>();
						for (int j = 0; j < size; j++) {
							List<Map<String, Object>> mapList=(List<Map<String, Object>>)qushiListTemp.get(j);
							 
							Map<String, Object> map=mapList.get(i);

							if(map.get("PRIORITY")==null){
								Object startTime=mapTemp.get(category);
								if(startTime==null){
									/* yxj
									 * Object startTimeObj=map.get("START_TIME");
									if(startTimeObj instanceof String){
										String key=(String)startTimeObj;
										mapTemp.put("START_TIME", key);
									}else if(startTimeObj instanceof java.sql.Timestamp){
										Timestamp key=(Timestamp)startTimeObj;
										String key2=Misc.formatDateToStringByType( new Date(key.getTime()), "yyyy-MM-dd HH:mm:ss");
										mapTemp.put("START_TIME", key2);
									}*/
									Object startTimeObj=map.get(category);
									mapTemp.put(category, startTimeObj);
									for (int k = 0; k < lenChartItems; k++) {
										Object value=map.get(chartItems[k]);
										mapTemp.put(chartItems[k], value);
									}
								}else{
									for (int k = 0; k < lenChartItems; k++) {
										Object value=map.get(chartItems[k]);
										Object sumValue=mapTemp.get(chartItems[k]);
										sumValue=ReportUiUtil.sumValue(sumValue, value);
										mapTemp.put(chartItems[k], sumValue);
									}
								}
							}else{
								String prioriry=(String)map.get("PRIORITY");
								
								String key=(String)mapTemp.get(category+prioriry);
								if(key==null){
									mapTemp.put(category+prioriry, category+prioriry);
									Object startTimeObj=map.get(category);
									Object priority=map.get("PRIORITY");
									mapTemp.put(category, startTimeObj);
									mapTemp.put("PRIORITY", priority);
									
									for (int k = 0; k < lenChartItems; k++) {
										Object value=map.get(chartItems[k]);
										mapTemp.put(chartItems[k], value);
									}
								}else{
									for (int k = 0; k < lenChartItems; k++) {
										Object value=map.get(chartItems[k]);
										Object sumValue=mapTemp.get(chartItems[k]);
										sumValue=ReportUiUtil.sumValue(sumValue, value);
										mapTemp.put(chartItems[k], sumValue);
									}
								}
							}
						
						}
						qushiList.add(mapTemp);
					}
				}
			}
		}
	}
	private String countTime(String time,String type) {
		int minuteV = 0;
		// int eC = 0;
		/* yxj 修改原因, 客户机器有时候会抛异常....
		 * if("hour".equals(type)){
			minuteV = new Integer(time.substring(15, 16));
			//if (minuteV > 5){
			//	tmpTime = new Integer(time.substring(14, 15)) + "5";
			//}
			//else{
			tmpTime = new Integer(time.substring(14, 15)) + "0";
			//}
			time = time.substring(0, 14) + tmpTime + ":00";
		}else if("day".equals(type)){
			minuteV = new Integer(time.substring(11, 13));
			if (minuteV > 18){
				tmpTime = "18";
			}else if(minuteV > 12){
				tmpTime = "12";
			}else if(minuteV > 6){
				tmpTime = "06";
			}else if(minuteV > 0){
				tmpTime = "00";
			}
			time = time.substring(0, 11) + tmpTime + ":00:00";
		}*/ 
		SimpleDateFormat sdf=new SimpleDateFormat(ReportUiConfig.dFormat1);
		Date date=null;
		Calendar calendar=Calendar.getInstance();
		try {
			date=sdf.parse(time);
		} catch (ParseException e) {
			//log.error(e.getMessage());
		}
		calendar.setTime(date);
		
		
		if("hour".equals(type)){
			calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE)/10*10);
			calendar.set(Calendar.SECOND, 0);
			date=calendar.getTime();
			time = sdf.format(date);
		}else if("day".equals(type)){
			minuteV = calendar.get(Calendar.HOUR_OF_DAY);
			if (minuteV > 18){
				calendar.set(Calendar.HOUR_OF_DAY, 18);
			}else if(minuteV > 12){
				calendar.set(Calendar.HOUR_OF_DAY, 12);
			}else if(minuteV > 6){
				calendar.set(Calendar.HOUR_OF_DAY, 6);
			}else if(minuteV > 0){
				calendar.set(Calendar.HOUR_OF_DAY, 0);
			}
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			date=calendar.getTime();
			time = sdf.format(date);
		}
		return time;
	}
	
	/**
	 * 针对趋势报表重构数据
	 * 
	 * @param List
	 *            result DB中原始数据
	 * @param int
	 *            sPage 开始页
	 * @param int
	 *            ePage 结束页
	 * @return List 重构后的数据
	 * 
	 */
	public Map<String, Object> getTimeMap(String tmpTime, String chartItem) {
		Map<String, Object> lastTimeMap = null;
		lastTimeMap = new HashMap<String, Object>();
		lastTimeMap.put("START_TIME", tmpTime);
		lastTimeMap.put(chartItem, "0");
		return lastTimeMap;
	}
	public int getSubjectId() {
		return subjectId;
	}
	public Map getSubject() {
		return subject;
	}
	public String getValueAxisLabel() {
		return (String) subject.get("valueAxisLabel");
	}
	public String getCategoryAxisLabel() {
		return (String) subject.get("categoryAxisLabel");
	}
}
