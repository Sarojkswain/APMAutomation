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
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
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
public class DeployServletExecFlow extends FlowBase {

    @FlowContext
    private ServletExecFlowContext context;
    private static final Logger LOGGER = LoggerFactory.getLogger(DeployServletExecFlow.class);

    /* (non-Javadoc)
     * @see com.ca.apm.automation.action.core.IAutomationFlow#run()
     */
    @Override
    public void run() throws Exception {

        final String installerName = context.getInstallerName();
        final String tempDir = context.getInstallerDir();
        final File tempInstaller = new File(tempDir + "\\" + installerName);

        // install
        final int result = Utils.exec(tempDir, tempInstaller.getPath(), new String[] {"/s",
            "/f1\"" + context.getReponseFile() + "\""}, LOGGER);

        if (result != 0) {
            LOGGER.error("Failed to install Servlet Exec!");
        }

        stopServletExecService(tempDir);
        stopServletExec(tempDir);

        replaceJavaPathTo32bit(context.getJavaPath());
        // TODO update to latest version or not?

        configFederationServices(tempDir);

        startServletExecService(tempDir);
        
        // Apache config is not updated by SE install, do it manually
        updateApacheConf(tempDir);

        restartApache(tempDir);
    }

    /**
     * Update apache httpd.conf with servlet exec module and config file
     * @param tempDir
     * @throws IOException
     */
    private void updateApacheConf(String tempDir) throws IOException {
        String httpdConf = context.getApacheDir() + "/conf/httpd.conf";
        if (!httpdConf.contains("ServletExecAdapterConfigFile")) {
            File propFile = new File(httpdConf);
            String encoding = System.getProperty("file.encoding");
            String content = FileUtils.readFileToString(propFile, encoding);
            String seDir = context.getServletExecInstallDir();
            String webAdapterFile = seDir + "/config/webadapter.properties";
            FileUtils.write(propFile, content + 
                "\nLoadModule servletexec_module modules/ApacheModuleServletExec.dll\nServletExecAdapterConfigFile \"" + webAdapterFile + "\"");
            FileUtils.write(new File(webAdapterFile), 
                "servletexec.aliasCheckInterval=10\nservletexec.localhost.hosts=all\nservletexec.localhost.instances=127.0.0.1:8999\n"
                + "servletexec.localhost.pool-increment=5\nservletexec.localhost.pool-max-idle=10");
        }
        
    }

    private void restartApache(final String workingDir) throws CommandLineException, InterruptedException {
        LOGGER.info("Restart apache service");
        Utils.exec(workingDir, "sc", new String[] {"stop", "apache2.2"}, LOGGER);
        Thread.sleep(10000);
        Utils.exec(workingDir, "sc", new String[] {"start", "apache2.2"}, LOGGER);
    }

    /**
     * @throws CommandLineException
     */
    private void startServletExec(final String workingDir) throws CommandLineException {
        //        Utils.exec(workingDir, "sc", new String[] {"start", "ServletExec-localhost"}, LOGGER);
        Utils.exec(workingDir, "cmd", new String[] {"/C", context.getStartScriptFile()}, LOGGER);
    }

    private void stopServletExec(final String workingDir) throws CommandLineException {
        // Call the stop script twice as there are some cases where it was not successful first time
        // TODO: review and fix
        Utils.exec(workingDir, "cmd", new String[] {"/C", context.getStopScriptFile()}, LOGGER);
        Utils.exec(workingDir, "cmd", new String[] {"/C", context.getStopScriptFile()}, LOGGER);
    }

    private void stopServletExecService(final String workingDir) throws CommandLineException {
        // Call the stop service twice as there are some cases where it was not successful first time
        // Wait some time between stopping to let it terminate
        // TODO: review and fix
        Utils.exec(workingDir, "sc", new String[] {"stop", "ServletExec-localhost"}, LOGGER);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Utils.exec(workingDir, "sc", new String[] {"stop", "ServletExec-localhost"}, LOGGER);
    }

    private void startServletExecService(final String workingDir) throws CommandLineException {
        Utils.exec(workingDir, "sc", new String[] {"start", "ServletExec-localhost"}, LOGGER);
    }

    /**
     * @param workingDir
     * @throws CommandLineException if xcopy command failed to execute
     * @throws IOException          if failed to read from/write to the configuration file
     */
    private void configFederationServices(final String workingDir) throws CommandLineException, IOException {
        String webAgentAffDir = context.getWebAgentDir() + "\\affwebservices";
        String servletExecInstanceDir = context.getAffWSDir();
        //C:\>xcopy C:\CA\install\webagent\affwebservices C:\CA\install\ServletExec\se-tas-cz-n13.ca.com\webapps\default\affwebservices /i /e
        Utils.exec(workingDir, "xcopy", new String[] {webAgentAffDir, servletExecInstanceDir, "/i", "/e"}, LOGGER);

        updateWebServiceProperties(servletExecInstanceDir);
    }

    private void updateWebServiceProperties(final String servletExecInstanceDir) throws IOException {
        String affWSPropsFile =
            servletExecInstanceDir + "\\WEB-INF\\classes\\affwebservices.properties";
        File propFile = new File(affWSPropsFile);
        String encoding = System.getProperty("file.encoding");
        String agentConfLoc = "AgentConfigLocation=";
        String apacheWebAgentConf = agentConfLoc + context.getApacheWebAgentConf();
        FileUtils.write(propFile, FileUtils.readFileToString(propFile, encoding).
            replaceFirst(agentConfLoc,
                apacheWebAgentConf.replace("\\", "\\\\") + "\r\n//" + agentConfLoc));
    }

    /**
     * @param javaPath
     * @throws IOException
     */
    private void replaceJavaPathTo32bit(final String javaPath) throws IOException {
        File startScript = new File(context.getStartScriptFile());
        String encoding = System.getProperty("file.encoding");
        String jvmHome = "set jvmHome=";
        String replacedJvmHome = jvmHome + javaPath + "\n#" + jvmHome;
        FileUtils.write(startScript, FileUtils.readFileToString(startScript, encoding).
            replaceFirst(jvmHome, replacedJvmHome));
        File stopScript = new File(context.getStopScriptFile());
        FileUtils.write(stopScript, FileUtils.readFileToString(stopScript, encoding).
            replaceFirst(jvmHome, replacedJvmHome));

    }

}
