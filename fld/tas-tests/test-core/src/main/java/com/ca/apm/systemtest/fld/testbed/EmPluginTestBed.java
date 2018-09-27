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

package com.ca.apm.systemtest.fld.testbed;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.DeployFreeRole;
import com.ca.tas.testbed.*;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Deploy EM installation environment class.
 * Create one machine for MOM, one collector and one database.
 */
@TestBedDefinition
public class EmPluginTestBed implements ITestbedFactory {

    public static final String MOM_MACHINE_ID = "momMachine";
    public static final String MOM_MACHINE_TEMPLATE_ID = "linux64t";

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        TestbedMachine momMachine =
            new TestbedMachine.Builder(MOM_MACHINE_ID).templateId(MOM_MACHINE_TEMPLATE_ID).build();
        TestbedMachine collectorMachine =
            new TestbedMachine.Builder("collectorMachine").templateId("linux64t").build();
        TestbedMachine databaseMachine =
            new TestbedMachine.Builder("databaseMachine").templateId("oracle11g").build();

        DeployFreeRole emRole = new DeployFreeRole("emRole");
        momMachine.addRole(emRole);
        collectorMachine.addRole(emRole);

        DeployFreeRole dbRole = new DeployFreeRole("dbRole");
        databaseMachine.addRole(dbRole);

        ITestbed testbed = new Testbed("EmCluster");
        testbed.addMachine(momMachine);
        testbed.addMachine(collectorMachine);
        testbed.addMachine(databaseMachine);

        return testbed;
    }
}
