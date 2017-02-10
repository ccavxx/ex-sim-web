package com.topsec.tsm.sim.report.service;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.sim.report.persistence.RptMaster;
import com.topsec.tsm.sim.report.persistence.RptRule;
import com.topsec.tsm.sim.report.persistence.RptSub;
import com.topsec.tsm.sim.response.persistence.Response;
import com.topsec.tsm.sim.response.persistence.ResponseResult;

public interface ReportService {
	
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
	
	public Response showPlanTaskById(String respId);
	
	public List<Response> showAllResponsesByCreater(String creater,String scheduleType);
	
	public List<Response> showAllResponses(String scheduleType);
	
	public Integer showPlanResultSuccessCountByRespId(String respId);
	
	public Integer showPlanResultFailedCountByRespId(String respId);
	
	public void delPlanResults(List<ResponseResult> responseResults);
	
	public List<Response> showPlanByTypeAndExeTimeType(final String type, final String exeTimeType,final int page, final int rows);
	
	public Integer showCountPlanByTypeAndExeTimeType(final String type,	final String exeTimeType);
	
	public List<Map> getLogCount(String dvcAddress);
	
	public List<Response> showPlanByTypeAndExeTimeTypeAndUser(String type,
			String exeTimeType,String userName, int page, int rows);
	
	public Integer showCountPlanByTypeAndExeTimeTypeAndUser(final String type,	final String exeTimeType,final String userName);
	
	public Integer showCountPlanByTypeAndUser(final String type,final String userName);
	
	public List<Response> showPlanByTypeAndUser(String type, String userName, int page, int rows);
	
	public List<RptMaster> showAllMyReportsByUser(String createUser);
	
	public List<String> findAllTypeList();
	
	public List<Map<String,Object>> getChildSubject(Integer masterId) ;
}
