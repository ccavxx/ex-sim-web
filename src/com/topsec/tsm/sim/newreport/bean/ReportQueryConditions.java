package com.topsec.tsm.sim.newreport.bean;

import java.util.HashMap;
import java.util.Map;

import com.topsec.tsm.sim.access.util.GlobalUtil;

/**
 * @ClassName: ReportQueryConditions
 * @Declaration: TODo
 * 
 * @author: WangZhiai create on2015年7月13日下午4:08:19
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class ReportQueryConditions {
	private String id;
	private Long resourceId;
	private String dvcAddress;
	private String securityObjectType;
	private String [] nodeIds;
	private Integer[] parentIds;
	private Integer parentSubId;
	private Integer subId;
	private String stime;
	private String endtime;
	private long pageIndex;
	private long pageSize;//默认第一页的话 可以当成TOP值来用
	private String params;//SRC_ADDRESS=12.3.3.1&OP=REMOVE&S_TIME=2015-01-01 00:00:00&URL=/index/sim,/index/report
	private Integer topn;
	private String exportFormat;//doc,excel...
	private String username;
	private String queryType;
	/**
	 * <p>
	 * params 格式如此：
	 * SRC_ADDRESS=12.3.3.1&OP=REMOVE&S_TIME=2015-01-01 00:00:00&URL=/index/sim,/index/report&...
	 * </p>
	 * @param params
	 * @return
	 */
	private Map<String,Object> paramsToMap(String params){
		Map<String, Object>map=null;
		if (GlobalUtil.isNullOrEmpty(params)) {
			return map;
		}
		map=new HashMap<String, Object>();
		String[]paranStrings=params.split(";");
		for (String string : paranStrings) {
			if (! GlobalUtil.isNullOrEmpty(string) && string.indexOf("=")>0) {
				String[] props=string.split("=");
				if (2==props.length) {
					String property=props[0].trim();
					String value=props[1].trim();
					
					if (! GlobalUtil.isNullOrEmpty(property)) {
						Object [] objs=null;
						if (! GlobalUtil.isNullOrEmpty(value) && value.indexOf(",")>0) {
							objs=value.split(",");
							if (objs.length<=1) {
								map.put(property, objs[0]);
							}else {
								map.put(property, objs);
							}
						}else {
							map.put(property, value);
						}
					}
				}
			}
		}
		return map;
	}
	
	public Map<String, Object> getParamMap(){
		return paramsToMap(this.params);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getResourceId() {
		return resourceId;
	}

	public void setResourceId(Long resourceId) {
		this.resourceId = resourceId;
	}

	public String getDvcAddress() {
		return dvcAddress;
	}

	public void setDvcAddress(String dvcAddress) {
		this.dvcAddress = dvcAddress;
	}

	public String getSecurityObjectType() {
		return securityObjectType;
	}

	public void setSecurityObjectType(String securityObjectType) {
		this.securityObjectType = securityObjectType;
	}

	public String[] getNodeIds() {
		return nodeIds;
	}

	public void setNodeIds(String[] nodeIds) {
		this.nodeIds = nodeIds;
	}

	public Integer[] getParentIds() {
		return parentIds;
	}

	public void setParentIds(Integer[] parentIds) {
		this.parentIds = parentIds;
	}

	public Integer getParentSubId() {
		return parentSubId;
	}

	public void setParentSubId(Integer parentSubId) {
		this.parentSubId = parentSubId;
	}

	public Integer getSubId() {
		return subId;
	}

	public void setSubId(Integer subId) {
		this.subId = subId;
	}

	public String getStime() {
		return stime;
	}

	public void setStime(String stime) {
		this.stime = stime;
	}

	public String getEndtime() {
		return endtime;
	}

	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}

	public long getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(long pageIndex) {
		this.pageIndex = pageIndex;
	}

	public long getPageSize() {
		return pageSize;
	}

	public void setPageSize(long pageSize) {
		this.pageSize = pageSize;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public Integer getTopn() {
		return topn;
	}

	public void setTopn(Integer topn) {
		this.topn = topn;
	}

	public String getExportFormat() {
		return exportFormat;
	}

	public void setExportFormat(String exportFormat) {
		this.exportFormat = exportFormat;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getQueryType() {
		return queryType;
	}

	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}
	
}
