package com.topsec.tsm.sim.common.tree;

/**
 * 树节点属性访问
 * @author hp
 *
 */
public interface FieldVisitResult extends VisitResult{

	public void visitField(String field,Tree node) ;
}
