package com.ca.apm.systemtest.fld.plugin.selenium;

public class SeleniumPluginException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 4267355172282498595L;

    public SeleniumPluginException() {}

    public SeleniumPluginException(String message) {
        super(message);
    }

    public SeleniumPluginException(Throwable cause) {
        super(cause);
    }

    public SeleniumPluginException(String message, Throwable cause) {
        super(message, cause);
    }

    public SeleniumPluginException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
