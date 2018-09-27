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
import java.util.Collections;
import java.util.List;

import org.apache.http.util.Args;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.agent.AgentMonitoringOption;
import com.ca.apm.automation.action.flow.agent.ApplicationServerType;
import com.ca.apm.automation.action.flow.agent.DeployAgentNoinstFlow;
import com.ca.apm.automation.action.flow.agent.DeployAgentNoinstFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * Deployment of agent into distributed Websphere, installed before.
 */
public class WasAgentRole extends AbstractRole {
    private static final String AGENT_SUBDIR = "wily";
    private static final String MQAGENT_PROPERTIES_PATH =  "\\common\\MQAgent.properties";
    private static final String MQAGENT_PROPERTIES_APPENDIX =  "mq.crossprocesstracing.enabled = true";

    private URL agentUrl;
    private final String installDir;
    private final String agentDir;
    private final String emHost;
    private final int emPort;
    private final Was8Role wasRole;

    protected WasAgentRole(Builder builder) {
        super(builder.roleId);

        agentUrl =
            builder.tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.delivery",
                "agent-noinstaller-websphere-windows", "zip", builder.tasResolver
                    .getDefaultVersion()));

        installDir = builder.wasRole.getInstallDir();
        agentDir = installDir + "\\" + AGENT_SUBDIR;
        emHost = builder.emHost;
        emPort = builder.emPort;
        wasRole = builder.wasRole;
    }


    public void deploy(IAutomationAgentClient aaClient) {
        deployAgent(aaClient);

        wasRole.stopWas(aaClient);
        wasRole.configureAgent(aaClient);
        wasRole.startWas(aaClient);
    }

    private void deployAgent(IAutomationAgentClient aaClient) {
        List<AgentMonitoringOption> options =
            Arrays.asList(AgentMonitoringOption.PowerPackForWebSphereMQ,
                AgentMonitoringOption.CrossEnterpriseWsTracer);

        // Download, unpack and configure APM agent
        DeployAgentNoinstFlowContext agentCtx =
            new DeployAgentNoinstFlowContext.Builder().installerUrl(agentUrl)
                .installDir(installDir).applicationServerType(ApplicationServerType.WEBSPHERE)
                .intrumentationLevel(AgentInstrumentationLevel.TYPICAL).monitoringOptions(options)
                .setupEm(emHost, emPort).build();

        runFlow(aaClient, DeployAgentNoinstFlow.class, agentCtx, 300);

        // Additional agent configuration
        FileModifierFlowContext.Builder ctxBuilder = new FileModifierFlowContext.Builder();
        ctxBuilder.append(agentDir + MQAGENT_PROPERTIES_PATH, Collections.singleton(MQAGENT_PROPERTIES_APPENDIX));
        runFlow(aaClient, FileModifierFlow.class, ctxBuilder.build());
    }

    public String getInstallDir() {
        return installDir;
    }


    public static class Builder extends BuilderBase<Builder, WasAgentRole> {
        private final String roleId;
        private final ITasResolver tasResolver;

        private Was8Role wasRole = null;
        private String emHost = null;
        private int emPort = 5001;
        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public WasAgentRole build() {
            Args.notEmpty(emHost, "EM Host");
            WasAgentRole role = getInstance();

            return role;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected WasAgentRole getInstance() {
            return new WasAgentRole(this);
        }

        public Builder wasRole(Was8Role role) {
            wasRole = role;
            return builder();
        }

        public Builder emHost(String host) {
            emHost = host;
            return builder();
        }

        public Builder emPort(int port) {
            emPort = port;
            return builder();
        }
    }

}
