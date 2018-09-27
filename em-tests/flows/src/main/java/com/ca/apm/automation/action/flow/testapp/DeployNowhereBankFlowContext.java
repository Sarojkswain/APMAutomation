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

import static org.apache.http.util.Args.notNull;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.resolver.ITasResolver;

/**
 * Immutable context configuration of {@link DeployNowhereBankFlow} flow.
 * 
 * @author Korcak, Zdenek <korzd01@ca.com>
 */
public class DeployNowhereBankFlowContext implements IFlowContext {

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

    public DeployNowhereBankFlowContext(Builder builder, ITasResolver tasResolver) {
        notNull(builder.stagingBaseDir,
                "The staging directory for downloaded artifacts");
        notNull(builder.nowhereBankVersion,
                "The version of NowhereBank artifact");
        notNull(builder.installDir, "The installation directory");

        stagingDir = new File(builder.stagingBaseDir, "nowherebank");
        nowhereBankArtifactUrl = tasResolver.getArtifactUrl(builder.nowhereBankVersion.getArtifact());
        installDir = builder.installDir;
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

    public static class Builder implements IBuilder<DeployNowhereBankFlowContext> {

        @NotNull
        private File stagingBaseDir;
        @NotNull
        private File installDir;
        @NotNull
        private NowhereBankVersion nowhereBankVersion = NowhereBankVersion.v10;
        
        private final ITasResolver tasResolver;

        public Builder(ITasResolver tasResolver) {
            this.tasResolver = tasResolver;
        //    installDir(new File(getWinDeployBase(), "nowherebank"));
            installDir = new File("C:\\automation\\deployed\\nowherebank");
        }
        
        @Override
        public DeployNowhereBankFlowContext build() {
            return new DeployNowhereBankFlowContext(this, tasResolver);
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
    }
}
