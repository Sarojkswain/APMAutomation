/*
 * Copyright (c) 2014 CA.  All rights reserved.
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

package com.ca.apm.tests.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.WorkstationRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/*
 * Testbed class with following components installed : 
 * 	EM
 * 	Webview
 */
@TestBedDefinition
public class EMWebviewWindowsTestbed implements ITestbedFactory{
	public static final String EM_MACHINE_ID = "emMachine";
    public static final String EM_ROLE_ID = "emRole";
    private static final String EM_MACHINE_TEMPLATE_ID = TEMPLATE_W64;
    public static final String WORKSTATION_ROLE_ID = "workstationRole";

    @Override
    public ITestbed create(ITasResolver tasResolver){
    	//Create EM Role
    	EmRole emRole = new EmRole.Builder(EM_ROLE_ID, tasResolver).nostartEM().nostartWV().build();
    	
    	ITestbedMachine emMachine = TestBedUtils.createWindowsMachine(EM_MACHINE_ID, EM_MACHINE_TEMPLATE_ID,emRole);
    	
    	return new Testbed(getClass().getSimpleName()).addMachine(emMachine);
    	
    	
    	//map EM role to machine    	

    }
	
	
	
}
