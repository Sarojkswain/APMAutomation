/*
 * Copyright (c) 2016 CA.  All rights reserved.
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
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class SimpleEmTestBed_10_2 extends SimpleEmTestBed_10_X {

    public static final String EM_VERSION_10_2 = "10.2.0.14";

    /* (non-Javadoc)
     * @see com.ca.tas.test.em.appmap.SimpleEmTestBed_10_X#create(com.ca.tas.resolver.ITasResolver)
     */
    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = super.create(tasResolver);
        testbed.addProperty("selenium.webdriverURL", "http://cz-selenium1.ca.com:4444/wd/hub");
        testbed.addProperty(
            "test.applicationBaseURL",
            String.format("http://%s:8082", tasResolver.getHostnameById(SimpleEmTestBed_10_X.EM_ROLE_ID)));
        return testbed;
    }

    String getEmVersion() {
        return EM_VERSION_10_2;
    }
}
