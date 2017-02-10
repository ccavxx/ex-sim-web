package com.topsec.tsm.sim.report.chart;

import java.awt.Color;

public class ChartPlotRender {
	private Color backgroundPaint;// 
	private boolean domainGridlinesVisible=false;//垂直道 
	private boolean rangeGridlinesVisible=true;//水平道
	private Color domainGridlinePaint=Color.WHITE;// 
	private Color rangeGridlinePaint=Color.WHITE;//
	
	public String getHexBgColor() {
	
		return ChartUtil.getHexColor(backgroundPaint);
	}
	public String getHexDomainGridlinePaint(){
		return ChartUtil.getHexColor(domainGridlinePaint);
	}
	public String getHexRangeGridlinePaint(){
		return ChartUtil.getHexColor(rangeGridlinePaint);
	}

	public Color getBackgroundPaint() {
		return backgroundPaint;
	}

	public void setBackgroundPaint(Color backgroundPaint) {
		this.backgroundPaint = backgroundPaint;
	}

	public boolean isDomainGridlinesVisible() {
		return domainGridlinesVisible;
	}

	public void setDomainGridlinesVisible(boolean domainGridlinesVisible) {
		this.domainGridlinesVisible = domainGridlinesVisible;
	}

	public boolean isRangeGridlinesVisible() {
		return rangeGridlinesVisible;
	}

	public void setRangeGridlinesVisible(boolean rangeGridlinesVisible) {
		this.rangeGridlinesVisible = rangeGridlinesVisible;
	}

	public Color getDomainGridlinePaint() {
		return domainGridlinePaint;
	}

	public void setDomainGridlinePaint(Color domainGridlinePaint) {
		this.domainGridlinePaint = domainGridlinePaint;
	}

	public Color getRangeGridlinePaint() {
		return rangeGridlinePaint;
	}

	public void setRangeGridlinePaint(Color rangeGridlinePaint) {
		this.rangeGridlinePaint = rangeGridlinePaint;
	}
}
