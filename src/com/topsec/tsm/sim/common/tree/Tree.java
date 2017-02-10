package com.topsec.tsm.sim.common.tree;

import java.util.List;

/**
 * tree
 * @author hp
 *
 * @param <T>
 */
public interface Tree<T extends Tree<?>> {
	/**
	 * 返回树子节点
	 * @return
	 */
	public List<T> getChildren() ;
	/**
	 * 判断一个节点是否是叶子节点
	 * @return
	 */
	public boolean isLeaf() ;
	/**
	 * 当前节点所处级别，root为1
	 * @return
	 */
	public int getLevel() ;
	/**
	 * 获取上级节点
	 * @return
	 */
	public T getParent() ;
}
