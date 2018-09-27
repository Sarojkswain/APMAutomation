package com.ca.apm.systemtest.fld.flow;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.Execution;

/**
 * @author haiva01
 */
@Flow
public class RunUniqueMetricsGeneratorFlow extends FlowBase {
    private static final Logger log = LoggerFactory.getLogger(RunUniqueMetricsGeneratorFlow.class);

    @FlowContext
    RunUniqueMetricsGeneratorFlowContext flowContext;

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

    private static String getJavaExePath() {
        return Paths.get(System.getProperty("java.home"), "bin",
            "java" + (SystemUtils.IS_OS_WINDOWS ? ".exe" : "")).toAbsolutePath().toString();
    }

    @Override
    public void run() throws Exception {
        final Collection<String> args = new ArrayList<>(16);

        File umegDir = new File(flowContext.getDir());

        if (SystemUtils.IS_OS_WINDOWS) {
            args.add("/S");
            args.add("/C");

            Collection<String> shellParams = new ArrayList<>(10);
            shellParams.add("start");
            shellParams.add("\"\"");
            addUmegArguments(umegDir, shellParams);

            args.add(quoteParam(StringUtils.join(shellParams, ' ')));
        } else {
            args.add("-c");

            Collection<String> shellParam = new ArrayList<>(10);
            shellParam.add("(");
            shellParam.add("setsid");
            addUmegArguments(umegDir, shellParam);
            shellParam.add(">/dev/null");
            shellParam.add("2>&1");
            shellParam.add("&");
            shellParam.add(")");

            args.add(StringUtils.join(shellParam, ' '));
        }

        String command = SystemUtils.IS_OS_WINDOWS ? getCmdExe() : getShell();
        Execution.Builder eb = new Execution.Builder(command, log)
            .useWindowsShell(false)
            .args(args)
            .workDir(new File(flowContext.getDir()).getAbsoluteFile());

        Execution execution = eb.build();
        execution.go();
    }

    private void addUmegArguments(File umegDir, Collection<String> args) {
        args.add(quoteParam(getJavaExePath()));

        args.add("-cp");
        args.add(quoteParam(Paths.get(umegDir.getAbsolutePath(), "classes").toString()));

        args.add("com.ca.apm.systemtest.fld.umeg.Main");

        args.add("-r");
        args.add(Long.toString(flowContext.getRate()));

        args.add("-a");
        args.add(quoteParam(
            "-javaagent:" + Paths.get(umegDir.getAbsolutePath(), "wily", "Agent.jar").toString()));

        args.add("-a");
        args.add(quoteParam("-Dcom.wily.introscope.agent.agentName=" + flowContext.getAgentName()));

        args.add("-a");
        args.add(quoteParam(
            "-Dintroscope.agent.customProcessName=" + flowContext.getCustomProcessName()));

        args.add("-a");
        args.add(quoteParam("-Dcom.wily.introscope.agentProfile="
            + Paths.get(umegDir.getAbsolutePath(), "wily", "core", "config",
            "IntroscopeAgent.profile")));

        args.add("-a");
        args.add(quoteParam("-DagentManager.url.1=" + flowContext.getEmUrl()));

        long runningTimeSecs = flowContext.getRunningTimeSecs();
        if (runningTimeSecs > 0) {
            args.add("-t");
            args.add(Long.toString(runningTimeSecs));
        }

        String uniqueString = flowContext.getUniqueString();
        if (isNotBlank(uniqueString)) {
            args.add("-u");
            args.add(uniqueString);
        }

        String pidFile = flowContext.getPidFile();
        if (isNotBlank(pidFile)) {
            args.add("-p");
            args.add(quoteParam(new File(pidFile).getAbsolutePath()));
        }
    }
}
