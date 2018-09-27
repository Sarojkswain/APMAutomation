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
public class SamlEmInternalIdpTestbedLinux implements SamlEmInternalIdpTestbed {

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed("SamlEmInternalIdpTestbedLinux");

        TestbedMachine machine01 =
            new TestbedMachine.LinuxBuilder(MACHINE_ID).templateId(ITestbedMachine.TEMPLATE_RH66).build();

        TestbedMachine machine02 =
            new TestbedMachine.Builder(MACHINE_ID2).templateId(ITestbedMachine.TEMPLATE_W64).build();

        String emInstallPath = machine01.getAutomationBaseDir() + "em";

        EmRole emRole =
            new EmRole.LinuxBuilder(ROLE_EM, tasResolver).installDir(emInstallPath).nostartEM()
                .nostartWV().build();

        // Reconfigure EM for SAML with internal IdP and restart EM and WV
        // Enable following when available in TAS release
        //        String emInstallDir = emRole.getDeployEmFlowContext().getInstallDir();
        SamlConfigurationRole samlRole =
            new SamlConfigurationRole.LinuxBuilder(ROLE_SAML_CONFIG).apmRootDir(emInstallPath)
                .build();
        samlRole.after(emRole);

        machine01.addRole(emRole, samlRole);

        SeleniumGridMachinesFactory seleniumGridMachinesFactory = new SeleniumGridMachinesFactory();
        Collection<ITestbedMachine> seleniumGridMachines = seleniumGridMachinesFactory.createMachines(tasResolver, machine02, machine02);

        testbed.addMachine(machine01, seleniumGridMachines.toArray(new ITestbedMachine[seleniumGridMachines.size()]));
        return testbed;
    }

}
