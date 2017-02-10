package com.topsec.tsm.sim.asset.web.vtclient;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

import com.topsec.tal.base.util.StringUtil;

public class ProxyType {

	private String proxyClass ;
	private Map<String,String> properties ;
	private List<Object> clientProperties ;
	private int defaultPort ;

	public ProxyType() {
		super();
	}

	public ProxyType(String proxyClass, Map<String, String> properties, int defaultPort) {
		super();
		this.proxyClass = proxyClass;
		this.properties = properties;
		this.defaultPort = defaultPort;
	}

	@SuppressWarnings("unchecked")
	public ConnectionProxy newInstance(String sessionId, String ip, String name,String password, int port)throws ProxyException{
		try {
			Class<ConnectionProxy> cls = (Class<ConnectionProxy>) Class.forName(proxyClass) ;
			Constructor<ConnectionProxy> cst = cls.getConstructor(String.class,String.class,String.class,String.class,int.class) ;
			if(cst == null){
				throw new ProxyException("不支持的代理类型！") ;
			}
			ConnectionProxy proxy = cst.newInstance(sessionId,ip,name,password,port) ;
			if (properties != null) {
				for(Map.Entry<String, String> property:properties.entrySet()){
					String propertyName = property.getKey() ;
					String value = property.getValue() ;
					Field field = cls.getDeclaredField(propertyName) ;
					Class<?> fieldClass = field.getType() ;
					if(fieldClass == String.class){
						BeanUtils.setProperty(proxy, propertyName, value) ;
					}else if(fieldClass == int.class || fieldClass == Integer.class){
						BeanUtils.setProperty(proxy, propertyName, StringUtil.toInteger(value,field.getInt(proxy))) ;
					}else if(fieldClass == long.class || fieldClass == Long.class){
						BeanUtils.setProperty(proxy, propertyName, StringUtil.toLong(value,field.getLong(proxy))) ;
					}else if(fieldClass == double.class || fieldClass == Double.class){
						BeanUtils.setProperty(proxy, propertyName, StringUtil.toDouble(value,field.getDouble(proxy))) ;
					}
				}
			}
			return proxy ;
		} catch (Exception e) {
			e.printStackTrace() ;
			throw new ProxyException("不支持的代理类型！") ;			
		}
	}

	public String getProxyClass() {
		return proxyClass;
	}

	public void setProxyClass(String proxyClass) {
		this.proxyClass = proxyClass;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public int getDefaultPort() {
		return defaultPort;
	}

	public void setDefaultPort(int defaultPort) {
		this.defaultPort = defaultPort;
	}
	
	public void setProperty(String key,String value){
		if (properties == null) {
			properties = new HashMap<String, String>() ;
		}
		properties.put(key, value) ;
	}

	public List<?> getClientProperties() {
		return clientProperties;
	}

	public void setClientProperties(List<Object> clientProperties) {
		this.clientProperties = clientProperties;
	}

	public void addClientProperty(Object obj){
		if (clientProperties == null) {
			clientProperties = new ArrayList<Object>() ;
		}
		clientProperties.add(obj) ;
	}

}
