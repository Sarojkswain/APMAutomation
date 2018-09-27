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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
 * TestCaseForMerge class to check for deployment merge and problem merge scenario in
 * PipeOrgan App.
 *
 */

public class TestCaseForMerge extends TasTestNgTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Common common = new Common();
    private ValidateVertexInfo validateVertex = new ValidateVertexInfo();

    private FetchAttributeInfo vertex = new FetchAttributeInfo();
    private RunCommandFlowContext runCommandFlowContext;
    private FetchATStories atStories = new FetchATStories();

    private static Timestamp Start_Time;

    private String agcHost = envProperties
        .getMachineHostnameByRoleId(AssistedTriageTestbed.SA_MASTER_EM_ROLE);

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
    @Test(groups = {"pipeorgan_merge_feature"})
    private void testCase_DeploymentMerge() throws Exception {
        /*
         * Test Case 1 - Test Case for Deployment Merging.
         * Triggers stall on ExecutorServlet_12 within the scenario.
         * Checks received ProblemZone Statement stories in Rest to see if more than one culprit Ids
         * are received.
         * If found, for every vertexId verifies Vertex Name, Application Name and Vertex Type.
         * If all succeeds passes test.
         */

        // Start time to query AT Rest API
        Start_Time = common.getCurrentTimeinISO8601Format(0);
        Timestamp end_time = common.getCurrentTimeinISO8601Format(2);

        // Enable stall on ExecutorServlet_12 - Start problem scripts and wait for 6 mins
        runCommandFlowContext =
            common.runPipeOrganScenario(batLocation, batFile, scenarioFolderName,
                "Problem-DeploymentMerge.xml");
        runCommandFlowByMachineId(AssistedTriageTestbed.TOMCAT_AGENT_ONLY, runCommandFlowContext);

        runCommandFlowContext =
            common.runPipeOrganScenario(batLocation, batFile, scenarioFolderName,
                "Problem-DeploymentMerge.xml");
        runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER, runCommandFlowContext);


        log.info("Generating Problem - Sleeping for 6 mins");
        Thread.sleep(360000);

        // Check for story in REST API Response
        final String urlPart = "http://" + agcHost + ":8081/apm/appmap/private/triage/stories";

        // Fetch detailed story information
        AtStoryList stories = atStories.fetchStories(urlPart, Start_Time, end_time, true);
        restTimestamp = common.timestamp2String(common.getCurrentTimeinISO8601Format(0));

        int storyCount = 0;

        // For each story find evidences
        if (stories.getStories().isEmpty()) {
            fail(" No new stories created ");
        } else {
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

                        // Checks accuracy of start and end time in story
                        common.verifyStoryTime(startTime, endTime);

                        Collection<String> culpritIds = problemStmt.getCulpritVertexIds();
                        int culpritSize = culpritIds.size();
                        log.info("Culprit Ids for story : " + culpritIds);

                        if (culpritSize > 1) {
                            // deployment merge story found continue
                            Iterator<String> culpritIterator = culpritIds.iterator();
                            storyCount++;

                            while (culpritIterator.hasNext()) {

                                String culpritId = culpritIterator.next();
                                String culpritName = problemStmt.getCulprit().getName();
                                String culpritAppName =
                                    problemStmt.getCulprit().getApplicationName();
                                String culpritType = problemStmt.getCulprit().getType();

                                // Check to see if culpritId is null, if true - fail
                                if (culpritId.isEmpty()) {
                                    fail(" No culprit Id found for evidence");
                                } else {
                                    Map<String, String> culpritVertexInfo =
                                        vertex.fetchVertexInfo(culpritId, restTimestamp, agcHost, false);
                                    validateVertex.verifyCulpritInfo(culpritId, culpritName,
                                        culpritAppName, culpritType, culpritVertexInfo);
                                }
                            }
                        }
                    }

                } else {
                    fail(" Patterns is empty in a story. Shouldn't be possible. ");
                }

            }

            if (storyCount == 0) {
                fail(" Test for Deployment Based Merging failed. No stories found with more than 1 cuplrit Ids. ");
            } else {
                log.info(storyCount
                    + " stories found with Deployment Based Merging. Test Completed. Test Passed.");
            }
        }
    }

    @Tas(testBeds = @TestBed(name = AssistedTriageTestbed.class, executeOn = AssistedTriageTestbed.SA_MASTER), size = SizeType.MEDIUM, owner = "katan09")
    @Test(groups = {"pipeorgan_merge_feature"})
    private void testCase_StoryMerge() throws IOException, ParseException, InterruptedException {
        /*
         * Test Case 2 - Test Case for Story Merge
         * Triggers stall on Webservice_5 within the scenario.
         * Checks received ProblemZone Statement stories in Rest to see if a story has more than one
         * story ids
         * If found, for every story id verify the culprit information is same for all stories.
         * If all succeeds passes test.
         */
        // Start time to query AT Rest API
        Start_Time = common.getCurrentTimeinISO8601Format(0);
        Timestamp end_time = common.getCurrentTimeinISO8601Format(2);

        // Enable stall on ExecutorServlet_12 - Start problem scripts and wait for 6 mins
        runCommandFlowContext =
            common.runPipeOrganScenario(batLocation, batFile, scenarioFolderName,
                "Problem-ProblemMerge.xml");
        runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER, runCommandFlowContext);

        runCommandFlowContext =
            common.runPipeOrganScenario(batLocation, batFile, scenarioFolderName,
                "Problem-ProblemMerge2.xml");
        runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER, runCommandFlowContext);

        log.info("Generating Problem - Sleeping for 6 mins");
        Thread.sleep(360000);

        // Check for story in REST API Response
        final String urlPart = "http://" + agcHost + ":8081/apm/appmap/private/triage/stories";

        // Fetch detailed story information
        AtStoryList stories = atStories.fetchStories(urlPart, Start_Time, end_time, true);
        restTimestamp = common.timestamp2String(common.getCurrentTimeinISO8601Format(0));

        int storyCount = 0;
        boolean culpritMatch = false;

        // For each story find evidences
        if (stories.getStories().isEmpty()) {
            fail(" No new stories created ");
        } else {
            Iterator<AtStory> itStory = stories.getStories().iterator();

            while (itStory.hasNext()) {
                AtStory storyLocation = itStory.next();
                Set<Integer> storyIds = convertToIntegerSet(storyLocation.getStoryIds());
                int storyIdSize = storyIds.size();

                if (storyIdSize > 1) {
                    // continue
                    String storyId = storyIds.toString();
                    log.info("Going to print logs for Test on Story ID : " + storyId);
                    storyCount++;

                    Map<String, String> individualStoryMap = new HashMap<String, String>();

                    Iterator<Integer> storyIdIterator = storyIds.iterator();

                    Long startTime = storyLocation.getStartTime();
                    Long endTime = storyLocation.getEndTime();

                    // Checks accuracy of start and end time in story
                    common.verifyStoryTime(startTime, endTime);

                    if (storyLocation.getPattern() != null) {

                        // in this pattern find type, if matches ProblemZoneStatement
                        // continue.
                        PatternAnalystStatement currPattern = storyLocation.getPattern();
                        boolean patternType =
                            currPattern.getClass().getSimpleName()
                                .equalsIgnoreCase("ProblemZoneStatement");

                        // If pattern is problem statement
                        if (patternType == true) {

                            while (storyIdIterator.hasNext()) {
                                // Get indiviual story details
                                Integer currStoryId = storyIdIterator.next();

                                AtStoryList storiesList =
                                    atStories.fetchStoriesDetailed(urlPart, Start_Time, end_time,
                                        currStoryId, false);

                                if (storiesList.getStories().isEmpty()) {
                                    fail(" No indiviual story found for story ID : " + currStoryId);
                                } else {
                                    Iterator<AtStory> itCurrStory =
                                        storiesList.getStories().iterator();
                                    innerloop: while (itCurrStory.hasNext()) {
                                        if (storyLocation.getPattern() != null) {
                                            currPattern = storyLocation.getPattern();
                                            patternType =
                                                currPattern.getClass().getSimpleName()
                                                    .equalsIgnoreCase("ProblemZoneStatement");
                                            if (patternType == true) {
                                                ProblemZoneStatement problemStmt =
                                                    (ProblemZoneStatement) currPattern;

                                                // Get culprit info for current story and put in
                                                // hashmap
                                                // String culpritId =
                                                // problemStmt.getCulpritVertexIds().iterator().next();
                                                String culpritInfo =
                                                    problemStmt.getCulprit().getName()
                                                        + ","
                                                        + problemStmt.getCulprit()
                                                            .getApplicationName() + ","
                                                        + problemStmt.getCulprit().getType();
                                                individualStoryMap.put(
                                                    Integer.toString(currStoryId), culpritInfo);
                                                break innerloop;
                                            } else {
                                                log.info("Pattern type for indiviual stories is different. StoryId : "
                                                    + currStoryId + " Pattern :" + currPattern);
                                            }
                                        } else {
                                            log.info("Pattern was null for StoryId : "
                                                + currStoryId);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        log.info("Pattern was null for Story ID : " + storyId);
                    }

                    // Compare is culprit Info is same for all stories
                    Iterator<Entry<String, String>> individualMapIterator =
                        individualStoryMap.entrySet().iterator();
                    while (individualMapIterator.hasNext()) {
                        Entry<String, String> nextEntry = individualMapIterator.next();
                        individualMapIterator.remove();

                        for (Entry<String, String> e : individualStoryMap.entrySet()) {
                            String value1 = e.getValue();
                            String value2 = nextEntry.getValue();
                            if (value1.equals(value2)) {
                                log.info("Culprit Information Matched. Culprit ID 1 : "
                                    + e.getKey() + " , Story Info 1: " + value1 + " Story ID 1 : "
                                    + nextEntry.getKey() + " , Story Info 1: " + value2);
                                culpritMatch = true;
                            }
                        }
                    }
                }
            }

            if (storyCount == 0) {
                fail(" Test for Problem Based Merging failed. No stories found with more than 1 story Ids. ");
            } else if (culpritMatch == false) {
                fail(" Test for Problem Based merging failed. Culprit ids of indiviual stories didn't match. Stories got merged incorrect. Check log above.");
            } else {
                log.info(storyCount
                    + " stories found with Problem Based Merging. Test Completed. Test Passed.");
            }
        }
    }

    public Set<Integer> convertToIntegerSet(Set<String> set) {
        HashSet<Integer> strs = new HashSet<Integer>(set.size());
        for (String str : set) {
            str = str.split(":")[0];
            strs.add(Integer.parseInt(str));
        }
        return strs;


    }



}
