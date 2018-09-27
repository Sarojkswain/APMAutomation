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

package com.ca.tas.test.em.agc;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.ManagementModuleRole;
import com.ca.tas.role.testapp.custom.NowhereBankBTRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.testbed.*;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;

import java.util.Arrays;
import java.util.Collection;

/**
 * Testbed for verifying the cross cluster trace correlation.
 * 
 */
@TestBedDefinition
public class CrossClusterTracesTestBed implements ITestbedFactory {

    private static final String SCRIPT_LOCATION = "scriptLocation";
    private static final String SCRIPT_FILE = "/tmp/add_token.sh";

    private static final String EM_CONF_PROP_TT_TIME_FAST =
        "introscope.enterprisemanager.transactiontrace.arrivalbuffer.incubationtime.fast";
    private static final String AGC_CONF_PROP_TT_TIME_FAST =
        "introscope.apmserver.agcmaster.correlation.buffer.incubationtime.fast";

    public static final String AGC_COLLECTOR_ROLE_ID = "agc_collector";

    public static final String AGC_ROLE_ID = "agc_em";
    public static final String MOM_ROLE_ID = "mom_em";
    public static final String STANDALONE_ROLE_ID = "introscope";

    public static final String ADMIN_AUX_TOKEN_HASHED =
                    "8f400c257611ed5d30c0e6607ac61074307dfa24cf70a8e92c3e8147d67d2c70";
    public static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    public static final String NWB_ROLE_ID_SUFFIX = "_nowherebank";
    public static final String MACHINE_ID_COLLECTOR = "collector";
    public static final String MACHINE_ID_AGC = "agc";
    public static final String MACHINE_ID_STANDALONE = "standalone";
    public static final String MACHINE_ID_MOM = "mom";

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        ITestbed testbed = new Testbed("AGC/NowhereBank");

        /* -- Initialize machines -- */
        ITestbedMachine agcMachine = createLinuxMachine(MACHINE_ID_AGC);
        ITestbedMachine standaloneMachine = createLinuxMachine(MACHINE_ID_STANDALONE);
        ITestbedMachine collectorMachine = createLinuxMachine(MACHINE_ID_COLLECTOR);
        ITestbedMachine momMachine = createLinuxMachine(MACHINE_ID_MOM);

        ITestbedMachine followers[] = {standaloneMachine, momMachine};

        /* -- Role definitions -- */
        EmRole standaloneRole = addStandaloneRoles(standaloneMachine, tasResolver);
        IRole followerTokenRole = addAuxTokenRole(standaloneMachine, standaloneRole);
        addStartEmRole(standaloneMachine, standaloneRole, false, followerTokenRole);
        EmRole agcRole = addMomRoles(agcMachine, null, followers, tasResolver);
        IRole agcTokenRole = addAuxTokenRole(agcMachine, agcRole);
        addStartEmRole(agcMachine, agcRole, true, agcTokenRole);
        EmRole collectorRole = addCollectorRoles(collectorMachine, "", tasResolver);
        EmRole momRole = addMomRoles(momMachine, collectorRole, null, tasResolver);
        IRole momTokenRole = addAuxTokenRole(momMachine, momRole);
        addStartEmRole(momMachine, momRole, false, momTokenRole);
        /* -- Role orchestration -- */
        // Done in the methods
        /* -- Map roles to machines -- */
        // Done in the methods
        /* -- Add machines to testbed -- */
        testbed.addMachine(standaloneMachine);
        testbed.addMachine(collectorMachine);
        testbed.addMachine(momMachine);
        testbed.addMachine(agcMachine);
        return testbed;
    }

    public static EmRole addStandaloneRoles(ITestbedMachine machine, ITasResolver tasResolver) {
        EmRole.Builder emBuilder =
            new EmRole.LinuxBuilder("introscope", tasResolver)
                .dbpassword("quality")
                .configProperty(EM_CONF_PROP_TT_TIME_FAST, "30")
                .nostartEM()
                .nostartWV();
        EmRole emStandaloneRole = emBuilder.build();
        
        machine.addRole(emStandaloneRole);
        addMmRole(machine, emStandaloneRole, "StatusTestMM");
        addNowhereBankRole(machine, emStandaloneRole, tasResolver);
        return emStandaloneRole;
    }
    
    private EmRole addMomRoles(ITestbedMachine machine, EmRole remoteCollectorRole, ITestbedMachine followers[], ITasResolver tasResolver) {
        EmRole collectorRole = addCollectorRoles(machine, "_collector", tasResolver);
        EmRole emMomRole; // used for installing MM
        EmRole.Builder emBuilder;
        emBuilder = new EmRole.LinuxBuilder(machine.getMachineId() + "_em", tasResolver)
                .dbpassword("quality")
                .dbhost(tasResolver.getHostnameById(collectorRole.getRoleId()))
                .emPort(5003)
                .wvEmPort(5003)
                // we need .emWebPort(8081), because WV uses this for proxy
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
                .emCollector(collectorRole)
                .silentInstallChosenFeatures(
                    Arrays.asList("Enterprise Manager", "WebView", "ProbeBuilder"))
                .configProperty(AGC_CONF_PROP_TT_TIME_FAST, "30")
                .configProperty(EM_CONF_PROP_TT_TIME_FAST, "30")
                .nostartEM()
                .nostartWV();
        if (remoteCollectorRole != null) {
            emBuilder.emCollector(remoteCollectorRole);
        }
        if (followers != null) {
            emBuilder.configProperty("introscope.apmserver.teamcenter.master", "true");
        }
        emMomRole = emBuilder.build();
        emMomRole.after(collectorRole);
        machine.addRole(emMomRole);
        addMmRole(machine, emMomRole, "NowhereBankMM");
        return emMomRole;
    }
    
    private EmRole addCollectorRoles(ITestbedMachine machine, String idSuffix,
                                     ITasResolver tasResolver) {
        EmRole.Builder emBuilder;
        emBuilder = new EmRole.LinuxBuilder(machine.getMachineId() + idSuffix, tasResolver)
                    .dbpassword("quality")
                    .installSubDir("collector")
                    .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                    // we need .emPort(5001), because NowhereBank uses it for sending data
                    .emWebPort(8083)
                    .silentInstallChosenFeatures(
                        Arrays.asList("Enterprise Manager", "ProbeBuilder", "Database"))
                    .configProperty(EM_CONF_PROP_TT_TIME_FAST, "30")
                    .nostartWV();
        EmRole collectorRole = emBuilder.build();
        machine.addRole(collectorRole);

        addNowhereBankRole(machine, collectorRole, tasResolver);
        return collectorRole;
    }
    
    private ITestbedMachine createLinuxMachine(String machineId) {
        return new TestbedMachine.LinuxBuilder(machineId)
            .platform(Platform.LINUX)
            .templateId("rh66")
            .bitness(Bitness.b64)
            .build();
    }
    
    public static void addNowhereBankRole(ITestbedMachine machine, EmRole emRole,
                                          ITasResolver tasResolver) {
        NowhereBankBTRole.Builder nowhereBankBuilder =
            new NowhereBankBTRole.LinuxBuilder(machine.getMachineId() + NWB_ROLE_ID_SUFFIX, tasResolver)
                .stagingBaseDir(machine.getAutomationBaseDir())
            .noStart();
        
        NowhereBankBTRole nowhereBankRole = nowhereBankBuilder.build();
        nowhereBankRole.after(emRole);
        machine.addRole(nowhereBankRole);
    }

    public static void addMmRole(ITestbedMachine machine, EmRole emRole, String mmName) {
        // install MM to EM
        ManagementModuleRole mmRole =
                new ManagementModuleRole(emRole.getRoleId() + "_mm", "/" + mmName + ".jar",
                    emRole.getDeployEmFlowContext().getInstallDir());

        mmRole.after(emRole);
        machine.addRole(mmRole);
    }

    public static IRole addAuxTokenRole(ITestbedMachine machine, IRole beforeRole) {
        // creates AGC token in the DB
        Collection<String> data = Arrays.asList(
            "export PGPASSWORD=Lister@123",
            "/opt/automation/deployed/database/bin/psql --username=postgres --dbname=cemdb"
                + " --command=\"INSERT INTO appmap_api_keys(id, username, date_created, hashed_token, description) "
                + "VALUES (NEXTVAL('seq_appmap_api_key_id'), 'Admin', NOW(), "
                + "'" + ADMIN_AUX_TOKEN_HASHED + "', "
                + "'{\\\"system\\\":false,\\\"description\\\":\\\"TAS aux token\\\"}')\""
        );
        
        FileModifierFlowContext createFileFlow =
            new FileModifierFlowContext.Builder().create(SCRIPT_FILE, data).build();
        ExecutionRole execRole =
            new ExecutionRole.Builder(machine.getMachineId() + "_token")
                .flow(FileModifierFlow.class, createFileFlow)
                .asyncCommand(new RunCommandFlowContext.Builder(SCRIPT_FILE).build()).build();
        execRole.addProperty(SCRIPT_LOCATION, SCRIPT_FILE);
        execRole.after(beforeRole);
        machine.addRole(execRole);
        return execRole;
    }

    public static void addStartEmRole(ITestbedMachine machine, EmRole emRole, boolean startWv, IRole beforeRole) {
        // starts EM and WebView
        ExecutionRole.Builder builder = new ExecutionRole.Builder(emRole.getRoleId() + "_start")
                                                    .asyncCommand(emRole.getEmRunCommandFlowContext());
        if (startWv) {
            builder.asyncCommand(emRole.getWvRunCommandFlowContext());
        }
        ExecutionRole startRole = builder.build();
        startRole.after(beforeRole);
        machine.addRole(startRole);
    }
}
