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
import com.ca.apm.automation.action.flow.utility.VarSubstitutionFilter;
import com.ca.apm.automation.utils.file.FileOperation;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * NetStockTraderFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class NerdDinnerDeployFlow extends DeployIisFlowAbs {

    public static final String DEFAULT_NERDDINNER_DOTNET_VERSION = "v4.0";

    private static final Logger LOGGER = LoggerFactory.getLogger(NerdDinnerDeployFlow.class);
    @FlowContext
    private NerdDinnerFlowContext context;

    public void run() throws IOException {
        this.archiveFactory.createArchive(context.getDeployPackageUrl()).unpack(new File(context.getDeploySourcesLocation()));
        // install/configure NerdDinner in IIS
        try {
            // the order of deployment is mandatory for the installation to succeed
            addSite(context.getAppName(), context.getDeploySourcesLocation(), context.getAppPort(), false);
            addAppPool(context.getAppName(), DEFAULT_NERDDINNER_DOTNET_VERSION, false);
            setAppPool(context.getAppName());
            setSite(context.getAppName(), context.getAppName());
            addApp(context.getAppName(), context.getDeploySourcesLocation(), "/" + context.getAppName(), false);
            setApp(context.getAppName() + "/" + context.getAppName(), context.getAppName());
            grantPermissions(context.getDeploySourcesLocation());

            File configFile = FileUtils.getFile(this.context.getDeploySourcesLocation() + "/" + "Web.config");

            Map<String, String> replacePairs = new HashMap<>();
            replacePairs.put("Server=REPLACE-SERVER-NAME", "Server=" + context.getDbHostname());
            replacePairs.put("User Id=AUTOMATION", "User Id=" + context.getDbAdminUserName());
            replacePairs.put("Password=AUTOMATION", "Password=" + context.getDbAdminUserPassword());

            prepareConfigFile(configFile, StandardCharsets.UTF_8.name(), replacePairs);
        } catch (InterruptedException var3) {
            throw new IllegalStateException(var3);
        }
        LOGGER.info("Flow has finished.");
    }

    protected void prepareConfigFile(File configFile, String configFileEncoding, Map<String, String> mapping) {
        LOGGER.info("Preparing response file");
        File installResponseFilePath = configFile.getAbsoluteFile();
        FileOperation replaceable = this.fileOperationFactory.createReplaceable(installResponseFilePath);
        VarSubstitutionFilter varSubstitutionFilter = VarSubstitutionFilter
                .withCharsetAndPlaceholder(configFileEncoding, "%s");
        varSubstitutionFilter.add(mapping);
        replaceable.perform(Collections.singleton(varSubstitutionFilter));
    }
}
