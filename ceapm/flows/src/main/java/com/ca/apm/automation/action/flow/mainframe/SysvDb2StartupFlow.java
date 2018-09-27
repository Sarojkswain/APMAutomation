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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.utils.mainframe.sysvdb2.Group;
import com.ca.apm.automation.utils.mainframe.sysvdb2.Instance;
import com.ca.apm.automation.utils.mainframe.sysvdb2.MfSystem;
import com.ca.apm.automation.utils.mainframe.sysview.Sysview;
import com.ca.apm.automation.utils.mainframe.sysview.Sysview.ExecResult;
import com.ca.apm.automation.utils.mainframe.sysview.TabularData;

/**
 * Flow that either identifies an already running SYSVDB2 instance or starts a new one that matches
 * the requested version.
 * <p>
 * This role works with instances as described <a href='http://ca31:6600'>here</a>.
 * </p>
 *
 * <p>
 * Once an instance is identified its connection information is saved to a properties file under the
 * specified path.
 * </p>
 */
@Flow
public class SysvDb2StartupFlow implements IAutomationFlow {
    private static final Logger logger = LoggerFactory.getLogger(SysvDb2StartupFlow.class);

    @FlowContext
    private SysvDb2StartupFlowContext context;

    /**
     * Saves connection parameters of a SYSVDB2 instance into a properties file identified by the
     * context.
     *
     * @param instance Instance to save parameters from.
     * @throws IOException When unable to write the properties file.
     */
    private void saveInstanceProperties(Instance instance) throws IOException {
        assert instance != null;

        String filePath = context.getPropertiesFilePath();

        logger.info("Saving SYSVDB2 ({}) properties to {}", instance, filePath);
        File file = new File(filePath);
        try (FileWriter writer = new FileWriter(file)) {
            Properties properties = instance.getProperties();
            properties.store(writer, "SYSVDB2 connection properties for instance: " + instance);
            // We need to make the file writabe for all because it is never removed and in some
            // cases the process that updates the file runs under a different user.
            if (!file.setWritable(true, true)) {
                logger.warn("Failed to make the '{}' properties file writable", filePath);
            }
        } catch (IOException e) {
            throw new IOException(
                "Unable to store instance properties for SYSVDB2 (" + instance + ")", e);
        }
    }

    /**
     * Executes the flow - Identify or start a matching SYSVIEW for DB2 instance.
     */
    @Override
    public void run() throws Exception {
        try (Sysview sysv = new Sysview(context.getSysviewLoadlib())) {
            Instance unused = null;

            boolean subsystemMonitored = false;
            final String subsystem = context.getSubsystem();
            if (subsystem != null) {
                subsystemMonitored = Instance.isSubsystemMonitored(subsystem, sysv);

                // Fail early if we're only verifying and a required subsystem is not monitored
                if (!subsystemMonitored && context.isOnlyVerify()) {
                    throw new IllegalStateException("Subsystem " + subsystem + " is not monitored");
                }
            }

            logger.info("Looking through active SYSVDB2 instances for a match");
            for (Group group : Group.values()) {
                String taskName = Instance.getRuntimeTaskName(group);

                ExecResult jobs = sysv.execute("LISTJOBS ,All,Execute; OWNER; PREFIX {0}", taskName);
                TabularData td = jobs.getTabularData();
                for (MfSystem system : MfSystem.values()) {
                    Map<String, String> match = td.getFirstRowMatching("Esys", system.name);
                    if (match == null) { // The instance isn't running, skip to the next one.
                        logger.info("No SYSVDB2 instance is running @ {}:{}",
                            system.host, group.port);
                        if (unused == null) { // Save the first unused instance for later.
                            unused = new Instance(group, system, context.getVersion());
                        }
                        continue;
                    }

                    // Get the instance since its running and check the version
                    Instance instance = Instance.fromRuntime(group, system, sysv);
                    assert instance != null; // Should throw otherwise
                    logger.debug("Identified running SYSVDB2 instance: {}", instance);

                    if (instance.getVersion() != context.getVersion()) {
                        logger.debug("Instance is not of the required version {} - ignoring",
                            context.getVersion());
                        continue;
                    }

                    if (subsystem != null) {
                        if (!subsystemMonitored) {
                            if (!instance.monitorSubsystem(context.getSubsystem(), sysv)) {
                                logger.debug("Failed to start monitoring subsystem {} - ignoring",
                                    subsystem);
                                continue;
                            }
                        } else if (!instance.isMonitoringSubsystem(subsystem, sysv)) {
                            logger.debug("Instance is not monitoring subsystem {} - ignoring",
                                subsystem);
                            continue;
                        }
                    }

                    logger.info("SYSVDB2 instance ({}) matches all requirements", instance);
                    saveInstanceProperties(instance);
                    return;
                }
            }

            if (context.isOnlyVerify()) {
                throw new IllegalStateException(
                    "Did not find a running SYSVDB2 instance matching requirements");
            } else {
                logger.info("Did not find a running SYSVDB2 instance matching requirements");
            }

            if (unused == null) {
                throw new IllegalStateException(
                    "No unused jobs found, unable to start new SYSVDB2 instance");
            }

            logger.info("Starting new SYSVDB2 instance ({})", unused);
            unused.start(sysv);
            logger.info("SYSVDB2 instance ({}) has been started", unused);

            if (subsystem != null) {
                if (!unused.monitorSubsystem(subsystem, sysv)) {
                    logger.debug("Failed to start monitoring subsystem {} - ignoring", subsystem);
                    return;
                }
            }

            logger.info("SYSVDB2 instance ({}) was initialized according to requirements", unused);
            saveInstanceProperties(unused);
        }
    }
}
