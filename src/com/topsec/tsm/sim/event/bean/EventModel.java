package com.topsec.tsm.sim.event.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.util.CommonUtils;

public class EventModel implements Serializable {
	private long eventId ;
	private String name; // 事件名称
	private String srcAddress; // 源地址
	private String destAddress; // 目的地址
	private Integer srcPort;// 源端口
	private Integer destPort;// 目的端口
	private String dvcAddress; // 设备地址
	private Integer priority; // 等级PRIORITY
	private String level;
	private Date createTime; // 事件时间
	private String uuid;
	private String description; // 描述
	private String cat1 ;
	private String cat2 ;
	private String cat3 ;
	private String cat4 ;
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSrcAddress() {
		return srcAddress;
	}

	public void setSrcAddress(String srcAddress) {
		this.srcAddress = srcAddress;
	}

	public String getDestAddress() {
		return destAddress;
	}

	public void setDestAddress(String destAddress) {
		this.destAddress = destAddress;
	}

	public Integer getSrcPort() {
		return srcPort;
	}

	public void setSrcPort(Integer srcPort) {
		this.srcPort = srcPort;
	}

	public Integer getDestPort() {
		return destPort;
	}

	public void setDestPort(Integer destPort) {
		this.destPort = destPort;
	}

	public String getDvcAddress() {
		return dvcAddress;
	}

	public void setDvcAddress(String dvcAddress) {
		this.dvcAddress = dvcAddress;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCat1() {
		return cat1;
	}

	public void setCat1(String cat1) {
		this.cat1 = cat1;
	}

	public String getCat2() {
		return cat2;
	}

	public void setCat2(String cat2) {
		this.cat2 = cat2;
	}

	public String getCat3() {
		return cat3;
	}

	public void setCat3(String cat3) {
		this.cat3 = cat3;
	}

	public String getCat4() {
		return cat4;
	}

	public void setCat4(String cat4) {
		this.cat4 = cat4;
	}

	public long getEventId() {
		return eventId;
	}

	public void setEventId(long eventId) {
		this.eventId = eventId;
	}

	/**
	 * 使用原始map结构事件信息、创建事件对象
	 * @param event
	 * @return
	 */
	public static EventModel createEvent(Map event){
		EventModel model = new EventModel() ;
		Number eventId = (Number) event.get("EVENT_ID") ;
		model.setEventId(eventId != null ? eventId.longValue() : -1) ;
		model.setUuid((String)event.get("UUID"));
		model.setDvcAddress(StringUtil.toString(event.get("DVC_ADDRESS")));
		model.setName((String)event.get("NAME"));
		model.setSrcAddress(StringUtil.toString(event.get("SRC_ADDRESS")));
		model.setDestAddress(StringUtil.toString(event.get("DEST_ADDRESS")));
		if (event.get("PRIORITY") != null) {
			Integer priority = (Integer) event.get("PRIORITY");
			model.setPriority(priority);
			model.setLevel(CommonUtils.getLevel(priority));
		}
		if (event.get("END_TIME") != null) {
			Date evenDate = (Date) event.get("END_TIME");
			model.setCreateTime(evenDate);
		}
		model.setDescription(event.get("DESCR").toString());
		model.setCat1((String)event.get("CAT1_ID")) ;
		model.setCat2((String)event.get("CAT2_ID")) ;
		model.setCat3((String)event.get("CAT3_ID")) ;
		model.setCat4((String)event.get("CAT4_ID")) ;
		return model ;
	}
	
}
