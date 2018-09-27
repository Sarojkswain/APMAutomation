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

package com.ca.apm.powerpack.sysview.cics.dflt;

import com.ibm.cics.server.RetrieveBits;
import com.ibm.cics.server.RetrievedData;
import com.ibm.cics.server.RetrievedDataHolder;
import com.ibm.cics.server.Task;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.headers.MQTM;
import com.ibm.mq.headers.MQTMC2;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.charset.Charset;
import java.util.BitSet;

/**
 * This CICS program reads one message from an MQ input queue and replies with another message into
 * a reply queue.
 * <p>It expects to be called via an MQ trigger monitor and uses the trigger data to determine the
 * input queue. If the trigger data also contains information about the Queue Manager it uses
 * that, otherwise it uses the Queue Manager specified by the {@link #DEFAULT_QUEUE_MANAGER}
 * constant.
 * <p>If the input queue name starts with {@link #WILY_INPUT_QUEUE_PREFIX} the output queue name is
 * {@link #WILY_REPLY_QUEUE}. Otherwise the name is formed by appending {@link #REPLY_QUEUE_SUFFIX}
 * to the input queue name.
 *
 * @see
 * <a href='http://www.ibm.com/support/knowledgecenter/SSGMCP_5.3.0/com.ibm.cics.ts.java.doc/topics/dfhpj_webspheremq.html'>Accessing IBM MQ from Java programs</a>
 * @see
 * <a href='http://www.ibm.com/support/knowledgecenter/SSFKSJ_8.0.0/com.ibm.mq.dev.doc/q030520_.htm'>Using IBM MQ classes for Java</a>
 */
public class ProcessMqMessage {
    private static final String DEFAULT_QUEUE_MANAGER = "CSQ4";
    private static final String WILY_INPUT_QUEUE_PREFIX = "WILY_";
    private static final String WILY_REPLY_QUEUE = "WILY_REPLY_QUEUE";
    private static final String REPLY_QUEUE_SUFFIX = "_REPLY";

    public static void main(String[] args) {
        final Task task = Task.getTask();
        if (task == null) {
            System.err.println("Unable to obtain task object");
            return;
        }

        final String startCode = task.getSTARTCODE();
        if (!startCode.equalsIgnoreCase("SD")) {
            task.err.println("Unexpected start code: '" + startCode + "'");
            return;
        }

        // First we get the trigger data
        String queueManagerName;
        String inputQueueName;
        String userData;
        try {
            BitSet retrieveFlags = new BitSet(RetrieveBits.NUMBER_OF_BITS);
            retrieveFlags.set(RetrieveBits.DATA);

            RetrievedDataHolder retrieveData = new RetrievedDataHolder();
            task.retrieve(retrieveFlags, retrieveData);

            RetrievedData triggerData = retrieveData.getValue();

            if (triggerData == null) {
                task.err.println("Failed to retrieve trigger data");
                return;
            }

            // Parse the data into the appropriate header type
            ByteArrayInputStream is = new ByteArrayInputStream(triggerData.getData());
            DataInputStream dis = new DataInputStream(is);

            switch (triggerData.getData().length) {
                case MQTM.SIZE:
                    MQTM mqtm = new MQTM(dis);
                    inputQueueName = mqtm.getQName();
                    queueManagerName = DEFAULT_QUEUE_MANAGER;
                    userData = mqtm.getUserData();
                    break;

                case MQTMC2.SIZE:
                    MQTMC2 mqtmc2 = new MQTMC2(dis);
                    inputQueueName = mqtmc2.getQName();
                    queueManagerName = mqtmc2.getQMgrName();
                    userData = mqtmc2.getUserData();
                    break;

                default:
                    task.err.println("Unknown header type for trigger data with length '"
                        + triggerData.getData().length + "'");
                    return;
            }
        } catch (Exception e) {
            task.err.println("Caught exception while processing trigger data: "
                + e.getLocalizedMessage());
            return;
        }

        String outputQueueName;
        if (inputQueueName.startsWith(WILY_INPUT_QUEUE_PREFIX)) {
            outputQueueName = WILY_REPLY_QUEUE;
        } else {
            outputQueueName = inputQueueName + REPLY_QUEUE_SUFFIX;
        }

        { // DEBUG
            task.out.println("  Queue Manager: " + queueManagerName);
            task.out.println("    Input Queue: " + inputQueueName);
            task.out.println("   Output Queue: " + outputQueueName);
            task.out.println("Input User Data: (" + userData.length() + ") " + userData);
        }

        MQQueueManager queueManager = null;
        try {
            // Use Binding mode
            MQEnvironment.properties.put(CMQC.TRANSPORT_PROPERTY, CMQC.TRANSPORT_MQSERIES_BINDINGS);
            MQEnvironment.hostname = null;

            // Connect to the queue manager
            queueManager = new MQQueueManager(queueManagerName);

            // Access the input queue
            MQQueue inputQueue = queueManager.accessQueue(inputQueueName,
                CMQC.MQOO_INQUIRE | CMQC.MQOO_INPUT_SHARED);
            task.out.println("Current input queue depth: " + inputQueue.getCurrentDepth());

            if (inputQueue.getCurrentDepth() <= 0) {
                task.err.println("No messages to process in the input queue");
                return;
            }

            // Get message from input queue
            MQMessage inputMessage = new MQMessage();
            MQGetMessageOptions gmo = new MQGetMessageOptions();
            inputQueue.get(inputMessage, gmo);

            byte[] strData = null;
            final int dataLength = inputMessage.getDataLength();
            if (dataLength > 0) {
                strData = new byte[dataLength];
                inputMessage.readFully(strData, 0, strData.length);
            }

            if (strData != null) { // DEBUG
                //task.out.println("Message Data (EBCDIC): "
                //    + new String(strData, Charset.forName("Cp1047")));
                task.out.println("Message Data (ASCII) : "
                    + new String(strData, Charset.forName("US-ASCII")));
            }

            // Access the output queue
            MQQueue outputQueue = queueManager.accessQueue(outputQueueName, CMQC.MQOO_OUTPUT);

            // Write reply message to output queue
            MQMessage outputMessage = new MQMessage();
            outputMessage.writeString("MQ Message processed by "
                + ProcessMqMessage.class.getSimpleName());
            MQPutMessageOptions pmo = new MQPutMessageOptions();
            outputQueue.put(outputMessage, pmo);
        } catch (Exception e) {
            task.err.println("Caught exception while processing MQ messages: "
                + e.getLocalizedMessage());
        } finally {
            if (queueManager != null) {
                try {
                    queueManager.disconnect();
                } catch (MQException e) {
                    task.err.println("Failed to disconnect from queue manager: "
                        + e.getLocalizedMessage());
                }
            }
        }
    }
}
