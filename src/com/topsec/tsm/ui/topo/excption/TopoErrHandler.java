package com.topsec.tsm.ui.topo.excption;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopoErrHandler implements UncaughtExceptionHandler {
	private static Logger log = LoggerFactory.getLogger(TopoErrHandler.class);

	public void uncaughtException(Thread t, Throwable e) {
		e.printStackTrace();
		log.error("Thread: " + t.getName() + " error:,Message:" + e.getMessage());
	}  
}