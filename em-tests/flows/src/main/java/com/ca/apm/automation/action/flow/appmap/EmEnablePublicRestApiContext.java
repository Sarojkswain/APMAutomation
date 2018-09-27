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
package com.ca.apm.automation.action.flow.appmap;

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.IFlowContext;

/**
 * @author surma04
 *
 */
public class EmEnablePublicRestApiContext implements IFlowContext {

    private String apmRootDir;
    private String introscopeEnterpriseManagerLax;
    private String developmentToken;
    private String developmentUser;

    public static String DEV_TOKEN_KEY = "-Dappmap.token";
    public static String DEV_USER_KEY = "-Dappmap.user";

    /**
     * @param builder
     * 
     */
    public EmEnablePublicRestApiContext(Builder builder) {
        this.apmRootDir = builder.apmRootDir;
        this.introscopeEnterpriseManagerLax = builder.introscopeEnterpriseManagerLax;
        this.developmentToken = builder.developmentToken;
        this.developmentUser = builder.developmentUser;
    }

    public static class Builder implements IBuilder<EmEnablePublicRestApiContext> {

        public String host;
        public String apmRootDir;
        String introscopeEnterpriseManagerLax = "Introscope_Enterprise_Manager.lax";
        String developmentToken = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
        String developmentUser = "admin";

        public Builder() {}


        @Override
        public EmEnablePublicRestApiContext build() {
            return new EmEnablePublicRestApiContext(this);
        }

        public Builder apmRootDir(String apmRootDir) {
            this.apmRootDir = apmRootDir;
            return this;
        }



        public Builder introscopeEnterpriseManagerLax(String introscopeEnterpriseManagerLax) {
            this.introscopeEnterpriseManagerLax = introscopeEnterpriseManagerLax;
            return this;
        }

        public Builder developmentToken(String developmentToken) {
            this.developmentToken = developmentToken;
            return this;
        }

        public Builder developmentUser(String developmentUser) {
            this.developmentUser = developmentUser;
            return this;
        }

        public Builder hostname(String hostname) {
            this.host = hostname;
            return this;
        }
    }

    public String getApmRootDir() {
        return this.apmRootDir;
    }

    public String getIntroscopeEnterpriseManagerLax() {
        return introscopeEnterpriseManagerLax;
    }

    public String getDevelopmentToken() {
        return developmentToken;
    }

    public String getDevelopmentUser() {
        return developmentUser;
    }
}
