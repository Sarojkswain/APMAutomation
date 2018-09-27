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
import java.util.Arrays;
import java.util.Map;

import com.ca.apm.automation.action.flow.FlowConfig;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.systemtest.fld.artifact.thirdparty.FldAgentVersion;
import com.ca.apm.systemtest.fld.flow.DeployAgentFlow;
import com.ca.apm.systemtest.fld.flow.DeployAgentFlowContext;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.OperatingSystemFamily;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;

public class AgentRole extends AbstractRole {
    private final String webapps;
    private final String activeMqUrl;
    private final String fldControllUrl;
    private final OperatingSystemFamily operationSystem;

    /**
     * Agent download and start.
     * @param roleId
     * @param webapps
     * @param activeMqUrl
     * @param fldControllUrl
     */
    public AgentRole(String roleId, String webapps, String activeMqUrl, String fldControllUrl, OperatingSystemFamily operationSystem) {
        super(roleId);
        this.webapps =
            (webapps == null || webapps.isEmpty()) ? (operationSystem == OperatingSystemFamily.Linux ? "/opt/CA/lo-agent" : "C:\\sw\\agent") : webapps;
        this.activeMqUrl =
            (activeMqUrl == null || activeMqUrl.isEmpty()) ? "tcp://localhost:61616" : activeMqUrl;
        this.fldControllUrl =
            (fldControllUrl == null || fldControllUrl.isEmpty()) ? "localhost:8080" : fldControllUrl;
        this.operationSystem = operationSystem;
    }

    @Override
    public Map<String, String> getEnvProperties() {
        return properties;
    }

    @Override
    public void deploy(IAutomationAgentClient client) {
        URL agentUrl =
            client.getArtifactoryClient().getArtifactUrl(FldAgentVersion.snapshot.getArtifact());
        
        DeployAgentFlowContext context =
            new DeployAgentFlowContext(webapps, agentUrl, activeMqUrl, fldControllUrl, 
                operationSystem == OperatingSystemFamily.Windows);
        client.runJavaFlow(new FlowConfig.FlowConfigBuilder(DeployAgentFlow.class, context,
            getHostingMachine()
                .getHostnameWithPort()));
        
        File installPath = new File(webapps);
        RunCommandFlowContext cmdContext = null;
        if (operationSystem != OperatingSystemFamily.Linux) {
            cmdContext = new RunCommandFlowContext.Builder("start")
                .args(Arrays.asList("cmd", "/C", installPath + "/startAgent.bat"))
                .name(getRoleId()).build();
        }
        else {
            cmdContext = new RunCommandFlowContext.Builder("nohup")
            .args(Arrays.asList(webapps + "/startAgent.sh", "&"))
            .name(getRoleId()).build();
        }
        

        client.runJavaFlow(new FlowConfig.FlowConfigBuilder(RunCommandFlow.class, cmdContext,
            getHostingMachine()
                .getHostnameWithPort()));
    }
}
