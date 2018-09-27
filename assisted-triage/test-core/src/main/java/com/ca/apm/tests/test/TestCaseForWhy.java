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

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.classes.from.appmap.plugin.AlertEventStatement;
import com.ca.apm.classes.from.appmap.plugin.AnalystStatement;
import com.ca.apm.classes.from.appmap.plugin.AtStory;
import com.ca.apm.classes.from.appmap.plugin.AtStoryList;
import com.ca.apm.classes.from.appmap.plugin.ErrorEventStatement;
import com.ca.apm.classes.from.appmap.plugin.StallEventStatement;
import com.ca.apm.classes.from.appmap.plugin.UvbEventStatement;
import com.ca.apm.tests.testbed.AssistedTriageTestbed;
import com.ca.apm.tests.utils.AlertUtils;
import com.ca.apm.tests.utils.Common;
import com.ca.apm.tests.utils.FetchATStories;
import com.ca.apm.tests.utils.FetchAttributeInfo;
import com.ca.apm.tests.utils.LocalStorage;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;


/**
 * DBTCTestOnPipeOrgan Test class to check for DBTC scenario in
 * PipeOrgan App.
 *
 */

public class TestCaseForWhy extends TasTestNgTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Common common = new Common();

    private FetchAttributeInfo vertex = new FetchAttributeInfo();
    private RunCommandFlowContext runCommandFlowContext;
    private FetchATStories atStories = new FetchATStories();
    private AlertUtils alerts = new AlertUtils();

    private static Timestamp Start_Time;

    private String agcHost = envProperties
        .getMachineHostnameByRoleId(AssistedTriageTestbed.SA_MASTER_EM_ROLE);
    private String momHost = envProperties
        .getMachineHostnameByRoleId(AssistedTriageTestbed.MOM_PROVIDER_EM_ROLE);

    private String batFile = "run.bat";
    private String batLocation = AssistedTriageTestbed.TOMCAT_INSTALL_DIR
        + "\\webapps\\pipeorgan\\WEB-INF\\lib\\";

    private String scenarioFolderName = "scenarios";
    private String restTimestamp = null;

    @BeforeMethod
    public void initTestMethod(Method testMethod) {
        @SuppressWarnings("unused")
        LocalStorage localStorage = new LocalStorage(testMethod);
    }

    @Tas(testBeds = @TestBed(name = AssistedTriageTestbed.class, executeOn = AssistedTriageTestbed.SA_MASTER), size = SizeType.MEDIUM, owner = "katan09")
    @Test(groups = {"pipeorgan_why_feature"})
    private void testCase_AccUVB() throws Exception {
        /*
         * Test Case 1 - Test Case for UVB evidence.
         * Fetches AT Rest API Json reponse for stories. Gets suspect ID and name for UVB evidence.
         * Fetches information of vertex ID from Rest AppMap API and checks for information against
         * AT story.
         */

        // Start time to query AT Rest API
        Start_Time = common.getCurrentTimeinISO8601Format(0);
        Timestamp end_time = common.getCurrentTimeinISO8601Format(2);

        // Enable dbtc - Start problem scripts and wait for 5 mins
        runCommandFlowContext =
            common.runPipeOrganScenario(batLocation, batFile, scenarioFolderName,
                "Problem-BTServletDBTC.xml");
        runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER, runCommandFlowContext);

        log.info("Generating Problem - Sleeping for 6 mins");
        Thread.sleep(360000);

        // Check for story in REST API Response
        final String urlPart = "http://" + agcHost + ":8081/apm/appmap/private/triage/stories";

        // Fetch detailed story information
        AtStoryList stories = atStories.fetchStories(urlPart, Start_Time, end_time, true);
        restTimestamp = common.timestamp2String(common.getCurrentTimeinISO8601Format(0));

        boolean failTest = false;

        // For each story find evidences
        if (stories.getStories() == null || stories.getStories().isEmpty()) {
            fail(" No new stories created ");
        } else {
            Iterator<AtStory> itStory = stories.getStories().iterator();

            while (itStory.hasNext()) {
                AtStory storyLocation = itStory.next();
                String storyId = storyLocation.getStoryIds().toString();

                if (storyLocation.getEvidences() == null || storyLocation.getEvidences().isEmpty()) {
                    fail(" Evidences missing for Story Id : " + storyId);
                }

                Iterator<AnalystStatement> itEvidences = storyLocation.getEvidences().iterator();

                // Iterate over evidences, find suspectId and suspectName for all UVB evidences and
                // call AppMapRest API to verify details
                while (itEvidences.hasNext()) {
                    AnalystStatement currEvidence = itEvidences.next();
                    boolean uvbType =
                        currEvidence.getClass().getSimpleName()
                            .equalsIgnoreCase("UvbEventStatement");

                    // If evidence is UVB continue
                    if (uvbType == true) {
                        log.info("Going to print logs for Test on Story ID : " + storyId);

                        UvbEventStatement uvb = (UvbEventStatement) currEvidence;
                        String suspectName = uvb.getSuspect().getName();
                        String suspectId = uvb.getSuspect().getVertexId();
                        Long firstOccurrence = uvb.getFirstOccurrence();
                        Long lastOccurrence = uvb.getLastOccurrence();

                        // Check to see if Origin Time occurs on or before End Time
                        if (firstOccurrence == null) {
                            fail(" Start Time of evidence cannot be null ");
                        } else if (lastOccurrence == null) {
                            fail(" End time of evidence cannot be null ");
                        } else if (firstOccurrence > lastOccurrence) {
                            fail(" Test Failed on UVB Evidence, Origin Time is greater than End Time - Impacted Vertex Id : "
                                + suspectId
                                + " Origin Time : "
                                + firstOccurrence
                                + " End Time : "
                                + lastOccurrence);
                        } else {
                            // Check to see if suspectId is null, if true - fail
                            if (suspectId.isEmpty()) {
                                fail(" No Suspect Id found for evidence");
                            }
                            // Check to see if suspectName is null, if true - fail
                            else if (suspectName.isEmpty()) {
                                fail(" No Suspect Name found for evidence");
                            } else {
                                // Handles Vertex info verification
                                Map<String, String> vertexInfo =
                                    vertex.fetchVertexInfo(suspectId, restTimestamp, agcHost, false);
                                String vertexName = vertexInfo.get("name");

                                // If vertex name in AppMap Rest Response is null, fail
                                if (vertexName.isEmpty()) {
                                    fail(" Vertex Information not found for Suspect ID "
                                        + suspectId + " and Suspect Name " + suspectName);
                                }

                                // If vertex name matches, then log and continue else fail.
                                if (suspectName.equals(vertexName)) {
                                    log.info("Test Pass 1: Vertex Information found for Vertex ID "
                                        + suspectId + " and Vertex Name " + vertexName);
                                } else {
                                    fail("Failed to match Suspect Information. Suspect ID "
                                        + suspectId + " ; Suspect Name " + suspectName
                                        + " and Vertex Name " + vertexName);
                                }

                                log.info("Test Passed : Test for accuracy of UVB evidence");
                            }
                        }
                    }
                }
            }
        }

        if (failTest == true) {
            fail("Test Failed : Test for accuracy of UVB evidence. Check logs printed above.");
        }
    }

    @Tas(testBeds = @TestBed(name = AssistedTriageTestbed.class, executeOn = AssistedTriageTestbed.SA_MASTER), size = SizeType.MEDIUM, owner = "katan09")
    @Test(groups = {"pipeorgan_why_feature"})
    private void testCase_ErrorSnap() throws Exception {
        /*
         * Test Case 3 - Test Case for ErrorStatement evidence.
         * Fetches AT Rest API Json reponse for stories. Gets suspect ID and name for ErrorSnapshot
         * evidence.
         * Fetches information of vertex ID from Rest AppMap API and checks for information against
         * AT story.
         * Fetches Error Per Interval Metrics for suspect and verifies occurrence count.
         * Verifies Error Message is non-empty.
         */

        // Start time to query AT Rest API
        Start_Time = common.getCurrentTimeinISO8601Format(0);
        Timestamp end_time = common.getCurrentTimeinISO8601Format(2);

        // Enable dbtc - Start problem scripts and wait for 5 mins
        runCommandFlowContext =
            common.runPipeOrganScenario(batLocation, batFile, scenarioFolderName,
                "Problem-BTServletDBError.xml");
        runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER, runCommandFlowContext);
        Thread.sleep(500);

        runCommandFlowContext =
            common.runPipeOrganScenario(batLocation, batFile, scenarioFolderName,
                "Problem-BTServletDBError2.xml");
        runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER, runCommandFlowContext);

        log.info("Generating Problem - Sleeping for 3 mins");
        Thread.sleep(180000);

        // Check for story in REST API Response
        final String urlPart = "http://" + agcHost + ":8081/apm/appmap/private/triage/stories";

        // Fetch detailed story information
        AtStoryList stories = atStories.fetchStories(urlPart, Start_Time, end_time, true);
        restTimestamp = common.timestamp2String(common.getCurrentTimeinISO8601Format(0));

        int count = 0;

        // For each story find evidences
        if (stories.getStories() == null || stories.getStories().isEmpty()) {
            fail(" No new stories created ");
        } else {
            Iterator<AtStory> itStory = stories.getStories().iterator();

            while (itStory.hasNext()) {
                AtStory storyLocation = itStory.next();
                String storyId = storyLocation.getStoryIds().toString();

                if (storyLocation.getEvidences() == null || storyLocation.getEvidences().isEmpty()) {
                    fail(" Evidences missing for Story Id : " + storyId);
                }

                Iterator<AnalystStatement> itEvidences = storyLocation.getEvidences().iterator();

                // Iterate over evidences, find suspectId and suspectName for all error evidences
                // and
                // call AppMapRest API to verify details
                while (itEvidences.hasNext()) {
                    AnalystStatement currEvidence = itEvidences.next();
                    boolean errorType =
                        currEvidence.getClass().getSimpleName()
                            .equalsIgnoreCase("ErrorEventStatement");

                    // If evidence is errorEvent continue
                    if (errorType == true) {
                        log.info("Going to print logs for Test on Story ID : " + storyId);

                        ErrorEventStatement error = (ErrorEventStatement) currEvidence;
                        String suspectName = error.getSuspect().getName();
                        String suspectId = error.getSuspect().getVertexId();
                        Long firstOccurrence = error.getFirstOccurrence();
                        Long lastOccurrence = error.getLastOccurrence();
                        String errorMessage = error.getErrorMessages().toString();

                        // Check to see if Origin Time occurs on or before End Time
                        if (firstOccurrence == null) {
                            fail(" Start Time of evidence cannot be null ");
                        } else if (lastOccurrence == null) {
                            fail(" End time of evidence cannot be null ");
                        } else if (firstOccurrence > lastOccurrence) {
                            fail(" Test Failed on ErrorEventStatement Evidence, Origin Time is greater than End Time - Impacted Vertex Id : "
                                + suspectId
                                + " Origin Time : "
                                + firstOccurrence
                                + " End Time : "
                                + lastOccurrence);
                        }

                        // Check to see if suspectId is null, if true - fail
                        if (suspectId.isEmpty()) {
                            fail(" No Suspect Id found for evidence");
                        }
                        // Check to see if suspectName is null, if true - fail
                        else if (suspectName.isEmpty()) {
                            fail(" No Suspect Name found for evidence");
                        } else {
                            // Handles Vertex info verification
                            Map<String, String> vertexInfo =
                                vertex.fetchVertexInfo(suspectId, restTimestamp, agcHost, false);
                            String vertexName = vertexInfo.get("name");

                            // If vertex name in AppMap Rest Response is null, fail
                            if (vertexName.isEmpty()) {
                                fail(" Vertex Information not found for Suspect ID " + suspectId
                                    + " and Suspect Name " + suspectName);
                            }

                            // If vertex name matches, then log and continue else fail.
                            if (suspectName.equals(vertexName)) {
                                log.info("Test Pass 1: Vertex Information found for Vertex ID "
                                    + suspectId + " and Vertex Name " + vertexName);
                            } else {
                                fail("Failed to match Suspect Information. Suspect ID " + suspectId
                                    + " ; Suspect Name " + suspectName + " and Vertex Name "
                                    + vertexName);
                            }

                            if (errorMessage.isEmpty()) {
                                fail(" Error Message cannot be empty. ");
                            }

                            count++;
                        }
                    }
                }
            }
        }

        if (count == 0) {
            fail("Test Failed : Test for accuracy of ErrorEventStatement evidence. No evidences found with ErrorEventStatement.");
        }
    }

    @Tas(testBeds = @TestBed(name = AssistedTriageTestbed.class, executeOn = AssistedTriageTestbed.SA_MASTER), size = SizeType.MEDIUM, owner = "katan09")
    @Test(groups = {"pipeorgan_why_feature"})
    private void testCase_Alerts() throws Exception {
        /*
         * Test Case 4 - Test Case for AlertEvent evidence.
         * Enables custom alerts and disables default alerts then runs problemn scenario, after
         * finishing scenario diables custom alerts and enables default alerts.
         * Fetches AT Rest API Json reponse for stories. Gets suspect ID and name for Alerts
         * evidence.
         * Fetches information of vertex ID from Rest AppMap API and checks for information against
         * AT story.
         * Calculates occurence count and compares.
         */

        // start CLW enable alert
        runCommandFlowContext =
            alerts.statusAlertCLW("enable", momHost, "PipeOrgan", "AT Automation MM");
        runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER, runCommandFlowContext);

        runCommandFlowContext = alerts.statusDefaultAlertCLW("disable", momHost);
        runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER, runCommandFlowContext);
        Thread.sleep(20000);

        // Start time to query AT Rest API
        Start_Time = common.getCurrentTimeinISO8601Format(0);
        Timestamp end_time = common.getCurrentTimeinISO8601Format(2);

        // Enable alert scenario - Start problem scripts and wait for 5 mins
        runCommandFlowContext =
            common.runPipeOrganScenario(batLocation, batFile, scenarioFolderName,
                "Problem-BTAlert.xml");
        runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER, runCommandFlowContext);

        log.info("Generating Problem - Sleeping for 4 mins");
        Thread.sleep(240000);

        // Check for story in REST API Response
        final String urlPart = "http://" + agcHost + ":8081/apm/appmap/private/triage/stories";

        // Fetch detailed story information
        AtStoryList stories = atStories.fetchStories(urlPart, Start_Time, end_time, true);
        restTimestamp = common.timestamp2String(common.getCurrentTimeinISO8601Format(0));

        // Stop CLW disable alert
        runCommandFlowContext =
            alerts.statusAlertCLW("disable", momHost, "PipeOrgan", "AT Automation MM");
        runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER, runCommandFlowContext);

        runCommandFlowContext = alerts.statusDefaultAlertCLW("enable", momHost);
        runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER, runCommandFlowContext);
        Thread.sleep(30000);

        int count = 0;
        boolean failFlag = false;
        // For each story find evidences
        if (stories.getStories() == null || stories.getStories().isEmpty()) {
            fail(" No new stories created ");
        } else {
            Iterator<AtStory> itStory = stories.getStories().iterator();

            while (itStory.hasNext()) {
                AtStory storyLocation = itStory.next();
                String storyId = storyLocation.getStoryIds().toString();

                if (storyLocation.getEvidences() == null || storyLocation.getEvidences().isEmpty()) {
                    fail(" Evidences missing for Story Id : " + storyId);
                }

                Iterator<AnalystStatement> itEvidences = storyLocation.getEvidences().iterator();

                // Iterate over evidences, find suspectId and suspectName for all alert evidences
                // and
                // call AppMapRest API to verify details
                while (itEvidences.hasNext()) {
                    AnalystStatement currEvidence = itEvidences.next();
                    boolean alertType =
                        currEvidence.getClass().getSimpleName()
                            .equalsIgnoreCase("AlertEventStatement");

                    // If evidence is Alert continue
                    if (alertType == true) {
                        log.info("Going to print logs for Test on Story ID : " + storyId);

                        AlertEventStatement alert = (AlertEventStatement) currEvidence;
                        String suspectName = alert.getSuspect().getName();
                        String suspectId = alert.getSuspect().getVertexId();
                        Long firstOccurrence = alert.getFirstOccurrence();
                        Long lastOccurrence = alert.getLastOccurrence();

                        Set<String> alerts = alert.getAlerts();

                        log.info(" Suspect Name = " + suspectName + " SuspectId = " + suspectId
                            + " first occurrence = " + firstOccurrence + " last occurrence = "
                            + lastOccurrence + " alerts = " + alerts);


                        // Check to see if Origin Time occurs on or before End Time
                        if (firstOccurrence == null) {
                            fail(" Start Time of evidence cannot be null ");
                        } else if (lastOccurrence == null) {
                            fail(" End time of evidence cannot be null ");
                        } else if (firstOccurrence > lastOccurrence) {
                            fail(" Test Failed on AlertEventStatement Evidence, Origin Time is greater than End Time - Impacted Vertex Id : "
                                + suspectId
                                + " Origin Time : "
                                + firstOccurrence
                                + " End Time : "
                                + lastOccurrence);
                        } else {
                            // Check to see if suspectId is null, if true - fail
                            if (suspectId.isEmpty()) {
                                fail(" No Suspect Id found for evidence");
                            }
                            // Check to see if suspectName is null, if true - fail
                            else if (suspectName.isEmpty()) {
                                fail(" No Suspect Name found for evidence");
                            } else {
                                // Handles Vertex info verification
                                Map<String, String> vertexInfo =
                                    vertex.fetchVertexInfo(suspectId, restTimestamp, agcHost, false);
                                String vertexName = vertexInfo.get("name");

                                // If vertex name in AppMap Rest Response is null, fail
                                if (vertexName.isEmpty()) {
                                    fail(" Vertex Information not found for Suspect ID "
                                        + suspectId + " and Suspect Name " + suspectName);
                                }

                                // If vertex name matches, then log and continue else fail.
                                if (suspectName.equals(vertexName)) {
                                    log.info("Test Pass 1: Vertex Information found for Vertex ID "
                                        + suspectId + " and Vertex Name " + vertexName);
                                } else {
                                    fail("Failed to match Suspect Information. Suspect ID "
                                        + suspectId + " ; Suspect Name " + suspectName
                                        + " and Vertex Name " + vertexName);
                                }

                                if (alerts.isEmpty()) {
                                    fail(" Alert Message cannot be empty. ");
                                }

                                log.info("Test Passed : Test for accuracy of Alert evidence");

                                count++;
                            }
                        }
                    }
                }
            }
        }

        if (count == 0) {
            fail("Test Failed : Test for accuracy of AlertEventStatement evidence. No evidences found with AlertEventStatement.");
        }
        if (failFlag == true) {
            fail("Test Failed : Test for accuracy of AlertEventStatement evidence. Occurence count didn't match");
        }
    }

    @Tas(testBeds = @TestBed(name = AssistedTriageTestbed.class, executeOn = AssistedTriageTestbed.SA_MASTER), size = SizeType.MEDIUM, owner = "katan09")
    @Test(groups = {"pipeorgan_why_feature"})
    private void testCase_Stall() throws Exception {
        /*
         * Test Case 5 - Test Case for StallEvent evidence.
         * Verifies if supportability metrics are not null.
         * Fetches AT Rest API Json reponse for stories. Gets suspect ID and name for Alerts
         * evidence.
         * Fetches information of vertex ID from Rest AppMap API and checks for information against
         * AT story.
         * Verifies is stall occured on a particular component
         */

        // Start time to query AT Rest API
        Start_Time = common.getCurrentTimeinISO8601Format(0);
        Timestamp end_time = common.getCurrentTimeinISO8601Format(2);

        // Enable stall scenario - Start problem scripts and wait for 5 mins
        runCommandFlowContext =
            common.runPipeOrganScenario(batLocation, batFile, scenarioFolderName,
                "Problem-BTStall.xml");
        runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER, runCommandFlowContext);

        log.info("Generating Problem - Sleeping for 3 mins");
        Thread.sleep(180000);

        // Check for story in REST API Response
        final String urlPart = "http://" + agcHost + ":8081/apm/appmap/private/triage/stories";

        // Fetch detailed story information
        AtStoryList stories = atStories.fetchStories(urlPart, Start_Time, end_time, true);
        restTimestamp = common.timestamp2String(common.getCurrentTimeinISO8601Format(0));

        int count = 0;

        // For each story find evidences
        if (stories.getStories() == null || stories.getStories().isEmpty()) {
            fail(" No new stories created ");
        } else {
            Iterator<AtStory> itStory = stories.getStories().iterator();

            while (itStory.hasNext()) {
                AtStory storyLocation = itStory.next();
                String storyId = storyLocation.getStoryIds().toString();

                if (storyLocation.getEvidences() == null || storyLocation.getEvidences().isEmpty()) {
                    fail(" Evidences missing for Story Id : " + storyId);
                }

                Iterator<AnalystStatement> itEvidences = storyLocation.getEvidences().iterator();

                // Iterate over evidences, find suspectId and suspectName for all stall evidences
                // and call AppMapRest API to verify details
                while (itEvidences.hasNext()) {
                    AnalystStatement currEvidence = itEvidences.next();
                    boolean stallType =
                        currEvidence.getClass().getSimpleName()
                            .equalsIgnoreCase("StallEventStatement");

                    // If evidence is Stall continue
                    if (stallType == true) {
                        log.info("Going to print logs for Test on Story ID : " + storyId);

                        StallEventStatement stall = (StallEventStatement) currEvidence;
                        String suspectName = stall.getSuspect().getName();
                        String suspectId = stall.getSuspect().getVertexId();
                        Long firstOccurrence = stall.getFirstOccurrence();
                        Long lastOccurrence = stall.getLastOccurrence();
                        Set<String> stallComponents = stall.getStalledComponents();

                        // Check to see if Origin Time occurs on or before End Time
                        if (firstOccurrence == null) {
                            fail(" Start Time of evidence cannot be null ");
                        } else if (lastOccurrence == null) {
                            fail(" End time of evidence cannot be null ");
                        } else if (firstOccurrence > lastOccurrence) {
                            fail(" Test Failed on StallEventStatement Evidence, Origin Time is greater than End Time - Impacted Vertex Id : "
                                + suspectId
                                + " Origin Time : "
                                + firstOccurrence
                                + " End Time : "
                                + lastOccurrence);
                        } else {
                            // Check to see if suspectId is null, if true - fail
                            if (suspectId.isEmpty()) {
                                fail(" No Suspect Id found for evidence");
                            }
                            // Check to see if suspectName is null, if true - fail
                            else if (suspectName.isEmpty()) {
                                fail(" No Suspect Name found for evidence");
                            } else {
                                // Handles Vertex info verification
                                Map<String, String> vertexInfo =
                                    vertex.fetchVertexInfo(suspectId, restTimestamp, agcHost, false);
                                String vertexName = vertexInfo.get("name");

                                // If vertex name in AppMap Rest Response is null, fail
                                if (vertexName.isEmpty()) {
                                    fail(" Vertex Information not found for Suspect ID "
                                        + suspectId + " and Suspect Name " + suspectName);
                                }

                                // If vertex name matches, then log and continue else fail.
                                if (suspectName.equals(vertexName)) {
                                    log.info("Test Pass 1: Vertex Information found for Vertex ID "
                                        + suspectId + " and Vertex Name " + vertexName);
                                } else {
                                    fail("Failed to match Suspect Information. Suspect ID "
                                        + suspectId + " ; Suspect Name " + suspectName
                                        + " and Vertex Name " + vertexName);
                                }

                                if (stallComponents.isEmpty()) {
                                    fail(" Stall Component cannot be empty. ");
                                }

                                count++;
                            }
                        }
                    }
                }
            }
        }

        if (count == 0) {
            fail("Test Failed : Test for accuracy of StallEventStatement evidence. No evidences found with StallEventStatement.");
        }
    }
}
