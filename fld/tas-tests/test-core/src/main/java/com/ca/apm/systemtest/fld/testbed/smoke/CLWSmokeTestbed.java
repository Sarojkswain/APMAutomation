package com.ca.apm.systemtest.fld.testbed.smoke;

import static com.ca.apm.systemtest.fld.testbed.loads.FldLoadWurlitzerProvider.SYSTEM_XML;
import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getJavaDir;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.Arrays;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext.EmRoleEnum;
import com.ca.apm.systemtest.fld.artifact.thirdparty.FLDJmeterScriptsVersion;
import com.ca.apm.systemtest.fld.artifact.thirdparty.JMeterVersion;
import com.ca.apm.systemtest.fld.role.CLWWorkStationLoadRole;
import com.ca.apm.systemtest.fld.role.JMeterRole;
import com.ca.apm.systemtest.fld.role.loads.JMeterLoadRole;
import com.ca.apm.systemtest.fld.role.loads.WurlitzerBaseRole;
import com.ca.apm.systemtest.fld.role.loads.WurlitzerLoadRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FLDMainClusterTestbed;
import com.ca.apm.systemtest.fld.testbed.loads.FldLoadTomcatProvider;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;

/**
 * Smoke test for the {@link CLWWorkStationLoadRole}.  Installs a complete EM, sets up the wurlitzer 
 * appmap-stress script 1Portlet-23agents-110apps-1000EJBsession, sets up 4x tomcat with applications,
 * and sets up JMeter loads for the Tomcats
 * @author keyja01
 *
 */
@TestBedDefinition
public class CLWSmokeTestbed implements ITestbedFactory, FLDConstants, FLDLoadConstants {
    private static final Logger log = LoggerFactory.getLogger(CLWSmokeTestbed.class);
    
    private JMeterRole jmeterRole;
    private EmRole coll;
    private EmRole mom;
    private EmRole dbRole;
    

    private ExecutionRole startCollector;
    private ExecutionRole startMom;

    private ExecutionRole startWv;

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        Testbed testbed = new Testbed("CLWSmokeTestbed");
        
        testbed.addMachine(initDatabaseMachine(tasResolver));
        testbed.addMachine(initCollectorMachine(tasResolver));
        testbed.addMachine(initMomMachine(tasResolver));
        testbed.addMachine(initWurlitzerMachine(tasResolver, testbed));
        testbed.addMachine(initClwMachine(tasResolver));
        testbed.addMachine(initJmeterMachine(tasResolver));
        
        // Ensure that the collector does not start until the database is available
        startMom.after(mom);
        startCollector.after(startMom);
        dbRole.before(mom, coll);
        
        FldLoadTomcatProvider provider = new FldLoadTomcatProvider();
        testbed.addMachines(provider.initMachines());
        provider.initTestbed(testbed, tasResolver);
        
        return testbed;
    }
    
    
    private ITestbedMachine initDatabaseMachine(ITasResolver tasResolver) {
        TestbedMachine machine = new TestbedMachine.Builder(DATABASE_MACHINE_ID).templateId("co65").build();
        
        dbRole = new EmRole.LinuxBuilder(EM_DATABASE_ROLE_ID, tasResolver)
            .silentInstallChosenFeatures(Collections.singletonList("Database"))
            .dbuser(FLDMainClusterTestbed.DB_USERNAME)
            .dbpassword(FLDMainClusterTestbed.DB_PASSWORD)
            .dbAdminUser(FLDMainClusterTestbed.DB_ADMIN_USERNAME)
            .dbAdminPassword(FLDMainClusterTestbed.DB_ADMIN_PASSWORD)
            .nostartEM()
            .nostartWV()
            .build();
        
        machine.addRole(dbRole);
        
        return machine;
    }
    
    
    private ITestbedMachine initMomMachine(ITasResolver tasResolver) {
        TestbedMachine machine = new TestbedMachine.Builder(MOM_MACHINE_ID).templateId("w64").bitness(Bitness.b64)
            .build();
        
        mom = new EmRole.Builder(EM_MOM_ROLE_ID, tasResolver)
            .silentInstallChosenFeatures(Arrays.asList("Enterprise Manager", "WebView"))
            .dbuser(FLDMainClusterTestbed.DB_USERNAME)
            .dbpassword(FLDMainClusterTestbed.DB_PASSWORD)
            .dbAdminUser(FLDMainClusterTestbed.DB_ADMIN_USERNAME)
            .dbAdminPassword(FLDMainClusterTestbed.DB_ADMIN_PASSWORD)
            .dbhost(tasResolver.getHostnameById(EM_DATABASE_ROLE_ID))
            .dbport(5432)
            .emClusterRole(EmRoleEnum.MANAGER)
            .emCollector(coll)
            .nostartEM()
            .nostartWV()
            .build();

        RunCommandFlowContext command = new RunCommandFlowContext.Builder("C:\\Windows\\System32\\icacls.exe")
            .args(Arrays.asList("C:\\automation", "/grant", "Everyone:(OI)(CI)M"))
            .build();
        ExecutionRole updatePermissionRole =
            new ExecutionRole.Builder(MOM_MACHINE_ID + "_" + "updatePermissionRole")
            .flow(RunCommandFlow.class, command)
            .build();
    
        ExecutionRole.Builder builder = new ExecutionRole.Builder(mom.getRoleId() + "_start")
            .asyncCommand(mom.getEmRunCommandFlowContext());
        startMom = builder.build();
        builder = new ExecutionRole.Builder(mom.getRoleId() + "_wvStart")
            .asyncCommand(mom.getWvRunCommandFlowContext());
        startWv = builder.build();
        startWv.after(startMom);
    
        updatePermissionRole.before(mom);
        machine.addRole(mom, updatePermissionRole, startMom, startWv);
        
        return machine;
    }
    
    
    private ITestbedMachine initJmeterMachine(ITasResolver tasResolver) {
        TestbedMachine machine = new TestbedMachine.Builder(JMETER_MACHINE_01_ID).templateId("w64").bitness(Bitness.b64)
            .build();
        
        JavaRole javaRole = new JavaRole.Builder("jmeterJavaRole", tasResolver)
            .dir(getJavaDir(JavaBinary.WINDOWS_64BIT_JDK_17))
            .version(JavaBinary.WINDOWS_64BIT_JDK_17)
            .build();

        
        jmeterRole = new JMeterRole.Builder(JMETER_ROLE_01_ID, tasResolver)
            .jmeterVersion(JMeterVersion.v213)
            .jmeterScriptsArchive(FLDJmeterScriptsVersion.v10_3_1)
            .customJava(javaRole)
            .build();
        
        String hostTomcat9080 = tasResolver.getHostnameById(TOMCAT_9080_ROLE_ID);
        configureLoad(machine, tasResolver, 0, 1, JMETER_LOAD_ROLE_TOMCAT9080_01_ID, "Tomcat_fld_01_02_Tomcat_9080_Axis2.jmx", hostTomcat9080, 8080);
        configureLoad(machine, tasResolver, 0, 1, JMETER_LOAD_ROLE_WURLITZER_TOMCAT9080_01_ID, "wurlitzer_fld_01_Tomcat_9080.jmx", hostTomcat9080, 8080);
    
        String hostTomcat9081 = tasResolver.getHostnameById(TOMCAT_9081_ROLE_ID);
        configureLoad(machine, tasResolver, 0, 1, JMETER_LOAD_ROLE_TOMCAT9081_01_ID, "Tomcat_fld_01_01_Tomcat_9081_Axis2.jmx", hostTomcat9081, 8080);
        configureLoad(machine, tasResolver, 0, 1, JMETER_LOAD_ROLE_WURLITZER_TOMCAT9081_01_ID, "wurlitzer_fld_01_Tomcat_9081.jmx", hostTomcat9081, 8080);
        
        
        String host6Tomcat9091 = tasResolver.getHostnameById(TOMCAT_6_ROLE_ID);
        configureLoad(machine, tasResolver, 0, 2, JMETER_LOAD_ROLE_6TOMCAT9091_01_ID, "Tomcat_fld_01_03_Tomcat6_9091_Axis2.jmx", host6Tomcat9091, 8080);
        configureLoad(machine, tasResolver, 0, 2, JMETER_LOAD_ROLE_6TOMCAT9091T_01_ID, "Tomcat_fld_01_04_Tomcat6_9091_Trade.jmx", host6Tomcat9091, 8080);
        
        String host7Tomcat9090 = tasResolver.getHostnameById(TOMCAT_7_ROLE_ID);
        configureLoad(machine, tasResolver, 0, 2, JMETER_LOAD_ROLE_7TOMCAT9090_01_ID, "Tomcat_fld_01_05_Tomcat7_9090_Axis2.jmx", host7Tomcat9090, 8080);
        
        machine.addRole(javaRole, jmeterRole);
        
        return machine;
    }
    
    
    private ITestbedMachine initClwMachine(ITasResolver tasResolver) {
        TestbedMachine clwMachine = new TestbedMachine.Builder(CLW_MACHINE_ID).templateId("w64").bitness(Bitness.b64)
            .build();
        
        CLWWorkStationLoadRole load = new CLWWorkStationLoadRole.Builder(CLW_ROLE_ID, tasResolver)
            .emHost(tasResolver.getHostnameById(EM_COLL03_ROLE_ID))
            .agentName(TOMCAT_HOST_NAME + "\\|" + TOMCAT6_AGENT + ".*")
            .agentName(TOMCAT_HOST_NAME + "\\|" + TOMCAT7_AGENT + ".*")
            .agentName(TOMCAT_HOST_NAME + "\\|" + TOMCAT_AGENT_9080 + ".*")
            .agentName(TOMCAT_HOST_NAME + "\\|" + TOMCAT_AGENT_9081 + ".*")
            .cleanupPeriod(1, MINUTES)
            .build();
        
        clwMachine.addRole(load);
        return clwMachine;
    }


    private ITestbedMachine initWurlitzerMachine(ITasResolver tasResolver, Testbed testbed) {
        TestbedMachine wurlitzerMachine =
            new TestbedMachine.Builder(WURLITZER_06_MACHINE_ID).platform(Platform.WINDOWS)
                .bitness(Bitness.b64).templateId("w64").build();
        WurlitzerBaseRole wurlitzerBaseRole =
            new WurlitzerBaseRole.Builder(WURLITZER_BASE06_ROLE_ID, tasResolver).deployDir(
                "wurlitzerBase").build();
        wurlitzerMachine.addRole(wurlitzerBaseRole);

        addWurlitzer(testbed, tasResolver, wurlitzerMachine, wurlitzerBaseRole, 
            EM_COLL03_ROLE_ID, WURLITZER_LOAD_BASE02_LOAD03_ROLE_ID, 
            SYSTEM_XML, "1Portlet-23agents-110apps-1000EJBsession");
        
        return wurlitzerMachine;
    }


    private TestbedMachine initCollectorMachine(ITasResolver tasResolver) {
        TestbedMachine coll03Machine = new TestbedMachine.Builder(COLL03_MACHINE_ID).bitness(Bitness.b64)
            .templateId("w64").build();
    
        String dbhost = tasResolver.getHostnameById(EM_DATABASE_ROLE_ID);
        coll = new EmRole.Builder(EM_COLL03_ROLE_ID, tasResolver)
            .silentInstallChosenFeatures(Collections.singletonList("Enterprise Manager"))
            .dbhost(dbhost)
            .dbuser(FLDMainClusterTestbed.DB_USERNAME)
            .dbpassword(FLDMainClusterTestbed.DB_PASSWORD)
            .dbAdminUser(FLDMainClusterTestbed.DB_ADMIN_USERNAME)
            .dbAdminPassword(FLDMainClusterTestbed.DB_ADMIN_PASSWORD)
            .emClusterRole(EmRoleEnum.COLLECTOR)
            .nostartEM()
            .nostartWV()
            .build();
        
        ExecutionRole.Builder builder = new ExecutionRole.Builder(coll.getRoleId() + "_start")
            .asyncCommand(coll.getEmRunCommandFlowContext());
        startCollector = builder.build();

        RunCommandFlowContext command = new RunCommandFlowContext.Builder("C:\\Windows\\System32\\icacls.exe")
            .args(Arrays.asList("C:\\automation", "/grant", "Everyone:(OI)(CI)M"))
            .build();
        ExecutionRole updatePermissionRole =
            new ExecutionRole.Builder(COLL03_MACHINE_ID + "_" + "updatePermissionRole")
            .flow(RunCommandFlow.class, command)
            .build();
    
        updatePermissionRole.before(coll);
        
        coll03Machine.addRole(coll, startCollector, updatePermissionRole);
        
        return coll03Machine;
    }
    

    
    private void addWurlitzer(Testbed testbed, ITasResolver tasResolver, ITestbedMachine machine,
                              WurlitzerBaseRole wurlitzerBaseRole, String collectorRoleId,
                              String wurlitzerRoleId, String buildFileLocation, 
                              String target) {
        EmRole emRole = (EmRole) testbed.getRoleById(collectorRoleId);
        WurlitzerLoadRole wurlitzerLoadrole =
            new WurlitzerLoadRole.Builder(wurlitzerRoleId, tasResolver)
                .emRole(emRole).buildFileLocation(buildFileLocation)
                .target(target)
                .logFile(target + ".log")
                .wurlitzerBaseRole(wurlitzerBaseRole).build();
        machine.addRole(wurlitzerLoadrole);
    }


    private void configureLoad(ITestbedMachine machine, ITasResolver tasResolver, int machineIdx, int timIdx, String roleId,
                               String script, String targetHost, int targetPort) {
        log.info("Configuring JMeter load {} ({}) -> {}:{}", roleId, script, targetHost, targetPort);
        String hostHeader = targetHost;
        if (targetHost == null) {
            // HACK - when using tas:deploy, hostnames are not yet properly worked out during the init phase
            targetHost = "localhost";
        }
        JMeterLoadRole jmeterLoadRole = new JMeterLoadRole.Builder(roleId, tasResolver)
            .host(targetHost)
            .hostHeader(hostHeader)
            .port(targetPort)
            .script(script)
            .resultFile(script.replace(".jmx", ".csv"))
            .isOutputToFile(true)
            .jmeter(jmeterRole)
            .build();
        
        machine.addRole(jmeterLoadRole);
    }
}
