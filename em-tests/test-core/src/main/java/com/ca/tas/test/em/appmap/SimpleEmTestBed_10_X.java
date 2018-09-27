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

import com.ca.apm.test.em.util.RoleUtility;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.oracle.OracleApmDbRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;

public abstract class SimpleEmTestBed_10_X implements ITestbedFactory {

    public static final String MACHINE_ID = "standalone";
    public static final String EM_ROLE_ID = "introscope";

    public static final String ORACLE_MACHINE_ID = "orclMachine";
    public static final String ORACLE_ROLE_ID = "role_em_oracle";
    
    private ITasResolver tasResolver = null;

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        this.tasResolver = tasResolver;
        ITestbed testbed = new Testbed("Introscope/AppMap/SimpleEm_10_X");

        ITestbedMachine emMachine =
            new TestbedMachine.Builder(MACHINE_ID).platform(Platform.WINDOWS)
                .templateId("w64").bitness(Bitness.b64).automationBaseDir("C:/sw").build();
        EmRole.Builder emBuilder = new EmRole.Builder(EM_ROLE_ID, tasResolver)
                .nostartEM()
                .version(getEmVersion());
        emBuilder.installerTgDir(emBuilder.getWinDeployBase() + "installers\\em_10_X");
        if (useOracle()) {
            emBuilder.useOracle(addOracleRole(testbed, tasResolver));
        }
        EmRole emRole = emBuilder.build();
        
        emMachine.addRole(emRole);
        RoleUtility.addMmRole(emMachine, emRole.getRoleId() + "_mm", emRole, "NowhereBankMM");
        RoleUtility.addNowhereBankRole(emMachine, emRole, null, tasResolver);
        
        testbed.addMachine(emMachine);

        return testbed;
    }
    
    abstract String getEmVersion();
    
    boolean useOracle() {
        return false;
    }
    
    OracleApmDbRole addOracleRole(ITestbed testbed, ITasResolver tasResolver) {
        ITestbedMachine dbMachine =
            new TestbedMachine.Builder(ORACLE_MACHINE_ID).platform(Platform.WINDOWS)
                .templateId("w64").bitness(Bitness.b64).automationBaseDir("C:/sw").build();
        OracleApmDbRole apmOracleRole =
            new OracleApmDbRole.Builder(ORACLE_ROLE_ID, tasResolver).build();
        dbMachine.addRole(apmOracleRole);
        testbed.addMachine(dbMachine);
        return apmOracleRole;
    }
    
    ITasResolver getTasResolver() {
        return tasResolver;
    }
}
