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
package com.ca.apm.tests.testbed.machines;

import com.ca.apm.tests.artifact.OracleTradeDbScriptVersion;
import com.ca.apm.tests.artifact.StocktraderTradeDbScriptVersion;
import com.ca.apm.tests.flow.oracleDb.OracleTradeDbScriptFlowContext;
import com.ca.apm.tests.role.OracleTradeDbScriptRole;
import com.ca.apm.tests.role.PerfOracleDbRole;
import com.ca.apm.tests.role.StocktraderTradeDbScriptRole;
import com.ca.tas.artifact.thirdParty.OracleDbVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.TestbedMachine;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class DbOracleTradeDbMachine {

    public static final String DEFAULT_TEMPLATE_WLS = "AgentPerf_DBWEBLOGIC";
    public static final String DEFAULT_TEMPLATE_WAS = "AgentPerf_DBWEBSPHERE";
    public static final String DEFAULT_TEMPLATE_TOMCAT = "AgentPerf_DBTOMCAT";

    public static final String DEFAULT_DB_ORACLE_INSTALL_PATH = "C:/sw/oracledb";
    public static final String DEFAULT_DB_ORACLE_HOME_PATH = DEFAULT_DB_ORACLE_INSTALL_PATH + "/product/11.1.0/db_1";


    public static final String DB_ROLE_ID = "_dbRoleId";
    public static final String STOCKTRADER_SCRIPT_ROLE_ID = "_stocktraderScriptRoleId";

    protected final String machineId;
    protected final ITasResolver tasResolver;
    protected final String template;

    protected boolean undeploy;
    protected boolean predeployed;

    public DbOracleTradeDbMachine(String machineId, ITasResolver tasResolver, String template) {
        this.machineId = machineId;
        this.tasResolver = tasResolver;
        this.template = template;
    }

    public TestbedMachine undeploy() {
        this.undeploy = true;
        return init();
    }

    public TestbedMachine initPredeployed() {
        this.predeployed = true;
        return init();
    }

    public TestbedMachine init() {
        TestbedMachine machine = new TestbedMachine.Builder(machineId).templateId(template).bitness(Bitness.b64).build();

        ///////////////////////////////////////////
        // DEPLOY DB
        ///////////////////////////////////////////

        PerfOracleDbRole dbRole = new PerfOracleDbRole.Builder(machineId + DB_ROLE_ID, tasResolver)
                .version(OracleDbVersion.Oracle11gR1w)
                .installPath(DEFAULT_DB_ORACLE_INSTALL_PATH)
                .homePath(DEFAULT_DB_ORACLE_HOME_PATH)
                .homeName("db_1")
                .undeployOnly(undeploy)
                .predeployed(predeployed)
                .build();

        machine.addRole(dbRole);

        if (!undeploy) {

            ///////////////////////////////////////////
            // CONFIGURE DB
            ///////////////////////////////////////////

            OracleTradeDbScriptRole oracleScriptRole = new OracleTradeDbScriptRole
                    .Builder(machineId + "_oracleScriptRoleId", tasResolver)
                    .version(OracleTradeDbScriptVersion.VER_55)
                    .runAsSysdba(true)
                    .plsqlExecutableLocation(DEFAULT_DB_ORACLE_HOME_PATH + "\\BIN\\sqlplus.exe")
                    .predeployed(predeployed)
                    .build();
            oracleScriptRole.after(dbRole);
            machine.addRole(oracleScriptRole);

            ///////////////////////////////////////////
            // CREATE TABLES
            ///////////////////////////////////////////

            IRole stocktraderScriptRole = createTables();

            stocktraderScriptRole.after(dbRole, oracleScriptRole);

            machine.addRole(stocktraderScriptRole);
        }

        return machine;
    }

    protected IRole createTables() {

        StocktraderTradeDbScriptRole stocktraderScriptRole = new StocktraderTradeDbScriptRole
                .Builder(machineId + STOCKTRADER_SCRIPT_ROLE_ID, tasResolver)
                .version(StocktraderTradeDbScriptVersion.VER_55)
                .runAsUser(OracleTradeDbScriptFlowContext.DEFAULT_USER)
                .runAsPassword(OracleTradeDbScriptFlowContext.DEFAULT_PASSWORD).runAsSysdba(false)
                .plsqlExecutableLocation(DEFAULT_DB_ORACLE_HOME_PATH + "\\BIN\\sqlplus.exe")
                .predeployed(predeployed)
                .build();

        return stocktraderScriptRole;

    }

}
