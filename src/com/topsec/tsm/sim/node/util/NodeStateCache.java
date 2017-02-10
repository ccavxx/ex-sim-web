package com.topsec.tsm.sim.node.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.topsec.tsm.node.status.ComponentStatusMap;
import com.topsec.tsm.node.status.NodeStatusMap;

public class NodeStateCache {
	private static NodeStateCache _instance = null;

	private Map<String, NodeStatusMap> cache = new ConcurrentHashMap<String, NodeStatusMap>();

	public synchronized static NodeStateCache getInstance() {
		if (_instance == null) {
			_instance = new NodeStateCache();	
			_instance.init();
		}
		return _instance;
	}
	
	private void init(){
		new Thread(new Clearable()).start();
	}

	public void putState(String nameSpace, NodeStatusMap state) {
		cache.put(nameSpace, state);
	}

	public NodeStatusMap getNodeStatus(String nodeNameSpance) {
		NodeStatusMap nodeState = cache.get(nodeNameSpance);
		if (nodeState == null) {
			return null;
		}
		return nodeState;
	}

	public ComponentStatusMap getComponentStates(String nodeNameSpance,
			String componentNameSpace) {
		NodeStatusMap nodeState = cache.get(nodeNameSpance);
		if (nodeState == null) {
			return null;
		}
		return nodeState.getComponentStatusMap(componentNameSpace);
	}

	public Object getComponentState(String nodeNameSpance,
			String componentNameSpace, String stateKey) {
		NodeStatusMap nodeState = cache.get(nodeNameSpance);
		if (nodeState == null) {
			return null;
		}
		ComponentStatusMap map = nodeState
				.getComponentStatusMap(componentNameSpace);

		if (map == null) {
			return null;
		}
		Map<String, Object> result = map.toMap();
		return result.get(componentNameSpace + "." + stateKey);
	}

	class Clearable implements Runnable {
		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(10 * 60 * 1000);
				} catch (InterruptedException e) {
					//e.printStackTrace();
				}
				cache.clear();
			}
		}
	}
}
