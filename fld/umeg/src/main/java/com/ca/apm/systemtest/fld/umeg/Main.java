package com.ca.apm.systemtest.fld.umeg;

import com.ca.apm.systemtest.fld.common.ErrorUtils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.zeroturnaround.exec.MessageLogger;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;
import org.zeroturnaround.process.PidUtil;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.net.SyslogAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Main class for FLD's Unique MEtricks Generator.
 *
 * @author haiva01
 */
public class Main {
    public static final String METRICS_PER_SECOND_RATE_KEY = "metrics-per-second-rate";
    public static final String RUNNING_TIME_KEY = "running-time";
    public static final double METRICS_PER_BATCH = 100_000.0;
    // For testing:
    //public static final double METRICS_PER_BATCH = 10_000.0;
    public static final String LOGGING_PATTERN
        = "%date %level [%thread] %logger [%file:%line] %msg%n";
    public static final String AGENT_OPTION_KEY = "agent-option";
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final AtomicLong threadPoolId = new AtomicLong(0);
    private final String hostName = StringUtils
        .defaultString(InetAddress.getLocalHost().getCanonicalHostName(),
            InetAddress.getLocalHost().getHostName());
    private final AtomicLong sqlIdCounter = new AtomicLong(0);
    private File pidFile;
    private CommandLine commandLine;
    private boolean verboseLogging = false;
    private ScheduledExecutorService scheduler;
    private JdbcConnectionPool jdbcConnectionPool;
    private long schedulingPeriodUs = 100 * 1000;
    private long metricsPerSecond = 0;
    private String uniqueString = RandomStringUtils.randomAlphanumeric(5);
    private long runningTimeSeconds = 0;
    private Thread mainThread;
    private List<String> agentOptions = new ArrayList<>(10);

    public Main() throws UnknownHostException {
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
            int retVal = new Main().run(args);
            if (retVal != 0) {
                System.exit(retVal);
            }
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(127);
        }
    }

    private static ScheduledExecutorService newScheduledThreadPool(int threads) {
        return Executors.newScheduledThreadPool(threads, new ThreadFactory() {
            private AtomicInteger threadsIdCounter = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setName(threadPoolId.incrementAndGet()
                    + "-scheduler-thread-" + Integer.toString(threadsIdCounter.incrementAndGet()));
                thread.setDaemon(true);
                log.info("Spawned new thread for scheduler: {}", thread.getName());
                return thread;
            }
        });
    }

    private static String getJavaExePath() {
        return Paths.get(System.getProperty("java.home"), "bin",
            "java" + (SystemUtils.IS_OS_WINDOWS ? ".exe" : "")).toAbsolutePath().toString();
    }

    private static String quoteParam(String param) {
        // TODO: This does not deal with other meta characters inside the param argument.
        return '"' + param + '"';
    }

    public static StartedProcess startProcess(final ProcessExecutor pe) {
        StartedProcess process;
        try {
            process = pe.start();
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to start process {1}. Exception: {0}", pe.getCommand());
        }
        return process;
    }

    public static ProcessExecutor newProcessExecutor() {
        return new ProcessExecutor()
            .redirectOutput(Slf4jStream.ofCaller().asInfo())
            .redirectError(Slf4jStream.ofCaller().asError())
            .setMessageLogger(new MessageLogger() {
                @Override
                public void message(Logger log, String format, Object... arguments) {
                    log.info(format, arguments);
                }
            });
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

        Option optAgentParams = Option.builder("a")
            .desc("APM agent options")
            .required(false)
            .hasArgs()
            .argName("option")
            .longOpt("agent-option")
            .build();
        cliOpts.addOption(optAgentParams);

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
            .optionalArg(true)
            .build();
        cliOpts.addOption(optVerbose);

        Option optMetricsPerSecond = Option.builder("r")
            .desc("Number of metrics generated per second")
            .required(false)
            .hasArg()
            .argName("rate")
            .longOpt(METRICS_PER_SECOND_RATE_KEY)
            .build();
        cliOpts.addOption(optMetricsPerSecond);

        Option optRunningTime = Option.builder("t")
            .desc("Running time of this tool")
            .required(false)
            .hasArg()
            .argName("seconds")
            .longOpt(RUNNING_TIME_KEY)
            .build();
        cliOpts.addOption(optRunningTime);

        Option optPrefix = Option.builder("u")
            .desc("Custom unique string")
            .required(false)
            .hasArg()
            .argName("string")
            .longOpt("unique-string")
            .build();
        cliOpts.addOption(optPrefix);

        return cliOpts;
    }

    private void printHelpAndExit(Options cliOpts) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("umeg ", cliOpts, true);
        System.exit(0);
    }

    private void evaluateCommandLine(String[] args) throws ParseException {
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

        if (commandLine.hasOption(AGENT_OPTION_KEY)) {
            agentOptions.addAll(Arrays.asList(commandLine.getOptionValues(AGENT_OPTION_KEY)));
        }

        if (commandLine.hasOption(METRICS_PER_SECOND_RATE_KEY)) {
            metricsPerSecond = Long
                .parseLong(commandLine.getOptionValue(METRICS_PER_SECOND_RATE_KEY));
            schedulingPeriodUs = (long) ((10.0 / ((double) metricsPerSecond)) * 1_000_000.0);
        }

        if (commandLine.hasOption(RUNNING_TIME_KEY)) {
            runningTimeSeconds = Long.parseLong(commandLine.getOptionValue(RUNNING_TIME_KEY));
        }

        if (commandLine.hasOption("unique-string")) {
            uniqueString = commandLine.getOptionValue("unique-string");
        }

        if (commandLine.hasOption("pid-file")) {
            pidFile = new File(commandLine.getOptionValue("pid-file"));
        }

        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory
            .getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        if (commandLine.hasOption("verbose")) {
            verboseLogging = true;
            int verboseLevel = NumberUtils.toInt(commandLine.getOptionValue('v'), 1);
            if (verboseLevel > 1) {
                rootLogger.setLevel(Level.TRACE);
            } else {
                rootLogger.setLevel(Level.DEBUG);
            }
        } else {
            rootLogger.setLevel(Level.INFO);
        }
    }

    private synchronized void stopGenerating() {
        if (scheduler != null) {
            log.info("Stopping scheduler...");
            scheduler.shutdown();
            try {
                scheduler.awaitTermination(5, TimeUnit.SECONDS);
                log.info("Scheduler stopped.");
            } catch (InterruptedException e) {
                ErrorUtils
                    .logExceptionFmt(log, e, "Scheduler failed to shutdown in timely manner.");
            }
            scheduler = null;
        }

        if (jdbcConnectionPool != null) {
            jdbcConnectionPool.dispose();
            jdbcConnectionPool = null;
        }

        mainThread.interrupt();
    }

    private void writePidFile(File pidFile) throws IOException {
        int pid = PidUtil.getMyPid();
        FileUtils.write(pidFile, Integer.toString(pid));
    }

    private void setUpLogging() throws UnknownHostException {
        int pid = PidUtil.getMyPid();

        // Set up basic logging.

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        // Set up Syslog logging on *NIX platforms.

        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory
            .getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);

        if (SystemUtils.IS_OS_UNIX) {
            PatternLayout layout = new PatternLayout();
            layout.setPattern(LOGGING_PATTERN);
            layout.setContext(loggerContext);
            layout.start();

            SyslogAppender syslogAppender = new SyslogAppender();
            syslogAppender.setSyslogHost(InetAddress.getLocalHost().getHostName());
            syslogAppender.setFacility("USER");
            syslogAppender.setLayout(layout);
            rootLogger.addAppender(syslogAppender);
        }

        // Set up rotating log file so that the tool's behaviour is observable even when it is
        // running as a daemon.

        {
            PatternLayout layout = new PatternLayout();
            layout.setPattern(LOGGING_PATTERN);
            layout.setContext(loggerContext);
            layout.start();

            RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
            appender.setContext(loggerContext);
            appender.setLayout(layout);
            appender.setFile("umeg." + pid + ".log");

            SizeAndTimeBasedRollingPolicy<ILoggingEvent> triggeringPolicy
                = new SizeAndTimeBasedRollingPolicy<>();
            triggeringPolicy.setContext(loggerContext);
            triggeringPolicy.setMaxHistory(2);
            triggeringPolicy.setMaxFileSize("10MB");
            triggeringPolicy.setFileNamePattern("umeg." + pid + ".log.%d");
            triggeringPolicy.setParent(appender);
            triggeringPolicy.start();

            appender.setRollingPolicy(triggeringPolicy);
            appender.start();

            rootLogger.addAppender(appender);
        }

        // Remove existing handlers attached to j.u.l root logger

        SLF4JBridgeHandler.removeHandlersForRootLogger();

        // Add SLF4JBridgeHandler to j.u.l's root logger.

        SLF4JBridgeHandler.install();
    }

    private void installShutdownHook() {
        log.info("Installing shutdown hook.");
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                stopGenerating();
            }
        }, "shutdown-hook"));
    }

    private void initializeDbConnectionPool(
        int maxConnections) throws PropertyVetoException, SQLException, IOException {
        String jdbcSpec = "jdbc:h2:mem:test";
        jdbcConnectionPool = JdbcConnectionPool.create(jdbcSpec, "sa", "sa");
        jdbcConnectionPool.setMaxConnections(maxConnections);

        try (Connection connection = jdbcConnectionPool.getConnection();
             Statement stmt = connection.createStatement()) {
            final String SQL_TABLE_CREATION = String.format(Locale.US,
                "CREATE TABLE\n"
                    + "PUBLIC.TEST_%s_TABLE0\n"
                    + "(id BIGINT NOT NULL AUTO_INCREMENT,\n"
                    + "str VARCHAR(25),\n"
                    + "PRIMARY KEY (id))", uniqueString);
            stmt.execute(SQL_TABLE_CREATION);

            final String SQL_INSERT_ONE_ROW = String.format(Locale.US,
                "INSERT INTO PUBLIC.TEST_%s_TABLE0(str) VALUES ('test')", uniqueString);
            stmt.execute(SQL_INSERT_ONE_ROW);
        } catch (SQLException ex) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, ex,
                "Failed table preparation. Exception: {0}");
        }
    }

    private void startGeneratorManager() throws IOException, InterruptedException {
        log.info("I am manager process.");

        final File currentDir = new File(".");

        final double targetMetricCount = runningTimeSeconds * metricsPerSecond;
        long loops;
        long lastBatchSeconds;
        final long batchSeconds = (long) Math.ceil(METRICS_PER_BATCH / metricsPerSecond);
        if (targetMetricCount != 0) {
            loops = (long) Math.floor(targetMetricCount / METRICS_PER_BATCH);
            lastBatchSeconds = (long) Math
                .ceil((targetMetricCount % METRICS_PER_BATCH) / metricsPerSecond);
        } else {
            loops = Long.MAX_VALUE;
            lastBatchSeconds = 0;
        }

        log.debug("Target metric count: {}", targetMetricCount);
        log.debug("Batch seconds: {}", batchSeconds);
        log.debug("Last batch seconds: {}", lastBatchSeconds);
        log.debug("Loops: {}", loops);

        for (long i = 0; i != loops; ++i) {
            log.debug("Loop number {}.", i);
            runGeneratorBatch(currentDir, batchSeconds);
        }

        if (lastBatchSeconds > 0) {
            runGeneratorBatch(currentDir, lastBatchSeconds);
        }
    }

    private void runGeneratorBatch(File currentDir, long batchSeconds) throws InterruptedException {
        List<String> args = new ArrayList<>(10);

        args.add(getJavaExePath());
        args.addAll(agentOptions);

        args.add("-cp");
        args.add(new File(currentDir, "classes").getAbsolutePath());

        args.add("com.ca.apm.systemtest.fld.umeg.Main");

        if (verboseLogging) {
            args.add("-v");
        }

        args.add("-r");
        args.add(Long.toString((long) Math.ceil(metricsPerSecond)));

        args.add("-t");
        args.add(Long.toString(batchSeconds));

        args.add("-u");
        args.add(RandomStringUtils.randomAlphanumeric(5));

        if (pidFile != null) {
            args.add("-p");
            args.add(new File(pidFile.getParentFile(), pidFile.getName() + ".generator")
                .getAbsolutePath());
        }

        ProcessExecutor pe = newProcessExecutor()
            .command(args)
            .directory(currentDir)
            .destroyOnExit();
        StartedProcess process = startProcess(pe);
        Future<ProcessResult> processFuture = process.getFuture();
        final long processWaitSeconds = batchSeconds + 120;
        try {
            log.info("Going to wait for generator process for {} seconds.", processWaitSeconds);
            final ProcessResult processResult = processFuture
                .get(processWaitSeconds, TimeUnit.SECONDS);
            final int exitValue = processResult.getExitValue();
            log.info("Generator process exited: {}", exitValue);
        } catch (ExecutionException e) {
            ErrorUtils.logExceptionFmt(log, e,
                "Generator process execution has failed. Exception: {0}");
        } catch (TimeoutException e) {
            log.warn("Generator process has not finished in expected time of {} seconds.",
                processWaitSeconds);
            log.warn("Process will be killed.");
            process.getProcess().destroy();
            log.warn("Process killed.");
        }
    }

    private void startGenerator() throws PropertyVetoException, SQLException, IOException {
        log.info("I am generator process.");

        initializeDbConnectionPool(2);
        scheduler = newScheduledThreadPool(2);

        log.info("Prefix is '{}'.", uniqueString);
        log.info(
            "Scheduling period of {} ms will produce, over long period of time,"
                + " {} metrics per second,"
                + " {} metrics per minute,"
                + " {} metrics per hour,"
                + " {} metrics per day,"
                + " {} metrics per week.",
            schedulingPeriodUs / 1000.0, metricsPerSecond, metricsPerSecond * 60,
            metricsPerSecond * 60 * 60, metricsPerSecond * 60 * 60 * 24,
            metricsPerSecond * 60 * 60 * 24 * 7);
        scheduler.scheduleAtFixedRate(new Runnable() {
            long startTime = -1;

            /**
             * <p>Single run of this task should generate 10 unique metrics. Each SQL command/query
             * generates five of them: TODO </p>
             */
            @Override
            public void run() {
                if (startTime == -1) {
                    startTime = System.nanoTime();
                }

                final long x = sqlIdCounter.incrementAndGet();
                try (Connection conn = jdbcConnectionPool.getConnection()) {
                    try (Statement statement = conn.createStatement()) {
                        String sql = String.format(Locale.US,
                            "ALTER TABLE PUBLIC.TEST_%s_TABLE%d RENAME TO PUBLIC.TEST_%s_TABLE%d",
                            uniqueString, x - 1, uniqueString, x);
                        if (verboseLogging) {
                            log.trace("Going to rename table: {}", sql);
                        }
                        statement.execute(sql);
                    }

                    String sqlQuery = String
                        .format(Locale.US, "SELECT * FROM PUBLIC.TEST_%s_TABLE%d", uniqueString, x);
                    if (verboseLogging) {
                        log.trace("Going to run query: {}", sqlQuery);
                    }
                    try (PreparedStatement preparedStatement = conn.prepareStatement(sqlQuery);
                         ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            if (verboseLogging) {
                                log.trace("Result: {}", resultSet.getString("str"));
                            }
                        } else {
                            log.warn("Query {} has returned empty result. That is unexpected.",
                                sqlQuery);
                        }
                    }
                } catch (SQLException e) {
                    ErrorUtils.logExceptionFmt(log, e,
                        "Error during query execution or preparation.");
                }

                // Print average generation speed every 10000 metrics generated.

                if (x % (10_000 / 10) == 0) {
                    final long now = System.nanoTime();
                    final double timeSpanSeconds = (now - startTime) / 1_000_000_000.0;
                    double averageMetricsPerSecond = (x * 10.0) / timeSpanSeconds;
                    log.info("Average metrics generation rate is"
                            + " {} metrics per second,"
                            + " {} metrics per minute,"
                            + " {} metrics per hour,"
                            + " {} metrics per day,"
                            + " {} metrics per week.",
                        averageMetricsPerSecond, averageMetricsPerSecond * 60,
                        averageMetricsPerSecond * 60 * 60, averageMetricsPerSecond * 60 * 60 * 24,
                        averageMetricsPerSecond * 60 * 60 * 24 * 7);
                    log.info("We have produced {} metrics so far.", x * 10);
                }
            }
        }, 0, schedulingPeriodUs, TimeUnit.MICROSECONDS);

        if (runningTimeSeconds > 0) {
            log.info("Running time will be {} seconds.", runningTimeSeconds);
            log.info("During that time we will generate {} metrics.",
                runningTimeSeconds * metricsPerSecond);
            scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            stopGenerating();
                        }
                    });
                    thread.setDaemon(true);
                    thread.setName("stopper-thread");
                    thread.start();
                }
            }, runningTimeSeconds, TimeUnit.SECONDS);
        }
    }

    private int run(
        String[] args) throws IOException, PropertyVetoException, SQLException, ParseException {
        // Configure basic logging so that the tool itself can do some output.

        mainThread = Thread.currentThread();

        setUpLogging();

        try {
            // Evaluate command line.

            evaluateCommandLine(args);
            log.debug("My canonical host name: {}", hostName);

            // Install shutdown hook for orderly shutdown.

            installShutdownHook();

            // Write PID file.

            if (pidFile != null) {
                try {
                    writePidFile(pidFile);
                } catch (IOException e) {
                    ErrorUtils
                        .logExceptionFmt(log, e, "Failed to write PID file {1}. Exception: {0}",
                            pidFile.getAbsolutePath());
                    return 4;
                }
            }

            // Start generating.

            if (agentOptions.isEmpty()) {
                startGenerator();

                // Wait forever.
                synchronized (this) {
                    for (; ; ) {
                        try {
                            this.wait();
                        } catch (InterruptedException ex) {
                            log.info("Exiting main thread.");
                            break;
                        }
                    }
                }
            } else {
                startGeneratorManager();
            }
        } catch (Throwable ex) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, ex,
                "Error during execution. Exception: {0}");
        }

        return 0;
    }
}
