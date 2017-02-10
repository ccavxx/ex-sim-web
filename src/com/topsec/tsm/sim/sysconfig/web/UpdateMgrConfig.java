package com.topsec.tsm.sim.sysconfig.web;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;


public class UpdateMgrConfig {

	private static Element element = null;

	private static UpdateMgrConfig instance = null;

	private static String xmlFile;
	private static final String path = "../../../../conf/";
	private Document doc;

	/* 读取xml文件对象 */
	private UpdateMgrConfig() {
		SAXReader sAXReader = new SAXReader();
		try {
			 doc = sAXReader.read(path + "updateMgrConfig.xml");
			 xmlFile = path + "updateMgrConfig.xml";
			doc = sAXReader.read(xmlFile);

		} catch (DocumentException e) {
			e.printStackTrace();
		}
		element = doc.getRootElement();
	}

	/* 获取单例对象 */
	public synchronized static UpdateMgrConfig getInstance() {
		if (instance == null) {
			instance = new UpdateMgrConfig();
		}
		return instance;
	}

	/**
	 * 通过xml配置中的 <property name="date">的name获取该子节点的信息
	 * 
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getUpdateMgrConfigUtilByName(String name) {
		Validate.notNull(name);
		List<Element> keys = element.elements();
		if (keys != null) {
			for (Element element : keys) {
				if (name.equals(element.attributeValue("name"))) {
					Map<String, Object> map = new HashMap<String, Object>();
					String value = element.elementTextTrim("value");
					map.put("value", value);

					Element nodeIdsElement = element.element("values");
					if (nodeIdsElement != null) {
						List<String> nodeIdsList = new ArrayList<String>();
						Iterator<Element> iter = nodeIdsElement.elementIterator("value");
						for (Iterator iterator = iter; iterator.hasNext();) {
							Element nodeIdsEle = (Element) iterator.next();
							nodeIdsList.add(nodeIdsEle.getTextTrim());
						}
						map.put("value", nodeIdsList);
					}
					return map;
				}
			}
		}
		return null;
	}

	/**
	 * 通过xml配置中的 <property name="date">的name获取该子节点的value值
	 * 
	 * @param name
	 * @return
	 */
	public Object getUpdateMgrConfigUtilValueByName(String name) {
		Validate.notNull(name);
		List<Element> keys = element.elements();
		if (keys != null) {
			for (Element element : keys) {
				if (name.equals(element.attributeValue("name"))) {

					Element nodeIdsElement = element.element("values");
					if (nodeIdsElement != null) {
						List<String> nodeIdsList = new ArrayList<String>();
						Iterator<Element> iter = nodeIdsElement.elementIterator("value");
						for (Iterator iterator = iter; iterator.hasNext();) {
							Element nodeIdsEle = (Element) iterator.next();
							nodeIdsList.add(nodeIdsEle.getTextTrim());
						}
						return nodeIdsList;
					}

					return element.elementTextTrim("value");
				}
			}
		}
		return null;
	}

	/**
	 * 获取配置文件中所有的属性对应的值
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getUpdateMgrConfigUtilList() {
		List<Element> keys = element.elements();
		Map<String, Object> map = new HashMap<String, Object>();
		if (keys != null) {
			for (Element element : keys) {
				String value = element.elementTextTrim("value");
				map.put(element.attributeValue("name"), value);

				Element nodeIdsElement = element.element("values");
				if (nodeIdsElement != null) {
					List<String> nodeIdsList = new ArrayList<String>();
					Iterator<Element> iter = nodeIdsElement.elementIterator("value");
					for (Iterator iterator = iter; iterator.hasNext();) {
						Element nodeIdsEle = (Element) iterator.next();
						nodeIdsList.add(nodeIdsEle.getTextTrim());
					}
					map.put(element.attributeValue("name"), nodeIdsList);
				}

			}
		}
		return map;
	}

	/**
	 * 指定属性name和值value来更新配置文件
	 * 
	 * @param name
	 * @param value
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void updateConfigByName(String name, Object value) throws IOException {
		Validate.notNull(name);
		if (value == null) {
			if ("nodeIds".equals(name)) {
				String[] _value = new String[1];
				_value[0] = "";
				value = _value;
			} else {
				value = "";
			}
		}

		List<Element> keys = element.elements();
		if (keys != null) {
			for (Element element : keys) {
				if (name.equals(element.attributeValue("name"))) {
					List<Element> elements = element.elements();
					for (Element e : elements) {
						if (value instanceof String[] && e.elements() != null) {// 如果是多个值
							List<Element> valuesEle = e.elements();
							String[] _value = (String[]) value;
							if (_value != null && _value.length > 0) {
								for (Element _valueEle : valuesEle) {
									_valueEle.detach();// 删除原来的
								}
								for (String val : _value) {
									e.addElement("value").setText(val);// 设置值
								}
							}
						}
						if (value != null && value instanceof String) {
							if ("value".equals(e.getName())) {
								e.setText(value.toString());
							}
						}
					}
				}
			}
		}
		writeToXml(doc);
	}

	public static void writeToXml(Document document) throws IOException {
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(new OutputStreamWriter(new FileOutputStream(xmlFile), "utf-8"), format);
		writer.write(document);
		writer.close();
	}

}
