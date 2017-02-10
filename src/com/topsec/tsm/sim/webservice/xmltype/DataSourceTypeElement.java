package com.topsec.tsm.sim.webservice.xmltype;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlType(name="DataSourceType")
@XmlRootElement
public class DataSourceTypeElement {

	private String type ;
	private String name ;
	private Set<DataSourceElement> dataSources ;
	private List<ReportSubjectCategoryElement> reportSubjectCategories ;
	public DataSourceTypeElement() {
		super();
	}
	public DataSourceTypeElement(String type,String name) {
		super();
		this.type = type ;
		this.name = name ;
	}
	
	@Override
	public int hashCode() {
		return type.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj){
			return true ;
		}
		if(!(obj instanceof DataSourceTypeElement)){
			return false ;
		}
		DataSourceTypeElement dst = (DataSourceTypeElement)obj ;
		return type.equals(dst.type);
	}
	@XmlAttribute
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	@XmlAttribute
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlElementWrapper(name="DataSourceList")
	@XmlElement(name="DataSource")
	public Set<DataSourceElement> getDataSources() {
		return dataSources;
	}
	public void setDataSources(Set<DataSourceElement> dataSources) {
		this.dataSources = dataSources;
	}
	
	public void addDataSource(DataSourceElement ds){
		if (dataSources == null) {
			dataSources = new LinkedHashSet<DataSourceElement>() ;
		}
		dataSources.add(ds) ;
	}
	
	@XmlElementWrapper(name="SubjectCategoryList")
	@XmlElement(name="SubjectCategory")
	public List<ReportSubjectCategoryElement> getReportSubjectCategories() {
		return reportSubjectCategories;
	}
	public void setReportSubjectCategories(List<ReportSubjectCategoryElement> reportSubjectCategories) {
		this.reportSubjectCategories = reportSubjectCategories;
	}
	public void addSubjectCategory(ReportSubjectCategoryElement category){
		if(reportSubjectCategories == null){
			reportSubjectCategories = new ArrayList<ReportSubjectCategoryElement>() ;
		}
		reportSubjectCategories.add(category) ;
	}
	
}
