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

package com.ca.apm.powerpack.sysview.tests.testbed;

import com.ca.apm.powerpack.sysview.tests.role.CeapmRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedCleanerFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic Mainframe cleanup testbed for use with testbeds that have no special requirements.
 *
 * <p>Currently it will go through all {@link CeapmRole} roles deployed by the testbed and stop the
 * associated started task if still running.
 */
@TestBedDefinition
public class MainframeTestbedCleanup implements TestBedCleanerFactory<ITestbedFactory> {
    private static final Logger logger = LoggerFactory.getLogger(MainframeTestbedCleanup.class);

    @Override
    public ITestbed create(ITasResolver tasResolver, ITestbedFactory testbedFactory) {
        ITestbed deployedTestbed = testbedFactory.create(tasResolver);
        ITestbed cleanupTestbed = new Testbed(getClass().getSimpleName());

        logger.info("Doing cleanup for mainframe testbed: {}", deployedTestbed);

        for (ITestbedMachine machine : deployedTestbed.getMachines()) {
            for (IRole role : machine.getRoles()) {

                // CE-APM Agent role cleanup
                if (CeapmRole.class.isAssignableFrom(role.getClass())) {
                    if (!cleanupTestbed.getMachines().contains(machine)) {
                        // Clean up original role-to-machine mapping (required if adding to the
                        // cleanup testbed)
                        machine.empty();
                        cleanupTestbed.addMachine(machine);
                    }

                    CeapmRole ceapmRole = (CeapmRole)role;
                    logger.info("Will clean up CE-APM Agent role '{}' on '{}'",
                        ceapmRole.getRoleId(), machine.getMachineId());
                    machine.addRole(new CeapmRole.Cleanup(ceapmRole.getRoleId() + "Cleanup",
                        ceapmRole));
                }
            }
        }

        return cleanupTestbed;
    }
}
