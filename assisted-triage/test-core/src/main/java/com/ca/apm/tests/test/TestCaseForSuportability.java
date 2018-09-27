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

package com.ca.apm.tests.test;

import static org.testng.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.classes.from.appmap.plugin.DataChunk;
import com.ca.apm.classes.from.appmap.plugin.MetricValueResponse;
import com.ca.apm.classes.from.appmap.plugin.MetricValues;
import com.ca.apm.classes.from.appmap.plugin.Specifier.SpecifierType;
import com.ca.apm.tests.testbed.AssistedTriageTestbed;
import com.ca.apm.tests.utils.Common;
import com.ca.apm.tests.utils.EmRestRequest;
import com.ca.apm.tests.utils.LocalStorage;
import com.ca.tas.restClient.IRestResponse;
import com.ca.tas.restClient.RestClient;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * TestCaseForSupportability Test class to check supportability metrics for AT Engine.
 *
 */

public class TestCaseForSuportability extends TasTestNgTest {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private Common common = new Common();
    private RestClient restClient = new RestClient();

    private String agcHost = envProperties
        .getMachineHostnameByRoleId(AssistedTriageTestbed.SA_MASTER_EM_ROLE);
    private String momHost = envProperties
        .getMachineHostnameByRoleId(AssistedTriageTestbed.MOM_PROVIDER_EM_ROLE)
        + ".ca.com"
        + ":8081";
    private String collHost = envProperties
        .getMachineHostnameByRoleId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER_EM_ROLE);

    private String ENTERPRISE_TEAM_CENTER = "Enterprise Team Center";
    private static final String VIRTUAL_AGENT =
        "Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Metric Agent (Virtual)";
    private static final String EXACT = SpecifierType.EXACT.toString();

    @BeforeMethod
    public void initTestMethod(Method testMethod) {
        @SuppressWarnings("unused")
        LocalStorage localStorage = new LocalStorage(testMethod);
    }

    @Tas(testBeds = @TestBed(name = AssistedTriageTestbed.class, executeOn = AssistedTriageTestbed.SA_MASTER), size = SizeType.MEDIUM, owner = "katan09")
    @Test(groups = {"SupportabilityMetrics"})
    private void testCase_SupportabilityMetrics() throws IOException, ParseException,
        InterruptedException {
        /*
         * Fetches supportability metrics fromAGC and MOM for past 2hrs and verfies if metrics got
         * reported or not.
         */

        // Start time to query AT Rest API
        Timestamp start_time = common.getCurrentTimeinISO8601Format(-2);
        Timestamp end_time = common.getCurrentTimeinISO8601Format(0);

        // Verify Metrics on AGC
        ArrayList<String> metricsOnAGC =
            new ArrayList<String>(
                Arrays
                    .asList(
                        "Enterprise Manager|Assisted Triage|Analyst|High Call Ratio:Statements Generated Per Interval",
                        "Enterprise Manager|Assisted Triage|Analyst|High Call Ratio:Unique Events Received Per Interval",
                        "Enterprise Manager|Assisted Triage|Analyst|Zone Identifier:Statements Generated Per Interval",
                        "Enterprise Manager|Assisted Triage|Analyst|Zone Identifier:Unique Events Received Per Interval",
                        "Enterprise Manager|Assisted Triage|Contextualizer:Incoming Vertices Per Interval",
                        "Enterprise Manager|Assisted Triage|Contextualizer:Current Live Contexts",
                        "Enterprise Manager|Assisted Triage|Database|Evidence Service:Evidences Updated Per Interval",
                        "Enterprise Manager|Assisted Triage|Database|Evidence Service:Evidences Created Per Interval",
                        "Enterprise Manager|Assisted Triage|Database|Story Service:Stories Created Per Interval",
                        "Enterprise Manager|Assisted Triage|Database|Story Service:Stories Updated Per Interval",
                        "Enterprise Manager|Assisted Triage|Editor:Processed Contexts Per Interval",
                        "Enterprise Manager|Assisted Triage|Event Processor:Events Received Per Interval",
                        "Enterprise Manager|Assisted Triage|REST|Story Controller:Requests Received per Interval",
                        "Enterprise Manager|Assisted Triage|REST|Story Controller:Stories Returned per Interval"));


        for (String metric : metricsOnAGC) {
            verifySupportabilityMetric(agcHost, start_time, end_time, VIRTUAL_AGENT, metric,
                ENTERPRISE_TEAM_CENTER, false);
        }

        // Verify Metrics on AGC which are expected to be null
        ArrayList<String> metricsOnAGCNull =
            new ArrayList<String>(Arrays.asList(
                "Enterprise Manager|Assisted Triage|Contextualizer:Exceptions Thrown Per Interval",
                "Enterprise Manager|Assisted Triage|Contextualizer:Rejected Contexts Per Interval",
                "Enterprise Manager|Assisted Triage|Editor:Exceptions Thrown Per Interval",
                "Enterprise Manager|Assisted Triage|Event Processor:Ignored Events Per Interval"));

        for (String metric : metricsOnAGCNull) {

            verifySupportabilityMetric(agcHost, start_time, end_time, VIRTUAL_AGENT, metric,
                ENTERPRISE_TEAM_CENTER, true);
        }

        // Verify Metrics on MOM (Event Generator - Alerts & DA)
        ArrayList<String> metricsOnMOM =
            new ArrayList<String>(
                Arrays
                    .asList(
                        "Enterprise Manager|Assisted Triage|Event Generator|Alert:Events Sent Per Interval",
                        "Enterprise Manager|Assisted Triage|Event Generator|Alert:Raw Alert States Received Per Interval",
                        "Enterprise Manager|Assisted Triage|Event Generator|DA:Events Sent Per Interval",
                        "Enterprise Manager|Assisted Triage|Event Generator|DA:Raw States Received Per Interval"));

        for (String metric : metricsOnMOM) {

            verifySupportabilityMetric(agcHost, start_time, end_time, VIRTUAL_AGENT, metric,
                momHost, false);
        }

        // Verify Metrics on MOM - Collector (Event Generator - Errors and Stalls)
        ArrayList<String> metricsOnError =
            new ArrayList<String>(
                Arrays
                    .asList(
                        "Enterprise Manager|Assisted Triage|Event Generator|Errors and Stalls:Error Snapshots Received Per Interval",
                        "Enterprise Manager|Assisted Triage|Event Generator|Errors and Stalls:Events Sent Per Interval"));

        for (String metric : metricsOnError) {

            String agentSpecifier =
                VIRTUAL_AGENT + " (" + collHost + "@" + AssistedTriageTestbed.EM_SERVER_PORT + ")";

            verifySupportabilityMetric(agcHost, start_time, end_time, agentSpecifier, metric,
                momHost, false);
        }
    }

    // Verify supportability metric
    public void verifySupportabilityMetric(String agcHost, Timestamp startTimestamp,
        Timestamp endTimestamp, String agentSpecifier, String metricSpecifier, String momHost,
        boolean isNull) throws IOException {

        final String urlPartMetrics = "http://" + agcHost + ":8081/apm/appmap/private/metric";

        Long rangeMillisec = endTimestamp.getTime() - startTimestamp.getTime();
        String payload =
            metricPayload(agentSpecifier, metricSpecifier, EXACT, momHost, rangeMillisec,
                endTimestamp);
        log.info("Logging Supporatbility Metric Payload-- " + VIRTUAL_AGENT + "|" + metricSpecifier);
        log.info(payload);

        List<MetricValues> currMetricList = fetchMetricsFromPayload(urlPartMetrics, payload);

        // Check to see if metric information was found
        if (currMetricList.isEmpty()) {
            fail(" Metric for payload not found " + payload);
        }
        MetricValues currentMetrics = currMetricList.iterator().next();

        Iterator<DataChunk> itMetrics = currentMetrics.getDataChunks().iterator();

        double value = 0.0;

        while (itMetrics.hasNext()) {
            DataChunk metric = itMetrics.next();

            double[] values = metric.getValues();
            value = values[0];
        }

        if (isNull == false) {
            if (value == 0) {
                fail("No values in metrics for current metric : " + VIRTUAL_AGENT + "|"
                    + metricSpecifier + " Test for Supportability metric failed.");
            } else {
                log.info("Metric values founs for current metric : " + VIRTUAL_AGENT + "|"
                    + metricSpecifier + " Test for Supportability metric passed.");
            }
        } else if (isNull == true) {
            if (value == 0) {
                log.info("Metric values not found for current metric : " + VIRTUAL_AGENT + "|"
                    + metricSpecifier + " Test for Supportability metric passed.");
            } else {
                fail("Expecting no value for metric. Metric values found for current metric : "
                    + VIRTUAL_AGENT + "|" + metricSpecifier + " Value Found : " + value
                    + " Test for Supportability metric failed.");
            }
        }
    }

    // Constructs payload to query AppMap Rest API to get metrics
    public String metricPayload(String agentSpecifier, String metricSpecifier, String type,
        String momRole, Long range, Timestamp lastTimestamp) {

        String payload =
            "{\"metricQueries\":" + "[{\"agentSpecifier\":" + "{\"specifier\": \"" + agentSpecifier
                + "\"," + "\"type\": \"EXACT\"}," + "\"metricSpecifier\":" + "{\"specifier\": \""
                + metricSpecifier + "\"," + "\"type\": \"" + type + "\"}," + "\"momFilter\":[\""
                + momRole + "\"]}],\"queryRange\":{\"rangeSize\":" + range + "," + "\"endTime\":\""
                + common.timestamp2String(lastTimestamp) + "\",\"frequency\":" + range
                + "},\"fillGapsFlag\":false,\"uvb\":false,\"aggregate\":false}";

        return payload;
    }

    // Fetches metric from payload and puts in right format
    public List<MetricValues> fetchMetricsFromPayload(String urlPart, String payload)
        throws IOException {

        // Calls AppMap Rest and metrics data
        EmRestRequest request = new EmRestRequest(urlPart, payload);
        IRestResponse<String> response = restClient.process(request);
        String jsonResponse = response.getContent();

        ObjectMapper mapper = new ObjectMapper();
        MetricValueResponse metricResponse =
            mapper.readValue(jsonResponse, MetricValueResponse.class);
        List<MetricValues> returnMetrics = metricResponse.getMetrics();

        return returnMetrics;
    }


}
