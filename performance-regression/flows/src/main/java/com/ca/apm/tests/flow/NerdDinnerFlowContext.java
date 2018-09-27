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
 * Flow Context for installing NET StockTrader WebApp into IIS
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class NerdDinnerFlowContext implements IFlowContext {

    private final URL deployPackageUrl;
    private final String deploySourcesLocation;

    private final String appName;
    private final Integer appPort;
    private final String dbHostname;
    private final String dbAdminUserName;
    private final String dbAdminUserPassword;

    protected NerdDinnerFlowContext(Builder builder) {
        this.deployPackageUrl = builder.deployPackageUrl;
        this.deploySourcesLocation = builder.deploySourcesLocation;
        this.appName = builder.appName;
        this.appPort = builder.appPort;
        this.dbHostname = builder.dbHostname;
        this.dbAdminUserName = builder.dbAdminUserName;
        this.dbAdminUserPassword = builder.dbAdminUserPassword;
    }

    public URL getDeployPackageUrl() {
        return deployPackageUrl;
    }

    public String getDeploySourcesLocation() {
        return deploySourcesLocation;
    }

    public String getAppName() {
        return appName;
    }

    public Integer getAppPort() {
        return appPort;
    }

    public String getDbHostname() {
        return dbHostname;
    }

    public String getDbAdminUserName() {
        return dbAdminUserName;
    }

    public String getDbAdminUserPassword() {
        return dbAdminUserPassword;
    }

    public static class Builder extends ExtendedBuilderBase<Builder, NerdDinnerFlowContext> {

        protected URL deployPackageUrl;
        protected String deploySourcesLocation;

        protected String appName;
        protected Integer appPort;
        protected String dbHostname;
        protected String dbAdminUserName;
        protected String dbAdminUserPassword;

        public Builder() {
            this.deploySourcesLocation(this.concatPaths(this.getDeployBase(), "nerd_dinner"));

            this.appName("NerdDinnerMVC");
            this.appPort(9090);
            this.dbHostname("localhost");
            this.dbAdminUserName("sa");

        }

        public NerdDinnerFlowContext build() {
            NerdDinnerFlowContext context = this.getInstance();
            Args.notNull(context.deployPackageUrl, "deployPackageUrl");
            Args.notNull(context.deploySourcesLocation, "deploySourcesLocation");
            Args.notNull(context.appName, "appName");
            Args.notNull(context.appPort, "appPort");
            Args.notNull(context.dbHostname, "dbHostname");
            Args.notNull(context.dbAdminUserName, "dbAdminUserName");
            Args.notNull(context.dbAdminUserPassword, "dbAdminUserPassword");

            return context;
        }

        protected NerdDinnerFlowContext getInstance() {
            return new NerdDinnerFlowContext(this);
        }

        public Builder deployPackageUrl(URL deployPackageUrl) {
            this.deployPackageUrl = deployPackageUrl;
            return this.builder();
        }

        public Builder deploySourcesLocation(String deploySourcesLocation) {
            this.deploySourcesLocation = deploySourcesLocation;
            return this.builder();
        }

        public Builder appName(String appName) {
            this.appName = appName;
            return this.builder();
        }

        public Builder appPort(Integer appPort) {
            this.appPort = appPort;
            return this.builder();
        }

        public Builder dbHostname(String dbHostname) {
            this.dbHostname = dbHostname;
            return this.builder();
        }

        public Builder dbAdminUserName(String dbAdminUserName) {
            this.dbAdminUserName = dbAdminUserName;
            return this.builder();
        }

        public Builder dbAdminUserPassword(String dbAdminUserPassword) {
            this.dbAdminUserPassword = dbAdminUserPassword;
            return this.builder();
        }

        protected Builder builder() {
            return this;
        }
    }
}