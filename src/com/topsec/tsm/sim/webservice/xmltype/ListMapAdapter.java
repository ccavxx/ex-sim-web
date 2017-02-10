package com.topsec.tsm.sim.webservice.xmltype;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;

public class ListMapAdapter extends XmlAdapter<Object, List<Map<String,Object>>> {

	@Override
	public List<Map<String,Object>> unmarshal(Object value) throws Exception {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public Object marshal(List<Map<String, Object>> data) throws Exception {
		if(ObjectUtils.isEmpty(data)){
			return null ;
		}
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();  
        DocumentBuilder db = dbf.newDocumentBuilder();  
        Document document = db.newDocument();  
        Element rootElement = document.createElement("data");  
        document.appendChild(rootElement);  
        for (Map<String, Object> record : data) {  
            Element recordElement = document.createElement("record");  
            for(Map.Entry<String, Object> entry:record.entrySet()){
            	recordElement.setAttribute(entry.getKey(), StringUtil.toString(entry.getValue())) ;
            }
            rootElement.appendChild(recordElement);  
        }  
		return rootElement;
	}

}
