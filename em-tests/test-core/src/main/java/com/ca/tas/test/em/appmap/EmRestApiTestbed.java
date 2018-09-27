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

import static java.lang.String.format;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.testbed.atc.SeleniumGridMachinesFactory;
import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmJettyEnabledRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;


/**
 * @author surma04
 *
 */

@TestBedDefinition
public class EmRestApiTestbed implements ITestbedFactory {

    public static final String ROLE = "role_em";
    public static final String MACHINE = "emMachine";

    public static final String EM_JETTY_ROLE_ID = "jettyEnabled";
    private static final Logger LOGGER = LoggerFactory.getLogger(EmRestApiTestbed.class);

    /*
     * (non-Javadoc)
     * 
     * @see com.ca.tas.testbed.ITestbedFactory#create(com.ca.tas.resolver.ITasResolver)
     */
    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = createTestbed("Introscope/AppMap/EmRestApi", tasResolver, false);

        return testbed;
    }

    protected ITestbed createTestbed(String name, ITasResolver tasResolver, boolean isAgc) {
        ITestbed testbed = new Testbed(name);

        TestbedMachine emMachine =
            new TestbedMachine.Builder(MACHINE).platform(Platform.WINDOWS).templateId("w64")
                .bitness(Bitness.b64).build();
        EmRole.Builder emRoleBuilder =
            new EmRole.Builder(ROLE, tasResolver)
                .introscopePlatform(ArtifactPlatform.WINDOWS_AMD_64).nostartEM().nostartWV();

        if (isAgc) {
            emRoleBuilder.configProperty("introscope.apmserver.teamcenter.master", "true");
        }

        EmRole emRole = emRoleBuilder.build();

        DeployEMFlowContext deployCtx = emRole.getDeployEmFlowContext();
        String apmRootDir = deployCtx.getInstallDir();

        RunCommandFlowContext emCtx = emRole.getEmRunCommandFlowContext();
        RunCommandFlowContext wvCtx = emRole.getWvRunCommandFlowContext();
        EmJettyEnabledRole jettyEnabled =
            new EmJettyEnabledRole.Builder(EM_JETTY_ROLE_ID, tasResolver).apmRootDir(apmRootDir)
                .runEmContext(emCtx).runWvContext(wvCtx).build();

        jettyEnabled.after(emRole);
        emMachine.addRole(emRole);
        emMachine.addRole(jettyEnabled);
        emMachine.addRemoteResource(RemoteResource.createFromRegExp(".*screenshots.*", RemoteResource.TEMP_FOLDER));
        
        // Remote Selenium Grid
        SeleniumGridMachinesFactory seleniumGridMachinesFactory = new SeleniumGridMachinesFactory();
        Collection<ITestbedMachine> seleniumGridMachines = seleniumGridMachinesFactory.createMachines(tasResolver);

        testbed.addMachine(emMachine);
        testbed.addMachines(seleniumGridMachines);

        final String emHostname = tasResolver.getHostnameById(emRole.getRoleId());
        testbed.addProperty("test.emHostname", emHostname);
        testbed.addProperty("test.applicationBaseURL", format("http://%s:8082", emHostname));
        
        String hostname = null;
        try {
            hostname =
                InetAddress.getByName(tasResolver.getHostnameById(ROLE)).getCanonicalHostName();
            LOGGER.debug("hostname in testbed: " + hostname);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Unable to get hostname: ", e);
        }
        testbed.addProperty("test.applicationBaseURL", format("http://%s:8082", hostname));
        return testbed;
    }

}
