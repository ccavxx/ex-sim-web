package com.topsec.tsm.sim.asset.group;

import com.topsec.tsm.sim.asset.AssetObject;

/**
 * 根据资产类型进行资产分组
 * assetCategory表示分组id
 * @author hp
 *
 */
public class GroupByAssetCategory extends AbstractGroupStrategy {

	private static GroupStrategy instance ;
	
	public GroupByAssetCategory(){
	}
	
	public GroupByAssetCategory(GroupStrategy childGroupStrategy){
		super(childGroupStrategy) ;
	}
	
	@Override
	public AssetGroup getGroup(AssetObject asset) {
		return new AssetGroup(asset.getAssetCategory(),asset.getAssetCategoryName()) ;
	}

	@Override
	public String getGroupId(AssetObject asset) {
		return asset.getAssetCategory();
	}

	@Override
	public String getGroupName(AssetObject asset) {
		return asset.getAssetCategoryName() ;
	}

	public static GroupStrategy getInstance(){
		if (instance == null) {
			synchronized (GroupByAssetCategory.class) {
				if (instance == null) {
					instance = new GroupByAssetCategory() ;
				}
			}
		} 
		return instance ;
	}
}
