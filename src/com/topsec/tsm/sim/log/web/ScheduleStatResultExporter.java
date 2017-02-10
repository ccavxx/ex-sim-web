package com.topsec.tsm.sim.log.web;

import java.io.OutputStream;

import com.topsec.tal.base.hibernate.ScheduleStatTask;

/**
 * 导出报表结果
 * @author hp
 *
 */
public interface ScheduleStatResultExporter {
	
	public void setTask(ScheduleStatTask task) ;
	
	/**
	 * 将报表结果导出到指定输出流
	 * @param task
	 * @param os
	 */
	public void exportTo(OutputStream os) ;
}
