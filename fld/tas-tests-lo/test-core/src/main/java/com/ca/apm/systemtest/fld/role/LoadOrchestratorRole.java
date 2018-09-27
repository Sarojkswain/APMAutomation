/*
 * Copyright (c) 2014 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and proprietary and shall not
 * be duplicated, used, disclosed or disseminated in any way except as authorized by the applicable
 * license agreement, without the express written permission of CA. All authorized reproductions
 * must be marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT PERMITTED BY APPLICABLE
 * LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF ANY KIND, INCLUDING WITHOUT LIMITATION, ANY
 * IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR INDIRECT, FROM THE
 * USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST PROFITS, BUSINESS INTERRUPTION,
 * GOODWILL, OR LOST DATA, EVEN IF CA IS EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.systemtest.fld.role;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ca.apm.automation.action.flow.FlowConfig;
import com.ca.apm.systemtest.fld.artifact.thirdparty.ActivitiVersion;
import com.ca.apm.systemtest.fld.flow.DeployArtifactFlow;
import com.ca.apm.systemtest.fld.flow.DeployArtifactFlowContext;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;

public class LoadOrchestratorRole extends AbstractRole {
    private static final int ASYNC_DELAY = 15;
    private final String webapps;

    /**
     * LoadOrchestrator deploy.
     *
     * @param roleId
     * @param webapps
     */
    public LoadOrchestratorRole(String roleId, String webapps) {
        super(roleId);
        this.webapps = StringUtils.isBlank(webapps)
            ? "C:\\sw\\wily\\tomcat\\webapps\\LoadOrchestrator.war"
            : webapps;
    }

    @Override
    public Map<String, String> getEnvProperties() {
        return properties;
    }

    @Override
    public void deploy(IAutomationAgentClient client) {
        ActivitiVersion artifact = ActivitiVersion.snapshot;
        URL orchestratorUrl =
            client.getArtifactoryClient().getArtifactUrl(artifact.getArtifact());

        DeployArtifactFlowContext context =
            new DeployArtifactFlowContext(new File(webapps), orchestratorUrl, false);
        client.runJavaFlow(new FlowConfig.FlowConfigBuilder(DeployArtifactFlow.class, context,
            getHostingMachine()
                .getHostnameWithPort()).delay(ASYNC_DELAY).async());
    }
}
