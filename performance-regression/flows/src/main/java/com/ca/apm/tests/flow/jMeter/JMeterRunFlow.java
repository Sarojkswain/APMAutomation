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
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JMeterRunFlow
 * <p/>
 * This flow runs JMeter with provided JMX file and optional run -J parameter
 * <p/>
 * At the end, it optionally copies the log files to a remote location
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class JMeterRunFlow extends CopyResultsFlowAbs {

    private static final Logger LOGGER = LoggerFactory.getLogger(JMeterRunFlow.class);
    @FlowContext
    private JMeterRunFlowContext context;

    public JMeterRunFlow() {
    }

    public void run() throws IOException {
        List<String> args = new ArrayList<>();
        args.add("-n");
        args.add("-t");
        args.add(context.getScriptFilePath());
        args.add("-l");
        args.add(context.getOutputJtlFile());
        args.add("-j");
        args.add(context.getOutputLogFile());
        for (Map.Entry<String, String> sheetEntry : context.getParams().entrySet()) {
            args.add("-J" + sheetEntry.getKey() + "=" + sheetEntry.getValue());
        }
        String[] argsArr = new String[args.size()];
        argsArr = args.toArray(argsArr);

        try {
            if (Boolean.TRUE.equals(context.getDeleteOutputLogsBeforeRun())) {
                LOGGER.info("Deleting log files...");
                File jtlFile = FileUtils.getFile(context.getOutputJtlFile());
                LOGGER.info("Deleting file " + jtlFile.toString());
                if (jtlFile.exists()) {
                    FileUtils.deleteQuietly(jtlFile);
                }
                File logFile = FileUtils.getFile(context.getOutputLogFile());
                LOGGER.info("Deleting file " + logFile.toString());
                if (logFile.exists()) {
                    FileUtils.deleteQuietly(logFile);
                }
            }
            runJmeter(argsArr);
            // convert logs if needed
            if (context.getJmeterLogConverterOutputFileName() != null) {
                String jmeterLogConverterCommand = "java -jar " + context.getJmeterLogConverterJarPath() + " " +
                        context.getOutputJtlFile() + " " + context.getJmeterLogConverterOutputFileName();
                LOGGER.info("Executing command " + jmeterLogConverterCommand);
                Process jmeterLogConverter = Runtime.getRuntime().exec(jmeterLogConverterCommand);
                jmeterLogConverter.waitFor();
            }
            // Copy files
            if (context.getCopyResultsDestinationDir() != null) {
                Path origJtlFilePath = Paths.get(context.getJmeterLogConverterOutputFileName() != null ?
                        context.getJmeterLogConverterOutputFileName() : context.getOutputJtlFile());
                Path origLogFilePath = Paths.get(context.getOutputLogFile());
                String jtlFile = origJtlFilePath.getFileName().toString();
                String logFile = origLogFilePath.getFileName().toString();
                if (context.getCopyResultsDestinationJtlFileName() != null) {
                    jtlFile = context.getCopyResultsDestinationJtlFileName();
                }
                if (context.getCopyResultsDestinationLogFileName() != null) {
                    logFile = context.getCopyResultsDestinationLogFileName();
                }
                Path destJtlFilePath = Paths.get(context.getCopyResultsDestinationDir(), jtlFile);
                Path destLogFilePath = Paths.get(context.getCopyResultsDestinationDir(), logFile);
                try {
                    if (context.getCopyResultsDestinationDir().startsWith("\\\\")
                            && context.getCopyResultsDestinationUser() != null
                            && context.getCopyResultsDestinationPassword() != null) {
                        // it's a network location
                        configNet(context.getCopyResultsDestinationDir(),
                                context.getCopyResultsDestinationUser(),
                                context.getCopyResultsDestinationPassword());
                    }
                    LOGGER.info("Copying file " + origJtlFilePath.toString() + " to " + destJtlFilePath.toString());
                    copyFile(origJtlFilePath.toString(), destJtlFilePath.toString());
                    LOGGER.info("Copying file " + origLogFilePath.toString() + " to " + destLogFilePath.toString());
                    copyFile(origLogFilePath.toString(), destLogFilePath.toString());
                } catch (InterruptedException var3) {
                    throw new IllegalStateException(var3);
                }
            }
            if (Boolean.TRUE.equals(context.getDeleteOutputLogsAfterCopy())) {
                LOGGER.info("Deleting log files...");
                File jtlFile = FileUtils.getFile(context.getOutputJtlFile());
                LOGGER.info("Deleting file " + jtlFile.toString());
                if (jtlFile.exists()) {
                    FileUtils.deleteQuietly(jtlFile);
                }
                File logFile = FileUtils.getFile(context.getOutputLogFile());
                LOGGER.info("Deleting file " + logFile.toString());
                if (logFile.exists()) {
                    FileUtils.deleteQuietly(logFile);
                }
            }
        } catch (InterruptedException var3) {
            throw new IllegalStateException(var3);
        }
        LOGGER.info("Flow has finished.");
    }

    protected void runJmeter(String[] argsArr) throws InterruptedException, IOException {
        File logFile = new File(context.getOutputLogFile());
        logFile.getParentFile().mkdirs();
        logFile.createNewFile();
        int responseCode = this.getExecutionBuilder(LOGGER, context.getJmeterPath() + "/bin/jmeter.bat")
                .args(argsArr).build().go();
        switch (responseCode) {
            case 0:
                LOGGER.info("JMeter Execution completed SUCCESSFULLY! Congratulations!");
                return;
            default:
                throw new IllegalStateException(String.format("JMeter Execution failed (%d)", new Object[]{responseCode}));
        }
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }
}
