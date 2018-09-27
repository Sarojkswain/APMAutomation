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
package com.ca.apm.test.em.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableSet;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.controller.DeployControllerFlowContext;
import com.ca.apm.automation.action.flow.testapp.NowhereBankVersion;
import com.ca.apm.automation.action.flow.utility.ConfigureFlow;
import com.ca.apm.automation.action.flow.utility.ConfigureFlowContext;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlow;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.TimeSyncFlow;
import com.ca.apm.automation.action.flow.utility.TimeSyncFlowContext;
import com.ca.apm.automation.action.flow.utility.Win32RegistryFlow;
import com.ca.apm.automation.action.flow.utility.Win32RegistryFlowContext;
import com.ca.apm.automation.action.flow.utility.XmlModifierFlow;
import com.ca.apm.automation.action.flow.utility.XmlModifierFlowContext;
import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform;
import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.IThirdPartyArtifact;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.dxc.artifacts.BRTMTestAppArtifact;
import com.ca.tas.dxc.role.BARole;
import com.ca.tas.dxc.role.DXCRole;
import com.ca.tas.dxc.role.KafkaZookeeperRole;
import com.ca.tas.dxc.role.LogstashRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.ControllerRole;
import com.ca.tas.role.CronEntryRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.HammondRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.ManagementModuleRole;
import com.ca.tas.role.linux.YumInstallPackageRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.AgentCapable;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;

import static com.ca.apm.automation.action.flow.utility.Win32RegistryFlowContext.RegHive.LOCAL_MACHINE;
import static com.ca.apm.automation.action.flow.utility.Win32RegistryFlowContext.RegValueType.DWORD;

public class RoleUtility {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoleUtility.class);

    public static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    public static final String ENV_NB_START_REQUESTS = "nbStartRequests";
    public static final String ENV_START_COLLECTOR_AGENT = "startCollectorAgent";
    public static final String ENV_DOCKER_HOSTNAME = "dockerHostname";
    public static final String ENV_DOCKER_CONF_PATH = "dockerConfigPath";
    
    private static final String BANKING_CONSOLE_CMD = "Banking-Console";
    private static final String BANKING_CONSOLE_FINAL_TEXT = "Please enter a command";
    private static final String BANKING_CONSOLE_CONTROL_CMD = "Banking-Console-Control";
    private static final String BANKING_CONSOLE_CONTROL_FINAL_TEXT = "Finished";
    private static final String START_REQ_ROLE_ID_SUFFIX = "_start_req";

    private static final String AGENT_GROUP_ID = "com.ca.apm.delivery";
    private static final String AGENT_ARTIFACT_ID = "agent-noinstaller-tomcat-windows";
    
    private static final String MATHAPP_GROUP_ID = "com.ca.apm.test-projects.mathapp";
    private static final String MATHAPP_VERSION = "1.0";
    
    private static final String SYSEDGE_GROUP_ID = "com.ca.SystemEDGE";
    private static final String SYSEDGE_ARTIFACT_ID = "SystemEDGE_Core";
    private static final String SYSEDGE_VERSION = "5.9.0";
    
    private static final String COLLECTOR_AGENT_GROUP_ID = "com.ca.apm.agent.CollectorAgent";
    private static final String COLLECTOR_AGENT_ARTIFACT_ID = "CollectorAgent-dist";
    
    private static final String SNMP_COLLECTOR_GROUP_ID = "com.ca.apm.agent";
    private static final String SNMP_COLLECTOR_ARTIFACT_ID = "SnmpCollector";

    private static final String DOCKER_MONITOR_GROUP_ID = "com.ca.apm.agent.docker.swarm";
    private static final String DOCKER_MONITOR_ARTIFACT_ID = "DockerMonitor";

    private static final String CONTAINER_FLOW_GROUP_ID = "com.ca.apm.agent.docker.swarm";
    private static final String CONTAINER_FLOW_ARTIFACT_ID = "ContainerFlow";

    private static final String WEB_SERVER_MONITOR_GROUP_ID = "com.ca.apm.powerpack.webserver";
    private static final String WEB_SERVER_MONITOR_ARTIFACT_ID = "ppk-webserver-agent-dist";
    
    public static final String WEB_SERVER_MONITOR_ROLE_SUFFIX = "_start_web_server_mon";

    private static final String[] MATHAPP_COMMANDS = {
          "curl %sMathProxy/rest/hello",
          "curl %sJersey-REST-Client/restClient",
          "curl %sJersey-REST-Client/JerseyApacheClientServlet",
          "curl '%sMathProxy/rest/hello/math?operation=add&value1=5&value2=10'",
          "curl '%sMathProxy/rest/hello/math?operation=subtract&value1=5&value2=10'",
          "curl '%sMathProxy/rest/hello/math?operation=multiply&value1=5&value2=10'",
          "curl '%sMathProxy/rest/hello/math?operation=divide&value1=5&value2=10'",
    
          "curl '%sMathProxy/rest/hello/mathcomplex?operation=flush&value1=5&value2=10'",
          "curl '%sMathProxy/rest/hello/mathcomplex?operation=mean&values=5,10,15'",
          "curl '%sMathProxy/rest/hello/mathcomplex?operation=median&values=5,10,15'",
          "curl '%sMathProxy/rest/hello/mathcomplex?operation=mode&values=5,10,15'",
          "curl '%sMathProxy/rest/hello/mathcomplex?operation=python&values=5,10,15'",
          "curl '%sMathProxy/rest/hello/mathcomplex?operation=barcode&values=1,2,3,4,5,6,7,8,9'",
    
          "curl '%sMathProxy/rest/hello/mathdotnet?operation=add&value1=5&value2=10'",
          "curl '%sMathProxy/rest/hello/mathdotnet?operation=multiply&value1=5&value2=10'",
          "curl '%sMathProxy/rest/hello/mathnode?operation=add&value1=5&value2=10'",
    
          "curl %sMathClient/MathClientServlet -X POST -d 'operation=add&value1=5&value2=6'",
          "curl %sMathClient/MathClientServlet -X POST -d 'operation=subtract&value1=5&value2=6'",
          "curl %sMathClient/MathClientServlet -X POST -d 'operation=multiply&value1=5&value2=6'",
          "curl %sMathClient/MathClientServlet -X POST -d 'operation=divide&value1=5&value2=6'",
    
          "curl %sMathClient/MathComplexServlet -X POST -d 'operation=mean&values=1,2,3,4,5,6,7,8,9'",
          "curl %sMathClient/MathComplexServlet -X POST -d 'operation=median&values=1,2,3,4,5,6,7,8,9'",
          "curl %sMathClient/MathComplexServlet -X POST -d 'operation=mode&values=1,2,3,4,5,6,7,8,9'",
          "curl %sMathClient/MathComplexServlet -X POST -d 'operation=python&values=1,2,3,4,5,6,7,8,9'",
          "curl %sMathClient/MathComplexServlet -X POST -d 'operation=barcode&values=1,2,3,4,5,6,7,8,9'",
          "curl %sMathClient/MathComplexServlet -X POST -d 'operation=flush&values=1,2,3,4,5,6,7,8,9'",
      };

    private static final Collection<String> DOMAINS_DATA = Arrays.asList(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
        "<domains xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"domains0.3.xsd\" version=\"0.3\">",
        "    <domain name=\"NowhereBank\">",
        "        <agent mapping=\"(.*)\\|Nowhere\\ Bank\\|(.*)\"/>",
        "        <grant group=\"Admin\" permission=\"full\"/>",
        "    </domain>",
        "    <SuperDomain>",
        "        <agent mapping=\"(.*)\"/>",
        "        <grant group=\"Admin\" permission=\"full\"/>",
        "        <grant user=\"Guest\" permission=\"read\"/>",
        "    </SuperDomain>",
        "</domains>"
    );
    
    // DxC
    public static final String DXC_MACHINE_ID = "dxc";
    public static final String DXCROLE = "dxcrole";
    public static final String KAFKAROLE = "kafka";
    public static final String LOGSTASHROLE = "logstash";
    public static final String TOMCATROLE = "tomcatrole";
    public static final String BRTM_TEST_APP_CONTEXT = "brtmtestapp";
    public static final String BRTM_TEST_APP_VERSION = "badev-SNAPSHOT";
    public static final int TOMCAT_PORT = 9091;

    public static Collection<String> getDevEmLaxnlJavaOption(int port) {
        return  Arrays.asList(
            "-Dappmap.token=" + ADMIN_AUX_TOKEN, "-Dappmap.user=admin",
            "-Dcom.wily.assert=false", "-XX:+HeapDumpOnOutOfMemoryError", "-verbosegc", "-Xdebug",
            "-Xrunjdwp:server=y,transport=dt_socket,address=" + Integer.toString(port) + ",suspend=n");
    }

    public static IRole fixRegistryForJenkinsRole(ITasResolver tasResolver, ITestbedMachine machine,
        IRole... beforeRoles) {
        Win32RegistryFlowContext context = new Win32RegistryFlowContext.Builder()
            .setValue(LOCAL_MACHINE, "SYSTEM\\CurrentControlSet\\Services\\LanmanServer"
                    + "\\Parameters\\SMB1",
                DWORD, 1)
            .build();

        UniversalRole role = new UniversalRole.Builder(machine.getMachineId() + "_SMBv1Enable",
            tasResolver)
            .runFlow(Win32RegistryFlow.class, context)
            .build();
        machine.addRole(role);

        if (beforeRoles != null) {
            for (IRole r : beforeRoles) {
                role.before(r);
            }
        }

        return role;
    }

    public static void gatherJenkinsLogs(ControllerRole controllerRole,
        ITestbedMachine controller) {
        DeployControllerFlowContext controllerFlowContext
            = controllerRole.getControllerFlowContext();
        controller.addRemoteResource(
            new RemoteResource.RemoteResourceBuilder(
                controllerFlowContext.getTargetDir()
                    + '/' + controllerFlowContext.getJenkinsDir()
                    + "/jobs")
                .name("jenkins-logs")
                .regExp(".+log$|.+\\.xml$")
                .build());
    }

    public enum NowhereBankLoad {
        SlowWebService("startSlowWebService"),
        DBFailing("startDBFailing"),
        Requests("startRequests"),
        NewSlowWebService("startNewSlowWebService"),
        WebServiceDegredation("startWebServiceDegredation");

        private String command;

        NowhereBankLoad(String command) {
            this.command = command;
        }

        public String getCommand() {
            return command;
        }
    }


    public static IRole addNowhereBankRole(ITestbedMachine machine, EmRole emRole,
        String secondHost, final ITasResolver tasResolver) {
        return addNowhereBankRole(machine, emRole, secondHost, null, tasResolver);
    }


    public static IRole addNowhereBankRole(ITestbedMachine machine, EmRole emRole,
        String secondHost, NowhereBankLoad[] nbLoads, final ITasResolver tasResolver) {
        return addNowhereBankRole(machine, emRole, new EmConnectionInfo(emRole, tasResolver),
                                  secondHost, null, tasResolver);
    }
        
    public static IRole addNowhereBankRole(ITestbedMachine machine, EmRole emRole, EmConnectionInfo emInfo,
        String secondHost, NowhereBankLoad[] nbLoads, final ITasResolver tasResolver) {
        String nbPath = machine.getAutomationBaseDir() + "nowherebank";
        String agentPath = machine.getAutomationBaseDir() + "agent";
        final String nowhereBankRoleName = machine.getMachineId() + "_nowherebank";
        final String nowhereBankHost = tasResolver.getHostnameById(nowhereBankRoleName);
        final GenericRole nowhereBankRole =
            new GenericRole.Builder(nowhereBankRoleName, tasResolver)
                .unpack(NowhereBankVersion.v13.getArtifact(), nbPath)
                .configuration(nbPath + "/NowhereBank.properties",
                    new LinkedHashMap<String, String>() {
                        private static final long serialVersionUID = 1L;
                    {
                        put("nowherebank.host", nowhereBankHost);
                        put("nowherebank.command.host", nowhereBankHost);
                    }})
                .build();
        machine.addRole(nowhereBankRole);
        if (emRole != null) {
            nowhereBankRole.after(emRole);
        }

        DefaultArtifact agentArtifact = new DefaultArtifact(AGENT_GROUP_ID, AGENT_ARTIFACT_ID,
                                            TasExtension.ZIP.getValue(), tasResolver.getDefaultVersion());
        GenericRole agentRole = new GenericRole.Builder(machine.getMachineId() + "_agent", tasResolver)
                                            .unpack(agentArtifact, agentPath).build();
        machine.addRole(agentRole);
        agentRole.after(nowhereBankRole);
        
        FileCreatorFlowContext copyContext = new FileCreatorFlowContext.Builder()
                                    .fromFile(agentPath + "/wily/Agent.jar")
                                    .destinationPath(nbPath + "/wily/Agent.jar").build();
        UniversalRole copyRole = new UniversalRole.Builder(machine.getMachineId() + "_agent_copy", tasResolver)
                                    .runFlow(FileCreatorFlow.class, copyContext).build();
        machine.addRole(copyRole);
        copyRole.after(agentRole);

        String configurationFile = nbPath + "/wily/core/config/IntroscopeAgent.profile";
        Map<String, String> replaceAgentHostConfig = new HashMap<String, String>();
        emInfo.fillAgentProperties(replaceAgentHostConfig);
        replaceAgentHostConfig.put("introscope.agent.deep.entrypoint.enabled", "false");
        replaceAgentHostConfig.put("introscope.agent.transactiontracer.sampling.perinterval.count", "1");
        replaceAgentHostConfig.put("introscope.agent.transactiontracer.sampling.interval.seconds", "120");
        ConfigureFlowContext configContext = new ConfigureFlowContext.Builder()
                .configurationMap(configurationFile, replaceAgentHostConfig)
                .configurationDeleteMap(configurationFile,
                    ImmutableSet.of("agentManager.url.2", "agentManager.url.3", "agentManager.url.4"))
                .build();
        UniversalRole configRole = new UniversalRole.Builder(machine.getMachineId() + "_config", tasResolver)
                .runFlow(ConfigureFlow.class, configContext)
                .build();
        machine.addRole(configRole);
        configRole.after(copyRole);

        Map<String, String> environmentProps = new HashMap<String, String>();
        environmentProps.put("_JAVA_OPTIONS", "-Xms64m");
        RunCommandFlowContext consoleContext = new RunCommandFlowContext.Builder(BANKING_CONSOLE_CMD)
                                                    .terminateOnMatch(BANKING_CONSOLE_FINAL_TEXT)
                                                    .workDir(nbPath)
                                                    .environment(environmentProps)
                                                    .build();
        IRole consoleRole = new UniversalRole.Builder(machine.getMachineId() + "_console", tasResolver).syncCommand(consoleContext).build();
        machine.addRole(consoleRole);
        consoleRole.after(configRole);

        IRole lastStartRole;
        if (secondHost != null) {
            String nbPath2 = machine.getAutomationBaseDir() + "nowherebank2";
            GenericRole nowhereBankRole2 =
                new GenericRole.Builder(machine.getMachineId() + "_nowherebank2", tasResolver)
                    .unpack(NowhereBankVersion.v13.getArtifact(), nbPath2).build();
            machine.addRole(nowhereBankRole2);
            nowhereBankRole2.after(consoleRole);
            
            /*FileCreatorFlowContext copyContext2 = new FileCreatorFlowContext.Builder()
                                                    .fromFile(agentPath + "/wily/Agent.jar")
                                                    .destinationPath(nbPath2 + "/wily/Agent.jar").build();
            UniversalRole copyRole2 = new UniversalRole.Builder(machine.getMachineId() + "_agent_copy2", tasResolver)
                                                    .runFlow(FileCreatorFlow.class, copyContext2).build();
            machine.addRole(copyRole2);
            copyRole2.after(nowhereBankRole2);*/

            Map<String, String> replaceNnPortConfig = new HashMap<String, String>();
            replaceNnPortConfig.put("nowherebank.command.port", "10301");
            replaceNnPortConfig.put("nowherebank.host", nowhereBankHost);
            replaceNnPortConfig.put("nowherebank.command.host", nowhereBankHost);
            UniversalRole configRole2 =
                new UniversalRole.Builder(machine.getMachineId() + "_config2", tasResolver)
                    .configuration(nbPath2 + "/NowhereBank.properties", replaceNnPortConfig)
                    .build();
            machine.addRole(configRole2);
            configRole2.after(nowhereBankRole2/*copyRole2*/);

            // TODO: Set nowherebank.host here as is done for the first Nowhere bank host.

            String configurationFile2 = nbPath2 + "/wily/core/config/IntroscopeAgent.profile";
            Map<String, String> replaceAgentHostConfig2 = new HashMap<String, String>();
            replaceAgentHostConfig2.put("agentManager.url.1", secondHost + ":5001");
            replaceAgentHostConfig2.put("introscope.agent.deep.entrypoint.enabled", "false");
            replaceAgentHostConfig2.put("introscope.agent.transactiontracer.sampling.perinterval.count", "1");
            replaceAgentHostConfig2.put("introscope.agent.transactiontracer.sampling.interval.seconds", "120");
            ConfigureFlowContext configFlow3 = new ConfigureFlowContext.Builder()
                    .configurationMap(configurationFile2, replaceAgentHostConfig2)
                    .configurationDeleteMap(configurationFile2,
                        ImmutableSet.of("agentManager.url.2", "agentManager.url.3", "agentManager.url.4"))
                    .build();
            UniversalRole configRole3 = new UniversalRole.Builder(machine.getMachineId() + "_config3", tasResolver)
                    .runFlow(ConfigureFlow.class, configFlow3)
                    .build();
            machine.addRole(configRole3);
            configRole3.after(configRole2);
            
            RunCommandFlowContext consoleContext2 = new RunCommandFlowContext.Builder(BANKING_CONSOLE_CMD)
                                                            .terminateOnMatch("Please enter a command")
                                                            .workDir(nbPath2)
                                                            .build();
            IRole consoleRole2 = new UniversalRole.Builder(machine.getMachineId() + "_console2", tasResolver).syncCommand(consoleContext2).build();
            machine.addRole(consoleRole2);
            consoleRole2.after(configRole3);

            IRole startMessagingRole = createStartNbPartRole(machine, "_start_messaging", nbPath,
                                                                "startMessaging", consoleRole2, null, tasResolver);
            IRole startPortalRole = createStartNbPartRole(machine, "_start_portal", nbPath,
                                                            "startPortal", startMessagingRole, null, tasResolver);
            IRole startMediatorRole = createStartNbPartRole(machine, "_start_mediator", nbPath2,
                                                                "startMediator", startPortalRole, null, tasResolver);
            lastStartRole = createStartNbPartRole(machine, "_start_engine", nbPath,
                                                    "startEngine", startMediatorRole, null, tasResolver);
        } else {
            lastStartRole = createStartNbPartRole(machine, "_start_all", nbPath,
                                                    "startAll", consoleRole, null, tasResolver);
        }

        lastStartRole = createStartNbPartRole(machine, START_REQ_ROLE_ID_SUFFIX, nbPath,
                                        "startRequests", lastStartRole, ENV_NB_START_REQUESTS, tasResolver);

        if (nbLoads != null && nbLoads.length > 0) {
            for (NowhereBankLoad load : nbLoads) {
                lastStartRole = createStartNbPartRole(machine, "_" + load.getCommand(),
                    nbPath, load.getCommand(), lastStartRole, null, tasResolver);
            }
        }

        return lastStartRole;
    }

    public static IRole addHttpdRole(ITestbedMachine machine, String port, String url,
                                        ITasResolver tasResolver) {
        final String CONFIG_FILE ="/etc/httpd/conf/httpd.conf";
        
        IRole httpdRole = new YumInstallPackageRole.Builder(machine.getMachineId() + "_install_httpd")
                .addPackage("httpd").build();
        
        Map<String, String> replacePairs = new HashMap<>();
        replacePairs.put("Listen 80", "Listen " + port);
        FileModifierFlowContext modifyConfigContex = new FileModifierFlowContext.Builder()
            .replace(CONFIG_FILE, replacePairs)
            .append(CONFIG_FILE, Arrays.asList("",
                "LoadModule status_module modules/mod_status.so",
                "",
                "<Location /server-status>",
                "SetHandler server-status",
                "Allow from all",
                "</Location>",
                "",
                "ExtendedStatus on",
                "",
                "<VirtualHost *:" + port +">",
                "ProxyPass /server-status !",
                "ProxyAddHeaders on",
                "ProxyPass     /     " + url,
                "</VirtualHost>"))
            .build();
        RunCommandFlowContext selinuxContext = new RunCommandFlowContext.Builder("setenforce")
            .doNotPrependWorkingDirectory()
            .args(Arrays.asList("0"))
            .build();
        RunCommandFlowContext startContext = new RunCommandFlowContext.Builder("/sbin/service")
            .args(Arrays.asList("httpd", "start"))
            .build();
        IRole runHttpdRole = new UniversalRole.Builder(machine.getMachineId() + "_run_httpd", tasResolver)
            .runFlow(FileModifierFlow.class, modifyConfigContex)
            .syncCommand(selinuxContext)
            .syncCommand(startContext)
            .build();

        runHttpdRole.after(httpdRole);
        machine.addRole(httpdRole, runHttpdRole);
        
        return runHttpdRole;
    }

    public static IRole addMathAppRoles(ITestbedMachine machine, EmConnectionInfo emInfo,
        String snippetPath, ITasResolver tasResolver) {

        DefaultArtifact mathClientArtifact = new DefaultArtifact(MATHAPP_GROUP_ID, "MathClient",
            TasExtension.WAR.getValue(), MATHAPP_VERSION);
        DefaultArtifact mathComplexBackendArtifact = new DefaultArtifact(MATHAPP_GROUP_ID, "MathComplexBackend",
            TasExtension.WAR.getValue(), MATHAPP_VERSION);
        DefaultArtifact mathProxyArtifact = new DefaultArtifact(MATHAPP_GROUP_ID, "MathProxy",
            TasExtension.WAR.getValue(), MATHAPP_VERSION);
        DefaultArtifact mathSimpleBackendArtifact = new DefaultArtifact(MATHAPP_GROUP_ID, "MathSimpleBackend",
            TasExtension.WAR.getValue(), MATHAPP_VERSION);
        ITasArtifact brtmTestAppArtifact = new BRTMTestAppArtifact(tasResolver).createArtifact(BRTM_TEST_APP_VERSION);

        TomcatRole tomcatRole =
            new TomcatRole.Builder("tomcat-mathapp", tasResolver).tomcatVersion(TomcatVersion.v60)
                .tomcatCatalinaPort(8080)
                .tomcatServerPort(8006)
                .jdkHomeDir("C:/Program Files/Java/jdk1.6.0_45")
                .installDir("C:/sw2/apache-tomcat-6.0.36")
                .webApplication(mathClientArtifact, "MathClient")
                .webApplication(mathComplexBackendArtifact, "MathComplexBackend")
                .webApplication(mathProxyArtifact, "MathProxy")
                .webApplication(mathSimpleBackendArtifact, "MathSimpleBackend")
                .webApplication(brtmTestAppArtifact, BRTM_TEST_APP_CONTEXT)
                .webApplication(PipeOrganUtility.getPipeOrganArtifact(tasResolver),
                        PipeOrganUtility.PIPEORGAN_CONTEXT)
                .build();
        machine.addRole(tomcatRole);

        Map<String, String> agentAdditionalProps = new HashMap<>();
        if (snippetPath != null) {
            enableBrowserAgent(agentAdditionalProps, "brtmtestapp", snippetPath);
        }
        emInfo.fillAgentProperties(agentAdditionalProps);

        AgentRole agentRole =
            new AgentRole.Builder("agent-tomcat-mathapp", tasResolver).webAppServer(tomcatRole)
                .platform(ArtifactPlatform.WINDOWS)//.emRole(emRole)
                .intrumentationLevel(AgentInstrumentationLevel.FULL).disableWebAppAutoStart()
                .customName("Tomcat-MathApp-BA-PO")
                .additionalProperties(agentAdditionalProps)
                .build();
        machine.addRole(agentRole);
        
        XmlModifierFlowContext modifyUsersFlow =
            new XmlModifierFlowContext.Builder(tomcatRole.getInstallDir() + "/conf/tomcat-users.xml")
                .createNodeByXml("/tomcat-users", "<role rolename=\"math\"/>")
                .createNodeByXml("/tomcat-users", "<user username=\"user\" password=\"math\" roles=\"math\"/>")
                .createNodeByXml("/tomcat-users", "<user username=\"userA\" password=\"math\" roles=\"math\"/>")
                .createNodeByXml("/tomcat-users", "<user username=\"userB\" password=\"math\" roles=\"math\"/>")
                .createNodeByXml("/tomcat-users", "<user username=\"userC\" password=\"math\" roles=\"math\"/>")
                .build();
        IRole addUsersRole = new UniversalRole.Builder(machine.getMachineId() + "_add_users", tasResolver)
                                .runFlow(XmlModifierFlow.class, modifyUsersFlow).build();
        machine.addRole(addUsersRole);
        addUsersRole.after(agentRole);
        
        Collection<IRole> startRoles = tomcatRole.getStartRoles();
        machine.addRoles(startRoles);
        for (IRole r : startRoles) {
            r.after(addUsersRole);
        }

        PipeOrganUtility.addPipeOrganRoles(machine, "tomcat", emInfo.getHostname(), tomcatRole, tasResolver);

        return tomcatRole;
    }
    
    public static IRole addMathAppCronRole(ITestbedMachine machine, String baseUrl,
        ITasResolver tasResolver) {

        // every minute downloads some rest operations
        StringBuilder cronEntry = new StringBuilder();
        for (String command: MATHAPP_COMMANDS) {
            cronEntry.append("* * * * * root ").append(String.format(command, baseUrl)).append(" >/dev/null 2>&1\n");
        }
        IRole cronRole = new CronEntryRole("curl_appmap", cronEntry.toString());
        machine.addRole(cronRole);
        return cronRole;
    }

    static public String getStartRequestRoleId(String machineId) {
        return machineId + START_REQ_ROLE_ID_SUFFIX;
    }
    
    private static IRole createStartNbPartRole(ITestbedMachine machine, String suffixRoleId, String workDir,
                                                 String command, IRole beforeRole, String envKey, ITasResolver tasResolver) {
        RunCommandFlowContext startContext = new RunCommandFlowContext.Builder(BANKING_CONSOLE_CONTROL_CMD)
                                                    .args(Arrays.asList(command))
                                                    .terminateOnMatch(BANKING_CONSOLE_CONTROL_FINAL_TEXT)
                                                    .workDir(workDir)
                                                    .build();
        UniversalRole.Builder startRoleBuilder = new UniversalRole.Builder(machine.getMachineId() + suffixRoleId, tasResolver)
                                                    .syncCommand(startContext);
        UniversalRole startRole = startRoleBuilder.build();
        machine.addRole(startRole);
        startRole.after(beforeRole);
        if (envKey != null) {
            startRoleBuilder.getEnvProperties().add(ENV_NB_START_REQUESTS, startContext);
        }
        return startRole;
    }

    public static IRole addMmRole(ITestbedMachine machine, String roleId, EmRole emRole, String mmName) {
        // install MM to EM
        ManagementModuleRole mmRole =
            new ManagementModuleRole(roleId, "/" + mmName + ".jar", emRole
                .getDeployEmFlowContext().getInstallDir());
    
        mmRole.after(emRole);
        machine.addRole(mmRole);
        return mmRole;
    }

    public static IRole addStartEmRole(ITestbedMachine machine, EmRole emRole, boolean startWv,
        IRole beforeRole) {
        // starts EM and WebView
        ExecutionRole.Builder builder =
            new ExecutionRole.Builder(emRole.getRoleId() + "_start").syncCommand(emRole
                .getEmRunCommandFlowContext());
        if (startWv) {
            builder.syncCommand(emRole.getWvRunCommandFlowContext());
        }
        ExecutionRole startRole = builder.build();
        startRole.after(beforeRole);
        machine.addRole(startRole);
        return startRole;
    }

    static public void addNewTradeServiceWars(ITestbedMachine machine, AgentCapable tomcatRole,
                                              IRole beforeRole, ITasResolver tasResolver) {
        
        Artifact oeArtifact = new DefaultArtifact("com.ca.apm.testing", "OrderEngine", "war",
                tasResolver.getDefaultVersion());
        IRole oeRole = new UniversalRole.Builder("OrderEngine", tasResolver).download(oeArtifact,
                tomcatRole.getWebappsDirectory(), "OrderEngine.war").build();
        
        Artifact rsArtifact = new DefaultArtifact("com.ca.apm.testing", "ReportingService", "war",
                tasResolver.getDefaultVersion());
        IRole rsRole = new UniversalRole.Builder("ReportingService", tasResolver).download(rsArtifact,
                tomcatRole.getWebappsDirectory(), "ReportingService.war").build();
        
        Artifact tsArtifact = new DefaultArtifact("com.ca.apm.testing", "TradeService", "war",
                tasResolver.getDefaultVersion());
        IRole tsRole = new UniversalRole.Builder("TradeService", tasResolver).download(tsArtifact,
                tomcatRole.getWebappsDirectory(), "TradeService.war").build();
        
        beforeRole.before(oeRole, rsRole, tsRole);
        machine.addRole(oeRole, rsRole, tsRole);
    }

    static public HammondRole addMfHammondRole(ITestbedMachine machine, String roleId, EmRole emRole, ITasResolver tasResolver) {
        
        IThirdPartyArtifact data = new Data("hammond-mf-data", "1.0");
        HammondRole hammondRole = new HammondRole.Builder(roleId, tasResolver)
                                .data(data)
                                .collector(tasResolver.getHostnameById(emRole.getRoleId()))
                                .build();
        machine.addRole(hammondRole);
        hammondRole.after(emRole);
        return hammondRole;
    }

    static public void createSleepTxtFile(ITestbedMachine machine,
                                       IRole afterRole, ITasResolver tasResolver) {
        FileCreatorFlowContext context = new FileCreatorFlowContext.Builder().fromResource("/sleep.txt").destinationPath("C:/sleep.txt").build();
        IRole createSleepRole = new UniversalRole.Builder("create_sleep", tasResolver)
                        .runFlow(FileCreatorFlow.class, context).build();
        afterRole.after(createSleepRole);
        machine.addRole(createSleepRole);
    }

    static public IRole createBaSnippetFile(ITestbedMachine machine,
                                            Map<String, String> agentAdditionalProps, ITasResolver tasResolver) {
        String snippetPath = "C:/sw/tradeservice.basnippet";
        FileCreatorFlowContext snippetContext = new FileCreatorFlowContext.Builder()
                                            .fromResource("/tradeservice.basnippet")
                                            .destinationPath(snippetPath)
                                            .build();
        IRole createSnippetRole = new UniversalRole.Builder("create_snippet", tasResolver)
                        .runFlow(FileCreatorFlow.class, snippetContext).build();
        machine.addRole(createSnippetRole);

        enableBrowserAgent(agentAdditionalProps, "tradeservice", snippetPath);

        return createSnippetRole;
    }

    public static void synchronizeTime(ITestbed testBed, ITasResolver tasResolver) {
        List<ITestbedMachine> machines = testBed.getMachines();
        for (ITestbedMachine machine : machines) {
            IRole timeSyncRole =
                new UniversalRole.Builder(machine.getMachineId() + "_syncTime", tasResolver)
                    .runFlow(TimeSyncFlow.class, new TimeSyncFlowContext.Builder().build()).build();
            for (IRole role : machine.getRoles()) {
                timeSyncRole.before(role);
            }
            machine.addRole(timeSyncRole);
        }
    }
    
    public static ITestbedMachine addDxcMachine(ITestbed testBed, EmRole em, ITasResolver tasResolver) {
        
        ITestbedMachine dxcMachine = new TestbedMachine.LinuxBuilder(
                DXC_MACHINE_ID).platform(Platform.LINUX).templateId("co66")
                .bitness(Bitness.b64).build();

        String snippetPath = dxcMachine.getAutomationBaseDir() + "brtmtestapp.basnippet";
        
        TomcatRole tomcat =
            new TomcatRole.LinuxBuilder(TOMCATROLE, tasResolver)
                .tomcatVersion(TomcatVersion.v70)
                .webApplication(new BRTMTestAppArtifact(tasResolver)
                                    .createArtifact(BRTM_TEST_APP_VERSION), BRTM_TEST_APP_CONTEXT)
                .tomcatCatalinaPort(TOMCAT_PORT).build();
      
        Map<String, String> agentAdditionalProps = new HashMap<>();
        enableBrowserAgent(agentAdditionalProps, "brtmtestapp", snippetPath);

        AgentRole agentRole =
            new AgentRole.Builder(TOMCATROLE + "-agent", tasResolver)
                .intrumentationLevel(AgentInstrumentationLevel.TYPICAL).webAppServer(tomcat)
                .additionalProperties(agentAdditionalProps)
                .emRole(em).build();
        
        KafkaZookeeperRole kafka = new KafkaZookeeperRole.LinuxBuilder(
                KAFKAROLE, tasResolver).build();
        LogstashRole logstash = new LogstashRole.LinuxBuilder(LOGSTASHROLE,
                tasResolver).emHost(tasResolver.getHostnameById(em.getRoleId())).emPort(5001).build();

        DXCRole dxc = new DXCRole.LinuxBuilder(DXCROLE, tasResolver).build();
        BARole ba = new BARole.LinuxBuilder("BA", tasResolver).snippetPath(snippetPath)
                .dxcHost(hostnameToFqdn(tasResolver.getHostnameById(DXCROLE))).build();
        ba.after(dxc);
        dxcMachine.addRole(tomcat, agentRole, logstash, kafka, dxc, ba);

        Map<String, String> updateArgs = new HashMap<String, String>();
        updateArgs.put("\"pageLoadMetricsThreshold\" : (.*)", "\"pageLoadMetricsThreshold\" : 0,");
        updateArgs.put("\"ajaxMetricsThreshold\" : (.*)", "\"ajaxMetricsThreshold\" : 0,");
        updateArgs.put("\"jsFunctionMetricsThreshold\" : (.*)", "\"jsFunctionMetricsThreshold\" : 0,");

        FileModifierFlowContext updateProps = new FileModifierFlowContext.Builder()
                .replace("/opt/dxc/browserAgent/wa/ba/profile/default-tenant/default-app/profile.json", updateArgs)
                .build();
        UniversalRole startRole = new UniversalRole.Builder("startAll", tasResolver)
                        .runFlow(RunCommandFlow.class, kafka.getStartZookeeperFlowContext())
                        .runFlow(RunCommandFlow.class, kafka.getStartKafkaFlowContext())
                        .runFlow(RunCommandFlow.class, logstash.getStartAPMLogstashFlowContext())
                        .runFlow(RunCommandFlow.class, dxc.getStartDxcFlowContext())
                        .runFlow(RunCommandFlow.class, kafka.getCreateTopicsFlowContext())
                        .runFlow(RunCommandFlow.class, tomcat.getStartCmdFlowContext())
                        .runFlow(FileModifierFlow.class, updateProps)
                        .runFlow(RunCommandFlow.class, dxc.getUploadBaFlowContext())
                        .build();
        dxcMachine.addRole(startRole);
        startRole.after(tomcat, agentRole, logstash, kafka, dxc, ba, em);

        String baseUrl = "http://" + tasResolver.getHostnameById(tomcat.getRoleId())
                + ":" + TOMCAT_PORT + "/" + BRTM_TEST_APP_CONTEXT;

        Artifact testArtifact =
            new DefaultArtifact("com.ca.apm.test", "em-tests-core", "jar-with-dependencies", "jar",
                tasResolver.getDefaultVersion());
        String jarPath = dxcMachine.getAutomationBaseDir() + "em-tests-core.jar";
        GenericRole downloadRole = new GenericRole.Builder("dxc_test_jar", tasResolver).download(testArtifact,
            jarPath).build();
        downloadRole.after(startRole);
        String cronEntry = "* * * * * root java -cp " + jarPath
                + " com.ca.tas.dxc.test.DXCTest " + baseUrl;
        CronEntryRole cronRole = new CronEntryRole("dxc_cron_chrome", cronEntry);
        cronRole.after(downloadRole);
        dxcMachine.addRole(downloadRole, cronRole);

        testBed.addMachine(dxcMachine);
        return dxcMachine;
    }
    
    public static String getIp(String hostname) {
        InetAddress address = null;
        try {
            address = InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            LOGGER.error("Failed to resolve IP address of {}", hostname, e);
            return hostname;
        }
        return address.getHostAddress();
    }

    static public IRole addSysedgeRole(ITestbedMachine machine, ITasResolver tasResolver) {
        
        boolean win = Platform.WINDOWS.equals(machine.getPlatform());
        TasExtension ext = win ? TasExtension.ZIP : TasExtension.TAR_GZ;
        Collection<String> responseData = Arrays.asList(
                "CA_SETUP_PACKAGE_NAME=CA_SystemEDGE_Core",
                "CASE_LEGACY_MODE=no",
                "EULA_ACCEPTED=YES");

        String sysedgePath = machine.getAutomationBaseDir() + "sysedge";
        DefaultArtifact sysedgeArtifact =
            new DefaultArtifact(SYSEDGE_GROUP_ID, SYSEDGE_ARTIFACT_ID, win ? "x64" : "linux_x86",
                ext.getValue(), SYSEDGE_VERSION);
        GenericRole sysedgeRole =
            new GenericRole.Builder(machine.getMachineId() + "_download_sysedge", tasResolver)
                .unpack(sysedgeArtifact, sysedgePath).build();
        
        if (!win) {
            IRole packagesRole = new YumInstallPackageRole.Builder(machine.getMachineId() + "_packages")
                    .addPackage("glibc.i686").addPackage("psmisc").build();
            machine.addRole(packagesRole);
            packagesRole.before(sysedgeRole);
        }
        String sysedgeBinPath = win ? sysedgePath : sysedgePath + "/CA_SystemEDGE_Core";
        String responsePath = sysedgeBinPath + "/ca-setup.dat";
        FileCreatorFlowContext responseContext = new FileCreatorFlowContext.Builder()
                                                    .fromData(responseData)
                                                    .destinationPath(responsePath)
                                                    .build();
        RunCommandFlowContext runContext = new RunCommandFlowContext.Builder(win ? "ca-setup.exe" : "ca-setup.sh")
                                                    .args(Arrays.asList("/r", responsePath))
                                                    .workDir(sysedgeBinPath)
                                                    .build();
        IRole runRole = new UniversalRole.Builder(machine.getMachineId() + "_run_sysedge", tasResolver)
                        .runFlow(FileCreatorFlow.class, responseContext)
                        .runFlow(RunCommandFlow.class, runContext).build();
        runRole.after(sysedgeRole);
        machine.addRole(sysedgeRole, runRole);

        return runRole;
    }

    static public IRole addCollectorAgentRole(ITestbedMachine machine, EmConnectionInfo emInfo, String dockerHost,
                                              Collection<IRole> sysedges,
                                              String serverUrl, String serverName,
                                              boolean start, ITasResolver tasResolver) {
        
        boolean win = Platform.WINDOWS.equals(machine.getPlatform());
        String colAgentPath = machine.getAutomationBaseDir() + "CollectorAgent";
        String deployPath = colAgentPath + "/extensions/deploy";
        TasExtension ext = win ? TasExtension.ZIP : TasExtension.TAR_GZ;
        
        DefaultArtifact colAgentArtifact =
            new DefaultArtifact(COLLECTOR_AGENT_GROUP_ID, COLLECTOR_AGENT_ARTIFACT_ID, win ? "windows" : "unix",
                ext.getValue(), tasResolver.getDefaultVersion());
        GenericRole downloadColAgent =
            new GenericRole.Builder(machine.getMachineId() + "_download_col_agent", tasResolver)
                .unpack(colAgentArtifact, colAgentPath).build();

        RunCommandFlowContext javaLinkContext =
            new RunCommandFlowContext.Builder("ln").doNotPrependWorkingDirectory()
                .args(Arrays.asList("-s", "/opt/jdk1.8/jre", colAgentPath + "/jre")).build();
        IRole startStopRole =
            new UniversalRole.Builder(machine.getMachineId() + "_start_stop_col_agent", tasResolver)
                .runFlow(RunCommandFlow.class, javaLinkContext)
                .runFlow(RunCommandFlow.class, createRunAgentContext(colAgentPath, true, win))
                .runFlow(RunCommandFlow.class, createRunAgentContext(colAgentPath, false, win)).build();

        UniversalRole.Builder configureColAgent = new UniversalRole.Builder(machine.getMachineId() + "_collector_agent", tasResolver);
        
        if (dockerHost != null) {
            DefaultArtifact monitorArtifact =
                new DefaultArtifact(DOCKER_MONITOR_GROUP_ID, DOCKER_MONITOR_ARTIFACT_ID,
                    TasExtension.TAR_GZ.getValue(), tasResolver.getDefaultVersion());
            IRole monitorRole = new GenericRole.Builder(machine.getMachineId() + "_download_monitor", tasResolver)
                    .download(monitorArtifact, deployPath + "/DockerMonitor.tar.gz")
                    .build();
            addExtensionRole(machine, startStopRole, downloadColAgent, monitorRole);
            monitorRole.addProperty(ENV_DOCKER_HOSTNAME, dockerHost);
            monitorRole.addProperty(ENV_DOCKER_CONF_PATH, colAgentPath + "/extensions/DockerMonitor/bundle.properties");
            
            DefaultArtifact containerArtifact =
                new DefaultArtifact(CONTAINER_FLOW_GROUP_ID, CONTAINER_FLOW_ARTIFACT_ID,
                    TasExtension.TAR_GZ.getValue(), tasResolver.getDefaultVersion());
            IRole containerRole = new GenericRole.Builder(machine.getMachineId() + "_download_discovery", tasResolver)
                    .download(containerArtifact, deployPath + "/ContainerFlow.tar.gz")
                    .build();
            addExtensionRole(machine, startStopRole, downloadColAgent, containerRole);
            
            Map<String, String> bundleMap = new HashMap<>();
            bundleMap.put("docker.hostname", dockerHost);
            bundleMap.put("docker.port", "2375");
            ConfigureFlowContext confDockerCtx =  new ConfigureFlowContext.Builder()
                    .configurationMap(colAgentPath + "/extensions/DockerMonitor/bundle.properties", bundleMap)
                    .build();
            configureColAgent.runFlow(ConfigureFlow.class, confDockerCtx);
        }
        
        if (sysedges != null) {
            downloadColAgent.after(sysedges);
            DefaultArtifact snmpArtifact =
                new DefaultArtifact(SNMP_COLLECTOR_GROUP_ID, SNMP_COLLECTOR_ARTIFACT_ID,
                    TasExtension.TAR_GZ.getValue(), tasResolver.getDefaultVersion());
            IRole snmpRole = new GenericRole.Builder(machine.getMachineId() + "_download_snmp", tasResolver)
                    .download(snmpArtifact, deployPath + "/SnmpCollector.tar.gz")
                    .build();
            addExtensionRole(machine, startStopRole, downloadColAgent, snmpRole);
            
            Map<String, String> bundleMap = new HashMap<>();
            bundleMap.put("snmpcollector.atc.accesstoken", ADMIN_AUX_TOKEN);
            StringBuilder hostlist = new StringBuilder();
            for (IRole sysedge : sysedges) {
                String sysedgeHost = tasResolver.getHostnameById(sysedge.getRoleId());
                if (hostlist.length() != 0) {
                    hostlist.append(',');
                }
                hostlist.append(sysedgeHost);

                bundleMap.put("snmpcollector." + sysedgeHost + ".config", "oid2metrics.cnf");
                bundleMap.put("snmpcollector." + sysedgeHost + ".graph", "graph.json");
                bundleMap.put("snmpcollector." + sysedgeHost + ".host", sysedgeHost);
                bundleMap.put("snmpcollector." + sysedgeHost + ".port", "161");
                bundleMap.put("snmpcollector." + sysedgeHost + ".version", "V1");
                bundleMap.put("snmpcollector." + sysedgeHost + ".community", "public");
            }
            bundleMap.put("snmpcollector.hostlist", hostlist.toString());
            ConfigureFlowContext configureContext =  new ConfigureFlowContext.Builder()
                .configurationMap(colAgentPath + "/extensions/SnmpCollector/bundle.properties", bundleMap)
                .build();
            configureColAgent.runFlow(ConfigureFlow.class, configureContext);
        }

        if (serverUrl != null) {
            DefaultArtifact webServerArtifact =
                new DefaultArtifact(WEB_SERVER_MONITOR_GROUP_ID, WEB_SERVER_MONITOR_ARTIFACT_ID,
                    TasExtension.TAR_GZ.getValue(), tasResolver.getDefaultVersion());
            GenericRole webServerRole =
                new GenericRole.Builder(machine.getMachineId() + "_download_web_server_mon", tasResolver)
                    .download(webServerArtifact, deployPath + "/WebServerAgent.tar.gz")
                    .build();
            addExtensionRole(machine, startStopRole, downloadColAgent, webServerRole);

            XmlModifierFlowContext configureWSContext =
                new XmlModifierFlowContext.Builder(colAgentPath + "/extensions/WebServerAgent/config/WebServerConfig.xml")
                    .createNodeByXml("/WebServers",
                        "<WebServer Type=\"Apache\" Enabled=\"true\" ServerURL=\"" + serverUrl
                            + "\" DisplayName=\"" + serverName + "\" RefreshFrequencyInSeconds=\"15\"/>")
                    .build();
            configureColAgent.runFlow(XmlModifierFlow.class, configureWSContext);
        }
        
        Map<String, String> propertyMap = new HashMap<>();
        emInfo.fillAgentProperties(propertyMap);
        ConfigureFlowContext configureContext =  new ConfigureFlowContext.Builder()
                .configurationMap(colAgentPath + "/core/config/IntroscopeCollectorAgent.profile", propertyMap).build();
        configureColAgent.runFlow(ConfigureFlow.class, configureContext);
        if (start) {
            configureColAgent.runFlow(RunCommandFlow.class, createRunAgentContext(colAgentPath, true, win));
        }
        IRole colAgentRole = configureColAgent.build();
        
        colAgentRole.addProperty(ENV_START_COLLECTOR_AGENT, createRunAgentContext(colAgentPath, true, win));

        /* download all, start/stop, configure/start */
        startStopRole.after(downloadColAgent);
        colAgentRole.after(startStopRole);
        machine.addRole(downloadColAgent, startStopRole, colAgentRole);

        return colAgentRole;
    }

    static private void addExtensionRole(ITestbedMachine machine, IRole startStopRole,
        IRole downloadColAgent, IRole extRole) {
        extRole.before(startStopRole);
        extRole.after(downloadColAgent);
        machine.addRole(extRole);
    }

    public static void addDomainXmlRole(ITestbedMachine machine, EmRole emRole, ITasResolver tasResolver) {
        FileCreatorFlowContext context = new FileCreatorFlowContext.Builder()
                .fromData(DOMAINS_DATA)
                .destinationPath(emRole.getInstallDir() + "/config/domains.xml")
                .build();
        IRole createDomainsRole = new UniversalRole.Builder(machine.getMachineId() + "create_domains", tasResolver)
                .runFlow(FileCreatorFlow.class, context).build();
        createDomainsRole.after(emRole);
        machine.addRole(createDomainsRole);
    }
    
    private static RunCommandFlowContext createRunAgentContext(String colAgentPath, boolean start, boolean win) {
         return new RunCommandFlowContext.Builder(win ? "CollectorAgent.cmd" : "CollectorAgent.sh")
                .terminateOnMatch(start ? "Introscope Agent startup complete." : "CollectorAgent stopped")
                .workDir(colAgentPath + "/bin")
                .args(Arrays.asList(start ? "start" : "stop"))
                .build();
    }
    
    private static class Data implements IThirdPartyArtifact {

        private static final long serialVersionUID = 102526828070184151L;
        private Artifact artifact;

        Data(String archiveFileName, String version) {
            artifact =
                new DefaultArtifact(GROUP_ID, archiveFileName, TasExtension.ZIP.getValue(), version);
        }

        @Override
        public Artifact getArtifact() {
            return artifact;
        }

        @Override
        public String getFilename() {
            return String.format("%s.%s", artifact.getArtifactId(), artifact.getExtension());
        }
    }
    
    public static String hostnameToFqdn(String hostname) {

        try {
            final InetAddress addr = InetAddress.getByName(hostname);
            return addr.getCanonicalHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void enableBrowserAgent(Map<String, String> props, String appID, String snippetPath) {
        props.put("introscope.agent.browseragent.autoInjectionEnabled", "true");
        props.put("introscope.agent.browseragent.response.decoration.enabled", "true");
        props.put("introscope.agent.browseragent.response.decoration.apmData.enabled", "true");
        props.put("introscope.agent.browseragent.responseCookieEnabled", "true");
        props.put("introscope.agent.browseragent.responseCookie.includeURLsRegex", ".*");
        props.put("introscope.agent.browseragent.autoInjection.appID", appID);
        props.put("introscope.agent.browseragent.autoInjection." + appID + ".snippetLocation", snippetPath);
        props.put("introscope.agent.browseragent.autoInjection." + appID + ".includeURLsRegex", ".*");
    }
}
