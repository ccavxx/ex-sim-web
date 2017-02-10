package com.topsec.tsm.sim.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.topsec.tsm.base.type.IpAddress;

public class FtpConfigUtil {

	private static Element element= null;

	private static FtpConfigUtil instance = null;
	
	private static final String path="../../../../conf/";
	
	private FtpConfigUtil() {
		 SAXReader sAXReader=new SAXReader();
		 Document doc = null;
		try {
			doc = sAXReader.read(path+"ftpConfig.xml");
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		element=doc.getRootElement();
	}
	
	public synchronized static FtpConfigUtil getInstance() {
		if (instance == null) {
			instance = new FtpConfigUtil();
		}
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,Object> getFTPConfigByKey(String key){
		Validate.notNull(key);
		List<Element> keys=element.elements();
		if(keys!=null){
			for (Element s : keys) {
				if(key.equals(s.attributeValue("id"))){
					Map<String,Object> map=new HashMap<String, Object>();
					String user=s.elementTextTrim("user");
					String password=s.elementTextTrim("password");
					String home=s.elementTextTrim("home");
					String downPath=s.elementTextTrim("downPath");
					String host=s.elementTextTrim("host");
					String port=s.elementTextTrim("port");
					String encoding=s.elementTextTrim("encoding");
					map.put("user", user);
					map.put("password", password);
					map.put("home", home);
					map.put("downPath", downPath);
					map.put("encoding", encoding);
					map.put("host", IpAddress.getLocalIp().toString());
					map.put("port", port);
					
					List<String> extsList=new ArrayList<String>();
					Element exts=s.element("exts");
					Iterator<Element> iter=exts.elementIterator("ext");
					for (Iterator iterator = iter; iterator
							.hasNext();) {
						Element ext = (Element) iterator.next();
						extsList.add(ext.getTextTrim());
					}
					map.put("exts", extsList);
					return map;
				}
			}
		}
		return null;
	}

}
