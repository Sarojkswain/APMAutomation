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

package com.ca.tas.test.em.agc;

import java.util.Arrays;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlow;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlowContext;
import com.ca.apm.test.em.util.RoleUtility;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.NginxRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class AgcTelemetryTestbed implements ITestbedFactory {

    public static final String MASTER_ROLE_ID = "master_em";
    public static final String PROVIDER_ROLE_ID = "provider_em";
    public static final String STANDALONE_ROLE_ID = "standalone_em";

    public static final String STANDALONE_ID = "standalone_id";
    public static final String COLLECTOR_ID_SUFFIX = "_collector_id";
    public static final String MOM_ID_SUFFIX = "_mom_id";

    public static final String MASTER_MACHINE = "master";
    public static final String PROVIDER_MACHINE = "provider";
    public static final String STANDALONE_MACHINE = "standalone";

    public static final String NGINX_ROLE_ID = "nginx";
    public static final String LOG_FOLDER = "/var/log/";
    public static final String POST_DATA_PATH = LOG_FOLDER + "nginx/postdata.log";

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed("Introscope/AppMap/AgcTelemetry");

        ITestbedMachine masterMachine = TestBedUtils.createLinuxMachine(MASTER_MACHINE,
                                            ITestbedMachine.TEMPLATE_CO66);
        IRole masterRole = addEmRoles(masterMachine, MASTER_ROLE_ID, true, tasResolver);
        
        ITestbedMachine providerMachine = TestBedUtils.createLinuxMachine(PROVIDER_MACHINE,
                                            ITestbedMachine.TEMPLATE_CO66);
        IRole providerRole = addEmRoles(providerMachine, PROVIDER_ROLE_ID, false, tasResolver);

        ITestbedMachine standaloneMachine = TestBedUtils.createLinuxMachine(STANDALONE_MACHINE,
                                            ITestbedMachine.TEMPLATE_CO66);
        IRole standaloneRole = addStandalone(standaloneMachine, STANDALONE_ROLE_ID, tasResolver);

        NginxRole nginxRole =
            new NginxRole.LinuxBuilder(NGINX_ROLE_ID, tasResolver).configurationResource(
                "/com/ca/tas/test/em/telemetry/nginx.conf").build();
        standaloneMachine.addRole(nginxRole);
        
        nginxRole.before(masterRole, providerRole, standaloneRole);
        testbed.addMachine(masterMachine, providerMachine, standaloneMachine);
        return testbed;
    }

    private IRole addEmRoles(ITestbedMachine machine, String roleId, boolean master, ITasResolver tasResolver) {
        EmRole.Builder collectorBuilder =
            new EmRole.LinuxBuilder(machine.getMachineId() + "_collector", tasResolver)
                    .dbpassword("quality")
                    .installSubDir("collector")
                    .emPort(5003)
                    .emWebPort(8003)
                    .emLaxNlJavaOption(RoleUtility.getDevEmLaxnlJavaOption(9003))
                    .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                    .silentInstallChosenFeatures(
                        Arrays.asList("Enterprise Manager", "ProbeBuilder", "Database"))
                    .nostartEM();
        addTelemetryProperties(machine.getMachineId() + COLLECTOR_ID_SUFFIX, false, collectorBuilder, tasResolver);
        EmRole collectorRole = collectorBuilder.build();
        IRole collectorConfig = createTelemetryConfig(collectorRole, tasResolver);
        collectorConfig.after(collectorRole);
        IRole startCollectorRole = RoleUtility.addStartEmRole(machine, collectorRole, false, collectorConfig);
        EmRole.Builder momBuilder =
            new EmRole.LinuxBuilder(roleId, tasResolver)
                    .dbpassword("quality")
                    .dbhost(tasResolver.getHostnameById(collectorRole.getRoleId()))
                    .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
                    .emCollector(collectorRole)
                    .emLaxNlJavaOption(RoleUtility.getDevEmLaxnlJavaOption(9001))
                    .silentInstallChosenFeatures(
                        Arrays.asList("Enterprise Manager", "WebView", "ProbeBuilder"))
                    .nostartEM();
        addTelemetryProperties(machine.getMachineId() + MOM_ID_SUFFIX, master, momBuilder, tasResolver);
        if (master) {
            momBuilder.configProperty("introscope.apmserver.teamcenter.master", "true");
        }
        EmRole momRole = momBuilder.build();
        IRole momConfig = createTelemetryConfig(momRole, tasResolver);
        momConfig.after(momRole);
        IRole startMomRole = RoleUtility.addStartEmRole(machine, momRole, true, momConfig);
        machine.addRole(collectorRole, collectorConfig, startCollectorRole, momRole, momConfig, startMomRole);
        momRole.after(collectorRole);
        return momRole;
    }

    private IRole addStandalone(ITestbedMachine machine, String roleId, ITasResolver tasResolver) {
        EmRole.Builder standaloneBuilder =
            new EmRole.LinuxBuilder(roleId, tasResolver)
                    .dbpassword("quality")
                    .emLaxNlJavaOption(RoleUtility.getDevEmLaxnlJavaOption(9001))
                    .nostartEM();
        addTelemetryProperties(STANDALONE_ID, false, standaloneBuilder, tasResolver);
        EmRole standaloneRole = standaloneBuilder.build();
        IRole standaloneConfig = createTelemetryConfig(standaloneRole, tasResolver);
        standaloneConfig.after(standaloneRole);
        IRole startStandaloneRole = RoleUtility.addStartEmRole(machine, standaloneRole, true, standaloneConfig);
        machine.addRole(standaloneRole, standaloneConfig, startStandaloneRole);
        return standaloneRole;
    }
    
    private void addTelemetryProperties(String instaneId, boolean agc, EmRole.Builder emBuilder, ITasResolver tasResolver) {
        emBuilder.configProperty("introscope.apmserver.telemetry.service.instance.id", instaneId);
        if (agc) {
            emBuilder.configProperty("introscope.apmserver.telemetry.service.enabled", "true");
            emBuilder.configProperty("introscope.apmserver.telemetry.service.url",
                "http://" + tasResolver.getHostnameById(NGINX_ROLE_ID));
        }
    }
    
    private IRole createTelemetryConfig(EmRole em, ITasResolver tasResolver) {
        FileCreatorFlowContext configContext = new FileCreatorFlowContext.Builder()
                .fromResource("/com/ca/tas/test/em/telemetry/telemetry.json")
                .destinationPath(em.getDeployEmFlowContext().getInstallDir() + "/config/telemetry.json")
                .build();
        IRole createConfigRole = new UniversalRole.Builder(em.getRoleId() + "_create_config", tasResolver)
                .runFlow(FileCreatorFlow.class, configContext).build();
        createConfigRole.after(em);
        return createConfigRole;
    }
}
