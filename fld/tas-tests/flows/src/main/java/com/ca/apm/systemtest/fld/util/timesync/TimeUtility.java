package com.ca.apm.systemtest.fld.util.timesync;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.StringTokenizer;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for managing time.
 */
/*
 * code based on
 * test-projects/emautomation/flows/src/main/java/com/ca/apm/tests/utils/configutils/TimeUtility.java
 */
public class TimeUtility {

    public static final String CA_NTP_SERVER_IP = "141.202.0.25"; // isltime01.ca.com

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeUtility.class);

    /**
     * Sets system time to passed ms time on windows
     * 
     * @param ms
     * @throws ExecuteException
     * @throws IOException
     */
    public static void setTimeThroughWindowsCommandLine(Long ms) throws ExecuteException,
        IOException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(ms);

        StringBuilder time = new StringBuilder();
        time.append(calendar.get(Calendar.HOUR_OF_DAY));
        time.append(":");
        time.append(calendar.get(Calendar.MINUTE));
        time.append(":");
        time.append(calendar.get(Calendar.SECOND));

        CommandLine cmdLine = new CommandLine("cmd.exe");
        cmdLine.addArgument("/c");
        cmdLine.addArgument("time");
        cmdLine.addArgument(time.toString());

        LOGGER.info("About to execute : " + cmdLine);
        DefaultExecutor executor = new DefaultExecutor();
        executor.execute(cmdLine);
    }

    /**
     * Sets system time to passed ms time on linux
     * 
     * @param ms
     * @throws ExecuteException
     * @throws IOException
     */
    public static void setTimeThroughLinuxCommandLine(Long ms) throws ExecuteException, IOException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(ms);

        StringBuilder time = new StringBuilder();
        time.append("\"");
        time.append(calendar.get(Calendar.HOUR_OF_DAY));
        time.append(":");
        time.append(calendar.get(Calendar.MINUTE));
        time.append(":");
        time.append(calendar.get(Calendar.SECOND));
        time.append("\"");

        // date +%T -s "10:13:13"

        CommandLine cmdLine = new CommandLine("date");
        cmdLine.addArgument("+%T");
        cmdLine.addArgument("-s");
        cmdLine.addArgument(time.toString());

        LOGGER.info("About to execute : " + cmdLine);
        DefaultExecutor executor = new DefaultExecutor();
        executor.execute(cmdLine);
    }

    /**
     * Synchronizes time with CA ntp server. Should be quite precise.
     * Though different executions may give different machines +-1s
     * because it doesn't take [ms] , and due to delay in execution.
     * Maximum difference should be 2s, but it is unlikely case.
     * 
     * Can be improved to be more precise with some work, if it's needed.
     * 
     * @author Artur
     * 
     * @throws ExecuteException
     * @throws IOException
     * @throws InterruptedException
     */
    public static void synchronizeTimeWithCAServer() throws Exception {
        if (SystemUtils.IS_OS_WINDOWS) {
            synchronizeWindowsTimeWithCAServer();
        } else if (SystemUtils.IS_OS_LINUX) {
            synchronizeLinuxTimeWithCAServer();
        } else {
            throw new UnsupportedOperationException("OS not supported: " + SystemUtils.OS_NAME);
        }
    }

    private static void synchronizeWindowsTimeWithCAServer() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        CommandLine cmdLine = new CommandLine("cmd.exe");
        cmdLine.addArgument("/c");
        cmdLine.addArgument("w32tm");
        cmdLine.addArgument("/stripchart");
        String ntpServer = "/computer:" + CA_NTP_SERVER_IP;
        cmdLine.addArgument(ntpServer);
        cmdLine.addArgument("/dataonly");
        cmdLine.addArgument("/samples:3");
        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(streamHandler);

        LOGGER.info("About to execute : " + cmdLine);
        executor.execute(cmdLine);
        String output = outputStream.toString();
        LOGGER.info("Result == " + output);

        if (output.contains("error")) {
            throw new RuntimeException(
                "Clock synchronization unsuccesfull, check NTP server availability. Current NTP server: "
                    + CA_NTP_SERVER_IP);
        }

        StringTokenizer st = new StringTokenizer(output, "\n");
        String line = st.nextToken();
        while (!(line.contains("+") || line.contains("-")) || line.contains("current")) {
            line = st.nextToken();
        }
        int secondOffset;
        if (output.contains("+")) {
            secondOffset = Integer.valueOf(line.substring(line.indexOf("+"), line.indexOf(".")));
        } else {
            secondOffset = Integer.valueOf(line.substring(line.indexOf("-"), line.indexOf(".")));
        }
        long msOffset = secondOffset * 1000;
        setTimeThroughWindowsCommandLine(System.currentTimeMillis() + msOffset);
    }

    private static void synchronizeLinuxTimeWithCAServer() throws ExecuteException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        CommandLine cmdLine = new CommandLine("/usr/sbin/ntpdate");
        cmdLine.addArgument(CA_NTP_SERVER_IP);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(streamHandler);

        LOGGER.info("About to execute : " + cmdLine);
        executor.execute(cmdLine);
        final String output = outputStream.toString();
        LOGGER.info("Result == " + output);
    }

}
