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
import com.ca.apm.automation.action.flow.utility.VarSubstitutionFilter;
import com.ca.apm.automation.utils.file.FileOperation;
import com.ca.apm.tests.flow.DeployIisFlowAbs;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * NetStockTraderFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class DeployNetStockTraderFlow extends DeployIisFlowAbs {

    public static final String DEFAULT_STOCKTRADER_APP_PATH_REL = "\\StockTrader\\StockTraderBusinessService\\TradeWebBSL";
    public static final String DEFAULT_STOCKTRADER_SITE_NAME = "StockTraderSite";
    public static final String DEFAULT_STOCKTRADER_APP_POOL_NAME = "StockTraderBSLAppPool1";
    public static final String DEFAULT_STOCKTRADER_APP_NAME = "TradeWebBSL";
    public static final String DEFAULT_STOCKTRADER_DOTNET_VERSION = "v4.0";
    public static final int DEFAULT_STOCKTRADER_PORT = 8080;

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployNetStockTraderFlow.class);
    @FlowContext
    private NetStockTraderFlowContext context;

    public void run() throws IOException {
        this.archiveFactory.createArchive(this.context.getDeployPackageUrl()).unpack(new File(this.context.getDeploySourcesLocation()));

        File unpackDir = FileUtils.getFile(this.context.getDeploySourcesLocation() + "/" + this.context.getUnpackDirName());
        // iterate through all CONFIG files and replace the tag for dbhost with actual DB domain name
        // only about 18 files of all config files contain this tag, but for simplicity we iterate through all files *.config
        Collection<File> files = FileUtils.listFiles(unpackDir, new RegexFileFilter("^.*\\.config$"),
                DirectoryFileFilter.DIRECTORY);
        for (File file : files) {
            prepareConfigFile(file, StandardCharsets.UTF_8.name(), this.context.getConfigFileOptions());
        }
        // install/configure StockTrader in IIS
        try {
            String stockTraderPath = this.context.getDeploySourcesLocation() + "\\" + this.context.getUnpackDirName();
            String stockTraderBslPath = stockTraderPath + DEFAULT_STOCKTRADER_APP_PATH_REL;
            // the order of deployment is mandatory for the installation to succeed
            addSite(DEFAULT_STOCKTRADER_SITE_NAME, stockTraderPath, DEFAULT_STOCKTRADER_PORT, false);
            addAppPool(DEFAULT_STOCKTRADER_APP_POOL_NAME, DEFAULT_STOCKTRADER_DOTNET_VERSION, false);
            setAppPool(DEFAULT_STOCKTRADER_APP_POOL_NAME);
            setSite(DEFAULT_STOCKTRADER_SITE_NAME, DEFAULT_STOCKTRADER_APP_POOL_NAME);
            addApp(DEFAULT_STOCKTRADER_SITE_NAME, stockTraderBslPath, "/" + DEFAULT_STOCKTRADER_APP_NAME, false);
            setApp(DEFAULT_STOCKTRADER_SITE_NAME + "/" + DEFAULT_STOCKTRADER_APP_NAME, DEFAULT_STOCKTRADER_APP_POOL_NAME);
        } catch (InterruptedException var3) {
            throw new IllegalStateException(var3);
        }
    }

    protected void prepareConfigFile(File configFile, String configFileEncoding, Map<String, String> configFileOptions) {
        LOGGER.info("Preparing config file " + configFile.toString());
        File configFilePath = configFile.getAbsoluteFile();
        FileOperation replaceable = this.fileOperationFactory.createReplaceable(configFilePath);
        VarSubstitutionFilter varSubstitutionFilter = VarSubstitutionFilter
                .withCharsetAndPlaceholder(configFileEncoding, "\\[\\[%s\\]\\]");
        varSubstitutionFilter.add(configFileOptions);
        replaceable.perform(Collections.singleton(varSubstitutionFilter));
    }
}
