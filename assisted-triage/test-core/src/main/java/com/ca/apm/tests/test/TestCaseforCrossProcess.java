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
 * TestCaseForCrossProcess class to check for AT story accuracy in CrossProcess (Cross-Provider)
 * Transaction scenario in PipeOrgan App.
 *
 */

public class TestCaseforCrossProcess extends TasTestNgTest {

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
    private void testCase_CrossProcess() throws Exception {
        /*
         * Test Case for CrossProcess.
         * Triggers stall on PipeOrganWebservice_6 within the scenario.
         * Checks received ProblemZone Statement stories in Rest to see if webservice_6 culprit is
         * found with a stall event type.
         * Fetches suspect vertex in story and checks to see if the vertices in the story belong to
         * different host machines.
         * If found, for vertexId verifies Vertex Name, Application Name and Vertex Type.
         * If all succeeds passes test.
         */

        // Start time to query AT Rest API
        Start_Time = common.getCurrentTimeinISO8601Format(0);
        Timestamp end_time = common.getCurrentTimeinISO8601Format(2);

        // Enable stall on PiprOrganWebservice_6 - Start problem script and wait for 4 mins
        runCommandFlowContext =
            common.runPipeOrganScenario(batLocation, batFile, scenarioFolderName,
                "Problem-CrossProcessWebserviceStall.xml");
        runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER, runCommandFlowContext);

        log.info("Generating Problem - Sleeping for 8 mins");
        Thread.sleep(480000);

        // Check for story in REST API Response
        final String urlPart = "http://" + agcHost + ":8081/apm/appmap/private/triage/stories";

        // Fetch detailed story information
        AtStoryList stories = atStories.fetchStories(urlPart, Start_Time, end_time, true);
        restTimestamp = common.timestamp2String(common.getCurrentTimeinISO8601Format(0));

        boolean storyFlag = false;


        // For each story iterate and find CrossProcess scenario story
        if (stories.getStories().isEmpty()) {
            fail(" No new stories created. Test for CrossProcess Transaction Scenario failed. ");
        } else {
            Iterator<AtStory> itStory = stories.getStories().iterator();

            while (itStory.hasNext()) {
                AtStory storyLocation = itStory.next();
                String storyId = storyLocation.getStoryIds().toString();

                Long startTime = storyLocation.getStartTime();
                Long endTime = storyLocation.getEndTime();

                Collection<String> suspects = storyLocation.getSuspectVertexIds();

                if (storyLocation.getPattern() != null) {

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
                        String culpritType = problemStmt.getCulprit().getType();

                        // If culprit is PipeOrganWebservice_6 Server continue
                        if (culpritName
                            .equalsIgnoreCase("PipeOrganWebService_6.webservices.executor.pipeorgan.tools.wily.com|execute")
                            && culpritType.equalsIgnoreCase("WEBSERVICE_SERVER")) {
                            // continue

                            log.info("Going to print logs for Test on Story ID : " + storyId);
                            String culpritId = problemStmt.getCulpritVertexIds().iterator().next();
                            String culpritAppName = problemStmt.getCulprit().getApplicationName();

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

                                String culpritHost = culpritVertexInfo.get("hostname");
                                String suspectHost = null;

                                // Find ExecutorServlet_15 servlet hostname
                                if (suspects.isEmpty()) {
                                    log.info(" Suspect IDs are empty. Invalid story.");
                                } else {
                                    Iterator<String> itSuspect = suspects.iterator();
                                    while (itSuspect.hasNext()) {
                                        String currSuspect = itSuspect.next();

                                        if (currSuspect.contains("ExecutorServlet_15")) {

                                            log.info(" Found " + currSuspect + " suspect in story.");
                                            Map<String, String> suspectVertexInfo =
                                                vertex.fetchVertexInfo(currSuspect, restTimestamp,
                                                    agcHost, false);

                                            suspectHost = suspectVertexInfo.get("hostname");
                                        }
                                    }
                                }

                                // Verify if host names are in fact different for vertices in same
                                // story.
                                if (suspectHost == null) {
                                    fail("No suspect hostname found for comparision to culprit host machine. Culprit Host:"
                                        + culpritHost);
                                } else if (culpritHost.equalsIgnoreCase(suspectHost)) {
                                    fail(" Servlet and webservice hostnames matched. Vertex are present on Vertex are present on same machine."
                                        + " CrossProcess Transaction Scenario failed to be verified. Culprit Host : "
                                        + culpritHost + " BT Host : " + suspectHost);
                                } else {
                                    storyFlag = true;
                                    log.info("Test case for CrossProcess Transaction Scenario passed. Culprit Host : "
                                        + culpritHost + " BT Host : " + suspectHost);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (storyFlag == false) {
            fail("Test case for CrossProcess Transaction Scenario failed. Story not found.");
        }
    }
}
