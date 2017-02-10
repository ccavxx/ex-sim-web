package com.topsec.tsm.sim.asset.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.ass.AssetState;
import com.topsec.tsm.sim.asset.AssetObject;
import com.topsec.tsm.sim.asset.AssetUtil;
import com.topsec.tsm.sim.common.tree.Tree;

public class AssetGroup implements Tree<AssetGroup>{

	private String id ;
	private String name ;
	/**
	 * 组中的子分组策略<br>
	 * 在向组中加入资产时，如果当前组也包含子分组策略，资产同样会被分组到当前组所在的子组中
	 */
	private GroupStrategy childGroupStrategy ;
	private List<AssetObject> assets ;
	private List<AssetGroup> children ;
	private Map<String,AssetGroup> childrenIdMap ;
	private AssetGroup parent ;
	public AssetGroup(String id) {
		this.id = id;
	}
	public AssetGroup(String id, String name) {
		this.id = id;
		this.name = name;
	}
	public AssetGroup(String id, String name, GroupStrategy childGroupStrategy) {
		this.id = id;
		this.name = name;
		this.childGroupStrategy = childGroupStrategy;
	}
	public AssetGroup(String id, String name, GroupStrategy childGroupStrategy,AssetGroup parent) {
		this.id = id;
		this.name = name;
		this.childGroupStrategy = childGroupStrategy;
		this.parent = parent ;
	}
	public void setAssets(List<AssetObject> assets) {
		this.assets = assets;
	}

	public List<AssetObject> getAssets() {
		return assets;
	}

	public GroupStrategy getChildGroupStrategy() {
		return childGroupStrategy;
	}
	public void setChildGroupStrategy(GroupStrategy childGroupStrategy) {
		this.childGroupStrategy = childGroupStrategy;
	}
	public List<AssetGroup> getChildren() {
		if(children!=null){
			return new ArrayList<AssetGroup>(children);
		}
		return Collections.emptyList() ;
	}
	
	@Override
	public boolean isLeaf() {
		return ObjectUtils.isEmpty(children);
	}
	public void removeChild(AssetGroup group){
		if(children!=null){
			childrenIdMap.remove(group.getId()) ;
			children.remove(group) ;
		}
	}
	public int getOnlineCount() {
		return assets == null ? 0 : AssetUtil.totalAssetUseState(assets, AssetState.ONLINE);
	}

	public int getOfflineCount() {
		return assets == null ? 0 : AssetUtil.totalAssetUseState(assets, AssetState.OFFLINE);
	}

	public int getCount() {
		return assets==null ? 0 : assets.size() ;
	}

	public int getTodayAlarmCount() {
		return 0;
	}

	public int getTodayEventCount() {
		return 0;
	}
	/**
	 * 创建一个组的实例对象
	 * @param groupId
	 * @param name
	 * @param childGroupStrategy
	 * @return
	 */
	public AssetGroup createInstance(String groupId,String name,GroupStrategy childGroupStrategy){
		return new AssetGroup(groupId, name, childGroupStrategy,this) ;
	}
	
	/**
	 * 将资产添加到组中<br>
	 * 如果当前组childGroupStrategy不为null、会继续使用childGroupStrategy对资产再进行分组<br>
	 * 然后将资产分配到新的子组中
	 * @param obj
	 * @return
	 */
	public void addAsset(AssetObject obj) {
		if(childGroupStrategy != null){
			String groupId = childGroupStrategy.getGroupId(obj) ;
			AssetGroup group = getChildById(groupId) ;
			if(group==null){
				group = createInstance(groupId,childGroupStrategy.getGroupName(obj),childGroupStrategy.getChildGroupStrategy()) ;
				addChild(group) ;
			}
			group.addAsset(obj) ;
		}else{
			if(assets==null){
				assets = new CopyOnWriteArrayList<AssetObject>() ;
			}
			assets.add(obj) ;
		}
	}
	
	public void removeAsset(AssetObject asset){
		if (childGroupStrategy != null) {
			String groupId = childGroupStrategy.getGroupId(asset) ;
			AssetGroup group = getChildById(groupId) ;
			if (group != null) {
				group.removeAsset(asset) ;
			}
		}else if (assets != null) {
			assets.remove(asset) ;
		}
	}
	
	public AssetGroup getChildById(String groupId){
		if(childrenIdMap == null){
			return null ;
		}
		return childrenIdMap.get(groupId) ;
	}
	/**
	 * 根据组id查找组对象，如果组不存在，根据当前的组id创建一个新的组，并将新组加入到children中
	 * @param groupId
	 * @return
	 */
	public AssetGroup getOrCreateChildById(String groupId){
		AssetGroup child = getChildById(groupId) ;
		if (child != null) {
			return child ;
		}
		addChild(createInstance(groupId, null, null)) ;
		return (AssetGroup) child ;
	}
	
	public void addChild(AssetGroup group){
		if(children==null){
			children = new CopyOnWriteArrayList<AssetGroup>() ;
			childrenIdMap = new HashMap<String, AssetGroup>() ;
		}
		children.add(group) ;
		childrenIdMap.put(group.getId(), group) ;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id ;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public int hashCode() {
		return this.getId().hashCode()*37+17 ;
	}
	
	/**
	 * 资产分组告警数<br>
	 * 如果当前分组包含子分组，子分组计算告警数量。否则计算资产告警总数
	 * @return
	 */
	public int getAlarmCount(){
		if (assets == null && children == null) {
			return 0 ;
		}
		int count = 0 ;
		if (children != null) {
			for (AssetGroup group : children) {
				count += group.getAlarmCount() ;
			}
		}else{
			for (AssetObject asset : assets) {
				count+=asset.getAlarmCount() ;
			}
		}
		return count ;
	}
	
	public int getEventCount(){
		if (assets == null && children==null) {
			return 0 ;
		}
		int eventCount = 0 ;
		if (children != null) {
			for (AssetGroup group : children) {
				eventCount+=group.getEventCount() ;
			}
		}else{
			for (AssetObject asset : assets) {
				eventCount+=asset.getEventCount() ;
			}
		}
		return eventCount ;
	}
	
	/**
	 * 返回组内所有资产，包含子组内的资产
	 * @return
	 */
	public List<AssetObject> getAllAssets(){
		List<AssetObject> allAssets = new ArrayList<AssetObject>() ;
		addAssetsToList(this, allAssets) ;
		return allAssets ;
	}
	
	private void addAssetsToList(AssetGroup group,List<AssetObject> allAssets){
		if (group.assets != null && assets.size()>0) {
			allAssets.addAll(assets) ;
		}
		if (children != null) {
			for(AssetGroup childGroup:group.getChildren()){
				addAssetsToList(childGroup, allAssets) ;
			}
		}
	}
	
	public void clear(){
		if (assets != null) {
			assets.clear() ;
		}
		if (children != null) {
			children.clear() ;
		}
		if (childrenIdMap != null) {
			childrenIdMap.clear() ;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj==this){
			return true ;
		}
		if(!(obj instanceof AssetGroup)){
			return false ;
		}
		AssetGroup as = (AssetGroup)obj ;
		return this.getId().equals(as.getId()) ;
	}
	@Override
	public String toString() {
		return JSONObject.toJSONString(this) ;
	}
	@Override
	public int getLevel() {
		int level = 1 ;
		AssetGroup group = this ;
		while((group = group.getParent()) != null){
			level++ ;
		}
		return level ;
	}
	@Override
	public AssetGroup getParent() {
		return parent ;
	}
	public void setParent(AssetGroup parent) {
		this.parent = parent;
	}
	public String getPathId(){
		StringBuffer pathId = new StringBuffer(this.id) ;
		AssetGroup parentGroup = parent ;
		while(parentGroup != null && StringUtil.isNotBlank(parentGroup.getId())){
			pathId.append("/").append(parentGroup.getId()) ;
			parentGroup = parentGroup.getParent() ;
		}
		return pathId.toString() ;
	}
}
