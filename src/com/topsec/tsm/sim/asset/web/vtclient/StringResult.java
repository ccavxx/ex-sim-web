package com.topsec.tsm.sim.asset.web.vtclient;

public class StringResult implements CommandResult {

	private String result ;
	
	public StringResult(String result){
		this.result = result ;
	}
	
	@Override
	public String getDisplayType() {
		return "string";
	}

	@Override
	public Object getResult() {
		return result;
	}

}
