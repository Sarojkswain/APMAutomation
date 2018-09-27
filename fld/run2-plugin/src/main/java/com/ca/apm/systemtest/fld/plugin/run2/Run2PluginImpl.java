package com.ca.apm.systemtest.fld.plugin.run2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.ProcessUtils2;
import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;

/**
 * Plugin to control external processes.
 *
 * @author tavpa01
 */
public class Run2PluginImpl extends AbstractPluginImpl implements Run2Plugin {
    private static final Logger log = LoggerFactory.getLogger(Run2PluginImpl.class);

    private final Map<Integer, StartedProcess> processHandles
        = new TreeMap<>();
    private int handleSerialNumber = 0;


    protected int addHandle(StartedProcess handle) {
        int index;

        synchronized (processHandles) {
            index = ++handleSerialNumber;
            processHandles.put(index, handle);
        }

        return index;
    }

    protected StartedProcess getHandle(int index) {
        synchronized (processHandles) {
            return processHandles.get(index);
        }
    }

    protected StartedProcess getHandleAndCheck(int index) {
        StartedProcess sp = getHandle(index);
        if (sp == null) {
            throw ErrorUtils
                .logErrorAndReturnException(log, "Handle {0} is not associated with any process.",
                    index);
        } else {
            return sp;
        }
    }

    protected StartedProcess removeHandle(int index) {
        synchronized (processHandles) {
            StartedProcess handle = processHandles.get(index);
            processHandles.remove(index);
            return handle;
        }
    }

    private int startProcess(ProcessExecutor pe) throws IOException {
        StartedProcess sp = pe.start();
        int intHandle = addHandle(sp);
        log.info("Returning int handle {}", intHandle);
        return intHandle;
    }

    @Override
    public Integer runProcess(List<String> commandLine,
        Map<String, String> environmentChanges) throws IOException {
        ProcessExecutor pe = ProcessUtils2.newProcessExecutor()
            .command(commandLine);
        if (environmentChanges != null) {
            pe.environment(environmentChanges);
        }
        return startProcess(pe);
    }

    @Override
    public Integer runProcess2(List<String> commandLine, String logFileName,
        String workingDirectoryName, Map<String, String> environmentChanges) throws IOException {
        ProcessExecutor pe = ProcessUtils2.newProcessExecutor()
            .command(commandLine);

        if (logFileName != null) {
            File logFile = new File(logFileName);
            FileUtils.forceMkdir(logFile.getParentFile());
            pe.redirectErrorStream(true)
                .redirectOutput(FileUtils.openOutputStream(logFile));
        }

        if (workingDirectoryName != null) {
            File workingDirectory = new File(workingDirectoryName);
            pe.directory(workingDirectory);
        }

        if (environmentChanges != null) {
            pe.environment(environmentChanges);
        }

        return startProcess(pe);
    }

    @Override
    public void stopProcess(Integer procId) {
        StartedProcess sp = getHandleAndCheck(procId);
        sp.getProcess().destroy();
        removeHandle(procId);
    }

    @Override
    public long exitValue(Integer procId) {
        StartedProcess sp = getHandleAndCheck(procId);
        try {
            ProcessResult pr = sp.getFuture().get(0, TimeUnit.SECONDS);
            int exitCode = pr.getExitValue();
            log.info("Exit code of process {} is {}.", procId, exitCode);
            return (long) exitCode;
        } catch (TimeoutException e) {
            return Run2Plugin.STILL_RUNNING;
        } catch (ExecutionException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to get process {1} exit code. Exception: {0}", procId);
        } catch (InterruptedException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Got interrupted while checking process {1} exit code. Exception: {0}", procId);
        }
    }

    @Override
    public void closeHandle(Integer procId) {
        log.info("Closing process handle {}.", procId);
        removeHandle(procId);
    }

    @Override
    public List<Integer> listRunningProcesses() {
        synchronized (processHandles) {
            return new ArrayList<>(processHandles.keySet());
        }
    }

    @Override
    public void stopAllProcesses() {
        synchronized (processHandles) {
            for (Integer handle : processHandles.keySet()) {
                try {
                    stopProcess(handle);
                } catch (Exception e) {
                    ErrorUtils.logExceptionFmt(log, e,
                        "Exception closing process with handle {1}. Exception: {0}", handle);
                }
            }
        }
    }
}