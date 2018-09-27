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

package com.ca.apm.browseragent.testsupport.collector.util;


import java.util.Date;

import com.ca.apm.eum.datamodel.EUM;


/**
 * This record represents the data of a single POST of metric collection received from the Browser
 * Agent
 *
 * @author Doug Briere
 */

public class MetricPostRecord {
    private String jsonMetricPost = null;
    private Date receivedTime = null;
    private EUM eumObject = null;

    /**
     * Constructor
     *
     * @param jsonMetricPost EUM formated json String
     * @throws Exception if the passed String cannot be converted to EUM object
     */

    public MetricPostRecord(String jsonMetricPost) throws Exception {
        this.jsonMetricPost = jsonMetricPost;
        this.receivedTime = new Date();

        // DO NOT REMOVE!!! MetricCollectionContextHandler.handlePOST will assume a sanity parse
        // check is performed
        // in MetricPostRecord so MetricCollectionContextHandler doesnt have to do itself or pass in
        // the EUM object.
        //
        this.eumObject = BATestCollectorUtils.convertToEUM(jsonMetricPost);

    }

    public EUM getEumObject() {
        return eumObject;
    }

    public Date getReceivedTime() {
        return receivedTime;
    }

    public String getJsonMetricPost() {
        return jsonMetricPost;
    }

    public String toString() {
        return "For record received on date: " + receivedTime + " has EUM as json: \n"
            + BATestCollectorUtils.getPrettyJson(eumObject);

    }

}
