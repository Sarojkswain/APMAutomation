package com.ca.apm.systemtest.fld.testbed.loads;

import java.util.Arrays;
import java.util.Collection;

import com.ca.apm.systemtest.fld.artifact.FLDHvrAgentLoadExtractArtifact;
import com.ca.apm.systemtest.fld.role.loads.HVRAgentLoadRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;

import static com.ca.apm.systemtest.fld.testbed.FLDMainClusterTestbed.EM_PORT;

/**
 * Load provider for the HVR Agent load in FLD
 * @author keyja01
 *
 */
public class HVRAgentLoadProvider implements FldTestbedProvider, FLDLoadConstants, FLDConstants {
    private ITestbedMachine machine;
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        machine = new TestbedMachine.Builder(EMLOAD_01_MACHINE_ID)
            .platform(Platform.WINDOWS)
            .templateId("w64")
            .bitness(Bitness.b64)
            .build();
        return Arrays.asList(machine);
    }

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        String host = tasResolver.getHostnameById(EM_MOM_ROLE_ID);
        FLDHvrAgentLoadExtractArtifact artifactFactory = new FLDHvrAgentLoadExtractArtifact(tasResolver);
        ITasArtifact artifact = artifactFactory.createArtifact("10.3");
        HVRAgentLoadRole role = new HVRAgentLoadRole.Builder(HVR_LOAD_ROLE_ID, tasResolver)
            .emHost(host)
            .emPort(Integer.toString(EM_PORT))
            .cloneagents(25)
            .cloneconnections(8)
            .agentHost("HVRAgent")
            .secondspertrace(1)
            .addMetricsArtifact(artifact.getArtifact())
            .build();
        machine.addRole(role);
    }

}
