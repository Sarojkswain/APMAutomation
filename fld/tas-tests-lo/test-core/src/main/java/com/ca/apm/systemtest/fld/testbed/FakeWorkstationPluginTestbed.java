package com.ca.apm.systemtest.fld.testbed;

import com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform;
import com.ca.tas.artifact.IBuiltArtifact.Version;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Testbed definition for testing Load Orchestrator Fake Workstation plugin.
 * 
 * 
 * @author SINAL04
 *
 */
@TestBedDefinition
public class FakeWorkstationPluginTestbed implements ITestbedFactory {

    public static final String TEST_MACHINE_ID = "emMachine";
    
    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbedMachine machine = new TestbedMachine.Builder(TEST_MACHINE_ID).templateId("w64").build();
        EmRole emRole =
            new EmRole.Builder("em", tasResolver).instroscopeVersion(Version.SNAPSHOT_SYS_99_99).introscopePlatform(ArtifactPlatform.WINDOWS_AMD_64)
                .osgiDistributionVersion(Version.SNAPSHOT_SYS_99_99).build();
        machine.addRole(emRole);
        
        ITestbed testbed = new Testbed("FakeWorkstationTestbed");
        testbed.addMachine(machine);
        return testbed;
    }

}
