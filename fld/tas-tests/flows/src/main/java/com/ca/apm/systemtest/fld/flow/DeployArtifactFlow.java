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

package com.ca.apm.systemtest.fld.flow;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;

@Flow
public class DeployArtifactFlow extends FlowBase implements IAutomationFlow {
    private static final Logger log = LoggerFactory.getLogger(DeployArtifactFlow.class);

    @FlowContext
    private DeployArtifactFlowContext context;

    @Override
    public void run() throws Exception {
        if (context.getUnpack()) {
            archiveFactory.createArchive(context.getArtifactUrl()).unpack(context.getInstallDir());
        }
        else {
            archiveFactory.createArtifact(context.getArtifactUrl()).download(context.getInstallDir());
        }

        log.info("Artifact {} has been deployed into {}", context.getArtifactUrl(),
                context.getInstallDir());
    }

    protected void clearTargetInstallationFolder(File targetInstallationFolder) throws IOException {
        if (targetInstallationFolder.exists()) {
            log.info("Deleting folder {}", targetInstallationFolder.getAbsolutePath());
            FileUtils.deleteDirectory(targetInstallationFolder);
        }
    }
}
