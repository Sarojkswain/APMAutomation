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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ca.apm.browseragent.testsupport.collector.util.BATestCollectorUtils;
import com.ca.apm.browseragent.testsupport.collector.util.MetricPostListener;
import com.ca.apm.browseragent.testsupport.collector.util.MetricPostRecord;
import com.sun.net.httpserver.HttpExchange;

/**
 * This handler covers the end point for displaying the list of received json objects
 *
 * @author Doug Briere
 */

public class MetricDisplayContextHandler extends AbstractHttpHandler implements MetricPostListener {
    public static final String METRIC_DISPLAY_CONTEXT = "/metricdisplay";

    private List<MetricPostRecord> metricRecordList = null;

    private final Logger LOGGER = Logger.getLogger(MetricDisplayContextHandler.class);

    public MetricDisplayContextHandler() {
        metricRecordList = new ArrayList<MetricPostRecord>();
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
                case BATestCollectorUtils.METHOD_GET:
                    handleGET(httpExchange);
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

    private void handleGET(HttpExchange httpExchange) throws IOException {
        LOGGER.debug("handleGET start ");

        String response = "<html>";

        synchronized (metricRecordList) {
            LOGGER.debug("handleGET called to dump metricRecordList of size "
                + metricRecordList.size());

            response += "Total records cached: " + metricRecordList.size() + "<hr/>";

            // Process the list in reverse order since the one at the top is likely of the most
            // interest
            for (int i = metricRecordList.size() - 1; i >= 0; i--) {
                MetricPostRecord record = metricRecordList.get(i);

                String prettyJson = BATestCollectorUtils.getPrettyJson(record.getEumObject());

                String recordTimeString =
                    "Record: " + i + " , Received at: " + record.getReceivedTime().toString();
                response += "<b>" + recordTimeString + "</b><pre>" + prettyJson + "</pre>";
                response += "<hr/>";
            }
        }

        response += "</html>";

        BATestCollectorUtils.sendResponse(httpExchange, response);

        LOGGER.debug("handleGET end");
    }

    // implements MetricPostListener

    public void jsonUpdate(MetricPostRecord record) {
        synchronized (metricRecordList) {
            metricRecordList.add(record);

            LOGGER.debug("jsonUpdate record added to metricRecordList total count: "
                + metricRecordList.size());
        }


    }

    /**
     * Called by the constructor of this abstract class, implementers should make calls to
     * addEndPointRecord
     * registering their end points
     */

    protected void registerAllEndPoints() {
        addEndPointRecord(getClass().getName(), new EndPointRecord("GET", METRIC_DISPLAY_CONTEXT
            + "", "Dump of EUM formated(pretty) json from browser agent"));

    }

}
