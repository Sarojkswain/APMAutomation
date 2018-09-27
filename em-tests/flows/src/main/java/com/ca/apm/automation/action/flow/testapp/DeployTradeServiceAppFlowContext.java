/*
 * Copyright (c) 2014 CA.  All rights reserved.
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

/**
 * Immutable context configuration of {@link DeployTradeServiceAppFlow} flow.
 * 
 * @author Pospichal, Pavel <pospa02@ca.com>
 */
public class DeployTradeServiceAppFlowContext implements IFlowContext {

    /**
     * The location to the folder with artifacts fetched from external environment.
     */
    private final File stagingDir;
    /**
     * The URL location of artifact representing distribution of Trade Service application
     */
    private final URL tradeServiceAppArtifactURL;
    /**
     * The location of folder where the Trade Service application should be installed
     */
    private final File installDir;

    public DeployTradeServiceAppFlowContext(Builder builder) {
        stagingDir = builder.stagingDir;
        tradeServiceAppArtifactURL = builder.tradeServiceAppArtifactUrl;
        installDir = builder.installDir;
    }

    public File getStagingDir() {
        return new File(FilenameUtils.separatorsToSystem(stagingDir.getPath()));
    }

    public URL getTradeServiceAppArtifactURL() {
        return tradeServiceAppArtifactURL;
    }

    public File getInstallDir() {
        return new File(FilenameUtils.separatorsToSystem(installDir.getPath()));
    }

    public static class Builder implements IBuilder<DeployTradeServiceAppFlowContext> {

        @NotNull
        private File stagingDir;
        @NotNull
        private File installDir;
        @NotNull
        private URL tradeServiceAppArtifactUrl;

        @Override
        public DeployTradeServiceAppFlowContext build() {
            DeployTradeServiceAppFlowContext deployTradeServiceAppFlowContext =
                    new DeployTradeServiceAppFlowContext(this);
            notNull(deployTradeServiceAppFlowContext.installDir, "The installation directory");
            notNull(deployTradeServiceAppFlowContext.stagingDir,
                    "The staging directory for downloaded artifacts");
            notNull(deployTradeServiceAppFlowContext.tradeServiceAppArtifactURL,
                    "The URL of Trade Service App artifact");

            return deployTradeServiceAppFlowContext;
        }

        public Builder stagingDir(@NotNull File stagingDir) {
            this.stagingDir = stagingDir;
            return this;
        }

        public Builder tradeServiceAppArtifactUrl(@NotNull URL tradeServiceAppArtifactURL) {
            this.tradeServiceAppArtifactUrl = tradeServiceAppArtifactURL;
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
