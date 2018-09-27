package com.ca.apm.systemtest.fld.flow;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.systemtest.fld.common.ProcessUtils2;
import com.ca.tas.annotation.TasDocFlow;

@Flow
@TasDocFlow(description = "Flow locates running EM/Webview, captures a thread dump and sends an email if a deadlock is found")
public class DeadlockDetectionFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeadlockDetectionFlow.class);

    private static final Pattern THREAD_DUMP_LINE_DEADLOCK_PATTERN = Pattern
        .compile("^\\bFound\\b.*?\\bdeadlock\\b.*?$");

    private static final String HOSTNAME;

    private static final int EMAIL_SOCKET_TIMEOUT = 60000;
    private static final int EMAIL_SOCKET_CONNECTION_TIMEOUT = 60000;

    static {
        try {
            HOSTNAME = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @FlowContext
    private DeadlockDetectionFlowContext context;

    @Override
    public void run() throws Exception {
        Set<Integer> pids = new HashSet<>();

        // PIDs from context
        Set<Integer> contextPids = context.getPids();
        if (contextPids != null && !contextPids.isEmpty()) {
            LOGGER.info("DeadlockDetectionFlow.run()::  requested PIDs for thread dump capture: "
                + contextPids);
            pids.addAll(contextPids);
        }

        // locate running EM or Webview
        if (context.locateRunningEm()) {
            Set<Integer> foundPids = findPids();
            LOGGER.info("DeadlockDetectionFlow.run():: found EM/WebView PIDs: " + foundPids);
            pids.addAll(foundPids);
        }

        if (pids.isEmpty()) {
            LOGGER.info("DeadlockDetectionFlow.run():: no PIDs for thread dump capture");
        }
        for (Integer pid : pids) {
            // get thread dump
            List<String> threadDump = getThreadDump(pid);

            // check for deadlock
            if (findDeadlock(threadDump)) {
                LOGGER
                    .info("DeadlockDetectionFlow.run():: deadlock found in thread dump of java process "
                        + pid);

                // send e-mail
                String now = (new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")).format(new Date());
                String prefix = "threadDump_" + now + "_" + HOSTNAME + "_pid" + pid + "__";
                File attachment =
                    Files.write(createTempFile(prefix, ".txt").toPath(), threadDump,
                        StandardOpenOption.WRITE).toFile();

                LOGGER.debug("DeadlockDetectionFlow.run():: attachment = "
                    + attachment.getAbsolutePath());

                String subject =
                    "FLD - deadlock found in EM/Webview process " + pid + " on host " + HOSTNAME;
                String body = subject + ".\nThread dump is in the attachment.\n";

                sendEmail(context.getFromEmailAddress(), context.getEmailAddresses(), subject,
                    body, Collections.singleton(attachment), context.getSmtpHost(),
                    context.getSmtpPort());

                LOGGER.info("DeadlockDetectionFlow.run():: e-mail was sent to "
                    + context.getEmailAddresses());
            } else {
                LOGGER
                    .debug("DeadlockDetectionFlow.run():: no deadlock found in thread dump of java process "
                        + pid);
            }
        }
    }

    private static Set<Integer> findPids() {
        Set<Integer> emPids = new HashSet<>();
        Set<Integer> webViewPids = new HashSet<>();
        List<String> lines = getLines(runProcess("jps", "-lmvV"));
        for (String line : lines) {
            String[] items = line.split("\\s");

            // detect EM
            if (isEm(line, items)) {
                emPids.add(Integer.parseInt(items[0]));
                continue;
            }

            // detect WebView
            if (isWebView(line, items)) {
                webViewPids.add(Integer.parseInt(items[0]));
                continue;
            }

            if (isJps(items)) {
                LOGGER.trace("DeadlockDetectionFlow.findPids():: jps line: "
                    + Arrays.toString(items));
                continue;
            }
            if (isProcessInformationUnavailable(items)) {
                LOGGER.warn("DeadlockDetectionFlow.findPids():: skipping unknown java process: "
                    + Arrays.toString(items));
                continue;
            }
            LOGGER.debug("DeadlockDetectionFlow.findPids():: skipping java process: "
                + Arrays.toString(items));
        }
        Set<Integer> pids = new HashSet<>();
        pids.addAll(emPids);
        pids.addAll(webViewPids);
        return pids;
    }

    private static List<String> getThreadDump(int pid) {
        return getLines(runProcess("jstack", "-l" /* "-Fml" */, String.valueOf(pid)));
    }

    private static boolean findDeadlock(List<String> threadDump) {
        for (String line : threadDump) {
            // i.e. "Found one Java-level deadlock:"
            if (THREAD_DUMP_LINE_DEADLOCK_PATTERN.matcher(line).matches()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isJps(String[] items) {
        return "sun.tools.jps.Jps".equals(items[1]);
    }

    private static boolean isProcessInformationUnavailable(String[] items) {
        return "--".equals(items[1]) && "process information unavailable".equals(items[2]);
    }

    private static boolean isEm(String line, String[] items) {
        return "com.zerog.lax.LAX".equals(items[1])
            && line.contains("Introscope_Enterprise_Manager.lax");
    }

    private static boolean isWebView(String line, String[] items) {
        return "com.zerog.lax.LAX".equals(items[1]) && line.contains("Introscope_WebView.lax");
    }

    private static List<String> getLines(ByteArrayOutputStream output) {
        try (BufferedReader reader =
            new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(output.toByteArray())));) {
            List<String> lines = new ArrayList<>();
            String line = null;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ByteArrayOutputStream runProcess(String... command) {
        ExecutedProcess executedProcess = execute(command);
        if (executedProcess.exitValue != 0) {
            throw new RuntimeException("Error executing " + Arrays.toString(command) + ":\n"
                + executedProcess.output);
        }
        return executedProcess.output;
    }

    private static ExecutedProcess execute(String... command) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();) {
            ProcessExecutor processExecutor =
                ProcessUtils2.newProcessExecutor().command(command).redirectErrorStream(true)
                    .redirectOutput(output);
            Process process = ProcessUtils2.startProcess(processExecutor).getProcess();
            int exitValue = process.waitFor();
            return new ExecutedProcess(exitValue, output);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static File createTempFile(String prefix, String suffix) throws IOException {
        File tempFile = File.createTempFile(prefix, suffix);
        tempFile.deleteOnExit();
        return tempFile;
    }

    private static void sendEmail(String fromEmailAddresses, Iterable<String> emailAddresses,
        String subject, String body, Iterable<File> attachments, String smtpHost, int smtpPort)
        throws EmailException {
        HtmlEmail email = new HtmlEmail();
        email.setFrom(fromEmailAddresses);
        for (String emailAddress : emailAddresses) {
            email.addTo(emailAddress);
        }
        if (subject != null) {
            email.setSubject(subject);
        }
        if (body != null) {
            email.setTextMsg(body);
        }
        if (attachments != null) {
            for (File attachment : attachments) {
                email.attach(attachment);
            }
        }
        email.setHostName(smtpHost);
        if (smtpPort > 0) {
            email.setSmtpPort(smtpPort);
        }
        email.setSocketTimeout(EMAIL_SOCKET_TIMEOUT);
        email.setSocketConnectionTimeout(EMAIL_SOCKET_CONNECTION_TIMEOUT);
        email.send();
    }

    private static class ExecutedProcess {
        private final int exitValue;
        private final ByteArrayOutputStream output;

        private ExecutedProcess(int exitValue, ByteArrayOutputStream output) {
            this.exitValue = exitValue;
            this.output = output;
        }
    }

}
