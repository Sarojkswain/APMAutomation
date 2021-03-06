/*
 * Copyright (c) 2017 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.tas.test.em.appmap;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.EmptyRole;
import com.ca.tas.role.oracle.OracleApmDbRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;

@TestBedDefinition
public class SimpleOracleEmUpgradeTestBed extends SimpleEmUpgradeTestBed {
    
    @Override
    void customizeEmBuilder(ITestbed testbed, EmRole.Builder emBuilder, ITasResolver tasResolver) {
        ITestbedMachine dbMachine =
            new TestbedMachine.Builder(SimpleEmTestBed_10_X.ORACLE_MACHINE_ID)
                .platform(Platform.WINDOWS).templateId("w64")
                .bitness(Bitness.b64).automationBaseDir("C:/sw")
                .build();
        EmptyRole emptyRole =
            new EmptyRole.Builder(SimpleEmTestBed_10_X.ORACLE_ROLE_ID, tasResolver).build();
        dbMachine.addRole(emptyRole);
        testbed.addMachine(dbMachine);
        emBuilder
            .useOracle()
            .oracleDbHost(tasResolver.getHostnameById(SimpleEmTestBed_10_X.ORACLE_ROLE_ID))
            .oracleDbUsername(OracleApmDbRole.Builder.DEFAULT_APM_USER)
            .oracleDbPassword(OracleApmDbRole.Builder.DEFAULT_APM_PASSWORD); 
    }

}
