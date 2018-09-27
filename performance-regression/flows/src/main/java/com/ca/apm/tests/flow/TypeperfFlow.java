/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 *
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.commandline.Execution;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * RegisterNetAgentFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class TypeperfFlow extends CopyResultsFlowAbs {

    public static final String TYPEPERF_EXECUTABLE = "typeperf";

    private static final Logger LOGGER = LoggerFactory.getLogger(TypeperfFlow.class);
    @FlowContext
    private TypeperfFlowContext context;

    public TypeperfFlow() {
    }

    public void run() throws IOException {
        String args = "";
        for (String arg : context.getMetrics()) {
            args += "\"" + arg + "\" ";
        }
        String typeperfCommand = TYPEPERF_EXECUTABLE + " " + args + " -sc " + context.getRunTime() + " -si " + context.getSamplesInterval();
        LOGGER.info("Executing command " + typeperfCommand);
        Process typeperf = Runtime.getRuntime().exec(typeperfCommand);
        try (InputStream typeperfIs = typeperf.getInputStream();
             FileOutputStream typeperfFileOs = new FileOutputStream(context.getOutputFileName())) {
            copyStream(typeperfIs, typeperfFileOs, true, context.getOutputFileName()); // blocks until process is running
            removeQuotes(context.getOutputFileName());
            IOUtils.closeQuietly(typeperfIs);
            IOUtils.closeQuietly(typeperfFileOs);
        }
        if (context.getCopyResultsDestinationDir() != null) {
            Path origFilePath = Paths.get(context.getOutputFileName());
            String file = origFilePath.getFileName().toString();
            if (context.getCopyResultsDestinationFileName() != null) {
                file = context.getCopyResultsDestinationFileName();
            }
            Path destFilePath = Paths.get(context.getCopyResultsDestinationDir(), file);
            try {
                LOGGER.info("Copying file " + origFilePath.toString() + " to " + destFilePath.toString());
                if (context.getCopyResultsDestinationDir().startsWith("\\\\")
                        && context.getCopyResultsDestinationUser() != null
                        && context.getCopyResultsDestinationPassword() != null) {
                    // it's a network location
                    configNet(context.getCopyResultsDestinationDir(),
                            context.getCopyResultsDestinationUser(),
                            context.getCopyResultsDestinationPassword());
                }
                copyFile(origFilePath.toString(), destFilePath.toString());
            } catch (InterruptedException var3) {
                throw new IllegalStateException(var3);
            }
        }
        LOGGER.info("Flow has finished.");
    }

    protected void removeQuotes(String file) throws IOException {
        Path path = Paths.get(file);
        Charset charset = StandardCharsets.UTF_8;

        String content = new String(Files.readAllBytes(path), charset);
        content = content.replaceAll("\"", "");
        Files.write(path, content.getBytes(charset));
    }


    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }
}
