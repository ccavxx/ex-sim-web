package com.topsec.tsm.sim.newreport.dao;

import java.util.List;
import java.util.Map;

/**
 * @ClassName: ReportDao
 * @Declaration: TODo
 * 
 * @author: WangZhiai create on2015年7月14日下午4:04:59
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public interface ReportNewDao {
	public List<Map<String,Object>> findParentTheme();
	public List<Map<String,Object>> findParentTheme(String securityObjectType);
	public List<Map<String,Object>> findRelevanceParentTheme(String securityObjectType);
	public List<Map<String,Object>> findSimpleSubThemes(Integer parentId);
	public Map<String,Object> findDetailSubThemes(Integer parentSubId);
	public List<Map<String,Object>> findDetailSubThemeList(Integer parentId);
	public List<Map<String,Object>> findTableNameList(Integer parentId);
}
