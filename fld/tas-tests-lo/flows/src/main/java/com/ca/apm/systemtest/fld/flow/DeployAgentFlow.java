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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;

@Flow
public class DeployAgentFlow extends FlowBase implements IAutomationFlow {
    private static final Logger log = LoggerFactory.getLogger(DeployAgentFlow.class);

    @FlowContext
    private DeployAgentFlowContext context;

    @Override
    public void run() throws Exception {
        archiveFactory.createArchive(context.getArtifactUrl()).unpack(new File(context.getInstallDir()));
        
        File source = new File(context.getInstallDir() + "/fld-agent.properties");
        File target = new File(context.getInstallDir() + "/fld-agent-production.properties");
        
        Files.move(source.toPath(), target.toPath());
        
        Properties prop = new Properties();
        prop.load(new FileReader(target));
        prop.setProperty("activemq.broker.url", context.getActiveMqUrl());
        //prop.setProperty("fld.file.download.cache.url", 
        //    "http://"+context.getFldControllUrl()+"/LoadOrchestrator/filecache/download");
        prop.setProperty("agent.download.path", "/orchestrator/api/agent");
        prop.setProperty("agent.download.server", context.getFldControllUrl());
        prop.store(new FileWriter(target), null);
        
        File script = new File(context.getInstallDir() + "/startAgent.sh");
        script.setExecutable(true);

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
