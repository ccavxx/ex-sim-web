package com.topsec.tsm.sim.report.chart.jfreechart;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.topsec.tsm.sim.report.chart.ChartRender;
import com.topsec.tsm.sim.report.model.ChartData;

public interface Createable {
	Object createChart(List<ChartData> data, ChartRender render,HttpServletRequest request, HttpServletResponse response)throws Exception;
}
