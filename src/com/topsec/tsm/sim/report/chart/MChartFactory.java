package com.topsec.tsm.sim.report.chart;

import java.awt.Color;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.topsec.tsm.sim.report.chart.jfreechart.CreateBarChart;
import com.topsec.tsm.sim.report.chart.jfreechart.CreateCyliderChart;
import com.topsec.tsm.sim.report.chart.jfreechart.CreateLineChart;
import com.topsec.tsm.sim.report.chart.jfreechart.CreatePieChart;
import com.topsec.tsm.sim.report.chart.jfreechart.CreateStackedChart;
import com.topsec.tsm.sim.report.chart.jfreechart.Createable;
import com.topsec.tsm.sim.report.model.ChartData;
import com.topsec.tsm.sim.report.util.ChartConstant;

public class MChartFactory {
	private static MChartFactory instance = new MChartFactory();

	private static Createable barChart = new CreateBarChart();
	private static Createable cyliderChart = new CreateCyliderChart();
	private static Createable lineChart = new CreateLineChart();
	private static Createable pieChart = new CreatePieChart();
	private static Createable stackedChart = new CreateStackedChart();

	public Object createChart(List<ChartData> data, ChartRender render,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		String type = render.getType();
		ChartPlotRender plot = render.getPlot();
		plot.setBackgroundPaint(Color.WHITE);
		
		if (type == null) {
			throw new Exception("The chart type is null!");
		}

		if (type.equals(ChartConstant.Type_BarChart)) {
			return barChart.createChart(data, render, request, response);

		} else if (type.equals(ChartConstant.Type_CyliderChart)) {

			return cyliderChart.createChart(data, render, request, response);
		} else if (type.equals(ChartConstant.Type_StackedChart)) {

			return stackedChart.createChart(data, render, request, response);

		} else if (type.equals(ChartConstant.Type_LineChart)) {

			return lineChart.createChart(data, render, request, response);

		} else if (type.equals(ChartConstant.Type_PieChart)) {
			return pieChart.createChart(data, render, request, response);
		}

		return null;
	}

	public static MChartFactory getInstance() {
		return instance;
	}

}
