package com.ca.apm.systemtest.fld.flow;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import com.ca.apm.automation.action.flow.FlowBase;

@Flow
public class DeployMemoryMonitorFlow extends FlowBase implements IAutomationFlow {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployMemoryMonitorFlow.class);

    @FlowContext
    private DeployMemoryMonitorFlowContext ctx;

    private File workDir;

    @Override
    public void run() throws Exception {
        LOGGER.info("DeployMemoryMonitorFlow.run():: entry");
        workDir = new File(ctx.getWorkDir());
        checkDir(workDir);
        installBatchFile();
        LOGGER.info("DeployMemoryMonitorFlow.run():: exit");
    }

    private void installBatchFile() throws Exception {
        boolean onWindows = ctx.isOnWindows();
        String startScriptSrcFile =
            onWindows
                ? "/memory-monitor/run-memory-monitor.bat"
                : "/memory-monitor/run-memory-monitor.sh";

        InputStream startScriptInputStream = getClass().getResourceAsStream(startScriptSrcFile);

        File startScriptDestFile =
            new File(workDir, onWindows ? "run-memory-monitor.bat" : "run-memory-monitor.sh");
        
        URL classLocation = getClass().getProtectionDomain().getCodeSource().getLocation();
        URI uri = classLocation.toURI();
        Path path = Paths.get(uri);
        System.out.println("The jar file is at " + path);
        
        logger.info("DeployMemoryMonitorRunner code source: " + classLocation);

        Map<String, String> mods = new HashMap<String, String>();
        mods.put("%%TITLE%%", ctx.getBatchFileTitle());
        mods.put("%%LOG_FILE%%", ctx.getLogFile());
        mods.put("%%JAVA_HOME%%", ctx.getJavaHome());
        mods.put("%%CP%%", ctx.getRunCp());
        mods.put("%%GC_LOG_FILE%%", ctx.getGcLogFile());
        mods.put("%%GROUP%%", ctx.getGroup());
        mods.put("%%ROLE_NAME%%", ctx.getRoleName());
        mods.put("%%MEMORY_MONITOR_WEBAPP_HOST%%", ctx.getMemoryMonitorWebappHost());
        mods.put("%%MEMORY_MONITOR_WEBAPP_PORT%%", (ctx.getMemoryMonitorWebappPort() > 0)
            ? ("" + ctx.getMemoryMonitorWebappPort())
            : "");
        mods.put("%%MEMORY_MONITOR_WEBAPP_CONTEXT_ROOT%%", StringUtils.isNotBlank(ctx
            .getMemoryMonitorWebappContextRoot()) ? ctx.getMemoryMonitorWebappContextRoot() : "");
        mods.put("%%CHART_WIDTH%%", (ctx.getChartWidth() > 0) ? ("" + ctx.getChartWidth()) : "");
        mods.put("%%CHART_HEIGHT%%", (ctx.getChartHeight() > 0) ? ("" + ctx.getChartHeight()) : "");
        mods.put("%%WAIT_INTERVAL%%", (ctx.getWaitInterval() > 0)
            ? ("" + ctx.getWaitInterval())
            : "");
        mods.put("%%WORK_DIR%%", StringUtils.isNotBlank(ctx.getWorkDir()) ? ctx.getWorkDir() : "");
        mods.put("%%ITERATION_COUNT%%", "" + ctx.getIterationCount());

        deployFile(startScriptInputStream, startScriptDestFile, mods);

        if (!onWindows) {
            String stopScriptSrcFile = "/memory-monitor/stop-memory-monitor.sh";
            InputStream stopScriptInputStream = getClass().getResourceAsStream(stopScriptSrcFile);
            File stopScriptDestFile = new File(workDir, "stop-memory-monitor.sh");
            deployFile(stopScriptInputStream, stopScriptDestFile, null);

            // chmod
            startScriptDestFile.setExecutable(true);
            stopScriptDestFile.setExecutable(true);
        }
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
