package com.ca.apm.systemtest.fld.plugin.powerpack.metrics;

import java.io.File;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.common.ProcessUtils;
import com.ca.apm.systemtest.fld.common.WmicUtils;
import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;

/**
 * Performance monitoring plugin which uses jstat tool.
 * 
 * @author rsssa02
 */
@PluginAnnotationComponent(pluginType = JstatMetricGatheringPlugin.PLUGIN)
public class JstatMetricGatheringPlugin extends AbstractPluginImpl implements MetricGatheringPlugin {
    public static final String PLUGIN = "JstatMetricGatheringPlugin";

    private static final Logger LOGGER = LoggerFactory
        .getLogger(JstatMetricGatheringPlugin.class);

    private static final String JSTAT_THREAD_NAME = "JstatRunner";
    private static final ThreadLocal<Long> jstatThreadId = new ThreadLocal<Long>();

    /**
     * Runs performance monitoring.
     *
     * @param config performance monitoring configuration
     */
    @Override
    public Long runMonitoring(PerfMonitoringConfig config) {
        if (null == config) {
            String msg = "Performance monitoring configuration object must not be null!";
            error(msg);
            throw new MetricGatheringPluginException(msg,
                MetricGatheringPluginException.ERR_MONITORING_CONFIG_IS_INVALID);
        }
        if (!(config instanceof JstatMonitoringConfig)) {
            String msg =
                MessageFormat.format("Expected performance monitoring object of type {0}, got {1}",
                    JstatMonitoringConfig.class.getName(), config.getClass().getName());
            error(msg);
            throw new MetricGatheringPluginException(msg,
                MetricGatheringPluginException.ERR_MONITORING_CONFIG_IS_INVALID);
        }
        
        Long pid = null;
        try {
            final JstatMonitoringConfig jsConfig = (JstatMonitoringConfig) config;
            pid = WmicUtils.getPid(jsConfig.getProcessName(), jsConfig.getCmdLineName());
            if (pid == null) {
                throw new UnknownError("pid is null!");
            }

            final List<String> args = new ArrayList<>(5);
            args.add("jstat.exe");
            args.add("-gc");
            args.add(pid.toString());
            args.add(jsConfig.getSampleIntervalMillis() + "ms");
            args.add(jsConfig.getSamplesCount().toString());

            Thread jstatThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    runJstatThread(args, jsConfig);
                }
            });
            jstatThread.setName(JSTAT_THREAD_NAME);
            jstatThread.start();
            jstatThreadId.set(jstatThread.getId());
        } catch (Exception e) {
            String msg = "Exception during jstat metric collection";
            error(msg, e);
            throw new MetricGatheringPluginException(msg, e,
                MetricGatheringPluginException.ERR_JSTAT_MONITORING_FAILED);
        }
        return pid;
    }

    private void runJstatThread(List<String> args, JstatMonitoringConfig jsConfig) {
        Process jstatProcess = null;
        String jstatoutPath = Paths.get(jsConfig.getOutputLogDirPath(),
            jsConfig.getJstatLogFileName()).toString();
        
        info("Running cmd args {0} output redirected to {1}", args.toString(), jstatoutPath);

        try {
            ProcessBuilder ps =
                ProcessUtils
                    .newProcessBuilder()
                    .command(args)
                    .directory(new File(jsConfig.getOutputLogDirPath()))
                    .redirectErrorStream(true)
                    .redirectOutput(
                        ProcessBuilder.Redirect.to(new File(jsConfig.getOutputLogDirPath(),
                            jsConfig.getJstatLogFileName())));
            jstatProcess = ProcessUtils.startProcess(ps);

            info("Waiting for Jstat to finish...");
            int exitCode = jstatProcess.waitFor();
            info("Jstat completed with exit code: {0}", exitCode);
        } catch (InterruptedException e) {
            String msg = "Error while running jstat.";
            throw new MetricGatheringPluginException(msg, e,
                MetricGatheringPluginException.ERR_TYPE_PERF_MONITORING_FAILED);
        } finally {
            if (jstatProcess != null) {
                jstatProcess.destroy();
            }
        }
    }

    /**
     * Finishes performance monitoring.
     */
    @Override
    public void stopMonitoring() {
        Long threadId = jstatThreadId.get();
        
        info("Stopping jstat tool running thread identified by id: {0}", threadId);
        
        try {
            Thread stopThread = null;
            for (Thread t : Thread.getAllStackTraces().keySet()) {
                if (threadId.equals(t.getId()) && JSTAT_THREAD_NAME.equals(t.getName())) {
                    stopThread = t;
                }
            }
            if (stopThread == null) {
                info("jstat tool running thread is null");    
            } else {
                info("jstat tool running thread (id={0}, name={1}) is not null, its state is: {2}", 
                    threadId, stopThread.getName(), stopThread.getState());
                if (stopThread.isAlive()) {
                    stopThread.interrupt();
                } else {
                    info("jstat thread is already dead.");
                }
            }
        } catch (Exception e) {
            String msg = "Exception caught while stopping jstat running thread";
            error(msg, e);
            throw new MetricGatheringPluginException(msg, e, 
                MetricGatheringPluginException.ERR_JSTAT_STOP_FAILED);
        }
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
