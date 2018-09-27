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
package com.ca.apm.tests.flow.oracleDb;

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
public class OracleDbUndeployFlowContextSerializer extends AbstractEnvPropertySerializer<OracleDbUndeployFlowContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OracleDbUndeployFlowContextSerializer.class);
    private static final String RESPONSE_FILE_DIR = "responseFileDir";
    private static final String RESPONSE_FILE_NAME = "responseFileName";
    private static final String INSTALL_SOURCES_DIR = "installSourcesPath";
    private static final String HOME_PATH = "homePath";
    private static final String DB_SID = "dbSid";
    private static final String SUPER_ADMIN_SAME_PASSWORD = "superAdminSamePassword";
    private static final String INSTALL_LOCATION = "installLocation";
    private final OracleDbUndeployFlowContext flowContext;

    public OracleDbUndeployFlowContextSerializer(@Nullable OracleDbUndeployFlowContext flowContext) {
        super(OracleDbUndeployFlowContextSerializer.class);
        this.flowContext = flowContext;
    }

    public OracleDbUndeployFlowContext deserialize(String key, Map<String, String> serializedData) {
        LOGGER.debug("Serialized data: {}", serializedData);
        Map deserializedMap = this.deserializeMapWithKey(key, serializedData);
        LOGGER.debug("Deserialized data: {}", deserializedMap);
        String responseFileDir = (String) deserializedMap.get(RESPONSE_FILE_DIR);
        String responseFileName = (String) deserializedMap.get(RESPONSE_FILE_NAME);
        String installSourcesPath = (String) deserializedMap.get(INSTALL_SOURCES_DIR);
        String homePath = (String) deserializedMap.get(HOME_PATH);
        String dbSid = (String) deserializedMap.get(DB_SID);
        String superAdminSamePassword = (String) deserializedMap.get(SUPER_ADMIN_SAME_PASSWORD);
        String installLocation = (String) deserializedMap.get(INSTALL_LOCATION);
        if (responseFileDir == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: responseFileDir is missing.");
        }
        if (responseFileName == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: responseFileName is missing.");
        }
        if (installSourcesPath == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: installSourcesPath is missing.");
        }
        if (homePath == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: homePath is missing.");
        }
        if (dbSid == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: dbSid is missing.");
        }
        if (superAdminSamePassword == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: superAdminSamePassword is missing.");
        }
        if (installLocation == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: installLocation is missing.");
        } else {
            OracleDbUndeployFlowContext.Builder builder = new OracleDbUndeployFlowContext.Builder()
                    .responseFileDir(responseFileDir).responseFileName(responseFileName).installSourcesPath(installSourcesPath)
                    .homePath(homePath).dbSid(dbSid).superAdminSamePassword(superAdminSamePassword)
                    .installLocation(installLocation);
            return builder.build();
        }
    }

    public Map<String, String> serialize(String key) {
        Args.notNull(this.flowContext, "Flow context");
        HashMap customData = new HashMap();
        customData.put(RESPONSE_FILE_DIR, this.flowContext.getResponseFileDir());
        customData.put(RESPONSE_FILE_NAME, this.flowContext.getResponseFileName());
        customData.put(INSTALL_SOURCES_DIR, this.flowContext.getInstallSourcesPath());
        customData.put(HOME_PATH, this.flowContext.getHomePath());
        customData.put(DB_SID, this.flowContext.getDbSid());
        customData.put(SUPER_ADMIN_SAME_PASSWORD, this.flowContext.getSuperAdminSamePassword());
        customData.put(INSTALL_LOCATION, this.flowContext.getInstallLocation());
        Map serializedData1 = super.serialize(key);
        serializedData1.putAll(this.serializeMapWithKey(key, customData));
        return serializedData1;
    }
}
