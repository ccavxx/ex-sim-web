package com.topsec.tsm.sim.node.util;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.framework.util.ResourceLoader;
import com.topsec.tsm.resource.SystemDefinition;

public class CollectorTypeUtil {
	private static String file = SystemDefinition.DEFAULT_CONF_DIR + "collecttype.xml";
	private static CollectorTypeUtil _instance = null;
	private Map<String, CollectorType> result = null;
	private long lastModified;
	
	public synchronized static CollectorTypeUtil getInstance() {
		if (_instance == null) {
			_instance = new CollectorTypeUtil();
			_instance.init();
		}
		return _instance;
	}

	private void init() {
		SAXReader reader = new SAXReader();
		try {			
			
			Document document = reader.read(new ResourceLoader().loadFile(file));
			List<Element> elements = document.selectNodes("collecttypes/collecttype");
			result = new HashMap<String,CollectorType>(elements.size());
			for(Element element:elements){
				String type=element.attributeValue("type");
				String name=element.attributeValue("name");
				boolean allowDupIp = StringUtil.booleanVal(StringUtil.ifBlank(element.attributeValue("allow_dup_ip"),"true")) ;
				String componenttype= element.attributeValue("componenttype");
				if(type != null){
					result.put(type, new  CollectorType(type,name,componenttype,allowDupIp));
				}
			}
			lastModified = new File(file).lastModified();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean isModified(){
		long currentModified = new File(file).lastModified();
		if(lastModified!=currentModified){
			return true;
		}else{
			return false;
		}
	}
	
	public CollectorType getCollectorType(String type){
		if(isModified()){
			_instance.init();
		}
		return result.get(type);
	}
}
