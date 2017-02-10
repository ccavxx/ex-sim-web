package com.topsec.tsm.sim.report.component;

import java.util.ArrayList;
import java.util.List;

import com.topsec.tsm.sim.report.common.SubjectVisitor;


/**
 * 报表主题组件容器
 * @author hp
 *
 */
public class Container extends AbstractSubject {

	private List<Subject> childList = new ArrayList<Subject>();

	/**
	 * 添加一个主题到主题容器
	 * 主题被添加到容器后会根据容器的全局坐标重新计算主题的全局坐标
	 * @param child
	 */
	public void addChild(Subject child) {
		if(child==null){
			throw new NullPointerException() ;
		}
		child.setParent(this) ;
		child.setGlobalX(getGlobalX()+child.getX()) ;
		child.setGlobalY(getGlobalY()+child.getY()) ; 
		childList.add(child) ;
	}

	/**
	 * 根据容器的globalX坐标，调整所有的子主题的globalX坐标
	 */
	@Override
	public void setGlobalX(int globalX) {
		super.setGlobalX(globalX) ;
		for(Subject child:getChild()){
			child.setGlobalX(globalX+child.getX()) ;
		}
	}
	/**
	 * 根据容器的globalY坐标，调整所有的子主题的globalY坐标
	 */
	@Override
	public void setGlobalY(int globalY) {
		super.setGlobalY(globalY) ;
		for(Subject child:getChild()){
			child.setGlobalY(globalY+child.getY()) ;
		}
	}

	public Subject getChildAt(int index) {
		if(index<0||index>=childList.size()){
			return null ;
		}
		return childList.get(index) ;
	}

	public int getChildCount() {
		return childList.size();
	}

	@Override
	public void accept(SubjectVisitor visitor) {
		for(Subject sub:childList){
			sub.accept(visitor) ;
		}
	}
	public List<Subject> getChild(){
		return childList ;
	}

}
