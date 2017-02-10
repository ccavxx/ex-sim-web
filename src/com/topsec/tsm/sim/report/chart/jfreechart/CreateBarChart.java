package com.topsec.tsm.sim.report.chart.jfreechart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.text.DecimalFormat;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.urls.StandardCategoryURLGenerator;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.jfree.ui.TextAnchor;

import com.topsec.tsm.sim.report.chart.ChartAxisLabelRender;
import com.topsec.tsm.sim.report.chart.ChartLegendRender;
import com.topsec.tsm.sim.report.chart.ChartPlotRender;
import com.topsec.tsm.sim.report.chart.ChartRender;
import com.topsec.tsm.sim.report.chart.ChartTitleRender;
import com.topsec.tsm.sim.report.model.ChartData;
import com.topsec.tsm.sim.report.util.ChartConstant;
import com.topsec.tsm.sim.report.util.ReportUiConfig;

public class CreateBarChart extends BaseCreate {

	static class CustomBarRenderer extends BarRenderer {

		private static final long serialVersionUID = 1L;

		private Paint[] colors;

		public CustomBarRenderer(Paint[] colors) {
			this.colors = colors;
		}

		public Paint getItemPaint(int row, int column) {
			this.setSeriesPaint(row, this.colors[column % this.colors.length]);
			return this.colors[column % this.colors.length];
		}

	}

	static class CustomBarRenderer3D extends BarRenderer3D {

		private Paint[] colors;

		public CustomBarRenderer3D(Paint[] colors) {
			this.colors = colors;
		}

		public Paint getItemPaint(int row, int column) {
			this.setSeriesPaint(row, this.colors[column % this.colors.length]);
			return this.colors[column % this.colors.length];
		}
	}

	public Object createChart(List<ChartData> data, ChartRender render, HttpServletRequest request, HttpServletResponse response) throws Exception {

		Object result = null;
		JFreeChart chart = null;
		ChartTitleRender title = render.getTitle();
		ChartAxisLabelRender categoryAxisLabel = render.getCategoryAxisLabel();
		ChartAxisLabelRender valueAxisLabel = render.getValueAxisLabel();
		ChartLegendRender legend = render.getLenged();
		ChartPlotRender plotRender = render.getPlot();

		PlotOrientation orientation = PlotOrientation.VERTICAL;

		if (render.getOrientation() != null && render.getOrientation().equals(ChartConstant.PlotOrientation_HORIZONTAL)) {
			orientation = PlotOrientation.HORIZONTAL;
		}

		String _title = title.isShow() ? title.getTitle() : "";

		String categoryAxisLabelName = categoryAxisLabel.isShow() ? categoryAxisLabel.getName() : "";

		String valueAxisLabelName = valueAxisLabel.isShow() ? valueAxisLabel.getName() : "";

		chart = ChartGraphFactory.createBarChart(render.isC3D(), _title, categoryAxisLabelName, valueAxisLabelName, createCategoryDataset(data), orientation, legend.isShow(), render.isTooltips(), render.isUrls());

		if (chart == null) {
			return null;
		}

		/*
		 * chart.setBackgroundPaint(new GradientPaint(0f, 0f, render
		 * .getBackgroundColor(), 350f, 0f, Color.white, true));
		 */

		if (render.getBackgroundColorOrientation() != null && render.getBackgroundColorOrientation().equals(ChartConstant.PlotOrientation_HORIZONTAL)) {
			chart.setBackgroundPaint(new GradientPaint(0f, 0f, render.getBackgroundColor(), render.getWidth(), 0f, render.getBackgroundColor1(), true));
		} else {
			chart.setBackgroundPaint(new GradientPaint(0f, 0f, render.getBackgroundColor(), 0f, render.getHight(), render.getBackgroundColor1(), true));
		}

		CategoryPlot plot = (CategoryPlot) chart.getPlot();

		doCategoryPlot(plot, plotRender);
		// x轴
		plot.setRangeGridlinesVisible(true) ;
		plot.setRangeGridlinePaint(Color.decode("#E3E3E3")) ;
		plot.setRangeGridlineStroke(new BasicStroke(0.5f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND,0.5f,new float[]{5,5},0f)) ;

		if (render.isC3D()) {
			CustomBarRenderer3D renderer = new CustomBarRenderer3D(createPaint1());
			// 柱子上面的数字 start
			// renderer.setBaseItemLabelGenerator(new
			// StandardCategoryItemLabelGenerator());
			// 需要显示精确数值
			DecimalFormat decimalformat1 = new DecimalFormat("#.#######");
			StandardCategoryItemLabelGenerator ss = new StandardCategoryItemLabelGenerator("{2}", decimalformat1);
			renderer.setBaseItemLabelGenerator(ss);
			renderer.setBaseItemLabelsVisible(false);

			// renderer.setItemLabelAnchorOffset(10.0);
			// Lable 位置越大越向下，面对屏幕内容相对与柱子的(左右), 内容旋转的位置(),Lable旋转的角度

			ItemLabelPosition p1 = (new ItemLabelPosition(ItemLabelAnchor.OUTSIDE3, TextAnchor.TOP_LEFT, TextAnchor.TOP_LEFT, -1.57D));
			renderer.setBasePositiveItemLabelPosition(p1);

			ItemLabelPosition p2 = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE3, TextAnchor.TOP_CENTER, TextAnchor.TOP_CENTER, -1.57D);
			renderer.setPositiveItemLabelPositionFallback(p2);

			plot.setRenderer(renderer);
			// 柱子上面的数字 End

			// Rick 解決顏色圖例不對應的bug
			for (int i = 0; i < createPaint1().length; i++) {
				((BarRenderer) (plot.getRenderer())).setSeriesPaint(i, createPaint1()[i]);
			}

		} else {
			GradientPaintTransformType ft = GradientPaintTransformType.CENTER_HORIZONTAL;

			if (render.getOrientation() != null && render.getOrientation().equals(ChartConstant.PlotOrientation_HORIZONTAL)) {
				ft = GradientPaintTransformType.CENTER_VERTICAL;
			}

			CustomBarRenderer renderer = new CustomBarRenderer(createPaint());

			renderer.setGradientPaintTransformer(new StandardGradientPaintTransformer(ft));

			renderer.setDrawBarOutline(false);

			renderer.setShadowXOffset(1);

		}
		CategoryAxis categoryAxis = plot.getDomainAxis();
		doCategoryAxis(categoryAxis, categoryAxisLabel);

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		doNumberAxis(rangeAxis, valueAxisLabel);

		if (legend.isShow()) {
			chart.getLegend().setItemFont(legend.getFont());
		}
		// set title
		doTitle(chart, title, render);

		// rick end
		LineAndShapeRenderer lineandshaperenderer = new LineAndShapeRenderer();
		lineandshaperenderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());

		plot.getRenderer().setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());

		// 柱子图title只显示一个 NumberFormat.getInstance()最好加上单位
		// plot.getRenderer().setBaseToolTipGenerator(
		// new StandardCategoryToolTipGenerator("[{0}]= {2}", NumberFormat
		// .getInstance()));

		// plot.getRenderer().setBaseToolTipGenerator(
		// new StandardCategoryToolTipGenerator("[{0}]= {2}", new
		// DecimalFormat("#,###.00")));
		//		

		// // " [{0},{1}] = {2} ",new DecimalFormat("￥#,###.00")
		// // NumberFormat.getInstance()));
		plot.getRenderer().setBaseToolTipGenerator(new StandardCategoryToolTipGenerator("({0})= {2}", new DecimalFormat("#.#######")));

		// Rick 报表下探
		if (render.getChartUrl() != null) {
			plot.getRenderer().setBaseItemURLGenerator(new StandardCategoryURLGenerator(render.getChartUrl(), ReportUiConfig.talCategory2, ReportUiConfig.talCategory));
		}

		// rick end
		// rick 最大柱子允许0.1
		((BarRenderer) plot.getRenderer()).setMaximumBarWidth(0.1);
		
		plot.getRangeAxis().setLowerBound(0) ;

		// plot.setAxisOffset(new RectangleInsets(1D, 1D, 1D, 1D));
		// //设定坐标轴与图表数据显示部分距离

		// 柱子上面的字跟框的距离
		// plot.getRangeAxis().setUpperMargin(0.15);
		// plot.setAxisOffset(new RectangleInsets(1D, 1D, 1D, 1D));
		// //设定坐标轴与图表数据显示部分距离
		//
		// plot.setOutlinePaint(Color.RED);//设置数据区的边界线条颜色
		// plot.getRangeAxis().setUpperMargin(1.0);

		// plot.setAxisOffset(new RectangleInsets(10D, 10D, 10D, 10D));
		// rick

		// chart.getTitle().setExpandToFitSpace(true);
		// chart.getTitle().setHeight(10d);
		// chart.getTitle().setWidth(10d);
		// chart.getTitle().setMargin(0.1d, 0.1d, 0.1d, 0.1d);
		// chart.getTitle().setMaximumLinesToDisplay(1);
		// chart.getTitle().setPadding(1d, 1d, 1d, 1d);
		//		 
		// chart.getTitle().setMargin(new RectangleInsets(2D, 2D, 2D, 2D));
		// chart.getTitle().setBorder(new BlockBorder());

		//BarRenderer3D barRenderer3D = (BarRenderer3D) plot.getRenderer();
		//barRenderer3D.setDrawBarOutline(true);
		//barRenderer3D.setBaseOutlinePaint(Color.white);
		plot.setNoDataMessageFont(ChartConstant.FONT);
		plot.setOutlineVisible(false);

		result = generateImgName(chart, render, request);
		// 设置地区、销量的显示位置
		// plot.setDomainAxisLocation(AxisLocation.TOP_OR_RIGHT);
		// plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
		//	

		return result;
	}
}
