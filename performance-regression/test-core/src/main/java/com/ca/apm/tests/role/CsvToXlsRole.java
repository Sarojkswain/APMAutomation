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
package com.ca.apm.tests.role;

import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.tests.artifact.CsvToXlsVersion;
import com.ca.apm.tests.flow.CsvToXlsFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.apache.http.util.Args;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.Map;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class CsvToXlsRole extends AbstractRole {

    public static final String RUN_CSV_TO_XLS = "runCsvToXls";

    private final CsvToXlsFlowContext runFlowContext;
    private final GenericFlowContext installContext;

    protected CsvToXlsRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        installContext = builder.installContext;
        runFlowContext = builder.runFlowContext;
    }

    @Override
    public void deploy(IAutomationAgentClient client) {
        runFlow(client, GenericFlow.class, installContext);
    }

    public CsvToXlsFlowContext getRunFlowContext() {
        return runFlowContext;
    }

    public static class Builder extends BuilderBase<Builder, CsvToXlsRole> {
        protected ITasResolver tasResolver;
        protected String roleId;

        @Nullable
        protected GenericFlowContext installContext;
        protected CsvToXlsFlowContext.Builder runFlowContextBuilder;
        protected CsvToXlsFlowContext runFlowContext;

        protected String installPath = getDeployBase() + getPathSeparator() + "csvToXls";

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;

            installPath(getWinDeployBase());

            this.initFlowContext();
        }

        protected void initFlowContext() {

            runFlowContextBuilder = new CsvToXlsFlowContext.Builder();

        }

        @Override
        public CsvToXlsRole build() {

            CsvToXlsVersion artifact = new CsvToXlsVersion(tasResolver);
            URL artifactUrl = tasResolver.getArtifactUrl(artifact.createArtifact());

            installContext =
                    new GenericFlowContext.Builder().artifactUrl(artifactUrl).destination(installPath)
                            .targetFilename(artifact.getFilename()).notArchive().build();

            runFlowContext =
                    runFlowContextBuilder.csvToXlsJarPath(installPath + "/" + artifact.getFilename())
                            .build();
            getEnvProperties().add(RUN_CSV_TO_XLS, runFlowContext);


            return getInstance();
        }

        @Override
        protected CsvToXlsRole getInstance() {
            return new CsvToXlsRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        public Builder installPath(String installPath) {
            Args.notBlank(installPath, "install dir");
            this.installPath = installPath;
            return builder();
        }

        public Builder shareDir(String shareDir) {
            this.runFlowContextBuilder.shareDir(shareDir);
            return this.builder();
        }

        public Builder templateFileName(String templateFileName) {
            this.runFlowContextBuilder.templateFileName(templateFileName);
            return this.builder();
        }

        public Builder outputFileName(String outputFileName) {
            this.runFlowContextBuilder.outputFileName(outputFileName);
            return this.builder();
        }

        public Builder sheetsMapping(Map<String, String> sheetsMapping) {
            this.runFlowContextBuilder.sheetsMapping(sheetsMapping);
            return this.builder();
        }
    }
}
