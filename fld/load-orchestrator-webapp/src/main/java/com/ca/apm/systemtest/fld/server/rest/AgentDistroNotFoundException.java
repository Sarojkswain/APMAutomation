/**
 * 
 */
package com.ca.apm.systemtest.fld.server.rest;

/**
 * @author KEYJA01
 *
 */
@SuppressWarnings("serial")
public class AgentDistroNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	public AgentDistroNotFoundException() {
	}

	/**
	 * @param message
	 */
	public AgentDistroNotFoundException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public AgentDistroNotFoundException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public AgentDistroNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public AgentDistroNotFoundException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
