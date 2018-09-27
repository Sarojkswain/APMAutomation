package com.ca.apm.systemtest.fld.role;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.tests.artifact.Trade6Version;
import com.ca.apm.tests.role.OjdbcRole;

import com.ca.apm.tests.role.Websphere85Role;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @Author rsssa02
 */
public class WASDeployTrade6Role extends AbstractRole {
    public static Logger LOGGER = LoggerFactory.getLogger(WASDeployTrade6Role.class);
    private static final String ADMIN_INSTALL_BATCH = "wsadmin.bat";
    private ITasResolver tasResolver;
    private String clientHome;
    private String version;
    private String wasHome;
    private URL artifactUrl;
    private OjdbcRole ojdbcRole;
    private String ojdbcPath;
    private String resourcesScriptFileName;
    private String installScriptFileName;
    private String nodeName;
    private String wasRoleId;
    private String profileName;
    private String serverName;
    private String dbHost;
    private String dbPort;

    public void setApplicationEarFileName(String applicationEarFileName) {
        this.applicationEarFileName = applicationEarFileName;
    }

    public void setInstallScriptFileName(String installScriptFileName) {
        this.installScriptFileName = installScriptFileName;
    }

    public void setResourcesScriptFileName(String resourcesScriptFileName) {
        this.resourcesScriptFileName = resourcesScriptFileName;
    }

    public void setOjdbcPath(String ojdbcPath) {
        this.ojdbcPath = ojdbcPath;
    }

    public void setWasHome(String wasHome) {
        this.wasHome = wasHome;
    }

    private String applicationEarFileName;

    public WASDeployTrade6Role(Builder builder){
        super(builder.roleId, builder.getEnvProperties());
        this.tasResolver = builder.tasResolver;
        this.wasHome = builder.wasHome;
        this.ojdbcRole = builder.ojdbcRole;
        this.ojdbcPath = builder.ojdbcPath;
        this.resourcesScriptFileName = builder.resourcesScriptFileName;
        this.installScriptFileName = builder.installScriptFileName;
        this.applicationEarFileName = builder.applicationEarFileName;
        this.artifactUrl = builder.artifactUrl;
        this.nodeName = builder.nodeName;
        this.wasRoleId = builder.wasRoleId;
        this.profileName = builder.profileName;
        this.serverName = builder.serverName;
        this.dbHost = builder.dbHost;
        this.dbPort = builder.dbPort;
    }



    public String getWasHome() {
        return wasHome;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        commandLineStartStopWAS(aaClient, true);
        deployArtifacts(aaClient);
        updateResourceScriptFile(aaClient);
        installTrade6WAS(aaClient);
        commandLineStartStopWAS(aaClient, false);
        //restarting the server so as to keep the server alive.
        commandLineStartStopWAS(aaClient, true);
    }

    private void commandLineStartStopWAS(IAutomationAgentClient aaClient, boolean start) {
        String execCmd = null;
        if(start)
            execCmd = "startServer.bat";
        else
            execCmd = "stopServer.bat";

        RunCommandFlowContext context = new RunCommandFlowContext.Builder(execCmd)
                .workDir(this.wasHome + "/bin/")
                .args(Arrays.asList("server1"))
                .build();
        runCommandFlow(aaClient, context);
    }

    private void updateResourceScriptFile(IAutomationAgentClient aaClient) {
        String modifyFileName = this.wasHome + "\\bin\\" + this.installScriptFileName;
        String ojdbcPath = this.ojdbcRole.getDeploySourcesLocation()+ "\\" + this.ojdbcRole.getJarName();

            FileModifierFlowContext context;
            HashMap<String, String> propsModifiers = new HashMap<>();
            propsModifiers.put("\\[NODE_NAME\\]", tasResolver.getHostnameById(wasRoleId) + this.nodeName);
            propsModifiers.put("\\[SERVER_NAME\\]", this.serverName);
            propsModifiers.put("\\[DEFAULT_PROVIDER\\]", "Oracle");
            propsModifiers.put("\\[DB_NAME\\]", "tradedb");
            propsModifiers.put("\\[HOST_NAME\\]", this.dbHost);
            propsModifiers.put("\\[PORT\\]", this.dbPort);
            propsModifiers.put("\\[USER\\]", "TRADE");
            propsModifiers.put("\\[DEPLOY_TYPE\\]", "ORACLE_V10G");
            propsModifiers.put("\\[PASSWORD\\]", "TRADE");
            propsModifiers.put("\\[OJDBC_PATH\\]", pathResolver(ojdbcPath));

            context = new FileModifierFlowContext.Builder()
                    .replace(modifyFileName, propsModifiers)
                    .build();
            runFlow(aaClient, FileModifierFlow.class, context);
    }

    private void installTrade6WAS(IAutomationAgentClient aaClient) {
        String execFile = this.wasHome + "\\bin\\" + ADMIN_INSTALL_BATCH;
        List<String> args = new ArrayList<>();
        args.add("-f");
        args.add(this.installScriptFileName);
        args.add("all");

        RunCommandFlowContext runContext = new RunCommandFlowContext.Builder(execFile)
                .args(args)
                .workDir(this.wasHome + "\\bin")
                .doNotPrependWorkingDirectory()
                .build();

        runCommandFlow(aaClient, runContext);

    }

    private void deployArtifacts(IAutomationAgentClient aaClient) {

        GenericFlowContext context = new GenericFlowContext.Builder()
                .artifactUrl(this.artifactUrl)
                .destination(this.wasHome + "\\bin")
                .build();
        runFlow(aaClient, GenericFlow.class, context);
        //FlowBase.archiveFactory.createArchive(this.context.getDeployPackageUrl()).unpack(new File(this.context.getDeploySourcesLocation()));
    }

    private String pathResolver(String dirPath){
        String resolvedPath = null;
        if(null == dirPath){
            return dirPath;
        } else {
            resolvedPath = StringUtils.replace(dirPath, "\\", "/");
            resolvedPath = StringUtils.replace(resolvedPath, "\\\\", "/");
            //LOGGER.info("resolving path..! "+ dirPath + " - to - " + resolvedPath);
//            LOGGER.info("resolving path..! "+ dirPath );
        }
        return resolvedPath;
    }

    public static class Builder extends BuilderBase<Builder, WASDeployTrade6Role>{

        private static final Trade6Version DEFAULT_ARTIFACT;
        private final String roleId;
        private ITasResolver tasResolver;
        protected URL artifactUrl;
        protected Trade6Version version;

        public String resourcesScriptFileName;
        public String installScriptFileName;
        public String applicationEarFileName;
        public String wasHome;
        public String ojdbcPath;
        public OjdbcRole ojdbcRole;
        public String nodeName;
        public String wasRoleId;
        public String profileName;
        public String serverName;
        public String dbHost;
        public String dbPort;

        static {
            DEFAULT_ARTIFACT = Trade6Version.VER_6;
        }

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            this.version(DEFAULT_ARTIFACT);
        }

        @Override
        protected WASDeployTrade6Role getInstance() {
            return new WASDeployTrade6Role(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public WASDeployTrade6Role build() {
            initSetValues();
            initTrade6Artifact();
            return getInstance();
        }

        private void initTrade6Artifact() {
            this.artifactUrl = this.version.getArtifactUrl(this.tasResolver.getRegionalArtifactory());

        }

        private void initSetValues() {
            if(this.ojdbcPath == null)
                this.ojdbcPath = this.concatPaths(this.ojdbcRole.getDeploySourcesLocation() + this.ojdbcRole.getJarName());
        }


        public WASDeployTrade6Role.Builder wasHome(String homeDir){
            this.wasHome = homeDir;
            return builder();
        }

        public WASDeployTrade6Role.Builder version(Trade6Version version) {
            this.version = version;
            this.applicationEarFileName(version.getApplicationEar());
            this.installScriptFileName(version.getInstallScript());
            this.resourcesScriptFileName(version.getResourcesScript());
            return this.builder();
        }

        private void resourcesScriptFileName(String resourcesScript) {
            this.resourcesScriptFileName = resourcesScript;
        }

        private void installScriptFileName(String installScript) {
            this.installScriptFileName = installScript;
        }

        private void applicationEarFileName(String applicationEar) {
            this.applicationEarFileName = applicationEar;
        }

        public WASDeployTrade6Role.Builder ojdbcRole(OjdbcRole ojdbcRole){
            this.ojdbcRole = ojdbcRole;
            return builder();
        }
        public WASDeployTrade6Role.Builder wasRoleId(String roleId){
            this.wasRoleId = roleId;
            return builder();
        }
        public WASDeployTrade6Role.Builder dbHost(String dbHost){
            this.dbHost = dbHost;
            return builder();
        }
        public WASDeployTrade6Role.Builder dbPort(String dbPort){
            this.dbPort = dbPort;
            return builder();
        }
        public WASDeployTrade6Role.Builder nodeName(String nodeName){
            this.nodeName = nodeName;
            return builder();
        }
        public WASDeployTrade6Role.Builder profileName(String profileName){
            this.profileName = profileName;
            return builder();
        }
        public WASDeployTrade6Role.Builder serverName(String serverName){
            this.serverName = serverName;
            return builder();
        }
    }
}
