package com.topsec.tsm.sim.log.dao;

import java.util.List;
import java.util.Map;

import com.topsec.tal.base.hibernate.ReportTask;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.sim.common.dao.BaseDao;


public interface LogReportTaskDao extends BaseDao<ReportTask, Integer>{
	/**
	 * 增加报表任务
	 * @param task
	 */
	public void addTask(ReportTask task) ;
	/**
	 * 根据任务名称获取任务
	 * @param taskName
	 * @return
	 */
	public ReportTask getTaskByName(String taskName) ;
	/**
	 * 更新任务
	 * @param task
	 * @return
	 */
	public int updateTask(ReportTask task) ;
	/**
	 * 根据任务id获得任务
	 * @param taskId
	 * @return
	 */
	public ReportTask getTask(Integer taskId);
	/**
	 * 删除报表任务
	 * @param taskId
	 * @return
	 */
	public int deleteTask(int taskId);
	/**
	 * 获取所有没有执行完成的任务(未执行或者正在执行中)
	 * @return
	 */
	public List<ReportTask> getUnCompleteTask();
	/**
	 * 根据任务id，获取不包含执行结果任务对象
	 * @param taskId
	 * @return
	 */
	public ReportTask getTaskWithoutResult(Integer taskId);
	
	public List<ReportTask> findByCriteria(Map<String, Object> condition);
	/**
	 * 根据任务名称查询任务
	 * @param name
	 * @return
	 */
	public ReportTask getByName(String name);
}
