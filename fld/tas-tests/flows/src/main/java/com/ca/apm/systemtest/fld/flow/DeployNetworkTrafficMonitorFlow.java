package com.ca.apm.systemtest.fld.flow;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;

@Flow
public class DeployNetworkTrafficMonitorFlow extends FlowBase implements IAutomationFlow {

    @FlowContext
    private DeployNetworkTrafficMonitorFlowContext ctx;

    private File workDir;

    @Override
    public void run() throws Exception {
        workDir = new File(ctx.getWorkDir());
        checkDir(workDir);
        installFiles();
    }

    private void installFiles() throws IOException {
        InputStream networkTrafficMonitorStartScriptInputStream =
            getClass().getResourceAsStream(
                "/network-traffic-monitor/run-network-traffic-monitor.sh");

        File networkTrafficMonitorStartScript = new File(workDir, "run-network-traffic-monitor.sh");

        Map<String, String> mods = new HashMap<String, String>();
        mods.put("%%TCPDUMP_FILTER%%", ctx.getTcpdumpFilter());
        mods.put("%%INTERVAL_DURATION%%", "" + ctx.getIntervalDuration());

        deployFile(networkTrafficMonitorStartScriptInputStream, networkTrafficMonitorStartScript,
            mods);

        // chmod
        networkTrafficMonitorStartScript.setExecutable(true);
    }

    private static void deployFile(InputStream in, File outputFile, Map<String, String> mods)
        throws IOException {
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
