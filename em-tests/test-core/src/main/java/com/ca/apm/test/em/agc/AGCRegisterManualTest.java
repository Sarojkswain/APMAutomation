/*
 * Copyright (c) 2015 CA. All rights reserved.
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
package com.ca.apm.test.em.agc;

import javax.swing.JOptionPane;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ca.tas.test.em.agc.SimpleAgcTestBed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.ca.tas.type.SnapshotMode;

/**
 * Class for manual running registration tests with manual restarting follower and master.
 */
@Tas(testBeds = @TestBed(name = SimpleAgcTestBed.class, executeOn = "master"), owner = "kacpe02", size = SizeType.DEBUG, snapshot = SnapshotMode.LIVE)
@Test
public class AGCRegisterManualTest extends AGCRegisterTest {

    @BeforeSuite
    public void beforeSuite() {
     
    }
    
    @BeforeMethod
    public void beforeMethod() {
        
    }
    
    protected void stopFollower() {
        JOptionPane.showMessageDialog(null, "Stop follower and then click ok.");
    }

    protected void startFollower() {
        JOptionPane.showMessageDialog(null, "Start follower and then click ok.");
    }

    protected void restartFollower() {
        JOptionPane.showMessageDialog(null, "Restart follower and then click ok.");
    }
}
