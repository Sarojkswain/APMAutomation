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
 */

package com.ca.apm.tests.testbed;

import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;

/**
 * 1 MOM + 2 Collectors, connected into cluster
 * Collector machines also have one Tomcat + agent
 */
@TestBedDefinition
public class OneCdvOneMomTwoCollectorsLinuxTestbed extends OneCdvOneMomTwoCollectorsAbstractTestbed {
    public OneCdvOneMomTwoCollectorsLinuxTestbed() {
        super();
        TESTBED_NAME = "OneCdvOneMomTwoCollectorsLinuxTestbed";
        PLATFORM = Platform.LINUX;
        NODE_TEMPLATE = ITestbedMachine.TEMPLATE_RH66;
        EM_INSTALL_DIR = "/opt/em";
        TOMCAT_INSTALL_DIR = "/opt/apache-tomcat-5.5.34";
    }
}
