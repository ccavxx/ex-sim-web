package com.topsec.tsm.sim.log.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class LogCache {
	private List<Map<String, Object>> cache = new CopyOnWriteArrayList<Map<String, Object>>();
	private int cachSize =100;
	private static LogCache instance = null;
	
	private LogCache() {
		
	}
	public synchronized static LogCache getInstance() {
		if(instance == null)
			instance = new LogCache();
		return instance;
	}
	public void add(Map<String, Object> value) {
		synchronized(cache) {
			int size = cache.size();
			if (size >= cachSize) {
				cache.remove(0);
			}
			cache.add(value);
		}
	}
	public void addAll(List<Map<String, Object>> values) {
		synchronized(cache) {
			int size = cache.size();
			while (size >= (cachSize-values.size()+1)) {
				cache.remove(0);
				size = cache.size();
			}
			cache.addAll(values);
		}
	}
	
	public List<Map<String, Object>> getCache(){
		return new ArrayList<Map<String, Object>>(cache);
	}
	
	public void clearCache(){
		cache.clear();
	}
}
