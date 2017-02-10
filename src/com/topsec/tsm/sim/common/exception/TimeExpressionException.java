package com.topsec.tsm.sim.common.exception;

public class TimeExpressionException extends Exception {

	private static final long serialVersionUID = 618767225840763597L;

	public TimeExpressionException(String message){
		super(message) ;
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}
	
}
