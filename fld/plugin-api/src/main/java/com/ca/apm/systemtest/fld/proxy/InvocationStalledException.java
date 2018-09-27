/**
 * 
 */
package com.ca.apm.systemtest.fld.proxy;

/**
 * @author KEYJA01
 *
 */
@SuppressWarnings("serial")
public class InvocationStalledException extends RuntimeException {

    /**
     * 
     */
    public InvocationStalledException() {
    }

    /**
     * @param message
     */
    public InvocationStalledException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public InvocationStalledException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public InvocationStalledException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public InvocationStalledException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
