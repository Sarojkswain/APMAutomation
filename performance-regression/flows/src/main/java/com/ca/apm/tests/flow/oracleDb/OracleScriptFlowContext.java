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
import org.apache.http.util.Args;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Basic Flow Context for running a SQL Script in Oracle DB
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class OracleScriptFlowContext implements IFlowContext {

    public static final String DEFAULT_RUN_AS_USER = "sys";
    public static final String DEFAULT_RUN_AS_PASSWORD = "password";
    public static final String DEFAULT_SCHEMA_NAME = "tradedb";

    private final String encoding;
    private final String runAsUser;
    private final String runAsPassword;
    private final boolean runAsSysdba;
    private final String schemaName;
    private final URL deployPackageUrl;
    private final String deploySourcesLocation;
    private final String plsqlExecutableLocation;


    protected OracleScriptFlowContext(OracleScriptFlowContext.Builder builder) {
        this.encoding = builder.getEncoding();
        this.runAsUser = builder.runAsUser;
        this.runAsPassword = builder.runAsPassword;
        this.runAsSysdba = builder.runAsSysdba;
        this.schemaName = builder.schemaName;
        this.deployPackageUrl = builder.deployPackageUrl;
        this.deploySourcesLocation = builder.deploySourcesLocation;
        this.plsqlExecutableLocation = builder.plsqlExecutableLocation;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getRunAsUser() {
        return runAsUser;
    }

    public String getRunAsPassword() {
        return runAsPassword;
    }

    public boolean isRunAsSysdba() {
        return runAsSysdba;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public URL getDeployPackageUrl() {
        return deployPackageUrl;
    }

    public String getDeploySourcesLocation() {
        return deploySourcesLocation;
    }

    public String getPlsqlExecutableLocation() {
        return plsqlExecutableLocation;
    }

    public static class Builder extends ExtendedBuilderBase<OracleScriptFlowContext.Builder, OracleScriptFlowContext> {
        public static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;
        protected String runAsUser;
        protected String runAsPassword;
        protected boolean runAsSysdba;
        protected String schemaName;
        protected URL deployPackageUrl;
        protected String deploySourcesLocation;
        protected String plsqlExecutableLocation;

        public Builder() {
            this.encoding(DEFAULT_ENCODING);
            this.runAsUser(DEFAULT_RUN_AS_USER);
            this.runAsPassword(DEFAULT_RUN_AS_PASSWORD);
            this.runAsSysdba(true);
            this.schemaName(DEFAULT_SCHEMA_NAME);
            this.deploySourcesLocation(this.concatPaths(this.getDeployBase(), "tradedb_scripts"));
        }

        public OracleScriptFlowContext build() {
            OracleScriptFlowContext context = this.getInstance();
            Args.notNull(context.runAsUser, "runAsUser");
            Args.notNull(context.runAsPassword, "runAsPassword");
            Args.notNull(context.runAsSysdba, "runAsSysdba");
            Args.notNull(context.schemaName, "schemaName");
            Args.notNull(context.deployPackageUrl, "deployPackageUrl");
            Args.notNull(context.deploySourcesLocation, "deploySourcesLocation");
            Args.notNull(context.plsqlExecutableLocation, "plsqlExecutableLocation");
            return context;
        }

        protected OracleScriptFlowContext getInstance() {
            return new OracleScriptFlowContext(this);
        }

        public OracleScriptFlowContext.Builder runAsUser(String user) {
            this.runAsUser = user;
            return this.builder();
        }

        public OracleScriptFlowContext.Builder runAsPassword(String password) {
            this.runAsPassword = password;
            return this.builder();
        }

        public OracleScriptFlowContext.Builder runAsSysdba(boolean runAsSysdba) {
            this.runAsSysdba = runAsSysdba;
            return this.builder();
        }

        public OracleScriptFlowContext.Builder schemaName(String schemaName) {
            this.schemaName = schemaName;
            return this.builder();
        }

        public OracleScriptFlowContext.Builder deployPackageUrl(URL deployPackageUrl) {
            this.deployPackageUrl = deployPackageUrl;
            return this.builder();
        }

        public OracleScriptFlowContext.Builder deploySourcesLocation(String deploySourcesLocation) {
            this.deploySourcesLocation = deploySourcesLocation;
            return this.builder();
        }

        public OracleScriptFlowContext.Builder plsqlExecutableLocation(String plsqlExecutableLocation) {
            this.plsqlExecutableLocation = plsqlExecutableLocation;
            return this.builder();
        }

        protected OracleScriptFlowContext.Builder builder() {
            return this;
        }
    }
}