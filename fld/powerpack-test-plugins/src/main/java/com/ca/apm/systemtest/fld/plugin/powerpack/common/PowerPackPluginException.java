package com.ca.apm.systemtest.fld.plugin.powerpack.common;

import com.ca.apm.systemtest.fld.plugin.RemoteCallException;

/**
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class PowerPackPluginException extends RemoteCallException {

    public static final String ERR_RECREATE_TRADE_DB_FAILED = "ERR_RECREATE_TRADE_DB_FAILED";
    public static final String ERR_BUILD_TEST_RESULT_REPORT_FAILED = "ERR_BUILD_TEST_RESULT_REPORT_FAILED";
    
    /**
     * 
     */
    private static final long serialVersionUID = 7793406247424510499L;

    public PowerPackPluginException() {
        // TODO Auto-generated constructor stub
    }

    public PowerPackPluginException(String msg) {
        super(msg);
        // TODO Auto-generated constructor stub
    }

    public PowerPackPluginException(String msg, String errorCode) {
        super(msg, errorCode);
        // TODO Auto-generated constructor stub
    }

    public PowerPackPluginException(Throwable ex) {
        super(ex);
        // TODO Auto-generated constructor stub
    }

    public PowerPackPluginException(Throwable ex, String errorCode) {
        super(ex, errorCode);
        // TODO Auto-generated constructor stub
    }

    public PowerPackPluginException(String msg, Throwable ex) {
        super(msg, ex);
        // TODO Auto-generated constructor stub
    }

    public PowerPackPluginException(String msg, Throwable ex, String errorCode) {
        super(msg, ex, errorCode);
        // TODO Auto-generated constructor stub
    }

}
