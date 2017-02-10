package com.topsec.tsm.sim.common.tree;

/**
 * tree遍历监听器
 * 在每一个树节点遍历完成后会通知已经注册的监听器<br>
 * 这样可以让客户端有机会去修改生成的result对象
 * @author hp
 *
 */
public interface VisitResultListener<R extends VisitResult,T extends Tree> {

	public void onResult(R result,T tree) ;
	
}
