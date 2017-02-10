package com.topsec.tsm.sim.sysman.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.topsec.tsm.sim.sysconfig.bean.CorrelationCmp;

public class EventRuleConfig implements Serializable {
		private Integer id;
		private String eventName;
		private Integer status;
		private Integer isAlarm;
		private Integer priority;
		private Integer cat1;
		private Integer cat2;
		private String[] fields;
		private String[] propOps;
		private List<Map<String,Object>> fVals=new ArrayList<Map<String,Object>>();//一个MAP表示一行规则值  val1：第一个参数，val2：第二个参数,dataType：参数值类型
		private Map<String,Object>  prevComp=new HashMap<String, Object>();
		//private List<CorrelationCmp> combinations=new ArrayList<CorrelationCmp>();
		private List<Map<String,Object>> combinations=new ArrayList<Map<String,Object>>();
		private Integer count;
		private Integer time;
		private String[] responseIds;
		private Integer[] knids;
		private String version;
		private String category;
		private long createTime;
		private Integer groupId=-1;
		
		
		public String getCategory() {
			return category;
		}
		public void setCategory(String category) {
			this.category = category;
		}
		public String getVersion() {
			return version;
		}
		public void setVersion(String version) {
			this.version = version;
		}
		public String[] getResponseIds() {
			return responseIds;
		}
		public void setResponseIds(String[] responseIds) {
			this.responseIds = responseIds;
		}
		
		public Integer[] getKnids() {
			return knids;
		}
		public void setKnids(Integer[] knids) {
			this.knids = knids;
		}
		public EventRuleConfig() {
			super();
		}
		public Integer getCount() {
			return count;
		}
		public void setCount(Integer count) {
			this.count = count;
		}
		public Integer getTime() {
			return time;
		}
		public void setTime(Integer time) {
			this.time = time;
		}
		public String[] getFields() {
			return fields;
		}
		public void setFields(String[] fields) {
			this.fields = fields;
		}
		public String[] getPropOps() {
			return propOps;
		}
		public void setPropOps(String[] propOps) {
			this.propOps = propOps;
		}
		public List<Map<String, Object>> getfVals() {
			return fVals;
		}
		public void setfVals(List<Map<String, Object>> fVals) {
			this.fVals = fVals;
		}
		public Integer getPriority() {
			return priority;
		}
		public void setPriority(Integer priority) {
			this.priority = priority;
		}
		public Integer getCat1() {
			return cat1;
		}
		public void setCat1(Integer cat1) {
			this.cat1 = cat1;
		}
		public Integer getCat2() {
			return cat2;
		}
		public void setCat2(Integer cat2) {
			this.cat2 = cat2;
		}
		public String getEventName() {
			return eventName;
		}
		public void setEventName(String eventName) {
			this.eventName = eventName;
		}
		public Integer getStatus() {
			return status;
		}
		public void setStatus(Integer status) {
			this.status = status;
		}
		public Integer getId() {
			return id;
		}
		public void setId(Integer id) {
			this.id = id;
		}
		public long getCreateTime() {
			return createTime;
		}
		public void setCreateTime(long createTime) {
			this.createTime = createTime;
		}
		public Integer getIsAlarm() {
			return isAlarm;
		}
		public void setIsAlarm(Integer isAlarm) {
			this.isAlarm = isAlarm;
		}
		public Integer getGroupId() {
			return groupId;
		}
		public void setGroupId(Integer groupId) {
			this.groupId = groupId;
		}
		/**
		 * @return the prevComp
		 */
		public Map<String, Object> getPrevComp() {
			return prevComp;
		}
		/**
		 * @param prevComp the prevComp to set
		 */
		public void setPrevComp(Map<String, Object> prevComp) {
			this.prevComp = prevComp;
		}
		/**
		 * @return the combinations
		 */
	/*	public CorrelationCmp[] getCombinations() {
			return combinations;
		}*/
		/**
		 * @param combinations the combinations to set
		 */
		/*public void setCombinations(CorrelationCmp[] combinations) {
			this.combinations = combinations;
		}*/
		/**
		 * @return the combinations
		 */
		/**
		 * @return the combinations
		 */
		public List<Map<String, Object>> getCombinations() {
			return combinations;
		}
		/**
		 * @param combinations the combinations to set
		 */
		public void setCombinations(List<Map<String, Object>> combinations) {
			this.combinations = combinations;
		}
		 
		
}
