package com.ca.apm.tests.test;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.LinkedList;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.tests.testbed.AssistedTriageTestbed;
import com.ca.apm.tests.utils.Common;
import com.ca.apm.tests.utils.LocalStorage;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

public class ValidateStoryIntegrity extends TasTestNgTest {


    private static final LinkedList<String> tables = new LinkedList<String>(
        Arrays.asList(new String[] {"at_stories", "at_evidences", "at_stories_pivot"}));
    private final Logger log = LoggerFactory.getLogger(getClass());
    private String agcHost = envProperties
        .getMachineHostnameByRoleId(AssistedTriageTestbed.SA_MASTER_EM_ROLE);
    private String colletcorHost = envProperties
        .getMachineHostnameByRoleId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER_EM_ROLE);
    private static int storiesCount = 0;
    private static int storiesInEvidences = 0;
    private static ResultSet rs;

    @BeforeMethod
    public void initTestMethod(Method testMethod) {
        @SuppressWarnings("unused")
        LocalStorage localStorage = new LocalStorage(testMethod);
    }

    @Tas(testBeds = @TestBed(name = AssistedTriageTestbed.class, executeOn = AssistedTriageTestbed.SA_MASTER), size = SizeType.MEDIUM, owner = "patpr15")
    @Test(groups = {"pipeorgan_generic_tests"})
    private void storiesIntegerity_TestCase() throws Exception {

        for (int i = 0; i < tables.size(); i++) {
            checkStoriesIntegerity(tables.get(i), agcHost);
        }

    }

    private void checkStoriesIntegerity(String table, String agcHost)
        throws Exception {

        switch (table) {

            case "at_stories":
                rs =
                    queryDb(agcHost,
                        "select count(DISTINCT story_id) as CountStoriesInAtStories from at_stories");
                rs.next();
                storiesCount = rs.getInt("CountStoriesInAtStories");
                if (storiesCount == 0) Assert.fail("No stories in at_stories");
                log.info(" Stories in Stories Table : " + storiesCount);
                break;

            case "at_evidences":
                rs =
                    queryDb(agcHost,
                        "select count(DISTINCT story_id) as CountStoriesInAtEvidences from at_evidences");
                rs.next();
                storiesInEvidences = rs.getInt("CountStoriesInAtEvidences");
                if (storiesCount != storiesInEvidences) {
                    log.info(" Stories in Stories Table : " + storiesCount);
                    log.info(" Stories in Evidences Table : " + storiesInEvidences);

                    Assert.fail(" Stories table and Evidences table have different stories");
                }
                break;

            case "at_stories_pivot":
                rs =
                    queryDb(agcHost,
                        "select count(DISTINCT story_id) as CountStoriesInAtPivot from at_stories_pivot");
                rs.next();
                if (storiesInEvidences != rs.getInt("CountStoriesInAtPivot")) {
                    log.info(" Stories in Stories Table : " + storiesCount);
                    log.info(" Stories in Evidences Table : " + storiesInEvidences);
                    log.info(" Stories in Pivot Table : " + rs.getInt("CountStoriesInAtPivot"));
                    Assert
                        .fail("Pivot table has different number of stories compared to stories and evidences");


                }

                break;
        }

    }

    private ResultSet queryDb(String agcHost, String query) throws Exception {

        Connection c = null;

        Class.forName("org.postgresql.Driver");
        c =
            DriverManager.getConnection("jdbc:postgresql://" + agcHost + ":5432/cemdb", "postgres",
                "Lister@123");
        return c.createStatement().executeQuery(query);
    }



    @Tas(testBeds = @TestBed(name = AssistedTriageTestbed.class, executeOn = AssistedTriageTestbed.SA_MASTER), size = SizeType.MEDIUM, owner = "patpr15")
    @Test(groups = {"pipeorgan_generic_tests"})
    private void storyEvidencesIntegrity_TestCase() throws Exception {

        Common common = new Common();
        Timestamp Start_Time = common.getCurrentTimeinISO8601Format(-2);
        Timestamp End_Time = common.getCurrentTimeinISO8601Format(1);

        String queryAlert =
            "select count(*) as Count from at_evidences where vertex_id = 'ATC:PipeOrgan Application:SERVLET:ExecutorServlet_5|service:"
                + colletcorHost
                + ":Tomcat Agent:Tomcat' and type = 'AlertEventStatement' "
                + "and start_time > "
                + "'"
                + common.timestamp2String(Start_Time)
                + "'"
                + " and end_time < " + "'" + common.timestamp2String(End_Time) + "'";
        
       log.info("Printing ALertEventStatement SQL Query");
       log.info(queryAlert);

        ResultSet rs = queryDb(agcHost, queryAlert);
        rs.next();
        int alertEventCount = rs.getInt("Count");

        String queryUvb =
            "select count(*) as Count from at_evidences where vertex_id = 'ATC:PipeOrgan Application:SERVLET:ExecutorServlet_5|service:"
                + colletcorHost
                + ":Tomcat Agent:Tomcat' and type = 'UvbEventStatement' "
                + "and start_time > "
                + "'"
                + common.timestamp2String(Start_Time)
                + "'"
                + " and end_time < " + "'" + common.timestamp2String(End_Time) + "'";
        
        log.info("Printing UvbEventStatement SQL Query");
        log.info(queryUvb);

        rs = queryDb(agcHost, queryUvb);
        rs.next();
        int uvbEventCount = rs.getInt("Count");

        if (uvbEventCount < 1 || alertEventCount < 1)
            Assert.fail("Evidences Integrity Test Failed. Found AlertEvent count: '" + alertEventCount +"' and UVBEvent Count: '"+ uvbEventCount + "' when checked for verted_id PipeOrgan Application:SERVLET:ExecutorServlet_5");

    }
}
