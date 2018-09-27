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

package com.ca.apm.automation.action.flow.mainframe;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.utility.ConfigureFlow;
import com.ca.apm.automation.utils.mainframe.EncodedPropertiesConfiguration;

import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * Flow that modifies differently encoded property files. Note that such file violates the Java
 * contract for property file encoding of Latin-1.
 *
 * <p>
 * When the modification map contains null values, those are interpreted as requests to remove
 * existing values. Use empty string to set blank values.
 */
@Flow
public class ConfigureEncodedFlow extends ConfigureFlow {

    @FlowContext
    private ConfigureEncodedFlowContext context;

    @Override
    // TODO properly extend parent class without duplicating the code
    public void run() throws Exception {
        final String encoding = context.getEncoding();

        for (Map.Entry<String, Map<String, String>> configFileEntry : context.getConfigMap()
            .entrySet()) {

            File propFile = new File(configFileEntry.getKey());

            EncodedPropertiesConfiguration config =
                new EncodedPropertiesConfiguration(propFile, encoding);

            config.configure(configFileEntry.getValue());
        }

        // TODO missing append functionality from parent

        for (Map.Entry<String, Set<String>> configFileEntry : context.getDeleteConfigMap()
            .entrySet()) {

            File propFile = new File(configFileEntry.getKey());

            EncodedPropertiesConfiguration config =
                new EncodedPropertiesConfiguration(propFile, encoding);

            for (String key : configFileEntry.getValue()) {
                config.clearProperty(key);
            }
        }
    }
}
