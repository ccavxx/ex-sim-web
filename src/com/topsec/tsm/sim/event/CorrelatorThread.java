package com.topsec.tsm.sim.event;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class CorrelatorThread extends Thread {
	private boolean flag = true;

	public CorrelatorThread(String name) {
		setName(name);
	}

	public CorrelatorThread() {
	}

	public void run() {
		while (flag) {
			Serializable obj = CorrelatorList.getInstance().getMessage();
			if (obj != null) {
				List result = (List) obj;
				for (Iterator it = result.iterator(); it.hasNext();) {
					Map map = (Map) it.next();
					CorrelatorCache.add(map);
				}
			}
		}
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}
}
