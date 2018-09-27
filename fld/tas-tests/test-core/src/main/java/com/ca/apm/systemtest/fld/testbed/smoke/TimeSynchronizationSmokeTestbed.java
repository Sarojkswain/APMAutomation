/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed.smoke;

import com.ca.apm.systemtest.fld.role.CentosVMDeployNtpdRole;
import com.ca.apm.systemtest.fld.testbed.TimeSynchronizationTestbedProvider;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author KEYJA01
 *
 */
@TestBedDefinition
public class TimeSynchronizationSmokeTestbed implements ITestbedFactory {

    /* (non-Javadoc)
     * @see com.ca.tas.testbed.ITestbedFactory#create(com.ca.tas.resolver.ITasResolver)
     */
    @Override
    public ITestbed create(ITasResolver tasResolver) {
        Testbed testbed = new Testbed("timesyncSmoke");
        
        TestbedMachine machine = new TestbedMachine.LinuxBuilder("fooMachine")
            .templateId("co65")
            .build();
        
        testbed.addMachine(machine);
        
        CentosVMDeployNtpdRole deployNtpd = new CentosVMDeployNtpdRole.Builder("deployNtpd")
            .ntpServer("isltime01.ca.com")
            .ntpServer("isltime02.ca.com")
            .build();
        
        machine.addRole(deployNtpd);
        
        return testbed;
    }

}
