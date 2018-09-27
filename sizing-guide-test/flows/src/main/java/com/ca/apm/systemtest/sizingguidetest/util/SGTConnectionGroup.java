package com.ca.apm.systemtest.sizingguidetest.util;

import com.ca.apm.testing.metricsynth.AgentConnection;
import com.ca.apm.testing.metricsynth.ConnectionGroup;

public class SGTConnectionGroup extends ConnectionGroup {

    @Override
    public void addAgentConnection(AgentConnection agentConnection, String[] ejbs, String[] webapps) {
        super.addAgentConnection(agentConnection, ejbs == null ? new String[0] : ejbs,
            webapps == null ? new String[0] : webapps);
    }

}
