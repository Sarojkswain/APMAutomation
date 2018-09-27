package com.ca.apm.systemtest.fld.testbed.devel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext.EmRoleEnum;
import com.ca.apm.systemtest.fld.role.AGCRegisterRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FLDMainClusterTestbed;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.apm.systemtest.fld.testbed.loads.FldControllerLoadProvider;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.TIMRole;
import com.ca.tas.role.EmRole.Builder;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;

/**
 * @author filja01
 */
@TestBedDefinition
public class ATCUITestbed implements ITestbedFactory, FLDLoadConstants, FLDConstants {

    public static final String AGCDB_ROLE_ID = "agcdbRole";
    public static final String AGCDB_MACHINE_ID = "agcdbMachine";
    public static final String AGC_MACHINE_ID = "agcMachine";
    public static final String AGC_ROLE_ID = "agcRole";
    
    public static final String EM_VERSION = "99.99.landing-SNAPSHOT";
    
    public static final String LOAD_TEMPLATE_ID = "w64";
    
    public static final int DB_PORT = 5432;
    public static final String DB_ADMIN_USER = "pgadmin";
    public static final String DB_ADMIN_PASSWORD = "password";
    public static final String DB_USER = "postgres";
    public static final String DB_PASSWORD = "password";
    public static final String DB_NAME = "cemdb";
    public static final int WV_PORT = 8084;
    public static final int EMWEB_PORT = 8081;
    
    public static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    
    private static final Collection<String> MOM_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true", "-Dmail.mime.charset=UTF-8",
        "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseConcMarkSweepGC",
        "-XX:+UseParNewGC", "-Xss256k", "-Dcom.wily.assert=false", "-showversion",
        "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError", "-Xms2048m",
        "-Xmx4096m", "-verbose:gc", "-Dappmap.user=admin",
        "-Dappmap.token=" + ADMIN_AUX_TOKEN);

    private static final Collection<String> COLL_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true", "-Dmail.mime.charset=UTF-8",
        "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseConcMarkSweepGC",
        "-XX:+UseParNewGC", "-Xss256k", "-Dcom.wily.assert=false", "-showversion",
        "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError", "-Xms2048m",
        "-Xmx4096m", "-verbose:gc");

    private static final Collection<String> AGC_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true", "-XX:MaxPermSize=256m", "-Dmail.mime.charset=UTF-8",
        "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseConcMarkSweepGC",
        "-XX:+UseParNewGC", "-Xss256k", "-Dcom.wily.assert=false", "-showversion",
        "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError", "-Xms2048m",
        "-Xmx8192m", "-verbose:gc", "-Dappmap.user=admin",
        "-Dappmap.token=" + ADMIN_AUX_TOKEN);
    
    public static final Collection<String> WV_LAXNL_JAVA_OPTION =
        Arrays
            .asList(
                "-Djava.awt.headless=true",
                "-Dorg.owasp.esapi.resources=./config/esapi",
                "-Dsun.java2d.noddraw=true",
                "-javaagent:./product/webview/agent/wily/Agent.jar",
                "-Dcom.wily.introscope.agentProfile=./product/webview/agent/wily/core/config/IntroscopeAgent.profile",
                "-Dcom.wily.introscope.wilyForWilyPrefix=com.wily", "-Xms2048m", "-Xmx2048m",
                "-XX:+PrintGCDateStamps", "-XX:+HeapDumpOnOutOfMemoryError", "-verbose:gc");
    
    @Override
    public ITestbed create(ITasResolver tasResolver) {
        
        Testbed testbed = new Testbed("FLDATTestbed");
        
        //TestbedMachine tim = new TestbedMachine.LinuxBuilder(TIM01_MACHINE_ID).templateId(ITestbedMachine.TEMPLATE_CO65).build();
        
        //TIMRole timRole = new TIMRole.Builder(TIM01_ROLE_ID, tasResolver)
        //    .timVersion("99.99.landing-SNAPSHOT")
        //    .build();
        //tim.addRole(timRole);
     
        /*
        TestbedMachine collMachine = em(COLL01_MACHINE_ID);
        List<EmRole> collectors = new ArrayList<>();
        collectors.add(emRole(EM_COLL01_ROLE_ID, tasResolver));
        collMachine.addRoles(collectors);
        
        TestbedMachine mom = em(MOM_MACHINE_ID);
        EmRole momRole = momRole(EM_MOM_ROLE_ID, tasResolver, null, collectors);
        mom.addRole(momRole);
        
        testbed.addMachine(mom, collMachine);
        
        
        // AGC+MOM TestBed

        ITestbedMachine agcMachine = em(AGC_MACHINE_ID);
        EmRole.LinuxBuilder agcBuilder = new EmRole.LinuxBuilder(AGC_ROLE_ID, tasResolver);

        String emHost = tasResolver.getHostnameById(AGC_ROLE_ID);

        // AGC role settings
        agcBuilder
            .silentInstallChosenFeatures(
                Arrays.asList("Enterprise Manager", "ProbeBuilder", "EPA", "WebView", "Database"))
            .emClusterRole(EmRoleEnum.MANAGER)
            .dbuser(FLDMainClusterTestbed.DB_USERNAME).dbpassword(DB_PASSWORD)
            .emWebPort(EMWEB_PORT)
            .wvPort(WV_PORT)
            .dbAdminPassword(DB_ADMIN_PASSWORD)
            .dbAdminUser(DB_ADMIN_USER)
            .version(EM_VERSION)
            .configProperty("introscope.apmserver.teamcenter.master", "true")
            .emLaxNlClearJavaOption(AGC_LAXNL_JAVA_OPTION)
            .configProperty("log4j.logger.Manager.AT", "INFO,console,logfile")
            .wvLaxNlClearJavaOption(WV_LAXNL_JAVA_OPTION);

        EmRole agcRole = agcBuilder.build();
        agcMachine.addRole(agcRole);


        // register MOM to AGC
        AGCRegisterRole agcRegister =
            new AGCRegisterRole.Builder("agcMOMRegister", tasResolver).agcHostName(emHost)
                .agcEmWvPort(new Integer(EMWEB_PORT).toString())
                .agcWvPort(new Integer(WV_PORT).toString())
                .hostName(tasResolver.getHostnameById(EM_MOM_ROLE_ID))
                .emWvPort(new Integer(EMWEB_PORT).toString())
                .wvHostName(tasResolver.getHostnameById(EM_MOM_ROLE_ID))
                .wvPort(new Integer(WV_PORT).toString())
                .startCommandContext(momRole.getEmRunCommandFlowContext())
                .stopCommandContext(momRole.getEmStopCommandFlowContext()).build();

        agcRegister.after(momRole);
        agcRegister.after(agcRole);
        agcMachine.addRole(agcRegister);

        testbed.addMachine(agcMachine);
        */
        FldTestbedProvider FLDControllerProvider = new FldControllerLoadProvider(null);
        testbed.addMachines(FLDControllerProvider.initMachines());
        FLDControllerProvider.initTestbed(testbed, tasResolver);
        
//        FldTestbedProvider seleniumLoadProvider = new FLDSeleniumATTestbedProvider(null,null);
//        testbed.addMachines(seleniumLoadProvider.initMachines());
//        seleniumLoadProvider.initTestbed(testbed, tasResolver);
        
        return testbed;
    }
    
    
    private TestbedMachine em(String machineId) {
         /*em = new TestbedMachine.Builder(machineId)
            .platform(Platform.WINDOWS)
            .templateId("w64")
            .bitness(Bitness.b64)
            .build();*/
        TestbedMachine em = new TestbedMachine.LinuxBuilder(machineId)
            .platform(Platform.CENTOS)
            .templateId("co65")
            .bitness(Bitness.b64)
            .build();
        
        return em;
    }

    private EmRole momRole(String machineId, ITasResolver tasResolver, TIMRole timRole, List<EmRole> collectors) {
        Collection<String> features = new HashSet<>(Arrays.asList("Enterprise Manager", "WebView", "Database", "ProbeBuilder", "EPA"));
        
        //Builder builder = new EmRole.Builder(machineId, tasResolver)
        //    .version("99.99.landing-SNAPSHOT")
        //    .emClusterRole(EmRoleEnum.MANAGER);
        Builder builder = new EmRole.LinuxBuilder(machineId, tasResolver)
            .version(EM_VERSION)
            .silentInstallChosenFeatures(features)
            .dbuser(FLDMainClusterTestbed.DB_USERNAME)
            .dbpassword(FLDMainClusterTestbed.DB_PASSWORD)
            .dbname(DB_NAME)
            .dbport(DB_PORT)
            .emWebPort(EMWEB_PORT)
            .wvPort(WV_PORT)
            .dbAdminPassword(DB_ADMIN_PASSWORD)
            .dbAdminUser(DB_ADMIN_USER)
            .emLaxNlClearJavaOption(MOM_LAXNL_JAVA_OPTION);
        
        if (collectors != null) {
            for (EmRole em: collectors) {
                builder.emCollector(em);
            }
            builder.emClusterRole(EmRoleEnum.MANAGER);
        } 
        
        if (timRole != null) {
            builder.tim(timRole);
        }
        
        EmRole em = builder.build();
        return em;
    }
    
    private EmRole emRole(String machineId, ITasResolver tasResolver) {
        Collection<String> features = new HashSet<>(Arrays.asList("Enterprise Manager", "ProbeBuilder", "EPA"));
        
        Builder builder = new EmRole.LinuxBuilder(machineId, tasResolver)
            .version(EM_VERSION)
            .emClusterRole(EmRoleEnum.COLLECTOR);
        
        String momHostname = tasResolver.getHostnameById(EM_MOM_ROLE_ID);
        if (momHostname != null) {
            builder.dbhost(momHostname)
                .silentInstallChosenFeatures(features)
                .dbuser(FLDMainClusterTestbed.DB_USERNAME)
                .dbpassword(FLDMainClusterTestbed.DB_PASSWORD)
                .dbname(DB_NAME)
                .dbport(DB_PORT)
                .emWebPort(EMWEB_PORT)
                .wvPort(WV_PORT)
                .dbAdminPassword(DB_ADMIN_PASSWORD)
                .dbAdminUser(DB_ADMIN_USER)
                .emLaxNlClearJavaOption(COLL_LAXNL_JAVA_OPTION)
                .nostartWV();
        }
        
        EmRole em = builder.build();
        return em;
    }
}
