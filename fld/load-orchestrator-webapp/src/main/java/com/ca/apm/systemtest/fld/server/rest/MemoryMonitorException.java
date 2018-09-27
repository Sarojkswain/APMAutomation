
package com.ca.apm.systemtest.fld.server.rest;

@SuppressWarnings("serial")
public class MemoryMonitorException extends RuntimeException {
    private ErrorCode errorCode;

    public enum ErrorCode {
        UnknownError, ImageNotFound, InvalidParameter
    }

    public MemoryMonitorException() {}

    public MemoryMonitorException(String message) {
        this(ErrorCode.UnknownError, message);
    }

    public MemoryMonitorException(ErrorCode errorCode, String message) {
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
