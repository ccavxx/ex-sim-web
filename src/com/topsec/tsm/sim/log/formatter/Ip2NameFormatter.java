package com.topsec.tsm.sim.log.formatter;

import java.util.Map;

import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.AssetObject;

public class Ip2NameFormatter implements FieldFormatter{

	@Override
	public Object format(Object value, Map<String, Object> row) {
		AssetObject ao = null;
		if(value instanceof IpAddress){
			ao =  AssetFacade.getInstance().getAssetByIp(value.toString()) ;
		}else if(value instanceof String){
			ao = AssetFacade.getInstance().getAssetByIp((String)value);
		}
		return ao == null ? "未知" : ao.getName() ;
	}
}
