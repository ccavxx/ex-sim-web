package com.topsec.tsm.sim.webservice.xmltype;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * 报表主题
 * @author hp
 *
 */
@XmlRootElement
public class ReportSubjectElement extends GenericIdElement{
	
	private List<EntryElement> fields = new ArrayList<EntryElement>(2);
	private List<Map<String,Object>> data ;
	
	public ReportSubjectElement() {
	}

	public ReportSubjectElement(Integer id, String name) {
		super(id,name) ;
	}

	@XmlElementWrapper(name="Fields")
	@XmlElement(name="Field")
	public List<EntryElement> getFields() {
		return fields;
	}

	public void setFields(List<EntryElement> fields) {
		this.fields = fields;
	}

	public void addFields(EntryElement field){
		fields.add(field) ;
	}
	
	@XmlElement(name="Data")
	@XmlJavaTypeAdapter(value=ListMapAdapter.class)
	public List<Map<String, Object>> getData() {
		return data;
	}

	public void setData(List<Map<String, Object>> data) {
		this.data = data;
	}
	
}
