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

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.ExtendedBuilderBase;
import com.ca.tas.property.EnvPropSerializable;
import org.apache.http.util.Args;

import java.util.Map;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class OracleDbUndeployFlowContext implements IFlowContext, EnvPropSerializable<OracleDbUndeployFlowContext> {

    private final String responseFileDir;
    private final String responseFileName;
    private final String installSourcesPath;
    private final String homePath;
    private final String dbSid;
    private final String superAdminSamePassword;
    private final String installLocation;

    private final transient OracleDbUndeployFlowContextSerializer envPropSerializer;

    protected OracleDbUndeployFlowContext(Builder builder) {

        responseFileDir = builder.responseFileDir;
        responseFileName = builder.responseFileName;
        installSourcesPath = builder.installSourcesPath;
        homePath = builder.homePath;
        dbSid = builder.dbSid;
        superAdminSamePassword = builder.superAdminSamePassword;
        installLocation = builder.installLocation;

        this.envPropSerializer = new OracleDbUndeployFlowContextSerializer(this);
    }

    public String getResponseFileDir() {
        return responseFileDir;
    }

    public String getResponseFileName() {
        return responseFileName;
    }

    public String getInstallSourcesPath() {
        return installSourcesPath;
    }

    public String getHomePath() {
        return homePath;
    }

    public String getDbSid() {
        return dbSid;
    }

    public String getSuperAdminSamePassword() {
        return superAdminSamePassword;
    }

    public String getInstallLocation() {
        return installLocation;
    }

    @Override
    public OracleDbUndeployFlowContext deserialize(String key, Map<String, String> serializedData) {
        return this.envPropSerializer.deserialize(key, serializedData);
    }

    @Override
    public Map<String, String> serialize(String key) {
        return this.envPropSerializer.serialize(key);
    }

    public static class Builder extends ExtendedBuilderBase<OracleDbUndeployFlowContext.Builder, OracleDbUndeployFlowContext> {

        protected String responseFileDir;
        protected String responseFileName;
        protected String installSourcesPath;
        protected String homePath;
        protected String dbSid;
        protected String superAdminSamePassword;
        protected String installLocation;

        public Builder() {

        }

        public OracleDbUndeployFlowContext build() {
            if (responseFileDir == null) {
                // responseFileDir in DeployOracleDbFlow may be null
                responseFileDir = installSourcesPath + "/database/response";
            }

            OracleDbUndeployFlowContext context = this.getInstance();

            Args.notNull(context.responseFileDir, "responseFileDir");
            Args.notNull(context.responseFileName, "responseFileName");
            Args.notNull(context.installSourcesPath, "installSourcesPath");
            Args.notNull(context.homePath, "homePath");
            Args.notNull(context.dbSid, "dbSid");
            Args.notNull(context.superAdminSamePassword, "superAdminSamePassword");
            Args.notNull(context.installLocation, "installLocation");

            return context;
        }

        protected OracleDbUndeployFlowContext getInstance() {
            return new OracleDbUndeployFlowContext(this);
        }

        public OracleDbUndeployFlowContext.Builder responseFileDir(String responseFileDir) {
            this.responseFileDir = responseFileDir;
            return this.builder();
        }

        public OracleDbUndeployFlowContext.Builder responseFileName(String responseFileName) {
            this.responseFileName = responseFileName;
            return this.builder();
        }

        public OracleDbUndeployFlowContext.Builder installSourcesPath(String installSourcesPath) {
            this.installSourcesPath = installSourcesPath;
            return this.builder();
        }

        public OracleDbUndeployFlowContext.Builder homePath(String homePath) {
            this.homePath = homePath;
            return this.builder();
        }

        public OracleDbUndeployFlowContext.Builder dbSid(String dbSid) {
            this.dbSid = dbSid;
            return this.builder();
        }

        public OracleDbUndeployFlowContext.Builder superAdminSamePassword(String superAdminSamePassword) {
            this.superAdminSamePassword = superAdminSamePassword;
            return this.builder();
        }

        public OracleDbUndeployFlowContext.Builder installLocation(String installLocation) {
            this.installLocation = installLocation;
            return this.builder();
        }

        protected OracleDbUndeployFlowContext.Builder builder() {
            return this;
        }
    }
}
