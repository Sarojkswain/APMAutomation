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
package com.ca.apm.tests.flow.agent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.commandline.Execution;

/**
 * UnregisterNetAgentFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class UnregisterNetAgentFlow extends RegisterNetAgentFlow {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnregisterNetAgentFlow.class);

    @FlowContext
    private RegisterNetAgentFlowContext context;

    public UnregisterNetAgentFlow() {
    }

    public void run() throws IOException {
        try {
            // UNREGISTER DLLs
            this.runRegsrv(context.getAgentPath() + "\\wily\\bin\\x86\\wily.NativeProfiler.dll", true);
            this.runRegsrv(context.getAgentPath() + "\\wily\\bin\\wily.NativeProfiler.dll", true);
            // MODIFY REGISTRY
            this.runReg("HKLM\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment",
                    "com.wily.introscope.agentProfile", "REG_SZ", "\"\"");
            this.runReg("HKLM\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment",
                    "Cor_Enable_Profiling", "REG_SZ", "0x0");
            this.runReg("HKLM\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment",
                    "COR_PROFILER", "REG_SZ", "\"\"");
            // UNREGISTER GACUTIL
            String dllVersion = getDllVersion(context.getAgentPath() + "\\wily\\bin\\wily.Agent.dll");
            if (dllVersion == null) {
                dllVersion = context.getAgentDllVersion() != null ? context.getAgentDllVersion() : context.getAgentVersion();
            }
            LOGGER.info(context.getAgentPath() + "\\wily\\bin\\wily.Agent.dll - DLL version: " + dllVersion);
            this.runGacutil("wily.Agent, Version=" + dllVersion, true);
            this.runGacutil("wily.WebServicesAgent.ext, Version=" + dllVersion, true);
            // DELETE SERVICE
            this.unregisterSc();
        } catch (InterruptedException var3) {
            throw new IllegalStateException(var3);
        }
    }

    protected String getDllVersion(String dllPath) {
        String dllVersion = null;
        try {
            ProcessBuilder pb = new ProcessBuilder("powershell", "(Get-Item " + dllPath + ").VersionInfo.ProductVersion");
            Process p = pb.start();
            synchronized (this) {
                try {
                    wait(5000L);
                } catch (InterruptedException e) {}
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                dllVersion = br.readLine();
            }
            LOGGER.info("Got DLL version for " + dllPath + ": " + dllVersion);
            try {
                p.destroy();
            } catch (Exception e) {}
        } catch (Exception e) {
            LOGGER.warn("Cannot get DLL version for " + dllPath + ": " + e, e);
        }
        return dllVersion;
    }

    protected void unregisterSc() throws InterruptedException {
        int responseCode = this.getExecutionBuilder(LOGGER, "sc")
                .args(new String[]{"delete", "PerfMonCollectorAgent"}).build().go();
        switch (responseCode) {
            case 0: {
                LOGGER.info("SC Execution completed SUCCESSFULLY! Congratulations!");
                return;
            }
            case 1060: {
                LOGGER.warn("The specified service does not exist as an installed service.");
                return;
            }
            default:
                throw new IllegalStateException(String.format("SC Execution failed (%d)", new Object[]{responseCode}));
        }
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }

}
