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
import com.ca.apm.systemtest.fld.artifact.thirdparty.ChromeBrowserVersion;
import com.ca.apm.systemtest.fld.flow.DeployArtifactFlow;
import com.ca.apm.systemtest.fld.flow.DeployArtifactFlowContext;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;

public class ChromeBrowserRole extends AbstractRole {
    private final String installdir;

    /**
     * Chrome browser install.
     * @param roleId
     * @param installdir
     */
    public ChromeBrowserRole(String roleId, String installdir) {
        super(roleId);
        this.installdir =
            (installdir == null || installdir.isEmpty()) ? "C:\\sw\\chromeinstaller.exe" : installdir;
    }

    @Override
    public Map<String, String> getEnvProperties() {
        return properties;
    }

    @Override
    public void deploy(IAutomationAgentClient client) {
        URL instUrl =
            client.getArtifactoryClient().getArtifactUrl(ChromeBrowserVersion.v13.getArtifact());
        
        DeployArtifactFlowContext context =
            new DeployArtifactFlowContext(new File(installdir), instUrl, false);
        client.runJavaFlow(new FlowConfig.FlowConfigBuilder(DeployArtifactFlow.class, context,
            getHostingMachine()
                .getHostnameWithPort()));
        
        RunCommandFlowContext cmdContext =
            new RunCommandFlowContext.Builder("start")
                .args(Arrays.asList("cmd", "/C", installdir, "/silent", "/install"))
                .name(getRoleId()).build();
        client.runJavaFlow(new FlowConfig.FlowConfigBuilder(RunCommandFlow.class, cmdContext,
            getHostingMachine()
                .getHostnameWithPort()));
        
    }
}
