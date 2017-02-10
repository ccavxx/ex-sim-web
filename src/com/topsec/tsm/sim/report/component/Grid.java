package com.topsec.tsm.sim.report.component;

/**
 * 表格主题<br>
 * 表格的每个主题都是一个Row类型,表格中不能添加其它类型的主题<br>
 * 表格的每行之间有间隔，可以使用通过gap属性指定间隔的大小默认gap取值5<br>
 * @author hp
 *
 */
public class Grid extends Container {
	
	/**
	 * 行与行之间的间距,默认为5像素
	 */
	private int gap = 5;
	/**
	 * 行高,默认为300
	 */
	private int rowHeight = 300 ;
	/**
	 * 创建宽度为0,高度为0,行间距为5的表格
	 */
	public Grid() {
		super();
	}
	/**
	 * 创建一个指定宽度,指定高度,行中间为5的表格
	 * @param width 宽度
	 * @param height 高度
	 */
	public Grid(int width,int height) {
		this(width,height,5) ;
	}
	/**
	 * 创建指定宽度,指定高度,指定行中间的表格
	 * @param width 宽度
	 * @param height 高度
	 * @param gap 行间距
	 */
	public Grid(int width,int height,int gap) {
		setWidth(width) ;
		setHeight(height) ;
		this.gap = gap ;
	}
	/**
	 * 创建宽度为0,高度为0,行间距为指定间距的表格
	 * @param gap 行间距
	 */
	public Grid(int gap) {
		this(0,0,5) ;
	}

	public void addChild(Subject child) {
		if(child instanceof Row){
			int y =0 ;
			for(int i=0;i<getChildCount();i++){
				y=y+getChildAt(i).getHeight() + gap ;
			}
			child.setX(0) ;
			child.setY(y) ;
			super.addChild(child);
		}else{
			throw new RuntimeException("表格的中只能添加Row对象") ;
		}
	}
	
	public int getGap() {
		return gap;
	}
	public void setGap(int gap) {
		this.gap = gap;
	}
	public int getRowHeight() {
		return rowHeight;
	}
	public void setRowHeight(int rowHeight) {
		this.rowHeight = rowHeight;
	}
}
