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

import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;

/**
 * 1 MOM + 1 Collector on a single server, connected into cluster
 * Collector machine also has one Tomcat + agent
 */
@TestBedDefinition
public class OneMomOneCollectorWindowsTestbed extends OneMomOneCollectorAbstractTestbed
    implements
        ITestbedFactory {


    public OneMomOneCollectorWindowsTestbed() {
        super();

        TESTBED_NAME = "OneMomOneCollectorWindowsTestbed";

        NODE_TEMPLATE = ITestbedMachine.TEMPLATE_W64;

        EM_INSTALL_DIR = "C:\\sw\\em";

        TOMCAT_INSTALL_DIR = "C:\\sw\\apache-tomcat-5.5.34";

        PLATFORM = Platform.WINDOWS;

    }

}
