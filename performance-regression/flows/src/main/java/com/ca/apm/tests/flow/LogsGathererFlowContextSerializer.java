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
public class LogsGathererFlowContextSerializer extends MyEnvPropertySerializerAbs<LogsGathererFlowContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogsGathererFlowContextSerializer.class);
    private static final String FILES_MAPPING = "filesMapping";
    private static final String SOURCE_DIR = "sourceDir";
    private static final String TARGET_DIR = "targetDir";
    private static final String TARGET_ZIP_FILE = "targetZipFile";
    private static final String DELETE_SOURCE = "deleteSource";
    private static final String ADD_TIMESTAMP = "addTimestamp";
    private static final String IGNORE_DELETION_ERRORS = "ignoreDeletionErrors";
    private static final String IGNORE_EMPTY = "ignoreEmpty";
    private static final String COPY_RESULTS_DESTINATION_DIR = "copyResultsDestinationDir";
    private static final String COPY_RESULTS_DESTINATION_FILE_NAME = "copyResultsDestinationFileName";
    private static final String COPY_RESULTS_DESTINATION_USER = "copyResultsDestinationUser";
    private static final String COPY_RESULTS_DESTINATION_PASSWORD = "copyResultsDestinationPassword";
    private final LogsGathererFlowContext flowContext;

    public LogsGathererFlowContextSerializer(@Nullable LogsGathererFlowContext flowContext) {
        super(LogsGathererFlowContextSerializer.class);
        this.flowContext = flowContext;
    }

    public LogsGathererFlowContext deserialize(String key, Map<String, String> serializedData) {
        LOGGER.debug("Serialized data: {}", serializedData);
        Map deserializedMap = this.deserializeMapWithKey(key, serializedData);
        LOGGER.debug("Deserialized data: {}", deserializedMap);
        String sourceDir = (String) deserializedMap.get(SOURCE_DIR);
        String targetDir = (String) deserializedMap.get(TARGET_DIR);
        String targetZipFile = (String) deserializedMap.get(TARGET_ZIP_FILE);
        Boolean deleteSource = Boolean.valueOf((String) deserializedMap.get(DELETE_SOURCE));
        Boolean addTimestamp = Boolean.valueOf((String) deserializedMap.get(ADD_TIMESTAMP));
        Boolean ignoreDeletionErrors = Boolean.valueOf((String) deserializedMap.get(IGNORE_DELETION_ERRORS));
        Boolean ignoreEmpty = Boolean.valueOf((String) deserializedMap.get(IGNORE_EMPTY));
        Map<String, String> filesMapping = deserializeMap(deserializedMap, FILES_MAPPING, String.class);
        String copyResultsDestinationDir = (String) deserializedMap.get(COPY_RESULTS_DESTINATION_DIR);
        String copyResultsDestinationFileName = (String) deserializedMap.get(COPY_RESULTS_DESTINATION_FILE_NAME);
        String copyResultsDestinationUser = (String) deserializedMap.get(COPY_RESULTS_DESTINATION_USER);
        String copyResultsDestinationPassword = (String) deserializedMap.get(COPY_RESULTS_DESTINATION_PASSWORD);
        if (filesMapping == null && (sourceDir == null || targetZipFile == null)) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: either filesMapping or sourceDir and targetZipFile is missing.");
        }
        if (targetDir == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: targetDir is missing.");
        }
        if (deleteSource == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: deleteSource is missing.");
        }
        if (addTimestamp == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: addTimestamp is missing.");
        }
        if (ignoreDeletionErrors == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: ignoreDeletionErrors is missing.");
        }
        if (ignoreEmpty == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: ignoreEmpty is missing.");
        } else {
            LogsGathererFlowContext.Builder builder = new LogsGathererFlowContext.Builder()
                    .sourceDir(sourceDir)
                    .targetDir(targetDir)
                    .targetZipFile(targetZipFile)
                    .deleteSource(deleteSource)
                    .addTimestamp(addTimestamp)
                    .ignoreDeletionErrors(ignoreDeletionErrors)
                    .ignoreEmpty(ignoreEmpty)
                    .filesMapping(filesMapping)
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
        if (this.flowContext.getSourceDir() != null)
            customData.put(SOURCE_DIR, this.flowContext.getSourceDir());
        customData.put(TARGET_DIR, this.flowContext.getTargetDir());
        if (this.flowContext.getTargetZipFile() != null)
            customData.put(TARGET_ZIP_FILE, this.flowContext.getTargetZipFile());
        customData.put(DELETE_SOURCE, this.flowContext.getDeleteSource().toString());
        customData.put(ADD_TIMESTAMP, this.flowContext.getAddTimestamp().toString());
        customData.put(IGNORE_DELETION_ERRORS, this.flowContext.getIgnoreDeletionErrors().toString());
        customData.put(IGNORE_EMPTY, this.flowContext.getIgnoreEmpty().toString());
        customData.put(FILES_MAPPING, serializeMap(this.flowContext.getFilesMapping()));
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
