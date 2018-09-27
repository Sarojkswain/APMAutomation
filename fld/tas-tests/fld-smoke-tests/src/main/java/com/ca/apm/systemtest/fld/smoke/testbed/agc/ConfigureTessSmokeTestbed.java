/**
 * 
 */
package com.ca.apm.systemtest.fld.smoke.testbed.agc;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import com.ca.apm.automation.action.flow.em.EmFeature;
import com.ca.apm.automation.action.flow.em.ImportDomainConfigFlowContext;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext.EmRoleEnum;
import com.ca.apm.systemtest.fld.artifact.thirdparty.FldDomainConfigArtifact;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FLDMainClusterTestbed;
import com.ca.apm.systemtest.fld.testbed.loads.ConfigureTessLoadProvider;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.ImportDomainConfigRole;
import com.ca.tas.role.TIMRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author keyja01
 *
 */
@TestBedDefinition
public class ConfigureTessSmokeTestbed implements ITestbedFactory, FLDConstants, FLDLoadConstants {
    private static final String INSTALL_DIR = "/home/sw/em/Introscope";
    private static final String INSTALL_TG_DIR = "/home/sw/em/Installer";
    private static final String DATABASE_DIR = "/data/em/database";
    private static final String INSTALL_TIM_DIR = "/opt";

    /* (non-Javadoc)
     * @see com.ca.tas.testbed.ITestbedFactory#create(com.ca.tas.resolver.ITasResolver)
     */
    @Override
    public ITestbed create(ITasResolver tasResolver) {
        Testbed tb = new Testbed(ConfigureTessSmokeTestbed.class.getSimpleName());
        
        TestbedMachine fldControllerMachine = new TestbedMachine.Builder(FLD_CONTROLLER_MACHINE_ID)
            .templateId("w64").build();
        tb.addMachine(fldControllerMachine);
        
        initDummyAppServerRoles(fldControllerMachine, tasResolver);
        
        IRole dbRole = db(tb, tasResolver, DATABASE_MACHINE_ID, EM_DATABASE_ROLE_ID);
        String dbHost = tasResolver.getHostnameById(EM_DATABASE_ROLE_ID);
        
        EmRole em01Role = em(tb, dbHost, tasResolver, COLL01_MACHINE_ID, EM_COLL01_ROLE_ID);
        EmRole em02Role = em(tb, dbHost, tasResolver, COLL02_MACHINE_ID, EM_COLL02_ROLE_ID);
        EmRole momRole = mom(tb, dbHost, tasResolver, MOM_MACHINE_ID, EM_MOM_ROLE_ID, em01Role, em02Role);
        momRole.before(em01Role, em02Role);
        
        TIMRole tim01 = tim(tb, tasResolver, TIM01_MACHINE_ID, TIM01_ROLE_ID);
        TIMRole tim02 = tim(tb, tasResolver, TIM02_MACHINE_ID, TIM02_ROLE_ID);
        TIMRole tim03 = tim(tb, tasResolver, TIM03_MACHINE_ID, TIM03_ROLE_ID);
        TIMRole tim04 = tim(tb, tasResolver, TIM04_MACHINE_ID, TIM04_ROLE_ID);
        TIMRole tim05 = tim(tb, tasResolver, TIM05_MACHINE_ID, TIM05_ROLE_ID);
        
        dbRole.before(momRole, em01Role, em02Role);
        
        ConfigureTessLoadProvider configureTess = new ConfigureTessLoadProvider();
        tb.addMachines(configureTess.initMachines());
        
        configureTess.initTestbed(tb, tasResolver);
        
        return tb;
    }
    
    private void initDummyAppServerRoles(TestbedMachine m, ITasResolver tasResolver) {
        String[] roleIds = new String[] {
            WLS_01_SERVER_01_ROLE_ID, WLS_01_SERVER_02_ROLE_ID, WLS_02_SERVER_01_ROLE_ID,
            WLS_02_SERVER_02_ROLE_ID, TOMCAT_6_ROLE_ID, TOMCAT_7_ROLE_ID, TOMCAT_9080_ROLE_ID,
            TOMCAT_9081_ROLE_ID, JBOSS6_ROLE_ID, JBOSS7_ROLE_ID, TC_ROLE_ID,
            WEBSPHERE_01_ROLE_ID, DOTNET_MACHINE1+"_"+DOTNET_AGENT_ROLE_ID,
            DOTNET_MACHINE2+"_"+DOTNET_AGENT_ROLE_ID
        };
        for (String roleId: roleIds) {
            RunCommandFlowContext ctx = new RunCommandFlowContext.Builder("cmd.exe")
                .doNotPrependWorkingDirectory().args(Arrays.asList("/c", "dir"))
                .terminateOnMatch("").allowPositiveExitStatus()
                .build();
            UniversalRole ur = new UniversalRole.Builder(roleId, tasResolver)
                .syncCommand(ctx)
                .build();
            m.addRole(ur);
        }
    }

    
    private TIMRole tim(Testbed tb, ITasResolver tasResolver, String machineId, String roleId) {
        TestbedMachine m = new TestbedMachine.LinuxBuilder(machineId).templateId("co65_tim").build();
        
        TIMRole timRole = new TIMRole.Builder(roleId, tasResolver)
            .installDir(INSTALL_TIM_DIR)
            .build();
        
        m.addRole(timRole);
        tb.addMachine(m);
        
        return timRole;
    }
    

    private IRole db(Testbed tb, ITasResolver tasResolver, String dbMachineId, String dbRoleId) {
        TestbedMachine m = new TestbedMachine.LinuxBuilder(dbMachineId).templateId("co65").build();
        
        EmRole emRole = new EmRole.LinuxBuilder(dbRoleId, tasResolver)
            .silentInstallChosenFeatures(EnumSet.of(EmFeature.DATABASE))
            .emClusterRole(EmRoleEnum.MANAGER)
            .databaseDir(DATABASE_DIR)
            .installDir(INSTALL_DIR)
            .installerTgDir(INSTALL_TG_DIR)
            .nostartEM().nostartWV()
            .dbuser(FLDMainClusterTestbed.DB_USERNAME)
            .dbpassword(FLDMainClusterTestbed.DB_PASSWORD)
            .dbAdminUser(FLDMainClusterTestbed.DB_ADMIN_USERNAME)
            .dbAdminPassword(FLDMainClusterTestbed.DB_ADMIN_PASSWORD)
            .build();
        ITasArtifact domainConfigArtifact = new FldDomainConfigArtifact().createArtifact("10.3");

        IRole domainConfigImportLinuxRole
            = new ImportDomainConfigRole.Builder(DB_DOMAIN_CONFIG_IMPORT_ROLE_ID, tasResolver)
            .dbHost(tasResolver.getHostnameById(EM_DATABASE_ROLE_ID))
            .dbName("cemdb")
            .dbPort(5432)
            .dbType(ImportDomainConfigFlowContext.DbType.PostgreSql)
            .dbUser(FLDMainClusterTestbed.DB_USERNAME)
            .dbPassword(FLDMainClusterTestbed.DB_PASSWORD)
            .emDir(INSTALL_DIR)
            .dbServiceUser(FLDMainClusterTestbed.DB_ADMIN_USERNAME)
            .dbServicePwd(FLDMainClusterTestbed.DB_ADMIN_PASSWORD)
            .dbInstallDir(DATABASE_DIR)
            .targetRelease("99.99.0.0")
            .importFile(domainConfigArtifact)
            .build();
        
        emRole.before(domainConfigImportLinuxRole);
        
        m.addRole(emRole, domainConfigImportLinuxRole);
        tb.addMachine(m);
        
        return emRole;
    }

    private EmRole em(Testbed tb, String dbHost, ITasResolver tasResolver, String emMachineId, String emRoleId) {
        TestbedMachine m = new TestbedMachine.LinuxBuilder(emMachineId).templateId("co65").build();
        
        EmRole emRole = new EmRole.LinuxBuilder(emRoleId, tasResolver)
            .silentInstallChosenFeatures(EnumSet.of(EmFeature.ENTERPRISE_MANAGER))
            .emClusterRole(EmRoleEnum.COLLECTOR)
            .dbhost(dbHost)
            .dbuser(FLDMainClusterTestbed.DB_USERNAME)
            .dbpassword(FLDMainClusterTestbed.DB_PASSWORD)
            .dbAdminUser(FLDMainClusterTestbed.DB_ADMIN_USERNAME)
            .dbAdminPassword(FLDMainClusterTestbed.DB_ADMIN_PASSWORD)
            .databaseDir(DATABASE_DIR)
            .installDir(INSTALL_DIR)
            .installerTgDir(INSTALL_TG_DIR)
            .nostartWV().nostartEM()
            .build();
        m.addRole(emRole);
        
        tb.addMachine(m);
        
        return emRole;
    }

    private EmRole mom(Testbed tb, String dbHost, ITasResolver tasResolver, String momMachineId, String emMomRoleId, 
                     EmRole em01, EmRole em02) {
        TestbedMachine m = new TestbedMachine.LinuxBuilder(momMachineId).templateId("co65").build();
        
        EmRole emRole = new EmRole.LinuxBuilder(emMomRoleId, tasResolver)
            .silentInstallChosenFeatures(EnumSet.of(EmFeature.ENTERPRISE_MANAGER))
            .emClusterRole(EmRoleEnum.MANAGER)
            .dbhost(dbHost)
            .dbuser(FLDMainClusterTestbed.DB_USERNAME)
            .dbpassword(FLDMainClusterTestbed.DB_PASSWORD)
            .dbAdminUser(FLDMainClusterTestbed.DB_ADMIN_USERNAME)
            .dbAdminPassword(FLDMainClusterTestbed.DB_ADMIN_PASSWORD)
            .databaseDir(DATABASE_DIR)
            .installDir(INSTALL_DIR)
            .installerTgDir(INSTALL_TG_DIR)
            .nostartWV().nostartEM()
            .emCollector(em01, em02)
            .build();
        m.addRole(emRole);
        
        tb.addMachine(m);
        
        return emRole;
    }
}
