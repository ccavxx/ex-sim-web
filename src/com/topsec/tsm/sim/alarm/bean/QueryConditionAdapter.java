package com.topsec.tsm.sim.alarm.bean;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.event.bean.Condition;

public class QueryConditionAdapter {
	
	
	private AlarmQueryCriteria   alarmQueriCriteria;

	public QueryConditionAdapter(AlarmQueryCriteria alarmQueriCriteria) {
		super();
		this.alarmQueriCriteria = alarmQueriCriteria;
	}
	
	public Condition getRequestCondition(){
		Condition  condition=new Condition();
		
		adapt(condition);
		return condition;
	}

	private void adapt(Condition condition) {
		String startDate=alarmQueriCriteria.getStartTime();
		String endDate=alarmQueriCriteria.getEndTime();
		String dvcIp=alarmQueriCriteria.getDeviceIp();
		String srcIP=alarmQueriCriteria.getSrcIp();
		String destIp=alarmQueriCriteria.getDestIp();
		String eventCategory1=alarmQueriCriteria.getCategory1();
		String eventCategory2=alarmQueriCriteria.getCategory2();
		String priority=alarmQueriCriteria.getPriority();
		String name=alarmQueriCriteria.getEventName();
		String cat3 = alarmQueriCriteria.getCategory3() ;
		String ip = alarmQueriCriteria.getIp() ;
		if(startDate!=null&&startDate.length()!=0){
			condition.setStart_time(startDate.replace("/", "-"));
		}
		if(endDate!=null&&endDate.length()!=0){
			condition.setEnd_time(endDate.replace("/", "-"));
		}
		if(dvcIp != null && dvcIp !=""){
			condition.setDvc_address(dvcIp);
		}
		if(srcIP != null && srcIP !=""){
			condition.setSrc_address(srcIP);
		}
		if(destIp != null && destIp !=""){
			condition.setDest_address(destIp);
		}
		if (eventCategory1!=null && !eventCategory1.equals("")) {
			condition.setCat1_id(eventCategory1);
		}
		if (eventCategory2!=null && !eventCategory2.equals("")) {
			condition.setCat2_id(eventCategory2);
		}
		if (StringUtil.isNotBlank(cat3)) {
			condition.setCat3_id(cat3) ;
		}
		if(StringUtil.isNotBlank(ip)){
			condition.setIp(ip) ;
		}
 
		condition.setPriori(priority);
		condition.setName(name);
		condition.setPageSize(alarmQueriCriteria.getRows());
		condition.setSizeStart((alarmQueriCriteria.getPage()-1)*alarmQueriCriteria.getRows());
		condition.setColumnsSet(alarmQueriCriteria.getFields());
		condition.setAlarmState(1);
	}
}
