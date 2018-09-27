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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.utils.mainframe.sysview.Sysview;
import com.ca.apm.automation.utils.mainframe.sysview.Sysview.ExecResult;
import com.ca.apm.automation.utils.mainframe.sysview.TabularData;

/**
 * Flow that starts and waits for initialization of an MQ instance.
 */
@Flow
public class MqStartupFlow implements IAutomationFlow {
    private static final Logger logger = LoggerFactory.getLogger(MqStartupFlow.class);
    private static final int TIMEOUT_WAIT_INTERVAL = 3000;
    private static final int TIMEOUT_MAX_INTERVALS = 40;

    @FlowContext
    private MqStartupFlowContext context;

    /**
     * Executes the flow - Starts the MQ instance.
     */
    @Override
    public void run() throws Exception {
        // Start the Queue Manager if not running. This also typically starts the Channel Initiator,
        // but that is not guaranteed.
        if (!isQueueManagerInitialized()) {
            if (context.isOnlyVerify()) {
                throw new IllegalStateException("MQ " + context.getQueueManagerName()
                    + " Queue Manager is not initialized");
            } else {
                start(false);
            }
        }

        // Start the Channel Initiator if not running
        if (!isChannelInitiatorInitialized()) {
            if (context.isOnlyVerify()) {
                throw new IllegalStateException("MQ " + context.getQueueManagerName()
                    + " Channel Initiator is not initialized");
            } else {
                start(true);
            }
        }

        logger.info("MQ {} is initialized", context.getQueueManagerName());
    }

    private void start(boolean onlyChInit) throws Exception {
        String subject = onlyChInit ? "channel initiator" : "queue manager and channel initiator";
        logger.debug("Starting MQ {} {}", context.getQueueManagerName(), subject);

        try (Sysview sysview = new Sysview(context.getSysviewLoadlib())) {
            String startCommand = onlyChInit ? "START CHINIT" : "START";
            sysview.execute("MQLIST; SELECT Qmgr EQ {0}; LINECMD ''{1}'' *",
                context.getQueueManagerName(), startCommand);
        }

        logger.debug("Waiting for MQ {} {} to initialize", context.getQueueManagerName(), subject);
        for (int i = 0; !isInitialized(!onlyChInit, true) && i < TIMEOUT_MAX_INTERVALS; ++i) {
            try {
                Thread.sleep(TIMEOUT_WAIT_INTERVAL);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for MQ {} {} to initialize",
                    context.getQueueManagerName(), subject);
                break; // Stop waiting if interrupted
            }
        }

        // Normally when starting the queue manager we also expect the channel initiator to come up
        // which is why we wait for both (above), however we don't consider it a failure if the
        // channel initiator doesn't come up in this scenario.
        if (!isInitialized(!onlyChInit, onlyChInit)) {
            throw new IllegalStateException("Timed out waiting for MQ "
                + context.getQueueManagerName() + " " + subject + " to initialize");
        }
    }

    /**
     * Indicates whether the MQ Queue Manager is running and fully initialized.
     *
     * @return {@code true} if initialized, {@code false} otherwise.
     * @throws Exception If execution of the SYSVIEW commands fails unexpectedly.
     */
    private boolean isQueueManagerInitialized() throws Exception {
        return isComponentInitialized("Status");
    }

    /**
     * Indicates whether the MQ Channel Initiator is running and fully initialized.
     *
     * @return {@code true} if initialized, {@code false} otherwise.
     * @throws Exception If execution of the SYSVIEW commands fails unexpectedly.
     */
    private boolean isChannelInitiatorInitialized() throws Exception {
        return isComponentInitialized("ChInit");
    }

    /**
     * Indicates whether the MQ queue manager and/or channel initiator are running and fully
     * initialized.
     *
     * @param qmgr Whether the state of the queue manager should be considered.
     * @param chinit Whether the state of the channel initiator should be considered.
     * @return true if initialized, false otherwise.
     */
    private boolean isInitialized(boolean qmgr, boolean chinit) throws Exception {
        assert qmgr || chinit;

        return (!qmgr || isQueueManagerInitialized())
            && (!chinit || isChannelInitiatorInitialized());
    }

    /**
     * Indicates whether an MQ component (e.g. queue manager) is running and fully
     * initialized.
     *
     * @param componentField Field in the SYSVIEW output identifying the status of the component.
     * @return {@code true} if initialized, {@code false} otherwise.
     * @throws Exception If execution of the SYSVIEW commands fails unexpectedly.
     */
    private boolean isComponentInitialized(String componentField) throws Exception {
        try (Sysview sysview = new Sysview(context.getSysviewLoadlib())) {
            ExecResult mqList = sysview.execute("MQLIST MONitored; SELECT Qmgr EQ {0}",
                context.getQueueManagerName());

            TabularData td = mqList.getTabularData();
            Map<String, String> row = td.getFirstRowMatching("Qmgr", context.getQueueManagerName());

            return row != null && row.get(componentField).equalsIgnoreCase("ACTIVE");
        }
    }
}
