/**
 * Copyright (c) 2017 CA. All rights reserved.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.utils.configuration.ConfigurationFile;
import com.ca.apm.automation.utils.configuration.ConfigurationFileFactory;

/**
 * Contains the flow to deploy the UMAgent.
 * 
 * @author Dhruv Mevada (mevdh01)
 *
 */
@Flow
public class DeployUMAgentFlow extends AbstractDeployAgentFlow {
    @FlowContext
    private DeployUMAgentFlowContext context;
    private static final Logger LOGGER = LoggerFactory.getLogger(DeployUMAgentFlow.class);
    
    private final String configFile = "IntroscopeAgent.profile";
    private final String installerScript = "apmia-ca-installer.sh";
    private final String extensionsProfile = "Extensions.profile";
    private final String extensionsLoadProperty = "introscope.agent.extensions.bundles.load";
    private final String umaBinScript = "APMIAgent.sh";
    
    private final String iaDir = "apmia";
    
    File agentInstallDir = null;
    File iaHome = null;
    
    
    @Override
    public void run() throws Exception {
        LOGGER.info("Starting Unified Monitoring Agent deployment");
        
        agentInstallDir = new File(context.getInstallDir());
        iaHome =  new File(context.getInstallDir() + File.separator + iaDir);
        
        //uninstall the agent instead of deleting
//        clearOldUMAgent(iaHome);
        deployUMAgent(agentInstallDir);
        configureAgent();

        LOGGER.info("Unified Monitoring Agent deployment task completed.");
    }

    /**
     * Remove old UMAgent
     * 
     * @param agentInstallDir The installation directory of the UMAgent.
     * @throws IOException IOException generated by file operations.
     */
    private void clearOldUMAgent(File agentInstallDir) throws IOException {
        super.clearOldAgent(agentInstallDir);
    }

    /**
     * Returns the current context.
     */
    @Override
    protected AbstractDeployAgentFlowContext getContext() {
        return context;
    }

    /**
     * Deploys the agent.
     * 
     * @param agentInstallDir The installation location for the agent.
     * @throws IOException IOException caused by file operations
     */
    
    protected void deployUMAgent(File agentInstallDir) throws IOException {
        File installerTgdirDir = new File(context.getInstallerTgdir());
        archiveFactory.createArchive(context.getInstallerUrl()).unpack(installerTgdirDir);

        LOGGER.info("Creating folder {}", agentInstallDir.getAbsolutePath());
        FileUtils.forceMkdir(agentInstallDir);

        File agentUnpackedDir = installerTgdirDir;
        LOGGER.info("Copying files from {} to {}", agentUnpackedDir.getAbsolutePath(),
            agentInstallDir.getAbsolutePath());
        
        FileUtils.copyDirectory(agentUnpackedDir, agentInstallDir);

        LOGGER.info("shell script:" + iaHome.getAbsolutePath() + File.separator + installerScript);
        
        File shellScript = new File(iaHome.getAbsolutePath() + File.separator + installerScript);
        
        if(!shellScript.exists())
        {
            throw new IllegalStateException(String.format("Didn't find %s as expected, deployment will not continue.",
              shellScript.getAbsolutePath()));
        }

        updateJREPermissions();

        LOGGER.info("Found {}", shellScript.getAbsolutePath());
        
        updateExtensionProfile(agentInstallDir, "NodeExtension");
        executeShellScript(shellScript.getAbsolutePath());
    }
    
    private void executeShellScript(String path)
    {
        String parentPath = new File(path).getParent();
        File parent = new File(parentPath);        
        String binScript = parent + File.separator + "bin" + File.separator + umaBinScript;
        
        ProcessBuilder installProcess;
        ProcessBuilder stopProcess;
        ProcessBuilder deleteLogsProcess;
        ProcessBuilder createLogsProcess;
        
        new File(path).setExecutable(true);
        new File(binScript).setExecutable(true);
        
        try
        {
            installProcess = new ProcessBuilder("sh", path, "install");
            installProcess.directory(parent);
            Process p = installProcess.start();
            p.waitFor();
            
            stopProcess = new ProcessBuilder("sh", path, "stop");
            stopProcess.directory(parent);
            Process p2 = stopProcess.start();
            p2.waitFor();
            
            deleteLogsProcess = new ProcessBuilder("rm", "-rf", parent + File.separator + "logs");
            deleteLogsProcess.directory(parent);
            Process p3 = deleteLogsProcess.start();
            p3.waitFor();
            
            createLogsProcess = new ProcessBuilder("mkdir", parent + File.separator + "logs");
            createLogsProcess.directory(parent);
            Process p4 = createLogsProcess.start();
            p4.waitFor();
        }
        
        catch(Exception error)
        {
            error.printStackTrace();
        }
    }
    
    private void updateExtensionProfile(File agentInstallDir, String extension)
    {
        String extensionsProfileFile = iaHome.getAbsolutePath() + File.separator + "extensions" + File.separator + extensionsProfile;
        String match = extensionsLoadProperty + "=";
        
         try {

             File file = new File(extensionsProfileFile);
             File newProfileFile = new File(extensionsProfileFile + ".new");

             BufferedReader reader = new BufferedReader(new FileReader(file));
             FileWriter writer = new FileWriter(newProfileFile);
             String readLine = "";
             String contents = "";
             
             while ((readLine = reader.readLine()) != null) 
             {
                 if(readLine.contains(match))
                 {
                     if(readLine.charAt(readLine.length() - 1) == '=')
                     {
                         contents = match + extension;
                     }
                     
                     else
                     {
                         contents = match + readLine.substring(readLine.indexOf('=') + 1, readLine.length()) + "," + extension;
                     }
                     
                     writer.write(contents + "\n");
                 }
                 
                 else
                 {
                     writer.write(readLine + "\n");
                 }
             }

             writer.close();
             reader.close();
             
             file.delete();
             newProfileFile.renameTo(new File(extensionsProfileFile));
         } 
         
         catch (IOException error) {
             error.printStackTrace();
         }
    }

    /**
     * Loads the profile.
     * 
     * @param coreAgentConfig the location of the profile.
     * @return The created configuration file.
     */
    protected ConfigurationFile loadConfigFile(File coreAgentConfig) {
        File agentConfigFile = FileUtils.getFile(coreAgentConfig, "config", configFile);

        LOGGER.info("Reading config file {}", agentConfigFile.getAbsolutePath());
        return new ConfigurationFileFactory().create(agentConfigFile);
    }

    /**
     * Configures the agent.
     * 
     * @param agentInstallDir The installation directory of the agent.
     */
    protected void configureAgent() {
        File coreAgentConfig = new File(iaHome.getAbsolutePath(), "core");
        ConfigurationFile agentProfile = loadConfigFile(coreAgentConfig);
        configureEm(agentProfile);
        configureInstrumentation(agentProfile);
        configureProperties(agentProfile);
    }

    /**
     * Configures the profile with the new EM connection property.
     * 
     * @param agentConfig The agent profile.
     */
    @Override
    protected void configureEm(ConfigurationFile agentConfig) {
        if (getContext().getEmHost() == null) {
            return;
        }

        String propertyKey = "agentManager.url.1";
        String src = agentConfig.getString(propertyKey);
        String dst = String.valueOf(getContext().getEmHost() + ":" + getContext().getEmPort());

        LOGGER.info("Changing value of {} from {} to {}", propertyKey, src, dst);
        agentConfig.setProperty(propertyKey, dst);
    }

    /**
     * Updates the permissions for java to
     * ensure that the UMAgent can be run.
     */
    private void updateJREPermissions() {
        String installDir = iaHome.getAbsolutePath();
        String fileName = installDir + File.separator + "jre" + File.separator + "bin" + File.separator + "java";
        new File(fileName).setExecutable(true);
    }
}
