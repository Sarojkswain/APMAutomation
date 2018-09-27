package com.ca.apm.tests.testbed;

import com.ca.apm.tests.role.EmRollbackRole;
import com.ca.apm.tests.role.EmUpgradeRole;
import com.ca.tas.artifact.thirdParty.OracleDbVersion;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.oracle.OracleApmDbRole;
import com.ca.tas.role.testapp.custom.NowhereBankBTRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.tests.annotations.TestBedDynamicField;
import com.ca.tas.type.Platform;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@TestBedDefinition
public class RevMigrationTestBed implements ITestbedFactory {

    public static final String PARAM_TEMPLATE_ID = "templateId";
    public static final String PARAM_APM_VERSION = "apmVersion";
    public static final String PARAM_DB_TYPE = "dbType";

    public static final String DB_TYPE_POSTGRES = "postgres";
    public static final String DB_TYPE_ORACLE = "oracle";

    public static final String APM_9_7_1 = "9.7.1.42";
    public static final String APM_10_1 = "10.1.0.46";
    public static final String APM_10_2 = "10.2.0.66";
    public static final String APM_10_3 = "10.3.0.58";
    public static final String APM_10_5 = "10.5.0.36";
    public static final String APM_10_5_1 = "10.5.1.8"; //10.5.1.39
    public static final String APM_10_5_2 = "10.5.2.15";

    public static final String TEMPLATE_ID_DEFAULT = ITestbedMachine.TEMPLATE_CO7;
//    public static final String TEMPLATE_ID_DEFAULT = TEMPLATE_W64;
    public static final String APM_VERSION_DEFAULT = APM_10_1;
    public static final String DB_TYPE_DEFAULT = DB_TYPE_POSTGRES;


    @TestBedDynamicField(PARAM_TEMPLATE_ID)
    private String templateId = TEMPLATE_ID_DEFAULT;
    @TestBedDynamicField(PARAM_APM_VERSION)
    private String apmVersion = APM_VERSION_DEFAULT;
    @TestBedDynamicField(PARAM_DB_TYPE)
    private String dbType = DB_TYPE_DEFAULT;

    public static final String STANDALONE_MACHINE = "standalone";
    public static final String ORACLE_MACHINE = "oraclemachine";

    public static final String EM_ROLE_ID = "emRole";
    public static final String ORACLE_ROLE_ID = "oracleRole";
    public static final String NOWHEREBANK_ROLE = "nowherebankRole";

    public static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    public static final List<String> LAXNL_ADMIN_AUTH_OPTION = Arrays.asList(
            "-Dappmap.user=admin", "-Dappmap.token=" + ADMIN_AUX_TOKEN);

    public static final String WV_ADMIN_USER = "admin";
    public static final String WV_ADMIN_PASS = "";

    @Override
    public ITestbed create(ITasResolver iTasResolver) {
        final Testbed testbed = new Testbed(getClass().getSimpleName());

        // standalone deployment

        // EM standalone machine
        TestbedMachine machine = TestbedMachine.Builder.fromPlatform(/*iTasResolver, */getPlatform(), STANDALONE_MACHINE)
                .templateId(templateId)
                .bitness(Bitness.b64)
                .build();

        // em role
        EmRole.Builder emRoleBuilder = (Platform.WINDOWS.equals(machine.getPlatform()))
                ? new EmRole.Builder(EM_ROLE_ID, iTasResolver)
                : new EmRole.LinuxBuilder(EM_ROLE_ID, iTasResolver);

        emRoleBuilder
                .version(apmVersion)
                .emLaxNlJavaOption(LAXNL_ADMIN_AUTH_OPTION);

        // oracle role
        IRole oracleApmDbRole = addDatabase(iTasResolver, testbed, emRoleBuilder);
        EmRole emRole = emRoleBuilder.build();
        machine.addRole(emRole);
        testbed.addMachine(machine);

        if (oracleApmDbRole != null) {
            oracleApmDbRole.before(emRole);
        }

        // upgrade role
        IRole upgradeRole = addUpgradeRole(iTasResolver, machine, emRole);

        // rollback role
        addRollbackRole(iTasResolver, machine, emRole, upgradeRole);

        // nowherebank role
        IRole noWhereBank = addNowhereBankRole(NOWHEREBANK_ROLE, machine, iTasResolver);
        machine.addRole(noWhereBank);
        noWhereBank.after(emRole);

        return testbed;
    }

    protected IRole addDatabase(final ITasResolver tasResolver, final Testbed testbed, final EmRole.Builder emRoleBuilder) {
        // must be here, as TAS has problem with dynamic machines in testbed....
        TestbedMachine oraMachine = new TestbedMachine.Builder(ORACLE_MACHINE)
                .platform(Platform.WINDOWS)
                .templateId(ITestbedMachine.TEMPLATE_W64)
                .bitness(Bitness.b64)
                .automationBaseDir("C:/sw")
                .build();
        testbed.addMachine(oraMachine);

        if (DB_TYPE_POSTGRES.equalsIgnoreCase(dbType)) {
            // Postgres id default, just skip
            NoOpRole noOpRole = new NoOpRole(ORACLE_ROLE_ID);
            oraMachine.addRole(noOpRole);
            return noOpRole;

        } else if (DB_TYPE_ORACLE.equalsIgnoreCase(dbType)) {
            // Oracle - add role & machine to install oracle
            OracleApmDbRole oracleApmDbRole = new OracleApmDbRole.Builder(ORACLE_ROLE_ID, tasResolver)
                    .version(apmVersion.startsWith("9.") ? OracleDbVersion.Oracle11gR2EEw
                                                         : OracleDbVersion.Oracle12cR1EEw)
                    .build();

            oraMachine.addRole(oracleApmDbRole);

            emRoleBuilder.useOracle(oracleApmDbRole);

            return oracleApmDbRole;

        } else {
            throw new IllegalArgumentException("unknown dbType: "+dbType);
        }
    }

    protected IRole addUpgradeRole(final ITasResolver tasResolver, final ITestbedMachine machine, final EmRole emRole) {
        final String upgradeRoleId = machine.getMachineId() + "_upgrade";

        EmUpgradeRole role = EmUpgradeRole.Builder.fromPlatform(machine.getPlatform(), upgradeRoleId, tasResolver)
                .olderEmInstallDir(emRole.getInstallDir())
                .silentInstallChosenFeatures(emRole.getSerializedSilentInstallChosenFeatures())
                .sampleResponseFile(machine.getAutomationBaseDir() + "installers/em/installer.properties")
                .caEulaPath("/ca-eula.silent.txt")
                .nostartUpgrade()
                .build();
        role.after(emRole);
        machine.addRole(role);
        return role;
    }

    protected void addRollbackRole(final ITasResolver tasResolver, final ITestbedMachine machine, final EmRole emRole,
                                   final IRole upgradeRole) {
        final String rollbackRoleId = machine.getMachineId() + "_rollback";
        EmRollbackRole.Builder builder = EmRollbackRole.Builder.fromPlatform(tasResolver, machine.getPlatform(), rollbackRoleId)
                .olderEmInstallDir(emRole.getInstallDir())
                .nostartRollback()
                .useReversedMigration();
        EmRollbackRole emRollbackRole = builder.build();
        machine.addRole(emRollbackRole);
        emRollbackRole.after(upgradeRole);
    }

    private NowhereBankBTRole addNowhereBankRole(String roleId, TestbedMachine machine, ITasResolver tasResolver) {
        String automationBaseDir = machine.getAutomationBaseDir();
        NowhereBankBTRole.Builder builder = (Platform.LINUX.equals(machine.getPlatform()))
                ? new NowhereBankBTRole.LinuxBuilder(roleId, tasResolver)
                : new NowhereBankBTRole.Builder(roleId, tasResolver);
        builder.stagingBaseDir(automationBaseDir)
                .noStart();
        return builder.build();
    }


    public Platform getPlatform() {
        switch (templateId) {
            case ITestbedMachine.TEMPLATE_W64:
            case ITestbedMachine.TEMPLATE_W10:
            case ITestbedMachine.TEMPLATE_W12: {
                return Platform.WINDOWS;
            }

            case ITestbedMachine.TEMPLATE_CO7:
            case ITestbedMachine.TEMPLATE_CO65:
            case ITestbedMachine.TEMPLATE_CO66:
            case ITestbedMachine.TEMPLATE_RH7:
            case ITestbedMachine.TEMPLATE_RH66: {
                return Platform.LINUX;
            }

            default: {
                throw new IllegalArgumentException("Unsupported platform of template: "+templateId);
            }
        }
    }

    private static class NoOpRole extends AbstractRole {
        public NoOpRole(final String roleId) {
            super(roleId);
        }

        @Override
        public void deploy(final IAutomationAgentClient aaClient) {
        }
    }
}
