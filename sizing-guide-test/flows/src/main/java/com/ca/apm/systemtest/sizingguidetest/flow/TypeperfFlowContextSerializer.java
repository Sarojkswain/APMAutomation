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
package com.ca.apm.systemtest.sizingguidetest.flow;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.util.Args;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class TypeperfFlowContextSerializer extends MyEnvPropertySerializerAbs<TypeperfFlowContext> {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(TypeperfFlowContextSerializer.class);
    private static final String METRICS = "metrics";
    private static final String RUN_TIME = "runTime";
    private static final String OUTPUT_FILE_NAME = "outputFileName";
    private static final String COPY_RESULTS_DESTINATION_DIR = "copyResultsDestinationDir";
    private static final String COPY_RESULTS_DESTINATION_FILE_NAME =
        "copyResultsDestinationFileName";
    private static final String COPY_RESULTS_DESTINATION_USER = "copyResultsDestinationUser";
    private static final String COPY_RESULTS_DESTINATION_PASSWORD =
        "copyResultsDestinationPassword";
    private final TypeperfFlowContext flowContext;

    public TypeperfFlowContextSerializer(@Nullable TypeperfFlowContext flowContext) {
        super(TypeperfFlowContextSerializer.class);
        this.flowContext = flowContext;
    }

    public TypeperfFlowContext deserialize(String key, Map<String, String> serializedData) {
        LOGGER.debug("Serialized data: {}", serializedData);
        Map deserializedMap = this.deserializeMapWithKey(key, serializedData);
        LOGGER.debug("Deserialized data: {}", deserializedMap);
        String[] metrics = deserializeArray(deserializedMap, METRICS, String.class);
        Long runTime = Long.valueOf((String) deserializedMap.get(RUN_TIME));
        String outputFileName = (String) deserializedMap.get(OUTPUT_FILE_NAME);
        String copyResultsDestinationDir =
            (String) deserializedMap.get(COPY_RESULTS_DESTINATION_DIR);
        String copyResultsDestinationFileName =
            (String) deserializedMap.get(COPY_RESULTS_DESTINATION_FILE_NAME);
        String copyResultsDestinationUser =
            (String) deserializedMap.get(COPY_RESULTS_DESTINATION_USER);
        String copyResultsDestinationPassword =
            (String) deserializedMap.get(COPY_RESULTS_DESTINATION_PASSWORD);
        if (metrics == null) {
            throw new IllegalArgumentException(
                "Insufficient arguments in env property file: metrics is missing.");
        }
        if (runTime == null) {
            throw new IllegalArgumentException(
                "Insufficient arguments in env property file: runTime is missing.");
        }
        if (outputFileName == null) {
            throw new IllegalArgumentException(
                "Insufficient arguments in env property file: outputFileName is missing.");
        } else {
            TypeperfFlowContext.Builder builder =
                new TypeperfFlowContext.Builder().metrics(metrics).runTime(runTime)
                    .outputFileName(outputFileName)
                    .copyResultsDestinationDir(copyResultsDestinationDir)
                    .copyResultsDestinationFileName(copyResultsDestinationFileName)
                    .copyResultsDestinationUser(copyResultsDestinationUser)
                    .copyResultsDestinationPassword(copyResultsDestinationPassword);
            return builder.build();
        }
    }

    public Map<String, String> serialize(String key) {
        Args.notNull(this.flowContext, "Flow context");
        HashMap customData = new HashMap();
        customData.put(METRICS, serializeAray(this.flowContext.getMetrics()));
        customData.put(RUN_TIME, this.flowContext.getRunTime().toString());
        customData.put(OUTPUT_FILE_NAME, this.flowContext.getOutputFileName());
        if (this.flowContext.getCopyResultsDestinationDir() != null)
            customData.put(COPY_RESULTS_DESTINATION_DIR,
                this.flowContext.getCopyResultsDestinationDir());
        if (this.flowContext.getCopyResultsDestinationFileName() != null)
            customData.put(COPY_RESULTS_DESTINATION_FILE_NAME,
                this.flowContext.getCopyResultsDestinationFileName());
        if (this.flowContext.getCopyResultsDestinationUser() != null)
            customData.put(COPY_RESULTS_DESTINATION_USER,
                this.flowContext.getCopyResultsDestinationUser());
        if (this.flowContext.getCopyResultsDestinationPassword() != null)
            customData.put(COPY_RESULTS_DESTINATION_PASSWORD,
                this.flowContext.getCopyResultsDestinationPassword());
        Map serializedData1 = super.serialize(key);
        serializedData1.putAll(this.serializeMapWithKey(key, customData));
        return serializedData1;
    }

}
