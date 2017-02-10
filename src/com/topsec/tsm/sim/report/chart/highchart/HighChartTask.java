package com.topsec.tsm.sim.report.chart.highchart;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import com.topsec.tsm.sim.report.bean.struct.ExpDateStruct;

public class HighChartTask implements Runnable, Callable<Map<ExpDateStruct,String>> {
	
	
	private ExpDateStruct struct ;
	private String exportTool ;
	
	public HighChartTask(ExpDateStruct struct) {
		super();
		this.struct = struct;
	}
	public HighChartTask(ExpDateStruct struct,String exportTool) {
		super();
		this.struct = struct;
		this.exportTool = exportTool ;
	}

	@Override
	public void run() {
		try {
			call() ;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Map<ExpDateStruct,String> call(){
		Map<ExpDateStruct, String> result = new HashMap<ExpDateStruct, String>(2);
		String jsonData = CreateChartFactory.getInstance().CreateJsonChart(struct.getSubChart()) ;
		String fileName;
		try {
			if(HighChartExportTool.RHINO_EXPORT.equals(exportTool)){
				fileName = HighChartExportTool.exportUseRhino(jsonData) ;
			}else{
				fileName = HighChartExportTool.exportUsePhantomjs(jsonData);
			}
			result.put(struct, fileName) ;
		} catch (Exception e) {
			e.printStackTrace();
			result.put(struct, null) ;
		}
		return result ;
	}
	
}
