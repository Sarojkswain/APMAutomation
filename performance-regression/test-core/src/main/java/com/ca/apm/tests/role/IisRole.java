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
package com.ca.apm.tests.role;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.apache.http.util.Args;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * MSSQL DB role class
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class IisRole extends AbstractRole {

    public static final String ENV_IIS_START = "iisStart";
    public static final String ENV_IIS_STOP = "iisStop";

    private final RunCommandFlowContext flowContext;
    private final RunCommandFlowContext undeployFlowContext;

    private final RunCommandFlowContext startIisFlowContext;
    private final RunCommandFlowContext stopIisFlowContext;

    private final boolean undeployOnly;
    private final boolean predeployed;

    /**
     * @param builder Builder object containing all necessary data
     */
    protected IisRole(IisRole.Builder builder) {
        super(builder.roleId, builder.getEnvProperties());

        startIisFlowContext = builder.startIisFlowContext;
        stopIisFlowContext = builder.stopIisFlowContext;

        flowContext = builder.flowContext;
        undeployFlowContext = builder.undeployFlowContext;

        undeployOnly = builder.undeployOnly;
        predeployed = builder.predeployed;
    }

    public boolean isUndeployOnly() {
        return undeployOnly;
    }

    public boolean isPredeployed() {
        return predeployed;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        if (undeployOnly) {
            // this.runFlow(aaClient, RunCommandFlow.class, this.undeployFlowContext); // todo needs machine restart, solve first, then uncomment
        } else if (!predeployed) {
            this.runFlow(aaClient, RunCommandFlow.class, this.flowContext);
        }
    }

    public static class Builder extends BuilderBase<IisRole.Builder, IisRole> {
        private final String roleId;
        private final ITasResolver tasResolver;

        protected RunCommandFlowContext.Builder startIisFlowContextBuilder;
        protected RunCommandFlowContext startIisFlowContext;
        protected RunCommandFlowContext.Builder stopIisFlowContextBuilder;
        protected RunCommandFlowContext stopIisFlowContext;

        protected RunCommandFlowContext.Builder flowContextBuilder;
        protected RunCommandFlowContext flowContext;
        protected RunCommandFlowContext.Builder undeployFlowContextBuilder;
        protected RunCommandFlowContext undeployFlowContext;

        protected boolean undeployOnly;
        protected boolean predeployed;

        protected Collection<String> args1 = Arrays.asList("/Online", "/quiet", "/norestart");
        protected Collection<String> args2enable = Arrays.asList("/Enable-Feature");
        protected Collection<String> args2disable = Arrays.asList("/Disable-Feature");
        protected Collection<String> args3 = Arrays.asList(
                "/FeatureName:IIS-ApplicationDevelopment", "/FeatureName:IIS-ASP", "/FeatureName:IIS-ASPNET",
                "/FeatureName:IIS-BasicAuthentication", "/FeatureName:IIS-CGI",
                "/FeatureName:IIS-ClientCertificateMappingAuthentication",
                "/FeatureName:IIS-CommonHttpFeatures", "/FeatureName:IIS-CustomLogging",
                "/FeatureName:IIS-DefaultDocument", "/FeatureName:IIS-DigestAuthentication",
                "/FeatureName:IIS-DirectoryBrowsing", "/FeatureName:IIS-FTPExtensibility",
                "/FeatureName:IIS-FTPServer", "/FeatureName:IIS-FTPSvc",
                "/FeatureName:IIS-HealthAndDiagnostics", "/FeatureName:IIS-HostableWebCore",
                "/FeatureName:IIS-HttpCompressionDynamic", "/FeatureName:IIS-HttpCompressionStatic",
                "/FeatureName:IIS-HttpErrors", "/FeatureName:IIS-HttpLogging", "/FeatureName:IIS-HttpRedirect",
                "/FeatureName:IIS-HttpTracing", "/FeatureName:IIS-IIS6ManagementCompatibility",
                "/FeatureName:IIS-IISCertificateMappingAuthentication", "/FeatureName:IIS-IPSecurity",
                "/FeatureName:IIS-ISAPIExtensions", "/FeatureName:IIS-ISAPIFilter",
                "/FeatureName:IIS-LegacyScripts", "/FeatureName:IIS-LegacySnapIn",
                "/FeatureName:IIS-LoggingLibraries", "/FeatureName:IIS-ManagementConsole",
                "/FeatureName:IIS-ManagementScriptingTools", "/FeatureName:IIS-ManagementService",
                "/FeatureName:IIS-Metabase", "/FeatureName:IIS-NetFxExtensibility",
                "/FeatureName:IIS-ODBCLogging", "/FeatureName:IIS-Performance",
                "/FeatureName:IIS-RequestFiltering", "/FeatureName:IIS-RequestMonitor",
                "/FeatureName:IIS-Security", "/FeatureName:IIS-ServerSideIncludes",
                "/FeatureName:IIS-StaticContent", "/FeatureName:IIS-URLAuthorization",
                "/FeatureName:IIS-WebDAV", "/FeatureName:IIS-WebServer",
                "/FeatureName:IIS-WebServerManagementTools", "/FeatureName:IIS-WebServerRole",
                "/FeatureName:IIS-WindowsAuthentication", "/FeatureName:IIS-WMICompatibility",
                "/FeatureName:WAS-ConfigurationAPI", "/FeatureName:WAS-NetFxEnvironment",
                "/FeatureName:WAS-ProcessModel", "/FeatureName:WAS-WindowsActivationService",
                "/FeatureName:MSMQ-Server");

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;

            startIisFlowContextBuilder = new RunCommandFlowContext.Builder("iisreset");
            stopIisFlowContextBuilder = new RunCommandFlowContext.Builder("iisreset");
            flowContextBuilder = new RunCommandFlowContext.Builder("dism");
            undeployFlowContextBuilder = new RunCommandFlowContext.Builder("dism");
        }

        public IisRole build() {


            // deploy context
            Collection<String> deployArgs = new ArrayList<>();
            deployArgs.addAll(args1);
            deployArgs.addAll(args2enable);
            deployArgs.addAll(args3);
            flowContext = flowContextBuilder.args(deployArgs).build();
            // undeploy context
            Collection<String> undeployArgs = new ArrayList<>();
            undeployArgs.addAll(args1);
            undeployArgs.addAll(args2disable);
            undeployArgs.addAll(args3);
            undeployFlowContext = undeployFlowContextBuilder.args(undeployArgs).build();
            // start context
            startIisFlowContext = startIisFlowContextBuilder.args(Collections.singletonList("/start")).build();
            getEnvProperties().add(ENV_IIS_START, startIisFlowContext);
            // stop context
            stopIisFlowContext = stopIisFlowContextBuilder.args(Collections.singletonList("/stop")).build();
            getEnvProperties().add(ENV_IIS_STOP, this.stopIisFlowContext);

            IisRole role = this.getInstance();
            Args.notNull(role.flowContext, "Deploy flow context cannot be null.");
            return role;
        }

        protected IisRole getInstance() {
            return new IisRole(this);
        }

        public Builder undeployOnly(boolean undeployOnly) {
            this.undeployOnly = undeployOnly;
            return this.builder();
        }

        public Builder undeployOnly() {
            this.undeployOnly = true;
            return this.builder();
        }

        public Builder predeployed(boolean predeployed) {
            this.predeployed = predeployed;
            return this.builder();
        }

        public Builder predeployed() {
            this.predeployed = true;
            return this.builder();
        }

        protected Builder builder() {
            return this;
        }
    }
}