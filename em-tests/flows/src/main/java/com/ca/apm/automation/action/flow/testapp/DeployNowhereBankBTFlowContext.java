/*
 * Copyright (c) 2015 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.automation.action.flow.testapp;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.resolver.ITasResolver;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import static org.apache.http.util.Args.notNull;

/**
 * Immutable context configuration of {@link DeployNowhereBankBTFlow} flow.
 *
 *  Note: Developed primarily for linux. Will need some tweaking to work on Windows
 */
public class DeployNowhereBankBTFlowContext implements IFlowContext {

    /**
     * The location to the folder with artifacts fetched from external environment.
     */
    private final File stagingDir;
    /**
     * The URL location of artifact representing distribution of NowhereBank
     */
    private final URL nowhereBankArtifactUrl;
    /**
     * The location of folder where the NowhereBank application should be installed
     */
    private final File installDir;
    private final Map<String, String> files;
    private final boolean autoStart;
    private final String javaBinDir;

    public DeployNowhereBankBTFlowContext(Builder builder, ITasResolver tasResolver) {
        notNull(builder.stagingBaseDir,
                "The staging directory for downloaded artifacts");
        notNull(builder.nowhereBankVersion,
                "The version of NowhereBank artifact");
        notNull(builder.installDir, "The installation directory");

        stagingDir = new File(builder.stagingBaseDir, "nowherebank");
        nowhereBankArtifactUrl = tasResolver.getArtifactUrl(builder.nowhereBankVersion.getArtifact());
        installDir = builder.installDir;
        files = builder.files;
        autoStart = builder.autoStart;
        javaBinDir = builder.javaBinDir;
    }

    public File getStagingDir() {
        return new File(FilenameUtils.separatorsToSystem(stagingDir.getPath()));
    }

    public URL getNowhereBankArtifactURL() {
        return nowhereBankArtifactUrl;
    }

    public File getInstallDir() {
        return new File(FilenameUtils.separatorsToSystem(installDir.getPath()));
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public Map<String, String> getFiles() {
        return files;
    }

    public String getJavaBinDir() {
        return javaBinDir;
    }

    public static class LinuxBuilder extends Builder {

        public LinuxBuilder(ITasResolver tasResolver) {
            super(tasResolver);
            installDir = new File(getLinuxDeployBase() + LINUX_SEPARATOR +  "nowherebank");
            javaBinDir = BuilderBase.LINUX_JDK_1_8 + getPathSeparator()  + "bin" + getPathSeparator();
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }
    }

    public static class Builder extends BuilderBase<Builder, DeployNowhereBankBTFlowContext> {

        @NotNull
        private File stagingBaseDir;
        @NotNull
        protected File installDir;
        @NotNull
        private NowhereBankVersion nowhereBankVersion = NowhereBankVersion.v103;
        
        private final ITasResolver tasResolver;
        private Map<String, String> files;
        private boolean autoStart = true;
        protected String javaBinDir;

        public Builder(ITasResolver tasResolver) {
            this.tasResolver = tasResolver;
            installDir = new File(getWinDeployBase() + WIN_SEPARATOR + "nowherebank");
            javaBinDir = BuilderBase.WIN_JDK_1_8 + getPathSeparator() + "bin" + getPathSeparator();
        }
        
        @Override
        public DeployNowhereBankBTFlowContext build() {
            return new DeployNowhereBankBTFlowContext(this, tasResolver);
        }

        public Builder stagingBaseDir(@NotNull File stagingBaseDir) {
            this.stagingBaseDir = stagingBaseDir;
            return this;
        }

        public Builder nowhereBankVersion(@NotNull NowhereBankVersion nowhereBankVersion) {
            this.nowhereBankVersion = nowhereBankVersion;
            return this;
        }

        public Builder installDir(@NotNull String installDir) {
            this.installDir = new File(installDir);
            return this;
        }

        public Builder installDir(@NotNull File installDir) {
            this.installDir = installDir;
            return this;
        }

        @Override
        protected DeployNowhereBankBTFlowContext getInstance() {
            return build();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        public Builder files(Map<String, String> files) {
            this.files = files;
            return this;
        }

        public Builder noStart() {
            this.autoStart = false;
            return this;
        }

    }
}
