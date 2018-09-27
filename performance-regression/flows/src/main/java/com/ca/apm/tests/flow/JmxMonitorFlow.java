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
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * RegisterNetAgentFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class JmxMonitorFlow extends CopyResultsFlowAbs {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmxMonitorFlow.class);
    private static final String DEFAULT_JMX_COLLECTION_STRING = "java.lang:type=MemoryPool,name=Java heap|Usage/used,max;java.lang:type=GarbageCollector,name=Copy|CollectionCount|CollectionTime;java.lang:type=GarbageCollector,name=MarkSweepCompact|CollectionCount|CollectionTime";
    @FlowContext
    private JmxMonitorFlowContext context;

    public JmxMonitorFlow() {
    }

    public void run() throws IOException {
        String jmxCollectionString = (context.getJmxCollectionString() != null) ? context.getJmxCollectionString() : DEFAULT_JMX_COLLECTION_STRING;
        String jmxMonitorCommand = "java -jar " +
                context.getJmxMonitorJarPath() + " " +
                context.getHost() + ":" + context.getPort() +
                " \"" + jmxCollectionString + "\" 1000 " + context.getRunTime();
        LOGGER.info("Going to use JMX metric collection string -> " + jmxCollectionString);
        LOGGER.info("Executing command " + jmxMonitorCommand);
        Process jmxMonitor = Runtime.getRuntime().exec(jmxMonitorCommand);

        InputStream jmxMonitorIs = jmxMonitor.getInputStream();
        FileOutputStream jmxMonitorFileOs = new FileOutputStream(context.getOutputFileName());
        copyStream(jmxMonitorIs, jmxMonitorFileOs, true, context.getOutputFileName()); // blocks until process is running
        IOUtils.closeQuietly(jmxMonitorIs);
        IOUtils.closeQuietly(jmxMonitorFileOs);
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


    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }
}
