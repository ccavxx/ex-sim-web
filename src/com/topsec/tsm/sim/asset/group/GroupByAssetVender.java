package com.topsec.tsm.sim.asset.group;

import com.topsec.tsm.sim.asset.AssetObject;

/**
 * 根据资产厂商对资产进行分类
 * @author hp
 *
 */
public class GroupByAssetVender extends AbstractGroupStrategy{

	public GroupByAssetVender(){}
	
	public GroupByAssetVender(GroupStrategy strategy){
		super(strategy) ;
	}
	
	@Override
	public AssetGroup getGroup(AssetObject asset) {
		AssetGroup group = new AssetGroup(asset.getVender(),asset.getVenderName(),getChildGroupStrategy()) ;
		return group ;
	}

	@Override
	public String getGroupId(AssetObject asset) {
		return asset.getVender() ;
	}

	@Override
	public String getGroupName(AssetObject asset) {
		return asset.getVenderName() ;
	}

	
}
