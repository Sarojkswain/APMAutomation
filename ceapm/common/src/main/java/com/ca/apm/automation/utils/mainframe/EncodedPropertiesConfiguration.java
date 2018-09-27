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

package com.ca.apm.automation.utils.mainframe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;

public class EncodedPropertiesConfiguration
    extends org.apache.commons.configuration.PropertiesConfiguration {
    public EncodedPropertiesConfiguration(File propFile, String encoding)
        throws FileNotFoundException, IOException, ConfigurationException {

        super();
        setFile(propFile);
        setEncoding(encoding);
        setAutoSave(true);
        if (propFile.isFile()) {
            try (FileInputStream fis = new FileInputStream(propFile)) {
                load(fis); // must load only after we set encoding
            }
        }
    }

    public void configure(String key, String value) {
        if (value == null) {
            clearProperty(key);
        } else {
            if (containsKey(key)) {
                setProperty(key, value);
            } else {
                addProperty(key, value);
            }
        }
    }

    public void configure(Map<String, String> propertyMap) {
        for (Map.Entry<String, String> change : propertyMap.entrySet()) {
            configure(change.getKey(), change.getValue());
        }
    }
}
