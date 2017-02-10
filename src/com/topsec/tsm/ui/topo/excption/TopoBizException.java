package com.topsec.tsm.ui.topo.excption;

public class TopoBizException extends TopoException {
	private static final long serialVersionUID = 1L;

	public TopoBizException() {
		super();
	}

	public TopoBizException(String message) {
		super(message);
	}

	public TopoBizException(Throwable cause) {
		super(cause);
	}

	public TopoBizException(String message, Throwable cause) {
		super(message, cause);
	}  

}
