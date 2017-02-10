package com.topsec.tsm.sim.report.service;

import java.util.List;
import java.util.Map;

import org.antlr.grammar.v3.ANTLRParser.finallyClause_return;

import com.topsec.tsm.sim.report.dao.ReportDao;
import com.topsec.tsm.sim.report.persistence.RptMaster;
import com.topsec.tsm.sim.report.persistence.RptRule;
import com.topsec.tsm.sim.report.persistence.RptSub;
import com.topsec.tsm.sim.response.persistence.Response;
import com.topsec.tsm.sim.response.persistence.ResponseResult;

public class ReportServiceImpl implements ReportService {
	
	private ReportDao reportDao;
	
	// 根据设备类型获得子报表
	public List<RptSub> getSubRepByDeviceType(String deviceTypeId){
		return this.getReportDao().getSubRepByDeviceType(deviceTypeId);
	}

	public Integer addMyReport(RptMaster masterRep){
		return this.getReportDao().addMyReport(masterRep);
	}

	public List<RptMaster> getAllMyReports(){
		return this.getReportDao().getAllMyReports();
	}
	@Override
	public List<RptMaster> showAllMyReportsByUser(String createUser){
		return this.getReportDao().findAllMyReportsByUser(createUser);
	}

	public RptMaster removeMyReport(Integer reportId){
		return this.getReportDao().removeMyReport(reportId);
	}

	public List<RptRule> getAllRules(Integer childRepId){
		return this.getReportDao().getAllRules(childRepId);
	}
	
	public RptMaster getMyReportById(Integer id){
		return this.getReportDao().getMyReportById(id);
	}

	public void updateMyReport(RptMaster rpt){
		this.getReportDao().updateMyReport(rpt);
	}

	
	@Override
	public List<Map> getSuperiorId(String mstId) {
		return this.getReportDao().getSuperiorId(mstId);
	}
	
	public List<Map> getRptMaster(String dvcType){
		List<Map> list = reportDao.getRptMaster(dvcType);
		if((list==null||list.size()==0)&&dvcType!=null){
			list = reportDao.getRptMaster(dvcType.substring(0,dvcType.indexOf("/")));
		}
		return list;
	}
	
	@Override
	public Response showPlanTaskById(String respId) {
		
		return this.getReportDao().findPlanTaskById(respId);
	}

	@Override
	public List<Response> showAllResponsesByCreater(String creater,String scheduleType) {
		return this.getReportDao().findAllResponsesByCreater(creater,scheduleType);
	}

	@Override
	public List<Response> showAllResponses(String scheduleType) {
		return this.getReportDao().findAllResponses(scheduleType);
	}

	@Override
	public Integer showPlanResultSuccessCountByRespId(String respId) {
		return getReportDao().findPlanResultSuccessCountByRespId(respId);
	}

	@Override
	public Integer showPlanResultFailedCountByRespId(String respId) {
		return getReportDao().findPlanResultFailedCountByRespId(respId);
	}

	@Override
	public void delPlanResults(List<ResponseResult> responseResults) {
		getReportDao().delPlanResults(responseResults);
	}

	@Override
	public List<Response> showPlanByTypeAndExeTimeType(String type,
			String exeTimeType, int page, int rows) {
		return getReportDao().findPlanByTypeAndExeTimeType(type, exeTimeType, page, rows);
	}
	
	@Override
	public List<Response> showPlanByTypeAndExeTimeTypeAndUser(String type,
			String exeTimeType,String userName, int page, int rows) {
		return getReportDao().findPlanByTypeAndExeTimeTypeAndUser(type, exeTimeType, userName, page, rows);
	}
	
	@Override
	public List<Response> showPlanByTypeAndUser(String type,
			String userName, int page, int rows) {
		return getReportDao().findPlanByTypeAndUser(type, userName, page, rows);
	}

	@Override
	public Integer showCountPlanByTypeAndExeTimeType(final String type,	final String exeTimeType) {
		return getReportDao().findCountPlanByTypeAndExeTimeType(type, exeTimeType);
	}
	
	@Override
	public Integer showCountPlanByTypeAndExeTimeTypeAndUser(final String type,	final String exeTimeType,final String userName) {
		return getReportDao().findCountPlanByTypeAndExeTimeTypeAndUser(type, exeTimeType, userName);
	}
	
	@Override
	public Integer showCountPlanByTypeAndUser(final String type,final String userName) {
		return getReportDao().findCountPlanByTypeAndUser(type, userName);
	}
	
	public ReportDao getReportDao() {
		return reportDao;
	}

	public void setReportDao(ReportDao reportDao) {
		this.reportDao = reportDao;
	}

	@Override
	public List<Map> getLogCount(String dvcAddress) {
		return getReportDao().getLogCount(dvcAddress);
	}

	@Override
	public List<String> findAllTypeList() {
		return getReportDao().findAllTypeList();
	}

	@Override
	public List<Map<String,Object>> getChildSubject(Integer masterId) {
		return reportDao.getChildSubject(masterId);
	}
}
