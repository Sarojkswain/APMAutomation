package com.ca.apm.systemtest.fld.testbed.smoke;

import static com.ca.apm.systemtest.fld.testbed.loads.FldLoadWurlitzerProvider.SYSTEM_XML;

import java.util.Arrays;
import java.util.Collections;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext.EmRoleEnum;
import com.ca.apm.systemtest.fld.role.loads.WurlitzerBaseRole;
import com.ca.apm.systemtest.fld.role.loads.WurlitzerLoadRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FLDMainClusterTestbed;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.apm.systemtest.fld.testbed.loads.FLDFakeWorkStationLoadProvider;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;

@TestBedDefinition
public class FLDFakeWorkStationLoadSmokeTestbed
    implements
        ITestbedFactory,
        FLDLoadConstants,
        FLDConstants {

    private EmRole collectorRole;
    private EmRole momRole;
    private EmRole dbRole;
    private ExecutionRole startCollector;
    private ExecutionRole startMom;
    private ExecutionRole startWv;

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        Testbed testbed = new Testbed("FLDFakeWorkStationLoadSmokeTestbed");

        testbed.addMachine(initDatabaseMachine(tasResolver));
        testbed.addMachine(initCollectorMachine(tasResolver));
        testbed.addMachine(initMomMachine(tasResolver));
        testbed.addMachine(initWurlitzerMachine(tasResolver, testbed));

        startMom.after(momRole);
        startCollector.after(startMom);
        dbRole.before(momRole, collectorRole);

        FldTestbedProvider fldTestbedProvider = new FLDFakeWorkStationLoadProvider(tasResolver.getDefaultVersion());
        testbed.addMachines(fldTestbedProvider.initMachines());
        fldTestbedProvider.initTestbed(testbed, tasResolver);

        return testbed;
    }

    private ITestbedMachine initDatabaseMachine(ITasResolver tasResolver) {
        TestbedMachine machine =
            new TestbedMachine.Builder(DATABASE_MACHINE_ID).templateId("co65").build();

        dbRole =
            new EmRole.LinuxBuilder(EM_DATABASE_ROLE_ID, tasResolver)
                .silentInstallChosenFeatures(Collections.singletonList("Database"))
                .dbuser(FLDMainClusterTestbed.DB_USERNAME)
                .dbpassword(FLDMainClusterTestbed.DB_PASSWORD)
                .dbAdminUser(FLDMainClusterTestbed.DB_ADMIN_USERNAME)
                .dbAdminPassword(FLDMainClusterTestbed.DB_ADMIN_PASSWORD).nostartEM().nostartWV()
                .build();

        machine.addRole(dbRole);
        return machine;
    }

    private TestbedMachine initCollectorMachine(ITasResolver tasResolver) {
        TestbedMachine coll03Machine =
            new TestbedMachine.Builder(COLL03_MACHINE_ID).bitness(Bitness.b64).templateId("w64")
                .build();

        String dbhost = tasResolver.getHostnameById(EM_DATABASE_ROLE_ID);
        collectorRole =
            new EmRole.Builder(EM_COLL03_ROLE_ID, tasResolver)
                .silentInstallChosenFeatures(Collections.singletonList("Enterprise Manager"))
                .dbhost(dbhost).dbuser(FLDMainClusterTestbed.DB_USERNAME)
                .dbpassword(FLDMainClusterTestbed.DB_PASSWORD)
                .dbAdminUser(FLDMainClusterTestbed.DB_ADMIN_USERNAME)
                .dbAdminPassword(FLDMainClusterTestbed.DB_ADMIN_PASSWORD)
                .emClusterRole(EmRoleEnum.COLLECTOR).nostartEM().nostartWV().build();

        ExecutionRole.Builder builder =
            new ExecutionRole.Builder(collectorRole.getRoleId() + "_start")
                .asyncCommand(collectorRole.getEmRunCommandFlowContext());
        startCollector = builder.build();

        RunCommandFlowContext command =
            new RunCommandFlowContext.Builder("C:\\Windows\\System32\\icacls.exe").args(
                Arrays.asList("C:\\automation", "/grant", "Everyone:(OI)(CI)M")).build();
        ExecutionRole updatePermissionRole =
            new ExecutionRole.Builder(COLL03_MACHINE_ID + "_" + "updatePermissionRole").flow(
                RunCommandFlow.class, command).build();

        updatePermissionRole.before(collectorRole);
        coll03Machine.addRole(collectorRole, startCollector, updatePermissionRole);
        return coll03Machine;
    }

    private ITestbedMachine initMomMachine(ITasResolver tasResolver) {
        TestbedMachine machine =
            new TestbedMachine.Builder(MOM_MACHINE_ID).templateId("w64").bitness(Bitness.b64)
                .build();

        momRole =
            new EmRole.Builder(EM_MOM_ROLE_ID, tasResolver)
                .silentInstallChosenFeatures(Arrays.asList("Enterprise Manager", "WebView"))
                .dbuser(FLDMainClusterTestbed.DB_USERNAME)
                .dbpassword(FLDMainClusterTestbed.DB_PASSWORD)
                .dbAdminUser(FLDMainClusterTestbed.DB_ADMIN_USERNAME)
                .dbAdminPassword(FLDMainClusterTestbed.DB_ADMIN_PASSWORD)
                .dbhost(tasResolver.getHostnameById(EM_DATABASE_ROLE_ID)).dbport(5432)
                .emClusterRole(EmRoleEnum.MANAGER).emCollector(collectorRole).nostartEM()
                .nostartWV().build();

        RunCommandFlowContext command =
            new RunCommandFlowContext.Builder("C:\\Windows\\System32\\icacls.exe").args(
                Arrays.asList("C:\\automation", "/grant", "Everyone:(OI)(CI)M")).build();
        ExecutionRole updatePermissionRole =
            new ExecutionRole.Builder(MOM_MACHINE_ID + "_" + "updatePermissionRole").flow(
                RunCommandFlow.class, command).build();

        ExecutionRole.Builder builder =
            new ExecutionRole.Builder(momRole.getRoleId() + "_start").asyncCommand(momRole
                .getEmRunCommandFlowContext());
        startMom = builder.build();
        builder =
            new ExecutionRole.Builder(momRole.getRoleId() + "_wvStart").asyncCommand(momRole
                .getWvRunCommandFlowContext());
        startWv = builder.build();
        startWv.after(startMom);

        updatePermissionRole.before(momRole);
        machine.addRole(momRole, updatePermissionRole, startMom, startWv);
        return machine;
    }

    private ITestbedMachine initWurlitzerMachine(ITasResolver tasResolver, Testbed testbed) {
        TestbedMachine wurlitzerMachine =
            new TestbedMachine.Builder(WURLITZER_06_MACHINE_ID).platform(Platform.WINDOWS)
                .bitness(Bitness.b64).templateId("w64").build();

        WurlitzerBaseRole wurlitzerBaseRole =
            new WurlitzerBaseRole.Builder(WURLITZER_BASE06_ROLE_ID, tasResolver).deployDir(
                "wurlitzerBase").build();
        wurlitzerMachine.addRole(wurlitzerBaseRole);

        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, EM_COLL03_ROLE_ID,
            WURLITZER_LOAD_BASE02_LOAD03_ROLE_ID, SYSTEM_XML,
            "1Portlet-23agents-110apps-1000EJBsession");

        return wurlitzerMachine;
    }

    private void addWurlitzer(Testbed testbed, ITasResolver tasResolver, ITestbedMachine machine,
        WurlitzerBaseRole wurlitzerBaseRole, String collectorRoleId, String wurlitzerRoleId,
        String buildFileLocation, String target) {
        EmRole emRole = (EmRole) testbed.getRoleById(collectorRoleId);

        WurlitzerLoadRole wurlitzerLoadrole =
            new WurlitzerLoadRole.Builder(wurlitzerRoleId, tasResolver).emRole(emRole)
                .buildFileLocation(buildFileLocation).target(target).logFile(target + ".log")
                .wurlitzerBaseRole(wurlitzerBaseRole).build();

        machine.addRole(wurlitzerLoadrole);
    }

}
