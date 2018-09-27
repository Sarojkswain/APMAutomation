/*
 * Copyright (c) 2017 CA.  All rights reserved.
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

import com.ca.apm.automation.action.test.ClwRunner;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.test.em.appmap.MathAppEmTestBed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

public class MathAppTest extends TasTestNgTest {

    @Tas(testBeds = @TestBed(name = MathAppEmTestBed.class, executeOn = MathAppEmTestBed.MACHINE_ID), owner = "korzd01", size = SizeType.SMALL)
    @Test(groups = {"appmap", "smoke"})
    public void configureTestbed() throws Exception {
        startTTs();
    }

    private void startTTs() {
        String command = "trace transactions exceeding 1 ms in agents matching \".*\" for 120 s";
        ClwRunner standaloneClwRunner =
            utilities.createClwUtils(MathAppEmTestBed.EM_ROLE_ID).getClwRunner();
        
        standaloneClwRunner.runClw(command);
    }

}
