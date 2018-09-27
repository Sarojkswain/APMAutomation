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

import com.ca.apm.tests.flow.MyEnvPropertySerializerAbs;
import org.apache.http.util.Args;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class JMeterRunFlowContextSerializer extends MyEnvPropertySerializerAbs<JMeterRunFlowContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JMeterRunFlowContextSerializer.class);
    private static final String JMETER_PATH = "jmeterPath";
    private static final String SCRIPT_FILE_PATH = "scriptFilePath";
    private static final String OUTPUT_JTL_FILE = "outputJtlFile";
    private static final String OUTPUT_LOG_FILE = "outputLogFile";
    private static final String DELETE_OUTPUT_LOGS_AFTER_COPY = "deleteOutputLogsAfterCopy";
    private static final String DELETE_OUTPUT_LOGS_BEFORE_RUN = "deleteOutputLogsBeforeRun";
    private static final String PARAMS = "params";
    private static final String COPY_RESULTS_DESTINATION_DIR = "copyResultsDestinationDir";
    private static final String COPY_RESULTS_DESTINATION_JTL_FILE_NAME = "copyResultsDestinationJtlFileName";
    private static final String COPY_RESULTS_DESTINATION_LOG_FILE_NAME = "copyResultsDestinationLogFileName";
    private static final String COPY_RESULTS_DESTINATION_USER = "copyResultsDestinationUser";
    private static final String COPY_RESULTS_DESTINATION_PASSWORD = "copyResultsDestinationPassword";
    private static final String JMETER_LOG_CONVERTER_JAR_PATH = "jmeterLogConverterJarPath";
    private static final String JMETER_LOG_CONVERTER_OUTPUT_FILE_NAME = "jmeterLogConverterOutputFileName";
    private final JMeterRunFlowContext flowContext;

    public JMeterRunFlowContextSerializer(@Nullable JMeterRunFlowContext flowContext) {
        super(JMeterRunFlowContextSerializer.class);
        this.flowContext = flowContext;
    }

    public JMeterRunFlowContext deserialize(String key, Map<String, String> serializedData) {
        LOGGER.debug("Serialized data: {}", serializedData);
        Map deserializedMap = this.deserializeMapWithKey(key, serializedData);
        LOGGER.debug("Deserialized data: {}", deserializedMap);
        String jmeterPath = (String) deserializedMap.get(JMETER_PATH);
        String scriptFilePath = (String) deserializedMap.get(SCRIPT_FILE_PATH);
        String outputJtlFile = (String) deserializedMap.get(OUTPUT_JTL_FILE);
        String outputLogFile = (String) deserializedMap.get(OUTPUT_LOG_FILE);
        Boolean deleteOutputLogsAfterCopy = Boolean.valueOf((String) deserializedMap.get(DELETE_OUTPUT_LOGS_AFTER_COPY));
        Boolean deleteOutputLogsBeforeRun = Boolean.valueOf((String) deserializedMap.get(DELETE_OUTPUT_LOGS_BEFORE_RUN));
        Map<String, String> params = deserializeMap(deserializedMap, PARAMS, String.class);
        String copyResultsDestinationDir = (String) deserializedMap.get(COPY_RESULTS_DESTINATION_DIR);
        String copyResultsDestinationJtlFileName = (String) deserializedMap.get(COPY_RESULTS_DESTINATION_JTL_FILE_NAME);
        String copyResultsDestinationLogFileName = (String) deserializedMap.get(COPY_RESULTS_DESTINATION_LOG_FILE_NAME);
        String copyResultsDestinationUser = (String) deserializedMap.get(COPY_RESULTS_DESTINATION_USER);
        String copyResultsDestinationPassword = (String) deserializedMap.get(COPY_RESULTS_DESTINATION_PASSWORD);
        String jmeterLogConverterJarPath = (String) deserializedMap.get(JMETER_LOG_CONVERTER_JAR_PATH);
        String jmeterLogConverterOutputFileName = (String) deserializedMap.get(JMETER_LOG_CONVERTER_OUTPUT_FILE_NAME);
        if (jmeterPath == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: jmeterPath is missing.");
        }
        if (scriptFilePath == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: scriptFilePath is missing.");
        }
        if (outputJtlFile == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: outputJtlFile is missing.");
        }
        if (outputLogFile == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: outputLogFile is missing.");
        }
        if (deleteOutputLogsAfterCopy == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: deleteOutputLogsAfterCopy is missing.");
        }
        if (deleteOutputLogsBeforeRun == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: deleteOutputLogsBeforeRun is missing.");
        } else {
            JMeterRunFlowContext.Builder builder = new JMeterRunFlowContext.Builder()
                    .jmeterPath(jmeterPath)
                    .outputJtlFile(outputJtlFile)
                    .outputLogFile(outputLogFile)
                    .deleteOutputLogsAfterCopy(deleteOutputLogsAfterCopy)
                    .deleteOutputLogsBeforeRun(deleteOutputLogsBeforeRun)
                    .scriptFilePath(scriptFilePath)
                    .params(params)
                    .copyResultsDestinationDir(copyResultsDestinationDir)
                    .copyResultsDestinationJtlFileName(copyResultsDestinationJtlFileName)
                    .copyResultsDestinationLogFileName(copyResultsDestinationLogFileName)
                    .copyResultsDestinationUser(copyResultsDestinationUser)
                    .copyResultsDestinationPassword(copyResultsDestinationPassword)
                    .jmeterLogConverterJarPath(jmeterLogConverterJarPath)
                    .jmeterLogConverterOutputFileName(jmeterLogConverterOutputFileName);
            return builder.build();
        }
    }

    public Map<String, String> serialize(String key) {
        Args.notNull(this.flowContext, "Flow context");
        HashMap customData = new HashMap();
        customData.put(JMETER_PATH, this.flowContext.getJmeterPath());
        customData.put(SCRIPT_FILE_PATH, this.flowContext.getScriptFilePath());
        customData.put(OUTPUT_JTL_FILE, this.flowContext.getOutputJtlFile());
        customData.put(OUTPUT_LOG_FILE, this.flowContext.getOutputLogFile());
        customData.put(DELETE_OUTPUT_LOGS_AFTER_COPY, String.valueOf(this.flowContext.getDeleteOutputLogsAfterCopy()));
        customData.put(DELETE_OUTPUT_LOGS_BEFORE_RUN, String.valueOf(this.flowContext.getDeleteOutputLogsBeforeRun()));
        customData.put(PARAMS, serializeMap(this.flowContext.getParams()));
        if (this.flowContext.getCopyResultsDestinationDir() != null)
            customData.put(COPY_RESULTS_DESTINATION_DIR, this.flowContext.getCopyResultsDestinationDir());
        if (this.flowContext.getCopyResultsDestinationJtlFileName() != null)
            customData.put(COPY_RESULTS_DESTINATION_JTL_FILE_NAME, this.flowContext.getCopyResultsDestinationJtlFileName());
        if (this.flowContext.getCopyResultsDestinationLogFileName() != null)
            customData.put(COPY_RESULTS_DESTINATION_LOG_FILE_NAME, this.flowContext.getCopyResultsDestinationLogFileName());
        if (this.flowContext.getCopyResultsDestinationUser() != null)
            customData.put(COPY_RESULTS_DESTINATION_USER, this.flowContext.getCopyResultsDestinationUser());
        if (this.flowContext.getCopyResultsDestinationPassword() != null)
            customData.put(COPY_RESULTS_DESTINATION_PASSWORD, this.flowContext.getCopyResultsDestinationPassword());
        if (this.flowContext.getJmeterLogConverterJarPath() != null)
            customData.put(JMETER_LOG_CONVERTER_JAR_PATH, this.flowContext.getJmeterLogConverterJarPath());
        if (this.flowContext.getJmeterLogConverterOutputFileName() != null)
            customData.put(JMETER_LOG_CONVERTER_OUTPUT_FILE_NAME, this.flowContext.getJmeterLogConverterOutputFileName());
        Map serializedData1 = super.serialize(key);
        serializedData1.putAll(this.serializeMapWithKey(key, customData));
        return serializedData1;
    }
}
