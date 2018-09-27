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
import java.util.Map;

import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;

public class MQQueueManagerObjects
{
    private MQQueueManager queueManager;

    private static Map     queuesMap = Collections
                                             .synchronizedMap(new HashMap());

    public void setQueueManager(MQQueueManager qm)
    {
        queueManager = qm;
    }

    public MQQueueManager getQueueManager()
    {
        return queueManager;
    }

    public MQQueue getQueue(String queueName)
    {
        return (MQQueue) queuesMap.get(queueName);
    }

    public void addQueue(String queueName, MQQueue queue)
    {
        queuesMap.put(queueName, queue);
    }

    public void removeQueue(String queueName)
    {
        queuesMap.remove(queueName);
    }

    public Map getQueuesMap()
    {
        return queuesMap;
    }

}
