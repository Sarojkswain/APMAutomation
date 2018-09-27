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
 */
package com.ca.apm.test.em.appmap;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.UI;
import com.ca.tas.test.em.appmap.UpgradeEmTestBed_10_1;
import com.ca.tas.test.em.appmap.UpgradeEmTestBed_10_2;
import com.ca.tas.test.em.appmap.UpgradeEmTestBed_10_3;
import com.ca.tas.test.em.appmap.UpgradeOracleEmTestBed_10_1;
import com.ca.tas.test.em.appmap.UpgradeOracleEmTestBed_10_2;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

public class UpgradeTest extends UITest {

    private UI ui;
    
    @Tas(testBeds = @TestBed(name = UpgradeEmTestBed_10_1.class, executeOn = "standalone"), owner = "korzd01", size = SizeType.SMALL)
    @Test(groups = {"atc", "smoke"})
    public void testNodesOnMap_10_1() throws Exception {
        testNodesOnMap();
    }
    
    @Tas(testBeds = @TestBed(name = UpgradeEmTestBed_10_2.class, executeOn = "standalone"), owner = "korzd01", size = SizeType.SMALL)
    @Test(groups = {"atc", "smoke"})
    public void testNodesOnMap_10_2() throws Exception {
        testNodesOnMap();
    }
    
    @Tas(testBeds = @TestBed(name = UpgradeEmTestBed_10_3.class, executeOn = "standalone"), owner = "korzd01", size = SizeType.SMALL)
    @Test(groups = {"atc", "smoke"})
    public void testNodesOnMap_10_3() throws Exception {
        testNodesOnMap();
    }
    
    @Tas(testBeds = @TestBed(name = UpgradeOracleEmTestBed_10_1.class, executeOn = "standalone"), owner = "korzd01", size = SizeType.SMALL)
    @Test(groups = {"atc", "smoke"})
    public void testNodesOnMap_Oracle_10_1() throws Exception {
        testNodesOnMap();
    }
    
    @Tas(testBeds = @TestBed(name = UpgradeOracleEmTestBed_10_2.class, executeOn = "standalone"), owner = "korzd01", size = SizeType.SMALL)
    @Test(groups = {"atc", "regression"})
    public void testNodesOnMap_Oracle_10_2() throws Exception {
        testNodesOnMap();
    }
    
    private void testNodesOnMap() throws Exception {
        ui = getUI();
        logger.info("should log into APM Server");
        ui.login();

        logger.info("should switch to Map view");
        ui.getLeftNavigationPanel().goToMapViewPage();

        String[] nodes = ui.getCanvas().getArrayOfNodeNames();
        
        logger.info("there should be nodes on the map");
        assertTrue(nodes.length > 0);
    }
}
