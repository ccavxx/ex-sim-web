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

public class ExcelImportExportBasePOI {
	
	
	protected static final String BASE_TITLE = "基本信息";
	protected static final String BASE_TITLE_NAME = "名称";
	protected static final String BASE_TITLE_FIRSTTYPE = "一级分类";
	protected static final String BASE_TITLE_SECONDTYPE = "二级分类";
	protected static final String BASE_TITLE_LEVEL = "级别";
	protected static final String BASE_TITLE_OVERTIME = "超时(秒)";
	protected static final String BASE_TITLE_START = "启用";
	protected static final String BASE_TITLE_DESC = "描述";
	
	protected static final String EVENTRULE_TITLE = "关联规则(EventRule)";
	protected static final String EVENTRULE_TITLE_RULENUM = "ruleNum";
	protected static final String EVENTRULE_TITLE_NAME = "name";
	protected static final String EVENTRULE_TITLE_RULETEMPLATE = "ruleTemplate";
	protected static final String EVENTRULE_TITLE_CAT1ID = "cat1id";
	protected static final String EVENTRULE_TITLE_CAT2ID = "cat2id";
	protected static final String EVENTRULE_TITLE_ALARMSTATE = "alarmState";
	protected static final String EVENTRULE_TITLE_STATUS = "status";
	protected static final String EVENTRULE_TITLE_RESPONSECFGNAMES = "responsecfgNames";
	protected static final String EVENTRULE_TITLE_ID = "id";
	
	protected static final String EVENTRULEDISPATCH_TITLE = "关联规则(EventRuleDispatch)";
	protected static final String EVENTRULEDISPATCH_TITLE_RULEID = "ruleId";
	protected static final String EVENTRULEDISPATCH_TITLE_ORDER = "order";
	protected static final String EVENTRULEDISPATCH_TITLE_COMPARATORTEMPLATE = "comparatorTemplate";
	protected static final String EVENTRULEDISPATCH_TITLE_TIMEOUT = "timeout";
	
}
