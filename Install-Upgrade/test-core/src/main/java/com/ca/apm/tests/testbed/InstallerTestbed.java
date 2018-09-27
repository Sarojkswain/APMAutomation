package com.ca.apm.tests.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import com.ca.tas.artifact.IBuiltArtifact.Version;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmInstallerRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

//this is a sample testbed written for checking installer role

@TestBedDefinition
public class InstallerTestbed implements ITestbedFactory
{
    public static final String EM_MACHINE_ID = "emMachine";
    public static final String EM_ROLE_ID = "emRole";
    private static final String EM_MACHINE_TEMPLATE_ID = TEMPLATE_W64;

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        
        //create EM role
        EmInstallerRole emRole = new EmInstallerRole.Builder(EM_ROLE_ID, tasResolver)
            .instroscopeVersion(Version.SNAPSHOT_SYS_99_99)
            .build();
        
        //map EM role to machine
        ITestbedMachine emMachine = TestBedUtils.createWindowsMachine(EM_MACHINE_ID, EM_MACHINE_TEMPLATE_ID, emRole);
        return new Testbed(getClass().getSimpleName()).addMachine(emMachine);
    }
}
