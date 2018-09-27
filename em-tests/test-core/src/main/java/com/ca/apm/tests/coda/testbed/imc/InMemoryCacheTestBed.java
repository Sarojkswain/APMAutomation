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

package com.ca.apm.tests.coda.testbed.imc;

import java.util.Arrays;

import org.jetbrains.annotations.NotNull;

import com.ca.apm.test.em.util.RoleUtility;
import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.ControllerRole;
import com.ca.tas.role.DeployFreeRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.CodaTestBed;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

/**
 * Testbed for InMemoryCache CODA tests.
 * Currently Windows only, to run on Linux some changes to tests are needed.
 *
 * @author myspe01@ca.com
 */
@TestBedDefinition
public class InMemoryCacheTestBed extends CodaTestBed {

    private static final String BASE_DIR = TasBuilder.WIN_SOFTWARE_LOC;
    private static final String ST_DIR = "st";
    private static final String TESTNG_DIR = "/testng";
    private static final int TOMCAT_PORT = 8090;

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        ControllerRole controllerRole = initControllerBuilder(tasResolver).build();
        ITestbedMachine controller = TestBedUtils
            .createWindowsMachine("controllerMachine", TEMPLATE_W64, controllerRole);
        RoleUtility.gatherJenkinsLogs(controllerRole, controller);
        ITestbed testBed = new Testbed(getTestBedName())
            .addMachine(controller);
        
        // run everything on controller machine
        ITestbedMachine machine01 = testBed.getMachineById(CONTROLLER_MACHINE_ID);

        // EM
        EmRole.Builder em01RoleBuilder = new EmRole.Builder("st", tasResolver);
        String baseDir = codifyPath(BASE_DIR);
        String emInstallDir = baseDir + "/" + ST_DIR;
        em01RoleBuilder.installDir(emInstallDir);
        em01RoleBuilder.nostartWV();
        em01RoleBuilder.silentInstallChosenFeatures(Arrays.asList("Enterprise Manager", "ProbeBuilder", "EPA", "Database"));
        em01RoleBuilder.emLaxNlJavaOption(Arrays.asList("-Xms512m", "-Xmx1024m", "-Djava.awt.headless=false", 
            "-Dcom.wily.assert=false", "-XX:MaxPermSize=256m", "-showversion", "-XX:+UseConcMarkSweepGC", "-XX:+UseParNewGC", 
            "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError", "-verbosegc"));
        em01RoleBuilder.configProperty("introscope.apm.agentcontrol.clw.enable", "true");
        em01RoleBuilder.configProperty("log4j.logger.additivity.Manager.LoadBalancer", "false");
        em01RoleBuilder.configProperty("introscope.enterprisemanager.loadbalancing.interval", "120");        
        em01RoleBuilder.configProperty("log4j.appender.logfile.File", emInstallDir + "/logs/em.out.txt"); // Must be full path?
        em01RoleBuilder.configProperty("log4j.logger.Manager.LoadBalancer", "DEBUG");
        EmRole em01Role = em01RoleBuilder.build();
        em01Role.addProperty("Agent.host", "localhost");
        em01Role.addProperty("Base_dir", baseDir);
        em01Role.addProperty("config.dir", "/");
        em01Role.addProperty("em.hostname", "localhost");
        em01Role.addProperty("em.password", "");
        em01Role.addProperty("em.username", "Admin");
        em01Role.addProperty("emPort", "5001");
        em01Role.addProperty("host", "localhost");
        em01Role.addProperty("install.dir", baseDir + TESTNG_DIR);
        em01Role.addProperty("install.parent.dir", baseDir);
        em01Role.addProperty("java.home", codifyPath(TasBuilder.WIN_JDK_1_7_51));
        em01Role.addProperty("password", "");
        em01Role.addProperty("standalone.base", ST_DIR);
        em01Role.addProperty("stPort", "5001");
        em01Role.addProperty("username", "Admin");
        machine01.addRole(em01Role);
        
        // Tomcat 5.5
        TomcatRole.Builder tomcatRoleBuilder = new TomcatRole.Builder("tomcatclient01", tasResolver);
        tomcatRoleBuilder.tomcatVersion(TomcatVersion.v55).tomcatCatalinaPort(TOMCAT_PORT);
        TomcatRole tomcatRole = tomcatRoleBuilder.build();
        machine01.addRole(tomcatRole);
        
        // JavaAgent
        AgentRole.Builder agent01Builder = new AgentRole.Builder("agent01", tasResolver);
        agent01Builder.emRole(em01Role);
        agent01Builder.webAppServer(tomcatRole);
        agent01Builder.platform(ArtifactPlatform.WINDOWS);
        agent01Builder.webAppAutoStart();
        machine01.addRole(agent01Builder.build());
        
        // Testing role - not converted to TAS
        machine01.addRole(new DeployFreeRole("client01"));
        
        // Get complete results dir
        machine01.addRemoteResource(RemoteResource.createFromLocation("c:/automation/deployed/results"));

        RoleUtility.fixRegistryForJenkinsRole(tasResolver, controller, controller.getRoles());
        if (machine01 != controller) {
            RoleUtility.fixRegistryForJenkinsRole(tasResolver, machine01, machine01.getRoles());
        }
        
        return testBed;
    }

    @NotNull
    @Override
    protected String getTestBedName() {
        return getClass().getSimpleName();
    }
    
}

