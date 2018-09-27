/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin;

import java.text.MessageFormat;

/**
 * 
 * Parent exception class for Load Orchestrator plugins.
 * 
 * @author keyja01
 *
 */
public class RemoteCallException extends RuntimeException {

    private static final long serialVersionUID = 1392007394600531429L;
    
    private String errorCode = null;

    /**
	 * 
	 */
	public RemoteCallException() {
	}

	/**
	 * 
	 * @param msg
	 */
	public RemoteCallException(String msg) {
        super(msg);
    }

    /**
     * 
     * @param msg
     * @param errorCode
     */
    public RemoteCallException(String msg, String errorCode) {
        super(msg);
        this.errorCode = errorCode;
    }

    /**
     * 
     * @param errorCode
     * @param pattern
     * @param arguments
     */
    public RemoteCallException(String errorCode, String pattern, Object...arguments) {
        this(null, errorCode, pattern, arguments);
    }

    /**
     * 
     * @param ex
     * @param errorCode
     * @param pattern
     * @param arguments
     */
    public RemoteCallException(Throwable ex, String errorCode, String pattern, Object...arguments) {
        super(MessageFormat.format(pattern, arguments), ex);
        this.errorCode = errorCode;
    }

    /**
     * 
     * @param ex
     */
    public RemoteCallException(Throwable ex) {
        super(ex);
    }

    /**
     * 
     * @param ex
     * @param errorCode
     */
    public RemoteCallException(Throwable ex, String errorCode) {
        super(ex);
        this.errorCode = errorCode;
    }

    /**
     * 
     * @param msg
     * @param ex
     */
    public RemoteCallException(String msg, Throwable ex) {
        super(msg, ex);
    }

    /**
     * 
     * @param msg
     * @param ex
     * @param errorCode
     */
    public RemoteCallException(String msg, Throwable ex, String errorCode) {
        super(msg, ex);
        this.errorCode = errorCode;
    }

    /**
     * 
     * @return
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 
     * @param errorCode
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

}
