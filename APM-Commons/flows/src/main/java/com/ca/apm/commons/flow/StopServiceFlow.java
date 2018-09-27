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
 * 
 * Author : JAMSA07/ SANTOSH JAMMI
 * Date : 11/04/2016
 */
package com.ca.apm.commons.flow;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.Os;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.commons.coda.common.ApmbaseUtil;


@Flow
public class StopServiceFlow implements IAutomationFlow {

    private static final Logger LOGGER = LoggerFactory.getLogger(StopServiceFlow.class);
    protected Process subprocess;
    @FlowContext
    private StopServiceFlowContext flowContext;

    @Override
    public void run() throws Exception {

        LOGGER.debug("In here to kill the process");
        List<String> commands = new ArrayList<String>();
        String dirLoc = (Os.isFamily(Os.FAMILY_WINDOWS)) ? "C:\\" : "/";

        if (Os.isFamily(Os.FAMILY_WINDOWS))
            commands.add("wmic Path win32_process Where \"CommandLine Like '%" + flowContext.getProcessToKill() + "%'\" Call Terminate");
        else
            commands
                .add("kill -9 `ps -ef |grep -i "+flowContext.getProcessToKill()+" |cut -d\" \" -f6,7 | sed ':a;N;$!ba;s/\\\\n/ /g'`");

        LOGGER.info(dirLoc);
        LOGGER.debug("About to kill the process");
        ApmbaseUtil.invokeProcessBuilder(commands, dirLoc);
    }
}
