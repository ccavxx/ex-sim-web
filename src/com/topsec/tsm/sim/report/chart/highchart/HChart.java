package com.topsec.tsm.sim.report.chart.highchart;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HChart {
	
	public static final String[] colors = new String[]{"#32c8fa","#f99049","#a4e120","#ffe666","#906bc8", "#08a4f3","#ffa03f","#99cc00","#fff558","#d040ff", "#99ccff","#ff7f3a","#labebe","#f7c43b","#ff52b0", "#6666ff","#fe8a8a","#2cb022","#ec6dff","#95cfd1"} ;
	
	Object createChart(List<Map> data,Map<Object,Object> subMap, HttpServletRequest request,
			HttpServletResponse response) throws Exception;

}
