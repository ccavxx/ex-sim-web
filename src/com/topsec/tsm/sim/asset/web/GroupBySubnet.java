package com.topsec.tsm.sim.asset.web;

import com.topsec.tsm.sim.asset.AssetObject;
import com.topsec.tsm.sim.asset.group.AbstractGroupStrategy;
import com.topsec.tsm.sim.asset.group.AssetGroup;
import com.topsec.tsm.sim.asset.group.GroupStrategy;

public class GroupBySubnet extends AbstractGroupStrategy {

	private static GroupBySubnet instance;
	@Override
	public AssetGroup getGroup(AssetObject asset) {
		return asset.getSubnet();
	}

	@Override
	public String getGroupId(AssetObject asset) {
		SubNet subnet = asset.getSubnet() ;
		if (subnet != null) {
			return subnet.getId() ;
		}
		return null ;
	}

	@Override
	public String getGroupName(AssetObject asset) {
		return getGroupId(asset) ;
	}
	public static GroupStrategy getInstance(){
		if (instance == null) {
			synchronized (GroupBySubnet.class) {
				if (instance == null) {
					instance = new GroupBySubnet() ;
				}
			}
		} 
		return instance ;
	}
}
