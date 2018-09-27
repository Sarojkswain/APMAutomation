package com.ca.apm.systemtest.fld.plugin;

import java.text.MessageFormat;

/**
 * Common Java Delegate exception class.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class JavaDelegateException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -3168049369661826489L;

    private String errorCode = null;

    /**
     * 
     */
    public JavaDelegateException() {
    }

    /**
     * 
     * @param msg
     */
    public JavaDelegateException(String msg) {
        super(msg);
    }

    /**
     * 
     * @param msg
     * @param errorCode
     */
    public JavaDelegateException(String msg, String errorCode) {
        super(msg);
        this.errorCode = errorCode;
    }

    /**
     * 
     * @param errorCode
     * @param pattern
     * @param arguments
     */
    public JavaDelegateException(String errorCode, String pattern, Object...arguments) {
        this(null, errorCode, pattern, arguments);
    }

    /**
     * 
     * @param ex
     * @param errorCode
     * @param pattern
     * @param arguments
     */
    public JavaDelegateException(Throwable ex, String errorCode, String pattern, Object...arguments) {
        super(MessageFormat.format(pattern, arguments), ex);
        this.errorCode = errorCode;
    }

    /**
     * 
     * @param ex
     */
    public JavaDelegateException(Throwable ex) {
        super(ex);
    }

    /**
     * 
     * @param ex
     * @param errorCode
     */
    public JavaDelegateException(Throwable ex, String errorCode) {
        super(ex);
        this.errorCode = errorCode;
    }

    /**
     * 
     * @param msg
     * @param ex
     */
    public JavaDelegateException(String msg, Throwable ex) {
        super(msg, ex);
    }

    /**
     * 
     * @param msg
     * @param ex
     * @param errorCode
     */
    public JavaDelegateException(String msg, Throwable ex, String errorCode) {
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
