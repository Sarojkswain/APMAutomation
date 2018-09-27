package com.ca.apm.systemtest.fld.flow;

import java.util.concurrent.TimeUnit;

import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.systemtest.fld.common.ProcessUtils2;

@Flow
public class StopJMeterFlow extends FlowBase {

    @FlowContext
    protected RunCommandFlowContext context;

    @Override
    public void run() throws Exception {
        shutDown();
    }

    public boolean shutDown() {
        ProcessExecutor processExecutor =
            ProcessUtils2.newProcessExecutor().command("wmic", "process", "where",
                "\"CommandLine like '%java%ApacheJMeter%' and not (CommandLine like '%wmic%')\"",
                "Call", "Terminate");
        StartedProcess startedProcess = ProcessUtils2.startProcess(processExecutor);
        int exitCode = ProcessUtils2.waitForProcess(startedProcess, 10, TimeUnit.MINUTES, false);
        return exitCode == 0;
    }

}
