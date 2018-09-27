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
package com.ca.apm.tests.flow.msSqlDb;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.ExtendedBuilderBase;
import org.apache.http.util.Args;

import java.net.URL;

/**
 * Flow Context for configuring TradeDb in MSSQL DB
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class MsSqlTradeDbScriptFlowContext implements IFlowContext {

    private final URL deployPackageUrl;
    private final String deploySourcesLocation;
    private final String unpackDirName;

    private final String configDbFileName;
    private final String createTablesFileName;
    private final String dbDeploySourcesLocation;
    private boolean recreateTablesOnly;

    protected MsSqlTradeDbScriptFlowContext(MsSqlTradeDbScriptFlowContext.Builder builder) {
        this.deployPackageUrl = builder.deployPackageUrl;
        this.deploySourcesLocation = builder.deploySourcesLocation;
        this.unpackDirName = builder.unpackDirName;
        this.configDbFileName = builder.configDbFileName;
        this.createTablesFileName = builder.createTablesFileName;
        this.recreateTablesOnly = builder.recreateTablesOnly;
        this.dbDeploySourcesLocation = builder.dbDeploySourcesLocation;
    }

    public URL getDeployPackageUrl() {
        return deployPackageUrl;
    }

    public String getDeploySourcesLocation() {
        return deploySourcesLocation;
    }

    public String getUnpackDirName() {
        return unpackDirName;
    }

    public String getConfigDbFileName() {
        return configDbFileName;
    }

    public String getCreateTablesFileName() {
        return createTablesFileName;
    }

    public boolean isRecreateTablesOnly() {
        return recreateTablesOnly;
    }

    public void setRecreateTablesOnly(boolean recreateTablesOnly) {
        this.recreateTablesOnly = recreateTablesOnly;
    }

    public String getDbDeploySourcesLocation() {
        return dbDeploySourcesLocation;
    }

    public static class Builder extends ExtendedBuilderBase<MsSqlTradeDbScriptFlowContext.Builder, MsSqlTradeDbScriptFlowContext> {
        protected URL deployPackageUrl;
        protected String deploySourcesLocation;
        protected String unpackDirName;

        protected String configDbFileName;
        protected String createTablesFileName;
        protected boolean recreateTablesOnly;

        protected String dbDeploySourcesLocation;

        public Builder() {
            this.deploySourcesLocation(this.concatPaths(this.getDeployBase(), "tradedb_scripts"));
            this.recreateTablesOnly(false);
        }

        public MsSqlTradeDbScriptFlowContext build() {
            MsSqlTradeDbScriptFlowContext context = this.getInstance();
            Args.notNull(context.deployPackageUrl, "deployPackageUrl");
            Args.notNull(context.deploySourcesLocation, "deploySourcesLocation");
            Args.notNull(context.unpackDirName, "unpackDirName");
            Args.notNull(context.configDbFileName, "configDbFileName");
            Args.notNull(context.createTablesFileName, "createTablesFileName");
            Args.notNull(context.recreateTablesOnly, "recreateTablesOnly");
            Args.notNull(context.dbDeploySourcesLocation, "dbDeploySourcesLocation");
            return context;
        }

        protected MsSqlTradeDbScriptFlowContext getInstance() {
            return new MsSqlTradeDbScriptFlowContext(this);
        }

        public Builder deployPackageUrl(URL deployPackageUrl) {
            this.deployPackageUrl = deployPackageUrl;
            return this.builder();
        }

        public Builder deploySourcesLocation(String deploySourcesLocation) {
            this.deploySourcesLocation = deploySourcesLocation;
            return this.builder();
        }

        public Builder unpackDirName(String unpackDirName) {
            this.unpackDirName = unpackDirName;
            return this.builder();
        }

        public Builder configDbFileName(String configDbFileName) {
            this.configDbFileName = configDbFileName;
            return this.builder();
        }

        public Builder createTablesFileName(String createTablesFileName) {
            this.createTablesFileName = createTablesFileName;
            return this.builder();
        }

        public Builder recreateTablesOnly(boolean recreateTablesOnly) {
            this.recreateTablesOnly = recreateTablesOnly;
            return this.builder();
        }

        public Builder dbDeploySourcesLocation(String dbDeploySourcesLocation) {
            this.dbDeploySourcesLocation = dbDeploySourcesLocation;
            return this.builder();
        }

        protected MsSqlTradeDbScriptFlowContext.Builder builder() {
            return this;
        }
    }
}