/*
 * Copyright (c) 2014 CA. All rights reserved.
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
package com.ca.tas.em.rest.api.test;

import static org.testng.Assert.assertEquals;

import java.net.UnknownHostException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.tas.test.em.appmap.EmRestApiLinuxTestbed;
import com.ca.tas.test.em.appmap.EmRestApiTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * Tests whether the root resource is accessible as expected under SSL and not allowed under
 * unsecured HTTP connection. It runs on both Windows- and Linux-based VMs
 * 
 * @author surma04
 *
 */
@Tas(testBeds = {@TestBed(name = EmRestApiTestbed.class, executeOn = EmRestApiTestbed.MACHINE),
        @TestBed(name = EmRestApiLinuxTestbed.class, executeOn = EmRestApiLinuxTestbed.MACHINE)}, owner = "surma04", size = SizeType.SMALL)
@Test(groups = "publicApi")
public class RootResourceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RootResourceTest.class);

    /**
     * Tests whether the root resource is available under SSL and forbidden under unsecured HTTP
     * channel
     * 
     * @throws UnknownHostException
     */
    public void verify_ALM_453029_testRootResource() throws Exception {
        String hostname = java.net.InetAddress.getLocalHost().getCanonicalHostName();
        String url = String.format("http://%s:8081/apm/appmap/", hostname);

        final CloseableHttpClient httpclient = HttpClients.createDefault();

        final HttpGet request = new HttpGet(url);
        final HttpResponse response = httpclient.execute(request);
        String jsonStr = EntityUtils.toString(response.getEntity());

        LOGGER.info("Response == " + jsonStr);
        LOGGER.info("Testing RootResource at {}, status {}", url, response.getStatusLine());
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
            "Root Resource on http should return 200 - OK");
        LOGGER.info(jsonStr);

        request.releaseConnection();
    }
}
