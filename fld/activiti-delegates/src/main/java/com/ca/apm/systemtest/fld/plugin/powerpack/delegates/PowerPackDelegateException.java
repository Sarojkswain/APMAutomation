package com.ca.apm.systemtest.fld.plugin.powerpack.delegates;

import com.ca.apm.systemtest.fld.plugin.JavaDelegateException;

/**
 * Common exception class for PowerPack Java Delegates.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class PowerPackDelegateException extends JavaDelegateException {

    public static final String ERR_UNKNOWN_TEST_TYPE = "ERR_UNKNOWN_TEST_TYPE";
    public static final String ERR_AGENT_EM_CONNECTION_CHECK_FAILED = "ERR_AGENT_EM_CONNECTION_CHECK_FAILED";
    
    /**
     * 
     */
    private static final long serialVersionUID = 5855945420595764866L;

    public PowerPackDelegateException() {
        // TODO Auto-generated constructor stub
    }

    public PowerPackDelegateException(String msg) {
        super(msg);
        // TODO Auto-generated constructor stub
    }

    public PowerPackDelegateException(String msg, String errorCode) {
        super(msg, errorCode);
        // TODO Auto-generated constructor stub
    }

    public PowerPackDelegateException(String errorCode, String pattern, Object... arguments) {
        super(errorCode, pattern, arguments);
        // TODO Auto-generated constructor stub
    }

    public PowerPackDelegateException(Throwable ex, String errorCode, String pattern,
        Object... arguments) {
        super(ex, errorCode, pattern, arguments);
        // TODO Auto-generated constructor stub
    }

    public PowerPackDelegateException(Throwable ex) {
        super(ex);
        // TODO Auto-generated constructor stub
    }

    public PowerPackDelegateException(Throwable ex, String errorCode) {
        super(ex, errorCode);
        // TODO Auto-generated constructor stub
    }

    public PowerPackDelegateException(String msg, Throwable ex) {
        super(msg, ex);
        // TODO Auto-generated constructor stub
    }

    public PowerPackDelegateException(String msg, Throwable ex, String errorCode) {
        super(msg, ex, errorCode);
        // TODO Auto-generated constructor stub
    }

}
