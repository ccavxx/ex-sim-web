package com.topsec.tsm.sim.log.service;

import java.util.List;
import java.util.Map;

import com.topsec.tal.base.hibernate.ScheduleStatTask;
import com.topsec.tal.base.search.LogRecordSet;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.node.component.service.stat.LogStatisticResult;
import com.topsec.tsm.sim.resource.exception.ResourceNameExistException;



public interface ScheduleStatTaskService {
	/**
	 * 保存任务
	 * @param task
	 */
	public void saveStatTask(ScheduleStatTask task)throws Exception;
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
	public void updateStatTask(ScheduleStatTask task)throws ResourceNameExistException;
	
	/**
	 * 根据搜索条件，检索任务
	 * @param condition
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	public PageBean<ScheduleStatTask> searchStatTaskList(int pageIndex,int pageSize,Map<String,Object> condition);
	
	/**
	 * 根据任务id获得任务
	 * @param taskId
	 * @return
	 */
	public ScheduleStatTask getStatTask(Integer taskId,boolean loadSubjects,boolean loadResult);
	/**
	 * 删除成功
	 * @param taskId
	 * @return　被删除的任务
	 */
	public ScheduleStatTask deleteStatTask(int taskId);
	/**
	 * 根据任务名称查询任务
	 * @param name
	 * @return
	 */
	public ScheduleStatTask getStatTaskByName(String name) ;
	
	/**
	 * 更新subject进度信息
	 * @param subjectId
	 * @param progress
	 */
	public void updateSubjectProgress(Integer taskId,Integer subjectId,int progress) ;
	/**
	 * 更新主题的进度
	 * @param taskId
	 * @param progress
	 */
	public void updateSubjectProgress(Integer taskId,Map<Integer,Integer> subjectProgress) ;
	/**
	 * 存储subject的统计结果
	 * @param taskId
	 * @param subjectId
	 * @param subjectResult
	 */
	public void saveSubjectResult(Integer taskId,Integer subjectId,LogStatisticResult result) ;
	/**
	 * 存储subject的统计结果
	 * @param taskId
	 * @param subjectId
	 * @param subjectResult
	 */
	public void saveSubjectResult(Integer taskId,LogStatisticResult result) ;
	/**
	 * 返回启用状态的任务
	 * @return
	 */
	public List<ScheduleStatTask> getEnabled() ;
	/**
	 * 获取启用状态的任务数
	 * @return
	 */
	public int getEnabledCount() ;
	/**
	 * 周期性任务开始执行
	 */
	public void onExecute(Integer taskId) ;
	/**
	 * 执行完成
	 */
	public void done(Integer taskId);
}
