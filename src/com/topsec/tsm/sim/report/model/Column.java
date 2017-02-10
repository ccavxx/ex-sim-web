package com.topsec.tsm.sim.report.model;

public class Column extends SubjectContainer{
	private int colSpan = 1 ;
	public Column(Row parent) {
		super(parent) ;
	}

	@Override
	public void draw(StringBuffer sb) {
		sb.append("<td ")
		  .append("colspan='").append(colSpan).append("'")
		  .append(">") ;
		super.draw(sb);
		sb.append("</td>") ;
	}

	public int getColSpan() {
		return colSpan;
	}

	public void setColSpan(int colSpan) {
		this.colSpan = colSpan;
	}
	
}
