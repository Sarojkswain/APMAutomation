/*
 * Copyright (c) 2017 CA. All rights reserved.
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

package com.ca.apm.saas.standalone;

import com.ca.apm.saas.standalone.atc.SeleniumGridMachinesFactory;
import com.ca.apm.systemtest.fld.artifact.FLDHvrAgentLoadExtractArtifact;
import com.ca.apm.systemtest.fld.role.loads.HVRAgentLoadRole;
import com.ca.apm.systemtest.fld.role.loads.WurlitzerBaseRole;
import com.ca.apm.systemtest.fld.role.loads.WurlitzerLoadRole;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.built.docker.ApmAllInDockerImage;
import com.ca.tas.artifact.built.docker.ApmSaasDockerImage;
import com.ca.tas.flow.docker.DockerCreateFlow;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.docker.DockerRole;
import com.ca.tas.testbed.*;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.google.common.net.HostAndPort;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author banda06
 * @author ahmal01
 * @author shadm01
 *         This testbed will deploy SAAS docker image and run FLD loads.
 *         Extracted from FLDStandAloneTestbed & em-tests/SaasTestBed.
 */

@TestBedDefinition
public class FLDDockerSaaSTestbed implements ITestbedFactory,
        FLDStandAloneConstants {

    public static final int EM_PORT = 5001;
    public static final int WV_PORT = 8082;
    public static final Logger log = LoggerFactory.getLogger(FLDDockerSaaSTestbed.class);

    private static final String SYSTEM_XML = "xml/appmap-stress/load-test/system.xml";

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        Testbed testbed = new Testbed("FLDStandAloneTestbed");

        ITestbedMachine emMachine = new TestbedMachine.LinuxBuilder(EM_MACHINE_ID)
                .templateId(EM_TEMPLATE_ID)
                .bitness(Bitness.b64)
                .build();

        Set<String> commands = new HashSet<>(Arrays.asList(
                "introscope.apmserver.teamcenter.master=true",
                "introscope.enterprisemanager.transactiontrace.arrivalbuffer.incubationtime.fast=10",
                "introscope.enterprisemanager.transactiontrace.arrivalbuffer.incubationtime.slow=60",
                "introscope.apmserver.saas.agentTokenAuthentication=false", //To be able to connect with normal agents
                "enable.default.BusinessTransaction=true"
        ));

        boolean apmSaas = true;

        final DockerRole emRole = new DockerRole.LinuxBuilder(EM_ROLE_ID)
                .registry(detectArtifactory(tasResolver))
                .image(apmSaas ? ApmSaasDockerImage.fromVersion("99.99.saastrial-SNAPSHOT")
                        : ApmAllInDockerImage.fromDefaultVersion(tasResolver))
                .commands(new ArrayList<>(commands))
//				.property(DockerCreateFlow.DockerProperty.MEMORY, this.memLimitEm)
                .env("HEAP_XMX_EM", "4096")
                .env("APM_USER_NAME", "hanz")
                .env("APM_USER_PASSWORD", "pass")
                .env("ACC_TOKEN", "0ff49528-2e1f-480a-8403-8b2e2f8f0025")
                .env("ACC_HOST_IP", "tas-cz-n128.ca.com")
                .env("APM_LOG_LEVEL_EM", "INFO")
                .env("APM_LOG_LEVEL_WV", "INFO")
                .env("DEBUG_ENABLED", "TRUE")
                .env("DEBUG_CUSTOM", "-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:/home/1010/apm/logs")
                .port(5001, 5001)
                .port(8081, 8081)
                .port(WV_PORT, 8080)
                .port(8443, 8443)
                .port(9009, 9009)
                .port(443, 443)
                .volume("/root/logs", "/opt/ca/logs").build();

        emMachine.addRole(emRole);
        testbed.addMachine(emMachine);

        log.info("EM Machine name " + emMachine.getHostname());

        testbed.addProperty("test.applicationBaseURL",
                "http://" + tasResolver.getHostnameById(emRole.getRoleId()) + ":" + WV_PORT + "/ApmServer");


        // Selenium Grid setup.
        SeleniumGridMachinesFactory seleniumGridMachinesFactory = new SeleniumGridMachinesFactory();
        Collection<ITestbedMachine> seleniumGridMachines =
                seleniumGridMachinesFactory.createMachines(tasResolver);

        testbed.addMachines(seleniumGridMachines);

        // register remote Selenium Grid
        String hubHostName = tasResolver.getHostnameById(SeleniumGridMachinesFactory.HUB_ROLE_ID);
        testbed.addProperty("selenium.webdriverURL", "http://" + hubHostName + ":4444/wd/hub");
        testbed.addProperty("driverPath", DRIVERS_PATH);

        // Load machine
        ITestbedMachine loadMachine = new TestbedMachine.Builder(
                LOAD_MACHINE1_ID).templateId(LOADMACHINE_TEMPLATE_ID).build();

        String emHost = tasResolver.getHostnameById(EM_ROLE_ID);
        // HVR Load
        FLDHvrAgentLoadExtractArtifact artifactFactory = new FLDHvrAgentLoadExtractArtifact(
                tasResolver);
        ITasArtifact artifact = artifactFactory.createArtifact("10.3");
        HVRAgentLoadRole hvrLoadRole = new HVRAgentLoadRole.Builder(
                HVR_ROLE_ID, tasResolver)
                .emHost(emHost).emPort(Integer.toString(EM_PORT))
                .cloneagents(26).cloneconnections(8).agentHost("HVRAgent")
                .secondspertrace(1).addMetricsArtifact(artifact.getArtifact())
                .build();

        // Wurlitzer Load
        WurlitzerBaseRole wurlitzerBaseRole = new WurlitzerBaseRole.Builder(
                "wurlitzer_base", tasResolver).deployDir("wurlitzerBase")
                .build();

        loadMachine.addRole(wurlitzerBaseRole);

        String xml = "3Complex-200agents-2apps-25frontends-100EJBsession";


        WurlitzerLoadRole wurlitzerLoadrole = new WurlitzerLoadRole.Builder(WURLITZER_ROLE_ID, tasResolver)
                .buildFileLocation(SYSTEM_XML).target(xml)
                .logFile(xml + ".log").wurlitzerBaseRole(wurlitzerBaseRole)
                .overrideEM(HostAndPort.fromParts(emHost, EM_PORT))
                .build();

        JMeterRole at_Jmeter = new JMeterRole.Builder(JMETER_LOAD8, tasResolver)
                .installJmeter(true).jmxFile("AT.JMX")
                .targetHost(emHost).build();

        JMeterRole ttviewer_Jmeter = new JMeterRole.Builder(JMETER_LOAD9,
                tasResolver).jmxFile("TT_Viewer.JMX")
                .targetHost(emHost).build();

        // CLW Load
        CLWWorkStationLoadRole clwRole = new CLWWorkStationLoadRole.Builder(
                CLW_ROLE_ID, tasResolver).emHost(emHost).agentName("tas.*")
                .build();

        loadMachine.addRole(clwRole, wurlitzerLoadrole, hvrLoadRole, at_Jmeter, ttviewer_Jmeter);

        testbed.addMachine(loadMachine);

        List<FldTestbedProvider> testbedProviders = new ArrayList<>();
        testbedProviders.add(new FldLoadTomcatProvider());
//		testbedProviders.add(new FLDWebSphereLoadProvider());
        testbedProviders.add(new FLDJbossLoadProvider());
        testbedProviders.add(new FLDWebLogicCrossClusterProvider());

        for (FldTestbedProvider provider : testbedProviders) {
            Collection<ITestbedMachine> machines = provider.initMachines();
            if (machines != null) {
                testbed.addMachines(machines);
            }
        }
        // and initialize the roles
        for (FldTestbedProvider provider : testbedProviders) {
            provider.initTestbed(testbed, tasResolver);
        }

        return testbed;
    }

    private HostAndPort detectArtifactory(final ITasResolver tasResolver) {
        final String regionalArtifactory = tasResolver.getRegionalArtifactory().toString();

        return !StringUtils.isEmpty(regionalArtifactory) && regionalArtifactory.contains("oerth-scx") ? HostAndPort.fromParts("oerth-scx.ca.com", 4443) : HostAndPort.fromParts(
                "artifactory-emea-cz.ca.com", 4443);
    }
}
