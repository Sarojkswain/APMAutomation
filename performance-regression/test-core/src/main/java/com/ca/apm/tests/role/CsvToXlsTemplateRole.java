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

import com.ca.apm.automation.action.flow.utility.UniversalFlow;
import com.ca.apm.automation.action.flow.utility.UniversalFlowContext;
import com.ca.apm.tests.artifact.CsvToXlsTemplateVersion;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.apache.http.util.Args;
import org.jetbrains.annotations.Nullable;

import java.net.URL;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class CsvToXlsTemplateRole extends AbstractRole {

    private final UniversalFlowContext installContext;
    private final String installPath;

    private final String templateFileName;

    protected CsvToXlsTemplateRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        installContext = builder.installContext;
        installPath = builder.installPath;
        templateFileName = builder.templateFileName;
    }

    public String getTemplateFilePath() {
        return installPath + "/" + templateFileName;
    }

    @Override
    public void deploy(IAutomationAgentClient client) {
        runFlow(client, UniversalFlow.class, installContext);
    }

    public static class Builder extends BuilderBase<Builder, CsvToXlsTemplateRole> {

        private static final CsvToXlsTemplateVersion DEFAULT_ARTIFACT;
        protected ITasResolver tasResolver;
        protected String roleId;

        protected CsvToXlsTemplateVersion version;

        @Nullable
        protected UniversalFlowContext installContext;

        protected String installPath = getDeployBase() + "csvToXls";
        protected String templateFileName;

        static {
            DEFAULT_ARTIFACT = CsvToXlsTemplateVersion.AGENT_VER_10_2;
        }

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;

            this.version(DEFAULT_ARTIFACT);
        }


        @Override
        public CsvToXlsTemplateRole build() {
            assert this.version != null;

            // Non-standard URL is resolved by the Artifact itself
            URL artifactUrl =
                    this.version.getArtifactUrl(this.tasResolver.getRegionalArtifactory());

            installContext =
                    new UniversalFlowContext.Builder().archive(artifactUrl, installPath)
                            .build();

            return getInstance();
        }

        @Override
        protected CsvToXlsTemplateRole getInstance() {
            return new CsvToXlsTemplateRole(this);
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

        public Builder version(CsvToXlsTemplateVersion version) {
            this.version = version;
            this.templateFileName = version.getXlsFileName();
            return this.builder();
        }
    }
}
