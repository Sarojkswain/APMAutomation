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

package com.ca.apm.tests.testbed;

import java.util.Arrays;
import java.util.Collection;

import com.ca.apm.systemtest.fld.role.CLWWorkStationLoadRole;
import com.ca.apm.tests.role.ClientDeployRole;
import com.ca.apm.tests.role.ElasticSearchRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class SimpleDevTestBedWithElastic implements ITestbedFactory {

    public static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    private static final Collection<String> LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true", "-Dmail.mime.charset=UTF-8",
        "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseConcMarkSweepGC",
        "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
        "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError", "-Xms4096m",
        "-Xmx4096m", "-verbose:gc", "-Dappmap.user=admin", "-Dappmap.token=" + ADMIN_AUX_TOKEN);

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        ITestbed bed = new Testbed(this.getClass().getSimpleName());

        ITestbedMachine esMachine =
            new TestbedMachine.LinuxBuilder("esMachine").templateId("co7").bitness(Bitness.b64)
                .build();
        ElasticSearchRole esRole = new ElasticSearchRole.Builder("esRole", tasResolver).build();
        esMachine.addRole(esRole);

        ITestbedMachine emMachine =
            new TestbedMachine.LinuxBuilder("emMachine").templateId("co66").bitness(Bitness.b64)
                .build();
        String esHost = tasResolver.getHostnameById("esRole");
        EmRole emRole =
            new EmRole.LinuxBuilder("emRole", tasResolver)
                .silentInstallChosenFeatures(
                    Arrays.asList("Enterprise Manager", "Database", "WebView"))
                .configProperty("com.ca.apm.ttstore", "es")
                .configProperty("ca.apm.ttstore.elastic.url", "http://" + esHost + ":9200")
                .emLaxNlClearJavaOption(LAXNL_JAVA_OPTION).build();
        emMachine.addRole(emRole);

        ITestbedMachine loadMachine =
            new TestbedMachine.LinuxBuilder("loadMachine").templateId("w64").bitness(Bitness.b64)
                .build();
        ClientDeployRole clientDeployRole =
            new ClientDeployRole.Builder("clientDeployRole", tasResolver).emHost(
                tasResolver.getHostnameById("emRole")).build();
        CLWWorkStationLoadRole clwRole =
            new CLWWorkStationLoadRole.Builder("clwRole", tasResolver)
                .emHost(tasResolver.getHostnameById("emRole")).agentName(".*ErrorStallAgent.*")
                .build();
        loadMachine.addRole(clientDeployRole, clwRole);

        bed.addMachine(esMachine, loadMachine, emMachine);
        return bed;

    }
}
