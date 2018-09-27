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

package com.ca.apm.automation.utils.mainframe;

import org.apache.commons.lang3.Validate;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Transactions {
    private static final Logger logger = LoggerFactory.getLogger(Transactions.class);
    private static final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    private static final CloseableHttpClient httpClient =
        HttpClients.custom().setConnectionManager(connectionManager).build();
    private static final Executor executor = Executor.newInstance(httpClient);

    /**
     * Generates CTG-to-CICS transactions using the CICSTestDriver test tool.
     *
     * @param ctdDir Path to a CICSTestDriver installation
     * @param xml XML definition to use
     * @param noSleep If {@code true} the process will not insert a grace period before/after the
     *                transaction generation.
     * @throws IOException
     * @throws InterruptedException
     */
    public static void generateCtgCics(String ctdDir, String xml, boolean noSleep)
        throws IOException, InterruptedException {

        Validate.notBlank(ctdDir);
        Validate.notBlank(xml);

        final ProcessBuilder pb = new ProcessBuilder(Arrays.asList("cmd.exe", "/C",
            "run.bat " + xml + (noSleep ? " -nosleep" : ""), ">nul", "2>&1"))
            .directory(new File(ctdDir));

        logger.debug("Generating CTG-CICS transactions ({})", xml);

        final Process process = pb.start();
        process.waitFor();
    }

    /**
     * Post transactions through CPTJCAApp to MQ and collect responses.
     *
     * @param wasContext WAS context root
     * @param mqHost MQ host
     * @param mqPort MQ port
     * @param mqManager MQ manager name
     * @param mqQueue MQ send queue
     * @param mqReplyQueue MQ reply queue
     * @param mqMessageCount MQ message count
     * @return Reply content
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static String postCptjcaapp(String wasContext, String mqHost, int mqPort,
        String mqManager, String mqQueue, String mqReplyQueue, int mqMessageCount)
        throws ClientProtocolException, IOException {
        logger.debug("Posting {} MQ transactions through {} to {} {}, reading reply from {}",
            mqMessageCount, wasContext, mqManager, mqQueue, mqReplyQueue);

        connectionManager.setDefaultMaxPerRoute(30);
        connectionManager.closeExpiredConnections();

        Request request = Request.Post(wasContext + "/JCAPutServlet").connectTimeout(10).socketTimeout(10)
            .bodyForm(
                Form.form().add("qmgr_name", mqManager).add("host_name", mqHost)
                    .add("port_num", Integer.toString(mqPort)).add("queue_name", mqQueue)
                    .add("num_msg", Integer.toString(mqMessageCount)).add("smsg", "1")
                    .add("pers", "n").add("msgid", "auto").add("corid", "auto")
                    .add("feedback", "trace").add("message_type", "datagram")
                    .add("reply_queue_name", mqReplyQueue).add("qmgr_nameR", mqManager)
                    .add("host_nameR", mqHost).add("port_numR", Integer.toString(mqPort))
                    .add("queue_nameR", mqReplyQueue)
                    .add("num_msgR", Integer.toString(mqMessageCount)).add("timeout", "100")
                    .build());

        Response response = executor.execute(request);
        String replyData = response.returnContent().asString();
        response.discardContent();

        return replyData;
    }

    /**
     * Post transactions through IBM sample application to CICS web service API.
     *
     * @param wasContext WAS root context
     * @param wsHost CICS web services host
     * @param wsPort CICS web services port
     * @param count Number of requests to send
     * @throws ClientProtocolException
     * @throws IOException
     * @throws InterruptedException
     */
    public static void postWsExampleApp(String wasContext, String wsHost, int wsPort, int count)
        throws ClientProtocolException, IOException, InterruptedException {

        // enable to get verbose transaction output for debugging purposes
        final boolean verbose = false;

        logger.debug("Configuring WS-CICS transactions through {} to {}:{}", wasContext, wsHost,
            wsPort);

        connectionManager.setDefaultMaxPerRoute(30);
        connectionManager.closeExpiredConnections();

        // NOTE: explanation of "magic" numbers used as parameter in code below
        // (test application specific constants): action=99 - configuration request,
        // submitOrder.x and y - parameters in form GUI
        Request cfgRequest =
            Request.Post(wasContext + "/CatalogController").connectTimeout(10).socketTimeout(10)
            .bodyForm(
                Form.form()
                    .add("inquireCatalogEndpoint",
                        "http://" + wsHost + ":" + wsPort + "/exampleApp/inquireCatalog")
                    .add("inquireSingleEndpoint",
                        "http://" + wsHost + ":" + wsPort + "/exampleApp/inquireSingle")
                    .add("orderEndpoint",
                        "http://" + wsHost + ":" + wsPort + "/exampleApp/placeOrder")
                    .add("action", "99").add("submitOrder.x", "0").add("submitOrder.y", "0")
                    .add("submitOrder", "submitOrder").build());

        logger.debug("Posting {} WS-CICS transactions through {}", count, wasContext);

        // NOTE: explanation of "magic" numbers used as parameter in code below
        // (test application specific constants): itemRef - sample app product code,
        // action=1 - list items, submitOrder.x and y - parameters in form GUI
        Request listRequest =
            Request.Post(wasContext + "/CatalogController").connectTimeout(10).socketTimeout(10)
            .bodyForm(
                Form.form().add("itemRef", "0010").add("action", "1").add("submitOrder.x", "0")
                    .add("submitOrder.y", "0").add("submitOrder", "submitOrder").build());

        CookieStore cookies = new BasicCookieStore();
        Response cfgResponse = executor.cookieStore(cookies).execute(cfgRequest);
        if (verbose) {
            logger.debug(cfgResponse.returnContent().asString());
        }
        cfgResponse.discardContent();

        for (int i = 1; i <= count; i++) {
            logger.debug("Executing WsExampleApp list POST request #{}", i);
            Response listResponse = executor.cookieStore(cookies).execute(listRequest);
            if (verbose) {
                logger.debug(listResponse.returnContent().asString());
            }
            listResponse.discardContent();
            if (i < count) {
                Thread.sleep(1000);
            }
        }
    }
}
