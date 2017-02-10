package com.topsec.tsm.sim.asset;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;

import com.topsec.tal.base.util.StringUtil;

/**
 * 资产属性
 * @author hp
 *
 */
public class AssetAttribute {

	private String id ;
	private String field ;
	private String label ;
	private boolean visible = true;
	private AssetAttributeType type ;
	private Set<String> groups ;
	private String formatter ;
	public AssetAttribute(String field, String label) {
		super();
		this.field = field;
		this.label = label;
	}

	public AssetAttribute(String id,AssetAttributeType type,String field, String label, boolean visible,String groupString,String formatter) {
		super();
		this.id = id ;
		this.type = type ;
		this.field = field;
		this.label = label;
		this.visible = visible;
		this.groups = new HashSet<String>(Arrays.asList(StringUtil.split(groupString))) ;
		this.formatter = formatter ;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public boolean isVisible() {
		return visible;
	}
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	/**
	 * 获取资产属性的值
	 * @param asset
	 * @return
	 */
	public Object getValue(Object data){
		try {
			return PropertyUtils.getNestedProperty(data, field) ;
		} catch(Exception e){
			e.printStackTrace() ;
			return null ;
		}
	}

	
	public AssetAttributeType getType() {
		return type;
	}

	public void setType(AssetAttributeType type) {
		this.type = type;
	}

	public Set<String> getGroups() {
		return groups;
	}

	public void setGroups(Set<String> groups) {
		this.groups = groups;
	}
	/**
	 * 判断一个属性是否属于一个组
	 * @param group
	 * @return
	 */
	public boolean belongTo(String group){
		return groups.contains(group) ;
	}
	/**
	 * 获取格式化的
	 * @param data
	 * @return
	 */
	public Object getFormatValue(Object data){
		AttributeFormatter fmt = AttributeFormatterFactory.get(formatter) ;
		if (fmt != null) {
			return fmt.format(getValue(data)) ;
		}
		return getValue(data) ;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode()*37;
	}

	@Override
	public boolean equals(Object obj) {
		if(this==obj){
			return true ;
		}
		if(!(obj instanceof AssetAttribute)){
			return false ;
		}
		AssetAttribute aa = (AssetAttribute) obj ;
		return this.id.equals(aa.id) ;
	}
	
}
