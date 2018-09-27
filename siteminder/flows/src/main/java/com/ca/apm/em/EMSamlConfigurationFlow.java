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
package com.ca.apm.em;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;

/**
 * @author Sundeep (bhusu01)
 */
@Flow
public class EMSamlConfigurationFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(EMSamlConfigurationFlow.class);

    @FlowContext
    private EMSamlConfigureFlowContext context;

    @Override
    public void run() throws Exception {
        String apmRootDir = context.getAPMRootDir();

        modifyIntroscopeConfig(new File(
            apmRootDir + context.getConfigFilePath()));
    }

    private void modifyIntroscopeConfig(File configFile) throws IOException {
        if (configFile.exists()) {
            String encoding = System.getProperty("file.encoding");
            String content = FileUtils.readFileToString(configFile, encoding);
            content = content.replaceAll("introscope.saml.enable=false", "introscope.saml.enable=true");
            content = content.replaceAll("introscope.saml.idpUrl=", 
                "introscope.saml.idpUrl=http://" + context.getSMHost()
                    + "/affwebservices/public/saml2sso" + "\n#");
            if (context.isEnabledInternalIdp()) {
                content = content.replaceAll("introscope.saml.internalIdp.enable=false", 
                    "introscope.saml.internalIdp.enable=true");
            }
            FileUtils.write(configFile, content, encoding);

        } else {
            LOGGER.error("{} does not exist", configFile.getCanonicalPath());
        }

    }
}
