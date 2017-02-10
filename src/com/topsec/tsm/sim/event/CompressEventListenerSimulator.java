package com.topsec.tsm.sim.event;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.base.interfaces.EventListener;
import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.node.comm.EventDispatcher;
import com.topsec.tsm.node.main.ChannelConstants;
import com.topsec.tsm.node.main.ChannelGate;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
 
public class CompressEventListenerSimulator {  
	
	private EventDispatcher commandCompressChannel;
	
	private EventDispatcher commandChannel;
	
	private final String route=".";
	
	class MyEventListener implements EventListener {
		 
		public void onEvent(Map<String, Object> arg0) {
			try {
				commandChannel.send(route, arg0);
			} catch (CommunicationException e) {
				e.printStackTrace();
			}

		}

		public void onEvent(List<Map<String, Object>> arg0) {
			if(arg0!=null){
				for (Map<String, Object> map : arg0) {
					try {
						commandChannel.send(route, map);
					} catch (CommunicationException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void init() {

		try {
			commandCompressChannel = ChannelGate
			.getEventChannel(ChannelConstants.COMM_RAW_COMPRESS_EVENT_CHANNEL);
			
			commandChannel = ChannelGate
			.getEventChannel(ChannelConstants.COMM_RAW_EVENT_CHANNEL);
			
			EventListener eventListener = new MyEventListener();
			
			commandCompressChannel.regist(eventListener);
			
		} catch (CommunicationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private NodeMgrFacade nodeMgrFacade;

	public NodeMgrFacade getNodeMgrFacade() {
		return nodeMgrFacade;
	}

	public void setNodeMgrFacade(NodeMgrFacade nodeMgrFacade) {
		this.nodeMgrFacade = nodeMgrFacade;
	}

}
