package com.topsec.tsm.sim.event;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CorrelatorCache {
	private static LinkedList<Map> cache = new LinkedList<Map>();
	private static int cacheSize = 100;
	
	public static void add( Map value) {
		int size = cache.size();
		if (size > cacheSize) {
			cache.removeFirst();
		}
		cache.addLast(value);
		
	}
	
	public static List getCach(){
		return cache;
	}
	
	public static void removeCach(){
		cache.clear();
	}
	
}
