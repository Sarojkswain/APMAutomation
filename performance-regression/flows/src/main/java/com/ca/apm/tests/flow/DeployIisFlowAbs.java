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
package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NetStockTraderFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public abstract class DeployIisFlowAbs extends FlowBase {

    public static final String DEFAULT_APPCMD_PATH = "%systemroot%\\system32\\inetsrv\\appcmd";

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployIisFlowAbs.class);

    protected void addSite(String siteName, String physicalPath, int port, boolean ignoreIfExists) throws InterruptedException {
        runAppcmd(ignoreIfExists, new String[]{"add", "site", "/name:" + siteName, "/id:" + port,
                "/physicalPath:" + physicalPath, "/bindings:http/*:" + port + ":"});
    }

    protected void setSite(String siteName, String poolName) throws InterruptedException {
        runAppcmd(new String[]{"set", "site", "/site.name:" + siteName, "/[path='/'].applicationPool:" + poolName});
    }

    protected void addAppPool(String poolName, String managedRuntimeVersion, boolean ignoreIfExists) throws InterruptedException {
        runAppcmd(ignoreIfExists, new String[]{"add", "apppool", "/name:" + poolName,
                "/managedRuntimeVersion:" + managedRuntimeVersion, "/managedPipelineMode:Integrated"});
    }

    protected void setAppPool(String poolName) throws InterruptedException {
        runAppcmd(new String[]{"set", "apppool", poolName, "/recycling.periodicRestart.time:00:00:00"});
    }

    protected void addApp(String siteName, String physicalPath, String contextPath, boolean ignoreIfExists) throws InterruptedException {
        runAppcmd(ignoreIfExists, new String[]{"add", "app", "/site.name:" + siteName, "/path:" + contextPath,
                "/physicalPath:" + physicalPath});
    }

    protected void setApp(String appName, String poolName) throws InterruptedException {
        runAppcmd(new String[]{"set", "app", "/app.name:" + appName, "/applicationPool:" + poolName});
    }

    protected void grantPermissions(String physicalPath) throws InterruptedException {
        int responseCode = this.getExecutionBuilder(LOGGER, "icacls")
                .args(new String[]{physicalPath, "/T", "/grant", "Users:(R,RX,RD)"}).build().go();
        switch (responseCode) {
            case 0:
                LOGGER.info("Grant Permissions Execution completed SUCCESSFULLY! Congratulations!");
                return;
            default:
                throw new IllegalStateException(String.format("Grant Permissions Execution failed (%d)", new Object[]{responseCode}));
        }
    }

    protected void runAppcmd(String[] args) throws InterruptedException {
        runAppcmd(false, args);
    }

    protected void runAppcmd(boolean ignoreIfExists, String[] args) throws InterruptedException {
        int responseCode = this.getExecutionBuilder(LOGGER, DEFAULT_APPCMD_PATH)
                .args(args).build().go();
        switch (responseCode) {
            case 0:
                LOGGER.info("Deploy Execution completed SUCCESSFULLY! Congratulations!");
                return;
            case 183:
                if (ignoreIfExists) {
                    LOGGER.warn(String.format("Deploy Execution FAILED (%d)", new Object[]{responseCode}));
                    return;
                }
            default:
                throw new IllegalStateException(String.format("Deploy Execution failed (%d)", new Object[]{responseCode}));
        }
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }
}
