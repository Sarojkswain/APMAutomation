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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.testbed.HVREMWindowsTestbed;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;


public class TestHVRAgent extends TasTestNgTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestHVRAgent.class);
    CLWCommons clw = new CLWCommons();
    String libDir = envProperties.getRolePropertyById(HVREMWindowsTestbed.EM_ROLE_ID, DeployEMFlowContext.ENV_EM_LIB_DIR);

    @Tas(testBeds = @TestBed(name = HVREMWindowsTestbed.class, executeOn = "emMachine"))
    @Test(groups = {"sample"})
    public void dummy() throws Exception {

        LOGGER.info("EM Host name: " + getEmHostName());
        LOGGER.info("EM port: " + getEmPort());
        LOGGER.info("CLW Directory: " + libDir);
        
        List<String> list = clw.getNodeList("admin", "", ".*", getEmHostName(), getEmPort(), libDir);
        
          LOGGER.info(""+list);
        
    }
    /* --- Non-public methods --- */

    private String getEmHostName() {
      return envProperties.getMachineHostnameByRoleId(HVREMWindowsTestbed.EM_ROLE_ID);
    }

    private int getEmPort() {
      return Integer.valueOf(envProperties.getRolePropertiesById(HVREMWindowsTestbed.EM_ROLE_ID).getProperty("emPort"));
    }
}
   