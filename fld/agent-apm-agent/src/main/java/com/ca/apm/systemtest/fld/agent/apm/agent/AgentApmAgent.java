package com.ca.apm.systemtest.fld.agent.apm.agent;

import java.lang.instrument.Instrumentation;

public class AgentApmAgent {
    @SuppressWarnings("unused")
    public static void agentmain(String agentArgs, Instrumentation inst) {
    }

    @SuppressWarnings("unused")
    public static void premain(String agentArgs, Instrumentation inst) {
        agentmain(agentArgs, inst);
    }
}
