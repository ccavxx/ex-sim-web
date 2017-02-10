package com.topsec.tsm.sim.alarm;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.base.interfaces.EventListener;
/**
 * 系统告警Listener
 * @author hp
 *
 */
public class AlarmEventListener implements EventListener {
	
		public void onEvent(Map<String, Object> arg0) {
			AlarmEventCache.getInstence().add(arg0);
		}

		public void onEvent(List<Map<String, Object>> arg0) {
			for(Map<String, Object >obj :arg0){
				AlarmEventCache.getInstence().add(obj);
			}
		}
}
