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
 * 
 * Author : KETSW01/ KETHIREDDY SWETHA
 */
package com.ca.apm.tests.testbed;


import com.ca.tas.artifact.thirdParty.OracleDbVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.oracle.OracleDbRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * The testbed is for deployment of Oracle Database
 * 
 */

@TestBedDefinition
public class OracleDatabaseTestbed implements ITestbedFactory {

    public static final String ORACLE_DB_MACHINE_ID = "oracleMachine";
    public static final String ORACLE_DB_ROLE_ID = "oracleRole";
    public static final String ORACLE_DB_TEMPLATE_ID = "w64_8G";
    public static final String ORACLE_DB_INSTALL_LOCATION = "";


    @Override
    public ITestbed create(ITasResolver tasResolver) {
        // Oracle database role
        OracleDbRole oracleDBRole =
            new OracleDbRole.Builder(ORACLE_DB_ROLE_ID, tasResolver).version(
                OracleDbVersion.Oracle12cR1EEw).build();

        // Configuration of oracle machine
        TestbedMachine oracleDBMachine =
            TestBedUtils.createWindowsMachine(ORACLE_DB_MACHINE_ID, ORACLE_DB_TEMPLATE_ID,
                oracleDBRole);

        return Testbed.create(this, oracleDBMachine);

    }


}
