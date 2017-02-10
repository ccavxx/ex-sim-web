package com.topsec.tsm.sim.event.bean;

import java.util.Calendar;
import java.util.Date;

import com.topsec.tsm.sim.util.DateUtils;

public class QueryConditionAdapter {
		private BasicQueryCriteria  basicQueriCriteria;

		public QueryConditionAdapter(BasicQueryCriteria basicQueriCriteria) {
			super();
			this.basicQueriCriteria = basicQueriCriteria;
		}
		
		public Condition getRequestCondition(){
			Condition  condition=new Condition();
			
			adapt(condition);
			return condition;
		}

		private void adapt(Condition condition) {
			// TODO Auto-generated method stub
			String startDate=basicQueriCriteria.getStartTime();
			String endDate=basicQueriCriteria.getEndTime();
			String deviceType=basicQueriCriteria.getDeviceType();
			String destPort=basicQueriCriteria.getDestPort();
			String srcPort=basicQueriCriteria.getSrcPort();
			String dvcIp=basicQueriCriteria.getDeviceIp();
			String srcIP=basicQueriCriteria.getSrcIp();
			String destIp=basicQueriCriteria.getDestIp();
			String eventCategory1=basicQueriCriteria.getCategory1();
			String eventCategory2=basicQueriCriteria.getCategory2();
			String eventType=basicQueriCriteria.getEventType();
			String trans_protocol=basicQueriCriteria.getProtocol();
			String priority=basicQueriCriteria.getPriority();
			String name=basicQueriCriteria.getEventName();
		    String ruleName=basicQueriCriteria.getRuleName();
		    String query_event_Name=basicQueriCriteria.getQuery_event_Name();
		    String confirm = basicQueriCriteria.getConfirm();
		    String confirm_person = basicQueriCriteria.getConfirm_person();
			if(startDate!=null&&startDate.length()!=0){
				
				condition.setStart_time(startDate.replace("/", "-"));
			}else{
				condition.setStart_time(DateUtils.formatDatetime(new Date(0), "yyyy-MM-dd HH:mm:ss"));
			}
			if(endDate!=null&&endDate.length()!=0){
				condition.setEnd_time(endDate.replace("/", "-"));
			}else{
				condition.setEnd_time(DateUtils.formatDatetime(new Date(System.currentTimeMillis()), "yyyy-MM-dd HH:mm:ss"));
			}
			if(deviceType != null && deviceType !=""){
				condition.setDevice_types(deviceType.split(","));
			}
			if(destPort != null && destPort !=""){
				condition.setDest_pts(destPort.split(","));
			}
			if(srcPort != null && srcPort !=""){
				condition.setSrc_pts(srcPort.split(","));
			}
			if(dvcIp != null && dvcIp !=""){
				condition.setDvc_ips(dvcIp.split(","));
			}
			if(srcIP != null && srcIP !=""){
				condition.setSrc_ips(srcIP.split(","));
			}
			if(destIp != null && destIp !=""){
				condition.setDest_ips(destIp.split(","));
			}
			if(eventType != null && eventType !=""){
				condition.setEve_types("EVENT_TYPE='"+eventType+"'");
			}
			
			if (eventCategory1!=null && !eventCategory1.equals("")) {
				
				condition.setCategory1(eventCategory1.split(","));
			}
			if (eventCategory2!=null && !eventCategory2.equals("")) {
				
				condition.setCategory2(eventCategory2.split(","));
			}
			if(query_event_Name!=null && !"".equals(query_event_Name)){
				condition.setQuery_event_Name(query_event_Name.split(","));
			}
			if(priority!=null && !"".equals(priority)){
				condition.setPriority(priority.split(","));
			}
			if (confirm != null && !"".equals(confirm)) {
				if ("已确认".equals(confirm)) {
					condition.setConfirm("1");
				} else {
					condition.setConfirm("0");
				}
			}
			if (confirm_person != null && !"".equals(confirm_person)) {
				condition.setConfirm_person(confirm_person);
			}
			condition.setIp(basicQueriCriteria.getIp()) ;
			condition.setTrans_protocol(trans_protocol);
			//condition.setPriori(priority);
			condition.setName(name);
			condition.setPageSize(basicQueriCriteria.getRows());
			condition.setSizeStart((basicQueriCriteria.getPage()-1)*basicQueriCriteria.getRows());
			condition.setColumnsSet(basicQueriCriteria.getFields());
		    condition.setEventName(ruleName);
		}
		
		public static void main(String[] args) {
			System.out.println(new Date(System.currentTimeMillis()));
			System.out.println(new Date(0));
		}
}
