/*
 * Copyright (c) 2017 CA.  All rights reserved.
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

package com.ca.tas.em.saas;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.test.em.util.EmConnectionInfo;
import com.ca.apm.test.em.util.RoleUtility;
import com.ca.tas.dxc.artifacts.BRTMTestAppArtifact;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.CronEntryRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.docker.DockerRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.tests.annotations.TestBedDynamicField;
import com.google.common.net.HostAndPort;

import org.apache.commons.lang.StringUtils;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * SaasAgentsTestBed
 *
 * @author Zdenek Korcak (korzd01@ca.com)
 */
@TestBedDefinition
public class SaasAgentsTestBed implements ITestbedFactory {
    public static final String BA_MACHINE_ID = "ba";
    public static final String DOCKER_MACHINE_ID = "docker";
    public static final String NB_MACHINE_ID = "nb";
    
    private static final String BA_MACHINE_TEMPLATE_ID = ITestbedMachine.TEMPLATE_CO66;

    public static final String TOMCATROLE = "tomcatrole";
    public static final String BRTM_TEST_APP_CONTEXT = "brtmtestapp";
    public static final int TOMCAT_PORT = 9091;

    private static final String PARAM_MANAGER_URL = "managerUrl";
    private static final String PARAM_MANAGER_CREDENTIAL = "managerCredential";
    private static final String PARAM_TENANT_ID = "tenantId";
    private static final String PARAM_APP_KEY = "appKey";

    @TestBedDynamicField(PARAM_MANAGER_URL)
    private String managerUrl = "https://454055.apm.cloud.ca.com:443";
    @TestBedDynamicField(PARAM_MANAGER_CREDENTIAL)
    private String managerCredential = "42d967a2-5b65-4232-bfff-b225090f0d7c";
    @TestBedDynamicField(PARAM_TENANT_ID)
    private String tenantId = "99A35FAC-855C-499E-8353-CD939A3A6E14";
    @TestBedDynamicField(PARAM_APP_KEY)
    private String appKey = "d5baefb0-5b0d-11e7-a390-8b94e2437e4f";

    public SaasAgentsTestBed() {
    }

    @Override
    public ITestbed create(final ITasResolver tasResolver) {

        ITestbedMachine baMachine =
            TestBedUtils.createLinuxMachine(BA_MACHINE_ID, BA_MACHINE_TEMPLATE_ID);

        EmConnectionInfo managerInfo = new EmConnectionInfo(managerUrl, managerCredential);
        String snippetPath = baMachine.getAutomationBaseDir() + "brtmtestapp.basnippet";

        IRole snippetRole = createSnippetRole(baMachine, snippetPath, tasResolver);

        TomcatRole tomcat =
            new TomcatRole.LinuxBuilder(TOMCATROLE, tasResolver)
                .tomcatVersion(com.ca.tas.artifact.thirdParty.TomcatVersion.v70)
                .webApplication(new BRTMTestAppArtifact(tasResolver).createArtifact(),
                    BRTM_TEST_APP_CONTEXT).tomcatCatalinaPort(TOMCAT_PORT).build();

        Map<String, String> agentAdditionalProps = new HashMap<>();
        managerInfo.fillAgentProperties(agentAdditionalProps);        
        RoleUtility.enableBrowserAgent(agentAdditionalProps, "brtmtestapp", snippetPath);

        AgentRole agentRole =
            new AgentRole.Builder(TOMCATROLE + "-agent", tasResolver)
                .intrumentationLevel(AgentInstrumentationLevel.TYPICAL).webAppServer(tomcat)
                .additionalProperties(agentAdditionalProps).disableWebAppAutoStart()
                .build();

        agentRole.after(snippetRole);
        
        IRole startRole = new UniversalRole.Builder("start_tomcat", tasResolver).syncCommand(tomcat.getStartCmdFlowContext()).build();
        startRole.after(agentRole);
        
        String baseUrl =
            "http://" + tasResolver.getHostnameById(tomcat.getRoleId()) + ":" + TOMCAT_PORT + "/"
                + BRTM_TEST_APP_CONTEXT;

        Artifact testArtifact =
            new DefaultArtifact("com.ca.apm.test", "em-tests-core", "jar-with-dependencies", "jar",
                tasResolver.getDefaultVersion());
        String jarPath = baMachine.getAutomationBaseDir() + "em-tests-core.jar";
        GenericRole downloadRole =
            new GenericRole.Builder("dxc_test_jar", tasResolver).download(testArtifact, jarPath)
                .build();
        downloadRole.after(startRole);
        String cronEntry =
            "* * * * * root java -cp " + jarPath + " com.ca.tas.dxc.test.DXCTest " + baseUrl;
        CronEntryRole cronRole = new CronEntryRole("dxc_cron_chrome", cronEntry);
        cronRole.after(downloadRole);
        baMachine.addRole(tomcat, agentRole, downloadRole, startRole, cronRole);

        IRole sysedgeBa = RoleUtility.addSysedgeRole(baMachine, tasResolver);

        ITestbedMachine nbMachine =
            TestBedUtils.createWindowsMachine(NB_MACHINE_ID, ITestbedMachine.TEMPLATE_W64);
        RoleUtility.addNowhereBankRole(nbMachine, null, managerInfo, null, null, tasResolver);
        IRole sysedgeNb = RoleUtility.addSysedgeRole(nbMachine, tasResolver);
        String snippetPath2 = nbMachine.getAutomationBaseDir() + "brtmtestapp.basnippet";

        IRole snippetRole2 = createSnippetRole(nbMachine, snippetPath2, tasResolver);
        IRole mathAppRole = RoleUtility.addMathAppRoles(nbMachine, managerInfo,
                                    snippetPath2, tasResolver);
        mathAppRole.after(snippetRole2);

        ITestbedMachine dockerMachine = createDockerMachine(tasResolver);
        
        IRole sysedgeDocker = RoleUtility.addSysedgeRole(dockerMachine, tasResolver);

        String mathBaseUrl = "http://"
            + RoleUtility.hostnameToFqdn(tasResolver.getHostnameById(mathAppRole.getRoleId()))
            + ":8080/";

        // httpd and WebServer powerpack
        IRole httpdRole = RoleUtility.addHttpdRole(dockerMachine, "8090", mathBaseUrl, tasResolver);
        httpdRole.after(mathAppRole);
        String mathSecondaryUrl = "http://"
                + RoleUtility.hostnameToFqdn(tasResolver.getHostnameById(httpdRole.getRoleId()))
                + ":8090/";
        
        IRole collectorAgentRole = RoleUtility.addCollectorAgentRole(dockerMachine, managerInfo,
                                            tasResolver.getHostnameById(sysedgeDocker.getRoleId()),
                                            Arrays.asList(sysedgeBa, sysedgeDocker, sysedgeNb),
                                            mathSecondaryUrl, "Apache-MathApp",
                                            true, tasResolver);
        
        IRole mathCronRole = RoleUtility.addMathAppCronRole(dockerMachine, mathSecondaryUrl, tasResolver);
        mathCronRole.after(collectorAgentRole);

        final ITestbed testbed = new Testbed(SaasEmTestBed.class.getSimpleName())
                .addMachine(baMachine, dockerMachine, nbMachine);

        return testbed;
    }

    private IRole createSnippetRole(ITestbedMachine machine, String snippetPath, final ITasResolver tasResolver) {
        Collection<String> data =
            Arrays
                .asList("<script type=\"text/javascript\" id=\"ca_eum_ba\" agent=browser"
                    + " "
                    + "src=\"https://cloud.ca.com/mdo/v1/sdks/browser/BA.js\""
                    + " "
                    + "data-profileUrl=\"https://collector-axa.cloud.ca.com/api/1/urn:ca:tenantId:"
                    + tenantId
                    + "/urn:ca:appId:BrtmTestApp/profile?agent=browser\""
                    + " "
                    + "data-tenantID=\"" + tenantId + "\" data-appID=\"BrtmTestApp\" data-appKey=\"" + appKey + "\"></script>");
        FileModifierFlowContext createFileFlow =
            new FileModifierFlowContext.Builder().create(snippetPath, data)
                .build();
        UniversalRole result = new UniversalRole.Builder(machine.getMachineId() + "_createSnippet", tasResolver)
                                        .runFlow(FileModifierFlow.class, createFileFlow).build();
        machine.addRole(result);
        return result;
    }

    public ITestbedMachine createDockerMachine(final ITasResolver tasResolver) {
        
        int commaIdx = managerUrl.lastIndexOf(":");

        final DockerRole tradeServicesRole = new DockerRole.LinuxBuilder("tradeservices-role")
                .registry(detectArtifactory(tasResolver))
                .image("apm-tradeservice-tomcat8")
                .version(tasResolver.getDefaultVersion())
                .hostname("tradeservice-app")
                .env("EM_HOST_IP", commaIdx < 0 ? "" : managerUrl.substring(0, commaIdx).replaceAll("/", "\\\\/"))
                .env("EM_PORT", managerUrl.substring(commaIdx + 1))
                .env("AGENT_TOKEN", managerCredential)
                .env("BROWSER_AGENT_DISABLED", "true")
                .env("AGENT_NAME", "Docker TradeService")
                .port(7080, 7080)
                .build();

        final DockerRole jMeterRole = new DockerRole.LinuxBuilder("jmeter-role")
                .registry(detectArtifactory(tasResolver))
                .image("jmeter-client")
                .version(tasResolver.getDefaultVersion())
                .link(tradeServicesRole, "tradeservice-app")
                .build();

        final ITestbedMachine emMachine =
            TestBedUtils.createLinuxMachine(DOCKER_MACHINE_ID, ITestbedMachine.TEMPLATE_CO7)
                .addRole(tradeServicesRole, jMeterRole);

        return emMachine;
    }

    private HostAndPort detectArtifactory(final ITasResolver tasResolver) {
        final String regionalArtifactory = tasResolver.getRegionalArtifactory().toString();

        return !StringUtils.isEmpty(regionalArtifactory) && regionalArtifactory.contains("oerth-scx") ? HostAndPort.fromParts("oerth-scx.ca.com", 4443) : HostAndPort.fromParts(
                "artifactory-emea-cz.ca.com", 4443);
    }
}
