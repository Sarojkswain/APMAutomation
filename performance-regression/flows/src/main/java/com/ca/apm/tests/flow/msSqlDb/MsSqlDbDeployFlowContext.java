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
package com.ca.apm.tests.flow.msSqlDb;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.tests.flow.INetShareUser;
import com.ca.tas.builder.ExtendedBuilderBase;
import org.apache.http.util.Args;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * DeployMsSqlDbFlowContext
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class MsSqlDbDeployFlowContext implements IFlowContext, INetShareUser {

    private final Map<String, String> responseFileOptions;
    private final String encoding;
    private final String adminUserName;
    private final String adminUserPassword;
    private final String installLocation;
    private final String responseFileName;
    private final String unpackDirName;
    private final String installerFileName;
    private final String installSourcesLocation;
    private final URL installPackageUrl;


    protected MsSqlDbDeployFlowContext(MsSqlDbDeployFlowContext.Builder builder) {
        this.responseFileOptions = builder.responseFileOptions;
        this.adminUserName = builder.adminUserName;
        this.adminUserPassword = builder.adminUserPassword;
        this.installLocation = builder.installPath;
        this.installSourcesLocation = builder.installSourcesPath;
        this.responseFileName = builder.responseFileName;
        this.unpackDirName = builder.unpackDirName;
        this.installerFileName = builder.installerFileName;
        this.installPackageUrl = builder.installPackageUrl;
        this.encoding = builder.getEncoding();
    }

    public URL getInstallPackageUrl() {
        return this.installPackageUrl;
    }

    public Map<String, String> getResponseFileOptions() {
        return this.responseFileOptions;
    }

    public String getInstallLocation() {
        return this.installLocation;
    }

    public String getInstallSourcesLocation() {
        return this.installSourcesLocation;
    }

    public String getResponseFileName() {
        return this.responseFileName;
    }

    public String getUnpackDirName() {
        return unpackDirName;
    }

    public String getInstallerFileName() {
        return installerFileName;
    }

    public String getAdminUserName() {
        return this.adminUserName;
    }

    public String getAdminUserPassword() {
        return this.adminUserPassword;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public static class Builder extends ExtendedBuilderBase<MsSqlDbDeployFlowContext.Builder, MsSqlDbDeployFlowContext> {
        private static final Charset DEFAULT_ENCODING;
        private static final String SVCACCOUNT = DEFAULT_COPY_RESULTS_USER;
        private static final String SVCPASSWORD = DEFAULT_COPY_RESULTS_PASSWORD;
        private final Map<String, String> responseFileOptions = new HashMap<>();
        protected String installPath;
        protected String installSourcesPath;
        protected String msSqlVersion;
        protected String adminUserPassword;
        protected String adminUserName;

        protected String responseFileName;
        protected String unpackDirName;
        protected String installerFileName;
        protected URL installPackageUrl;

        static {
            DEFAULT_ENCODING = StandardCharsets.UTF_8;
        }

        public Builder() {
            this.encoding(DEFAULT_ENCODING);
            this.installPath(this.concatPaths(this.getDeployBase(), "mssql"));
            this.installSourcesPath(this.concatPaths(this.getDeployBase(), "mssql_sources"));
            this.adminUserName(SVCACCOUNT);
            this.adminUserPassword(SVCPASSWORD);
        }

        public String getAdminUserPassword() {
            return adminUserPassword;
        }

        public String getAdminUserName() {
            return adminUserName;
        }

        public MsSqlDbDeployFlowContext build() {
            this.initResponseFileData();
            MsSqlDbDeployFlowContext context = this.getInstance();
            Args.notNull(context.installLocation, "MSSQL install path");
            Args.notNull(context.adminUserName, "adminUserName");
            Args.notNull(context.adminUserPassword, "adminUserPassword");
            Args.notNull(context.installLocation, "installPath");
            Args.notNull(context.installPackageUrl, "installPackageUrl");
            Args.notNull(context.installSourcesLocation, "installSourcesPath");
            Args.notNull(context.responseFileName, "responseFileName");
            Args.notNull(context.unpackDirName, "unpackDirName");
            Args.notNull(context.installerFileName, "installerFileName");
            Args.notNull(context.encoding, "Response file encoding");
            return context;
        }

        protected void initResponseFileData() {
            this.responseFileOptions.put("MEDIASOURCE", this.installSourcesPath + "\\\\" + this.unpackDirName);
            this.responseFileOptions.put("INSTANCEDIR", this.installPath);
            this.responseFileOptions.put("SVCACCOUNT", this.adminUserName);
            this.responseFileOptions.put("SVCPASSWORD", this.adminUserPassword);
        }

        protected MsSqlDbDeployFlowContext getInstance() {
            return new MsSqlDbDeployFlowContext(this);
        }

        public MsSqlDbDeployFlowContext.Builder installPath(String installLocation) {
            this.installPath = installLocation;
            return this.builder();
        }

        public MsSqlDbDeployFlowContext.Builder installSourcesPath(String installSourcesPath) {
            this.installSourcesPath = installSourcesPath;
            return this.builder();
        }

        public MsSqlDbDeployFlowContext.Builder adminUserName(String adminUserName) {
            this.adminUserName = adminUserName;
            return this.builder();
        }

        public MsSqlDbDeployFlowContext.Builder adminUserPassword(String adminUserPassword) {
            this.adminUserPassword = adminUserPassword;
            return this.builder();
        }

        public MsSqlDbDeployFlowContext.Builder installPackageUrl(URL installPackageUrl) {
            this.installPackageUrl = installPackageUrl;
            return this.builder();
        }

        public MsSqlDbDeployFlowContext.Builder version(String version) {
            this.msSqlVersion = version;
            return this.builder();
        }

        public MsSqlDbDeployFlowContext.Builder responseFileName(String responseFileName) {
            this.responseFileName = responseFileName;
            return this.builder();
        }

        public MsSqlDbDeployFlowContext.Builder unpackDirName(String unpackDirName) {
            this.unpackDirName = unpackDirName;
            return this.builder();
        }

        public MsSqlDbDeployFlowContext.Builder installerFileName(String installerFileName) {
            this.installerFileName = installerFileName;
            return this.builder();
        }

        protected MsSqlDbDeployFlowContext.Builder builder() {
            return this;
        }
    }
}