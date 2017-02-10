package com.topsec.tsm.ui.topo.svg.elements;

public class Text extends BaseElement{
	private String fontSize="12";
	private String writingMode = "";
	public String getWritingMode() {
		return writingMode;
	}

	public void setWritingMode(String writingMode) {
		this.writingMode = writingMode;
	}

	private String style="font-family:'SimHei';text-anchor:middle;";



	public String getFontSize() {
		return fontSize;
	}

	public void setFontSize(String fontSize) {
		this.fontSize = fontSize;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

}
   