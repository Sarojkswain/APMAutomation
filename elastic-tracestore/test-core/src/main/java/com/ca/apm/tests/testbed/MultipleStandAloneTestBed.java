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

import com.ca.apm.automation.action.flow.utility.FileCreatorFlow;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlowContext;
import com.ca.apm.systemtest.fld.artifact.FLDHvrAgentLoadExtractArtifact;
import com.ca.apm.systemtest.fld.role.CLWWorkStationLoadRole;
import com.ca.apm.systemtest.fld.role.loads.HVRAgentLoadRole;
import com.ca.apm.systemtest.fld.role.loads.WurlitzerBaseRole;
import com.ca.apm.systemtest.fld.role.loads.WurlitzerLoadRole;
import com.ca.apm.tests.role.ClientDeployRole;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.utility.UtilityRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class MultipleStandAloneTestBed implements ITestbedFactory {

    public static final int NUM_OF_SA = 5;

    private static final String SYSTEM_XML = "xml/appmap-stress/load-test/system.xml";
    public static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    private static final Collection<String> LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true", "-Dmail.mime.charset=UTF-8",
        "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseConcMarkSweepGC",
        "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
        "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError", "-Xms4096m",
        "-Xmx4096m", "-verbose:gc", "-Dappmap.user=admin", "-Dappmap.token=" + ADMIN_AUX_TOKEN);

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        ITestbed bed = new Testbed("Mutiple SA Environment");

        // String yml = "/jarvis_data/docker-compose.yml";
        // UtilityRole<?> fileCreationRole =
        // UtilityRole.flow("jarvisDockerComposeCopyRole", FileCreatorFlow.class,
        // new FileCreatorFlowContext.Builder().fromResource("/docker/docker-compose.yml")
        // .destinationPath(yml).build());
        // UtilityRole<?> executionRole =
        // UtilityRole.commandFlow("startJarvisDockerRole", new RunCommandFlowContext.Builder(
        // "docker-compose").args(Arrays.asList("-f", yml, "up", "-d")).build());
        // fileCreationRole.before(executionRole);
        // ITestbedMachine jarvisMachine =
        // new TestbedMachine.LinuxBuilder("jarvisMachine").templateId("co7_500g")
        // .bitness(Bitness.b64).build();
        // jarvisMachine.addRole(executionRole, fileCreationRole);
        // bed.addMachine(jarvisMachine);

        for (int i = 0; i < NUM_OF_SA; i++) {

            EmRole emRole =
                new EmRole.LinuxBuilder("emRole" + i, tasResolver)
                    .silentInstallChosenFeatures(
                        Arrays.asList("Enterprise Manager", "Database", "WebView"))
                    .configProperty("com.ca.apm.ttstore", "es")
                    .configProperty("ca.apm.ttstore.elastic.url","http://130.200.67.237:9200")
//                    .configProperty("ca.apm.ttstore.jarvis.ingestion.url",
//                        "http://sc97a:8081/ingestion")
//                    .configProperty("ca.apm.ttstore.jarvis.es.url", "http://sc97a:9200")
//                    .configProperty("ca.apm.ttstore.jarvis.onboarding.url",
//                        "http://sc97a:8080/onboarding")
//                    .configProperty("introscope.tenantId", "load-tenant-" + i)
                    .configProperty("introscope.apmserver.teamcenter.saas", "true")
                    .configProperty("introscope.enterprisemanager.tess.enabled", "false")
                    .emLaxNlClearJavaOption(LAXNL_JAVA_OPTION).build();

            ITestbedMachine emMachine =
                new TestbedMachine.LinuxBuilder("emMachine" + i).templateId("co66")
                    .bitness(Bitness.b64).build();
            emMachine.addRole(emRole);

            ITestbedMachine loadMachine =
                new TestbedMachine.LinuxBuilder("loadMachine" + i).templateId("w64")
                    .bitness(Bitness.b64).build();

            // HVR Load
            FLDHvrAgentLoadExtractArtifact artifactFactory =
                new FLDHvrAgentLoadExtractArtifact(tasResolver);
            ITasArtifact artifact = artifactFactory.createArtifact("10.3");
            HVRAgentLoadRole hvrLoadRole =
                new HVRAgentLoadRole.Builder("hvrRole" + i, tasResolver)
                    .emHost(tasResolver.getHostnameById("emRole" + i)).emPort("5001")
                    .cloneagents(10).cloneconnections(25).agentHost("HVRAgent").secondspertrace(1)
                    .addMetricsArtifact(artifact.getArtifact()).build();
            loadMachine.addRole(hvrLoadRole);

            // Wurlitzer Load
            WurlitzerBaseRole wurlitzerBaseRole =
                new WurlitzerBaseRole.Builder("wurlitzerBaseRole" + i, tasResolver).deployDir(
                    "wurlitzerBase").build();
            loadMachine.addRole(wurlitzerBaseRole);
            String xml = "3Complex-200agents-2apps-25frontends-100EJBsession";
            WurlitzerLoadRole wurlitzerLoadrole =
                new WurlitzerLoadRole.Builder("wurlitzerRole" + i, tasResolver).emRole(emRole)
                    .buildFileLocation(SYSTEM_XML).target(xml).logFile(xml + ".log")
                    .wurlitzerBaseRole(wurlitzerBaseRole).build();
            loadMachine.addRole(wurlitzerLoadrole);

            // CLW Load
            CLWWorkStationLoadRole clwRole =
                new CLWWorkStationLoadRole.Builder("clwRole" + i, tasResolver)
                    .emHost(tasResolver.getHostnameById("emRole" + i)).agentName("HVRAgent.*")
                    .agentName(".*ErrorStallAgent.*").build();
            loadMachine.addRole(clwRole);

            // client deploy role
            ClientDeployRole clientDeployRole =
                new ClientDeployRole.Builder("clientDeployRole" + i, tasResolver).emHost(
                    tasResolver.getHostnameById("emRole" + i)).build();
            loadMachine.addRole(clientDeployRole);

            // deploy jmeter load script role
            try {
                UtilityRole<?> copyRestTraceLoadRole =
                    UtilityRole
                        .flow(
                            "copyRestTraceLoadJmx" + i,
                            FileCreatorFlow.class,
                            new FileCreatorFlowContext.Builder()
                                .fromResource("/jmeter/TT_Viewer.JMX")
                                .destinationPath("C:/automation/deployed/jmeter/TT_Viewer.JMX")
                                .build());
                clientDeployRole.before(copyRestTraceLoadRole);
                loadMachine.addRole(copyRestTraceLoadRole);
            } catch (Exception e) {}

            bed.addMachine(emMachine, loadMachine);
        }

        return bed;
    }
}
