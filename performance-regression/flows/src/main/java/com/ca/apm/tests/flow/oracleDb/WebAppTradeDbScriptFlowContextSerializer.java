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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class WebAppTradeDbScriptFlowContextSerializer extends AbstractEnvPropertySerializer<WebAppTradeDbScriptFlowContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebAppTradeDbScriptFlowContextSerializer.class);
    private static final String SQL_FILE_NAME = "sqlFileName";
    private static final String RUN_AS_SYSDBA = "runAsSysdba";
    private static final String RUN_AS_USER = "runAsUser";
    private static final String RUN_AS_PASSWORD = "runAsPassword";
    private static final String PLSQL_EXECUTABLE_LOCATION = "plsqlExecutableLocation";
    private static final String DEPLOY_PACKAGE_URL = "deployPackageUrl";
    private final WebAppTradeDbScriptFlowContext flowContext;

    public WebAppTradeDbScriptFlowContextSerializer(@Nullable WebAppTradeDbScriptFlowContext flowContext) {
        super(WebAppTradeDbScriptFlowContextSerializer.class);
        this.flowContext = flowContext;
    }

    public WebAppTradeDbScriptFlowContext deserialize(String key, Map<String, String> serializedData) {
        LOGGER.debug("Serialized data: {}", serializedData);
        Map deserializedMap = this.deserializeMapWithKey(key, serializedData);
        LOGGER.debug("Deserialized data: {}", deserializedMap);
        String sqlFileName = (String) deserializedMap.get(SQL_FILE_NAME);
        Boolean runAsSysdba = Boolean.valueOf((String) deserializedMap.get(RUN_AS_SYSDBA));
        String runAsUser = (String) deserializedMap.get(RUN_AS_USER);
        String runAsPassword = (String) deserializedMap.get(RUN_AS_PASSWORD);
        String plsqlExecutableLocation = (String) deserializedMap.get(PLSQL_EXECUTABLE_LOCATION);
        URL deployPackageUrl = null;
        String deployPackageUrlString = (String) deserializedMap.get(DEPLOY_PACKAGE_URL);
        if (deployPackageUrlString == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: deployPackageUrl (String) is missing.");
        } else {
            try {
                deployPackageUrl = new URL(deployPackageUrlString);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Error parsing URL arguments in env property file: deployPackageUrl is missing.");
            }
        }
        if (sqlFileName == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: sqlFileName is missing.");
        }
        if (runAsSysdba == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: runAsSysdba is missing.");
        }
        if (runAsUser == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: runAsUser is missing.");
        }
        if (runAsPassword == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: runAsPassword is missing.");
        }
        if (plsqlExecutableLocation == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: plsqlExecutableLocation is missing.");
        } else {
            WebAppTradeDbScriptFlowContext.Builder builder = new WebAppTradeDbScriptFlowContext.Builder()
                    .sqlFileName(sqlFileName);
            builder.runAsSysdba(runAsSysdba).runAsUser(runAsUser).runAsPassword(runAsPassword)
                    .plsqlExecutableLocation(plsqlExecutableLocation).deployPackageUrl(deployPackageUrl);
            return builder.build();
        }
    }

    public Map<String, String> serialize(String key) {
        Args.notNull(this.flowContext, "Flow context");
        HashMap customData = new HashMap();
        customData.put(DEPLOY_PACKAGE_URL, String.valueOf(this.flowContext.getDeployPackageUrl()));
        customData.put(SQL_FILE_NAME, this.flowContext.getSqlFileName());
        customData.put(RUN_AS_SYSDBA, String.valueOf(this.flowContext.isRunAsSysdba()));
        customData.put(RUN_AS_USER, this.flowContext.getRunAsUser());
        customData.put(RUN_AS_PASSWORD, this.flowContext.getRunAsPassword());
        customData.put(PLSQL_EXECUTABLE_LOCATION, this.flowContext.getPlsqlExecutableLocation());
        Map serializedData1 = super.serialize(key);
        serializedData1.putAll(this.serializeMapWithKey(key, customData));
        return serializedData1;
    }
}
