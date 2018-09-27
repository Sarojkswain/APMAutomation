package com.ca.apm.systemtest.fld.common;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.MessageLogger;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

/**
 * Created by haiva01 on 22.1.2015.
 */
public class ProcessUtils2 {
    private static Logger log = LoggerFactory.getLogger(ProcessUtils2.class);

    /**
     * Start process specified by ProcessExecutor.
     *
     * @param pe ProcessExecutor instance
     * @return new StartedProcess
     */
    public static StartedProcess startProcess(final ProcessExecutor pe) {
        StartedProcess process;
        try {
            process = pe.start();
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to start process {1}. Exception: {0}", pe.getCommand());
        }
        return process;
    }


    /**
     * Returns new ProcessExecutor providing INFO level logger output stream and 
     * ERROR level logger output stream for redirecting process's STD OUT and STD ERR.
     * 
     * @return new ProcessExecutor
     */
    public static ProcessExecutor newProcessExecutor() {
        return newProcessExecutor(Slf4jStream.ofCaller().asInfo(), 
            Slf4jStream.ofCaller().asError());
    }

    /**
     * Returns new ProcessExecutor providing output streams for process STD OUT and 
     * STD ERR redirection.
     * 
     * @param   logStdOut    std out log output stream
     * @param   logStdErr    std err log output stream
     * @return new ProcessExecutor
     */
    public static ProcessExecutor newProcessExecutor(OutputStream logStdOut, 
                                                     OutputStream logStdErr) {
        return new ProcessExecutor()
            .redirectOutput(logStdOut)
            .redirectError(logStdErr)
            .setMessageLogger(new MessageLogger() {
                @Override
                public void message(Logger log, String format, Object... arguments) {
                    log.info(format, arguments);
                }
            });
    }


    /**
     * Wait for process to exit for given amount of time.
     *
     * @param process
     *        process to wait for
     * @param amount
     *        amount of time to wait
     * @param unit
     *        unit of time
     * @param abortOnTimeout
     *        kill process on timeout
     * @return process exit code
     * @throws RuntimeException
     */
    public static int waitForProcess(final StartedProcess process, final int amount,
        final TimeUnit unit, boolean abortOnTimeout) throws RuntimeException {
        return ProcessUtils.waitForProcess(process.getProcess(), amount, unit, abortOnTimeout);
    }
}
