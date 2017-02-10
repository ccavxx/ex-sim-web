package com.topsec.tsm.sim.report.chart.jfreechart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.PieDataset;

public class ChartGraphFactory {

	public static JFreeChart createPieChart(boolean is3D, String title,PieDataset dataset, boolean legend, boolean tooltips, boolean urls) {
		JFreeChart chart;
		if (is3D) {
			chart = ChartFactory.createPieChart3D(title, dataset, legend, tooltips, urls);
		} else {

			chart = ChartFactory.createPieChart(title, dataset, legend, tooltips, urls);
		}

		return chart;
	}

	public static JFreeChart createBarChart(boolean is3D, String title,
			String categoryAxisLabel, String valueAxisLabel,
			CategoryDataset dataset, PlotOrientation orientation,
			boolean legend, boolean tooltips, boolean urls) {

		JFreeChart chart;
		if (is3D)
			chart = ChartFactory.createBarChart3D(title, categoryAxisLabel, valueAxisLabel, dataset, orientation, legend, tooltips, urls);
		else
			chart = ChartFactory.createBarChart(title, categoryAxisLabel, valueAxisLabel, dataset, orientation, legend, tooltips, urls);

		return chart;

	}

	public static JFreeChart createStackedBarChart(boolean is3D, String title,
			String categoryAxisLabel, String valueAxisLabel,
			CategoryDataset dataset, PlotOrientation orientation,
			boolean legend, boolean tooltips, boolean urls) {
		JFreeChart chart;
		if (is3D) {
			chart = ChartFactory.createStackedBarChart3D(title, categoryAxisLabel, valueAxisLabel, dataset, orientation, legend, tooltips, urls);
		} else {
			chart = ChartFactory.createStackedBarChart(title, categoryAxisLabel, valueAxisLabel, dataset, orientation, legend, tooltips, urls);
		}

		return chart;
	}

	public static JFreeChart createLineChart(boolean is3D, String title,
			String categoryAxisLabel, String valueAxisLabel,
			CategoryDataset dataset, PlotOrientation orientation,
			boolean legend, boolean tooltips, boolean urls) {
		JFreeChart chart;
		if (is3D) {
			chart = ChartFactory.createLineChart3D(title, categoryAxisLabel, valueAxisLabel, dataset, orientation, legend, tooltips, urls);
		} else {
			chart = ChartFactory.createLineChart(title, categoryAxisLabel, valueAxisLabel, dataset, orientation, legend, tooltips, urls);
		}

		return chart;
	}

	public static JFreeChart createAreaChart(boolean isStacked, String title,
			String categoryAxisLabel, String valueAxisLabel,
			CategoryDataset dataset, PlotOrientation orientation,
			boolean legend, boolean tooltips, boolean urls) {
		JFreeChart chart;
		if (isStacked) {
			chart = ChartFactory.createStackedAreaChart(title, categoryAxisLabel, valueAxisLabel, dataset, orientation, legend, tooltips, urls);
		} else {
			chart = ChartFactory.createAreaChart(title, categoryAxisLabel, valueAxisLabel, dataset, orientation, legend, tooltips, urls);
		}

		return chart;
	}
}
