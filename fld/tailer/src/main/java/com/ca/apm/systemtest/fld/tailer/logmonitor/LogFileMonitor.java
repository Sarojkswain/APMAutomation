/**
 *
 */

package com.ca.apm.systemtest.fld.tailer.logmonitor;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.logmonitor.config.LogStream;
import com.ca.apm.systemtest.fld.logmonitor.config.Rule;
import com.ca.apm.systemtest.fld.logmonitor.config.Rule.RuleMatchResult;

/**
 * @author KEYJA01
 */
public class LogFileMonitor implements LogFileTailerListener {
    public static final String EMAIL_TEMPLATE_RESOURCE
        = "com/ca/apm/systemtest/fld/tailer/LogMonitorMailTemplate.html";
    private static long count = 0;
    String[] prevLines = null;
    int prevLinesPointer = 0;
    private Logger log = LoggerFactory.getLogger(LogFileMonitor.class);
    private long fnfInterval;
    private String hostname;
    private List<Rule> rules;
    private Collection<String> emails;
    private int concatLines;
    private int waitForLines;
    private int conquare = 0;
    private StringBuilder buffer = null;
    private Date startedAt;
    // Status fields
    private String canonicalPath = null;
    private long lastFnf = 0;
    // Timeout
    private volatile Thread waitLinesTimeout = null;

    public LogFileMonitor(String hostname, int fnfInterval, int prevLines, LogStream stream,
        Collection<String> emails) {
        this.hostname = hostname;
        this.fnfInterval = fnfInterval * 1000L;
        this.rules = stream.getRules();
        this.emails = new TreeSet<>(emails);
        this.concatLines = stream.getConcatLines();
        this.waitForLines = stream.getWaitForLines();
        this.prevLines = new String[prevLines];
    }

    @Override
    public void init(LogFileTailer tailer) {
        // TODO: 4.5.2016 Consider using commons-io's Tailer class.
        // DO NOT EVER EVEN CONSIDER USING COMMON IO'S TAILER CLASS.  IT IS SO BUGGY THAT IT IS BEYOND
        // EVEN BEING FIXED AND IS THE REASON WE HAVE OUR OWN IMPLEMENTATION.  ITS HANDLING OF 
        // LARGE FILES ON UNIX SYSTEMS MAKES IT COMPLETELY UNUSABLE.
        // AVOID!
        // AVOID!
        // AVOID!
        // AVOID!
        // AVOID!
        // AVOID!
        // AVOID!
        // AVOID!
        // AVOID!
        try {
            canonicalPath = tailer.getFile().getCanonicalPath();
        } catch (IOException e) {
            final String msg = MessageFormat.format(
                "Failed to get canonical path for {1}. Exception: {0}",
                e.getMessage(), tailer.getFile().toString());
            log.error(msg, e);
        }
    }

    @Override
    public synchronized void handle(String line) {
        if (count++ % 10000 == 0) {
            log.info("Processed {} lines", count);
        }
        log.debug("Line: {}", line);

        for (Rule r : rules) {
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
            if (matchResult == RuleMatchResult.MatchesIgnore
                || matchResult == RuleMatchResult.MatchesInclude) {
                // stop checking rules if we have a match
                break;
            }
        }

        if (conquare > 0) {
            buffer.append(line).append("\n");
            conquare++;
        } else if (prevLines.length > 0) {
            prevLinesPointer = ++prevLinesPointer % prevLines.length;
            prevLines[prevLinesPointer] = line;
        }
        if (conquare > concatLines) {
            sendLines();
        }
    }

    private void sendMessage(
        LoggingMonitorEvent event) throws IOException, TemplateException, MessagingException {
        // Prepare email contents by filling in a template.

        String mailTemplate = IOUtils.toString(
            this.getClass().getClassLoader().getResourceAsStream(EMAIL_TEMPLATE_RESOURCE),
            StandardCharsets.UTF_8);
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_21);
        Template temp = new Template("mailbody", new StringReader(mailTemplate), cfg);
        Map<String, String> map = new LinkedHashMap<>(5);
        map.put("hostName", event.getHostName());
        map.put("serverId", event.getServerId());
        map.put("timestamp", String.valueOf(event.getTimestamp()));
        map.put("logFileLocation", event.getLogFileLocation());
        map.put("log", event.getLog());
        StringWriter out = new StringWriter();
        temp.process(map, out);
        final String emailContent = out.toString();

        // Prepare email message.

        Properties props = new Properties();
        props.put("mail.smtp.host", "mail.ca.com");
        props.put("mail.smtp.starttls.enable", "true");
        // XXX: mail.ca.com does not seem to have good certificate trust chain so trust implicitly.
        props.put("mail.smtp.ssl.trust", "*");
        //props.put("mail.debug", "true");
        Session session = Session.getInstance(props);
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom("tailer@" + hostname);
        for (String email : emails) {
            msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
        }
        msg.setSubject("Log monitoring event");
        msg.setSentDate(new Date());
        msg.setContent(emailContent, "text/html; charset=utf-8");

        // Send message using SMTP.

        Transport.send(msg);
    }

    @Override
    public void handle(Exception ex) {
        LoggingMonitorEvent event = new LoggingMonitorEvent();
        event.setHostName(hostname);
        event.setLogFileLocation(canonicalPath);
        event.setServerId(hostname);
        event.setTimestamp(new Date());
        event.setLog("Logging error: " + ex.getMessage());
        try {
            sendMessage(event);
        } catch (IOException | TemplateException | MessagingException e) {
            ErrorUtils
                .logExceptionFmt(log, e, "Failed to send logging error event. Exception: {0}");
        }
        final String msg = MessageFormat.format("Exception: {0}", ex.getMessage());
        log.info(msg, ex);
    }

    @Override
    public void fileNotFound() {
        if (lastFnf < (System.currentTimeMillis() - fnfInterval)) {
            lastFnf = System.currentTimeMillis();
            LoggingMonitorEvent event = new LoggingMonitorEvent();
            event.setHostName(hostname);
            event.setLogFileLocation(canonicalPath);
            event.setServerId(hostname);
            event.setTimestamp(new Date());
            event.setLog("Log file not found");
            try {
                sendMessage(event);
            } catch (IOException | TemplateException | MessagingException e) {
                ErrorUtils
                    .logExceptionFmt(log, e, "Failed to send file not found event. Exception: {0}");
            }
        }
    }

    @Override
    public void fileRotated() {
        // Ignore - normal behavior
        log.debug("Rotated file");
    }

    private synchronized void sendLines() {
        // Reset variables first
        StringBuilder b = buffer;
        stopTimeout();
        buffer = null;
        conquare = 0;

        if (b != null) {
            LoggingMonitorEvent event = new LoggingMonitorEvent();
            event.setHostName(hostname);
            event.setLogFileLocation(canonicalPath);
            event.setServerId(hostname);
            event.setTimestamp(startedAt);
            event.setLog(b.toString());
            try {
                sendMessage(event);
            } catch (IOException | TemplateException | MessagingException e) {
                ErrorUtils
                    .logExceptionFmt(log, e, "Failed to send monitored log event. Exception: {0}");
            }
        }
    }

    private void embedPrevLines(StringBuilder buffer) {
        // Find first not null;
        for (int i = 0; i < prevLines.length; i++) {
            int lookAt = (prevLinesPointer + i + 1) % prevLines.length;
            if (prevLines[lookAt] != null) {
                buffer.append(prevLines[lookAt]).append("\n");
            }
        }
        prevLines = new String[prevLines.length];
        prevLinesPointer = 0;
    }

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
        waitLinesTimeout = new Thread(r, "waitLinesTimeout");
        waitLinesTimeout.setDaemon(true);
        waitLinesTimeout.start();
    }

    private void stopTimeout() {
        if (waitLinesTimeout != null && waitLinesTimeout.isAlive()) {
            waitLinesTimeout.interrupt();
        }
        waitLinesTimeout = null;
    }
}
