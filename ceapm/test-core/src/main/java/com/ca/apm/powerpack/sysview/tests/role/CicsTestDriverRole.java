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

package com.ca.apm.powerpack.sysview.tests.role;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.util.Args;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.agent.ApplicationServerType;
import com.ca.apm.automation.action.flow.agent.DeployAgentNoinstFlow;
import com.ca.apm.automation.action.flow.agent.DeployAgentNoinstFlowContext;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.java.DeployJavaFlow;
import com.ca.apm.automation.action.flow.java.DeployJavaFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

public class CicsTestDriverRole extends AbstractRole {
    public static final String INSTALL_DIR_PROPERTY = "ctd.install.dir";
    public static final String AGENT_DIR_PROPERTY = "ctd.agent.dir";
    private static final String INSTALL_DIR_DEFAULT =
        "C:\\automation\\deployed\\tools\\CICSTestDriver";
    // The wily suffix is hard-coded in the Agent flow
    private static final String AGENT_SUBDIR = "wily";

    private URL ctdUrl;
    private URL agentUrl;
    private final String installDir;
    private final String agentDir;
    private final String emHost;
    private final int emPort;

    protected CicsTestDriverRole(Builder builder) {
        super(builder.roleId);

        installDir = builder.installDir;
        agentDir = installDir + "\\" + AGENT_SUBDIR;
        ctdUrl = builder.tasResolver.getArtifactUrl(new DefaultArtifact(
            "com.ca.apm.powerpack.sysview.tests", "ceapm.tools.cicstestdriver", "dist", "zip",
            builder.tasResolver.getDefaultVersion()));
        agentUrl = builder.tasResolver.getArtifactUrl(new DefaultArtifact(
            "com.ca.apm.delivery", "agent-noinstaller-default-windows", "zip",
            builder.tasResolver.getDefaultVersion()));
        emHost = builder.emHost;
        emPort = builder.emPort;

        addProperty(INSTALL_DIR_PROPERTY, installDir);
        addProperty(AGENT_DIR_PROPERTY, agentDir);
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        Map<String, String> agentProperties = new HashMap<String, String>();
        agentProperties.put("com.wily.introscope.agent.httpheaderinsertion.enabled", "true");
        agentProperties.put("introscope.ctg.tracer.inject.guid", "true");
        agentProperties.put("introscope.agent.transactiontracer.tailfilterPropagate.enable", "true");
        agentProperties.put("ceapm.ipic.supported", "true");
        agentProperties.put("ceapm.commarea.sampling.supported", "true");
        agentProperties.put("ceapm.commarea.program.name.regex", ".*");
        agentProperties.put("ceapm.nodata.program.name.regex", ".*");
        agentProperties.put("ceapm.channel.program.name.regex", ".*");
        agentProperties.put("introscope.autoprobe.directivesFile",
            "default-typical.pbl,CTG_ECI_Tracer_For_SYSVIEW.pbd,CICSTestDriver.pbd");

        // Download and unpack CICSTestDriver
        DeployJavaFlowContext ctgCtx = new DeployJavaFlowContext.Builder()
            .artifactUrl(ctdUrl)
            .dir(installDir)
            .build();
        runFlow(aaClient, DeployJavaFlow.class, ctgCtx, 300);

        // Download, unpack, and configure the APM Agent
        DeployAgentNoinstFlowContext agentCtx = new DeployAgentNoinstFlowContext.Builder()
            .installerUrl(agentUrl)
            .installDir(installDir)
            .applicationServerType(ApplicationServerType.DEFAULT)
            .intrumentationLevel(AgentInstrumentationLevel.TYPICAL)
            .additionalProps(agentProperties)
            .setupEm(emHost, emPort)
            .build();
        runFlow(aaClient, DeployAgentNoinstFlow.class, agentCtx, 300);

        // Add the CICSTestDriver custom pbd to the agent
        RunCommandFlowContext copyCtx = new RunCommandFlowContext.Builder("copy")
            .args(Arrays.asList("/Y", installDir + "\\pbd\\*.pbd ", agentDir + "\\core\\config"))
            .name("Copy CICSTestDriver PBDs")
            .build();
        runFlow(aaClient, RunCommandFlow.class, copyCtx);

        // Add necessary extensions
        copyCtx = new RunCommandFlowContext.Builder("copy")
            .args(Arrays.asList("/Y", agentDir + "\\examples\\Cross-Enterprise_APM\\ext\\*.jar",
                agentDir + "\\core\\ext"))
            .name("Copy CEAPM extensions")
            .build();
        runFlow(aaClient, RunCommandFlow.class, copyCtx);
    }

    public static class Builder extends BuilderBase<Builder, CicsTestDriverRole> {
        private final String roleId;
        private final ITasResolver tasResolver;

        private String installDir = INSTALL_DIR_DEFAULT;
        private String emHost = null;
        private int emPort = 5001;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public CicsTestDriverRole build() {
            Args.notEmpty(emHost, "EM Host");
            CicsTestDriverRole role = getInstance();
            return role;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected CicsTestDriverRole getInstance() {
            return new CicsTestDriverRole(this);
        }

        public Builder emHost(String host) {
            emHost = host;
            return builder();
        }

        public Builder emPort(int port) {
            emPort = port;
            return builder();
        }

        public Builder installDir(String installDir) {
            this.installDir = installDir;
            return builder();
        }
    }
}
