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

package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.IFlowContext;

import org.apache.http.util.Args;
import org.codehaus.plexus.util.FileUtils;

import java.net.URL;

/**
 * DeployJavaFlowContext
 *
 * Shows a sample template for flow context handling & creation.
 *
 * @author TAS (tas@ca.com)
 * @since 1.0
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
        installDir = builder.installDirectory;
        artifactUrl = builder.artifactUrl;
    }

    public String getInstallDir() {
        return installDir;
    }

    public URL getArtifactUrl() {
        return artifactUrl;
    }

    /**
     * Linux builder
     */
    public static class LinuxBuilder extends Builder {

        /**
         * Default home directory for Linux platform
         */
        public static final String JDK_HOME_DIR = "/usr/java/";

        public LinuxBuilder() {
            jdkHomeDir = JDK_HOME_DIR;
        }
    }

    /**
     * Default Windows builder
     */
    public static class Builder implements IBuilder<DeployJavaFlowContext> {

        /**
         * Default home directory for Windows platform
         */
        public static final String JDK_HOME_DIR = "C:\\Program Files\\Java\\";
        /**
         * Java home installDirectory - needs to be passed as String as we can't be sure on what
         * platform the resman is going to run
         */
        protected String jdkHomeDir;
        /**
         * Java binary artifact
         */
        private URL artifactUrl;
        /**
         * Final installation directory
         */
        private String installDirectory;

        public Builder() {
            jdkHomeDir = JDK_HOME_DIR;
        }

        /**
         * @param artifactUrl Java artifact's URL
         */
        public Builder artifactUrl(URL artifactUrl) {
            this.artifactUrl = artifactUrl;

            return this;
        }

        public Builder installDir(String installDir) {
            installDirectory = installDir;

            return this;
        }

        @Override
        public DeployJavaFlowContext build() {
            if (installDirectory == null) {
                initInstallDir();
            }
            DeployJavaFlowContext flowContext = new DeployJavaFlowContext(this);
            Args.notNull(flowContext.artifactUrl, "The URL of Java artifact");
            Args.notNull(flowContext.installDir, "Install directory");

            return flowContext;
        }

        /**
         * When no custom installation dir was specified the directory path is constructed using
         * java artifact' name and home directory
         */
        private void initInstallDir() {
            installDirectory = jdkHomeDir + getJavaFolder(artifactUrl);
        }

        /**
         * Extraction of artifact's name to be used in installation path
         *
         * @param artifactUrl URL of the java artifact
         * @return Constructed installation directory path
         */
        private String getJavaFolder(URL artifactUrl) {
            String basename = FileUtils.basename(artifactUrl.getFile());
            if (basename.isEmpty()) {
                return "";
            }
            return basename.substring(0, basename.length() - 1);
        }
    }

    @Override
    public String toString() {
        return "DeployJavaFlowContext{" + "installDir='" + installDir + '\'' + ", artifactUrl="
            + artifactUrl + '}';
    }
}
