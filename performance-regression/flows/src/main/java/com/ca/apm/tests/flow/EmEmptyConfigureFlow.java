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
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * EmEmptyConfigureFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class EmEmptyConfigureFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmEmptyConfigureFlow.class);
    @FlowContext
    private EmEmptyDeployFlowContext context;

    public void run() throws IOException {
        if (context.getProperties() != null) {
            File configFile = FileUtils.getFile(this.context.getInstallLocation(), "config/IntroscopeEnterpriseManager.properties");
            for (Map.Entry<String, String> property : context.getProperties().entrySet()) {
                String propertyName = property.getKey();
                String propertyValue = property.getValue();
                //
                LOGGER.info("EM: Setting property " + propertyName + " = " + propertyValue);
                uncommentProperty(configFile, propertyName);
                setProperty(configFile, propertyName, propertyValue);
            }
        }
        LOGGER.info("Flow has finished.");
    }

    protected void uncommentProperty(File configFile, String property) throws IOException {

        Path path = Paths.get(configFile.toString());
        Charset charset = StandardCharsets.UTF_8;

        String content = new String(Files.readAllBytes(path), charset);
        content = content.replaceAll("\\#(\\s*)" + property + "(\\s*)=(\\s*)", property + "=");
        Files.write(path, content.getBytes(charset));
    }

    protected void setProperty(File configFile, String property, String value) throws IOException {

        Path path = Paths.get(configFile.toString());
        Charset charset = StandardCharsets.UTF_8;

        String content = new String(Files.readAllBytes(path), charset);
        content = content.replaceAll(property + "(\\s*)=.*", property + "=" + value);
        Files.write(path, content.getBytes(charset));
    }
}
