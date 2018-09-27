/*
 * Copyright (c) 2014 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.transactiontrace.appmap.flow;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Flow for downloading and unpacking Nowhere bank application
 *
 * @author bhusu01
 */
@Flow
public class NowhereBankFlow extends FlowBase {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(NowhereBankFlow.class);

    @FlowContext
    private NowhereBankFlowContext context;

    @Override
    public void run() throws Exception {
        File artifactFile =
            fetchDistribution(context.getArtifactURL(), context.getInstallDirectory());
        File nowWhereBankDirectory = new File(context.getExtractionDirectory());
        unpackDistribution(artifactFile, nowWhereBankDirectory);
    }

    private File fetchDistribution(URL artifactURL, String installDir) throws IOException {
        String artifactBasename = FilenameUtils.getBaseName(artifactURL.getPath() + ".zip");
        File appArtifact = new File(installDir, artifactBasename);

        LOGGER.info("Checking if file {} already exists in directory", artifactBasename);
        if (appArtifact.exists()) {
            LOGGER.info("File already exists, download is not required");
        } else {
            LOGGER.info("Downloading from {}", artifactURL);
            FileUtils.copyURLToFile(artifactURL, appArtifact);
            LOGGER.info("Download completed.");
        }

        return appArtifact;
    }

    private void unpackDistribution(File distributionArtifact, File extractionDir) throws Exception {
        String basename = FilenameUtils.getBaseName(distributionArtifact.getPath());
        /**
         * leverage java.nio.file.Files#createTempDirectory() if we are sure
         * java 1.7 is provided
         */
        File unpackedDistribArtifactDir =
            org.codehaus.plexus.util.FileUtils.createTempFile(basename, ".zip", new File(context.getInstallDirectory()));
        FileUtils.forceMkdir(unpackedDistribArtifactDir);
        unpackedDistribArtifactDir.deleteOnExit();

        LOGGER.debug("Unzipping to {}", unpackedDistribArtifactDir.getAbsolutePath());
        getArchiveFactory().createArchive(distributionArtifact).unpack(unpackedDistribArtifactDir);

        if (unpackedDistribArtifactDir.listFiles().length == 1
            && unpackedDistribArtifactDir.listFiles()[0].isDirectory()) {
            unpackedDistribArtifactDir = unpackedDistribArtifactDir.listFiles()[0];
            FileUtils.moveDirectory(unpackedDistribArtifactDir, extractionDir);
            LOGGER.debug("Folder {} moved to {}", unpackedDistribArtifactDir.getAbsolutePath(), extractionDir.getAbsolutePath());
        } else {
            FileUtils.forceMkdir(extractionDir);
            for (File tradeServiceAppContentItem : unpackedDistribArtifactDir.listFiles()) {
                FileUtils.moveToDirectory(tradeServiceAppContentItem, extractionDir, false);
            }
            LOGGER.debug("Content of folder {} moved to {}", unpackedDistribArtifactDir.getAbsolutePath(), extractionDir.getAbsolutePath());
        }
    }
}
