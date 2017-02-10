package com.topsec.tsm.sim.event;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.base.interfaces.EventListener;
import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.framework.interfaces.TypeDef;
import com.topsec.tsm.framework.selector.ParseException;
import com.topsec.tsm.framework.selector.Selector;
import com.topsec.tsm.framework.selector.Selectorable;
import com.topsec.tsm.framework.util.TypeDefFactory;
import com.topsec.tsm.node.comm.EventDispatcher;
import com.topsec.tsm.node.main.ChannelConstants;
import com.topsec.tsm.node.main.ChannelGate;
import com.topsec.tsm.resource.SystemDefinition;

public class EventListenerSimulator {
	private List<EventListener> listeners ;

	public void init() {
		try {
			EventDispatcher commandChannel = ChannelGate.getEventChannel(ChannelConstants.COMM_RAW_EVENT_CHANNEL);
			for(EventListener listener:listeners){
				commandChannel.regist(listener);
			}
		} catch (CommunicationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<EventListener> getListeners() {
		return listeners;
	}

	public void setListeners(List<EventListener> listeners) {
		this.listeners = listeners;
	}
}
