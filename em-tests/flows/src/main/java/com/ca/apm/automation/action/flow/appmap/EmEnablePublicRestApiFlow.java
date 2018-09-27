/*
 * Copyright (c) 2014 CA. All rights reserved.
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
package com.ca.apm.automation.action.flow.appmap;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;

/**
 * This flow edits the IntroscopeEnterpriseManager.properties file
 * 
 * @author surma04
 *
 */
@Flow
public class EmEnablePublicRestApiFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmEnablePublicRestApiFlow.class);

    @FlowContext
    private EmEnablePublicRestApiContext context;


    public static String DEV_TOKEN_KEY = "-Dappmap.token";
    public static String DEV_USER_KEY = "-Dappmap.user";

    private static final String EM_CONFIG_FILE = File.separatorChar + "config" + File.separatorChar
        + "IntroscopeEnterpriseManager.properties";

    private static final String ISCP_PUBLIC_RESTAPI_ENABLED = "introscope.public.restapi.enabled=";

    /*
     * (non-Javadoc)
     * 
     * @see com.ca.apm.automation.action.core.IAutomationFlow#run()
     */
    @Override
    public void run() throws Exception {

        String apmRootDir = context.getApmRootDir();
        modifyIntroscopeConfig(new File(apmRootDir + EM_CONFIG_FILE));
        modifyLaxProperties(new File(apmRootDir + File.separatorChar
            + context.getIntroscopeEnterpriseManagerLax()));
    }

    private void modifyIntroscopeConfig(File configFile) throws IOException {
        if (configFile.exists()) {
            String encoding = System.getProperty("file.encoding");
            FileUtils.write(
                configFile,
                FileUtils
                    .readFileToString(configFile, encoding)
                    // enable public rest api
                    .replace(ISCP_PUBLIC_RESTAPI_ENABLED + "false",
                        ISCP_PUBLIC_RESTAPI_ENABLED + "true"), encoding);

        } else {
            LOGGER.error("{} does not exist", configFile.getCanonicalPath());
        }
    }

    private void modifyLaxProperties(File properties) throws IOException {

        if (properties.exists()) {
            StringBuilder sb = new StringBuilder();
            sb.append("lax.nl.java.option.additional=");
            sb.append(" ").append(DEV_TOKEN_KEY).append("=").append(context.getDevelopmentToken());
            sb.append(" ").append(DEV_USER_KEY).append("=").append(context.getDevelopmentUser());

            String encoding = System.getProperty("file.encoding");
            FileUtils.write(properties, FileUtils.readFileToString(properties, encoding)
                .replaceAll("lax.nl.java.option.additional=", sb.toString()), encoding);
        } else {
            LOGGER.error("{} does not exist", properties.getCanonicalPath());
        }

    }
}
