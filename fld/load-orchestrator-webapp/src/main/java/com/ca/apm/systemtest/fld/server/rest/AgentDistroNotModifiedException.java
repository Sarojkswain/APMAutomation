/**
 * 
 */
package com.ca.apm.systemtest.fld.server.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author KEYJA01
 *
 */
@SuppressWarnings("serial")
@ResponseStatus(value=HttpStatus.NOT_MODIFIED, reason="Agent distribution not modified")
public class AgentDistroNotModifiedException extends RuntimeException {

	/**
	 * 
	 */
	public AgentDistroNotModifiedException() {
	}

	/**
	 * @param message
	 */
	public AgentDistroNotModifiedException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public AgentDistroNotModifiedException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public AgentDistroNotModifiedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public AgentDistroNotModifiedException(String message, Throwable cause,	boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
