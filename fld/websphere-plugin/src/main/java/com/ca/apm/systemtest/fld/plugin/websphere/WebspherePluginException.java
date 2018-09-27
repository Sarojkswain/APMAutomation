package com.ca.apm.systemtest.fld.plugin.websphere;

public class WebspherePluginException
    extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID = 4267355172282498595L;

    public WebspherePluginException()
    {}

    public WebspherePluginException(String message)
    {
        super(message);
    }

    public WebspherePluginException(Throwable cause)
    {
        super(cause);
    }

    public WebspherePluginException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public WebspherePluginException(String message, Throwable cause,
                                   boolean enableSuppression,
                                   boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
