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
 * Author : GAMSA03/ SANTOSH JAMMI
 * Author : KETSW01/ KETHIREDDY SWETHA
 * Author : JAMSA07/ SANTOSH JAMMI
 * Date : 20/11/2015
 */
package com.ca.apm.commons.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.testbed.CommonsWindowsTestbed;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;


public class TestCommonsWindows extends TasTestNgTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestCommonsWindows.class);
    TestUtils utility = new TestUtils();

    private final String host;
    private final String watchDogDir;
    private final int sshPort;
    private final String emRoleId;
    private final String emPort;
    private final String libDir;
    private final String logDir;
    private final String configDir;

    public TestCommonsWindows() {
        emRoleId = CommonsWindowsTestbed.EM_ROLE_ID;
        
        host =
            envProperties
            .getMachineHostnameByRoleId(CommonsWindowsTestbed.EM_ROLE_ID);
        watchDogDir = envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_BIN_DIR);
        libDir = envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_LIB_DIR);
        sshPort = 22;
        emPort = envProperties
        .getRolePropertiesById(emRoleId).getProperty("emPort");
        logDir = envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_LIB_DIR);
        configDir = envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR);
        
        
    }
    
    @Tas(testBeds = @TestBed(name = CommonsWindowsTestbed.class, executeOn = CommonsWindowsTestbed.EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "jamsa07")
    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_257487_txBD01() {
        try {
            LOGGER.info(Boolean.toString(ApmbaseUtil.isPortAvailable(5001, host)));
            LOGGER.info(Boolean.toString(ApmbaseUtil.lookForPortAvailability(host, 5001, 100, true)));
            
            ApmbaseUtil.startWatchDogEM(watchDogDir);
            LOGGER.info(Integer.toString(ApmbaseUtil.lookForPortReady(host, 5001)));
            Thread.sleep(100000);
            ApmbaseUtil.checkTranscationTraces("admin", "", "", host, 5001, libDir, "error");
            ApmbaseUtil.checkListAgentsQuery("admin", "", "", host, 5001, libDir, "error");
            
            
            String[] list ={"findstr INFO IntroscopeEnterpriseManager.log",""};
            List<String> compareWith = new ArrayList<String>();
            compareWith.add("INFO");
            ApmbaseUtil.executeCommandAndCheckOutput(list, logDir, "jamsa07.txt", compareWith, 60);
            
            List<String> commands = new ArrayList<String>();
            commands.add("findstr INFO IntroscopeEnterpriseManager.log");
            
            Process p = ApmbaseUtil.runCommand(commands, logDir);
            commands.add("mkdir C:\\JAMSA");
            
            p = ApmbaseUtil.runCommand(commands, logDir);
            ApmbaseUtil.sleep(10);
            ApmbaseUtil.killProcess(p);
            ApmbaseUtil.checkproperties("IntroscopeEnterpriseManager.properties", configDir, "introscope.enterprisemanager.port.channel1", "5002");
            ApmbaseUtil.setproperties("IntroscopeEnterpriseManager.properties", configDir, "introscope.enterprisemanager.port.channel1", "5002");
            commands.clear();
            commands.add("introscope.enterprisemanager.query.datapointlimit=100000");
            ApmbaseUtil.appendProperties(commands,"IntroscopeEnterpriseManager.properties", configDir);
            ApmbaseUtil.removeProperties("IntroscopeEnterpriseManager.properties", configDir,commands);
            ApmbaseUtil.verifyService("SNMPTRAP");
            ApmbaseUtil.StartService("SNMPTRAP");
            ApmbaseUtil.StopService("SNMPTRAP");
            ApmbaseUtil.doServiceServer("NET START SNMPTRAP");
            ApmbaseUtil.doServiceServer("NET STOP SNMPTRAP");
            
            
            ApmbaseUtil.copyDirectory(new File(libDir), new File("C:\\jamsa"));
            ApmbaseUtil.fileBackUp(configDir+"IntroscopeEnterpriseManager.properties");
            ApmbaseUtil.revertFile(configDir+"IntroscopeEnterpriseManager.properties.bak");
            
            ApmbaseUtil.copy(configDir+"IntroscopeEnterpriseManager.properties",configDir+"IntroscopeEnterpriseManager.properties.bak.copy");

            ApmbaseUtil.deleteFile(configDir+"IntroscopeEnterpriseManager.properties.bak.copy");
            
            ApmbaseUtil.deleteDir(new File("C:\\jamsa"));
            
            ApmbaseUtil.stopWatchDogEM(watchDogDir);

            
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
}
   