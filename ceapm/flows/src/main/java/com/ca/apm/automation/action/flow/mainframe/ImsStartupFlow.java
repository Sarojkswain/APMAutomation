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

import static com.ca.apm.automation.utils.mainframe.ims.ImsSubsystem.CONTROL_REGION;
import static com.ca.apm.automation.utils.mainframe.ims.ImsSubsystem.IMS_CONNECT;
import static com.ca.apm.automation.utils.mainframe.ims.ImsSubsystem.INTER_REGION_LOCK_MANAGER;
import static com.ca.apm.automation.utils.mainframe.ims.ImsSubsystem.MESSAGE_PROCESSING_REGION;
import static com.ca.apm.automation.utils.mainframe.ims.ImsSubsystem.OPERATIONS_MANAGER;
import static com.ca.apm.automation.utils.mainframe.ims.ImsSubsystem.RESOURCE_MANAGER;
import static com.ca.apm.automation.utils.mainframe.ims.ImsSubsystem.STRUCTURED_CALL_INTERFACE;
import static com.ca.apm.automation.utils.mainframe.ims.ImsSubsystem.TRIGGER_MONITOR;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.utils.mainframe.MvsTask;
import com.ca.apm.automation.utils.mainframe.ims.ImsSubsystem;
import com.ca.apm.automation.utils.mainframe.sysview.Sysview;
import com.ca.apm.automation.utils.mainframe.sysview.Sysview.ExecResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Flow that starts a IMS region that follows the structure as described
 * <a href='https://cawiki.ca.com/x/r7DIKQ'>here</a>.
 */
@Flow
public class ImsStartupFlow implements IAutomationFlow {
    private static final Logger logger = LoggerFactory.getLogger(ImsStartupFlow.class);
    private static final int CHECK_RETRY = 30;

    @FlowContext
    private ImsStartupFlowContext context;

    /**
     * Obtains the list of WTORs for a specific task.
     *
     * @param sysv SYSVIEW instance used for queries.
     * @param taskName Task name.
     * @return Table rows containing WTOR details.
     * @throws IOException If querying state of active WTORs fails.
     */
    private List<Map<String, String>> getTaskWtors(Sysview sysv, String taskName)
        throws IOException {
        assert sysv != null;
        assert taskName != null && !taskName.trim().isEmpty();

        ExecResult wtors = sysv.execute("WTOR; SELECT Jobname = {0}", taskName);
        return wtors.getTabularData().getAllRowsMatching("Jobname", taskName);
    }

    /**
     * Obtains the WTOR reply identifier (number) containing IMS ready prompt for a specific task.
     *
     * @param sysv SYSVIEW instance used for queries.
     * @param taskName Task name.
     * @return WTOR reply identifier.
     * @throws IOException If querying state of active WTORs fails.
     * @throws IllegalStateException If number of active WTORs for the task isn't equal to one.
     */
    private int getTaskReplyId(Sysview sysv, String taskName) throws IOException {
        assert sysv != null;
        assert taskName != null && !taskName.trim().isEmpty();

        List<Map<String, String>> rows = getTaskWtors(sysv, taskName);
        if (rows.size() != 1) {
            // We only answer if there is exactly one WTOR for the JOB.
            // We can't safely continue if we detect multiple.
            // Detecting none means that we are unable to proceed with the startup procedure.
            for (Map<String, String> row : rows) {
                logger.debug("WTOR for '{}': {}", taskName, row);
            }
            throw new IllegalStateException("Got " + rows.size() + " WTOR entries for task "
                + taskName);
        }

        Map<String, String> row = rows.get(0);
        String message = row.get("Message");
        String id = row.get("Iden");
        if (message == null || !message.contains("IMS READY")) {
            logger.debug("WTOR for '{}': {}", taskName, row);
            throw new IllegalStateException("Got unexpected WTOR: " + id + " " + message);
        }

        return Integer.valueOf(id);
    }

    /**
     * Replies to a WTOR.
     *
     * @param sysv SYSVIEW instance used for queries.
     * @param replyId WTOR reply identifier.
     * @param reply Text of the reply.
     * @throws IOException If unable to reply.
     */
    private void replyToWtor(Sysview sysv, int replyId, String reply) throws IOException {
        assert sysv != null;
        assert replyId >= 0;
        assert reply != null;

        logger.info("Replying to WTOR #{} with: {}", replyId, reply);
        sysv.execute("MVS REPLY {0},{1}", String.valueOf(replyId), reply);
    }

    /**
     * Obtains the status of the control region in the context.
     *
     * @param sysv SYSVIEW instance used for queries.
     * @return Status of the control region.
     * @throws IOException If execution of a command through SYSVIEW fails.
     * @throws IllegalStateException If unable to parse status output.
     */
    private String getControlRegionStatus(Sysview sysv) throws IOException {
        assert sysv != null;

        ExecResult imsList = sysv.execute("IMSLIST");
        Map<String, String> row =
            imsList.getTabularData().getFirstRowMatching("Id", context.getRegion());
        if (row == null || !row.containsKey("Status")) {
            // TODO investigate why this occasionally fails immediately after starting the CR job
            // successfully
            logger.debug("{}", imsList);
            throw new IllegalStateException("Unable to query IMS control region status");
        }

        final String status = row.get("Status");
        logger.debug("IMS control region {} is {}", context.getRegion(), status);
        return status;
    }

    /**
     * Obtains the status of the external MQ subsystem connection.
     *
     * @param sysv SYSVIEW instance used for queries.
     * @return Status of the connection - forced to uppercase.
     * @throws IOException If execution of a command through SYSVIEW fails.
     * @throws IllegalStateException If unable to parse status output.
     */
    private String getMqConnectionStatus(Sysview sysv) throws IOException {
        assert sysv != null;

        ExecResult res = sysv.execute("IMS {0}; IMSSSYS; SELECT Type = WMQ", context.getRegion());
        Map<String, String> row =
            res.getTabularData().getFirstRowMatching("Name", context.getQueueManagerName());
        if (row == null || !row.containsKey("Status")) {
            throw new IllegalStateException(
                "Unable to query state of the IMS external MQ subsystem connection");
        }

        return row.get("Status").toUpperCase();
    }

    /**
     * Identifies whether the external MQ Subsystem connection is established.
     *
     * @param sysv SYSVIEW instance used for queries.
     * @return {@code true} if connected, {@code false} otherwise.
     * @throws IOException If querying of connection status fails.
     */
    private boolean isMqConnected(Sysview sysv) throws IOException {
        return getMqConnectionStatus(sysv).equals("CONNECTED");
    }

    /**
     * Establishes the IMS external MQ connection to the MQ queue manager in the context, if not
     * already connected.
     *
     * @param sysv SYSVIEW instance used for queries.
     * @throws Exception If unable to query or control the state of the connection.
     */
    private void connectExternalMqSubsystem(Sysview sysv) throws Exception {
        assert sysv != null;

        logger.info("Verifying IMS connection to MQ Queue Manager {}",
            context.getQueueManagerName());

        String mqConnectionStatus = getMqConnectionStatus(sysv);
        switch (mqConnectionStatus) {
            case "CONNECTED":
                // Nothing to do.
                break;

            case "STATUS UNDETERMINATE":
                // In this case it is usually enough to (re)connect so we make the attempt.
                logger.warn("Undeterminate connection status, will attempt (re)connect");
            default:
                // In all other cases we attempt to (re)connect.
                logger.info("Establishing IMS connection to MQ {}, current status: {}",
                    context.getQueueManagerName(), mqConnectionStatus);
                sysv.execute("IMS {0}; IMSSSYS; SELECT Type = WMQ AND Name = {1}; LINECMD 'START' *",
                    context.getRegion(), context.getQueueManagerName());
                long limit = System.nanoTime() + 30_000_000_000L; // Wait no longer than 30 seconds
                do {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        throw new IllegalStateException(
                            "Interrupted while attempting to (re)connect external MQ subsystem", e);
                    }
                } while (getMqConnectionStatus(sysv).compareTo("CONNECTED") != 0
                    && System.nanoTime() < limit);
                break;
        }

        logger.info("IMS connection to MQ Queue Manager {} is up", context.getQueueManagerName());
    }

    /**
     * Starts an IMS subsystem.
     *
     * @param sysv Sysview instance used for queries.
     * @param subsystem Subsystem to start.
     * @throws Exception If unable to start the task or query its output.
     */
    private void startSubsystem(Sysview sysv, ImsSubsystem subsystem) throws Exception {
        assert sysv != null;
        assert subsystem != null;

        if (subsystem.isShared()) {
            startSharedSubsystem(sysv, subsystem.getTaskName(context.getVersion()),
                subsystem.getName(), subsystem.getSharedDetectionMessage());
        } else {
            startNonSharedSubsystem(subsystem.getTaskName(context.getVersion()),
                subsystem.getName());
        }
    }

    /**
     * Identifies whether an IMS subsystem is running.
     *
     * @param sysv Sysview instance used for queries.
     * @param subsystem Subsystem to check.
     * @return {@code true} if the subsystem is running, {@code false} otherwise.
     * @throws Exception If querying the state of the associated tasks fails.
     */
    private boolean isSubsystemRunning(Sysview sysv, ImsSubsystem subsystem) throws Exception {
        if (MvsTask.isTaskInState(subsystem.getTaskName(context.getVersion()),
            null, MvsTask.State.RUNNING)) {
            return true;
        }

        if (subsystem.isShared()) {
            // For shared subsystems the only way we currently know of to figure out if they are
            // up is to try and start the specific instance for this IMS and see the output.
            try {
                // This throws an IllegalStateException if unable to start the subsystem instance
                // or identify an existing instance on the LPAR.
                startSubsystem(sysv, subsystem);
                return true;
            } catch (IllegalStateException e) {
                return false;
            }
        }

        return false;
    }

    /**
     * Starts an IMS subsystem that is shared between IMS regions on the LPAR.
     *
     * @param sysv SYSVIEW instance used for queries.
     * @param taskName Name of the task to start for the subsystem.
     * @param subsysName Descriptive name of the subsystem.
     * @param alreadyAvailableMessage Message that, when found in the task output, identifies that
     *        the particular IMS subsystem is already available.
     * @throws IOException If unable to start the task or query its output.
     * @throws IllegalStateException If unable to get the IMS subsystem running.
     */
    private void startSharedSubsystem(Sysview sysv, String taskName, String subsysName,
        String alreadyAvailableMessage) throws IOException {
        assert sysv != null;
        assert taskName != null && !taskName.trim().isEmpty();
        assert subsysName != null && !subsysName.trim().isEmpty();
        assert alreadyAvailableMessage != null;

        logger.info("Starting the {} for IMS", subsysName);
        boolean checkOutput = false;
        try {
            // Start the task, it should appear immediately
            MvsTask.start(taskName, null, 15);
            // We have to wait to see if the task stays up.
            Thread.sleep(10_000);
            if (MvsTask.getTaskState(taskName, null) != MvsTask.State.RUNNING) {
                checkOutput = true;
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted while starting the " + subsysName
                + " for IMS", e);
        } catch (TimeoutException e) {
            checkOutput = true;
        }

        if (checkOutput) {
            // If the job didn't come up, or failed to stay up for at least 10 seconds we have to
            // check its output. If we see the text specified by the alreadyAvailableMessage
            // argument we assume that the shared subsystem is already available.
            logger.info("Starting the {} using '{}' failed, checking whether another one is up",
                subsysName, taskName);
            // TODO When the job ABENDs, the output may not be retrieved completely
            // Use OUTPUT jobname, jobnum to capture whole output instead.
            ExecResult jobOutput = sysv.execute(
                "JOBSUM; PREFIX {0}; OWNER; SORT InpDate,D,InpTime,D; LINECMD 'S' *", taskName);
            if (!jobOutput.outputContains(alreadyAvailableMessage)) {
                logger.debug("{}", jobOutput);
                throw new IllegalStateException("Failed to start the " + subsysName + " for IMS");
            }
        }

        logger.info(subsysName + " for IMS is now available");
    }

    /**
     * Starts an IMS subsystem that is specific to the IMS region (not shared).
     *
     * @param taskName Name of the task to start for the subsystem.
     * @param subsysName Descriptive name of the subsystem.
     * @throws Exception If unable to start the task.
     */
    private void startNonSharedSubsystem(String taskName, String subsysName) throws Exception {
        assert taskName != null && !taskName.trim().isEmpty();
        assert subsysName != null && !taskName.trim().isEmpty();

        logger.info("Starting the {} for IMS region {}", subsysName, context.getRegion());
        MvsTask.start(taskName, null);
        logger.info("{} for IMS region {} was started", subsysName, context.getRegion());
    }

    /**
     * Starts the Control Region for the IMS region in the context.
     *
     * @throws Exception If unable to start the region.
     */
    private void startControlRegion(Sysview sysv) throws Exception {
        assert sysv != null;

        logger.info("Starting the Control Region for IMS region {}", context.getRegion());
        String taskName = CONTROL_REGION.getTaskName(context.getVersion());
        MvsTask.start(taskName, null);

        // Give the task a chance to initialize
        Thread.sleep(10_000);
        if (MvsTask.getTaskState(taskName, null) != MvsTask.State.RUNNING) {
            throw new IllegalStateException("Failed to start the Control Region for IMS region "
                + context.getRegion());
        }

        // TODO rewrite to properly check preconditions, trigger actions and wait for results
        // (which differ for the two WTORs)
        String[] wtorReplies = {"/NRE CHKPT 0 FORMAT ALL.", "/ERE OVERRIDE."};
        for (String wtorReply : wtorReplies) {
            String regionState = getControlRegionStatus(sysv);
            switch (regionState) {
                case "ACTIVE":
                    // If already active we're done.
                    return;

                case "STARTING":
                    logger.info("Attempting to activate Control Region for IMS region "
                        + context.getRegion());

                    // Wait until there is a WTOR for the task
                    for (int i = 1; i <= CHECK_RETRY; i++) {
                        List<Map<String, String>> wtors = getTaskWtors(sysv, taskName);
                        if (!wtors.isEmpty()) {
                            break;
                        }
                        logger.warn("No WTOR found in attempt {}", i);
                        Thread.sleep(10_000);
                        if (!getControlRegionStatus(sysv).equals("STARTING")) {
                            throw new IllegalStateException("Control Region isn't starting: "
                                + regionState);
                        }
                    }
                    // If starting we need to reply to the regions WTOR.
                    int replyId = getTaskReplyId(sysv, taskName);
                    replyToWtor(sysv, replyId, wtorReply);
                    // Give the task time to process the reply.
                    Thread.sleep(10_000);
                    break;

                default:
                    // We don't know how to handle any other region state at this point.
                    throw new IllegalStateException("Unexpected Control Region state: "
                        + regionState);
            }
        }

        // If the Control Region still does not come active at this point we failed.
        boolean active = false;
        for (int i = 1; i <= CHECK_RETRY; i++) {
            if (active = getControlRegionStatus(sysv).equals("ACTIVE")) {
                break;
            }
            Thread.sleep(10_000);
        }
        if (!active) {
            throw new IllegalStateException("Unable to activate Control Region for IMS region "
                + context.getRegion());
        }

        logger.info("Control Region for IMS {} is now active", context.getRegion());
    }

    /**
     * Identifies whether the Control Region of the IMS Subsystem is running and active.
     *
     * @param sysv Sysview instance used for queries.
     * @return {@code true} if the Control Region is running and active, {@code false} otherwise.
     * @throws Exception If unable to query region state.
     */
    private boolean isControlRegionRunning(Sysview sysv) throws Exception {
        return isSubsystemRunning(sysv, CONTROL_REGION)
            && getControlRegionStatus(sysv).equals("ACTIVE");
    }

    /**
     * Executes the flow - Starts the IMS region and dependencies.
     */
    @Override
    public void run() throws Exception {
        try (Sysview sysv = new Sysview(context.getSysviewLoadlib())) {
            logger.info("{} IMS region {}", context.isOnlyVerify() ? "Verifying" : "Starting",
                context.getRegion());

            // Inter-Region Lock Manager
            if (!context.isOnlyVerify()) {
                startSubsystem(sysv, INTER_REGION_LOCK_MANAGER);
            } else if (!isSubsystemRunning(sysv, INTER_REGION_LOCK_MANAGER)) {
                throw new IllegalStateException(INTER_REGION_LOCK_MANAGER.getName()
                    + " is not running");
            }

            // Structured Call Interface
            if (!context.isOnlyVerify()) {
                startSubsystem(sysv, STRUCTURED_CALL_INTERFACE);
            } else if (!isSubsystemRunning(sysv, STRUCTURED_CALL_INTERFACE)) {
                throw new IllegalStateException(STRUCTURED_CALL_INTERFACE.getName()
                    + " is not running");
            }

            // Operations Manager
            if (!context.isOnlyVerify()) {
                startSubsystem(sysv, OPERATIONS_MANAGER);
            } else if (!isSubsystemRunning(sysv, OPERATIONS_MANAGER)) {
                throw new IllegalStateException(OPERATIONS_MANAGER.getName() + " is not running");
            }

            // Resource Manager
            if (!context.isOnlyVerify()) {
                startSubsystem(sysv, RESOURCE_MANAGER);
            } else if (!isSubsystemRunning(sysv, RESOURCE_MANAGER)) {
                throw new IllegalStateException(RESOURCE_MANAGER.getName() + " is not running");
            }

            // Control Region
            if (!context.isOnlyVerify()) {
                startControlRegion(sysv);
            } else if (!isControlRegionRunning(sysv)) {
                throw new IllegalStateException(CONTROL_REGION.getName() + " is not running");
            }

            // Message Processing Region
            if (!context.isOnlyVerify()) {
                startSubsystem(sysv, MESSAGE_PROCESSING_REGION);
            } else if (!isSubsystemRunning(sysv, MESSAGE_PROCESSING_REGION)) {
                throw new IllegalStateException(MESSAGE_PROCESSING_REGION.getName()
                    + " is not running");
            }

            // IMS Connect
            if (context.isStartImsConnect()) {
                if (!context.isOnlyVerify()) {
                    startSubsystem(sysv, IMS_CONNECT);
                } else if (!isSubsystemRunning(sysv, IMS_CONNECT)) {
                    throw new IllegalStateException(IMS_CONNECT.getName() + " is not running");
                }
            }

            // External MQ connection
            if (!context.isOnlyVerify()) {
                connectExternalMqSubsystem(sysv);
            } else if (!isMqConnected(sysv)) {
                throw new IllegalStateException("MQ Subsystem is not connected");
            }

            // IMS-MQ Trigger Monitor
            if (!context.isOnlyVerify()) {
                startSubsystem(sysv, TRIGGER_MONITOR);
            } else if (!isSubsystemRunning(sysv, TRIGGER_MONITOR)) {
                throw new IllegalStateException(TRIGGER_MONITOR.getName() + " is not running");
            }

            logger.info("IMS region {} is {}running", context.getRegion(),
                context.isOnlyVerify() ? "" : "now ");
        }
    }
}
