/*
 * Copyright (c) 2016 CA. All rights reserved.
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

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.DeployFreeRole;
import com.ca.tas.role.IRole;
import com.ca.tas.testbed.CodaTestBed;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.tests.annotations.TestBedDefinition;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

/**
 * CodaNoOpSmokeTestbed class.
 *
 * CODA no-op smoke test-bed
 *
 * @author pojja01@ca.com
 */
@TestBedDefinition
public class CodaNoOpSmokeTestbed extends CodaTestBed {

    public static final String NODE_MACHINE_ID = "nodeMachine";
    private static final String CODA_NODE_TEMPLATE = TEMPLATE_W64;

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed codaTestBed = super.create(tasResolver);

        // create roles used in CODA test
        IRole webappRole = new DeployFreeRole("webapp01");
        IRole agentRole = new DeployFreeRole("agent01");
        IRole dbRole = new DeployFreeRole("db01");
        IRole emRole = new DeployFreeRole("em01");
        IRole clientRole = new DeployFreeRole("client01");

        // assign roles to node machine
        ITestbedMachine nodeMachine =
            TestBedUtils.createWindowsMachine(NODE_MACHINE_ID, CODA_NODE_TEMPLATE, webappRole,
                agentRole, dbRole, emRole, clientRole);
        return codaTestBed.addMachine(nodeMachine);
    }
}
