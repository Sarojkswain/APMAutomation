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
package com.ca.apm.siteminder;

import java.net.URL;

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.IFlowContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author surma04
 */
public class ApacheFlowContext implements IFlowContext {

    private URL artifactUrl;
    private String tempDir;
    private String installer;
    private String installationDir;

    public ApacheFlowContext(final Builder b) {
        this.artifactUrl = b.artifactUrl;
        this.tempDir = b.tempDir;
        this.installer = b.installer;
        this.installationDir = b.installDir;
    }

    public String getTempDir() {
        return this.tempDir;
    }

    public String getInstallDir() {
        return this.installationDir;
    }

    public URL getArtifactUrl() {
        return this.artifactUrl;
    }

    public String getInstallerName() {
        return this.installer;
    }

    /**
     * @author surma04
     */
    public static class Builder implements IBuilder<ApacheFlowContext> {

        public String installer = "apache.msi";
        public String tempDir = "C:\\CA\\sourcesUnpacked\\install";
        public URL artifactUrl;
        private String installDir;

        /*
         * (non-Javadoc)
         * 
         * @see com.ca.apm.automation.action.flow.IBuilder#build()
         */
        @Override
        public ApacheFlowContext build() {
            return new ApacheFlowContext(this);
        }

        public Builder artifactUrl(@NotNull final URL url) {
            this.artifactUrl = url;
            return this;
        }

        public Builder tempDir(@NotNull final String temp) {
            this.tempDir = temp;
            return this;
        }

        public Builder installer(@NotNull final String installerFilename) {
            this.installer = installerFilename;
            return this;
        }

        /**
         * @param installDir
         */
        public Builder installDir(@NotNull final String installDir) {
            this.installDir = installDir;
            return this;
        }
    }

    /**
     * @return %apacheInstallDir%\conf\httpd.conf
     */
    public String getConfFile() {
        return this.installationDir + "\\conf\\httpd.conf";
    }

}
