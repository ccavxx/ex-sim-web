package com.topsec.tsm.sim.asset;

public enum AssetAttributeType {
	STATIC,DYNAMIC ;
	@Override
	public String toString() {
		if(this==STATIC){
			return "static" ;
		}else if(this==DYNAMIC){
			return "dynamic" ;
		}
		return "unknow" ;
	}
	
	public static AssetAttributeType parse(String type){
		if(type==null){
			throw new NullPointerException("资产属性类型不能为空") ;
		}
		if(type.equals("static")){
			return STATIC ;
		}else if(type.equals("dynamic")){
			return DYNAMIC ;
		}else{
			throw new RuntimeException("不支持的资产属性类型:"+type) ;
		}
	}
}
