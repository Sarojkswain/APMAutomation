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
import java.util.LinkedHashSet;
import java.util.Set;

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.automation.action.responsefile.Triplet;
import org.jetbrains.annotations.NotNull;

/**
 * @author surma04
 */
public class DeployCADirectoryFlowContext implements IFlowContext {

    private String installLocation;
    private URL artifactUrl;
    private String pathToInstaller;


    /**
     *
     */
    public DeployCADirectoryFlowContext(Builder b) {
        this.installLocation = b.installLocation;
        this.artifactUrl = b.artifactUrl;
        this.pathToInstaller = b.pathToInstallerFile;
    }

    public Set<Triplet> getInstallResponseFileData() {
        Set<Triplet> responses = new LinkedHashSet<>();

        /*
         * ETRDIRBASEPATH="C:\Program Files\CA\Directory\"
         * ETRDIR_DXSERVER_SAMPLES=1
         * ETRDIR_CONFIGURE_DXADMIND=0
         * RESPONSE_FILE_NAME=cadir.rsp
         * RESPONSE_FILE_FOLDER=C:\
         * UI_LEVEL=1
         * ADDLOCAL=DXServer
         */

        responses.add(new Triplet("ETRDIRBASEPATH", "=", this.installLocation.toString().replace("\\", "\\\\")));
        responses.add(new Triplet("ETRDIR_DXSERVER_SAMPLES", "=", "1"));
        responses.add(new Triplet("ETRDIR_CONFIGURE_DXADMIND", "=", "0"));
        responses.add(new Triplet("UI_LEVEL", "=", "1"));
        responses.add(new Triplet("ADDLOCAL", "=", "DXServer"));
        return responses;
    }

    /**
     * @return
     */
    public String getInstallDir() {
        return this.installLocation;
    }

    /**
     * @return
     */
    public URL getArtifactUrl() {
        return this.artifactUrl;
    }

    /**
     * @return the path to the installer within the unzipped structure
     */
    public String getPathToInstaller() {
        return this.pathToInstaller;
    }

    public static class Builder implements IBuilder<DeployCADirectoryFlowContext> {

        public String pathToInstallerFile = "\\dxserver\\windows\\dxsetup.exe";
        private URL artifactUrl;
        private String installLocation = "C:\\CA\\install";

        /*
         * (non-Javadoc)
         * 
         * @see com.ca.apm.automation.action.flow.IBuilder#build()
         */
        @Override
        public DeployCADirectoryFlowContext build() {
            return new DeployCADirectoryFlowContext(this);
        }

        public Builder pathToInstallerFile(@NotNull String installFilePath) {
            this.pathToInstallerFile = installFilePath;
            return this;
        }

        public Builder installLocation(@NotNull String installLocation) {
            this.installLocation = installLocation;
            return this;
        }

        public Builder artifactUrl(@NotNull URL artifactUrl) {
            this.artifactUrl = artifactUrl;
            return this;
        }

    }

    /**
     * dxsetup RESPONSE_FILE=C:\cadir.rsp ETRDIR_DXADMIND_PASSWORD=dxadmind
     *
     * @return params for the silent installation
     */
    public String[] getInstallerParams() {
        return new String[] {
            "RESPONSE_FILE=" + this.getResponseFileLocation(), "ETRDIR_DXADMIND_PASSWORD=dxadmind"};
    }

    /**
     * @return
     */
    public String getResponseFileLocation() {
        return this.installLocation + "/cadir.rsp";
    }

}
