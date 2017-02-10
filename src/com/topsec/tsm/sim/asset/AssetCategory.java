package com.topsec.tsm.sim.asset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.mapping.Collection;


import com.topsec.tsm.asset.AssStatueStatistic;
import com.topsec.tsm.collector.datasource.DataSource;

/**
 * 资产分类
 * @author hp
 *
 */
public class AssetCategory implements Comparable<AssetCategory>{

	private String id ;
	private String name ;
	private int order ;
	private AssetCategory parent ;
	private List<AssetDataSource> dataSources ;
	private List<AssetCategory> children ;
	private List<String> tools ;
	/**
	 * 资产基本属性
	 */
	private Map<String,AssetAttribute> attributes ;

	public AssetCategory() {
	}
	public AssetCategory(String id) {
		this.id = id;
	}
	public AssetCategory(String id, String name,int order) {
		this(id,name,order,null) ;
	}
	public AssetCategory(String id, String name,int order, AssetCategory parent) {
		this.id = id;
		this.name = name;
		this.parent = parent;
		this.order = order ;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public AssetCategory getParent() {
		return parent;
	}
	public void setParent(AssetCategory parent) {
		this.parent = parent;
	}		
	/**
	 * 获得设备类型的路径id,例如:Firewall/Topsec
	 * @return
	 */
	public String getPathId(){
		StringBuffer path = new StringBuffer() ;
		if(parent!=null){
			path.append(parent.getPathId()).append('/') ;
		}
		path.append(id) ;
		return path.toString() ;
	}
	/**
	 * 获得资产分类的路径名称,例如:防火墙/天融信
	 * @return
	 */
	public String getPathName(){
		StringBuffer fullName = new StringBuffer() ;
		if(parent!=null){
			fullName.append(parent.getPathName()).append('/') ;
		}
		fullName.append(name) ;
		return fullName.toString() ;
	}
	public List<AssetDataSource> getDataSources(String ownerGroup){
		List<AssetDataSource> allDataSources = new ArrayList<AssetDataSource>() ;
		if(parent!=null){
			allDataSources.addAll(parent.getDataSources(ownerGroup)) ;
		}
		if(dataSources!=null){
			for(AssetDataSource ads:dataSources){
				if(ads.getOwnerGroup().equals(ownerGroup)){
					allDataSources.add(ads) ;
				}
			}
		}
		return allDataSources ;
	}
	public List<AssetCategory> getChildren() {
		return children;
	}
	public void setChildren(List<AssetCategory> children) {
		this.children = children;
	}
	public void addChild(AssetCategory child){
		if(children==null){
			children = new ArrayList<AssetCategory>() ;
		}
		children.add(child) ;
	}
	/**
	 * 增加属性
	 * @param property
	 */
	public void addAttribute(AssetAttribute property){
		if (attributes == null) {
			attributes = new LinkedHashMap<String, AssetAttribute>();
		}
		attributes.put(property.getId(),property) ;
	}
	/**
	 * 获得资产的所有属性
	 * @return
	 */
	public List<AssetAttribute> getAttributes(){
		if (attributes != null) {
			return new ArrayList<AssetAttribute>(attributes.values()) ;
		}
		return Collections.emptyList() ;
	}
	public AssetAttribute getAttribute(String id){
		if (attributes != null) {
			return attributes.get(id) ;
		}
		return null ;
	}
	public void addAll(List<AssetAttribute> attributes){
		for(AssetAttribute attr:attributes){
			addAttribute(attr) ;
		}
	}
	/**
	 * 获取指定类型的属性
	 * @param type
	 * @return
	 */
	public List<AssetAttribute> getAttributes(AssetAttributeType type){
		if (attributes != null) {
			List<AssetAttribute> atts = new ArrayList<AssetAttribute>() ;
			for(AssetAttribute attr:attributes.values()){
				if(attr.getType()==type){
					atts.add(attr) ;
				}
			}
			return atts ;
		}
		return Collections.emptyList() ;
	}
	public void addDataSource(AssetDataSource dataSource){
		if(dataSources==null){
			dataSources = new ArrayList<AssetDataSource>() ;
		}
		dataSources.add(dataSource) ;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public List<String> getTools() {
		return tools;
	}
	public void setTools(List<String> tools) {
		this.tools = tools;
	}
	@Override
	public String toString() {
		return getPathName() ;
	}
	/**
	 * 根据pathId查找对象的对象
	 * @param pathId
	 * @return
	 */
	public AssetCategory findByPathId(String pathId){
		String myPathId = getPathId() ;
		if(!pathId.contains(myPathId)){//例如当查找pathId为“Firewall/Topsec”的分类时，当前分类为 “IPS/xxx”就无需再继续往下查找直接返回null
			return null ;
		}
		if(myPathId.equals(pathId)){
			return this ;
		}else if(children!=null){
			for(AssetCategory child:children){
				AssetCategory ac = child.findByPathId(pathId) ;
				if(ac!=null){
					return ac;
				}
			}
		}
		return null ;
	}
	
	public List<AssetAttribute> getAttributesByGroup(String group){
		List<AssetAttribute> allAttributes = getAttributes() ;
		List<AssetAttribute> groupAttributes = new ArrayList<AssetAttribute>() ;
		for(AssetAttribute att:allAttributes){
			if(att.belongTo(group)){
				groupAttributes.add(att) ;
			}
		}
		return groupAttributes ;
	}
	
	@Override
	public int compareTo(AssetCategory o) {
		int anotherOrder = o.order ;
		return (order < o.order ? 1 : (order == anotherOrder ? 0 : -1));
	}
	/**
	 * 排序子分类
	 */
	public void sortChild(){
		if(children != null ){
			Collections.sort(children,Collections.reverseOrder()) ;
			for(AssetCategory cat:children){
				cat.sortChild() ;
			}
		}
		if (dataSources != null) {
			Collections.sort(dataSources,Collections.reverseOrder()) ;
		}
	}
}
