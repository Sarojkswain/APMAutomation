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

package com.ca.tas.test.em.appmap;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.revert.RevertRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Extends AppMapTradingServiceTestBed to revert all machines.
 * 
 * @author korzd01
 * 
 */
@TestBedDefinition
public class AppMapTradingServiceMaintenanceTestBed extends AppMapTradingServiceTestBed {

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        
        ITestbed result = super.create(tasResolver);
        
        new RevertRole.Builder(result, tasResolver).build();
        
        return result;
    }
}
