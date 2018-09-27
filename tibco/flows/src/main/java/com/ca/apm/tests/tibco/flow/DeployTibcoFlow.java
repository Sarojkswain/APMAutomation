/*
 * Copyright (c) 2015 CA. All rights reserved.
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

package com.ca.apm.tests.tibco.flow;

import java.io.File;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.utils.Utils;
import com.ca.apm.automation.action.utils.monitor.TasFileWatchMonitor;
import com.ca.apm.tests.tibco.flow.action.responsefile.TibcoResponseFile;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DeployTibcoFlow
 *
 * To deploy the various tibco components.
 * 
 * Vashistha Singh (sinva01.ca.com)
 */
@Flow
public class DeployTibcoFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployTibcoFlow.class);

    @FlowContext
    private DeployTibcoFlowContext context;

    @Override
    public void run() throws Exception {
        // download & unpack installer to desired location
        File destination = new File(context.getInstallerUnpackDir());
        destination.mkdir();

        File resFile = new File(context.getResponseFileName());
        TibcoResponseFile resfile = new TibcoResponseFile(context.getResponseFileData());
        resfile.create(resFile);

        archiveFactory.createArchive(context.getInstallerSourceURL()).unpack(destination);
        dirHandlerFactory.create(destination).explodeIfImploded();

        // install
        runAndMonitorInstallationProcess();

    }

    protected String getCommand() {
        if (!SystemUtils.IS_OS_WINDOWS) {
            throw new RuntimeException("This Platform Not supported yet...");
        }
        return context.getInstallerUnpackDir() + "\\TIBCOUniversalInstaller.cmd";
    }

    protected void runAndMonitorInstallationProcess() throws Exception {
        File installerLogfile = new File(context.getInstallerLogFile());
        String[] args =
            {"-silent", "-V", "responseFile=" + "\"" + context.getResponseFileName() + "\"",
                    "logFile=" + context.getInstallerLogFile()};

        try (TasFileWatchMonitor watchMonitor = monitorFactory.createWatchMonitor()) {
            // setup monitoring
            watchMonitor.watchFileChanged(installerLogfile).watchFileCreated(installerLogfile)
                .monitor();
            // execute
            int responseCode =
                Utils.exec(context.getInstallerUnpackDir(), getCommand(), args, LOGGER);
            // evaluate
            switch (responseCode) {
                case 0:
                    LOGGER.info("Installation completed SUCCESSFULLY!!!");
                    break;
                default:
                    throw new IllegalStateException(String.format(
                        "Installation failed due to a fatal error (%d)", responseCode));
            }
        }
    }
}
