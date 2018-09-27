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

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.ExtendedBuilderBase;
import org.apache.http.util.Args;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Flow Context for installing Trade6 WebApp into Websphere
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class Trade6FlowContext implements IFlowContext {

    private final Map<String, String> propertiesFileOptions;

    private final URL deployPackageUrl;
    private final String deploySourcesLocation;

    private final String applicationEarFileName;
    private final String installScriptFileName;
    private final String resourcesScriptFileName;

    private final String nodeName;
    private final String serverName;
    private final String defaultProvider;
    private final String tradeDbDatabaseName;
    private final String dbHostname;
    private final Integer dbTnsPort;
    private final String dbUser;
    private final String dbPassword;
    private final String deployType;
    private final String ojdbcPath;

    private final String websphereInstallPath;
    private final String websphereProfileBinPath;

    protected Trade6FlowContext(Trade6FlowContext.Builder builder) {
        this.propertiesFileOptions = builder.propertiesFileOptions;
        this.deployPackageUrl = builder.deployPackageUrl;
        this.deploySourcesLocation = builder.deploySourcesLocation;
        this.applicationEarFileName = builder.applicationEarFileName;
        this.installScriptFileName = builder.installScriptFileName;
        this.resourcesScriptFileName = builder.resourcesScriptFileName;

        this.nodeName = builder.nodeName;
        this.serverName = builder.serverName;
        this.defaultProvider = builder.defaultProvider;
        this.tradeDbDatabaseName = builder.tradeDbDatabaseName;
        this.dbHostname = builder.dbHostname;
        this.dbTnsPort = builder.dbTnsPort;
        this.dbUser = builder.dbUser;
        this.dbPassword = builder.dbPassword;
        this.deployType = builder.deployType;
        this.ojdbcPath = builder.ojdbcPath;

        this.websphereInstallPath = builder.websphereInstallPath;
        this.websphereProfileBinPath = builder.websphereProfileBinPath;
    }

    public Map<String, String> getPropertiesFileOptions() {
        return propertiesFileOptions;
    }

    public URL getDeployPackageUrl() {
        return deployPackageUrl;
    }

    public String getDeploySourcesLocation() {
        return deploySourcesLocation;
    }

    public String getApplicationEarFileName() {
        return applicationEarFileName;
    }

    public String getInstallScriptFileName() {
        return installScriptFileName;
    }

    public String getResourcesScriptFileName() {
        return resourcesScriptFileName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getServerName() {
        return serverName;
    }

    public String getDefaultProvider() {
        return defaultProvider;
    }

    public String getTradeDbDatabaseName() {
        return tradeDbDatabaseName;
    }

    public String getDbHostname() {
        return dbHostname;
    }

    public Integer getDbTnsPort() {
        return dbTnsPort;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getDeployType() {
        return deployType;
    }

    public String getOjdbcPath() {
        return ojdbcPath;
    }

    public String getWebsphereInstallPath() {
        return websphereInstallPath;
    }

    public String getWebsphereProfileBinPath() {
        return websphereProfileBinPath;
    }

    public static class Builder extends ExtendedBuilderBase<Trade6FlowContext.Builder, Trade6FlowContext> {
        protected Map<String, String> propertiesFileOptions = new HashMap<>();

        protected URL deployPackageUrl;
        protected String deploySourcesLocation;

        protected String applicationEarFileName;
        protected String installScriptFileName;
        protected String resourcesScriptFileName;

        protected String nodeName;
        protected String serverName;
        protected String defaultProvider;
        protected String tradeDbDatabaseName;
        protected String dbHostname;
        protected Integer dbTnsPort;
        protected String dbUser;
        protected String dbPassword;
        protected String deployType;
        protected String ojdbcPath;

        protected String websphereInstallPath;
        protected String websphereProfileBinPath;

        public Builder() {
            this.deploySourcesLocation(this.concatPaths(this.getDeployBase(), "was_trade6"));

            this.nodeName("node01");
            this.serverName("server1");
            this.defaultProvider("Oracle");
            this.tradeDbDatabaseName("tradedb");
            this.dbTnsPort(1521);
            this.dbUser("TRADE");
            this.dbPassword("TRADE");
            this.deployType("ORACLE_V10G");

        }

        public Trade6FlowContext build() {
            this.initPropertiesFileData();
            Trade6FlowContext context = this.getInstance();
            Args.notNull(context.deployPackageUrl, "deployPackageUrl");
            Args.notNull(context.deploySourcesLocation, "deploySourcesLocation");
            Args.notNull(context.applicationEarFileName, "applicationEarFileName");
            Args.notNull(context.installScriptFileName, "installScriptFileName");
            Args.notNull(context.resourcesScriptFileName, "resourcesScriptFileName");

            Args.notNull(context.nodeName, "nodeName");
            Args.notNull(context.serverName, "serverName");
            Args.notNull(context.defaultProvider, "defaultProvider");
            Args.notNull(context.tradeDbDatabaseName, "tradeDbDatabaseName");
            Args.notNull(context.dbHostname, "dbHostname");
            Args.notNull(context.dbTnsPort, "dbTnsPort");
            Args.notNull(context.dbUser, "dbUser");
            Args.notNull(context.dbPassword, "dbPassword");
            Args.notNull(context.deployType, "deployType");
            Args.notNull(context.ojdbcPath, "ojdbcPath");

            Args.notNull(context.websphereInstallPath, "websphereInstallPath");
            Args.notNull(context.websphereProfileBinPath, "websphereProfileBinPath");
            return context;
        }

        protected void initPropertiesFileData() {
            this.propertiesFileOptions.put("NODE_NAME", this.nodeName);
            this.propertiesFileOptions.put("SERVER_NAME", this.serverName);
            this.propertiesFileOptions.put("DEFAULT_PROVIDER", this.defaultProvider);
            this.propertiesFileOptions.put("DB_NAME", this.tradeDbDatabaseName);
            this.propertiesFileOptions.put("HOST_NAME", this.dbHostname);
            this.propertiesFileOptions.put("PORT", String.valueOf(this.dbTnsPort));
            this.propertiesFileOptions.put("USER", this.dbUser);
            this.propertiesFileOptions.put("PASSWORD", this.dbPassword);
            this.propertiesFileOptions.put("DEPLOY_TYPE", this.deployType);
            this.propertiesFileOptions.put("OJDBC_PATH", this.ojdbcPath);
        }

        protected Trade6FlowContext getInstance() {
            return new Trade6FlowContext(this);
        }

        public Trade6FlowContext.Builder deployPackageUrl(URL deployPackageUrl) {
            this.deployPackageUrl = deployPackageUrl;
            return this.builder();
        }

        public Trade6FlowContext.Builder deploySourcesLocation(String deploySourcesLocation) {
            this.deploySourcesLocation = deploySourcesLocation;
            return this.builder();
        }

        public Trade6FlowContext.Builder applicationEarFileName(String applicationEarFileName) {
            this.applicationEarFileName = applicationEarFileName;
            return this.builder();
        }

        public Trade6FlowContext.Builder installScriptFileName(String installScriptFileName) {
            this.installScriptFileName = installScriptFileName;
            return this.builder();
        }

        public Trade6FlowContext.Builder resourcesScriptFileName(String resourcesScriptFileName) {
            this.resourcesScriptFileName = resourcesScriptFileName;
            return this.builder();
        }

        public Trade6FlowContext.Builder nodeName(String nodeName) {
            this.nodeName = nodeName;
            return this.builder();
        }

        public Trade6FlowContext.Builder serverName(String serverName) {
            this.serverName = serverName;
            return this.builder();
        }

        public Trade6FlowContext.Builder defaultProvider(String defaultProvider) {
            this.defaultProvider = defaultProvider;
            return this.builder();
        }

        public Trade6FlowContext.Builder tradeDbDatabaseName(String tradeDbDatabaseName) {
            this.tradeDbDatabaseName = tradeDbDatabaseName;
            return this.builder();
        }

        public Trade6FlowContext.Builder dbHostname(String dbHostname) {
            this.dbHostname = dbHostname;
            return this.builder();
        }

        public Trade6FlowContext.Builder dbTnsPort(Integer dbTnsPort) {
            this.dbTnsPort = dbTnsPort;
            return this.builder();
        }

        public Trade6FlowContext.Builder dbUser(String dbUser) {
            this.dbUser = dbUser;
            return this.builder();
        }

        public Trade6FlowContext.Builder dbPassword(String dbPassword) {
            this.dbPassword = dbPassword;
            return this.builder();
        }

        public Trade6FlowContext.Builder deployType(String deployType) {
            this.deployType = deployType;
            return this.builder();
        }

        public Trade6FlowContext.Builder ojdbcPath(String ojdbcPath) {
            this.ojdbcPath = ojdbcPath;
            return this.builder();
        }

        public Trade6FlowContext.Builder websphereInstallPath(String websphereInstallPath) {
            this.websphereInstallPath = websphereInstallPath;
            return this.builder();
        }

        public Trade6FlowContext.Builder websphereProfileBinPath(String websphereProfileBinPath) {
            this.websphereProfileBinPath = websphereProfileBinPath;
            return this.builder();
        }

        protected Trade6FlowContext.Builder builder() {
            return this;
        }
    }
}