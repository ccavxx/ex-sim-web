package com.topsec.tsm.sim.common.exception;

/**
 * 通用用户自定义异常
 * @author hp
 *
 */
public class CommonUserException extends RuntimeException{

	private static final long serialVersionUID = -1562594936542275429L;

	public CommonUserException(String message){
		super(message) ;
	}
	
	public CommonUserException(String message,Throwable cause){
		super(message,cause) ;
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this ;
	}
	
}
