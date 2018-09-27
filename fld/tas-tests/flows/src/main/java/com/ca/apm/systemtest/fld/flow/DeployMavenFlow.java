package com.ca.apm.systemtest.fld.flow;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;

/**
 * @author haiva01
 */
@Flow
public class DeployMavenFlow extends FlowBase {
    private static final Logger log = LoggerFactory.getLogger(DeployMavenFlow.class);
    private static final String SETTINGS_XML_RESOURCE
        = "/com/ca/apm/systemtest/fld/flow/maven/settings.xml";

    @FlowContext
    DeployMavenFlowContext flowContext;

    @Override
    public void run() throws Exception {
        String mavenArtifactUrlStr = flowContext.getMavenArtifactUrl();
        URL mavenArtifactUrl = new URL(mavenArtifactUrlStr);
        String destFileName = new File(mavenArtifactUrl.getPath()).getName();
        File destFileNameFile = new File(destFileName);
        archiveFactory.createArtifact(mavenArtifactUrl).download(destFileNameFile);
        archiveFactory.createArchive(destFileNameFile).unpack(new File(flowContext.getDestDir()));
        if (!FileUtils.deleteQuietly(destFileNameFile)) {
            log.error("Failed to delete archive {}.", destFileNameFile.getAbsolutePath());
        }
        InputStream settingsXmlStream
            = ClassLoader.class.getResourceAsStream(SETTINGS_XML_RESOURCE);
        File settingsXmlFile = Paths.get(flowContext.getM2Home(), "conf", "settings.xml")
            .toAbsolutePath().toFile();
        FileUtils.copyInputStreamToFile(settingsXmlStream, settingsXmlFile);
    }
}
