package com.ca.apm.systemtest.fld.common;

import java.util.concurrent.TimeUnit;

import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;

/**
 * This class groups methods for starting and stopping of Microsoft IIS.
 */
public class IisUtils {
    /**
     * Stop IIS.
     *
     * @return exit code of the IISRESET utility
     */
    public static int stop() {
        return runAndWait("IISRESET", "/STOP");
    }

    /**
     * Start IIS.
     *
     * @return exit code of the IISRESET utility
     */
    public static int start() {
        return runAndWait("IISRESET", "/START");
    }

    /**
     * Reset IIS.
     *
     * @return exit code of the IISRESET utility
     */
    public static int reset() {
        return runAndWait("IISRESET");
    }

    private static int runAndWait(String... command) {
        ProcessExecutor pb = ProcessUtils2.newProcessExecutor().command(command);
        StartedProcess proc = ProcessUtils2.startProcess(pb);
        return ProcessUtils2.waitForProcess(proc, 5, TimeUnit.MINUTES, true);
    }
}
