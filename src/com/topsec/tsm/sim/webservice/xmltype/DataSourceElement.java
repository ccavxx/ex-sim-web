package com.topsec.tsm.sim.webservice.xmltype;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="DataSource")
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class DataSourceElement {

	private String securityObjectType ;
	private String ip ;
	private String assetName ;
	
	public DataSourceElement() {
		super();
	}

	public DataSourceElement(String securityObjectType,String ip,String assetName) {
		super();
		this.securityObjectType = securityObjectType ;
		this.ip = ip;
		this.assetName = assetName ;
	}

	@Override
	public int hashCode() {
		return ip.hashCode()+securityObjectType.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj){
			return true ;
		}
		if(!(obj instanceof DataSourceElement)){
			return false ;
		}
		DataSourceElement ds = (DataSourceElement)obj ;
		return securityObjectType.equals(ds.securityObjectType) && ip.equals(ds.ip);
	}
	@XmlAttribute
	public String getSecurityObjectType() {
		return securityObjectType;
	}

	public void setSecurityObjectType(String securityObjectType) {
		this.securityObjectType = securityObjectType;
	}
	@XmlAttribute
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	@XmlAttribute
	public String getAssetName() {
		return assetName;
	}

	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}
	
}
