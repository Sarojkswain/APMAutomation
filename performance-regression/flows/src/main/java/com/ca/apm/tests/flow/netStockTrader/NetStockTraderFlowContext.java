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
package com.ca.apm.tests.flow.netStockTrader;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.ExtendedBuilderBase;
import org.apache.http.util.Args;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Flow Context for installing NET StockTrader WebApp into IIS
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class NetStockTraderFlowContext implements IFlowContext {

    private final Map<String, String> configFileOptions;

    private final URL deployPackageUrl;
    private final String deploySourcesLocation;

    private final String unpackDirName;
    private final String dbHostname;

    protected NetStockTraderFlowContext(NetStockTraderFlowContext.Builder builder) {
        this.configFileOptions = builder.configFileOptions;
        this.deployPackageUrl = builder.deployPackageUrl;
        this.deploySourcesLocation = builder.deploySourcesLocation;
        this.unpackDirName = builder.unpackDirName;
        this.dbHostname = builder.dbHostname;
    }

    public Map<String, String> getConfigFileOptions() {
        return configFileOptions;
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

    public String getDbHostname() {
        return dbHostname;
    }

    public static class Builder extends ExtendedBuilderBase<NetStockTraderFlowContext.Builder, NetStockTraderFlowContext> {

        protected Map<String, String> configFileOptions = new HashMap<>();

        protected URL deployPackageUrl;
        protected String deploySourcesLocation;

        protected String unpackDirName;
        protected String dbHostname;

        public Builder() {
            this.deploySourcesLocation(this.concatPaths(this.getDeployBase(), "net_stocktrader"));

            this.dbHostname("localhost");

        }

        public NetStockTraderFlowContext build() {
            this.initPropertiesFileData();
            NetStockTraderFlowContext context = this.getInstance();
            Args.notNull(context.deployPackageUrl, "deployPackageUrl");
            Args.notNull(context.deploySourcesLocation, "deploySourcesLocation");
            Args.notNull(context.unpackDirName, "unpackDirName");
            Args.notNull(context.dbHostname, "dbHostname");

            return context;
        }

        protected void initPropertiesFileData() {
            this.configFileOptions.put("SQL_DB_HOST", this.dbHostname);
        }

        protected NetStockTraderFlowContext getInstance() {
            return new NetStockTraderFlowContext(this);
        }

        public NetStockTraderFlowContext.Builder deployPackageUrl(URL deployPackageUrl) {
            this.deployPackageUrl = deployPackageUrl;
            return this.builder();
        }

        public NetStockTraderFlowContext.Builder deploySourcesLocation(String deploySourcesLocation) {
            this.deploySourcesLocation = deploySourcesLocation;
            return this.builder();
        }

        public NetStockTraderFlowContext.Builder unpackDirName(String unpackDirName) {
            this.unpackDirName = unpackDirName;
            return this.builder();
        }

        public NetStockTraderFlowContext.Builder dbHostname(String dbHostname) {
            this.dbHostname = dbHostname;
            return this.builder();
        }

        protected NetStockTraderFlowContext.Builder builder() {
            return this;
        }
    }
}