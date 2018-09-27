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

/**
 * DeployMsSqlDbFlowContext
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class MsSqlDbUndeployFlowContext implements IFlowContext {

    private final String responseFileName;
    private final String unpackDirName;
    private final String installerFileName;
    private final String installSourcesLocation;
    private final String installPath;
    private final boolean deleteSources;


    protected MsSqlDbUndeployFlowContext(MsSqlDbUndeployFlowContext.Builder builder) {
        this.installSourcesLocation = builder.installSourcesPath;
        this.responseFileName = builder.responseFileName;
        this.unpackDirName = builder.unpackDirName;
        this.installerFileName = builder.installerFileName;
        this.installPath = builder.installPath;
        this.deleteSources = builder.deleteSources;
    }

    public String getInstallSourcesLocation() {
        return this.installSourcesLocation;
    }

    public String getResponseFileName() {
        return this.responseFileName;
    }

    public String getUnpackDirName() {
        return unpackDirName;
    }

    public String getInstallerFileName() {
        return installerFileName;
    }

    public String getInstallPath() {
        return installPath;
    }

    public boolean isDeleteSources() {
        return deleteSources;
    }

    public static class Builder extends ExtendedBuilderBase<MsSqlDbUndeployFlowContext.Builder, MsSqlDbUndeployFlowContext> {
        protected String installSourcesPath;
        protected String responseFileName;
        protected String unpackDirName;
        protected String installerFileName;
        protected String installPath;
        protected boolean deleteSources;

        public Builder() {
            this.installSourcesPath(this.concatPaths(this.getDeployBase(), "mssql_sources"));
        }

        public MsSqlDbUndeployFlowContext build() {
            MsSqlDbUndeployFlowContext context = this.getInstance();
            Args.notNull(context.installSourcesLocation, "installSourcesPath");
            Args.notNull(context.responseFileName, "responseFileName");
            Args.notNull(context.unpackDirName, "unpackDirName");
            Args.notNull(context.installerFileName, "installerFileName");
            Args.notNull(context.installPath, "installPath");
            return context;
        }

        protected MsSqlDbUndeployFlowContext getInstance() {
            return new MsSqlDbUndeployFlowContext(this);
        }


        public MsSqlDbUndeployFlowContext.Builder installSourcesPath(String installSourcesPath) {
            this.installSourcesPath = installSourcesPath;
            return this.builder();
        }

        public MsSqlDbUndeployFlowContext.Builder responseFileName(String responseFileName) {
            this.responseFileName = responseFileName;
            return this.builder();
        }

        public MsSqlDbUndeployFlowContext.Builder unpackDirName(String unpackDirName) {
            this.unpackDirName = unpackDirName;
            return this.builder();
        }

        public MsSqlDbUndeployFlowContext.Builder installerFileName(String installerFileName) {
            this.installerFileName = installerFileName;
            return this.builder();
        }

        public MsSqlDbUndeployFlowContext.Builder installPath(String installPath) {
            this.installPath = installPath;
            return this.builder();
        }

        public MsSqlDbUndeployFlowContext.Builder deleteSources(boolean deleteSources) {
            this.deleteSources = deleteSources;
            return this.builder();
        }

        protected MsSqlDbUndeployFlowContext.Builder builder() {
            return this;
        }
    }
}