package com.dbms.util.exceptions;

public class ReportGenerationException extends Exception {

	private static final long serialVersionUID = 1078085067463737860L;

	public ReportGenerationException() {
		super();
	}

	public ReportGenerationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ReportGenerationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReportGenerationException(String message) {
		super(message);
	}

	public ReportGenerationException(Throwable cause) {
		super(cause);
	}
}
