package com.ca.apm.tests.testbed.jvm8;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author kurma05
 * TAS testbed without CODA bridge
 */
@TestBedDefinition
public class Weblogic12SysTestBed extends Weblogic12TestBed {

    @Override
    protected void addWeblogicRoles(String wlsRoleId, 
                                    TestbedMachine machine, 
                                    ITasResolver tasResolver) {
        
        addWeblogicRoles(wlsRoleId, machine, tasResolver, "99.99.sys-SNAPSHOT");
    }
}