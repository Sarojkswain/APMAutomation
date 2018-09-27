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
package com.ca.apm.testbed.aca;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.testapp.custom.NowhereBankBTRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.ca.apm.automation.action.flow.em.DeployEMFlowContext.EmRoleEnum.*;

/**
 */
@TestBedDefinition
public class AcaUniverseTestbed implements ITestbedFactory {

    public static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    public static final String TOKEN_ADMIN = "tokenadmin";
    public static final String ADMIN = "admin";

    public static final String MACHINE_ID_ETC = "etc";
    public static final String MACHINE_ID_STANDALONE = "standalone";
    public static final String MACHINE_ID_COLLECTOR = "collector";
    public static final String MACHINE_ID_MOM = "mom";

    public static final String ROLE_ID_ETC = "etcEM";
    public static final String ROLE_ID_ETC_COLLECTOR = "etcCollectorEM";
    public static final String ROLE_ID_MOM = "momEM";
    public static final String ROLE_ID_MOM_COLLECTOR_LOCAL = "momCollectorLocal";
    public static final String ROLE_ID_MOM_COLLECTOR_REMOTE = "momCollectorRemote";
    public static final String ROLE_ID_STANDALONE = "standalone";
    // Nowhere bank roles
    public static final String ROLE_ID_ETC_NWB = "etcNwb";
    public static final String ROLE_ID_COLLECTOR_NWB = "collectorNwb";
    public static final String ROLE_ID_MOM_NWB = "momNwb";

    private static final String TEAM_CENTER_MASTER_PROPERTY = "introscope.apmserver.teamcenter.master";
    private static final String TRANSACTION_TRACES_FAST_BUFFER_PROPERTY =
        "introscope.enterprisemanager.transactiontrace.arrivalbuffer.incubationtime.fast";
    private static final String FAST_BUFFER_DEFAULT_TIME = "30";

    private static final String COLLECTOR_INSTALL_SUB_DIR = "collector";
    private static final List<String> COLLECTOR_CHOSEN_FEATURES =
        Arrays.asList(new String[] {"Enterprise Manager"});
    private static final int COLLECTOR_EM_WEB_PORT = 8083;
    private static final int COLLECTOR_EM_PORT = 5002;

    // admin user from ldap doesn't have any permission, hence tokenAdmin
    public static final Collection<String> ETC_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Dappmap.token=" + ADMIN_AUX_TOKEN, "-Dappmap.user=" + TOKEN_ADMIN,
        "-Dcom.wily.assert=false", "-XX:+HeapDumpOnOutOfMemoryError", "-verbosegc");

    public static final Collection<String> EM_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Dappmap.token=" + ADMIN_AUX_TOKEN, "-Dappmap.user=" + ADMIN,
        "-Dcom.wily.assert=false", "-XX:+HeapDumpOnOutOfMemoryError", "-verbosegc");

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed("ACACrossCluster");
        /* -- Initialize machines -- */
        ITestbedMachine etcMachine = createLinuxMachine(MACHINE_ID_ETC);
        ITestbedMachine standaloneMachine = createLinuxMachine(MACHINE_ID_STANDALONE);
        ITestbedMachine collectorMachine = createLinuxMachine(MACHINE_ID_COLLECTOR);
        ITestbedMachine momMachine = createLinuxMachine(MACHINE_ID_MOM);

        /* -- Define roles -- */
        EmRole etc, etcCollector, mom, collectorLocal, collectorRemote, standalone;
        NowhereBankBTRole etcNwb, collectorNwb, momNwb;

        /* -- Build roles -- */
        etcCollector = buildCollectorRole(ROLE_ID_ETC_COLLECTOR, tasResolver);
        etc = buildEtcEMRole(ROLE_ID_ETC, tasResolver, etcCollector);

        collectorLocal = buildCollectorRole(ROLE_ID_MOM_COLLECTOR_LOCAL,tasResolver);
        collectorRemote = buildRemoteCollectorRole(ROLE_ID_MOM_COLLECTOR_REMOTE, ROLE_ID_MOM, tasResolver);
        EmRole[] collectors = new EmRole[]{collectorLocal, collectorRemote};
        mom = buildMOMRole(ROLE_ID_MOM, collectors, tasResolver);

        standalone = defaultEMBuilder(ROLE_ID_STANDALONE, tasResolver)
            .emLaxNlJavaOption(EM_LAXNL_JAVA_OPTION)
            .build();

        // 3 Instances of nowherebank
        etcNwb = buildNowhereBankRole(ROLE_ID_ETC_NWB, etcMachine.getAutomationBaseDir(), tasResolver);
        collectorNwb = buildNowhereBankRole(ROLE_ID_COLLECTOR_NWB, collectorMachine.getAutomationBaseDir(), tasResolver);
        momNwb = buildNowhereBankRole(ROLE_ID_MOM_NWB, momMachine.getAutomationBaseDir(), tasResolver);

        /* -- Role orchestration -- */

        /* -- Map roles to machines -- */
        etcMachine.addRole(etc, etcCollector, etcNwb);
        standaloneMachine.addRole(standalone);
        collectorMachine.addRole(collectorRemote,collectorNwb);
        momMachine.addRole(mom,collectorLocal,momNwb);

        /* -- Add machines to testbed -- */
        testbed.addMachine(etcMachine, standaloneMachine, collectorMachine, momMachine);

        return testbed;
    }

    private NowhereBankBTRole buildNowhereBankRole(String roleId, String automationBaseDir, ITasResolver tasResolver) {
        NowhereBankBTRole.LinuxBuilder builder = new NowhereBankBTRole.LinuxBuilder(roleId, tasResolver);
        builder.stagingBaseDir(automationBaseDir)
               .noStart();
        return builder.build();
    }

    private EmRole buildMOMRole(String roleId, EmRole[] collectors, ITasResolver tasResolver) {
        EmRole.Builder builder = defaultEMBuilder(roleId, tasResolver);
        builder.emClusterRole(MANAGER)
            .emLaxNlJavaOption(EM_LAXNL_JAVA_OPTION);
        for(EmRole collector : collectors){
            builder.emCollector(collector);
        }

        return builder.build();
    }

    private EmRole buildRemoteCollectorRole(String roleId, String momRoleId, ITasResolver tasResolver) {
        EmRole.Builder builder = defaultEMBuilder(roleId, tasResolver);
        builder.emClusterRole(COLLECTOR)
            .dbhost(tasResolver.getHostnameById(momRoleId))
            .silentInstallChosenFeatures(COLLECTOR_CHOSEN_FEATURES)
            .installSubDir(COLLECTOR_INSTALL_SUB_DIR);
        return builder.build();
    }

    private EmRole buildCollectorRole(String roleId, ITasResolver tasResolver) {
        EmRole.Builder builder = defaultEMBuilder(roleId, tasResolver);
        builder.emClusterRole(COLLECTOR)
            .silentInstallChosenFeatures(COLLECTOR_CHOSEN_FEATURES)
            .installSubDir(COLLECTOR_INSTALL_SUB_DIR)
            .emPort(COLLECTOR_EM_PORT)
            .emWebPort(COLLECTOR_EM_WEB_PORT);

        return builder.build();
    }

    private EmRole buildEtcEMRole(String roleId, ITasResolver tasResolver, EmRole collector) {
        EmRole.Builder builder = defaultEMBuilder(roleId, tasResolver);
        builder.emClusterRole(MANAGER)
            .configProperty(TEAM_CENTER_MASTER_PROPERTY, String.valueOf(Boolean.TRUE))
            .emLaxNlJavaOption(ETC_LAXNL_JAVA_OPTION)
            .emCollector(collector);
        return builder.build();
    }

    private EmRole.Builder defaultEMBuilder(String roleId, ITasResolver tasResolver) {
        EmRole.Builder emRoleBuilder =
            new EmRole.LinuxBuilder(roleId, tasResolver)
                .configProperty(TRANSACTION_TRACES_FAST_BUFFER_PROPERTY,FAST_BUFFER_DEFAULT_TIME)
                .nostartEM()
                .nostartWV();
        return emRoleBuilder;
    }

    private ITestbedMachine createLinuxMachine(String machineId) {
        return new TestbedMachine.LinuxBuilder(machineId)
            .platform(Platform.LINUX)
            .templateId("rh66")
            .bitness(Bitness.b64)
            .build();
    }
}
