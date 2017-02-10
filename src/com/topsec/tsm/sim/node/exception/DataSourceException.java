package com.topsec.tsm.sim.node.exception;

public class DataSourceException extends RuntimeException{

	public DataSourceException(){
	}
	
	public DataSourceException(String message){
		super(message) ;
	}
	public DataSourceException(String message,Throwable cause){
		super(message,cause) ;
	}
}
