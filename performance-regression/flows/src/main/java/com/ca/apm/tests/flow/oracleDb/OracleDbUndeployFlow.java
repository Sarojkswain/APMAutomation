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
package com.ca.apm.tests.flow.oracleDb;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.commandline.Execution;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class OracleDbUndeployFlow extends OracleDbStopFlow {

    private static final Logger LOGGER = LoggerFactory.getLogger(OracleDbUndeployFlow.class);
    @FlowContext
    private OracleDbUndeployFlowContext context;

    public OracleDbUndeployFlow() {
    }

    public void run() throws IOException {
        try {
            deleteDatabaseSchema();
            uninstallDatabase();
            deleteService(TRADEDB_SERVICE_NAME);
            deleteService(ORACLETNS_LISTENER_SERVICE_NAME);
            killMsdtc();
            rmDir();
        } catch (InterruptedException var3) {
            throw new IllegalStateException(var3);
        }
        LOGGER.info("Flow has finished.");
    }

    protected void deleteDatabaseSchema() throws InterruptedException, IOException {
        File dbca = FileUtils.getFile(this.context.getHomePath(), "BIN/dbca.bat");
        if (dbca.exists()) {
            int responseCode = this.getExecutionBuilder(LOGGER, dbca.getAbsolutePath())
                    .args(new String[]{"-silent", "-deleteDatabase", "-sourceDB", this.context.getDbSid(),
                            "-sysDBAUserName", "sys", "-sysDBAPassword", this.context.getSuperAdminSamePassword()}).build().go();
            switch (responseCode) {
                case 0:
                    LOGGER.info("Oracle DB Deletion completed SUCCESSFULLY! Congratulations!");
                    return;
                default:
                    throw new IllegalStateException(String.format("Oracle DB Deletion failed (%d)", new Object[]{responseCode}));
            }
        } else {
            LOGGER.info("DB SID '" + this.context.getDbSid() + "' does not exist. Skipping Oracle DB Deletion.");
        }
    }

    protected void uninstallDatabase() throws InterruptedException, IOException {
        File setup = FileUtils.getFile(this.context.getInstallSourcesPath(), "/database/setup.exe");
        if (setup.exists()) {
            int responseCode = this.getExecutionBuilder(LOGGER, setup.getAbsolutePath())
                    .args(new String[]{"-silent", "-deinstall", "-waitforcompletion", "-nowait",
                            "-ignorePrereq", "-ignoreSysPrereqs", "-removeallfiles", "-responseFile",
                            this.context.getResponseFileDir() + "\\" + this.context.getResponseFileName()}).build().go();
            switch (responseCode) {
                case 0:
                    LOGGER.info("Oracle DB Uninstallation completed SUCCESSFULLY! Congratulations!");
                    return;
                default:
                    throw new IllegalStateException(String.format("Oracle DB Uninstallation failed (%d)", new Object[]{responseCode}));
            }
        } else {
            LOGGER.info("DB Installer '" + setup.getAbsolutePath() + "' does not exist. Skipping Oracle DB uninstallation.");
        }
    }

    protected void killMsdtc() throws InterruptedException, IOException {
        int responseCode = this.getExecutionBuilder(LOGGER, "taskkill")
                .args(new String[]{"/F", "/FI", "IMAGENAME eq msdtc.exe"}).build().go();
        switch (responseCode) {
            case 0:
                LOGGER.info("MSDTC Kill completed SUCCESSFULLY! Congratulations!");
                return;
            default:
                throw new IllegalStateException(String.format("MSDTC Kill failed (%d)", new Object[]{responseCode}));
        }
    }

    protected void rmDir() throws InterruptedException, IOException {
        File installDir = FileUtils.getFile(this.context.getInstallLocation());
        if (installDir.exists()) {
            FileUtils.deleteDirectory(installDir);
        }
        File inventoryDir = FileUtils.getFile("c:/Program Files/Oracle/Inventory");
        if (inventoryDir.exists()) {
            FileUtils.deleteDirectory(inventoryDir);
            LOGGER.info("DB Install directory '" + inventoryDir.getAbsolutePath() + "' deleted.");
        } else {
            LOGGER.info("DB Install directory '" + inventoryDir.getAbsolutePath() + "' does not exist. Skipping deletion.");
        }
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }
}
