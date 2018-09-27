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
 * 
 * Author : TUUJA01/ JAYARAM PRASAD TADIMETI
 * Date : 13/04/2016
 */
package com.ca.apm.tests.agentEMFailoverTests;

import com.ca.apm.automation.action.flow.utility.LogCheckFlow;
import com.ca.apm.automation.action.flow.utility.LogCheckFlowContext;

import com.ca.apm.automation.action.flow.utility.LogCheckFlow;
import com.ca.apm.automation.action.flow.utility.LogCheckFlowContext;
import com.ca.apm.commons.flow.FailoverEMStartStopFlow;
import com.ca.apm.commons.flow.FailoverEMStartStopFlowContext;
import com.ca.apm.commons.flow.FailoverModifierFlowContext;
import com.ca.apm.commons.tests.BaseAgentTest;

public class AgentEMFailoverBase extends BaseAgentTest{

    
    public void startFailOverEM(String machineID,String emDir)
    {
        FailoverEMStartStopFlowContext FEF1 =
            FailoverEMStartStopFlowContext.startStopFailoverEM(
               emDir,"startFailOverEM");

        runFlowByMachineId(machineID,
            FailoverEMStartStopFlow.class, FEF1);
    }
    
    public void stopFailOverEM(String machineID)
    {
        FailoverEMStartStopFlowContext FEF1 =
            FailoverEMStartStopFlowContext.startStopFailoverEM("/"
                ,"stopFailOverEM");

        runFlowByMachineId(machineID,
            FailoverEMStartStopFlow.class, FEF1);
    }
    
    public void startFailOverLocalEM(String machineID,String emDir)
    {
        FailoverEMStartStopFlowContext FEF1 =
            FailoverEMStartStopFlowContext.startStopFailoverEM(emDir
                ,"startFailOverLocalEM");

        runFlowByMachineId(machineID,
            FailoverEMStartStopFlow.class, FEF1);
    }
 
    public void stopFailOverLocalEM(String machineID)
    {
        FailoverEMStartStopFlowContext FEF1 =
            FailoverEMStartStopFlowContext.startStopFailoverEM("/"
                ,"stopFailOverLocalEM");

        runFlowByMachineId(machineID,
            FailoverEMStartStopFlow.class, FEF1);
    }
    
    public void startFailOverPrimaryEM(String machineID, String emDir)
    {
        FailoverEMStartStopFlowContext FEF1 =
            FailoverEMStartStopFlowContext.startStopFailoverEM(emDir
                ,"startFailOverPrimaryEM");

        runFlowByMachineId(machineID,
            FailoverEMStartStopFlow.class, FEF1);
    }
    
    
    public void startPrimaryEMWithoutLock(String machineID, String emDir)
    {
        FailoverEMStartStopFlowContext FEF1 =
            FailoverEMStartStopFlowContext.startStopFailoverEM(emDir
                ,"startPrimaryEMWithoutLock");

        runFlowByMachineId(machineID,
            FailoverEMStartStopFlow.class, FEF1);
    }
    
    public void startFailOverPrimaryEMFirst(String machineID, String emDir)
    {
        FailoverEMStartStopFlowContext FEF1 =
            FailoverEMStartStopFlowContext.startStopFailoverEM(emDir
                ,"startSecondPrimaryEMFirst");

        runFlowByMachineId(machineID,
            FailoverEMStartStopFlow.class, FEF1);
    }
    
    
       public void checkRemoteLog(String logFile,String logMessage,String machineID)
    {
        LogCheckFlowContext LCP =
            LogCheckFlowContext.createWithNoTimeout(logFile, logMessage);
        runFlowByMachineId(machineID, LogCheckFlow.class, LCP);
    }
}
