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

import java.util.ArrayList;

import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class SimpleJmeterTestbed implements ITestbedFactory {

    public static final String JMETER_ROLE_ID              = "jmeterRole";
    public static final String JMETER_SCRIPTS_ROLE_ID      = "jmeterScriptsRole";   
    public static final String JMETER_PERMISSIONS_ROLE_ID  = "jmeterPermissionsRole";
    public static final String TEMPLATE_ID                 = ITestbedMachine.TEMPLATE_CO66;
    public static final String JMETER_MACHINE              = "jmeterMachine";
    public static final String JMETER_PARENT_HOME          = "/opt/automation";
    public static final String JMETER_HOME                 = "/opt/automation/apache-jmeter-2.12";
    public static final String JMETER_SCRIPTS_HOME         = "/opt/automation/jmeter_scripts";

	@Override
	public ITestbed create(ITasResolver tasResolver) {

        ITestbedMachine machine = TestBedUtils.createLinuxMachine(JMETER_MACHINE, TEMPLATE_ID);
        
        ArrayList<IRole> roles = createJmeterRoles(tasResolver);        
        for (IRole role: roles ){
            machine.addRole(role);
        }
        
        ITestbed testbed = new Testbed(getClass().getSimpleName());     
        testbed.addMachine(machine);
        return testbed;
	}
	
	public static ArrayList<IRole> createJmeterRoles(ITasResolver tasResolver) {
	    
	    GenericRole jmeterRole = createJmeterBundleRole (tasResolver);		  
	    GenericRole jmeterScriptsRole = createJmeterScriptsRole(tasResolver);		
	    ExecutionRole permissionsRole = createJmeterPermissionRole (tasResolver);

	    jmeterRole.before(permissionsRole);
	    permissionsRole.after(jmeterRole);
		ArrayList<IRole> roles = new ArrayList<IRole>();
		roles.add(jmeterRole);
		roles.add(jmeterScriptsRole);
		roles.add(permissionsRole);
		return roles;
	}	
	
	private static GenericRole createJmeterBundleRole(ITasResolver tasResolver) {
	    
        DefaultArtifact jmeterArtifact = new DefaultArtifact("com.ca.apm.binaries", "apache-jmeter", "", "zip", "2.12");        
        
        GenericRole jmeterRole = new GenericRole.Builder(JMETER_ROLE_ID, tasResolver)
        .unpack(jmeterArtifact, JMETER_PARENT_HOME)
        .build(); 
        
        return jmeterRole;
	}
	
	private static GenericRole createJmeterScriptsRole(ITasResolver tasResolver) {
        
	    DefaultArtifact jmxScripts = new DefaultArtifact("com.ca.apm.tests",
            "nodeagent-tests-core",
            "dist",
            "zip",
            tasResolver.getDefaultVersion());
	    
        GenericRole jmeterScriptsRole = new GenericRole.Builder(JMETER_SCRIPTS_ROLE_ID, tasResolver)
        .unpack(jmxScripts, JMETER_SCRIPTS_HOME)
        .build();
        
        return jmeterScriptsRole;        
    }

	private static ExecutionRole createJmeterPermissionRole(ITasResolver tasResolver) {
    
        ArrayList<String> args = new ArrayList<String>();
        args.add("a+x");
        args.add(SimpleJmeterTestbed.JMETER_HOME + "/bin/jmeter.sh");
        args.add(SimpleJmeterTestbed.JMETER_HOME + "/bin/shutdown.sh");
        
        RunCommandFlowContext permissionsContext = new RunCommandFlowContext.Builder("chmod")
        .args(args)
        .workDir("/bin")
        .build();
        
        ExecutionRole permissionsRole =
            new ExecutionRole.Builder(JMETER_PERMISSIONS_ROLE_ID)
            .flow(RunCommandFlow.class, permissionsContext)
            .build();
        
        return permissionsRole;
	}
}
