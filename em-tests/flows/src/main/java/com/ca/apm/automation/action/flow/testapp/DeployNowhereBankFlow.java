/*
 * Copyright (c) 2015 CA.  All rights reserved.
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

package com.ca.apm.automation.action.flow.testapp;

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
 * Deploy the distribution of <b>NowhereBank application</b>.
 * 
 * @author Korcak, Zdenek <korzd01@ca.com>
 */
@Flow
public class DeployNowhereBankFlow extends FlowBase {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @FlowContext
    private DeployNowhereBankFlowContext context;

    @Override
    public void run() throws Exception {

        try {
            clearTargetInstallationFolder(context.getInstallDir());

            unpackDistributionIntoTargetInstallationDir(context.getNowhereBankArtifactURL(),
                                                        context.getInstallDir());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to deploy Nowherebank", e);
        }
        LOGGER.info("Nowherebank has been deployed into {}", context.getInstallDir());
    }

    private void clearTargetInstallationFolder(File targetInstallationFolder) throws IOException {
        if (targetInstallationFolder.exists()) {
            LOGGER.info("Deleting folder {}", targetInstallationFolder.getAbsolutePath());
            FileUtils.deleteDirectory(targetInstallationFolder);
        }
    }

    private void unpackDistributionIntoTargetInstallationDir(URL distributionArtifactLocation, File targetInstallationFolder) throws Exception {
        File unpackedDistroArtDir = context.getStagingDir();
        unpackedDistroArtDir.deleteOnExit();

        getArchiveFactory().createArchive(distributionArtifactLocation).unpack(unpackedDistroArtDir);

        File[] unpackedArtifactFiles = unpackedDistroArtDir.listFiles();
        if (unpackedArtifactFiles == null) {
            throw new IllegalArgumentException("Unpacked directory contains no files (" + unpackedDistroArtDir + ").");
        }
        if ((unpackedArtifactFiles.length == 1) && unpackedArtifactFiles[0].isDirectory()) {
            unpackedDistroArtDir = unpackedArtifactFiles[0];
            FileUtils.moveDirectory(unpackedDistroArtDir, targetInstallationFolder);
            LOGGER.debug("Folder {} moved to {}", unpackedDistroArtDir.getAbsolutePath(), targetInstallationFolder.getAbsolutePath());
        } else {
            FileUtils.forceMkdir(targetInstallationFolder);
            for (File tradeServiceAppContentItem : unpackedArtifactFiles) {
                FileUtils.moveToDirectory(tradeServiceAppContentItem, targetInstallationFolder, false);
            }
            LOGGER.debug("Content of folder {} moved to {}",
                      unpackedDistroArtDir.getAbsolutePath(),
                      targetInstallationFolder.getAbsolutePath());
        }
    }
}
