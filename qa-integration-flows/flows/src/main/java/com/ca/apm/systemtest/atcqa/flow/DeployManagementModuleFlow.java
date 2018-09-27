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
package com.ca.apm.systemtest.atcqa.flow;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;

/**
 * Copies a file to EM/config/modules
 *
 * @author korzd01
 */
@Flow
public class DeployManagementModuleFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployManagementModuleFlow.class);

    @FlowContext
    private DeployManagementModuleFlowContext context;

    @Override
    public void run() throws Exception {
        try {
            exportResource(context.getManagementModulePathName(),
                new File(context.getEmInstallDir(), "deploy"));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to deploy a management module!", e);
        }
        LOGGER.info("A management module was deployed.");
    }

    /**
     * Export a resource embedded into a Jar file to the local file path.
     *
     * @param resourceName ie.: "/SmartLibrary.dll"
     * @return The path to the exported resource
     * @throws Exception
     */
    private static void exportResource(String resourcePathName, File outFolder) throws Exception {
        InputStream stream = null;
        try {
            // note that each / is a directory down in the "jar tree" been the jar the root of the
            // tree
            stream = DeployManagementModuleFlow.class.getResourceAsStream(resourcePathName);
            if (stream == null) {
                throw new Exception("Cannot get resource \"" + resourcePathName
                    + "\" from Jar file.");
            }
            File outFile = new File(outFolder, new File(resourcePathName).getName());
            outFile.getParentFile().mkdirs();
            Files.copy(stream, outFile.toPath());
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

}
