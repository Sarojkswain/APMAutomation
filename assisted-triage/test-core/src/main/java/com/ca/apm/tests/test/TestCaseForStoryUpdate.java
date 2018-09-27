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
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.classes.from.appmap.plugin.AtStory;
import com.ca.apm.classes.from.appmap.plugin.AtStoryList;
import com.ca.apm.classes.from.appmap.plugin.PatternAnalystStatement;
import com.ca.apm.classes.from.appmap.plugin.ProblemZoneStatement;
import com.ca.apm.tests.testbed.AssistedTriageTestbed;
import com.ca.apm.tests.utils.AlertUtils;
import com.ca.apm.tests.utils.Common;
import com.ca.apm.tests.utils.FetchATStories;
import com.ca.apm.tests.utils.FetchAttributeInfo;
import com.ca.apm.tests.utils.LocalStorage;
import com.ca.apm.tests.utils.ValidateVertexInfo;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;


/**
 * TestCaseForStoryUpdate Test class to check for story updates scenario in
 * PipeOrgan App.
 *
 */

public class TestCaseForStoryUpdate extends TasTestNgTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Common common = new Common();
    private ValidateVertexInfo validateVertex = new ValidateVertexInfo();

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

    private String IMPACT = "impact";
    private String NOIMPACT = "noImpact";
    private String FINALNOIMPACT = "finalNoImpact";

    private String restTimestamp = null;

    @BeforeMethod
    public void initTestMethod(Method testMethod) {
        @SuppressWarnings("unused")
        LocalStorage localStorage = new LocalStorage(testMethod);
    }

    @Tas(testBeds = @TestBed(name = AssistedTriageTestbed.class, executeOn = AssistedTriageTestbed.SA_MASTER), size = SizeType.MEDIUM, owner = "katan09")
    @Test(groups = {"pipeorgan_merge_feature"})
    private void testCase_StoryUpdate() throws Exception {
        /*
         * Test Case - Test Case for Story Update.
         * Create a scenario on ExecutorServlet_14, ExecutorServlet_13 and User_Updated BT.
         * Trigger alerts on ExecutorServlet_14, ExecutorServlet_13 and verifies if story gets
         * created with problem as ExecutorServlet_14 and no impacted component.
         * Then fires alert on BT (Left most component) and checks if impact vertex is now BT.
         * Stops alerts on BT (LMC) and check if impacted vertex is again null, and story got
         * update.
         * If everything passes, test passes.
         */

        // Start time to query AT Rest API
        Start_Time = common.getCurrentTimeinISO8601Format(0);
        Timestamp end_time = common.getCurrentTimeinISO8601Format(2);
        final String urlPart = "http://" + agcHost + ":8081/apm/appmap/private/triage/stories";


        // Enable alerts on ExecutorServlet_14, ExecuotrServlet_13 and BT and wait 20 seconds
        runCommandFlowContext =
            alerts.statusAlertCLW("enable", momHost, "Story Update ART", "AT Automation MM");
        runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER, runCommandFlowContext);
        Thread.sleep(20000);


        log.info("Generating Problem on ExecutorServlet_14 and ExecutorServlet_13 - Sleeping for 2 mins 30 seconds");
        Thread.sleep(150000);

        // Fetch detailed story information
        AtStoryList stories = atStories.fetchStories(urlPart, Start_Time, end_time, true);
        restTimestamp = common.timestamp2String(common.getCurrentTimeinISO8601Format(0));

        // verify story has culprit and no impactR
        verifyStoryHasNoImpact(stories, NOIMPACT);


        // Enable problem on BT and wait 20 seconds
        runCommandFlowContext =
            common.runPipeOrganScenario(batLocation, batFile, scenarioFolderName,
                "Problem-StoryUpdated.xml");
        runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER, runCommandFlowContext);
        Thread.sleep(30000);


        log.info("Generating Problem on BT along with problem occurring on ExecutorServlet_14 and ExecutorServlet_13 - Sleeping for 3 mins");
        Thread.sleep(180000);

        // Fetch detailed story information
        stories = atStories.fetchStories(urlPart, Start_Time, end_time, true);
        restTimestamp = common.timestamp2String(common.getCurrentTimeinISO8601Format(0));

        // verify story has culprit and no impact
        verifyStoryHasNoImpact(stories, IMPACT);

        log.info("Waiting for problem to clear on BT and keep happening on ExecutorServlet_14 and ExecuotrServlet_13 - Sleeping for 5 mins");
        Thread.sleep(300000);

        // Fetch detailed story information
        stories = atStories.fetchStories(urlPart, Start_Time, end_time, true);
        restTimestamp = common.timestamp2String(common.getCurrentTimeinISO8601Format(0));

        // verify story has culprit and no impact
        int storyCount = verifyStoryHasNoImpact(stories, FINALNOIMPACT);

        runCommandFlowContext =
            alerts.statusAlertCLW("disable", momHost, "Story Update ART", "AT Automation MM");
        runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER, runCommandFlowContext);

        if (storyCount == 0)
            fail("No story found for Update story. Test failed.");
        else
            log.info("Test for Story Update Passed");

    }

    // Fetches story and verifies if story has no impact component
    private int verifyStoryHasNoImpact(AtStoryList stories, String impactCheck)
        throws Exception {

        int count = 0;

        if (stories.getStories() == null || stories.getStories().isEmpty()) {
            fail(" No new stories created. ");
        } else {
            Iterator<AtStory> itStory = stories.getStories().iterator();

            while (itStory.hasNext()) {
                AtStory storyLocation = itStory.next();
                String storyId = storyLocation.getStoryIds().toString();

                Long startTime = storyLocation.getStartTime();
                Long endTime = storyLocation.getEndTime();

                // Checks accuracy of start and end time in story
                common.verifyStoryTime(startTime, endTime);

                if (storyLocation.getPattern() == null) {
                    fail(" Patterns is empty in a story. Shouldn't be possible. ");
                } else {

                    // in this pattern find type, if matches ProblemZoneStatement
                    // continue.
                    PatternAnalystStatement currPattern = storyLocation.getPattern();
                    boolean patternType =
                        currPattern.getClass().getSimpleName()
                            .equalsIgnoreCase("ProblemZoneStatement");

                    // If pattern is problem statement
                    if (patternType == true) {

                        ProblemZoneStatement problemStmt = (ProblemZoneStatement) currPattern;

                        String culpritName = problemStmt.getCulprit().getName();
                        if (culpritName.isEmpty()) {
                            fail(" No Culprit Name found for story ID " + storyId);
                        }

                        if (culpritName.contains("ExecutorServlet_14|service")) {
                            log.info("Going to check current story for story updates : " + storyId
                                + " , Printing logs:");
                            count++;

                            String culpritId = problemStmt.getCulpritVertexIds().iterator().next();
                            String culpritAppName = problemStmt.getCulprit().getApplicationName();
                            String culpritType = problemStmt.getCulprit().getType();

                            // Check to see if culpritId is null, if true - fail
                            if (culpritId.isEmpty()) {
                                fail(" No Culprit Id found for story");
                            }

                            Map<String, String> culpritVertexInfo =
                                vertex.fetchVertexInfo(culpritId, restTimestamp, agcHost, false);
                            validateVertex.verifyCulpritInfo(culpritId, culpritName,
                                culpritAppName, culpritType, culpritVertexInfo);

                            Collection<String> impacts = storyLocation.getImpactVertexIds();

                            switch (impactCheck) {
                                case "impact":
                                    if (impacts == null || impacts.isEmpty()) {
                                        fail("Expecting an impact on BT. Story not updated. Step 2 failed.");
                                    } else {
                                        log.info("Impact on story found. Story Updated. Impacted component : "
                                            + impacts.toString() + ". Step 2 Passed.");
                                    }
                                    break;

                                case "noImpact":
                                    if (impacts != null)
                                        fail("Impact on BT found. Expecting no impact on left most component. Story not updated. Step 1 failed. Impacted Component : "
                                            + impacts.toString());
                                    else
                                        log.info("Impact on story NOT found. Story created with no impact. Step 1 Passed.");
                                    break;


                                case "finalNoImpact":
                                    if (impacts != null)
                                        fail("Impact on BT found. Expecting no impact on left most component. Story not updated. Step 3 failed. Impacted Component : "
                                            + impacts.toString());
                                    else
                                        log.info("Impact on story NOT found. Story Updated with no impact. Step 3 Passed. Test for Story Update Passed.");
                                    break;
                            }

                        }
                    }
                }
            }
        }
        return count;
    }
}
