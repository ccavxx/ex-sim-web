package com.topsec.tsm.sim.webservice.xmltype;

import javax.xml.bind.annotation.XmlAttribute;

public class GenericIdElement {
	/**id*/
	private Integer id ;
	/**名称*/
	private String name ;
	public GenericIdElement() {
		super();
	}

	public GenericIdElement(Integer id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	
	@XmlAttribute
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof GenericIdElement)) {
			return false;
		}
		GenericIdElement rs = (GenericIdElement) obj;
		return id.equals(rs.id);
	}
}
