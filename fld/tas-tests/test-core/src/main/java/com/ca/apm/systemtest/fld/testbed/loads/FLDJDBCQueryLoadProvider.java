/*
 * Copyright (c) 2014 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and proprietary and shall not
 * be duplicated, used, disclosed or disseminated in any way except as authorized by the applicable
 * license agreement, without the express written permission of CA. All authorized reproductions
 * must be marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT PERMITTED BY APPLICABLE
 * LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF ANY KIND, INCLUDING WITHOUT LIMITATION, ANY
 * IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR INDIRECT, FROM THE
 * USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST PROFITS, BUSINESS INTERRUPTION,
 * GOODWILL, OR LOST DATA, EVEN IF CA IS EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.systemtest.fld.testbed.loads;

import java.util.Arrays;
import java.util.Collection;

import com.ca.apm.systemtest.fld.role.JDBCQueryLoadRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * FLD JDBC Query Load testbed.
 * @author filja01
 *
 */
@TestBedDefinition
public class FLDJDBCQueryLoadProvider implements FldTestbedProvider, FLDConstants, FLDLoadConstants {
    private ITestbedMachine jdbcMachine;
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        jdbcMachine = TestBedUtils.createWindowsMachine(JDBC_QUERY_MACHINE_ID, "w64");

        return Arrays.asList(jdbcMachine);
    }
    
    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {

        String emHost = tasResolver.getHostnameById(EM_MOM_ROLE_ID);
            
        JDBCQueryLoadRole jdbcRole = 
            new JDBCQueryLoadRole.Builder(JDBC_QUERY_ROLE_ID, tasResolver)
                .emHost(emHost)
                .build();
        
        jdbcMachine.addRole(jdbcRole);
    }
}
