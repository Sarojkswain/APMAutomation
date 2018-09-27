//---------------------------------------------------------------------------//
//  Copyright (c) 2009-2013 CA. All rights reserved.                         //
//                                                                           //
//  This software and all information contained therein is confidential and  //
//  proprietary and shall not be duplicated, used, disclosed or disseminated //
//  in any way except as authorized by the applicable license agreement,     //
//  without the express written permission of CA. All authorized             //
//  reproductions must be marked with this language.                         //
//                                                                           //
//  EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT   //
//  PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY  //
//  OF ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF     //
//  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA //
//  BE LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE,     //
//  DIRECT OR INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT     //
//  LIMITATION, LOST PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, //
//  EVEN IF CA IS EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.                  //
//---------------------------------------------------------------------------//

package com.ca.cpt.app;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.mq.MQC;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;

public class JCAPutServlet extends HttpServlet implements Servlet
{
    static final int FEEDBACK_TRACE = 99990000;
    static final int FEEDBACK_TRACE_PROPAGATE = 99990001;
    static final int FEEDBACK_CUSTOMER_USED = 99996666;
    static final int FEEDBACK_NONE = MQC.MQFB_NONE;

    private static final long serialVersionUID = 4150991562589696662L;

    public JCAPutServlet()
    {}

    final private AtomicInteger successfulCount = new AtomicInteger(0);

    final private AtomicInteger failureCount    = new AtomicInteger(0);

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
        throws ServletException, IOException
    {
        doGet1(request, response);
    }

    protected void doGet1(HttpServletRequest request,
            HttpServletResponse response)
        throws ServletException, IOException
    {
        doGet2(request, response);
    }

    protected void doGet2(HttpServletRequest request,
            HttpServletResponse response)
        throws ServletException, IOException
    {
        doGet3(request, response);
    }

    protected void doGet3(HttpServletRequest request,
            HttpServletResponse response)
        throws ServletException, IOException
    {
        doGet4(request, response);
    }

    protected void doGet4(HttpServletRequest request,
            HttpServletResponse response)
        throws ServletException, IOException
    {
        doGet5(request, response);
    }

    protected void doGet5(HttpServletRequest request,
            HttpServletResponse response)
        throws ServletException, IOException
    {
        doGet6(request, response);
    }

    protected void doGet6(HttpServletRequest request,
            HttpServletResponse response)
        throws ServletException, IOException
    {
        response.setContentType("text/html");
        PrintWriter pw = response.getWriter();
        MQQueueManagerObjects mqObjects = null;
        MQQueue queueSending = null;
        MQQueue queueReceiving = null;
        int feedbackCode = FEEDBACK_NONE;

        try {
            // Sending QueueManager parameters.
            String qmSend = request.getParameter("qmgr_name");
            String hostSend = request.getParameter("host_name");
            int portSend = Integer.parseInt(request.getParameter("port_num"));
            String queueSend = request.getParameter("queue_name");
            String replyQueue = request.getParameter("reply_queue_name");
            int numOfMsgSend = Integer
                .parseInt(request.getParameter("num_msg"));
            String persistence = request.getParameter("pers");
            String feedbackStr = request.getParameter("feedback");
            String msgIDStr = request.getParameter("msgid");
            String corIDStr = request.getParameter("corid");
            if (feedbackStr != null) {
                if (feedbackStr.equalsIgnoreCase("trace")) {
                    feedbackCode = FEEDBACK_TRACE;
                }
                else if (feedbackStr.equalsIgnoreCase("propagate")) {
                    feedbackCode = FEEDBACK_TRACE_PROPAGATE;
                }
                else if (feedbackStr.equalsIgnoreCase("customer")) {
                    feedbackCode = FEEDBACK_CUSTOMER_USED;
                }
                else {
                    feedbackCode = FEEDBACK_NONE;
                }
            }

            // Get message type
            String messageTypeStr = request.getParameter("message_type");
            int messageType = MQC.MQMT_DATAGRAM;
            if (messageTypeStr.equalsIgnoreCase("report"))
                messageType = MQC.MQMT_REPORT;
            else if (messageTypeStr.equalsIgnoreCase("request"))
                messageType = MQC.MQMT_REQUEST;

            int sizeOfMsg = Integer.parseInt(request.getParameter("smsg"));
            String qmInstanceSend = qmSend + "|" + hostSend + "|" + portSend;

            // Receiving QueueManager parameters.
            String qmReceive = request.getParameter("qmgr_nameR");
            String hostReceive = request.getParameter("host_nameR");
            int portReceive = Integer.parseInt(request
                    .getParameter("port_numR"));
            String queueReceive = request.getParameter("queue_nameR");
            int numOfMsgReceive = Integer.parseInt(request
                    .getParameter("num_msgR"));
            int waitInterval = Integer
                .parseInt(request.getParameter("timeout"));
            String qmInstanceReceive = qmReceive + "|" + hostReceive + "|"
                + portReceive;

            pw.println("<br>SENDING PROCESS");
            pw.println("<br>---------------<br>");
            ConnectionParams paramsSend = new ConnectionParams(
                    hostSend,
                    qmSend,
                    portSend,
                    queueSend,
                    MQConnectionPool.SEND);
            try {
                mqObjects = MQConnectionPool.getMQQueueManager(paramsSend);
            }
            catch (MQException mqe) {
                pw.println( "<br><br>Exception has occured while connecting to "
                        + "sending queue manager and queue " + queueSend);
                pw.println("<br>MQException: " + mqe.getMessage());
                pw.println("<br>Validate configuration parameters for Sending "
                        + "queue manager and queue " + queueSend);
                pw.println("<br><a href=\"/CPTJCAApp/\">Click here</a> to go back");
                MQConnectionPool.releaseMQQueueManager(paramsSend);
            }

            if (mqObjects != null) {
                queueSending = mqObjects.getQueue(queueSend + "|"
                        + MQConnectionPool.SEND);
            }
            else {
                pw.println("<br>Could not connect to queue manager "
                        + qmInstanceSend);
            }

            if (queueSending != null) {
                MQMessage putMessage = new MQMessage();
                // Setting size of Message.
                int numBytes = 1024 * sizeOfMsg;
                byte msgBytes[] = new byte[numBytes];
                for (int j = 0; j < numBytes; j++) {
                    msgBytes[j] = (byte) 'X';
                }

                // Show the feedback code in the message bytes
                String feedbackCodeStr = Integer.toString(feedbackCode);
                byte[] feedbackCodeCharBytes = feedbackCodeStr.getBytes();
                for (int j = 0; j < feedbackCodeCharBytes.length; j++) {
                    msgBytes[j] = feedbackCodeCharBytes[j];
                }

                // Setting Persistence attribute for message.
                if (persistence.equalsIgnoreCase("p")) {
                    putMessage.persistence = MQC.MQPER_PERSISTENT;
                }
                else {
                    putMessage.persistence = MQC.MQPER_NOT_PERSISTENT;
                }

                // Setting feedback
                putMessage.feedback = feedbackCode;

                // Setting message type
                putMessage.messageType = messageType;

                // Set up the reply queue
                putMessage.replyToQueueManagerName = qmSend;
                putMessage.replyToQueueName = replyQueue;
                putMessage.report = MQC.MQRO_COPY_MSG_ID_TO_CORREL_ID;

                // Generate random UUID to use as random portion of message ID
                UUID uuid;
                String uuidStr;
                byte[] uuidBytes;

                // Generate the Message ID
                // Insert the word ABCDEFGH at the beginning
                byte[] abcdefgh = {'A', 'B', 'C', 'D', 'E', 'F', 'H', 'I'};
                byte[] newMessageID = new byte[24];
                int msgIndex;

                // If asked to zero out message id then do so
                if (msgIDStr.equalsIgnoreCase("zeros")) {
                    for (msgIndex = 0; msgIndex < 24; msgIndex++) {
                        newMessageID[msgIndex] = 0;
                    }
                }
                // Otherwise do auto case
                else {
                    uuid = UUID.randomUUID();
                    uuidStr = uuid.toString();
                    uuidStr.replaceAll("-", "");
                    uuidBytes = uuidStr.getBytes();

                    // If doing 00010203040506070809FF-unique
                    if (msgIDStr.equalsIgnoreCase("0123")) {
                        for (msgIndex = 0; msgIndex < 10; msgIndex++) {
                            newMessageID[msgIndex] = (byte)msgIndex;
                        }
                        newMessageID[msgIndex++] = (byte)0xFF;
                    }
                    // Otherwise doing auto
                    else {
                        for (msgIndex = 0; msgIndex < abcdefgh.length; msgIndex++) {
                            newMessageID[msgIndex] = abcdefgh[msgIndex];
                        }
                    }

                    // Pull unique portion from uuid
                    for (; msgIndex < 24; msgIndex++) {
                        newMessageID[msgIndex] = uuidBytes[msgIndex];
                    }
                }

                putMessage.messageId = newMessageID;

                // Correlation id is reverse of message id
                byte [] correlationID = new byte[24];

                // If asked to zero out message id then do so
                if (corIDStr.equalsIgnoreCase("zeros")) {
                    for (msgIndex = 0; msgIndex < 24; msgIndex++) {
                        correlationID[msgIndex] = 0;
                    }
                }
                else if (corIDStr.equalsIgnoreCase("reverse")) {
                    for (int i = 0; i < 24; i++) {
                        correlationID[i] = newMessageID[24 - 1 - i ];
                    }
                }
                else if (corIDStr.equalsIgnoreCase("same")) {
                    for (int i = 0; i < 24; i++) {
                        correlationID[i] = newMessageID[i];
                    }
                }
                // Otherwise do auto case
                else {
                    uuid = UUID.randomUUID();
                    uuidStr = uuid.toString();
                    uuidStr.replaceAll("-", "");
                    uuidBytes = uuidStr.getBytes();

                    // If doing 00010203040506070809FF-unique
                    if (corIDStr.equalsIgnoreCase("0123")) {
                        for (msgIndex = 0; msgIndex < 10; msgIndex++) {
                            correlationID[msgIndex] = (byte)msgIndex;
                        }
                        correlationID[msgIndex++] = (byte)0xFF;
                    }
                    // Otherwise doing auto
                    else {
                        for (msgIndex = 0; msgIndex < abcdefgh.length; msgIndex++) {
                            correlationID[msgIndex] = abcdefgh[msgIndex];
                        }
                    }

                    // Pull unique portion from uuid
                    for (; msgIndex < 24; msgIndex++) {
                        correlationID[msgIndex] = uuidBytes[msgIndex];
                    }
                }

                // Assign the correlation ID
                putMessage.correlationId = correlationID;


                // Putting message with settings.
                putMessage.write(msgBytes);
                for (int i = 0; i < numOfMsgSend; i++) {
                    // Setting feedback
                    putMessage.feedback = feedbackCode;

                    // Setting message type
                    putMessage.messageType = messageType;

                    try {
                        MQPutMessageOptions messageOpts =
                            new MQPutMessageOptions();
                        queueSending.put(putMessage);
                        pw.println("<br>Sent message " + (i + 1)
                                + " to Queue Manager Instance: "
                                + qmInstanceSend + " Queue: " + queueSend);
                    }
                    catch (MQException mqe) {
                        mqe.printStackTrace();

                        int rc = mqe.reasonCode;
                        if (rc == MQException.MQRC_PUT_INHIBITED) {
                            pw
                                .println("<br>"
                                        + qmInstanceSend
                                        + " Queue: "
                                        + queueSend
                                        + " is PUT INHIBITED, Change the queue attribute Put Messages as ALLOWED");
                        }
                        else {
                            pw
                                .println("<br>MQException occured while putting the message in queue "
                                        + queueSend);
                            pw.println("<br>MQException: " + mqe.getMessage());
                            if (rc == MQException.MQRC_CONNECTION_QUIESCING
                                    || rc == MQException.MQRC_CONNECTION_BROKEN
                                    || rc == MQException.MQRC_CONNECTION_STOPPING
                                    || rc == MQException.MQRC_CONNECTION_NOT_AUTHORIZED
                                    || rc == MQException.MQRC_Q_MGR_QUIESCING
                                    || rc == MQException.MQRC_Q_MGR_STOPPING
                                    || rc == MQException.MQRC_Q_MGR_NOT_AVAILABLE
                                    || rc == MQException.MQRC_Q_MGR_NOT_ACTIVE
                                    || rc == MQException.MQRC_HOBJ_ERROR
                                    || rc == MQException.MQRC_HCONN_ERROR
                                    || rc == MQException.MQRC_HANDLE_NOT_AVAILABLE) {
                                pw
                                    .println("<br><br>Could not get handle to Queue Manager Object");
                                pw
                                    .println("<br>Make sure the queue manager connection parameters are correct");
                                pw
                                    .println("<br>Check if queue manager and listener port is up and running fine");
                                pw
                                    .println("<br><br>If everything is fine, try again...");
                            }
                            else if (rc == MQException.MQRC_UNKNOWN_OBJECT_NAME) {
                                pw
                                    .println("<br><br>Verify the queue name and queue manager name");
                            }
                            else {
                                pw
                                    .println("<br><br><br><br>For unknown errors, contact DEV team.");
                            }

                            MQConnectionPool.releaseMQQueueManager(paramsSend);
                            pw
                                .println("<br><a href=\"/CPTJCAApp/\">Click here</a> to go back");
                        }

                        // Check that the feedback was left alone
                        if (putMessage.feedback != feedbackCode) {
                            pw.println("<br>Error: Tracer modified feedback code with exception!!!");
                        }
                    }

                    // Check that the feedback was left alone
                    if (putMessage.feedback != feedbackCode) {
                        pw.println("<br>Error: Tracer modified feedback code!!!");
                    }
                }
            }
            else {
                pw.println("<br>Could not connect to queue " + queueSend);
            }

            pw.println("<br><br>RECEIVING PROCESS");
            pw.println("<br>-----------------<br>");
            // Reading same messages from Receiving Queue Manager.
            ConnectionParams paramsReceive = new ConnectionParams(
                    hostReceive,
                    qmReceive,
                    portReceive,
                    queueReceive,
                    MQConnectionPool.RECEIVE);
            try {
                mqObjects = MQConnectionPool.getMQQueueManager(paramsReceive);
            }
            catch (MQException mqe) {
                pw
                    .println("<br><br>Exception has occured while connecting to receiving queue manager and queue "
                            + queueReceive);
                pw.println("<br>MQException: " + mqe.getMessage());
                pw
                    .println("<br>Validate configuration parameters for Receiving queue manager and queue "
                            + queueReceive);
                pw
                    .println("<br><a href=\"/CPTJCAApp/\">Click here</a> to go back");
                MQConnectionPool.releaseMQQueueManager(paramsSend);
            }

            if (mqObjects != null) {
                queueReceiving = mqObjects.getQueue(queueReceive + "|"
                        + MQConnectionPool.RECEIVE);
            }
            else {
                pw.println("<br>Could not connect to queue manager "
                        + qmInstanceReceive);
            }

            int j = 1;
            if (queueReceiving != null) {
                // Receive all the messages.
                MQGetMessageOptions gOption = new MQGetMessageOptions();
                gOption.options = MQC.MQGMO_WAIT;
                gOption.waitInterval = waitInterval;
                for (; j <= numOfMsgReceive; j++) {
                    try {
                        MQMessage getMessage = new MQMessage();
                        queueReceiving.get(getMessage, gOption);
                        pw.println("<br>Received message " + j
                                + " from Queue Manager Instance: "
                                + qmInstanceReceive + " Queue: "
                                + queueReceive +
                                " Message Type: " + getMessage.messageType);
                    }
                    catch (MQException mqe) {
                        mqe.printStackTrace();

                        int rc = mqe.reasonCode;
                        if (rc == MQException.MQRC_NO_MSG_AVAILABLE) {
                            pw
                                .println("<br>No message available from Queue Manager Instance: "
                                        + qmInstanceReceive
                                        + " Queue: "
                                        + queueReceive);
                        }
                        else if (rc == MQException.MQRC_GET_INHIBITED) {
                            pw
                                .println("<br>"
                                        + qmInstanceReceive
                                        + " Queue: "
                                        + queueReceive
                                        + " is GET INHIBITED, Change the queue attribute Get Messages as ALLOWED");
                        }
                        else {
                            pw
                                .println("<br>MQException occured while receiving the message from queue "
                                        + queueReceive);
                            pw.println("<br>MQException: " + mqe.getMessage());
                            if (rc == MQException.MQRC_CONNECTION_QUIESCING
                                    || rc == MQException.MQRC_CONNECTION_BROKEN
                                    || rc == MQException.MQRC_CONNECTION_STOPPING
                                    || rc == MQException.MQRC_CONNECTION_NOT_AUTHORIZED
                                    || rc == MQException.MQRC_Q_MGR_QUIESCING
                                    || rc == MQException.MQRC_Q_MGR_STOPPING
                                    || rc == MQException.MQRC_Q_MGR_NOT_AVAILABLE
                                    || rc == MQException.MQRC_Q_MGR_NOT_ACTIVE
                                    || rc == MQException.MQRC_HOBJ_ERROR
                                    || rc == MQException.MQRC_HCONN_ERROR
                                    || rc == MQException.MQRC_HANDLE_NOT_AVAILABLE) {
                                pw
                                    .println("<br><br>Could not get handle to Queue Manager Object");
                                pw
                                    .println("<br>Make sure the queue manager connection parameters are correct");
                                pw
                                    .println("<br>Check if queue manager and listener port is up and running fine");
                                pw
                                    .println("<br><br>If everything is fine, try again...");
                            }
                            else if (rc == MQException.MQRC_UNKNOWN_OBJECT_NAME) {
                                pw
                                    .println("<br><br>Verify the queue name and queue manager name");
                            }
                            else if (rc == MQException.MQRC_OBJECT_IN_USE) {
                                pw
                                    .println("<br><br>Change the timeout variable to 0. This error is coming due to lock on receiving queue.");
                            }
                            else {
                                pw
                                    .println("<br><br><br><br>For unknown errors, contact DEV team.");
                            }

                            MQConnectionPool
                                .releaseMQQueueManager(paramsReceive);
                            pw
                                .println("<br><a href=\"/CPTJCAApp/\">Click here</a> to go back");
                        }

                        break;
                    }
                }
            }
            else {
                pw.println("<br>Could not connect to queue " + queueReceive);
            }

            if (j > numOfMsgReceive) {
                pw.println("<br><br><br>Trasaction Complete.");
                pw
                    .println("<br><a href=\"/CPTJCAApp/\">Click here</a> to go back");

                successfulCount.incrementAndGet();
            }
            else {
                failureCount.incrementAndGet();
            }
        }
        catch (NumberFormatException ex) {
            ex.printStackTrace();
            pw
                .println("<br>Exception occured: NumberFormatException, validate the numerical fields "
                        + ex.getMessage());
        }
        catch (Exception ex) {
            ex.printStackTrace();
            pw.println("<br>Exception occured: " + ex.getMessage());
            pw.println("<br><a href=\"/CPTJCAApp/\">Click here</a> to go back");
        }
    }

    protected void doPut(HttpServletRequest httpservletrequest,
            HttpServletResponse httpservletresponse)
        throws ServletException, IOException
        {}

    public void init(ServletConfig servletconfig) throws ServletException
    {}

    public void destroy()
    {
        System.out.println("==========Transaction Summary=============");
        System.out.println("Successful count: " + successfulCount.get());
        System.out.println("Failure count: " + failureCount.get());
        System.out.println("==========Transaction Summary=============");

        MQConnectionPool.releaseAllMQQueueManagers();
    }

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
        throws ServletException, IOException
    {
        doGet(request, response);
    }
}
