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
import java.net.UnknownHostException;

import org.codehaus.plexus.util.cli.CommandLineException;
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
public class DeployWebAgentFlow extends FlowBase {

    @FlowContext
    private DeployWebAgentFlowContext context;

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployWebAgentFlow.class);

    /*
     * (non-Javadoc)
     * 
     * @see com.ca.apm.automation.action.core.IAutomationFlow#run()
     */
    @Override
    public void run() throws Exception {
        final File installDir = new File(context.getInstallDir());
        final File tempForInstallers = new File(context.getTempDir());

        // download & unpack installer to a desired location
        getArchiveFactory().createArchive(context.getArtifactUrl()).unpack(tempForInstallers);

        // create the response file for silent installation
        File responseFile = new File(tempForInstallers + "\\" + context.getReponseFileName());
        // TODO CADirRespFile to be replaced by a general CARespFile class
        IResponseFile psResponseFile = new CADirectoryResponseFile(context.getResponseFileData());
        psResponseFile.create(responseFile);


        // install
        final int result = Utils.exec(tempForInstallers.getPath(),
            tempForInstallers.getPath() + "\\"
                + context.getInstallerFileName(), new String[] {"-f", context.getReponseFileName(), "-i", "silent"}, LOGGER);

        switch (result) {
            case 0:
                LOGGER.info("Successfully installed CA Web Agent.");
                break;
            case 1001:
                LOGGER.error("Missing response file ({}) for CA Web Agent silent install.", context.getReponseFileName());
                break;
            case 2000:
                LOGGER.error("Error in response file ({}) - probably an invalid install path.", context.getReponseFileName());
            default:
                LOGGER.error("Error during CA Web Agent installation to {}, check logs if you can find them", installDir.getPath());
        }

        if (context.isOptionPackRequired()) {
            getArchiveFactory().createArchive(context.getOptionPackUrl()).unpack(tempForInstallers);
            File optionPackResponseFile =
                new File(tempForInstallers + "\\" + context.getOptionPackReponseFileName());
            IResponseFile opResponseFile =
                new CADirectoryResponseFile(context.getOptionPackResponseFileData());
            opResponseFile.create(optionPackResponseFile);

            final int opResult = Utils.exec(tempForInstallers.getPath(),
                tempForInstallers.getPath() + "\\"
                    + context.getOptionPackInstaller(), new String[] {"-f", context.getOptionPackReponseFileName(), "-i", "silent"}, LOGGER);
            if (opResult != 0) {
                LOGGER.error("Failed to install CA Web Agent Option Pack to {}.", installDir.getPath());
            }

        } else {
            LOGGER.info("Option Pack for CA Web Agent was not installed per builder configuration.");
        }

        //        registerSMHost();
    }

    /**
     * @throws UnknownHostException
     * @throws CommandLineException
     */
    // smreghost -i 127.0.0.1 -u siteminder -p siteminder -hn tas-cz-ne -hc cawebhost -f c:\CA\install\webagent\config\
    private void registerSMHost() throws UnknownHostException, CommandLineException {
        String hostName = context.getHostName();
        String installDir = context.getInstallDir();
        String config = installDir + "\\config\\SmHost.conf";
        Utils.exec(installDir, installDir
            + "\\bin\\smreghost", new String[] {"-i", "127.0.0.1", "-u", "siteminder", "-p", "siteminder", "-hn", hostName, "-hc", "cawebhost", "-f", config}, LOGGER);
    }

}
