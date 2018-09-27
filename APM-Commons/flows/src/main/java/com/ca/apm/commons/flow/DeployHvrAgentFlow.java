package com.ca.apm.commons.flow;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.NotDirectoryException;

import static org.apache.commons.io.FileUtils.cleanDirectory;

/**
 * Created by nick on 1.10.14.
 */
@Flow
public class DeployHvrAgentFlow extends FlowBase {

    private static final Logger LOG = LoggerFactory.getLogger(DeployHvrAgentFlow.class);

    @FlowContext
    private DeployHvrAgentFlowContext context;

    @Override
    public void run() throws Exception {

        deleteInstallDirectory();

        cleanStageDirectory();

        downloadAndExtractArtifact();
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

    private void cleanStageDirectory() throws IOException {

        File stagingPath = new File(context.getStagingDir());

        ensurePath(stagingPath);
        cleanDirectory(stagingPath);
    }

    private void downloadAndExtractArtifact() throws IOException {
        getArchiveFactory().createArchive(context.getHvrAgentUrl()).unpack(
            new File(context.getInstallDir()));
    }

    private void ensurePath(File path) throws IOException {

        if (!path.exists()) {
            if (!path.mkdir()) {
                throw new IOException("Could not make directory: " + path);
            }
        } else if (!path.isDirectory()) {
            // Exists, but isn't a directory
            throw new NotDirectoryException("The specified path exists but is not a directory: " + path);
        }
    }
}
