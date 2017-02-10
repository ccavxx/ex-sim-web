package com.topsec.tsm.sim.report.model;

public class Row extends SubjectContainer{

	public Row(ReportComponent parent) {
		super(parent);
	}

	@Override
	public void draw(StringBuffer sb) {
		sb.append("<tr>") ;
		super.draw(sb);
		sb.append("</tr>") ;
	}
}
