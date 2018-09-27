/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin;

/**
 * @author keyja01
 *
 */
@SuppressWarnings("serial")
public class AgentInstallException extends RemoteCallException {
    public static final String ERR_AGENT_ALREADY_INSTALLED = "ERR_AGENT_ALREADY_INSTALLED";
    public static final String ERR_SERVER_INSTANCE_MISSING = "ERR_SERVER_INSTANCE_MISSING";
    public static final String ERR_TARGET_DIR_NOT_EMPTY = "ERR_TARGET_DIR_NOT_EMPTY";
    public static final String ERR_OS_NOT_SUPPORTED = "ERR_OS_NOT_SUPPORTED";
    public static final String ERR_SERVER_NOT_INSTALLED = "ERR_SERVER_NOT_INSTALLED";

    public AgentInstallException() {
        super();
    }

    public AgentInstallException(String msg, String errorCode) {
        super(msg, errorCode);
    }

    public AgentInstallException(String msg, Throwable ex, String errorCode) {
        super(msg, ex, errorCode);
    }

    public AgentInstallException(String msg, Throwable ex) {
        super(msg, ex);
    }

    public AgentInstallException(String msg) {
        super(msg);
    }

    public AgentInstallException(Throwable ex, String errorCode) {
        super(ex, errorCode);
    }

    public AgentInstallException(Throwable ex) {
        super(ex);
    }
}
