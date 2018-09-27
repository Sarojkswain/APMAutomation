/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 *
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.automation.action.flow.mainframe;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.utils.mainframe.MvsTask;
import com.ca.apm.automation.utils.mainframe.sysview.Sysview;
import com.ca.apm.automation.utils.mainframe.sysview.Sysview.ExecResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Flow that starts and waits for initialization of a CICS region.
 */
@Flow
public class CicsStartupFlow implements IAutomationFlow {
    private static final Logger logger = LoggerFactory.getLogger(CicsStartupFlow.class);
    private static final int TIMEOUT_WAIT_INTERVAL = 3000;
    private static final int TIMEOUT_MAX_INTERVALS = 40;

    @FlowContext
    private CicsStartupFlowContext context;

    /**
     * Executes the flow - Starts the CICS region and takes care of any other requirements like
     * DB2 monitoring.
     */
    @Override
    public void run() throws Exception {
        final String region = context.getTaskName();

        try (Sysview sysview = new Sysview(context.getSysviewLoadlib())) {
            // CICS Region
            if (!isRegionRunning(sysview, region)) {
                if (context.isOnlyVerify()) {
                    throw new IllegalStateException(
                        "The " + region + " CICS region is not running");
                } else {
                    startRegion(sysview, region, context.getLpar());
                }
            }

            // Monitoring of the region by Sysview
            if (!isRegionMonitored(sysview, region)) {
                if (context.isOnlyVerify()) {
                    throw new IllegalStateException(
                        "The " + region + " CICS region is not monitored by Sysview");
                } else {
                    monitorRegion(sysview, region);
                }
            }

            // SMF delivery
            logger.info("Enabling delivery of CICS SMF records in SYSVIEW");
            sysview.execute("CICSSET CONFIG WILY-INTROSCOPE YES JOBNAME ALL");

            // DB2 monitoring
            if (context.getDb2Subsystem() != null) {
                if (!isDb2InterfaceInitialized(sysview, region)) {
                    if (context.isOnlyVerify()) {
                        throw new IllegalStateException("The " + region + " <=> " +
                            context.getDb2Subsystem() + " CICS-DB2 interface is not initialized");
                    } else {
                        initializeDb2Interface(sysview, region, context.getDb2Subsystem());
                    }
                }
            }
        }
    }

    /**
     * Starts a CICS region.
     *
     * @param sysview Sysview instance to use for queries.
     * @param region CICS region to start.
     * @param lpar LPAR where the region is to be started from.
     * @throws Exception If execution of a Sysview command fails unexpectedly.
     */
    private void startRegion(Sysview sysview, String region, String lpar) throws Exception {
        logger.info("Starting the {} CICS region task", region);
        MvsTask.start(region, lpar);

        logger.debug("Waiting for the {} CICS region to start", region);
        for (int i = 0; !isRegionRunning(sysview, region) && i < TIMEOUT_MAX_INTERVALS; ++i) {
            try {
                Thread.sleep(TIMEOUT_WAIT_INTERVAL);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for the {} CICS region to start", region);
                break; // Stop waiting if interrupted
            }
        }

        if (!isRegionRunning(sysview, region)) {
            throw new IllegalStateException(
                "Timed out while waiting for the " + region + " CICS region to start");
        }

        logger.info("The {} CICS region is now running", region);
    }

    /**
     * Enables monitoring of a CICS region by sysview.
     *
     * @param sysview Sysview instance to use for queries and monitoring.
     * @param region CICS region to monitor.
     * @throws Exception If execution of a Sysview command fails unexpectedly.
     */
    private void monitorRegion(Sysview sysview, String region) throws Exception {
        logger.info("Enabling Sysview monitoring for the {} CICS region", region);
        sysview.execute("MVS MODIFY " + region + ",GSVS START=COLD");

        logger.debug("Waiting for the {} CICS region to be monitored by Sysview");
        for (int i = 0; !isRegionMonitored(sysview, region) && i < TIMEOUT_MAX_INTERVALS; ++i) {
            try {
                Thread.sleep(TIMEOUT_WAIT_INTERVAL);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for the {} CICS region to be monitored by"
                    + " Sysview", region);
                break; // Stop waiting if interrupted
            }
        }

        if (!isRegionMonitored(sysview, region)) {
            throw new IllegalStateException("Timed out while waiting for the " + region
                + " CICS region to be monitored by Sysview");
        }

        logger.info("The {} CICS region is now monitored by Sysview", region);
    }

    /**
     * Initializes the CICS-DB2 interface in a CICS region.
     *
     * @param sysview Sysview instance to use for queries.
     * @param region CICS region.
     * @param db2Subsystem DB2 subsystem to connect to.
     * @throws Exception If execution of a Sysview command fails unexpectedly.
     */
    private void initializeDb2Interface(Sysview sysview, String region, String db2Subsystem)
        throws Exception {
        logger.info("Initializing the {} <=> {} CICS-DB2 interface", region, db2Subsystem);
        sysview.execute("MVS MODIFY " + region + ",DSNC STRT " + db2Subsystem);

        logger.debug("Waiting for the {} <=> {} CICS-DB2 interface to initialize",
            region, db2Subsystem);
        for (int i = 0; !isDb2InterfaceInitialized(sysview, region)
            && i < TIMEOUT_MAX_INTERVALS; ++i) {
            try {
                Thread.sleep(TIMEOUT_WAIT_INTERVAL);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for the {} <=> {} CICS-DB2 interface to"
                    + " initialize", region, db2Subsystem);
                break; // Stop waiting if interrupted
            }
        }

        if (!isRegionMonitored(sysview, region)) {
            throw new IllegalStateException("Timed out while waiting for the " + region
                + " <=> " + db2Subsystem + " CICS-DB2 interface to initialize");
        }

        logger.info("The {} <=> {} CICS-DB2 interface is now initialized", region, db2Subsystem);
    }

    /**
     * Indicates whether a CICS region is running.
     *
     * @param sysview Sysview instance to use for queries.
     * @param region CICS region to check.
     * @return {@code true} if the CICS region is running, {@code false} otherwise.
     * @throws Exception If execution of a Sysview command fails unexpectedly.
     */
    private boolean isRegionRunning(Sysview sysview, String region) throws Exception {
        if (!MvsTask.isTaskInState(region, context.getLpar(), MvsTask.State.RUNNING)) {
            return false;
        }

        // The state in Sysview can temporarily switch back to INACTIVE after being in EXECUTING.
        // To address this we consider the region as running only once it's in a stable state.
        int consecutiveChecks = 5;
        for (int i = 0; i < consecutiveChecks; ++i) {
            if (i != 0) {
                TimeUnit.SECONDS.sleep(1);
            }

            String execValue = getRegionFieldValue(sysview, region, "ExecStage");
            if (execValue == null || execValue.compareToIgnoreCase("EXECUTING") != 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Indicates whether a CICS region is being monitored by Sysview.
     *
     * @param sysview Sysview instance to use for queries and for monitoring status.
     * @param region CICS region to check.
     * @return {@code true} if the CICS region is monitored, {@code false} otherwise.
     * @throws Exception If execution of a Sysview command fails unexpectedly.
     */
    private boolean isRegionMonitored(Sysview sysview, String region) throws Exception {
        String monitoringValue = getRegionFieldValue(sysview, region, "Status");

        return monitoringValue != null && monitoringValue.compareToIgnoreCase("ACTIVE") == 0;
    }

    /**
     * Indicates whether a CICS region has a DB2 interface connection active.
     *
     * @param sysview Sysview instance to use for queries.
     * @param region CICS region to check.
     * @return {@code true} if the CICS region has an active DB2 interface connection
     * {@code false} otherwise.
     * @throws Exception If execution of a Sysview command fails unexpectedly.
     */
    private boolean isDb2InterfaceInitialized(Sysview sysview, String region) throws Exception {
        String connectionValue = getRegionFieldValue(sysview, region, "DB2");

        return connectionValue != null && connectionValue.compareToIgnoreCase("CONNECTED") == 0;
    }

    /**
     * Helper method that returns the value of a specific field for a region from the CICSLIST
     * Sysview panel.
     *
     * @param sysview Sysview instance to use for queries.
     * @param region CICS region to get the value from.
     * @param field Field to get the value from.
     * @return The field value.
     * @throws Exception If execution of a Sysview command fails unexpectedly.
     */
    private String getRegionFieldValue(Sysview sysview, String region, String field)
        throws Exception {
        ExecResult cicsList = sysview.execute("CICSLIST; SELECT Name EQ {0}", region);

        Map<String, String> row = cicsList.getTabularData().getFirstRowMatching("Name", region);
        if (row == null || !row.containsKey(field)) {
            throw new IllegalStateException("Unable to query '" + field + "' CICS region field" );
        }

        logger.debug("Field {} for CICS region {} has value '{}'", field, region, row.get(field));
        return row.get(field);
    }
}
