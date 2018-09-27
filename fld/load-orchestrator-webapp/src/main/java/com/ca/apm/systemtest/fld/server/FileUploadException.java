package com.ca.apm.systemtest.fld.server;

public class FileUploadException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -2285901702512272850L;

    public FileUploadException() {
        this(null);
    }
    
    public FileUploadException(String message) {
        this(message, null);
    }
    
    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }

}
