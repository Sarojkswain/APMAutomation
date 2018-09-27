/**
 *
 */

package com.ca.apm.systemtest.fld.tailer.logmonitor;

/**
 * @author KEYJA01
 */
@SuppressWarnings("serial")
public class LogMonitorException extends RuntimeException {

    /**
     *
     */
    public LogMonitorException() {
    }

    /**
     * @param message
     */
    public LogMonitorException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public LogMonitorException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public LogMonitorException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public LogMonitorException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
