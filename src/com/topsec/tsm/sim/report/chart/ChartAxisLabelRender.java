package com.topsec.tsm.sim.report.chart;

import java.awt.Font;

public class ChartAxisLabelRender {
	private String name;
	private Font font;
	private boolean show;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
