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
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.ca.apm.browseragent.testsupport.collector.util.BATestCollectorUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * This class provides an abstract HttpHandler that all others should extend from
 *
 * @author Doug Briere
 */

public abstract class AbstractHttpHandler implements HttpHandler {


    /**
     * Helper class used by derived classes when they call addEndPointRecord in their
     * registerAllEndPoints method
     */

    protected static class EndPointRecord {
        public String httpMethodType;
        public String url;
        public String description;

        /**
         *
         * @param httpMethodType such as GET, POST, etc
         * @param url the end point i.e. /getSomething
         * @param description what does this do ?
         */

        public EndPointRecord(String httpMethodType, String url, String description) {
            this.httpMethodType = httpMethodType;
            this.url = url;
            this.description = description;
        }
    }

    // All derived classes will register their end points for informational display purposes only
    private static final Map<String, Set<EndPointRecord>> END_POINT_RECORDS_MAP =
        new ConcurrentHashMap<String, Set<EndPointRecord>>();


    /**
     * Constructor
     */

    public AbstractHttpHandler() {
        registerAllEndPoints();
    }

    /**
     * Some requests have to provide a precheck for cors and provide options
     * 
     * @param httpExchange
     * @throws IOException
     */

    protected void handleOPTIONS(HttpExchange httpExchange) throws IOException {
        setResponseHeaders(httpExchange);

        BATestCollectorUtils.sendResponse(httpExchange, "");

    }

    /**
     * Headers to subvert the cors stuff...
     * 
     * @param httpExchange
     */

    protected void setResponseHeaders(HttpExchange httpExchange) {
        // To silence this error:
        // Response to preflight request doesn't pass access control check: No
        // 'Access-Control-Allow-Origin' header is present on the requested resource
        httpExchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

        // To silence this error:
        // Request header field Content-type is not allowed by Access-Control-Allow-Headers in
        // preflight response.
        httpExchange.getResponseHeaders().set("Access-Control-Allow-Headers",
            "Origin, X-Requested-With, Content-Type, Accept, Cache-Control");
        // "Origin, X-Requested-With, Content-Type, Accept, Key,  X-Auth-Token , Authorization, ");
        // httpExchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

        // Do we need!??!
        // httpExchange.getResponseHeaders().set("Content-Type", "text/plain");
    }



    /**
     * implements HttpHandler
     *
     * @param httpExchange
     * @throws IOException
     */

    public abstract void handle(HttpExchange httpExchange) throws IOException;


    /**
     * Called by the constructor of this abstract class, implementers should make calls to
     * addEndPointRecord
     * registering their end points
     */

    protected abstract void registerAllEndPoints();

    /**
     * This method should be called for each end point the handler implements
     *
     * @param name , could be the class name or something else
     * @param record
     */

    protected static void addEndPointRecord(String name, EndPointRecord record) {
        // Yes its a ConcurrentHashMap, but the put for set initialization should still be atomic
        synchronized (END_POINT_RECORDS_MAP) {
            Set<EndPointRecord> recordSet = END_POINT_RECORDS_MAP.get(name);

            if (recordSet == null) {
                recordSet = Collections.synchronizedSet(new HashSet<EndPointRecord>());
                END_POINT_RECORDS_MAP.put(name, recordSet);
            }

            recordSet.add(record);
        }
    }

    /**
     * Everything that is registered to date.
     *
     * @return returns of a map of strings (end point class names) to set of records.
     */

    protected static Map<String, Set<EndPointRecord>> getEndPointRecordsSet() {
        return END_POINT_RECORDS_MAP;
    }

}
