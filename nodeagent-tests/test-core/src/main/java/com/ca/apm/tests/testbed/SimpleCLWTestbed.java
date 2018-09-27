/*
 * Copyright (c) 2015 CA.  All rights reserved.
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

package com.ca.apm.tests.testbed;

import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class SimpleCLWTestbed implements ITestbedFactory {

    public static final String TEMPLATE_ID       = ITestbedMachine.TEMPLATE_CO66;
    public static final String CLW_MACHINE       = "clwMachine";
    public static final String CLW_ROLE_ID       = "clwRole";
    public static final String CLW_UNIX_JAR_HOME = "/opt/automation/deployed/em/lib/";
    public static final String CLW_WIN_JAR_HOME  = "C:\\automation\\deployed\\em\\lib\\";

	@Override
	public ITestbed create(ITasResolver tasResolver) {

        GenericRole clwRole = createCLWRole(tasResolver, CLW_UNIX_JAR_HOME);       
        ITestbedMachine machine = TestBedUtils.createLinuxMachine(CLW_MACHINE, TEMPLATE_ID, clwRole);     
        ITestbed testbed = new Testbed(getClass().getSimpleName());     
        testbed.addMachine(machine);
        return testbed;
	}
	
	public static GenericRole createCLWRole(ITasResolver tasResolver,
	                                        String home) {
        
        DefaultArtifact clwJar = new DefaultArtifact("com.ca.apm.em",
            "com.wily.introscope.clw.feature",
            "",
            "jar",
            tasResolver.getDefaultVersion());
        
        GenericRole clwRole = new GenericRole.Builder(CLW_ROLE_ID, tasResolver)
        .download(clwJar, home + "CLWorkstation.jar")
        .build();
        
        return clwRole;        
    }
}
