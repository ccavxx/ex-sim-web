/*
 * 版权声明:北京天融信科技有限公司，版权所有违者必究
 * Copyright：Copyright (c) 2011
 * Company：北京天融信科技有限公司
 * @author zhouxiaohu 
 * 2011-08-04
 * @version 1.0
 */
package com.topsec.tsm.sim.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.topsec.tsm.sim.response.persistence.Response;
/**
 * 用于读取SystemConfig.xml
 * 或许所有系统配置信息
 * @author Meteor
 *
 */
public class SystemConfigUtil {
	private static Element element= null;

	private static SystemConfigUtil instance = null;
	
	private static final String path="../../../../conf/";
	//private static final String path="E:\\TopsecServer\\conf\\";
	
	private SystemConfigUtil() {
		 SAXReader sAXReader=new SAXReader();
		 Document doc = null;
		try {
			doc = sAXReader.read(path+"SystemConfigs.xml");
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		element=doc.getRootElement();
	}
	
	public synchronized static SystemConfigUtil getInstance() {
		if (instance == null) {
			instance = new SystemConfigUtil();
		}
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public List<Response> getSystemConfigByKey(String id){
		Validate.notNull(id);
		List<Response> responses = new ArrayList<Response>();
		List<Element> keys=element.elements();
		if(keys!=null){
			for (Element s : keys) {
				if(id.equals(s.attributeValue("nodeType"))){
					Response response=new Response();
					response.setCfgKey(s.attributeValue("key"));
					response.setName(s.attributeValue("name"));
					response.setDesc(s.attributeValue("desc"));
					response.setType(s.attributeValue("type"));
					response.setSubType(s.attributeValue("subType"));
					response.setConfig(s.elementTextTrim("despConfig"));
					responses.add(response);
				}
			}
		}
		return responses;
	}
	public static void main(String[] args) {
		List<Response> responses = SystemConfigUtil.getInstance().getSystemConfigByKey("ReportService");
		System.out.println(responses.get(0).getCfgKey());
	}

}
