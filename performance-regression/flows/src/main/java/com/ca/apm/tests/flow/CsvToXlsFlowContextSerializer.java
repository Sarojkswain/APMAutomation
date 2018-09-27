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
public class CsvToXlsFlowContextSerializer extends MyEnvPropertySerializerAbs<CsvToXlsFlowContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvToXlsFlowContextSerializer.class);
    private static final String SHARE_DIR = "shareDir";
    private static final String CSVTOXLS_JAR_PATH = "csvToXlsJarPath";
    private static final String TEMPLATE_FILE_NAME = "templateFileName";
    private static final String OUTPUT_FILE_NAME = "outputFileName";
    private static final String HEAP_MEMORY = "heapMemory";
    private static final String SHEETS_MAPPING = "sheetsMapping";
    private final CsvToXlsFlowContext flowContext;

    public CsvToXlsFlowContextSerializer(@Nullable CsvToXlsFlowContext flowContext) {
        super(CsvToXlsFlowContextSerializer.class);
        this.flowContext = flowContext;
    }

    public CsvToXlsFlowContext deserialize(String key, Map<String, String> serializedData) {
        LOGGER.debug("Serialized data: {}", serializedData);
        Map<?, ?> deserializedMap = this.deserializeMapWithKey(key, serializedData);
        LOGGER.debug("Deserialized data: {}", deserializedMap);
        String shareDir = (String) deserializedMap.get(SHARE_DIR);
        String csvToXlsJarPath = (String) deserializedMap.get(CSVTOXLS_JAR_PATH);
        String templateFileName = (String) deserializedMap.get(TEMPLATE_FILE_NAME);
        String outputFileName = (String) deserializedMap.get(OUTPUT_FILE_NAME);
        String heapMemory = (String) deserializedMap.get(HEAP_MEMORY);
        Map<String, String> sheetsMapping = deserializeMap(deserializedMap, SHEETS_MAPPING, String.class);
        if (shareDir == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: shareDir is missing.");
        }
        if (csvToXlsJarPath == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: csvToXlsJarPath is missing.");
        }
        if (templateFileName == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: templateFileName is missing.");
        }
        if (outputFileName == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: outputFileName is missing.");
        }
        if (heapMemory == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: heapMemory is missing.");
        } else {
            CsvToXlsFlowContext.Builder builder = new CsvToXlsFlowContext.Builder()
                    .shareDir(shareDir).templateFileName(templateFileName).csvToXlsJarPath(csvToXlsJarPath)
                    .outputFileName(outputFileName).heapMemory(heapMemory).sheetsMapping(sheetsMapping);
            return builder.build();
        }
    }

    public Map<String, String> serialize(String key) {
        Args.notNull(this.flowContext, "Flow context");
        HashMap<String, String> customData = new HashMap<String, String>();
        customData.put(SHARE_DIR, this.flowContext.getShareDir());
        customData.put(CSVTOXLS_JAR_PATH, this.flowContext.getCsvToXlsJarPath());
        customData.put(TEMPLATE_FILE_NAME, this.flowContext.getTemplateFileName());
        customData.put(OUTPUT_FILE_NAME, this.flowContext.getOutputFileName());
        customData.put(HEAP_MEMORY, this.flowContext.getHeapMemory());
        customData.put(SHEETS_MAPPING, serializeMap(this.flowContext.getSheetsMapping()));
        Map<String, String> serializedData1 = super.serialize(key);
        serializedData1.putAll(this.serializeMapWithKey(key, customData));
        return serializedData1;
    }
}
