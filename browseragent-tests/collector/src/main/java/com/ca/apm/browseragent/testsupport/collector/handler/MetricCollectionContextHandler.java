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

package com.ca.apm.browseragent.testsupport.collector.handler;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.ca.apm.browseragent.testsupport.collector.BATestCollector;
import com.ca.apm.browseragent.testsupport.collector.pojo.Configuration;
import com.ca.apm.browseragent.testsupport.collector.util.AbstractPayloadType;
import com.ca.apm.browseragent.testsupport.collector.util.BATestCollectorUtils;
import com.ca.apm.browseragent.testsupport.collector.util.MetricPostListener;
import com.ca.apm.browseragent.testsupport.collector.util.MetricPostRecord;
import com.ca.apm.eum.datamodel.App;
import com.ca.apm.eum.datamodel.EUM;
import com.ca.apm.eum.datamodel.ProfileInfo;
import com.sun.net.httpserver.HttpExchange;

/***
 * This handler accepts the json metric data from ajax post requests
 *
 * @author Doug Briere
 */

public class MetricCollectionContextHandler extends AbstractHttpHandler {
    public static final String METRIC_COLLECTION_CONTEXT = "/metriccollection";

    private List<MetricPostListener> metricPostListenerList = new ArrayList<MetricPostListener>();
    private List<MetricPostRecord> masterMetricRecordList = new ArrayList<MetricPostRecord>();

    private final Logger LOGGER = Logger.getLogger(MetricCollectionContextHandler.class);

    private Date lastWaitDate = null;

    /**
     * If the number of expected repsonses do not occur in the specified timeout this exception is
     * thrown
     */

    public static class MetricCollectionTimeoutException extends Exception {
        public List<MetricPostRecord> partialResponseList = null;

        public MetricCollectionTimeoutException(List<MetricPostRecord> partialResponseList, String s) {
            super(s);
            this.partialResponseList = partialResponseList;
        }
    }


    private BATestCollector testCollector = null;

    public MetricCollectionContextHandler(BATestCollector testCollector) {
        this.testCollector = testCollector;
    }


    // implements HttpHandler

    public void handle(HttpExchange httpExchange) throws IOException {

        // From HttpExchange JavaDocs:
        // The typical life-cycle of a HttpExchange is shown in the sequence below.

        // 1. getRequestMethod() to determine the command
        // 2. getRequestHeaders() to examine the request headers (if needed)
        // 3. getRequestBody() returns a InputStream for reading the request body. After reading the
        // request body, the stream is close.
        // 4. getResponseHeaders() to set any response headers, except content-length
        // 5. sendResponseHeaders(int,long) to send the response headers. Must be called before next
        // step.
        // 6. getResponseBody() to get a OutputStream to send the response body. When the response
        // body has been written, the stream must be closed to terminate the exchange.

        try {
            String requestMethod = httpExchange.getRequestMethod();

            switch (requestMethod) {
                case BATestCollectorUtils.METHOD_OPTIONS:
                    handleOPTIONS(httpExchange);
                    break;
                case BATestCollectorUtils.METHOD_POST:
                    handlePOST(httpExchange);
                    break;
                default:
                    LOGGER.warn("Unknown request: " + requestMethod);
                    BATestCollectorUtils.sendResponse(httpExchange, requestMethod
                        + " method not supported for this end point",
                        BATestCollectorUtils.HTTP_SERVER_ERROR);
            }
        }
        // DO NOT REMOVE !!! The server that calls HttpHandler tries to log excetpion to a
        // configured logger
        // but when the logger isnt configured the exception doesnt go to standard output and not
        // seen. horrible!
        catch (Exception e) {
            LOGGER.error("Server error: ", e);
            BATestCollectorUtils.sendResponse(httpExchange, "Server error: " + e.getMessage(),
                BATestCollectorUtils.HTTP_SERVER_ERROR);
            throw e;
        }
    }


    private void handlePOST(HttpExchange httpExchange) throws IOException {
        LOGGER.debug("handlePOST start ");

        setResponseHeaders(httpExchange);

        InputStreamReader inputStream =
            new InputStreamReader(httpExchange.getRequestBody(), "utf-8");

        BufferedReader bufferedReader = new BufferedReader(inputStream);
        String entirePost = BATestCollectorUtils.readString(bufferedReader);

        LOGGER.debug("handlePOST getRequestBody:\n" + entirePost);


        try {
            MetricPostRecord record = new MetricPostRecord(entirePost);

            int status = determineStatus(record);

            BATestCollectorUtils.sendResponse(httpExchange, "", status);

            // Update listeners of new metric, yes technically could block this thread...
            // Perhaps of loaded to our own thread pool, for now we are the only listener.
            // Keeping after sendResponse since the listener might be the one in wait method
            // which could terminate the wait before sendResponse had a chance to fire
            notifyMetricPostListener(record);
        } catch (Exception ex) {
            LOGGER.error("handlePOST exception: ", ex);

            BATestCollectorUtils.sendResponse(httpExchange,
                "JSON failed to parse with exception message: " + ex.getMessage(),
                BATestCollectorUtils.HTTP_SERVER_ERROR);
        }

        LOGGER.debug("handlePOST end");
    }

    /**
     * This method will look at the passed MetricPostRecord and determine if status 200 or 204
     * should be sent back.
     * 204 is determined if the stored configuration (profile) has a last updated time greater than
     * the one in record.
     *
     * @param record
     * @return
     */

    private int determineStatus(MetricPostRecord record) {
        int returnStatus = BATestCollectorUtils.HTTP_OK;

        String appId = null;
        String tenantId = null;
        long lastUpdatedAt = 0;

        EUM eumObject = record.getEumObject();
        App app = eumObject != null ? eumObject.getApp() : null;

        if (app != null) {
            appId = app.getId();
            tenantId = app.getTenantId();

            ProfileInfo profileInfo = app.getProfileInfo();

            if (profileInfo != null) {
                lastUpdatedAt = profileInfo.getLastUpdatedAt();
                LOGGER.debug("determineStatus posted EUM record lastUpdatedAt: " + lastUpdatedAt);
            }
        }

        Configuration config = testCollector.getConfiguration(tenantId, appId, false);

        if (config != null) {
            if (config.getLastUpdated() > lastUpdatedAt) {
                LOGGER
                    .debug("determineStatus config on file has more recent time, getLastUpdated: "
                        + config.getLastUpdated());
                returnStatus = BATestCollectorUtils.HTTP_OK_NO_CONTENT;
            } else {
                LOGGER.debug("Not sending 204, config.getLastUpdated() " + config.getLastUpdated()
                    + " lastUpdatedAt: " + lastUpdatedAt);
            }


        } else {
            LOGGER.debug("determineStatus config was null, bogus config, sending 204");
            returnStatus = BATestCollectorUtils.HTTP_OK_NO_CONTENT;
        }

        return returnStatus;
    }



    /**
     * This call will block the calling thread and is designed to wait for the metric response(s)
     * from the user test.
     * Test code should look something like:
     *
     * 
     * BATestCollector baTestCollector = new BATestCollector(5000, // The port this collector will
     * // listen on locahost
     * "MyTestTenantId", // The default tenant used when not specified in other method calls
     * "MyTestAppId", // The default app used when not specified in other method calls
     * "C:\\MyCollectorWorkingDir",// A directory where the collector and write files (must
     * // exist)
     * "MyCollectorInstance" // A name for this collector instance, test id , etc
     * );
     * 
     * // Returns default config based on the default located in the working directory
     * // (defaultprofile.json) and ten & app above.
     * // If defaultprofile.json is not found one is memory is used
     * Configuration config = baTestCollector.getConfiguration();
     * 
     * // Make changes as needed that vary from the default
     * Attributes attrs = config.getBaAttributes();
     * attrs.setPageLoadMetricsThreshold(2000);
     * 
     * // This will commit the changes to the profile, this change is saved off in format
     * // profile.ten.app.extension
     * // where in this example extension is test1234-setPageLoadMetricsThreshold-change
     * baTestCollector.updateConfiguration(config, "test1234-setPageLoadMetricsThreshold-change");
     * 
     * // About to start the test, mark this moment in time
     * Date testStartTime = new Date();
     * 
     * //
     * // PUT SELENIUM CODE HERE
     * //
     * 
     * // Now wait on the response. We expect 10 responses to be returned in 20 seconds or less.
     * // Adjust the responses and waittime out as desired
     * 
     * try {
     * List<AbstractPayloadType> typeList =
     * PayloadUtils.generateTypesList(PayloadTypes.PAGE_TYPE);
     * List<MetricPostRecord> list =
     * baTestCollector.waitForNextNotification(20000, testStartTime, typeList);
     * 
     * // Actually look into the record, validate values
     * for (MetricPostRecord record : list) {
     * // TODO, run asserts
     * }
     * 
     * } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
     * List<MetricPostRecord> partialResponseList = timeoutException.partialResponseList;
     * 
     * for (MetricPostRecord record : partialResponseList) {
     * System.out.println(record);
     * }
     * 
     * // DO DEBUGGING HERE
     * System.out.println("partialResponseList size = " + partialResponseList.size());
     * 
     * // FAIL THE TEST!!!!!!!!!!
     * throw timeoutException;
     * }
     * 
     * // //////////////////////////////////////////////
     * // OPTIONAL: Generally each test should have its own collector started/stopped. However some
     * // tests
     * // are really extensions of the first and build on the origial. To make another call in the
     * // same method/collector
     * // Follow outline similar to above:
     * // 1. Make additional changes to the config
     * // 2. if yes to #1, call updateConfiguration
     * // 3. update start time, testStartTime = new Date();
     * // 4. Run second selenium test
     * // 5. call waitForNextNotification
     * // //////////////////////////////////////////////
     * 
     * 
     * //
     * // REQUIRED! stop the server after each test.
     * //
     * baTestCollector.stopServer();
     * 
     * 
     * 
     *
     *
     *
     * @param waitTimeout - the maximum time to wait for the typeList
     * @param afterOrOnDate - the responses that have arrived by this time. Should be the date when
     *        the test was started
     * @param typeList - the types list we are expecting once hit the method will
     *        return
     * @return List of MetricPostRecords
     */

    public List<MetricPostRecord> waitForNextNotification(long waitTimeout, Date afterOrOnDate,
        List<AbstractPayloadType> typeList) throws MetricCollectionTimeoutException {

        LOGGER.debug("waitForNextNotification start");

        int expectedResponseCount = 0;

        for (AbstractPayloadType type : typeList) {
            // Detected timeout type attempt to wait forever
            if (type.getCount() == Integer.MAX_VALUE) {
                expectedResponseCount = Integer.MAX_VALUE;
                break;
            } else if (type.getCount() == 0) {
                throw new IllegalArgumentException(
                    "AbstractPayloadType "
                        + type.getClass().getName()
                        + " zero count passed. "
                        + "please call PayloadUtils.generateTypesMap or generateTypesList for each test. Dont reuse");
            } else {
                expectedResponseCount += type.getCount();
            }
        }


        // Lets test the time this method takes and compare to the passed waitTimeout
        long internalStartTime = System.currentTimeMillis();

        if (expectedResponseCount <= 0) {
            throw new IllegalArgumentException("expectedResponseCount: " + expectedResponseCount
                + " is invalid. " + " Must be greater than zero");
        }

        final List<MetricPostRecord> returnList =
            Collections.synchronizedList(new ArrayList<MetricPostRecord>());

        final List<AbstractPayloadType> syncTypeList =
            Collections.synchronizedList(new ArrayList<AbstractPayloadType>(typeList));

        CountDownLatch countDownLatch = null;
        MetricPostListener metricPostListener = null;

        // Using this to hold the lock because this is used to make the master list change and
        // notification atomic
        // We want to search the current records and add our listener prior to the next record
        // update.
        // All other records we will look for will come from the attached listener below.
        synchronized (this) {

            // Ensure test writers pass a new date for each test. Random results could
            // happen otherwise..
            if (lastWaitDate != null && lastWaitDate.equals(afterOrOnDate)) {
                throw new IllegalArgumentException(
                    "waitForNextNotification invalid afterOrOnDate.  Please pass new Date instance for each wait call");
            }

            lastWaitDate = new Date(afterOrOnDate.getTime());

            // First look at the existing records and add whatever records that pass the time
            // requirement passed.
            // Now an argument can be made for why isnt a seperate listener attached to capture
            // these. That would
            // require the user to remember to call a function (start/reset or whatever). And makes
            // reuse of the
            // collector in the same test more prone because would have to be called multiple times.
            // Doing this
            // / approach the user would only have to call one function waitForNextNotification
            //
            LOGGER.debug("waitForNextNotification checking existing records for date on or after: "
                + afterOrOnDate);

            for (MetricPostRecord record : masterMetricRecordList) {
                // check to look for record.receivedTime is on (0) or after (1) afterOrOnDate date
                if (record.getReceivedTime().compareTo(afterOrOnDate) >= 0) {

                    if (checkAndUpdateCount(syncTypeList, record, null)) {
                        returnList.add(record);
                        LOGGER.debug("waitForNextNotification found record with date: "
                            + record.getReceivedTime());
                    }
                }
            }

            LOGGER.info("waitForNextNotification searched: " + masterMetricRecordList.size()
                + " records found " + returnList.size());

            // diff are we expecting i.e. 5 and we added 3 from above?
            int remainingCount = expectedResponseCount - returnList.size();

            // if we are still waiting for more records..
            if (remainingCount > 0) {
                LOGGER.debug("waitForNextNotification need to wait for remainingCount: "
                    + remainingCount);
                // need to access outside the sync block
                countDownLatch = new CountDownLatch(remainingCount);
                final CountDownLatch countDownLatchFinal = countDownLatch;

                metricPostListener = new MetricPostListener() {
                    public void jsonUpdate(MetricPostRecord record) {
                        // Check the countdown latch. The time between
                        // countDownLatch.await and removeMetricPostListener(metricPostListener);
                        // could have more metrics come in
                        AtomicInteger timesToCountDown = new AtomicInteger(0);
                        if (countDownLatchFinal.getCount() > 0
                            && checkAndUpdateCount(syncTypeList, record, timesToCountDown)) {

                            returnList.add(record);
                            int times = timesToCountDown.get();

                            LOGGER.debug("waitForNextNotification timesToCountDown " + times);

                            for (int i = 0; i < times; i++) {
                                countDownLatchFinal.countDown();
                            }

                            LOGGER.debug("waitForNextNotification record added, counting down to "
                                + countDownLatchFinal.getCount());
                        }
                    }
                };
                addMetricPostListener(metricPostListener);
            }
        }

        MetricCollectionTimeoutException timeoutException = null;

        // doing outsize the sync block above because we would be wait blocking the thread that
        // would count down!
        if (countDownLatch != null) {
            try {
                LOGGER.info("Going to await with timeout: " + waitTimeout + " ... ");
                boolean countReachedZero = countDownLatch.await(waitTimeout, TimeUnit.MILLISECONDS);
                LOGGER.info("... done waiting countReachedZero: " + countReachedZero
                    + " actual count: " + returnList.size() + " (latch count: "
                    + countDownLatch.getCount() + ")");

                if (!countReachedZero) {
                    timeoutException =
                        new MetricCollectionTimeoutException(returnList,
                            "ALERT: Does the test page contain the snippet information??? "
                                + "Test timed out waiting to reach desired response count: "
                                + expectedResponseCount + " actual count: " + returnList.size());
                }
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted while waiting ", e);
            }
        }

        // last order of business remove the listener if we created one
        if (metricPostListener != null) {
            removeMetricPostListener(metricPostListener);
        }

        // Doing after the listener removal above... could change to a try/finally, but would be
        // really big block
        if (timeoutException != null) {
            // Keep this log message!!! If test writers catch this but dont do anything we still
            // should have a
            // record we can go back and check. Yes the downside is we see this twice, on failures
            // -- a good thing

            // UPDATE: For now lets not, assume the test writters wont be copying and pasting code
            // that ignores... this is misleading in cases where timeout is deliberate
            // Update Update: just log a message no stack trace.
            LOGGER.error("waitForNextNotification going to throw timeout exception "
                + timeoutException.getMessage());

            throw timeoutException;
        }

        long internalEndTime = System.currentTimeMillis();

        long internalTimeTaken = internalEndTime - internalStartTime;
        // Since we didnt timeout assume waitTimeout is greater than internalTimeTaken
        long extraTime = waitTimeout - internalTimeTaken;
        LOGGER.info("waitForNextNotification elapsed time: " + internalTimeTaken
            + " ms to complete.");
        LOGGER.info("Compare with passed allowed time: " + waitTimeout + " extraTime to spare "
            + extraTime);

        BATestCollectorUtils.addNewWaitPerformanceMetric(testCollector.COLLECTOR_NAME,
            internalTimeTaken, waitTimeout);

        // Issue seen only a VM. The theory is the VM time was not in correct
        // sync which allows multiple instructions to occur in the same time slice.
        // After this wait method a caller (test writer) is likely to perform a
        // new test calling wait again with a new date. What this sleep is designed
        // to do is ensure metrics from this current test have settled out before
        // creating a new start date and running a new test. The danger is metrics
        // from the first test arrive late AND after the start date of the second
        // test. Thus when the cache is searched the second test (second call to
        // this method) finds metrics that belong from the first test. Clearing
        // the cache will not solve this problem. The cache is also needed due
        // to the fact that metrics could arrive before the caller even entered its
        // waiting state.
        try {
            // Becareful here... since this could have several sleeps to each test
            Thread.currentThread().sleep(3000);
        } catch (Throwable t) {
            LOGGER.error("waitForNextNotification sleep woken up", t);
        }


        LOGGER.debug("waitForNextNotification end, return list size: " + returnList.size());

        return returnList;
    }

    /**
     * Helper for waitForNextNotification
     * 
     * @param syncTypeList
     * @param record to check
     * @oaran decCount this is the count of times the type was found
     * @return boolean true if the metric should be added
     */

    private boolean checkAndUpdateCount(List<AbstractPayloadType> syncTypeList,
        MetricPostRecord record, AtomicInteger decCount) {
        List<AbstractPayloadType> toRemove = new ArrayList<AbstractPayloadType>();
        boolean shouldBeAdded = false;

        // For all types check the record. Dont break early
        // because the same record may match for multiple types
        for (AbstractPayloadType type : syncTypeList) {

            // The current records matches the type seenTypeCount times
            int seenTypeCount = type.calculateTypeCount(record);
            if (seenTypeCount > 0) {

                // update the times we are suppose to see this type
                // Exit this while loop on which ever condition occurs first
                // type.getCount() = 0: Expecting 3 ajax(type count), but found 4(seen)
                // seenTypeCount = 0: Expecting 3 ajax, but found only 2 (maybe on the next EUM)
                while (type.getCount() > 0 && seenTypeCount > 0) {
                    // Process the counts
                    seenTypeCount -= 1;
                    type.decrementCount();

                    // Keep a count of processed items for the caller
                    if (decCount != null) {
                        decCount.incrementAndGet();
                    }
                    shouldBeAdded = true;
                }

                // We seen all the items for this type, remove the type from processing
                if (type.getCount() == 0) {
                    toRemove.add(type);
                }
            }
        }

        // Once all types have been processed, update the type list
        // done this way to avoid updating the list while looping...
        syncTypeList.removeAll(toRemove);

        return shouldBeAdded;
    }

    public void addMetricPostListener(MetricPostListener l) {
        if (l != null) {
            // Using this to lock, see notifyMetricPostListener
            synchronized (this) {
                metricPostListenerList.add(l);
            }
        }
    }

    public void removeMetricPostListener(MetricPostListener l) {
        if (l != null) {
            // Using this to lock, see notifyMetricPostListener
            synchronized (this) {
                metricPostListenerList.remove(l);
            }
        }
    }

    private void notifyMetricPostListener(MetricPostRecord record) {
        LOGGER.debug("notifyMetricPostListener start");
        // this serves as a more generic lock because here we want to make the add record and
        // listener notication atomic.
        synchronized (this) {
            masterMetricRecordList.add(record);

            // BUG: should we be doing a clone here? not passing the master record to listeners...
            // EUM doesnt implement copy ctor
            // MetricPostRecord cloneRecord = new MetricPostRecord(record);

            for (MetricPostListener listener : metricPostListenerList) {
                listener.jsonUpdate(record);
            }
        }
        LOGGER.debug("notifyMetricPostListener end , record sent to listener count: "
            + metricPostListenerList.size());
    }

    /**
     * Called by the constructor of this abstract class, implementers should make calls to
     * addEndPointRecord
     * registering their end points
     */

    protected void registerAllEndPoints() {
        addEndPointRecord(getClass().getName(), new EndPointRecord("POST",
            METRIC_COLLECTION_CONTEXT + "", "EUM formated json from browser agent"));
    }
}
