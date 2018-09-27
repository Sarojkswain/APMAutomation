package com.ca.apm.systemtest.fld.testbed.machines;

import com.ca.apm.systemtest.fld.artifact.thirdparty.PPWebLogicVersion;
import com.ca.apm.systemtest.fld.role.PPWLSStockTraderRole;
import com.ca.apm.tests.artifact.OracleTradeDbScriptVersion;
import com.ca.apm.tests.artifact.StocktraderTradeDbScriptVersion;
import com.ca.apm.tests.flow.oracleDb.OracleTradeDbScriptFlowContext;
import com.ca.apm.tests.role.OracleTradeDbScriptRole;
import com.ca.apm.tests.role.StocktraderTradeDbScriptRole;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.artifact.thirdParty.OracleDbVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.oracle.OracleDbRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.WebLogicRole;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import org.testng.ITest;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

/**
 * @Author rsssa02
 */
public class WLSTradeTestbed {

    private ITasResolver tasResolver;
    public static WebLogicRole wlsRole;
    protected String JAVA_ROLE_ID = "jvm_role";
    public static String WLS_ROLE_ID = "wlsstock_role";
    private static final String DEFAULT_DB_ORACLE_INSTALL_PATH = "c:/sw/performancetest/oracledb";
    private static final String DEFAULT_DB_ORACLE_HOME_PATH = DEFAULT_DB_ORACLE_INSTALL_PATH + "/product/11.1.0/db_1";
    public static final String DB_ROLE_ID = "oracledb_role";
    //protected String DB_ROLE_ID = "oracledb_role";
    public static final String STOCKTRADE_ROLE_ID = "stocktrade_role";
    public static final PPWebLogicVersion WLS_VERSION = PPWebLogicVersion.v1036x86w;

    public WLSTradeTestbed(ITasResolver tasResolver) {
        this.tasResolver = tasResolver;
    }

    public ITestbedMachine init(String machineID, String INSTALL_LOC) {
        TestbedMachine machine = new TestbedMachine.Builder(machineID).templateId(TEMPLATE_W64).build();

        JavaRole javaRole = new JavaRole.Builder(JAVA_ROLE_ID,tasResolver)
                .version(JavaBinary.WINDOWS_32BIT_JDK_17_0_25)
                .dir(INSTALL_LOC + "\\Java\\jdk1.7")
                .build();
        machine.addRole(javaRole);
        //EmptyRole
        wlsRole = new WebLogicRole.Builder(WLS_ROLE_ID, tasResolver)
                .version(WLS_VERSION.getArtifact())
                .customJvm(javaRole.getInstallDir())
                .installLocation(INSTALL_LOC + "\\wls")
                .installDir(INSTALL_LOC +"\\wls\\wlserver_10.3")
                .build();
        wlsRole.after(javaRole);
        machine.addRole(wlsRole);

        PPWLSStockTraderRole ppwlsStockTraderRole = new PPWLSStockTraderRole.Builder(STOCKTRADE_ROLE_ID,tasResolver)
                .javaRole(javaRole)
                .webLogicRole(wlsRole)
                .dbRole(DB_ROLE_ID)
                .serverPort("7001")
                .build();
        ppwlsStockTraderRole.after(javaRole,wlsRole);
        machine.addRole(ppwlsStockTraderRole);

        OracleDbRole oracleDbRole = new OracleDbRole.Builder(DB_ROLE_ID, tasResolver)
                .version(OracleDbVersion.Oracle11gR1w)
                .installPath(DEFAULT_DB_ORACLE_INSTALL_PATH)
                .homePath(DEFAULT_DB_ORACLE_HOME_PATH)
                .build();
        machine.addRole(oracleDbRole);

        OracleTradeDbScriptRole createOracleTradedbRole = new OracleTradeDbScriptRole.Builder("createDBRole", tasResolver)
                .runAsSysdba(true)
                .version(OracleTradeDbScriptVersion.VER_55)
                .plsqlExecutableLocation(DEFAULT_DB_ORACLE_HOME_PATH + "\\BIN\\sqlplus.exe")
                .build();
        createOracleTradedbRole.after(oracleDbRole);
        machine.addRole(createOracleTradedbRole);

        StocktraderTradeDbScriptRole stockTradeCreateDBScriptRole = new StocktraderTradeDbScriptRole.Builder("createDBRole2", tasResolver)
                .runAsSysdba(false)
                .plsqlExecutableLocation(DEFAULT_DB_ORACLE_HOME_PATH + "\\BIN\\sqlplus.exe")
                .version(StocktraderTradeDbScriptVersion.VER_55)
                .runAsUser(OracleTradeDbScriptFlowContext.DEFAULT_USER)
                .runAsPassword(OracleTradeDbScriptFlowContext.DEFAULT_PASSWORD)
                .build();

        stockTradeCreateDBScriptRole.after(oracleDbRole, createOracleTradedbRole);
        machine.addRole(stockTradeCreateDBScriptRole);

        return machine;
    }
}
