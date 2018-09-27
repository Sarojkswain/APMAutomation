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
package com.ca.apm.siteminder;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.utils.Utils;

/**
 * @author surma04
 */
@Flow
public class DeployApacheFlow extends FlowBase {

    @FlowContext
    private ApacheFlowContext context;
    private static final Logger LOGGER = LoggerFactory.getLogger(DeployApacheFlow.class);

    /*
     * (non-Javadoc)
     * 
     * @see com.ca.apm.automation.action.core.IAutomationFlow#run()
     */
    @Override
    public void run() throws Exception {

        final String installerName = context.getInstallerName();
        final String tempDir = context.getTempDir();
        final File tempInstaller = new File(tempDir + "\\" + installerName);

        // download & unpack installer to a desired location
        getArchiveFactory().createArtifact(context.getArtifactUrl()).download(tempInstaller);

        // install
        final int result = Utils.exec(tempDir, "cmd", new String[] {"/C", tempInstaller.getPath(),
            "INSTALLDIR="
                + context.getInstallDir(), "ALLUSERS=1", "SERVERADMIN=admin@localhost", "/quiet"}, LOGGER);

        if (result != 0) {
            LOGGER.error("Failed to install Apache Server");
        }

        editHttpdConfToAllowAll();

    }

    /**
     * Sets the directories access to Allow from all instead of Deny from all
     *
     * @throws IOException
     */
    private void editHttpdConfToAllowAll() throws IOException {
        String oldString = "Deny from all";
        String replaceString = "Allow from all";
        final String encoding = System.getProperty("file.encoding");
        File apacheConf = new File(context.getConfFile());
        FileUtils.write(apacheConf, FileUtils.readFileToString(apacheConf, encoding).
            replaceFirst(oldString, replaceString));
    }

}
