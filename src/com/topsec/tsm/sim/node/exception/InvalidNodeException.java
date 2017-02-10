package com.topsec.tsm.sim.node.exception;

/**
 * 无效的结点异常
 * @author hp
 *
 */
public class InvalidNodeException extends RuntimeException {

	public InvalidNodeException(){
		super() ;
	}
	
	public InvalidNodeException(String nodeId){
		super("Invalid node id:"+nodeId) ;
	}
}
