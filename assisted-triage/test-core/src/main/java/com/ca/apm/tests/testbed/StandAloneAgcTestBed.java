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

package com.ca.apm.tests.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO66;

import java.util.Arrays;
import java.util.HashSet;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext.EmRoleEnum;
import com.ca.apm.systemtest.fld.role.AGCRegisterRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Deploys simple cluster with stand alone as AGC and have a stand alone and
 * simple cluster register as providers.
 */
@TestBedDefinition
public class StandAloneAgcTestBed implements ITestbedFactory {

    public static final String SA_MASTER_MACHINE = "saMaster";
    public static final String SA_MASTER_ROLE = "saMasterRole";

    public static final String SA_PROVIDER_MACHINE = "saProvider";
    public static final String SA_PROVIDER_ROLE = "saProviderRole";

    public static final String MOM_PROVIDER_MACHINE = "momProvider";
    public static final String MOM_PROVIDER_ROLE = "momProviderRole";
    public static final String COLL_MACHINE = "collEm";
    public static final String COLL_ROLE = "collRole";

    public static final String ADMIN_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    public static final String ADMIN_TOKEN_CMDLINE = "-Dappmap.token=" + ADMIN_TOKEN
        + " -Dappmap.user=admin";

    @Override
    public ITestbed create(ITasResolver arg0) {

        ITestbed testbed = new Testbed(this.getClass().getSimpleName());

        EmRole saMasterRole =
            new EmRole.LinuxBuilder(SA_MASTER_ROLE, arg0)
                .configProperty("introscope.apmserver.teamcenter.master", "true")
                .emLaxNlJavaOption(Arrays.asList(ADMIN_TOKEN_CMDLINE)).nostartWV().build();
        ITestbedMachine saMasterMachine =
            TestBedUtils.createWindowsMachine(SA_MASTER_MACHINE, TEMPLATE_CO66, saMasterRole);
        testbed.addMachine(saMasterMachine);

        EmRole saProviderRole =
            new EmRole.LinuxBuilder(SA_PROVIDER_ROLE, arg0).nostartWV()
                .emLaxNlJavaOption(Arrays.asList(ADMIN_TOKEN_CMDLINE)).build();
        ITestbedMachine saProviderMachine =
            TestBedUtils.createWindowsMachine(SA_PROVIDER_MACHINE, ITestbedMachine.TEMPLATE_CO66,
                saProviderRole);
        testbed.addMachine(saProviderMachine);

        EmRole collRole =
            new EmRole.LinuxBuilder(COLL_ROLE, arg0).emClusterRole(EmRoleEnum.COLLECTOR)
                .emLaxNlJavaOption(Arrays.asList(ADMIN_TOKEN_CMDLINE)).nostartWV().build();
        ITestbedMachine collMachine =
            TestBedUtils
                .createWindowsMachine(COLL_MACHINE, ITestbedMachine.TEMPLATE_CO66, collRole);
        EmRole momProviderRole =
            new EmRole.LinuxBuilder(MOM_PROVIDER_ROLE, arg0).emClusterRole(EmRoleEnum.MANAGER)
                .emCollector(collRole).emLaxNlJavaOption(Arrays.asList(ADMIN_TOKEN_CMDLINE))
                .nostartWV().build();
        ITestbedMachine momProviderMachine =
            TestBedUtils.createWindowsMachine(MOM_PROVIDER_MACHINE, ITestbedMachine.TEMPLATE_CO66,
                momProviderRole);
        testbed.addMachine(momProviderMachine, collMachine);

        final String agcHost = arg0.getHostnameById(SA_MASTER_ROLE);
        final EmRole saProv = (EmRole) testbed.getRoleById(SA_PROVIDER_ROLE);
        final EmRole momProv = (EmRole) testbed.getRoleById(MOM_PROVIDER_ROLE);

        AGCRegisterRole agcRegisterSa =
            new AGCRegisterRole.Builder("agcSaRegister", arg0).agcHostName(agcHost)
                .agcEmWvPort("8081").agcWvPort("8082")
                .hostName(arg0.getHostnameById(SA_PROVIDER_ROLE)).emWvPort("8081")
                .wvHostName(arg0.getHostnameById(SA_PROVIDER_ROLE)).wvPort("8082")
                .startCommand(RunCommandFlow.class, saProv.getEmRunCommandFlowContext())
                .stopCommand(RunCommandFlow.class, saProv.getEmStopCommandFlowContext())
                .build();

        agcRegisterSa.after(new HashSet<IRole>(Arrays.asList(testbed.getMachineById(
            SA_PROVIDER_MACHINE).getRoles())));

        AGCRegisterRole agcRegisterMom =
            new AGCRegisterRole.Builder("agcMomegister", arg0).agcHostName(agcHost)
                .agcEmWvPort("8081").agcWvPort("8082")
                .hostName(arg0.getHostnameById(MOM_PROVIDER_ROLE)).emWvPort("8081")
                .wvHostName(arg0.getHostnameById(MOM_PROVIDER_ROLE)).wvPort("8082")
                .startCommand(RunCommandFlow.class, momProv.getEmRunCommandFlowContext())
                .stopCommand(RunCommandFlow.class, momProv.getEmStopCommandFlowContext())
                .build();
        agcRegisterMom.after(new HashSet<IRole>(Arrays.asList(testbed.getMachineById(
            MOM_PROVIDER_MACHINE).getRoles())));

        saMasterMachine.addRole(agcRegisterSa, agcRegisterMom);

        return testbed;
    }
}
