package com.topsec.tsm.sim.sysconfig.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class CorrRuleCondition implements Serializable{
	private Integer groupId;
	private Integer status;
	private String groupName;
	private Integer alarmState;
	private long timeout;
	private Date createTime;
	private List<Integer> eventRuleIdList=new ArrayList<Integer>();
	private CorrelationCmp[] combinations;
	private FreqState[] freqStates;
	private Integer priority;
	private List<Integer> knowledgeId=new ArrayList<Integer>();
	private List<String> responseIds=new ArrayList<String>();
	private Integer cat1id;
	private Integer cat2id;
	private String desc ;
	public Integer getAlarmState() {
		return alarmState;
	}
	public void setAlarmState(Integer alarmState) {
		this.alarmState = alarmState;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public List<Integer> getEventRuleIdList() {
		return eventRuleIdList;
	}
	public void setEventRuleIdList(List<Integer> eventRuleIdList) {
		this.eventRuleIdList = eventRuleIdList;
	}
	public Integer getGroupId() {
		return groupId;
	}
	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}
	public CorrelationCmp[] getCombinations() {
		return combinations;
	}
	public void setCombinations(CorrelationCmp[] combinations) {
		this.combinations = combinations;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public FreqState[] getFreqStates() {
		return freqStates;
	}
	public void setFreqStates(FreqState[] freqStates) {
		this.freqStates = freqStates;
	}
	public long getTimeout() {
		return timeout;
	}
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	/**
	 * @return the priority
	 */
	public Integer getPriority() {
		return priority;
	}
	/**
	 * @param priority the priority to set
	 */
	public void setPriority(Integer priority) {
		this.priority = priority;
	}
 
	/**
	 * @return the knowledgeId
	 */
	public List<Integer> getKnowledgeId() {
		return knowledgeId;
	}
	/**
	 * @param knowledgeId the knowledgeId to set
	 */
	public void setKnowledgeId(List<Integer> knowledgeId) {
		this.knowledgeId = knowledgeId;
	}
	/**
	 * @return Integer
	 */
	public Integer getCat1id() {
		return cat1id;
	}
	/**
	 * @param cat1id Integer
	 */
	public void setCat1id(Integer cat1id) {
		this.cat1id = cat1id;
	}
	/**
	 * @return Integer
	 */
	public Integer getCat2id() {
		return cat2id;
	}
	/**
	 * @param cat2id Integer
	 */
	public void setCat2id(Integer cat2id) {
		this.cat2id = cat2id;
	}
	/**
	 * @return the responseIds
	 */
	public List<String> getResponseIds() {
		return responseIds;
	}
	/**
	 * @param responseIds the responseIds to set
	 */
	public void setResponseIds(List<String> responseIds) {
		this.responseIds = responseIds;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	 
	
}
