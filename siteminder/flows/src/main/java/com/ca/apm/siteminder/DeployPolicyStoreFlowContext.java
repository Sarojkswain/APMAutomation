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

import java.io.File;
import java.net.URL;

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.IFlowContext;
import org.jetbrains.annotations.NotNull;

public class DeployPolicyStoreFlowContext implements IFlowContext {

    private File installLocation;
    private URL artifactUrl;
    private String psDir;
    private String cadirectoryDir;

    public DeployPolicyStoreFlowContext(Builder b) {
        installLocation = b.installLocation;
        artifactUrl = b.artifactUrl;
        psDir = b.psDir;
        cadirectoryDir = b.cadirectoryDir;
    }

    /**
     * @return where the policy store setup scripts will be unpacked to
     */
    public File getInstallDir() {
        return installLocation;
    }

    public URL getArtifactUrl() {
        return artifactUrl;
    }

    public String getPsDir() {
        return psDir;
    }

    public String getCadirectoryDir() {
        return cadirectoryDir;
    }

    public static class Builder implements IBuilder<DeployPolicyStoreFlowContext> {
        private File installLocation = new File("C:\\CA\\install");
        private URL artifactUrl;
        private String psDir = "C:\\CA\\install\\siteminder";
        private String cadirectoryDir = "C:\\CA\\install\\dxserver";

        public Builder installLocation(@NotNull File installLocation) {
            this.installLocation = installLocation;
            return this;
        }

        @Override
        public DeployPolicyStoreFlowContext build() {
            return new DeployPolicyStoreFlowContext(this);
        }

        /**
         * @param artifactUrl
         */
        public Builder storeArtifactUrl(URL artifactUrl) {
            this.artifactUrl = artifactUrl;
            return this;

        }

        public Builder psDir(String psDir) {
            this.psDir = psDir;
            return this;
        }

        public Builder cadirectoryDir(String cadirectoryDir) {
            this.cadirectoryDir = cadirectoryDir;
            return this;

        }

    }

}
