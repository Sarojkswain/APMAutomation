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

package com.ca.tas.test.em.agc;

import static java.lang.String.format;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.test.em.util.EmConnectionInfo;
import com.ca.apm.test.em.util.RoleUtility;
import com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.em.saas.SaasEmTestBed;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.CronEntryRole;
import com.ca.tas.role.ElasticSearchRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.KibanaRole;
import com.ca.tas.role.PhantomJSRole;
import com.ca.tas.role.TIMAttendeeRole;
import com.ca.tas.role.TIMRole;
import com.ca.tas.role.testapp.custom.TradeServiceAppRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;

/**
 * Represents testbed for verifying the AGC capability of Introscope CA product.
 * 
 * @author Korcak, Zdenek <korzd01@ca.com>
 * 
 */
@TestBedDefinition
public class ComplexNowhereBankTestBed implements ITestbedFactory {

    private static final String EM_CONF_PROP_TT_TIME_FAST =
        "introscope.enterprisemanager.transactiontrace.arrivalbuffer.incubationtime.fast";
    private static final String EM_CONF_PROP_DUMP_GRAPHS =
        "introscope.apmserver.agcmaster.correlation.dumpgraphs";
    private static final String EM_CONF_PROP_TOPOLOGY_POLLER =
            "introscope.enterprisemanager.appmap.em.topologyPoller";
    private static final String EM_CONF_PROP_DYNAMIC_UPDATE =
        "introscope.enterprisemanager.domainsconfiguration.dynamicupdate.enable";

    public static final String AGC_COLLECTOR_ROLE_ID = "agc_collector";
    public static final String MOM_COLLECTOR_ROLE_ID1 = "mom_collector";
    public static final String MOM_COLLECTOR_ROLE_ID2 = "collector";
    public static final String COLLECTOR_AGENT_ROLE_ID = SaasEmTestBed.EM_MACHINE_ID + "_collector_agent";
    public static final String DOCKER_MONITOR_ROLE_ID = SaasEmTestBed.EM_MACHINE_ID + "_download_monitor";
    public static final String WEB_SERVER_MONITOR_ROLE_ID = SaasEmTestBed.EM_MACHINE_ID
                                        + RoleUtility.WEB_SERVER_MONITOR_ROLE_SUFFIX;

    public static final String AGC_ROLE_ID = "agc_em";
    public static final String MOM_ROLE_ID = "mom_em";
    public static final String STANDALONE_ROLE_ID = "introscope";
    public static final String TOMCAT_ROLE_ID = "tomcat60";
    public static final String AGC_MACHINE = "agc";
    public static final String HAMMOND_ROLE_ID = "mf_hammond";

    private String esHost = null;

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        ITestbed testbed = new Testbed("AGC/NowhereBank");

        ITestbedMachine timMachine = createLinuxMachine("gateway");
        TIMRole timRole = new TIMRole.Builder("tim", tasResolver).build();
        timMachine.addRole(timRole);
        testbed.addMachine(timMachine);
        testbed.addProperty("machine.gateway.ip_v4", RoleUtility.getIp(tasResolver.getHostnameById(timRole.getRoleId())));

        ElasticSearchRole esRole = new ElasticSearchRole.Builder("elastic", tasResolver).build();
        esHost = tasResolver.getHostnameById(esRole.getRoleId());
        UniversalRole startESRole = new UniversalRole.Builder("startES", tasResolver)
                                    .runFlow(RunCommandFlow.class, esRole.getStartElasticSearchContext())
                                    .build();
        KibanaRole kibanaRole = new KibanaRole.Builder("kibana", tasResolver).elasticSearch(esHost).build();
        UniversalRole startKibanaRole = new UniversalRole.Builder("startKibana", tasResolver)
                                    .runFlow(RunCommandFlow.class, kibanaRole.getStartKibanaContext())
                                    .build();
        startESRole.after(esRole);
        kibanaRole.after(startESRole);
        startKibanaRole.after(kibanaRole);

        ITestbedMachine agcMachine = createMachine(AGC_MACHINE);

        ITestbedMachine standaloneMachine = createMachine("standalone");
        EmRole standaloneRole = addStandaloneRoles(standaloneMachine, tasResolver);
        standaloneMachine.addRole(new PhantomJSRole.Builder("phantomjs", tasResolver).build());
        TIMAttendeeRole eumTimAttendeeRole =
            new TIMAttendeeRole.Builder("eum-tim-attendee", timRole, tasResolver).build();
        eumTimAttendeeRole.after(standaloneRole);
        standaloneMachine.addRole(eumTimAttendeeRole);
        RoleUtility.addStartEmRole(standaloneMachine, standaloneRole, false, eumTimAttendeeRole);
        RoleUtility.addMfHammondRole(standaloneMachine, HAMMOND_ROLE_ID, standaloneRole, tasResolver);
        testbed.addMachine(standaloneMachine);

        ITestbedMachine collectorMachine = createMachine("collector");
        ITestbedMachine momMachine = createMachine("mom");
        IRole sysedgeMom = RoleUtility.addSysedgeRole(momMachine, tasResolver);
        String momHostName = tasResolver.getHostnameById(sysedgeMom.getRoleId());
        EmRole collectorRole = addCollectorRoles(collectorMachine, "",
                tasResolver.getHostnameById(standaloneRole.getRoleId()), momHostName, tasResolver);
        EmConnectionInfo collectorInfo = new EmConnectionInfo(collectorRole, tasResolver);
        IRole tradeServiceRole =
            addTradeServiceRoles(collectorMachine, standaloneRole, timRole, tasResolver);
        String tradeBaseUrl = "http://"
            + RoleUtility.hostnameToFqdn(tasResolver.getHostnameById(tradeServiceRole.getRoleId()))
            + ":7080/";
        IRole mathAppRole = RoleUtility.addMathAppRoles(collectorMachine, collectorInfo, null, tasResolver);
        mathAppRole.after(collectorRole);
        testbed.addMachine(collectorMachine);
        String mathBaseUrl = "http://"
            + RoleUtility.hostnameToFqdn(tasResolver.getHostnameById(mathAppRole.getRoleId()))
            + ":8080/";
        RoleUtility.addMathAppCronRole(timMachine, mathBaseUrl, tasResolver);

        EmRole momRole = addMomRoles(momMachine, collectorRole, null, tasResolver);
        RoleUtility.addStartEmRole(momMachine, momRole, false, momRole);
        testbed.addMachine(momMachine);

        ITestbedMachine followers[] = {standaloneMachine, momMachine};
        EmRole agcRole = addMomRoles(agcMachine, null, followers, tasResolver);
        RoleUtility.addStartEmRole(agcMachine, agcRole, true, agcRole);
        testbed.addMachine(agcMachine);
        agcRole.after(startESRole);
        startKibanaRole.after(agcRole);
        
        ITestbedMachine dxcMachine = RoleUtility.addDxcMachine(testbed, collectorRole, tasResolver);

        // docker machine
        ITestbed dockerTestbed = new SaasEmTestBed(esHost).create(tasResolver);
        List<ITestbedMachine> dockerMachines = dockerTestbed.getMachines();
        if (dockerMachines.size() > 0) {
            testbed.addMachines(dockerMachines);

            // docker and sysedge monitoring
            ITestbedMachine dockerMachine = dockerMachines.get(0);

            dockerMachine.addRole(esRole, startESRole, kibanaRole, startKibanaRole);
            
            IRole sysedgeDocker = RoleUtility.addSysedgeRole(dockerMachine, tasResolver);
            IRole sysedgeCollector = RoleUtility.addSysedgeRole(collectorMachine, tasResolver);
            IRole sysedgeTim = RoleUtility.addSysedgeRole(timMachine, tasResolver);
            IRole sysedgeAgc = RoleUtility.addSysedgeRole(agcMachine, tasResolver);
            IRole sysedgeStandalone = RoleUtility.addSysedgeRole(standaloneMachine, tasResolver);
            IRole sysedgeDxc = RoleUtility.addSysedgeRole(dxcMachine, tasResolver);

            // httpd and WebServer powerpack
            IRole httpdRole = RoleUtility.addHttpdRole(dockerMachine, "8090", tradeBaseUrl, tasResolver);
            String tradeSecondaryUrl = "http://"
                    + RoleUtility.hostnameToFqdn(tasResolver.getHostnameById(httpdRole.getRoleId()))
                    + ":8090/";

            RoleUtility.addCollectorAgentRole(dockerMachine,
                collectorInfo,
                tasResolver.getHostnameById(sysedgeDocker.getRoleId()),
                Arrays.asList(sysedgeDocker, sysedgeCollector, sysedgeTim, sysedgeAgc,
                              sysedgeStandalone, sysedgeMom, sysedgeDxc),
                tradeSecondaryUrl, "Apache-TradeService",
                false, tasResolver);

            addCronRoles(timMachine, tradeSecondaryUrl, mathAppRole, tasResolver);
        } else {
            addCronRoles(timMachine, tradeBaseUrl, mathAppRole, tasResolver);
        }

        testbed.addProperty("selenium.webdriverURL", "http://cz-selenium1.ca.com:4444/wd/hub");
        final String emHostname = tasResolver.getHostnameById(agcRole.getRoleId());
        testbed.addProperty("test.emHostname", emHostname);
        testbed.addProperty("test.applicationBaseURL", format("http://%s:8082", emHostname));

        RoleUtility.synchronizeTime(testbed, tasResolver);
        
        return testbed;
    }

    public EmRole addStandaloneRoles(ITestbedMachine machine, ITasResolver tasResolver) {
        // "introscope" role required by import_bt
        EmRole.Builder emBuilder =
            new EmRole.Builder("introscope", tasResolver)
                .dbpassword("quality")
                .configProperty(EM_CONF_PROP_TT_TIME_FAST, "30")
                .configProperty(EM_CONF_PROP_DUMP_GRAPHS, "true")
                .configProperty(EM_CONF_PROP_TOPOLOGY_POLLER, "true")                
                .installerProperty("shouldEnableSysview", "true")
                .emLaxNlJavaOption(RoleUtility.getDevEmLaxnlJavaOption(9001))
                .nostartEM().nostartWV();
        addElasticSearchProperties(emBuilder, tasResolver);
        EmRole emStandaloneRole = emBuilder.build();

        machine.addRole(emStandaloneRole);
        RoleUtility.addMmRole(machine, emStandaloneRole.getRoleId() + "1_mm", emStandaloneRole, "StatusTestMM");
        RoleUtility.addMmRole(machine, emStandaloneRole.getRoleId() + "2_mm", emStandaloneRole, "TradingServiceMM");
        RoleUtility.addMmRole(machine, emStandaloneRole.getRoleId() + "3_mm", emStandaloneRole, "PipeOrganMM");
        // addNowhereBankRole(machine, emStandaloneRole, null, tasResolver);

        return emStandaloneRole;
    }

    private EmRole addMomRoles(ITestbedMachine machine, EmRole remoteCollectorRole,
        ITestbedMachine followers[], ITasResolver tasResolver) {
        EmRole collectorRole = addCollectorRoles(machine, "_collector", null, null, tasResolver);

        EmRole emMomRole; // used for installing MM
        EmRole.Builder emBuilder;
        emBuilder =
            new EmRole.Builder(machine.getMachineId() + "_em", tasResolver)
                .dbpassword("quality")
                .dbhost(tasResolver.getHostnameById(collectorRole.getRoleId()))
                .emPort(5003)
                .wvEmPort(5003)
                // we need .emWebPort(8081), because WV uses this for proxy
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
                .emCollector(collectorRole)
                .emLaxNlJavaOption(RoleUtility.getDevEmLaxnlJavaOption(9003))
                .configProperty(EM_CONF_PROP_DUMP_GRAPHS, "true")
                .configProperty(EM_CONF_PROP_TOPOLOGY_POLLER, "true")
                .configProperty(EM_CONF_PROP_DYNAMIC_UPDATE, "true")
                .installerProperty("shouldEnableSysview", "true")
                .nostartEM().nostartWV();
        if (remoteCollectorRole != null) {
            emBuilder.emCollector(remoteCollectorRole);
        }
        if (followers != null) {
            emBuilder.configProperty("introscope.apmserver.teamcenter.master", "true");
        }
        addElasticSearchProperties(emBuilder, tasResolver);
        emMomRole = emBuilder.build();
        emMomRole.after(collectorRole);

        machine.addRole(emMomRole);
        RoleUtility.addMmRole(machine, emMomRole.getRoleId() + "_mm1", emMomRole, "NowhereBankMM");
        RoleUtility.addMmRole(machine, emMomRole.getRoleId() + "_mm2", emMomRole, "PipeOrganMM");
        //RoleUtility.addDomainXmlRole(machine, emMomRole, tasResolver);

        return emMomRole;
    }

    private EmRole addCollectorRoles(ITestbedMachine machine, String idSuffix, String secondHost,
                                     String dbHost, ITasResolver tasResolver) {
        EmRole.Builder emBuilder;
        emBuilder =
            new EmRole.Builder(machine.getMachineId() + idSuffix, tasResolver)
                .dbpassword("quality")
                .installSubDir("collector")
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                // we need .emPort(5001), because NowhereBank uses it for sending data
                .emWebPort(8083)
                .silentInstallChosenFeatures(
                    Arrays.asList("Enterprise Manager", "ProbeBuilder"))
                .configProperty(EM_CONF_PROP_TT_TIME_FAST, "30")
                .configProperty(EM_CONF_PROP_TOPOLOGY_POLLER, "true")
                .emLaxNlJavaOption(RoleUtility.getDevEmLaxnlJavaOption(9001))
                .nostartWV();
        addElasticSearchProperties(emBuilder, tasResolver);
        if (dbHost != null) {
            emBuilder.dbhost(dbHost);
        }
        EmRole collectorRole = emBuilder.build();
        machine.addRole(collectorRole);

        RoleUtility.addNowhereBankRole(machine, collectorRole, secondHost, tasResolver);
        return collectorRole;
    }

    private IRole addTradeServiceRoles(ITestbedMachine machine, EmRole emRole, TIMRole timRole,
        ITasResolver tasResolver) {
        TomcatRole tomcatRole =
            new TomcatRole.Builder(TOMCAT_ROLE_ID, tasResolver).tomcatVersion(TomcatVersion.v60)
                .tomcatCatalinaPort(7080).jdkHomeDir("C:/Program Files/Java/jdk1.6.0_45")
                .installDir("C:/sw/apache-tomcat-6.0.36").build();
        machine.addRole(tomcatRole);
        RoleUtility.createSleepTxtFile(machine, tomcatRole, tasResolver);

        Map<String, String> agentAdditionalProps = new HashMap<>();
        IRole createSnippetRole = RoleUtility.createBaSnippetFile(machine, agentAdditionalProps, tasResolver);

        AgentRole agentRole =
            new AgentRole.Builder("agent-tomcat", tasResolver).webAppServer(tomcatRole)
                .platform(ArtifactPlatform.WINDOWS).emRole(emRole)
                .intrumentationLevel(AgentInstrumentationLevel.TYPICAL).disableWebAppAutoStart()
                .additionalProperties(agentAdditionalProps)
                .build();
        machine.addRole(agentRole);
        agentRole.after(createSnippetRole);

        TradeServiceAppRole tradeServiceAppRole =
            new TradeServiceAppRole.Builder("trade-service", tasResolver).tomcatRole(tomcatRole)
                .build();
        tradeServiceAppRole.after(agentRole);
        machine.addRole(tradeServiceAppRole);
        RoleUtility.addNewTradeServiceWars(machine, tomcatRole, tradeServiceAppRole, tasResolver);

        TIMAttendeeRole tsTimAttendeeRole =
            new TIMAttendeeRole.Builder("tsapp-tim-attendee", timRole, tasResolver).build();
        machine.addRole(tsTimAttendeeRole);
        return tradeServiceAppRole;
    }
    
    private void addCronRoles(ITestbedMachine machine, String tradeBaseUrl, IRole mathAppRole,
        ITasResolver tasResolver) {
        // every 4 hours downloads all links from TradeService
        String cronEntry =
            "* */4 * * * root wget -r --delete-after -nd " + tradeBaseUrl + "TradeService/ 2>/dev/null";
        machine.addRole(new CronEntryRole("cron_tradeservice", cronEntry));

        // every minute downloads login
        cronEntry =
            "* * * * * root wget --delete-after -nd " + tradeBaseUrl
                + "AuthenticationService/ServletA6 2>/dev/null";
        machine.addRole(new CronEntryRole("cron_login", cronEntry));

        // every even minute downloads TradeOptions 4 times
        String wgetStr =
            "wget --delete-after -nd " + tradeBaseUrl + "TradeService/TradeOptions 2>/dev/null";
        cronEntry =
            "*/2 * * * * root " + wgetStr + "\n" + "*/2 * * * * root sleep 15; " + wgetStr + "\n"
                + "*/2 * * * * root sleep 30; " + wgetStr + "\n" + "*/2 * * * * root sleep 45; "
                + wgetStr;
        machine.addRole(new CronEntryRole("cron_trade_options", cronEntry));

        Artifact testArtifact =
            new DefaultArtifact("com.ca.apm.test", "em-tests-core", "jar-with-dependencies", "jar",
                tasResolver.getDefaultVersion());
        String jarPath = machine.getAutomationBaseDir() + "em-tests-core.jar";
        machine.addRole(new GenericRole.Builder("test_jar", tasResolver).download(testArtifact,
            jarPath).build());
        cronEntry = "* * * * * root java -cp " + jarPath
                + " com.ca.apm.test.em.appmap.AppMapTopologySystemTest " + tradeBaseUrl;
        machine.addRole(new CronEntryRole("cron_chrome", cronEntry));

        String brtmBaseUrl = "http://" + tasResolver.getHostnameById(mathAppRole.getRoleId()) + ":8080/"
                + RoleUtility.BRTM_TEST_APP_CONTEXT;
        cronEntry = "* * * * * root java -cp " + jarPath
                + " com.ca.tas.dxc.test.DXCTest " + brtmBaseUrl;
        machine.addRole(new CronEntryRole("cron_chrome_brtm", cronEntry));
    }
    
    private void addElasticSearchProperties(EmRole.Builder builder, ITasResolver tasResolver) {
        if (esHost != null) {
            builder.configProperty("com.ca.apm.ttstore", "es");
            builder.configProperty("ca.apm.ttstore.elastic.url", "http://" + esHost + ":9200");
            builder.configProperty("ca.apm.ttstore.elastic.index.init", "true");
        }
    }

    private ITestbedMachine createMachine(String machineId) {
        return new TestbedMachine.Builder(machineId).platform(Platform.WINDOWS).templateId("w64")
            .bitness(Bitness.b64).build();
    }

    private ITestbedMachine createLinuxMachine(String machineId) {
        return new TestbedMachine.LinuxBuilder("gateway").platform(Platform.LINUX).templateId("co65")
            .bitness(Bitness.b64).build();
    }
}
