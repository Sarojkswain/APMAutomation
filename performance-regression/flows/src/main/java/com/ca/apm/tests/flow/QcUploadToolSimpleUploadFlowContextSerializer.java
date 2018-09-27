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

import org.apache.http.util.Args;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class QcUploadToolSimpleUploadFlowContextSerializer extends MyEnvPropertySerializerAbs<QcUploadToolSimpleUploadFlowContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(QcUploadToolSimpleUploadFlowContextSerializer.class);
    private static final String INSTALL_PATH = "installPath";
    private static final String JAVA_HOME = "javaHome";
    private static final String TEST_SET_FOLDER = "testSetFolder";
    private static final String TEST_SET_NAME = "testSetName";
    private static final String TEST_ID = "testId";
    private static final String PASSED = "passed";
    private final QcUploadToolSimpleUploadFlowContext flowContext;

    public QcUploadToolSimpleUploadFlowContextSerializer(@Nullable QcUploadToolSimpleUploadFlowContext flowContext) {
        super(QcUploadToolSimpleUploadFlowContextSerializer.class);
        this.flowContext = flowContext;
    }

    public QcUploadToolSimpleUploadFlowContext deserialize(String key, Map<String, String> serializedData) {
        LOGGER.debug("Serialized data: {}", serializedData);
        Map<?, ?> deserializedMap = this.deserializeMapWithKey(key, serializedData);
        LOGGER.debug("Deserialized data: {}", deserializedMap);
        String installPath = (String) deserializedMap.get(INSTALL_PATH);
        String javaHome = (String) deserializedMap.get(JAVA_HOME);
        String testSetFolder = (String) deserializedMap.get(TEST_SET_FOLDER);
        String testSetName = (String) deserializedMap.get(TEST_SET_NAME);
        String testId = (String) deserializedMap.get(TEST_ID);
        Boolean passed = Boolean.valueOf((String) deserializedMap.get(PASSED));
        if (installPath == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: installPath is missing.");
        } else {
            QcUploadToolSimpleUploadFlowContext.Builder builder = new QcUploadToolSimpleUploadFlowContext.Builder()
                    .installPath(installPath).javaHome(javaHome).testSetFolder(testSetFolder)
                    .testSetName(testSetName).testId(testId).passed(passed);
            return builder.build();
        }
    }

    public Map<String, String> serialize(String key) {
        Args.notNull(this.flowContext, "Flow context");
        HashMap<String, String> customData = new HashMap<String, String>();
        customData.put(INSTALL_PATH, this.flowContext.getInstallPath());
        if (this.flowContext.getJavaHome() != null) {
            customData.put(JAVA_HOME, this.flowContext.getJavaHome());
        }
        if (this.flowContext.getTestSetFolder() != null) {
            customData.put(TEST_SET_FOLDER, this.flowContext.getTestSetFolder());
        }
        if (this.flowContext.getTestSetName() != null) {
            customData.put(TEST_SET_NAME, this.flowContext.getTestSetName());
        }
        if (this.flowContext.getTestId() != null) {
            customData.put(TEST_ID, this.flowContext.getTestId());
        }
        if (this.flowContext.getPassed() != null) {
            customData.put(PASSED, String.valueOf(this.flowContext.getPassed()));
        }
        Map<String, String> serializedData1 = super.serialize(key);
        serializedData1.putAll(this.serializeMapWithKey(key, customData));
        return serializedData1;
    }
}
