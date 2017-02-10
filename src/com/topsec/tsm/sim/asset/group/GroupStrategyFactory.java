package com.topsec.tsm.sim.asset.group;

public final class GroupStrategyFactory {

	public static GroupStrategy getGroupStrategy(String groupAlias){
		return getGroupStrategy(groupAlias, null) ;
	}
	
	public static GroupStrategy getGroupStrategy(String groupAlias,GroupStrategy childGroupStrategy){
		if("category".equalsIgnoreCase(groupAlias)){
			return new GroupByAssetCategory(childGroupStrategy) ;
		}else if("vender".equalsIgnoreCase(groupAlias)){
			return new GroupByAssetVender(childGroupStrategy) ;
		}else if("assGroup".equalsIgnoreCase(groupAlias)){
			return new GroupByAssGroup(childGroupStrategy) ;
		}
		return null ;
	}
}
