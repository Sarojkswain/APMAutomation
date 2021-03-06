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

package com.ca.apm.systemtest.fld.testbed;

import java.util.Arrays;
import java.util.Collection;

import com.ca.apm.systemtest.fld.role.WorkStationLoadRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * FLD WorkStation Load testbed.
 * 
 * @author banra06
 *
 */
@TestBedDefinition
public class FLDWorkStationLoadProvider implements FldTestbedProvider, FLDConstants, FLDLoadConstants {
    private ITestbedMachine wsMachine;
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        wsMachine = TestBedUtils.createWindowsMachine(WORKSTATION_LOAD_MACHINE_ID, "w64");
        return Arrays.asList(wsMachine);
    }
    
	@Override
	public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
		WorkStationLoadRole wsRole = new WorkStationLoadRole.Builder(
				WORKSTATION_LOAD_ROLE_ID, tasResolver)
				.emHost(tasResolver.getHostnameById(EM_MOM_ROLE_ID))
				.emPort(5001).branch("99.99.phoenix-SNAPSHOT").build();

		wsMachine.addRole(wsRole);
	}
}
