package com.ca.apm.systemtest.sizingguidetest.flow;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;

@Flow
public class DeployFileFlow extends FlowBase implements IAutomationFlow {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployFileFlow.class);

    @FlowContext
    private DeployFileFlowContext ctx;

    @Override
    public void run() throws Exception {
        InputStream srcInputStream = getClass().getResourceAsStream(ctx.getSrcFile());
        Path dstFilePath = Paths.get("", ctx.getDstFilePath());
        long bytes = Files.copy(srcInputStream, dstFilePath);
        LOGGER.info("DeployFileFlow.run():: " + ctx.getSrcFile() + " ==> " + dstFilePath
            + ", bytes wrote: " + bytes);
    }

}
