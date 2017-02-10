package com.topsec.tsm.sim.event;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.base.interfaces.EventListener;
import com.topsec.tsm.framework.interfaces.TypeDef;
import com.topsec.tsm.framework.selector.ParseException;
import com.topsec.tsm.framework.selector.Selector;
import com.topsec.tsm.framework.selector.Selectorable;
import com.topsec.tsm.framework.util.TypeDefFactory;
import com.topsec.tsm.resource.SystemDefinition;

public class CacheEventListener implements EventListener{
	private int threadCount = 1;
	private TypeDef typedef = null;
	private Selectorable s = null;

	public CacheEventListener() {
		typedef = TypeDefFactory.createInstance(SystemDefinition.DEFAULT_CONF_DIR + "typedef.xml");
		try {
			s = new Selector(typedef, null).createSelector("SELECTOR(TRUE)");
		} catch (ParseException e) {
			e.printStackTrace();
		}
//		MessageCach.setRe(this);
		startDoMessage();
	}

	private void startDoMessage() {
		for (int i = 0; i < threadCount; i++) {
			new CorrelatorThread("worker " + (i + 1)).start();
		}
	}
	
	public void onEvent(Map<String, Object> arg0) {
	}

	public void onEvent(List<Map<String, Object>> arg0) {
		CorrelatorList.getInstance().putMessage(s.select((List) arg0));
	}
}
