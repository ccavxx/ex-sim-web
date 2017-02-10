package com.topsec.tsm.ui.topo.svg.elements;

public class PolyLine extends BaseElement{
	private String points;
	private String stroke="red";
	private String strokeWidth="2";
	private String fill="none";
	private String markerEnd;
	private String markerStart;
	public String getPoints() {
		return points;
	}
	public void setPoints(String points) {
		this.points = points;
	}
	public String getMarkerEnd() {
		return markerEnd;
	}
	public void setMarkerEnd(String markerEnd) {
		this.markerEnd = markerEnd;
	}  
	public String getMarkerStart() {
		return markerStart;
	}
	public void setMarkerStart(String markerStart) {
		this.markerStart = markerStart;
	}
	public String getStroke() {
		return stroke;
	}
	public void setStroke(String stroke) {
		this.stroke = stroke;
	}
	public String getStrokeWidth() {
		return strokeWidth;
	}
	public void setStrokeWidth(String strokeWidth) {
		this.strokeWidth = strokeWidth;
	}
	public String getFill() {
		return fill;
	}
	public void setFill(String fill) {
		this.fill = fill;
	}
	
}
