package com.ca.apm.tests.sdk.service;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.common.util.SDKWebServiceUtil;
import com.ca.apm.tests.base.StandAloneEMOneTomcatTestsBase;

public class SDKEMStandaloneTomcatTests extends StandAloneEMOneTomcatTestsBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SDKEMStandaloneTomcatTests.class);
    protected String testCaseId;
    protected String testCaseName;
    protected String targetString = null;
    protected String response = null;

    Map<String, String> webServiceParamIn = new HashMap<String, String>();

    public SDKEMStandaloneTomcatTests() {


    }

    @BeforeTest(alwaysRun = true)
    public void initialize() {
        super.initialize();

        appendProp(SDKWebServiceUtil.listAddPropsToEnableWebserviceLog(), EM_MACHINE_ID, configFileEM);
        webServiceParamIn.clear();
        webServiceParamIn.put("userName", ApmbaseConstants.emUser);
        webServiceParamIn.put("password", ApmbaseConstants.emPassw);
        webServiceParamIn.put("EMHost", EMHost);
        webServiceParamIn.put("emWebPort", emWebPort);
    }

    @Test(groups = {"FULL"}, enabled = true)
    public void verify_ALM_455085_DE176345_AlertPollingService_GetAgentSnapshots_Failing() {
        testCaseId = "455085";
        testCaseName = testCaseId + "_DE176345_AlertPollingService_GetAgentSnapshots_Failing";
        String manModuleName = "Default";
        testCaseStart(testCaseName);

        try {
            startTestBed();
            waitForAgentNodes(tomcatAgentExp, EMHost, Integer.parseInt(emPort), emLibDir);
            LOGGER.info("Agnet EM started ....");
            harvestWait(60); // wait before webservice call

            webServiceParamIn.put("serviceName", "AlertPollingService");
            webServiceParamIn.put("operationName", "getAgentSnapshots");
            webServiceParamIn.put("manModuleName", manModuleName);

            response = SDKWebServiceUtil.createSOAPRequest(webServiceParamIn);

            targetString = "java.lang.reflect.InvocationTargetException";
            Assert.assertFalse(SDKWebServiceUtil.validateResult(response, targetString));
            targetString = "<agentName xsi:type=\"xsd:string\">Tomcat Agent</agentName>";
            Assert.assertTrue(SDKWebServiceUtil.validateResult(response, targetString));
            targetString = "<manModuleName xsi:type=\"xsd:string\">" + manModuleName + "</manModuleName>";
            Assert.assertTrue(SDKWebServiceUtil.validateResult(response, targetString));

            LOGGER.info("VAlidation Completed For Test:After call:" + testCaseId);
        } catch (Exception e) {
            LOGGER.error("Test Fail ...." + testCaseId);
            Assert.assertTrue(false);
            e.printStackTrace();
        } finally {
            stopTestBed();
            revertConfigAndRenameLogsWithTestId(testCaseId);
            testCaseEnd(testCaseName);

        }

    }


    @Test(groups = {"FULL"}, enabled = true)
    public void verify_ALM_455087_DE186983_Web_Services_API_Operation_GetMetricData() {
        testCaseId = "455087";
        testCaseName = testCaseId + "_DE186983_Web_Services_API_Operation_GetMetricData";
        String agentRegex = null, metricRegex = null, startTime = null, endTime = null, dataFrequency = null;
        testCaseStart(testCaseName);
        agentRegex = "(.*)\\|(.*)\\|(.*)";
        metricRegex = "XML\\|SAX\\|SAXParserImpl:Concurrent Invocations";
        dataFrequency = "15";
        try {
            startTestBed();
            waitForAgentNodes(tomcatAgentExp, EMHost, Integer.parseInt(emPort), emLibDir);
            LOGGER.info("Agnet EM started ....");
            harvestWait(60); // wait before webservice call

            startTime = SDKWebServiceUtil.getCurrentTime();
            LOGGER.info("Start time ...." + startTime);
            harvestWait(60 * 2); // wait to get time GAP for Metric query
            endTime = SDKWebServiceUtil.getCurrentTime();
            LOGGER.info("end time ...." + endTime);

            webServiceParamIn.put("serviceName", "MetricsDataService");
            webServiceParamIn.put("operationName", "getMetricData");
            webServiceParamIn.put("agentRegex", agentRegex);
            webServiceParamIn.put("metricRegex", metricRegex);
            webServiceParamIn.put("startTime", startTime);
            webServiceParamIn.put("endTime", endTime);
            webServiceParamIn.put("dataFrequency", dataFrequency);

            response = SDKWebServiceUtil.createSOAPRequest(webServiceParamIn);

            targetString = "<metricValue xsi:type=\"xsd:string\" xsi:nil=\"true\"/>";
            LOGGER.info("Recived SOAP response ...." + response);
            Assert.assertTrue(SDKWebServiceUtil.validateResult(response, targetString));
            metricRegex = "XML|SAX|SAXParserImpl:Concurrent Invocations";
            targetString = "<metricName xsi:type=\"xsd:string\">" + metricRegex + "</metricName>";
            Assert.assertTrue(SDKWebServiceUtil.validateResult(response, targetString));
            targetString = "</metricValue>";
            Assert.assertFalse(SDKWebServiceUtil.validateResult(response, targetString));

            LOGGER.info("VAlidation Completed For Test:After call:" + testCaseId);
        } catch (Exception e) {
            LOGGER.error("Test Fail ...." + testCaseId);
            Assert.assertTrue(false);
            e.printStackTrace();
        } finally {
            stopTestBed();
            revertConfigAndRenameLogsWithTestId(testCaseId);
            testCaseEnd(testCaseName);

        }

    }



}
