package com.ca.apm.commons.flow;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.systemtest.fld.common.ProcessUtils2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;

import java.util.concurrent.TimeUnit;

@Flow
public class StopJMeterFlow extends FlowBase {
    private static final Logger log = LoggerFactory.getLogger(StopJMeterFlow.class);

    @FlowContext
    protected RunCommandFlowContext context;

    @Override
    public void run() throws Exception {
        ProcessExecutor processExecutor =
            ProcessUtils2.newProcessExecutor().command("wmic", "process", "where",
                "\"CommandLine like '%java%ApacheJMeter%' and not (CommandLine like '%wmic%')\"",
                "Call", "Terminate");
        StartedProcess startedProcess = ProcessUtils2.startProcess(processExecutor);
        int exitCode = ProcessUtils2.waitForProcess(startedProcess, 10, TimeUnit.MINUTES, false);
        if (exitCode != 0) {
            log.warn("jMeter stopping process exited with non-zero value {}", exitCode);
        }
    }
}
