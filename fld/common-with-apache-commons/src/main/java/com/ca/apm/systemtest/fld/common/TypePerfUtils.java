package com.ca.apm.systemtest.fld.common;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;

/**
 * Common utility methods which target area relates to Windows' tool "typeperf". 
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class TypePerfUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(TypePerfUtils.class);
    
    public static final String TYPEPERF_COMMAND                            = "typeperf.exe";
    public static final String DEFAULT_TYPEPERF_PERF_COUNTER_FILE_PREFIX   = "typePerfCounters";  
    public static final String DEFAULT_TYPEPERF_PERF_COUNTER_FILE_SUFFIX   = ".txt";
    public static final String DEFAULT_PROC_ID_TYPEPERF_RESULT_FILE_PREFIX = "procIdTypePerfCounter";
    public static final String DEFAULT_PROC_ID_TYPEPERF_RESULT_FILE_SUFFIX = ".csv";
    
    /**
     * Returns process instance name for the provided process id.
     * This method runs 'typeperf' utility to get all performance counter names matching the regexp <code>"\Process(procRegex)\ID Process"</code>. 
     * The required process instance name is matched then by comparing "ID Process" counter value with the provided <code>pid</code>.  
     * 
     * @param pid           process id which process instance name is to be found
     * @param procRegex     regular expression to match only certain subset of processes producing less results; 
     *                      if <code>null</code> or empty string is passed, defaults to <code>"*"</code> (i.e. gets counters for all 
     *                      processes currently running in the system) 
     * @return              process instance name
     * @throws Exception
     */
    public static String getProcessInstanceNameByPID(Long pid, String procRegex) throws Exception {
        if (pid == null) {
            String message = "PID can not be null";
            LOGGER.error(message);
            throw new IllegalArgumentException(message);
        }
        if (StringUtils.isBlank(procRegex)) {
            procRegex = "*";
        } else if (!procRegex.endsWith("*")) {
            procRegex += "*";
        }
        
        TypePerfCounter counter = TypePerfCounter.createPerfCounter(TypePerfCounter.PROCESS_ID_COUNTER_TEMPLATE, procRegex);
        String tmpOutputFilePath = Files.createTempFile(DEFAULT_PROC_ID_TYPEPERF_RESULT_FILE_PREFIX, 
            DEFAULT_PROC_ID_TYPEPERF_RESULT_FILE_SUFFIX).toString();
        
        List<String> args = new ArrayList<>(10);
        args.add("cmd.exe");
        args.add("/C");
        args.add(TYPEPERF_COMMAND);
        args.add(counter.getPerformanceCounter());
        args.add("-sc");
        args.add("1");
        args.add("-o");
        args.add(tmpOutputFilePath);
        args.add("-y");
        
        List<String> resultLines = null;
        ProcessExecutor procExecutor = ProcessUtils2.newProcessExecutor().command(args);

        LOGGER.info("typeperf command: {}", args);
        StartedProcess process = null;
        try  {
            process = ProcessUtils2.startProcess(procExecutor);
            int exitCode = ProcessUtils2.waitForProcess(process, 1, TimeUnit.MINUTES, true);
            LOGGER.info("TypePerf process exited with code: {}", exitCode);
            resultLines = org.apache.commons.io.FileUtils.readLines(new File(tmpOutputFilePath));
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error while getting process instance name by PID={}", pid, e);
            }
            
            throw e;
        }

        if (resultLines == null || resultLines.isEmpty() || resultLines.size() < 2) {
            String message = MessageFormat.format("Expected 2 lines in proc id counter typeperf result file, but got {0}: {1}", 
                resultLines != null ? resultLines.size() : 0, resultLines);
            LOGGER.error(message);
            throw new UnknownError(message);
        }
        
        String[] instanceNames = StringUtils.split(resultLines.get(0), ",");
        LOGGER.info("Instance names: {}", Arrays.toString(instanceNames));
        String[] processIds = StringUtils.split(resultLines.get(1), ",");
        LOGGER.info("Process ids: {}", Arrays.toString(processIds));
        
        LOGGER.info("Searching for PID: {}", pid.toString());
        
        int ind = -1;
        for (int i = 0; i < processIds.length; i++) {
            String procIdStr = StringUtils.remove(processIds[i], '"').trim();
            Long procId = null;
            try {
                procId = Long.parseLong(procIdStr);
            } catch (NumberFormatException e) {
                if (i > 0) {
                    LOGGER.info("Exception occurred while trying to convert {} into java.lang.Long",
                        procIdStr, e);
                }
                continue;
            }
            if (pid.equals(procId)) {
                ind = i;
                break;
            }
        }
        if (ind < 0) {
            String message = MessageFormat.format("Could not find provided PID={0} in the typeperf command output. Output lines: {1}", 
                pid.toString(), resultLines);
            LOGGER.error(message);
            throw new UnknownError(message);
        }
        if (ind > instanceNames.length - 1) {
            String message = MessageFormat.format("PID found at index {0} higher than the length of the found instances array: {1}", 
                ind, instanceNames);
            LOGGER.error(message);
            throw new UnknownError(message);
        }
        
        String counterName = instanceNames[ind];
        String origCounterName = counterName;
        LOGGER.info("Found counter '{}' matching provided process id={}", origCounterName, pid);
        
        String searchToken1 = "\\Process(";
        ind = counterName.indexOf(searchToken1);
        if (ind < 0) {
            String message = MessageFormat.format("Could not locate process instance counter starting with ''{0}'' in the found counter ''{1}''", 
                searchToken1, origCounterName);
            LOGGER.error(message);
            throw new UnknownError(message);
        }
        
        counterName = counterName.substring(ind + searchToken1.length());
        
        String searchToken2 = ")\\";
        ind = counterName.indexOf(searchToken2);
        if (ind < 0) {
            String message = MessageFormat.format("Could not locate process instance counter ending with ''{0}'' in the found counter ''{1}''", 
                searchToken2, origCounterName);
            LOGGER.error(message);
            throw new UnknownError(message);
        }
        String instanceName = counterName.substring(0, ind);
        
        LOGGER.info("Calculated process instance name: {}", instanceName);
        return instanceName;
    }
    
    public static Long getPidByProcNamePattern(String procNamePattern, 
                                               String javaProcNamePattern, 
                                               String cmdLinePattern) throws IOException {
        Long pid = null;
        if (isBlank(javaProcNamePattern)) {
            LOGGER.info("No process name pattern for JPS provided, using Wmic with process name pattern=\"{}\" and command line pattern=\"{}\".", 
                procNamePattern, cmdLinePattern);
            if (isBlank(procNamePattern)) {
                String message = "No process instance name provided, JPS process name pattern and Wmic process name pattern can not be both empty at the same time.";
                LOGGER.error(message);
                throw new IllegalArgumentException(message);
            }
            pid = WmicUtils.getPid(procNamePattern, cmdLinePattern);
        } else {
            LOGGER.info("JPS process name pattern=\"{}\" provided, using JPS.", javaProcNamePattern);
            pid = ProcessUtils.findJavaProcessPid(javaProcNamePattern);
        }
        return pid;
    }
    
    
    /**
     * Creates a file with performance counters to be used by the typeperf tool. 
     * 
     * @param  perfCounterFile    output file for performance counters; if <code>null</code> is provided,
     *                            a temporary file is created which is returned as the result of this method
     * @param  perfCounters       performance counters
     * @return
     * @throws IOException
     */
    public static File createPerfCounterFile(File perfCounterFile, 
                                             Collection<TypePerfCounter> perfCounters) throws IOException {
        if (perfCounters == null || perfCounters.isEmpty()) {
            throw new IllegalArgumentException("Performance counters can not be empty!");
        }
        
        if (perfCounterFile == null) {
            perfCounterFile = File.createTempFile(DEFAULT_TYPEPERF_PERF_COUNTER_FILE_PREFIX, 
                DEFAULT_TYPEPERF_PERF_COUNTER_FILE_SUFFIX);
        }
        
        LOGGER.info("Creating performance counters file '{}'. Performance counters: {}", 
            perfCounterFile, perfCounters);
        
        Collection<String> perfCounterLines = new ArrayList<>(perfCounters.size());
        for (TypePerfCounter counter : perfCounters) {
            perfCounterLines.add(counter.getPerformanceCounter());
        }
        
        FileUtils.writeLines(perfCounterFile, perfCounterLines, false);
        
        return perfCounterFile;
    }

    /**
     * Starts external typeperf process and returns a {@link Process} object representing it. 
     * 
     * @param perfCountersFilePath
     * @param resultFilePath
     * @param sampleIntervalMillis
     * @param samplesCount
     * @return
     * @throws IOException
     */
    public static Process runTypePerfCmd(String perfCountersFilePath, String resultFilePath, Integer sampleIntervalMillis,
                                         Integer samplesCount) throws IOException {
        int sampleIntervalSeconds = sampleIntervalMillis / 1000;
        List<String> args = new ArrayList<>(10);
        args.add(TYPEPERF_COMMAND);
        args.add("-si");
        args.add(String.valueOf(sampleIntervalSeconds));
        args.add("-sc");
        args.add(String.valueOf(samplesCount));
        args.add("-cf");
        args.add(perfCountersFilePath);
        args.add("-o");
        args.add(resultFilePath);
        args.add("-y");
        
        LOGGER.info("Starting typeperf with command: {}", 
            StringUtils.replace(args.toString(), ",", " "));

        ProcessBuilder pb = ProcessUtils.newProcessBuilder(); 
        pb.command(args);
        Process process = pb.start();
        return process;
    }

}
