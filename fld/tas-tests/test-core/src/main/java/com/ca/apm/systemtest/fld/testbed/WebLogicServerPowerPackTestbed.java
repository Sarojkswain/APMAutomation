package com.ca.apm.systemtest.fld.testbed;

import com.ca.tas.artifact.thirdParty.OracleDbVersion;
import com.ca.tas.artifact.thirdParty.WebLogicVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.oracle.OracleDbRole;
import com.ca.tas.role.webapp.WebLogicRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class WebLogicServerPowerPackTestbed implements ITestbedFactory {
    public static final String MACHINE_ID = "webLogicPowerPackTestMachine"; 
    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbedMachine testMachine =
            new TestbedMachine.Builder(MACHINE_ID).templateId(ITestbedMachine.TEMPLATE_W64).build();

        WebLogicRole wlRole =
            new WebLogicRole.Builder("wls", tasResolver)
                .installLocation("C:/sw/webLogicServer")
                .installLogFile("C:/sw/webLogicServer/install.log")
                .version(WebLogicVersion.v103x86w)
                .responseFileDir("C:/sw/webLogicServer/responseFiles")
                .installDir("C:/sw/webLogicServer/wlserver_10.3")
                .build();

        testMachine.addRole(wlRole);

        
        OracleDbRole oracleDBRole = new OracleDbRole.Builder("oracleDb", tasResolver)
        .installPath("C:/sw/oracle/product/11.1.0/db_1")
        .installSourcesPath("C:/sw/unpackedSources/oracle11gR1")
        .version(OracleDbVersion.Oracle11gR1w)
        .build();
        
        testMachine.addRole(oracleDBRole);
        
        ITestbed testbed = new Testbed("WebLogicAgentTestbed");
        testbed.addMachine(testMachine);

        return testbed;
    }

}
