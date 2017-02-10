package com.topsec.tsm.sim.sysconfig.bean;

import java.util.ArrayList;
import java.util.List;

import com.topsec.tsm.sim.sysman.bean.EventRuleConfig;

public class CorrRuleConfig {

	private Integer groupId;
	private Integer status;
	private String groupName;
	private Integer priority;
	private Integer cat1id;
	private Integer cat2id;
	private Integer alarmState;
	private Integer timeout;
	private String desc;
	private List<Integer> knowledgeId = new ArrayList<Integer>();
	private List<String> responseIds = new ArrayList<String>();
	private EventRuleConfig[] eventRuleConfigs;

	// private CorrelationCmp[] combinations;

	/**
	 * @return the groupId
	 */
	public Integer getGroupId() {
		return groupId;
	}

	/**
	 * @param groupId
	 *            the groupId to set
	 */
	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}

	/**
	 * @return the status
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}

	/**
	 * @return the groupName
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * @param groupName
	 *            the groupName to set
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	/**
	 * @return the priority
	 */
	public Integer getPriority() {
		return priority;
	}

	/**
	 * @param priority
	 *            the priority to set
	 */
	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	/**
	 * @return the cat1id
	 */
	public Integer getCat1id() {
		return cat1id;
	}

	/**
	 * @param cat1id
	 *            the cat1id to set
	 */
	public void setCat1id(Integer cat1id) {
		this.cat1id = cat1id;
	}

	/**
	 * @return the cat2id
	 */
	public Integer getCat2id() {
		return cat2id;
	}

	/**
	 * @param cat2id
	 *            the cat2id to set
	 */
	public void setCat2id(Integer cat2id) {
		this.cat2id = cat2id;
	}

	/**
	 * @return the alarmState
	 */
	public Integer getAlarmState() {
		return alarmState;
	}

	/**
	 * @param alarmState
	 *            the alarmState to set
	 */
	public void setAlarmState(Integer alarmState) {
		this.alarmState = alarmState;
	}

	/**
	 * @return the timeout
	 */
	public Integer getTimeout() {
		return timeout;
	}

	/**
	 * @param timeout
	 *            the timeout to set
	 */
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	/**
	 * @return the knowledgeId
	 */
	public List<Integer> getKnowledgeId() {
		return knowledgeId;
	}

	/**
	 * @param knowledgeId
	 *            the knowledgeId to set
	 */
	public void setKnowledgeId(List<Integer> knowledgeId) {
		this.knowledgeId = knowledgeId;
	}

	/**
	 * @return the responseIds
	 */
	public List<String> getResponseIds() {
		return responseIds;
	}

	/**
	 * @param responseIds
	 *            the responseIds to set
	 */
	public void setResponseIds(List<String> responseIds) {
		this.responseIds = responseIds;
	}

	/**
	 * @return the eventRuleConfigs
	 */
	public EventRuleConfig[] getEventRuleConfigs() {
		return eventRuleConfigs;
	}

	/**
	 * @param eventRuleConfigs
	 *            the eventRuleConfigs to set
	 */
	public void setEventRuleConfigs(EventRuleConfig[] eventRuleConfigs) {
		this.eventRuleConfigs = eventRuleConfigs;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

}
