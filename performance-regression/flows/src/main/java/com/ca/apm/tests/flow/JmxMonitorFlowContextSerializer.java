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

import com.ca.tas.property.AbstractEnvPropertySerializer;
import org.apache.http.util.Args;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class JmxMonitorFlowContextSerializer extends AbstractEnvPropertySerializer<JmxMonitorFlowContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JmxMonitorFlowContextSerializer.class);
    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String JAVA_HOME = "javaHome";
    private static final String JMX_MONITO_JAR_PATH = "jmxMonitorJarPath";
    private static final String RUN_TIME = "runTime";
    private static final String OUTPUT_FILE_NAME = "outputFileName";
    private static final String COPY_RESULTS_DESTINATION_DIR = "copyResultsDestinationDir";
    private static final String COPY_RESULTS_DESTINATION_FILE_NAME = "copyResultsDestinationFileName";
    private static final String COPY_RESULTS_DESTINATION_USER = "copyResultsDestinationUser";
    private static final String COPY_RESULTS_DESTINATION_PASSWORD = "copyResultsDestinationPassword";
    private static final String JMX_COLLECTION_STRING = "jmxCollectionString";

    private final JmxMonitorFlowContext flowContext;

    public JmxMonitorFlowContextSerializer(@Nullable JmxMonitorFlowContext flowContext) {
        super(JmxMonitorFlowContextSerializer.class);
        this.flowContext = flowContext;
    }

    public JmxMonitorFlowContext deserialize(String key, Map<String, String> serializedData) {
        LOGGER.debug("Serialized data: {}", serializedData);
        Map deserializedMap = this.deserializeMapWithKey(key, serializedData);
        LOGGER.debug("Deserialized data: {}", deserializedMap);
        String host = (String) deserializedMap.get(HOST);
        Integer port = Integer.valueOf((String) deserializedMap.get(PORT));
        String javaHome = (String) deserializedMap.get(JAVA_HOME);
        String jmxMonitorJarPath = (String) deserializedMap.get(JMX_MONITO_JAR_PATH);
        Long runTime = Long.valueOf((String) deserializedMap.get(RUN_TIME));
        String outputFileName = (String) deserializedMap.get(OUTPUT_FILE_NAME);
        String copyResultsDestinationDir = (String) deserializedMap.get(COPY_RESULTS_DESTINATION_DIR);
        String copyResultsDestinationFileName = (String) deserializedMap.get(COPY_RESULTS_DESTINATION_FILE_NAME);
        String copyResultsDestinationUser = (String) deserializedMap.get(COPY_RESULTS_DESTINATION_USER);
        String copyResultsDestinationPassword = (String) deserializedMap.get(COPY_RESULTS_DESTINATION_PASSWORD);
        String jmxCollectionString = (String) deserializedMap.get(JMX_COLLECTION_STRING);
        if (host == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: host is missing.");
        }
        if (port == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: port is missing.");
        }
        if (jmxMonitorJarPath == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: jmxMonitorJarPath is missing.");
        }
        if (runTime == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: runTime is missing.");
        }
        if (outputFileName == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: outputFileName is missing.");
        } else {
            JmxMonitorFlowContext.Builder builder = new JmxMonitorFlowContext.Builder()
                    .host(host)
                    .javaHome(javaHome)
                    .port(port)
                    .jmxMonitorJarPath(jmxMonitorJarPath)
                    .runTime(runTime)
                    .outputFileName(outputFileName)
                    .copyResultsDestinationDir(copyResultsDestinationDir)
                    .copyResultsDestinationFileName(copyResultsDestinationFileName)
                    .copyResultsDestinationUser(copyResultsDestinationUser)
                    .copyResultsDestinationPassword(copyResultsDestinationPassword)
                    .jmxCollectionString(jmxCollectionString);
            return builder.build();
        }
    }

    public Map<String, String> serialize(String key) {
        Args.notNull(this.flowContext, "Flow context");
        HashMap customData = new HashMap();
        customData.put(HOST, this.flowContext.getHost());
        customData.put(PORT, this.flowContext.getPort().toString());
        if (this.flowContext.getJavaHome() != null)
            customData.put(JAVA_HOME, this.flowContext.getJavaHome());
        customData.put(JMX_MONITO_JAR_PATH, this.flowContext.getJmxMonitorJarPath());
        customData.put(RUN_TIME, this.flowContext.getRunTime().toString());
        customData.put(OUTPUT_FILE_NAME, this.flowContext.getOutputFileName());
        if (this.flowContext.getCopyResultsDestinationDir() != null)
            customData.put(COPY_RESULTS_DESTINATION_DIR, this.flowContext.getCopyResultsDestinationDir());
        if (this.flowContext.getCopyResultsDestinationFileName() != null)
            customData.put(COPY_RESULTS_DESTINATION_FILE_NAME, this.flowContext.getCopyResultsDestinationFileName());
        if (this.flowContext.getCopyResultsDestinationUser() != null)
            customData.put(COPY_RESULTS_DESTINATION_USER, this.flowContext.getCopyResultsDestinationUser());
        if (this.flowContext.getCopyResultsDestinationPassword() != null)
            customData.put(COPY_RESULTS_DESTINATION_PASSWORD, this.flowContext.getCopyResultsDestinationPassword());
        if (this.flowContext.getJmxCollectionString() != null)
            customData.put(JMX_COLLECTION_STRING, this.flowContext.getJmxCollectionString());
        Map serializedData1 = super.serialize(key);
        serializedData1.putAll(this.serializeMapWithKey(key, customData));
        return serializedData1;
    }
}
