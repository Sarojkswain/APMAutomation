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

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * RegisterNetAgentFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class RegisterNetAgentFlow extends FlowBase {

    public static final String REGSRV32_PATH = "c:\\Windows\\SysWOW64\\regsvr32.exe";

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterNetAgentFlow.class);
    @FlowContext
    private RegisterNetAgentFlowContext context;

    public RegisterNetAgentFlow() {
    }

    public void run() throws IOException {
        try {
            // REGISTER DLLs
            runRegsrv(this.context.getAgentPath() + "\\wily\\bin\\x86\\wily.NativeProfiler.dll", false);
            runRegsrv(this.context.getAgentPath() + "\\wily\\bin\\wily.NativeProfiler.dll", false);
            // REGISTER GACUTIL
            runGacutil(this.context.getAgentPath() + "\\wily\\bin\\wily.Agent.dll", false);
            runGacutil(this.context.getAgentPath() + "\\wily\\ext\\wily.WebServicesAgent.ext.dll", false);
            // MODIFY REGISTRY
            runReg("HKLM\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment",
                    "com.wily.introscope.agentProfile", "REG_SZ", this.context.getAgentPath() + "\\wily\\IntroscopeAgent.profile");
            runReg("HKLM\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment",
                    "Cor_Enable_Profiling", "REG_SZ", "0x1");
            runReg("HKLM\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment",
                    "COR_PROFILER", "REG_SZ", "{5F048FC6-251C-4684-8CCA-76047B02AC98}");
            // CREATE SERVICE
            this.registerSc(this.context.getAgentPath() + "\\wily\\bin\\PerfMonCollectorAgent.exe");
        } catch (InterruptedException var3) {
            throw new IllegalStateException(var3);
        }
        LOGGER.info("Flow has finished.");
    }

    protected void runRegsrv(String dllPath, boolean unregister) throws InterruptedException {
        int responseCode = this.getExecutionBuilder(LOGGER, REGSRV32_PATH)
                .args(new String[]{"/s", unregister ? "/u" : "", dllPath}).build().go();
        switch (responseCode) {
            case 0:
                LOGGER.info("Regsrv Execution completed SUCCESSFULLY! Congratulations!");
                return;
            default:
                throw new IllegalStateException(String.format("Regsrv Execution failed (%d)", new Object[]{responseCode}));
        }
    }

    protected void runGacutil(String arg, boolean unregister) throws InterruptedException {
        int responseCode = this.getExecutionBuilder(LOGGER, this.context.getGacutilPath() + "\\gacutil.exe")
                .args(new String[]{unregister ? "/u" : "/i", arg}).build().go();
        switch (responseCode) {
            case 0:
                LOGGER.info("Gacutil Execution completed SUCCESSFULLY! Congratulations!");
                return;
            default:
                throw new IllegalStateException(String.format("Gacutil Execution failed (%d)", new Object[]{responseCode}));
        }
    }

    protected void runReg(String key, String vParam, String tParam, String dParam) throws InterruptedException {
        int responseCode = this.getExecutionBuilder(LOGGER, "reg")
                .args(new String[]{"add", key, "/v", vParam, "/t", tParam, "/d", dParam, "/f"}).build().go();
        switch (responseCode) {
            case 0:
                LOGGER.info("REG Execution completed SUCCESSFULLY! Congratulations!");
                return;
            default:
                throw new IllegalStateException(String.format("REG Execution failed (%d)", new Object[]{responseCode}));
        }
    }

    protected void registerSc(String binPath) throws InterruptedException {
        int responseCode = this.getExecutionBuilder(LOGGER, "sc")
                .args(new String[]{"create", "PerfMonCollectorAgent", "binPath=", binPath, "start=", "auto", "DisplayName=", "CA APM PerfMon Collector Service"}).build().go();
        switch (responseCode) {
            case 0:
                LOGGER.info("SC Execution completed SUCCESSFULLY! Congratulations!");
                return;
            default:
                throw new IllegalStateException(String.format("SC Execution failed (%d)", new Object[]{responseCode}));
        }
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }
}
