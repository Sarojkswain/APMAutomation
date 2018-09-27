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

/**
 * Metric Utilities class for BrowserAgent Automation
 *
 * @author Legacy BRTM automation code
 *         Updates for TAS - gupra04
 * 
 */

package com.ca.apm.tests.utils;

import com.ca.apm.tests.common.introscope.util.CLWBean;
import com.ca.apm.tests.test.BrowserAgentBaseTest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.NodeList;

public class TransactionTraceUtils extends BrowserAgentBaseTest implements Runnable {

    protected static final Logger LOGGER = LoggerFactory.getLogger(TransactionTraceUtils.class);

    // For the Transaction Trace Multithreading
    public void run() {
        CLWBean clw = MetricUtils.getClwBeanInstance(em);
        String clwCommand;
        if (TransactionTraceFilter == 0)
            clwCommand =
                "trace transactions exceeding 1 ms in agents matching .*" + agent.getAgentName()
                    + ".* for 90 secs";
        else if (TransactionTraceFilter == 1)
            clwCommand =
                "trace transactions where userid not equals bob in agents matching .*"
                    + agent.getAgentName() + ".* for 60 secs";
        else
            clwCommand =
                "trace transactions where url contains /brtmtestapp/GETCORS.jsp in agents matching .*"
                    + agent.getAgentName() + ".* for 90 secs";
        try {
            clw.setTtDirName(agent.getTransactionTraceDirectory());
            clw.setTtFileName(agent.getTransactionTraceFile());
            // running the CLW TT command
            LOGGER.info("CLW TT Command Started");
            clw.runCLW(clwCommand);
            LOGGER.info("CLW TT Command Finished");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean matchXMLValue(NodeList nodes, String nodeName, String nodeValue) {
        boolean result = false;
        LOGGER.info("Trying to match TT XML nodename " + nodeName + " and nodevalue " + nodeValue);
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i).getAttributes().getNamedItem("Name").getNodeValue()
                .contains(nodeName)) {
                if (nodes.item(i).getAttributes().getNamedItem("Value").getNodeValue()
                    .contains(nodeValue)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
}
