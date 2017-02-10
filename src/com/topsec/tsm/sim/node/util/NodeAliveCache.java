package com.topsec.tsm.sim.node.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NodeAliveCache {
	private static NodeAliveCache _instance = null;
	
	public static final long interval=2 * 60 * 1000;

	private Map<String,Long> cache = new HashMap<String,Long>();

	public synchronized static NodeAliveCache getInstance() {
		if (_instance == null) {
			_instance = new NodeAliveCache();				
		}
		return _instance;
	}
	
	public synchronized void  putLastAliveTime(String nodeId,long lasttime){
		cache.put(nodeId, lasttime);		
	}
	
	public Long getNodeAliveTime(String nodeId){
		return cache.get(nodeId);
	}
	
	public Map<String,Long> getCache(){
		return cache;
	}
	public boolean isAlive(String nodeId){
		Long time =this.getNodeAliveTime(nodeId);
		if(time==null)
			return false;
		if (new Date().getTime() - time.longValue() > interval) {
			return false;
		}
		return true;
	}
}
