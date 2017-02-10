package com.topsec.tsm.sim.log.util;

import java.util.TimerTask;

import com.topsec.tsm.sim.log.web.LogMonitorController;

public class LogTimerTask extends TimerTask {
	public long millis = 20*1000;
	private volatile boolean running = true;
	
	
	public void setRunning(boolean running) {
 		this.running = running;
	}


	@Override
	public void run() {
		while(running){
			try {
				if(LogMonitorController.isSend)
					LogMonitorController.isSend = false;
				else
					LogMonitorController.logOut();
				Thread.sleep(millis);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
