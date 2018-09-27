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
package com.ca.apm.tests.flow.jMeter;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.commandline.Execution;
import com.ca.apm.tests.flow.CopyResultsFlowAbs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * JMeterStatsFlow
 * <p/>
 * This flow creates a csv file containing information about JMeter load based on provided parameters, namely
 * - Number of threads
 * - Ramp-up time (seconds)
 * - Run time (minutes)
 * - Delay between requests (milliseconds)
 * - Startup delay (seconds)
 * - Throughput
 * <p/>
 * At the end, it optionally copies the file to a remote location
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class JMeterStatsFlow extends CopyResultsFlowAbs {

    private static final Logger LOGGER = LoggerFactory.getLogger(JMeterStatsFlow.class);
    @FlowContext
    private JMeterStatsFlowContext context;

    public JMeterStatsFlow() {
    }

    public void run() throws IOException {
        LOGGER.info("Generating file " + this.context.getOutputFile());
        File outputFile = new File(this.context.getOutputFile());
        outputFile.getParentFile().mkdirs();
        outputFile.createNewFile();
        try (PrintWriter writer = new PrintWriter(this.context.getOutputFile(), "UTF-8");) {
            writer.println("");
            writer.println("");
            writer.println("Num_threads,Ramp_up_time_sec,Run_time_min,Delay_between_requests_ms,Startup_delay_sec,Throughput");
            writer.println(this.context.getNumThreads() + "," + this.context.getRampUpTime() + "," +
                    this.context.getRunMinutes() + "," + this.context.getDelayBetweenRequests() + "," +
                    this.context.getStartupDelaySeconds() + ",-");
        }
        // Copy files
        if (this.context.getCopyResultsDestinationDir() != null) {
            Path origFilePath = Paths.get(this.context.getOutputFile());
            String file = origFilePath.getFileName().toString();
            if (this.context.getCopyResultsDestinationFileName() != null) {
                file = this.context.getCopyResultsDestinationFileName();
            }
            Path destFilePath = Paths.get(this.context.getCopyResultsDestinationDir(), file);
            try {
                if (this.context.getCopyResultsDestinationDir().startsWith("\\\\")
                        && this.context.getCopyResultsDestinationUser() != null
                        && this.context.getCopyResultsDestinationPassword() != null) {
                    // it's a network location
                    configNet(this.context.getCopyResultsDestinationDir(),
                            this.context.getCopyResultsDestinationUser(),
                            this.context.getCopyResultsDestinationPassword());
                }
                LOGGER.info("Copying file " + origFilePath.toString() + " to " + destFilePath.toString());
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
