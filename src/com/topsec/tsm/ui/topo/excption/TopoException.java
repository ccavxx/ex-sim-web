package com.topsec.tsm.ui.topo.excption;

public class TopoException extends RuntimeException {
	public TopoException() {
		super();
	}

	public TopoException(String message) {
		super(message);
	}

	public TopoException(Throwable cause) {
		super(cause);
	}

	public TopoException(String message, Throwable cause) {
		super(message, cause);
	}
  
}
