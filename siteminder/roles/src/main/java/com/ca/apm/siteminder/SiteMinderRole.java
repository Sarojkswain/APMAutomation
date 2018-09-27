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

package com.ca.apm.siteminder;

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.tas.artifact.thirdParty.JavaVersion;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.webapp.JavaRole;

public class SiteMinderRole extends AbstractRole {

    PolicyServerRole policyServerRole;
    JavaRole javaRole;
    PolicyStoreRole policyStoreRole;
    CADirectoryRole caDirectoryRole;
    AdminUIRole adminUIRole;
    PolicyServerConfigurationRole policyServerConfigurationRole;
    ApacheRole apacheRole;
    WebAgentRole webAgentRole;
    ServletExecRole servletExecRole;

    protected SiteMinderRole(Builder b) {
        super(b.roleID);

        policyServerRole = b.psRole;
        javaRole = b.javaRole;
        policyStoreRole = b.pstoreRole;
        caDirectoryRole = b.caDirRole;
        adminUIRole = b.auiRole;
        policyServerConfigurationRole = b.psConfRole;
        apacheRole = b.apacheRole;
        webAgentRole = b.webAgentRole;
        servletExecRole = b.servletExecRole;
    }

    private void addRoleToHostingMachine(IRole roleToAdd) {
        getHostingMachine().addRole(roleToAdd);
    }

    @Override
    public void deploy(IAutomationAgentClient aac) {

        addRoleToHostingMachine(policyServerRole);
        addRoleToHostingMachine(javaRole);
        addRoleToHostingMachine(policyStoreRole);
        addRoleToHostingMachine(caDirectoryRole);
        addRoleToHostingMachine(adminUIRole);
        addRoleToHostingMachine(policyServerConfigurationRole);
        addRoleToHostingMachine(apacheRole);
        addRoleToHostingMachine(webAgentRole);
        addRoleToHostingMachine(servletExecRole);

        javaRole.deploy(aac);
        policyServerRole.deploy(aac);
        caDirectoryRole.deploy(aac);
        policyStoreRole.deploy(aac);
        adminUIRole.deploy(aac);
        policyServerConfigurationRole.deploy(aac);
        apacheRole.deploy(aac);
        webAgentRole.deploy(aac);
        // Servlet exec installer binaries are extracted in PolicyStoreRole
        // So, servlet Exec role should run after policyStore
        servletExecRole.deploy(aac);
    }

    public static class Builder implements IBuilder<SiteMinderRole> {

        private final String roleID;
        private final ITasResolver tasResolver;
        private String jdkPath;

        String jreHome;
        private final JavaVersion javaVersion = JavaVersion.JDK17_v051b32w;
        private final String javaInstallDir = "C:\\CA\\Java32\\";
        private final String webAgentLocation = "C:\\CA\\install\\webagent";
        private final String apacheDir = "C:\\\\CA\\\\Apache";

        private PolicyServerRole psRole;
        private JavaRole javaRole;
        private PolicyStoreRole pstoreRole;
        private CADirectoryRole caDirRole;
        private AdminUIRole auiRole;
        private PolicyServerConfigurationRole psConfRole;
        private ApacheRole apacheRole;
        private WebAgentRole webAgentRole;
        private ServletExecRole servletExecRole;

        public Builder(String roleID, ITasResolver tasResolver) {
            this.roleID = roleID;
            this.tasResolver = tasResolver;
        }

        @Override
        public SiteMinderRole build() {
            psRole = new PolicyServerRole.Builder("policyServerRole", tasResolver).
                javaInstallDir(jreHome).build();
            javaRole =
                new JavaRole.Builder("javaRole", tasResolver).version(javaVersion).dir(javaInstallDir).build();
            pstoreRole = new PolicyStoreRole.Builder("policyStoreRole", tasResolver).build();
            caDirRole =
                new CADirectoryRole.Builder("caDirRole", tasResolver).version(CADirectoryVersion.v120x64w).build();
            auiRole = new AdminUIRole.Builder("adminUIRole", tasResolver).build();
            psConfRole =
                new PolicyServerConfigurationRole.Builder("policyServerConfigurationRole", tasResolver).jrePath(jreHome).build();
            webAgentRole =
                new WebAgentRole.Builder("webAgentRole", tasResolver).version(WebAgentVersion.v1252sp1x86w).
                    javaLocation(jdkPath).installDir(webAgentLocation).build();
            apacheRole =
                new ApacheRole.Builder("apacheRole", tasResolver).version(ApacheVersion.v2225x32w).installDir(apacheDir).build();
            servletExecRole =
                new ServletExecRole.Builder("servletExecRole").version(ServletExecVersion.v60ax32win).
                    javaPath(jdkPath).apacheDir(apacheDir).webAgentDir(webAgentLocation).build();

            SiteMinderRole siteMinderRole = new SiteMinderRole(this);

            return siteMinderRole;
        }

        public Builder javaInstallDir(String jdkPath) {
            this.jdkPath = jdkPath;
            jreHome = jdkPath + "\\jre";
            return this;
        }

    }
}
