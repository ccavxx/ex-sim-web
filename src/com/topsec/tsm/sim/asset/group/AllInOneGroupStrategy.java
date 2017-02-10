package com.topsec.tsm.sim.asset.group;

import com.topsec.tsm.sim.asset.AssetObject;

/**
 * 所有资产分为同一组
 * @author hp
 *
 */
public class AllInOneGroupStrategy extends AbstractGroupStrategy {

	private String groupId ;
	private String groupName ;
	
	public AllInOneGroupStrategy(String groupId, String groupName) {
		super();
		this.groupId = groupId;
		this.groupName = groupName;
	}

	public AllInOneGroupStrategy() {
		this("all","全部") ;
	}

	@Override
	public String getGroupId(AssetObject asset) {
		return groupId;
	}

	@Override
	public String getGroupName(AssetObject asset) {
		return groupName;
	}

}
