package com.topsec.tsm.sim.alarm;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tsm.base.interfaces.EventListener;
import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.node.comm.EventDispatcher;
import com.topsec.tsm.node.main.ChannelConstants;
import com.topsec.tsm.node.main.ChannelGate;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;

public class AlarmEventListenerSimulator {  
	
	private static final Logger log = LoggerFactory.getLogger(AlarmEventListenerSimulator.class);
	private EventDispatcher alarmEventChannel;
	private List<EventListener> listeners ;
	public void init() {

		try {
			alarmEventChannel = ChannelGate.getEventChannel(ChannelConstants.COMM_ALARM_CHANNEL);
			if(ObjectUtils.isNotEmpty(listeners)){
				for(EventListener lis:listeners){
					alarmEventChannel.regist(lis) ;
				}
			}
		} catch (CommunicationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	public void setListeners(List<EventListener> listeners) {
		this.listeners = listeners;
	}
}
