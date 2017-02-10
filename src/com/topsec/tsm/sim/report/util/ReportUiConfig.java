package com.topsec.tsm.sim.report.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ReportUiConfig {
	/* Sql Start */
	public static final String SubSql = "select new map(sub.subDig as dig,sub.chartProperty as chartProperty,sub.paginationSql as pagesql,sub.id as subId,sub.subName as subName,sub.subType as subType,sub.chartSql as chartSql,sub.chartType as chartType,sub.category as category ,sub.serise as serise,sub.chartItem as chartItem,sub.chartLink as chartLink,sub.tableSql as tableSql,sub.tableLable as tableLable,sub.tableMore as tableMore,sub.tableLink as tableLink,sub.subPolicyId as  subPolicyId,sub.subProperty as subProperty,sub.tableFiled as tableFiled,sub.paginationLable as paginationLable,sub.paginationSql as paginationSql,sub.tableName as subTableName) from  RptSub sub  where sub.id = ?";
	public static final String SubTitleSql = "select new map(mst.id as mstId,mst.subject as subject,mst.mstDig as dig,sub.chartProperty as chartProperty,sub.paginationSql as pagesql,mst.viewItem as viewItem,mst.mstType as mstType,mst.mstName as mstName,mst.mstType as mstType,mst_sub.id as id,mst_sub.deviceType as deviceType,mst_sub.subRow as subRow,mst_sub.subColumn as subColumn,sub.id as subId,sub.subName as subName,sub.subType as subType,sub.chartSql as chartSql,sub.chartType as chartType,sub.category as category ,sub.serise as serise,sub.chartItem as chartItem,sub.chartLink as chartLink,sub.tableSql as tableSql,sub.tableLable as tableLable,sub.tableMore as tableMore,sub.tableLink as tableLink,sub.subPolicyId as  subPolicyId,sub.subProperty as subProperty,sub.tableFiled as tableFiled,sub.paginationLable as paginationLable,sub.paginationSql as paginationSql,sub.tableName as subTableName,sub.logQueryCondition as logQueryCondition,sub.summarize as subSummarize) from RptMaster mst, RptSub sub , RptMstSub mst_sub where sub.id = ? and mst.id = mst_sub.mstId and sub.id = mst_sub.subId";
	/**
	 * 根据主题id索引主题对象
	 */
	public static final String GetSub = "select new map(mst_sub.id as id,sub.subDig as dig,sub.subSubject as subject,sub.chartProperty as chartProperty,sub.paginationSql as pagesql,sub.id as subId,sub.subName as subName,sub.subType as subType,sub.chartSql as chartSql,sub.chartType as chartType,sub.category as category ,sub.serise as serise,sub.chartItem as chartItem,sub.chartLink as chartLink,sub.tableSql as tableSql,sub.tableLable as tableLable,sub.tableMore as tableMore,sub.tableLink as tableLink,sub.subPolicyId as  subPolicyId,sub.subProperty as subProperty,sub.tableFiled as tableFiled,sub.paginationLable as paginationLable,sub.paginationSql as paginationSql,sub.tableName as subTableName) from  RptSub sub , RptMstSub mst_sub  where sub.id=mst_sub.subId and sub.id = ?";
	public static final String MstSubSql = "select new map(mst.pdffooter as pdffooter,mst.summarize as summarize,sub.subSubject as subSubject,mst.mstDig as dig,sub.chartProperty as chartProperty,sub.paginationSql as pagesql,mst.viewItem as viewItem,mst.mstType as mstType,mst.mstName as mstName,mst.subject as subject,mst_sub.id as id,mst_sub.deviceType as deviceType,mst_sub.subRow as subRow,mst_sub.subColumn as subColumn,sub.id as subId,sub.subName as subName,sub.subType as subType,sub.chartSql as chartSql,sub.chartType as chartType,sub.category as category ,sub.serise as serise,sub.chartItem as chartItem,sub.chartLink as chartLink,sub.tableSql as tableSql,sub.tableLable as tableLable,sub.tableMore as tableMore,sub.tableLink as tableLink,sub.subPolicyId as  subPolicyId,sub.subProperty as subProperty,sub.tableFiled as tableFiled,sub.paginationLable as paginationLable,sub.paginationSql as paginationSql,sub.tableName as subTableName) from RptMaster mst, RptSub sub , RptMstSub mst_sub where mst.id = ? and mst.id = mst_sub.mstId and sub.id = mst_sub.subId order by mst_sub.subRow,mst_sub.subColumn";
	public static final String RuleSql = "select new map(r.ruleName as ruleName,r.htmlField as htmlField, r.sqlParam as sqlParam,r.sqlDefValue as sqlDefValue, r.ruleDisplay as ruleDisplay) from RptPolicyRule p , RptRule r where  p.subPolicyId = ? And p.ruleId = r.id order by p.id desc";
	public static final String RuleValueSql = "select new map(rv.sqlValue as sqlValue,r.ruleName as ruleName,r.htmlField as htmlField, r.sqlParam as sqlParam,r.sqlDefValue as sqlDefValue, r.ruleDisplay as ruleDisplay) from RptRuleValue rv,RptPolicyRule p , RptRule r where  p.subPolicyId = ? And rv.mstSubId = ? And p.ruleId = r.id  And rv.ruleId = r.id order by p.id desc";
	public static final String MstListSql = "select new map(mst.mstDig as dig,mst.id as id ,mst.mstName as mstName,mst.viewItem as viewItem) from RptMaster  mst where mst.subject =? and mst.mstDig is null order by mst.id ";
	public static final String PlanListSql = "select new map(mst.mstDig as dig,mst.id as id ,mst.mstName as mstName) from RptMaster  mst where mst.subject =? and mst.mstDig is null  order by mst.id ";
	public static final String PaginationSql = "select new map(mst.id as mstId,mst.mstType as mstType,sub.chartType as chartType,sub.tableLable as tableLable,sub.tableFiled as tableFiled,sub.subType as subType,sub.chartItem as chartItem,sub.category as category,sub.chartProperty as chartProperty,sub.subName as subName,sub.tableLink as tableLink, sub.paginationSql as paginationSql,sub.paginationSql as paginationSql, sub.paginationViewFiled as paginationViewFiled ,sub.paginationHtmFiled as paginationHtmFiled,sub.paginationSqlFiled as paginationSqlFiled,sub.tableName as subTableName,sub.serise as serise,sub.subSubject as subject) from RptMaster mst, RptSub sub , RptMstSub mst_sub where sub.id = ? and mst.id = mst_sub.mstId and sub.id = mst_sub.subId";
	public static final String MinDateSql = "select new map(sub.chartProperty as chartProperty,sub.subName as subName,sub.tableLink as tableLink, sub.paginationSql as paginationSql,sub.paginationSql as paginationSql, sub.paginationViewFiled as paginationViewFiled ,sub.paginationHtmFiled as paginationHtmFiled,sub.paginationSqlFiled as paginationSqlFiled,sub.tableName as subTableName) from  RptSub sub  where sub.id = ?";
	public static final String PageSqlRule = " and startTime >= ? and startTime <= ? and dvcAddress = ? ";
	public static final String PageSqlRule2 = " and startTime >= ? and startTime <= ?  ";
	public static final String PageSqlRuleAlias = " and alias.startTime >= ? and alias.startTime <= ? and alias.dvcAddress = ? ";
	public static final String PageSqlRuleAlias2 = " and alias.startTime >= ? and alias.startTime <= ? ";
	public static final String DigBrotherSql = "select new map(mst.mstDig as dig, mst.digBrother as brother) from RptMaster mst where mst.id = ?";
	public static final String MstListSql2 = "select new map(mst.mstDig as dig,mst.id as id ,mst.mstName as mstName) from RptMaster  mst where CONDITION order by mst.id ";
	public static final String MASTER_TO_SUB_SQL = "select new map(sub.subSubject as subSubject,mst.mstType as mstType,mst.subject as matSubject,mst_sub.id as id,mst_sub.deviceType as deviceType,sub.id as subId,sub.subType as subType,sub.tableSql as tableSql,sub.tableName as subTableName) from RptMaster mst, RptSub sub , RptMstSub mst_sub where mst.id = ? and mst.id = mst_sub.mstId and sub.id = mst_sub.subId order by sub.id";

	public static final String MstBean = "rptMaster";
	public static final String dbmgr = "dbmgr";

	public static final String Rn = "\r\n";

	public static final String OSXP = "XP";
	public static final String OS2K = "2000";
	public static final String WINOS = "win";
	
	/** The value of <tt>System.getProperty("os.name")<tt>. * */
	public static final String OS_NAME = System.getProperty("os.name");
	/** True iff running on Linux. */
	public static final boolean LINUX = OS_NAME.startsWith("Linux");
	/** True iff running on Windows. */
	public static final boolean WINDOWS = OS_NAME.startsWith("Windows");
	/** True iff running on SunOS. */
	public static final boolean SUN_OS = OS_NAME.startsWith("SunOS");
	// top N
	public static final String[] Topvalues = { "5", "10"};

	public static final String[] Toplabels = { "Top 5", "Top 10"};

	public static final String[] Jivalues = { "1", "2", "3", "4" };// 季度
	public static final String[] Jilabels = { "1季度", "2季度", "3季度", "4季度" };// 季度
	// page N
	public static final String[] PageSizevalues = { "10", "20", "50" };
	public static final String[] PageSizelabels = { "10", "20", "50" };
	public static final String TalTop = "talTop";
	public static final String mstrptid = "mstrptid"; // mstrptid
	public static final String sTime = "talStartTime"; // 开始时间
	public static final String eTime = "talEndTime";
	public static final String talCategory = "talCategory"; // 通用 下探参数2
	public static final String talCategory2 = "talCategory2";  // 无用
	// public static final String talSerise = "talSerise"; // 通用 下探参数1
	public static final String showTime = "showTimeInput";
	public static final String createPagination = "createPagination";
	public static final String createReport = "createReport";
	public static final String dvcaddress = "dvcaddress"; // 设备地址
	public static final String viewshow = "showTimeInput";
	public static final String viewji = "qselected";
	public static final String viewtype = "datetypeselected";
	public static final String dvctype = "dvctype";// 设备类型
	public static final String nodeType ="nodeType";
	public static final String subrptid = "subrptid"; // 子报表
	public static final String pagination = "pagination"; // 分页
	public static final String exprpt = "exprpt";
	public static final String rootId = "rootId";//业务报表根id
	public static final String assGroupNodeId="assGroupNodeId";//设备组节点id
	public static final String topoId="topoId";//设备组节点id
	public static final String nodeLevel="nodeLevel";
	public static final String goUrl = "viewMst.do?method=";
	public static final String subUrl ="/sim/report/getSubTitle?";
	public static final String moreUrl ="/sim/report/moreReport?";
	public static final String expUrl="/sim/report/expMstReport?";
	public static final String reportUrl = "/sim/report/reportQuery?";
	public static final String subEvtUrl ="/sim/topoReport/getSubTitle?";
	public static final String moreEvtUrl ="/sim/topoReport/moreReport?";
	public static final String expEvtUrl="/sim/topoReport/expMstReport?";
	public static final String reportEvtUrl = "/sim/topoReport/reportQuery?";
	public static final String reportQueryLogUrl = "/sim/logSearch/doLogSearch?";
	public static final String reportQueryEventUrl = "/sim/eventQuery/basicEventQuery?";
	public static final String PaintDot = ".";
	public static final int Paintl = 30;
	public static final String ExpUnit = "单位*";
	public static final int UnitValue = 10;// 单位换算阀值
	public static final int Dfraction = 2;// 保留几位小数
	public static final int HourTime = 5 * 60;
	public static final int DayTime = 60 * 60;
	public static final int MonthTime = 24 * 60 * 60;
	public static final String reportType = "reportType"; // 报表类型
	public static final String reportValue = "reportValue"; // 类型值

	public static final String rptDirection = "趋势报表";
	public static final String[] DateTypes = { "lable.report.typeyearReportType",
			"lable.report.typemonthReportType", "lable.report.typedayReportType",
			"lable.report.weekReportType", "lable.report.quarterReportType", "lable.report.cdateReportType" };
	public static final String DateTypeStr = "lable.report.typeyear,lable.report.typemonth,lable.report.typeday,lable.report.week,lable.report.quarter,lable.report.cdate";
	// 无数值时显示内容
	public static final String NoDataMessage = "No data to display.";
	public static final String NA = "N/A";
	public static final String moreTableLink = "更多";
	// 类型
	public static final String dTypeNum = "3";
	public static final String CustomRptLogo = "custom/";//自定义报表页脚路径
	
	// 结束时间
	public static final String pageStyle = " style=\"width:104px;\" ";
	public static final String PageParam = "Param";
	public static final String digRuleList = ",5,6,9,10,11,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,"; //以","号结尾
	public static final String numReg = "[0-9]*";

	public static final String dFormat1 = "yyyy-MM-dd HH:mm:ss";
	public static final String dFormat2 = "yyyy-MM-dd";

	public static final String sTimePostfix = " 00:00:00";
	public static final String eTimePostfix = " 23:59:59";
	public static final int PicHight = 216;
	public static final int PicWidth = 360;
	public static final int SubTableWidth = 580;
	public static final String param_P = "$P";
	public static final String param_F = "$F";
	public static final Map<Integer, String> Capability = new HashMap<Integer, String>();// kb
	// 数字图形对应Map
	public static final Map<Integer, String> GraphType = new HashMap<Integer, String>();
	// 前台对应字段
	public static final Map<Integer, String> Html_Field = new HashMap<Integer, String>();
	// 规则名称 top规则等
	public static final Map<Integer, String> Rule_Name = new HashMap<Integer, String>();
	
	public static final Map<String, String> ColumnRuleMap = new HashMap<String, String>();
	// Table 内容
	public static final String MstTable = "<table class=\"rowTable\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">";
	public static final String TableC1 = "<a href=\"#\" onClick=\"viewCmd(this,"
			+ "\\'table_";
	public static final String TableC2 = "\" width=\"99%\" border=\"0\" align=\"center\" cellpadding=\"0\" cellspacing=\"1\"   >";
	public static final String ExpSyn = "报表综述：" + "$title" + "，向您展示了"
			+ "$stime" + "至" + "$etime" + "时间区间，" + "$title" + "的情况,更详细的内容请查看"
			+ "$title" + "。";
	public static final String ExpSyn2 = "报表综述：" + "$title" + "，向您展示了"+ "$title" + "的情况,更详细的内容请查看"+ "$title" + "。";
	public static final String ExpDvc = "报表设备：";
	public static final String ExpPic = "统计图：";
	public static final String ExpTable = "统计表：";
	public static final String ExpNA = "该报表无数据";

	public static final String ExpDotS = "......";
	public static final String ExpDotL = "..............................";
	static {
		Capability.put(0, "(Bytes)");
		Capability.put(1, "(GB)");
		Capability.put(2, "(MB)");
		Capability.put(3, "(KB)");
		Capability.put(-1, "");
		Collections.unmodifiableMap(Capability);

		GraphType.put(1, "BarChart");
		GraphType.put(2, "CyliderChart");
		GraphType.put(3, "StackedChart");
		GraphType.put(4, "LineChart");
		GraphType.put(5, "PieChart");
		Collections.unmodifiableMap(GraphType);
		// 前台对应子段
		/*
		 * 规则名称 0, "gorup by order by"； 1, "Top规则"； 2，设备IP,3, "开始时间"； 4, "结束时间"；
		 * 5, "IP地址"； 6, "协议"； 7, "union规则"； 8, "addDate规则"; 9,"源IP"; 10,"目的IP";
		 * 11,"协议" 0 1 2 3 8 6
		 */
		// 设备地址
		Html_Field.put(0, dvcaddress);
		// top
		Html_Field.put(1, TalTop);
		// 开始时间
		Html_Field.put(2, sTime);
		// 结束时间
		Html_Field.put(3, eTime);
		// 下探 Category轴table
		Html_Field.put(6, talCategory);
		// 下探Serise轴table
		// Html_Field.put(7, talSerise);
		// 中文
		Html_Field.put(8, "rickNiu1979");
		Collections.unmodifiableMap(Html_Field);
		
		// 规则名称 top规则等 前台显示名称
		Rule_Name.put(0, "gorup by order by");// 不显示的
		Rule_Name.put(1, "Top规则");
		Rule_Name.put(2, "设备地址");
		Rule_Name.put(3, "开始时间");
		Rule_Name.put(4, "结束时间");
		Rule_Name.put(5, "IP地址");
		Rule_Name.put(6, "协议");
		Rule_Name.put(7, "union规则");
		Rule_Name.put(8, "addDate规则");
		Rule_Name.put(9, "源IP");
		Rule_Name.put(10, "目的地址");
		Rule_Name.put(11, "协议");
		// 中文问题 //severity='信息'
		Rule_Name.put(12, "中文规则");
		Rule_Name.put(13, "mintime");// *
		Rule_Name.put(14, "攻击类型");
		Rule_Name.put(15, "攻击范围");
		Rule_Name.put(16, "服务类型");
		Rule_Name.put(17, "类型"); //18 
		Rule_Name.put(18, "事件分类1");
		Rule_Name.put(19, "事件分类2");//22
		Rule_Name.put(20, "事件级别");
		Rule_Name.put(21, "标志");
		Rule_Name.put(22, "用户");// 24、28
		Rule_Name.put(23, "操作");
		Rule_Name.put(24, "应用子类型");//41
		Rule_Name.put(25, "操作");
		Rule_Name.put(26, "来源");
		Rule_Name.put(27, "用户类型");
		Rule_Name.put(28, "策略");//38
		Rule_Name.put(29, "设备类型");
		Rule_Name.put(30, "URL地址");
		Rule_Name.put(31, "病毒名");
		Rule_Name.put(32, "病毒文件名");
		Rule_Name.put(33, "类型");
		Rule_Name.put(34, "发送者");
		Rule_Name.put(35, "接收者");
		Rule_Name.put(36, "事件类型名称");
		Rule_Name.put(37, "危险级别");
		Rule_Name.put(38, "操作类型");//42
		Rule_Name.put(39, "数据库名称");//43
		Rule_Name.put(40, "表名");//45
		Rule_Name.put(41, "目的端口");//45
		Rule_Name.put(42, "域");
		Rule_Name.put(43, "访问方式");
		Rule_Name.put(44, "访问状态");
		
		Collections.unmodifiableMap(Rule_Name);
		ColumnRuleMap.put("CAT1_ID","cat1ID");
		ColumnRuleMap.put("CAT2_ID","cat2ID");
		ColumnRuleMap.put("PRIORITY","priority");
		ColumnRuleMap.put("SRC_ADDRESS","srcAddress");
		ColumnRuleMap.put("DEST_ADDRESS","destAddress");
		ColumnRuleMap.put("START_TIME","startTime");
		ColumnRuleMap.put("APP_PROTOCOL","appProtocol");
		ColumnRuleMap.put("MESSAGE","message");
		ColumnRuleMap.put("risk","tbrisk.risk");
		ColumnRuleMap.put("DVC_EVENT_CATEGORY","dvcEventCategory");
		ColumnRuleMap.put("REQUEST_OBJECT","requestObject");
		ColumnRuleMap.put("SRC_PORT","srcPort");
		ColumnRuleMap.put("DEST_PORT","destPort");
		ColumnRuleMap.put("TRANS_PROTOCOL","transProtocol");
		ColumnRuleMap.put("DVC_PROCESS_NAME","dvcProcessName");
		ColumnRuleMap.put("SENDER","sender");
		ColumnRuleMap.put("RECEIVER","receiver");
		ColumnRuleMap.put("SEVERITY","severity");
		ColumnRuleMap.put("SA","sa");
		ColumnRuleMap.put("NAV_VIRUS","navVirus");
		ColumnRuleMap.put("OP","op");
		ColumnRuleMap.put("DEST_HOST_NAME","destHostName");
		ColumnRuleMap.put("TBRULEATTACKTYPE","tbEcat.category");
		ColumnRuleMap.put("TBRULEATTACKRANGE","tbRule.attackRange");
		ColumnRuleMap.put("TBRULESERVICETYPE","tbRule.appType");
		ColumnRuleMap.put("TBRULENAME","tbRule.title");
		ColumnRuleMap.put("SRC_USER_NAME","srcUserName");
		ColumnRuleMap.put("DVC_CUSTOM_STRING1","dvcCustomString1");
		ColumnRuleMap.put("EVENT_TYPE","eventType");
		ColumnRuleMap.put("TYPE","type");
		ColumnRuleMap.put("IP","ip"); 
		ColumnRuleMap.put("NAV_VIRUS_LOCATION","navVirusLocation"); 
		ColumnRuleMap.put("DVC_EVENT_CATEGORY2","dvcEventCategory2");
		ColumnRuleMap.put("TBRULERISK","tbRule.risk");
		ColumnRuleMap.put("POLICY","policy");
		ColumnRuleMap.put("DVC_CUSTOM_STRING6","dvcCustomString6");
		ColumnRuleMap.put("DB_CMD","dbCmd"); 
		ColumnRuleMap.put("DB_NAME","dbName"); 
		ColumnRuleMap.put("TABLE_NAME","tableName");
		ColumnRuleMap.put("FWRULE","fwrule");
		ColumnRuleMap.put("REQUEST_URL","requestUrl");
		ColumnRuleMap.put("REQUEST_DOMAIN","requestDomain");
		ColumnRuleMap.put("REQUEST_METHOD","requestMethod");
		ColumnRuleMap.put("REQUEST_STATUS","requestStatus");
		
		Collections.unmodifiableMap(ColumnRuleMap);
	}

	public enum ReportTreeType{
		/*树干**/
		TRUNK,
		/*树枝**/
		BRANCH,
		/*叶子*/
		LEAF
	}
}
