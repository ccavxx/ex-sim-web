package com.topsec.tsm.sim.webservice.xmltype;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.topsec.tsm.sim.report.persistence.RptMaster;



@XmlType(name="ReportSubjectCategory")
@XmlRootElement
public class ReportSubjectCategoryElement extends GenericIdElement{

	
	private List<ReportSubjectElement> subjects = new ArrayList<ReportSubjectElement>() ;

	public ReportSubjectCategoryElement() {
	}

	public ReportSubjectCategoryElement(Integer id, String name) {
		super(id,name) ;
	}

	public void addSubject(ReportSubjectElement subject){
		subjects.add(subject) ;
	}

	@XmlElement(name="Subject")
	@XmlElementWrapper(name="SubjectList")
	public List<ReportSubjectElement> getSubjects() {
		return subjects;
	}

	public void setSubjects(List<ReportSubjectElement> subjects) {
		this.subjects = subjects;
	}
	
}
