package com.topsec.tsm.sim.node.exception;

/**
 * 组件配置异常
 * @author hp
 *
 */
public class ComponentConfigException extends RuntimeException {

	
	public ComponentConfigException(){
		super() ;
	}
	
	public ComponentConfigException(String message){
		super(message) ;
	}
	
	public ComponentConfigException(Throwable e){
		super(e) ;
	}
	
	public ComponentConfigException(String message,Throwable cause){
		super(message,cause) ;
	}
}
