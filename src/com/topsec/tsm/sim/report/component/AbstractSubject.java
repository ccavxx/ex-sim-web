package com.topsec.tsm.sim.report.component;

import com.topsec.tsm.sim.report.common.SubjectVisitor;

/**
 * 抽象主题
 * @author hp
 *
 */
public abstract class AbstractSubject implements Subject {

	private int x ;
	private int y ;
	private int height ;
	private int width ;
	private int globalX ;
	private int globalY ;
	protected Container parent ;
	public AbstractSubject() {
		super();
	}
	public AbstractSubject(int x, int y,int width,int height ) {
		this.x = x;
		this.y = y;
		this.height = height;
		this.width = width;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getHeight() {
		return height;
	}
	public int getGlobalX() {
		return globalX;
	}
	public void setGlobalX(int globalX) {
		this.globalX = globalX;
	}
	public int getGlobalY() {
		return globalY;
	}
	public void setGlobalY(int globalY) {
		this.globalY = globalY;
	}
	
	public abstract void accept(SubjectVisitor visitor) ;
	@Override
	public String toString() {
		return "("+globalX+","+globalY+","+width+","+height+")";
	}
	public Container getParent() {
		return parent;
	}
	
	public void setParent(Container parent){
		this.parent = parent;
	}
}
