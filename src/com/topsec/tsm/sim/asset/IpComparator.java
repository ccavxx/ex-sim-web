package com.topsec.tsm.sim.asset;

import java.util.Comparator;

import com.topsec.tsm.base.type.IpAddress;

public class IpComparator implements Comparator<AssetObject> {

	private static IpComparator instance;
	
	private IpComparator(){
	}
	
	@Override
	public int compare(AssetObject o1, AssetObject o2) {
		if(o1 == null){
			return -1 ;
		}
		if(o2==null){
			return 1 ;
		}
		IpAddress ip1 = new IpAddress(o1.getIp()) ;
		IpAddress ip2 = new IpAddress(o2.getIp()) ;
		return ip1.compareTo(ip2) ;
	}
	public static IpComparator getInstance() {
		if (instance == null) {
			synchronized (IpComparator.class) {
				if (instance == null) {
					instance = new IpComparator();
				}
			}
		}
		return instance;
	}
}
