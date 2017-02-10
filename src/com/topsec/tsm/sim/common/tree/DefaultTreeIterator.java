package com.topsec.tsm.sim.common.tree;

import java.util.ArrayList;
import java.util.List;

import com.topsec.tal.base.util.ObjectUtils;

public class DefaultTreeIterator implements TreeIterator {
	private List<VisitResultListener> listeners = new ArrayList<VisitResultListener>() ;
	
	@Override
	public <T extends Tree> VisitResult iterate(Tree<T> treeNode,TreeVisitor visitor) {
		VisitResult result = visitor.visit(treeNode) ;
		if(ObjectUtils.isNotEmpty(treeNode.getChildren())){
			for(Tree<T> t:treeNode.getChildren()){
				result.addChildResult(iterate(t, visitor)) ;
			}
		}
		for(VisitResultListener listener:listeners){
			listener.onResult(result, treeNode) ;
		}
		return result ;
	}


	public void regist(VisitResultListener listener){
		listeners.add(listener) ;
	}
	
	public void unregist(VisitResultListener listener){
		if(listeners.contains(listener)){
			listeners.remove(listener) ;
		}
	}
}
