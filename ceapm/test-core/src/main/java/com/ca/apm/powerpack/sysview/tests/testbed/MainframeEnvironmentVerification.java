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

import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Version of the {@link MainframeEnvironment} testbed that only checks the environment.
 */
// TODO: This testbed doesn't technically require cleanup but there is a bug in TAS that causes
//       testbeds with physical machines that do not include an explicit cleanup testbed to fail.
//       The issues is tracked under DE163116.
@TestBedDefinition(cleanUpTestBed = MainframeTestbedCleanup.class)
public class MainframeEnvironmentVerification extends MainframeEnvironment {
    @Override
    protected boolean shouldOnlyVerify() {
        return true;
    }
}
