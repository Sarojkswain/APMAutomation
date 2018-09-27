/**
 * 
 */
package com.ca.apm.systemtest.fld.flow;

import java.io.File;
import java.io.FileOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.systemtest.fld.util.memorymonitor.MemoryMonitorRunner;

/**
 * Runs until explicitly shut down
 * @author keyja01
 *
 */
@Flow
public class RunMemoryMonitorFlow implements IAutomationFlow {

    public static final String START_WV_FLOW = "startWvFlow";
    public static final String STOP_WV_FLOW = "stopWvFlow";
    private static final Logger logger = LoggerFactory.getLogger(RunMemoryMonitorFlow.class);
    private static final long TEN_MINUTES = 600000L;
    
    @FlowContext
    private RunMemoryMonitorFlowContext ctx;

    /* (non-Javadoc)
     * @see com.ca.apm.automation.action.core.IAutomationFlow#run()
     */
    @Override
    public void run() throws Exception {
        if (ctx.shutdown) {
            createShutdownFile();
            return;
        }
        
        logger.info("About to create new MemoryMonitorRunner");
        logger.info(ctx.gcLogFile);
        logger.info(ctx.group);
        logger.info(ctx.roleName);
        logger.info(ctx.memoryMonitorWebappHost);
        logger.info("" + ctx.memoryMonitorWebappPort);
        logger.info(ctx.memoryMonitorWebappContextRoot);
        logger.info("" + ctx.chartWidth);
        logger.info("" + ctx.chartHeight);
        MemoryMonitorRunner mm = new MemoryMonitorRunner(ctx.gcLogFile, ctx.group, ctx.roleName, 
            ctx.memoryMonitorWebappHost, ctx.memoryMonitorWebappPort, ctx.memoryMonitorWebappContextRoot, ctx.chartWidth, ctx.chartHeight);
        logger.info("Created MMR!");
        
        long nextRun = 0L;
        boolean done = false;
        long waitInterval = ctx.waitInterval == null ? TEN_MINUTES : ctx.waitInterval;
        while (!done) {
            if (System.currentTimeMillis() > nextRun) {
                try {
                    logger.info("About to create the chart");
                    mm.createChart();
                    logger.info("Created the chart!");
                } catch (Exception e) {
                    logger.warn("Exception creating chart", e);
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
        logger.info("Will create shutdown file " + ctx.shutdownFile);
        if (file.exists()) {
            logger.warn("Shutdown file already exists!");
            return;
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            out.write("foo".getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            logger.warn("Unable to shutdown memory monitor: " + e.getMessage(), e);
        }
    }
    

    private boolean checkShutDown() {
        File file = new File(ctx.shutdownFile);
        logger.trace("Checking for shutdown file " + file);
        if (file.exists()) {
            file.delete();
            logger.trace("File exists!");
            return true;
        }
        
        return false;
    }
}
