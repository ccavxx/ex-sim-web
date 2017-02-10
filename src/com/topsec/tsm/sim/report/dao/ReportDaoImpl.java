package com.topsec.tsm.sim.report.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.topsec.tsm.sim.report.persistence.RptMaster;
import com.topsec.tsm.sim.report.persistence.RptMstSub;
import com.topsec.tsm.sim.report.persistence.RptPolicyRule;
import com.topsec.tsm.sim.report.persistence.RptRule;
import com.topsec.tsm.sim.report.persistence.RptRuleValue;
import com.topsec.tsm.sim.report.persistence.RptSub;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.response.persistence.Response;
import com.topsec.tsm.sim.response.persistence.ResponseResult;
@SuppressWarnings("unchecked")
public class ReportDaoImpl extends HibernateDaoSupport implements ReportDao {
	public List<RptSub> getSubRepByDeviceType(String deviceType) {
		return getHibernateTemplate().find("from RptSub where subSubject = ? and subVisible != '0' ", deviceType);
	}
	public Integer addMyReport(RptMaster masterRep) {
		Integer id = (Integer)getHibernateTemplate().save(masterRep);
		Integer mstId = masterRep.getId();
		Integer mstSubId = 0;
		List<RptMstSub> repMstSubs = masterRep.getRepMstSubs();
		List<RptRuleValue> rptRuleValues = null;
		for (RptMstSub repMstSub : repMstSubs) {
			repMstSub.setMstId(mstId);
			getHibernateTemplate().save(repMstSub);
			mstSubId = repMstSub.getId();
			rptRuleValues = repMstSub.getRptRuleValues();
			for (RptRuleValue rptRuleValue : rptRuleValues) {
				rptRuleValue.setMstSubId(mstSubId);
				getHibernateTemplate().save(rptRuleValue);
			}
		}
		return id ;
	}

	public void updateMyReport(RptMaster masterRep) {
		getHibernateTemplate().update(masterRep);
		String hql2 = "from RptMstSub where mstId = ?";
		List<RptMstSub> rptMstSubList = getHibernateTemplate().find(hql2, masterRep.getId());

		String hql4 = "from RptRuleValue where mstSubId = ?";
		List<RptRuleValue> rptRuleValues = null;

		getHibernateTemplate().deleteAll(rptMstSubList);
		for (int i = 0; i < rptMstSubList.size(); i++) {
			rptRuleValues = getHibernateTemplate().find(hql4, rptMstSubList.get(i).getId());
			getHibernateTemplate().deleteAll(rptRuleValues);
		}

		Integer mstId = masterRep.getId();
		Integer mstSubId = 0;
		List<RptMstSub> repMstSubs = masterRep.getRepMstSubs();
		for (RptMstSub repMstSub : repMstSubs) {
			repMstSub.setMstId(mstId);
			getHibernateTemplate().save(repMstSub);
			mstSubId = repMstSub.getId();
			rptRuleValues = repMstSub.getRptRuleValues();
			for (RptRuleValue rptRuleValue : rptRuleValues) {
				rptRuleValue.setMstSubId(mstSubId);
				getHibernateTemplate().save(rptRuleValue);
			}
		}

	}

	public List<RptMaster> getAllMyReports() {
		return getHibernateTemplate().find("from RptMaster as rptM where rptM.mstType = 2 and rptM.delflg != 1 ORDER BY rptM.creTime DESC");
	}
	@Override
	public List<RptMaster> findAllMyReportsByUser(String createUser){
		return getHibernateTemplate().find("from RptMaster as rptM where rptM.creUser=? and rptM.mstType = 2 and rptM.delflg != 1 ORDER BY rptM.creTime DESC",createUser);
	}

	public RptMaster removeMyReport(Integer reportId) {
		List<RptMaster> temp = getHibernateTemplate().find("from RptMaster where id = ?", reportId);
		if (temp != null && temp.size() > 0) {
			RptMaster masterRep = temp.get(0);
			masterRep.setDelflg(1);
			getHibernateTemplate().update(masterRep);
			return masterRep ;
		}
		return null ;
	}

	public List<RptRule> getAllRules(Integer childRepId) {
		return getHibernateTemplate()
				.find("select rule from RptRule as rule,RptSub as sub,RptPolicyRule as rptPoRule where sub.id = ? and sub.subPolicyId = rptPoRule.subPolicyId and rptPoRule.ruleId = rule.id order by rptPoRule.id desc",
					  childRepId);
	}

	public RptMaster getMyReportById(Integer id) {
		RptMaster res = new RptMaster();
		String hql1 = "from RptMaster where id = ?";
		List<RptMaster> rptMstList = getHibernateTemplate().find(hql1, id);
		res = rptMstList.get(0);

		String hql2 = "from RptMstSub where mstId = ? order by subRow,subColumn";
		List<RptMstSub> rptMst_subList = getHibernateTemplate().find(hql2, id);

		String hql3 = "from RptSub where id = ?";
		List<RptSub> rptSubList = new ArrayList<RptSub>();
		RptSub rptSub = null;
		List<RptSub> rptSubListTemp = null;

		String hql5 = "from RptRule where id = ?";
		String hql6 = "from RptPolicyRule where subPolicyId = ?";
		List<RptPolicyRule> rptPolicyRuleList = null;
		List<RptRule> rptRuleListTemp = null;
		List<RptRule> rptRuleList = new ArrayList<RptRule>();

		String hql4 = "from RptRuleValue where mstSubId = ?";
		List<RptRuleValue> rptRuleValueList = null;
		for (int i = 0; i < rptMst_subList.size(); i++) {
			rptSubListTemp = getHibernateTemplate().find(hql3, rptMst_subList.get(i).getSubId());

			if (rptSubListTemp.size() > 0) {
				rptPolicyRuleList = getHibernateTemplate().find(hql6, rptSubListTemp.get(0).getSubPolicyId());
				for (int j = 0; j < rptPolicyRuleList.size(); j++) {
					rptRuleListTemp = getHibernateTemplate().find(hql5, rptPolicyRuleList.get(j).getRuleId());
					if (rptRuleListTemp.size() > 0)
						rptRuleList.add(rptRuleListTemp.get(0));
				}
				rptSubListTemp.get(0).setRptRules(rptRuleList);
				rptSubList.add(rptSubListTemp.get(0));
			}

			rptRuleValueList = getHibernateTemplate().find(hql4, rptMst_subList.get(i).getId());
			rptMst_subList.get(i).setRptRuleValues(rptRuleValueList);
		}
		res.setRepMstSubs(rptMst_subList);
		res.setRepSubs(rptSubList);
		return res;
	}

	@Override
	public List<Map> getSuperiorId(String mstId) {
		String hql = "SELECT new map(mst.id AS mstId) FROM RptMaster mst, RptSub sub , RptMstSub mst_sub WHERE sub.tableLink= ? AND mst.id = mst_sub.mstId AND sub.id = mst_sub.subId";
		List<Map> list = getHibernateTemplate().find(hql,mstId);
		
		return list;
	}
	
	public List<Map> getRptMaster(String dvcType){
		String hql = "select new map(mst.id as id ,mst.mstName as mstName,mst.viewItem as viewItem) from RptMaster mst where mst.subject =? and mst.mstDig is null order by mst.id";
		List list = getHibernateTemplate().find(hql,dvcType);
		return list;
	}
	
	@Override
	public Response findPlanTaskById(final  String respId) {
		Response planTask=getHibernateTemplate().get(Response.class, respId);
		String nodeId=getHibernateTemplate().execute(new HibernateCallback<String>() {

			@Override
			public String doInHibernate(Session session) throws HibernateException,
					SQLException {
				Query query = session.createSQLQuery("SELECT rp.NODEID FROM TAL_RESPONSE rp where rp.RESPONSE_ID =?");
				String tempidString=respId;
				query.setString(0, tempidString);
				List<String> reList=query.list();
				if (reList.size()>0) {
					return reList.get(0);
				}
				return null;

			}
		});
		if(null!=nodeId){
			Node node=new Node(nodeId);
			
			planTask.setNode(node);
		}
		
		return planTask;
	}
	
	@Override
	public List<Response> findAllResponsesByCreater(String creater,String scheduleType) {
		if(null==creater){
			return null;
		}
		if(null==scheduleType){
			return getHibernateTemplate().find("from Response resp where resp.creater=?",creater);
		}
		return getHibernateTemplate().find("from Response resp where resp.creater=? and resp.ScheduleType=?",creater,scheduleType);
	}
	
	@Override
	public List<Response> findAllResponses(String scheduleType) {
		if(null==scheduleType){return getHibernateTemplate().find("from Response");}
		return getHibernateTemplate().find("from Response resp where resp.ScheduleType=?",scheduleType);
	}
	
	@Override
	public Integer findPlanResultSuccessCountByRespId(String respId) {
		final String planId=respId;
		return getHibernateTemplate().execute(new HibernateCallback<Integer>(){

			@Override
			public Integer doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query = session.createSQLQuery("SELECT count(*) FROM TAL_RESPONSE_RESULT trr WHERE trr.RESPONSE_ID=? and trr.RESP_RESULT=?");
				query.setString(0, planId);
				query.setString(1, "true");
				List<?> list=query.list();
				Integer resu=list.size()>0?Integer.valueOf(list.get(0).toString()):0;
				return resu;
			}});
	}
	
	@Override
	public Integer findPlanResultFailedCountByRespId(String respId) {
		final String planId=respId;
		return getHibernateTemplate().execute(new HibernateCallback<Integer>(){

			@Override
			public Integer doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query = session.createSQLQuery("SELECT count(*) FROM TAL_RESPONSE_RESULT trr WHERE trr.RESPONSE_ID=? and trr.RESP_RESULT=?");
				query.setString(0, planId);
				query.setString(1, "false");
				List<?> list=query.list();
				Integer resu=list.size()>0?Integer.valueOf(list.get(0).toString()):0;
				return resu;
			}});
	}
	
	@Override
	public void delPlanResults(List<ResponseResult> responseResults) {
		getHibernateTemplate().deleteAll(responseResults);
	}
	
	@Override
	public List<Response> findPlanByTypeAndExeTimeType(final String type,
			final String exeTimeType, final int page, final int rows) {
		return getHibernateTemplate().execute(new HibernateCallback<List<Response>>() {
			@Override
			public List<Response> doInHibernate(Session session) throws HibernateException, SQLException {
				 Query query=session.createQuery(" FROM Response rp WHERE rp.Type=? and rp.ScheduleType=? ORDER BY rp.createTime DESC ");
				 query.setString(0, type);
				 query.setString(1, exeTimeType);
				 query.setFirstResult((page - 1) * rows);// 计算当前页的索引首项
				 query.setMaxResults(rows);
				 return query.list();
			}
		});
	}
	
	@Override
	public List<Response> findPlanByTypeAndExeTimeTypeAndUser(final String type,
			final String exeTimeType,final String userName, final int page, final int rows) {
		return getHibernateTemplate().execute(new HibernateCallback<List<Response>>() {
			@Override
			public List<Response> doInHibernate(Session session) throws HibernateException, SQLException {
				 Query query=session.createQuery(" FROM Response rp WHERE rp.Type=? and rp.ScheduleType=? and rp.creater=? ORDER BY rp.createTime DESC ");
				 query.setString(0, type);
				 query.setString(1, exeTimeType);
				 query.setString(2, userName);
				 query.setFirstResult((page - 1) * rows);// 计算当前页的索引首项
				 query.setMaxResults(rows);
				 return query.list();
			}
		});
	}
	
	@Override
	public List<Response> findPlanByTypeAndUser(final String type,
			final String userName, final int page, final int rows) {
		return getHibernateTemplate().execute(new HibernateCallback<List<Response>>() {
			@Override
			public List<Response> doInHibernate(Session session) throws HibernateException, SQLException {
				 Query query=session.createQuery(" FROM Response rp WHERE rp.Type=? and rp.creater=? ORDER BY rp.createTime DESC ");
				 query.setString(0, type);
				 query.setString(1, userName);
				 query.setFirstResult((page - 1) * rows);// 计算当前页的索引首项
				 query.setMaxResults(rows);
				 return query.list();
			}
		});
	}
	
	@Override
	public Integer findCountPlanByTypeAndExeTimeType(final String type,	final String exeTimeType) {
		return getHibernateTemplate().execute(new HibernateCallback<Integer>(){

			@Override
			public Integer doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query = session.createSQLQuery("SELECT count(*) FROM TAL_RESPONSE rp WHERE rp.RESP_TYPE=? and rp.SCDL_TYPE=?");
				query.setString(0, type);
				query.setString(1, exeTimeType);
				List<?> list=query.list();
				Integer resu=list.size()>0?Integer.valueOf(list.get(0).toString()):0;
				return resu;
			}});
	}
	
	@Override
	public Integer findCountPlanByTypeAndExeTimeTypeAndUser(final String type,	final String exeTimeType,final String userName) {
		return getHibernateTemplate().execute(new HibernateCallback<Integer>(){

			@Override
			public Integer doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query = session.createSQLQuery("SELECT count(*) FROM TAL_RESPONSE rp WHERE rp.RESP_TYPE=? and rp.SCDL_TYPE=? and rp.RESP_CREATER=?");
				query.setString(0, type);
				query.setString(1, exeTimeType);
				query.setString(2, userName);
				List<?> list=query.list();
				Integer resu=list.size()>0?Integer.valueOf(list.get(0).toString()):0;
				return resu;
			}});
	}
	
	@Override
	public Integer findCountPlanByTypeAndUser(final String type,final String userName) {
		return getHibernateTemplate().execute(new HibernateCallback<Integer>(){

			@Override
			public Integer doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query = session.createSQLQuery("SELECT count(*) FROM TAL_RESPONSE rp WHERE rp.RESP_TYPE=? and rp.RESP_CREATER=?");
				query.setString(0, type);
				query.setString(1, userName);
				List<?> list=query.list();
				Integer resu=list.size()>0?Integer.valueOf(list.get(0).toString()):0;
				return resu;
			}});
	}

	public List<Map> getLogCount(String dvcAddress){
		String hql = "select new map(type as type,sum(counts) as counts ) from LogFile where ip = ? and type not like 'Monitor%' group by type ";
		List list = getHibernateTemplate().find(hql, dvcAddress);
		return list;
	}
	@Override
	public List<String> findAllTypeList() {
		return getHibernateTemplate().find("select rptM.subject from RptMaster as rptM where rptM.mstType = 1 group by 1");
	}
	@Override
	public List<Map<String,Object>> getChildSubject(Integer masterId) {
		String hql = "select new map(sub.id as id,sub.subName as subName,sub.tableLable as tableLabel,sub.tableFiled as tableField) from RptMaster mst, RptSub sub , RptMstSub mst_sub where mst.id = ? and mst.id = mst_sub.mstId and sub.id = mst_sub.subId order by mst_sub.subRow,mst_sub.subColumn";
		return getHibernateTemplate().find(hql,masterId);
	}
	
}
