package com.ca.apm.systemtest.fld.tattoo.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.ErrorUtils;

/**
 * Created by haiva01 on 13.1.2016.
 */
public abstract class TattooCore {
    public static final int READ_BUFFER_CAPACITY = 2048;
    private static final Logger log = LoggerFactory.getLogger(TattooCore.class);
    private static final long LOGGING_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(5);
    private File loggingConfigurationFile;
    private CommandLine commandLine;
    private Charset characterEncoding;
    private Logger outputLogger;
    private boolean verboseLogging = false;

    protected abstract void configureLoggingSystem(File configurationFile);

    protected abstract void enableVerboseLogging();

    /**
     * Command line parameters parsing setup.
     *
     * @return prepared options parser
     */
    private Options prepareOptionsParser() {
        Options cliOpts = new Options();

        Option optHelp = Option.builder("h")
            .desc("This help")
            .required(false)
            .longOpt("help")
            .build();
        cliOpts.addOption(optHelp);

        Option optFile = Option.builder("c")
            .desc("logging system configuration file (e.g., log4j.properties)")
            .required(false)
            .hasArg()
            .argName("file")
            .longOpt("config")
            .build();
        cliOpts.addOption(optFile);

        Option optInputEncoding = Option.builder("i")
            .desc("input character encoding (default is ISO-8859-1)")
            .required(false)
            .hasArg()
            .argName("encoding")
            .longOpt("input-encoding")
            .build();
        cliOpts.addOption(optInputEncoding);

        Option optLogger = Option.builder("l")
            .desc("output logger name (default is stdout)")
            .required(false)
            .hasArg()
            .argName("name")
            .longOpt("output-logger")
            .build();
        cliOpts.addOption(optLogger);

        Option optVerbose = Option.builder("v")
            .desc("Verbose output")
            .required(false)
            .longOpt("verbose")
            .build();
        cliOpts.addOption(optVerbose);

        return cliOpts;
    }

    private void evaluateCommandLine(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        Options cliOpts = prepareOptionsParser();

        try {
            commandLine = parser.parse(cliOpts, args);
            assert commandLine != null;
        } catch (ParseException e) {
            log.error("command line parsing error", e);
            System.exit(2);
        }

        if (commandLine.hasOption('h')) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("tattoo ", cliOpts, true);
            System.exit(0);
        }

        if (commandLine.hasOption("config")) {
            loggingConfigurationFile = new File(commandLine.getOptionValue("config"));
        }

        characterEncoding = Charset
            .forName(commandLine.getOptionValue("input-encoding", "ISO-8859-1"));

        outputLogger = LoggerFactory.getLogger(commandLine.getOptionValue("logger", "stdout"));

        if (commandLine.hasOption("verbose")) {
            verboseLogging = true;
        }
    }

    private void processInput(@NotNull final LoggingState loggingState,
        @NotNull final CharBuffer charBuffer, final int readLength,
        ExecutorService loggingExecutorService) {

        loggingExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                final long currentTime = System.currentTimeMillis();
                if (log.isDebugEnabled()) {
                    log.debug("Processing output");
                }
                try {
                    char[] rawBuf = charBuffer.array();
                    boolean loopOnce = readLength == 0;

                    while (charBuffer.position() != readLength
                        || loopOnce) {
                        loopOnce = false;

                        // Try to find EOL.

                        int eolIndex = 0;
                        boolean hasEol = false;
                        int index;
                        for (index = charBuffer.position(); index != readLength; ++index) {
                            if (rawBuf[index] == '\n') {
                                eolIndex = index;
                                hasEol = true;
                                break;
                            }
                        }
                        final int position = charBuffer.position();

                        if (hasEol) {
                            // Log whole line if we reached EOL.
                            int lineSize = eolIndex - position /* + 1 to include EOL */;
                            // Strip also \r.
                            if (eolIndex - 1 >= 0
                                && lineSize >= 1
                                && rawBuf[eolIndex - 1] == '\r') {
                                //log.debug("Stripping \\r from input");
                                --lineSize;
                            } else if (loggingState.backlog.length() > 0
                                && loggingState.backlog.charAt(loggingState.backlog.length() - 1)
                                == '\r') {
                                //log.debug("Stripping \\r from backlog");
                                loggingState.backlog.setLength(loggingState.backlog.length() - 1);
                            }
                            if (loggingState.backlog.length() > 0) {
                                // Gather backlog and the relevant part of this buffer and log it.
                                loggingState.backlog
                                    .append(rawBuf, position, lineSize);
                                outputLogger.info(loggingState.backlog.toString());
                                loggingState.backlog.setLength(0);
                            } else {
                                // Or just log this line if the backlog is empty.
                                outputLogger.info(new String(rawBuf, position, lineSize));
                            }
                            // Remember the last time we logged something.
                            loggingState.lastLoggedLineTime = System.currentTimeMillis();
                            charBuffer.position(eolIndex + 1);

                        } else if (currentTime - loggingState.lastLoggedLineTime
                            >= LOGGING_TIMEOUT_MS) {
                            if (log.isDebugEnabled()) {
                                log.debug("No EOL, logging timeout");
                            }

                            // Log even without EOL if we have reached timeout.
                            if (loggingState.backlog.length() > 0) {
                                // Gather backlog and the relevant part of this buffer and log it.
                                loggingState.backlog
                                    .append(rawBuf, position, readLength - position);
                                outputLogger.info("partial line ({} characters): {}",
                                    loggingState.backlog.length(), loggingState.backlog.toString());
                                loggingState.backlog.setLength(0);
                            } else {
                                // There is no backlog so we will try to give the rest of this line
                                // a chance to arrive. Reset the timeout.
                                final int len = readLength - position;
                                if (log.isDebugEnabled()) {
                                    log.debug("Trying to wait for the rest of the line,"
                                        + " adding {} characters to backlog", len);
                                }
                                loggingState.backlog
                                    .append(rawBuf, position, len);
                            }
                            loggingState.lastLoggedLineTime = System.currentTimeMillis();
                            charBuffer.position(index);

                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("No EOL, no timeout");
                            }

                            // No EOL, no timeout, just add this bit to backlog.
                            final int len = readLength - position;
                            if (log.isDebugEnabled()) {
                                log.debug("Adding {} characters to backlog", len);
                            }
                            loggingState.backlog.append(rawBuf, position, len);
                            charBuffer.position(index);
                        }
                    }
                } finally {
                    loggingState.clearAndTrimBuffers();
                }
            }
        });
    }

    protected int run(String[] args) {
        try {
            evaluateCommandLine(args);
            configureLoggingSystem(loggingConfigurationFile);
            if (verboseLogging) {
                enableVerboseLogging();
            }
        } catch (ParseException e) {
            ErrorUtils.logExceptionFmt(log, e, "Error parsing command line option. Exception: {0}");
            return 1;
        }

        try {
            final LoggingState loggingState = new LoggingState();

            final Readable inputStreamReader
                = new InputStreamReader(System.in, characterEncoding);

            final ExecutorService readExecutorService
                = Executors.newFixedThreadPool(2, new WorkersThreadFactory("read executor thread"));
            final CompletionService<TattooTaskResult> completionService
                = new ExecutorCompletionService<>(readExecutorService);

            Runtime.getRuntime().addShutdownHook(
                new Thread(
                    new ShutdownHook(completionService), "Tattoo shutdown hook"));

            final ExecutorService loggingExecutorService
                = Executors
                .newSingleThreadExecutor(new WorkersThreadFactory("logging executor thread"));

            ScheduledExecutorService scheduledExecutorService
                = Executors.newSingleThreadScheduledExecutor(
                new WorkersThreadFactory("scheduled executor thread"));
            scheduledExecutorService
                .scheduleWithFixedDelay(new Flusher(loggingExecutorService, loggingState),
                    LOGGING_TIMEOUT_MS, LOGGING_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            // Prime the loop by submitting first read request.

            if (log.isDebugEnabled()) {
                log.debug("Issuing first read request");
            }
            final Callable<TattooTaskResult> readerTask = new ReaderTask(inputStreamReader);
            completionService.submit(readerTask);

            // Main loop.

        mainloop:
            for (; ; ) {
                Future<TattooTaskResult> completedFuture;
                try {
                    completedFuture = completionService.poll(1, TimeUnit.MINUTES);
                    if (completedFuture == null) {
                        log.info("Nothing on input");
                        continue;
                    }
                } catch (InterruptedException e) {
                    ErrorUtils.logExceptionFmt(log, e,
                        "Got interrupted while polling completion service. Exception: {0}");
                    break;
                }

                TattooTaskResult result;
                try {
                    result = completedFuture.get();
                } catch (InterruptedException e) {
                    ErrorUtils.logExceptionFmt(log, e,
                        "Got interrupted while getting future result. Exception: {0}");
                    return 3;
                } catch (ExecutionException e) {
                    ErrorUtils.logExceptionFmt(log, e,
                        "Got execution exception while polling completion service. Exception: {0}");
                    // Fall through.
                    continue;
                }

                switch (result.type) {
                    case STOP:
                        log.debug("Exiting orderly on signal.");
                        shutdown(readExecutorService, loggingExecutorService,
                            scheduledExecutorService, loggingState);

                        break mainloop;

                    case DATA_READ:
                        if (log.isDebugEnabled()) {
                            log.debug("Read task returned {} bytes", result.readBytes);
                        }

                        if (result.readBytes < 0) {
                            log.debug("Exiting orderly on EOF.");
                            shutdown(readExecutorService, loggingExecutorService,
                                scheduledExecutorService, loggingState);

                            break mainloop;
                        }

                        // First submit a new read.

                        if (log.isDebugEnabled()) {
                            log.debug("Issuing new read request");
                        }
                        completionService.submit(readerTask);

                        // Then process the result.
                        result.charBuffer.flip();
                        processInput(loggingState, result.charBuffer, result.readBytes,
                            loggingExecutorService);

                        continue mainloop;

                    default:
                        throw ErrorUtils
                            .logErrorAndReturnException(log, "Unknown result type: {0}",
                                result.type);
                }
            }
        } catch (Throwable throwable) {
            ErrorUtils.logExceptionFmt(log, throwable, "Exiting on exception: {0}");
            return 2;
        }

        return 0;
    }

    private void shutdown(ExecutorService readExecutorService,
        ExecutorService loggingExecutorService, ExecutorService scheduledExecutorService,
        LoggingState loggingState) {
        log.info("Shutting down");
        scheduledExecutorService.shutdown();
        readExecutorService.shutdownNow();
        loggingExecutorService.shutdown();

        try {
            readExecutorService.awaitTermination(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            ErrorUtils.logExceptionFmt(log, e,
                "Got interrupted while waiting for read executor service"
                    + " shutdown. Exception: {0}");
        }

        try {
            scheduledExecutorService.awaitTermination(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            ErrorUtils.logExceptionFmt(log, e,
                "Got interrupted while waiting for scheduled executor service"
                    + " shutdown. Exception: {0}");
        }

        boolean loggingExecutorServiceTerminated = false;
        try {
            loggingExecutorServiceTerminated
                = loggingExecutorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            ErrorUtils.logExceptionFmt(log, e,
                "Got interrupted while waiting for logging executor service"
                    + " shutdown. Exception: {0}");
        }
        loggingExecutorService.shutdownNow();

        // Synchronously flush backlog if there is any.

        if (loggingExecutorServiceTerminated && loggingState.backlog.length() > 0) {
            outputLogger.info(loggingState.backlog.toString());
        } else if (!loggingExecutorServiceTerminated) {
            log.error("Logging executor service did not terminate. Some logs might be lost.");
        }
    }

    private enum TattooTaskResultType {
        STOP,
        DATA_READ,
    }

    private static class LoggingState {
        public static final int HUGE_LINE_LIMIT = 1024 * 63;
        public static final int INITIAL_BUFFER_SIZE = 1024;

        public StringBuffer backlog = new StringBuffer(INITIAL_BUFFER_SIZE);
        public StringBuffer tmp = new StringBuffer(INITIAL_BUFFER_SIZE);
        public long lastLoggedLineTime = 0;

        public void clearAndTrimBuffers() {
            if (tmp.capacity() > HUGE_LINE_LIMIT) {
                tmp = new StringBuffer(INITIAL_BUFFER_SIZE);
            } else {
                tmp.setLength(0);
            }
            if (backlog.capacity() > HUGE_LINE_LIMIT) {
                backlog.trimToSize();
            }
        }
    }

    private static class TattooTaskResult {
        public static final TattooTaskResult STOP_RESULT
            = new TattooTaskResult(TattooTaskResultType.STOP);
        public final TattooTaskResultType type;
        public final CharBuffer charBuffer;
        public final int readBytes;

        public TattooTaskResult(@NotNull TattooTaskResultType type) {
            this.type = type;
            this.charBuffer = null;
            this.readBytes = 0;

        }

        public TattooTaskResult(@NotNull CharBuffer charBuffer, int readBytes) {
            this.type = TattooTaskResultType.DATA_READ;
            this.charBuffer = charBuffer;
            this.readBytes = readBytes;
        }
    }

    private static class ShutdownHook implements Runnable {
        private static final Logger log = LoggerFactory.getLogger(ShutdownHook.class);
        private final CompletionService<TattooTaskResult> completionService;

        public ShutdownHook(@NotNull CompletionService<TattooTaskResult> completionService) {
            this.completionService = completionService;
        }

        @Override
        public void run() {
            try {
                log.debug("Shutdown hook invoked");
                completionService.submit(new Callable<TattooTaskResult>() {
                    @Override
                    public TattooTaskResult call() throws Exception {
                        return TattooTaskResult.STOP_RESULT;
                    }
                });
            } catch (RejectedExecutionException ex) {
                // Being rejected by the completion service means we are already shutting down.
                // Just ignore the exception.
            }
        }
    }

    private static class ReaderTask implements Callable<TattooTaskResult> {
        private static final Logger log = LoggerFactory.getLogger(ReaderTask.class);
        private final Readable inputStreamReader;

        public ReaderTask(Readable inputStreamReader) {
            this.inputStreamReader = inputStreamReader;
        }

        private static CharBuffer obtainCharBuffer() {
            return CharBuffer.allocate(READ_BUFFER_CAPACITY);
        }

        /**
         * Computes a result, or throws an exception if unable to do so.
         *
         * @return computed result
         * @throws Exception if unable to compute a result
         */
        @Override
        public TattooTaskResult call() throws IOException {
            CharBuffer charBuffer = obtainCharBuffer();
            int readBytes = inputStreamReader.read(charBuffer);
            if (log.isDebugEnabled()) {
                log.debug("Read {} bytes from input.", readBytes);
            }
            return new TattooTaskResult(charBuffer, readBytes);
        }
    }

    private class Flusher implements Runnable {
        private final Logger log = LoggerFactory.getLogger(Flusher.class);
        private final ExecutorService loggingExecutorService;
        private final LoggingState loggingState;

        public Flusher(ExecutorService loggingExecutorService, LoggingState loggingState) {
            this.loggingExecutorService = loggingExecutorService;
            this.loggingState = loggingState;
        }

        @Override
        public void run() {
            if (log.isDebugEnabled()) {
                log.debug("Flush issued");
            }
            processInput(loggingState, CharBuffer.allocate(0), 0,
                loggingExecutorService);
        }
    }
}
