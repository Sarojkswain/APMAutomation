/**
 * 
 */
package com.ca.apm.systemtest.fld.server.rest;

/**
 * Exception class for Log Monitor Recipients REST controller's API.
 * 
 * @author TAVPA01
 *
 */
public class LogMonitorUserException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private ErrorCode errorCode;

	public enum ErrorCode {
		UnknownError, LogMonitorUserNotFound
	}

	/**
	 * 
	 */
	public LogMonitorUserException() {
	}

	/**
	 * @param message
	 */
	public LogMonitorUserException(String message) {
		this(ErrorCode.UnknownError, message);
	}

	/**
	 * @param message
	 */
	public LogMonitorUserException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}
}
