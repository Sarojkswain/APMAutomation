/*
 * Copyright (c) 2016 CA. All rights reserved.
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
package com.ca.apm.tests.flow.websphere85;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.ExtendedBuilderBase;
import org.apache.http.util.Args;

import java.net.URL;

/**
 * Flow Context for installing Websphere 8.5 And IBM Java 7
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class Websphere85DeployFlowContext implements IFlowContext {


    private final String managerZipSourcesLocation;
    private final String was85ZipSourcesLocation;
    private final String java7ZipSourcesLocation;

    private final URL managerZipPackageUrl;
    private final URL was85Zip1PackageUrl;
    private final URL was85Zip2PackageUrl;
    private final URL was85Zip3PackageUrl;
    private final URL java7Zip1PackageUrl;
    private final URL java7Zip2PackageUrl;
    private final URL java7Zip3PackageUrl;

    private final String installManagerLocation;
    private final String installWasLocation;
    private final String imSharedLocation;
    private final String sdkName;
    private final String profileName;
    private final String profilePath;
    private final String templatePath;
    private final String cellName;
    private final String hostName;
    private final String nodeName;
    private final Boolean def;
    private final Boolean enableAdminSecurity;
    private final String adminUserName;
    private final String adminPassword;
    private final Boolean winserviceCheck;

    protected Websphere85DeployFlowContext(Websphere85DeployFlowContext.Builder builder) {

        this.managerZipSourcesLocation = builder.managerZipSourcesLocation;
        this.was85ZipSourcesLocation = builder.was85ZipSourcesLocation;
        this.java7ZipSourcesLocation = builder.java7ZipSourcesLocation;

        this.managerZipPackageUrl = builder.managerZipPackageUrl;
        this.was85Zip1PackageUrl = builder.was85Zip1PackageUrl;
        this.was85Zip2PackageUrl = builder.was85Zip2PackageUrl;
        this.was85Zip3PackageUrl = builder.was85Zip3PackageUrl;
        this.java7Zip1PackageUrl = builder.java7Zip1PackageUrl;
        this.java7Zip2PackageUrl = builder.java7Zip2PackageUrl;
        this.java7Zip3PackageUrl = builder.java7Zip3PackageUrl;

        this.installManagerLocation = builder.installManagerLocation;
        this.installWasLocation = builder.installWasLocation;
        this.imSharedLocation = builder.imSharedLocation;
        this.sdkName = builder.sdkName;
        this.profileName = builder.profileName;
        this.profilePath = builder.profilePath;
        this.templatePath = builder.templatePath;
        this.cellName = builder.cellName;
        this.hostName = builder.hostName;
        this.nodeName = builder.nodeName;
        this.def = builder.def;
        this.enableAdminSecurity = builder.enableAdminSecurity;
        this.adminUserName = builder.adminUserName;
        this.adminPassword = builder.adminPassword;
        this.winserviceCheck = builder.winserviceCheck;
    }

    public String getManagerZipSourcesLocation() {
        return managerZipSourcesLocation;
    }

    public String getWas85ZipSourcesLocation() {
        return was85ZipSourcesLocation;
    }

    public String getJava7ZipSourcesLocation() {
        return java7ZipSourcesLocation;
    }

    public String getInstallManagerLocation() {
        return installManagerLocation;
    }

    public URL getManagerZipPackageUrl() {
        return managerZipPackageUrl;
    }

    public URL getWas85Zip1PackageUrl() {
        return was85Zip1PackageUrl;
    }

    public URL getWas85Zip2PackageUrl() {
        return was85Zip2PackageUrl;
    }

    public URL getWas85Zip3PackageUrl() {
        return was85Zip3PackageUrl;
    }

    public URL getJava7Zip1PackageUrl() {
        return java7Zip1PackageUrl;
    }

    public URL getJava7Zip2PackageUrl() {
        return java7Zip2PackageUrl;
    }

    public URL getJava7Zip3PackageUrl() {
        return java7Zip3PackageUrl;
    }

    public String getManagerInstallLocation() {
        return installManagerLocation;
    }

    public String getInstallWasLocation() {
        return installWasLocation;
    }

    public String getImSharedLocation() {
        return imSharedLocation;
    }

    public String getSdkName() {
        return sdkName;
    }

    public String getProfileName() {
        return profileName;
    }

    public String getProfilePath() {
        return profilePath;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public String getCellName() {
        return cellName;
    }

    public String getHostName() {
        return hostName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public Boolean getDef() {
        return def;
    }

    public Boolean getEnableAdminSecurity() {
        return enableAdminSecurity;
    }

    public String getAdminUserName() {
        return adminUserName;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public Boolean getWinserviceCheck() {
        return winserviceCheck;
    }

    public static class Builder extends ExtendedBuilderBase<Websphere85DeployFlowContext.Builder, Websphere85DeployFlowContext> {
        protected String managerZipSourcesLocation;
        protected String was85ZipSourcesLocation;
        protected String java7ZipSourcesLocation;

        protected URL managerZipPackageUrl;
        protected URL was85Zip1PackageUrl;
        protected URL was85Zip2PackageUrl;
        protected URL was85Zip3PackageUrl;
        protected URL java7Zip1PackageUrl;
        protected URL java7Zip2PackageUrl;
        protected URL java7Zip3PackageUrl;

        protected String installManagerLocation;
        protected String installWasLocation;
        protected String imSharedLocation;
        protected String sdkName;
        protected String profileName;
        protected String profilePath;
        protected String templatePath;
        protected String cellName;
        protected String hostName;
        protected String nodeName;
        protected Boolean def;
        protected Boolean enableAdminSecurity;
        protected String adminUserName;
        protected String adminPassword;
        protected Boolean winserviceCheck;

        public Builder() {
            this.managerZipSourcesLocation(this.concatPaths(this.getDeployBase(), "managerZip"));
            this.was85ZipSourcesLocation(this.concatPaths(this.getDeployBase(), "was85Zip"));
            this.java7ZipSourcesLocation(this.concatPaths(this.getDeployBase(), "java7Zip"));

            this.sdkName("1.7_64");

            this.profileName("appsvr01");
            this.cellName("cell01");
            this.nodeName("node01");
            this.def(true);
            this.enableAdminSecurity(false);
            this.adminUserName("wasadmin");
            this.adminPassword("wasadmin");
            this.winserviceCheck(false);
        }

        public Websphere85DeployFlowContext build() {
            if (profilePath == null) {
                Args.notNull(installWasLocation, "installWasLocation");
                Args.notNull(profileName, "profileName");
                this.profilePath = this.installWasLocation + "\\profiles\\" + this.profileName;
            }
            if (templatePath == null) {
                Args.notNull(installWasLocation, "installWasLocation");
                this.templatePath = this.installWasLocation + "\\profileTemplates\\default";
            }

            Websphere85DeployFlowContext context = this.getInstance();

            Args.notNull(context.managerZipSourcesLocation, "managerZipSourcesLocation");
            Args.notNull(context.was85ZipSourcesLocation, "was85ZipSourcesLocation");
            Args.notNull(context.java7ZipSourcesLocation, "java7ZipSourcesLocation");

            Args.notNull(context.managerZipPackageUrl, "managerZipPackageUrl");
            Args.notNull(context.was85Zip1PackageUrl, "was85Zip1PackageUrl");
            Args.notNull(context.was85Zip2PackageUrl, "was85Zip2PackageUrl");
            Args.notNull(context.was85Zip3PackageUrl, "was85Zip3PackageUrl");
            Args.notNull(context.java7Zip1PackageUrl, "java7Zip1PackageUrl");
            Args.notNull(context.java7Zip2PackageUrl, "java7Zip2PackageUrl");
            Args.notNull(context.java7Zip3PackageUrl, "java7Zip3PackageUrl");

            Args.notNull(context.installManagerLocation, "installManagerLocation");
            Args.notNull(context.installWasLocation, "installWasLocation");
            Args.notNull(context.imSharedLocation, "imSharedLocation");
            Args.notNull(context.sdkName, "sdkName");
            Args.notNull(context.profileName, "profileName");
            Args.notNull(context.profilePath, "profilePath");
            Args.notNull(context.templatePath, "templatePath");
            Args.notNull(context.cellName, "cellName");
            Args.notNull(context.hostName, "hostName");
            Args.notNull(context.nodeName, "nodeName");
            Args.notNull(context.def, "def");
            Args.notNull(context.enableAdminSecurity, "enableAdminSecurity");
            Args.notNull(context.adminUserName, "adminUserName");
            Args.notNull(context.adminPassword, "adminPassword");
            Args.notNull(context.winserviceCheck, "winserviceCheck");

            return context;
        }

        protected Websphere85DeployFlowContext getInstance() {
            return new Websphere85DeployFlowContext(this);
        }

        public Builder managerZipSourcesLocation(String managerZipSourcesLocation) {
            this.managerZipSourcesLocation = managerZipSourcesLocation;
            return this.builder();
        }

        public Builder was85ZipSourcesLocation(String was85ZipSourcesLocation) {
            this.was85ZipSourcesLocation = was85ZipSourcesLocation;
            return this.builder();
        }

        public Builder java7ZipSourcesLocation(String java7ZipSourcesLocation) {
            this.java7ZipSourcesLocation = java7ZipSourcesLocation;
            return this.builder();
        }

        public Builder managerZipPackageUrl(URL managerZipPackageUrl) {
            this.managerZipPackageUrl = managerZipPackageUrl;
            return this.builder();
        }

        public Builder was85Zip1PackageUrl(URL was85Zip1PackageUrl) {
            this.was85Zip1PackageUrl = was85Zip1PackageUrl;
            return this.builder();
        }

        public Builder was85Zip2PackageUrl(URL was85Zip2PackageUrl) {
            this.was85Zip2PackageUrl = was85Zip2PackageUrl;
            return this.builder();
        }

        public Builder was85Zip3PackageUrl(URL was85Zip3PackageUrl) {
            this.was85Zip3PackageUrl = was85Zip3PackageUrl;
            return this.builder();
        }

        public Builder java7Zip1PackageUrl(URL java7Zip1PackageUrl) {
            this.java7Zip1PackageUrl = java7Zip1PackageUrl;
            return this.builder();
        }

        public Builder java7Zip2PackageUrl(URL java7Zip2PackageUrl) {
            this.java7Zip2PackageUrl = java7Zip2PackageUrl;
            return this.builder();
        }

        public Builder java7Zip3PackageUrl(URL java7Zip3PackageUrl) {
            this.java7Zip3PackageUrl = java7Zip3PackageUrl;
            return this.builder();
        }

        public Builder installManagerLocation(String installManagerLocation) {
            this.installManagerLocation = installManagerLocation;
            return this.builder();
        }

        public Builder installWasLocation(String installWasLocation) {
            this.installWasLocation = installWasLocation;
            return this.builder();
        }

        public Builder imSharedLocation(String imSharedLocation) {
            this.imSharedLocation = imSharedLocation;
            return this.builder();
        }

        public Builder sdkName(String sdkName) {
            this.sdkName = sdkName;
            return this.builder();
        }

        public Builder profileName(String profileName) {
            this.profileName = profileName;
            return this.builder();
        }

        public Builder profilePath(String profilePath) {
            this.profilePath = profilePath;
            return this.builder();
        }

        public Builder templatePath(String templatePath) {
            this.templatePath = templatePath;
            return this.builder();
        }

        public Builder cellName(String cellName) {
            this.cellName = cellName;
            return this.builder();
        }

        public Builder hostName(String hostName) {
            this.hostName = hostName;
            return this.builder();
        }

        public Builder nodeName(String nodeName) {
            this.nodeName = nodeName;
            return this.builder();
        }

        public Builder def(Boolean def) {
            this.def = def;
            return this.builder();
        }

        public Builder enableAdminSecurity(Boolean enableAdminSecurity) {
            this.enableAdminSecurity = enableAdminSecurity;
            return this.builder();
        }

        public Builder adminUserName(String adminUserName) {
            this.adminUserName = adminUserName;
            return this.builder();
        }

        public Builder adminPassword(String adminPassword) {
            this.adminPassword = adminPassword;
            return this.builder();
        }

        public Builder winserviceCheck(Boolean winserviceCheck) {
            this.winserviceCheck = winserviceCheck;
            return this.builder();
        }

        protected Websphere85DeployFlowContext.Builder builder() {
            return this;
        }
    }
}