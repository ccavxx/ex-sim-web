package com.topsec.tsm.sim.auth.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.topsec.tsm.resource.SystemDefinition;

/*
 * 读取\conf\safeMgrConfig.xml文件类，该文件配置了密码安全和登录安全的信息
 */
public class SafeMgrConfigUtil {


	private static Element root = null;

	private static SafeMgrConfigUtil instance = null;

	private static String xmlFile;
	private static final String path = SystemDefinition.DEFAULT_CONF_DIR;
	private Document doc;

	/**
	 * 读取xml文件对象 
	 */
	private SafeMgrConfigUtil() {
		SAXReader sAXReader = new SAXReader();
		try {
			xmlFile = path + "/safeMgrConfig.xml";
			doc = sAXReader.read(xmlFile);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		root = doc.getRootElement();
	}

	/* 获取单例对象 */
	public synchronized static SafeMgrConfigUtil getInstance() {
		if (instance == null) {
			instance = new SafeMgrConfigUtil();
		}
		return instance;
	}

	/**
	 * 通过xml配置中的 <property name="minCount">的name获取该子节点的信息
	 * 
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getSafeMgrConfigByName(String name) {
		Validate.notNull(name);
		List<Element> keys = root.elements();
		if (keys != null) {
			for (Element element : keys) {
				if (name.equals(element.attributeValue("name"))) {
					Map<String, Object> map = new HashMap<String, Object>();
					String defultValue = element.elementTextTrim("defultValue");
					String value = element.elementTextTrim("value");
					map.put("defultValue", defultValue);
					map.put("value", value);
					return map;
				}
			}
		}
		return null;
	}

	/**
	 * 通过xml配置中的 <property name="minCount">的name获取该子节点的value值
	 * 
	 * @param name
	 * @return
	 */
	public String getSafeMgrConfigValueByName(String name) {
		Validate.notNull(name);
		List<Element> keys = root.elements();
		if (keys != null) {
			for (Element element : keys) {
				if (name.equals(element.attributeValue("name"))) {
//					Map<String, Object> map = new HashMap<String, Object>();
					return element.elementTextTrim("value");
				}
			}
		}
		return null;
	}

	public String getValue(String name){
		return getSafeMgrConfigValueByName(name) ;
	}
	
	/**
	 * 获取配置文件中所有的属性对应的值
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getSafeMgrConfigList() {
		List<Element> keys = root.elements();
		Map<String, Object> map = new HashMap<String, Object>();
		if (keys != null) {
			for (Element element : keys) {
				String value = element.elementTextTrim("value");
				// map.put("name", element.attributeValue("name"));
				// map.put("value", value);
				map.put(element.attributeValue("name"), value);

			}
		}
		return map;
	}

	public void updateSafeConfig(String name,String value){
		Element ele = (Element) root.selectSingleNode("property[@name='"+name+"']") ;
		if(ele == null){
			ele = root.addElement("property") ; 
			ele.addAttribute("name", name) ;
			ele.addElement("defaultValue").setText(value) ;
			ele.addElement("value").setText(value) ;
		}else{
			ele.element("value").setText(value) ;
		}
	}
	
	public void store() throws IOException{
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(new OutputStreamWriter(new FileOutputStream(xmlFile), "UTF-8"), format);
		try {
			writer.write(doc);
		}finally{
			writer.close();
		}
	}
}
