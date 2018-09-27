/*
 * Copyright (c) 2015 CA.  All rights reserved.
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

package com.ca.apm.siteminder;

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.IFlowContext;

/**
 * @author Sundeep (bhusu01)
 */
public class ConfigureSMFedPartnershipFlowContext implements IFlowContext {

    private String adminUIURL;
    private String adminUser;
    private String adminPassword;
    private String siteMinderHost;
    private String wvMetadataPath;
    private String wsMetadataPath;
    private String emMetadataPath;
    private String webViewSPID;
    private String webstartSPID;
    private String emSPID;
    private String idpEntityID;
    private String idpEntityName;
    private boolean importWvSP;
    private boolean importWsSP;
    private boolean importEmSP;
    

    ConfigureSMFedPartnershipFlowContext(Builder builder) {
        this.adminUIURL = "https://" + builder.adminUIHost + builder.loginPathWithPort;
        this.adminUser = builder.adminUser;
        this.adminPassword = builder.adminPassword;
        this.webViewSPID = builder.webViewSPID;
        this.webstartSPID = builder.webstartSPID;
        this.emSPID = builder.emSPID;
        this.siteMinderHost = builder.adminUIHost;
        this.idpEntityID = builder.idpEntityID;
        this.idpEntityName = builder.idpEntityName;
        this.wvMetadataPath = builder.emRootDir + builder.webviewMetaDataPath;
        this.wsMetadataPath = builder.emRootDir + builder.webstartMetaDataPath;
        this.emMetadataPath = builder.emRootDir + builder.emMetaDataPath;
        this.importWvSP = builder.importWvSP;
        this.importWsSP = builder.importWsSP;
        this.importEmSP = builder.importEmSP;
    }

    public String getAdminUIURL() {
        return adminUIURL;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public String getWVMetadataPath() {
        return wvMetadataPath;
    }

    public String getWebViewSPID() {
        return webViewSPID;
    }

    public String getWebstartSPID() {
        return webstartSPID;
    }

    public String getEmSPID() {
        return emSPID;
    }

    public String getSMHost() {
        return siteMinderHost;
    }

    public String getIdPEntityID() {
        return idpEntityID;
    }

    public String getIdpEntityID() {
        return idpEntityID;
    }

    public String getIdpEntityName() {
        return idpEntityName;
    }

    public String getWsMetadataPath() {
        return wsMetadataPath;
    }

    public String getEmMetadataPath() {
        return emMetadataPath;
    }
    
    public boolean isImportWvSP() {
        return importWvSP;
    }

    public boolean isImportWsSP() {
        return importWsSP;
    }

    public boolean isImportEmSP() {
        return importEmSP;
    }

    public static class Builder implements IBuilder<ConfigureSMFedPartnershipFlowContext> {

        private String adminUIHost = "localhost";
        private String loginPathWithPort = ":8443/iam/siteminder/adminui";
        private String adminUser = "siteminder";
        private String adminPassword = "siteminder";
        public String webViewSPID = "apm.webview.sp";
        public String webstartSPID = "apm.webstart.sp";
        public String emSPID = "apm.em.sp";
        public String idpEntityID = "ca.sm.idp";
        public String idpEntityName = "SiteminderIdP";
        private String emRootDir = "c:\\automation\\deployed\\em\\";
        private String webviewMetaDataPath = "config\\saml-sp-metadata.xml";
        private String webstartMetaDataPath = "config\\saml-sp-webstart-metadata.xml";
        private String emMetaDataPath = "config\\saml-sp-em-metadata.xml";
        public boolean importWvSP = false;
        public boolean importWsSP = false;
        public boolean importEmSP = false;
        

        public Builder adminUIHost(String adminUIHost) {
            this.adminUIHost = adminUIHost;
            return this;
        }

        public Builder adminUser(String user) {
            this.adminUser = user;
            return this;
        }

        public Builder adminPass(String password) {
            this.adminPassword = password;
            return this;
        }

        public Builder webViewSPID(String wvSPID) {
            this.webViewSPID = wvSPID;
            return this;
        }

        public Builder webstartSPID(String wsSPID) {
            this.webstartSPID = wsSPID;
            return this;
        }

        public Builder emSPID(String emSPID) {
            this.emSPID = emSPID;
            return this;
        }

        public Builder idpEntityID(String entityID) {
            this.idpEntityID = entityID;
            return this;
        }

        public Builder idpEntityName(String entityName) {
            this.idpEntityName = entityName;
            return this;
        }

        public Builder emRootDir(String emRootDir) {
            this.emRootDir = emRootDir;
            return this;
        }
        
        public Builder importWvSP() {
            this.importWvSP = true;
            return this;
        }

        public Builder importWsSP() {
            this.importWsSP = true;
            return this;
        }

        public Builder importEmSP() {
            this.importEmSP = true;
            return this;
        }


        @Override
        public ConfigureSMFedPartnershipFlowContext build() {
            return new ConfigureSMFedPartnershipFlowContext(this);
        }
    }


}
