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

import java.net.URL;

import com.ca.apm.automation.action.flow.IFlowContext;

public class DeployAgentFlowContext implements IFlowContext {
    private final URL artifactUrl;
    private final String installDir;
    private final String activeMqUrl;
    private final String fldControllUrl;
    private final boolean isWindows;

    public DeployAgentFlowContext(String installDir, URL artifactUrl, String activeMqUrl, String fldControllUrl, boolean isWindows) {
        this.installDir = installDir;
        this.artifactUrl = artifactUrl;
        this.activeMqUrl = activeMqUrl;
        this.fldControllUrl = fldControllUrl;
        this.isWindows = isWindows;
    }

    public String getInstallDir() {
        return installDir;
    }

    public URL getArtifactUrl() {
        return artifactUrl;
    }
    
    public String getActiveMqUrl() {
        return activeMqUrl;
    }
    
    public String getFldControllUrl() {
        return fldControllUrl;
    }
    
    public boolean isWindows() {
        return isWindows;
    }
}
