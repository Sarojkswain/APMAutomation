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

package com.ca.apm.tests.testbed.utils;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.testbed.TestbedMachine;

import java.util.Arrays;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class NetworkUtils {

    ///////////////////////////////////////////
    // INCREASE NETWORK BUFFER
    ///////////////////////////////////////////

    public static void configureNetworkBuffer(TestbedMachine machine, String machineId) {
        RunCommandFlowContext reg1FlowContext = new RunCommandFlowContext.Builder("reg")
                .args(Arrays.asList("add", "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Services\\Tcpip\\Parameters",
                        "/v", "TcpTimedWaitDelay", "/t", "REG_DWORD", "/d", "30", "/f")).build();
        ExecutionRole reg1Role = new ExecutionRole.Builder(machineId + "_reg1RoleId")
                .syncCommand(reg1FlowContext).build();
        machine.addRole(reg1Role);

        RunCommandFlowContext reg2FlowContext = new RunCommandFlowContext.Builder("reg")
                .args(Arrays.asList("add", "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Services\\Tcpip\\Parameters",
                        "/v", "StrictTimeWaitSeqCheck", "/t", "REG_DWORD", "/d", "1", "/f")).build();
        ExecutionRole reg2Role = new ExecutionRole.Builder(machineId + "_reg2RoleId")
                .syncCommand(reg2FlowContext).build();
        machine.addRole(reg2Role);

        RunCommandFlowContext netsh1FlowContext = new RunCommandFlowContext.Builder("netsh")
                .args(Arrays.asList("int", "ipv4", "set", "dynamicport", "tcp", "start=1025", "num=60000")).build();
        ExecutionRole netsh1Role = new ExecutionRole.Builder(machineId + "_netsh1RoleId")
                .syncCommand(netsh1FlowContext).build();
        machine.addRole(netsh1Role);

        RunCommandFlowContext netsh2FlowContext = new RunCommandFlowContext.Builder("netsh")
                .args(Arrays.asList("int", "ipv4", "set", "dynamicport", "udp", "start=1025", "num=60000")).build();
        ExecutionRole netsh2Role = new ExecutionRole.Builder(machineId + "_netsh2RoleId")
                .syncCommand(netsh2FlowContext).build();
        machine.addRole(netsh2Role);

        RunCommandFlowContext netsh3FlowContext = new RunCommandFlowContext.Builder("netsh")
                .args(Arrays.asList("int", "ipv6", "set", "dynamicport", "tcp", "start=1025", "num=60000")).build();
        ExecutionRole netsh3Role = new ExecutionRole.Builder(machineId + "_netsh3RoleId")
                .syncCommand(netsh3FlowContext).build();
        machine.addRole(netsh3Role);

        RunCommandFlowContext netsh4FlowContext = new RunCommandFlowContext.Builder("netsh")
                .args(Arrays.asList("int", "ipv6", "set", "dynamicport", "udp", "start=1025", "num=60000")).build();
        ExecutionRole netsh4Role = new ExecutionRole.Builder(machineId + "_netsh4RoleId")
                .syncCommand(netsh4FlowContext).build();
        machine.addRole(netsh4Role);
    }


}
