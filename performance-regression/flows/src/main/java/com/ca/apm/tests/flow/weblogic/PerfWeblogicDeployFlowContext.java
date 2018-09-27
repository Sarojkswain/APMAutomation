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
package com.ca.apm.tests.flow.weblogic;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.ExtendedBuilderBase;
import org.apache.http.util.Args;

import java.net.URL;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class PerfWeblogicDeployFlowContext implements IFlowContext {

    private final String sourcesLocation;
    private final URL installerUrl;
    private final String installerFileName;
    private final String beaHome;
    private final String installDir;
    private final String customJvm;

    protected PerfWeblogicDeployFlowContext(Builder builder) {
        sourcesLocation = builder.sourcesLocation;
        installerUrl = builder.installerUrl;
        installerFileName = builder.installerFileName;
        beaHome = builder.beaHome;
        installDir = builder.installDir;
        customJvm = builder.customJvm;
    }

    public String getSourcesLocation() {
        return sourcesLocation;
    }

    public URL getInstallerUrl() {
        return installerUrl;
    }

    public String getInstallerFileName() {
        return installerFileName;
    }

    public String getBeaHome() {
        return beaHome;
    }

    public String getInstallDir() {
        return installDir;
    }

    public String getCustomJvm() {
        return customJvm;
    }

    public static class Builder extends ExtendedBuilderBase<PerfWeblogicDeployFlowContext.Builder, PerfWeblogicDeployFlowContext> {

        protected String sourcesLocation;
        protected URL installerUrl;
        protected String installerFileName;
        protected String beaHome;
        protected String installDir;
        protected String customJvm;

        public Builder() {
            this.sourcesLocation(this.concatPaths(this.getDeployBase(), "wls_sources"));
        }

        public PerfWeblogicDeployFlowContext build() {
            PerfWeblogicDeployFlowContext context = this.getInstance();

            Args.notNull(context.sourcesLocation, "sourcesLocation");
            Args.notNull(context.installerUrl, "installerUrl");
            Args.notNull(context.installerFileName, "installerFileName");
            Args.notNull(context.beaHome, "beaHome");
            Args.notNull(context.installDir, "installDir");

            return context;
        }

        protected PerfWeblogicDeployFlowContext getInstance() {
            return new PerfWeblogicDeployFlowContext(this);
        }

        public Builder sourcesLocation(String sourcesLocation) {
            this.sourcesLocation = sourcesLocation;
            return this.builder();
        }

        public Builder installerUrl(URL installerUrl) {
            this.installerUrl = installerUrl;
            return this.builder();
        }

        public Builder installerFileName(String installerFileName) {
            this.installerFileName = installerFileName;
            return this.builder();
        }

        public Builder beaHome(String beaHome) {
            this.beaHome = beaHome;
            return this.builder();
        }

        public Builder installDir(String installDir) {
            this.installDir = installDir;
            return this.builder();
        }

        public Builder customJvm(String customJvm) {
            this.customJvm = customJvm;
            return this.builder();
        }

        protected Builder builder() {
            return this;
        }
    }
}
