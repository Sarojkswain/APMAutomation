package com.ca.apm.systemtest.alertstateload.flow;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;

@Flow
public class AlertStateLoadMMFlow extends FlowBase implements IAutomationFlow {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertStateLoadMMFlow.class);

    @FlowContext
    private AlertStateLoadMMFlowContext ctx;

    @Override
    public void run() throws Exception {
        // modify xml
        // TODO

        Files.createDirectories(Paths.get(ctx.getWorkDir()));

        // copy xml
        String xmlSrcFile = ctx.getSrcFile();
        Path xmlFileDstPath = Paths.get("", ctx.getWorkDir(), "ManagementModule.xml");
        InputStream xmlFileSrcInputStream = getClass().getResourceAsStream(xmlSrcFile);
        long bytes = Files.copy(xmlFileSrcInputStream, xmlFileDstPath);
        LOGGER.info("AlertStateLoadMMFlow.run():: " + xmlSrcFile + " ==> " + xmlFileDstPath
            + ", bytes wrote: " + bytes);

        // create manifest file
        Path manifestFileDstPath = Paths.get(ctx.getWorkDir(), "MANIFEST.MF");
        byte[] data = "\r\n".getBytes();
        InputStream manifestFileSrcInputStream = new ByteArrayInputStream(data);
        bytes = Files.copy(manifestFileSrcInputStream, manifestFileDstPath);
        LOGGER.info("AlertStateLoadMMFlow.run():: " + Arrays.toString(data) + " ==> "
            + manifestFileDstPath + ", bytes wrote: " + bytes);

        // create jar
        // TODO - this does not work properly, use zipping instead
        String[] params =
            new String[] {"jar", "cmf", manifestFileDstPath.toString(),
                    Paths.get(ctx.getWorkDir(), "AlertStateLoadMM.jar").toString(),
                    xmlFileDstPath.toString()};
        LOGGER.info("AlertStateLoadMMFlow.run():: params = {}", Arrays.toString(params));
        Process createJarProcess = Runtime.getRuntime().exec(params);
        LOGGER.info("AlertStateLoadMMFlow.run():: jar creation exit code: {}",
            createJarProcess.waitFor());

        // copy MM to EM
        // TODO
    }

}
