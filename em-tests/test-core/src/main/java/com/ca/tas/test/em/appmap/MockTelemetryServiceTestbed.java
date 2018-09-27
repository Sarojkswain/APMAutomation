/*
 * Copyright (c) 2017 CA. All rights reserved.
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

package com.ca.tas.test.em.appmap;

import com.ca.apm.automation.action.flow.utility.FileCreatorFlow;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlowContext;
import com.ca.apm.test.em.util.RoleUtility;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.NginxRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;

@TestBedDefinition
public class MockTelemetryServiceTestbed implements ITestbedFactory {

    public static final String EM_ROLE = "role_em";
    public static final String NGINX_ROLE = "role_nginx";
    public static final String EM_MACHINE = "emMachine";
    public static final String NGINX_MACHINE = "nginxMachine";
    public static final String LOG_FOLDER = "/var/log/";
    public static final String POST_DATA_PATH = LOG_FOLDER + "nginx/postdata.log";

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed("Introscope/AppMap/MockTelemetry");

        ITestbedMachine nginxMachine =
            new TestbedMachine.Builder(NGINX_MACHINE).platform(Platform.LINUX).templateId("co66")
                .bitness(Bitness.b64).build();

        NginxRole nginxRole =
            new NginxRole.LinuxBuilder(NGINX_ROLE, tasResolver).configurationResource(
                "/com/ca/tas/test/em/telemetry/nginx.conf").build();
        nginxMachine.addRole(nginxRole);
        
        ITestbedMachine emMachine =
            new TestbedMachine.Builder(EM_MACHINE).platform(Platform.WINDOWS)
                .templateId("w64").bitness(Bitness.b64).build();
        
        EmRole.Builder emBuilder = new EmRole.Builder(EM_ROLE, tasResolver).dbpassword("quality")
                .emLaxNlJavaOption(RoleUtility.getDevEmLaxnlJavaOption(9001)).nostartEM()
                .nostartWV();
        emBuilder.configProperty("introscope.apmserver.telemetry.service.enabled", "true");
        emBuilder.configProperty("introscope.apmserver.telemetry.service.instance.id", "TestInstanceId");
        emBuilder.configProperty("introscope.apmserver.telemetry.service.url",
            "http://" + tasResolver.getHostnameById(NGINX_ROLE));

        EmRole emRole = emBuilder.build();
        emMachine.addRole(emRole);

        FileCreatorFlowContext configContext = new FileCreatorFlowContext.Builder()
                .fromResource("/com/ca/tas/test/em/telemetry/telemetry.json")
                .destinationPath(emRole.getDeployEmFlowContext().getInstallDir() + "/config/telemetry.json")
                .build();
        IRole createConfigRole = new UniversalRole.Builder("create_config", tasResolver)
                        .runFlow(FileCreatorFlow.class, configContext).build();
        createConfigRole.after(emRole);
        emMachine.addRole(createConfigRole);

        RoleUtility.addMmRole(emMachine, emRole.getRoleId() + "_mm", emRole, "NowhereBankMM");
        IRole startReqRole = RoleUtility.addNowhereBankRole(emMachine, emRole, null, tasResolver);
        IRole startEmRole = RoleUtility.addStartEmRole(emMachine, emRole, true, emRole);
        startEmRole.after(createConfigRole, nginxRole);
        startReqRole.after(startEmRole);
        
        testbed.addMachine(nginxMachine, emMachine);
        return testbed;
    }

}
