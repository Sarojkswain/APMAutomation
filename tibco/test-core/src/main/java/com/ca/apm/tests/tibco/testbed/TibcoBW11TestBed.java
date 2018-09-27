/*
 * Copyright (c) 2015 CA. All rights reserved.
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

package com.ca.apm.tests.tibco.testbed;

import com.ca.apm.tests.tibco.artifact.TibcoSoftwareComponentVersions;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

/**
 * @author Vashistha Singh (sinva01@ca.com)
 *
 */
@TestBedDefinition
public class TibcoBW11TestBed implements ITestbedFactory {
    // machine and test bed
    public static final String TIBCO_MACHINE_ID = "tibcoMachine";
    public static final String TIBCO_TESTBED_ID = "tibcoTestBed";

    // Role Ids
    public static final String JAVA7_ROLE_ID = "java7";

    // Variables
    public static final String TIBCO_MACHINE_TEMPLATE_ID = TEMPLATE_W64;
    public static final String INSTALL_DIR = "C:\\sw\\tibco";
    public static final String INSTALL_UNPACK_DIR = "C:\\temp\\tibco";
    public static final String INSTALL_LOGFILE = "installer.log";


    @Override
    public ITestbed create(ITasResolver tasResolver) {

        ITestbed testBed = new Testbed(TIBCO_TESTBED_ID);
        // create a machine for EM using the w64 template id
        TestbedMachine machine =
            TestBedUtils.createWindowsMachine(TIBCO_MACHINE_ID, TIBCO_MACHINE_TEMPLATE_ID);


        // Add roles
        IRole rvRole =
            TibcoTestBedUtil.getTibcoRVRole(TibcoSoftwareComponentVersions.TibcoRVWindowsx64v8_4_0,
                tasResolver, INSTALL_LOGFILE, INSTALL_DIR, INSTALL_UNPACK_DIR);
        IRole emsRole =
            TibcoTestBedUtil.getTibcoEMSRole(
                TibcoSoftwareComponentVersions.TibcoEMSWindowsx64v6_3_0, tasResolver,
                INSTALL_LOGFILE, INSTALL_DIR, INSTALL_UNPACK_DIR);
        IRole traRole =
            TibcoTestBedUtil.getTibcoTRARole(
                TibcoSoftwareComponentVersions.TibcoTRAWindowsx64v5_8_0, tasResolver,
                INSTALL_LOGFILE, INSTALL_DIR, INSTALL_UNPACK_DIR);
        IRole bwRole =
            TibcoTestBedUtil.getTibcoBWRole(
                TibcoSoftwareComponentVersions.TibcoBWWindowsx64v5_11_0, tasResolver,
                INSTALL_LOGFILE, INSTALL_DIR, INSTALL_UNPACK_DIR);
        IRole bwAdminRole =
            TibcoTestBedUtil.getTibcoBWAdminRole(
                TibcoSoftwareComponentVersions.TibcoAdminWindowsx64v5_8_0, tasResolver,
                INSTALL_LOGFILE, INSTALL_DIR, INSTALL_UNPACK_DIR);

        // Fix the role dependencies
        bwAdminRole.after(traRole, rvRole, bwRole);
        bwRole.after(rvRole, traRole);
        emsRole.after(rvRole);
        traRole.after(rvRole, emsRole);
        // Add roles to the machine
        machine.addRole(rvRole);
        machine.addRole(emsRole);
        machine.addRole(traRole);
        machine.addRole(bwRole);
        machine.addRole(bwAdminRole);
        // setup the testbed
        testBed.addMachine(machine);
        return testBed;
    }

}
