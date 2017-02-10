/**
* 版权声明北京天融信科技有限公司，版权所有违者必究
*
* Copyright：Copyright (c) 2011
* Company：北京天融信科技有限公司
* @author 杨轩嘉（yang_xuanjia@topsec.com.cn）
* @since  2011-12-16
* @version 1.0
* 
*/
package com.topsec.tsm.sim.asset;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.topsec.tal.base.util.StringUtil;

/**
* 功能描述: 日志源名称片段中文对应工具类
*/
public class DeviceTypeShortKeyUtil {

	private Map<String,ShortKey> innerMap;
	private static DeviceTypeShortKeyUtil instance = null;
	
	private static final String path="../../../../conf/";
	
	@SuppressWarnings("unchecked")
	private DeviceTypeShortKeyUtil() {
		innerMap=new HashMap<String, ShortKey>();
		SAXReader sAXReader=new SAXReader();
		Document doc = null;
		try {
			doc = sAXReader.read(path+"deviceTypeShortKey.xml");
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		
		Element root=doc.getRootElement();
		
		List<Element> elements = root.elements("type");
		if(elements!=null){
			for (Element element : elements) {
				 String en = element.attributeValue("en");
				 String cn = element.attributeValue("cn");
				 String order = element.attributeValue("order") ;
				 innerMap.put(en, new ShortKey(cn, StringUtil.toInt(order, Integer.MAX_VALUE)));
			}
		}
	}
	
	public synchronized static DeviceTypeShortKeyUtil getInstance() {
		if (instance == null) {
			instance = new DeviceTypeShortKeyUtil();
		}
		return instance;
	}
	
	public String getShortZhCN(String en){
		Validate.notNull(en);
		en = en.startsWith("Monitor") ? en.substring(7) : en;
		ShortKey key = innerMap.get(en);
		return key == null ? en : key.cn;
	}

	public ShortKey getShortKey(String en){
		en = en.startsWith("Monitor") ? en.substring(7) : en;
		ShortKey key = innerMap.get(en) ;
		return key == null ? new ShortKey(en, Integer.MAX_VALUE) : key;
	}
	
	public String[] getShortZhCN(String... enArray){
		String[] result = new String[enArray.length] ;
		for(int i=0;i<enArray.length;i++){
			result[i] = getShortZhCN(enArray[i]) ;
		}
		return result ;
	}
	/**
	 * 将deviceType根据/分割后将每个类型转换为中文后再连接起来
	 * @param deviceType
	 * @param joinString　重新连接时使用的字符串
	 * @return
	 */
	public String deviceTypeToCN(String deviceType,String joinString){
		return StringUtil.join(getShortZhCN(StringUtil.split(deviceType,"/")),joinString) ;
	}
	
	public static class ShortKey{
		public String cn ;
		public int order ;
		 
		public ShortKey(String cn, int order) {
			super();
			this.cn = cn;
			this.order = order;
		}
	}
}
