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

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.Set;

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.automation.action.responsefile.Triplet;
import org.jetbrains.annotations.NotNull;

/**
 * @author surma04
 */
public class DeployWebAgentFlowContext implements IFlowContext {

    private String installLocation;
    private String java32Path;
    private URL artifactoryUrl;
    private URL optionPackUrl;
    private String installerFileName;
    private String optionPackFileName;
    private String responseFile;
    private String optionPackResponseFile;
    private boolean optionPackRequired;
    private String tempForInstallers;
    private String hostName;

    /**
     *
     */
    public DeployWebAgentFlowContext(final Builder b) {
        this.installLocation = b.installLocation;
        this.artifactoryUrl = b.artifactoryUrl;
        this.installerFileName = b.installerFileName;
        this.optionPackFileName = b.optionPackFilename;
        this.responseFile = b.responseFile;
        this.optionPackUrl = b.optionPackUrl;
        this.optionPackResponseFile = b.optionPackResponseFile;
        this.java32Path = b.java32Path;
        this.optionPackRequired = b.optionPackRequired;
        this.tempForInstallers = b.tempForInstallers;
        this.hostName = b.hostName;

    }

    public String getInstallDir() {
        return this.installLocation;
    }

    public URL getArtifactUrl() {
        return this.artifactoryUrl;
    }

    public URL getOptionPackUrl() {
        return this.optionPackUrl;
    }

    public String getInstallerFileName() {
        return this.installerFileName;
    }

    public String getReponseFileName() {
        return this.responseFile;
    }

    public String getOptionPackReponseFileName() {
        return this.optionPackResponseFile;
    }

    public String getOptionPackInstaller() {
        return this.optionPackFileName;
    }

    /**
     * @return true if the Option Pack should be installed
     */
    public boolean isOptionPackRequired() {
        return this.optionPackRequired;
    }

    /**
     * @return the directory where the installers and response files are downloaded to
     */
    public String getTempDir() {
        return this.tempForInstallers;
    }

    public Set<Triplet> getResponseFileData() throws UnknownHostException {
        Set<Triplet> responses = new LinkedHashSet<>();

        responses.add(new Triplet("USER_INSTALL_DIR", "=", this.installLocation.toString().replace("\\", "\\\\")));
        responses.add(new Triplet("USER_SHORTCUTS", "=", "C:\\\\Users\\\\Administrator\\\\AppData\\Roaming\\\\Microsoft\\\\Windows\\\\Start Menu\\\\Programs"));
        responses.add(new Triplet("HOST_REGISTRATION_YES", "=", "1"));
        responses.add(new Triplet("ADMIN_REG_NAME", "=", "siteminder"));
        responses.add(new Triplet("ADMIN_REG_PASSWORD", "=", "ENC:k8Spqa5DB1+h92ptzzeQWg=="));
        responses.add(new Triplet("SHARED_SECRET_ROLLOVER_YES", "=", "1"));

        responses.add(new Triplet("TRUSTED_HOST_NAME", "=", this.getHostName()));
        responses.add(new Triplet("CONFIG_OBJ", "=", "cawebhost"));
        responses.add(new Triplet("IP_ADDRESS_STRING", "=", "127.0.0.1"));
        responses.add(new Triplet("FIPS_VALUE", "=", "COMPAT"));

        responses.add(new Triplet("SM_HOST_FILENAME", "=", "SmHost.conf"));
        responses.add(new Triplet("SM_HOST_DIR", "=",
            this.installLocation.toString().replace("\\", "\\\\") + "\\\\config"));
        /*
         * APACHE_SELECTED=
         * APACHE_WEBSERVER_ROOT=
         * APACHE_SPECIFIC_PATH_YES=
         * APACHE_VERSION=
         * DOMINO_SELECTED=
         * DOMINO_WEBSERVER_ROOT=
         * IPLANET_SELECTED=
         * IPLANET_WEBSERVER_ROOT=
         * IBM_HTTP_SERVER_ZOS=
         * IBM_HTTP_WEBSERVER_ZOS_CONF_PATH=
         */
        responses.add(new Triplet("APACHE_VENDOR_TYPE", "=", "HTTP_APACHE"));

        responses.add(new Triplet("WEB_SERVER_INFO", "=", "Apache,C:\\\\CA\\\\Apache\\\\conf,Apache 2.2.25,+EMPTYSTR+,apache,2.2.25,C:\\\\CA\\\\Apache,Windows,+EMPTYSTR+,1,1,0,0,0,1,HTTP Basic over SSL,cawebagentconf,0,undefined,ENC:6f1I5TLVEpuSBHpf4GrASg==,"));
        responses.add(new Triplet("ENABLE_WEBAGENT_RESULT", "=", "YES"));
        return responses;
    }

    /**
     * @return
     */
    public Set<Triplet> getOptionPackResponseFileData() {
        Set<Triplet> responses = new LinkedHashSet<>();
        responses.add(new Triplet("USER_INSTALL_DIR", "=", this.installLocation.toString().replace("\\", "\\\\")));
        responses.add(new Triplet("NETE_JDK_ROOT", "=", this.java32Path.toString().replace("\\", "\\\\")));
        responses.add(new Triplet("USER_REQUESTED_RESTART", "=", "NO"));

        return responses;
    }

    public static class Builder implements IBuilder<DeployWebAgentFlowContext> {

        public String hostName;
        private String tempForInstallers = "C:\\CA\\sourcesUnpacked\\install";
        private boolean optionPackRequired;
        private String java32Path;
        private String optionPackResponseFile = "ca-wa-opack-installer.properties";
        private String installLocation = "C:\\CA\\install\\webagent";
        private String responseFile = "ca-wa-installer.properties";
        private String installerFileName;
        private String optionPackFilename;
        private URL artifactoryUrl;
        private URL optionPackUrl;

        /**
         * @param required specify whether you need the Option Pack for Web Agent installed or not.
         *                 By default it is installed
         * @return this Builder instance
         */
        public Builder optionPackRequired(boolean required) {
            this.optionPackRequired = required;
            return this;
        }

        public Builder installLocation(@NotNull String installLocation) {
            this.installLocation = installLocation;
            return this;
        }

        public Builder installerFileName(@NotNull String installFile) {
            this.installerFileName = installFile;
            return this;
        }

        public Builder artifactUrl(@NotNull URL artifactUrl) {
            this.artifactoryUrl = artifactUrl;
            return this;
        }

        public Builder tempDirectoryForInstallers(@NotNull String tempDir) {
            this.tempForInstallers = tempDir;
            return this;
        }

        /**
         * @param javaLocation path to the 32-bit JDK
         * @return
         */
        public Builder javaLocation(@NotNull String javaLocation) {
            this.java32Path = javaLocation;
            return this;
        }

        /**
         * @param optionPackUrl
         */
        public Builder optionPackUrl(URL optionPackUrl) {
            this.optionPackUrl = optionPackUrl;
            return this;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.ca.apm.automation.action.flow.IBuilder#build()
         */
        @Override
        public DeployWebAgentFlowContext build() {
            return new DeployWebAgentFlowContext(this);
        }

        /**
         * @param optionPackInstallerFilename
         */
        public Builder optionPackFileName(@NotNull String optionPackInstallerFilename) {
            this.optionPackFilename = optionPackInstallerFilename;
            return this;
        }
    }

    /**
     * @throws UnknownHostException
     */
    public String getHostName() throws UnknownHostException {
        if (this.hostName == null) {
            this.hostName = InetAddress.getLocalHost().getHostName();
        }
        return this.hostName;
    }

}
