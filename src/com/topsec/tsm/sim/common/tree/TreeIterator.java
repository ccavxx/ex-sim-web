package com.topsec.tsm.sim.common.tree;

public interface TreeIterator {

	public <T extends Tree> VisitResult iterate(Tree<T> treeNode,TreeVisitor visitor) ;
	
}
