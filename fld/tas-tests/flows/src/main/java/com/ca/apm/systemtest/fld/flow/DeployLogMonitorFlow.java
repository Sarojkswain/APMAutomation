package com.ca.apm.systemtest.fld.flow;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.tools.ant.taskdefs.Classloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.Execution;
import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.spel.ConfigurationPlaceholderResolver;
import com.ca.apm.systemtest.fld.common.spel.StringEvaluator;
import com.ca.apm.systemtest.fld.flow.DeployLogMonitorFlowContext.LogMonitorConfigSource;
import com.ca.apm.systemtest.fld.logmonitor.config.LogMonitorConfiguration;
import com.ca.apm.systemtest.fld.logmonitor.config.LogStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author haiva01
 */
@Flow
public class DeployLogMonitorFlow extends FlowBase {
    private static final Logger log = LoggerFactory.getLogger(DeployLogMonitorFlow.class);
    private String configFileName;
    private File configFile;

    @FlowContext
    DeployLogMonitorFlowContext flowContext;

    private static String getJavaExePath() {
        return Paths.get(System.getProperty("java.home"), "bin",
            "java" + (SystemUtils.IS_OS_WINDOWS ? ".exe" : "")).toAbsolutePath().toString();
    }

    private InputStream resolveConfigFile() throws IOException {
        LogMonitorConfigSource configSourceKind = flowContext.getLogMonitorConfigSource();
        switch (configSourceKind) {
            case DiskFile:
                configFileName = flowContext.getConfigFile();
                configFile = new File(configFileName).getAbsoluteFile();
                return IOUtils
                    .toBufferedInputStream(FileUtils.openInputStream(new File(flowContext.getConfigFile())));

            case ResourceFile: {
                configFileName = flowContext.getConfigFile();
                int idx = configFileName.lastIndexOf("/");
                configFileName = configFileName.substring(idx + 1);
                File baseDir = new File(flowContext.getTargetDir());
                configFile = new File(baseDir, configFileName).getAbsoluteFile();
                log.debug("Using configFileName: " + configFileName);
                log.debug("configFile: " + configFile.getAbsolutePath());
                InputStream inputStream = Classloader.class.getResourceAsStream(flowContext.getConfigFile());
                if (inputStream == null) {
                    ErrorUtils
                        .logErrorAndReturnException(log, "Failed to open resource {1} as stream.",
                            configFile);
                }
                return inputStream;
            }

            default:
                throw ErrorUtils
                    .logErrorAndReturnException(log, "Unknown log monitor source kind {0}",
                        configSourceKind);
        }
    }

    private static String quoteParam(String param) {
        // TODO: This does not deal with other meta characters inside the param argument.
        return '"' + param + '"';
    }

    private static String getCmdExe() {
        return StringUtils.defaultIfBlank(
            System.getenv("ComSpec"), "C:\\Windows\\System32\\cmd.exe");
    }

    private static String getShell() {
        return StringUtils.defaultIfBlank(System.getenv("SHELL"), "/bin/sh");
    }

    private void addTailerArguments(File tailerJar, Collection<String> args) {
        args.add(quoteParam(getJavaExePath()));

        args.add("-jar");
        args.add(quoteParam(tailerJar.getAbsolutePath()));

        args.add("-c");
        args.add(quoteParam(configFile.getAbsolutePath()));

        args.add("-m");
        args.add(Integer.toString(flowContext.getMaxMatchesPerPeriod()));

        args.add("-n");
        args.add(Integer.toString(flowContext.getNumberOfPreviousLines()));

        args.add("-p");
        args.add(quoteParam(new File(flowContext.getPidFile()).getAbsolutePath()));

        for (String email : flowContext.getEmails()) {
            args.add("-t");
            args.add(quoteParam(email));
        }
    }

    @Override
    public void run() throws Exception {
        // Deploy Tailer JAR file.

        URL tailerArtifactUrl = new URL(flowContext.getTailerArtifactUrl());
        File tailerJar = Paths
            .get(flowContext.getTargetDir(), new File(tailerArtifactUrl.getPath()).getName())
            .toFile();
        archiveFactory.createArtifact(tailerArtifactUrl).downloadForced(tailerJar);

        // Deploy configuration file. Evaluate embedded placeholders.

        final ConfigurationPlaceholderResolver placeholderResolver
            = new ConfigurationPlaceholderResolver(
            Collections.<String, Object>unmodifiableMap(flowContext.getVars()));
        final StringEvaluator evaluator = new StringEvaluator(placeholderResolver);

//        final String configFile = flowContext.getConfigFile();
//        final File configFileFile = new File(configFile).getAbsoluteFile();
        ObjectMapper om = new ObjectMapper();
        om.enable(SerializationFeature.INDENT_OUTPUT);
        om.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

        
        LogMonitorConfiguration logMonitorConfiguration;
        try (InputStream inputStream = resolveConfigFile();
             Reader inputReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            logMonitorConfiguration = om.readValue(inputReader, LogMonitorConfiguration.class);
            Map<String, LogStream> streams = logMonitorConfiguration.getLogStreams();

            // Evaluate each file name for placeholders.

            for (Map.Entry<String, LogStream> entry : streams.entrySet()) {
                LogStream stream = entry.getValue();
                String fileName = evaluator.evaluateString(stream.getFileName());
                stream.setFileName(fileName);
            }
        }

        // Write above modified configuration back.

        String evaluatedJson = om.writeValueAsString(logMonitorConfiguration);
        log.debug("Writing config file to " + configFile);
        FileUtils.write(configFile, evaluatedJson, StandardCharsets.UTF_8);

        // Start Tailer.

        if (!flowContext.isStart()) {
            return;
        }

        final Collection<String> args = new ArrayList<>(16);

        if (SystemUtils.IS_OS_WINDOWS) {
            args.add("/S");
            args.add("/C");
            args.add("\"");

            args.add("start");
            args.add("\"\"");
            addTailerArguments(tailerJar, args);

            args.add("\"");
        } else {
            args.add("-c");

            Collection<String> shellParam = new ArrayList<>(10);
            shellParam.add("(");
            shellParam.add("setsid");
            addTailerArguments(tailerJar, shellParam);
            shellParam.add(">/dev/null");
            shellParam.add("2>&1");
            shellParam.add("&");
            shellParam.add(")");

            args.add(StringUtils.join(shellParam, ' '));
        }

        final Map<String, String> env = new HashMap<>(1);
        env.put("CLASSPATH", tailerJar.getParent());

        String command = SystemUtils.IS_OS_WINDOWS ? getCmdExe() : getShell();
        Execution exec = new Execution.Builder(command, log)
            .args(args)
            .workDir(tailerJar.getParentFile())
            .environment(env)
            .useWindowsShell(false)
            .build();

        exec.go();
    }
    
    public static void main(String[] args) throws Exception {
        URL url = DeployLogMonitorFlow.class.getResource("/memory-monitor/run-memory-monitor.bat");
        System.out.println(url);
        System.out.println(url.getFile());
        System.out.println(url.getPath());
        System.out.println(url.getQuery());
        System.out.println(FilenameUtils.getName(url.getPath()));
        URI uri = new URI(url.toString());
        String p = uri.getPath();
        System.out.println(Paths.get(p).getFileName().toString());
        
    }
}
