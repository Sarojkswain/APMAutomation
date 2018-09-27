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

package com.ca.apm.automation.action.flow.testapp;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;

/**
 * Deploy the distribution of <b>Trade Service application</b> into provided web container.
 * 
 * @author Pospichal, Pavel <pospa02@ca.com>
 */
@Flow
public class DeployTradeServiceAppFlow extends FlowBase {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @FlowContext
    private DeployTradeServiceAppFlowContext context;

    @Override
    public void run() throws Exception {

        try {
            File stagedDistributionArifact =
                    downloadDistributionArtifact(context.getTradeServiceAppArtifactURL(),
                            context.getStagingDir());
            deployWebApps(context.getInstallDir(), stagedDistributionArifact);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to deploy Trade service application", e);
        }
        LOGGER.info("Trade service application has been deployed into {}", context.getInstallDir());
    }

    private File downloadDistributionArtifact(URL artifactURL, File stagingDir)
            throws IOException {

        File stagedDistributionArifact = new File(stagingDir, artifactURL.getFile());

        FileUtils.forceMkdir(stagedDistributionArifact.getParentFile());

        if (stagedDistributionArifact.exists()) {
            LOGGER.info("Distribution is already downloaded");
        } else {
            LOGGER.info("Downloading from {}", artifactURL);
            getArchiveFactory().createArtifact(artifactURL).download(stagedDistributionArifact);
            LOGGER.info("Download completed.");
        }

        return stagedDistributionArifact;
    }

    private void deployWebApps(File targetInstallationFolder, File stagedEarDistributionArifact)
            throws Exception {
        // TODO pospa02: if there is support for Java EE in target environment install the EAR
        // directly
        File stagedZipDistributionArifact =
                new File(stagedEarDistributionArifact.getParentFile(), stagedEarDistributionArifact
                        .getName().replace(".ear", ".zip"));
        FileUtils.copyFile(stagedEarDistributionArifact, stagedZipDistributionArifact);

        File stagedDistributionArifact =
                new File(stagedEarDistributionArifact.getParentFile(), "unpacked");
        getArchiveFactory().createArchive(stagedZipDistributionArifact).unpack(
                stagedDistributionArifact);

        FileUtils.copyDirectory(stagedDistributionArifact, targetInstallationFolder,
                FileFilterUtils.suffixFileFilter(".war"));
        // TODO pospa02: replace previous by following once support made in TAS core
        // getArchiveFactory().createArchive(stagedEarDistributionArifact).unpack(stagedDistributionArifact,
        // FileFilterUtils.suffixFileFilter(".war"));
    }
}
