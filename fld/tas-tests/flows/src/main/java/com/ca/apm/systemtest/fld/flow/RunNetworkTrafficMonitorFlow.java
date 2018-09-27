package com.ca.apm.systemtest.fld.flow;

import java.io.File;
import java.io.FileOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.commandline.BackgroundExecution;
import com.ca.apm.systemtest.fld.util.networktrafficmonitor.NetworkTrafficMonitorRunner;

/**
 * Runs until explicitly shut down
 * 
 * @author bocto01
 *
 */
@Flow
public class RunNetworkTrafficMonitorFlow implements IAutomationFlow {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(RunNetworkTrafficMonitorFlow.class);

    private static final long TEN_MINUTES = 600000L;

    @FlowContext
    private RunNetworkTrafficMonitorFlowContext ctx;

    private File workDir;

    /*
     * (non-Javadoc)
     * 
     * @see com.ca.apm.automation.action.core.IAutomationFlow#run()
     */
    @Override
    public void run() throws Exception {
        if (ctx.shutdown) {
            createShutdownFile();
            return;
        }

        workDir = new File(ctx.workDir);
        LOGGER.info("workDir = " + ctx.workDir);
        runScript();

        LOGGER.info("About to create new NetworkTrafficMonitorRunner");
        LOGGER.info(ctx.networkTrafficMonitorWebappHost);
        LOGGER.info("" + ctx.networkTrafficMonitorWebappPort);
        LOGGER.info(ctx.networkTrafficMonitorWebappContextRoot);
        LOGGER.info("" + ctx.chartWidth);
        LOGGER.info("" + ctx.chartHeight);
        NetworkTrafficMonitorRunner ntm =
            new NetworkTrafficMonitorRunner(ctx.networkTrafficMonitorWebappHost,
                ctx.networkTrafficMonitorWebappPort, ctx.networkTrafficMonitorWebappContextRoot,
                ctx.chartWidth, ctx.chartHeight);
        LOGGER.info("Created NTMR!");

        long nextRun = 0L;
        boolean done = false;
        long waitInterval = ctx.waitInterval == null ? TEN_MINUTES : ctx.waitInterval;
        while (!done) {
            if (System.currentTimeMillis() > nextRun) {
                try {
                    LOGGER.info("About to create the chart");
                    ntm.createChart();
                    LOGGER.info("Created the chart!");
                } catch (Exception e) {
                    LOGGER.warn("Exception creating chart", e);
                }
                nextRun = System.currentTimeMillis() + waitInterval;
            }

            try {
                Thread.sleep(10000L);
            } catch (InterruptedException ex) {
                // do nothing
            }
            done = checkShutDown();
        }
    }

    private void createShutdownFile() {
        File file = new File(ctx.shutdownFile);
        LOGGER.info("Will create shutdown file " + ctx.shutdownFile);
        if (file.exists()) {
            LOGGER.warn("Shutdown file already exists!");
            return;
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            out.write("network-traffic-monitor".getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            LOGGER.warn("Unable to shutdown network traffic monitor: " + e.getMessage(), e);
        }
    }

    private boolean checkShutDown() {
        File file = new File(ctx.shutdownFile);
        LOGGER.trace("Checking for shutdown file " + file);
        if (file.exists()) {
            file.delete();
            LOGGER.trace("File exists!");
            return true;
        }
        return false;
    }

    private void runScript() throws Exception {
        String command = "run-network-traffic-monitor.sh";
        (new BackgroundExecution.Builder(workDir, command)).build().go();
    }

}
