package com.topsec.tsm.sim.common.tree;

import java.util.ArrayList;
import java.util.Collection;


/**
 * 树属性访问对象 visitor
 * @author hp
 *
 */
public abstract class TreeFieldVisitor extends AbstractTreeVisitor implements TreeVisitor {
	

	private Collection<String> visitFields ;
	
	public TreeFieldVisitor() {
		super();
		visitFields = new ArrayList<String>() ;
	}

	public TreeFieldVisitor(Collection<String> visitFields) {
		super();
		this.visitFields = visitFields;
	}
	
	public void addField(String field){
		visitFields.add(field) ;
	}
	
	@Override
	public VisitResult visit(Tree treeNode) {
		FieldVisitResult result = createVisitResult() ;
		beforeVisitField(treeNode, result) ;
		for(String field:visitFields){
			result.visitField(field, treeNode) ;
		}
		afterVisitField(treeNode, result) ;
		return result ;
	}
	
	/**
	 * 创建VisitResult
	 * @return
	 */
	public abstract FieldVisitResult createVisitResult() ;
	/**
	 * 遍历树属性完成后通知
	 * @param tree
	 * @param result
	 */
	public void afterVisitField(Tree tree,VisitResult result){}
	/**
	 * 遍历树属性前通知
	 * @param tree
	 * @param result
	 */
	public void beforeVisitField(Tree tree,VisitResult result){}
}
