package com.topsec.tsm.sim.webservice.xmltype;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="Types",namespace="http://www.topsec.com.cn/")
@XmlType
public class DataSourceTypeListElement {

	private List<DataSourceTypeElement> dataSourceTypes ;

	public DataSourceTypeListElement() {
		super();
	}

	public DataSourceTypeListElement(List<DataSourceTypeElement> dataSourceTypes) {
		super();
		this.dataSourceTypes = dataSourceTypes;
	}

	@XmlElement(name="DataSourceType")
	public List<DataSourceTypeElement> getDataSourceTypes() {
		return dataSourceTypes;
	}

	public void setDataSourceTypes(List<DataSourceTypeElement> dataSourceTypes) {
		this.dataSourceTypes = dataSourceTypes;
	}
	
}
