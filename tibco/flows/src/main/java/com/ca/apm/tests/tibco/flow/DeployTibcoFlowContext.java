/*
 * Copyright (c) 2015 CA. All rights reserved.
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

package com.ca.apm.tests.tibco.flow;

import java.io.File;
import java.net.URL;
import java.util.Set;

import com.ca.tas.builder.BuilderBase;
import com.ca.apm.automation.action.flow.IFlowContext;

import com.ca.apm.automation.action.responsefile.Triplet;
import com.ca.apm.tests.tibco.artifact.TibcoSoftwareComponentVersions;

/**
 * DeployEmptyFlowContext
 *
 * The deployment context to hold all the parameters.
 *
 * Vashistha Singh (sinva01.ca.com)
 */
public class DeployTibcoFlowContext implements IFlowContext {

    private File installLocation;

    private URL installerSourceURL;

    private String installDir;

    private String installerUnpackDir;

    private Set<Triplet> responseFileData;

    private String installerLogFile;

    private TibcoSoftwareComponentVersions version;

    private String responseFileName;

    private String roleId;

    private String domainName;

    private String domainUser;

    private String domainPassword;

    private boolean autoStartBWService;

    public File getInstallLocation() {
        return installLocation;
    }

    public boolean shouldAutoStartBWService() {
        return autoStartBWService;
    }

    public String getDomainName() {
        return domainName;
    }

    public String getDomainUser() {
        return domainUser;
    }

    public String getDomainPassword() {
        return domainPassword;
    }

    public URL getInstallerSourceURL() {
        return installerSourceURL;
    }

    public String getInstallDir() {
        return installDir;
    }

    public String getInstallerUnpackDir() {
        return installerUnpackDir;
    }

    public Set<Triplet> getResponseFileData() {
        return responseFileData;
    }

    public TibcoSoftwareComponentVersions getVersion() {
        return version;
    }

    public String getResponseFileName() {
        return responseFileName;
    }

    public String getInstallerLogFile() {
        return installerLogFile;
    }

    public String getRoleId() {
        return roleId;
    }

    @SuppressWarnings("unused")
    protected DeployTibcoFlowContext(Builder builder) {
        roleId = builder.roleId;
        installerSourceURL = builder.artifactURL;
        installDir = builder.installDir;
        responseFileData = builder.responseFileData;
        version = builder.version;
        domainName = builder.domainName;
        domainUser = builder.domainUser;
        domainPassword = builder.domainPassword;
        autoStartBWService = builder.autoStartBWService;
        installerUnpackDir = builder.installerUnpackDir + builder.getPathSeparator() + roleId;
        responseFileName =
            installerUnpackDir + builder.getPathSeparator() + builder.responseFileName;
        installerLogFile =
            installerUnpackDir + builder.getPathSeparator() + "logs" + builder.getPathSeparator()
                + builder.installerLogFile;
    }

    public static class LinuxBuilder extends Builder {

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }

        @Override
        protected String getDeployBase() {
            return getLinuxDeployBase();
        }
    }

    public static class Builder extends BuilderBase<Builder, DeployTibcoFlowContext> {

        public boolean autoStartBWService = false;

        public String domainName = "BAT";

        public String installerLogFile;

        public String installerUnpackDir;

        private URL artifactURL;

        private String installDir;

        private Set<Triplet> responseFileData;

        private TibcoSoftwareComponentVersions version;

        private String responseFileName;

        private String roleId;

        private String domainUser = "admin";

        private String domainPassword = "admin";

        @Override
        public DeployTibcoFlowContext build() {

            return getInstance();
        }

        @Override
        protected String getPathSeparator() {
            return WIN_SEPARATOR;
        }

        @Override
        protected DeployTibcoFlowContext getInstance() {
            return new DeployTibcoFlowContext(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        public Builder artifactURL(URL artifactUrl) {
            this.artifactURL = artifactUrl;
            return this;
        }

        public Builder roleId(String roleId) {
            this.roleId = roleId;
            return this;
        }

        public Builder installDir(String installDir) {
            this.installDir = installDir;
            return this;
        }

        public Builder version(TibcoSoftwareComponentVersions version) {
            this.version = version;
            return this;
        }

        public Builder responsefileData(Set<Triplet> installResponseFileData) {
            this.responseFileData = installResponseFileData;
            return this;
        }

        public Builder installerUnpackDir(String installerUnpackDir) {
            this.installerUnpackDir = installerUnpackDir;
            return this;
        }

        public Builder domainName(String domainName) {
            this.domainName = domainName;
            return this;
        }

        public Builder domainUser(String user) {
            this.domainUser = user;
            return this;
        }

        public Builder domainPassword(String passwd) {
            this.domainPassword = passwd;
            return this;
        }

        public Builder setAutoStartBWService() {
            this.autoStartBWService = true;
            return this;
        }

        /**
         * @param fileName Name of the response file only. The fully qualified name is computed.
         * @return
         */
        public Builder responseFileName(String fileName) {
            this.responseFileName = fileName;
            return this;
        }

        /**
         * @param logfileName Name of the log file.
         * @return
         */
        public Builder installerLogFile(String logfile) {
            this.installerLogFile = logfile;
            return this;
        }
    }


}
