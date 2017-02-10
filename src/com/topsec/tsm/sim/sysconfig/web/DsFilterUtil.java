package com.topsec.tsm.sim.sysconfig.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.resource.SystemDefinition;
import com.topsec.tsm.sim.common.bean.FilterField;
import com.topsec.tsm.ui.util.StringUtils;

public class DsFilterUtil {
	
	private static Element element= null;
	private static DsFilterUtil instance = null;
	private Map<String, List<FilterField>> FieldCache = new ConcurrentHashMap<String, List<FilterField>>();
    private DsFilterUtil(){
    	SAXReader sAXReader=new SAXReader();
    	 Document doc = null;
 		try {
 			doc = sAXReader.read(this.getClass().getClassLoader().getResource("IndexTemplate.xml"));
// 			doc = sAXReader.read("J:/TopsecServer/conf/node/IndexTemplate.xml");
 		} catch (DocumentException e) {
 			e.printStackTrace();
 		}
 		element=doc.getRootElement();
//
    }
    
    public synchronized static DsFilterUtil getInstance() {
		if (instance == null) {
			instance = new DsFilterUtil();
		}
		return instance;
	}
	
    
    public List<FilterField> getFieldByType(String dsType){
    	List<FilterField> fields = null;
    	fields = FieldCache.get(dsType);
    	if(fields == null){
    		fields = new ArrayList<FilterField>();
    		List<Element> nodes=element.elements("DeviceType");
    		for(Element node :nodes){
    			String type = node.attributeValue("id");
    			if(type!= null && type.equals(dsType)){
    				List<Element> fiellist = node.elements("Field");
    				for(Element field:fiellist){
    					if(!StringUtil.booleanVal(field.attributeValue("visible"))){
    						continue ;
    					}
    					FilterField filterField = new FilterField(field.attributeValue("name"),field.attributeValue("alias"),field.attributeValue("type"));
    					String tmpvalue = field.attributeValue("value");
    					if(tmpvalue != null &&  tmpvalue.trim().length() != 0){
    						String[] values = StringUtils.split(tmpvalue, ",");
    						filterField.setValues(values);
    					}
    					fields.add(filterField);
    				}
    				FieldCache.put(dsType, fields);
    				break;
    			}
    		}
    		
    	}
    	return fields;
    }
	
	public static void main(String[] args){
		DsFilterUtil.getInstance().getFieldByType("UTM/NeuSoft/NetEye-NISG6K");
	}

}
