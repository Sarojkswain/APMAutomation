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
public class JMeterStatsFlowContextSerializer extends AbstractEnvPropertySerializer<JMeterStatsFlowContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JMeterStatsFlowContextSerializer.class);
    private static final String NUM_THREADS = "numThreads";
    private static final String RAMP_UP_TIME = "rampUpTime";
    private static final String RUN_MINUTES = "runMinutes";
    private static final String DELAY_BETWEEN_REQUESTS = "delayBetweenRequests";
    private static final String STARTUP_DELAY_SECONDS = "startupDelaySeconds";
    private static final String OUTPUT_FILE = "outputFile";
    private static final String COPY_RESULTS_DESTINATION_DIR = "copyResultsDestinationDir";
    private static final String COPY_RESULTS_DESTINATION_FILE_NAME = "copyResultsDestinationFileName";
    private static final String COPY_RESULTS_DESTINATION_USER = "copyResultsDestinationUser";
    private static final String COPY_RESULTS_DESTINATION_PASSWORD = "copyResultsDestinationPassword";
    private final JMeterStatsFlowContext flowContext;

    public JMeterStatsFlowContextSerializer(@Nullable JMeterStatsFlowContext flowContext) {
        super(JMeterStatsFlowContextSerializer.class);
        this.flowContext = flowContext;
    }

    public JMeterStatsFlowContext deserialize(String key, Map<String, String> serializedData) {
        LOGGER.debug("Serialized data: {}", serializedData);
        Map deserializedMap = this.deserializeMapWithKey(key, serializedData);
        LOGGER.debug("Deserialized data: {}", deserializedMap);
        Long numThreads = Long.valueOf((String) deserializedMap.get(NUM_THREADS));
        Long rampUpTime = Long.valueOf((String) deserializedMap.get(RAMP_UP_TIME));
        Long runMinutes = Long.valueOf((String) deserializedMap.get(RUN_MINUTES));
        Long delayBetweenRequests = Long.valueOf((String) deserializedMap.get(DELAY_BETWEEN_REQUESTS));
        Long startupDelaySeconds = Long.valueOf((String) deserializedMap.get(STARTUP_DELAY_SECONDS));
        String outputFile = (String) deserializedMap.get(OUTPUT_FILE);
        String copyResultsDestinationDir = (String) deserializedMap.get(COPY_RESULTS_DESTINATION_DIR);
        String copyResultsDestinationFileName = (String) deserializedMap.get(COPY_RESULTS_DESTINATION_FILE_NAME);
        String copyResultsDestinationUser = (String) deserializedMap.get(COPY_RESULTS_DESTINATION_USER);
        String copyResultsDestinationPassword = (String) deserializedMap.get(COPY_RESULTS_DESTINATION_PASSWORD);
        if (numThreads == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: numThreads is missing.");
        }
        if (rampUpTime == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: rampUpTime is missing.");
        }
        if (runMinutes == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: runMinutes is missing.");
        }
        if (delayBetweenRequests == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: delayBetweenRequests is missing.");
        }
        if (startupDelaySeconds == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: startupDelaySeconds is missing.");
        }
        if (outputFile == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: outputFile is missing.");
        } else {
            JMeterStatsFlowContext.Builder builder = new JMeterStatsFlowContext.Builder()
                    .numThreads(numThreads)
                    .rampUpTime(rampUpTime)
                    .runMinutes(runMinutes)
                    .delayBetweenRequests(delayBetweenRequests)
                    .startupDelaySeconds(startupDelaySeconds)
                    .outputFile(outputFile)
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
        customData.put(NUM_THREADS, String.valueOf(this.flowContext.getNumThreads()));
        customData.put(RAMP_UP_TIME, String.valueOf(this.flowContext.getNumThreads()));
        customData.put(RUN_MINUTES, String.valueOf(this.flowContext.getRunMinutes()));
        customData.put(DELAY_BETWEEN_REQUESTS, String.valueOf(this.flowContext.getDelayBetweenRequests()));
        customData.put(STARTUP_DELAY_SECONDS, String.valueOf(this.flowContext.getStartupDelaySeconds()));
        customData.put(OUTPUT_FILE, this.flowContext.getOutputFile());
        if (this.flowContext.getCopyResultsDestinationDir() != null)
            customData.put(COPY_RESULTS_DESTINATION_DIR, this.flowContext.getCopyResultsDestinationDir());
        if (this.flowContext.getCopyResultsDestinationFileName() != null)
            customData.put(COPY_RESULTS_DESTINATION_FILE_NAME, this.flowContext.getCopyResultsDestinationFileName());
        if (this.flowContext.getCopyResultsDestinationUser() != null)
            customData.put(COPY_RESULTS_DESTINATION_USER, this.flowContext.getCopyResultsDestinationUser());
        if (this.flowContext.getCopyResultsDestinationPassword() != null)
            customData.put(COPY_RESULTS_DESTINATION_PASSWORD, this.flowContext.getCopyResultsDestinationPassword());
        Map serializedData1 = super.serialize(key);
        serializedData1.putAll(this.serializeMapWithKey(key, customData));
        return serializedData1;
    }
}
