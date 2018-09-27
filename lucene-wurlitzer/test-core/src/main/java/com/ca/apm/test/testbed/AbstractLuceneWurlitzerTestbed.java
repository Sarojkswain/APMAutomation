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

package com.ca.apm.test.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_RH66;

import com.ca.apm.test.artifact.AntVersion;
import com.ca.apm.test.role.WurlitzerRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;

/**
 * AbstractLuceneWurlitzerTestbed class.
 *
 * Abstract superclass for LuceneWurlitzer* testbeds:
 * - deploying and starting EM with Public REST API enabled
 * - deploying Wurlitzer and starting one of the app map stress tests
 *
 * @author Jan Zak (zakja01@ca.com)
 */
public abstract class AbstractLuceneWurlitzerTestbed implements ITestbedFactory {

    public static final String EM_MACHINE_ID = "emMachine";
    public static final String EM_ROLE_ID = "emRole";
    private static final String EM_MACHINE_TEMPLATE_ID = TEMPLATE_RH66;

    private static final String EM_CONF_PROPERTY_PUBLIC_API_ENABLED =
        "introscope.public.restapi.enabled";

    public static final String WURLITZER_ROLE_ID = "wurlitzerRole";

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        // create EM role
        EmRole emRole =
            new EmRole.LinuxBuilder(EM_ROLE_ID, tasResolver).configProperty(
                EM_CONF_PROPERTY_PUBLIC_API_ENABLED, "true").build();

        // create Wurlitzer role
        WurlitzerRole wurlitzerRole =
            new WurlitzerRole.LinuxBuilder(WURLITZER_ROLE_ID, tasResolver)
                .antVersionToDeploy(AntVersion.v1_7_1)
                .antScriptPathSegments("scripts", "xml", "appmap-stress", "load-test", "build.xml")
                .antScriptArgs(getEmStressScenario())
                .terminateOnMatch("com.ca.wurlitzer.appmap.SimulatedAgentConnection connect").build();

        // map roles to machine
        ITestbedMachine emMachine =
            TestBedUtils.createLinuxMachine(EM_MACHINE_ID, EM_MACHINE_TEMPLATE_ID, emRole,
                wurlitzerRole);

        return new Testbed(getClass().getSimpleName()).addMachine(emMachine);

    }

    protected abstract String getEmStressScenario();
}
