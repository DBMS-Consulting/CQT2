package com.dbms.util.exceptions;

public class CqtServiceException extends Exception {

	private static final long serialVersionUID = 1078085067463737860L;

	public CqtServiceException() {
		super();
	}

	public CqtServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CqtServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public CqtServiceException(String message) {
		super(message);
	}

	public CqtServiceException(Throwable cause) {
		super(cause);
	}
}
