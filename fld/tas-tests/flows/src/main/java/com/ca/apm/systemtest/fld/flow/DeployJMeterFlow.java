package com.ca.apm.systemtest.fld.flow;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;

/**
 * @author Boch, Tomas (bocto01@ca.com)
 */
@Flow
public class DeployJMeterFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployJMeterFlow.class);

    @FlowContext
    protected DeployJMeterFlowContext context;

    @Override
    public void run() throws Exception {
        File jmeterInstallDir = new File(context.getJMeterInstallDir());
        File scriptsInstallDir = new File(jmeterInstallDir, "scripts");
        try {
            clearTargetInstallationFolder(jmeterInstallDir);
            downloadAndUnpack(context.getJMeterBinariesArtifactURL(), jmeterInstallDir);
            if (context.getJMeterScriptsArtifactURL() != null) {
                downloadAndUnpack(context.getJMeterScriptsArtifactURL(), scriptsInstallDir);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to deploy Apache JMeter", e);
        }
        LOGGER.info("Apache JMeter has been deployed into {}", jmeterInstallDir);
    }

    protected void clearTargetInstallationFolder(File targetInstallationFolder) throws IOException {
        if (!targetInstallationFolder.exists()) {
            return;
        }
        LOGGER.info("Deleting folder {}", targetInstallationFolder.getAbsolutePath());
        FileUtils.deleteDirectory(targetInstallationFolder);
    }

    protected void downloadAndUnpack(URL fromLocation, File destinationFolder) throws IOException {
        archiveFactory.createArchive(fromLocation).unpack(destinationFolder);
        dirHandlerFactory.create(destinationFolder).explodeIfImploded();
    }

}
