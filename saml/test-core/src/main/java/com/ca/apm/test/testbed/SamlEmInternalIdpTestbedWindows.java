/*
 * Copyright (c) 2014 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.test.testbed;

import java.util.Collection;

import com.ca.apm.test.SamlConfigurationRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class SamlEmInternalIdpTestbedWindows implements SamlEmInternalIdpTestbed {

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed("SamlEmInternalIdPTestbedWindows");
        TestbedMachine machine01 = new TestbedMachine.Builder(MACHINE_ID)
            .templateId(ITestbedMachine.TEMPLATE_W64)
            .build();

        EmRole emRole = new EmRole.Builder(ROLE_EM, tasResolver)
            .nostartEM()
            .nostartWV()
            .build();
        // Reconfigure EM for SAML with internal IdP and restart EM and WV
        SamlConfigurationRole samlRole = new SamlConfigurationRole.Builder(ROLE_SAML_CONFIG).build();
        samlRole.after(emRole);

        machine01.addRole(emRole);
        machine01.addRole(samlRole);

        SeleniumGridMachinesFactory seleniumGridMachinesFactory = new SeleniumGridMachinesFactory();
        Collection<ITestbedMachine> seleniumGridMachines = seleniumGridMachinesFactory.createMachines(tasResolver, machine01, machine01);

        testbed.addMachine(machine01);
        testbed.addMachines(seleniumGridMachines);
        return testbed;
    }

}
