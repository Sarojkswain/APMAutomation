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

package com.ca.apm.automation.utils.mainframe.sysvdb2;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.utils.mainframe.sysview.Sysview;
import com.ca.apm.automation.utils.mainframe.sysview.Sysview.ExecResult;
import com.ca.apm.automation.utils.mainframe.sysview.Sysview.Rc;
import com.ca.apm.automation.utils.mainframe.sysview.TabularData;

/**
 * Represents a SYSVDB2 instance uniquely identified by the system, group, and version.
 */
public class Instance {
    private static final Logger logger = LoggerFactory.getLogger(Instance.class);

    private static final String TASK_PREFIX = "PTX";
    private static final String XNET_EXECUTION_SPACE_SUFFIX = "XES";
    private static final String DATA_COLLECTOR_SUFFIX = "IDC";
    private static final Pattern initPattern = Pattern
        .compile(".*CA Xnet initialization complete - Release (\\d+.\\d+).*");
    private static final Pattern dcPattern = Pattern
        .compile(".*INITIALIZATION IS COMPLETE FOR DATA COLLECTOR FOR DB2 SUBSYSTEM (.*)$");

    private Group group;
    private MfSystem system;
    private Version version;

    /**
     * Constructor using explicit values.
     *
     * @param group SYSVDB2 group.
     * @param system Mainframe system.
     * @param version SYSVDB2 version.
     */
    public Instance(Group group, MfSystem system, Version version) {
        assert group != null;
        assert system != null;
        assert version != null;

        this.group = group;
        this.system = system;
        this.version = version;
    }

    /**
     * Factory that can generate instances based on JOB output for a running group/system
     * combination.
     *
     * @param group SYSVDB2 group.
     * @param system Mainframe system.
     * @param sysv SYSVIEW instance used for queries.
     * @return Instance.
     * @throws IOException When unable to read or parse the job output.
     */
    public static Instance fromRuntime(Group group, MfSystem system, Sysview sysv)
        throws IOException {
        Version version = null;
        String taskName = getRuntimeTaskName(group);

        ExecResult result = sysv.execute(
            "LISTJOBS ,All,Execute; OWNER; PREFIX {0}; SELECT Esys EQ {1}; LINECMD 'S' *",
            taskName, system.name);

        for (String line : result.getOutput()) {
            Matcher matcher = initPattern.matcher(line);
            if (matcher.matches()) {
                version = Version.fromString(matcher.group(1));
            }
        }

        if (version == null) {
            throw new IOException("Failed to determine instance version from task output");
        }

        return new Instance(group, system, version);
    }

    /**
     * Returns the name of the DB2 subsystem monitored by a Job.
     * Typically the job passed in should be one for a DB2 data collector.
     *
     * @param jobName Name of the Job.
     * @param jobNumber Job number.
     * @param sysv SYSVIEW instance used for queries.
     * @return Name of the monitored DB2 subsystem or null if none could be identified.
     * @throws IOException When unable to read or parse the job output.
     */
    public static String getSubsystemMonitoredByJob(String jobName, String jobNumber, Sysview sysv) throws IOException {
        logger.debug("Checking collector job {},{}", jobName, jobNumber);

        ExecResult jobOutput = sysv.execute("OUTPUT {0},{1}", jobName, String.valueOf(jobNumber));
        for (String line : jobOutput.getOutput()) {
            Matcher matcher = dcPattern.matcher(line);
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }

        return null;
    }

    /**
     * Returns whether a specific DB2 subsystem is monitored by any data collector.
     *
     * @param subsystem DB2 subsystem to look for.
     * @param sysv SYSVIEW instance used for queries.
     * @return {@link true} if the subsystem is monitored, {@link false} otherwise.
     * @throws IOException When unable to read or parse job outputs.
     */
    public static boolean isSubsystemMonitored(String subsystem, Sysview sysv) throws IOException {
        logger.info("Checking whether subsystem {} is monitored by any collector", subsystem);

        for (Group group : Group.values()) {
            final String jobName = getCollectorTaskName(group);
            ExecResult jobList = sysv.execute("LISTJOBS ,All,Execute; OWNER; PREFIX {0}", jobName);

            TabularData td = jobList.getTabularData();
            for (Map<String, String> row : td.getAllRows()) {
                String jobNumber =  row.get("JobNr");
                String system = row.get("Esys");
                if (jobNumber == null) {
                    continue;
                }

                String monitoredSubsystem = getSubsystemMonitoredByJob(jobName, jobNumber, sysv);
                if (monitoredSubsystem == null) {
                    logger.warn("Failed to identify subsystem monitored by collector job {},{}",
                        jobName, jobNumber);
                } else {
                    logger.debug("Collector job {},{} is monitoring the {} subsystem on {}",
                        jobName, jobNumber, monitoredSubsystem, system);
                    if (monitoredSubsystem.equals(subsystem)) {
                        logger.info("The queried subsystem {} is being monitored", subsystem);
                        return true;
                    }
                }
            }
        }

        logger.info("The queried subsystem {} is not being monitored", subsystem);
        return false;
    }

    /**
     * Identifies whether the instance is fully initialized.
     *
     * @param sysv SYSVIEW instance used for queries.
     * @return true if initialized, false otherwise.
     * @throws IOException If unable to determine the state of instance.
     */
    public boolean isInitialized(Sysview sysv) throws IOException {
        String taskName = getRuntimeTaskName(group);

        ExecResult jobOutput = sysv.execute(
            "LISTJOBS ,All,Execute; OWNER; PREFIX {0}; SELECT Esys EQ {1}; LINECMD 'S' *",
            taskName, system.name);

        return jobOutput.outputContains(initPattern);
    }

    /**
     * Starts the tasks related to the instance and waits for its initialization to complete.
     *
     * @param sysv SYSVIEW instance used for queries.
     * @throws InterruptedException If interrupted while waiting for the instance to initialize.
     * @throws TimeoutException If the method times out while waiting for the instance to
     *         initialize.
     * @throws IOException If the method fails to start or query task statuses.
     */
    public void start(Sysview sysv) throws InterruptedException, TimeoutException, IOException {
        String jclMember = TASK_PREFIX + group.taskSuffix + system.taskSuffix + version.taskSuffix;
        try {
            // This JCL is just a wrapper that starts other tasks so all we care about here is
            // that it was submitted correctly, we check/wait for its side effects below.
            Rc rc = sysv.execute("MVS START {0}", jclMember).getRc();
            if (!rc.isOk()) {
                throw new IllegalStateException("Failed to start SYSVDB2 instance (" + this
                    + ") using JCL " + jclMember + ": rc=" + rc);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start SYSVDB2 instance (" + this
                + ") using JCL " + jclMember, e);
        }

        logger.info("Waiting for SYSVDB2 instance to initialize");
        // SYSVDB2 can take a long time to come up, we give it three minutes
        long limit = System.nanoTime() + 180_000_000_000L;
        boolean initialized;
        do {
            Thread.sleep(10_000);
            initialized = isInitialized(sysv);
        } while (!initialized && System.nanoTime() < limit);

        if (!initialized) {
            throw new TimeoutException("Failed to initialize SYSVDB2 instance (" + this + ").");
        }
    }

    /**
     * Returns whether the instance is monitoring a specific DB2 subsystem.
     *
     * @param subsystem DB2 subsystem to look for.
     * @param sysv SYSVIEW instance used for queries.
     * @return {@link true} if the instance is monitoring the DB2 subsystem {@link false} otherwise.
     * @throws IOException If querying jobs or their outputs fails.
     */
    public boolean isMonitoringSubsystem(String subsystem, Sysview sysv) throws IOException {
        logger.info("Checking whether {} is monitoring subsystem {}", this, subsystem);

        String jobName = getCollectorTaskName(group);
        ExecResult jobs = sysv.execute(
            "LISTJOBS ,All,Execute; OWNER; PREFIX {0}; SELECT Esys EQ {1}", jobName, system.name);

        TabularData td = jobs.getTabularData();
        for (Map<String, String> row : td.getAllRows()) {
            String jobNumber =  row.get("JobNr");
            if (jobNumber == null) {
                continue;
            }

            String monitoredSubsystem = getSubsystemMonitoredByJob(jobName, jobNumber, sysv);
            if (monitoredSubsystem != null && monitoredSubsystem.equals(subsystem)) {
                logger.info("The queried subsystem {} is being monitored by {}", subsystem, this);
                return true;
            }
        }

        return false;
    }

    /**
     * Starts data collection with the instance for the specified DB2 subsystem.
     *
     * @param subsystem DB2 subsystem to monitor.
     * @return {@link true} if started successfully, {@link false} otherwise.
     */
    public boolean monitorSubsystem(String subsystem, Sysview sysv) {
        logger.info("Will start collector for {} to monitor subsystem {}", this, subsystem);

        try {
            // /S PTXnnIDC.PTXnnddd,DBSUB=dddd
            sysv.execute("/S {0}.{1}{2}{3},DBSUB={4}", getCollectorTaskName(group),
                TASK_PREFIX, group.taskSuffix, subsystem.substring(1), subsystem);

            logger.info("Waiting for started collector to initialize");
            for (int i = 0; i < 3; ++i) {
                Thread.sleep(20_000);

                if (isMonitoringSubsystem(subsystem, sysv)) {
                    return true;
                }
            }
            return false;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    /**
     * Returns the name of the task for the Execution Space for a specific group instance.
     *
     * @param group SYSVDB2 group.
     * @return Task name.
     */
    public static String getRuntimeTaskName(Group group) {
        return TASK_PREFIX + group.taskSuffix + XNET_EXECUTION_SPACE_SUFFIX;
    }

    /**
     * Returns the name of the task(s) of the data collectors for a specific group instance.
     *
     * @param group SYSVDB2 group.
     * @return Task name.
     */
    public static String getCollectorTaskName(Group group) {
        return TASK_PREFIX + group.taskSuffix + DATA_COLLECTOR_SUFFIX;
    }

    /**
     * Returns connection properties for the instance.
     *
     * @return Properties.
     */
    public Properties getProperties() {
        Properties properties = new Properties();

        properties.setProperty("host", system.host);
        properties.setProperty("port", String.valueOf(group.port));
        properties.setProperty("passticket", group.passticket);

        return properties;
    }

    public Group getGroup() {
        return group;
    }

    public MfSystem getSystem() {
        return system;
    }

    public Version getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "r" + version.version + " @ " + system.host + ":" + group.port;
    }
}
