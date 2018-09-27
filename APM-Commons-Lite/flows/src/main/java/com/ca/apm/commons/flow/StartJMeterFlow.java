package com.ca.apm.commons.flow;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.systemtest.fld.common.ProcessUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Boch, Tomas (bocto01@ca.com)
 */
@Flow
public class StartJMeterFlow extends FlowBase {
    private static final Logger log = LoggerFactory.getLogger(StartJMeterFlow.class);

    @FlowContext
    protected RunCommandFlowContext context;

    @Override
    public void run() throws Exception {
        startUp();
    }

    public void startUp() {
        ProcessBuilder pb = ProcessUtils.newProcessBuilder();
        pb.environment().putAll(context.getEnvironment());
        pb.directory(new File(context.getWorkDir()).getAbsoluteFile());
        pb.command(getCommand());
        Process sp = ProcessUtils.startProcess(pb);
        log.info("jMeter process started with PID {}", ProcessUtils.getPid(sp));
        ProcessUtils.waitForProcess(sp, 5, TimeUnit.MINUTES, false);
    }

    private List<String> getCommand() {
        List<String> command = new ArrayList<>(20);
        command.add(context.getExec());
        Collection<String> args = context.getArgs();
        if (args != null) {
            command.addAll(args);
        }
        return command;
    }
}
