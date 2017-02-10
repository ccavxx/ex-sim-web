package com.topsec.tsm.sim.report.model;

import java.util.Map;

public class Subject implements ReportComponent {

	private Map subject ;
	
	public Subject(Map subject) {
		this.subject = subject;
	}

	@Override
	public void draw(StringBuffer sb) {
		sb.append("<div class='easyui-panel' ")
		  .append("title='").append(subject.get("subName")).append("'")
		  .append(">")
		  .append("")
		  .append("</div>") ;
	}

}
