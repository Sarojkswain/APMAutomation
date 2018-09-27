package com.ca.apm.systemtest.fld.flow;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.zeroturnaround.exec.ProcessExecutor;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.systemtest.fld.common.ProcessUtils2;

/**
 * @author Boch, Tomas (bocto01@ca.com)
 */
@Flow
public class StartJMeterFlow extends FlowBase {

    @FlowContext
    protected RunCommandFlowContext context;

    @Override
    public void run() throws Exception {
        startUp();
    }

    public void startUp() {
        ProcessExecutor processExecutor =
            ProcessUtils2.newProcessExecutor().environment(context.getEnvironment())
                .directory(new File(getAbsolutePath(context.getWorkDir()))).command(getCommand());
        ProcessUtils2.startProcess(processExecutor);
    }

    private List<String> getCommand() {
        List<String> command = new ArrayList<>();
        command.add(getAbsolutePath(context.getWorkDir(), context.getExec()));
        Collection<String> args = context.getArgs();
        if (args != null) {
            command.addAll(args);
        }
        return command;
    }

    private static String getAbsolutePath(String... pathElements) {
        return Paths.get("", pathElements).toAbsolutePath().toString();
    }

}
