package com.topsec.tsm.sim.report.component;

/**
 * 行主题<br>
 * 行中的每一个主题都是一个Column类型的主题,不能添加其它类型的主题<br>
 * @author hp
 *
 */
public class Row extends Container {

	/**
	 * 列与列之间的间距
	 */
	private int gap = 5 ;
	
	public Row() {}
	/**
	 * 创建指定高度的行
	 * @param height 行高
	 */
	public Row(int height) {
		setHeight(height) ;
	}
	/**
	 * 创建指定行高指定间距的行
	 * @param height 行高
	 * @param gap 列间距
	 */
	public Row(int height,int gap) {
		setHeight(height) ;
		this.gap = gap;
	}
	@Override
	public void addChild(Subject child) {
		if(child instanceof Column){
			int x =0 ;
			for(int i=0;i<getChildCount();i++){
				x=x+getChildAt(i).getWidth() + gap ;
			}
			child.setX(x) ;
			child.setY(0) ;//相对Grid的开始
			super.addChild(child);
		}else{
			throw new RuntimeException("行中只能添加Column对象") ;
		}
	}
	public int getGap() {
		return gap;
	}
	public void setGap(int gap) {
		this.gap = gap;
	}
	
}
