package com.topsec.tsm.sim.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;



public class SessionMrg {
	private static HashMap sessionMap = new HashMap();
	
	private static SessionMrg sessionMrg = new SessionMrg();
	
	public static SessionMrg getInstance(){
		return sessionMrg;
	}

	public static HashMap getSessionMap() {
		return sessionMap;
	}
	
	//jms实时数据填充
	public static void setToSessionMap(List messages) {
		if(messages!=null&&sessionMap!=null){
			Set keys = sessionMap.entrySet();
			for (Iterator it = keys.iterator(); it.hasNext();) {
				Map.Entry<String,SessionCacheImpl> _map = (Map.Entry<String,SessionCacheImpl>)it.next();
				String key = _map.getKey();
				SessionCacheImpl cachImpl = (SessionCacheImpl)_map.getValue();
				SessionCache cach = cachImpl.getCach();
				List<Map> sessionMessages =  cachImpl.filterMessage(messages);
				if(sessionMessages!=null){
					for(Map msg:sessionMessages){
						cach.add(msg);
					}
//delete code					
//					cachImpl.setCach(cach);
//					sessionMap.put(key, cachImpl);
				}
			}
		}
	}
	
	//删除session消息
	public static void removeMsg(String sessionId){
		sessionMap.remove(sessionId);
	}
	
	//清空session消息
	public static void clearMsg(String sessionId){
		SessionCacheImpl cachImpl = (SessionCacheImpl)sessionMap.get(sessionId);
		SessionCache cach = cachImpl.getCach();
		cach.removeCach();
	}
	
}
