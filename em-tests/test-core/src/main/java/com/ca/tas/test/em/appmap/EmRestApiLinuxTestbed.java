/*
 * Copyright (c) 2014 CA. All rights reserved.
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

package com.ca.tas.test.em.appmap;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_RH66;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmJettyEnabledRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;


@TestBedDefinition
public class EmRestApiLinuxTestbed implements ITestbedFactory {

    private static final String RESMAN_API_RESOURCE =
        "http://tas-cz-res-man:8080/resman/api/resource/";

    private static final int WS_PORT = 8081;
    private static final int WV_PORT = 8082;

    public static final String ROLE = "role_em";
    public static final String MACHINE = "emMachine";
    private static final String EM_JETTY_ROLE_ID = "jettyEnabled";

    /*
     * (non-Javadoc)
     * 
     * @see com.ca.tas.testbed.ITestbedFactory#create(com.ca.tas.resolver.ITasResolver)
     */
    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed("Introscope/AppMap/EmRestApi");

        EmRole emRole = new EmRole.LinuxBuilder(ROLE, tasResolver).nostartEM().nostartWV().build();

        DeployEMFlowContext deployCtx = emRole.getDeployEmFlowContext();
        String apmRootDir = deployCtx.getInstallDir();

        RunCommandFlowContext emCtx = emRole.getEmRunCommandFlowContext();
        RunCommandFlowContext wvCtx = emRole.getWvRunCommandFlowContext();


        EmJettyEnabledRole jettyEnabled =
            new EmJettyEnabledRole.Builder(EM_JETTY_ROLE_ID, tasResolver).apmRootDir(apmRootDir)
                .runEmContext(emCtx).runWvContext(wvCtx).build();

        jettyEnabled.after(emRole);

        ITestbedMachine emMachine =
            TestBedUtils.createLinuxMachine(MACHINE, TEMPLATE_RH66, emRole, jettyEnabled);
        testbed.addMachine(emMachine);

        return testbed;
    }
}
