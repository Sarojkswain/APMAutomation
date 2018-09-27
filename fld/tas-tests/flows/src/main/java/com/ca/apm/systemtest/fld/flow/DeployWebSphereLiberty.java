package com.ca.apm.systemtest.fld.flow;

import java.io.File;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;

/**
 * @author haiva01
 */
@Flow
public class DeployWebSphereLiberty extends FlowBase {
    private static final Logger log = LoggerFactory.getLogger(DeployWebSphereLiberty.class);

    @FlowContext
    DeployWebSphereLibertyContext flowContext;

    @Override
    public void run() throws Exception {
        String wlpArtifactUrlStr = flowContext.getWlpArtifactUrl();
        URL mavenArtifactUrl = new URL(wlpArtifactUrlStr);
        String destDir = flowContext.getDestDir();
        File destDirFile = new File(destDir);
        archiveFactory.createArchive(mavenArtifactUrl).unpack(destDirFile);

        File wlpDir = new File(destDirFile, "wlp");
        log.info("WebSphere Liberty was extracted into {}.", wlpDir.getAbsolutePath());
    }
}
