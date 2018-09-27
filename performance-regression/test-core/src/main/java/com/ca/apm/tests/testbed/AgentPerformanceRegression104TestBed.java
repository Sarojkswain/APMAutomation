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
package com.ca.apm.tests.testbed;

import com.ca.apm.tests.artifact.AgentTrussVersion;
import com.ca.apm.tests.artifact.NetAgentTrussVersion;
import com.ca.apm.tests.role.KonakartRole;
import com.ca.apm.tests.role.MsSqlDbRole;
import com.ca.apm.tests.role.NerdDinnerRole;
import com.ca.apm.tests.role.NetStockTraderRole;
import com.ca.apm.tests.role.StockTraderRole;
import com.ca.apm.tests.role.Trade6Role;
import com.ca.apm.tests.testbed.machines.AppServerIis104Machine;
import com.ca.apm.tests.testbed.machines.AppServerTomcat104Machine;
import com.ca.apm.tests.testbed.machines.AppServerWeblogic104Machine;
import com.ca.apm.tests.testbed.machines.AppServerWebsphere104Machine;
import com.ca.apm.tests.testbed.machines.DbMsSqlTradeDbMachine;
import com.ca.apm.tests.testbed.machines.DbOracleKonakartTradeDbMachine;
import com.ca.apm.tests.testbed.machines.DbOracleTradeDbMachine;
import com.ca.apm.tests.testbed.machines.Em104Machine;
import com.ca.apm.tests.testbed.machines.EmMachine;
import com.ca.apm.tests.testbed.machines.JMeterIis104Machine;
import com.ca.apm.tests.testbed.machines.JMeterTomcatMachine;
import com.ca.apm.tests.testbed.machines.JMeterWeblogicMachine;
import com.ca.apm.tests.testbed.machines.JMeterWebsphereMachine;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
@TestBedDefinition(cleanUpTestBed = AgentPerformanceRegressionTestBedCleaner.class)
public class AgentPerformanceRegression104TestBed extends AgentPerformanceRegression103TestBed {

    protected String AGENT_DLL_CURRENT_VERSION;
    protected String AGENT_DLL_PREV_VERSION;

    protected boolean PREDEPLOYED_JMETER_TOMCAT;
    protected boolean PREDEPLOYED_JMETER_WAS;
    protected boolean PREDEPLOYED_JMETER_WLS;
    protected boolean PREDEPLOYED_JMETER_IIS;

    protected boolean PREDEPLOYED_DB_TOMCAT;
    protected boolean PREDEPLOYED_DB_WAS;
    protected boolean PREDEPLOYED_DB_WLS;
    protected boolean PREDEPLOYED_DB_IIS;

    public AgentPerformanceRegression104TestBed() {
        DEPLOY_EM = true;

        DEPLOY_TOMCAT = true;
        DEPLOY_WAS = true;
        DEPLOY_WLS = true;
        DEPLOY_IIS = true;

        PREDEPLOYED_EM = false;

        PREDEPLOYED_TOMCAT = false;
        PREDEPLOYED_WAS = false;
        PREDEPLOYED_WLS = false;
        PREDEPLOYED_IIS = false;

        PREDEPLOYED_DB_TOMCAT = PREDEPLOYED_TOMCAT;
        PREDEPLOYED_DB_WAS = PREDEPLOYED_WAS;
        PREDEPLOYED_DB_WLS = PREDEPLOYED_WLS;
        PREDEPLOYED_DB_IIS = PREDEPLOYED_IIS;

        PREDEPLOYED_JMETER_TOMCAT = PREDEPLOYED_TOMCAT;
        PREDEPLOYED_JMETER_WAS = PREDEPLOYED_WAS;
        PREDEPLOYED_JMETER_WLS = PREDEPLOYED_WLS;
        PREDEPLOYED_JMETER_IIS = PREDEPLOYED_IIS;

        AGENT_CURRENT_VERSION = "10.5.2.10";
        AGENT_PREV_VERSION = "10.5.1.31";

        AGENT_DLL_CURRENT_VERSION = null;
        AGENT_DLL_PREV_VERSION = null;

        AGENT_USE_TRUSS_GA = true;

        AGENT_CURRENT_TRUSS_TOMCAT = AgentTrussVersion.ISCP_10_7_0_GA_TOMCAT_WIN;
        AGENT_CURRENT_TRUSS_WLS = AgentTrussVersion.ISCP_10_7_0_GA_WLS_WIN;
        AGENT_CURRENT_TRUSS_WAS = AgentTrussVersion.ISCP_10_7_0_GA_WAS_WIN;
        AGENT_CURRENT_TRUSS_IIS = NetAgentTrussVersion.NET_10_7_0_GA_x64;

        AGENT_PREV_TRUSS_TOMCAT = AgentTrussVersion.ISCP_10_5_2_GA_TOMCAT_WIN;
        AGENT_PREV_TRUSS_WLS = AgentTrussVersion.ISCP_10_5_2_GA_WLS_WIN;
        AGENT_PREV_TRUSS_WAS = AgentTrussVersion.ISCP_10_5_2_GA_WAS_WIN;
        AGENT_PREV_TRUSS_IIS = NetAgentTrussVersion.NET_10_5_2_GA_x64;
    }

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testBed = new Testbed("AgentPerformanceRegressionTestBed");

        //EM
        ITestbedMachine emMachine = (new Em104Machine(EM_MACHINE_ID, tasResolver, DEPLOY_EM, PREDEPLOYED_EM)).init();
        testBed.addMachine(emMachine);
        IRole emRole = emMachine.getRoleById(EM_MACHINE_ID + EmMachine.EM_ROLE_ID);

        if (DEPLOY_TOMCAT) {
            deployTomcat(tasResolver, testBed, emRole);
        }

        if (DEPLOY_WAS) {
            deployWas(tasResolver, testBed, emRole);
        }

        if (DEPLOY_WLS) {
            deployWls(tasResolver, testBed, emRole);
        }

        if (DEPLOY_IIS) {
            deployIis(tasResolver, testBed, emRole);
        }

        return testBed;
    }

    @Override
    protected void deployIis(ITasResolver tasResolver, ITestbed testBed, IRole emRole) {
        //DB IIS
        DbMsSqlTradeDbMachine db = new DbMsSqlTradeDbMachine(DB_MSSQL_IIS_MACHINE_ID, tasResolver, DEFAULT_COPY_RESULTS_PASSWORD);
        ITestbedMachine dbMachine = PREDEPLOYED_DB_IIS ? db.initPredeployed() : db.init();
        testBed.addMachine(dbMachine);
        MsSqlDbRole dbRole = (MsSqlDbRole) dbMachine.getRoleById(DB_MSSQL_IIS_MACHINE_ID + DbMsSqlTradeDbMachine.MSSQL_DB_ROLE_ID);
        IRole dbScriptRole = dbMachine.getRoleById(DB_MSSQL_IIS_MACHINE_ID + DbMsSqlTradeDbMachine.MSSQL_SCRIPT_ROLE_ID);

        // AS IIS
        AppServerIis104Machine as = AGENT_USE_TRUSS_GA ?
                new AppServerIis104Machine(AS_IIS_MACHINE_ID, tasResolver, AGENT_CURRENT_TRUSS_IIS, AGENT_PREV_TRUSS_IIS, DEFAULT_COPY_RESULTS_PASSWORD) :
                new AppServerIis104Machine(AS_IIS_MACHINE_ID, tasResolver, AGENT_CURRENT_VERSION, AGENT_PREV_VERSION, AGENT_DLL_CURRENT_VERSION, AGENT_DLL_PREV_VERSION, DEFAULT_COPY_RESULTS_PASSWORD);
        ITestbedMachine appServerMachine = PREDEPLOYED_IIS ? as.initPredeployed(dbRole, dbScriptRole, emRole) : as.init(dbRole, dbScriptRole, emRole);
        testBed.addMachine(appServerMachine);
        NetStockTraderRole appRole = (NetStockTraderRole) appServerMachine.getRoleById(AS_IIS_MACHINE_ID + AppServerIis104Machine.NET_STOCKTRADER_ROLE_ID);
        NerdDinnerRole nerdDinner4Role = (NerdDinnerRole) appServerMachine.getRoleById(AS_IIS_MACHINE_ID + AppServerIis104Machine.NERD_DINNER_4_ROLE_ID);

        // JMETER IIS
        JMeterIis104Machine jmeter = new JMeterIis104Machine(JMETER_IIS_MACHINE_ID, tasResolver, DEFAULT_COPY_RESULTS_PASSWORD);
        ITestbedMachine jmeterMachine = PREDEPLOYED_JMETER_IIS ? jmeter.initPredeployed(appRole, nerdDinner4Role, emRole) : jmeter.init(appRole, nerdDinner4Role, emRole);
        testBed.addMachine(jmeterMachine);
    }

    @Override
    protected void deployWls(ITasResolver tasResolver, ITestbed testBed, IRole emRole) {
        // DB WEBLOGIC
        DbOracleTradeDbMachine db = new DbOracleTradeDbMachine(DB_ORACLE_WLS_MACHINE_ID, tasResolver, DbOracleTradeDbMachine.DEFAULT_TEMPLATE_WLS);
        ITestbedMachine dbMachine = PREDEPLOYED_DB_WLS ? db.initPredeployed() : db.init();
        testBed.addMachine(dbMachine);
        IRole dbScriptRole = dbMachine.getRoleById(DB_ORACLE_WLS_MACHINE_ID + DbOracleTradeDbMachine.STOCKTRADER_SCRIPT_ROLE_ID);

        // AS WEBLOGIC
        AppServerWeblogic104Machine as = AGENT_USE_TRUSS_GA ?
                new AppServerWeblogic104Machine(AS_WLS_MACHINE_ID, tasResolver, AGENT_CURRENT_TRUSS_WLS, AGENT_PREV_TRUSS_WLS, DEFAULT_COPY_RESULTS_PASSWORD) :
                new AppServerWeblogic104Machine(AS_WLS_MACHINE_ID, tasResolver, AGENT_CURRENT_VERSION, AGENT_PREV_VERSION, DEFAULT_COPY_RESULTS_PASSWORD);
        ITestbedMachine appServerMachine = PREDEPLOYED_WLS ? as.initPredeployed(dbScriptRole, emRole) : as.init(dbScriptRole, emRole);
        testBed.addMachine(appServerMachine);
        StockTraderRole appRole = (StockTraderRole) appServerMachine.getRoleById(AS_WLS_MACHINE_ID + AppServerWeblogic104Machine.STOCKTRADER_ROLE_ID);

        // JMETER WEBLOGIC
        JMeterWeblogicMachine jmeter = new JMeterWeblogicMachine(JMETER_WLS_MACHINE_ID, tasResolver, DEFAULT_COPY_RESULTS_PASSWORD);
        ITestbedMachine jmeterMachine = PREDEPLOYED_JMETER_WLS ? jmeter.initPredeployed(appRole, emRole) : jmeter.init(appRole, emRole);
        testBed.addMachine(jmeterMachine);
    }

    @Override
    protected void deployWas(ITasResolver tasResolver, ITestbed testBed, IRole emRole) {
        // DB WEBSPHERE
        DbOracleTradeDbMachine db = new DbOracleTradeDbMachine(DB_ORACLE_WAS_MACHINE_ID, tasResolver, DbOracleTradeDbMachine.DEFAULT_TEMPLATE_WAS);
        ITestbedMachine dbMachine = PREDEPLOYED_DB_WAS ? db.initPredeployed() : db.init();
        testBed.addMachine(dbMachine);
        IRole dbScriptRole = dbMachine.getRoleById(DB_ORACLE_WAS_MACHINE_ID + DbOracleTradeDbMachine.STOCKTRADER_SCRIPT_ROLE_ID);

        // AS WEBSPHERE
        AppServerWebsphere104Machine as = AGENT_USE_TRUSS_GA ?
                new AppServerWebsphere104Machine(AS_WAS_MACHINE_ID, tasResolver, AGENT_CURRENT_TRUSS_WAS, AGENT_PREV_TRUSS_WAS, DEFAULT_COPY_RESULTS_PASSWORD) :
                new AppServerWebsphere104Machine(AS_WAS_MACHINE_ID, tasResolver, AGENT_CURRENT_VERSION, AGENT_PREV_VERSION, DEFAULT_COPY_RESULTS_PASSWORD);
        ITestbedMachine appServerMachine = PREDEPLOYED_WAS ? as.initPredeployed(dbScriptRole, emRole) : as.init(dbScriptRole, emRole);
        testBed.addMachine(appServerMachine);
        Trade6Role appRole = (Trade6Role) appServerMachine.getRoleById(AS_WAS_MACHINE_ID + AppServerWebsphere104Machine.TRADE6_ROLE_ID);

        // JMETER WEBSPHERE
        JMeterWebsphereMachine jmeter = new JMeterWebsphereMachine(JMETER_WAS_MACHINE_ID, tasResolver, DEFAULT_COPY_RESULTS_PASSWORD);
        ITestbedMachine jmeterMachine = PREDEPLOYED_JMETER_WAS ? jmeter.initPredeployed(appRole, emRole) : jmeter.init(appRole, emRole);
        testBed.addMachine(jmeterMachine);
    }

    @Override
    protected void deployTomcat(ITasResolver tasResolver, ITestbed testBed, IRole emRole) {
        //DB TOMCAT
        DbOracleKonakartTradeDbMachine db = new DbOracleKonakartTradeDbMachine(DB_ORACLE_TOMCAT_MACHINE_ID, tasResolver, DbOracleKonakartTradeDbMachine.DEFAULT_TEMPLATE_TOMCAT);
        ITestbedMachine dbMachine = PREDEPLOYED_DB_TOMCAT ? db.initPredeployed() : db.init();
        testBed.addMachine(dbMachine);
        IRole dbScriptRole = dbMachine.getRoleById(DB_ORACLE_TOMCAT_MACHINE_ID + DbOracleKonakartTradeDbMachine.KONAKART_SCRIPT_ROLE_ID);

        // AS TOMCAT
        AppServerTomcat104Machine as = AGENT_USE_TRUSS_GA ?
                new AppServerTomcat104Machine(AS_TOMCAT_MACHINE_ID, tasResolver, AGENT_CURRENT_TRUSS_TOMCAT, AGENT_PREV_TRUSS_TOMCAT, DEFAULT_COPY_RESULTS_PASSWORD) :
                new AppServerTomcat104Machine(AS_TOMCAT_MACHINE_ID, tasResolver, AGENT_CURRENT_VERSION, AGENT_PREV_VERSION, DEFAULT_COPY_RESULTS_PASSWORD);
        ITestbedMachine appServerMachine = PREDEPLOYED_TOMCAT ? as.initPredeployed(dbScriptRole, emRole) : as.init(dbScriptRole, emRole);
        testBed.addMachine(appServerMachine);
        KonakartRole appRole = (KonakartRole) appServerMachine.getRoleById(AS_TOMCAT_MACHINE_ID + AppServerTomcat104Machine.KONAKART_ROLE_ID);

        // JMETER TOMCAT
        JMeterTomcatMachine jmeter = new JMeterTomcatMachine(JMETER_TOMCAT_MACHINE_ID, tasResolver, DEFAULT_COPY_RESULTS_PASSWORD);
        ITestbedMachine jmeterMachine = PREDEPLOYED_JMETER_TOMCAT ? jmeter.initPredeployed(appRole, emRole) : jmeter.init(appRole, emRole);
        testBed.addMachine(jmeterMachine);
    }

}
