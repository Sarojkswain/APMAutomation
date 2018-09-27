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

/**
 * Flow Context for installing Konakart WebApp into Tomcat
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class KonakartFlowContext implements IFlowContext {

    private final URL deployPackageUrl;
    private final String deploySourcesLocation;

    private final String tomcatInstallPath;
    private final String setupFileName;

    private final String databaseType;
    private final String databaseDriver;
    private final String dbHostname;
    private final Integer dbTnsPort;
    private final String tradeDbDatabaseName;
    private final String databaseUsername;
    private final String databasePassword;
    private final boolean loadDB;
    private final String javaJRE;
    private final Integer portNumber;
    private final String installationDir;

    protected KonakartFlowContext(KonakartFlowContext.Builder builder) {
        this.deployPackageUrl = builder.deployPackageUrl;
        this.deploySourcesLocation = builder.deploySourcesLocation;
        this.tomcatInstallPath = builder.tomcatInstallPath;
        this.setupFileName = builder.setupFileName;

        this.databaseType = builder.databaseType;
        this.databaseDriver = builder.databaseDriver;
        this.dbHostname = builder.dbHostname;
        this.dbTnsPort = builder.dbTnsPort;
        this.tradeDbDatabaseName = builder.tradeDbDatabaseName;
        this.databaseUsername = builder.databaseUsername;
        this.databasePassword = builder.databasePassword;
        this.loadDB = builder.loadDB;
        this.javaJRE = builder.javaJRE;
        this.portNumber = builder.portNumber;
        this.installationDir = builder.installationDir;
    }

    public URL getDeployPackageUrl() {
        return deployPackageUrl;
    }

    public String getDeploySourcesLocation() {
        return deploySourcesLocation;
    }

    public String getTomcatInstallPath() {
        return tomcatInstallPath;
    }

    public String getSetupFileName() {
        return setupFileName;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public String getDatabaseDriver() {
        return databaseDriver;
    }

    public String getDbHostname() {
        return dbHostname;
    }

    public Integer getDbTnsPort() {
        return dbTnsPort;
    }

    public String getTradeDbDatabaseName() {
        return tradeDbDatabaseName;
    }

    public String getDatabaseUsername() {
        return databaseUsername;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public boolean isLoadDB() {
        return loadDB;
    }

    public String getJavaJRE() {
        return javaJRE;
    }

    public Integer getPortNumber() {
        return portNumber;
    }

    public String getInstallationDir() {
        return installationDir;
    }

    public static class Builder extends ExtendedBuilderBase<KonakartFlowContext.Builder, KonakartFlowContext> {

        protected URL deployPackageUrl;
        protected String deploySourcesLocation;

        protected String tomcatInstallPath;
        protected String setupFileName;

        protected String databaseType;
        protected String databaseDriver;
        protected String dbHostname;
        protected Integer dbTnsPort;
        protected String tradeDbDatabaseName;
        protected String databaseUsername;
        protected String databasePassword;
        protected boolean loadDB;
        protected String javaJRE;
        protected Integer portNumber;
        protected String installationDir;

        public Builder() {
            this.deploySourcesLocation(this.concatPaths(this.getDeployBase(), "konakart"));
            this.installationDir(this.concatPaths(this.getDeployBase(), "konakartInstallation"));

            this.databaseType("oracle");
            this.databaseDriver("oracle.jdbc.OracleDriver");
            this.dbTnsPort(1521);
            this.tradeDbDatabaseName("tradedb");
            this.databaseUsername("TRADE");
            this.databasePassword("TRADE");
            this.loadDB(true);
            this.portNumber(8080);

        }

        public KonakartFlowContext build() {
            KonakartFlowContext context = this.getInstance();
            Args.notNull(context.deployPackageUrl, "deployPackageUrl");
            Args.notNull(context.deploySourcesLocation, "deploySourcesLocation");
            Args.notNull(context.tomcatInstallPath, "tomcatInstallPath");
            Args.notNull(context.setupFileName, "setupFileName");

            Args.notNull(context.databaseType, "databaseType");
            Args.notNull(context.databaseDriver, "databaseDriver");
            Args.notNull(context.dbHostname, "dbHostname");
            Args.notNull(context.dbTnsPort, "dbTnsPort");
            Args.notNull(context.tradeDbDatabaseName, "tradeDbDatabaseName");
            Args.notNull(context.databaseUsername, "databaseUsername");
            Args.notNull(context.databasePassword, "databasePassword");
            Args.notNull(context.loadDB, "loadDB");
            Args.notNull(context.javaJRE, "javaJRE");
            Args.notNull(context.portNumber, "portNumber");
            Args.notNull(context.installationDir, "installationDir");
            return context;
        }

        protected KonakartFlowContext getInstance() {
            return new KonakartFlowContext(this);
        }

        public KonakartFlowContext.Builder deployPackageUrl(URL deployPackageUrl) {
            this.deployPackageUrl = deployPackageUrl;
            return this.builder();
        }

        public KonakartFlowContext.Builder deploySourcesLocation(String deploySourcesLocation) {
            this.deploySourcesLocation = deploySourcesLocation;
            return this.builder();
        }

        public KonakartFlowContext.Builder tomcatInstallPath(String tomcatInstallPath) {
            this.tomcatInstallPath = tomcatInstallPath;
            return this.builder();
        }

        public KonakartFlowContext.Builder setupFileName(String setupFileName) {
            this.setupFileName = setupFileName;
            return this.builder();
        }

        public KonakartFlowContext.Builder databaseType(String databaseType) {
            this.databaseType = databaseType;
            return this.builder();
        }

        public KonakartFlowContext.Builder databaseDriver(String databaseDriver) {
            this.databaseDriver = databaseDriver;
            return this.builder();
        }

        public KonakartFlowContext.Builder dbHostname(String dbHostname) {
            this.dbHostname = dbHostname;
            return this.builder();
        }

        public KonakartFlowContext.Builder dbTnsPort(int dbTnsPort) {
            this.dbTnsPort = dbTnsPort;
            return this.builder();
        }

        public KonakartFlowContext.Builder tradeDbDatabaseName(String tradeDbDatabaseName) {
            this.tradeDbDatabaseName = tradeDbDatabaseName;
            return this.builder();
        }

        public KonakartFlowContext.Builder databaseUsername(String databaseUsername) {
            this.databaseUsername = databaseUsername;
            return this.builder();
        }

        public KonakartFlowContext.Builder databasePassword(String databasePassword) {
            this.databasePassword = databasePassword;
            return this.builder();
        }

        public KonakartFlowContext.Builder loadDB(boolean loadDB) {
            this.loadDB = loadDB;
            return this.builder();
        }

        public KonakartFlowContext.Builder javaJRE(String javaJRE) {
            this.javaJRE = javaJRE;
            return this.builder();
        }

        public KonakartFlowContext.Builder portNumber(Integer portNumber) {
            this.portNumber = portNumber;
            return this.builder();
        }

        public KonakartFlowContext.Builder installationDir(String installationDir) {
            this.installationDir = installationDir;
            return this.builder();
        }


        protected KonakartFlowContext.Builder builder() {
            return this;
        }
    }
}