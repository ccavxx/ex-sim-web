package com.topsec.tsm.sim.log.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.topsec.tal.base.hibernate.ScheduleStatTask;
import com.topsec.tal.base.hibernate.StatSubject;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.node.component.service.stat.LogStatisticResult;
import com.topsec.tsm.sim.log.dao.ScheduleStatTaskDao;
import com.topsec.tsm.sim.resource.exception.ResourceNameExistException;

public class ScheduleStatTaskServiceImp implements ScheduleStatTaskService {
	
	private ScheduleStatTaskDao scheduleStatTaskDao;
	
	public ScheduleStatTaskDao getScheduleStatTaskDao() {
		return scheduleStatTaskDao;
	}
	public void setScheduleStatTaskDao(ScheduleStatTaskDao scheduleStatTaskDao) {
		this.scheduleStatTaskDao = scheduleStatTaskDao;
	}
	/**
	 * 判断任务名称是否已经存在
	 * @param taskName
	 * @return
	 */
	@Override
	public boolean isTaskNameExist(String taskName) {
		ScheduleStatTask task = scheduleStatTaskDao.getByName(taskName) ;
		if(task!=null){
			return true ;
		}
		else{
			return false ;
		}
	}
	/**
	 * 保存任务
	 * @param task
	 */
	@Override
	public void saveStatTask(ScheduleStatTask task)throws Exception {
		if(isTaskNameExist(task.getName())){
			throw new ResourceNameExistException("任务名称已经存在！") ;
		}
		scheduleStatTaskDao.save(task) ;
	}
	
	
	/**
	 * 更新任务
	 * @param task
	 * @return 是否更新成功
	 * @throws ResourceNameExistException 
	 */
	@Override
	public void updateStatTask(ScheduleStatTask task) throws ResourceNameExistException {
		scheduleStatTaskDao.update(task);
	}
	
	/**
	 * 根据搜索条件，检索任务
	 * @param condition
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	@Override
	public PageBean<ScheduleStatTask> searchStatTaskList(int pageIndex,int pageSize,Map<String,Object> condition){
		return scheduleStatTaskDao.search(pageIndex, pageSize,condition) ;
	}
	/**
	 * 根据任务id获得任务
	 * @param taskId
	 * @return
	 */
	@Override
	public ScheduleStatTask getStatTask(Integer taskId,boolean loadSubjects,boolean loadResult) {
		return scheduleStatTaskDao.get(taskId,loadSubjects, loadResult);
	}
	
	/**
	 * 删除成功
	 * @param taskId
	 * @return　被删除的任务
	 */
	public ScheduleStatTask deleteStatTask(int taskId){
		ScheduleStatTask task = scheduleStatTaskDao.findById(taskId) ;
		scheduleStatTaskDao.delete(taskId) ;
		return task ;
	}
	@Override
	public ScheduleStatTask getStatTaskByName(String name) {
		ScheduleStatTask statTask = scheduleStatTaskDao.getByName(name);
		return statTask;
	}
	@Override
	public void updateSubjectProgress(Integer taskId, Integer subjectId, int progress) {
		ScheduleStatTask task = getStatTask(taskId,true,false) ;
		if(task != null && task.getSubjects() != null){
			for(StatSubject subject:task.getSubjects()){
				if(subject.getSubjectId().equals(subjectId)){
					subject.setProgress(progress) ;
					break ;
				}
			}
			scheduleStatTaskDao.update(task) ;
		}
	}
	
	@Override
	public void updateSubjectProgress(Integer taskId, Map<Integer, Integer> subjectProgress) {
		ScheduleStatTask task = getStatTask(taskId,true,false) ;
		if(task != null && task.getSubjects() != null){
			for(StatSubject subject:task.getSubjects()){
				Integer progress = subjectProgress.get(subject.getSubjectId()) ;
				if(progress != null){
					subject.setProgress(progress) ;
				}
			}
			scheduleStatTaskDao.update(task) ;
		}
	}
	@Override
	public void saveSubjectResult(Integer taskId, Integer subjectId, LogStatisticResult result) {
		ScheduleStatTask task = getStatTask(taskId,true,false) ;
		if(task != null && task.getSubjects() != null){
			JSONArray jsonArray = (JSONArray) JSONArray.toJSON(result.getResults()) ;
			for(StatSubject subject:task.getSubjects()){
				if(subject.getSubjectId().equals(subjectId)){
					subject.setStatResult(jsonArray.toJSONString()) ;
					subject.setProgress(100) ;
					subject.setStartTime(result.getStart()) ;
					subject.setEndTime(result.getEnd()) ;
					break ;
				}
			}
			scheduleStatTaskDao.update(task) ;
		}
	}
	
	@Override
	public void saveSubjectResult(Integer taskId, LogStatisticResult result) {
		ScheduleStatTask task = getStatTask(taskId,true,false) ;
		if(task != null && task.getSubjects() != null){
			for(StatSubject subject:task.getSubjects()){
				List<?> subjectStatResult = result.getResults().get(subject.getSubjectId()) ;
				if(subjectStatResult != null){
					JSONArray jsonArray = (JSONArray) JSONArray.toJSON(subjectStatResult) ;
					subject.setStatResult(jsonArray.toJSONString()) ;
					subject.setProgress(100) ;
					subject.setStartTime(result.getStart()) ;
					subject.setEndTime(result.getEnd()) ;
				}
			}
			scheduleStatTaskDao.update(task) ;
		}
	}
	@Override
	public List<ScheduleStatTask> getEnabled() {
		return scheduleStatTaskDao.getEnabled();
	}
	@Override
	public int getEnabledCount() {
		return scheduleStatTaskDao.getEnabledCount();
	}
	@Override
	public void onExecute(Integer taskId) {
		ScheduleStatTask task = getStatTask(taskId,true,false) ;
		if(task != null && task.getSubjects() != null){
			task.setStatus(ScheduleStatTask.RUNNING) ;
			task.setBeginTime(new Date()) ;
			task.setEndTime(null) ;
			task.setExecuteCount(ObjectUtils.nvl(task.getExecuteCount(), 0) + 1) ;
			for(StatSubject subject:task.getSubjects()){
				subject.setProgress(0) ;
				subject.setStatResult(null) ;
				subject.setStartTime(null) ;
				subject.setEndTime(null) ;
			}
			scheduleStatTaskDao.update(task) ;
		}
	}
	@Override
	public void done(Integer taskId) {
		ScheduleStatTask task = scheduleStatTaskDao.get(taskId, true, false) ;
		if (task != null) {
			task.setStatus(ScheduleStatTask.DONE) ;
			task.setEndTime(new Date()) ;
			for(StatSubject subject:task.getSubjects()){
				subject.setProgress(100) ;
			}
		}
	}
	
}
