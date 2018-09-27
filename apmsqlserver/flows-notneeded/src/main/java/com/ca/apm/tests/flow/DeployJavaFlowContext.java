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

package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.automation.action.utils.TasFileUtils;
import com.ca.tas.builder.BuilderBase;

import org.apache.http.util.Args;

import java.net.URL;

/**
 * DeployJavaFlowContext
 *
 * @author Jan Pojer (pojja01@ca.com)
 */
public class DeployJavaFlowContext implements IFlowContext {

    /**
     * The location to the install folder.
     */
    private final String installDir;
    /**
     * The URL location of artifact representing distribution of Apache Tomcat web server
     */
    private final URL artifactUrl;

    private DeployJavaFlowContext(Builder builder) {
        installDir = builder.dir;
        artifactUrl = builder.artifactUrl;
    }

    public String getInstallDir() {
        return installDir;
    }

    public URL getArtifactUrl() {
        return artifactUrl;
    }

    public static class LinuxBuilder extends Builder {

        public LinuxBuilder() {
            jdkHomeDir = getLinuxJavaBase();
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }

    public static class Builder extends BuilderBase<Builder, DeployJavaFlowContext> {

        protected String jdkHomeDir;
        private URL artifactUrl;
        private String dir;

        public Builder() {
            jdkHomeDir = getWinJavaBase();
        }

        @Override
        public DeployJavaFlowContext build() {
            if (dir == null) {
                dir = jdkHomeDir + getJavaFolder(artifactUrl);
            }
            DeployJavaFlowContext flowContext = getInstance();
            Args.notNull(flowContext.artifactUrl, "The URL of Java artifact");

            return flowContext;
        }

        private String getJavaFolder(URL artifactUrl) {
            return TasFileUtils.getBasename(artifactUrl);
        }

        @Override
        protected DeployJavaFlowContext getInstance() {
            return new DeployJavaFlowContext(this);
        }

        public Builder artifactUrl(URL artifactUrl) {
            this.artifactUrl = artifactUrl;

            return builder();
        }

        public Builder dir(String installDir) {
            dir = installDir;

            return builder();
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }

    @Override
    public String toString() {
        return "DeployJavaFlowContext{" +
               "installDir='" + installDir + '\'' +
               ", artifactUrl=" + artifactUrl +
               '}';
    }
}
