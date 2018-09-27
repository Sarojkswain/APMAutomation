package com.ca.apm.systemtest.fld.server.util;

public class BpmnParseError extends RuntimeException {

    private static final long serialVersionUID = 3353636544967320152L;

    public BpmnParseError() {
        super();
    }
    
    public BpmnParseError(String message) {
        super(message);
    }
    
    public BpmnParseError(String message, Throwable throwable) {
        super(message, throwable);
    }
    
}
