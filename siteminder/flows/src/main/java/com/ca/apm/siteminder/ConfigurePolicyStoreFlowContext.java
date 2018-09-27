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

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.IFlowContext;
import org.jetbrains.annotations.NotNull;

public class ConfigurePolicyStoreFlowContext implements IFlowContext {

    private String ldapHost;
    private String ldapPort;
    private String ldapUser;
    private String ldapPass;
    private String ldapRoot;
    private String psRootDir;
    private String jrePath;

    public ConfigurePolicyStoreFlowContext(Builder b) {
        ldapHost = b.ldapHost;
        ldapPort = b.ldapPort;
        ldapUser = b.ldapUser;
        ldapPass = b.ldapPass;
        ldapRoot = b.ldapRoot;
        psRootDir = b.psRootDir;
        this.jrePath = b.jrePath;
    }

    public String getLdapHost() {
        return ldapHost;
    }

    public String getLdapPort() {
        return ldapPort;
    }

    public String getLdapUser() {
        return ldapUser;
    }

    public String getLdapPass() {
        return ldapPass;
    }

    public String getLdapRoot() {
        return ldapRoot;
    }

    public String getPsRootDir() {
        return psRootDir;
    }

    /**
     * @return the pre-installed 32-bit JRE
     */
    public String getJavaRE() {
        return jrePath;
    }

    public static class Builder implements IBuilder<ConfigurePolicyStoreFlowContext> {
        private String jrePath;
        private String ldapHost = "localhost";
        private String ldapPort = "19999";
        private String ldapUser = "uid=siteminder,o=root";
        private String ldapPass = "siteminder";
        private String ldapRoot = "o=root";

        private String psRootDir = "c:\\CA\\install\\siteminder\\"; //System.getenv("NETE_PS_ROOT");

        public Builder ldapHost(@NotNull String ldapHost) {
            this.ldapHost = ldapHost;
            return this;
        }

        public Builder jrePath(@NotNull final String pathToJre) {
            this.jrePath = pathToJre;
            return this;
        }

        public Builder ldapPort(@NotNull String ldapPort) {
            this.ldapPort = ldapPort;
            return this;
        }

        public Builder ldapUser(@NotNull String ldapUser) {
            this.ldapUser = ldapUser;
            return this;
        }

        public Builder ldapPass(@NotNull String ldapPass) {
            this.ldapPass = ldapPass;
            return this;
        }

        public Builder ldapRoot(@NotNull String ldapRoot) {
            this.ldapRoot = ldapRoot;
            return this;
        }

        public Builder psRootDir(@NotNull String psRootDir) {
            this.psRootDir = psRootDir;
            return this;
        }

        @Override
        public ConfigurePolicyStoreFlowContext build() {
            return new ConfigurePolicyStoreFlowContext(this);
        }
    }

}
