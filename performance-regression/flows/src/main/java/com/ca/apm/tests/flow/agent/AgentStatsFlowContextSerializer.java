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
package com.ca.apm.tests.flow.agent;

import com.ca.apm.tests.flow.MyEnvPropertySerializerAbs;
import org.apache.http.util.Args;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class AgentStatsFlowContextSerializer extends MyEnvPropertySerializerAbs<AgentStatsFlowContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentStatsFlowContextSerializer.class);
    private static final String AGENT_NAMES = "agentNames";
    private static final String BUILD_NUMBERS = "buildNumbers";
    private static final String BUILD_SUFFIXES = "buildSuffixes";
    private static final String SPM_ON = "spmOn";
    private static final String SI_ON = "siOn";
    private static final String ACC_ON = "accOn";
    private static final String ACC_MOCK_ON = "accMockOn";
    private static final String BT_ON = "btOn";
    private static final String BRTM_ON = "brtmOn";
    private static final String OUTPUT_FILE = "outputFile";
    private static final String COPY_RESULTS_DESTINATION_DIR = "copyResultsDestinationDir";
    private static final String COPY_RESULTS_DESTINATION_FILE_NAME = "copyResultsDestinationFileName";
    private static final String COPY_RESULTS_DESTINATION_USER = "copyResultsDestinationUser";
    private static final String COPY_RESULTS_DESTINATION_PASSWORD = "copyResultsDestinationPassword";
    private final AgentStatsFlowContext flowContext;

    public AgentStatsFlowContextSerializer(@Nullable AgentStatsFlowContext flowContext) {
        super(AgentStatsFlowContextSerializer.class);
        this.flowContext = flowContext;
    }

    public AgentStatsFlowContext deserialize(String key, Map<String, String> serializedData) {
        LOGGER.debug("Serialized data: {}", serializedData);
        Map deserializedMap = this.deserializeMapWithKey(key, serializedData);
        LOGGER.debug("Deserialized data: {}", deserializedMap);
        List<String> agentNames = deserializeListNotNull(deserializedMap, AGENT_NAMES, String.class);
        List<String> buildNumbers = deserializeListNotNull(deserializedMap, BUILD_NUMBERS, String.class);
        List<String> buildSuffixes = deserializeListNotNull(deserializedMap, BUILD_SUFFIXES, String.class);
        List<Boolean> spmOn = deserializeListNotNull(deserializedMap, SPM_ON, Boolean.class);
        List<Boolean> siOn = deserializeListNotNull(deserializedMap, SI_ON, Boolean.class);
        List<Boolean> accOn = deserializeListNotNull(deserializedMap, ACC_ON, Boolean.class);
        List<Boolean> accMockOn = deserializeListNotNull(deserializedMap, ACC_MOCK_ON, Boolean.class);
        List<Boolean> btOn = deserializeListNotNull(deserializedMap, BT_ON, Boolean.class);
        List<Boolean> brtmOn = deserializeListNotNull(deserializedMap, BRTM_ON, Boolean.class);
        String outputFile = (String) deserializedMap.get(OUTPUT_FILE);
        String copyResultsDestinationDir = (String) deserializedMap.get(COPY_RESULTS_DESTINATION_DIR);
        String copyResultsDestinationFileName = (String) deserializedMap.get(COPY_RESULTS_DESTINATION_FILE_NAME);
        String copyResultsDestinationUser = (String) deserializedMap.get(COPY_RESULTS_DESTINATION_USER);
        String copyResultsDestinationPassword = (String) deserializedMap.get(COPY_RESULTS_DESTINATION_PASSWORD);

        if (outputFile == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: outputFile is missing.");
        } else {
            AgentStatsFlowContext.Builder builder = new AgentStatsFlowContext.Builder()
                    .agentNames(agentNames)
                    .buildNumbers(buildNumbers)
                    .buildSuffixes(buildSuffixes)
                    .spmOn(spmOn)
                    .siOn(siOn)
                    .accOn(accOn)
                    .accMockOn(accMockOn)
                    .btOn(btOn)
                    .brtmOn(brtmOn)
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
        customData.put(AGENT_NAMES, serializeList(this.flowContext.getAgentNames()));
        customData.put(BUILD_NUMBERS, serializeList(this.flowContext.getBuildNumbers()));
        customData.put(BUILD_SUFFIXES, serializeList(this.flowContext.getBuildSuffixes()));
        customData.put(SPM_ON, serializeList(this.flowContext.getSpmOn()));
        customData.put(SI_ON, serializeList(this.flowContext.getSiOn()));
        customData.put(ACC_ON, serializeList(this.flowContext.getAccOn()));
        customData.put(ACC_MOCK_ON, serializeList(this.flowContext.getAccMockOn()));
        customData.put(BT_ON, serializeList(this.flowContext.getBtOn()));
        customData.put(BRTM_ON, serializeList(this.flowContext.getBrtmOn()));
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
