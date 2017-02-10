package com.topsec.tsm.sim.asset;

import java.util.Map;

import com.topsec.tal.base.util.StringUtil;

public class AssetStatus extends AssetAttribute{

	private String stateKey ;

	public AssetStatus(String id, AssetAttributeType type, String field,String stateKey,String label, boolean visible,String groupString,String formatter) {
		super(id, type, field, label, visible,groupString,formatter);
		this.stateKey = stateKey ;
	}
	
	public String getStateKey() {
		return stateKey;
	}
	
	public void setStateKey(String stateKey) {
		this.stateKey = stateKey;
	}

	@Override
	public Object getValue(Object data) {
		if(data == null){
			return null ;
		}
		if(StringUtil.isBlank(stateKey)){
			return data ;
		}
		Map statuData = (Map)data ;
		return statuData.get(stateKey) ;
	}
	
}
