package com.ca.apm.tests.em.properties;


import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.tests.base.OneCollectorOneTomcatTestsBase;



public class LaxEMPropertiesTestsOneCollectorOneAgnet extends OneCollectorOneTomcatTestsBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(LaxEMPropertiesTestsOneCollectorOneAgnet.class);

    protected String testCaseId;
    protected String testCaseName;
    protected ArrayList<String> rolesInvolved = new ArrayList<String>();


    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_372932_transport_override_isengard_high_concurrency_pool_max_size() {

        testCaseId = "372932";
        testCaseName = "ALM_" + testCaseId + "transport_override_isengard_high_concurrency_pool_max_size";
        String momMsg = "The EM failed to start. null";

        List<String> addEMProperties = new ArrayList<String>();
        rolesInvolved.add(MOM_ROLE_ID);
        rolesInvolved.add(COLLECTOR1_ROLE_ID);

        try {


            testCaseStart(testCaseName);
            addEMProperties.add("transport.override.isengard.high.concurrency.pool.max.size=-10");

            appendProp(addEMProperties, MOM_MACHINE_ID, momconfigFile);
            startEMServices();
            checkMoMLogForMsg(momMsg);
            LOGGER.info("Validation completed for  MoM. Log Message :::" + momMsg);
            stopEMServices();
            replaceMoMProperty("log4j.logger.Manager=INFO, console, logfile",
                "log4j.logger.Manager=DEBUG, console, logfile");
            replaceMoMProperty("transport.override.isengard.high.concurrency.pool.max.size=-10",
                "transport.override.isengard.high.concurrency.pool.max.size=5");
            LOGGER.info("Validation completed for  MoM. Log Message :::" + momMsg);
            startEMServices();

            momMsg = "Introscope Enterprise Manager started";
            checkMoMLogForMsg(momMsg);
            LOGGER.info("Validation completed for  MoM. Log Message :::" + momMsg);
            momMsg = "Added transport.override.isengard.high.concurrency.pool.max.size=5";
            checkMoMLogForMsg(momMsg);

            LOGGER.info("Validation completed for  MoM. Log Message :::" + momMsg);


        } finally {
            stopEMServices();
            revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
            rolesInvolved.clear();
            testCaseEnd(testCaseName);
        }
    }


    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_359218_Collector_Identifier() {

        testCaseId = "359218";
        testCaseName = "ALM_" + testCaseId + "Collector_Identifier";
        List<String> addEMProperties = new ArrayList<String>();
        rolesInvolved.add(MOM_ROLE_ID);
        rolesInvolved.add(COLLECTOR1_ROLE_ID);
        rolesInvolved.add(TOMCAT_ROLE_ID);

        try {

            testCaseStart(testCaseName);
            addEMProperties.add("introscope.enterprisemanager.clustering.collector.identifier=abcd");
            appendProp(addEMProperties, COLLECTOR1_MACHINE_ID, momconfigFile);
            startTestBed();
            clw.checkCollectorToMOMConnectivity(".*" + "abcd" + ".*", ".*CPU.*", momHost, momPort, emLibDir);

            LOGGER.info("Validation completed for  Collector_Identifier :::");

        } finally {
            stopTestBed();
            revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
            rolesInvolved.clear();
            testCaseEnd(testCaseName);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_392282_Default_Enterprise_Manager_Name() {

        testCaseId = "392282";
        testCaseName = "ALM_" + testCaseId + "Default_Enterprise_Manager_Name";
        List<String> addEMProperties = new ArrayList<String>();
        rolesInvolved.add(MOM_ROLE_ID);
        rolesInvolved.add(COLLECTOR1_ROLE_ID);
        rolesInvolved.add(TOMCAT_ROLE_ID);

        try {

            testCaseStart(testCaseName);
            addEMProperties.add("introscope.enterprisemanager.name=abcd");
            appendProp(addEMProperties, MOM_MACHINE_ID, momconfigFile);
            startTestBed();
            List<String> arr =
                clw.getMetricValueForTimeInMinutes(ApmbaseConstants.emUser, ApmbaseConstants.emPassw, ".*", ".*",
                    momHost, Integer.parseInt(momPort), emLibDir, 1);
            Assert.assertTrue(arr.toString().contains("abcd"));

            LOGGER.info("Validation completed for  Collector_Identifier :::");

        } finally {
            stopTestBed();
            revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
            rolesInvolved.clear();
            testCaseEnd(testCaseName);
        }
    }


    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_392301_Invalid_Clustering_Manager_SlowCollectorThreshold() {

        testCaseId = "392301";
        testCaseName = "ALM_" + testCaseId + "Invalid_Clustering_Manager_SlowCollectorThreshold";
        String momMsg = "Introscope Enterprise Manager started";
        rolesInvolved.add(MOM_ROLE_ID);
        rolesInvolved.add(COLLECTOR1_ROLE_ID);

        try {

            testCaseStart(testCaseName);
            replaceMoMProperty("introscope.enterprisemanager.clustering.manager.slowcollectorthreshold=10000",
                "introscope.enterprisemanager.clustering.manager.slowcollectorthreshold=-10000");
            replaceMoMProperty(
                "introscope.enterprisemanager.clustering.manager.slowcollectordisconnectthresholdseconds=60",
                "introscope.enterprisemanager.clustering.manager.slowcollectordisconnectthresholdseconds=70");
            replaceMoMProperty("#introscope.enterprisemanager.clustering.login.em1.weight=",
                "introscope.enterprisemanager.clustering.login.em1.weight=");

            startEMServices();

            momMsg =
                "Property introscope.enterprisemanager.clustering.manager.slowcollectorthreshold set to -10000, set less than zero. Ignoring";
            checkMoMLogForMsg(momMsg);

            LOGGER.info("Validation completed for  MoM. Log Message :::" + momMsg);

            momMsg = "New slow collector disconnect threshold property detected: 70 seconds";

            checkMoMLogForMsg(momMsg);

            LOGGER.info("Validation completed for  MoM. Log Message :::" + momMsg);

        } finally {
            stopEMServices();
            revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
            rolesInvolved.clear();
            testCaseEnd(testCaseName);
        }
    }


    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_455089_DE176341_Network_Performance_Due_To_Isengard_SetReceiveBufferSize() {

        testCaseId = "455089";
        String bufferSize = "100000";
        String bufferSizeWithComma = "100,000";
        int hardcodebufferSize = 32768;
        testCaseName = "ALM_" + testCaseId + "_DE176341_Network_Performance_Due_To_Isengard_SetReceiveBufferSize";
        String colMsg = "Accepted incoming connection";
        String momMsg = "Established client connection";
        rolesInvolved.add(MOM_ROLE_ID);
        rolesInvolved.add(COLLECTOR1_ROLE_ID);
        List<String> addEMProperties = new ArrayList<String>();
        try {

            testCaseStart(testCaseName);
            addEMProperties.add("introscope.enterprisemanager.sockets.receivebuffersize=" + bufferSize);
            appendProp(addEMProperties, MOM_MACHINE_ID, momconfigFile);
            appendProp(addEMProperties, COLLECTOR1_MACHINE_ID, colconfigFile);

            replaceMoMProperty("log4j.logger.Manager=INFO, console, logfile",
                "log4j.logger.Manager=DEBUG, console, logfile");
            replaceColProperty("log4j.logger.Manager=INFO, console, logfile",
                "log4j.logger.Manager=DEBUG, console, logfile");

            startEMServices();


            checkCollLogForMsg(colMsg);

            if (System.getProperty("os.name").toUpperCase().contains("WINDOWS"))
                colMsg = "socket receive buffer size [bytes]: " + bufferSize;
            else

                colMsg = "Socket receive buffer size is " + bufferSizeWithComma;

            checkCollLogForMsg(colMsg);

            LOGGER.info("Validation completed for  COL. Log Message :::" + colMsg);

            checkMoMLogForMsg(momMsg);

            momMsg = "socket send buffer size [bytes]: ";
            checkMoMLogForNoMsg(momMsg + hardcodebufferSize);

            momMsg = "socket receive buffer size [bytes]: ";
            checkMoMLogForNoMsg(momMsg + hardcodebufferSize);

            LOGGER.info("Validation completed for  MoM. Log Message :::" + momMsg);

        } finally {
            stopEMServices();
            revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
            rolesInvolved.clear();
            testCaseEnd(testCaseName);
        }
    }



}
