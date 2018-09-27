/**
 * 
 */
package com.ca.apm.systemtest.fld.role.loads;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.net.HostAndPort;
import org.apache.http.util.Args;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.StringUtils;

import com.ca.apm.automation.action.flow.agent.ApplicationServerType;
import com.ca.apm.automation.action.flow.agent.DeployAgentNoinstFlow;
import com.ca.apm.automation.action.flow.agent.DeployAgentNoinstFlowContext;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.automation.action.flow.webapp.weblogic.ConfigureWebLogicAgentFlow;
import com.ca.apm.automation.action.flow.webapp.weblogic.ConfigureWebLogicAgentFlowContext;
import com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.IThirdPartyArtifact;
import com.ca.tas.artifact.built.AgentNoInstaller;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.webapp.WebLogicRole;

/**
 * Requires a WebLogicRole.  Will download and install the requested domain template.  It will download
 * and configure an agent. It will automatically start the domain after installation.
 * 
 * @author keyja01
 *
 */
public class WebLogicDomainRole extends AbstractRole {

    private GenericFlowContext downloadDomainTemplateContext;
    private ConfigureWebLogicAgentFlowContext configureWebLogicContext;
    private FileModifierFlowContext configureEnvWebLogicContext;
    private FileModifierFlowContext configureBrowserAgentContext;
    private DeployAgentNoinstFlowContext deployAgentContext;
    private RunCommandFlowContext unpackDomainContext;
    private RunCommandFlowContext startDomainContext;
    private boolean autoStart;
    private boolean noInstallAgent;

    /**
     * @param roleId
     * @param envPropertyContainer
     */
    public WebLogicDomainRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.downloadDomainTemplateContext  = builder.downloadDomainTemplateContext;
        this.configureWebLogicContext = builder.configureWebLogicContext;
        this.configureEnvWebLogicContext = builder.configureEnvWebLogicContext;
        this.configureBrowserAgentContext = builder.configureBrowserAgentContext;
        this.deployAgentContext = builder.deployAgentContext;
        this.unpackDomainContext = builder.unpackDomainContext;
        this.startDomainContext = builder.startDomainContext;
        this.autoStart = builder.autoStart;
        this.noInstallAgent = builder.noInstallAgent;
    }

    
    public static class Builder extends BuilderBase<Builder, WebLogicDomainRole> {
        private String roleId;
        private ITasResolver resolver;
        private WebLogicRole wlsRole;
        private IThirdPartyArtifact domainTemplate;
        private String targetDir;
        private DeployAgentNoinstFlowContext deployAgentContext;
        private ConfigureWebLogicAgentFlowContext configureWebLogicContext;
        private FileModifierFlowContext configureBrowserAgentContext;
        private FileModifierFlowContext configureEnvWebLogicContext;
        private GenericFlowContext downloadDomainTemplateContext;
        private RunCommandFlowContext unpackDomainContext;
        private boolean autoStart = true;
        private RunCommandFlowContext startDomainContext;
        private EmRole emRole;
        private HostAndPort emHostAndPort;
        private String javaHomeDir = null;
        private String agentName = "WebLogic Agent";
        private String agentHostName = "fldweblogic";
        private String agentDecorator = "true";
        private String directivesFile = "weblogic-typical.pbl,hotdeploy,bizrecording.pbd,ServletHeaderDecorator.pbd,browseragent.pbd";
        private boolean noInstallAgent = false;
        private String version;

        public Builder(String roleId, ITasResolver resolver) {
            this.roleId = roleId;
            this.resolver = resolver;
        }
        
        
        public Builder addDomain(IThirdPartyArtifact domainTemplate, String targetDir) {
            Args.notNull(domainTemplate, "domainTemplate");
            Args.notNull(targetDir, "targetDir");
            this.domainTemplate = domainTemplate;
            this.targetDir = targetDir;
            
            return this;
        }
        
        
        /**
         * Override the hostname used by the agent
         * @param agentHostName
         * @return
         */
        public Builder agentHostName(String agentHostName) {
            this.agentHostName = agentHostName;
            return this;
        }

        public Builder webLogicRole(WebLogicRole wlsRole) {
            this.wlsRole = wlsRole;
            return this;
        }
        
        
        public Builder noInstallAgent(boolean noInstallAgent) {
            this.noInstallAgent = noInstallAgent;
            return this;
        }
        
        public Builder agentName(String agentName) {
            this.agentName = agentName;
            return this;
        }
        
        public Builder agentDecorator(String agentDecorator) {
            this.agentDecorator = agentDecorator;
            return this;
        }
        
        public Builder directivesFile(String directivesFile) {
            this.directivesFile = directivesFile;
            return this;
        }
        
        public Builder javaHomeDir(String javaHomeDir) {
            this.javaHomeDir = javaHomeDir;
            return this;
        }
        
        public Builder version(String version) {
            this.version = version;
            return this;
        }
        
        @Override
        public WebLogicDomainRole build() {
            initDownloadDomainTemplate();
            initUnpackDomainTemplate();
            initDeployAgent();
            initConfigureWebLogic();
            initConfigureEnvWebLogic();
            //initBrowserAgentToAgent();
            initStartDomain();
            
            WebLogicDomainRole role = getInstance();
            
            return role;
        }
        
        /**
         * Sets the domain to not automatically start
         * @return
         */
        public Builder noAutoStart() {
            this.autoStart = false;
            return this;
        }

        public Builder overrideEM(@NotNull HostAndPort hostAndPort) {
            this.emHostAndPort = hostAndPort;
            return this.builder();
        }


        public Builder emRole(EmRole emRole) {
            this.emRole = emRole;
            return this;
        }
        
        
        private void initConfigureWebLogic() {
            String wlsDomainDir = wlsRole.getInstallDir() + "\\user_projects\\" + targetDir; 
            String wilyDir = wlsDomainDir + "\\wily";
            String agentArgs = String.format("-javaagent:%s -Dcom.wily.introscope.agentProfile=%s", 
                wilyDir + "\\Agent.jar",
                wilyDir + "\\core\\config\\IntroscopeAgent.profile");
            ConfigureWebLogicAgentFlowContext.Builder builder = new ConfigureWebLogicAgentFlowContext.Builder()
                .javaAgentArgument(agentArgs)
                .oracleBaseDirectory("c:\\Oracle\\Middleware")
                .domainDirRelativePath("user_projects\\" + targetDir)
                .webLogicDirectory(wlsRole.getInstallDir());
            configureWebLogicContext = builder.build();
        }

        
        private void initConfigureEnvWebLogic() {
            String modifiedFile = wlsRole.getInstallDir() + "\\user_projects\\" + targetDir +"\\bin\\setDomainEnv.cmd";
            Collection<String> values = new ArrayList<String>();
            if (!StringUtils.isEmpty(javaHomeDir)) {
                values.add("set JAVA_HOME="+javaHomeDir);
            }
            FileModifierFlowContext modifEnv = new FileModifierFlowContext.Builder()
                .insertAt(modifiedFile , 56, values) //insert lines on the right place between 'set' and 'if'
                .build();
            
            configureEnvWebLogicContext = modifEnv;
        }
        
        
        private void initBrowserAgentToAgent() {
            
            String wlsDomainDir = wlsRole.getInstallDir() + "\\user_projects\\" + targetDir; 
            String wilyDir = wlsDomainDir + "\\wily";
            
            String fromFile = wilyDir + "\\examples\\APM\\BrowserAgent\\ext\\BrowserAgentExt.jar";
            String toFile = wilyDir + "\\core\\ext\\BrowserAgentExt.jar";

            
            FileModifierFlowContext copyEnv = new FileModifierFlowContext.Builder()
                .copy(fromFile, toFile)
                .build();
            
            configureBrowserAgentContext = copyEnv;
        }
        

        private void initDeployAgent() {
            String wilyDir = wlsRole.getInstallDir() + "\\user_projects\\" + targetDir;
            ITasArtifact agentInstaller = new AgentNoInstaller(AgentNoInstaller.Type.WEBLOGIC, ArtifactPlatform.WINDOWS, resolver).createArtifact(version);
            URL agentInstallerUrl = resolver.getArtifactUrl(agentInstaller);
            String emHost = "localhost";
            int emPort = 5001;
            if (emHostAndPort != null) {
                emHost = emHostAndPort.getHostText();
                emPort = emHostAndPort.getPort();
            } else if (emRole != null) {
                emHost = resolver.getHostnameById(emRole.getRoleId());
                emPort = emRole.getEmPort();
            }
            Map<String, String> additionalProperties = new HashMap<String, String>();
            if (!StringUtils.isEmpty(agentName)) {
                additionalProperties.put("introscope.agent.agentName", agentName);
            }
            if (!StringUtils.isEmpty(agentHostName)) {
                additionalProperties.put("introscope.agent.hostName", agentHostName);
            }
            if (!StringUtils.isEmpty(agentDecorator)) {
                additionalProperties.put("introscope.agent.decorator.enabled", agentDecorator);
            }
            if (!StringUtils.isEmpty(directivesFile)) {
                additionalProperties.put("introscope.autoprobe.directivesFile", directivesFile);
            }
            DeployAgentNoinstFlowContext.Builder builder = new DeployAgentNoinstFlowContext.Builder()
                .installDir(wilyDir)
                .installerUrl(agentInstallerUrl)
                .additionalProps(additionalProperties)
                .applicationServerType(ApplicationServerType.WEBLOGIC)
                .setupEm(emHost, emPort)
                ;
            deployAgentContext = builder.build();
        }


        private void initDownloadDomainTemplate() {
            URL url = resolver.getArtifactUrl(domainTemplate);
            String destination = wlsRole.getInstallDir() + "\\user_templates\\" + domainTemplate.getFilename();
            GenericFlowContext.Builder builder = new GenericFlowContext.Builder(url)
                .destination(destination).notArchive();
            downloadDomainTemplateContext = builder.build();
            
        }
        
        private void initUnpackDomainTemplate() {
            String template = wlsRole.getInstallDir() + "\\user_templates\\" + domainTemplate.getFilename();
            String domainDir = wlsRole.getInstallDir() + "\\user_projects\\" + targetDir;
            String unpackCommand = wlsRole.getInstallDir() + "\\common\\bin\\unpack.cmd";
            Collection<String> args = new ArrayList<>(Arrays.asList(
                "/C", unpackCommand, "-template", template, "-domain", domainDir, "-server_start_mode", "dev" ));
            
            RunCommandFlowContext.Builder builder = new RunCommandFlowContext.Builder("cmd.exe")
                .args(args)
                .doNotPrependWorkingDirectory()
                ;
            unpackDomainContext = builder.build();
        }
        
        
        private void initStartDomain() {
            String domainDir = wlsRole.getInstallDir() + "\\user_projects\\" + targetDir;
            Collection<String> args = new ArrayList<>(Arrays.asList("/C", "startWebLogic.cmd"));
            startDomainContext = new RunCommandFlowContext.Builder("cmd.exe")
                .args(args)
                .workDir(domainDir)
                .doNotPrependWorkingDirectory()
                .terminateOnMatch("Server started in RUNNING mode")
                .build();
        }
        

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected WebLogicDomainRole getInstance() {
            return new WebLogicDomainRole(this);
        }
        
    }
    
    /* (non-Javadoc)
     * @see com.ca.tas.role.Deployable#deploy(com.ca.tas.client.IAutomationAgentClient)
     */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        runFlow(aaClient, GenericFlow.class, downloadDomainTemplateContext);
        runCommandFlow(aaClient, unpackDomainContext);
        if (!noInstallAgent) {
            runFlow(aaClient, DeployAgentNoinstFlow.class, deployAgentContext);
            runFlow(aaClient, ConfigureWebLogicAgentFlow.class, configureWebLogicContext);
            runFlow(aaClient, FileModifierFlow.class, configureEnvWebLogicContext);
            //runFlow(aaClient, FileModifierFlow.class, configureBrowserAgentContext);
        }
        if (autoStart) {
            runCommandFlowAsync(aaClient, startDomainContext);
        }
        System.out.println("Done!");
    }
}
