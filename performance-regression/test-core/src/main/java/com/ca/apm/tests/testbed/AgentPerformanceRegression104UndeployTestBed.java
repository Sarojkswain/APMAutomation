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

import com.ca.apm.tests.testbed.machines.*;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
@TestBedDefinition
public class AgentPerformanceRegression104UndeployTestBed extends AgentPerformanceRegression104TestBed {

    public static boolean UNDEPLOY_TOMCAT;
    public static boolean UNDEPLOY_WAS;
    public static boolean UNDEPLOY_WLS;
    public static boolean UNDEPLOY_IIS;

    public AgentPerformanceRegression104UndeployTestBed() {
        super();

        UNDEPLOY_TOMCAT = true;
        UNDEPLOY_WAS = true;
        UNDEPLOY_WLS = true;
        UNDEPLOY_IIS = true;
    }

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        ITestbed testBed = new Testbed("AgentPerformanceRegressionUndeployTestBed");

        //EM
//        ITestbedMachine emMachine = (new EmMachine(EM_MACHINE_ID, tasResolver)).init();
//        testBed.addMachine(emMachine);

        if (UNDEPLOY_TOMCAT) {
            //DB TOMCAT
            ITestbedMachine dbOracleTomcatMachine = (new DbOracleKonakartTradeDbMachine(DB_ORACLE_TOMCAT_MACHINE_ID, tasResolver,
                    DbOracleKonakartTradeDbMachine.DEFAULT_TEMPLATE_TOMCAT)).undeploy();
            testBed.addMachine(dbOracleTomcatMachine);
            // AS TOMCAT
            ITestbedMachine appServerTomcatMachine = (new AppServerTomcat104Machine(AS_TOMCAT_MACHINE_ID, tasResolver, AGENT_CURRENT_VERSION, AGENT_PREV_VERSION, DEFAULT_COPY_RESULTS_PASSWORD))
                    .undeploy();
            testBed.addMachine(appServerTomcatMachine);
            // JMETER TOMCAT
            ITestbedMachine jmeterTomcatMachine = (new JMeterTomcatMachine(JMETER_TOMCAT_MACHINE_ID, tasResolver, true, DEFAULT_COPY_RESULTS_PASSWORD))
                    .undeploy();
            testBed.addMachine(jmeterTomcatMachine);
        }

        if (UNDEPLOY_WAS) {
            // DB WEBSPHERE
            ITestbedMachine dbOracleWasMachine = (new DbOracleTradeDbMachine(DB_ORACLE_WAS_MACHINE_ID, tasResolver,
                    DbOracleTradeDbMachine.DEFAULT_TEMPLATE_WAS)).undeploy();
            testBed.addMachine(dbOracleWasMachine);
            // AS WEBSPHERE
            ITestbedMachine appServerWasMachine = (new AppServerWebsphere104Machine(AS_WAS_MACHINE_ID, tasResolver, AGENT_CURRENT_VERSION, AGENT_PREV_VERSION, DEFAULT_COPY_RESULTS_PASSWORD))
                    .undeploy();
            testBed.addMachine(appServerWasMachine);
            // JMETER WEBSPHERE
            ITestbedMachine jmeterWasMachine = (new JMeterWebsphereMachine(JMETER_WAS_MACHINE_ID, tasResolver, true, DEFAULT_COPY_RESULTS_PASSWORD))
                    .undeploy();
            testBed.addMachine(jmeterWasMachine);
        }

        if (UNDEPLOY_WLS) {
            // DB WEBLOGIC
            ITestbedMachine dbOracleWlsMachine = (new DbOracleTradeDbMachine(DB_ORACLE_WLS_MACHINE_ID, tasResolver,
                    DbOracleTradeDbMachine.DEFAULT_TEMPLATE_WLS)).undeploy();
            testBed.addMachine(dbOracleWlsMachine);
            // AS WEBLOGIC
            ITestbedMachine appServerWlsMachine = (new AppServerWeblogic104Machine(AS_WLS_MACHINE_ID, tasResolver, AGENT_CURRENT_VERSION, AGENT_PREV_VERSION, DEFAULT_COPY_RESULTS_PASSWORD))
                    .undeploy();
            testBed.addMachine(appServerWlsMachine);
            // JMETER WEBLOGIC
            ITestbedMachine jmeterWlsMachine = (new JMeterWeblogicMachine(JMETER_WLS_MACHINE_ID, tasResolver, true, DEFAULT_COPY_RESULTS_PASSWORD))
                    .undeploy();
            testBed.addMachine(jmeterWlsMachine);
        }

        if (UNDEPLOY_IIS) {
            //DB IIS
            ITestbedMachine dbMssqlIisMachine = (new DbMsSqlTradeDbMachine(DB_MSSQL_IIS_MACHINE_ID, tasResolver, DEFAULT_COPY_RESULTS_PASSWORD)).undeploy();
            testBed.addMachine(dbMssqlIisMachine);
            // AS IIS
            ITestbedMachine appServerIisMachine = (new AppServerIis104Machine(AS_IIS_MACHINE_ID, tasResolver, AGENT_CURRENT_VERSION, AGENT_PREV_VERSION, DEFAULT_COPY_RESULTS_PASSWORD))
                    .undeploy();
            testBed.addMachine(appServerIisMachine);
            // JMETER IIS
            ITestbedMachine jmeterIisMachine = (new JMeterIisMachine(JMETER_IIS_MACHINE_ID, tasResolver, true, DEFAULT_COPY_RESULTS_PASSWORD))
                    .undeploy();
            testBed.addMachine(jmeterIisMachine);
        }

        return testBed;

    }


}

