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

import com.ca.apm.test.em.util.EmConnectionInfo;
import com.ca.apm.test.em.util.RoleUtility;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class MathAppEmTestBed implements ITestbedFactory {
    
    static final public String MACHINE_ID = "endUserMachine";
    static final public String EM_ROLE_ID = "introscope";

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed("Introscope/AppMap/MathApp");

        ITestbedMachine emMachine = TestBedUtils.createWindowsMachine(MACHINE_ID, ITestbedMachine.TEMPLATE_W64);
        ITestbedMachine cronMachine = TestBedUtils.createLinuxMachine("gateway", ITestbedMachine.TEMPLATE_CO65);
        testbed.addMachine(emMachine, cronMachine);
        
        EmRole emRole = new EmRole.Builder(EM_ROLE_ID, tasResolver).dbpassword("quality")
                .emLaxNlJavaOption(RoleUtility.getDevEmLaxnlJavaOption(9001)).build();
        emMachine.addRole(emRole);
        
        EmConnectionInfo emInfo = new EmConnectionInfo(emRole, tasResolver);
        
        IRole mathAppRole = RoleUtility.addMathAppRoles(emMachine, emInfo, null, tasResolver);
        mathAppRole.after(emRole);
        
        String mathBaseUrl = "http://"
            + RoleUtility.hostnameToFqdn(tasResolver.getHostnameById(mathAppRole.getRoleId()))
            + ":8080/";
        IRole cronRole = RoleUtility.addMathAppCronRole(cronMachine, mathBaseUrl, tasResolver);
        cronRole.after(mathAppRole);
        
        return testbed;
    }
}
