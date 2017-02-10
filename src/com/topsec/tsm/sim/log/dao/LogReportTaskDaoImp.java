package com.topsec.tsm.sim.log.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.topsec.tal.base.hibernate.ReportTask;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.common.dao.HibernateDaoImpl;
public class LogReportTaskDaoImp extends HibernateDaoImpl<ReportTask, Integer> implements LogReportTaskDao{
	@Override
	public void addTask(ReportTask task) {
		Session session = super.getSessionFactory().getCurrentSession() ;
		session.save(task);
	}
	@Override
	public ReportTask getTaskByName(String taskName) {
		Session session = super.getSessionFactory().getCurrentSession() ;
		Criteria cri = session.createCriteria(ReportTask.class) ;
		cri.add(Restrictions.eq("taskName", taskName)) ;
		cri.setMaxResults(1) ;
		ReportTask task = (ReportTask) cri.uniqueResult() ;
		session.evict(task) ;
		return task;
	}
	
	@Override
	protected Criterion[] getSearchCriterions( Map<String, Object> searchCondition) {
		List<Criterion> criterionList = new ArrayList<Criterion>(4);
		if(searchCondition.containsKey("taskState")){
			criterionList.add(Restrictions.eq("taskState", searchCondition.get("taskState"))) ;
		}
		if(searchCondition.containsKey("taskName")){
			criterionList.add(Restrictions.eq("taskName", searchCondition.get("taskName"))) ;
		}
		String creator = (String)searchCondition.get("creater") ;
		if(StringUtil.isNotBlank(creator)){
			criterionList.add(Restrictions.eq("creater",creator)) ;
		}
		String role = (String) searchCondition.get("role") ;
		if(StringUtil.isNotBlank(role)){
			criterionList.add(Restrictions.eq("role",role));
		}
		return criterionList.toArray(new Criterion[0]) ;
	}
	@Override
	public int updateTask(ReportTask task) {
		Session session = super.getSessionFactory().getCurrentSession() ;
		session.update(task) ;
		return 1 ;
	}
	@Override
	public ReportTask getTask(Integer taskId){
		Session session = super.getSessionFactory().getCurrentSession() ;
		ReportTask task = (ReportTask) session.get(ReportTask.class, taskId) ;
		if(task!=null&&!Hibernate.isInitialized(task.getJsonResult())){
			Hibernate.initialize(task.getJsonResult()) ;
		}
		return task;
	}
	@Override
	public int deleteTask(int taskId) {
		Session session = super.getSessionFactory().getCurrentSession() ;
		return session.createQuery("delete from ReportTask rt where rt.id=:id").setInteger("id", taskId).executeUpdate();
	}
	@Override
	public List<ReportTask> getUnCompleteTask() {
		return findByCriteria(Restrictions.in("taskState", new Object[]{ReportTask.TASK_STATE_RUNNING}));
	}
	@Override
	public ReportTask getTaskWithoutResult(Integer taskId) {
		return findById(taskId);
	}
	@Override
	public ReportTask getByName(String name) {
		return findUniqueByCriteria(Restrictions.eq("taskName", name));
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<ReportTask> findByCriteria(Map<String, Object> condition) {
		List<Criterion> conditions = new ArrayList<Criterion>() ;
		String creator = (String)condition.get("creater") ;
		String role = (String)condition.get("role");
		if(StringUtil.isNotBlank(creator)){
			conditions.add(Restrictions.eq("creater", creator)) ;
		}
		if(StringUtil.isNotBlank(role)){
			conditions.add(Restrictions.eq("role",role));
		}
		Criteria cri = createCriteria(conditions.toArray(new Criterion[0])) ;
		cri.addOrder(Order.desc("createDate")) ;
		return cri.list();
	}
	
}
