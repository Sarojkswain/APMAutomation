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
import com.ca.apm.tests.flow.INetShareUser;
import com.ca.apm.tests.role.KonakartRole;
import com.ca.apm.tests.role.NetStockTraderRole;
import com.ca.apm.tests.role.StockTraderRole;
import com.ca.apm.tests.role.Trade6Role;
import com.ca.apm.tests.testbed.machines.AppServerIisMachine;
import com.ca.apm.tests.testbed.machines.AppServerTomcatMachine;
import com.ca.apm.tests.testbed.machines.AppServerWeblogicMachine;
import com.ca.apm.tests.testbed.machines.AppServerWebsphereMachine;
import com.ca.apm.tests.testbed.machines.DbMsSqlTradeDbMachine;
import com.ca.apm.tests.testbed.machines.DbOracleKonakartTradeDbMachine;
import com.ca.apm.tests.testbed.machines.DbOracleTradeDbMachine;
import com.ca.apm.tests.testbed.machines.EmMachine;
import com.ca.apm.tests.testbed.machines.JMeterIisMachine;
import com.ca.apm.tests.testbed.machines.JMeterTomcatMachine;
import com.ca.apm.tests.testbed.machines.JMeterWeblogicMachine;
import com.ca.apm.tests.testbed.machines.JMeterWebsphereMachine;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
@TestBedDefinition(cleanUpTestBed = AgentPerformanceRegressionTestBedCleaner.class)
public class AgentPerformanceRegression103TestBed implements ITestbedFactory, INetShareUser {

    protected boolean DEPLOY_EM;

    protected boolean DEPLOY_TOMCAT;
    protected boolean DEPLOY_WAS;
    protected boolean DEPLOY_WLS;
    protected boolean DEPLOY_IIS;

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String DEFAULT_TEMPLATE = TestbedMachine.TEMPLATE_W64;

    public static final String EM_MACHINE_ID = "emMachine";

    public static final String DB_ORACLE_TOMCAT_MACHINE_ID = "dbTomcatMachine";
    public static final String DB_ORACLE_WAS_MACHINE_ID = "dbWasMachine";
    public static final String DB_ORACLE_WLS_MACHINE_ID = "dbWlsMachine";
    public static final String DB_MSSQL_IIS_MACHINE_ID = "dbIisMachine";

    public static final String AS_TOMCAT_MACHINE_ID = "asTomcatMachine";
    public static final String AS_WAS_MACHINE_ID = "asWasMachine";
    public static final String AS_WLS_MACHINE_ID = "asWlsMachine";
    public static final String AS_IIS_MACHINE_ID = "asIisMachine";

    public static final String JMETER_TOMCAT_MACHINE_ID = "jmeterTomcatMachine";
    public static final String JMETER_WAS_MACHINE_ID = "jmeterWasMachine";
    public static final String JMETER_WLS_MACHINE_ID = "jmeterWlsMachine";
    public static final String JMETER_IIS_MACHINE_ID = "jmeterIisMachine";

    protected boolean PREDEPLOYED_EM;

    protected boolean PREDEPLOYED_TOMCAT;
    protected boolean PREDEPLOYED_WAS;
    protected boolean PREDEPLOYED_WLS;
    protected boolean PREDEPLOYED_IIS;

    // Artifactory
    protected String AGENT_CURRENT_VERSION;
    protected String AGENT_PREV_VERSION;

    protected boolean AGENT_USE_TRUSS_GA = true;

    // Truss
    protected AgentTrussVersion AGENT_CURRENT_TRUSS_TOMCAT;
    protected AgentTrussVersion AGENT_CURRENT_TRUSS_WLS;
    protected AgentTrussVersion AGENT_CURRENT_TRUSS_WAS;
    protected NetAgentTrussVersion AGENT_CURRENT_TRUSS_IIS;
    protected AgentTrussVersion AGENT_PREV_TRUSS_TOMCAT;
    protected AgentTrussVersion AGENT_PREV_TRUSS_WLS;
    protected AgentTrussVersion AGENT_PREV_TRUSS_WAS;
    protected NetAgentTrussVersion AGENT_PREV_TRUSS_IIS;

    public AgentPerformanceRegression103TestBed() {

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

        AGENT_CURRENT_VERSION = "10.3.0.15";
        AGENT_PREV_VERSION = "10.2.0.14";

        AGENT_USE_TRUSS_GA = true;

        AGENT_CURRENT_TRUSS_TOMCAT = AgentTrussVersion.ISCP_10_3_GA_TOMCAT_WIN;
        AGENT_CURRENT_TRUSS_WLS = AgentTrussVersion.ISCP_10_3_GA_WLS_WIN;
        AGENT_CURRENT_TRUSS_WAS = AgentTrussVersion.ISCP_10_3_GA_WAS_WIN;
        AGENT_CURRENT_TRUSS_IIS = NetAgentTrussVersion.NET_10_3_GA_x64;
        AGENT_PREV_TRUSS_TOMCAT = AgentTrussVersion.ISCP_10_2_GA_TOMCAT_WIN;
        AGENT_PREV_TRUSS_WLS = AgentTrussVersion.ISCP_10_2_GA_WLS_WIN;
        AGENT_PREV_TRUSS_WAS = AgentTrussVersion.ISCP_10_2_GA_WAS_WIN;
        AGENT_PREV_TRUSS_IIS = NetAgentTrussVersion.NET_10_2_GA_x64;
    }

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testBed = new Testbed("AgentPerformanceRegressionTestBed");

        //EM
        ITestbedMachine emMachine = (new EmMachine(EM_MACHINE_ID, tasResolver)).init();
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

    protected void deployIis(ITasResolver tasResolver, ITestbed testBed, IRole emRole) {
        //DB IIS
        DbMsSqlTradeDbMachine db = new DbMsSqlTradeDbMachine(DB_MSSQL_IIS_MACHINE_ID, tasResolver, DEFAULT_COPY_RESULTS_PASSWORD);
        ITestbedMachine dbMachine = PREDEPLOYED_IIS ? db.initPredeployed() : db.init();
        testBed.addMachine(dbMachine);
        IRole dbScriptRole = dbMachine.getRoleById(DB_MSSQL_IIS_MACHINE_ID + DbMsSqlTradeDbMachine.MSSQL_SCRIPT_ROLE_ID);

        // AS IIS
        AppServerIisMachine as = AGENT_USE_TRUSS_GA ?
                new AppServerIisMachine(AS_IIS_MACHINE_ID, tasResolver, AGENT_CURRENT_TRUSS_IIS, AGENT_PREV_TRUSS_IIS, DEFAULT_COPY_RESULTS_PASSWORD) :
                new AppServerIisMachine(AS_IIS_MACHINE_ID, tasResolver, AGENT_CURRENT_VERSION, AGENT_PREV_VERSION, DEFAULT_COPY_RESULTS_PASSWORD);
        ITestbedMachine appServerMachine = PREDEPLOYED_IIS ? as.initPredeployed(dbScriptRole, emRole) : as.init(dbScriptRole, emRole);
        testBed.addMachine(appServerMachine);
        NetStockTraderRole appRole = (NetStockTraderRole) appServerMachine.getRoleById(AS_IIS_MACHINE_ID + AppServerIisMachine.NET_STOCKTRADER_ROLE_ID);

        // JMETER IIS
        JMeterIisMachine jmeter = new JMeterIisMachine(JMETER_IIS_MACHINE_ID, tasResolver, DEFAULT_COPY_RESULTS_PASSWORD);
        ITestbedMachine jmeterMachine = PREDEPLOYED_IIS ? jmeter.initPredeployed(appRole, emRole) : jmeter.init(appRole, emRole);
        testBed.addMachine(jmeterMachine);
    }

    protected void deployWls(ITasResolver tasResolver, ITestbed testBed, IRole emRole) {
        // DB WEBLOGIC
        DbOracleTradeDbMachine db = new DbOracleTradeDbMachine(DB_ORACLE_WLS_MACHINE_ID, tasResolver, DbOracleTradeDbMachine.DEFAULT_TEMPLATE_WLS);
        ITestbedMachine dbMachine = PREDEPLOYED_WLS ? db.initPredeployed() : db.init();
        testBed.addMachine(dbMachine);
        IRole dbScriptRole = dbMachine.getRoleById(DB_ORACLE_WLS_MACHINE_ID + DbOracleTradeDbMachine.STOCKTRADER_SCRIPT_ROLE_ID);

        // AS WEBLOGIC
        AppServerWeblogicMachine as = AGENT_USE_TRUSS_GA ?
                new AppServerWeblogicMachine(AS_WLS_MACHINE_ID, tasResolver, AGENT_CURRENT_TRUSS_WLS, AGENT_PREV_TRUSS_WLS, DEFAULT_COPY_RESULTS_PASSWORD) :
                new AppServerWeblogicMachine(AS_WLS_MACHINE_ID, tasResolver, AGENT_CURRENT_VERSION, AGENT_PREV_VERSION, DEFAULT_COPY_RESULTS_PASSWORD);
        ITestbedMachine appServerMachine = PREDEPLOYED_WLS ? as.initPredeployed(dbScriptRole, emRole) : as.init(dbScriptRole, emRole);
        testBed.addMachine(appServerMachine);
        StockTraderRole appRole = (StockTraderRole) appServerMachine.getRoleById(AS_WLS_MACHINE_ID + AppServerWeblogicMachine.STOCKTRADER_ROLE_ID);

        // JMETER WEBLOGIC
        JMeterWeblogicMachine jmeter = new JMeterWeblogicMachine(JMETER_WLS_MACHINE_ID, tasResolver, DEFAULT_COPY_RESULTS_PASSWORD);
        ITestbedMachine jmeterMachine = PREDEPLOYED_WLS ? jmeter.initPredeployed(appRole, emRole) : jmeter.init(appRole, emRole);
        testBed.addMachine(jmeterMachine);
    }

    protected void deployWas(ITasResolver tasResolver, ITestbed testBed, IRole emRole) {
        // DB WEBSPHERE
        DbOracleTradeDbMachine db = new DbOracleTradeDbMachine(DB_ORACLE_WAS_MACHINE_ID, tasResolver, DbOracleTradeDbMachine.DEFAULT_TEMPLATE_WAS);
        ITestbedMachine dbMachine = PREDEPLOYED_WAS ? db.initPredeployed() : db.init();
        testBed.addMachine(dbMachine);
        IRole dbScriptRole = dbMachine.getRoleById(DB_ORACLE_WAS_MACHINE_ID + DbOracleTradeDbMachine.STOCKTRADER_SCRIPT_ROLE_ID);

        // AS WEBSPHERE
        AppServerWebsphereMachine as = AGENT_USE_TRUSS_GA ?
                new AppServerWebsphereMachine(AS_WAS_MACHINE_ID, tasResolver, AGENT_CURRENT_TRUSS_WAS, AGENT_PREV_TRUSS_WAS, DEFAULT_COPY_RESULTS_PASSWORD) :
                new AppServerWebsphereMachine(AS_WAS_MACHINE_ID, tasResolver, AGENT_CURRENT_VERSION, AGENT_PREV_VERSION, DEFAULT_COPY_RESULTS_PASSWORD);
        ITestbedMachine appServerMachine = PREDEPLOYED_WAS ? as.initPredeployed(dbScriptRole, emRole) : as.init(dbScriptRole, emRole);
        testBed.addMachine(appServerMachine);
        Trade6Role appRole = (Trade6Role) appServerMachine.getRoleById(AS_WAS_MACHINE_ID + AppServerWebsphereMachine.TRADE6_ROLE_ID);

        // JMETER WEBSPHERE
        JMeterWebsphereMachine jmeter = new JMeterWebsphereMachine(JMETER_WAS_MACHINE_ID, tasResolver, DEFAULT_COPY_RESULTS_PASSWORD);
        ITestbedMachine jmeterMachine = PREDEPLOYED_WAS ? jmeter.initPredeployed(appRole, emRole) : jmeter.init(appRole, emRole);
        testBed.addMachine(jmeterMachine);
    }

    protected void deployTomcat(ITasResolver tasResolver, ITestbed testBed, IRole emRole) {
        //DB TOMCAT
        DbOracleKonakartTradeDbMachine db = new DbOracleKonakartTradeDbMachine(DB_ORACLE_TOMCAT_MACHINE_ID, tasResolver, DbOracleKonakartTradeDbMachine.DEFAULT_TEMPLATE_TOMCAT);
        ITestbedMachine dbMachine = PREDEPLOYED_TOMCAT ? db.initPredeployed() : db.init();
        testBed.addMachine(dbMachine);
        IRole dbScriptRole = dbMachine.getRoleById(DB_ORACLE_TOMCAT_MACHINE_ID + DbOracleKonakartTradeDbMachine.KONAKART_SCRIPT_ROLE_ID);

        // AS TOMCAT
        AppServerTomcatMachine as = AGENT_USE_TRUSS_GA ?
                new AppServerTomcatMachine(AS_TOMCAT_MACHINE_ID, tasResolver, AGENT_CURRENT_TRUSS_TOMCAT, AGENT_PREV_TRUSS_TOMCAT, DEFAULT_COPY_RESULTS_PASSWORD) :
                new AppServerTomcatMachine(AS_TOMCAT_MACHINE_ID, tasResolver, AGENT_CURRENT_VERSION, AGENT_PREV_VERSION, DEFAULT_COPY_RESULTS_PASSWORD);
        ITestbedMachine appServerMachine = PREDEPLOYED_TOMCAT ? as.initPredeployed(dbScriptRole, emRole) : as.init(dbScriptRole, emRole);
        testBed.addMachine(appServerMachine);
        KonakartRole appRole = (KonakartRole) appServerMachine.getRoleById(AS_TOMCAT_MACHINE_ID + AppServerTomcatMachine.KONAKART_ROLE_ID);

        // JMETER TOMCAT
        JMeterTomcatMachine jmeter = new JMeterTomcatMachine(JMETER_TOMCAT_MACHINE_ID, tasResolver, DEFAULT_COPY_RESULTS_PASSWORD);
        ITestbedMachine jmeterMachine = PREDEPLOYED_TOMCAT ? jmeter.initPredeployed(appRole, emRole) : jmeter.init(appRole, emRole);
        testBed.addMachine(jmeterMachine);
    }

}
