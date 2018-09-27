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
public class JMeterLogConverterFlowContextSerializer extends AbstractEnvPropertySerializer<JMeterLogConverterFlowContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JMeterLogConverterFlowContextSerializer.class);
    private static final String JMETER_LOG_CONVERTER_JAR_PATH = "jmeterLogConverterJarPath";
    private static final String OUTPUT_FILE_NAME = "outputFileName";
    private final JMeterLogConverterFlowContext flowContext;

    public JMeterLogConverterFlowContextSerializer(@Nullable JMeterLogConverterFlowContext flowContext) {
        super(JMeterLogConverterFlowContextSerializer.class);
        this.flowContext = flowContext;
    }

    public JMeterLogConverterFlowContext deserialize(String key, Map<String, String> serializedData) {
        LOGGER.debug("Serialized data: {}", serializedData);
        Map deserializedMap = this.deserializeMapWithKey(key, serializedData);
        LOGGER.debug("Deserialized data: {}", deserializedMap);
        String jmeterLogConverterJarPath = (String) deserializedMap.get(JMETER_LOG_CONVERTER_JAR_PATH);
        String outputFileName = (String) deserializedMap.get(OUTPUT_FILE_NAME);
        if (jmeterLogConverterJarPath == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: jmeterLogConverterJarPath is missing.");
        }
        if (outputFileName == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: outputFileName is missing.");
        } else {
            JMeterLogConverterFlowContext.Builder builder = new JMeterLogConverterFlowContext.Builder()
                    .jmeterLogConverterJarPath(jmeterLogConverterJarPath)
                    .outputFileName(outputFileName);
            return builder.build();
        }
    }

    public Map<String, String> serialize(String key) {
        Args.notNull(this.flowContext, "Flow context");
        HashMap customData = new HashMap();
        customData.put(JMETER_LOG_CONVERTER_JAR_PATH, this.flowContext.getJmeterLogConverterJarPath());
        customData.put(OUTPUT_FILE_NAME, this.flowContext.getOutputFileName());
        Map serializedData1 = super.serialize(key);
        serializedData1.putAll(this.serializeMapWithKey(key, customData));
        return serializedData1;
    }
}
