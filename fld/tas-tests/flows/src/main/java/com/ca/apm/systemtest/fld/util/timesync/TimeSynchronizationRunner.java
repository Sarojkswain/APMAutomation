package com.ca.apm.systemtest.fld.util.timesync;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.ca.apm.systemtest.fld.common.ErrorUtils;

public class TimeSynchronizationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeSynchronizationRunner.class);

    private static final String TIME_SYNCHRONIZATION_RUNNER = "TimeSynchronizationRunner";

    private static final long DEFAULT_WAIT_INTERVAL = 60000L; // 1 min.

    private static final String DEFAULT_WORK_DIR = ".";

    private static ArgumentParser parser;

    private static long waitInterval = DEFAULT_WAIT_INTERVAL;

    private static String workDir = DEFAULT_WORK_DIR;


    private TimeSynchronizationRunner() {}

    public static void main(String[] args) {
        initLog();
        LOGGER.info("TimeSynchronizationRunner.main():: entry");
        try {
            // parse arguments
            LOGGER.info("TimeSynchronizationRunner.main():: parsing arguments");
            Namespace namespace = parseArgs(args);
            initVariables(namespace);
            int iteration = 1;
            System.out.println(TIME_SYNCHRONIZATION_RUNNER);

            while (true) {
                LOGGER.info(
                    "TimeSynchronizationRunner.main():: iteration {}: start time synchronization",
                    iteration);
                // time synchronization
                try {
                    timeSynchronization();
                } catch (Exception e) {
                    ErrorUtils
                        .logExceptionFmt(LOGGER, e,
                            "TimeSynchronizationRunner.main():: cannot perform time synchronization: {0}");
                }
                LOGGER.info("TimeSynchronizationRunner.main():: iteration {}: waiting", iteration);
                sleep(waitInterval);
                iteration++;
            }
        } catch (Exception e) {
            throw ErrorUtils.logExceptionAndWrap(LOGGER, e,
                "TimeSynchronizationRunner.main():: exception occurred: {0}");
        } finally {
            LOGGER.info("TimeSynchronizationRunner.main():: exit");
        }
    }

    private static void timeSynchronization() {
        try {
            TimeUtility.synchronizeTimeWithCAServer();
            LOGGER
                .info("TimeSynchronizationRunner.timeSynchronization():: DONE synchronize time with CA server");
        } catch (Exception e) {
            ErrorUtils
                .logExceptionAndWrap(LOGGER, e,
                    "TimeSynchronizationRunner.timeSynchronization():: exception during time synchronization: {0}");
        }
    }

    private static Namespace parseArgs(String[] args) {
        parser =
            ArgumentParsers.newArgumentParser(TimeSynchronizationRunner.class.getName())
                .description(
                    "TimeSynchronizationRunner - synchronizes OS time via NTP periodically");

        parser.addArgument("-w", "-waitInterval").dest("waitInterval").type(Long.class)
            .action(Arguments.store()).help("Wait interval").setDefault(DEFAULT_WAIT_INTERVAL);

        parser.addArgument("-d", "-workDir").dest("workDir").type(String.class)
            .action(Arguments.store()).help("Working dir").setDefault(DEFAULT_WORK_DIR);

        Namespace namespace = parser.parseArgsOrFail(args);
        LOGGER.debug("TimeSynchronizationRunner.parseArgs():: namespace = {}", namespace);
        return namespace;
    }

    private static void initVariables(Namespace namespace) {
        waitInterval = namespace.getLong("waitInterval");
        workDir = namespace.getString("workDir");

        Args.notNegative(waitInterval, "waitInterval");
        Args.notBlank(workDir, "workDir");
    }

    private static void sleep(long sleepTime) {
        try {
            LOGGER.info("TimeSynchronizationRunner.sleep():: sleeping for {} [s]",
                (sleepTime / 1000));
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            LOGGER.debug("TimeSynchronizationRunner.sleep():: InterruptedException");
        }
    }

    private static void initLog() {
        RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
        String pid = rt.getName();
        MDC.put("PID", pid);
    }

}
