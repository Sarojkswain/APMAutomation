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
import java.nio.file.Paths;
import java.util.Map;

import com.ca.apm.automation.action.flow.FlowConfig;
import com.ca.apm.systemtest.fld.artifact.thirdparty.ActivitiVersion;
import com.ca.apm.systemtest.fld.artifact.thirdparty.FldAgentVersion;
import com.ca.apm.systemtest.fld.flow.DeployActivitiFlow;
import com.ca.apm.systemtest.fld.flow.DeployActivitiFlowContext;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;

public class ActivitiRole extends AbstractRole {
    public static final String AGENT_DIST = "agent_dist_zip";
    private final String webapps;

    /**
     * Activiti artifact + agent zip file deploy.
     * @param roleId
     * @param webapps
     */
    public ActivitiRole(String roleId, String webapps) {
        super(roleId);
        this.webapps =
            (webapps == null || webapps.isEmpty()) ? "C:\\sw\\wily\\tomcat\\webapps" : webapps;
    }

    @Override
    public Map<String, String> getEnvProperties() {
        return properties;
    }

    @Override
    public void deploy(IAutomationAgentClient client) {
        URL orchestratorUrl =
            client.getArtifactoryClient().getArtifactUrl(ActivitiVersion.snapshot.getArtifact());
        URL agentUrl =
            client.getArtifactoryClient().getArtifactUrl(FldAgentVersion.snapshot.getArtifact());

        DeployActivitiFlowContext context =
            new DeployActivitiFlowContext(new File(webapps), orchestratorUrl, agentUrl);
        client.runJavaFlow(new FlowConfig.FlowConfigBuilder(DeployActivitiFlow.class, context,
            getHostingMachine()
                .getHostnameWithPort()));

        properties.put(AGENT_DIST, Paths.get(webapps).getParent().getParent().toString() + "\\"
            + DeployActivitiFlow.AGENT_DIST);
    }
}
