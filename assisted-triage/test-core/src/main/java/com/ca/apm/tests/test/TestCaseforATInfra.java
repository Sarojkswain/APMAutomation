/*
 * Copyright (c) 2017 CA. All rights reserved.
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
import java.util.Arrays;
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
import com.ca.apm.tests.testbed.AssistedTriageTestbed;
import com.ca.apm.tests.utils.Common;
import com.ca.apm.tests.utils.FetchATStories;
import com.ca.apm.tests.utils.FetchAttributeInfo;
import com.ca.apm.tests.utils.LocalStorage;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;


/**
 * TestCaseForMerge class to check for deployment merge and problem merge scenario in
 * PipeOrgan App.
 *
 */

public class TestCaseforATInfra extends TasTestNgTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Common common = new Common();

    private FetchAttributeInfo vertex = new FetchAttributeInfo();
    private RunCommandFlowContext runCommandFlowContext;
    private FetchATStories atStories = new FetchATStories();

    private static Timestamp Start_Time;

    private String agcHost = envProperties
        .getMachineHostnameByRoleId(AssistedTriageTestbed.SA_MASTER_EM_ROLE);
    
    private String tomcatAgent = envProperties
            .getMachineHostnameByRoleId(AssistedTriageTestbed.TOMCAT_AGENT_ONLY_ROLE);

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
    private void testCase_cpuResource() throws Exception {
        /*
         * Test Case 1 - Test Case for CPU Resource Alert verification.
         * Triggers a LoadGenerator app that exhaust cpu resource for agent machine, causing app to slow down.
         * Checks received story evidences for CPU Utilization AT Alert.
         * If found passes test.
         */

        // Start time to query AT Rest API
        Start_Time = common.getCurrentTimeinISO8601Format(-4);
        Timestamp end_time = common.getCurrentTimeinISO8601Format(2);

        String command = "START /D " + AssistedTriageTestbed.CPU_APP_LOCATION + " Load.bat";
        
        // Start LoadGenerator App to exhaust CPU of host machine
        runCommandFlowContext =
                new RunCommandFlowContext.Builder("").args(Arrays.asList(command)).build();
        runCommandFlowByMachineId(AssistedTriageTestbed.TOMCAT_AGENT_ONLY, runCommandFlowContext);
        
        
       log.info("Generating Problem - Sleeping for 6 mins");
       Thread.sleep(360000);

        // Check for story in REST API Response
        final String urlPart = "http://" + agcHost + ":8081/apm/appmap/private/triage/stories";

        // Fetch detailed story information
        AtStoryList stories = atStories.fetchStories(urlPart, Start_Time, end_time, true);
        restTimestamp = common.timestamp2String(common.getCurrentTimeinISO8601Format(0));

        boolean failTest = true;

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
                    boolean alertType =
                        currEvidence.getClass().getSimpleName()
                            .equalsIgnoreCase("AlertEventStatement");

                    // If evidence is UVB continue
                    if (alertType == true) {
                      
                        String agentName = tomcatAgent + "|Tomcat|Tomcat Agent";

                        AlertEventStatement alert = (AlertEventStatement) currEvidence;
                        String suspectName = alert.getSuspect().getName();
                        String suspectId = alert.getSuspect().getVertexId();
                        Long firstOccurrence = alert.getFirstOccurrence();
                        Long lastOccurrence = alert.getLastOccurrence();
                        Set<String> alerts = alert.getAlerts();
                        
                        // Check to see if suspectName is null, if true - fail
                        if (suspectName.isEmpty()) {
                            fail(" No Suspect Name found for evidence. Story ID : " + storyId);
                        } 
                        else if(agentName.equalsIgnoreCase(suspectName)){
                            //tomcatAgent
                            
                            log.info("Going to print logs for Test on Story ID : " + storyId);
      
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
                                 else {
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
                                         log.info("Vertex Information found for Vertex ID "
                                             + suspectId + " and Vertex Name " + vertexName);
                                     } else {
                                         fail("Failed to match Suspect Information. Suspect ID "
                                             + suspectId + " ; Suspect Name " + suspectName
                                             + " and Vertex Name " + vertexName);
                                     }
    
                                     if (alerts.isEmpty()) {
                                         fail(" Alert Message cannot be empty. ");
                                     }else if(alerts.contains("SuperDomain:AT Automation MM:CPU Utilization AT") || alerts.contains("SuperDomain:Default:CPU Utilization")){
                                         failTest = false;
                                         log.info("CPU Alert found in evidence.");
                                     }
                                 }
                            }
                        }
                    }
                }
            }
        }

        if (failTest == true) {
            fail("Test Failed : Test for CPU Resource Failed. Check logs printed above.");
        }else{
            log.info("Test Passed : Test for accuracy of CPU Resource Analysts.");

        }
    }

    @Tas(testBeds = @TestBed(name = AssistedTriageTestbed.class, executeOn = AssistedTriageTestbed.SA_MASTER), size = SizeType.MEDIUM, owner = "katan09")
    @Test(groups = {"pipeorgan_merge_feature"})
    private void testCase_heapResource() throws Exception {
        /*
         * Test Case 2 - Test Case for Heap Used Percent Resource Alert verification.
         * Triggers a stall scenario that uses excessive heap resource for agent machine, causing app to stall.
         * Checks received story evidences for Heap Used Percent AT Alert.
         * If found passes test.
         */       
        
        // Start time to query AT Rest API
        Start_Time = common.getCurrentTimeinISO8601Format(-2);
        Timestamp end_time = common.getCurrentTimeinISO8601Format(2);

        
        // Enable stall on ExecutorServlet_4 - Start problem scripts and wait for 6 mins
        runCommandFlowContext =
            common.runPipeOrganScenario(batLocation, batFile, scenarioFolderName,
                "Problem-BTStall.xml");
        runCommandFlowByMachineId(AssistedTriageTestbed.TOMCAT_AGENT_ONLY, runCommandFlowContext);

        log.info("Generating Problem - Sleeping for 6 mins");
        Thread.sleep(360000);

        // Check for story in REST API Response
        final String urlPart = "http://" + agcHost + ":8081/apm/appmap/private/triage/stories";

        // Fetch detailed story information
        AtStoryList stories = atStories.fetchStories(urlPart, Start_Time, end_time, true);
        restTimestamp = common.timestamp2String(common.getCurrentTimeinISO8601Format(0));

        boolean failTest = true;

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
                    boolean alertType =
                        currEvidence.getClass().getSimpleName()
                            .equalsIgnoreCase("AlertEventStatement");

                    // If evidence is UVB continue
                    if (alertType == true) {
                      
                        String agentName = tomcatAgent + "|Tomcat|Tomcat Agent";

                        AlertEventStatement alert = (AlertEventStatement) currEvidence;
                        String suspectName = alert.getSuspect().getName();
                        String suspectId = alert.getSuspect().getVertexId();
                        Long firstOccurrence = alert.getFirstOccurrence();
                        Long lastOccurrence = alert.getLastOccurrence();
                        Set<String> alerts = alert.getAlerts();
                        
                     // Check to see if suspectName is null, if true - fail
                        if (suspectName.isEmpty()) {
                            fail(" No Suspect Name found for evidence. Story ID : " + storyId);
                        } 
                        else if(agentName.equalsIgnoreCase(suspectName)){
                            //tomcatAgent
                            
                            log.info("Going to print logs for Test on Story ID : " + storyId);
      
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
                                 else {
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
                                         log.info("Vertex Information found for Vertex ID "
                                             + suspectId + " and Vertex Name " + vertexName);
                                     } else {
                                         fail("Failed to match Suspect Information. Suspect ID "
                                             + suspectId + " ; Suspect Name " + suspectName
                                             + " and Vertex Name " + vertexName);
                                     }
    
                                     if (alerts.isEmpty()) {
                                         fail(" Alert Message cannot be empty. ");
                                     }else if(alerts.contains("SuperDomain:AT Automation MM:Heap Used Percent AT") || alerts.contains("SuperDomain:Default:Heap Used Percent")){
                                         failTest = false;
                                         log.info("Heap Used Percent Alert found in evidence.");                                     
                                     }                         
                                 }
                            }
                        }
                    }
                }
            }
        }

        if (failTest == true) {
            fail("Test Failed : Test for Heap Used Percent Resource Failed. Check logs printed above.");
        }else{
            log.info("Test Passed : Test for accuracy of Heap Used Percent Resource Analysts.");
        }
    }
   
}
