/*
 * Copyright (c) 2014 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE 
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR 
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST 
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS 
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.  
 */
package com.ca.apm.siteminder;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.responsefile.IResponseFile;
import com.ca.apm.automation.action.utils.Utils;

/**
 * @author surma04
 */
@Flow
public class DeployCADirectoryFlow extends FlowBase {


    private static final Logger LOGGER = LoggerFactory.getLogger(DeployCADirectoryFlow.class);

    @FlowContext
    private DeployCADirectoryFlowContext context;

    @Override
    public void run() throws Exception {
        // download the self-extractor and save as a zip file
        File installDir = new File(context.getInstallDir());
        File archive = new File(installDir.getPath() + "\\cadir.zip");
        // unpack installer to desired location
        getArchiveFactory().createArtifact(context.getArtifactUrl()).download(archive);
        getArchiveFactory().createArchive(archive).unpack(installDir);
        // create silent install response file
        File installResponseFile = new File(context.getResponseFileLocation());
        IResponseFile psResponseFile =
            new CADirectoryResponseFile(context.getInstallResponseFileData());
        psResponseFile.create(installResponseFile);
        // run the silent installation
        final int responseCode = Utils.exec(installDir.getPath(), installDir.getPath()
            + context.getPathToInstaller(), context.getInstallerParams(), LOGGER);
        if (responseCode != 0) {
            LOGGER.error("Failed to install CA Directory to {} from {} file.", installDir.getPath(), context.getPathToInstaller());
        }
    }
}
