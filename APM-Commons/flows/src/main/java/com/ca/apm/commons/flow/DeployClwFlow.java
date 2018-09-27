package com.ca.apm.commons.flow;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static org.apache.commons.io.FileUtils.cleanDirectory;

/**
 * Created by nick on 15.10.14.
 */
@Flow
public class DeployClwFlow implements IAutomationFlow {

    private static final Logger LOG = LoggerFactory.getLogger(DeployClwFlow.class);

    @FlowContext
    private DeployClwFlowContext context;

    @Override
    public void run() throws Exception {
        deleteInstallDirectory();

        downloadArtifact();
    }

    /* Non-public methods */

    private void deleteInstallDirectory() throws IOException {

        File installDirectory = new File(context.getInstallDir());

        if (installDirectory.exists()) {
            LOG.info("Cleaning install directory: " + context.getInstallDir());
            cleanDirectory(installDirectory);

            LOG.info("Deleting install directory: " + context.getInstallDir());
            if (!installDirectory.delete()) {
                throw new RuntimeException("Cannot delete directory: " + installDirectory.getPath());
            }
        }
    }

    private void downloadArtifact() {
        try {
            FileUtils.copyURLToFile(context.getClwUrl(), clwDestination());
        } catch (IOException e) {
            throw new RuntimeException("Cannot download CommandLineWorkstation: " + context.getClwUrl().toString(), e);
        }
    }

    private File clwDestination() {
        return new File(ensureTrailingBackslash(context.getInstallDir()) + context.getClwLocalFilename());
    }

    private String ensureTrailingBackslash(String input) {
        return input.endsWith("\\") ? input : input + "\\";
    }
}
