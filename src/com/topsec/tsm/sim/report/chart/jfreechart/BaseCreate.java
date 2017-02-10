package com.topsec.tsm.sim.report.chart.jfreechart;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.servlet.ServletUtilities;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.ui.RectangleInsets;

import com.topsec.tsm.sim.report.chart.ChartAxisLabelRender;
import com.topsec.tsm.sim.report.chart.ChartPlotRender;
import com.topsec.tsm.sim.report.chart.ChartRender;
import com.topsec.tsm.sim.report.chart.ChartTitleRender;
import com.topsec.tsm.sim.report.model.ChartData;
import com.topsec.tsm.sim.report.util.ChartConstant;
import com.topsec.tsm.sim.report.util.ReportUiConfig;

public abstract class BaseCreate implements Createable {
	private static final String[] defaultColor = new String[] {"#FFA940","#ACD25E","#FCDE3D","#CCFFFF","#94E4AE","#F4A0A0","#409FFF","#DDE6E6","#F2D8C2","#FEF5C8","#CBD8E9","#F1EAA4","#AFD8F8","#F6BD0F","#8BBA00","#FF8E46","#008E8E","#D64646","#8E468E","#588526","#B3AA00","#4169E1"} ;
	public abstract Object createChart(List<ChartData> data,
			ChartRender render, HttpServletRequest request,
			HttpServletResponse response) throws Exception;

	public Object generateImgName(JFreeChart chart, ChartRender render,
			HttpServletRequest request) throws Exception {

		String result = null;

		if (request == null) {
			return chart;
		}

		ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
		result = ServletUtilities.saveChartAsPNG(chart, render.getWidth(),render.getHight(), info, request.getSession());

		StringWriter out = new StringWriter();

		ChartUtilities.writeImageMap(new PrintWriter(out), result, info, false);

		StringBuffer sb = new StringBuffer();

		sb.append("<div class=\"imgWap\">");
		sb.append("<img align=\"center\"");
		sb.append(" style=\"border:0px;width:").append(render.getWidth()).append("px;");
		sb.append("height:").append(render.getHight()).append("px;\"");
		sb.append(" src=\"").append(request.getContextPath() + "/servlet/DisplayChart?filename="+ result);
		sb.append("\" usemap=\"#");
		sb.append(result);
		sb.append("\"> ");
		sb.append(out.getBuffer().toString());
		sb.append("</div>");
		return sb.toString();
	}

	public Paint[] createPaint() {
		Paint[] colors = new Paint[26];
		for (int i = 0; i < defaultColor.length; i++) {
			colors[i] = Color.decode(defaultColor[i]);
		}
		colors[22] = new GradientPaint(0f, 0f, Color.white, 0f, 0f,Color.magenta);
		colors[23] = new GradientPaint(0f, 0f, Color.white, 0f, 0f, Color.red);
		colors[24] = new GradientPaint(0f, 0f, Color.white, 0f, 0f,Color.yellow);
		colors[25] = new GradientPaint(0f, 0f, Color.white, 0f, 0f, Color.CYAN);

		return colors;
	}

	public Paint[] createPaint1() {
		Paint[] colors = new Paint[26];
		for (int i = 0; i < defaultColor.length; i++) {
			colors[i] = Color.decode(defaultColor[i]);
		}
		colors[22] = Color.magenta;
		colors[23] = Color.red;
		colors[24] = Color.yellow;
		colors[25] = Color.CYAN;

		return colors;
	}

	protected void doTitle(JFreeChart chart, ChartTitleRender render,
			ChartRender chartRender) {
		// 不显示title
		 TextTitle title = chart.getTitle();
		 title.setBorder(0, 0, 0, 0);
		 title.setWidth(chartRender.getWidth());
		 Font f = render.getFont();	
		 Font ft = new Font(f.getName(), 0, f.getSize());
		 title.setFont(ft);
		 title.setExpandToFitSpace(true);
		
	}

	protected CategoryDataset createCategoryDataset(List<ChartData> data) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (ChartData dt : data) {
			dataset.addValue(dt.getValue(), dt.getSerise(), dt.getCategory());
		}
		return dataset;
	}

	protected PieDataset createPieDataset(List<ChartData> data) {
		DefaultPieDataset dataset = new DefaultPieDataset();
		for (ChartData dt : data) {
			String ctg = dt.getCategory();
			String[]ctgArray=StringUtils.split(ctg, "***");
			dataset.setValue(ctgArray[1], dt.getValue());
		}
		return dataset;
	}

	protected void doNumberAxis(NumberAxis rangeAxis,
			ChartAxisLabelRender valueAxisLabel) {

		rangeAxis.setUpperMargin(0.15);
		if (valueAxisLabel.getFont() == null) {
			rangeAxis.setLabelFont(new Font("Tahoma", 10, 12));
		} else {
			rangeAxis.setLabelFont(valueAxisLabel.getFont());
		}
		// 保证不出现小数
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		// 百分数
		// rangeAxis.setNumberFormatOverride(new DecimalFormat("0%"));
	}

	protected void doCategoryAxis(CategoryAxis categoryAxis,
			ChartAxisLabelRender categoryAxisLabel) {
		if (categoryAxisLabel.getFont() == null) {
			categoryAxis.setLabelFont(ChartConstant.Default_Font);
			categoryAxis.setTickLabelFont(ChartConstant.Default_Font);
		} else {
			categoryAxis.setLabelFont(categoryAxisLabel.getFont());
			categoryAxis.setTickLabelFont(categoryAxisLabel.getFont());
		}

		categoryAxis.setCategoryLabelPositions(CategoryLabelPositions
				.createDownRotationLabelPositions(Math.PI / 6));
		// categoryAxis.setVisible(false);
	}

	// 设置背景网格
	protected void doCategoryPlot(CategoryPlot plot, ChartPlotRender plotRender) {
		plot.setNoDataMessage(ReportUiConfig.NoDataMessage);
		plot.setNoDataMessageFont(ChartConstant.Default_Font);
		plot.setBackgroundPaint(Color.white);
		plot.setInsets(new RectangleInsets(0, 5, 5, 5));
		plot.setOutlinePaint(Color.black);
		/*// x轴
		plot.setRangeGridlinesVisible(true) ;
		plot.setRangeGridlinePaint(Color.decode("#E3E3E3")) ;
		plot.setRangeGridlineStroke(new BasicStroke(0.5f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND,0.5f,new float[]{5,5},0f)) ;

		// y轴
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.decode("#E3E3E3"));
		plot.setDomainGridlineStroke(new BasicStroke(0.5f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND,0.5f,new float[]{5,5},0f));
        plot.setDomainCrosshairVisible(true);*/
		
      	// 曲线
		// plot.setRangeGridlineStroke(new BasicStroke(1.0F, 1, 1,
		// 1.0F, new float[] { 6F, 6F }, 0.0F));
		
		plot.getDomainAxis().setVisible(false);
		org.jfree.chart.axis.CategoryAxis domainAxis = plot.getDomainAxis();
		// domainAxis.setLowerMargin(0.1);// 设置距离图片左端距离此时为10%
		// domainAxis.setUpperMargin(0.1);// 设置距离图片右端距离此时为百分之10
		// domainAxis.setCategoryLabelPositionOffset(120);// 图表横轴与标签的距离(10像素)
		// domainAxis.setCategoryMargin(1.0);// 横轴标签之间的距离20%
	}
}
