/*
 * Copyright (c) 2014 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and proprietary and shall not
 * be duplicated, used, disclosed or disseminated in any way except as authorized by the applicable
 * license agreement, without the express written permission of CA. All authorized reproductions
 * must be marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT PERMITTED BY APPLICABLE
 * LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF ANY KIND, INCLUDING WITHOUT LIMITATION, ANY
 * IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR INDIRECT, FROM THE
 * USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST PROFITS, BUSINESS INTERRUPTION,
 * GOODWILL, OR LOST DATA, EVEN IF CA IS EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

/*
 * Copyright (c) 2014 CA. All rights reserved.
 *
 * This software and all information contained therein is confidential and proprietary and shall not
 * be duplicated, used, disclosed or disseminated in any way except as authorized by the applicable
 * license agreement, without the express written permission of CA. All authorized reproductions
 * must be marked with this language.
 *
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT PERMITTED BY APPLICABLE
 * LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF ANY KIND, INCLUDING WITHOUT LIMITATION, ANY
 * IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR INDIRECT, FROM THE
 * USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST PROFITS, BUSINESS INTERRUPTION,
 * GOODWILL, OR LOST DATA, EVEN IF CA IS EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.apm.tests.testbed;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.tests.artifact.StockTraderVersion;
import com.ca.apm.tests.artifact.WeblogicVersion;
import com.ca.apm.tests.role.PerfJavaRole;
import com.ca.apm.tests.role.StockTraderRole;
import com.ca.apm.tests.role.Weblogic103Role;
import com.ca.apm.tests.testbed.machines.DbOracleTradeDbMachine;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.testbed.*;
import com.ca.tas.tests.annotations.TestBedDefinition;

import java.util.HashMap;
import java.util.Map;

import static com.ca.apm.tests.testbed.AgentPerformanceRegression103TestBed.DB_ORACLE_WLS_MACHINE_ID;
import static com.ca.apm.tests.testbed.machines.AppServerWeblogic104Machine.DEFAULT_WLS12_BEA_HOME;
import static com.ca.apm.tests.testbed.machines.AppServerWeblogic104Machine.DEFAULT_WLS12_INSTALL_DIR;
import static com.ca.apm.tests.testbed.machines.AppServerWeblogicMachine.STOCKTRADER_ROLE_ID;

/**
 * SamplePerfTestBed class
 * <p/>
 * TestBed description
 */
@TestBedDefinition
public class SamplePerformTestBed implements ITestbedFactory {

    public static final String MACHINE_ID = "asWlsMachine";
    public static final String DEFAULT_TEMPLATE = "AgentPerf_ASWEBLOGIC";

    public static final String WEBLOGIC12_ROLE_ID = "_wls12RoleId";

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        ITestbed testbed = new Testbed("SamplePerfTestBed");

        // DB WEBLOGIC
        DbOracleTradeDbMachine db = new DbOracleTradeDbMachine(DB_ORACLE_WLS_MACHINE_ID, tasResolver, DbOracleTradeDbMachine.DEFAULT_TEMPLATE_WLS);
        ITestbedMachine dbMachine = db.initPredeployed();
        testbed.addMachine(dbMachine);
        IRole dbScriptRole = dbMachine.getRoleById(DB_ORACLE_WLS_MACHINE_ID + DbOracleTradeDbMachine.STOCKTRADER_SCRIPT_ROLE_ID);

        TestbedMachine machine = new TestbedMachine.Builder(MACHINE_ID).templateId(DEFAULT_TEMPLATE).build();
        testbed.addMachine(machine);

        JavaRole java8Role = new PerfJavaRole.Builder(MACHINE_ID + "_java8RoleId", tasResolver)
                .version(JavaBinary.WINDOWS_64BIT_JDK_18)
                .dir("c:/sw/java8")
                .predeployed(true)
                .build();
        machine.addRole(java8Role);

        Weblogic103Role wls12Role = new Weblogic103Role.Builder(MACHINE_ID + WEBLOGIC12_ROLE_ID, tasResolver)
                .version(WeblogicVersion.v1213generic)
                .customJvm(java8Role.getInstallDir())
                .sourcesLocation("c:\\sw\\weblogic12_sources")
                // BEA HOME (parent dir)
                .beaHome(DEFAULT_WLS12_BEA_HOME)
                // WLS HOME (Install dir)
                .installDir(DEFAULT_WLS12_INSTALL_DIR)
                .undeployOnly(false)
                .predeployed(true)
                .build();
        wls12Role.after(java8Role);
        machine.addRole(wls12Role);

        ////////////////////////////
        // START APP SERVER
        ////////////////////////////

        String startWls = DEFAULT_WLS12_INSTALL_DIR + "/samples/domains/wl_server/bin/startWebLogic.cmd";

        Map<String, String> wlsEnv = new HashMap<>();
        wlsEnv.put("USER_MEM_ARGS", "-XX:PermSize=512m -XX:MaxPermSize=512m");
        RunCommandFlowContext runWlsFlowContext = new RunCommandFlowContext.Builder(startWls).environment(wlsEnv)
                .terminateOnMatch("Server started in RUNNING mode").build();
        ExecutionRole wlsExecutionRole = new ExecutionRole.Builder(MACHINE_ID + "_wlsExecutionRoleId")
                .syncCommand(runWlsFlowContext).build();
        wlsExecutionRole.after(wls12Role);
        machine.addRole(wlsExecutionRole);

        ///////////////////////////////////////////
        // DEPLOY STOCKTRADER (AND LIBRARIES)
        ///////////////////////////////////////////

        StockTraderRole stockTraderRole = new StockTraderRole.Builder(MACHINE_ID + STOCKTRADER_ROLE_ID, tasResolver)
                .version(StockTraderVersion.VER_55)
                .weblogicRole(wls12Role)
                .weblogicUserName("weblogic")
                .weblogicUserPassword("weblogic1")
                .weblogicTargetServer("AdminServer")
                .dbRole(dbScriptRole)
                .predeployed(false)
                .build();

        stockTraderRole.after(dbScriptRole, java8Role, wls12Role, wlsExecutionRole);
        machine.addRole(stockTraderRole);

        ///////////////////////////////////////////
        // STOP APP SERVER
        ///////////////////////////////////////////

        String stopWls = DEFAULT_WLS12_INSTALL_DIR + "/samples/domains/wl_server/bin/stopWebLogic.cmd";

        RunCommandFlowContext stopWlsFlowContext = new RunCommandFlowContext.Builder(stopWls).build();
        ExecutionRole wlsStoppingRole = new ExecutionRole.Builder(MACHINE_ID + "_wlsStoppingRoleId")
                .syncCommand(stopWlsFlowContext).build();
        wlsStoppingRole.after(stockTraderRole);
        machine.addRole(wlsStoppingRole);

        return testbed;
    }
}
