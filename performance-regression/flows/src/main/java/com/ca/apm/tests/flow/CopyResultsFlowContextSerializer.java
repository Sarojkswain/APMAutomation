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
public class CopyResultsFlowContextSerializer extends AbstractEnvPropertySerializer<CopyResultsFlowContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CopyResultsFlowContextSerializer.class);
    private static final String COPY_RESULTS_DESTINATION_DIR = "copyResultsDestinationDir";
    private static final String COPY_RESULTS_DESTINATION_FILE_NAME = "copyResultsDestinationFileName";
    private final CopyResultsFlowContext flowContext;

    public CopyResultsFlowContextSerializer(@Nullable CopyResultsFlowContext flowContext) {
        super(CopyResultsFlowContextSerializer.class);
        this.flowContext = flowContext;
    }

    public CopyResultsFlowContext deserialize(String key, Map<String, String> serializedData) {
        LOGGER.debug("Serialized data: {}", serializedData);
        Map deserializedMap = this.deserializeMapWithKey(key, serializedData);
        LOGGER.debug("Deserialized data: {}", deserializedMap);
        String copyResultsDestinationDir = (String) deserializedMap.get(COPY_RESULTS_DESTINATION_DIR);
        String copyResultsDestinationFileName = (String) deserializedMap.get(COPY_RESULTS_DESTINATION_FILE_NAME);
//        if (outputFileName == null) {
//            throw new IllegalArgumentException("Insufficient arguments in env property file: outputFileName is missing.");
//        } else {
        CopyResultsFlowContext.Builder builder = new CopyResultsFlowContext.Builder()
                .copyResultsDestinationDir(copyResultsDestinationDir)
                .copyResultsDestinationFileName(copyResultsDestinationFileName);
        return builder.build();
//        }
    }

    public Map<String, String> serialize(String key) {
        Args.notNull(this.flowContext, "Flow context");
        HashMap customData = new HashMap();
        if (this.flowContext.getCopyResultsDestinationDir() != null)
            customData.put(COPY_RESULTS_DESTINATION_DIR, this.flowContext.getCopyResultsDestinationDir());
        if (this.flowContext.getCopyResultsDestinationFileName() != null)
            customData.put(COPY_RESULTS_DESTINATION_FILE_NAME, this.flowContext.getCopyResultsDestinationFileName());
        Map serializedData1 = super.serialize(key);
        serializedData1.putAll(this.serializeMapWithKey(key, customData));
        return serializedData1;
    }
}
