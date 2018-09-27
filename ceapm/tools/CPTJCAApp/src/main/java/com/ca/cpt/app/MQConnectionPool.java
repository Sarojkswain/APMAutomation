/*
 * Copyright (c) 2015 CA. All rights reserved.
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

package com.ca.cpt.app;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.ibm.mq.MQC;
import com.ibm.mq.MQException;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;

public class MQConnectionPool
{
    private static Map queueManagerPool = Collections.synchronizedMap(new HashMap());

    public static final String SEND             = "SEND";

    public static final String RECEIVE          = "RECEIVE";

    public MQConnectionPool()
    {}

    public static synchronized MQQueueManagerObjects getMQQueueManager(ConnectionParams params)
        throws MQException
    {
        // Check for the connection params
        MQQueueManagerObjects mqObjects = null;
        MQQueueManager queueManager = null;
        MQQueue queue = null;
        String host = params.getHostName();
        int port = params.getPort();
        String qmType = params.getQmType();
        String queueName = params.getQName();
        int qOpenOption = 0;

        // Check for the queue manager entry in HashMap . if its already there
        // then don't create new queue manager object and return the already
        // existing one. If queue manager entry is not there then create new
        // queue manager object and first store then return the new one. HashMap
        // has entry stored with QueueManagerName|host as a key.
        String key = host + "|" + String.valueOf(port) + "|" + qmType;
        mqObjects = (MQQueueManagerObjects) queueManagerPool.get(key);

        if (mqObjects == null) {
            mqObjects = new MQQueueManagerObjects();
            Hashtable props = new Hashtable();
            // MQQueueManager object is not already stored so create new one
            // then return it.
            props.put(MQC.CHANNEL_PROPERTY, "SYSTEM.AUTO.SVRCONN");
            props.put(MQC.HOST_NAME_PROPERTY, host);
            props.put(MQC.PORT_PROPERTY, new Integer(port));
            // Create MQQueueManager object
            try {
                queueManager = new MQQueueManager("", props);
                mqObjects.setQueueManager(queueManager);
            }
            catch (MQException mqe) {
                mqObjects = null;
                throw mqe;
            }

            System.out
                .println("Successful connection to queue manager instance at "
                        + key);
        }
        else {
            queueManager = mqObjects.getQueueManager();
        }

        queue = mqObjects.getQueue(queueName + "|" + qmType);
        if (queue == null) {
            // Create MQQueue object. Open it for both input and for output.
            if (SEND.equals(qmType)) {
                qOpenOption = MQC.MQOO_OUTPUT | MQC.MQOO_FAIL_IF_QUIESCING;
            }
            else if (RECEIVE.equals(qmType)) {
                qOpenOption = MQC.MQOO_INPUT_SHARED
                    | MQC.MQOO_FAIL_IF_QUIESCING;
            }

            try {
                queue = queueManager.accessQueue(queueName, qOpenOption);
                mqObjects.addQueue(queueName + "|" + qmType, queue);
            }
            catch (MQException mqe) {
                mqObjects = null;
                throw mqe;
            }

            System.out.println("Successful access to queue " + queueName + "|"
                    + qmType);
        }

        queueManagerPool.put(key, mqObjects);
        return mqObjects;
    }

    public static synchronized void releaseMQQueueManager(ConnectionParams params)
    {
        String host = params.getHostName();
        int port = params.getPort();
        String qmType = params.getQmType();

        String key = host + "|" + String.valueOf(port) + "|" + qmType;
        MQQueueManagerObjects mqObject = (MQQueueManagerObjects) queueManagerPool
            .get(key);
        if (mqObject != null) {
            try {
                Map queuesMap = mqObject.getQueuesMap();
                if (!queuesMap.isEmpty()) {
                    Set queueNames = queuesMap.keySet();
                    Iterator queuesIter = queueNames.iterator();
                    while (queuesIter.hasNext()) {
                        MQQueue queue = (MQQueue) queuesMap.get(queuesIter
                                .next());
                        if (queue != null) {
                            try {
                                queue.close();
                                queue = null;
                            }
                            catch (MQException mqe) {
                                // Ignore
                            }
                        }
                    }

                    queuesMap.clear();
                }

                if (mqObject.getQueueManager() != null) {
                    mqObject.getQueueManager().disconnect();
                    mqObject.getQueueManager().close();
                }

                System.out.println("Releasing JCA connection for " + key);
            }
            catch (MQException mqe) {
                // do nothing
            }
        }

        queueManagerPool.remove(key);
        key = host + "|" + String.valueOf(port) + "|" + SEND;
        queueManagerPool.remove(key);
        key = host + "|" + String.valueOf(port) + "|" + RECEIVE;
        queueManagerPool.remove(key);
    }

    // This method will be called from servlet's destroy method.
    public static synchronized void releaseAllMQQueueManagers()
    {
        // Get all MQQueueManager object and disconnect them.
        Set keys = queueManagerPool.keySet();
        Iterator iter = keys.iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            MQQueueManagerObjects mqObject = (MQQueueManagerObjects) queueManagerPool
                .get(key);
            if (mqObject != null) {
                try {
                    Map queuesMap = mqObject.getQueuesMap();
                    if (!queuesMap.isEmpty()) {
                        Set queueNames = queuesMap.keySet();
                        Iterator queuesIter = queueNames.iterator();
                        while (queuesIter.hasNext()) {
                            MQQueue queue = (MQQueue) queuesMap.get(queuesIter
                                    .next());
                            if (queue != null) {
                                try {
                                    queue.close();
                                    queue = null;
                                }
                                catch (MQException mqe) {
                                    // Ignore
                                }
                            }
                        }
                        queuesMap.clear();
                    }

                    if (mqObject.getQueueManager() != null) {
                        mqObject.getQueueManager().disconnect();
                        mqObject.getQueueManager().close();
                    }

                    System.out.println("Releasing JCA connection for " + key);
                }
                catch (MQException e) {
                    System.out.println("Error in releasing JCA connection ");
                    e.printStackTrace();
                }
            }
        }

        // Clear the HashMap
        queueManagerPool.clear();
    }
}
