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

import org.jetbrains.annotations.NotNull;

import com.ca.apm.automation.action.flow.utility.Win32RegistryFlow;
import com.ca.apm.automation.action.flow.utility.Win32RegistryFlowContext;
import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.DeployFreeRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.testbed.CodaTestBed;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

import static com.ca.apm.automation.action.flow.utility.Win32RegistryFlowContext.RegHive.LOCAL_MACHINE;
import static com.ca.apm.automation.action.flow.utility.Win32RegistryFlowContext.RegValueType.DWORD;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

/**
 * SupportabilityMetrics class.
 *
 * CQ Supportability metrics test-bed
 *
 * @author gamsa03@ca.com
 */
@TestBedDefinition
public class SupportabilityMetricsWindowsCodaTestbed extends CodaTestBed {
	
	public static final String JAVA8_ROLE_ID = "java8Role";
    public static final String JAVA_HOME = "C:\\automation\\deployed";
	
    @Override
    public ITestbed create(ITasResolver tasResolver) {

        IRole controllerRole = initControllerBuilder(tasResolver).build();
        ITestbedMachine controller = TestBedUtils
            .createWindowsMachine("controllerMachine", TEMPLATE_W64, controllerRole);
        ITestbed testBed = new Testbed(getTestBedName())
            .addMachine(controller);
 
        //create machines used in CODA test
        TestbedMachine TestMachine = new TestbedMachine.Builder("TestMachine").templateId(TEMPLATE_W64).build();
        DeployFreeRole c1Role = new DeployFreeRole("c1");
        String TestMachineHostname = tasResolver.getHostnameById(c1Role.getRoleId());
        //c1Role.addProperty("bea.home", "C:/sw/bea");
        //c1Role.addProperty("collector02.hostname", "tcssmoke07");
        //c1Role.addProperty("collector02.port", "5001");
        //c1Role.addProperty("mom.hostname", "tcssmoke05");
        //c1Role.addProperty("sap.em.password", "a20041013b");
        //c1Role.addProperty("sapem.stage.dir", "${java.io.tmpdir}/automation_stage/${role.name}");
        //c1Role.addProperty("weblogic.version", "10.3");
        //c1Role.addProperty("wls.agent.install.dir", "C:/sw/webapp/pipeorgandomain/wily");
        //c1Role.addProperty("wls.home", "C:/sw/bea/wlserver_10.3");
        //c1Role.addProperty("wls.port", "7011");
        c1Role.addProperty("Agent.host", TestMachineHostname);
        c1Role.addProperty("Base_dir", "C:/sw");
        c1Role.addProperty("c1Port", "5004");
        c1Role.addProperty("collector1.base", "c1");
        c1Role.addProperty("collector1.hostname", TestMachineHostname);
        c1Role.addProperty("collector01.hostname", TestMachineHostname);
        c1Role.addProperty("collector01.port", "5001");
        c1Role.addProperty("em.hostname", TestMachineHostname);
        c1Role.addProperty("em.loc", "C:/sw/em");
        c1Role.addProperty("em.password", "");
        c1Role.addProperty("em.username", "Admin");
        c1Role.addProperty("host", TestMachineHostname);
        c1Role.addProperty("install.dir", "C:/sw/supportability_metrics");
        c1Role.addProperty("install.parent.dir", "C:/sw");
        c1Role.addProperty("java.home", JAVA_HOME);
        c1Role.addProperty("max.heap.mb", "1024");
        c1Role.addProperty("max.permsize.mb", "256");
        c1Role.addProperty("min.heap.mb", "512");
        c1Role.addProperty("mom.base", "mom");
        c1Role.addProperty("mom.port", "5001");
        c1Role.addProperty("momPort", "5003");
        c1Role.addProperty("password", "");
        c1Role.addProperty("sap.em.username", "sapsupport");
        c1Role.addProperty("standalone.base", "st");
        c1Role.addProperty("stPort", "5001");
        c1Role.addProperty("testng.xml", "C:/sw/testng/testng.xml");
        c1Role.addProperty("username", "Admin");


        IRole java8Role = new JavaRole.Builder(JAVA8_ROLE_ID, tasResolver)
            .dir(JAVA_HOME)
            .build();


        TestMachine.addRole(c1Role, java8Role);        
        TestMachine.addRole(new DeployFreeRole("stga"));
        TestMachine.addRole(new DeployFreeRole("emdb"));
        TestMachine.addRole(new DeployFreeRole("client01"));
        TestMachine.addRole(new DeployFreeRole("st"));
        TestMachine.addRole(new DeployFreeRole("tomcatclient01"));
        TestMachine.addRole(new DeployFreeRole("mom")); 
        TestMachine.addRole(new DeployFreeRole("qcuploadtool01"));
        
        //To retrieve coda results and EM logs of the run

        TestMachine.addRemoteResource(RemoteResource.createFromRegExp(".*", "C:\\automation\\deployed\\results"));        
        TestMachine.addRemoteResource(RemoteResource.createFromRegExp(".*\\\\mom\\\\logs\\\\.*", "C:\\sw"));
        TestMachine.addRemoteResource(RemoteResource.createFromRegExp(".*\\\\st\\\\logs\\\\.*", "C:\\sw"));
        TestMachine.addRemoteResource(RemoteResource.createFromRegExp(".*\\\\c1\\\\logs\\\\.*", "C:\\sw"));


        testBed.addMachine(TestMachine);

        fixRegistryForJenkinsRole(tasResolver, controller, controller.getRoles());
        fixRegistryForJenkinsRole(tasResolver, TestMachine, TestMachine.getRoles());
        
        return testBed;
    }

    @NotNull
    @Override
    protected String getTestBedName() {
        return getClass().getSimpleName();
    }

    public static IRole fixRegistryForJenkinsRole(ITasResolver tasResolver, ITestbedMachine machine,
        IRole... beforeRoles) {
        Win32RegistryFlowContext context = new Win32RegistryFlowContext.Builder()
            .setValue(LOCAL_MACHINE, "SYSTEM\\CurrentControlSet\\Services\\LanmanServer"
                    + "\\Parameters\\SMB1",
                DWORD, 1)
            .build();

        UniversalRole role = new UniversalRole.Builder(machine.getMachineId() + "_SMBv1Enable",
            tasResolver)
            .runFlow(Win32RegistryFlow.class, context)
            .build();
        machine.addRole(role);

        if (beforeRoles != null) {
            for (IRole r : beforeRoles) {
                role.before(r);
            }
        }

        return role;
    }
}

