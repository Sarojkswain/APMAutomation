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
package com.ca.apm.tests.flow.msSqlDb;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.Execution;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Flow for configuring TradeDb in MSSQL DB
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class MsSqlTradeDbScriptFlow extends FlowBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(MsSqlTradeDbScriptFlow.class);
    @FlowContext
    private MsSqlTradeDbScriptFlowContext context;

    public MsSqlTradeDbScriptFlow() {
    }

    public void run() throws IOException {
        this.archiveFactory.createArchive(this.context.getDeployPackageUrl()).unpack(new File(this.context.getDeploySourcesLocation()));

        File batFileDir = FileUtils.getFile(this.context.getDeploySourcesLocation() + "/" + this.context.getUnpackDirName());
        File configDbFile = this.createBatFilePath(this.context.getConfigDbFileName());
        File createTablesFile = this.createBatFilePath(this.context.getCreateTablesFileName());
        try {
            if (!this.context.isRecreateTablesOnly()) {
                LOGGER.info("Configuring database");
                this.runInstallationProcess(batFileDir, configDbFile);
            }
            LOGGER.info("Configuring tables");
            this.runInstallationProcess(batFileDir, createTablesFile);
        } catch (InterruptedException var3) {
            throw new IllegalStateException(var3);
        }
        LOGGER.info("Flow has finished.");
    }

    @NotNull
    protected File createBatFilePath(String fileName) throws IOException {
        File sqlFileDir = FileUtils.getFile(this.context.getDeploySourcesLocation() + "/" + this.context.getUnpackDirName());
        File sqlFile = new File(sqlFileDir, fileName);
        if (sqlFile.exists() && sqlFile.canRead()) {
            LOGGER.info("Installation BAT file located at: {}", sqlFile);

//            File binnPath = FileUtils.getFile(this.context.getDbDeploySourcesLocation() +
//                    "\\x64\\Setup\\sql_engine_core_shared_msi\\PFiles\\SqlServr\\100\\Tools\\Binn"); // todo parametrize
            File binnPath = FileUtils.getFile("c:\\Program Files\\Microsoft SQL Server\\100\\Tools\\Binn"); // todo parametrize

            setPath(sqlFile, binnPath.getAbsolutePath());

            return sqlFile;
        } else {
            throw new IllegalStateException("Installation BAT file(\'" + sqlFile.getAbsolutePath() + "\') is either missing or can\'t be read.");
        }
    }

    protected void setPath(File batchFile, String addPathString) throws IOException {
        Path path = Paths.get(batchFile.toString());
        Charset charset = Charset.forName("Cp1252");
        String content = new String(Files.readAllBytes(path), charset);
        content = "set PATH=%PATH%;" + addPathString + "\r\n" + content;
        Files.write(path, content.getBytes(charset));
    }

    protected void runInstallationProcess(File workDir, File installBatFile) throws InterruptedException {
        int responseCode = this.getExecutionBuilder(LOGGER, installBatFile.toString()).workDir(workDir)
                .build().go();
        LOGGER.info("Execution finished with response code " + responseCode);
        /*switch (responseCode) {
            case 0:
                LOGGER.info("BAT Execution completed SUCCESSFULLY! Congratulations!");
                return;
            default:
                throw new IllegalStateException(String.format("BAT Execution failed (%d)", new Object[]{responseCode}));
        }*/ // TODO revise SQL response codes
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }
}
