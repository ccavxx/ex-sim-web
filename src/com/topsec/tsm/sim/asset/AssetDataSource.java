package com.topsec.tsm.sim.asset;

public class AssetDataSource extends AssetCategory{

	private String securityObjectType ;
	private String ownerGroup ;
	
	public AssetDataSource(String securityObjectType,String ownerGroup) {
		this.securityObjectType = securityObjectType ;
		this.ownerGroup = ownerGroup ;
	}
	public AssetDataSource(String securityObjectType,String ownerGroup,int order) {
		this.securityObjectType = securityObjectType ;
		this.ownerGroup = ownerGroup ;
		super.setOrder(order) ;
	}

	public String getSecurityObjectType() {
		return securityObjectType;
	}

	public void setSecurityObjectType(String securityObjectType) {
		this.securityObjectType = securityObjectType;
	}
	
	public String getOwnerGroup() {
		return ownerGroup;
	}
	
	public void setOwnerGroup(String ownerGroup) {
		this.ownerGroup = ownerGroup;
	}
	
}
