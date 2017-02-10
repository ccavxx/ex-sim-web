package com.topsec.tsm.sim.sysman.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RespConfig implements Serializable {
	
	private String respName;
	private String respDesc;
	private String respCfgType;
	private String subType;
	private String cfgType;
 
	private List<Map<String,Object>>  cfgItems=new ArrayList<Map<String,Object>>();

	
	
	
	
	public RespConfig() {
		super();
	}

	public String getRespName() {
		return respName;
	}

	public void setRespName(String respName) {
		this.respName = respName;
	}

	public String getRespDesc() {
		return respDesc;
	}

	public void setRespDesc(String respDesc) {
		this.respDesc = respDesc;
	}

	public String getRespCfgType() {
		return respCfgType;
	}

	public void setRespCfgType(String respCfgType) {
		this.respCfgType = respCfgType;
	}

	public String getSubType() {
		return subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}

	public String getCfgType() {
		return cfgType;
	}

	public void setCfgType(String cfgType) {
		this.cfgType = cfgType;
	}

	public List<Map<String, Object>> getCfgItems() {
		return cfgItems;
	}

	public void setCfgItems(List<Map<String, Object>> cfgItems) {
		this.cfgItems = cfgItems;
	}
	
	
	
	
	
}
