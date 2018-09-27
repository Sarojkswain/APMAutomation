/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.logmonitor;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.logmonitor.config.LogStream;
import com.ca.apm.systemtest.fld.logmonitor.config.Rule;
import com.ca.apm.systemtest.fld.logmonitor.config.Rule.RuleMatchResult;

/**
 * @author KEYJA01
 *
 */
public class LogFileMonitor implements LogFileTailerListener {
    private Logger log = LoggerFactory.getLogger(LogFileMonitor.class);

    private LogMonitorPluginImpl pluginImpl;
    private long fnfInterval;
    private String hostname;
    private List<Rule> rules;
    private int concatLines;
    private int waitForLines;

    private int conquare=0;
    private StringBuilder buffer = null;
    private Date startedAt;

    // Status fields
    private String canonicalPath = null;
    private long lastFnf = 0;

    String[] prevLines = null;
    int prevLinesPointer = 0;

    public LogFileMonitor(LogMonitorPluginImpl pluginImpl, String hostname, int fnfInterval, int prevLines, LogStream stream) {
        this.pluginImpl = pluginImpl;
        this.hostname = hostname;
        this.fnfInterval = fnfInterval * 1000l;
        this.rules = stream.getRules();
        this.concatLines = stream.getConcatLines();
        this.waitForLines = stream.getWaitForLines();
        this.prevLines = new String[prevLines];
    }

    @Override
    public void init(LogFileTailer tailer) {
        try {
            canonicalPath = tailer.getFile().getCanonicalPath();
        } catch (IOException e) {
            final String msg = MessageFormat.format(
                "Failed to get canonical path for {1}. Exception: {0}",
                e.getMessage(), tailer.getFile().toString());
            log.error(msg, e);
        }
    }

    private static long count = 0;
    @Override
    public void handle(String line) {
        if (count++ % 10000 == 0) {
            System.out.println("Processed " + count + " lines");
        }
        log.debug("Line: " + line);
        
        for (Rule r: rules) {
            RuleMatchResult matchResult = r.matches(line);
            if (conquare == 0 && matchResult == RuleMatchResult.MatchesInclude) {
                log.debug("Matched line");
                conquare = 1;
                buffer = new StringBuilder(1024 * 10);
                buffer.append("Matched rule ").append(r).append("\n");
                buffer.append("   *** for line ***\n");
                buffer.append(line).append("\n");
                buffer.append(" ------------------\n");
                embedPrevLines(buffer);
                startedAt = new Date();
                startTimeout();
            }
            if (matchResult == RuleMatchResult.MatchesIgnore || matchResult == RuleMatchResult.MatchesInclude) {
                // stop checking rules if we have a match
                break;
            }
        }
        
        if (conquare > 0) {
            buffer.append(line).append("\n");
            conquare ++;
        } else if (prevLines.length > 0) {
            prevLinesPointer = ++prevLinesPointer % prevLines.length;
            prevLines[prevLinesPointer] = line;
        }
        if (conquare > concatLines) {
            sendLines();
        }
    }

    @Override
    public void handle(Exception ex) {
        LoggingMonitorEvent event = new LoggingMonitorEvent();
        event.setHostName(hostname);
        event.setLogFileLocation(canonicalPath);
        event.setServerId("UNKNOWN");
        event.setTimestamp(new Date());
        event.setLog("Logging error: " + ex.getMessage());
        pluginImpl.sendMessage(event);
        final String msg = MessageFormat.format(
            "Exception: {0}", ex.getMessage());
        log.info(msg, ex);
    }

    @Override
    public void fileNotFound() {
        if (lastFnf < (System.currentTimeMillis() - fnfInterval)) {
            lastFnf = System.currentTimeMillis();
            LoggingMonitorEvent event = new LoggingMonitorEvent();
            event.setHostName(hostname);
            event.setLogFileLocation(canonicalPath);
            event.setServerId("UNKNOWN");
            event.setTimestamp(new Date());
            event.setLog("Log file not found");
            pluginImpl.sendMessage(event);
        }
    }

    @Override
    public void fileRotated() {
        // Ignore - normal behavior
        System.out.println("Rotated file");
    }

    private void sendLines() {
        // Reset variables first
        StringBuilder b = buffer;
        stopTimeout();
        buffer = null;
        conquare = 0;

        if (b != null) {
            LoggingMonitorEvent event = new LoggingMonitorEvent();
            event.setHostName(hostname);
            event.setLogFileLocation(canonicalPath);
            event.setServerId("UNKNOWN");
            event.setTimestamp(startedAt);
            event.setLog(b.toString());
            pluginImpl.sendMessage(event);
        }
    }

    private void embedPrevLines(StringBuilder buffer) {
        // Find first not null;
        for (int i=0; i < prevLines.length; i++) {
            int lookAt = (prevLinesPointer + i + 1) % prevLines.length;
            if (prevLines[lookAt] != null) {
                buffer.append(prevLines[lookAt]).append("\n");
            }
        }
        prevLines = new String[prevLines.length];
        prevLinesPointer = 0;
    }


    // Timeout
    private Thread waitLinesTimeout = null;

    private void startTimeout() {
        stopTimeout();
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(waitForLines);
                    waitLinesTimeout = null;
                    sendLines();
                } catch (InterruptedException e) {
                    // OK - We have got sufficient number of lines before timeout - interrupted
                }
            }
        };
        waitLinesTimeout = new Thread(r);
        waitLinesTimeout.start();
    }

    private void stopTimeout() {
        if (waitLinesTimeout != null && waitLinesTimeout.isAlive()) {
            waitLinesTimeout.interrupt();
        }
        waitLinesTimeout = null;
    }
}
