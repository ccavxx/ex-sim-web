package com.topsec.tsm.sim.log.web;

import java.io.OutputStream;

import com.topsec.tal.base.hibernate.ReportTask;

public interface ReportFileExporter {

	public void setReport(ReportTask task) ;
	
	public void exportReportTo(OutputStream os) ;
	
}
