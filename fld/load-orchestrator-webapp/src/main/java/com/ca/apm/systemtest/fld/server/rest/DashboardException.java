/**
 *
 */
package com.ca.apm.systemtest.fld.server.rest;

/**
 * @author KEYJA01
 */
@SuppressWarnings("serial")
public class DashboardException extends RuntimeException {
    private ErrorCode errorCode;

    public enum ErrorCode {
        UnknownError, DashboardNotFound, InvalidProcessState, InvalidParameter, ProcessNotFound
    }

    /**
     * Default constructor.
     */
    public DashboardException() {
    }

    /**
     * Constructor.
     * 
     * @param message  error message
     */
    public DashboardException(String message) {
        this(ErrorCode.UnknownError, message);
    }

    /**
     * Constructor. 
     * 
     * @param errorCode error code
     * @param message   error message
     * 
     */
    public DashboardException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    /**
     * Constructor.
     * 
     * @param errorCode  error code 
     * @param message    error message 
     * @param cause      error cause
     */
    public DashboardException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }


}
