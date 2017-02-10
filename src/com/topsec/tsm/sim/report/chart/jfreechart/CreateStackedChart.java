package com.topsec.tsm.sim.report.chart.jfreechart;

import java.awt.GradientPaint;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;

import com.topsec.tsm.sim.report.chart.ChartAxisLabelRender;
import com.topsec.tsm.sim.report.chart.ChartLegendRender;
import com.topsec.tsm.sim.report.chart.ChartPlotRender;
import com.topsec.tsm.sim.report.chart.ChartRender;
import com.topsec.tsm.sim.report.chart.ChartTitleRender;
import com.topsec.tsm.sim.report.model.ChartData;
import com.topsec.tsm.sim.report.util.ChartConstant;

public class CreateStackedChart extends BaseCreate {

	public Object createChart(List<ChartData> data, ChartRender render,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		Object result = null;

		String type = render.getType();

		if (type == null) {
			throw new Exception("The chart type is null!");
		}
		JFreeChart chart = null;

		ChartTitleRender title = render.getTitle();
		ChartAxisLabelRender categoryAxisLabel = render.getCategoryAxisLabel();
		ChartAxisLabelRender valueAxisLabel = render.getValueAxisLabel();
		ChartLegendRender legend = render.getLenged();
		ChartPlotRender plotRender = render.getPlot();
		PlotOrientation orientation = PlotOrientation.VERTICAL;

		if (render.getOrientation() != null&& render.getOrientation().equals(ChartConstant.PlotOrientation_HORIZONTAL)) {
			orientation = PlotOrientation.HORIZONTAL;
		}

		String _title = title.isShow() ? title.getTitle() : "";

		String categoryAxisLabelName = categoryAxisLabel.isShow() ? categoryAxisLabel.getName(): "";

		String valueAxisLabelName = valueAxisLabel.isShow() ? valueAxisLabel.getName() : "";

		chart = ChartGraphFactory.createStackedBarChart(render.isC3D(), _title,categoryAxisLabelName,
				valueAxisLabelName,createCategoryDataset(data), orientation, legend.isShow(),
				render.isTooltips(), render.isUrls());

		if (chart == null) {
			return null;
		}
		if(render.getBackgroundColorOrientation()!=null && render.getBackgroundColorOrientation().equals(ChartConstant.PlotOrientation_HORIZONTAL)){
			chart.setBackgroundPaint(new GradientPaint(0f, 0f, render.getBackgroundColor(), render.getWidth(), 0f,render.getBackgroundColor1(), true));
		}else{
			chart.setBackgroundPaint(new GradientPaint(0f, 0f, render.getBackgroundColor(), 0f, render.getHight(),render.getBackgroundColor1(), true));
		}
		
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		doCategoryPlot(plot, plotRender);

		CategoryAxis categoryAxis = plot.getDomainAxis();
		doCategoryAxis(categoryAxis, categoryAxisLabel);
		
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		doNumberAxis(rangeAxis, valueAxisLabel);

		if (!render.isC3D()) {
			CategoryItemRenderer renderer = new ExtendedStackedBarRenderer();

			renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
			plot.setRenderer(renderer);
		}
		if (legend.isShow()) {
			chart.getLegend().setItemFont(legend.getFont());
		}
		// set title
		doTitle(chart, title, render);
		
		result = generateImgName(chart, render, request);

		return result;
	}

}
