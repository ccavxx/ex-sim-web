package com.topsec.tsm.sim.log;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.base.interfaces.EventListener;
import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.node.comm.EventDispatcher;
import com.topsec.tsm.node.main.ChannelConstants;
import com.topsec.tsm.node.main.ChannelGate;
import com.topsec.tsm.sim.log.util.LogCache;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;

public class LogListenerSimulator {

	class MyEventListener implements EventListener {
		
		public void onEvent(Map<String, Object> arg0) {
			// TODO Auto-generated method stub
			//System.out.println("events=="+arg0);
			LogCache.getInstance().add(arg0);
		}

		public void onEvent(List<Map<String, Object>> arg0) {
			// TODO Auto-generated method stub
			//System.out.println("events==events==");
			LogCache.getInstance().addAll(arg0);
		}
	}
	public void init() {

		try {

			EventDispatcher commandChannel = ChannelGate
					.getEventChannel(ChannelConstants.COMM_RAW_LOG_CHANNEL);

			commandChannel.regist(new MyEventListener());

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
