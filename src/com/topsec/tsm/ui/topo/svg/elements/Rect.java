package com.topsec.tsm.ui.topo.svg.elements;

import java.util.HashSet;
import java.util.Set;

public class Rect extends BaseElement {
	private String fill = "url(#greenGradient)";
	private String stroke = "#bd4d02";
//	private String stroke = "black";
	private String strokeDasharray;
	private String strokeWidth = "2";
	private String rx = "5";
	private String ry = "5";
	private String nodeId;
	private String fillOpacity = "0";

	public String getFillOpacity() {
		return fillOpacity;
	}

	public void setFillOpacity(String fillOpacity) {
		this.fillOpacity = fillOpacity;
	}

	private Set childs = new HashSet();

	private Set images = new HashSet();

	private Text text = new Text();

	public Set getImages() {
		return images;
	}

	public void setImages(Set images) {
		this.images = images;
	}

	public String getFill() {
		return fill;
	}  

	public void setFill(String fill) {
		this.fill = fill;
	}

	public String getStroke() {
		return stroke;
	}

	public void setStroke(String stroke) {
		this.stroke = stroke;
	}

	public String getStrokeDasharray() {
		return strokeDasharray;
	}

	public void setStrokeDasharray(String strokeDasharray) {
		this.strokeDasharray = strokeDasharray;
	}

	public String getStrokeWidth() {
		return strokeWidth;
	}

	public void setStrokeWidth(String strokeWidth) {
		this.strokeWidth = strokeWidth;
	}

	public Set getChilds() {
		return childs;
	}

	public void setChilds(Set childs) {
		this.childs = childs;
	}

	public Text getText() {
		return text;
	}

	public void setText(Text text) {
		this.text = text;
	}

	public String getRx() {
		return rx;
	}

	public void setRx(String rx) {
		this.rx = rx;
	}

	public String getRy() {
		return ry;
	}

	public void setRy(String ry) {
		this.ry = ry;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

}
