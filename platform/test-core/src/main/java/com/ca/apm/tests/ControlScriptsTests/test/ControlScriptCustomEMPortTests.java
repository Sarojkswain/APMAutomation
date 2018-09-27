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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.commons.common.PropertiesUtility;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.tests.testbed.ControlScriptsLinuxTestbed;
import com.ca.tas.test.TasTestNgTest;

public class ControlScriptCustomEMPortTests extends TasTestNgTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControlScriptCustomEMPortTests.class);
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

    public ControlScriptCustomEMPortTests() {

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
            
            str = startWatchDogUsingJarEmPort();
            assertStartsWatchDog(str);
            harvestWait(240);
            
            stopWatchDogUsingJarEmPort();
            
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
            
            str = startWatchDogUsingJarEmPort();
            assertStartsWatchDog(str);
            harvestWait(240);
            
            str = stopWatchDogUsingJarEmPort();
            assertStopsWatchDog(str);
            harvestWait(240);

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
            
            startWatchDogUsingJarEmPort();
            str = statusWatchDogUsingJarEmPort();
            assertStatusWatchDog(str);
            harvestWait(240);
            
            stopWatchDogUsingJarEmPort();
            str = statusWatchDogUsingJarEmPort();
            assertStatusWatchDog(str);
            harvestWait(240);
            
        } catch (Exception e) {
            e.printStackTrace();
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
