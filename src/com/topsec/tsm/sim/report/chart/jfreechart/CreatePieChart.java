package com.topsec.tsm.sim.report.chart.jfreechart;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.util.Rotation;

import com.topsec.tsm.sim.report.chart.ChartLegendRender;
import com.topsec.tsm.sim.report.chart.ChartPlotRender;
import com.topsec.tsm.sim.report.chart.ChartRender;
import com.topsec.tsm.sim.report.chart.ChartTitleRender;
import com.topsec.tsm.sim.report.model.ChartData;
import com.topsec.tsm.sim.report.util.ChartConstant;
import com.topsec.tsm.sim.report.util.ReportUiConfig;

public class CreatePieChart extends BaseCreate {

	public Object createChart(List<ChartData> data, ChartRender render, HttpServletRequest request, HttpServletResponse response) throws Exception {

		for (int i = 0; i < data.size(); i++) {
			String ctg = data.get(i).getCategory();
			String[] ctgArray = StringUtils.split(ctg, "***");

			if (ctgArray[1].equals("总事件")) {
				data.remove(i);
				break;
			}
		}
		Object result = null;

		JFreeChart chart = null;
		render.setC3D(true);

		ChartTitleRender title = render.getTitle();
		ChartLegendRender legend = render.getLenged();
		ChartPlotRender plotRender = render.getPlot();

		String _title = title.isShow() ? title.getTitle() : "";

		chart = ChartGraphFactory.createPieChart(render.isC3D(), _title, createPieDataset(data), legend.isShow(), render.isTooltips(), render.isUrls());

		if (chart == null) {
			return null;
		}
		if (render.getBackgroundColorOrientation() != null && render.getBackgroundColorOrientation().equals(ChartConstant.PlotOrientation_HORIZONTAL)) {
			chart.setBackgroundPaint(new GradientPaint(0f, 0f, render.getBackgroundColor(), render.getWidth(), 0f, render.getBackgroundColor1(), true));
		} else {
			chart.setBackgroundPaint(new GradientPaint(0f, 0f, render.getBackgroundColor(), 0f, render.getHight(), render.getBackgroundColor1(), true));
		}

		if (render.isC3D()) {
			PiePlot3D plot = (PiePlot3D) chart.getPlot();
			plot.setLabelFont(title.getFont());
			plot.setStartAngle(290);
			plot.setDirection(Rotation.CLOCKWISE);
			plot.setForegroundAlpha(0.5f);
			plot.setNoDataMessage(ReportUiConfig.NoDataMessage);
			plot.setNoDataMessageFont(ChartConstant.Default_Font);
			plot.setBackgroundPaint(plotRender.getBackgroundPaint());
			Paint[] paint = createPaint() ;
			for(int i=0;i<plot.getDataset().getItemCount();i++){
				if(i<paint.length){
					plot.setSectionPaint(plot.getDataset().getKey(i), paint[i]) ;
				}
			}
			
		} else {
			PiePlot plot = (PiePlot) chart.getPlot();
			plot.setLabelFont(title.getFont());
			plot.setNoDataMessage(ReportUiConfig.NoDataMessage);
			plot.setNoDataMessageFont(ChartConstant.Default_Font);
			plot.setCircular(false);
			plot.setLabelGap(0.02);
			plot.setBackgroundPaint(plotRender.getBackgroundPaint());
			Paint[] paint = createPaint() ;
			for(int i=0;i<plot.getDataset().getItemCount();i++){
				if(i<paint.length){
					plot.setSectionPaint(plot.getDataset().getKey(i), paint[i]) ;
				}
			}
		}

		if (legend.isShow()) {
			chart.getLegend().setItemFont(legend.getFont());
		}
		// set title
		doTitle(chart, title, render);
		PiePlot3D plot = (PiePlot3D) chart.getPlot();
		plot.setForegroundAlpha(0.95f);
		String labelFormat = "{0}({2})";
		plot.setLabelGenerator(new StandardPieSectionLabelGenerator(labelFormat));
		plot.setBackgroundPaint(Color.WHITE);
		plot.setDarkerSides(true);
		plot.setLabelFont(ChartConstant.FONT);
		plot.setOutlineVisible(false);
		result = generateImgName(chart, render, request);

		return result;
	}
}
