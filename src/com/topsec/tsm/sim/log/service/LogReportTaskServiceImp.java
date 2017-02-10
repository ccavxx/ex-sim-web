package com.topsec.tsm.sim.log.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.topsec.tal.base.hibernate.ReportTask;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.auth.manage.AuthAccount;
import com.topsec.tsm.auth.manage.AuthUserDevice;
import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.auth.dao.UserDao;
import com.topsec.tsm.sim.log.dao.LogReportTaskDao;
import com.topsec.tsm.sim.node.dao.NodeMgrDao;
import com.topsec.tsm.sim.resource.exception.ResourceNameExistException;
import com.topsec.tsm.sim.util.NodeUtil;
import com.topsec.tsm.sim.util.RouteUtils;

public class LogReportTaskServiceImp implements LogReportTaskService {
	private LogReportTaskDao logReportTaskDao;
	private NodeMgrDao nodeMgrDao ;
	private UserDao userDao ;
	private DataSourceService dataSourceService;
	public void setDataSourceService(DataSourceService dataSourceService) {
		this.dataSourceService = dataSourceService;
	}
	public LogReportTaskDao getLogReportTaskDao() {
		return logReportTaskDao;
	}
	public void setLogReportTaskDao(LogReportTaskDao logReportTaskDao) {
		this.logReportTaskDao = logReportTaskDao;
	}
	
	public void setNodeMgrDao(NodeMgrDao nodeMgrDao) {
		this.nodeMgrDao = nodeMgrDao;
	}
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	/**
	 * 保存任务
	 * @param task
	 */
	@Override
	public void saveTask(ReportTask task)throws Exception {
		if(isTaskNameExist(task.getTaskName())){
			throw new ResourceNameExistException("任务名称已经存在！") ;
		}
		task.setTaskState(ReportTask.TASK_STATE_WAIT) ;
		logReportTaskDao.addTask(task) ;
	}
	/**
	 * 判断任务名称是否已经存在
	 * @param taskName
	 * @return
	 */
	@Override
	public boolean isTaskNameExist(String taskName) {
		ReportTask task = logReportTaskDao.getTaskByName(taskName) ;
		if(task!=null){
			return true ;
		}
		else{
			return false ;
		}
	}
	/**
	 * 更新任务
	 * @param task
	 * @return 是否更新成功
	 */
	@Override
	public boolean updateTask(ReportTask task) throws Exception {
		ReportTask dbNameTask = logReportTaskDao.getTaskByName(task.getTaskName()) ;
		if(dbNameTask!=null && !dbNameTask.equals(task)){//任务名称已存在
			throw new ResourceNameExistException(task.getTaskName());
		}
		ReportTask dbTask = logReportTaskDao.getTransient(task.getId()) ;
		task.setStartTime(dbTask.getStartTime());
		task.setEndTime(dbTask.getEndTime());
		task.setProgress(dbTask.getProgress());
		task.setTaskState(dbTask.getTaskState());
		task.setCreateDate(dbTask.getCreateDate());
		boolean isSuccesss = logReportTaskDao.updateTask(task)>0 ;
		
		return isSuccesss;
	}
	/**
	 * 手动执行任务
	 * @param task
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean executeSubject(ReportTask task){
		task.setStartTime(new Date()) ;
		task.setTaskState(ReportTask.TASK_STATE_RUNNING) ;
		boolean isSuccesss = logReportTaskDao.updateTask(task)>0 ;
		task.getBrowseObject().setId(String.valueOf(task.getId()));
		AuthAccount account = userDao.getByName(task.getCreater()) ;
		if(account != null && !"operator".equalsIgnoreCase(task.getCreater()) && ObjectUtils.isNotEmpty(account.getUserDevice())){
			List<String> userDevice = new ArrayList<String>() ;
			for(AuthUserDevice device:(Set<AuthUserDevice>)account.getUserDevice()){
				userDevice.add(device.getIp()+","+device.getDeviceType()) ;
			}
			task.getBrowseObject().setUserDevice(userDevice) ;
		}
		//Node node = nodeMgrDao.getNodeByNodeId(task.getBrowseObject().getNodeId(),false,true,false,false);
		String[] routes = RouteUtils.getQueryServiceRoutes() ;
		try {
			NodeUtil.dispatchCommand(routes, MessageDefinition.CMD_SUBJECT_STAT_REEXECUTE,  task.getBrowseObject(), 1000*10) ;
		} catch (CommunicationException e) {
			throw new RuntimeException("下发任务超时，请检查Service节点是否掉线") ;
		}
		return isSuccesss;
	}
	public void refreshTask(ReportTask task){
		logReportTaskDao.updateTask(task) ;
	}
	
	/**
	 * 根据搜索条件，检索任务
	 * @param condition
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	@Override
	public PageBean<ReportTask> search(Map<String,Object> condition,int pageIndex,int pageSize){
		return logReportTaskDao.search(pageIndex, pageSize,condition) ;
	}
	/**
	 * 根据任务id获得任务
	 * @param taskId
	 * @return
	 */
	@Override
	public ReportTask getTask(Integer taskId) {
		return logReportTaskDao.getTask(taskId);
	}
	
	@Override
	public ReportTask getTaskWithoutResult(Integer taskId) {
		return logReportTaskDao.getTaskWithoutResult(taskId);
	}
	/**
	 * 删除成功
	 * @param taskId
	 * @return　被删除的任务
	 */
	public ReportTask deleteTask(int taskId){
		ReportTask task = logReportTaskDao.getTask(taskId) ;
		logReportTaskDao.deleteTask(taskId) ;
		try {
			String[] routes = RouteUtils.getQueryServiceRoutes() ;
			NodeUtil.dispatchCommand(routes, MessageDefinition.CMD_SUBJECT_STAT_CANCEL,  taskId, 1000*10) ;
		} catch (Exception e) {
			e.printStackTrace() ;
		}
		return task ;
	}
	@Override
	public List<ReportTask> getUncompleteTask() {
		return logReportTaskDao.getUnCompleteTask();
	}
	@Override
	public List<ReportTask> getAllTask(Map<String,Object> condition) {
		return logReportTaskDao.findByCriteria(condition);
	}
	//取消正在执行的任务
	public ReportTask cancelTask(int taskId){
		ReportTask task = logReportTaskDao.getTask(taskId) ;
		task.setProgress(0.0);
		task.setTaskState(ReportTask.TASK_STATE_CANCEL);
		task.setEndTime(new Date()) ;
		logReportTaskDao.updateTask(task) ;
		try {
			String[] routes = RouteUtils.getQueryServiceRoutes() ;
			NodeUtil.dispatchCommand(routes, MessageDefinition.CMD_SUBJECT_STAT_CANCEL,  taskId, 1000*10) ;
		} catch (Exception e) {
			e.printStackTrace() ;
		}
		return task;
	}
	@Override
	public ReportTask getByName(String name) {
		return logReportTaskDao.getByName(name);
	}
	
}
