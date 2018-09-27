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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * OracleTradeDbScriptFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class StockTraderFlow extends WeblogicWlstFlowAbs {

    public static final String DEFAULT_STOCKTRADER_APP_NAME = "Stocktrader";
    public static final String DEFAULT_STOCKTRADER_BSL_APP_NAME = "StocktraderBSL";

    private static final Logger LOGGER = LoggerFactory.getLogger(StockTraderFlow.class);
    @FlowContext
    private StockTraderFlowContext context;

    public void prepareAndExecuteWlst() throws IOException {
        this.archiveFactory.createArchive(this.context.getDeployPackageUrl()).unpack(new File(this.context.getDeploySourcesLocation()));

        String sources = this.context.getDeploySourcesLocation();
        File applicationEarFile = this.createFilePath(sources, this.context.getApplicationEarFileName());
        File applicationBslEarFile = this.createFilePath(sources, this.context.getApplicationBslEarFileName());
        File createDatasourceScriptFile = this.createFilePath(sources, this.context.getCreateDatasourceScriptFileName());
        File deleteDatasourceScriptFile = this.createFilePath(sources, this.context.getDeleteDatasourceScriptFileName());
        File propertiesFile = this.createFilePath(sources, this.context.getPropertiesFileFileName());
        this.context.getPropertiesFileOptions().put("PROPERTIES_FILE", propertiesFile.toString().replaceAll("\\\\", "/"));

        File jsfWarFile = this.createFilePath(null,
                this.context.getWeblogicInstallPath() + WeblogicWlstFlowContext.JSF_LIB_WAR_PATH_REL);
        File jstlWarFile = this.createFilePath(null,
                this.context.getWeblogicInstallPath() + WeblogicWlstFlowContext.JSTL_LIB_WAR_PATH_REL);

        try {
            this.prepareScriptFile(propertiesFile, StandardCharsets.UTF_8.name(), this.context.getPropertiesFileOptions());
            this.prepareScriptFile(createDatasourceScriptFile, StandardCharsets.UTF_8.name(), this.context.getPropertiesFileOptions());
            this.prepareScriptFile(deleteDatasourceScriptFile, StandardCharsets.UTF_8.name(), this.context.getPropertiesFileOptions());
            this.runScript(createDatasourceScriptFile);

            this.deployLibrary(jsfWarFile, this.context.getWeblogicPort(), this.context.getWeblogicUserName(),
                    this.context.getWeblogicUserPassword());
            this.deployLibrary(jstlWarFile, this.context.getWeblogicPort(), this.context.getWeblogicUserName(),
                    this.context.getWeblogicUserPassword());

            this.deployApplication(applicationEarFile, DEFAULT_STOCKTRADER_APP_NAME, this.context.getWeblogicPort(),
                    this.context.getWeblogicUserName(), this.context.getWeblogicUserPassword());
            this.deployApplication(applicationBslEarFile, DEFAULT_STOCKTRADER_BSL_APP_NAME, this.context.getWeblogicPort(),
                    this.context.getWeblogicUserName(), this.context.getWeblogicUserPassword());
        } catch (InterruptedException var3) {
            throw new IllegalStateException(var3);
        }
    }
}
