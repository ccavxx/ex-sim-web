package com.topsec.tsm.sim.report.component;

import com.topsec.tsm.sim.report.common.SubjectVisitor;

public class TextSubject extends AbstractSubject {

	private String text ;
	
	public TextSubject(String text){
		this(text,0,0,0,0) ;
	}
	/**
	 * 
	 * @param text 文本内容
	 * @param width 宽度
	 * @param height 高度
	 */
	public TextSubject(String text,int width,int height) {
		this(text,0,0,width,height) ;
	}
	/**
	 * 
	 * @param text 文本内容
	 * @param x x坐标
	 * @param y y坐标
	 * @param width 宽度
	 * @param height 高度
	 */
	public TextSubject(String text,int x,int y,int width,int height) {
		super(x,y,width,height) ;
		this.text = text;
	}
	
	@Override
	public void accept(SubjectVisitor visitor) {
		visitor.visitTextSubject(this) ;
	}
	public void addChild(Subject child) {
		throw new UnsupportedOperationException() ;
	}
	public Subject getChildAt(int index) {
		throw new UnsupportedOperationException() ;
	}
	public int getChildCount() {
		throw new UnsupportedOperationException() ;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
}
