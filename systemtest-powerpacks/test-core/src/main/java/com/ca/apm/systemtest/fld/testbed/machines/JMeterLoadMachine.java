package com.ca.apm.systemtest.fld.testbed.machines;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import com.ca.apm.systemtest.fld.role.ClientDeployRole;
import com.ca.apm.tests.artifact.JMeterVersion;
import com.ca.apm.tests.role.JMeterRole;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.TestbedMachine;

/**
 * @Author rsssa02
 */
public class JMeterLoadMachine {

    private ITasResolver tasResolver;
    public static final String JMETER_ROLE_ID = "jmeter_role";
    public static final String CLIENT_ID = "clientRole";

    public JMeterLoadMachine(ITasResolver tasResolver) {
        this.tasResolver = tasResolver;
    }

    public TestbedMachine init(String machineID) {
        TestbedMachine testMachine = new TestbedMachine.Builder(machineID).templateId(TEMPLATE_W64).build();

        ClientDeployRole clientDeployRole = new ClientDeployRole.Builder(CLIENT_ID, tasResolver).build();
        testMachine.addRole(clientDeployRole);

        JMeterRole jMeterRole = new JMeterRole.Builder(JMETER_ROLE_ID, tasResolver)
                .scriptFilePath(TasBuilder.WIN_SOFTWARE_LOC)
                .version(JMeterVersion.VER_2_11).build();
        testMachine.addRole(jMeterRole);

        return testMachine;
    }
}