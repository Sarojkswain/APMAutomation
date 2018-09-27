package com.ca.apm.systemtest.alertstateload.util;

import com.ca.apm.testing.metricsynth.AgentConnection;
import com.ca.apm.testing.metricsynth.ConnectionGroup;

public class ASLConnectionGroup extends ConnectionGroup {

    @Override
    public void addAgentConnection(AgentConnection agentConnection, String[] ejbs, String[] webapps) {
        super.addAgentConnection(agentConnection, ejbs == null ? new String[0] : ejbs,
            webapps == null ? new String[0] : webapps);
    }

}
