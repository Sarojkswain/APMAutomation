package com.ca.apm.tests.test;

import com.ca.apm.tests.testbed.WindowsStandaloneTestbed;
import com.ca.tas.role.webapp.TomcatRole;

public class AgentOperations extends ElementsIdentification {

    public void stopAllAgents() {
        initializeEMandAgents();
        // stop Tomcat
        runSerializedCommandFlowFromRole(WindowsStandaloneTestbed.TOMCAT_ROLE_ID,
            TomcatRole.ENV_TOMCAT_STOP);

    }

    public void startAllAgents() {
        initializeEMandAgents();
        // start Tomcat
        runSerializedCommandFlowFromRole(WindowsStandaloneTestbed.TOMCAT_ROLE_ID,
            TomcatRole.ENV_TOMCAT_START);

    }

}
