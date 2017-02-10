package com.topsec.tsm.sim.asset.web.vtclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.topsec.tal.base.util.PropertyManager;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.common.SIMConstant;
import com.topsec.tsm.sim.asset.exception.LimitedNumException;
import com.topsec.tsm.util.UUIDUtils;
import com.topsec.tsm.util.ticker.Tickerable;
public class ConnectionProxyFactory implements Tickerable{
	
	private static final Map<String,ProxyType> supportTypes = new HashMap<String,ProxyType>() ;
	private static boolean loadComplete = false;
	private static final Map<String,ConnectionProxy> cache = new ConcurrentHashMap<String, ConnectionProxy>();
	public static final int PROXY_COUNT_LIMIT = 20 ;//最多允许同时打开的客户端上限
	private static final Logger logger = LoggerFactory.getLogger(ConnectionProxyFactory.class) ;
	public static ConnectionProxy getProxy(String sessionId){
		ConnectionProxy proxy = cache.get(sessionId) ;
		if (proxy != null) {
			proxy.setLastAccessTimes(System.currentTimeMillis()) ;
		}
		return proxy ;
	}
	
	public static void deleteProxy(String sessionId){
		try{
			ConnectionProxy proxy = cache.remove(sessionId) ;
			if (proxy != null) {
				proxy.close() ;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static ConnectionProxy createProxy(String type,String ip,String username,String password,int port) throws ProxyException, LimitedNumException{
		if(cache.size() >= PROXY_COUNT_LIMIT){
			throw new LimitedNumException(String.valueOf(PROXY_COUNT_LIMIT)) ;
		}
		if(!loadComplete){
			loadProxyTypes() ;
		}
		ProxyType proxyType = supportTypes.get(type) ;
		if (proxyType == null) {
			throw new ProxyException("不支持的代理方式！") ;
		}
		String id = UUIDUtils.compactUUID() ;
		ConnectionProxy proxy = proxyType.newInstance(id, ip, username, password, port);
		return proxy ;
	}
	
	public static int getDefaultPort(String type){
		if(!loadComplete){
			loadProxyTypes() ;
		}
		ProxyType p = supportTypes.get(type) ;
		int port = p!= null ? p.getDefaultPort() : -1;
		return port ; 
	}

	public static void put(String sessionId, ConnectionProxy proxy) {
		cache.put(sessionId, proxy) ;
	}
	
	private synchronized static void loadProxyTypes(){
		if(loadComplete){
			return ;
		}
		Properties pt = PropertyManager.getResource(SIMConstant.SYS_PROPERTIE_PATH) ;
		for(Map.Entry<?, ?> entry:pt.entrySet()){
				String key = (String) entry.getKey() ;
				String value = (String) entry.getValue() ;
				if(!key.startsWith("proxy")){
					continue ;
				}
				String[] proxyArgs = StringUtil.split(key,"\\.") ;
				if(proxyArgs.length < 3){
					continue ;
				}
				String type = proxyArgs[1] ;//代理类型：ssh,ftp,sftp等
				try {
					ProxyType proxyType = supportTypes.get(type) ;
					if(proxyType == null){
						proxyType = new ProxyType();
						supportTypes.put(type, proxyType) ;
					}
					String arg2 = proxyArgs[2] ; 
					if(arg2.equals("proxyClass")){
						proxyType.setProxyClass(value) ;
					}else if(arg2.equals("defaultPort")){
						proxyType.setDefaultPort(StringUtil.toInt(value,-1)) ;
					}else if(arg2.equals("clientProperties")){//需要由客户端输入的参数
						JSONArray clientInputProperties = (JSONArray) JSON.parse(value) ;
						proxyType.setClientProperties(clientInputProperties) ;
					}else if(arg2.equals("property") && proxyArgs.length == 4){
						proxyType.setProperty(proxyArgs[3], value) ;
					}
				} catch (Exception e) {
					logger.error("{}代理配置解析失败！", type) ;
				}
		}
		loadComplete = true ;
	}
	@Override
	public void onTicker(long ticker) {
		if(cache.size()==0){
			return ;
		}
		List<String> removeSessions = new ArrayList<String>() ;
		long currentTimes = System.currentTimeMillis() ; 
		for(Map.Entry<String, ConnectionProxy> entry:cache.entrySet()){
			long lastAccessTimes = entry.getValue().getLastAccessTimes() ;
			if(currentTimes-lastAccessTimes > 5*60*1000){//超时
				removeSessions.add(entry.getKey()) ;
			}
		}
		if(removeSessions.size()>0){
			for(String sessionId:removeSessions){
				deleteProxy(sessionId) ;
			}
		}
	}

	public static ProxyType getProxyType(String type) {
		if(!loadComplete){
			loadProxyTypes() ;
		}
		return supportTypes.get(type);
	}

}
