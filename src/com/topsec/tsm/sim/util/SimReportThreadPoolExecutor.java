package com.topsec.tsm.sim.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.topsec.tsm.util.thread.TsmThreadFactory;

public class SimReportThreadPoolExecutor extends ThreadPoolExecutor {

	private static BlockingQueue<Runnable> bqueue = new ArrayBlockingQueue<Runnable>(128);
	private static int core = Runtime.getRuntime().availableProcessors();

	public SimReportThreadPoolExecutor(){
		super(core, core , 60,
				TimeUnit.MILLISECONDS, bqueue,
				new TsmThreadFactory("SimReportThreadPool"),//,Thread.MIN_PRIORITY),
				new ThreadPoolExecutor.CallerRunsPolicy());
		
	}
	public SimReportThreadPoolExecutor(int arg0, int arg1, long arg2, TimeUnit arg3,
			BlockingQueue<Runnable> arg4) {
		super(arg0, arg1, arg2, arg3, arg4);
	}

}
