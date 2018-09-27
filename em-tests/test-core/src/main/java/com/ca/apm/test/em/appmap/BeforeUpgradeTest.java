/*
 * Copyright (c) 2016 CA.  All rights reserved.
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

package com.ca.apm.test.em.appmap;

import org.testng.annotations.Test;

import com.ca.apm.test.atc.common.WebViewTestNgTest;
import com.ca.tas.role.EmRole;
import com.ca.tas.test.em.appmap.SimpleEmTestBed_10_1;
import com.ca.tas.test.em.appmap.SimpleEmTestBed_10_X;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

public class BeforeUpgradeTest extends WebViewTestNgTest  {
    
    @Tas(testBeds = @TestBed(name = SimpleEmTestBed_10_1.class, executeOn = "standalone"), owner = "korzd01", size = SizeType.MEDIUM)
    @Test(groups = {"agc", "smoke"})
    public void collectData() throws Exception {
        
        runSerializedCommandFlowFromRole(SimpleEmTestBed_10_X.EM_ROLE_ID, EmRole.ENV_START_EM);
        
        Thread.sleep(120 * 1000);
        
        utilities.createClwUtils(SimpleEmTestBed_10_X.EM_ROLE_ID).getClwRunner().runClw("shutdown");
        killEM(SimpleEmTestBed_10_X.EM_ROLE_ID);
    }
}
