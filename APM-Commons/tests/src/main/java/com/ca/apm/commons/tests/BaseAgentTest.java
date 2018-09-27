package com.ca.apm.commons.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.testng.Assert;

import com.ca.apm.automation.action.flow.FlowConfig.FlowConfigBuilder;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.TimeSyncFlow;
import com.ca.apm.automation.action.flow.utility.TimeSyncFlowContext;
import com.ca.apm.automation.action.flow.utility.XmlModifierFlow;
import com.ca.apm.automation.action.flow.utility.XmlModifierFlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.automation.action.flow.webapp.jboss.DeployJbossFlowContext;
import com.ca.apm.automation.action.test.ClwUtils;
import com.ca.apm.automation.action.test.EmUtils;
import com.ca.apm.automation.utils.file.TasFileNameFilter;
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.commons.flow.CheckFileExistenceFlow;
import com.ca.apm.commons.flow.CheckFileExistenceFlowContext;
import com.ca.apm.commons.flow.CheckLogKeywordFlow;
import com.ca.apm.commons.flow.CheckLogKeywordFlowContext;
import com.ca.apm.commons.flow.CheckLogKeywordFlowOneTimeCounter;
import com.ca.apm.commons.flow.CheckLogKeywordFlowOneTimeCounterContext;
import com.ca.apm.commons.flow.FailoverModifierFlow;
import com.ca.apm.commons.flow.FailoverModifierFlowContext;
import com.ca.apm.commons.flow.FileBackupFlow;
import com.ca.apm.commons.flow.FileBackupFlowContext;
import com.ca.apm.commons.flow.RunHvrAgentFlow;
import com.ca.apm.commons.flow.RunHvrAgentFlowContext;
import com.ca.apm.commons.flow.StopServiceFlow;
import com.ca.apm.commons.flow.StopServiceFlowContext;
import com.ca.apm.commons.flow.XMLModifierFlow;
import com.ca.apm.commons.flow.XMLModifierFlowContext;
import com.ca.tas.client.AutomationAgentClientFactory;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.envproperty.MachineEnvironmentProperties;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.webapp.JbossRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.test.TasTestNgTest;


public class BaseAgentTest extends TasTestNgTest {
    private static Logger LOGGER = Logger.getLogger(BaseAgentTest.class);
    CLWCommons clw = new CLWCommons();
    TestUtils testutils = new TestUtils();

    /**
     * Starts the EM Service.
     * 
     * @param emRoleId
     */
    public void startEM(String emRoleId) {
        try {
            runSerializedCommandFlowFromRole(emRoleId, EmRole.ENV_START_EM);
        } catch (Exception e) {
            LOGGER.error("Already started");
        }
    }

    /**
     * Stops the EM Service
     * 
     * @param emRoleId
     */
    public void stopEM(String emRoleId) {
        LOGGER.debug("Shutting down MOM");
        EmUtils emUtils = utilities.createEmUtils();
        ClwUtils clwUtilsEM = utilities.createClwUtils(emRoleId);
        try {
            emUtils.stopLocalEmWithTimeoutSec(clwUtilsEM.getClwRunner(), emRoleId, 240);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(" Improper Shutdown MOM");
        }
    }

    /**
     * Stops the specified collector service connected to the MOM
     * 
     * @param momRoleId
     * @param CollectorRoleId
     */
    public void stopCollectorEM(String momRoleId, String CollectorRoleId) {
        EmUtils emUtils = utilities.createEmUtils();

        ClwUtils clwUtilsMOM = utilities.createClwUtils(momRoleId);
        ClwUtils clwUtilsCOL = utilities.createClwUtils(CollectorRoleId);
        try {
            emUtils.stopRemoteEmWithTimeoutSec(clwUtilsMOM.getClwRunner(),
                clwUtilsCOL.getClwRunner(), 240);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(" Improper Shutdown COLLECTOR");
        }


    }

    /**
     * Restart EM. Applicable for the role where the automation RUNS. In general, all the automation
     * runs on EM machine or MOM machine.
     * 
     * @param emRoleId
     */
    public void restartEM(String emRoleId) {
        stopEM(emRoleId);
        startEM(emRoleId);
    }

    /**
     * Waits for the specified number of seconds.
     * 
     * @param seconds
     */
    public void harvestWait(int seconds) {
        try {
            LOGGER.info("Harvesting crops for " + String.valueOf(seconds) + " seconds");
            Thread.sleep(seconds * 1000);
            LOGGER.info("Crops harvested.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the tomcat Service for the given tomcat role. App Server can be on any machine in the
     * testbed.
     * 
     * @param tomcatRoleId
     */
    public void startTomcatAgent(String tomcatRoleId) {
        try {
            runSerializedCommandFlowFromRole(tomcatRoleId, TomcatRole.ENV_TOMCAT_START);
        } catch (Exception e) {
            LOGGER.error("Already started");
        }
    }

    /**
     * Starts the jboss service for the given jboss role. App Server can be on any machine in the
     * testbed.
     * 
     * @param jBossRoleId
     */
    public void startJBossAgent(String jBossRoleId) {
        try {
            runSerializedCommandFlowFromRole(jBossRoleId, JbossRole.ENV_JBOSS_START);
        } catch (Exception e) {
            LOGGER.error("Already started");
        }
    }

    /**
     * Stops the tomcat service for the given tomcat role. App Server can be on any machine in the
     * testbed.
     * 
     * @param tomcatRoleId
     */
    public void stopTomcatAgent(String tomcatRoleId) {
        try {
            runSerializedCommandFlowFromRole(tomcatRoleId, TomcatRole.ENV_TOMCAT_STOP);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(" Improper Shutdown TOMCAT AGENT");
        }
    }

    /**
     * Stops the jboss service for the given jboss role. App Server can be on any machine in the
     * testbed.
     * 
     * @param jBossRoleId
     */
    public void stopJBossAgent(String jBossRoleId) {
        try {
            runSerializedCommandFlowFromRole(jBossRoleId, JbossRole.ENV_JBOSS_STOP);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(" Improper Shutdown JBOSS AGENT");
        }
    }

    /**
     * Replace a certain string entirely with the new String that is provided. This is often used
     * for replacing properties. File can be on any machine and hence we provide the machine ID.
     * 
     * @param oldProp
     * @param newProp
     * @param machineID
     * @param filePath
     */

    public void replaceProp(String oldProp, String newProp, String machineID, String filePath) {

        FileModifierFlowContext propertyChange =
            new FileModifierFlowContext.Builder().replace(filePath,
                Collections.singletonMap(oldProp, newProp)).build();

        runFlowByMachineId(machineID, FileModifierFlow.class, propertyChange);

    }

    /**
     * Replace a certain string entirely with the new String that is provided. This is often used
     * for replacing properties. File can be on any machine and hence we provide the machine ID.
     * 
     * @param oldProp
     * @param newProp
     * @param machineID
     * @param filePath
     */

    public void replaceProp(Map<String, String> propValues, String machineID, String filePath) {

        FileModifierFlowContext propertyChange =
            new FileModifierFlowContext.Builder().replace(filePath,
                    propValues).build();

        runFlowByMachineId(machineID, FileModifierFlow.class, propertyChange);

    }
    
    /**
     * Append a list of Strings (used for properties) at the end of the file. File can be on any
     * machine and hence we provide the machine ID.
     * 
     * @param newProp
     * @param machineID
     * @param filePath
     */

    public void appendProp(List<String> newProp, String machineID, String filePath) {

        FileModifierFlowContext propertyChange =
            new FileModifierFlowContext.Builder().append(filePath, newProp).build();

        runFlowByMachineId(machineID, FileModifierFlow.class, propertyChange);
    }

    /**
     * Takes the backup of a file on any machine. File can be on any machine and hence we provide
     * the machine ID. File cannot be backed up to a different machine.
     * 
     * @param orgFile
     * @param bkpFile
     * @param machineID
     */
    public void backupFile(String orgFile, String bkpFile, String machineID) {
        FileModifierFlowContext FMF =
            new FileModifierFlowContext.Builder().copy(orgFile, bkpFile).build();
        runFlowByMachineId(machineID, FileModifierFlow.class, FMF);
    }


    /**
     * Copy a file on any machine. File can be on any machine and hence we provide the machine ID.
     * File can be copied to a different location in the same machine.
     *
     * @param srcFile
     * @param destinationFile
     * @param machineID
     * @throws Exception
     */
    public void copyFile(String srcFile, String destinationFile, String machineID) throws Exception {
        FileModifierFlowContext FMF =
            new FileModifierFlowContext.Builder().copy(srcFile, destinationFile).build();
        runFlowByMachineId(machineID, FileModifierFlow.class, FMF);
    }

    /**
     * Takes the backup fileName, original File name and restores it. File can be on any machine and
     * hence we provide the machine ID.
     * 
     * @param bkpFile
     * @param origFile
     * @param machineID
     */
    public void restoreFile(String bkpFile, String origFile, String machineID) {
        FileModifierFlowContext FMF =
            new FileModifierFlowContext.Builder().copy(bkpFile, origFile).build();
        runFlowByMachineId(machineID, FileModifierFlow.class, FMF);
    }

    /**
     * Delete a particular file. File can be on any machine and hence we provide the machine ID.
     * 
     * @param orgFile
     * @param machineID
     */

    public void deleteFile(String orgFile, String machineID) {
        FileModifierFlowContext FMF = new FileModifierFlowContext.Builder().delete(orgFile).build();
        runFlowByMachineId(machineID, FileModifierFlow.class, FMF);
    }

    public void deleteFilteredFiles(String dir, TasFileNameFilter filter, String machineId) {
        FileModifierFlowContext FMF =
            new FileModifierFlowContext.Builder().deleteFiltered(dir, filter).build();
        runFlowByMachineId(machineId, FileModifierFlow.class, FMF);
    }

    /**
     * To perform a move operation on a file.
     * 
     * @param bkpFile
     * @param orgFile
     * @param machineID
     */
    public void moveFile(String bkpFile, String orgFile, String machineID) {
        FileModifierFlowContext rePlaceFile =
            new FileModifierFlowContext.Builder().move(bkpFile, orgFile).build();
        runFlowByMachineId(machineID, FileModifierFlow.class, rePlaceFile);
    }

    /**
     * Rename the log file with the test ID.
     * 
     * @param testID
     * @param logFile
     * @param MachineID
     */
    public void renameLogWithTestID(String testID, String logFile, String MachineID) {
        backupFile(logFile, logFile + testID, MachineID);
        deleteFile(logFile, MachineID);
        moveFile(logFile + "_1", logFile, MachineID);

    }

    public void takeBackupAndDelete(String logFile, String MachineID) {
        backupFile(logFile, logFile + "_1", MachineID);
        deleteFile(logFile, MachineID);
    }

    public void revertFile(String file, String origFile, String machineID) {
        deleteFile(file, machineID);
        backupFile(origFile, file, machineID);
    }

    /**
     * Sync up of time happens for the list of machineIds that are sent.
     * 
     * @param machineIds
     */
    public void syncTimeOnMachines(Collection<String> machineIds) {
        for (String machineId : machineIds) {
            runFlowByMachineId(machineId, TimeSyncFlow.class,
                new TimeSyncFlowContext.Builder().build());
        }
    }

    /**
     * 
     * 
     * @param hostName
     * @return
     * @throws IOException
     */
    
    public String returnIPforGivenHost(String hostName) throws IOException {
        return testutils.returnIPforGivenHost(hostName);
    }

    public void renameFile(String orgFile, String bkpFile, String machineID) {

        moveFile(orgFile, bkpFile, machineID);
    }

    public void waitForAgentNodes(String expression, String emHost, int emPort, String emLibDir) {
        clw.waitForAgentNodes(expression, emHost, emPort, emLibDir);
    }

    public void startHVRAgent(String momhost, int momPort, String hvrLoc, String hvrMachineId,
        String fileToLoad, String cloneconnections, String cloneagents, String secondsPerTrace) {
        LOGGER.info("Starting HVR Agent");

        try {
            RunHvrAgentFlowContext.Builder runHvrContextbuilder =
                new RunHvrAgentFlowContext.Builder();
            runHvrContextbuilder.emHost(momhost).emPort(momPort)
                .hvrAgentInstallationDirectory(hvrLoc).fileToLoad(fileToLoad)
                .cloneconnections(cloneconnections).cloneagents(cloneagents)
                .secondspertrace(secondsPerTrace).action("start").build();
            RunHvrAgentFlowContext runHvrContext = new RunHvrAgentFlowContext(runHvrContextbuilder);
            runFlowByMachineId(hvrMachineId, RunHvrAgentFlow.class, runHvrContext);
        } catch (Exception e) {
            LOGGER.error("Error occurred while starting the HVR Agent");
        }
    }

    public void stopHVRAgent(String momhost, int momPort, String hvrLoc, String hvrMachineId,
        String fileToLoad, String cloneconnections, String cloneagents, String secondsPerTrace) {
        LOGGER.info("Stopping HVR Agent");
        try {
            RunHvrAgentFlowContext.Builder runHvrContextbuilder =
                new RunHvrAgentFlowContext.Builder();
            runHvrContextbuilder.emHost(momhost).emPort(momPort)
                .hvrAgentInstallationDirectory(hvrLoc).fileToLoad(fileToLoad)
                .cloneconnections(cloneconnections).cloneagents(cloneagents)
                .secondspertrace(secondsPerTrace).action("stop").build();
            RunHvrAgentFlowContext runHvrContext = new RunHvrAgentFlowContext(runHvrContextbuilder);
            runFlowByMachineId(hvrMachineId, RunHvrAgentFlow.class, runHvrContext);
        } catch (Exception e) {
            LOGGER.error("Could not successfully stop the HVR Agent");
        }

    }

    /**
     * Check if keyword exists in log at the given logPath, If not throws exception
     */
    public void isKeywordInFile(EnvironmentPropertyContext envProps, String machineId,
        String logPath, String keyword) throws Exception {
        List<String> args = new ArrayList<String>();
        args.add(logPath);
        args.add(keyword);
        System.out.println("Arguments List: " + args);
        CheckLogKeywordFlowContext logCheckFlowContext =
            new CheckLogKeywordFlowContext.Builder().arguments(args).methodName("isKeywordInFile")
                .build();
        runFlowByMachineId(machineId, CheckLogKeywordFlow.class, logCheckFlowContext);
    }

    /**
     * Check if keyword exists only one time in log at the given logPath, If not throws exception
     */
    public void isKeywordInFileOneTimeCounter(EnvironmentPropertyContext envProps,
        String machineId, String logPath, String keyWord) throws Exception {
        final IAutomationAgentClient aaClient = new AutomationAgentClientFactory(envProps).create();
        CheckLogKeywordFlowOneTimeCounterContext logCheckFlowContext =
            new CheckLogKeywordFlowOneTimeCounterContext(logPath, keyWord);

        final String hostnameWithPort =
            envProps.getMachinePropertyById(machineId,
                MachineEnvironmentProperties.HOSTNAME_WITH_PORT);

        aaClient.runJavaFlow(new FlowConfigBuilder(CheckLogKeywordFlowOneTimeCounter.class,
            logCheckFlowContext, hostnameWithPort));
    }

    /**
     * Test method is used before starting of every testcase.
     * 
     * @param testCaseNameIDPath
     *        -Passing Testcase ID
     */
    public void testCaseStart(String testCaseNameIDPath) {
        LOGGER.info("##########" + testCaseNameIDPath + "#########" + "Start");
    }

    /**
     * Test method is used at the end of every Test case.
     * 
     * -Passing Test case ID
     */
    public void testCaseEnd(String testCaseNameIDPath) {
        LOGGER.info("##########" + testCaseNameIDPath + "#########" + "End");
    }

    /**
     *
     * @param machineId
     * @param logPath
     * @param keyWord
     * @throws Exception
     */
    public void checkLogForMsg(EnvironmentPropertyContext envProps, String machineId,
        String logPath, String keyWord) {
        int i = 0;
        boolean flag = false;
        for (i = 0; i < 80; i++) {
            try {
                isKeywordInFile(envProps, machineId, logPath, keyWord);
                flag = true;
                break;
            } catch (Exception e) {
                if (i < 80) {
                    harvestWait(15);
                    continue;
                }
            }
        }
        LOGGER
            .info("The message is found after " + i + " iterations taking " + i * 15 + " seconds");
        Assert.assertTrue(flag);
    }

    /**
     * Checks for non existence of specified keyword in the given file at the instance
     * @param envProps
     * @param machineId
     * @param logPath
     * @param keyWord
     */
    public void checkLogForNoMsg(EnvironmentPropertyContext envProps, String machineId,
        String logPath, String keyWord) {
        boolean flag = false;
        try {
                isKeywordInFile(envProps, machineId, logPath, keyWord);
                flag = true;
            } catch (Exception e) {
               System.err.println(e);
            }
        LOGGER.info("The specified keyword search status is " + flag);
        Assert.assertFalse(flag);
    }

    /**
     *
     * @param machineId
     * @param logPath
     * @param keyWord
     * @throws Exception
     */
    public boolean checkForKeyword(EnvironmentPropertyContext envProps, String machineId,
        String logPath, String keyWord) {
        int i = 0;
        boolean flag = false;
        for (i = 0; i < 80; i++) {
            try {
                isKeywordInFile(envProps, machineId, logPath, keyWord);
                flag = true;
                break;
            } catch (Exception e) {
                if (i < 80) {
                    harvestWait(15);
                    continue;
                }
            }
        }
        return flag;
    }

    /**
     * Checks for a keyword, based on flag no. of iterations change
     * 
     * @param envProps
     * @param machineId
     * @param logPath
     * @param keyWord
     * @param state
     * @return
     */
    public boolean checkForKeyword(EnvironmentPropertyContext envProps, String machineId,
        String logPath, String keyWord, boolean state) {
        int i = 0;
        int iteration;
        boolean flag = false;
        if (state) {
            iteration = 80;
        } else {
            iteration = 20;
        }

        for (i = 0; i < iteration; i++) {
            try {
                isKeywordInFile(envProps, machineId, logPath, keyWord);
                flag = true;
                break;
            } catch (Exception e) {
                if (i < iteration) {
                    harvestWait(15);
                    continue;
                }
            }
        }
        return flag;
    }

    /**
     * 
     /**
     * 
     * @param roleIds
     * @param testCaseId
     */
    public void renameLogWithTestCaseId(List<String> roleIds, String testCaseId) {
        String LogFile = "";
        for (String roleId : roleIds) {
            if (roleId.toLowerCase().contains("tomcat")) {
                LogFile =
                    envProperties.getRolePropertyById(roleId,
                        DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                        + "/wily/logs/IntroscopeAgent.log";
            } else if (roleId.toLowerCase().contains("jboss")) {
                LogFile =
                    envProperties.getRolePropertyById(roleId,
                        DeployJbossFlowContext.ENV_JBOSS_INSTALL_DIR)
                        + "/wily/logs/IntroscopeAgent.JBoss_Agent.log";
            } else
                LogFile =
                    envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_LOG_FILE);
            System.out.println("Now renaming the file" + LogFile);
            renameFile(LogFile, LogFile + "_" + testCaseId,
                envProperties.getMachineIdByRoleId(roleId));
        }
    }

    public void renamePropertyFilesWithTestCaseId(List<String> roleIds, String testCaseId) {
        String propFile = "";
        for (String roleId : roleIds) {
            if (roleId.toLowerCase().contains("tomcat")) {
                propFile =
                    envProperties.getRolePropertyById(roleId,
                        DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                        + "/wily/core/config/IntroscopeAgent.profile";;
            } else if (roleId.toLowerCase().contains("jboss")) {
                propFile =
                    envProperties.getRolePropertyById(roleId,
                        DeployJbossFlowContext.ENV_JBOSS_INSTALL_DIR)
                        + "/wily/core/config/IntroscopeAgent.profile";
            } else {
                propFile =
                    envProperties.getRolePropertyById(roleId,
                        DeployEMFlowContext.ENV_EM_CONFIG_FILE);
            }
            renameFile(propFile, propFile + "_" + testCaseId,
                envProperties.getMachineIdByRoleId(roleId));
        }
    }

    public void restorePropFiles(List<String> roleIds) {
        String propFile = "";
        for (String roleId : roleIds) {
            if (roleId.toLowerCase().contains("tomcat")) {
                propFile =
                    envProperties.getRolePropertyById(roleId,
                        DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                        + "/wily/core/config/IntroscopeAgent.profile";
            } else if (roleId.toLowerCase().contains("jboss")) {
                propFile =
                    envProperties.getRolePropertyById(roleId,
                        DeployJbossFlowContext.ENV_JBOSS_INSTALL_DIR)
                        + "/wily/core/config/IntroscopeAgent.profile";
            } else
                propFile =
                    envProperties.getRolePropertyById(roleId,
                        DeployEMFlowContext.ENV_EM_CONFIG_FILE);
            restoreFile(propFile + "_backup", propFile, envProperties.getMachineIdByRoleId(roleId));
        }
    }

    /**
     * Check in the log to see if there is at least one key word from the list of keywords
     *
     * @param machineId
     * @param logPath
     * @param keyWords
     */
    public void verifyIfAtleastOneKeywordIsInLog(String machineId, String logPath,
        List<String> keyWords) {
        int i = 0;
        boolean flag = false;
        for (i = 0; i < 80; i++) {
            try {
                for (String keyWord : keyWords) {
                    try {
                        isKeywordInFile(envProperties, machineId, logPath, keyWord);
                        flag = true;
                        break;
                    } catch (Exception e) {
                        continue;
                    }
                }
                if (flag)
                    break;
                else {
                    if (i < 80) {
                        harvestWait(15);
                        continue;
                    }
                }
            } catch (Exception e) {
                LOGGER.info("Some exception occurred");
            }
        }
        LOGGER.info("The message is found after " + (i + 1) + " iterations taking " + (i + 1) * 15
            + " seconds");
        Assert.assertTrue(flag);
    }

    /**
     * Check in the log to see if there is at least one key word from the list of keywords
     *
     * @param machineId
     * @param logPath
     * @param keyWords
     */
    public boolean verifiesIfAtleastOneKeywordIsInLog(String machineId, String logPath,
        List<String> keyWords) {
        int i = 0;
        boolean flag = false;
        for (i = 0; i < 80; i++) {
            try {
                for (String keyWord : keyWords) {
                    try {
                        isKeywordInFile(envProperties, machineId, logPath, keyWord);
                        flag = true;
                        break;
                    } catch (Exception e) {
                        continue;
                    }
                }
                if (flag)
                    break;
                else {
                    if (i < 80) {
                        harvestWait(15);
                        continue;
                    }
                }
            } catch (Exception e) {
                LOGGER.info("Some exception occurred");
            }
        }
        LOGGER.info("The message is found after " + (i + 1) + " iterations taking " + (i + 1) * 15
            + " seconds");
        return flag;
    }

    /**
     * Creates a FILE_backup as a backup in EMs
     * 
     * @param roleIds
     */
    public void backupPropFiles(List<String> roleIds) {
        String propFile = "";
        for (String roleId : roleIds) {
            if (roleId.toLowerCase().contains("tomcat")) {
                propFile =
                    envProperties.getRolePropertyById(roleId,
                        DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                        + "/wily/core/config/IntroscopeAgent.profile";
            } else if (roleId.toLowerCase().contains("jboss")) {
                propFile =
                    envProperties.getRolePropertyById(roleId,
                        DeployJbossFlowContext.ENV_JBOSS_INSTALL_DIR)
                        + "/wily/core/config/IntroscopeAgent.profile";
            } else
                propFile =
                    envProperties.getRolePropertyById(roleId,
                        DeployEMFlowContext.ENV_EM_CONFIG_FILE);
            backupFile(propFile, propFile + "_backup", envProperties.getMachineIdByRoleId(roleId));
        }
    }

    /**
     * Stop the tomcat service
     * 
     * @param machineId
     */
    public void stopTomcatServiceFlowExecutor(String machineId) {

        StopServiceFlowContext.Builder stopServiceFlowContextBuilder =
            new StopServiceFlowContext.Builder();
        stopServiceFlowContextBuilder.processToKill("tomcat").build();
        StopServiceFlowContext ssf = new StopServiceFlowContext(stopServiceFlowContextBuilder);

        runFlowByMachineId(machineId, StopServiceFlow.class, ssf);
    }
    
    /**
     * Stop the JBoss service
     * 
     * @param machineId
     */
    public void stopJBossServiceFlowExecutor(String machineId) {

        StopServiceFlowContext.Builder stopServiceFlowContextBuilder =
            new StopServiceFlowContext.Builder();
        stopServiceFlowContextBuilder.processToKill("jboss").build();
        StopServiceFlowContext ssf = new StopServiceFlowContext(stopServiceFlowContextBuilder);

        runFlowByMachineId(machineId, StopServiceFlow.class, ssf);
    }

    /**
     * Stop the EM service
     * 
     * @param machineId
     */
    public void stopEMServiceFlowExecutor(String machineId) {

        StopServiceFlowContext.Builder stopServiceFlowContextBuilder =
            new StopServiceFlowContext.Builder();
        stopServiceFlowContextBuilder.processToKill("Introscope_Enterprise_Manager.lax").build();
        StopServiceFlowContext ssf = new StopServiceFlowContext(stopServiceFlowContextBuilder);

        runFlowByMachineId(machineId, StopServiceFlow.class, ssf);
    }

    /**
     * Update the EM Jetty file and enable EM Jetty in EM properties
     * to enable HTTP Communication
     *
     * @param roleIds
     */
    public void enableHTTPOnEM(List<String> roleIds) {
        for (String roleId : roleIds) {
            String emJetty =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                    + "/em-jetty-config.xml";
            String configFile =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
            addHTTPEntryInEMJetty(emJetty, envProperties.getMachineIdByRoleId(roleId));
            replaceProp(
                "#introscope.enterprisemanager.webserver.jetty.configurationFile=em-jetty-config.xml",
                "introscope.enterprisemanager.webserver.jetty.configurationFile=em-jetty-config.xml",
                envProperties.getMachineIdByRoleId(roleId), configFile);
        }
    }

    /**
     * Take a backup of the file and
     * Add the HTTP Entry in EM Jetty File
     *
     * @param emJettyFile
     * @param emMachineId
     */
    public void addHTTPEntryInEMJetty(String emJettyFile, String emMachineId) {
        backupFile(emJettyFile, emJettyFile + "_backup", emMachineId);
        String OrigString =
            "<!DOCTYPE Configure PUBLIC \"-//Mort Bay Consulting//DTD Configure//EN\" \"http://jetty.mortbay.org/configure.dtd\">";
        String stringToReplace =
            "<!DOCTYPE Configure PUBLIC \"-//Mort Bay Consulting//DTD Configure//EN\" \"http://www.eclipse.org/jetty/configure.dtd\">";
        replaceProp(OrigString, stringToReplace, emMachineId, emJettyFile);
        List<String> args = new ArrayList<String>();
        args.add(emJettyFile);
        try {
            XMLModifierFlowContext modifyXML =
                new XMLModifierFlowContext.Builder().arguments(args)
                    .methodName("addHttpEntryInEMJetty").build();

            runFlowByMachineId(emMachineId, XMLModifierFlow.class, modifyXML);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Take a backup of the file and
     * Add the HTTP Entry in EM Jetty File
     *
     * @param emJettyFile
     * @param emMachineId
     * @param emWebPort
     */
    public void addCustomHTTPEntryInEMJetty(String emJettyFile, String emMachineId, String emWebPort) {
        backupFile(emJettyFile, emJettyFile + "_backup", emMachineId);
        String OrigString =
            "<!DOCTYPE Configure PUBLIC \"-//Mort Bay Consulting//DTD Configure//EN\" \"http://jetty.mortbay.org/configure.dtd\">";
        String stringToReplace =
            "<!DOCTYPE Configure PUBLIC \"-//Mort Bay Consulting//DTD Configure//EN\" \"http://www.eclipse.org/jetty/configure.dtd\">";
        replaceProp(OrigString, stringToReplace, emMachineId, emJettyFile);
        List<String> args = new ArrayList<String>();
        args.add(emJettyFile);
        args.add(emWebPort);
        try {
            XMLModifierFlowContext modifyXML =
                new XMLModifierFlowContext.Builder().arguments(args)
                    .methodName("addHttpEntryInEMJetty").build();

            runFlowByMachineId(emMachineId, XMLModifierFlow.class, modifyXML);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Get the count of disallowed agents
     * Hover in a loop until disallowed count comes up as they might not show up in the first
     * instance.
     * 
     * @param emHost
     * @param emPort
     * @param emLibPath
     * @return
     */
    public int getNumberOfDisallowedAgents(String emHost, int emPort, String emLibPath) {
        return clw.getNumberOfDisallowedAgents(emHost, emPort, emLibPath);
    }

    /**
     * Get the count of disallowed agents at Collector
     * Hover in a loop until disallowed count comes up as they might not show up in the first
     * instance.
     * 
     * @param emHost
     * @param emPort
     * @param emLibPath
     * @return
     *         Ex:*SuperDomain*|Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom
     *         Metric Agent (Virtual) (tas-itc-n9@5001)|Enterprise Manager|Connections:Number of
     *         Disallowed Agents
     */
    public int getNumberOfDisallowedAgentsAtCollector(String collectorHost, int collectorPort,
        String emLibPath) {
        return clw.getNumberOfDisallowedAgentsAtCollector(collectorHost, collectorPort, emLibPath);
    }


    /**
     * Validate count of disallowed agents
     * Hover in a loop until disallowed count comes up as they might not show up in the first
     * instance.
     * 
     * @param emHost
     * @param emPort
     * @param emLibPath
     * @param count
     * @return
     */
    public int validateNumberOfDisallowedAgents(String emHost, int emPort, String emLibPath,
        int count) {
        return clw.validateNumberOfDisallowedAgents(emHost, emPort, emLibPath, count);
    }


    /**
     * Get disallowed agents metrics
     * Hover in a loop until disallowed count comes up as they might not show up in the first
     * instance.
     * 
     * @param emHost
     * @param emPort
     * @param emLibPath
     * @param agentExpression
     * @param metricExpression
     * @return
     */
    public int getDisallowedAgentMetrics(String emHost, int emPort, String emLibPath,
        String agentExpression, String metricExpression) {
        return clw.getDisallowedAgentMetrics(emHost, emPort, emLibPath, agentExpression,
            metricExpression);
    }


    /**
     * validate disallowed agents metrics
     * Hover in a loop until disallowed count comes up as they might not show up in the first
     * instance.
     * 
     * @param emHost
     * @param emPort
     * @param emLibPath
     * @param agentExpression
     * @param metricExpression
     * @return
     */
    public int validateDisallowedAgentMetricsByCount(String emHost, int emPort, String emLibPath,
        String agentExpression, String metricExpression, int count) {
        return clw.validateDisallowedAgentMetricsByCount(emHost, emPort, emLibPath,
            agentExpression, metricExpression, count);
    }


    /**
     * Get the list of metrics for the agent which is connected to the EM Host
     *
     * @param agentExpression
     * @param metricExpression
     * @param emHost
     * @param emPort
     * @param emLibDir
     * @return
     */
    public List<String> getConnectedAgentMetricForEMHost(String agentExpression,
        String metricExpression, String emHost, int emPort, String emLibDir) {
        return clw.getConnectedAgentMetricForEMHost(agentExpression, metricExpression, emHost,
            emPort, emLibDir);
    }


    /**
     * Check the connectivity between MOM and the specified COLLECTOR
     *
     * @param agentExpression
     * @param metricExpression
     * @param emhost
     * @param emPort
     * @param emLibDir
     */
    public void checkSpecificCollectorToMOMConnectivity(String agentExpression,
        String metricExpression, String emhost, String emPort, String emLibDir) {
        clw.checkCollectorToMOMConnectivity(agentExpression, metricExpression, emhost, emPort,
            emLibDir);
    }

    /**
     * Get the agent metrics for a specified host
     * waits a maximum time of 1200 seconds for the
     * value to be obtained and checks every 15 seconds for the result.
     * 
     * @param agentExpression
     * @param metricExpression
     * @param emHost
     * @param emPort
     * @param emLibDir
     * @return
     */
    public List<String> getAgentMetricsForEMHost(String agentExpression, String metricExpression,
        String emHost, int emPort, String emLibDir) {
        return clw.getAgentMetricsForEMHost(agentExpression, metricExpression, emHost, emPort,
            emLibDir);
    }

    /**
     * Get the name of the collector for the given agent
     * 
     * @param collectorNames
     * @param collectorPorts
     * @param collector_RoleIDs
     * @param agentName
     * @param roleOrHost
     * @param emLibDir
     * @return
     */
    public String getAgentConnectedCollectorName(List<String> collectorNames,
        List<Integer> collectorPorts, List<String> collector_RoleIDs, String agentName,
        String roleOrHost, String emLibDir) {
        return clw.getAgentConnectedCollectorName(collectorNames, collectorPorts,
            collector_RoleIDs, agentName, roleOrHost, emLibDir);
    }

    /**
     * Get the agent expression for the given EMHost, connected to that agent.
     * 
     * @param emHost
     * @param emPort
     * @param expression
     * @param emLibDir
     * @return
     */
    public List<String> getConnectedAgentsExpressionToEMHost(String emHost, int emPort,
        String expression, String emLibDir) {
        return clw.getConnectedAgentsExpressionToEMHost(emHost, emPort, expression, emLibDir);
    }

    /**
     * Get the agent name for the given EMHost, connected to that agent.
     * 
     * @param emHost
     * @param emPort
     * @param expression
     * @param emLibDir
     * @return
     */
    public List<String> getConnectedAgentNamesToEMHost(String emHost, int emPort,
        String expression, String emLibDir) {
        return clw.getConnectedAgentNamesToEMHost(emHost, emPort, expression, emLibDir);
    }


    /**
     * Get the agent expression for the given EMHost, connected to that agent.
     * 
     * @param emHost
     * @param emPort
     * @param expression
     * @param emLibDir
     * @return
     */
    public List<String> ValidateCountForConnectedAgentsExpressionToEMHost(String emHost,
        int emPort, String expression, String emLibDir, int count) {
        int i = 0;
        List<String> nodeList = null;
        for (i = 0; i < 80; i++) {
            nodeList = clw.getNodeList("Admin", "", expression, emHost, emPort, emLibDir);
            if (nodeList.size() == count)
                break;
            else
                harvestWait(15);
        }
        LOGGER.info("Agent connected to EM after " + i + " iterations taking " + i * 15
            + " seconds");
        return nodeList;
    }

    /**
     * Rename the log on the given machine with testCaseId.
     * 
     * @param LogFile
     * @param machineId
     * @param testCaseId
     */
    public void renameLogWithTestCaseID(String LogFile, String machineId, String testCaseId) {
        backupFile(LogFile, LogFile + "_" + testCaseId, machineId);
        deleteFile(LogFile, machineId);
    }

    /**
     * Check the file for presence of all keywords from the list of keywords
     * 
     * @param envProps
     * @param machineId
     * @param logPath
     * @param keyWords
     * @return
     */
    public boolean isKeywordInFile(EnvironmentPropertyContext envProps, String machineId,
        String logPath, List<String> keyWords) {
        boolean flag = false;
        int i = 0;
        for (String keyWord : keyWords) {
            flag = false;
            for (i = 0; i < 20; i++) {
                try {
                    isKeywordInFile(envProps, machineId, logPath, keyWord);
                    flag = true;
                    break;
                } catch (Exception e) {
                    if (i < 20) {
                        harvestWait(15);
                        continue;
                    }
                }
            }
        }
        return flag;
    }

    /**
     * Looks through file and checks if keywords appears in sequence. At least 2
     * keywords should be passed as arguments
     * 
     * @param envProps
     * @param machineId
     * @param logPath
     * @param keywords
     * @throws Exception
     */
    public void checkMessagesInSequence(EnvironmentPropertyContext envProps, String machineId,
        String logPath, String... keywords) throws Exception {
        List<String> args = new ArrayList<String>();
        args.add(logPath);
        for (int i = 0; i < keywords.length; i++)
            args.add(keywords[i]);

        CheckLogKeywordFlowContext logCheckFlowContext =
            new CheckLogKeywordFlowContext.Builder().arguments(args)
                .methodName("checkMessagesInSequence").build();
        runFlowByMachineId(machineId, CheckLogKeywordFlow.class, logCheckFlowContext);

    }

    /**
     * Creates a Map of Host and Port for the EM Roles provided in the list
     * @param emRoles
     * @return emHostPortMap
     */
    public HashMap<String, String> createEMHostPortMap(List<String> emRoles) {
        HashMap<String, String> emHostPortMap = new HashMap<String, String>();
        Iterator<String> itr = emRoles.iterator();
        while (itr.hasNext()) {
            String tempRole = (String) itr.next();
            String tempEmHost = envProperties.getMachineHostnameByRoleId(tempRole);
            String tempEmPort = envProperties.getRolePropertiesById(tempRole).getProperty("emPort");
            emHostPortMap.put(tempEmHost, tempEmPort);
        }

        return emHostPortMap;

    }

    /**
     * Checks the EM list contents in the specified file on given machine with the list 
     * of EM Roles provided
     * @param envProps
     * @param machineId
     * @param logPath
     * @param emRoles
     */
    public void checkEMListContents(EnvironmentPropertyContext envProps, String machineId,
        String logPath, List<String> emRoles) {
    	
    	// the list holds all the arguments required for the flow, first being the filepath 
        List<String> args = new ArrayList<String>();
        HashMap<String, String> emHostPortMap = createEMHostPortMap(emRoles);
        args.add(logPath);

        //Prepares required string to validate in EM list and add to arguments for flow
        for (String key : emHostPortMap.keySet()) {
            String tempEmListElement = key + "@" + emHostPortMap.get(key);
            args.add(tempEmListElement);
        }

        CheckLogKeywordFlowContext checkLogKeywordFlowContext =
            new CheckLogKeywordFlowContext.Builder().arguments(args)
                .methodName("checkEMListContents").build();
        runFlowByMachineId(machineId, CheckLogKeywordFlow.class, checkLogKeywordFlowContext);
    }

    /**
     * Checks the number of elements in latest EM list of the file specified .
     * @param envProps
     * @param machineId
     * @param logPath
     * @param emListSize
     */
    public void checkEMListSize(EnvironmentPropertyContext envProps, String machineId,
        String logPath, String emListSize) {

        //Holds the arguments required for the flow first being filepath and next EM list size
    	List<String> args = new ArrayList<String>();
        args.add(logPath);
        args.add(emListSize);
        
        CheckLogKeywordFlowContext checkLogKeywordFlowContext =
            new CheckLogKeywordFlowContext.Builder().arguments(args)
                .methodName("checkEMListSize").build();
        runFlowByMachineId(machineId, CheckLogKeywordFlow.class, checkLogKeywordFlowContext);
    }
    
    /**
     * Check for the existence of the given file on a machine
     * @param envProps
     * @param machineId
     * @param filePath
     */
    public void checkFileExistence(EnvironmentPropertyContext envProps, String machineId,
        String filePath) {
        CheckFileExistenceFlowContext checkFileExistenceFlowContext =
            new CheckFileExistenceFlowContext.Builder().filePath(filePath).build();
        runFlowByMachineId(machineId, CheckFileExistenceFlow.class, checkFileExistenceFlowContext);
    }

    /**
     * Checks if timestamp value of a String logged as a string-value pair in
     * file is greater than the timestamp passed
     * 
     * @param envProps
     * @param machineId
     * @param logPath
     * @param keywords
     * @throws Exception
     */
    public void checkTimeStampValueOfKeyword(EnvironmentPropertyContext envProps, String machineId,
        String logPath, String keyword, String timestamp) throws Exception {
        List<String> args = new ArrayList<String>();
        args.add(logPath);
        args.add(keyword);
        args.add(timestamp);

        CheckLogKeywordFlowContext logCheckFlowContext =
            new CheckLogKeywordFlowContext.Builder().arguments(args)
                .methodName("checkTimeStampValueOfKeyword").build();
        runFlowByMachineId(machineId, CheckLogKeywordFlow.class, logCheckFlowContext);

    }

    public void testcountmethod(EnvironmentPropertyContext envProps, String machineId,
        String logPath, String keyword, String timestamp) throws Exception {
        List<String> args = new ArrayList<String>();
        args.add(logPath);
        args.add(keyword);
        args.add(timestamp);

        CheckLogKeywordFlowContext logCheckFlowContext =
            new CheckLogKeywordFlowContext.Builder().arguments(args).methodName("testcount")
                .build();
        runFlowByMachineId(machineId, CheckLogKeywordFlow.class, logCheckFlowContext);

    }

    /**
     * sets the agentManager url
     * 
     * @param Host - EM Host to which you want to connect your tomcat agent to
     * @param port - EM port
     */
    public void setAgentManagerUrl(String agentProfileFile, String agentMachineId,
        String emHostOriginal, int portOriginal, String replaceEmHost, int replaceemPort) {

        LOGGER.info("Inside setAgentmanagerUrl");
        replaceProp("agentManager.url.1=" + emHostOriginal + ":" + portOriginal,
            "agentManager.url.1=" + replaceEmHost + ":" + replaceemPort, agentMachineId,
            agentProfileFile);

    }

    /**
     * appends a property with absolute path in its value
     * 
     * @param machineId
     * @param filepath
     * @param propertyname
     * @param propertyvalue
     */
    public void appendPropwithAbsolutePath(String machineId, String filepath, String propertyname,
        String propertyvalue) {

        List<String> keyWords = new ArrayList<String>();
        File path = new File(propertyvalue);
        path.getParentFile().mkdirs();
        String property = propertyname + "=" + path.getAbsolutePath().replace('\\', '/');;
        keyWords.add(property);
        appendProp(keyWords, machineId, filepath);

    }

    /**
     * Adds a new attribute. If an attribute with that name is already present
     * in the element, its value is changed to be that of the value
     * parameter.
     *
     * @param xpathtonode The xpath to a node to change attribute.
     * @param nodeattribute The name of the attribute to create or alter.
     * @param value Value to set in string form.
     * @return this builder
     */

    public void setattributeinapmthresholdXML(String machineId, String xmlFilePath,
        String xpathToNode, String nodeAttribute, String value) {

        XmlModifierFlowContext modifyXML =
            new XmlModifierFlowContext.Builder(xmlFilePath).setAttribute(xpathToNode,
                nodeAttribute, value).build();

        runFlowByMachineId(machineId, XmlModifierFlow.class, modifyXML);
    }

    public void backupEMJettyFiles(List<String> roleIds) {
        String emJettyFile = "";
        for (String roleId : roleIds) {
            emJettyFile =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                    + "/em-jetty-config.xml";
            backupFile(emJettyFile, emJettyFile + "_backup",
                envProperties.getMachineIdByRoleId(roleId));
        }
    }

    public void backupEMLoadBalancingXMLFiles(List<String> roleIds) {
        String loadBalanceFile = "";
        for (String roleId : roleIds) {
            loadBalanceFile =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                    + "/loadbalancing.xml";
            backupFile(loadBalanceFile, loadBalanceFile + "_backup",
                envProperties.getMachineIdByRoleId(roleId));
        }
    }


    public void restoreEMJettyFiles(List<String> roleIds) {
        String emJettyFile = "";
        for (String roleId : roleIds) {
            emJettyFile =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                    + "/em-jetty-config.xml";
            restoreFile(emJettyFile + "_backup", emJettyFile,
                envProperties.getMachineIdByRoleId(roleId));
        }
    }

    public void restoreEMLoadBalancingXMLFiles(List<String> roleIds) {
        String loadBalanceFile = "";
        for (String roleId : roleIds) {
            loadBalanceFile =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                    + "/loadbalancing.xml";
            restoreFile(loadBalanceFile + "_backup", loadBalanceFile,
                envProperties.getMachineIdByRoleId(roleId));
        }
    }

    public void renameEMJettyFilesWithTestCaseId(List<String> roleIds, String testCaseId) {
        String emJettyFile = "";
        for (String roleId : roleIds) {
            emJettyFile =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                    + "/em-jetty-config.xml";
            renameFile(emJettyFile, emJettyFile + "_" + testCaseId,
                envProperties.getMachineIdByRoleId(roleId));
        }
    }

    public void renameEMLoadBalancingXMLFilesWithTestCaseId(List<String> roleIds, String testCaseId) {
        String loadBalanceFile = "";
        for (String roleId : roleIds) {
            loadBalanceFile =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                    + "/loadbalancing.xml";
            renameFile(loadBalanceFile, loadBalanceFile + "_" + testCaseId,
                envProperties.getMachineIdByRoleId(roleId));
        }
    }

    public void backupConfigDir(List<String> roleIds)
    {
        String configDir= "";
        for (String roleId : roleIds) {
            if (roleId.toLowerCase().contains("tomcat")) {
                configDir = envProperties.getRolePropertyById(roleId,
                    DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                    + "/wily/core/config";
            } else if (roleId.toLowerCase().contains("jboss")) {
                configDir =
                    envProperties.getRolePropertyById(roleId,
                        DeployJbossFlowContext.ENV_JBOSS_INSTALL_DIR)
                        + "/wily/core/config";
            } else{
                configDir =
                    envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_INSTALL_DIR)+"/config";
            }
            copyDir(configDir, configDir + "_backup",
                envProperties.getMachineIdByRoleId(roleId));
        }
    }
    
    
    public void restoreConfigDir(List<String> roleIds)
    {
        String configDir= "";
        for (String roleId : roleIds) {
            if (roleId.toLowerCase().contains("tomcat")) {
                configDir = envProperties.getRolePropertyById(roleId,
                    DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                    + "/wily/core/config";
            } else if (roleId.toLowerCase().contains("jboss")) {
                configDir =
                    envProperties.getRolePropertyById(roleId,
                        DeployJbossFlowContext.ENV_JBOSS_INSTALL_DIR)
                        + "/wily/core/config";
            } else{
                configDir =
                    envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_INSTALL_DIR)+"/config";
            }
            copyDir(configDir + "_backup",configDir, 
                envProperties.getMachineIdByRoleId(roleId));
        }
    }
    
    public void renameDirWithTestCaseId(List<String> roleIds, String testCaseId) {
        String LogDir = "";
        String configDir= "";
        for (String roleId : roleIds) {
            if (roleId.toLowerCase().contains("tomcat")) {
                LogDir =
                    envProperties.getRolePropertyById(roleId,
                        DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                        + "/wily/logs";
                configDir = envProperties.getRolePropertyById(roleId,
                    DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                    + "/wily/core/config";
            } else if (roleId.toLowerCase().contains("jboss")) {
                LogDir =
                    envProperties.getRolePropertyById(roleId,
                        DeployJbossFlowContext.ENV_JBOSS_INSTALL_DIR)
                        + "/wily/logs";
                configDir =
                    envProperties.getRolePropertyById(roleId,
                        DeployJbossFlowContext.ENV_JBOSS_INSTALL_DIR)
                        + "/wily/core/config";
                
            } else{
                LogDir =
                    envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_INSTALL_DIR)+"/logs";
                configDir =
                    envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_INSTALL_DIR)+"/config";
            }
            System.out.println("Now renaming the file" + LogDir);
            renameDir(LogDir, LogDir + "_" + testCaseId,
                envProperties.getMachineIdByRoleId(roleId));
            renameDir(configDir, configDir + "_" + testCaseId,
                envProperties.getMachineIdByRoleId(roleId));

        }
    }

    public void copyDir(String srcDir, String destDir, String machineID) {
        List<String> args = new ArrayList<String>();
        args.add(srcDir);
        args.add(destDir);
        FileBackupFlowContext FBF =
            new FileBackupFlowContext.Builder().methodName("copyDir").arguments(args).build();
        runFlowByMachineId(machineID, FileBackupFlow.class, FBF);
    }
    
    public void renameDir(String oldDirName, String newDirName, String machineID) {
        List<String> args = new ArrayList<String>();
        args.add(oldDirName);
        args.add(newDirName);
        
        FileBackupFlowContext FBF =
            new FileBackupFlowContext.Builder().methodName("renameDir").arguments(args).build();
        runFlowByMachineId(machineID, FileBackupFlow.class, FBF);
    }
    
    /**
     * Method to Hit the URL base on OS Type
     * 
     * @param rul
     * @param machineID
     * @return Boolean
     */
    public boolean hitURL(String url, String machineID) {
        List<String> browserInvokeCommand = new ArrayList<String>();

        if (System.getProperty("os.name").toUpperCase().contains("WINDOWS")) {
            browserInvokeCommand.clear();
            browserInvokeCommand.add(" start "
                + "\" %PROGRAMFILES%\\Internet Explorer\\IExplore\" " + url);
            try {
                ApmbaseUtil.runCommand(browserInvokeCommand, "/");
                browserInvokeCommand.clear();
                browserInvokeCommand.add(" start "
                    + "\" %PROGRAMFILES%\\Internet Explorer\\IExplore\" " + url);
                ApmbaseUtil.runCommand(browserInvokeCommand, "/");
                LOGGER.info("Invoking URL in Windows Platform is success");
                return true;

            } catch (IOException e) {
                return false;
            }

        } else {

            browserInvokeCommand.clear();
            browserInvokeCommand.add("watch -n 10 curl  " + url + "/qa-app" + "|sleep 10 |exit");
            try {
                FailoverModifierFlowContext FOM =
                    FailoverModifierFlowContext.remoteMount(browserInvokeCommand, "/");
                runFlowByMachineId(machineID, FailoverModifierFlow.class, FOM);
                LOGGER.info("Invoking URL in non-Windows Platform is success");
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }    
	/**
	 * Method to Hit the URL and returns the response in String format
	 * 
	 * @param URL
	 */

	public String callURLAndReturnResponse(String URL) {
		LOGGER.info("Requeted URL:" + URL);
		StringBuilder sb = new StringBuilder();
		URLConnection urlConn = null;
		InputStreamReader in = null;
		try {
			URL url = new URL(URL);
			urlConn = url.openConnection();
			if (urlConn != null)
				urlConn.setReadTimeout(60 * 1000);
			if (urlConn != null && urlConn.getInputStream() != null) {
				in = new InputStreamReader(urlConn.getInputStream(),
						Charset.defaultCharset());
				BufferedReader bufferedReader = new BufferedReader(in);
				if (bufferedReader != null) {
					int cp;
					while ((cp = bufferedReader.read()) != -1) {
						sb.append((char) cp);
					}
					bufferedReader.close();
				}
			}
			in.close();
		} catch (Exception e) {
			throw new RuntimeException("Exception while calling URL:" + URL,
					e);
		}

		return sb.toString();
	}
	
	
	/**
     * Changes the attribute value of the corresponding element
     * 
     * @param xmlFilePath
     * @param element
     * @param attrName
     * @param attrOldValue
     * @param attrNewValue
     * @param machineID
     */
    public void changeAttributeValue(String xmlFilePath, String element, String attrName,
            String attrOldValue, String attrNewValue, String machineID) {
    	
    	 
        List<String> args = new ArrayList<String>();
        args.add(xmlFilePath);
        args.add(element);
        args.add(attrName);
        args.add(attrOldValue);
        args.add(attrNewValue);
       
        XMLModifierFlowContext modifyXML =
            new XMLModifierFlowContext.Builder().arguments(args)
                .methodName("XMLUtil.changeAttributeValue").build();
        runFlowByMachineId(machineID,  XMLModifierFlow.class, modifyXML);
    
    }
}
