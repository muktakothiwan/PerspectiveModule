package com.vaspsolutions.analytics.common;

public class MODIAServiceUnavailableException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MODIAServiceUnavailableException() {
	}

	public MODIAServiceUnavailableException(String message) {
		super(message);
	}

	public MODIAServiceUnavailableException(Throwable cause) {
		super(cause);
	}

	public MODIAServiceUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

	public MODIAServiceUnavailableException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
