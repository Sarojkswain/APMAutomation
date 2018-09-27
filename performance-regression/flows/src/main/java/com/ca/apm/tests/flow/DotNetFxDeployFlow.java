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
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * DotNetFxDeployFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class DotNetFxDeployFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DotNetFxDeployFlow.class);
    @FlowContext
    private DotNetFxFlowContext context;

    public void run() throws IOException {
        File installerFile = new File(context.getDeploySourcesLocation(), this.context.getSetupFileName());
        this.archiveFactory.createArtifact(context.getDeployPackageUrl()).download(installerFile);

        try {
            runInstallationProcess();
        } catch (InterruptedException var3) {
            throw new IllegalStateException(var3);
        }
        LOGGER.info("Flow has finished.");
    }

    protected void runInstallationProcess() throws InterruptedException { // TODO
//        File installExecutable = FileUtils.getFile(this.context.getDeploySourcesLocation(),
//                this.context.getSetupFileName());
//        int responseCode = this.getExecutionBuilder(LOGGER, installExecutable.toString())
//                .args(new String[]{"-S", "-DDatabaseType", this.context.getDatabaseType(),
//                        "-DDatabaseDriver", this.context.getDatabaseDriver(),
//                        "-DDatabaseUrl",
//                        "jdbc:oracle:thin:@" + this.context.getDbHostname() + ":" + this.context.getDbTnsPort() + ":" + this.context.getTradeDbDatabaseName(),
//                        "-DDatabaseUsername", this.context.getDatabaseUsername(),
//                        "-DDatabasePassword", this.context.getDatabasePassword(),
//                        "-DInstallationDir", this.context.getInstallationDir(),
//                        "-DLoadDB", this.context.isLoadDB() ? "1" : "0",
//                        "-DJavaJRE", this.context.getJavaJRE(),
//                        "-DPortNumber", String.valueOf(this.context.getPortNumber())})
//                .build().go();
//        //copy();
//        switch (responseCode) {
//            case 0:
//                LOGGER.info("Konakart installation completed SUCCESSFULLY! Congratulations!");
//                return;
//            default:
//                throw new IllegalStateException(String.format("Launching silent installation of Konakart failed (%d)", new Object[]{responseCode}));
//        }
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }
}
