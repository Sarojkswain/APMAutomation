/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed.util;

import java.io.IOException;
import java.util.Date;

import org.apache.http.client.ClientProtocolException;

/**
 * FLD Report REST's client interface to notify the FLD controller of load and test statuses.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public interface FLDReportClient {

    /**
     * 
     * @param name
     * @param status
     * @param timestamp
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public JsonReportResult notifyLoadStatus(String name, LoadStatus status, Date timestamp) throws ClientProtocolException, IOException;
}
