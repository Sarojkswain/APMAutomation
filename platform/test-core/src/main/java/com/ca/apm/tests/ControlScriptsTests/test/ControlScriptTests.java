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
 * 
 * Author : JAMSA07/ SANTOSH JAMMI
 * Date : 01/08/2016
 */

package com.ca.apm.tests.ControlScriptsTests.test;

import java.io.BufferedWriter;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.commons.common.PropertiesUtility;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.tests.testbed.ControlScriptsLinuxTestbed;
import com.ca.tas.test.TasTestNgTest;

public class ControlScriptTests extends TasTestNgTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControlScriptTests.class);
    TestUtils utility = new TestUtils();
    PropertiesUtility changeProp;
    private final String host;
    private final String user;
    private final String password;
    private final String watchDogDir;
    private final int sshPort;
    private final String emRoleId;
    private final String emPort;
    String hostIP;
    protected BufferedWriter writer = null;

    /**
     * Constructor for initializing variables
     */

    public ControlScriptTests() {

        emRoleId = ControlScriptsLinuxTestbed.EM_ROLE_ID;
        host = envProperties.getMachineHostnameByRoleId(ControlScriptsLinuxTestbed.EM_ROLE_ID);
        user = "root";
        password = "Lister@123";
        watchDogDir =
            "cd " + envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_BIN_DIR);
        sshPort = 22;
        emPort = envProperties.getRolePropertiesById(emRoleId).getProperty("emPort");
        try {
            String[] getIP =
                {"ifconfig | grep \"inet addr\" | cut -d\":\" -f2 | cut -d\" \" -f1", "\\r"};
            String str = utility.execUnixCmd(host, sshPort, user, password, getIP);
            hostIP = str.split(":::")[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop all the services before starting the tests
     */

    @BeforeClass
    public void testControlScripts() {
        try {
            stopWatchDogUsingJar();
            stopWatchDog();
            stopUsingEMCtrlScript();
            harvestWait(20);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Test Case ID: 205152
     * Start EM with shell script
     * Ascertain Results
     */
    
    
    @Test(groups = {"controlScripts", "WatchDog", "EM", "BAT"})
    public void verify_ALM_205152_startEMWithShellScript() {
    verify_ALM_205180_StartStatusStopWithJarAndEMPort();
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
            assertStartsUsingEMCtrl(str);
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
     * Test Case ID: 280480
     * Start EM with shell script
     * Ascertain Results
     */

    @Test(groups = {"controlScripts", "WatchDog", "EM", "DEEP"})
    public void verify_ALM_280480_startEMWithShellScript() {
        try {
            String str = startUsingEMCtrlScript();
            assertStartsUsingEMCtrl(str);
            harvestWait(30);
            stopUsingEMCtrlScript();
            harvestWait(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Test Case ID: 298500
     * Stop EM with shell script
     * Ascertain Results
     */

    @Test(groups = {"controlScripts", "WatchDog", "EM", "DEEP"})
    public void verify_ALM_298500_stopEMWithShellScript() {
        try {
            startUsingEMCtrlScript();
            harvestWait(240);
            String str = stopUsingEMCtrlScript();
            assertStopUsingEMCtrl(str);
            harvestWait(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test Case ID: 298501
     * Start EM with shell script
     * Ascertain Results
     */

    @Test(groups = {"controlScripts", "WatchDog", "EM", "DEEP"})
    public void verify_ALM_298501_statusEMWithShellScript() {
        try {
            startUsingEMCtrlScript();
            harvestWait(240);
            String str = statusUsingEMCtrlScript();
            assertStatusUsingEMCtrl(str);
            stopUsingEMCtrlScript();
            harvestWait(120);
            str = statusUsingEMCtrlScript();
            assertStatusUsingEMCtrl(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test Case ID: 298509
     * Start EM with shell script
     * Ascertain Results
     */

    @Test(groups = {"controlScripts", "WatchDog", "EM", "DEEP"})
    public void verify_ALM_298509_VerifyHelpEMCtrlShellScript() {
        try {
            String str = invokeHelpUsingEMCtrlScript();
            assertHelpUsingEMCtrl(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Test Case ID: 280480
     * Start EM with shell script when EM is running
     * Ascertain Results
     */

    @Test(groups = {"controlScripts", "WatchDog", "EM", "DEEP"})
    public void verify_ALM_298510_startEMWithShellScriptEMRunning() {
        try {
            String str = startUsingEMCtrlScript();
            assertStartsUsingEMCtrl(str);
            harvestWait(240);

            str = startUsingEMCtrlScript();
            assertStartsUsingEMCtrl(str);

            stopUsingEMCtrlScript();
            harvestWait(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test Case ID: 205163
     * Start WatchDog usingJar With StartUp Time option
     * Ascertain Results
     * 
     */

    @Test(groups = {"controlScripts", "WatchDog", "EM", "DEEP"})
    public void verify_ALM_205163_StartWithStartUpTime() {
        try {
            String str = startWatchDogUsingJarWithStartUpTime("240");
            assertStartsWatchDog(str);

            str = startWatchDogUsingJarWithStartUpTime("240");
            assertStartsWatchDog(str);

            str = statusWatchDogUsingJar();
            assertStatusWatchDog(str);
            harvestWait(240);
            str = stopWatchDogUsingJar();
            harvestWait(120);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test Case ID: 205169
     * Start using watchdog with watch option, then stop it and check status.
     * 
     */

    @Test(groups = {"controlScripts", "WatchDog", "EM", "SMOKE"})
    public void verify_ALM_205169_StartWithWatchOption() {
        try {
            String str = startWatchDogUsingJarWatchOption();
            assertStartsWatchDog(str);

            str = statusWatchDogUsingJar();
            assertStartsWatchDog(str);
            /**
             * Stop EM
             */
            str = stopUsingEMCtrlScript();
            harvestWait(240);

            str = statusWatchDogUsingJar();
            assertStatusWatchDog(str);

            str = stopWatchDogUsingJar();
            assertStopsWatchDog(str);
            harvestWait(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test Case ID: 205143
     * Start watch Dog specifying EM PORT and check status.
     * 
     */

    @Test(groups = {"controlScripts", "WatchDog", "EM", "DEEP"})
    public void verify_ALM_205143_StartStatusWithJarAndEMPort() {
        try {
            String str = startWatchDogUsingJarEmPort();
            assertStartsWatchDog(str);
            harvestWait(240);

            str = statusWatchDogUsingJarEmPort();
            assertStatusWatchDog(str);

            str = stopWatchDogUsingJarEmPort();
            harvestWait(120);
            assertStopsWatchDog(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test Case ID: 205180
     * Start watch Dog specifying EM PORT check status and stop.
     * 
     */

    @Test(groups = {"controlScripts", "WatchDog", "EM", "DEEP"})
    public void verify_ALM_205180_StartStatusStopWithJarAndEMPort() {
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
     * Test Case ID: 205149
     * When EM is running, Start watch Dog specifying EM PORT check status and stop.
     * 
     */

    @Test(groups = {"controlScripts", "WatchDog", "EM", "SMOKE"})
    public void verify_ALM_205149_ControlStartAndStartStatusStopWithJarAndEMPort() {
        try {
            String str = startUsingEMCtrlScript();
            LOGGER.info(str);
            harvestWait(240);

            str = startWatchDogUsingJarWatchOption();
            assertStartsWatchDog(str);

            str = statusWatchDogUsingJarWatchOption();
            assertStatusWatchDog(str);

            str = stopWatchDogUsingJarWatchOption();
            assertStopsWatchDog(str);
            harvestWait(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test Case ID: 205178
     * Start watch dog using interval option
     * 
     */

    @Test(groups = {"controlScripts", "WatchDog", "EM", "DEEP"})
    public void verify_ALM_205178_startWithIntervalOption() {
        try {
            String str = startWatchDogUsingJarWithInterval();
            assertStartsWatchDog(str);
            harvestWait(240);

            str = statusWatchDogUsingJar();
            assertStatusWatchDog(str);

            str = stopWatchDogUsingJar();
            assertStopsWatchDog(str);

            harvestWait(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test Case ID: 205176
     * When EM is not running, check status without watch option
     * 
     */

    @Test(groups = {"controlScripts", "WatchDog", "EM", "SMOKE"})
    public void verify_ALM_205176_CheckStatusWithoutWatchEMNotRunning() {
        try {
            String str = stopWatchDogUsingJar();
            LOGGER.info(str);
            harvestWait(240);

            statusWatchDogUsingJar();
            assertStatusWatchDog(str);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test Case ID: 205175
     * Check the status of watch dog when it is running using watchdog
     * 
     */

    @Test(groups = {"controlScripts", "WatchDog", "EM", "DEEP"})
    public void verify_ALM_205175_CheckStatusWithoutWatchEMRunning() {
        try {
            String str = startUsingEMCtrlScript();
            LOGGER.info(str);
            harvestWait(240);

            str = startWatchDogUsingJarWatchOption();
            assertStartsWatchDog(str);

            str = statusWatchDogUsingJarWatchOption();
            assertStatusWatchDog(str);

            str = stopWatchDogUsingJarWatchOption();
            assertStopsWatchDog(str);
            harvestWait(120);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test Case ID: 205146
     * Check the status of watch dog when it is running using watchdog with the watch option
     * 
     */

    @Test(groups = {"controlScripts", "WatchDog", "EM", "DEEP"})
    public void verify_ALM_205146_CheckStatusWithWatchEMRunning() {
        try {
            String str = startUsingEMCtrlScript();
            LOGGER.info(str);
            harvestWait(240);

            str = startWatchDogUsingJarWatchOption();
            assertStartsWatchDog(str);

            str = statusWatchDogUsingJarWatchOption();
            assertStatusWatchDog(str);

            str = stopWatchDogUsingJarWatchOption();
            assertStopsWatchDog(str);
            harvestWait(120);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test Case ID: 205174
     * Check the status of watch dog when it is running using watchdog with the watch option
     * 
     */

    @Test(groups = {"controlScripts", "WatchDog", "EM", "DEEP"})
    public void verify_ALM_205174_CheckStatusWithWatchEMRunning() {
        try {
            String str = startUsingEMCtrlScript();
            LOGGER.info(str);
            harvestWait(240);

            str = startWatchDogUsingJar();
            assertStartsWatchDog(str);

            str = statusWatchDogUsingJar();
            assertStatusWatchDog(str);

            str = stopWatchDogUsingJar();
            assertStopsWatchDog(str);

            harvestWait(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test Case ID: 205168
     * Verify start, stop and status with watch dog port option
     * 
     */

    @Test(groups = {"controlScripts", "WatchDog", "EM", "SMOKE"})
    public void verify_ALM_205168_CheckWatchDogPort() {
        try {

            String str = startWatchDogWithPort();
            harvestWait(240);
            assertStartsWatchDog(str);

            str = statusWatchDogWithPort();
            assertStatusWatchDog(str);

            str = stopWatchDogWithPort();
            assertStopsWatchDog(str);
            harvestWait(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test Case ID: 205170
     * Start using watch dog when EM is not running
     * 
     */

    @Test(groups = {"controlScripts", "WatchDog", "EM", "DEEP"})
    public void verify_ALM_205170_StartUsingWatchDogWithoutWatchEMRunning() {
        try {

            String str = startWatchDogUsingJar();
            harvestWait(240);
            str = startWatchDogUsingJar();
            assertStartsWatchDog(str);

            str = stopWatchDogUsingJar();
            assertStopsWatchDog(str);
            harvestWait(120);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test Case ID: 205179
     * Check status using watch when EM is not running
     */

    @Test(groups = {"controlScripts", "WatchDog", "EM", "DEEP"})
    public void verify_ALM_205179_StatusUsingWatchDogNoWatchEMNotRunning() {
        try {
            String str = stopWatchDogUsingJar();
            harvestWait(120);

            str = statusWatchDogUsingJar();
            assertStatusWatchDog(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test Case ID: 205172
     * Try to Stop EM using Watch dog when EM is not running
     * 
     */

    @Test(groups = {"controlScripts", "WatchDog", "EM", "DEEP"})
    public void verify_ALM_205172_StopUsingWatchDogNoWatchEMRunning() {
        try {
            String str = startWatchDogUsingJar();
            harvestWait(240);

            str = stopWatchDogUsingJar();
            harvestWait(120);
            assertStopsWatchDog(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test Case ID: 205173
     * Start watchDog which inturn starts EM when EM is not running
     * 
     */

    @Test(groups = {"controlScripts", "WatchDog", "EM", "SMOKE"})
    public void verify_ALM_205173_StartUsingWatchDogNoWatchEMNotRunning() {
        try {
            String str = startWatchDogUsingJar();
            harvestWait(240);
            assertStartsWatchDog(str);
            harvestWait(120);

            str = stopWatchDogUsingJar();
            assertStopsWatchDog(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test Case ID: 205164
     * Start using watch dog and then stop using watchDog. Ascertain the results.
     */

    @Test(groups = {"controlScripts", "WatchDog", "EM", "DEEP"})
    public void verify_ALM_205164_StopUsingWatchDogNoWatchEMNotRunning() {
        try {
            String str = startWatchDog();
            harvestWait(240);

            str = stopWatchDog();
            assertStopsWatchDog(str);
            harvestWait(120);

            str = stopWatchDog();
            assertStopsWatchDog(str);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test Case ID: 205147
     * When EM is running, start using watch dog and check status
     */

    @Test(groups = {"controlScripts", "WatchDog", "EM", "DEEP"})
    public void verify_ALM_205147_StartUsingWatchDogCheckStatus() {
        try {
            String str = startUsingEMCtrlScript();
            LOGGER.info(str);
            harvestWait(240);

            str = startWatchDogUsingJarWatchOption();
            assertStartsWatchDog(str);

            str = statusWatchDogUsingJarWatchOption();
            assertStatusWatchDog(str);

            str = stopWatchDogUsingJar();
            assertStopsWatchDog(str);

            harvestWait(120);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test Case ID: 205171
     * When EM is running, start watchdog with watch option and check status with watch option
     */

    @Test(groups = {"controlScripts", "WatchDog", "EM", "DEEP"})
    public void verify_ALM_205171_StartUsingWatchDogCheckStatus() {
        try {
            String str = startUsingEMCtrlScript();
            LOGGER.info(str);
            harvestWait(240);

            str = startWatchDogUsingJarWatchOption();
            assertStartsWatchDog(str);

            str = statusWatchDogUsingJarWatchOption();
            assertStatusWatchDog(str);

            str = stopWatchDogUsingJar();
            assertStopsWatchDog(str);

            harvestWait(120);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test Case ID: 451791
     * Customer Defect
     * Start using watchdog again and again will increase the number of files associated to it.
     */

    @Test(groups = {"controlScripts", "WatchDog", "EM", "FULL"})
    public void verify_ALM_451791_TooManyOpenfilesinWatchDogLog() {
        String listpidsCommand[] = {watchDogDir, "lsof -t WatchDog.jar"};

        try {
            String str = startWatchDog();
            str = utility.execUnixCmd(host, sshPort, user, password, listpidsCommand);
            String pids[] = str.split(":::");
            int[] a = new int[10];
            for (int j = 0; j < pids.length; j++) {
                if (j > 10)
                    break;
                else {
                    String listOpenFilesCommand[] = {watchDogDir, "lsof -p "};
                    System.out.println(pids[j]);
                    listOpenFilesCommand[1] = listOpenFilesCommand[1] + pids[j] + " | wc -l";
                    str = utility.execUnixCmd(host, sshPort, user, password, listOpenFilesCommand);
                    a[j] = Integer.parseInt(str.trim().split(":::")[0]);
                }
            }
            for (int i = 0; i < 3; i++) {
                startWatchDog();
                Thread.sleep(12000);
                int[] b = new int[10];
                for (int j = 0; j < pids.length; j++) {
                    if (j > 10)
                        break;
                    else {
                        String listOpenFilesCommand[] = {watchDogDir, "lsof -p "};
                        System.out.println(pids[j]);
                        listOpenFilesCommand[1] = listOpenFilesCommand[1] + pids[j] + " | wc -l";
                        str =
                            utility
                                .execUnixCmd(host, sshPort, user, password, listOpenFilesCommand);
                        b[j] = Integer.parseInt(str.trim().split(":::")[0]);
                    }
                    if (a[j] != b[j]) Assert.assertFalse(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test Case ID: 450710
     * Set the IP Address of the EM machine in IntroscopeEnterpriseManager.properties file and start
     * using watchdog.
     */

    @Test(groups = {"controlScripts", "WatchDog", "EM", "SMOKE"})
    public void verify_ALM_450710_BindToLocalHost() {
        String filePath =
            envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        try {
            String Orig =
                "# introscope.enterprisemanager.ipaddress=(set to a valid IP address on EM machine)";
            String Mod = "introscope.enterprisemanager.ipaddress=" + hostIP;
            String[] modifyIPCommands =
                {
                        "cd "
                            + envProperties.getRolePropertyById(emRoleId,
                                DeployEMFlowContext.ENV_EM_CONFIG_DIR),
                        "sed -i 's/" + Orig + "/" + Mod + "/' " + filePath};
            String str = utility.execUnixCmd(host, sshPort, user, password, modifyIPCommands);
            str = startWatchDog();
            assertStartsWatchDog(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test Case ID: 350027
     * Start with Interactive console disabled
     */

    @Test(groups = {"controlScripts", "WatchDog", "EM", "SMOKE"})
    public void verify_ALM_239562_startWithInteractiveConsoleDisabled() {
        String filePath =
            envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        try {
            HashMap<String, String> props = PropertiesUtility.getPropertiesAsMap(filePath);
            if (props.get("introscope.enterprisemanager.disableInteractiveMode").equalsIgnoreCase(
                "true"))
                Assert.assertTrue(true);
            else
                Assert.assertTrue(false);
            LOGGER.info("The value is set to true ... Hence proceeding further");
            String str = startUsingEMCtrlScript();
            assertStartsUsingEMCtrl(str);
            harvestWait(240);
            str = stopUsingEMCtrlScript();
            harvestWait(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * Test Case ID: 454104
     * set introscope.enterprisemanager.ipaddress to localhost and verify EM status
     * author : velra06
     */
    @Test(groups = {"controlScripts", "deep"})
    public void verify_ALM_454104_DE48906_Watchdog_always_reports_status_stopped_wdwatching_for_EM()
    {
        try
        {
            String filePath = envProperties
                    .getRolePropertyById(emRoleId,
                                         DeployEMFlowContext.ENV_EM_CONFIG_FILE);
            String propertyKey = "introscope.enterprisemanager.ipaddress";
            String propertyValue = "localhost";
            PropertiesUtility.insertProperty(filePath, propertyKey,
                                             propertyValue);
            startUsingEMCtrlScript();
            String zStsatus = "java -jar WatchDog.jar status -emhost localhost";
            String str = utility.execWindowsCmd(zStsatus, envProperties
                    .getRolePropertyById(emRoleId,
                                         DeployEMFlowContext.ENV_EM_BIN_DIR));
            Assert.assertTrue(str.contains("running"));
            stopUsingEMCtrlScript();
        } catch (Exception e)
        {
            e.printStackTrace();
            Assert.assertTrue(false);
        }
    }

    /**
     * To start EM using EMCtrl.sh
     * 
     * @return String
     * @throws Exception
     */

    public String startUsingEMCtrlScript() throws Exception {
        String startWithWatchDogPort[] = {watchDogDir, "sh EMCtrl.sh start", "\\r"};
        String str = utility.execUnixCmd(host, sshPort, user, password, startWithWatchDogPort);
        harvestWait(10);
        return str;
    }

    /**
     * To stop EM using EMCtrl.sh
     * 
     * @return String
     * @throws Exception
     */


    public String stopUsingEMCtrlScript() throws Exception {
        String startWithWatchDogPort[] = {watchDogDir, "sh EMCtrl.sh stop", "\\r"};
        String str = utility.execUnixCmd(host, sshPort, user, password, startWithWatchDogPort);
        harvestWait(10);
        return str;
    }


    /**
     * Check status EM using EMCtrl.sh
     * 
     * @return String
     * @throws Exception
     */


    public String statusUsingEMCtrlScript() throws Exception {
        String statusWithWatchDogPort[] = {watchDogDir, "sh EMCtrl.sh status", "\\r"};
        String str = utility.execUnixCmd(host, sshPort, user, password, statusWithWatchDogPort);
        harvestWait(10);
        return str;
    }

    /**
     * Check status EM using EMCtrl.sh
     * 
     * @return String
     * @throws Exception
     */


    public String invokeHelpUsingEMCtrlScript() throws Exception {
        String statusWithWatchDogPort[] = {watchDogDir, "sh EMCtrl.sh help", "\\r"};
        String str = utility.execUnixCmd(host, sshPort, user, password, statusWithWatchDogPort);
        harvestWait(10);
        return str;
    }


    /**
     * start watchdog using shell script and watchdog port
     * 
     * @return String
     * @throws Exception
     */

    public String startWatchDogWithPort() throws Exception {
        String startWithWatchDogPort[] =
            {
                    watchDogDir,
                    "sh WatchDog.sh start -startcmd  ../Introscope_Enterprise_Manager -watch -port 1234",
                    "\\r"};
        String str = utility.execUnixCmd(host, sshPort, user, password, startWithWatchDogPort);
        harvestWait(10);
        return str;
    }

    /**
     * check status of watchdog using shell script and watchdog port
     * 
     * @return
     * @throws Exception
     */

    public String statusWatchDogWithPort() throws Exception {
        String statusWithWatchDogPort[] = {watchDogDir, "sh WatchDog.sh status -port 1234", "\\r"};
        String str = utility.execUnixCmd(host, sshPort, user, password, statusWithWatchDogPort);
        harvestWait(10);
        return str;
    }

    /**
     * stop watchdog using shell script and watchdog port
     * 
     * @return
     * @throws Exception
     */

    public String stopWatchDogWithPort() throws Exception {
        String stopWithWatchDogPort[] = {watchDogDir, "sh WatchDog.sh stop -port 1234", "\\r"};
        String str = utility.execUnixCmd(host, sshPort, user, password, stopWithWatchDogPort);
        harvestWait(10);
        return str;
    }

    /**
     * start watchdog using shell script
     * 
     * @return
     * @throws Exception
     */

    public String startWatchDog() throws Exception {
        String startWithWatchDogScript[] =
            {watchDogDir, "sh WatchDog.sh start -startcmd  ../Introscope_Enterprise_Manager", "\\r"};
        String str = utility.execUnixCmd(host, sshPort, user, password, startWithWatchDogScript);
        harvestWait(10);
        return str;
    }

    /**
     * check status of watchdog using shell script
     * 
     * @return
     * @throws Exception
     */

    public String statusWatchDog() throws Exception {
        String statusWithWatchDogScript[] = {watchDogDir, "sh WatchDog.sh status", "\\r"};
        String str = utility.execUnixCmd(host, sshPort, user, password, statusWithWatchDogScript);
        harvestWait(10);
        return str;
    }

    /**
     * stop watchdog using shell script
     * 
     * @return
     * @throws Exception
     */

    public String stopWatchDog() throws Exception {
        String stopWithWatchDogScript[] = {watchDogDir, "sh WatchDog.sh stop", "\\r"};
        String str = utility.execUnixCmd(host, sshPort, user, password, stopWithWatchDogScript);
        harvestWait(10);
        return str;
    }

    /**
     * start watchdog using JAR
     * 
     * @return
     * @throws Exception
     */

    public String startWatchDogUsingJar() throws Exception {
        String startWithWatchDogPort[] =
            {watchDogDir,
                    "java -jar WatchDog.jar start -startcmd  ../Introscope_Enterprise_Manager",
                    "\\r"};
        String str = utility.execUnixCmd(host, sshPort, user, password, startWithWatchDogPort);
        harvestWait(10);
        return str;
    }

    /**
     * status using JAR
     * 
     * @return
     * @throws Exception
     */

    public String statusWatchDogUsingJar() throws Exception {
        String statusWithWatchDogPort[] = {watchDogDir, "java -jar WatchDog.jar status", "\\r"};
        String str = utility.execUnixCmd(host, sshPort, user, password, statusWithWatchDogPort);
        harvestWait(10);
        return str;
    }

    /**
     * stop using JAR
     * 
     * @return
     * @throws Exception
     */

    public String stopWatchDogUsingJar() throws Exception {
        String stopWithWatchDogPort[] = {watchDogDir, "java -jar WatchDog.jar stop", "\\r"};
        String str = utility.execUnixCmd(host, sshPort, user, password, stopWithWatchDogPort);
        harvestWait(10);
        return str;
    }

    /**
     * Start using JAR with watch option
     * 
     * @return
     * @throws Exception
     */

    public String startWatchDogUsingJarWatchOption() throws Exception {
        String startWatchDogUsingJarWatchOption[] =
            {
                    watchDogDir,
                    "java -jar WatchDog.jar start -startcmd  ../Introscope_Enterprise_Manager -watch",
                    "\\r"};
        String str =
            utility.execUnixCmd(host, sshPort, user, password, startWatchDogUsingJarWatchOption);
        harvestWait(10);
        return str;
    }

    /**
     * status using JAR with watch option
     * 
     * @return
     * @throws Exception
     */
    public String statusWatchDogUsingJarWatchOption() throws Exception {
        String statusWatchDogUsingJarWatchOption[] =
            {watchDogDir, "java -jar WatchDog.jar status -watch", "\\r"};
        String str =
            utility.execUnixCmd(host, sshPort, user, password, statusWatchDogUsingJarWatchOption);
        harvestWait(10);
        return str;
    }

    /**
     * Stop using JAR with watch option
     * 
     * @return
     * @throws Exception
     */

    public String stopWatchDogUsingJarWatchOption() throws Exception {
        String stopWatchDogUsingJarWatchOption[] =
            {watchDogDir, "java -jar WatchDog.jar stop -watch", "\\r"};
        String str =
            utility.execUnixCmd(host, sshPort, user, password, stopWatchDogUsingJarWatchOption);
        harvestWait(10);
        return str;
    }

    /**
     * start with JAR specifying EM port
     * 
     * @return
     * @throws Exception
     */

    public String startWatchDogUsingJarEmPort() throws Exception {
        String startWatchDogUsingJarEmPort[] =
            {
                    watchDogDir,
                    "java -jar WatchDog.jar start -emport " + emPort
                        + " -startcmd  ../Introscope_Enterprise_Manager", "\\r"};
        String str =
            utility.execUnixCmd(host, sshPort, user, password, startWatchDogUsingJarEmPort);
        harvestWait(10);
        return str;
    }

    /**
     * status with jar specifying EM port
     * 
     * @return
     * @throws Exception
     */

    public String statusWatchDogUsingJarEmPort() throws Exception {
        String statusWatchDogUsingJarEmPort[] =
            {watchDogDir, "java -jar WatchDog.jar status -emport " + emPort, "\\r"};
        String str =
            utility.execUnixCmd(host, sshPort, user, password, statusWatchDogUsingJarEmPort);
        harvestWait(10);
        return str;
    }

    /**
     * stop with jar specifying EM port
     * 
     * @return
     * @throws Exception
     */

    public String stopWatchDogUsingJarEmPort() throws Exception {
        String stopWatchDogUsingJarEmPort[] =
            {watchDogDir, "java -jar WatchDog.jar stop -emport " + emPort, "\\r"};
        String str = utility.execUnixCmd(host, sshPort, user, password, stopWatchDogUsingJarEmPort);
        harvestWait(10);
        return str;

    }


    /**
     * start with Jar command and interval option
     * 
     * @return
     * @throws Exception
     */

    public String startWatchDogUsingJarWithInterval() throws Exception {
        String startWatchDogUsingJarWithInterval[] =
            {
                    watchDogDir,
                    "java -jar WatchDog.jar start -interval 20 -startcmd  ../Introscope_Enterprise_Manager",
                    "\\r"};
        String str =
            utility.execUnixCmd(host, sshPort, user, password, startWatchDogUsingJarWithInterval);
        harvestWait(10);
        return str;
    }

    /**
     * start with Jar command and startuptime option
     * 
     * @return
     * @throws Exception
     */
    public String startWatchDogUsingJarWithStartUpTime(String startuptime) throws Exception {
        String startWatchDogUsingJarWithStartUpTime[] =
            {
                    watchDogDir,
                    "java -jar WatchDog.jar start -startcmd  ../Introscope_Enterprise_Manager -watch -startuptime "
                        + startuptime, "\\r"};
        String str =
            utility
                .execUnixCmd(host, sshPort, user, password, startWatchDogUsingJarWithStartUpTime);
        harvestWait(10);
        return str;
    }

    /**
     * Assert the start command output when run
     * 
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
     * Assert the stop command output when run
     * 
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
     * Assert the status command output when run
     * 
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
     * Assert the start using shell script
     * 
     * initialization is the keyword
     */

    private void assertStartsUsingEMCtrl(String str) {
        if (str.contains("initialization") || str.contains("already") || str.contains("running")
            || str.contains("pid"))
            Assert.assertTrue(true);
        else
            Assert.assertTrue(false);
    }
    
    /**
     * Assert the start using shell script
     * 
     * stopped is the keyword
     */

    private void assertStopUsingEMCtrl(String str) {
        if (str.contains("stopped"))
            Assert.assertTrue(true);
        else
            Assert.assertTrue(false);
    }

    /**
     * Assert the start using shell script
     * 
     * running or stopped is the keyword
     */

    private void assertStatusUsingEMCtrl(String str) {
        if (str.contains("running") || str.contains("stopped"))
            Assert.assertTrue(true);
        else
            Assert.assertTrue(false);
    }

    /**
     * Assert the help using shell script
     * 
     * running or stopped is the keyword
     */

    private void assertHelpUsingEMCtrl(String str) {
        if (str.contains("usage:") && str.contains("start") && str.contains("stop")
            && str.contains("status") && str.contains("help"))
            Assert.assertTrue(true);
        else
            Assert.assertTrue(false);
    }

    /**
     * Method to wait specifying the number of seconds
     * 
     * @param seconds
     */
    private void harvestWait(int seconds) {
        try {
            LOGGER.info("Harvesting crops.");
            Thread.sleep(seconds * 1000);
            LOGGER.info("Crops harvested.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
}
