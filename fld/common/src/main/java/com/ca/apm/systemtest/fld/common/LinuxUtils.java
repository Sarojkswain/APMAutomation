package com.ca.apm.systemtest.fld.common;

import java.util.concurrent.TimeUnit;

/**
 * Linux utilities.
 * Created by haiva01 on 28.11.2014.
 */
public final class LinuxUtils {
    public static enum ServiceOp {
        START("start"),
        STOP("stop"),
        RESTART("restart");

        String command;

        private ServiceOp(String command) {
            this.command = command;
        }

        @Override
        public String toString() {
            return command;
        }
    }


    /**
     * Turns off firewall.
     *
     * @return /sbin/service exit code
     */
    public static int turnOffFirewall() {
        return serviceOperation("iptables", ServiceOp.STOP);
    }


    /**
     * This function runs /sbin/service with given service name and operation.
     * The function uses 5 minute timeout.
     *
     * @param serviceName service name
     * @param op          service operation, i.e., one of  ServiceOp.START, ServiceOp.STOP,
     *                    ServiceOp.RESTART
     * @return /sbin/service exit code
     */
    public static int serviceOperation(String serviceName, ServiceOp op) {
        final ProcessBuilder pb = ProcessUtils.newProcessBuilder()
            .command("/sbin/service", serviceName, op.toString());
        Process process = ProcessUtils.startProcess(pb);
        int exitCode = ProcessUtils.waitForProcess(process, 5, TimeUnit.MINUTES, true);
        return exitCode;
    }
}
