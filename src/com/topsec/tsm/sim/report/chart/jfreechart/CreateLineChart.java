package com.topsec.tsm.sim.report.chart.jfreechart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.geom.Ellipse2D;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;

import com.topsec.tsm.sim.report.chart.ChartAxisLabelRender;
import com.topsec.tsm.sim.report.chart.ChartLegendRender;
import com.topsec.tsm.sim.report.chart.ChartPlotRender;
import com.topsec.tsm.sim.report.chart.ChartRender;
import com.topsec.tsm.sim.report.chart.ChartTitleRender;
import com.topsec.tsm.sim.report.model.ChartData;
import com.topsec.tsm.sim.report.util.ChartConstant;
import com.topsec.tsm.sim.report.util.ReportUiUtil;

public class CreateLineChart extends BaseCreate {

	public Object createChart(List<ChartData> data, ChartRender render, HttpServletRequest request, HttpServletResponse response) throws Exception {

		Object result = null;

		JFreeChart chart = null;

		ChartTitleRender title = render.getTitle();
		ChartAxisLabelRender categoryAxisLabel = render.getCategoryAxisLabel();
		ChartAxisLabelRender valueAxisLabel = render.getValueAxisLabel();
		ChartLegendRender legend = render.getLenged();
		legend.setShow(true);
		ChartPlotRender plotRender = render.getPlot();
		PlotOrientation orientation = PlotOrientation.VERTICAL;

		if (render.getOrientation() != null && render.getOrientation().equals(ChartConstant.PlotOrientation_HORIZONTAL)) {
			orientation = PlotOrientation.HORIZONTAL;
		}

		String _title = title.isShow() ? title.getTitle() : "";

		String categoryAxisLabelName = categoryAxisLabel.isShow() ? categoryAxisLabel.getName() : "";

		String valueAxisLabelName = valueAxisLabel.isShow() ? valueAxisLabel.getName() : "";

		for (ChartData cd : data) {
			cd.setCategory(ReportUiUtil.filterTime(cd.getCategory(), false, null));
		}

		render.setC3D(false);
		chart = ChartGraphFactory.createLineChart(render.isC3D(), _title, categoryAxisLabelName, valueAxisLabelName,
				createCategoryDataset(data), orientation, legend.isShow(), render.isTooltips(), render.isUrls());
		if (chart == null) {
			return null;
		}
		if (render.getBackgroundColorOrientation() != null && render.getBackgroundColorOrientation().equals(ChartConstant.PlotOrientation_HORIZONTAL)) {
			chart.setBackgroundPaint(new GradientPaint(0f, 0f, render.getBackgroundColor(), render.getWidth(), 0f, render.getBackgroundColor1(), true));
		} else {
			chart.setBackgroundPaint(new GradientPaint(0f, 0f, render.getBackgroundColor(), 0f, render.getHight(), render.getBackgroundColor1(), true));
		}

		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		doCategoryPlot(plot, plotRender);
		// x��
		plot.setRangeGridlinesVisible(true) ;
		plot.setRangeGridlinePaint(Color.decode("#E3E3E3")) ;
		plot.setRangeGridlineStroke(new BasicStroke(0.5f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND,0.5f,new float[]{5,5},0f)) ;
		// y��
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.decode("#E3E3E3"));
		plot.setDomainGridlineStroke(new BasicStroke(0.5f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND,0.5f,new float[]{5,5},0f));


		CategoryAxis categoryAxis = plot.getDomainAxis();
		doCategoryAxis(categoryAxis, categoryAxisLabel);

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		doNumberAxis(rangeAxis, valueAxisLabel);

		LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
		renderer.setDrawOutlines(true);
		renderer.setUseFillPaint(true);

		if (legend.isShow()) {
			chart.getLegend().setItemFont(legend.getFont());
		}
		// set title
		doTitle(chart, title, render);

		if (data.size() == 1) {
			renderer.setBaseShapesVisible(true);
			renderer.setDrawOutlines(true);
			renderer.setUseFillPaint(true);
			// renderer.setFillPaint(Color.white);
			renderer.setSeriesStroke(0, new BasicStroke(3.0f));
			renderer.setSeriesOutlineStroke(0, new BasicStroke(2.0f));
			renderer.setSeriesShape(0, new Ellipse2D.Double(-5.0, -5.0, 10.0, 10.0));
		}
		// renderer.setPaint(Color.blue);
		renderer.setBaseStroke(new BasicStroke(2.0f));
		Paint[] paints = createPaint() ;
		for (int i = 0; i < paints.length; i++) {
			renderer.setSeriesPaint(i, paints[i]) ;
		}
		LegendTitle lg = chart.getLegend();
		lg.setItemFont(ChartConstant.FONT);
		plot.setNoDataMessageFont(ChartConstant.FONT);
		plot.setOutlinePaint(Color.gray);
		result = generateImgName(chart, render, request);
		return result;
	}

}
