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

package com.ca.apm.tests.testbed;

import com.ca.apm.tests.role.ElasticSearchRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class ElasticOnlyTestBed implements ITestbedFactory {

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        ITestbed bed = new Testbed(this.getClass().getSimpleName());

        // TODO: change to 5.4.0 - it is awesome
        ITestbedMachine esMachine =
            new TestbedMachine.LinuxBuilder("esMachine").templateId("co7").bitness(Bitness.b64)
                .build();
        ElasticSearchRole esRole = new ElasticSearchRole.Builder("esRole", tasResolver).build();
        esMachine.addRole(esRole);

        ITestbedMachine loadMachine =
            new TestbedMachine.LinuxBuilder("loadMachine").templateId("co66").bitness(Bitness.b64)
                .build();

        bed.addMachine(esMachine, loadMachine);
        return bed;

    }
}
