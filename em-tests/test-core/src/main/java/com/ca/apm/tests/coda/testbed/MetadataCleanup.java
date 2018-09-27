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

package com.ca.apm.tests.coda.testbed;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.jetbrains.annotations.NotNull;

import com.ca.apm.test.em.util.RoleUtility;
import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.ControllerRole;
import com.ca.tas.role.DeployFreeRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.testbed.CodaTestBed;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

/**
 * MetadataCleanup class.
 *
 * MetadataCleanup test-bed
 *
 * @author korzd01@ca.com
 */
@TestBedDefinition
public class MetadataCleanup extends CodaTestBed {

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        ControllerRole controllerRole = initControllerBuilder(tasResolver).build();
        ITestbedMachine controller = TestBedUtils
            .createWindowsMachine("controllerMachine", TEMPLATE_W64, controllerRole);
        RoleUtility.gatherJenkinsLogs(controllerRole, controller);
        ITestbed testBed = new Testbed(getTestBedName())
            .addMachine(controller);
 
        //create machines used in CODA test
        TestbedMachine metadata02 =
            new TestbedMachine.Builder("metadata02").templateId(TEMPLATE_W64)
                .automationBaseDir("C:\\sw").build();
        DeployFreeRole mom01Role = new DeployFreeRole("mom01");
        String hostname02 = tasResolver.getHostnameById(mom01Role.getRoleId());

        TestbedMachine metadata01 =
            new TestbedMachine.Builder("metadata01").templateId(TEMPLATE_W64)
                .automationBaseDir("C:\\sw").build();
        DeployFreeRole emdbRole = new DeployFreeRole("emdb");
        String hostname01 = tasResolver.getHostnameById(emdbRole.getRoleId());
        
        //mom01Role.addProperty("em.home.ip", "130.119.135.70");
        //mom01Role.addProperty("ipaddress", "130.119.135.70");
        //mom01Role.addProperty("java.home", "C:/sw/Java/jdk1.6.0_22");
        mom01Role.addProperty("agent.host", hostname02);
        mom01Role.addProperty("automation.install.dir", "C:/sw/metadata");
        mom01Role.addProperty("c1.host", hostname02);
        mom01Role.addProperty("c1.loc", "C:/sw/c1");
        mom01Role.addProperty("c2.host", hostname02);
        mom01Role.addProperty("c2.loc", "C:/sw/C2");
        mom01Role.addProperty("CLWJar", "C:/sw/em/lib/CLWorkstation.jar");
        mom01Role.addProperty("collector1.hostname", hostname02);
        mom01Role.addProperty("collector1.port", "5003");
        mom01Role.addProperty("collector2.hostname", hostname02);
        mom01Role.addProperty("collector2.port", "5004");
        mom01Role.addProperty("demoagent.deployment.dir", "C:/sw/DemoAgent");
        mom01Role.addProperty("em.host", hostname02);
        mom01Role.addProperty("em.loc", "C:/sw/em");
        mom01Role.addProperty("em.password", "");
        mom01Role.addProperty("em.port", "5001");
        mom01Role.addProperty("em.user", "Admin");
        mom01Role.addProperty("hostfullname", RoleUtility.hostnameToFqdn(hostname02));
        mom01Role.addProperty("install.parent.dir", "C:/sw");
        mom01Role.addProperty("max.heap.mb", "1024");
        mom01Role.addProperty("max.permsize.mb", "256");
        mom01Role.addProperty("min.heap.mb", "1024");
        mom01Role.addProperty("mom.host", hostname02);
        mom01Role.addProperty("mom.loc", "C:/sw/mom");
        mom01Role.addProperty("mom.port", "5002");
        mom01Role.addProperty("myresults.dir", "C:/sw/results_metadata");
        mom01Role.addProperty("sapem.stage.dir", "${java.io.tmpdir}/automation_stage/${role.name}");
        mom01Role.addProperty("YourKit.dir", "''");
        mom01Role.addProperty("YourKit.max.heap.mb", "8000");
        mom01Role.addProperty("YourKit.min.heap.mb", "8000");
        metadata02.addRole(mom01Role);
        
        metadata02.addRole(new DeployFreeRole("em01"));
        metadata02.addRole(new DeployFreeRole("col01"));
        metadata02.addRole(new DeployFreeRole("client01"));
        metadata02.addRole(new DeployFreeRole("col02"));
        metadata02.addRole(new DeployFreeRole("demoagent01"));
        
        UniversalRole dataRole =
            new UniversalRole.Builder("91_data", tasResolver)
                .unpack(new DefaultArtifact("com.ca.apm.coda.testdata.metadatacleanup", 
                    "91_data", "zip", "1.0"), "C:/sw/")
                .build();
        metadata02.addRole(dataRole);

        UniversalRole exampleRole =
            new UniversalRole.Builder("em_example", tasResolver)
                .unpack(new DefaultArtifact("com.ca.apm.powerpack.wls", 
                    "ppk-wls-em-dist", "windows-dist", "zip", "10.2.0.31"), "C:/sw/", "PowerPackForWebLogicServer")
                .build();
        metadata02.addRole(exampleRole);

        metadata02.addRemoteResource(RemoteResource.createFromLocation("C:/sw/results_metadata"));
        
        //emdbRole.addProperty("em.home.ip", "130.119.135.70");
        //emdbRole.addProperty("ipaddress", "130.119.135.72");
        emdbRole.addProperty("agent.host", hostname02);
        emdbRole.addProperty("automation.install.dir", "C:/sw/metadata");
        emdbRole.addProperty("c1.host", hostname02);
        emdbRole.addProperty("c1.loc", "C:/sw/c1");
        emdbRole.addProperty("c2.host", hostname02);
        emdbRole.addProperty("c2.loc", "C:/sw/C2");
        emdbRole.addProperty("CLWJar", "C:/sw/em/lib/CLWorkstation.jar");
        emdbRole.addProperty("collector1.hostname", hostname02);
        emdbRole.addProperty("collector1.port", "5003");
        emdbRole.addProperty("collector2.hostname", hostname02);
        emdbRole.addProperty("collector2.port", "5004");
        emdbRole.addProperty("demoagent.deployment.dir", "C:/sw/DemoAgent");
        emdbRole.addProperty("em.host", hostname02);
        emdbRole.addProperty("em.loc", "C:/sw/em");
        emdbRole.addProperty("em.password", "");
        emdbRole.addProperty("em.port", "5001");
        emdbRole.addProperty("em.user", "Admin");
        emdbRole.addProperty("hostfullname", RoleUtility.hostnameToFqdn(hostname01));
        emdbRole.addProperty("install.parent.dir", "C:/sw");
        emdbRole.addProperty("java.home", "C:/sw/Java/jdk1.6.0_22");
        emdbRole.addProperty("max.heap.mb", "1024");
        emdbRole.addProperty("max.permsize.mb", "256");
        emdbRole.addProperty("min.heap.mb", "1024");
        emdbRole.addProperty("mom.host", hostname02);
        emdbRole.addProperty("mom.loc", "C:/sw/mom");
        emdbRole.addProperty("mom.port", "5002");
        emdbRole.addProperty("myresults.dir", "C:/sw/results_metadata");
        emdbRole.addProperty("sapem.stage.dir", "${java.io.tmpdir}/automation_stage/${role.name}");
        emdbRole.addProperty("YourKit.dir", "''");
        emdbRole.addProperty("YourKit.max.heap.mb", "8000");
        emdbRole.addProperty("YourKit.min.heap.mb", "8000");
        metadata01.addRole(emdbRole);
        
        testBed.addMachine(metadata01, metadata02);

        RoleUtility.fixRegistryForJenkinsRole(tasResolver, controller, controller.getRoles());
        RoleUtility.fixRegistryForJenkinsRole(tasResolver, metadata01, metadata01.getRoles());
        RoleUtility.fixRegistryForJenkinsRole(tasResolver, metadata02, metadata02.getRoles());

        return testBed;
    }

    @NotNull
    @Override
    protected String getTestBedName() {
        return getClass().getSimpleName();
    }
}

