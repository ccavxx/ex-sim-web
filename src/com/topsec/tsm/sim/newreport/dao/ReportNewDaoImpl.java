package com.topsec.tsm.sim.newreport.dao;

import java.util.List;
import java.util.Map;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @ClassName: ReportNewDaoImpl
 * @Declaration: TODo
 * 
 * @author: WangZhiai create on2015年7月14日下午4:17:18
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class ReportNewDaoImpl extends HibernateDaoSupport implements ReportNewDao {
	private static final String TYPE_QUERY_ALL_PRTTHEME="select new map(prt.id as id ,prt.securityObjectType as fieldType,prt.reportName as reportName,prt.showUnits as showUnits) from ParentTheme prt order by prt.id";
	private static final String TYPE_QUERY_PRTTHEME="select new map(prt.id as id ,prt.reportName as reportName,prt.showUnits as showUnits,prt.specialFields as specialFields) from ParentTheme prt where prt.securityObjectType =? order by prt.id";
	private static final String TYPE_RELEVANCE_QUERY_PRTTHEME="select new map(prt.id as id ,prt.reportName as reportName,prt.showUnits as showUnits,dtp.coverName as coverReportName) from ParentTheme prt,DeviceTypeParent dtp where prt.id=dtp.parentId and dtp.securityObjectType =? order by dtp.id";
	private static final String QUERY_SIMPLE_SUBTHEME="select new map(psr.id as parentSubId,psr.subId as subId,psr.subReportName as subReportName,psr.showType as showType,psr.showOrder as showOrder,psr.userShow as userShow,psr.reportGroup as reportGroup) from ParentSubRelevance psr where psr.parentId =? order by psr.showOrder ASC";
	private static final String QUERY_DETAIL_SUBTHEME="select new map(psr.reportType as reportType ,psr.linkCondition as linkCondition,psr.specialCondition as specialCondition,psr.groupCondition as groupCondition,psr.sortCondition as sortCondition,psr.dataStructureDesc as dataStructureDesc,psr.queryType as queryType,psr.queryCondition as queryCondition,psr.describe as describe,psr.showType as showType,st.sql as sql,st.allPurposeCondition as allPurposeCondition,st.special as special) from ParentSubRelevance psr ,SubTheme st where psr.subId=st.id and psr.id =? ";
	private static final String QUERY_DETAIL_THEME_BY_PARENT="select new map(pt.reportName as parentReportName,pt.describe as parentDescribe,pt.formatStyle as formatStyle,pt.showUnits as showUnits,psr.reportType as reportType,psr.linkCondition as linkCondition,psr.specialCondition as specialCondition,psr.groupCondition as groupCondition,psr.sortCondition as sortCondition,psr.subReportName as subReportName,psr.dataStructureDesc as dataStructureDesc,psr.queryType as queryType,psr.queryCondition as queryCondition,psr.site as site,psr.describe as subDescribe,psr.showType as showType,psr.showOrder as showOrder,psr.userShow as userShow,psr.reportGroup as reportGroup,st.sql as sql,st.allPurposeCondition as allPurposeCondition,st.special as special) from ParentTheme pt,ParentSubRelevance psr,SubTheme st where pt.id=psr.parentId and psr.subId=st.id and pt.id=? group by psr.id order by psr.showOrder Asc";
	private static final String QUERY_TABLE_NAME_BY_PARENT="select new map(st.tableName as tableName) from ParentTheme pt,ParentSubRelevance psr,SubTheme st where pt.id=psr.parentId and psr.subId=st.id and pt.id=? group by psr.id order by psr.showOrder Asc";
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Object>> findParentTheme() {
		return getHibernateTemplate().find(TYPE_QUERY_ALL_PRTTHEME);
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Object>> findParentTheme(String securityObjectType) {
		return getHibernateTemplate().find(TYPE_QUERY_PRTTHEME,securityObjectType);
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Object>> findRelevanceParentTheme(
			String securityObjectType) {
		return getHibernateTemplate().find(TYPE_RELEVANCE_QUERY_PRTTHEME,securityObjectType);
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Object>> findSimpleSubThemes(Integer parentId) {
		return getHibernateTemplate().find(QUERY_SIMPLE_SUBTHEME,parentId);
	}

	@Override
	public Map<String, Object> findDetailSubThemes(Integer parentSubId) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list=getHibernateTemplate().find(QUERY_DETAIL_SUBTHEME,parentSubId);
		if (1==list.size()) {
			return list.get(0);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Object>> findDetailSubThemeList(Integer parentId) {
		return getHibernateTemplate().find(QUERY_DETAIL_THEME_BY_PARENT,parentId);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Object>> findTableNameList(Integer parentId) {
		return getHibernateTemplate().find(QUERY_TABLE_NAME_BY_PARENT,parentId);
	}

}
