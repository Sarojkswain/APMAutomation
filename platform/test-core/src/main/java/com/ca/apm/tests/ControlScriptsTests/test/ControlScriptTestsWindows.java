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
 * 
 * Author : KETSW01/ KETHIREDDY SWETHA
 */
package com.ca.apm.tests.ControlScriptsTests.test;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.commons.common.AssertTests;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.commons.tests.BaseAgentTest;
import com.ca.apm.tests.testbed.ControlScriptsWindowsTestbed;
import com.ca.tas.test.TasTestNgTest;

/**
 * Class to test ControlScripts-Watchdog test cases
 * @author ketsw01
 *
 */

public class ControlScriptTestsWindows extends BaseAgentTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControlScriptTestsWindows.class);
    private static Long STARTUP_TIMEOUT = 120 * 1000L;
    protected String emMachineId;
    private final String emRoleId;
    protected String emInstallDir;
    protected String emLibDir;
    protected String emBinDir;
    protected int emPort;
    protected String emHost;
    
    protected CLWCommons clwCommon;
    protected AssertTests assertTest;
    protected TestUtils utility;

    /**
     * Constructor
     */
    public ControlScriptTestsWindows() {
        emMachineId = ControlScriptsWindowsTestbed.EM_MACHINE_ID;
        emRoleId = ControlScriptsWindowsTestbed.EM_ROLE_ID;
        emHost =
            envProperties
                .getMachineHostnameByRoleId(ControlScriptsWindowsTestbed.EM_ROLE_ID);
        emPort =
            Integer.parseInt(envProperties.getRolePropertyById(emRoleId,
                DeployEMFlowContext.ENV_EM_PORT));
        emInstallDir = envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_INSTALL_DIR);
        emLibDir = envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_LIB_DIR);
        emBinDir = envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_BIN_DIR);

        clwCommon = new CLWCommons();
        assertTest = new AssertTests();
        utility = new TestUtils();
    }


    /**
     * Before Class - runs all pre-requisite operations
     * 
     * @throws Exception
     */
    @BeforeClass(alwaysRun = true)
    public void testWatchdog() throws Exception {
        RegisterEmAsService();
        LOGGER.info("Adding a new user by name \'testuser\' to users.xml");
        utility.replaceContentInFile(emInstallDir+"/config/users.xml","</users>","<user password=\"\" name=\"testuser\"/></users>");
    }

    
    /**
     * Test Case ID: 205152
     * Start EM with shell script
     * Ascertain Results
     */
    @Test(groups = {"controlScripts", "WatchDog", "EM", "BAT"})
    public void verify_ALM_205152_startEMWithShellScript() {
        try {
            String str = startWatchDogUsingJarEmPort();
            assertStartsWatchDog(str);
            harvestWait(240);

            str = statusWatchDogUsingJarEmPort();
            assertStatusWatchDog(str);

            str = stopWatchDogUsingJarEmPort();
            assertStopsWatchDog(str);
            harvestWait(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test Case ID: 205144
     * Start EM with shell script
     * Ascertain Results
     */
    @Test(groups = {"controlScripts", "WatchDog", "EM", "BAT"})
    public void verify_ALM_205144_startEMWithShellScript() {
        try {
            String str = startUsingEMCtrlScript();
            harvestWait(30);
            str = startWatchDog();
            assertStartsWatchDog(str);
            harvestWait(30);
            stopUsingEMCtrlScript();
            harvestWait(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    /**
     *  verify EM start with control scripts and stop with Watchdog
     */
    @Test(groups = {"controlScripts", "smoke"})
    public void verify_ALM_205135_Start_EM_with_ControlScripts_and_verify_Stop_option_of_WatchDog() {
        try {
            String str = startUsingEMCtrlScript();
            TestUtils.waitTillPortIsBusy(emPort, STARTUP_TIMEOUT);
            str = statusWatchDogUsingJar();
            assertStatusWatchDog(str);
            str = stopWatchDogUsingJarWatchOption();
            assertStopsWatchDog(str);
            harvestWait(120);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }        
    }
    
    /**
     * verify stop with Watchdog
     */
    @Test(groups = {"controlScripts", "deep"})
    public void verify_ALM_205138_Check_for_Stop_command() {
        try {
            String str = startWatchDogUsingJar();
            assertStartsWatchDog(str);
            harvestWait(240);
            str = stopWatchDogUsingJar();
            assertStopsWatchDog(str);
            harvestWait(120);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }        
    }
    
    /**
     * verify start with Watchdog
     */
    @Test(groups = {"controlScripts", "deep"})
    public void verify_ALM_205141_Check_made_for_Start_command() {
        try {
            String str = startWatchDogUsingJar();
            assertStartsWatchDog(str);
            harvestWait(240);
            str = statusWatchDogUsingJar();
            assertStatusWatchDog(str);           
          } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }        
    }
    
    /**
     * verify stop using watchdog with watch parameter
     */
    @Test(groups = {"controlScripts", "deep"})
    public void verify_ALM_205151_Check_for_STOP_without_Watch_EM_is_running() {
        try {
            String str = startWatchDogUsingJar();
            assertStartsWatchDog(str);
            harvestWait(240);
            str = statusWatchDogUsingJarEmPort();
            assertStatusWatchDog(str);
            str = stopWatchDogUsingJarEmPort();
            assertStopsWatchDog(str);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }        
    }
    
    /**
     * verify status with Watchdog
     */
    @Test(groups = {"controlScripts", "deep"})
    public void verify_ALM_205153_Check_made_for_the_STATUS_command() {
        try {
            String str = startWatchDogUsingJar();
            assertStartsWatchDog(str);
            harvestWait(240);
            str = statusWatchDogUsingJarEmPort();
            assertStatusWatchDog(str);
         } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }        
    }
    
    /**
     * start em using EM service and check Watchdog functionality
     */
    @Test(groups = {"controlScripts", "deep"})
    public void verify_ALM_205160_Start_EM_Service_and_test_all_the_functionalities_of_watchdog() {
        try {
            String str = startUsingEMCtrlScript();
            harvestWait(240);
            str = startWatchDog();
            assertStartsWatchDog(str);
            str=statusWatchDog();
            assertStatusWatchDog(str);
            str = stopWatchDog();
            assertStopsWatchDog(str);
            harvestWait(120);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }        
    }
    
    /**
     * verify watchdog command with emuser and empwd options
     */
    @Test(groups = {"controlScripts", "deep"})
    public void verify_ALM_205142_Verify_the_emuser_and_empwd_options_of_WatchDog() {
        try {                        
            String str = startWatchDogUsingJarEmuser();
            assertStartsWatchDog(str);
            harvestWait(240);
            str = stopWatchDogUsingJar();
            assertStopsWatchDog(str);
            harvestWait(120);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }        
    }
  
    /**
     * start em using watchdog and verify all options of Watchdog
     */
    @Test(groups = {"controlScripts", "deep"})
    public void verify_ALM_205161_Start_EM_from_the_EMHome_and_verify_all_options_of_WatchDog() {
        try {
            String str = startWatchDogUsingJar();
            harvestWait(240);
            str = startWatchDog();
            assertStartsWatchDog(str);
            str=statusWatchDog();
            assertStatusWatchDog(str);
            str = stopWatchDog();
            assertStopsWatchDog(str);
            harvestWait(120);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }        
    }  
    
    /**
     * Register EM as Service
     * @return
     * @throws Exception
     */
    public String RegisterEmAsService() throws Exception
    {
        String registerEM = "EMCtrl64.bat register & \\r";
        return runCommand(registerEM, emBinDir);
    }
    
    /**
     * Unregister EM as Service
     * @return
     * @throws Exception
     */
    public String UnregisterEmAsService() throws Exception
    {
        String unregisterEM = "EMCtrl64.bat unregister & \\r";
        return runCommand(unregisterEM, emBinDir);
    }

    /**
     * Start using EMCtrl.bat
     * @return
     * @throws Exception
     */
    public String startUsingEMCtrlScript() throws Exception
    {
        String startUsingEMCtrlScript = "EMCtrl64.bat start & \\r";
        return runCommand(startUsingEMCtrlScript, emBinDir);
    }

    /**
     * Stop using EMCtrl.bat
     * @return
     * @throws Exception
     */
    public String stopUsingEMCtrlScript() throws Exception
    {
        String stopUsingEMCtrlScript ="EMCtrl64.bat stop & \\r";
    return runCommand(stopUsingEMCtrlScript, emBinDir);
    }
    
    /**
     * Start using Watchdog.bat
     * @return
     * @throws Exception
     */
    public String startWatchDog() throws Exception {
        String startWatchDog =
            "WatchDog.bat start & \\r";
        return runCommand(startWatchDog, emBinDir);
    }

    /**
     * Status using Watchdog.bat
     * @return
     * @throws Exception
     */
    public String statusWatchDog() throws Exception {
        String statusWatchDog = "WatchDog.bat status";
        return runCommand(statusWatchDog, emBinDir);
    }

    /**
     * Stop using Watchdog.bat
     * @return
     * @throws Exception
     */
    public String stopWatchDog() throws Exception {
        String stopWatchDog= "WatchDog.bat stop & //r";
        return runCommand(stopWatchDog, emBinDir);
    }

    /**
     * Start EM and watchdog using Watchdog.bat
     * @return
     * @throws Exception
     */
    public String startWatchDogWatch() throws Exception {
        String startWatchDogWatch= "WatchDog.bat watch & //r";
        return runCommand(startWatchDogWatch, emBinDir);
    }
    
    /**
     * Watchdog.bat help
     * @return
     * @throws Exception
     */
    public String helpWatchDog() throws Exception {
        String helpWatchDog= "WatchDog.bat help";
        return runCommand(helpWatchDog, emBinDir);
    }
    
    /**
     * Start Watchdog with -port
     * @return
     * @throws Exception
     */
    public String startWatchDogWithPort() throws Exception {
        String startWatchDogWithPort =
                   "java -jar WatchDog.jar start -startcmd  ../Introscope_Enterprise_Manager -watch -port 1234 & \\r";
        return runCommand(startWatchDogWithPort, emBinDir);
    }

    /**
     * Status Watchdog with -port
     * @return
     * @throws Exception
     */
    public String statusWatchDogWithPort() throws Exception {
        String statusWithWatchDogPort = "java -jar WatchDog.jar status -port 1234 & \\r";
        return runCommand(statusWithWatchDogPort, emBinDir);
    }

    /**
     * Stop Watchdog with -port
     * @return
     * @throws Exception
     */
    public String stopWatchDogWithPort() throws Exception {
        String stopWithWatchDogPort = "java -jar WatchDog.jar stop -port 1234 & \\r";
        return runCommand(stopWithWatchDogPort, emBinDir);
    }
    
    /**
     * Start using Watchdog.Jar 
     * @return
     * @throws Exception
     */
    public String startWatchDogUsingJar() throws Exception {
        String startWatchDogUsingJar =
                    "java -jar WatchDog.jar start -startcmd  ../Introscope_Enterprise_Manager & \\r";
        return runCommand(startWatchDogUsingJar, emBinDir);
    }

    /**
     * Check Status using Watchdog.Jar
     * @return
     * @throws Exception
     */
    public String statusWatchDogUsingJar() throws Exception {
        String statusWithWatchDogPort = "java -jar WatchDog.jar status & \\r";
        return runCommand(statusWithWatchDogPort, emBinDir);
    }

    /**
     * Stop using Watchdog.Jar
     * @return
     * @throws Exception
     */
    public String stopWatchDogUsingJar() throws Exception {
        String stopWatchDogUsingJar = "java -jar WatchDog.jar stop & \\r";
        return runCommand(stopWatchDogUsingJar, emBinDir);
    }

    /**
     * Start using Watchdog.jar with -watch
     * @return
     * @throws Exception
     */
    public String startWatchDogUsingJarWatchOption() throws Exception {
        String startWatchDogUsingJarWatchOption =
                    "java -jar WatchDog.jar start -startcmd  ../Introscope_Enterprise_Manager -watch & \\r";
        return runCommand(startWatchDogUsingJarWatchOption, emBinDir);
    }

    /**
     * Check status using Watchdog.Jar with -watch
     * @return
     * @throws Exception
     */
    public String statusWatchDogUsingJarWatchOption() throws Exception {
        String statusWatchDogUsingJarWatchOption =
            "java -jar WatchDog.jar status -watch & \\r";
        return runCommand(statusWatchDogUsingJarWatchOption, emBinDir);
    }

    /**
     * Stop using Watchdog.Jar with -watch
     * @return
     * @throws Exception
     */
    public String stopWatchDogUsingJarWatchOption() throws Exception {
        String stopWatchDogUsingJarWatchOption = "java -jar WatchDog.jar stop -watch & \\r";
        return runCommand(stopWatchDogUsingJarWatchOption, emBinDir);
    }
     
    
    /**
     * Start using Watchdog.Jar with emport parameter
     * @return
     * @throws Exception
     */
    public String startWatchDogUsingJarEmPort() throws Exception {
        String startWatchDogUsingJarEmPort =
                    "java -jar WatchDog.jar start -emport "+emPort+" -startcmd  ../Introscope_Enterprise_Manager & \\r";
        return runCommand(startWatchDogUsingJarEmPort, emBinDir);
    }

    /**
     * Check Watchdog status with emport parameter
     * @return
     * @throws Exception
     */
    public String statusWatchDogUsingJarEmPort() throws Exception {
        String statusWatchDogUsingJarEmPort = "java -jar WatchDog.jar status -emport "+emPort+" & \\r";
        return runCommand(statusWatchDogUsingJarEmPort, emBinDir);
    }

    /**
     * Invoke Watchdog.Jar with emport parameter
     * @return
     * @throws Exception
     */
    public String stopWatchDogUsingJarEmPort() throws Exception {
        String stopWatchDogUsingJarEmPort = "java -jar WatchDog.jar stop -emport "+emPort+ " & \\r";
        return runCommand(stopWatchDogUsingJarEmPort, emBinDir);
    }  
    
    /**
     * Start EM with emuser empwd parameters
     * @return
     * @throws Exception
     */
    public String startWatchDogUsingJarEmuser() throws Exception {
        String startWatchDogUsingJarEmuser = "java -jar WatchDog.jar start -emuser testuser -empwd -startcmd  ../Introscope_Enterprise_Manager";
        return runCommand(startWatchDogUsingJarEmuser, emBinDir);
    } 
    
    /**
     * Invoke Watchdog.Jar with interval option
     * @return
     * @throws Exception
     */
    
    public String startWatchDogUsingJarWithInterval() throws Exception {
        String startWatchDogUsingJarWithInterval = 
            "java -jar WatchDog.jar start -interval 20 -startcmd  ../Introscope_Enterprise_Manager & \\r";
        return runCommand(startWatchDogUsingJarWithInterval, emBinDir);
    }
    
    /**
     * Invoke Watchdog.Jar with startuptime option
     * @return
     * @throws Exception
     */
    public String startWatchDogUsingJarWithStartUpTime(String startuptime) throws Exception {
        String startWatchDogUsingJarWithStartUpTime =           
                 "java -jar WatchDog.jar start -startcmd  ../Introscope_Enterprise_Manager -watch -startuptime "+startuptime+" & \\r";
        return runCommand(startWatchDogUsingJarWithStartUpTime, emBinDir);
    }
    
    /**
     * To assert results of start related watchdog commands
     * @param str
     */
    private void assertStartsWatchDog(String str) {
        if (str.contains("startcommandissued") || str.contains("wdstartedwatching")
            || str.contains("wdalreadywatching") || str.contains("alreadyrunning")
            || str.contains("starting") || str.contains("wdwatching")
            || str.contains("wdInvalidPort"))
            Assert.assertTrue(true);
        else
            Assert.assertTrue(false);
    }

    /**
     * To assert results of stop related watchdog commands
     * @param str
     */
    private void assertStopsWatchDog(String str) {
        if (str.contains("stopcommandissued") || str.contains("wdstopcommandissued")
            || str.contains("stopped") || str.contains("wdstopped")
            || str.contains("alreadystopped") || str.contains("wdstopped"))

            Assert.assertTrue(true);
        else
            Assert.assertTrue(false);
    }

    /**
     * To assert results of status related watchdog commands
     * @param str
     */
    private void assertStatusWatchDog(String str) {
        if (str.contains("running") || str.contains("wdwatching") || str.contains("alreadystopped")
            || str.contains("wdstopped") || str.contains("wdsleeping"))
            Assert.assertTrue(true);
        else
            Assert.assertTrue(false);
    }
    
    /**
     * After class method to handle clean up code
     * @throws Exception
     */
    @AfterClass(alwaysRun = true)
    public void teardown() throws Exception{
        utility.replaceContentInFile(emInstallDir+"/config/users.xml","<user password=\"\" name=\"testuser\"/></users>","</users>");
        UnregisterEmAsService();
    }

    
    public String runCommand(String cmd, String dirLoc) throws Exception {
        
        List<String> list = utility.runCmd(cmd, dirLoc);
        String value = null;
        
        for (String item:list)
            value=value+item;
        
        return value;
    }    
}
