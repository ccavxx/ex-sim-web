package com.topsec.tsm.sim.log.formatter;

import java.util.Map;

import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.base.type.IpLocationUtil;

/**
 * 将ip地址转换为对应的ISP信息
 * @author hp
 *
 */
public class IPLocationFormatter implements FieldFormatter{

	public IPLocationFormatter(){
	}
	
	@Override
	public Object format(Object value, Map<String, Object> row) {
		if(value instanceof IpAddress){
			return getLocation((IpAddress)value) ;
		}
		return value;
	}
	
	private static String getLocation(IpAddress ip){
		long address = ip.getLowAddress() ;
		if(address == 0){
			return "未知" ;
		}
		String ipISP = IpLocationUtil.getFullISP(address) ;
		if(ipISP == null){
			return "未知" ;
		}
		return ipISP;
	}
}
