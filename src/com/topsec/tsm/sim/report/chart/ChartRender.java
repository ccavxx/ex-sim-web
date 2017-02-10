package com.topsec.tsm.sim.report.chart;

import java.awt.Color;

public class ChartRender {
	private ChartTitleRender title;
	private ChartAxisLabelRender categoryAxisLabel;
	private ChartAxisLabelRender valueAxisLabel;
	private ChartPlotRender plot;
	private boolean c3D;
	private ChartLegendRender lenged;
	private Color backgroundColor = new Color(240,250,248);
	private String type;
	private String orientation;
	private boolean tooltips;
	private boolean urls;
	private int width = 400;
	private int hight = 200;
	private String x;
	private String y;
	private String serise;
	private String url;
	private Color backgroundColor1 = new Color(216,216,216);
	private String backgroundColorOrientation="VERTICAL";
	private String chartUrl; //rick
	public String getBackgroundColorOrientation() {
		return backgroundColorOrientation;
	}

	public void setBackgroundColorOrientation(String backgroundColorOrientation) {
		this.backgroundColorOrientation = backgroundColorOrientation;
	}

	public String getHexBackgroundColor() {
		return ChartUtil.getHexColor(this.backgroundColor);
	}

	public String getHexBackgroundColor1() {
		return ChartUtil.getHexColor(this.backgroundColor1);
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHight() {
		return hight;
	}

	public void setHight(int hight) {
		this.hight = hight;
	}

	public ChartTitleRender getTitle() {
		return title;
	}

	public void setTitle(ChartTitleRender title) {
		this.title = title;
	}

	public ChartAxisLabelRender getCategoryAxisLabel() {
		return categoryAxisLabel;
	}

	public void setCategoryAxisLabel(ChartAxisLabelRender categoryAxisLabel) {
		this.categoryAxisLabel = categoryAxisLabel;
	}

	public ChartAxisLabelRender getValueAxisLabel() {
		return valueAxisLabel;
	}

	public void setValueAxisLabel(ChartAxisLabelRender valueAxisLabel) {
		this.valueAxisLabel = valueAxisLabel;
	}

	public boolean isC3D() {
		return c3D;
	}

	public void setC3D(boolean c3d) {
		c3D = c3d;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public ChartLegendRender getLenged() {
		return lenged;
	}

	public void setLenged(ChartLegendRender lenged) {
		this.lenged = lenged;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOrientation() {
		return orientation;
	}

	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}

	public boolean isTooltips() {
		return tooltips;
	}

	public void setTooltips(boolean tooltips) {
		this.tooltips = tooltips;
	}

	public boolean isUrls() {
		return urls;
	}

	public void setUrls(boolean urls) {
		this.urls = urls;
	}

	public String getX() {
		return x;
	}

	public void setX(String x) {
		this.x = x;
	}

	public String getY() {
		return y;
	}

	public void setY(String y) {
		this.y = y;
	}

	public String getSerise() {
		return serise;
	}

	public void setSerise(String serise) {
		this.serise = serise;
	}

	public ChartPlotRender getPlot() {
		return plot;
	}

	public void setPlot(ChartPlotRender plot) {
		this.plot = plot;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Color getBackgroundColor1() {
		return backgroundColor1;
	}

	public void setBackgroundColor1(Color backgroundColor1) {
		this.backgroundColor1 = backgroundColor1;
	}

	public String getChartUrl() {
		return chartUrl;
	}

	public void setChartUrl(String chartUrl) {
		this.chartUrl = chartUrl;
	}
}
