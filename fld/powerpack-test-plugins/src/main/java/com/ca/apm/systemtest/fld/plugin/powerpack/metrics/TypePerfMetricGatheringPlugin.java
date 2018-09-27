package com.ca.apm.systemtest.fld.plugin.powerpack.metrics;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.common.TypePerfCounter;
import com.ca.apm.systemtest.fld.common.TypePerfUtils;
import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.powerpack.common.PowerPackConstants;
import com.ca.apm.systemtest.fld.plugin.vo.DashboardIdStore;
import com.ca.apm.systemtest.fld.plugin.vo.ProcessInstanceIdStore;

/**
 * FLD LO plugin which launches Windows utility TypePerf to generate performance metrics.
 * 
 * @author rsssa02
 * @author Alexander Sinyushkin (sinal04@ca.com)
 */
@PluginAnnotationComponent(pluginType = TypePerfMetricGatheringPlugin.PLUGIN)
public class TypePerfMetricGatheringPlugin extends AbstractPluginImpl implements MetricGatheringPlugin {
    public static final String PLUGIN = "typePerfMetricGatheringPlugin";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TypePerfMetricGatheringPlugin.class);

    private static final String TYPEPERF_THREAD_NAME = "TypePerfRunner";
    
    private static final ThreadLocal<Long> typePerfThreadId = new ThreadLocal<Long>();

    @Override
    @ExposeMethod(description = "Starts performance monitoring using TypePerf utility")
    public Long runMonitoring(PerfMonitoringConfig config) {
        if (config == null) {
            String msg = "Performance monitoring configuration object must not be null!";
            error(msg);
            throw new MetricGatheringPluginException(msg,
                MetricGatheringPluginException.ERR_MONITORING_CONFIG_IS_INVALID);
        }

        if (!(config instanceof TypePerfMonitoringConfig)) {
            String msg =
                MessageFormat.format("Expected performance monitoring object of type {0}, got {1}",
                    TypePerfMonitoringConfig.class.getName(), config.getClass().getName());
            error(msg);
            throw new MetricGatheringPluginException(msg,
                MetricGatheringPluginException.ERR_MONITORING_CONFIG_IS_INVALID);
        }

        TypePerfMonitoringConfig typePerfMonConfig = (TypePerfMonitoringConfig) config;

        final String performanceCounters = typePerfMonConfig.getPerformanceCounters();
        final Integer sampleIntervalMillis = typePerfMonConfig.getSampleIntervalMillis();
        final Integer samplesCount = typePerfMonConfig.getSamplesCount();
        final String outputLogDir = typePerfMonConfig.getOutputLogDirPath();
        final String cmdLineArguments = typePerfMonConfig.getCmdLineArguments();
        String typePerfLogFileName = typePerfMonConfig.getTypePerfLogFileName();
        if (isBlank(typePerfLogFileName)) {
            typePerfLogFileName = PowerPackConstants.DEFAULT_TYPE_PERF_LOG_FILE_NAME; 
            info("No TypePerf log file name provided, using default {0}", 
                typePerfLogFileName);
        }

        final String outFilePath = Paths.get(outputLogDir, typePerfLogFileName).toString();
        info("TypePerf result output file: {0}", outFilePath);

        final String procNamePattern = typePerfMonConfig.getProcessNamePattern();
        final String javaProcNamePattern = typePerfMonConfig.getJavaProcessNamePattern();
        String procInstanceName = typePerfMonConfig.getProcInstanceName();
        Long pid = null;

        if (isBlank(procInstanceName)) {
            StringBuffer buf = new StringBuffer("Process instance name is not provided, trying to find PID first to use it to find process instance name.")
            .append('\n')
            .append("JPS process name pattern: {0}")
            .append('\n')
            .append("Wmic process name pattern: {1}")
            .append('\n')
            .append("Wmic process command line pattern: {2}");
            
            info(buf.toString(), javaProcNamePattern, procNamePattern, cmdLineArguments);
            
            try {
                pid = TypePerfUtils.getPidByProcNamePattern(procNamePattern, javaProcNamePattern, cmdLineArguments);
            } catch (Exception e) {
                String msg = "Getting process id failed:";
                error(msg, e);
                throw new MetricGatheringPluginException(msg, e,
                    MetricGatheringPluginException.ERR_TYPE_PERF_MONITORING_FAILED);
            }
            
            info("Found PID: {0}", pid);
            
            try {
                procInstanceName = TypePerfUtils.getProcessInstanceNameByPID(pid, procNamePattern);
            } catch (Exception e) {
                String msg = "Getting process instance for typeperf failed:";
                error(msg, e);
                throw new MetricGatheringPluginException(msg, e,
                    MetricGatheringPluginException.ERR_TYPE_PERF_MONITORING_FAILED);
            }

            if (isBlank(procInstanceName)) {
                String msg = "Calculated an empty process instance";
                error(msg);
                throw new MetricGatheringPluginException(msg,
                    MetricGatheringPluginException.ERR_TYPE_PERF_MONITORING_FAILED);
            }
        }
        
        int hashInd = procInstanceName.indexOf('#');
        if (hashInd > -1) {
            /*
             * We'd like to collect performance counter data for all processes of the same kind as by default Windows 
             * assigns process names to process instances of the same application in form of: app_name#number.
             * If there's only one process instance running at the moment it will not have the #number part in its name.
             * But if there're several of them running, they'll be named app_name, app_name#1, app_name#2. And during runtime 
             * these names can be re-assigned to different process instances whenever some of the running processes finish. 
             * So, we'd like rather collecting data for all such processes and then filter the output by PID. 
             */
            procInstanceName = procInstanceName.substring(0, hashInd);
        }
        
        try {
            
            Collection<TypePerfCounter> perfCounters = performanceCounters != null ? TypePerfCounter.parsePerfCounters(performanceCounters, procInstanceName) : null;  
            if (perfCounters == null) {
                perfCounters = new ArrayList<>(7); 
            }
            if (perfCounters.isEmpty()) {
                perfCounters.add(TypePerfCounter.createPerfCounter(TypePerfCounter.PROCESSOR_TOTAL_CPU_TIME_COUNTER));
                perfCounters.add(TypePerfCounter.createPerfCounter(TypePerfCounter.PROCESS_CPU_TIME_COUNTER_TEMPLATE, procInstanceName));
                perfCounters.add(TypePerfCounter.createPerfCounter(TypePerfCounter.PROCESS_PRIVATE_BYTES_COUNTER_TEMPLATE, procInstanceName));
                perfCounters.add(TypePerfCounter.createPerfCounter(TypePerfCounter.PROCESS_WORKING_SET_PRIVATE_COUNTER_TEMPLATE, procInstanceName));
                perfCounters.add(TypePerfCounter.createPerfCounter(TypePerfCounter.PROCESS_HANDLE_COUNT_COUNTER_TEMPLATE, procInstanceName));
                perfCounters.add(TypePerfCounter.createPerfCounter(TypePerfCounter.PROCESS_THREAD_COUNT_COUNTER_TEMPLATE, procInstanceName));
                perfCounters.add(TypePerfCounter.createPerfCounter(TypePerfCounter.PROCESS_WORKING_SET_COUNTER_TEMPLATE, procInstanceName));
                perfCounters.add(TypePerfCounter.createPerfCounter(TypePerfCounter.PROCESS_ID_COUNTER_TEMPLATE, procInstanceName));
                info("No performance counters were provided, using default {0}", perfCounters);
            }
            
            final File perfCountersFile = TypePerfUtils.createPerfCounterFile(null, perfCounters);

            info("TypePerf counters file path: {0}", perfCountersFile);
            final Long dashbId = DashboardIdStore.getDashboardId();
            final String processId = ProcessInstanceIdStore.getProcessInstanceId();
            Thread perfThread = new Thread(new Runnable() {
                private final Long dshbId = dashbId;
                private final String prcInstId = processId;
                
                @Override
                public void run() {
                    DashboardIdStore.setDashboardId(dshbId);
                    ProcessInstanceIdStore.setProcessInstanceId(prcInstId);
                    runTypePerf(perfCountersFile.getAbsolutePath(), outFilePath, sampleIntervalMillis, samplesCount);
                }
            });

            perfThread.setName(TYPEPERF_THREAD_NAME);//double check if the thread id was re-used
            perfThread.start();
            info("TypePerf monitoring thread: pid={0}, name={1}", perfThread.getId(), perfThread.getName());
            typePerfThreadId.set(perfThread.getId());
        } catch (Exception ex) {
            String msg = "Exception during TypePerf metric collection";
            error(msg, ex);
            throw new MetricGatheringPluginException(msg, ex,
                MetricGatheringPluginException.ERR_TYPE_PERF_MONITORING_FAILED);
        }
        return pid;
    }

    @Override
    @ExposeMethod(description = "Stops monitoring.")
    public void stopMonitoring() {
        Long threadId = typePerfThreadId.get();
        info("Stopping TypePerf tool running thread identified by id: {0}", threadId);

        Thread perfThread = getThreadById(threadId);
        
        if (perfThread == null) {
            info("TypePerf tool running thread is null");    
        } else {
            info("TypePerf tool running thread (id={0}, name={1}) is not null, its state is: {2}", 
                threadId, perfThread.getName(), perfThread.getState());
            try {
                if (perfThread.isAlive()) {
                    perfThread.interrupt();
                    info("TypePerf tool running thread state after killing: {0}", perfThread.getState());
                } else {
                    info("TypePerf thread is already dead.");
                }
            } catch (Exception ex) {
                String msg = "Exception caught while stopping TypePerf tool running thread";
                error(msg, ex);
                throw new MetricGatheringPluginException(msg, ex, 
                    MetricGatheringPluginException.ERR_TYPE_PERF_STOP_FAILED);
            }
        }
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    private int runTypePerf(String perfCountersFilePath, String resultFilePath, Integer sampleIntervalMillis,
                            Integer samplesCount) {
        Process typeperfProc = null;
        int exitCode = -1;
        try {
            typeperfProc = TypePerfUtils.runTypePerfCmd(perfCountersFilePath, resultFilePath, sampleIntervalMillis, samplesCount);
            info("Waiting for TypePerf to finish...");
            exitCode = typeperfProc.waitFor();
            info("TypePerf exited with code {0}", exitCode);
        } catch (InterruptedException e) {
            String msg = "TypePerf thread was force terminated!";
            warn(msg, e);
        } catch (IOException e) {
            String msg = "IOException caught while starting TypePerf tool"; 
            error(msg, e);
            throw new MetricGatheringPluginException(msg, e,
                MetricGatheringPluginException.ERR_TYPE_PERF_MONITORING_FAILED);
        } finally {
            if (typeperfProc != null) {
                typeperfProc.destroy();
            }
        }
        return exitCode;
    }

    private Thread getThreadById(Long threadId) {
        if (threadId == null) {
            return null;
        }
        
        info("Searching for TypePerf monitoring thread by id={0} and name={1}", threadId, TYPEPERF_THREAD_NAME);
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (threadId.equals(t.getId())) {
                info("Found thread with id={0} and name={1}", threadId, t.getName());
                if (TYPEPERF_THREAD_NAME.equals(t.getName())) {
                    info("Thread found!");
                    return t;    
                }
            }
        }
        info("No TypePerf monitoring thread found.");
        return null;
    }

}
