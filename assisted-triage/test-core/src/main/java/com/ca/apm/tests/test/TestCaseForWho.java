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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.classes.from.appmap.plugin.AnalystHelperServices;
import com.ca.apm.classes.from.appmap.plugin.AnalystStatement;
import com.ca.apm.classes.from.appmap.plugin.AtStory;
import com.ca.apm.classes.from.appmap.plugin.AtStoryList;
import com.ca.apm.classes.from.appmap.plugin.DbtcStatement;
import com.ca.apm.classes.from.appmap.plugin.EvidenceStatement;
import com.ca.apm.classes.from.appmap.plugin.ExternalId;
import com.ca.apm.classes.from.appmap.plugin.MetricValues;
import com.ca.apm.classes.from.appmap.plugin.PatternAnalystStatement;
import com.ca.apm.classes.from.appmap.plugin.ProblemZoneStatement;
import com.ca.apm.classes.from.appmap.plugin.Specifier;
import com.ca.apm.classes.from.appmap.plugin.Specifier.SpecifierType;
import com.ca.apm.classes.from.appmap.plugin.Vertex;
import com.ca.apm.classes.from.appmap.plugin.VertexType;
import com.ca.apm.classes.from.appmap.plugin.ZoneIdentifierAnalyst;
import com.ca.apm.tests.testbed.AssistedTriageTestbed;
import com.ca.apm.tests.utils.Common;
import com.ca.apm.tests.utils.EmRestRequest;
import com.ca.apm.tests.utils.FetchATStories;
import com.ca.apm.tests.utils.FetchAttributeInfo;
import com.ca.apm.tests.utils.FetchMetricsUtils;
import com.ca.apm.tests.utils.LocalStorage;
import com.ca.apm.tests.utils.TemporaryEdge;
import com.ca.apm.tests.utils.TemporaryGraph;
import com.ca.apm.tests.utils.ValidateVertexInfo;
import com.ca.tas.restClient.IRestResponse;
import com.ca.tas.restClient.RestClient;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.fasterxml.jackson.databind.ObjectMapper;


public class TestCaseForWho extends TasTestNgTest {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private Common common = new Common();
    private ValidateVertexInfo validateVertex = new ValidateVertexInfo();
    private RunCommandFlowContext runCommandFlowContext;

    private FetchMetricsUtils fetchMetrics = new FetchMetricsUtils();
    private FetchAttributeInfo vertex = new FetchAttributeInfo();
    private FetchATStories atStories = new FetchATStories();

    private static Timestamp DBTC_Start_Time;
    private String restTimestamp = null;;

    private String agcHost = envProperties
        .getMachineHostnameByRoleId(AssistedTriageTestbed.SA_MASTER_EM_ROLE);
    private String momHost = envProperties
        .getMachineHostnameByRoleId(AssistedTriageTestbed.MOM_PROVIDER_EM_ROLE);

    private String batFile = "run.bat";
    private String batLocation = AssistedTriageTestbed.TOMCAT_INSTALL_DIR
        + "\\webapps\\pipeorgan\\WEB-INF\\lib\\";
    private String scenarioFolderName = "scenarios";

    private static final String IMPACT = "impact";
    private static final String EXACT = SpecifierType.EXACT.toString();

    @BeforeMethod
    public void initTestMethod(Method testMethod) {
        @SuppressWarnings("unused")
        LocalStorage localStorage = new LocalStorage(testMethod);
    }

    @Tas(testBeds = @TestBed(name = AssistedTriageTestbed.class, executeOn = AssistedTriageTestbed.SA_MASTER), size = SizeType.MEDIUM, owner = "katan09")
    @Test(groups = {"pipeorgan_who_feature"})
    private void testCase_DBTC() throws Exception {

        /* Test Case 1 - Test Case for DBTC scenario impacts BT/Application. */

        // Initialize query time for REST
        DBTC_Start_Time = common.getCurrentTimeinISO8601Format(0);
        Timestamp end_time = common.getCurrentTimeinISO8601Format(2);

        // Enable dbtc - Start problem scripts and wait for 5 mins
        runCommandFlowContext =
            common.runPipeOrganScenario(batLocation, batFile, scenarioFolderName,
                "Problem-BTWebserviceDBTC.xml");
        runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER, runCommandFlowContext);

        runCommandFlowContext =
            common.runPipeOrganScenario(batLocation, batFile, scenarioFolderName,
                "Problem-BTServletDBTC.xml");
        runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER, runCommandFlowContext);

        log.info("Generating Problem - Sleeping for 6 mins");
        Thread.sleep(360000);

        // Check for story in REST API Response
        final String urlPart = "http://" + agcHost + ":8081/apm/appmap/private/triage/stories";

        // Fetch detailed story information
        AtStoryList stories = atStories.fetchStories(urlPart, DBTC_Start_Time, end_time, false);
        restTimestamp = common.timestamp2String(common.getCurrentTimeinISO8601Format(0));

        int dbtcCount = 0;

        // make sure stories non-empty
        if (stories == null || stories.getStories() == null || stories.getStories().isEmpty()) {
            fail("No suitable response for stories REST. Empty or null");
        } else {
            Iterator<AtStory> itStory = stories.getStories().iterator();
            // For each story find culprit type in patterns
            while (itStory.hasNext()) {
                AtStory storyLocation = itStory.next();

                String storyId = storyLocation.getStoryIds().toString();

                Long startTime = storyLocation.getStartTime();
                Long endTime = storyLocation.getEndTime();

                long startTimestamp = Long.MAX_VALUE;
                long endTimestamp = Long.MIN_VALUE;

                for (AnalystStatement stmnt : storyLocation.getEvidences()) {

                    EvidenceStatement evi = (EvidenceStatement) stmnt;
                    if (startTimestamp > evi.getFirstOccurrence()) {

                        startTimestamp = evi.getFirstOccurrence();
                    }
                    if (endTimestamp < evi.getLastOccurrence()) {

                        endTimestamp = evi.getLastOccurrence();
                    }
                }
                if (storyLocation.getPattern() != null) {
                    // in this pattern find type, if matches DbtcStatement
                    // continue.
                    PatternAnalystStatement currPattern = storyLocation.getPattern();
                    boolean patternType =
                        currPattern.getClass().getSimpleName().equalsIgnoreCase("DbtcStatement");

                    // If pattern is Dbtc Statement
                    if (patternType == true) {
                        log.info("Going to print logs for Test on Story ID : " + storyId);

                        DbtcStatement dbtcStmt = (DbtcStatement) currPattern;

                        // String confidence = dbtcStmt.getConfidence().toString();
                        String culpritId = dbtcStmt.getCulpritVertexIds().iterator().next();
                        String culpritName = dbtcStmt.getCulprit().getName();
                        String culpritAppName = dbtcStmt.getCulprit().getApplicationName();
                        String culpritType = dbtcStmt.getCulprit().getType();
                        int ratio = dbtcStmt.getRatio();
                        String calledCompId = dbtcStmt.getCalledCompVertexIds().iterator().next();
                        String calledCompName = dbtcStmt.getCalledComp().getName();
                        String calledCompAppName = dbtcStmt.getCalledComp().getApplicationName();
                        String calledCompType = dbtcStmt.getCalledComp().getType();
                        String calledCompPath = dbtcStmt.getCalledCompPaths().iterator().next();

                        // Checks accuracy of start and end time in story
                        common.verifyStoryTime(startTime, endTime);

                        // Check to see if culpritId is null, if true - fail
                        if (culpritId.isEmpty()) {
                            fail(" No Culprit Id found for story");
                        }
                        if (culpritName.isEmpty()) {
                            fail(" No Culprit Name found for story");
                        }

                        Map<String, String> culpritVertexInfo =
                            vertex.fetchVertexInfo(culpritId, restTimestamp, agcHost, false);
                        validateVertex.verifyCulpritInfo(culpritId, culpritName, culpritAppName,
                            culpritType, culpritVertexInfo);

                        // Verify Called component Info
                        if (calledCompId.isEmpty()) {
                            fail(" No Called Component Id found for story");
                        } else if (calledCompName.isEmpty()) {
                            fail(" No Called Component Name found for story");
                        } else if (calledCompPath.isEmpty()) {
                            fail(" No Called Component Path found for story");
                        }

                        Map<String, String> calledCompVertexInfo =
                            vertex.fetchVertexInfo(calledCompId, restTimestamp, agcHost, false);
                        verifyCalledCompInfo(calledCompId, calledCompName,
                            calledCompAppName, calledCompType, calledCompVertexInfo);
                        
                        // Find ratio for current story
                        int calculatedRatio =
                            fetchRatio(new Timestamp(startTimestamp), new Timestamp(endTimestamp),
                                calledCompPath, calledCompVertexInfo, culpritVertexInfo);

                        if (calculatedRatio == 0) {
                            log.info(" Unable to calculate ratio. One of the metric values is zero.");
                        }

                        log.info("Ratio calculated for time range: " + startTimestamp + " -> "
                            + endTimestamp);
                        // TODO: till we find a proper way to calculate ratio
                        if (calculatedRatio > 10) {
                            log.info("Test Pass : Calculated Ratio matches approx. to Story Ratio. "
                                + "Calculated Ratio : "
                                + calculatedRatio
                                + " Story Ratio : "
                                + ratio);
                        } else {
                            fail(" Ratio in DBTC Pattern in story didn't match. "
                                + "Calculated Ratio : " + calculatedRatio + " Story Ratio : "
                                + ratio);
                        }

                        // To be verified manually for now
                        // verifyCalledComponentEdge(startTimestamp, endTimestamp, culpritId,
                        // calledCompId);

                        // Check to see accuracy of Impacts and Potential Impacts
                        verifyBothImpactsAccuracy(storyLocation);
                        dbtcCount++;
                        log.info("Test Passed : Test for accuracy of DBTC Statement");
                    }

                } else {
                    fail(" Patterns is empty in a story. Shouldn't be possible. ");
                }
            }

            if (dbtcCount == 0) {
                fail(" New DBTC Analysts story failed. No BT's or Frontend found with DbtcStatement ");
            } else {
                log.info(dbtcCount
                    + " stories found with DbtcStatement. Test Completed. Test Passed.");
            }
        }
    }

    @SuppressWarnings("null")
    private void verifyCalledCompInfo(String calledCompId, String calledCompName,
        String calledCompAppName, String calledCompType, Map<String, String> calledCompVertexInfo) throws Exception {
        
        // Check to see if calledComp is null, if true - fail
        if (calledCompId.isEmpty()) {
            fail(" No Called Component ID found for evidence");
        } else {
            // Handles Culprit info verification
            String vertexName = calledCompVertexInfo.get("name");
            String vertexAppName = calledCompVertexInfo.get("applicationName");
            String vertexType = calledCompVertexInfo.get("type");
            String backendNode = calledCompVertexInfo.get("backendNode");
            
            // if backend node attributes exists then continue
            if(backendNode != null && !backendNode.isEmpty() && backendNode.equalsIgnoreCase("true")){
                
                String callCompVertexId = Integer.toString(vertex.getVertexIDfromExternalID(agcHost, ExternalId.fromString(calledCompId).getJustExternalId()));
                
                String inferredCompVertexId = Integer.toString(vertex.getEdgesFromVertexID(agcHost, callCompVertexId));
                
                if("0".equalsIgnoreCase(inferredCompVertexId)){
                    fail("ERROR : Inferred Backend Node Vertex ID not found in appmap_edges table for Called Component VertexID (backend node) : " + callCompVertexId + " ExternalID :" + calledCompId + " VertexName: " + calledCompName);
                }
                
                Map<String, String> inferredNodeVertexInfo =
                    vertex.fetchVertexInfo(inferredCompVertexId, restTimestamp, agcHost, true);
                
                String inferredVertexName = inferredNodeVertexInfo.get("name");
                
                if (inferredVertexName == null || inferredVertexName.isEmpty()) {
                    fail(" Inferred Node Name not found for Called Component ID: " + calledCompId + " . But Inferred VertexID was found : " + inferredCompVertexId);
                }
                
                // getting just the name of the node, as in dbtc logic
                int typeSeparator = inferredVertexName.indexOf(':');
                if (typeSeparator != -1) {
                    vertexName = inferredVertexName.substring(typeSeparator + 1).trim();
                }
            }

            // If calledComp name in AT Story Rest Response is null, fail
            if (calledCompName == null || calledCompName.isEmpty()) {
                fail(" Called Component Name not found for Called Component ID: " + calledCompId + " in AT Story");
            }

            // If calledComp app name in AT Story Rest Response is null, fail
            if (calledCompAppName == null || calledCompAppName.isEmpty()) {
                fail(" Called Component App Name not found for Called Component ID: " + calledCompId
                    + " and Called Component Name: " + calledCompName + " in AT Story");
            }

            // If calledComp type name in AT Story Rest Response is null, fail
            if (calledCompType == null || calledCompType.isEmpty()) {
                fail(" Called Component Type not found for Called Component ID: " + calledCompId + " and Called Component Name: "
                    + calledCompName + " in AT Story");
            }

            // If vertex name in AppMap Rest Response is null, fail
            if (vertexName == null) {
                fail(" Vertex Name not found for Called Component ID: " + calledCompId + " and Called Component Name: "
                    + calledCompName);
            }

            // If vertex type in AppMap Rest Response is null, fail
            if (vertexType == null) {
                fail(" Vertex type not found for Called Component ID: " + calledCompId + " and Called Component Type: "
                    + calledCompName);
            }

            if (vertexAppName == null || vertexAppName.isEmpty()) {
                log.info(" Vertex App Name not found for Called Component ID: " + calledCompId
                    + " and Called Component Name: " + calledCompName + ". Setting AppName to 'Unknown'.");
                vertexAppName = "Unknown";
            }

            // If vertex name matches, then log and continue else fail.
            if (calledCompName.equals(vertexName) && calledCompAppName.equalsIgnoreCase(vertexAppName)
                && calledCompType.equals(vertexType)) {
                log.info("Test Pass: Vertex Information found for Vertex ID: " + calledCompId
                    + " ; Vertex Name: " + vertexName + " ; Vertex App Name: " + vertexAppName
                    + " ; Vertex Type: " + vertexType);
            } else {
                fail("Failed to match Called Vertex Information for Called Component ID: " + calledCompId
                    + " ; Called Component Name: " + calledCompName + " and Vertex Name: " + vertexName
                    + " ; Called Component App Name: " + calledCompAppName + " and Vertex App Name: "
                    + vertexAppName + " ; Called Component Type: " + calledCompType + " and Vertex Type: "
                    + vertexType);
            }
        }
    }
        

    @Tas(testBeds = @TestBed(name = AssistedTriageTestbed.class, executeOn = AssistedTriageTestbed.SA_MASTER), size = SizeType.MEDIUM, owner = "katan09")
    @Test(groups = {"pipeorgan_who_feature"})
    private void testCase_DefaultAnalyst() throws Exception {
        /* Test Case for Default Analysts */

        // Start time to query AT Rest API
        DBTC_Start_Time = common.getCurrentTimeinISO8601Format(0);
        Timestamp end_time = common.getCurrentTimeinISO8601Format(2);

        // Enable problem scenario - Start problem scripts and wait for 5 mins
        runCommandFlowContext =
            common.runPipeOrganScenario(batLocation, batFile, scenarioFolderName,
                "Problem-DefaultAnalysts.xml");
        runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER, runCommandFlowContext);
        log.info("Generating Problem - Sleeping for 4 mins - 1min each for problem ramp up, alerts/uvb generation and track story");
        Thread.sleep(240000);

        // Check for story in REST API Response
        final String urlPart = "http://" + agcHost + ":8081/apm/appmap/private/triage/stories";

        // Fetch detailed story information
        AtStoryList stories = atStories.fetchStories(urlPart, DBTC_Start_Time, end_time, true);
        restTimestamp = common.timestamp2String(common.getCurrentTimeinISO8601Format(0));

        // make sure stories non-empty
        if (stories == null || stories.getStories() == null || stories.getStories().isEmpty()) {
            fail("No suitable response for stories REST. Empty or null");
        } else {
            int defaultCount = 0;

            Iterator<AtStory> itStory = stories.getStories().iterator();

            while (itStory.hasNext()) {
                AtStory storyLocation = itStory.next();

                String storyId = storyLocation.getStoryIds().toString();

                Long startTime = storyLocation.getStartTime();
                Long endTime = storyLocation.getEndTime();

                if (storyLocation.getPattern() != null) {

                    // in this pattern find type, if matches ProblemZoneStatement
                    // continue.
                    PatternAnalystStatement currPattern = storyLocation.getPattern();
                    boolean patternType =
                        currPattern.getClass().getSimpleName()
                            .equalsIgnoreCase("ProblemZoneStatement");

                    // If pattern is problem statement
                    if (patternType == true) {

                        log.info("Going to print logs for Test on Story ID : " + storyId);
                        ProblemZoneStatement problemStmt = (ProblemZoneStatement) currPattern;
                        String zone = problemStmt.getZone();

                        String culpritId = problemStmt.getCulpritVertexIds().iterator().next();
                        String culpritName = problemStmt.getCulprit().getName();
                        String culpritAppName = problemStmt.getCulprit().getApplicationName();
                        String culpritType = problemStmt.getCulprit().getType();
                        defaultCount++;

                        // Checks accuracy of start and end time in story
                        common.verifyStoryTime(startTime, endTime);

                        // Check to see if culpritId is null, if true - fail
                        if (culpritId.isEmpty()) {
                            fail(" No Suspect Id found for evidence");
                        } else {
                            Map<String, String> culpritVertexInfo =
                                vertex.fetchVertexInfo(culpritId, restTimestamp, agcHost, false);
                            validateVertex.verifyCulpritInfo(culpritId, culpritName,
                                culpritAppName, culpritType, culpritVertexInfo);

                            String vertexType = culpritVertexInfo.get("type");
                            String identifiedZone = verifyZone(vertexType);

                            // Check for zone in story depending on vertex type
                            if (zone.isEmpty()) {
                                fail(" Zone is empty for a ProblemZoneStatement Story. Shouldn't be possible.");
                            } else if (zone.equalsIgnoreCase(identifiedZone)) {
                                log.info("Test Pass 2: Zone matched for story. Story Zone: " + zone
                                    + " and Identified Zone: " + identifiedZone);
                            } else if (!zone.equalsIgnoreCase(identifiedZone)) {
                                fail(" Zone didn't match to culprit vertex type.  Story Zone: "
                                    + zone + " and Identified Zone: " + identifiedZone);
                            }

                            // Check to see accuracy of Impacts and Potential Impacts
                            verifyBothImpactsAccuracy(storyLocation);
                        }
                    }

                } else {
                    fail(" Patterns is empty in a story. Shouldn't be possible. ");
                }
            }

            if (defaultCount == 0) {
                fail(" New default analysts story failed. No BT's or Frontend found with ProblemZoneStatement ");
            } else {
                log.info(defaultCount
                    + " stories found with ProblemZoneStatement.Test Completed. Test Passed.");
            }
        }
    }

    @Tas(testBeds = @TestBed(name = AssistedTriageTestbed.class, executeOn = AssistedTriageTestbed.SA_MASTER), size = SizeType.MEDIUM, owner = "katan09")
    @Test(groups = {"pipeorgan_who_feature"})
    private void testCase_ProblemAnomaly() throws Exception {
        /* Test Case for Existence of Anomaly */

        // Start time to query AT Rest API
        DBTC_Start_Time = common.getCurrentTimeinISO8601Format(0);
        Timestamp end_time = common.getCurrentTimeinISO8601Format(2);

        // Enable problem - Start problem scripts and wait for 5 mins
        runCommandFlowContext =
            common.runPipeOrganScenario(batLocation, batFile, scenarioFolderName,
                "Problem-NoBTDefaultAnalysts.xml");
        runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER, runCommandFlowContext);

        runCommandFlowContext =
            common.runPipeOrganScenario(batLocation, batFile, scenarioFolderName,
                "Problem-BTWebserviceDBTC.xml");
        runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER, runCommandFlowContext);

        log.info("Generating Problem - Sleeping for 3 mins - 1min each for problem ramp up, alerts/uvb generation and track story");
        Thread.sleep(180000);

        // Check for story in REST API Response
        final String urlPart = "http://" + agcHost + ":8081/apm/appmap/private/triage/stories";

        // Fetch detailed story information
        AtStoryList stories = atStories.fetchStories(urlPart, DBTC_Start_Time, end_time, false);
        restTimestamp = common.timestamp2String(common.getCurrentTimeinISO8601Format(0));

        int anomalyCount = 0;
        int problemCount = 0;

        // make sure stories non-empty
        if (stories == null || stories.getStories() == null || stories.getStories().isEmpty()) {
            fail("No suitable response for stories REST. Empty or null");
        } else {
            Iterator<AtStory> itStory = stories.getStories().iterator();
            // For each story get impacts and potential impacts

            while (itStory.hasNext()) {
                AtStory storyLocation = itStory.next();
                String storyId = storyLocation.getStoryIds().toString();
                log.info("Going to print logs for Test on Story ID : " + storyId);

                // Check to see accuracy of Impacts and Potential Impacts
                Map<String, Integer> temp = verifyBothImpactsAccuracy(storyLocation);
                anomalyCount = anomalyCount + temp.get("anomalyCount");
                problemCount = problemCount + temp.get("problemCount");
            }

            if (anomalyCount == 0) {
                fail(" Test for anomalies failed. No story found as a anomaly. ");
            } else {
                log.info(anomalyCount + " stories found are anomalies.Test Completed. Test Passed.");
            }

            if (problemCount == 0) {
                fail(" Test for Problem Scenario failed. No story found as problem scenario. ");
            } else {
                log.info(problemCount + " stories found are problems.Test Completed. Test Passed.");
            }
        }
    }

    // Takes input as vertex type, identifies zone for that vertex and returns identifiesZone for
    // storyZone comparison.
    private String verifyZone(String vertexType) {

        String identifiedZone = null;
        List<String> frontend =
            Arrays.asList(VertexType.Type.APPLICATION_ENTRYPOINT.toString(),
                VertexType.Type.GENERICFRONTEND.toString());
        String businessTransaction = VertexType.Type.BUSINESSTRANSACTION.toString();
        List<String> supported_backends =
            Arrays.asList(VertexType.Type.DATABASE.toString(),
                VertexType.Type.GENERICBACKEND.toString(), VertexType.Type.SOCKET.toString(),
                VertexType.Type.DATABASE_SOCKET.toString());

        // Assign a zone for comparison depending on vertex type
        if (frontend.contains(vertexType)) {
            identifiedZone = ZoneIdentifierAnalyst.FRONTEND_ZONE;

        } else if (businessTransaction.equalsIgnoreCase(vertexType)) {
            identifiedZone = ZoneIdentifierAnalyst.BUSINESS_TRANSACTION_ZONE;

        } else if (supported_backends.contains(vertexType)) {

            identifiedZone = ZoneIdentifierAnalyst.BACKEND_ZONE;

        } else {
            identifiedZone = ZoneIdentifierAnalyst.INTERNAL_COMPONENT_ZONE;
        }

        return identifiedZone;

    }

    /*
     * Take AtStory as input and verifies story Impact and Potential Impact accuracy.
     * Checks for impacts and potential impacts to be non-empty.
     * Checks to see the verify existence of impacted vertex ID.
     * Checks to see if impacts are mutually exclusive.
     * Checks for existence of impact to determine problem or anomaly.
     */
    private Map<String, Integer> verifyBothImpactsAccuracy(AtStory currStory) throws Exception {

        Map<String, Integer> returnValue = new HashMap<String, Integer>();;
        Collection<String> Impacts = currStory.getImpactVertexIds();

        boolean impactEmpty = false;
        int anomalyCount = 0;
        int problemCount = 0;

        if (Impacts == null) {
            impactEmpty = true;
        }

        if (impactEmpty) {
            // Impact is empty then its an anomaly
            log.info(" No Impacts found for story.");
            anomalyCount++;

        } else if (!impactEmpty) {
            // Impact is non-empty - Problem Scenario

            verifyImpacts(Impacts, currStory, IMPACT);
            problemCount++;

        }
        returnValue.put("anomalyCount", anomalyCount);
        returnValue.put("problemCount", problemCount);

        return returnValue;
    }

    /*
     * Takes input as impact, story Location, String for ImpactType (IMPACT) and verifies accuracy
     * of each impacted vertex within the list
     */
    private void verifyImpacts(Collection<String> impactList, AtStory storyLocation,
        String impactType) throws Exception {

        Iterator<String> itImpact = impactList.iterator();

        while (itImpact.hasNext()) {
            String currImpact = itImpact.next();
            Collection<AnalystStatement> evidences = storyLocation.getEvidences();

            Map<String, String> impactInfo =
                vertex.fetchVertexInfo(currImpact, restTimestamp, agcHost, false);

            if (impactInfo.isEmpty()) {
                fail(" No information for impact vertex found. Impact : " + currImpact);
            }
            // String impactVertexType = impactInfo.get("type");

            // Check for evidences for Impacts
            boolean impactFoundinEvidences = verifyImpactedEvidence(currImpact, evidences);

            if (!impactFoundinEvidences && impactType.equals(IMPACT)) {
                fail(" Impact '" + currImpact
                    + "' not found in Evidences. Shouldn't be an impact then.");
            } else if (impactFoundinEvidences && impactType.equals(IMPACT)) {
                log.info(" Impact found an evidence related to it. ");
            }

            // To do: Verify is the impacted vertex is left most component here.

            verifyLMC(currImpact);
        }
    }


    // Verifies if the impacted component is indeed the Left most component(LMC)
    private void verifyLMC(String currImpact)
    {

        boolean foundEdgeFlag = true;
        try
        {
            String currImpactVertexId = Integer.toString(vertex
                    .getVertexIDfromExternalID(agcHost, ExternalId.fromString(currImpact).getJustExternalId()));
            
            if ("0".equals(currImpactVertexId)) {
                log.info("ERROR : VertexId not found in appmap_id_mappings table for ExternalId : "
                         + currImpact);
            }

            Map<String, String> currSourceInfo = vertex.fetchVertexInfo(currImpactVertexId, restTimestamp, agcHost, true);

            if ("Yes".equalsIgnoreCase(currSourceInfo
                    .get(Vertex.MainAttribute.ATTRIBUTE_NAME_IS_EXPERIENCE
                            .getName()))) {
                log.info("The isExprience property is true. So current target is LMC. Vertex"
                         + currImpact);
                foundEdgeFlag = false;
                
            } else {
                fail("Current node is not Exprience Tile Enabled. Current impact it not LMC. Impacted Verted (target) : "
                     + currImpact);
            }

            if (foundEdgeFlag == false) {
                log.info("Ipmacted vertex is left most component : "
                         + currImpact + " VertexId: " + currImpactVertexId);
            } else if (foundEdgeFlag == true) {
                fail("No edge found for impacted vertex or impacted vertex is not the left most component : "
                     + currImpact + " VertexId: " + currImpactVertexId);
            }

        } catch (Exception e) {
            fail("Unexpected exception " + e.getMessage()
                 + ". Rest call to AppMap didn't go through.");
        }
    }

    /*
     * Takes currImpact vertex Id and evidences as input, loops through evidences and matches
     * vertexID to impact vertex ID.
     * If found, return true, else return false
     */
    private boolean verifyImpactedEvidence(String currImpact, Collection<AnalystStatement> evidences) {

        boolean impactFlag = false;

        if (evidences == null) {
            fail(" Evidences cannot be null ");
        } else {
            Iterator<AnalystStatement> itEvidences = evidences.iterator();

            // Iterate over evidences, find suspectId/impactedId and match it current
            // Impact/Potential Impact vertex
            while (itEvidences.hasNext()) {
                AnalystStatement currEvidence = itEvidences.next();

                String vertexID = null;

                EvidenceStatement event = (EvidenceStatement) currEvidence;
                vertexID = event.getSuspect().getVertexId();

                if (currImpact.equalsIgnoreCase(vertexID)) {
                    impactFlag = true;
                }
            }
        }
        return impactFlag;
    }

    /*
     * Fetches Metric information for both Culprit and Called Component. Compares the metrics and
     * returns ratio.
     * Input : Story Start time, Story End time, DBTC Statement
     * Returns int Ratio
     */
    private int fetchRatio(Timestamp startTimestamp, Timestamp endTimestamp, String calledCompPath,
        Map<String, String> calledCompVertexInfo, Map<String, String> culpritVertexInfo)
        throws Exception {

        int returnValue = 0;

        // Calculate time difference to get range
        Long rangeMillisec = endTimestamp.getTime() - startTimestamp.getTime();

        // Check for story in REST API Response
        final String urlPartMetrics = "http://" + agcHost + ":8081/apm/appmap/private/metric";

        String agentSpecifier = calledCompVertexInfo.get("agent");

        String[] stringParts = calledCompPath.split("\\|", 5);
        String metricSpecifier = stringParts[4];
        
        String payload =
            fetchMetrics.metricPayload(agentSpecifier, metricSpecifier, EXACT, momHost,
                rangeMillisec, endTimestamp);
        log.info("Logging Metric Payload--");
        log.info(payload);

        Map<Long, Double> calledComponentMetric =
            getCurrentComponentMetric(urlPartMetrics, payload);

        agentSpecifier = culpritVertexInfo.get("agent");

        String metricType = AnalystHelperServices.RESP_PER_INT;
        Specifier specifier = fetchMetrics.getMetricSpecifier(culpritVertexInfo, metricType);
        String metricCallType = specifier.getType().toString();

        metricSpecifier = specifier.getSpecifier();
        payload =
            fetchMetrics.metricPayload(agentSpecifier, metricSpecifier, metricCallType, momHost,
                rangeMillisec, endTimestamp);
        log.info("Logging Metric Payload--");
        log.info(payload);

        Map<Long, Double> culpritVertexMetric = getCurrentComponentMetric(urlPartMetrics, payload);

        returnValue = fetchMetrics.calculateRatio(culpritVertexMetric, calledComponentMetric);

        return returnValue;
    }

    // Take payload and url as input and return metrics for the component in MetricValues format
    private Map<Long, Double> getCurrentComponentMetric(String urlPartMetrics, String payload)
        throws IOException {

        List<MetricValues> currMetricList =
            fetchMetrics.fetchMetricsFromPayload(urlPartMetrics, payload);

        // Check to see if metric information was found
        if (currMetricList.isEmpty()) {
            fail(" Metric for payload not found " + payload);
            return null;
        }

        MetricValues currentMetrics = currMetricList.iterator().next();

        Map<Long, Double> returnValue = fetchMetrics.getTimeslicedResults(currentMetrics);

        return returnValue;
    }
}
