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

package com.ca.apm.tests.testbed;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.apm.tests.role.JavaRole;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO66;

/**
 * JavaSmokeTestbed class.
 *
 * Java template testbed to illustrate the test-bed instrumentation using custom roles and flows
 *
 * @author pojja01@ca.com
 */
@TestBedDefinition
public class JavaTestbed implements ITestbedFactory {

    public static final String JAVA_MACHINE_ID = "javaMachine";
    public static final String JAVA_MACHINE_TEMPLATE_ID = TEMPLATE_CO66;
    public static final String JAVA7_ROLE_ID = "java7Role";
    public static final String JAVA8_ROLE_ID = "java8Role";
    public static final String JAVA_HOME = "/opt/automation/deployed/java8";

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        // create a Java 7 role
        IRole java7Role = new JavaRole.LinuxBuilder(JAVA7_ROLE_ID, tasResolver).build();
        // create a Java 8 role
        IRole java8Role =
            new JavaRole.LinuxBuilder(JAVA8_ROLE_ID, tasResolver).dir(JAVA_HOME)
                .version(JavaBinary.LINUX_64BIT_JRE_18).build();

        ITestbedMachine javaMachine =
            new TestbedMachine.LinuxBuilder(JAVA_MACHINE_ID).templateId(JAVA_MACHINE_TEMPLATE_ID)
                .build();
        javaMachine.addRole(java8Role);
        javaMachine.addRole(java7Role);

        ITestbed testbed = new Testbed("JavaSmokeTestbed");
        testbed.addMachine(javaMachine);

        return testbed;
    }
}
