package com.ca.apm.systemtest.fld.testbed;

import com.ca.apm.systemtest.fld.role.APMJDBCQueryLoadRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

public class FLDApmJDBCQueryLoadTestbedProvider implements FldTestbedProvider, FLDLoadConstants, FLDConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(FLDApmJDBCQueryLoadTestbedProvider.class);
    
    private String apmServer;
    private String machineId;

    public FLDApmJDBCQueryLoadTestbedProvider(String apmServer, String machineId) {
        this.apmServer = apmServer;
        this.machineId = machineId;
    }
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        
        return new ArrayList<ITestbedMachine>();
    }

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        ITestbedMachine machine = testbed.getMachineById(machineId);
        
        APMJDBCQueryLoadRole role = new APMJDBCQueryLoadRole.Builder(APM_JDBC_QUERY_LOAD_ROLE_ID, tasResolver)
                .setApmServer(apmServer).build();
        
        machine.addRole(role);
    }
}
