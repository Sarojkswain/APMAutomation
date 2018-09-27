package com.ca.apm.systemtest.fld.testbed;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.artifact.thirdparty.TasTestsCoreVersion;
import com.ca.apm.systemtest.fld.role.CentosVMDeployNtpdRole;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;

public class TimeSynchronizationTestbedProvider implements FldTestbedProvider, FLDLoadConstants, FLDConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeSynchronizationTestbedProvider.class);

    public static final JavaBinary JAVA_VERSION = JavaBinary.WINDOWS_64BIT_JDK_17;
    public static final String NTPDATE_PACKAGE_NAME = "ntpdate";

    private String[] machineIds;

    public TimeSynchronizationTestbedProvider(String[] machineIds) {
        Args.check(machineIds != null && machineIds.length > 0,
            "machineIds is null or empty");
        this.machineIds = Arrays.copyOf(machineIds, machineIds.length);
    }
    
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        // this provider does not create any new machines
        return Collections.emptySet();
    }
    

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        for (String machineId : machineIds) {
            LOGGER.debug("Installing and configuring NTPD on " + machineId);
            ITestbedMachine timeSynchronizationMachine = testbed.getMachineById(machineId);
            
            CentosVMDeployNtpdRole deployNtpdRole = new CentosVMDeployNtpdRole.Builder("deployNtpd" + machineId)
                .ntpServer("isltime01.ca.com")
                .ntpServer("isltime02.ca.com")
                .build();

            timeSynchronizationMachine.addRole(deployNtpdRole);
        }
    }

}
