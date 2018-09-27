package com.ca.apm.tests.testbed;

import com.ca.apm.tests.role.NodeJSAppRole;
import com.ca.apm.tests.role.NodeJSProbeRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.NodeJsRole;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.apm.tests.role.UMAgentRole;

@TestBedDefinition
public class NodeJSLoadGATestbed extends NodeJSLoadTestbed {
  
    public static final String PUBLISHED_PROBE_PACKAGE_NAME = "ca-apm-probe";

    @Override
    protected NodeJSProbeRole createNodeJsProbeRole(ITasResolver tasResolver,
            UMAgentRole umAgentRole, NodeJSAppRole nodejsAppRole,
            NodeJsRole nodeJsRole, String roleId) {
        
        NodeJSProbeRole role = new NodeJSProbeRole.LinuxBuilder(roleId, tasResolver)
                .packageName(PUBLISHED_PROBE_PACKAGE_NAME).UMAgentRole(umAgentRole)
                .nodeJSAppRole(nodejsAppRole).nodeJSRole(nodeJsRole).build();

        role.addProperty(NODEJS_PROBE_ARTIFACT_NAME, PUBLISHED_PROBE_PACKAGE_NAME);
        return role;
    }
}
