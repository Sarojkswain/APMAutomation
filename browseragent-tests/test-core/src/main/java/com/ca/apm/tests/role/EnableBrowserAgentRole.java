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

/**
 * Browser Agent automation - Enable Browser Agent Role
 *
 * @author gupra04
 */

package com.ca.apm.tests.role;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

public class EnableBrowserAgentRole extends AbstractRole {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnableBrowserAgentRole.class);

    private String agentInstallDir;
    private String browserAgentExtJar;
    private String browserAgentProfileFile;
    private String appServerPblFile;
    private String agentName;
    private String agentProcessName;

    private String browserAgentProfileFilePath;
    private String browserAgentProfileFileFullPath;

    /**
     * @param builder
     *        Builder object containing all necessary data
     */
    protected EnableBrowserAgentRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.agentInstallDir = builder.agentInstallDir;
        this.browserAgentExtJar = builder.browserAgentExtJar;
        this.browserAgentProfileFile = builder.browserAgentProfileFile;
        this.appServerPblFile = builder.appServerPblFile;
        this.agentName = builder.agentName;
        this.agentProcessName = builder.agentProcessName;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {

        browserAgentProfileFilePath =
            agentInstallDir + TasBuilder.WIN_SEPARATOR + "wily" + TasBuilder.WIN_SEPARATOR + "core"
                + TasBuilder.WIN_SEPARATOR + "config" + TasBuilder.WIN_SEPARATOR;
        browserAgentProfileFileFullPath = browserAgentProfileFilePath + browserAgentProfileFile;

        getEnvProperties().put("agentHome", agentInstallDir + TasBuilder.WIN_SEPARATOR + "wily");
        getEnvProperties().put("agentProfileFileFullPath", browserAgentProfileFileFullPath);
        getEnvProperties().put("browserAgentExtJar", browserAgentExtJar);
        getEnvProperties().put("appServerPblFile", appServerPblFile);
        getEnvProperties().put("agentProfileFile", browserAgentProfileFile);
        getEnvProperties().put("agentName", agentName);
        getEnvProperties().put("agentProcessName", agentProcessName);

        //copyBrowserAgentJar(aaClient);
        //updatePbl(aaClient);
        //updateProfile(aaClient);
    }

    private void updatePbl(IAutomationAgentClient aaClient) {
        String browserAgentPblFilePath =
            agentInstallDir + TasBuilder.WIN_SEPARATOR + "wily" + TasBuilder.WIN_SEPARATOR + "core"
                + TasBuilder.WIN_SEPARATOR + "config" + TasBuilder.WIN_SEPARATOR + appServerPblFile;

        LOGGER.info("Updating file : {}", browserAgentPblFilePath);

        Map<String, String> replacePairs = new HashMap<String, String>();
        replacePairs.put("#browseragent.pbd", "browseragent.pbd");

        FileModifierFlowContext updatePblFlow =
            new FileModifierFlowContext.Builder().replace(browserAgentPblFilePath, replacePairs)
                .build();

        runFlow(aaClient, FileModifierFlow.class, updatePblFlow);
    }

    private void copyBrowserAgentJar(IAutomationAgentClient aaClient) {

        // TODO: REPLACE TasBuild.WIN_SEPARATOR with calls to getPathSeparator
        String browserAgentExtFromJar =
            agentInstallDir + TasBuilder.WIN_SEPARATOR + "wily" + TasBuilder.WIN_SEPARATOR
                + "extensions" + TasBuilder.WIN_SEPARATOR + "deploy" + TasBuilder.WIN_SEPARATOR
                + browserAgentExtJar;
        String browserAgentExtToJar =
            agentInstallDir + TasBuilder.WIN_SEPARATOR + "wily" + TasBuilder.WIN_SEPARATOR + "core"
                + TasBuilder.WIN_SEPARATOR + "ext" + TasBuilder.WIN_SEPARATOR + browserAgentExtJar;

        LOGGER.info("Copying file:", browserAgentExtFromJar, " TO " + browserAgentExtToJar);

        FileModifierFlowContext copyBrowserAgentExt =
            new FileModifierFlowContext.Builder()
                .copy(browserAgentExtFromJar, browserAgentExtToJar).build();

        runFlow(aaClient, FileModifierFlow.class, copyBrowserAgentExt);

    }

    private void updateProfile(IAutomationAgentClient aaClient) {

        LOGGER.info("Updating file : {}", browserAgentProfileFileFullPath);

        Map<String, String> replacePairs = new HashMap<String, String>();
        replacePairs.put("#introscope.agent.browseragent.enabled=false",
            "introscope.agent.browseragent.enabled=true");
        replacePairs.put("introscope.agent.browseragent.enabled=false",
            "introscope.agent.browseragent.enabled=true");
        replacePairs.put("#introscope.agent.browseragent.enabled=true",
            "introscope.agent.browseragent.enabled=true");
        replacePairs.put("introscope.agent.agentName=Tomcat Agent", "introscope.agent.agentName="
            + agentName);
        replacePairs.put("introscope.agent.customProcessName=Tomcat",
            "introscope.agent.customProcessName=" + agentProcessName);

        FileModifierFlowContext updateConfigFile =
            new FileModifierFlowContext.Builder().replace(browserAgentProfileFileFullPath,
                replacePairs).build();

        runFlow(aaClient, FileModifierFlow.class, updateConfigFile);

        /*
         * Map<String,String> configurationProps = new HashMap<String,String>();
         * 
         * configurationProps.put("introscope.agent.browseragent.enabled",
         * "true");
         * 
         * ConfigureFlowContext updateConfigFile = new
         * ConfigureFlowContext.Builder()
         * .configurationMap(browserAgentProfileFilePath, configurationProps)
         * .build();
         * 
         * runFlow(aaClient, ConfigureFlow.class, updateConfigFile);
         */
    }

    /**
     * Linux Builder responsible for holding all necessary properties to
     * instantiate {@link EnableBrowserAgentRole}
     */
    public static class LinuxBuilder extends Builder {

        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected String getDeployBase() {
            return getLinuxDeployBase();
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }
    }

    /**
     * Builder responsible for holding all necessary properties to instantiate
     * {@link EnableBrowserAgentRole}
     */
    public static class Builder extends BuilderBase<Builder, EnableBrowserAgentRole> {

        private final String roleId;

        @SuppressWarnings("unused")
        private final ITasResolver tasResolver;
        protected String agentInstallDir;
        protected String browserAgentExtJar;
        protected String browserAgentProfileFile;
        protected String appServerPblFile;
        private String agentName;
        private String agentProcessName;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public EnableBrowserAgentRole build() {

            EnableBrowserAgentRole instance = getInstance();
            Args.notNull(instance.agentInstallDir, "AGENT INSTALL DIR");
            Args.notNull(instance.appServerPblFile, "PBL FILE TO BE UPDATED");
            Args.notNull(instance.agentName, "AGENT NAME TO BE UPDATED");
            Args.notNull(instance.agentProcessName, "AGENT PROCESS NAME TO BE UPDATED");

            if (instance.browserAgentProfileFile == null) {
                instance.browserAgentProfileFile = "IntroscopeAgent.profile";
            }
            if (instance.browserAgentExtJar == null) {
                instance.browserAgentExtJar = "browser-agent-ext.tar.gz";
            }
            return instance;
        }

        @Override
        protected EnableBrowserAgentRole getInstance() {
            return new EnableBrowserAgentRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        public Builder agentInstallDir(String agentInstallDir) {
            this.agentInstallDir = agentInstallDir;
            return builder();
        }

        public Builder browserAgentExtJar(String browserAgentExtJar) {
            this.browserAgentExtJar = browserAgentExtJar;
            return builder();
        }

        public Builder browserAgentProfileFile(String browserAgentProfileFile) {
            this.browserAgentProfileFile = browserAgentProfileFile;
            return builder();
        }

        public Builder appServerPblFile(String appServerPblFile) {
            this.appServerPblFile = appServerPblFile;
            return builder();
        }

        public Builder agentName(String agentName) {
            this.agentName = agentName;
            return builder();
        }

        public Builder agentProcessName(String agentProcessName) {
            this.agentProcessName = agentProcessName;
            return builder();
        }

        protected String getPathSeparator() {
            return WIN_SEPARATOR;
        }

    }
}
