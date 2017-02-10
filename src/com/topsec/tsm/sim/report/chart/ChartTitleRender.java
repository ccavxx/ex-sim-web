package com.topsec.tsm.sim.report.chart;


import java.awt.Color;
import java.awt.Font;


public class ChartTitleRender {
	private String title;
	boolean show;
	private Color bgColor;
	private Font font;
	
	public String getHexBgColor() {
		return ChartUtil.getHexColor(bgColor);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Color getBgColor() {
		return bgColor;
	}

	public void setBgColor(Color bgColor) {
		this.bgColor = bgColor;
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public boolean isShow() {
		return show;
	}

	public void setShow(boolean show) {
		this.show = show;
	}

}
