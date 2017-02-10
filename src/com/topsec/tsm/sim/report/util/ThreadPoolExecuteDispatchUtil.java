/**
* 版权声明北京天融信科技有限公司，版权所有违者必究
*
* Copyright：Copyright (c) 2011
* Company：北京天融信科技有限公司
* @author 杨轩嘉（yang_xuanjia@topsec.com.cn）
* @since  2011-07-26
* @version 1.0
*/
package com.topsec.tsm.sim.report.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tsm.sim.report.model.ThreadPoolExecuteDispatchUtilListener;

/**
* 功能描述: 线程池执行分发器
* 仿照丁广富的PooingHandler
*/
public class ThreadPoolExecuteDispatchUtil<T extends Object&ThreadPoolExecuteDispatchUtilListener> {
	
	private static final Logger log = LoggerFactory.getLogger(ThreadPoolExecuteDispatchUtil.class);
	
	//	线程池，当选择并行调用时该值必须被设置
	protected ThreadPoolExecutor threadPool;
	
	protected List<InnerParallelExecutor> parallelExecutors;

	protected Object idleLock = new Object();
	
	protected int idleParallelExecutors = 0;
	
	protected List<T> tList;
	
	public ThreadPoolExecuteDispatchUtil(List<T> tList) {
		Validate.notNull(tList);
		this.tList=tList;
		buildParallelExecutors();
	}

	public void execute() {
			Validate.notNull(threadPool);
			if (parallelExecutors == null) {
				buildParallelExecutors();
			}
			
			if (canParallelWork()) {
				for(InnerParallelExecutor executor : parallelExecutors) {
					executor.testAndSetBusy();
					threadPool.execute(executor);
				}
			}
	}
	
	protected void buildParallelExecutors() {
		parallelExecutors = new ArrayList<InnerParallelExecutor>(tList.size());
		for(T t : tList) {
			InnerParallelExecutor executor = new InnerParallelExecutor(t);
			parallelExecutors.add(executor);
		}
		idleParallelExecutors = parallelExecutors.size();
	}
	
	protected boolean canParallelWork() {
		if (idleParallelExecutors == parallelExecutors.size()){
			return true;
		}
		return false;
	}

class InnerParallelExecutor implements Runnable {
		protected T t;
		
		protected boolean busy = false;
		
		public InnerParallelExecutor(T t) {
			this.t = t;
		}
		
		public void run() {
			try {
					try {
						t.onCommand();
					} catch (Exception e) {
						log.error(e.getMessage());
//						e.printStackTrace();
					}
			}
			finally {
				resetBusy();
			}
		}
		
		public synchronized boolean testAndSetBusy() {
			if (!busy) {
				synchronized(idleLock) {
					idleParallelExecutors --;
				}
				busy = true;
				return true;
			}
			return false;
		}

		public synchronized void resetBusy() {
			if (busy) {
				synchronized(idleLock) {
					idleParallelExecutors ++;
				}
				busy = false;
			}
		}
		
		public synchronized boolean isBusy() {
			return busy;
		}
	}

	public ThreadPoolExecutor getThreadPool() {
		return threadPool;
	}
	
	public void setThreadPool(ThreadPoolExecutor threadPool) {
		this.threadPool = threadPool;
	}

	public List<T> gettList() {
		return tList;
	}

	public void settList(List<T> tList) {
		this.tList = tList;
	}
}
