/*
 * Copyright (c) 2016 CA. All rights reserved.
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
package com.ca.apm.tests.flow.netStockTrader;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * NetStockTraderFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class UndeployNetStockTraderFlow extends DeployNetStockTraderFlow {

    private static final Logger LOGGER = LoggerFactory.getLogger(UndeployNetStockTraderFlow.class);
    @FlowContext
    private NetStockTraderFlowContext context;

    public void run() throws IOException {
        try {
            File installDir = FileUtils.getFile(this.context.getDeploySourcesLocation());
            if (installDir.exists()) { // todo perform better check (site exists, apppool exists ..)
                // the order of deployment is mandatory for the installation to succeed
                deleteSite(DEFAULT_STOCKTRADER_SITE_NAME);
                deleteAppPool(DEFAULT_STOCKTRADER_APP_POOL_NAME);
                rmDir();
            }
        } catch (InterruptedException var3) {
            throw new IllegalStateException(var3);
        }
    }

    protected void deleteSite(String siteName) throws InterruptedException {
        runAppcmd(new String[]{"delete", "site", "/site.name:" + siteName});
    }

    protected void deleteAppPool(String poolName) throws InterruptedException {
        runAppcmd(new String[]{"delete", "apppool", "/apppool.name:" + poolName});
    }

    protected void rmDir() throws InterruptedException, IOException {
        LOGGER.info("Deleting installation dir at '" + this.context.getDeploySourcesLocation() + "'");
        File installDir = FileUtils.getFile(this.context.getDeploySourcesLocation());
        if (installDir.exists()) {
            FileUtils.deleteDirectory(installDir);
        }
    }
}
