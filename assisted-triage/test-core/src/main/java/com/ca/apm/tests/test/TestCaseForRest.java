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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.classes.from.appmap.plugin.AnalystStatement;
import com.ca.apm.classes.from.appmap.plugin.AtFilter;
import com.ca.apm.classes.from.appmap.plugin.AtStory;
import com.ca.apm.classes.from.appmap.plugin.AtStoryList;
import com.ca.apm.classes.from.appmap.plugin.EvidenceStatement;
import com.ca.apm.classes.from.appmap.plugin.ExternalId;
import com.ca.apm.classes.from.appmap.plugin.PatternAnalystStatement;
import com.ca.apm.classes.from.appmap.plugin.ProblemZoneStatement;
import com.ca.apm.tests.testbed.AssistedTriageTestbed;
import com.ca.apm.tests.utils.Common;
import com.ca.apm.tests.utils.EmRestRequest;
import com.ca.apm.tests.utils.FetchATStories;
import com.ca.apm.tests.utils.LocalStorage;
import com.ca.tas.restClient.IRestResponse;
import com.ca.tas.restClient.RestClient;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * General Tests on Overall AT and Rest interface
 *
 */

public class TestCaseForRest extends TasTestNgTest {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final ObjectMapper mapper = new ObjectMapper();

    private RestClient restClient = new RestClient();
    private Common common = new Common();
    private FetchATStories atStories = new FetchATStories();
    private RunCommandFlowContext runCommandFlowContext;

    private static Timestamp Rest_Start_Time;

    private String agcHost = envProperties
        .getMachineHostnameByRoleId(AssistedTriageTestbed.SA_MASTER_EM_ROLE);

    private String batFile = "run.bat";
    private String batLocation = AssistedTriageTestbed.TOMCAT_INSTALL_DIR
        + "\\webapps\\pipeorgan\\WEB-INF\\lib\\";

    private String scenarioFolderName = "scenarios";

    @BeforeMethod
    public void initTestMethod(Method testMethod) {
        @SuppressWarnings("unused")
        LocalStorage localStorage = new LocalStorage(testMethod);
    }

    @Tas(testBeds = @TestBed(name = AssistedTriageTestbed.class, executeOn = AssistedTriageTestbed.SA_MASTER), size = SizeType.MEDIUM, owner = "patpr15")
    @Test(groups = {"pipeorgan_generic_tests"})
    private void testCase_NoOfStories() throws Exception {
        int expectedNumberofStories = 2;
        Rest_Start_Time = common.getCurrentTimeinISO8601Format(0);
        runCommandFlowContext =
            common.runPipeOrganScenario(batLocation, batFile, scenarioFolderName,
                "Problem-BTWebserviceDBTC.xml");
        runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER, runCommandFlowContext);

        runCommandFlowContext =
            common.runPipeOrganScenario(batLocation, batFile, scenarioFolderName,
                "Problem-DefaultAnalysts.xml");
        runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER, runCommandFlowContext);
        log.info("Generating Problem - Sleeping for 6 mins");
        Thread.sleep(360000);
        if (numberofStories(agcHost, Rest_Start_Time, common.getCurrentTimeinISO8601Format(2)) == 0) {
            fail(" No new stories created ");
        } else {

            int numberofStoriesRest =
                numberofStories(agcHost, Rest_Start_Time, common.getCurrentTimeinISO8601Format(2));
            if (numberofStoriesRest == expectedNumberofStories) {
                log.info("Test Passed: Number of stories EXACTLY matched in Rest "
                    + numberofStoriesRest + "; Expected stories: " + expectedNumberofStories);
                restFilters_Tests();
            } else if ((numberofStoriesRest + 1) == expectedNumberofStories
                || (numberofStoriesRest - 1) == expectedNumberofStories) {
                log.info(" Number of stories APPROX. matched in Rest " + numberofStoriesRest
                    + "; Expected stories: " + expectedNumberofStories);
                restFilters_Tests();
            }
        }
    }

    public int numberofStories(String dbName, Timestamp start_time, Timestamp end_time)
        throws ClassNotFoundException, SQLException {

        Connection c = null;
        Statement stmt = null;
        Class.forName("org.postgresql.Driver");
        c =
            DriverManager.getConnection("jdbc:postgresql://" + dbName + ":5432/cemdb", "postgres",
                "Lister@123");
        String problemZoneStoryQuery =
            "select * from at_stories where statements LIKe '%ProblemZoneStatement%PipeOrganWebService_2.webservices.executor.pipeorgan.tools.wily.com|execute%' and start_time >"
                + "'"
                + common.timestamp2String(start_time)
                + "'"
                + "and end_time <"
                + "'"
                + common.timestamp2String(end_time) + "'";

        String dbtcStoryQuery =
            "select * from at_stories where statements LIKe '%Dbtc%PipeOrganWebService_0.webservices.executor.pipeorgan.tools.wily.com|execute%PipeOrgan Application:WEBSERVICE_SERVER:http_//PipeOrganWebService_0.webservices.executor.pipeorgan.tools.wily.com|execute:%Tomcat Agent:Tomcat%' and start_time > "
                + "'"
                + common.timestamp2String(start_time)
                + "'"
                + "and end_time < "
                + "'"
                + common.timestamp2String(end_time) + "'";


        log.info("Problem Query : " + problemZoneStoryQuery);
        log.info("Problem Query : " + dbtcStoryQuery);
        stmt = c.createStatement();
        ResultSet rsProblem = stmt.executeQuery(problemZoneStoryQuery);
        stmt = c.createStatement();
        ResultSet rsDbtc = stmt.executeQuery(dbtcStoryQuery);
        return (getResultSetSize(rsProblem) + getResultSetSize(rsDbtc));

    }

    private int getResultSetSize(ResultSet rs) throws SQLException {
        int i = 0;
        while (rs.next())
            i++;
        return i;
    }

    public void restFilters_Tests() throws IOException {

        final String urlPart = "http://" + agcHost + ":8081/apm/appmap/private/triage/stories";
        Map<String, AtFilter> payloadsMap = new LinkedHashMap<String, AtFilter>();
        Set<ExternalId> vertexIds = new HashSet<ExternalId>();
        Set<Integer> storyIds = new HashSet<Integer>();

        AtFilter filter = new AtFilter();
        payloadsMap.put("payloadEmpty", filter);

        filter = new AtFilter();
        filter.setStartTime(common.timestamp2String(Rest_Start_Time));
        filter.setEndTime(common.timestamp2String(common.getCurrentTimeinISO8601Format(1)));
        filter.setMergeStories(false);
        payloadsMap.put("payloadStartEndMergedFalse", filter);

        filter = new AtFilter();
        filter.setStartTime(common.timestamp2String(Rest_Start_Time));
        filter.setEndTime(common.timestamp2String(common.getCurrentTimeinISO8601Format(1)));
        payloadsMap.put("payloadStartTime", filter);

        filter = new AtFilter();
        filter.setEndTime(common.timestamp2String(common.getCurrentTimeinISO8601Format(1)));
        payloadsMap.put("payloadEndTime", filter);

        filter = new AtFilter();
        filter.setStartTime(common.timestamp2String(Rest_Start_Time));
        filter.setEndTime(common.timestamp2String(common.getCurrentTimeinISO8601Format(1)));
        payloadsMap.put("payloadStartEndStoryIds", filter);

        filter = new AtFilter();
        filter.setStartTime(common.timestamp2String(Rest_Start_Time));
        filter.setEndTime(common.timestamp2String(common.getCurrentTimeinISO8601Format(1)));
        filter.setProjection(AtFilter.PROJ_DETAILED);
        payloadsMap.put("payloadStartEndProjectionStoryIds", filter);

        filter = new AtFilter();

        payloadsMap.put("payloadVertexIds", filter);

        filter = new AtFilter();
        filter.setStartTime(common.timestamp2String(Rest_Start_Time));
        filter.setEndTime(common.timestamp2String(common.getCurrentTimeinISO8601Format(1)));
        filter.setProjection(AtFilter.PROJ_DETAILED);
        payloadsMap.put("payloadStartEndProjection", filter);

        Iterator<String> itPayload = payloadsMap.keySet().iterator();
        while (itPayload.hasNext()) {
            String payloadCase = itPayload.next();
            switch (payloadCase) {

                case "payloadEmpty":
                    AtFilter caseFilter = payloadsMap.get(payloadCase);
                    log.info(payloadCase + " : " + mapper.writeValueAsString(caseFilter));
                    AtStoryList stories =
                        atStories.fetchStories(urlPart, payloadsMap.get(payloadCase));
                    if (common.isNull(stories) || stories.getStories().size() == 0) {
                        Assert.fail("No stories returned with {} payload");
                        break;
                    }
                    Iterator<AtStory> itStory = stories.getStories().iterator();
                    while (itStory.hasNext()) {
                        AtStory storyLocation = itStory.next();
                        PatternAnalystStatement currPattern = storyLocation.getPattern();

                        boolean patternType =
                            currPattern.getClass().getSimpleName()
                                .equalsIgnoreCase("DbtcStatement");
                        if (!patternType) {
                            ProblemZoneStatement problemZoneStmt =
                                (ProblemZoneStatement) currPattern;
                            vertexIds.add(ExternalId.fromString(problemZoneStmt.getCulpritVertexIds().iterator().next()));
                            storyIds.addAll(convertToIntegerSet(storyLocation.getStoryIds()));
                        }

                    }
                    break;
                case "payloadStartEndStoryIds":
                    caseFilter = payloadsMap.get(payloadCase);
                    caseFilter.setStoryIds(convertToStringSet(storyIds));
                    log.info(payloadCase + " : " + mapper.writeValueAsString(caseFilter));
                    stories = atStories.fetchStories(urlPart, caseFilter);
                    if (common.isNull(stories) || stories.getStories().size() == 0)
                        Assert
                            .fail(" No stories returned with {startTime, endTime, storyIds} payload ");
                    break;
                case "payloadStartEndMergedFalse":
                    caseFilter = payloadsMap.get(payloadCase);
                    log.info(payloadCase + " : " + mapper.writeValueAsString(caseFilter));
                    stories = atStories.fetchStories(urlPart, caseFilter);
                    if (common.isNull(stories) || stories.getStories().size() == 0)
                        Assert
                            .fail("No stories returned with {startTime, endTime and Merged=true} payload");
                    break;
                case "payloadStartTime":
                    caseFilter = payloadsMap.get(payloadCase);
                    log.info(payloadCase + " : " + mapper.writeValueAsString(caseFilter));
                    stories = atStories.fetchStories(urlPart, caseFilter);
                    if (common.isNull(stories) || stories.getStories().size() == 0)
                        Assert.fail("No stories returned with {startTime, endTime} payload");
                    break;
                case "payloadEndTime":
                    caseFilter = payloadsMap.get(payloadCase);
                    log.info(payloadCase + " : " + mapper.writeValueAsString(caseFilter));
                    stories = atStories.fetchStories(urlPart, caseFilter);
                    if (common.isNull(stories) || stories.getStories().size() == 0)
                        Assert.fail("No stories returned with {endTime} payload");
                    break;
                case "payloadStartEndProjectionStoryIds":
                    caseFilter = payloadsMap.get(payloadCase);
                    caseFilter.setStoryIds(convertToStringSet(storyIds));
                    log.info(payloadCase + " : " + mapper.writeValueAsString(caseFilter));
                    stories = atStories.fetchStories(urlPart, caseFilter);
                    if (common.isNull(stories) || stories.getStories().size() == 0)
                        Assert
                            .fail("No stories returned with {startTime, endTime, Projection, storyIds} payload");
                    break;
                case "payloadVertexIds":
                    caseFilter = payloadsMap.get(payloadCase);
                    caseFilter.setVertexIds(vertexIds);
                    log.info(payloadCase + " : " + mapper.writeValueAsString(caseFilter));
                    stories = atStories.fetchStories(urlPart, caseFilter);
                    if (common.isNull(stories) || stories.getStories().size() == 0)
                        Assert.fail("No stories returned with {vertexIds} payload");
                    break;
                case "payloadStartEndProjection":
                    caseFilter = payloadsMap.get(payloadCase);
                    log.info(payloadCase + " : " + mapper.writeValueAsString(caseFilter));
                    if (common.restIResponse(restClient, urlPart,
                        mapper.writeValueAsString(caseFilter)) != 500)
                        Assert
                            .fail("Stories returned with {startTime, endTime, Projection} payload, (storyIds or vertexIds must be present in this query)");
                    break;
            }
        }
    }

    @Tas(testBeds = @TestBed(name = AssistedTriageTestbed.class, executeOn = AssistedTriageTestbed.SA_MASTER), size = SizeType.MEDIUM, owner = "patpr15")
    @Test(groups = {"pipeorgan_generic_tests"})
    private void testCase_StoryClosing() throws InterruptedException, IOException, ParseException {

        Rest_Start_Time = common.getCurrentTimeinISO8601Format(-2);
        Thread.sleep(260000); // added this delay to make sure old stories get closed

        runCommandFlowContext =
            common.runPipeOrganScenario(batLocation, batFile, scenarioFolderName,
                "Problem-BTWebserviceDBTC.xml");
        runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER, runCommandFlowContext);
        runCommandFlowContext =
            common.runPipeOrganScenario(batLocation, batFile, scenarioFolderName,
                "Problem-DefaultAnalysts.xml");
        runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER, runCommandFlowContext);

        log.info("Generating Problem - Sleeping for 5 min");
        Thread.sleep(360000);

        // Check for story in REST API Response
        String urlPart = "http://" + agcHost + ":8081/apm/appmap/private/triage/stories";

        AtFilter filter = new AtFilter();
        filter.setStartTime(common.timestamp2String(Rest_Start_Time));
        filter.setEndTime(common.timestamp2String(common.getCurrentTimeinISO8601Format(2)));

        log.info("Printing Rest Payload--");
        log.info(mapper.writeValueAsString(filter));
        log.info("Waiting for a min to fetch stories ....");
        Thread.sleep(60000);

        AtStoryList stories =
            atStories.fetchStories(urlPart, Rest_Start_Time,
                common.getCurrentTimeinISO8601Format(2), false);
        HashSet<Integer> dbtcStoriesSet = getPatternTypeStories(stories, "DbtcStatement");
        HashSet<Integer> problemZoneStoriesSet =
            getPatternTypeStories(stories, "ProblemZoneStatement");

        // CHeck to see if either sets of story pattern is null
        if ((dbtcStoriesSet.size() == 0) || (problemZoneStoriesSet.size() == 0)) {
            log.info(" Story Closing Test Information !! Found " + dbtcStoriesSet.size()
                + " no. of stories with DBTC and " + problemZoneStoriesSet.size()
                + " no. of stories with ProblemZoneStatement.");
        }

        if ((dbtcStoriesSet.size() != 0 && storyClosed(urlPart, stories, dbtcStoriesSet))
            || (problemZoneStoriesSet.size() != 0 && storyClosed(urlPart, stories,
                problemZoneStoriesSet)))
            log.info(" Story Closing Test Passed !! Found " + dbtcStoriesSet.size()
                + " no. of stories with DBTC and " + problemZoneStoriesSet.size()
                + " no. of stories with ProblemZoneStatement.");
        else
            fail("Story Closing Test Failed!!  Found " + dbtcStoriesSet.size()
                + " no. of stories with DBTC and " + problemZoneStoriesSet.size()
                + " no. of stories with ProblemZoneStatement.");
    }

    public boolean storyClosed(String urlPart, AtStoryList stories, HashSet<Integer> patternSet)
        throws IOException, ParseException {

        // Reads all stories received in REST and puts in storiesMap and calls checkForCLosedStory
        // for further checks

        AtStoryList detailedStories =
            fetchStoriesDetails(urlPart, Rest_Start_Time, common.getCurrentTimeinISO8601Format(2),
                patternSet, false);
        HashMap<String, StoryDetails> storiesMap = new HashMap<String, StoryDetails>();
        Iterator<AtStory> itDbtcStory = detailedStories.getStories().iterator();
        while (itDbtcStory.hasNext()) {
            AtStory story = itDbtcStory.next();
            PatternAnalystStatement pattern = story.getPattern();
            List<String> storyList = new ArrayList<String>(story.getStoryIds());
            List<String> culpritVertexIdsList = new ArrayList<String>();
            List<String> evidencesList = new ArrayList<String>();
            Iterator<String> culpritIterator = pattern.getCulpritVertexIds().iterator();
            while (culpritIterator.hasNext()) {
                culpritVertexIdsList.add(culpritIterator.next() + " ");
            }
            Collection<AnalystStatement> evidences = story.getEvidences();
            Iterator<AnalystStatement> itEvidences = evidences.iterator();
            while (itEvidences.hasNext()) {
                EvidenceStatement event = (EvidenceStatement) itEvidences.next();
                String eventType = "";
                if (event.getClass().getSimpleName().equalsIgnoreCase("UvbEventStatement"))
                    eventType = "UvbEventStatement";
                else if (event.getClass().getSimpleName().equalsIgnoreCase("AlertEventStatement"))
                    eventType = "AlertEventStatement";
                else if (event.getClass().getSimpleName().equalsIgnoreCase("ErrorEventStatement"))
                    eventType = "ErrorEventStatement";
                else if (event.getClass().getSimpleName().equalsIgnoreCase("StallEventStatement"))
                    eventType = "StallEventStatement";

                evidencesList.add(eventType + " " + event.getSuspect().getVertexId() + " "
                    + event.getSuspect().getName());
            }

            storiesMap.put(storyList.get(0),
                new StoryDetails(story.getStartTime(), story.getEndTime(), pattern.getCulprit()
                    .getName(), pattern.getCulprit().getApplicationName(), pattern.getCulprit()
                    .getType(), culpritVertexIdsList, evidencesList));
        }

        if (checkForClosedStory(storiesMap)) return true;

        return false;
    }

    // Checks to see if saet returned by getKeysByValue contains more than one story ids.
    public boolean checkForClosedStory(HashMap<String, StoryDetails> stories) {

        if (stories.size() > 1) {
            for (Map.Entry<String, StoryDetails> entrySet : stories.entrySet()) {
                StoryDetails story = entrySet.getValue();

                Set<String> keys = getKeysByValue(stories, story);
                if (keys.size() > 1) {
                    log.info(" Closed Stories ids : " + keys);
                    return true;
                }
            }
        }
        return false;
    }

    // Compares all stories in the storiesMap to see if culprit information matches for stories. If
    // culprit information matches, gets difference in time stamp and logs it. Adds current matched
    // storyId to keys list and returns list back
    public Set<String> getKeysByValue(Map<String, StoryDetails> map, StoryDetails value) {
        Set<String> keys = new HashSet<String>();
        for (Entry<String, StoryDetails> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {

                long startTime2 = value.getStartTime();
                long endTime1 = entry.getValue().getEndTime();
                int diff = (int) ((Math.abs(startTime2 - endTime1) / (1000 * 60)) % 60);

                if (startTime2 != endTime1) {
                    log.info("StartTime and EndTime doesn't match. StartTime and EndTime difference is : "
                        + diff);
                    keys.add(entry.getKey());
                }
            }
        }
        return keys;
    }

    public AtStoryList fetchStoriesDetails(String urlPart, Timestamp start_time,
        Timestamp end_time, Set<Integer> storyIds, boolean merged) throws IOException {
        RestClient restClient = new RestClient();
        AtFilter filter = new AtFilter();
        filter.setStartTime(common.timestamp2String(start_time));
        filter.setEndTime(common.timestamp2String(end_time));
        filter.setStoryIds(convertToStringSet(storyIds));
        filter.setProjection(AtFilter.PROJ_DETAILED);
        filter.setMergeStories(merged);
        filter.setLimit(-1);
        EmRestRequest request = new EmRestRequest(urlPart, mapper.writeValueAsString(filter));
        IRestResponse<String> response = restClient.process(request);
        String returnValue = response.getContent();
        return mapper.readValue(returnValue, AtStoryList.class);
    }

    public HashSet<Integer> getPatternTypeStories(AtStoryList stories, String pattrenType) {

        HashSet<Integer> result = new HashSet<Integer>();
        Iterator<AtStory> itStory = stories.getStories().iterator();
        while (itStory.hasNext()) {
            AtStory story = itStory.next();
            PatternAnalystStatement currPattern = story.getPattern();
            if (currPattern.getClass().getSimpleName().equalsIgnoreCase(pattrenType)) {
                result.addAll(convertToIntegerSet(story.getStoryIds()));
            }
        }
        return result;
    }

    public Set<Integer> convertToIntegerSet(Set<String> set) {
        HashSet<Integer> strs = new HashSet<Integer>(set.size());
        for (String str : set) {
            str = str.split(":")[0];
            strs.add(Integer.parseInt(str));
        }
        return strs;
    }

    public Set<String> convertToStringSet(Set<Integer> set) {
        HashSet<String> strs = new HashSet<String>(set.size());
        for (Integer integer : set)
            strs.add(integer.toString());

        return strs;


    }

}


class StoryDetails {

    private String culpritName;
    private String applicationName;
    private String culpritType;
    private List<String> culpritVertexIds;
    private List<String> evidencesList;
    private long startTime;
    private long endTime;


    StoryDetails(long startTime, long endTime, String culpritName, String applicationName,
        String culpritType, List<String> culpritVertexIds, List<String> evidencesList) {

        this.startTime = startTime;
        this.endTime = endTime;
        this.culpritName = culpritName.trim();
        this.applicationName = applicationName.trim();
        this.culpritType = culpritType.trim();
        this.culpritVertexIds = culpritVertexIds;
        this.evidencesList = evidencesList;

    }


    String getCulpritName() {
        return culpritName;
    }

    String getApplicationName() {
        return applicationName;
    }

    String getCulpritType() {
        return culpritType;
    }

    long getStartTime() {
        return startTime;
    }

    long getEndTime() {
        return endTime;
    }

    List<String> getCulpritVertexIds() {

        return culpritVertexIds;

    }

    List<String> getEvidencesList() {
        return evidencesList;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof StoryDetails)) return false;
        StoryDetails story = (StoryDetails) obj;
        return story.getCulpritName().equals(this.getCulpritName())
            && story.getApplicationName().equals(this.getApplicationName())
            && story.getCulpritType().equals(this.getCulpritType())
            && equalLists(story.getCulpritVertexIds(), this.getCulpritVertexIds());
        // && equalLists(story.getEvidencesList(), this.getEvidencesList());
    }

    public boolean equalLists(List<String> a, List<String> b) {
        if ((a.size() != b.size()) || (a == null && b != null) || (a != null && b == null)) {
            return false;
        }

        Collections.sort(a);
        Collections.sort(b);
        return a.equals(b);
    }



}
