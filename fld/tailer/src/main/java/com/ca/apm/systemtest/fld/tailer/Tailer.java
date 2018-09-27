package com.ca.apm.systemtest.fld.tailer;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.logmonitor.config.LogMonitorConfiguration;
import com.ca.apm.systemtest.fld.logmonitor.config.LogStream;
import com.ca.apm.systemtest.fld.tailer.logmonitor.LogFileMonitor;
import com.ca.apm.systemtest.fld.tailer.logmonitor.LogFileTailer;
import com.ca.apm.systemtest.fld.tailer.logmonitor.LogFileTailerImpl;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.TTCCLayout;
import org.apache.log4j.net.SyslogAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.zeroturnaround.process.PidUtil;

import sun.misc.Signal;
import sun.misc.SignalHandler;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Main class for FLD's tail-like utility.
 *
 * @author haiva01
 */
public class Tailer {
    private static final Logger log = LoggerFactory.getLogger(Tailer.class);
    private final String hostName = StringUtils
        .defaultString(InetAddress.getLocalHost().getCanonicalHostName(),
            InetAddress.getLocalHost().getHostName());
    private File configurationFile;
    private File pidFile;
    private CommandLine commandLine;
    private boolean verboseLogging = false;
    private LogMonitorConfiguration configuration;
    private Map<String, LogFileTailer> tailersMap = new LinkedHashMap<>(10);
    private int fileNotFoundInterval = 300;
    private int numberOfPreviousLines = 5;
    private int maxMatches = 10;
    private Collection<String> emails = new TreeSet<>();

    public Tailer() throws UnknownHostException {
    }

    private static Option helpOption() {
        return Option.builder("h")
            .desc("This help")
            .required(false)
            .longOpt("help")
            .build();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        try {
            int retVal = new Tailer().run(args);
            if (retVal != 0) {
                System.exit(retVal);
            } else {
                // Wait forever.
                synchronized (Tailer.class) {
                    for (; ; ) {
                        Tailer.class.wait();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            System.exit(127);
        }
    }

    private Options pepareHelpOptionsParser() {
        Options cliOpts = new Options();
        cliOpts.addOption(helpOption());
        return cliOpts;
    }

    /**
     * Command line parameters parsing setup.
     *
     * @return prepared options parser
     */
    private Options prepareOptionsParser() {
        Options cliOpts = new Options();

        cliOpts.addOption(helpOption());

        Option optFile = Option.builder("c")
            .desc("Configuration file")
            .required(true)
            .hasArg()
            .argName("file")
            .longOpt("config")
            .build();
        cliOpts.addOption(optFile);

        Option optPidFile = Option.builder("p")
            .desc("PID file")
            .required(false)
            .hasArg()
            .argName("file")
            .longOpt("pid-file")
            .build();
        cliOpts.addOption(optPidFile);

        Option optVerbose = Option.builder("v")
            .desc("Verbose output")
            .required(false)
            .longOpt("verbose")
            .build();
        cliOpts.addOption(optVerbose);

        Option optFileNotFoundInterval = Option.builder()
            .desc("Check interval after file is not found")
            .required(false)
            .longOpt("file-not-found-interval")
            .hasArg()
            .argName("seconds")
            .build();
        cliOpts.addOption(optFileNotFoundInterval);

        Option optNumberOfPrevLines = Option.builder("n")
            .desc("Number of previous lines")
            .required(false)
            .longOpt("prev-lines")
            .hasArg()
            .argName("count")
            .build();
        cliOpts.addOption(optNumberOfPrevLines);

        Option optMaxMatches = Option.builder("m")
            .desc("Maximum number of matches per period")
            .required(false)
            .longOpt("max-matches")
            .hasArg()
            .argName("count")
            .build();
        cliOpts.addOption(optMaxMatches);

        Option optEmails = Option.builder("t")
            .desc("Deliver log file events notifications to these emails")
            .required(false)
            .longOpt("email-to")
            .hasArgs()
            .argName("email")
            .build();
        cliOpts.addOption(optEmails);

        return cliOpts;
    }

    private void printHelpAndExit(Options cliOpts) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("tailer ", cliOpts, true);
        System.exit(0);
    }

    private void evaluateCommandLine(String[] args) {
        CommandLineParser parser = new DefaultParser();
        Options cliOpts = prepareOptionsParser();
        Options helpOpts = pepareHelpOptionsParser();

        try {
            commandLine = parser.parse(helpOpts, args, true);
            if (commandLine.hasOption('h')) {
                printHelpAndExit(cliOpts);
            }

            commandLine = parser.parse(cliOpts, args);
            assert commandLine != null;
        } catch (ParseException e) {
            ErrorUtils.logExceptionFmt(log, e, "Command line parsing error: {0}");
            System.exit(1);
        }

        if (commandLine.hasOption('h')) {
            printHelpAndExit(cliOpts);
        }

        if (commandLine.hasOption("config")) {
            configurationFile = new File(commandLine.getOptionValue("config"));
        }

        if (commandLine.hasOption("pid-file")) {
            pidFile = new File(commandLine.getOptionValue("pid-file"));
        }

        if (commandLine.hasOption("verbose")) {
            verboseLogging = true;
            org.apache.log4j.Logger.getRootLogger().setLevel(Level.DEBUG);
        } else {
            org.apache.log4j.Logger.getRootLogger().setLevel(Level.INFO);
        }

        if (commandLine.hasOption("file-not-found-interval")) {
            fileNotFoundInterval = Integer
                .valueOf(commandLine.getOptionValue("file-not-found-interval"));
        }

        if (commandLine.hasOption("prev-lines")) {
            numberOfPreviousLines = Integer.valueOf(commandLine.getOptionValue("prev-lines"));
        }

        if (commandLine.hasOption("max-matches")) {
            maxMatches = Integer.valueOf(commandLine.getOptionValue("max-matches"));
        }

        if (commandLine.hasOption("email-to")) {
            emails.addAll(Arrays.asList(commandLine.getOptionValues("email-to")));
            log.info("Log events will go to these emails: {}", emails);
        }
    }

    private LogFileTailer createTailer(String streamId, LogStream logStream) throws IOException {
        File logFile = new File(logStream.getFileName());
        LogFileMonitor listener = new LogFileMonitor(hostName, fileNotFoundInterval,
            numberOfPreviousLines, logStream, emails);
        LogFileTailer tailer = new LogFileTailerImpl(logFile, listener);
        if (tailersMap.put(streamId, tailer) != null) {
            log.error("Stream ID {} is already present in tailers map!", streamId);
            tailer.stop();
        }
        return tailersMap.get(streamId);
    }

    private synchronized void startMonitoring(LogMonitorConfiguration configuration)
        throws IOException {
        for (Map.Entry<String, LogStream> streamEntry : configuration.getLogStreams().entrySet()) {
            String streamId = streamEntry.getKey();
            log.info("Starting monitoring for stream {}", streamId);
            createTailer(streamEntry.getKey(), streamEntry.getValue());
        }
    }

    private synchronized void stopMonitoring() {
        for (Map.Entry<String, LogFileTailer> entry : tailersMap.entrySet()) {
            String streamId = entry.getKey();
            LogFileTailer tailer = entry.getValue();
            log.info("Stopping log monitoring for stream {}", streamId);
            tailer.stop();
        }
        tailersMap.clear();
    }

    private void writePidFile(File pidFile) throws IOException {
        int pid = PidUtil.getMyPid();
        FileUtils.write(pidFile, Integer.toString(pid));
    }

    private void setUpLogging() throws UnknownHostException {
        // Set up basic logging.

        BasicConfigurator.configure();

        // Set up Syslog logging on *NIX platforms.

        if (SystemUtils.IS_OS_UNIX) {
            org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
            rootLogger.addAppender(
                new SyslogAppender(new TTCCLayout(), InetAddress.getLocalHost().getHostName(),
                    SyslogAppender.LOG_USER));
        }

        // Remove existing handlers attached to j.u.l root logger

        SLF4JBridgeHandler.removeHandlersForRootLogger();

        // Add SLF4JBridgeHandler to j.u.l's root logger.

        SLF4JBridgeHandler.install();

        // Print over SLF4J.

        SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
    }

    private void installSignalHandlers() {
        if (SystemUtils.IS_OS_UNIX) {
            log.debug("Installing SIGHUP handler for configuration refresh.");
            sun.misc.Signal.handle(new Signal("HUP"), new SignalHandler() {
                @Override
                public void handle(Signal signal) {
                    assert signal.getName().equals("HUP");

                    // Stop monitoring.

                    stopMonitoring();

                    // Re-load configuration file.

                    LogMonitorConfiguration tmpConfiguration = null;
                    try {
                        tmpConfiguration = readConfigurationFile(configurationFile);
                    } catch (Exception e) {
                        ErrorUtils.logExceptionFmt(log, e,
                            "Failed to re-load configuration file {1}. Exception: {0}",
                            configurationFile.getAbsolutePath());
                    }
                    if (tmpConfiguration != null) {
                        configuration = tmpConfiguration;
                    }

                    // Re-start monitoring.

                    try {
                        startMonitoring(configuration);
                    } catch (IOException e) {
                        ErrorUtils.logExceptionFmt(log, e,
                            "Log monitoring could not be re-started after configuration reload. "
                                + "Exception: {0}");
                    }
                }
            });
        }
    }

    private void installShutdownHook() {
        log.info("Installing shutdown hook.");
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                stopMonitoring();
            }
        }, "shutdown-hook"));
    }

    LogMonitorConfiguration readConfigurationFile(File configurationFile) {
        ObjectMapper om = new ObjectMapper();
        om.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        om.enable(JsonParser.Feature.ALLOW_COMMENTS);
        om.enable(JsonParser.Feature.ALLOW_YAML_COMMENTS);

        try {
            return om.readValue(configurationFile, LogMonitorConfiguration.class);
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to parse configuration file {1}. Exception: {0}",
                configurationFile.getAbsolutePath());
        }
    }

    private int run(String[] args) throws UnknownHostException {
        // Configure basic logging so that the tool itself can do some output.

        setUpLogging();

        // Evaluate command line.

        evaluateCommandLine(args);

        // Read configuration JSON file.

        configuration = readConfigurationFile(configurationFile);

        // Start monitoring files using parsed configuration.

        log.debug("My canonical host name: {}", hostName);
        try {
            startMonitoring(configuration);
        } catch (IOException e) {
            ErrorUtils.logExceptionFmt(log, e,
                "Failed to start monitoring. Exception: {0}");
            return 3;
        }

        // Start handling signals on POSIX platforms for configuration refresh.

        installSignalHandlers();

        // Install shutdown hook for orderly shutdown.

        installShutdownHook();

        // Write PID file.

        if (pidFile != null) {
            try {
                writePidFile(pidFile);
            } catch (IOException e) {
                ErrorUtils.logExceptionFmt(log, e, "Failed to write PID file {1}. Exception: {0}",
                    pidFile.getAbsolutePath());
                return 4;
            }
        }

        return 0;
    }

}
