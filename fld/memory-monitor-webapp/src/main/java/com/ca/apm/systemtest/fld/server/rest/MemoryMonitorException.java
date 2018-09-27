package com.ca.apm.systemtest.fld.server.rest;

public class MemoryMonitorException extends RuntimeException {

    private static final long serialVersionUID = 3242012805096555526L;

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
