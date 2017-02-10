package com.topsec.tsm.sim.log.service;

import java.util.List;
import java.util.Map;

import com.topsec.tal.base.hibernate.ReportTask;
import com.topsec.tsm.ass.PageBean;



public interface LogReportTaskService {
	/**
	 * 保存任务
	 * @param task
	 */
	public void saveTask(ReportTask task)throws Exception;
	/**
	 * 判断任务名称是否已经存在
	 * @param taskName
	 * @return
	 */
	public boolean isTaskNameExist(String taskName);
	/**
	 * 更新任务
	 * @param task
	 * @return 是否更新成功
	 */
	public boolean updateTask(ReportTask task)throws Exception;
	
	public void refreshTask(ReportTask task) ;
	
	/**
	 * 根据搜索条件，检索任务
	 * @param condition
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	public PageBean<ReportTask> search(Map<String,Object> condition,int pageIndex,int pageSize);
	/**
	 * 根据任务id获得任务
	 * @param taskId
	 * @return
	 */
	public ReportTask getTask(Integer taskId);
	/**
	 * 根据任务id获取任务对象，不包含任务结果
	 * @param taskId
	 * @return
	 */
	public ReportTask getTaskWithoutResult(Integer taskId) ;
	/**
	 * 删除成功
	 * @param taskId
	 * @return　被删除的任务
	 */
	public ReportTask deleteTask(int taskId);
	/**
	 * 获取所有没有执行的或者正在执行未完成的任务
	 * @return
	 */
	public List<ReportTask> getUncompleteTask();
	
	/**
	 * 返回所有的日志统计内容
	 * @return
	 */
	public List<ReportTask> getAllTask(Map<String,Object> condition);
	/**
	 * 根据任务ID取消正在执行的任务
	 * @param taskId
	 */
	public ReportTask cancelTask(int taskId);
	/**
	 * 根据任务名称查询任务
	 * @param name
	 * @return
	 */
	public ReportTask getByName(String name) ;
	/**
	 * 手动执行主题
	 * @param task
	 * @return
	 */
	public boolean executeSubject(ReportTask task);
}
