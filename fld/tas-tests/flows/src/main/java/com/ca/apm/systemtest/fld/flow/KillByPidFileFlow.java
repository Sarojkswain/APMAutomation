/*
 * Copyright (c) 2014 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and proprietary and shall not
 * be duplicated, used, disclosed or disseminated in any way except as authorized by the applicable
 * license agreement, without the express written permission of CA. All authorized reproductions
 * must be marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT PERMITTED BY APPLICABLE
 * LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF ANY KIND, INCLUDING WITHOUT LIMITATION, ANY
 * IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR INDIRECT, FROM THE
 * USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST PROFITS, BUSINESS INTERRUPTION,
 * GOODWILL, OR LOST DATA, EVEN IF CA IS EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.systemtest.fld.flow;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.process.PidProcess;
import org.zeroturnaround.process.ProcessUtil;
import org.zeroturnaround.process.Processes;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;

@Flow
public class KillByPidFileFlow extends FlowBase implements IAutomationFlow {
    
    private static final Logger log = LoggerFactory.getLogger(KillByPidFileFlow.class);
    
    @FlowContext
    private KillByPidFileFlowContext context;

    @Override
    public void run() throws Exception {
        try (BufferedReader pidFileReader = new BufferedReader(new FileReader(context.getPidFile()))) {
            log.info("Kill pidfile flow:: START");
            int pid = Integer.parseInt(pidFileReader.readLine());
            log.info("Pid to kill: {}", pid);
            PidProcess pidProcess = Processes.newPidProcess(pid);
            ProcessUtil.destroyGracefullyOrForcefullyAndWait(pidProcess, 5, TimeUnit.SECONDS,
                5, TimeUnit.SECONDS);
            
        } catch(Exception e) {
            log.info("Kill pidFile ended with error: {}", e.getMessage(), e);
        }
    }
}
