package com.topsec.tsm.sim.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class SessionCache {
	private List<Map<String,Object>> cache = new CopyOnWriteArrayList<Map<String,Object>>();
	private int cacheSize = 50;
	
	public void add(Map<String,Object> value) {
		synchronized(this) {
			int size = cache.size();
			if (size >= cacheSize) {
				cache.remove(0);
			}
			cache.add(value);
		}
	}
	
	public List<Map<String,Object>> getCach(){
		return new ArrayList<Map<String,Object>>(cache);
	}
	/**
	 * 
	 * @param count
	 * @return
	 */
	public List<Map<String,Object>> pop(int count){
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>(count) ;
		synchronized(this) {
			int limit = count > cache.size() ? cache.size() : count ;
			for(int i=0;i<limit;i++){
				result.add(cache.remove(0)) ;
			}
		}
		return result ;
	}
	
	public void removeCach(){
		synchronized (cache) {
			cache.clear();
		}
	}
}
