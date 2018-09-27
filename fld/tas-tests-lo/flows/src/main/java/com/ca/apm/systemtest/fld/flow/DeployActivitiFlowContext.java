/*
 * Copyright (c) 2014 CA. All rights reserved.
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

package com.ca.apm.systemtest.fld.flow;

import java.io.File;
import java.net.URL;

import com.ca.apm.automation.action.flow.IFlowContext;

public class DeployActivitiFlowContext implements IFlowContext {
    private final File webappsDir;
    private URL orchestratorUrl;
    private URL agentUrl;

    /**
     * KP. 
     * 
     * @param webappsDir Tomcat webapp folder
     * @param orchestratorUrl load-orchestrator-webapp artifact location
     * @param agentUrl Url agent-dist artifact location
     */
    public DeployActivitiFlowContext(File webappsDir, URL orchestratorUrl, URL agentUrl) {
        this.webappsDir = webappsDir;
        this.orchestratorUrl = orchestratorUrl;
        this.agentUrl = agentUrl;
    }

    public File getWebapsDir() {
        return webappsDir;
    }

    public URL getOrchestratorUrl() {
        return orchestratorUrl;
    }

    public URL getAgentUrl() {
        return agentUrl;
    }
}
