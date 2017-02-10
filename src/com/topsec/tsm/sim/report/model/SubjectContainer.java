package com.topsec.tsm.sim.report.model;

import java.util.ArrayList;
import java.util.List;

public class SubjectContainer implements ReportComponent{
	
	private ReportComponent parent ;
	private List<ReportComponent> child = new ArrayList<ReportComponent>() ;
	
	public SubjectContainer(ReportComponent parent) {
		this.parent = parent;
	}

	public SubjectContainer() {
		super();
	}



	@Override
	public void draw(StringBuffer sb) {
		for(ReportComponent comp:child){
			comp.draw(sb) ;
		}
	}
	
	public void add(ReportComponent comp){
		child.add(comp) ;
	}
	
	public ReportComponent getChild(int index){
		if(index>=child.size()||index<0){
			return null ;
		}
		return child.get(index) ;
	}
	
	public int childCount(){
		return child.size() ;
	}

	public ReportComponent getParent() {
		return parent;
	}

	public void setParent(ReportComponent parent) {
		this.parent = parent;
	}

	public List<ReportComponent> getChild() {
		return child;
	}

	public void setChild(List<ReportComponent> child) {
		this.child = child;
	}

}
