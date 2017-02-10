package com.topsec.tsm.sim.asset.group;

import com.topsec.tsm.sim.asset.AssetObject;

public class GroupByAssGroup extends AbstractGroupStrategy{

	public GroupByAssGroup(){
	}
	
	public GroupByAssGroup(GroupStrategy childGroupStrategy){
		super(childGroupStrategy) ;
	}
	
	
	@Override
	public String getGroupId(AssetObject asset) {
		return String.valueOf(asset.getAssGroup().getGroupId());
	}

	@Override
	public String getGroupName(AssetObject asset) {
		return asset.getAssGroup().getGroupName();
	}

}
