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
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.Execution;
import com.ca.apm.automation.action.flow.utility.VarSubstitutionFilter;
import com.ca.apm.automation.utils.file.FileOperation;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Basic Flow for running a SQL Script in Oracle DB
 * <p/>
 * This flow only downloads the Artifact and unpacks it.
 * Method prepareAndExecuteSql() has to be implemented for actual running of a specific script from the archive.
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public abstract class OracleScriptFlow extends FlowBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(OracleScriptFlow.class);
    @FlowContext
    private OracleScriptFlowContext context;

    public OracleScriptFlow() {
    }

    public void run() throws IOException {
        this.archiveFactory.createArchive(this.context.getDeployPackageUrl()).unpack(new File(this.context.getDeploySourcesLocation()));
        prepareAndExecuteSql();
        LOGGER.info("Flow has finished.");
    }

    public abstract void prepareAndExecuteSql();

    @NotNull
    protected File createSqlFilePath(String fileName) {
        File sqlFileDir = FileUtils.getFile(this.context.getDeploySourcesLocation());
        File sqlFile = new File(sqlFileDir, fileName);
        if (sqlFile.exists() && sqlFile.canRead()) {
            LOGGER.info("Installation sql file located at: {}", sqlFile);
            return sqlFile;
        } else {
            throw new IllegalStateException("Installation sql file(\'" + sqlFile.getAbsolutePath() + "\') is either missing or can\'t be read.");
        }
    }

    protected void prepareSqlFile(File sqlFile, Map<String, String> sqlFileOptions) {
        LOGGER.info("Preparing response file");
        File installResponseFilePath = sqlFile.getAbsoluteFile();
        FileOperation replaceable = this.fileOperationFactory.createReplaceable(installResponseFilePath);
        VarSubstitutionFilter varSubstitutionFilter = VarSubstitutionFilter.withCharsetAndPlaceholder(this.context.getEncoding(), "\\[%s\\]");
        varSubstitutionFilter.add(sqlFileOptions);
        replaceable.perform(Collections.singleton(varSubstitutionFilter));
    }

    protected void runInstallationProcess(File installSqlFile) throws InterruptedException {
        List<String> args = new ArrayList<>();
        args.add(this.context.getRunAsUser() + "/" + this.context.getRunAsPassword() + "@" + this.context.getSchemaName());
        if (this.context.isRunAsSysdba()) {
            args.add("as");
            args.add("sysdba");
        }
        args.add("@" + installSqlFile.toString());
        int responseCode = this.getExecutionBuilder(LOGGER, this.context.getPlsqlExecutableLocation())
                .args(args.toArray(new String[1]))
                .build()
                .go();
        switch (responseCode) {
            case 0:
                LOGGER.info("SQL Execution completed SUCCESSFULLY! Congratulations!");
                return;
            default:
                throw new IllegalStateException(String.format("SQL Execution failed (%d)", new Object[]{responseCode}));
        }
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }
}
