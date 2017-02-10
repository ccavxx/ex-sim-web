package com.topsec.tsm.sim.common.bean;

public enum State {
	
	DISABLED,ENABLED ;
	
	public static State parse(Integer state){
		if(state == null) return null ;
		if(state == 1){
			return ENABLED ;
		}else if(state == 0){
			return DISABLED ;
		}
		throw new IllegalArgumentException(String.valueOf(state)) ;
	}
}
