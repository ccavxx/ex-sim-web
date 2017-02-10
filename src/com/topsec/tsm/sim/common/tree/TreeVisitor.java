package com.topsec.tsm.sim.common.tree;

public interface TreeVisitor{
	
	/**
	 * 
	 * @param treeNode
	 * @return
	 */
	public VisitResult visit(Tree treeNode) ;
}
