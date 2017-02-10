package com.topsec.tsm.sim.report.model;

public class Table extends SubjectContainer{

	
	
	@Override
	public void draw(StringBuffer sb) {
		sb.append("<table style='width:100%;height:100%;' border='0' cellspacing='0' cellpadding='0' border='0'>") ;
		super.draw(sb);
		sb.append("</table>") ;
	}
	public int getMaxColumnCount(){
		int maxColumnCount = -1 ;
		for(ReportComponent row:getChild()){
			maxColumnCount = Math.max(maxColumnCount, ((SubjectContainer)row).childCount()) ;
		}
		return maxColumnCount ;
	}
	/**
	 * 根据最大列宽重新计划各列的colspan
	 */
	public void reCalculate(){
		int maxColumnCount = getMaxColumnCount() ;
		for(ReportComponent row:getChild()){
			Row r = (Row) row ;
			if(r.childCount()<maxColumnCount){
				for(int i=r.childCount()-1;i<maxColumnCount-r.childCount();i++){
					Column c = (Column)r.getChild(i%r.childCount()) ;
					c.setColSpan(c.getColSpan()+1) ;
				}
			}
		}
	}
}
