package com.ca.apm.systemtest.fld.role;
/*
 * Copyright (c) 2014 CA. All rights reserved.
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

import java.net.URL;

import org.apache.http.util.Args;
import org.jetbrains.annotations.Nullable;

import com.ca.apm.automation.action.flow.utility.UniversalFlow;
import com.ca.apm.automation.action.flow.utility.UniversalFlowContext;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.built.HammondArtifact;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * Role to download and install only Hammond artifact. 
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class HammondInstallRole extends AbstractRole {
    public static final String ENV_HAMMOND_HOME = "home";
    public static final String DEFAULT_INSTALL_JAR_FILE_NAME = "hammond.jar"; 

    protected final UniversalFlowContext hammondInstallContext;

    protected HammondInstallRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        hammondInstallContext = builder.hammondInstallContext;
    }

    @Override
    public void deploy(IAutomationAgentClient client) {
        runFlow(client, UniversalFlow.class, hammondInstallContext);
    }

    /**
     * Linux builder.
     * 
     * @author Alexander Sinyushkin (sinal04@ca.com)
     *
     */
    public static class LinuxBuilder extends Builder {
        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }

        @Override
        protected LinuxBuilder builder() {
            return this;
        }

    }

    /**
     * Windows builder.
     * 
     * @author Alexander Sinyushkin (sinal04@ca.com)
     *
     */
    public static class Builder extends BuilderBase<Builder, HammondInstallRole> {
        protected ITasResolver tasResolver;
        protected String roleId;

        @Nullable
        protected UniversalFlowContext hammondInstallContext;

        protected String installPath;
        protected String hammondJarFileName = DEFAULT_INSTALL_JAR_FILE_NAME;
        protected ITasArtifact hammondArtifact;
        
        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public HammondInstallRole build() {
            getEnvProperties().add(ENV_HAMMOND_HOME, installPath.toString());

            String hammondJar = concatPaths(installPath, hammondJarFileName);

            if (hammondArtifact == null) {
                hammondArtifact = new HammondArtifact(tasResolver).createArtifact();
            }

            URL artifactUrl = tasResolver.getArtifactUrl(hammondArtifact);
            hammondInstallContext =
                new UniversalFlowContext.Builder().artifact(artifactUrl, hammondJar).build();

            return getInstance();
        }

        @Override
        protected HammondInstallRole getInstance() {
            return new HammondInstallRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        /**
         * Sets the home path where Hammond is installed. 
         * 
         * @param     path  Hammond root installation path
         * @return          this builder object
         */
        public Builder installDir(String path) {
            Args.notBlank(path, "Install dir");
            this.installPath = path;
            return builder();
        }

        /**
         * Provides a Hammond artifact to install. If this role is aimed to install a Hammond 
         * distribution and no custom Hammond artifact provided then {@link HammondArtifact} is used.
         * 
         * @param    hammondArtifact Hammond artifact coordinates
         * @return                   this builder object
         */
        public Builder hammondArtifact(ITasArtifact hammondArtifact) {
            Args.notNull(hammondArtifact, "Hammond artifact");
            this.hammondArtifact = hammondArtifact;
            return this;
        }

        /**
         * Sets a name for the installed Hammond distribution jar file. Full path to the jar is then obtained 
         * concatenating {@link HammondInstallRole.Builder#installDir(String) the install path} with this jar name.  
         * 
         * @param    hammondJarFileName  name for the Hammond jar file
         * @return                       this builder object
         */
        public Builder hammondJarFileName(String hammondJarFileName) {
            Args.notBlank(hammondJarFileName, "Hammond Jar file name");
            this.hammondJarFileName = hammondJarFileName;
            return this;
        }
        
    }
}
