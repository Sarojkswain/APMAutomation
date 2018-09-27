package com.ca.apm.browseragent.testng;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.browseragent.testsupport.collector.BATestCollector;
import com.ca.apm.browseragent.testsupport.collector.handler.GetFileContextHandler;
import com.ca.apm.browseragent.testsupport.collector.handler.MetricCollectionContextHandler;
import com.ca.apm.browseragent.testsupport.collector.handler.MetricDisplayContextHandler;
import com.ca.apm.browseragent.testsupport.collector.handler.ProfileConfigContextHandler;
import com.ca.apm.browseragent.testsupport.collector.handler.RootContextHandler;
import com.ca.apm.browseragent.testsupport.collector.pojo.Attributes;
import com.ca.apm.browseragent.testsupport.collector.pojo.Configuration;
import com.ca.apm.browseragent.testsupport.collector.util.AbstractPayloadType;
import com.ca.apm.browseragent.testsupport.collector.util.BATestCollectorUtils;
import com.ca.apm.browseragent.testsupport.collector.util.MetricPostListener;
import com.ca.apm.browseragent.testsupport.collector.util.MetricPostRecord;
import com.ca.apm.eum.datamodel.App;
import com.ca.apm.eum.datamodel.ClientInfo;
import com.ca.apm.eum.datamodel.Creator;
import com.ca.apm.eum.datamodel.EUM;
import com.ca.apm.eum.datamodel.ProfileInfo;

/**
 * This class attempts to test some of the more important functionality of the collector.
 * While the collector itself will be used in TAS, these unit tests will ensure fundamental
 * Expectations of the collector work. For example, the collector will throw a timeout after
 * a specified time limit. Validating these core features will help ensure tas tests
 * "passing" are not passing due to a broken collector
 * 
 *
 * @author Doug Briere
 */


public class BATestCollectorTests {

    private static final int DEFAULT_PORT = 5000;
    private static final String DEFAULT_PORT_STRING = Integer.toString(DEFAULT_PORT);



    private static final String PROFILE_PREFIX = "profile.";
    private static final String PROFILE_SUFFIX = ".json";

    private static final String DEFAULT_PROFILE_FILE_NAME = PROFILE_PREFIX
        + BATestCollectorUtils.DEFAULT_TENANT + "." + BATestCollectorUtils.DEFAULT_APP
        + PROFILE_SUFFIX;


    /**
     * This tests that when working directory and instance is passed null
     * values are set within the collector
     */

    @Test
    public void testNullWorkingDir() {
        BATestCollector baTestCollector = null;
        try {

            baTestCollector = new BATestCollector(null, null);

            Assert.assertTrue(baTestCollector.WORKING_DIRECTORY != null);
            Assert.assertTrue(baTestCollector.COLLECTOR_NAME != null);

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            if (baTestCollector != null) {
                baTestCollector.stopServer();
            }
        }
    }


    /**
     * This tests that when null is passed for tenant and app , defaults are used
     * values are set within the collector
     */

    @Test
    public void testNullTenantAndApp() {
        BATestCollector baTestCollector = null;
        try {

            String collectorWorkingDir = BATestCollectorUtils.getCollectorWorkingDir();

            baTestCollector =
                new BATestCollector(DEFAULT_PORT, null, null, collectorWorkingDir,
                    "testNullTenantAndApp");

            Assert.assertTrue(BATestCollectorUtils.DEFAULT_TENANT
                .equals(baTestCollector.DEFAULT_TENANT_ID));
            Assert.assertTrue(BATestCollectorUtils.DEFAULT_APP
                .equals(baTestCollector.DEFAULT_APP_ID));

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            if (baTestCollector != null) {
                baTestCollector.stopServer();
            }
        }
    }

    /**
     * This method deliberately waits for one metric post that will never arrive.
     * The configured wait time is random and on timeout exception (expected) the elapsed test time
     * is compared to see if falls within range.
     */

    @Test
    public void testWaitMethodForTimeout() {
        BATestCollector baTestCollector = null;
        Random random = new Random();
        long startTime = 0;

        // Make things interesting, timeout will be 2 to 10 seconds;
        long waitTime = random.nextInt(8000) + 2000;

        try {

            baTestCollector = getBATestCollector("testWaitMethodForTimeout");

            startTime = System.currentTimeMillis();
            java.util.Date dontCare = new java.util.Date();

            List<AbstractPayloadType> timeOutTypeList = new ArrayList<AbstractPayloadType>();
            AbstractPayloadType.TimeOutType type = new AbstractPayloadType.TimeOutType();
            // Yes set the count to 10 and confirm its still max value
            // Timeout type always returns MAX_VALUE
            type.setCount(10);
            Assert.assertTrue(type.getCount() == Integer.MAX_VALUE);

            // Timeout type derives from AnyType and is always true
            Assert.assertTrue(type.calculateTypeCount(null) > 0);

            timeOutTypeList.add(type);

            baTestCollector.waitForNextNotification(waitTime, dontCare, timeOutTypeList);
        } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
            // We expected to time out, here we want to see if the exception was thrown
            // in the waitTime we passed. Due to thread timing allow +/- on either side
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;

            long lowerBound = waitTime - 100;
            long upperBound = waitTime + 1000;

            // Give some tolerable bound to accept...
            boolean inBound = (lowerBound < elapsedTime && upperBound > elapsedTime);

            Assert.assertTrue(inBound);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            if (baTestCollector != null) {
                baTestCollector.stopServer();
            }
        }
    }

    /**
     * This test method will wait for an expected count. Fail the test if timeout is reached
     */

    @Test
    public void testWaitMethodForCount() {
        BATestCollector baTestCollector = null;
        try {

            final int TEST_COUNT_SIZE = 10;

            baTestCollector = getBATestCollector("testWaitMethodForCount");

            // Put 5 items onto the cache (random stuff before the test started)
            // These will use some current last updated
            List<EUM> eumList = createBakedEUMObjectList(5, 100);
            for (EUM eum : eumList) {
                postEUMToMetricCollection(eum, baTestCollector);
            }

            // The metrics above is to intentionally create some cache items.
            // this small sleep is to ensure they have been received and processed
            // with a received time before the testStart date below.
            sleepFor(1000);

            Configuration config = baTestCollector.getConfiguration();
            final long configLastUpdated = config.getLastUpdated();

            // Mark the start of this test.
            java.util.Date testStart = new Date();

            // lets put one on the cache that will be there
            postEUMToMetricCollection(createBakedEUMObject(configLastUpdated), baTestCollector);
            // same as above, ensure its processed. In this case before we enter
            // waitForNextNotification
            sleepFor(1000);

            final BATestCollector baTestCollectorFinal = baTestCollector;

            final AtomicInteger metricsSent = new AtomicInteger(0);

            Thread t = new Thread(new Runnable() {
                public void run() {
                    // First sleep about 2 seconds allow the waitFor method to enter its waiting
                    // state.

                    try {
                        Thread.currentThread().sleep(2000);

                        System.out.println("Worker thread going to add records");

                        // For the one that represent the "test" use lastUpdated from config
                        // Yes send 10 configs, even though wait below will stop once 9 from here
                        // has been sent because already 1 record in cache
                        for (int i = 0; i < TEST_COUNT_SIZE; i++) {

                            // need to run before because as soon as postEUMToMetricCollection runs
                            // the waitMethod will unblock itself
                            synchronized (metricsSent) {
                                int currentValue = metricsSent.incrementAndGet();
                                System.out.println("METRIC ADDED # " + currentValue);
                            }

                            BATestCollectorTests.this.postEUMToMetricCollection(
                                createBakedEUMObject(configLastUpdated), baTestCollectorFinal);
                        }

                    } catch (Exception e) {
                        Assert.fail(e.getMessage());
                    }
                }

            });
            t.start();

            System.out.println("Going to enter wait state ");
            List<MetricPostRecord> recordList =
                baTestCollector.waitForNextNotification(10000, testStart,
                    getAnyTypeList(TEST_COUNT_SIZE));
            System.out.println("DONE WAITING to enter wait state ");
            // We expect TEST_COUNT_SIZE records
            Assert.assertEquals(recordList.size(), TEST_COUNT_SIZE);

            // Allow the simulated "worker" thread above to finish
            sleepFor(1000);

            int threadSent = 0;
            synchronized (metricsSent) {
                threadSent = metricsSent.get();
            }
            // Confirm 10 records was sent despite just needing 9.. the 10th was sent outside
            // the worker thread
            Assert.assertEquals(threadSent, TEST_COUNT_SIZE);

            long startDateAsLong = testStart.getTime();

            for (MetricPostRecord record : recordList) {
                EUM eum = record.getEumObject();
                App app = eum.getApp();

                // Might as well check these since we know we set them
                Assert.assertEquals(app.getId(), BATestCollectorUtils.DEFAULT_APP);
                Assert.assertEquals(app.getTenantId(), BATestCollectorUtils.DEFAULT_TENANT);

                ProfileInfo profileInfo = app.getProfileInfo();
                long lastUpdatedAt = profileInfo.getLastUpdatedAt();

                // get the eum as string and just at least verify it contains the last updated
                // dont want to reference brtm util for the full conversion
                String eumAsString = record.getJsonMetricPost();
                Assert.assertTrue(eumAsString.contains(Long.toString(lastUpdatedAt)));

                // Do the same for toString just to hit the code coverage ;)
                String recordToString = record.toString();
                Assert.assertTrue(recordToString.contains(Long.toString(lastUpdatedAt)));

                // Verify that all the returned configs (lastUpdatedAt) have the same
                // configLastUpdated
                Assert.assertEquals(lastUpdatedAt, configLastUpdated);

                // Now also confirm that all records we see are on or after the start of the test
                Date recordRecv = record.getReceivedTime();
                Assert.assertTrue(recordRecv.getTime() >= startDateAsLong);
            }

        } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
            Assert.fail(timeoutException.getMessage());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            if (baTestCollector != null) {
                baTestCollector.stopServer();
            }
        }
    }

    /**
     * Test the wait method for an invalid count
     */

    @Test
    public void testWaitForInvalidCount() {

        // Test wait on 0
        boolean seenExceptionOnZero = false;
        BATestCollector baTestCollector = null;
        try {
            baTestCollector = getBATestCollector("testWaitForInvalidCount");
            // pass zero for wait count which is invalid!
            AbstractPayloadType type = new AbstractPayloadType() {
                public int calculateTypeCount(MetricPostRecord recordToTest) {
                    return 1;
                }

                public int getCount() {
                    return 0; // Yes, to get a zero sent into wait method
                }
            };
            List<AbstractPayloadType> list = new ArrayList<AbstractPayloadType>();
            list.add(type);
            baTestCollector.waitForNextNotification(5000, new Date(), list);
        } catch (IllegalArgumentException e) {
            seenExceptionOnZero = true;
        } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
            Assert.fail(timeoutException.getMessage());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }


        Assert.assertEquals(seenExceptionOnZero, true);

        // Test wait on -1
        boolean seenExceptionOnBelowZero = false;
        try {
            // pass -1 for wait count which is invalid!

            AbstractPayloadType type = new AbstractPayloadType() {
                public int calculateTypeCount(MetricPostRecord recordToTest) {
                    return 1;
                }

                public int getCount() {
                    return -1; // Yes, to get a -1 sent into wait method
                }
            };
            List<AbstractPayloadType> list = new ArrayList<AbstractPayloadType>();
            list.add(type);

            baTestCollector.waitForNextNotification(5000, new Date(), list);
        } catch (IllegalArgumentException e) {
            seenExceptionOnBelowZero = true;
        } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
            Assert.fail(timeoutException.getMessage());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            if (baTestCollector != null) {
                baTestCollector.stopServer();
            }
        }
        Assert.assertEquals(seenExceptionOnBelowZero, true);

        // Test the AbstractPayloadType
        boolean exceptionSeenOnZero = false;
        try {
            AbstractPayloadType type = new AbstractPayloadType() {
                public int calculateTypeCount(MetricPostRecord recordToTest) {
                    return 1;
                }
            };

            // test count can be set to a valid value
            int valueCount = 10;
            type.setCount(valueCount);
            Assert.assertTrue(type.getCount() == valueCount);

            // properly decremented
            type.decrementCount();
            valueCount -= 1;
            Assert.assertTrue(type.getCount() == valueCount);

            // Do last this will throw exception
            type.setCount(0);
        } catch (IllegalArgumentException e) {
            exceptionSeenOnZero = true;
        }
        Assert.assertEquals(exceptionSeenOnZero, true);

        // Test the AbstractPayloadType for -1
        boolean exceptionSeenOnNegative = false;
        try {
            AbstractPayloadType type = new AbstractPayloadType() {
                public int calculateTypeCount(MetricPostRecord recordToTest) {
                    return 1;
                }
            };

            // Do last this will throw exception
            type.setCount(-1);
        } catch (IllegalArgumentException e) {
            exceptionSeenOnNegative = true;
        }
        Assert.assertEquals(exceptionSeenOnNegative, true);

    }

    /**
     * This test method tests the 204 contract which is:
     * When the posted EUM data contains an older last updated value than the current config
     * then return 204 (not 200)
     */

    @Test
    public void test204Response() {
        BATestCollector baTestCollector = null;
        try {

            // Test plan, make two metric posts and verify each is 200. Why two posts? Well just to
            // ensure the second post isnt 204.
            // Then before the third post make a change to the config, the response code on the
            // third metric post should be 204.

            baTestCollector = getBATestCollector("test204Response");

            Configuration config = baTestCollector.getConfiguration();
            long lastUpdated = config.getLastUpdated();

            // First make two posts using the lastUpdated of the config
            int responseCode =
                postEUMToMetricCollection(createBakedEUMObject(lastUpdated), baTestCollector);
            Assert.assertEquals(responseCode, BATestCollectorUtils.HTTP_OK);

            responseCode =
                postEUMToMetricCollection(createBakedEUMObject(lastUpdated), baTestCollector);
            Assert.assertEquals(responseCode, BATestCollectorUtils.HTTP_OK);


            // Now change the config

            Attributes attrs = config.getBaAttributes();
            attrs.setPageLoadMetricsThreshold(2000);
            // This call will change the lastupdated time on the config
            baTestCollector.updateConfiguration(config, "test1234");

            // Again use the lastUpdated of the original config... because the client still doesnt
            // know
            // about the updated config (yet).
            responseCode =
                postEUMToMetricCollection(createBakedEUMObject(lastUpdated), baTestCollector);
            // When the above line posts the latest metrics, lastUpdated will be in the past
            // compared to
            // the more recent lastUpdated in the updated configuration
            Assert.assertEquals(responseCode, BATestCollectorUtils.HTTP_OK_NO_CONTENT);

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            if (baTestCollector != null) {
                baTestCollector.stopServer();
            }
        }
    }

    /**
     * This method tests the /metricdisplay end point that dumps the cache and total record count
     */

    @Test
    public void testGetMetricDisplay() {

        final String GET_SEARCH_MESSAGE = "Total records cached: ";

        BATestCollector baTestCollector = null;
        try {
            baTestCollector = getBATestCollector("testGetMetricDisplay");

            Configuration config = baTestCollector.getConfiguration();
            long lastUpdated = config.getLastUpdated();
            int responseCode =
                postEUMToMetricCollection(createBakedEUMObject(lastUpdated), baTestCollector);
            Assert.assertEquals(responseCode, BATestCollectorUtils.HTTP_OK);

            String url =
                baTestCollector.getHostAddress()
                    + MetricDisplayContextHandler.METRIC_DISPLAY_CONTEXT;
            String fullResponse = performGET(url, null);

            // Not the best, but gets the job done...
            Assert.assertTrue(fullResponse.contains(GET_SEARCH_MESSAGE + "1"));

            // look for the last updated config information
            Assert.assertTrue(fullResponse.contains(String.valueOf(lastUpdated)));

            // Now lets add 5 more records and verify a total of 6
            for (int i = 0; i < 5; i++)
                postEUMToMetricCollection(createBakedEUMObject(lastUpdated), baTestCollector);

            fullResponse = performGET(url, null);

            // Not the best, but gets the job done...
            Assert.assertTrue(fullResponse.contains(GET_SEARCH_MESSAGE + "6"));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            if (baTestCollector != null) {
                baTestCollector.stopServer();
            }
        }
    }

    /**
     * Tests the root context host:port/
     */

    @Test
    public void testRootContext() {
        BATestCollector baTestCollector = null;
        try {
            baTestCollector = getBATestCollector("testRootContext");
            String url = baTestCollector.getHostAddress() + RootContextHandler.HOME_ROOT_CONTEXT;
            String fullResponse = performGET(url, null);

            boolean hasRoot = fullResponse.contains("returns the listing you are viewing now");
            Assert.assertTrue(hasRoot);

            boolean hasDisplay =
                fullResponse.contains("Dump of EUM formated(pretty) json from browser agent");
            Assert.assertTrue(hasDisplay);

            boolean hasCollection = fullResponse.contains("EUM formated json from browser agent");
            Assert.assertTrue(hasCollection);

            boolean hasGetFile =
                fullResponse.contains("returns file by that name searched in working directory");
            Assert.assertTrue(hasGetFile);

            boolean hasConfig = fullResponse.contains("returns all configs");
            Assert.assertTrue(hasConfig);

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            if (baTestCollector != null) {
                baTestCollector.stopServer();
            }
        }
    }

    /**
     * Tests adding and removing of custom notifications, which is also used internally for the wait
     * method
     */

    @Test
    public void testAddRemoveListener() {
        BATestCollector baTestCollector = null;
        try {
            baTestCollector = getBATestCollector("testAddRemoveListener");

            // Add our own listener and record the responses
            final List<MetricPostRecord> recordList = new ArrayList<MetricPostRecord>();
            MetricPostListener l = new MetricPostListener() {
                public void jsonUpdate(MetricPostRecord record) {
                    synchronized (recordList) {
                        recordList.add(record);
                    }
                }
            };
            baTestCollector.addMetricPostListener(l);

            // start a test by adding (posting) two records
            Date testStart = new Date();
            final int RECORDS_TO_SEND = 2;
            List<EUM> eumList = createBakedEUMObjectList(RECORDS_TO_SEND, 100);
            for (EUM eum : eumList) {
                postEUMToMetricCollection(eum, baTestCollector);
            }

            // Sleep for two seconds, put these records on the cache...
            // testWaitMethodForCount already tests when they have not arrived...
            sleepFor(2000);

            // now wait for these two records, technically they already arrived and due to
            // sleep, already waiting in the cache (wont enter countdown latch situation)
            List<MetricPostRecord> responseRecordList =
                baTestCollector.waitForNextNotification(10000, testStart,
                    getAnyTypeList(RECORDS_TO_SEND));

            synchronized (recordList) {
                Assert.assertEquals(responseRecordList.size(), recordList.size());

                // now both are equal, compare each..
                for (int i = 0; i < responseRecordList.size(); i++)
                    Assert.assertTrue(responseRecordList.get(i).equals(recordList.get(i)));
            }

            //
            // Now remove the listener and repeat, but verify no records
            //
            baTestCollector.removeMetricPostListener(l);

            // Empty out the existing records, this should stay 0 because listner now removed
            synchronized (recordList) {
                recordList.clear();
            }

            testStart = new Date();
            eumList = createBakedEUMObjectList(RECORDS_TO_SEND, 100);
            for (EUM eum : eumList) {
                postEUMToMetricCollection(eum, baTestCollector);
            }

            // now wait for these two records, technically they already arrived and
            // probably waiting in the cache (wont enter countdown latch situation)
            responseRecordList =
                baTestCollector.waitForNextNotification(10000, testStart,
                    getAnyTypeList(RECORDS_TO_SEND));

            synchronized (recordList) {
                // The response should still be 2 more records...
                Assert.assertEquals(responseRecordList.size(), RECORDS_TO_SEND);

                // But our listener was removed so it should remain 0
                Assert.assertEquals(recordList.size(), 0);
            }

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            if (baTestCollector != null) {
                baTestCollector.stopServer();
            }
        }
    }

    /**
     * Tests the static main method used by bat file
     */

    @Test
    public void testStaticMainStartAndSnippet() {

        try {
            // first create two two temp files, the first contains <head> the second contains <HEAD>
            String content1 = "<head></head>";
            String content2 = "<HEAD></HEAD>";

            //
            File file1 = File.createTempFile("lowerCaseHead", "html");
            BATestCollectorUtils.writeStringToFile(content1, file1);

            File file2 = File.createTempFile("upperCaseHead", "html");
            BATestCollectorUtils.writeStringToFile(content2, file2);

            String pathToEdit = file1.getParent();
            // should have same parent
            Assert.assertTrue(pathToEdit.equals(file2.getParent()));

            String filesToEdit = file1.getName() + "," + file2.getName();

            // DONT DO FOR THIS TEST, see call to "main" below
            // baTestCollector = getBATestCollector("testStaticMainStart");
            String[] args =
                new String[] {DEFAULT_PORT_STRING, BATestCollectorUtils.DEFAULT_TENANT,
                        BATestCollectorUtils.DEFAULT_APP,
                        BATestCollectorUtils.getCollectorWorkingDir(), "testStaticMainStart",
                        pathToEdit, filesToEdit};

            // Confirm first its false
            Assert.assertTrue(BATestCollector.staticMainBATestCollector == null);

            // If works should not throw exception
            BATestCollector.main(args);

            // Now should be non null
            Assert.assertTrue(BATestCollector.staticMainBATestCollector != null);

            final int RECORDS_TO_SEND = 2;
            Date testStart = new Date();
            List<EUM> eumList = createBakedEUMObjectList(RECORDS_TO_SEND, 100);
            for (EUM eum : eumList) {
                postEUMToMetricCollection(eum, BATestCollector.staticMainBATestCollector);
            }

            // now wait for these two records, technically they already arrived and
            // probably waiting in the cache (wont enter countdown latch situation)
            List<MetricPostRecord> responseRecordList =
                BATestCollector.staticMainBATestCollector.waitForNextNotification(10000, testStart,
                    getAnyTypeList(RECORDS_TO_SEND));

            Assert.assertEquals(responseRecordList.size(), RECORDS_TO_SEND);


            // Now lets confirm the contents of the snippet
            String file1Updated = BATestCollectorUtils.readStringFromFile(file1);
            System.out.println(file1Updated);
            Assert.assertTrue(file1Updated.contains(BATestCollector.staticMainBATestCollector
                .getSnippetCode()));

            String file2Updated = BATestCollectorUtils.readStringFromFile(file2);
            Assert.assertTrue(file2Updated.contains(BATestCollector.staticMainBATestCollector
                .getSnippetCode()));


        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            if (BATestCollector.staticMainBATestCollector != null) {
                BATestCollector.staticMainBATestCollector.stopServer();
            }
        }
    }

    /**
     * Test config retrieval host:port/configs and singleton: host:port/configs/tenant/app/
     */

    @Test
    public void testConfigGet() {
        BATestCollector baTestCollector = null;
        try {
            String testName = "testConfigGet";
            baTestCollector = getBATestCollector(testName);

            // Full path to the default config
            String fileName =
                baTestCollector.WORKING_DIRECTORY + File.separator + testName + File.separator
                    + "profiles" + File.separator + DEFAULT_PROFILE_FILE_NAME;

            // default profile by file read
            String configContents = BATestCollectorUtils.readStringFromFile(new File(fileName));

            // All configs by web request
            String url =
                baTestCollector.getHostAddress() + ProfileConfigContextHandler.CONFIGS_CONTEXT;
            String fullResponse = performGET(url, null);

            // We should see an entry for the default profile config
            Assert.assertTrue(fullResponse.contains(DEFAULT_PROFILE_FILE_NAME));
            // System.out.println("fullResponse " + fullResponse);
            // System.out.println("confingContents " + configContents);

            // fullResponse has html content, check only contains
            Assert.assertTrue(fullResponse.contains(configContents));

            // Now change the url the specific config
            url =
                baTestCollector.getHostAddress() + ProfileConfigContextHandler.CONFIGS_CONTEXT
                    + "/" + BATestCollectorUtils.DEFAULT_TENANT + "/"
                    + BATestCollectorUtils.DEFAULT_APP;
            AtomicInteger response = new AtomicInteger();
            fullResponse = performGET(url, response);

            // now should see only the file contents
            Assert.assertEquals(fullResponse, configContents);

            // Now test invalid get
            url =
                baTestCollector.getHostAddress() + ProfileConfigContextHandler.CONFIGS_CONTEXT
                    + "/" + "somerandomtenantname" + "/" + "somerandomapp";
            response = new AtomicInteger();
            performGET(url, response);
            Assert.assertEquals(response.get(), BATestCollectorUtils.HTTP_NOT_FOUND);
            // System.out.println("fullResponse " + fullResponse + " response " + response);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            if (baTestCollector != null) {
                baTestCollector.stopServer();
            }
        }
    }

    /**
     * Testing of getting a file host:port/getfile/filename
     */

    @Test
    public void testGetFile() {
        BATestCollector baTestCollector = null;
        try {
            baTestCollector = getBATestCollector("testGetFile");

            String url =
                baTestCollector.getHostAddress() + GetFileContextHandler.GET_FILE_CONTEXT + "/"
                    + "whatalongstragetripithasbeenfilethatshouldntexist";
            AtomicInteger responseCode = new AtomicInteger(0);
            String fullResponse = performGET(url, responseCode);

            // confirm empty string and not found
            Assert.assertEquals(fullResponse, "");
            Assert.assertEquals(responseCode.get(), BATestCollectorUtils.HTTP_NOT_FOUND);

            final String FILE_NAME = "BA.js";
            url =
                baTestCollector.getHostAddress() + GetFileContextHandler.GET_FILE_CONTEXT + "/"
                    + FILE_NAME;
            responseCode = new AtomicInteger(0);
            fullResponse = performGET(url, responseCode);
            Assert.assertTrue(fullResponse.length() > 0);
            Assert.assertEquals(responseCode.get(), BATestCollectorUtils.HTTP_OK);

            // Big file is located at top of working directory
            String bigFileLoc = baTestCollector.WORKING_DIRECTORY + File.separator + FILE_NAME;
            String bigFileAsString = BATestCollectorUtils.readStringFromFile(new File(bigFileLoc));

            Assert.assertEquals(fullResponse, bigFileAsString);
            // System.out.println("fullResponse" + fullResponse.length());
            // System.out.println("bigFileAsString" + bigFileAsString.length());
            // System.out.println("fullResponse: " + fullResponse);
            // System.out.println("bigFileAsString: " + bigFileAsString);

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            if (baTestCollector != null) {
                baTestCollector.stopServer();
            }
        }
    }

    /**
     * Tests the response code handler host:port/codes/<code>
     */

    @Test
    public void testResponseCode() {
        BATestCollector baTestCollector = null;
        try {
            baTestCollector = getBATestCollector("testResponseCode");

            Random random = new Random();
            // Generate a random http status code, but do choose something in the valid range
            // otherwise it seems the http client does error out
            int codeToTest = random.nextInt(3) + 200;
            String url =
                baTestCollector.getHostAddress()
                    + BATestCollector.ResponseCodes.RESPONSE_CODE_CONTEXT + "/" + codeToTest;
            System.out.println("url " + url);
            AtomicInteger responseCode = new AtomicInteger(0);
            String fullResponse = performGET(url, responseCode);
            Assert.assertEquals(responseCode.get(), codeToTest);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (baTestCollector != null) {
                baTestCollector.stopServer();
            }
        }
    }

    /**
     * testing of posting to create a new config post: host:port/configs/tenant/app
     * cases:
     * a) no optional content
     * b) with optional content
     * c) with optional content, but invalid
     */

    @Test
    public void testConfigPost() {
        BATestCollector baTestCollector = null;
        try {
            final String INSTANCE_NAME = "testConfigPost";
            baTestCollector = getBATestCollector(INSTANCE_NAME);

            // try to post to defaulttenant defaultapp should get error
            int resp =
                postOrPutConfigToConfigs("", BATestCollectorUtils.DEFAULT_TENANT,
                    BATestCollectorUtils.DEFAULT_APP, baTestCollector, true);
            Assert.assertEquals(resp, BATestCollectorUtils.HTTP_NOT_FOUND);

            // post to new no optional content, read actual file on disk verify equal to get
            String tenant2 = BATestCollectorUtils.DEFAULT_TENANT + "2";
            String app2 = BATestCollectorUtils.DEFAULT_APP + "2";
            resp = postOrPutConfigToConfigs("", tenant2, app2, baTestCollector, true);
            Assert.assertEquals(resp, BATestCollectorUtils.HTTP_OK);

            // the file should have been created
            String profileOnDisk =
                readProfileFile(baTestCollector.WORKING_DIRECTORY, INSTANCE_NAME, tenant2, app2);

            // the new config, perform get
            String url =
                baTestCollector.getHostAddress() + ProfileConfigContextHandler.CONFIGS_CONTEXT
                    + "/" + tenant2 + "/" + app2;
            AtomicInteger response = new AtomicInteger();
            String fullResponse = performGET(url, response);

            Assert.assertEquals(fullResponse, profileOnDisk);

            // post to new with optional content, confirm optional content
            String myCollectionURL = "http://somerandomurl";
            Configuration defaultConfig =
                BATestCollectorUtils.getDefaultConfiguration(myCollectionURL);

            String tenant3 = BATestCollectorUtils.DEFAULT_TENANT + "3";
            String app3 = BATestCollectorUtils.DEFAULT_APP + "3";
            final String VALID_CONTENT = BATestCollectorUtils.getPrettyJson(defaultConfig);
            resp = postOrPutConfigToConfigs(VALID_CONTENT, tenant3, app3, baTestCollector, true);
            Assert.assertEquals(resp, BATestCollectorUtils.HTTP_OK);

            // the file should have been created
            profileOnDisk =
                readProfileFile(baTestCollector.WORKING_DIRECTORY, INSTANCE_NAME, tenant3, app3);

            // the new config, perform get
            url =
                baTestCollector.getHostAddress() + ProfileConfigContextHandler.CONFIGS_CONTEXT
                    + "/" + tenant3 + "/" + app3;
            response = new AtomicInteger();
            fullResponse = performGET(url, response);

            Assert.assertEquals(fullResponse, profileOnDisk);
            Assert.assertTrue(fullResponse.contains(myCollectionURL));

            // post to new with invalid optional content confirm
            String tenant4 = BATestCollectorUtils.DEFAULT_TENANT + "4";
            String app4 = BATestCollectorUtils.DEFAULT_APP + "4";
            final String INVALID_CONFIG = "{ Im invalid config basdsa ad ad asd asd asd,,,,,, }}}}";
            resp = postOrPutConfigToConfigs(INVALID_CONFIG, tenant4, app4, baTestCollector, true);
            Assert.assertEquals(resp, BATestCollectorUtils.HTTP_OK);

            // the file should have been created
            profileOnDisk =
                readProfileFile(baTestCollector.WORKING_DIRECTORY, INSTANCE_NAME, tenant4, app4);

            // the new config, perform get
            url =
                baTestCollector.getHostAddress() + ProfileConfigContextHandler.CONFIGS_CONTEXT
                    + "/" + tenant4 + "/" + app4;
            response = new AtomicInteger();
            fullResponse = performGET(url, response);

            Assert.assertEquals(fullResponse, profileOnDisk);
            Assert.assertEquals(fullResponse, INVALID_CONFIG);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (baTestCollector != null) {
                baTestCollector.stopServer();
            }
        }
    }


    /**
     * Testing of updating a config through put
     * 
     */

    @Test
    public void testConfigPut() {
        BATestCollector baTestCollector = null;
        try {
            final String INSTANCE_NAME = "testConfigPut";
            baTestCollector = getBATestCollector(INSTANCE_NAME);

            // put to config doesnt exist
            String tenant2 = BATestCollectorUtils.DEFAULT_TENANT + "2";
            String app2 = BATestCollectorUtils.DEFAULT_APP + "2";
            int resp = postOrPutConfigToConfigs("", tenant2, app2, baTestCollector, false);
            Assert.assertEquals(resp, BATestCollectorUtils.HTTP_NOT_FOUND);

            // put valid content to exiting config
            Configuration config = baTestCollector.getConfiguration();
            Attributes attrs = config.getBaAttributes();
            String urlString = "http://somelinkeand I dont care what value";
            attrs.setCollectorUrl(urlString);
            String validPut = BATestCollectorUtils.getPrettyJson(config);
            resp =
                postOrPutConfigToConfigs(validPut, BATestCollectorUtils.DEFAULT_TENANT,
                    BATestCollectorUtils.DEFAULT_APP, baTestCollector, false);
            Assert.assertEquals(resp, BATestCollectorUtils.HTTP_OK);
            String profile =
                readProfileFile(baTestCollector.WORKING_DIRECTORY, INSTANCE_NAME,
                    BATestCollectorUtils.DEFAULT_TENANT, BATestCollectorUtils.DEFAULT_APP);

            String url =
                baTestCollector.getHostAddress() + ProfileConfigContextHandler.CONFIGS_CONTEXT
                    + "/" + BATestCollectorUtils.DEFAULT_TENANT + "/"
                    + BATestCollectorUtils.DEFAULT_APP;
            AtomicInteger response = new AtomicInteger();
            String fullResponse = performGET(url, response);

            Assert.assertEquals(profile, fullResponse);
            Assert.assertTrue(fullResponse.contains(urlString));

            // put invalid content to existing config
            String invalidPut = "{ adasd asd as dasd as  ,,,,, asdasd I dont care on content ";
            resp =
                postOrPutConfigToConfigs(invalidPut, BATestCollectorUtils.DEFAULT_TENANT,
                    BATestCollectorUtils.DEFAULT_APP, baTestCollector, false);
            Assert.assertEquals(resp, BATestCollectorUtils.HTTP_OK);
            profile =
                readProfileFile(baTestCollector.WORKING_DIRECTORY, INSTANCE_NAME,
                    BATestCollectorUtils.DEFAULT_TENANT, BATestCollectorUtils.DEFAULT_APP);

            url =
                baTestCollector.getHostAddress() + ProfileConfigContextHandler.CONFIGS_CONTEXT
                    + "/" + BATestCollectorUtils.DEFAULT_TENANT + "/"
                    + BATestCollectorUtils.DEFAULT_APP;
            response = new AtomicInteger();
            fullResponse = performGET(url, response);
            Assert.assertEquals(profile, fullResponse);
            Assert.assertEquals(profile, invalidPut);

            // now restore the config by posting valid config back to it
            resp =
                postOrPutConfigToConfigs(validPut, BATestCollectorUtils.DEFAULT_TENANT,
                    BATestCollectorUtils.DEFAULT_APP, baTestCollector, false);
            Assert.assertEquals(resp, BATestCollectorUtils.HTTP_OK);

            profile =
                readProfileFile(baTestCollector.WORKING_DIRECTORY, INSTANCE_NAME,
                    BATestCollectorUtils.DEFAULT_TENANT, BATestCollectorUtils.DEFAULT_APP);
            url =
                baTestCollector.getHostAddress() + ProfileConfigContextHandler.CONFIGS_CONTEXT
                    + "/" + BATestCollectorUtils.DEFAULT_TENANT + "/"
                    + BATestCollectorUtils.DEFAULT_APP;
            response = new AtomicInteger();
            fullResponse = performGET(url, response);

            Assert.assertEquals(profile, fullResponse);


            // Perform an update from the API, perform a get through the rest and confirm equal

            final String INVALID_FILE_EXT = "testinvalid";
            baTestCollector.updateConfiguration(invalidPut, BATestCollectorUtils.DEFAULT_TENANT,
                BATestCollectorUtils.DEFAULT_APP, INVALID_FILE_EXT);
            url =
                baTestCollector.getHostAddress() + ProfileConfigContextHandler.CONFIGS_CONTEXT
                    + "/" + BATestCollectorUtils.DEFAULT_TENANT + "/"
                    + BATestCollectorUtils.DEFAULT_APP;
            response = new AtomicInteger();
            fullResponse = performGET(url, response);
            Assert.assertEquals(fullResponse, invalidPut);

            // now post to the collector should get back 204, sends back 204 on ANY invalid config;
            int respAfterInvalid =
                postEUMToMetricCollection(createBakedEUMObject(), baTestCollector);
            Assert.assertEquals(respAfterInvalid, BATestCollectorUtils.HTTP_OK_NO_CONTENT);

            // now test the extension file was created and the original exists too

            String profileName =
                PROFILE_PREFIX + BATestCollectorUtils.DEFAULT_TENANT + "."
                    + BATestCollectorUtils.DEFAULT_APP + PROFILE_SUFFIX;
            String fileName =
                baTestCollector.WORKING_DIRECTORY + File.separator + INSTANCE_NAME + File.separator
                    + "profiles" + File.separator + profileName;
            File file = new File(fileName);
            boolean primaryFileExists = file.exists();
            Assert.assertTrue(primaryFileExists);


            String profileNameExt =
                PROFILE_PREFIX + BATestCollectorUtils.DEFAULT_TENANT + "."
                    + BATestCollectorUtils.DEFAULT_APP + "." + INVALID_FILE_EXT + PROFILE_SUFFIX;
            String fileNameExt =
                baTestCollector.WORKING_DIRECTORY + File.separator + INSTANCE_NAME + File.separator
                    + "profiles" + File.separator + profileNameExt;
            File fileExt = new File(fileNameExt);
            boolean extFileExists = fileExt.exists();
            Assert.assertTrue(extFileExists);

            System.out.println("file " + file);
            System.out.println("fileExt " + fileExt);

            // run delete and confirm both files are gone
            int deleteResponse =
                doDelete(baTestCollector, BATestCollectorUtils.DEFAULT_TENANT,
                    BATestCollectorUtils.DEFAULT_APP);
            Assert.assertEquals(deleteResponse, BATestCollectorUtils.HTTP_OK);

            // There is a belief that file delete in java can be queued. Lets
            // just wait a couple seconds just in case...
            sleepFor(2000);

            // Confirm two files are now gone
            primaryFileExists = file.exists();
            Assert.assertFalse(primaryFileExists);

            extFileExists = fileExt.exists();
            Assert.assertFalse(extFileExists);

            // delete again should fail, confirm not found response
            deleteResponse =
                doDelete(baTestCollector, BATestCollectorUtils.DEFAULT_TENANT,
                    BATestCollectorUtils.DEFAULT_APP);
            Assert.assertEquals(deleteResponse, BATestCollectorUtils.HTTP_NOT_FOUND);

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            if (baTestCollector != null) {
                baTestCollector.stopServer();
            }
        }
    }



    /**
     * ***********************************************************************************
     * END OF TESTS
     * ***********************************************************************************
     * 
     * 
     * 
     * 
     * 
     * ***********************************************************************************
     * START OF HELPER FUNCTIONS
     * ***********************************************************************************
     */


    /**
     * Helper to read the profile stored on disk
     * 
     * @param workingDir
     * @param instance - name of the collector
     * @param ten
     * @param app
     * @return String contents of the profile on disk
     * @throws IOException
     */

    private String readProfileFile(String workingDir, String instance, String ten, String app)
        throws IOException {
        String profileName = PROFILE_PREFIX + ten + "." + app + PROFILE_SUFFIX;
        String profileNameLoc =
            workingDir + File.separator + instance + File.separator + "profiles" + File.separator
                + profileName;
        String configAsString = BATestCollectorUtils.readStringFromFile(new File(profileNameLoc));
        return configAsString;
    }



    /**
     * Helper to return content from the passed url
     * 
     * @param url
     * @param returnStatusCode mutable to return status code, null if not interested
     * @return String get response
     */

    private String performGET(String url, AtomicInteger returnStatusCode) throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        System.out.println("httpClient" + httpClient.getClass().getName());

        HttpGet getRequest = new HttpGet(url);
        CloseableHttpResponse closeableHttpResponse =
            (CloseableHttpResponse) httpClient.execute(getRequest);

        // set response code for callers that are interested
        if (returnStatusCode != null) {
            returnStatusCode.set(closeableHttpResponse.getStatusLine().getStatusCode());
        }

        BufferedReader bufferedReader =
            new BufferedReader(
                new InputStreamReader(closeableHttpResponse.getEntity().getContent()));

        String fullResponse = BATestCollectorUtils.readString(bufferedReader);
        bufferedReader.close();
        closeableHttpResponse.close();

        return fullResponse;
    }

    /**
     * Posts an EUM object to the collector
     * 
     * @param eumObject
     * @param baTestCollector
     * @return int response code
     * @throws Exception
     */

    private int postEUMToMetricCollection(EUM eumObject, BATestCollector baTestCollector)
        throws Exception {
        HttpClient httpClient = null;
        int responseCode = 0;

        try {
            httpClient = HttpClientBuilder.create().build();

            HttpPost metricPost =
                new HttpPost(baTestCollector.getHostAddress()
                    + MetricCollectionContextHandler.METRIC_COLLECTION_CONTEXT);

            String postMetricString = BATestCollectorUtils.convertToJsonString(eumObject);
            metricPost.setEntity(new StringEntity(postMetricString));

            CloseableHttpResponse closeableHttpResponse =
                (CloseableHttpResponse) httpClient.execute(metricPost);


            responseCode = closeableHttpResponse.getStatusLine().getStatusCode();
            closeableHttpResponse.close();

            System.out.println("postEUMToMetricCollection  " + responseCode);
        } finally {
            if (httpClient != null) {
                // httpClient.close();
            }
        }

        return responseCode;
    }

    /**
     * Posts the config to the configs url for a tenant and app
     * 
     * @param configContent
     * @param tenant
     * @param app
     * @param baTestCollector
     * @return int response code
     * @throws Exception
     */

    private int postOrPutConfigToConfigs(String configContent, String tenant, String app,
        BATestCollector baTestCollector, boolean isPost) throws Exception {
        HttpClient httpClient = null;
        int responseCode = 0;

        try {
            httpClient = HttpClientBuilder.create().build();

            HttpEntityEnclosingRequestBase httpPostOrPut = null;

            String url =
                baTestCollector.getHostAddress() + ProfileConfigContextHandler.CONFIGS_CONTEXT
                    + "/" + tenant + "/" + app;

            if (isPost) {
                httpPostOrPut = new HttpPost(url);
            } else {
                httpPostOrPut = new HttpPut(url);
            }


            httpPostOrPut.setEntity(new StringEntity(configContent));

            CloseableHttpResponse closeableHttpResponse =
                (CloseableHttpResponse) httpClient.execute(httpPostOrPut);

            responseCode = closeableHttpResponse.getStatusLine().getStatusCode();
            closeableHttpResponse.close();

        } finally {
            if (httpClient != null) {
                // httpClient.close();
            }
        }

        return responseCode;
    }

    /**
     * Performs delete method for /configs/tenant/app
     * 
     * @param baTestCollector
     * @param tenant
     * @param app
     * @return int response code
     * @throws Exception
     */

    private int doDelete(BATestCollector baTestCollector, String tenant, String app)
        throws Exception {
        HttpClient httpClient = null;
        int responseCode = 0;

        try {
            httpClient = HttpClientBuilder.create().build();

            HttpDelete metricDelete =
                new HttpDelete(baTestCollector.getHostAddress()
                    + ProfileConfigContextHandler.CONFIGS_CONTEXT + "/" + tenant + "/" + app);


            CloseableHttpResponse closeableHttpResponse =
                (CloseableHttpResponse) httpClient.execute(metricDelete);

            responseCode = closeableHttpResponse.getStatusLine().getStatusCode();
            closeableHttpResponse.close();

        } finally {
            if (httpClient != null) {
                // httpClient.close();
            }
        }

        return responseCode;
    }


    /**
     * Returns a collector for testing. Callers must stop when done, best practice
     * in a finally block
     * 
     * @param testName
     * @return BATestCollector with instance of testName
     * @throws IOException
     */

    private BATestCollector getBATestCollector(final String testName) throws IOException {

        String collectorWorkingDir = BATestCollectorUtils.getCollectorWorkingDir();

        BATestCollector baTestCollector =
            new BATestCollector(DEFAULT_PORT, BATestCollectorUtils.DEFAULT_TENANT,
                BATestCollectorUtils.DEFAULT_APP, collectorWorkingDir, testName);

        return baTestCollector;
    }



    /**
     * Helper method to provide an EUM object, one that has needed information such as:
     * appId, tenant, timestamps configured, etc
     * 
     * @return
     */

    private EUM createBakedEUMObject() {
        return createBakedEUMObject(System.currentTimeMillis());
    }

    /**
     * Helper to create an EUM object with needed fields
     * 
     * @param timeStampToUse
     * @return
     */

    private EUM createBakedEUMObject(long timeStampToUse) {
        EUM returnEUM = new EUM();

        returnEUM.setClientInfo(new ClientInfo());

        App app = new App();
        app.setId(BATestCollectorUtils.DEFAULT_APP);
        app.setTenantId(BATestCollectorUtils.DEFAULT_TENANT);

        ProfileInfo profileInfo = new ProfileInfo();

        long timeStamp = timeStampToUse > 0 ? timeStampToUse : System.currentTimeMillis();

        profileInfo.setCreatedAt(timeStamp);
        profileInfo.setLastUpdatedAt(timeStamp);

        app.setProfileInfo(profileInfo);

        returnEUM.setApp(app);

        returnEUM.setCreator(new Creator());

        return returnEUM;
    }



    /**
     * Helper to create a list of eum objects.
     * 
     * @param count
     * @param sleepTime time to wait between each creation. This helps with having unique create
     *        dates
     * @return
     */

    private List<EUM> createBakedEUMObjectList(int count, long sleepTime) {
        List<EUM> returnList = new ArrayList<EUM>();

        for (int i = 0; i < count; i++) {
            // Create some padding from the last object
            sleepFor(sleepTime);
            EUM eum = createBakedEUMObject();

            // create some more padding
            sleepFor(sleepTime);

            returnList.add(eum);
        }

        return returnList;
    }

    /**
     * Helper to sleep
     * 
     * @param sleepTime
     */

    private void sleepFor(long sleepTime) {
        try {
            Thread.currentThread().sleep(sleepTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper for generating a list of AnyType with the passed expected count
     * 
     * @param expectedCount
     * @return
     */

    private List<AbstractPayloadType> getAnyTypeList(int expectedCount) {
        List<AbstractPayloadType> typesList = new ArrayList<AbstractPayloadType>();
        AbstractPayloadType.AnyType type = new AbstractPayloadType.AnyType();
        type.setCount(expectedCount);
        typesList.add(type);

        return typesList;
    }

}
