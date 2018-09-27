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

public class DeployArtifactFlowContext implements IFlowContext {
    private final URL artifactUrl;
    private final File installDir;
    private final boolean unpack;   //unpack downloaded Artifact or just download 

    public DeployArtifactFlowContext(File installDir, URL artifactUrl) {
        this.installDir = installDir;
        this.artifactUrl = artifactUrl;
        this.unpack = true;
    }
    
    public DeployArtifactFlowContext(File installDir, URL artifactUrl, boolean unpack) {
        this.installDir = installDir;
        this.artifactUrl = artifactUrl;
        this.unpack = unpack;
    }

    public File getInstallDir() {
        return installDir;
    }

    public URL getArtifactUrl() {
        return artifactUrl;
    }
    
    public boolean getUnpack() {
        return unpack;
    }
}
