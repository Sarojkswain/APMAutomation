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

package com.ca.apm.systemtest.atcqa.role;

import org.jetbrains.annotations.NotNull;

import com.ca.apm.automation.action.flow.FlowConfig;
import com.ca.apm.systemtest.atcqa.flow.DeployManagementModuleFlow;
import com.ca.apm.systemtest.atcqa.flow.DeployManagementModuleFlowContext;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;

/**
 * Install a management module to EM.
 * 
 * @author Korcak, Zdenek <korzd01@ca.com>
 * 
 */
public class ManagementModuleRole extends AbstractRole {

    @NotNull
    private final String mmPathName;
    private final String emInstallDir;

    public ManagementModuleRole(String roleId, String mmPathName, String emInstallDir) {
        super(roleId);
        this.mmPathName = mmPathName;
        this.emInstallDir = emInstallDir;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        aaClient.runJavaFlow(new FlowConfig.FlowConfigBuilder(DeployManagementModuleFlow.class,
            new DeployManagementModuleFlowContext(mmPathName, emInstallDir), getHostingMachine()
                .getHostnameWithPort()));
    }

}
