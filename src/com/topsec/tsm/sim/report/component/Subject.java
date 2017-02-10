package com.topsec.tsm.sim.report.component;

import com.topsec.tsm.sim.report.common.SubjectVisitor;

/**
 * 报表主题组件
 * 每一个报表中的组件都要实现此接口
 * 整个报表也可以视为一个大的主题
 * @author hp
 *
 */
public interface Subject {

	
	/**
	 * 设置组件的宽度
	 * @param width
	 */
	public void setWidth(int width) ;
	/**
	 *设置组件的高度
	 */
	public void setHeight(int height) ;
	/**
	 * 获得组件的宽度
	 * @return
	 */
	public int getWidth() ;
	
	/**
	 * 获得组件的高度
	 * @return
	 */
	public int getHeight() ;
	/**
	 * 设置组件所在的轴坐标
	 * @param x
	 */
	public void setX(int x) ;
	/**
	 * 设置组件所在的Y轴坐标
	 * @param y
	 */
	public void setY(int y) ;
	/**
	 * 获得组件x轴坐标
	 * @return
	 */
	public int getX() ;
	/**
	 * 获得组件y轴坐标
	 * @return
	 */
	public int getY() ;
	/**
	 * 访问主题组件
	 * @param visitor
	 */
	public void accept(SubjectVisitor visitor);
	/**
	 * 设置全局X坐标
	 * @return
	 */
	public void setGlobalX(int x) ;
	/**
	 * 设置全局Y坐标
	 * @return
	 */
	public void setGlobalY(int y) ;
	/**
	 * 获得全局Y坐标
	 * @return
	 */
	public int getGlobalX() ;
	/**
	 * 获得全局Y坐标
	 * @return
	 */
	public int getGlobalY() ;
	/**
	 * 获得组件的父组件
	 * @return
	 */
	public Container getParent() ;
	/**
	 * 设置主题容器
	 * @param parent
	 */
	public void setParent(Container parent) ;
}
