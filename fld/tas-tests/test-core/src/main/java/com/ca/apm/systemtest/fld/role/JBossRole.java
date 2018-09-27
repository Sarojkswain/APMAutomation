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

import com.ca.apm.automation.action.flow.FlowConfig;
import com.ca.apm.systemtest.fld.artifact.thirdparty.JBossVersion;
import com.ca.apm.systemtest.fld.flow.DeployArtifactFlow;
import com.ca.apm.systemtest.fld.flow.DeployArtifactFlowContext;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;

public class JBossRole extends AbstractRole {
    private final String rootPath;

    public JBossRole(String roleId, String rootPath) {
        super(roleId);
        this.rootPath = (rootPath == null || rootPath.isEmpty()) ? "C:/sw" : rootPath;
    }

    @Override
    public Map<String, String> getEnvProperties() {
        return properties;
    }

    @Override
    public void deploy(IAutomationAgentClient client) {
        JBossVersion artifact = JBossVersion.v711;
        URL artifactUrl = client.getArtifactoryClient().getArtifactUrl(artifact.getArtifact());

        DeployArtifactFlowContext context =
            new DeployArtifactFlowContext(new File(rootPath), artifactUrl);

        client.runJavaFlow(new FlowConfig.FlowConfigBuilder(DeployArtifactFlow.class, context,
            getHostingMachine()
                .getHostnameWithPort()));


        File installPath = new File(rootPath, "jboss-as-7.1.1.Final");
        properties.put("home", installPath.toString());
    }
}
