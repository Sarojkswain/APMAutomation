package com.ca.apm.systemtest.fld.testbed.machines;

import com.ca.apm.systemtest.fld.role.WASDeployTrade6Role;
import com.ca.apm.tests.artifact.OjdbcVersion;
import com.ca.apm.tests.role.OjdbcRole;
import com.ca.apm.tests.testbed.machines.DbOracleTradeDbMachine;
import com.ca.tas.artifact.thirdParty.WebSphere8FixPacksVersion;
import com.ca.tas.artifact.thirdParty.WebSphere8JavaVersion;
import com.ca.tas.artifact.thirdParty.WebSphere8Version;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.webapp.WebSphere8Role;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

/**
 * @Author rsssa02
 */
public class WASTradeAppTestbed {
    public ITasResolver tasResolver;
    public static String WAS_85_ROLE_ID = "_wasRoleId";
    public static String OJDBC_ROLE_ID = "_ojdbcRoleId";
    public static String TRADE_APP_ROLE_ID = "_trade6Role";

    public static String WAS_INSTALL_DIR = "c:\\sw\\ibm\\was";
    public String machineId;

    public WASTradeAppTestbed(String machineId, ITasResolver tasResolver){
        this.tasResolver = tasResolver;
        this.machineId = machineId;
    }

    public TestbedMachine init(String orclRoleId) {
        TestbedMachine tradeMachine = new TestbedMachine.Builder(machineId).templateId(TEMPLATE_W64).build();
        ////////////////////////////
        // DEPLOY APP SERVER
        ////////////////////////////
        WebSphere8Role was85Role = new WebSphere8Role.Builder(machineId + WAS_85_ROLE_ID , tasResolver)
                .wasVersion(WebSphere8Version.v85base)
                .wasFixPackVersion(WebSphere8FixPacksVersion.v8557base)
                .wasJavaVersion(WebSphere8JavaVersion.v71310for8557x64)
                .build();
        WAS_INSTALL_DIR = was85Role.getInstallDir();
        tradeMachine.addRole(was85Role);

        ////////////////////////////
        // DEPLOY OJDBC
        ////////////////////////////

        OjdbcRole ojdbcRole = new OjdbcRole.Builder(machineId + OJDBC_ROLE_ID, tasResolver)
                .version(OjdbcVersion.VER_6).deploySourcesLocation(was85Role.getInstallDir() + "\\lib").build();
        ojdbcRole.after(was85Role);
        tradeMachine.addRole(ojdbcRole);

        ////////////////////////////
        // DEPLOY TRADE6
        ////////////////////////////
        WASDeployTrade6Role trade6Role = new WASDeployTrade6Role.Builder(machineId + TRADE_APP_ROLE_ID, tasResolver)
                .ojdbcRole(ojdbcRole)
                .wasHome(was85Role.getInstallDir())
                .wasRoleId(was85Role.getRoleId())
                .profileName("AppSrv01")
                .nodeName("Node01")
                .serverName("server1")
                .dbHost(tasResolver.getHostnameById(orclRoleId + DbOracleTradeDbMachine.DB_ROLE_ID))
                .dbPort("1521")
                .build();
        trade6Role.after(was85Role, ojdbcRole);
        tradeMachine.addRole(trade6Role);


        return tradeMachine;
    }
}
