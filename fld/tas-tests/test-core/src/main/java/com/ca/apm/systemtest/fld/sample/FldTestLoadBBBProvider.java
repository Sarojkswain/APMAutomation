/**
 * 
 */
package com.ca.apm.systemtest.fld.sample;

import java.util.Collection;
import java.util.Collections;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.web.QaAppTomcatRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.TestbedMachine;

/**
 * @author keyja01
 *
 */
public class FldTestLoadBBBProvider implements FldTestbedProvider {
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        return Collections.emptySet();
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.testbed.FldTestbedProvider#initTestbed(com.ca.tas.testbed.Testbed, com.ca.tas.resolver.ITasResolver)
     */
    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        
        WebAppRole<TomcatRole> qaAppTomcatRole =
            new QaAppTomcatRole.Builder("qaAppTomcatRole", tasResolver)
                .cargoDeploy().contextName("qa-app")
                .build();

        TomcatRole tomcatRole = new TomcatRole.Builder("tomcatRole", tasResolver)
            .tomcatVersion(TomcatVersion.v80)
            .tomcatCatalinaPort(9091)
            .webApp(qaAppTomcatRole)
            .build();
        
        EmRole emRole = (EmRole) testbed.getRoleById("emRole");
        
        IRole tomcatAgentRole = new AgentRole.Builder("tomcatAgentRole", tasResolver)
            .webAppServer(tomcatRole)
            .intrumentationLevel(AgentInstrumentationLevel.FULL)
            .emRole(emRole)
            .build();
        
        TestbedMachine tomcatMachine = TestBedUtils.createWindowsMachine("tomcatMachine", "w64", qaAppTomcatRole, tomcatRole, tomcatAgentRole);
        testbed.addMachine(tomcatMachine);
    }

}
