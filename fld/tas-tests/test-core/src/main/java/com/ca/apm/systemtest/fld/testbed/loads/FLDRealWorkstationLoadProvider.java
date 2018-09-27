package com.ca.apm.systemtest.fld.testbed.loads;

import java.util.Arrays;
import java.util.Collection;

import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfigurationService;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.WorkstationRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;

/**
 * Testbed definition for Real Workstation Role Provider.
 *
 * @author filja01
 */
public class FLDRealWorkstationLoadProvider implements FLDConstants, FLDLoadConstants, FldTestbedProvider {
    private ITestbedMachine machine1;
    private ITestbedMachine machine2;
    
    private FLDConfiguration fldConfig = FLDConfigurationService.getConfig();
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        
        machine1 = new TestbedMachine.Builder(REAL_WORKSTATION_01_MACHINE_ID)
            .platform(Platform.WINDOWS)
            .bitness(Bitness.b64)
            .templateId(ITestbedMachine.TEMPLATE_W64)
            .build();
        machine2 = new TestbedMachine.Builder(REAL_WORKSTATION_02_MACHINE_ID)
            .platform(Platform.WINDOWS)
            .bitness(Bitness.b64)
            .templateId(ITestbedMachine.TEMPLATE_W64)
            .build();
        
        return Arrays.asList(machine1, machine2);
    }
    

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        
        initRealWorkstation(testbed, tasResolver, machine1, REAL_WORKSTATION_01_ROLE_ID);
        initRealWorkstation(testbed, tasResolver, machine2, REAL_WORKSTATION_02_ROLE_ID);
    }
        
    private void initRealWorkstation(ITestbed testbed, ITasResolver tasResolver, ITestbedMachine machine, String roleId) {

        WorkstationRole realWorkstationRole = new WorkstationRole.Builder(roleId, tasResolver)
            .deployPath("C:/SW")
            .workstationVersion(fldConfig.getEmVersion())
            .eulaVersion(fldConfig.getEmVersion())
            .host(tasResolver.getHostnameById(EM_MOM_ROLE_ID))
            .agent("SuperDomain|Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Metric Agent (Virtual)")
            .metric("Enterprise Manager:Overall Capacity (%)")
            //.start()
            .build();
        machine.addRole(realWorkstationRole);
        
    }

}
