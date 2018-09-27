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
import java.util.Collections;
import java.util.HashSet;

import com.ca.apm.systemtest.fld.role.GeolocationLoadRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FLDMainClusterTestbed;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;

/**
 * FLD Geolocation Load testbed.
 * @author filja01
 *
 */
public class FLDGeolocationLoadProvider implements FldTestbedProvider, FLDConstants, FLDLoadConstants {
    
    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {

        //get TIM machine
        ITestbedMachine timMachine = testbed.getMachineById(TIM01_MACHINE_ID); 
            
        GeolocationLoadRole geoRole = 
            new GeolocationLoadRole.LinuxBuilder(GEOLOCATION_LOAD_TIM01_ROLE_ID, tasResolver)
                .dbHost(tasResolver.getHostnameById(EM_DATABASE_ROLE_ID))
                .dbAdmin(FLDMainClusterTestbed.DB_USERNAME)
                .dbPassword(FLDMainClusterTestbed.DB_PASSWORD)
                .build();
        
        geoRole.after(new HashSet<IRole>(Arrays.asList(timMachine.getRoles())));
        timMachine.addRole(geoRole);
    }

    @Override
    public Collection<ITestbedMachine> initMachines() {
        return Collections.emptySet();
    }
}
