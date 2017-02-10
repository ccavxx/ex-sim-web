package com.topsec.tsm.sim.asset.group;

import com.topsec.tsm.sim.asset.AssetObject;

public class AbstractGroupStrategy implements GroupStrategy {

	private GroupStrategy childGroupStrategy ;
	
	public AbstractGroupStrategy() {
		super();
	}
	public AbstractGroupStrategy(GroupStrategy childGroupStrategy) {
		this.childGroupStrategy = childGroupStrategy;
	}


	@Override
	public AssetGroup getGroup(AssetObject asset) {
		return null ;
	}

	@Override
	public String getGroupId(AssetObject asset) {
		return null;
	}

	@Override
	public String getGroupName(AssetObject asset) {
		return null;
	}

	@Override
	public GroupStrategy getChildGroupStrategy() {
		return childGroupStrategy;
	}

	@Override
	public void setChildGroupStrategy(GroupStrategy strategy) {
		this.childGroupStrategy = strategy ;
	}
	
}
