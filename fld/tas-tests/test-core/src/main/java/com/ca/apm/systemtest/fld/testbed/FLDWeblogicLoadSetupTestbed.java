package com.ca.apm.systemtest.fld.testbed;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.ca.apm.automation.action.flow.agent.DeployAgentNoinstFlow;
import com.ca.apm.automation.action.flow.agent.DeployAgentNoinstFlowContext;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.webapp.weblogic.ConfigureWebLogicAgentFlow;
import com.ca.apm.automation.action.flow.webapp.weblogic.ConfigureWebLogicAgentFlowContext;
import com.ca.apm.systemtest.fld.artifact.WurlitzerWebAppArtifact;
import com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.built.AgentNoInstaller;
import com.ca.tas.artifact.thirdParty.WebLogicVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.WebLogicRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * TAS testbed class which deploys two Weblogic Administration Servers in two different domains called "wurlitzer1" and "wurlitzer2", 
 * as well as a Wurlitzer-noejb WAR web application into each of the domains, instrumented by Introscope agent each of them.
 * 
 * Used to setup a new FLD environment. 
 * 
 * 
 * <h3>URLs to check after deployment (example)</h3>
 * 
 * <p> 
 * E.g.for host tas-cz-na4 (130.119.79.57):
 * 
 * :7001 (wurlitzer1)
 *  + WebServiceBean1Service
 *    - http://130.119.79.57:7001/wurlitzer1/WebServiceBean1Service?WSDL
 *    - http://130.119.79.57:7001/wls_utc/?wsdlUrl=http%3A%2F%2F130.119.79.57%3A7001%2Fwurlitzer1%2FWebServiceBean1Service%3FWSDL
 *  + WebServiceBean2Service
 *    - http://130.119.79.57:7001/wurlitzer1/WebServiceBean2Service?WSDL
 *    - http://130.119.79.57:7001/wls_utc/?wsdlUrl=http%3A%2F%2F130.119.79.57%3A7001%2Fwurlitzer1%2FWebServiceBean2Service%3FWSDL
 *  
 *  
 * :7002 (wurlitzer2)
 *  + WebServiceBean1Service 
 *    - http://130.119.79.57:7002/wurlitzer2/WebServiceBean1Service?WSDL
 *    - http://130.119.79.57:7002/wls_utc/?wsdlUrl=http%3A%2F%2F130.119.79.57%3A7002%2Fwurlitzer2%2FWebServiceBean1Service%3FWSDL
 *  
 *  + WebServiceBean2Service
 *    - http://130.119.79.57:7002/wurlitzer2/WebServiceBean2Service?WSDL
 *    - http://130.119.79.57:7002/wls_utc/?wsdlUrl=http%3A%2F%2F130.119.79.57%3A7002%2Fwurlitzer2%2FWebServiceBean2Service%3FWSDL
 *  
 * </p>
 *  
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
@TestBedDefinition
public class FLDWeblogicLoadSetupTestbed implements ITestbedFactory {

    public static final String MACHINE_ID = "fldwls";
    public static final String WLS_ROLE_ID = MACHINE_ID + "_weblogic_server_role";
    public static final String AGENT_ARG_TEMPLATE = "-javaagent:%s -Dcom.wily.introscope.agentProfile=%s";
    public static final String WLS_LAUNCH_EXECUTION_TERMINATE_READINESS_FLOW_TOKEN = "Server started in RUNNING mode";
    
    public static final int WLS_INSTANCE1_PORT = 7001;
    public static final int WLS_INSTANCE2_PORT = 7002;
    
    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbedMachine wlsMachine1 =
            new TestbedMachine.Builder(MACHINE_ID).templateId(ITestbedMachine.TEMPLATE_W64).build();

        WebLogicRole wlsRole =
            new WebLogicRole.Builder(WLS_ROLE_ID, tasResolver)
                .installLocation("C:/sw/oracle/middleware")
                .installLogFile("C:/sw/oracle/install.log")
                .version(WebLogicVersion.v103x86w)
                .responseFileDir("C:/sw/oracle/responseFiles")
                .installDir("C:/sw/oracle/middleware/wlserver_10.3")
                .build();
        
        AbstractRole createWurlitzer1DomainRole = createWlsInstanceForDomainCreationRole("wurlitzer1", WLS_INSTANCE1_PORT);
        createWurlitzer1DomainRole.after(wlsRole);
        
        AbstractRole createWurlitzer2DomainRole = createWlsInstanceForDomainCreationRole("wurlitzer2", WLS_INSTANCE2_PORT);
        createWurlitzer2DomainRole.after(createWurlitzer1DomainRole);

        WurlitzerWebAppArtifact wurlitzer = new WurlitzerWebAppArtifact(tasResolver);
        UniversalRole wurlitzer1WarDownloadRole = new UniversalRole.Builder("wurlitzer1_download_role", tasResolver).
            download(wurlitzer.createArtifact().getArtifact(), 
                "C:/sw/oracle/middleware/user_projects/domains/wurlitzer1/autodeploy/wurlitzer1.war").
            build();
        wurlitzer1WarDownloadRole.after(createWurlitzer2DomainRole);

        
        UniversalRole wurlitzer2WarDownloadRole = new UniversalRole.Builder("wurlitzer2_download_role", tasResolver).
            download(wurlitzer.createArtifact().getArtifact(), 
                "C:/sw/oracle/middleware/user_projects/domains/wurlitzer2/autodeploy/wurlitzer2.war").
            build();
        wurlitzer2WarDownloadRole.after(wurlitzer1WarDownloadRole);

        
        //Deploy Introscope Agents into created wurlitzer app domains

        ITasArtifact introscopeAgentArtifact = new AgentNoInstaller(AgentNoInstaller.Type.WEBLOGIC, 
            ArtifactPlatform.WINDOWS, tasResolver).createArtifact();
        URL introscopeAgentURL = tasResolver.getArtifactUrl(introscopeAgentArtifact);


        DeployAgentNoinstFlowContext deployAgent1FlowContext = new DeployAgentNoinstFlowContext.Builder().
            installDir("C:/sw/oracle/middleware/user_projects/domains/wurlitzer1").
            installerUrl(introscopeAgentURL).
            additionalProps(getAgentAdditionalProps("wurlitzerWls1Agent")).
            build();
        
        DeployAgentNoinstFlowContext deployAgent2FlowContext = new DeployAgentNoinstFlowContext.Builder().
            installDir("C:/sw/oracle/middleware/user_projects/domains/wurlitzer2").
            installerUrl(introscopeAgentURL).
            additionalProps(getAgentAdditionalProps("wurlitzerWls2Agent")).            
            build();
        
        ExecutionRole deployAgentsRole = new ExecutionRole.Builder("deploy_introscope_agents_role").
            flow(DeployAgentNoinstFlow.class, deployAgent1FlowContext).
            flow(DeployAgentNoinstFlow.class, deployAgent2FlowContext).
            build();

        deployAgentsRole.after(wurlitzer2WarDownloadRole);
        
        
        //Configure deployed Introscope Agents
        String agent1Path = "C:/sw/oracle/middleware/user_projects/domains/wurlitzer1/wily/Agent.jar";
        String agent1ProfilePath = "C:/sw/oracle/middleware/user_projects/domains/wurlitzer1/wily/core/config/IntroscopeAgent.profile";
        String agent1Arg = String.format(AGENT_ARG_TEMPLATE, agent1Path, agent1ProfilePath);
        
        String agent2Path = "C:/sw/oracle/middleware/user_projects/domains/wurlitzer2/wily/Agent.jar";
        String agent2ProfilePath = "C:/sw/oracle/middleware/user_projects/domains/wurlitzer2/wily/core/config/IntroscopeAgent.profile";
        String agent2Arg = String.format(AGENT_ARG_TEMPLATE, agent2Path, agent2ProfilePath);
        
        ConfigureWebLogicAgentFlowContext instrumentWls1WithAgentFlowContext = new ConfigureWebLogicAgentFlowContext.Builder().
            oracleBaseDirectory("C:/sw/oracle/middleware").
            webLogicDirectory("C:/sw/oracle/middleware/user_projects").
            domainDirRelativePath("domains/wurlitzer1").
            javaAgentArgument(agent1Arg).
            build();

        ConfigureWebLogicAgentFlowContext instrumentWls2WithAgentFlowContext = new ConfigureWebLogicAgentFlowContext.Builder().
            oracleBaseDirectory("C:/sw/oracle/middleware").
            webLogicDirectory("C:/sw/oracle/middleware/user_projects").
            domainDirRelativePath("domains/wurlitzer2").
            javaAgentArgument(agent2Arg).
            build();

        ExecutionRole instrumentWlsInstancesWithIntroscopeAgentsRole = new ExecutionRole.Builder("instrument_wls_instances_with_introscope_agents_role").
            flow(ConfigureWebLogicAgentFlow.class, instrumentWls1WithAgentFlowContext).
            flow(ConfigureWebLogicAgentFlow.class, instrumentWls2WithAgentFlowContext).
            build();
        instrumentWlsInstancesWithIntroscopeAgentsRole.after(deployAgentsRole);
        
        //Finally run Weblogic instances with Wurlitzer Apps installed
        AbstractRole runWlsInstancesRole = createRunWlsInstancesRole("wurlitzer1", "wurlitzer2");
        runWlsInstancesRole.after(instrumentWlsInstancesWithIntroscopeAgentsRole);

        
        wlsMachine1.addRole(wlsRole, createWurlitzer1DomainRole, createWurlitzer2DomainRole, wurlitzer1WarDownloadRole, 
            wurlitzer2WarDownloadRole, deployAgentsRole, instrumentWlsInstancesWithIntroscopeAgentsRole, runWlsInstancesRole); 

        ITestbed testbed = new Testbed("FLDWeblogicLoadSetupTestbed");
        testbed.addMachine(wlsMachine1);

        return testbed;
    }

    private AbstractRole createWlsInstanceForDomainCreationRole(String domainName, int port) {
        String responseFile = "C:/sw/oracle/create_" + domainName + "_domain.rsp";
        Collection<String> setupDomainWithWLSTScriptRows = createWlsAdminServerInDomain(domainName, port); 
        FileModifierFlowContext createWlsDomainFileCreateFlowContext = new FileModifierFlowContext.Builder().
            create(responseFile, setupDomainWithWLSTScriptRows).build();
        
        ExecutionRole executionRole = new ExecutionRole.Builder("execute_create_" + domainName + "_domain_role").
            flow(FileModifierFlow.class, createWlsDomainFileCreateFlowContext).
            syncCommand(new RunCommandFlowContext.Builder("C:/sw/oracle/middleware/wlserver_10.3/common/bin/config.cmd").
                args(Arrays.asList("-mode=silent", 
                    "-silent_script=" + responseFile, 
                    "-logfile=C:/sw/oracle/middleware/create_" + domainName + "_domain.log")).build()).build();
        return executionRole;
    }
    
    private Collection<String> createWlsAdminServerInDomain(String domainName, int port) {
        Collection<String> setupDomainRows = new ArrayList<String>();
        setupDomainRows.add("read template from \"C:/sw/oracle/middleware/wlserver_10.3/common/templates/domains/wls.jar\";");

        
        setupDomainRows.add("set ServerStartMode \"dev\";");
        setupDomainRows.add("find Server \"AdminServer\" as AdminServer;");
        setupDomainRows.add("set AdminServer.ListenAddress \"\";");
        setupDomainRows.add("set AdminServer.ListenPort \"" + port + "\";");
        
        //setup user
        setupDomainRows.add("find User \"weblogic\" as defaultUser;");
        setupDomainRows.add("set defaultUser.password \"weblogic\";");

        //write & close domain
        setupDomainRows.add("write domain to \"C:/sw/oracle/middleware/user_projects/domains/" + domainName + "\";");
        setupDomainRows.add("close template;");
        return setupDomainRows;
    }

    private AbstractRole createRunWlsInstancesRole(String wlsInstName1, String wlsInstName2) {
        String runCmd1 = "C:/sw/oracle/middleware/user_projects/domains/" + wlsInstName1 + "/bin/startWebLogic.cmd";
        String runCmd2 = "C:/sw/oracle/middleware/user_projects/domains/" + wlsInstName2 + "/bin/startWebLogic.cmd";
        ExecutionRole runWlsInstanceExecutionRole = new ExecutionRole.Builder("execute_run_wls_instances_role").
            asyncCommand(new RunCommandFlowContext.Builder(runCmd1).terminateOnMatch(WLS_LAUNCH_EXECUTION_TERMINATE_READINESS_FLOW_TOKEN).build()).
            asyncCommand(new RunCommandFlowContext.Builder(runCmd2).terminateOnMatch(WLS_LAUNCH_EXECUTION_TERMINATE_READINESS_FLOW_TOKEN).build()).
            build();
        return runWlsInstanceExecutionRole;
    }
    
    private Map<String, String> getAgentAdditionalProps(String customAgentName) {
        Map<String, String> additionalProperties = new HashMap<>();
        additionalProperties.put("introscope.agent.customProcessName", customAgentName);
        additionalProperties.put("introscope.agent.agentName", customAgentName);
        return additionalProperties;
    }
    
}
