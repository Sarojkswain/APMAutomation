/*
 * Copyright (c) 2014 CA. All rights reserved.
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
 * 
 * Author : JAMSA07/ SANTOSH JAMMI
 * Date : 11/12/2016
 */
package com.ca.apm.tests.smartstor.DBPersistenceCollection;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.commons.coda.common.OracleUtil;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.tests.base.StandAloneEMOneTomcatOneJBossTestsBase;

public class DatabasePersistentCollectionChanges extends StandAloneEMOneTomcatOneJBossTestsBase {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(DatabasePersistentCollectionChanges.class);
    public List<String> oracleSmartStorData = new ArrayList<String>();
    TestUtils utility = new TestUtils();
    OracleUtil oracleutil = new OracleUtil("swasa02-win14", "1521", "orcl.ca.com",
        "automationuser", "automationuser");
    List<String> dbPersistenceProperties = new ArrayList<String>();
    List<String> roleIds = new ArrayList<String>();
    String oracleUrl=null;
    public DatabasePersistentCollectionChanges() {
        roleIds.add(EM_ROLE_ID);
    }
    
    @BeforeTest(alwaysRun = true)
    public void initialize() {

        syncMachines();
        String oracleHostname = "swasa02-win14";
        String oraclePort="1521";
        String oracleServiceName="orcl";
        oracleUrl="jdbc:oracle:thin:@"+oracleHostname+":"+oraclePort+":"+oracleServiceName;
        replaceProp("log4j.logger.Manager=INFO,console,logfile",
            "log4j.logger.Manager=DEBUG,console,logfile", EM_MACHINE_ID, emConfigFile);
        backupConfigs();
        setUpDBPersistenceCollection();

        startEM();
        startAgents();
        utility.connectToURL("http://" + tomcatHost + ":9091", 50);
        utility.connectToURL("http://" + jbossHost + ":8080", 50);
        harvestWait(120);
        try {

            String query =
                "SELECT WT_AGENT.AGENT_NAME, WT_METRIC_NAME.METRIC_NAME FROM WT_METRIC INNER JOIN WT_METRIC_NAME ON WT_METRIC_NAME.METRIC_NAME_ID=WT_METRIC.METRIC_NAME_ID INNER JOIN WT_AGENT ON WT_AGENT.AGENT_ID=WT_METRIC.AGENT_ID";
            oracleSmartStorData = oracleutil.getOracleSmartStorData(query);

            LOGGER.info("" + oracleSmartStorData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test(groups = {"FULL"}, enabled = true)
    public void verify_ALM_298604_DBPersistenceCollectionRecordMetrics() {
        boolean flag = false;
        Assert.assertFalse(oracleSmartStorData.isEmpty());
        for (String item : oracleSmartStorData) {
            if (item.toLowerCase().contains("tomcat")) flag = true;
        }
        Assert.assertTrue(flag);
    }

    @Test(groups = {"FULL"}, enabled = true)
    public void verify_ALM_298605_DBPersistenceCollectionRecordMetrics_JBoss() {
        boolean flag = false;
        Assert.assertFalse(oracleSmartStorData.isEmpty());
        for (String item : oracleSmartStorData) {
            if (item.toLowerCase().contains("jboss")) flag = true;
        }
        Assert.assertTrue(flag);
    }

    @Test(groups = {"FULL"}, enabled = true)
    public void verify_ALM_298608_DBPersistenceCollectionRecordMetrics_HotConfig() {
        dbPersistenceProperties.clear();
        dbPersistenceProperties
            .add("(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\)");
        dbPersistenceProperties
            .add("Enterprise Manager\\|Database: Metric Data Points Sent per Interval");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection1.frequencyinseconds=15");
        appendProp(dbPersistenceProperties, emMachineId, emConfigFile);
        checkEMLogForMsg("(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\)");
    }

    @Test(groups = {"FULL"}, enabled = true)
    public void verify_ALM_298609_DBPersistenceCollectionRecordMetrics_MetricDataPointSentPerInterval() {
        String metric = "EM|Database|Metric data points Sent per interval";
        List<String> list = getConnectedAgentMetricForEMHost(
            "(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\)",
            metric, emHost, Integer.parseInt(emPort), emLibDir);
        Assert.assertTrue(list.size()>=17);
    }

    @Test(groups = {"FULL"}, enabled = true)
    public void verify_ALM_298610_DBPersistenceCollectionRecordMetrics_QueuedMetricsDataPoints() {
        try {
            List<String> dbPersistenceProperties = new ArrayList<String>();
            dbPersistenceProperties
                .add("introscope.enterprisemanager.db.driver=oracle.jdbc.driver.OracleDriver");
            dbPersistenceProperties
                .add("introscope.enterprisemanager.db.url="+oracleUrl);
            dbPersistenceProperties.add("introscope.enterprisemanager.db.username=automationuser");
            dbPersistenceProperties.add("introscope.enterprisemanager.db.password=automationuser");
            dbPersistenceProperties
                .add("introscope.enterprisemanager.db.password.plaintextpassword=true");

            dbPersistenceProperties
                .add("introscope.enterprisemanager.database.collection1.agentExpression=(.*)");
            dbPersistenceProperties
                .add("introscope.enterprisemanager.database.collection1.metricExpression=(.*)");
            dbPersistenceProperties
                .add("introscope.enterprisemanager.database.collection1.frequencyinseconds=15");
            appendProp(dbPersistenceProperties, EM_MACHINE_ID, emConfigFile);
            startEM();
            replaceProp(
                "introscope.enterprisemanager.database.recordingQueue.upperLimit.value=153600",
                "introscope.enterprisemanager.database.recordingQueue.upperLimit.value=10",
                emMachineId, emConfigFile);
            utility.connectToURL("http://" + tomcatHost + ":9091", 200);
            utility.connectToURL("http://" + jbossHost + ":8080", 200);
            checkEMLogForMsg("Database Persistent Collections dropping data. Queue limit exceeded:");
        } finally {
            stopEM();
            revertConfigAndRenameLogsWithTestId("298610", roleIds);

        }
    }

    @Test(groups = {"FULL"}, enabled = true)
    public void verify_ALM_298612_DBPersistenceCollectionRecordMetrics_QueuedMetricsDataPointsHotConfig() {
        replaceProp("introscope.enterprisemanager.database.recordingQueue.upperLimit.value=153600",
            "introscope.enterprisemanager.database.recordingQueue.upperLimit.value=10",
            emMachineId, emConfigFile);
        checkEMLogForMsg("introscope.enterprisemanager.database.recordingQueue.upperLimit.value=10");
        replaceProp("introscope.enterprisemanager.database.recordingQueue.upperLimit.value=10",
            "introscope.enterprisemanager.database.recordingQueue.upperLimit.value=153600",
            emMachineId, emConfigFile);
    }

    @Test(groups = {"FULL"}, enabled = true)
    public void verify_ALM_298619_DBPersistenceCollectionRecordMetrics_WritingDataToDB() {
        checkEMLogForMsg("Start writing db data");
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_392305_DBPersistenceCollectionRecordMetrics_InvalidDBAttributes() {
        try {
            stopEM();
            setUpDBPersistenceCollection();
            dbPersistenceProperties.add("introscope.enterprisemanager.db.useBatchInserts=true");
            dbPersistenceProperties.add("introscope.enterprisemanager.db.batchInsertSize=");
            dbPersistenceProperties.add("introscope.enterprisemanager.db.queryTimeoutInSeconds=");
            dbPersistenceProperties.add("introscope.enterprisemanager.db.maximumTableCacheSize=");
            dbPersistenceProperties.add("introscope.enterprisemanager.db.queryConnections=");
            appendProp(dbPersistenceProperties, emMachineId, emConfigFile);
            startEM();

            checkEMLogForMsg(" introscope.enterprisemanager.db.queryTimeoutInSeconds is not an integer:");
            checkEMLogForMsg("Using default value for introscope.enterprisemanager.db.queryTimeoutInSeconds: 0");
            checkEMLogForMsg("introscope.enterprisemanager.db.batchInsertSize is not an integer:");
            checkEMLogForMsg("Using default value for introscope.enterprisemanager.db.batchInsertSize: 100");
            checkEMLogForMsg("introscope.enterprisemanager.db.maximumTableCacheSize is not an integer:");
            checkEMLogForMsg("Using default value for introscope.enterprisemanager.db.maximumTableCacheSize: -1");
            checkEMLogForMsg("Added introscope.enterprisemanager.db.queryConnections=");
        } finally {
            stopEM();
            revertConfigAndRenameLogsWithTestId("392305", roleIds);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_392299_DBPersistenceCollectionRecordMetrics_InvalidCollection1Frequency() {
        try {
            setUpDBPersistenceCollection();
            startEM();

            replaceProp("introscope.enterprisemanager.database.collection1.frequencyinseconds=15",
                "introscope.enterprisemanager.database.collection1.frequencyinseconds=18",
                EM_MACHINE_ID, emConfigFile);

            checkEMLogForMsg("Changed introscope.enterprisemanager.database.collection1.frequencyinseconds=18 (15)");
        } finally {
            stopEM();
            revertConfigAndRenameLogsWithTestId("392299", roleIds);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_372948_DBPersistenceCollectionRecordMetrics_InvalidQueuedUpperLimitValue() {
        try {
            setUpDBPersistenceCollection();
            startEM();
            replaceProp(
                "introscope.enterprisemanager.database.recordingQueue.upperLimit.value=153600",
                "introscope.enterprisemanager.database.recordingQueue.upperLimit.value=",
                EM_MACHINE_ID, emConfigFile);
            checkEMLogForMsg("introscope.enterprisemanager.database.recordingQueue.upperLimit.value is not an integer:");
            checkEMLogForMsg("Using default value for introscope.enterprisemanager.database.recordingQueue.upperLimit.value: 153600");
        } finally {
            stopEM();
            revertConfigAndRenameLogsWithTestId("372948", roleIds);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_372944_DBPersistenceCollectionRecordMetrics_InvalidDBDriver() {
        setUpDBPersistenceCollection();
        replaceProp("introscope.enterprisemanager.db.driver=oracle.jdbc.driver.OracleDriver",
            "introscope.enterprisemanager.db.driver=oracle.jdbc.driver.OracleDriverr",
            EM_MACHINE_ID, emConfigFile);
        startEM();
        checkEMLogForMsg("Error loading the SQL driver: oracle.jdbc.driver.OracleDriverr:");
        // Error loading the SQL driver: oracle.jdbc.driver.OracleDriverr:
        // "java.lang.ClassNotFoundException: oracle.jdbc.driver.OracleDriverr"
        stopEM();
        revertConfigAndRenameLogsWithTestId("372944", roleIds);
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_372945_DBPersistenceCollectionRecordMetrics_InvalidDBURL() {
        try{
        setUpDBPersistenceCollection();
        replaceProp(
            "introscope.enterprisemanager.db.url="+oracleUrl,
            "introscope.enterprisemanager.db.url=jdbc:oracle:thin:@swasa02-win14:1521:orcl123",
            EM_MACHINE_ID, emConfigFile);
        startEM();
        checkEMLogForMsg("The Enterprise Manager will attempt to connect to the database.");
        checkEMLogForMsg("[Manager.Database] Failed to connect to the database");
        }finally
        {
        stopEM();
        revertConfigAndRenameLogsWithTestId("372945", roleIds);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_372947_DBPersistenceCollectionRecordMetrics_InvalidReconnectIntervalInSeconds() {
        try{
        setUpDBPersistenceCollection();
        startEM();
        replaceProp("introscope.enterprisemanager.db.reconnect.intervalInSeconds=30",
            "introscope.enterprisemanager.db.reconnect.intervalInSeconds=", EM_MACHINE_ID,
            emConfigFile);
        checkEMLogForMsg("introscope.enterprisemanager.db.reconnect.intervalInSeconds is not an integer:");
        checkEMLogForMsg("[Manager.Database] Using default value for introscope.enterprisemanager.db.reconnect.intervalInSeconds: ");
        }
        finally{
            stopEM();
            revertConfigAndRenameLogsWithTestId("372947", roleIds);
        }
    }

    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_372946_DBPersistenceCollectionRecordMetrics_InvalidDBCredentials() {
        try{
        setUpDBPersistenceCollection();
        replaceProp("introscope.enterprisemanager.db.password=automationuser",
            "introscope.enterprisemanager.db.password=automationuser123", EM_MACHINE_ID,
            emConfigFile);
        startEM();
        checkEMLogForMsg("[Manager.Database] Error opening the SQL connection: java.sql.SQLException: ORA-01017: invalid username/password; logon denied");
        }
        finally{
        stopEM();
        revertConfigAndRenameLogsWithTestId("372946", roleIds);
        }
    }

    public void stopEMServices() {
        stopEM(EM_ROLE_ID);
        stopEMServiceFlowExecutor(EM_MACHINE_ID);
        harvestWait(10);
    }

    public void stopAllAgents() {
        stopTomcatServiceFlowExecutor(TOMCAT_MACHINE_ID);
        stopJBossServiceFlowExecutor(TOMCAT_MACHINE_ID);
    }

    public void setUpDBPersistenceCollection() {
        List<String> dbPersistenceProperties = new ArrayList<String>();
        dbPersistenceProperties
            .add("introscope.enterprisemanager.db.driver=oracle.jdbc.driver.OracleDriver");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.db.url="+oracleUrl);
        dbPersistenceProperties.add("introscope.enterprisemanager.db.username=automationuser");
        dbPersistenceProperties.add("introscope.enterprisemanager.db.password=automationuser");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.db.password.plaintextpassword=true");

        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection1.agentExpression=(.*)\\|tomcat\\|tomcatagent");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection1.metricExpression=CPU:Processor Count");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection1.frequencyinseconds=15");

        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection2.agentExpression=(.*)\\|JBoss\\|JBoss Agent");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection2.metricExpression=CPU:Processor Count");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection2.frequencyinseconds=15");

        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection3.agentExpression=(.*)\\|tomcat\\|tomcatagent");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection3.metricExpression=CPU\\|Processor 0:Utilization % \\(aggregate\\)");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection3.frequencyinseconds=15");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection4.agentExpression=(.*)\\|JBoss\\|JBoss Agent");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection4.metricExpression=CPU\\|Processor 0:Utilization % \\(aggregate\\)");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection4.frequencyinseconds=15");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection5.agentExpression=(.*)\\|tomcat\\|tomcatagent");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection5.metricExpression=GC Heap:Bytes In Use");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection5.frequencyinseconds=15");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection6.agentExpression=(.*)\\|JBoss\\|JBoss Agent");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection6.metricExpression=GC Heap:Bytes In Use");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection6.frequencyinseconds=15");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection7.agentExpression=(.*)\\|tomcat\\|tomcatagent");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection7.metricExpression=GC Heap:Bytes Total");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection7.frequencyinseconds=15");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection8.agentExpression=(.*)\\|JBoss\\|JBoss Agent");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection8.metricExpression=GC Heap:Bytes Total");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection8.frequencyinseconds=15");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection9.agentExpression=(.*)\\|tomcat\\|tomcatagent");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection9.metricExpression=Agent Stats\\|Resources:% CPU Utilization \\(Host\\)");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection9.frequencyinseconds=15");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection10.agentExpression=(.*)\\|JBoss\\|JBoss Agent");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection10.metricExpression=Agent Stats\\|Resources:% CPU Utilization \\(Host\\)");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection10.frequencyinseconds=15");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection11.agentExpression=(.*)\\|tomcat\\|tomcatagent");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection11.metricExpression=Agent Stats\\|Resources:% Time Spent in GC");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection11.frequencyinseconds=15");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection12.agentExpression=(.*)\\|JBoss\\|JBoss Agent");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection12.metricExpression=Agent Stats\\|Resources:% Time Spent in GC");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection12.frequencyinseconds=15");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection13.agentExpression=(.*)\\|tomcat\\|tomcatagent");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection13.metricExpression=GC Monitor:GC Policy");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection13.frequencyinseconds=15");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection14.agentExpression=(.*)\\|JBoss\\|JBoss Agent");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection14.metricExpression=GC Monitor:GC Policy");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection14.frequencyinseconds=15");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection15.agentExpression=(.*)\\|tomcat\\|tomcatagent");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection15.metricExpression=Frontends\\|Apps\\|Welcome to Tomcat:Responses Per Interval");
        dbPersistenceProperties
            .add("introscope.enterprisemanager.database.collection15.frequencyinseconds=15");

        appendProp(dbPersistenceProperties, EM_MACHINE_ID, emConfigFile);

    }

    @AfterClass
    public void end() {
        stopEMServices();
        stopAllAgents();
        OracleUtil oracleutil = new OracleUtil("swasa02-win14", "1521", "orcl.ca.com",
            "automationuser", "automationuser");
        oracleutil.deleteAllSmartStorData();
    }
}
