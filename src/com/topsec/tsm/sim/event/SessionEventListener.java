package com.topsec.tsm.sim.event;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.base.interfaces.EventListener;
import com.topsec.tsm.sim.util.SessionMrg;

public class SessionEventListener implements EventListener{
	public void onEvent(Map<String, Object> arg0) {
	}

	public void onEvent(List<Map<String, Object>> arg0) {
		// rewrite code
		SessionMrg.getInstance().setToSessionMap(arg0);
	}
}
