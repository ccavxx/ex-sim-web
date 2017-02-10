package com.topsec.tsm.sim.event;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class CorrelatorList {
	private LinkedList messages = new LinkedList();
	
	private int waitWorkers = 0;

	private static CorrelatorList corList = new CorrelatorList();

	public static CorrelatorList getInstance() {
		return corList;
	}

	public void putMessage(List obj) {
		synchronized (messages) {
			messages.addFirst(obj);
			if (waitWorkers > 0)
				messages.notifyAll();
		}
	}

	public void clearMessage() {
		synchronized (messages) {
			messages.clear();
		}
	}

	public Serializable getMessage() {
		synchronized (messages) {

			waitWorkers++;
			while (messages.size() == 0) {
				try {
					messages.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			waitWorkers--;
			return (Serializable) messages.removeLast();
		}
	}
}
