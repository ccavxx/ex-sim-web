package com.topsec.tsm.common.message;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.base.interfaces.EventListener;
import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.node.comm.EventDispatcher;
import com.topsec.tsm.node.main.ChannelConstants;
import com.topsec.tsm.node.main.ChannelGate;
 
public class MonitorEventListenerSimulator{
	
   private MonitorStatisticService monitorStatisticService;
   
   class MyMonitorEventListener implements EventListener {
		
		public void onEvent(Map<String, Object> arg0) {
			monitorStatisticService.statisticHandler(arg0);
		}

		public void onEvent(List<Map<String, Object>> arg0) {
			monitorStatisticService.statisticHandler(arg0);
		}
	}
	 

	public void init() {

		try {

			EventDispatcher evnetChannel = ChannelGate
					.getEventChannel(ChannelConstants.COMM_DEVICESTATE_MONITOR_CHANNEL);

			evnetChannel.regist(new MyMonitorEventListener());
		} catch (CommunicationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public MonitorStatisticService getMonitorStatisticService() {
		return monitorStatisticService;
	}


	public void setMonitorStatisticService(
			MonitorStatisticService monitorStatisticService) {
		this.monitorStatisticService = monitorStatisticService;
	}
	
	
}
