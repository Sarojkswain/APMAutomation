package com.ca.apm.systemtest.fld.flow;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.commandline.BackgroundExecution;

@Flow
public class TimeSynchronizationFlow implements IAutomationFlow {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeSynchronizationFlow.class);

    @FlowContext
    private TimeSynchronizationFlowContext ctx;

    boolean onWindows;
    private File workDir;
    private File startScript;
    private File log4jConfig;

    @Override
    public void run() throws Exception {
        LOGGER.info("TimeSynchronizationFlow.run():: entry");
        onWindows = ctx.isOnWindows();
        workDir = new File(ctx.getWorkDir());
        log4jConfig = new File(workDir, "log4j.xml");
        checkDir(workDir);
        installBatchFile();
        runBatchFile();
        LOGGER.info("TimeSynchronizationFlow.run():: exit");
    }

    private void installBatchFile() throws Exception {
        LOGGER.info("TimeSynchronizationFlow.installBatchFile():: entry");
        startScript =
            new File(workDir, onWindows
                ? "run-time-synchronization.bat"
                : "run-time-synchronization.sh");
        String startScriptSrcFile =
            onWindows
                ? "/time-synchronization/run-time-synchronization.bat"
                : "/time-synchronization/run-time-synchronization.sh";

        InputStream startScriptInputStream = getClass().getResourceAsStream(startScriptSrcFile);

        Map<String, String> mods = new HashMap<String, String>();
        mods.put("%%TITLE%%", ctx.getBatchFileTitle());
        mods.put("%%LOG_FILE%%", ctx.getLogFile());
        mods.put("%%JAVA_HOME%%", ctx.getJavaHome());
        mods.put("%%CP%%", ctx.getRunCp());
        long waitInterval = ctx.getWaitInterval();
        mods.put("%%WAIT_INTERVAL%%", (waitInterval > 0) ? ("" + waitInterval) : "");
        mods.put("%%WORK_DIR%%", StringUtils.isNotBlank(ctx.getWorkDir()) ? ctx.getWorkDir() : "");
        mods.put("%%OPTS%%", "");
        mods.put("%%LOG4J_CONFIG%%", "file:///" + log4jConfig.getAbsolutePath());

        deployFile(startScriptInputStream, startScript, mods);

        if (!onWindows) {
            // chmod
            startScript.setExecutable(true);
        }

        // log4j config
        String log4jConfigSrcFile = "/time-synchronization/log4j.xml";
        InputStream log4jConfigInputStream = getClass().getResourceAsStream(log4jConfigSrcFile);
        deployFile(log4jConfigInputStream, log4jConfig, mods);

        LOGGER.info("TimeSynchronizationFlow.installBatchFile():: exit");
    }

    private void runBatchFile() throws Exception {
        LOGGER.info("TimeSynchronizationFlow.runBatchFile():: entry");
        // run time synchronization script
        String command = startScript.getAbsolutePath();
        LOGGER.info("TimeSynchronizationFlow.runBatchFile():: command: {}", command);
        int exitCode = (new BackgroundExecution.Builder(workDir, command)).build().go();
        LOGGER.info("TimeSynchronizationFlow.runBatchFile():: command: {}, exitCode = {}", command,
            exitCode);
        LOGGER.info("TimeSynchronizationFlow.runBatchFile():: exit");
    }

    private static void deployFile(InputStream in, File outputFile, Map<String, String> mods)
        throws Exception {
        if (mods == null) {
            mods = Collections.emptyMap();
        }
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(in));
        String line = null;
        PrintWriter out = new PrintWriter(outputFile);
        while ((line = reader.readLine()) != null) {
            for (Entry<String, String> entry : mods.entrySet()) {
                line = line.replace(entry.getKey(), entry.getValue());
            }
            out.println(line);
        }
        out.flush();
        out.close();
    }

    private static void checkDir(File dir) {
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdirs();
        }
    }

}
