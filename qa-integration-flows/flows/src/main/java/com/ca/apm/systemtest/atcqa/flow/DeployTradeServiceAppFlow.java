/*
 * Copyright (c) 2014 CA. All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.systemtest.atcqa.flow;

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
            File stagedDistributionArifact;

            // TradeService
            stagedDistributionArifact =
                downloadDistributionArtifact(context.getTradeServiceAppArtifactURL(),
                    context.getStagingDir(), "TradeService.war");
            deployWebApps(context.getInstallDir(), stagedDistributionArifact);

            // ReportingService
            stagedDistributionArifact =
                downloadDistributionArtifact(context.getReportingServiceAppArtifactURL(),
                    context.getStagingDir(), "ReportingService.war");
            deployWebApps(context.getInstallDir(), stagedDistributionArifact);

            // OrderEngine
            stagedDistributionArifact =
                downloadDistributionArtifact(context.getOrderEngineAppArtifactURL(),
                    context.getStagingDir(), "OrderEngine.war");
            deployWebApps(context.getInstallDir(), stagedDistributionArifact);

        } catch (Exception e) {
            throw new IllegalStateException("Unable to deploy Trade service application", e);
        }
        LOGGER.info("Trade service application has been deployed into {}", context.getInstallDir());
    }

    private File downloadDistributionArtifact(URL artifactURL, File stagingDir, String warName)
        throws IOException {
        File stagedDistributionArifact = new File(stagingDir, warName);
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
        File stagedZipDistributionArifact =
            new File(stagedEarDistributionArifact.getParentFile(), stagedEarDistributionArifact
                .getName().replace(".ear", ".zip"));
        File stagedDistributionArifact;
        if (stagedEarDistributionArifact.equals(stagedZipDistributionArifact)) {
            stagedDistributionArifact = stagedEarDistributionArifact;
            FileUtils.copyDirectory(stagedDistributionArifact.getParentFile(),
                targetInstallationFolder, FileFilterUtils.suffixFileFilter(".war"));
        } else {
            FileUtils.copyFile(stagedEarDistributionArifact, stagedZipDistributionArifact);
            stagedDistributionArifact =
                new File(stagedEarDistributionArifact.getParentFile(), "unpacked");
            getArchiveFactory().createArchive(stagedZipDistributionArifact).unpack(
                stagedDistributionArifact);
            FileUtils.copyDirectory(stagedDistributionArifact, targetInstallationFolder,
                FileFilterUtils.suffixFileFilter(".war"));
        }
    }

}
