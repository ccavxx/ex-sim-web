package com.topsec.tsm.sim.webservice.xmltype;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="field")
public class EntryElement {

	private String name ;
	private String value ;
	
	public EntryElement() {
	}

	public EntryElement(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	@XmlAttribute
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof EntryElement)) {
			return false;
		}
		EntryElement ee = (EntryElement) obj;
		return name.equals(ee.name) ;
	}
	
}
