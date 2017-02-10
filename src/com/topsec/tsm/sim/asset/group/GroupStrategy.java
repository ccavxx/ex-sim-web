package com.topsec.tsm.sim.asset.group;

import com.topsec.tsm.sim.asset.AssetObject;

/**
 * 资产分组策略<br>
 * 资产进行分组时支持多级分组<br>
 * 根据分组策略分配到指定组后，在组中可以再根据分组策略的childGroupStrategy对资产再进行分组
 * @author hp
 *
 */
public interface GroupStrategy {

	/**
	 * 将资产分配到指定的组中
	 */
	public AssetGroup getGroup(AssetObject asset) ;
	/**
	 * 返回资产所属组的id
	 * @param asset
	 * @return
	 */
	public String getGroupId(AssetObject asset) ;
	/**
	 * 返回分组名称
	 * @return
	 */
	public String getGroupName(AssetObject asset);
	/**
	 * 子分组策略
	 * @return
	 */
	public GroupStrategy getChildGroupStrategy() ;
	/**
	 * 
	 * @param strategy
	 */
	public void setChildGroupStrategy(GroupStrategy strategy) ;
}
