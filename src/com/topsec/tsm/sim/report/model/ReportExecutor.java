package com.topsec.tsm.sim.report.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;

import com.topsec.tal.base.report.service.RptMasterTbService;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.report.bean.struct.ExpDateStruct;
import com.topsec.tsm.sim.report.bean.struct.ExpStruct;
import com.topsec.tsm.sim.report.bean.struct.SqlStruct;
import com.topsec.tsm.sim.report.util.ReportUiConfig;
import com.topsec.tsm.sim.report.util.ReportUiUtil;

public class ReportExecutor implements Runnable,Callable<Boolean>{
	
	private RptMasterTbService rptMasterTbImp ;
	private ExpStruct exp ;
	private Map<SubjectKey, Map<Integer,ExpDateStruct>> expMap ;
	private List<ExpDateStruct> expList ;
	private Map sub;
	private HttpServletRequest request ;
	private int order ;
	private SID sid;
	public ReportExecutor(int order,RptMasterTbService rptMasterTbImp,ExpStruct exp,
			Map<SubjectKey, Map<Integer,ExpDateStruct>> expMap, List<ExpDateStruct> expList,
			Map sub, HttpServletRequest request,SID sid) {
		super();
		this.order = order ;
		this.rptMasterTbImp = rptMasterTbImp;
		this.exp = exp;
		this.expMap = expMap;
		this.expList = expList;
		this.sub = sub;
		this.request = request;
		this.sid=sid;
	}

	@Override
	public Boolean call() throws Exception {
		run();
		return true ;
	}

	public void run(){
		try {
			SID.setCurrentUser(sid);
			int mstType = Integer.parseInt(sub.get("mstType").toString());
			// 子报表信息
			List ruleResult = ReportModel.getRuleRs(mstType, sub, rptMasterTbImp);
			boolean isCoreNode = ReportUiUtil.isCoreNodeReport(sub);
			if(isCoreNode){
				for (int j = 0; j < ruleResult.size(); j++) {
					Map map = (Map) ruleResult.get(j);
					String talCategoryKey=(String)map.get("sqlParam");
					if("and dvcAddress = ?".equals(talCategoryKey) ||"and alias.dvcAddress = ?".equals(talCategoryKey) || "and fwrisk.dvcAddress = ?".equals(talCategoryKey)){
						ruleResult.remove(j);
						break;
					}
				}
			}
			// 设备类型 报表原子主题 vpn risk等
			String subSubject = (String) sub.get("subSubject");
			Integer subType = (Integer) sub.get("subType");
			SqlStruct struct=null;
			boolean isTrendChart = StringUtil.booleanVal(sub.get("chartProperty")) ;
			String tableSql = StringUtil.nvl((String)sub.get("tableSql")) ;
			String pageSql = StringUtil.nvl((String)sub.get("pagesql")) ;
			String chartSql = StringUtil.nvl((String)sub.get("chartSql")) ;
			String subName = StringUtil.nvl((String)sub.get("subName"));
			String mstName = StringUtil.nvl((String)sub.get("mstName"));
			if (isTrendChart && (subName.contains("时趋势")||subName.contains("日趋势")||subName.contains("月趋势")
					||subName.contains("时总趋势")||mstName.contains("时总趋势")
					||mstName.contains("时趋势")||mstName.contains("日趋势")||mstName.contains("月趋势"))) {
				String stimeHourType=ReportUiUtil.toStartTime("hour", exp.getRptTimeE());
				String stimeDayType=ReportUiUtil.toStartTime("day", exp.getRptTimeE());
				String stimeMonthType=ReportUiUtil.toStartTime("month", exp.getRptTimeE());
				if (tableSql.indexOf("Hour") > 20 || tableSql.indexOf("_hour") > 20) {
					struct = ReportModel.getSqlStruct(mstType, ruleResult, request, exp, sub, stimeHourType, exp.getRptTimeE());
				} else if (tableSql.indexOf("Day") > 20 || tableSql.indexOf("_day") > 20) {
					struct = ReportModel.getSqlStruct(mstType, ruleResult, request, exp, sub, stimeDayType, exp.getRptTimeE());
				}else if (tableSql.indexOf("Month") > 20 || tableSql.indexOf("_month") > 20) {
					struct = ReportModel.getSqlStruct(mstType, ruleResult, request, exp, sub, stimeMonthType, exp.getRptTimeE());
				} else if (pageSql.indexOf("Hour") > 20 || pageSql.indexOf("_hour") > 20 || chartSql.indexOf("Hour") > 20){
					struct = ReportModel.getSqlStruct(mstType, ruleResult, request, exp, sub, stimeHourType, exp.getRptTimeE());
				} else if (pageSql.indexOf("Day") > 20 || pageSql.indexOf("_day") > 20|| chartSql.indexOf("Day") > 20) {
					struct = ReportModel.getSqlStruct(mstType, ruleResult, request, exp, sub, stimeDayType, exp.getRptTimeE());
				} else if (pageSql.indexOf("Month") > 20 || pageSql.indexOf("_month") > 20|| chartSql.indexOf("Day") > 20) {
					struct = ReportModel.getSqlStruct(mstType, ruleResult, request, exp, sub, stimeMonthType, exp.getRptTimeE());
				} else{
					struct = ReportModel.getSqlStruct(mstType, ruleResult, request, exp, sub, exp.getRptTimeS(), exp.getRptTimeE());
				}	
			}else if(sub.get("subName").toString().indexOf("分布图") > 1){
				if (tableSql.indexOf("Hour") > 20 || tableSql.indexOf("_hour") > 20) {
					struct = ReportModel.getSqlStruct(mstType, ruleResult, request, exp, sub, ReportUiUtil.toStartTime("undefined", exp.getRptTimeE()), exp.getRptTimeE());
				}else{
					struct = ReportModel.getSqlStruct(mstType, ruleResult, request, exp, sub, exp.getRptTimeS(), exp.getRptTimeE());
				}
			} else{
				struct = ReportModel.getSqlStruct(mstType, ruleResult, request, exp, sub, exp.getRptTimeS(), exp.getRptTimeE());
			}
			
			ExpDateStruct exptmp = ReportModel.createExp2(sub, struct, subType, exp, request);
			exptmp.setTalCategoryLevel((short) 1);
			exptmp.setMstType(mstType);
			
			boolean isSysLog = ReportUiUtil.isSystemLog(sub);
			 if(isSysLog){
				subSubject="日志报表";
				exptmp.setSubType(subSubject.replace("Monitor/", ""));
			}else{ 
				exptmp.setSubType(subSubject.replace("Monitor/", ""));
			} 
			 SubjectKey sk = new SubjectKey(subSubject, order) ;
			Map<Integer,ExpDateStruct> tpmExp = expMap.get(sk) ;
			// 将同一种主题的报表放在一起
			if (tpmExp == null) {
				synchronized (expMap) {
					tpmExp = expMap.get(sk) ;
					if (tpmExp == null) {
						expMap.put(sk, (tpmExp = new TreeMap<Integer,ExpDateStruct>()));
					}
				}
			}
			tpmExp.put(order,exptmp);
			// 子报表内容
			//加入下钻标题 
			if (ReportUiUtil.checkNull(exptmp.getTalCategory())){
				String[] talCategoryArray=exptmp.getTalCategory();
				String talCategory="";
				if(talCategoryArray!=null&&talCategoryArray.length>0){
					for (int j = 0; j < talCategoryArray.length; j++) {
						if(talCategoryArray[j]!=null&&!talCategoryArray[j].equals("")&&!talCategoryArray[j].equals("null")){
							if(isSysLog){
								talCategoryArray[j]=ReportUiUtil.getDeviceTypeName(talCategoryArray[j], Locale.getDefault());
							}
							talCategory+="->"+talCategoryArray[j];
						}
					}
					if(talCategory.length()>2){
						talCategory=talCategory.substring(2);
					}
				}
				exptmp.setTitle(exptmp.getTitle().replace("(","(" + talCategory + " "));
			}
			expList.add(exptmp);
			exp.setRptSummarize(StringUtil.nvl((String)sub.get("summarize")));
			String rpttype="";
			if (request != null) {
				rpttype=request.getParameter(ReportUiConfig.dvctype);
			}else {
				rpttype=sub.get("subSubject")==null?"":sub.get("subSubject").toString();
			}
			boolean isEvtAssetReport="Profession/Group/Asset".equals(rpttype);
			if (isEvtAssetReport) {
				exp.setRptName("业务组设备详情报表");
			}else {
				exp.setRptName((String) sub.get("mstName"));
			}
			exp.setPdffooter( StringUtil.nvl((String)sub.get("pdffooter"),""));
			//为了能把下钻的内容导出而写
			if (sub.get("tableLink")!=null){
				ExpStruct exp2 = new ExpStruct();
				exp2.setCreTime(exp.getCreTime());
				exp2.setDvc(exp.getDvc());
				exp2.setFileType(exp.getFileType());
				exp2.setMstrptid(sub.get("tableLink").toString());
				exp2.setPdffooter(exp.getPdffooter());
				exp2.setRptIp(exp.getRptIp());
				exp2.setRptName(exp.getRptName());
				exp2.setRptSummarize(exp.getRptSummarize());
				exp2.setRptTimeE(exp.getRptTimeE());
				exp2.setRptTimeS(exp.getRptTimeS());
				exp2.setRptType(exp.getRptType());
				exp2.setRptUser(exp.getRptUser());
				exp2.setSubList(exp.getSubList());
				exp2.setTop(exp.getTop());
				
				if (exp.getDvc()=="" || exp.getDvc()==null){
					for(int j=0; j<ruleResult.size(); j++){
						Map tmpMap = (Map)ruleResult.get(j);
						if (tmpMap.get("ruleName").toString().equals("2")){
							if(request!=null){
								String onlyByDvctype=request.getParameter("onlyByDvctype");
								if(onlyByDvctype!=null&&onlyByDvctype.equals("onlyByDvctype")){
									
								}else{
									String onlyByDvctype2 = exptmp.getOnlyByDvctype();
									if(onlyByDvctype2!=null&&"onlyByDvctype".equals(onlyByDvctype2)){
										exp2.setOnlyByDvctype("onlyByDvctype");
										exp2.setDvc(tmpMap.get("sqlValue").toString());
									}else{
										exp2.setDvc(tmpMap.get("sqlValue").toString());
										exp2.setRptIp(tmpMap.get("sqlValue").toString());
										break;
									}
								}
							}else{
								String onlyByDvctype2 = exptmp.getOnlyByDvctype();
								if(onlyByDvctype2!=null&&"onlyByDvctype".equals(onlyByDvctype2)){
									exp2.setOnlyByDvctype("onlyByDvctype");
									exp2.setDvc(tmpMap.get("sqlValue").toString());
								}else{
									exp2.setDvc(tmpMap.get("sqlValue").toString());
									exp2.setRptIp(tmpMap.get("sqlValue").toString());
									break;
								}
							}
						}
					}		
				}
				if(mstType==2){
					for(int j=0; j<ruleResult.size(); j++){
						Map tmpMap = (Map)ruleResult.get(j);
						if((Integer)tmpMap.get("ruleDisplay")==1&&tmpMap.get("sqlValue")!=null){
							exp2.getResultList().add(tmpMap);
						}
					}
				}
				//对结果循环分别下钻
				String category;   //不是数组
				String talValue = exptmp.getSubTableFile().split(",")[0];
				if("ALLLOGTYPE".equals(talValue)){//如果是格式化后的数据，需要加前台获取原来没有进行格式化的数据
					talValue = ReportModel.UNFMT_DATA_PREFIX + talValue ;
				}
				Iterator it = exptmp.getTable().iterator(); 
				int subOrder = 1 ;
				while(it.hasNext()){ 
					Map talMap = (Map)it.next(); 
					category = (String)talMap.get(talValue);
					exp2.getMap().put("talCategory", category);  
					LinkedHashMap<String, List> expMap2 = ReportModel.expMstReport2(rptMasterTbImp, exp2, request);
					
					Iterator expIr = expMap2.keySet().iterator();
					Object strKey = null;
					List<ExpDateStruct> expList2 = null;
					while (expIr.hasNext()) {
						strKey = expIr.next();
						expList2 = expMap2.get(strKey);
						for (int k=0; k<expList2.size(); k++,subOrder++ ) {
							ExpDateStruct exp3 = expList2.get(k);
							expMap.get(new SubjectKey(subSubject,order)).put(order+subOrder,exp3);
						}
					}
				} 
			}
			 
			if(isSysLog){
				Collection<ExpDateStruct> list = expMap.get(new SubjectKey(subSubject,order)).values();
				if(list!=null){
					for (ExpDateStruct ex : list) {
						if("TYPE,COUNTS".equals(ex.getSubTableFile())){
							List<Map> subTable = ex.getSubTable();
							if(subTable!=null){
								for (Map map : subTable) {
									String type=(String)map.get("TYPE");
									type=type.replace("／", "/");
									String deviceTypeName = ReportUiUtil.getDeviceTypeName(type, Locale.getDefault());
									map.put("TYPE", deviceTypeName);
								}
							}
						}
					}
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			SID.removeCurrentUser();
		}
	}
	
	public static class SubjectKey implements Comparable<SubjectKey>{
		public String subject ;
		public int order ;
		public SubjectKey(String subject, int order) {
			super();
			this.subject = subject;
			this.order = order;
		}
		
		@Override
		public int hashCode() {
			return subject.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof SubjectKey)){
				return false ;
			}
			SubjectKey sk = (SubjectKey)obj ;
			return sk.subject.equals(this.subject);
		}

		@Override
		public int compareTo(SubjectKey o) {
			return subject.equals(o.subject) ? 0 : order-o.order;
		} 
		
	}
}
