/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed.loads;

import java.util.Arrays;
import java.util.Collection;

import com.ca.apm.systemtest.fld.role.CLWWorkStationLoadRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;

public class FLDCLWLoadProvider implements FLDConstants, FLDLoadConstants, FldTestbedProvider {
    
    private ITestbedMachine clwMachine = null;
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        clwMachine = TestBedUtils.createWindowsMachine(CLW_MACHINE_ID, "w64");
        
        return Arrays.asList(clwMachine);
    }
    

	@Override
	public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {

	    String emHost = tasResolver.getHostnameById(EM_MOM_ROLE_ID);
		// Pass MOM EM host and port and agents hosts for TT. Install Directory is optional
		CLWWorkStationLoadRole clwRole = new CLWWorkStationLoadRole.Builder(CLW_ROLE_ID, tasResolver)
	        .emHost(emHost)
	        .agentName(tasResolver.getHostnameById(EM_WEBVIEW_ROLE_ID) + ".*")
            .agentName(WLS01_HOST_NAME + ".*")
            .agentName(WLS02_HOST_NAME + ".*")
            .agentName(WAS_HOST_NAME + ".*")
            .agentName(TOMCAT_HOST_NAME + "\\|" + TOMCAT6_AGENT + ".*")
            .agentName(TOMCAT_HOST_NAME + "\\|" + TOMCAT7_AGENT + ".*")
            .agentName(TOMCAT_HOST_NAME + "\\|" + TOMCAT_AGENT_9080 + ".*")
            .agentName(TOMCAT_HOST_NAME + "\\|" + TOMCAT_AGENT_9081 + ".*")
            .agentName(DOTNET_01_HOST_NAME + ".*")
            .agentName(DOTNET_02_HOST_NAME + ".*")
            .agentName(JBOSS01_HOST_NAME + ".*")
			.build();

		clwMachine.addRole(clwRole);
	}

}
