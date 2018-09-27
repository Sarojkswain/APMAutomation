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

import com.ca.apm.powerpack.sysview.tests.role.CicsRole.CicsConfig;
import com.ca.apm.powerpack.sysview.tests.role.DatacomRole.DatacomConfig;
import com.ca.apm.powerpack.sysview.tests.role.ImsRole.ImsConfig;
import com.ca.apm.powerpack.sysview.tests.role.MqZosRole.MqZosConfig;
import com.ca.apm.powerpack.sysview.tests.role.SysvDb2Role.SysvDb2Config;
import com.ca.apm.powerpack.sysview.tests.role.SysviewRole.SysviewConfig;

import java.util.Collection;

// TODO: When we can switch to 1.8 two things can be improved here:
//   1. Make the get* methods static
//   2. Add default implementation to the get* methods that use reflection to look for
//      static class variables of the appropriate types.

/**
 * Interface for testbeds containing mainframe roles.
 */
public interface MainframeTestbed {
    /**
     * Returns the set of Sysview instances deployed by the testbed.
     *
     * @return Sysview instances deployed by the testbed.
     */
    Collection<SysviewConfig> getSysviewInstances();

    /**
     * Returns the set of Sysview for DB2 instances deployed by the testbed.
     *
     * @return Sysview for DB2 instances deployed by the testbed.
     */
    Collection<SysvDb2Config> getSysvdb2Instances();

    /**
     * Returns the set of CICS regions deployed by the testbed.
     *
     * @return CICS regions deployed by the testbed.
     */
    Collection<CicsConfig> getCicsRegions();

    /**
     * Returns the set of MQ subsystems deployed by the testbed.
     *
     * @return MQ subsystems deployed by the testbed.
     */
    Collection<MqZosConfig> getMqSubsystems();

    /**
     * Returns the set of IMS regions deployed by the testbed.
     *
     * @return IMS regions deployed by the testbed.
     */
    Collection<ImsConfig> getImsRegions();

    /**
     * Returns the set of DATACOM instances deployed by the testbed.
     *
     * @return DATACOM instances deployed by the testbed.
     */
    Collection<DatacomConfig> getDatacomInstances();
}
