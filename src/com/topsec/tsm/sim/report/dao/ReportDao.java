package com.topsec.tsm.sim.report.dao;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.sim.report.persistence.RptMaster;
import com.topsec.tsm.sim.report.persistence.RptRule;
import com.topsec.tsm.sim.report.persistence.RptSub;
import com.topsec.tsm.sim.response.persistence.Response;
import com.topsec.tsm.sim.response.persistence.ResponseResult;

public interface ReportDao {
	
	// 根据设备类型获得子报表
	public List<RptSub> getSubRepByDeviceType(String deviceTypeId);
 
	public Integer addMyReport(RptMaster masterRep);

	public List<RptMaster> getAllMyReports();

	public RptMaster removeMyReport(Integer reportId);

	public List<RptRule> getAllRules(Integer childRepId);

	public RptMaster getMyReportById(Integer id);

	public void updateMyReport(RptMaster rpt);

	public List<Map> getSuperiorId(String mstId);
	
	public List<Map> getRptMaster(String dvcType);
	
	public Response findPlanTaskById(String respId);
	
	public List<Response> findAllResponsesByCreater(String creater,String scheduleType);
	
	public List<Response> findAllResponses(String scheduleType);
	
	public Integer findPlanResultSuccessCountByRespId(String respId);
	
	public Integer findPlanResultFailedCountByRespId(String respId);
	
	public void delPlanResults(List<ResponseResult> responseResults);
	
	public List<Response> findPlanByTypeAndExeTimeType(final String type, final String exeTimeType,final int page, final int rows);
	
	public Integer findCountPlanByTypeAndExeTimeType(final String type,	final String exeTimeType);
	
	public List<Map> getLogCount(String dvcAddress);
	
	public List<Response> findPlanByTypeAndExeTimeTypeAndUser(final String type,
			final String exeTimeType,final String userName, final int page, final int rows);
	public Integer findCountPlanByTypeAndExeTimeTypeAndUser(final String type,	final String exeTimeType,final String userName);
	
	public Integer findCountPlanByTypeAndUser(final String type,final String userName);
	
	public List<Response> findPlanByTypeAndUser(final String type, final String userName, final int page, final int rows);
	public List<RptMaster> findAllMyReportsByUser(String createUser);
	public List<String> findAllTypeList();

	public List<Map<String,Object>> getChildSubject(Integer masterId);
}