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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * OracleTradeDbScriptFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class OracleTradeDbScriptFlow extends OracleScriptFlow {
    private static final Logger LOGGER = LoggerFactory.getLogger(OracleTradeDbScriptFlow.class);
    @FlowContext
    private OracleTradeDbScriptFlowContext context;

    public void prepareAndExecuteSql() {
        File createUserSqlFile = this.createSqlFilePath(this.context.getCreateUserSqlFileName());
        File configDbSqlFile = this.createSqlFilePath(this.context.getConfigDbSqlFileName());
        try {
            this.prepareSqlFile(configDbSqlFile, this.context.getSqlFileOptions());
            this.runInstallationProcess(configDbSqlFile);
            this.prepareSqlFile(createUserSqlFile, this.context.getSqlFileOptions());
            this.runInstallationProcess(createUserSqlFile);
        } catch (InterruptedException var3) {
            throw new IllegalStateException(var3);
        }
    }
}
