package com.ca.apm.systemtest.fld.server.rest;

public class NetworkTrafficMonitorException extends RuntimeException {

    private static final long serialVersionUID = -1192066277259452488L;

    private ErrorCode errorCode;

    public enum ErrorCode {
        InvalidParameter, DataNotFound, UnknownError;
    }

    public NetworkTrafficMonitorException() {}

    public NetworkTrafficMonitorException(String message) {
        this(ErrorCode.UnknownError, message);
    }

    public NetworkTrafficMonitorException(ErrorCode errorCode, String message) {
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
