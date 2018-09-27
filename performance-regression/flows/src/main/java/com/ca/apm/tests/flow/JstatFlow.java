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
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * RegisterNetAgentFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class JstatFlow extends CopyResultsFlowAbs {

    public static final String JSTAT_EXECUTABLE = "jstat.exe";

    private static final Logger LOGGER = LoggerFactory.getLogger(JstatFlow.class);
    @FlowContext
    private JstatFlowContext context;

    public JstatFlow() {
    }

    public static long copyStream(InputStream input, OutputStream output) throws IOException {
        long count = 0L;

        byte[] buffer = new byte[4096];
        int n1;
        long time = System.currentTimeMillis();
        for (boolean n = false; -1 != (n1 = input.read(buffer)); count += (long) n1) {
            output.write(buffer, 0, n1);
            long currentTime = System.currentTimeMillis();
            if (currentTime - time > 20000) {
                // log every 20 seconds
                LOGGER.info("Stream copy is alive.");
            }
        }

        return count;
    }

    public void run() throws IOException {
        String wmicCommand = "wmic PROCESS where \"name like '%java%' and CommandLine like '%" +
                context.getIdentString() + "%'\" get Processid";
        LOGGER.info("Executing command " + wmicCommand);
        Process wmic = Runtime.getRuntime().exec(wmicCommand);
        InputStream wmicIs = wmic.getInputStream();
        List<String> lines = IOUtils.readLines(wmicIs);
        Integer wmicOutput = null;
        IOUtils.closeQuietly(wmicIs);
        for (String line : lines) {
            line = line.trim();
            if (NumberUtils.isNumber(line)) {
                wmicOutput = NumberUtils.createInteger(line);
                break;
            }
        }
        if (wmicOutput != null) {
            String jstatCommand = context.getJavaHome() + "\\bin\\" + JSTAT_EXECUTABLE + " -gc " + wmicOutput
                    + " 1s " + context.getRunTime();
            LOGGER.info("Executing command " + jstatCommand);
            Process jstat = Runtime.getRuntime().exec(jstatCommand);

            InputStream jstatIs = jstat.getInputStream();
            FileOutputStream jstatFileOs = new FileOutputStream(context.getOutputFileName());
            copyStream(jstatIs, jstatFileOs, true, context.getOutputFileName()); // blocks until process is running
            convertSpacesToCommas(context.getOutputFileName());
            IOUtils.closeQuietly(jstatIs);
            IOUtils.closeQuietly(jstatFileOs);
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
        } else {
            throw new IllegalStateException("WMIC was unable to get process ID");
        }
        LOGGER.info("Flow has finished.");
    }

    protected void convertSpacesToCommas(String file) throws IOException {
        Path path = Paths.get(file);
        Charset charset = StandardCharsets.UTF_8;

        String content = new String(Files.readAllBytes(path), charset);
        content = content.replaceAll("[\\s&&[^\n\r]]+", ",");
        Files.write(path, content.getBytes(charset));
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }
}
