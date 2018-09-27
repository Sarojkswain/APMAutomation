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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Flow Context for installing StockTrader WebApp into Weblogic
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class StockTraderFlowContext extends WeblogicWlstFlowContext {

    private final Map<String, String> propertiesFileOptions;

    private final URL deployPackageUrl;
    private final String deploySourcesLocation;

    private final String applicationEarFileName;
    private final String applicationBslEarFileName;
    private final String createDatasourceScriptFileName;
    private final String deleteDatasourceScriptFileName;
    private final String propertiesFileFileName;

    private final String weblogicDomainName;
    private final Integer weblogicPort;
    private final String weblogicUserName;
    private final String weblogicUserPassword;
    private final Integer tradeDbPoolInitCapacity;
    private final Integer tradeDbPoolMaxCapacity;
    private final String tradeDbJndi;
    private final String tradeDbDatabaseName;
    private final String weblogicTargetServer;
    private final String dbHostname;
    private final Integer dbTnsPort;
    private final String dbUser;
    private final String dbPassword;

    protected StockTraderFlowContext(StockTraderFlowContext.Builder builder) {
        super(builder);
        this.propertiesFileOptions = builder.propertiesFileOptions;
        this.deployPackageUrl = builder.deployPackageUrl;
        this.deploySourcesLocation = builder.deploySourcesLocation;
        this.applicationEarFileName = builder.applicationEarFileName;
        this.applicationBslEarFileName = builder.applicationBslEarFileName;
        this.createDatasourceScriptFileName = builder.createDatasourceScriptFileName;
        this.deleteDatasourceScriptFileName = builder.deleteDatasourceScriptFileName;
        this.propertiesFileFileName = builder.propertiesFileFileName;

        this.weblogicDomainName = builder.weblogicDomainName;
        this.weblogicPort = builder.weblogicPort;
        this.weblogicUserName = builder.weblogicUserName;
        this.weblogicUserPassword = builder.weblogicUserPassword;
        this.tradeDbPoolInitCapacity = builder.tradeDbPoolInitCapacity;
        this.tradeDbPoolMaxCapacity = builder.tradeDbPoolMaxCapacity;
        this.tradeDbJndi = builder.tradeDbJndi;
        this.tradeDbDatabaseName = builder.tradeDbDatabaseName;
        this.weblogicTargetServer = builder.weblogicTargetServer;
        this.dbHostname = builder.dbHostname;
        this.dbTnsPort = builder.dbTnsPort;
        this.dbUser = builder.dbUser;
        this.dbPassword = builder.dbPassword;
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

    public String getApplicationBslEarFileName() {
        return applicationBslEarFileName;
    }

    public String getCreateDatasourceScriptFileName() {
        return createDatasourceScriptFileName;
    }

    public String getDeleteDatasourceScriptFileName() {
        return deleteDatasourceScriptFileName;
    }

    public String getPropertiesFileFileName() {
        return propertiesFileFileName;
    }

    public String getWeblogicDomainName() {
        return weblogicDomainName;
    }

    public Integer getWeblogicPort() {
        return weblogicPort;
    }

    public String getWeblogicUserName() {
        return weblogicUserName;
    }

    public String getWeblogicUserPassword() {
        return weblogicUserPassword;
    }

    public Integer getTradeDbPoolInitCapacity() {
        return tradeDbPoolInitCapacity;
    }

    public Integer getTradeDbPoolMaxCapacity() {
        return tradeDbPoolMaxCapacity;
    }

    public String getTradeDbJndi() {
        return tradeDbJndi;
    }

    public String getTradeDbDatabaseName() {
        return tradeDbDatabaseName;
    }

    public String getWeblogicTargetServer() {
        return weblogicTargetServer;
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

    public static class Builder extends WeblogicWlstFlowContext.Builder {
        protected Map<String, String> propertiesFileOptions = new HashMap<>();
        ;

        protected URL deployPackageUrl;
        protected String deploySourcesLocation;

        protected String applicationEarFileName;
        protected String applicationBslEarFileName;
        protected String createDatasourceScriptFileName;
        protected String deleteDatasourceScriptFileName;
        protected String propertiesFileFileName;

        protected String weblogicDomainName;
        protected Integer weblogicPort;
        protected String weblogicUserName;
        protected String weblogicUserPassword;
        protected Integer tradeDbPoolInitCapacity;
        protected Integer tradeDbPoolMaxCapacity;
        protected String tradeDbJndi;
        protected String tradeDbDatabaseName;
        protected String weblogicTargetServer;
        protected String dbHostname;
        protected Integer dbTnsPort;
        protected String dbUser;
        protected String dbPassword;

        public Builder() {
            this.deploySourcesLocation(this.concatPaths(this.getDeployBase(), "wls_stocktrader"));

            this.weblogicDomainName("wl_server");
            this.weblogicPort(7001);
            this.weblogicUserName("weblogic");
            this.weblogicUserPassword("welcome1");
            this.tradeDbPoolInitCapacity(100);
            this.tradeDbPoolMaxCapacity(100);
            this.tradeDbJndi("Trade");
            this.tradeDbDatabaseName("tradedb");
            this.weblogicTargetServer("examplesServer");
            //
            this.dbTnsPort(1521);
            this.dbUser("TRADE");
            this.dbPassword("TRADE");

        }

        public StockTraderFlowContext build() {
            super.build();
            this.initPropertiesFileData();
            StockTraderFlowContext context = this.getInstance();
            Args.notNull(context.deployPackageUrl, "deployPackageUrl");
            Args.notNull(context.deploySourcesLocation, "deploySourcesLocation");
            Args.notNull(context.applicationEarFileName, "applicationEarFileName");
            Args.notNull(context.applicationBslEarFileName, "applicationBslEarFileName");
            Args.notNull(context.createDatasourceScriptFileName, "createDatasourceScriptFileName");
            Args.notNull(context.deleteDatasourceScriptFileName, "deleteDatasourceScriptFileName");
            Args.notNull(context.propertiesFileFileName, "propertiesFileFileName");

            Args.notNull(context.weblogicDomainName, "weblogicDomainName");
            Args.notNull(context.weblogicPort, "weblogicPort");
            Args.notNull(context.weblogicUserName, "weblogicUserName");
            Args.notNull(context.weblogicUserPassword, "weblogicUserPassword");
            Args.notNull(context.tradeDbPoolInitCapacity, "tradeDbPoolInitCapacity");
            Args.notNull(context.tradeDbPoolMaxCapacity, "tradeDbPoolMaxCapacity");
            Args.notNull(context.tradeDbJndi, "tradeDbJndi");
            Args.notNull(context.tradeDbDatabaseName, "tradeDbDatabaseName");
            Args.notNull(context.weblogicTargetServer, "weblogicTargetServer");
            Args.notNull(context.dbHostname, "dbHostname");
            Args.notNull(context.dbTnsPort, "dbTnsPort");
            Args.notNull(context.dbUser, "dbUser");
            Args.notNull(context.dbPassword, "dbPassword");
            return context;
        }

        protected void initPropertiesFileData() {
            this.propertiesFileOptions.put("DOMAIN_NAME", this.weblogicDomainName);
            this.propertiesFileOptions.put("ADMIN_URL", "t3://localhost:" + this.weblogicPort);
            this.propertiesFileOptions.put("ADMIN_USERNAME", this.weblogicUserName);
            this.propertiesFileOptions.put("ADMIN_PASSWORD", this.weblogicUserPassword);
            this.propertiesFileOptions.put("POOL_INIT_CAPACITY", String.valueOf(this.tradeDbPoolInitCapacity));
            this.propertiesFileOptions.put("POOL_MAX_CAPACITY", String.valueOf(this.tradeDbPoolMaxCapacity));
            this.propertiesFileOptions.put("DATASOURCE_NAME", this.tradeDbJndi);
            this.propertiesFileOptions.put("DATASOURCE_DATABASE_NAME", this.tradeDbDatabaseName);
            this.propertiesFileOptions.put("DATASOURCE_TARGET", this.weblogicTargetServer);
            this.propertiesFileOptions.put("DATASOURCE_FILENAME", this.tradeDbJndi + ".xml");
            this.propertiesFileOptions.put("DATASOURCE_JNDINAME", this.tradeDbJndi);
            this.propertiesFileOptions.put("DATASOURCE_URL", "jdbc:oracle:thin:@" + this.dbHostname + ":" + this.dbTnsPort + ":" + this.tradeDbDatabaseName);
            this.propertiesFileOptions.put("DATASOURCE_USERNAME", this.dbUser);
            this.propertiesFileOptions.put("DATASOURCE_PASSWORD", this.dbPassword);
        }

        protected StockTraderFlowContext getInstance() {
            return new StockTraderFlowContext(this);
        }

        public StockTraderFlowContext.Builder deployPackageUrl(URL deployPackageUrl) {
            this.deployPackageUrl = deployPackageUrl;
            return this.builder();
        }

        public StockTraderFlowContext.Builder deploySourcesLocation(String deploySourcesLocation) {
            this.deploySourcesLocation = deploySourcesLocation;
            return this.builder();
        }

        public StockTraderFlowContext.Builder applicationEarFileName(String applicationEarFileName) {
            this.applicationEarFileName = applicationEarFileName;
            return this.builder();
        }

        public StockTraderFlowContext.Builder applicationBslEarFileName(String applicationBslEarFileName) {
            this.applicationBslEarFileName = applicationBslEarFileName;
            return this.builder();
        }

        public StockTraderFlowContext.Builder createDatasourceScriptFileName(String createDatasourceScriptFileName) {
            this.createDatasourceScriptFileName = createDatasourceScriptFileName;
            return this.builder();
        }

        public StockTraderFlowContext.Builder deleteDatasourceScriptFileName(String deleteDatasourceScriptFileName) {
            this.deleteDatasourceScriptFileName = deleteDatasourceScriptFileName;
            return this.builder();
        }

        public StockTraderFlowContext.Builder propertiesFileFileName(String propertiesFileFileName) {
            this.propertiesFileFileName = propertiesFileFileName;
            return this.builder();
        }

        public StockTraderFlowContext.Builder weblogicDomainName(String weblogicDomainName) {
            this.weblogicDomainName = weblogicDomainName;
            return this.builder();
        }

        public StockTraderFlowContext.Builder weblogicPort(Integer weblogicPort) {
            this.weblogicPort = weblogicPort;
            return this.builder();
        }

        public StockTraderFlowContext.Builder weblogicUserName(String weblogicUserName) {
            this.weblogicUserName = weblogicUserName;
            return this.builder();
        }

        public StockTraderFlowContext.Builder weblogicUserPassword(String weblogicUserPassword) {
            this.weblogicUserPassword = weblogicUserPassword;
            return this.builder();
        }

        public StockTraderFlowContext.Builder tradeDbPoolInitCapacity(Integer tradeDbPoolInitCapacity) {
            this.tradeDbPoolInitCapacity = tradeDbPoolInitCapacity;
            return this.builder();
        }

        public StockTraderFlowContext.Builder tradeDbPoolMaxCapacity(Integer tradeDbPoolMaxCapacity) {
            this.tradeDbPoolMaxCapacity = tradeDbPoolMaxCapacity;
            return this.builder();
        }

        public StockTraderFlowContext.Builder tradeDbJndi(String tradeDbJndi) {
            this.tradeDbJndi = tradeDbJndi;
            return this.builder();
        }

        public StockTraderFlowContext.Builder tradeDbDatabaseName(String tradeDbDatabaseName) {
            this.tradeDbDatabaseName = tradeDbDatabaseName;
            return this.builder();
        }

        public StockTraderFlowContext.Builder weblogicTargetServer(String weblogicTargetServer) {
            this.weblogicTargetServer = weblogicTargetServer;
            return this.builder();
        }

        public StockTraderFlowContext.Builder dbHostname(String dbHostname) {
            this.dbHostname = dbHostname;
            return this.builder();
        }

        public StockTraderFlowContext.Builder dbTnsPort(Integer dbTnsPort) {
            this.dbTnsPort = dbTnsPort;
            return this.builder();
        }

        public StockTraderFlowContext.Builder dbUser(String dbUser) {
            this.dbUser = dbUser;
            return this.builder();
        }

        public StockTraderFlowContext.Builder dbPassword(String dbPassword) {
            this.dbPassword = dbPassword;
            return this.builder();
        }

        public StockTraderFlowContext.Builder weblogicInstallPath(String weblogicInstallPath) {
            super.weblogicInstallPath(weblogicInstallPath);
            return this.builder();
        }

        protected StockTraderFlowContext.Builder builder() {
            return this;
        }
    }
}