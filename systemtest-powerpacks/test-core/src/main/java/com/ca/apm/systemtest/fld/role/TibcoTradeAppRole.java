package com.ca.apm.systemtest.fld.role;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.util.Args;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.tests.tibco.artifact.TibcoSoftwareComponentVersions;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * @Author rsssa02
 */
public class TibcoTradeAppRole extends AbstractRole {

    private static final Logger LOGGER = LoggerFactory.getLogger(TibcoTradeAppRole.class);

    public static final String TIB_START = "tibTradeStart";
    public static final String TIB_STOP  = "tibTradeStop";
    public static final String EMS_START  = "tibEMSStart";
    public static final String EMS_STOP  = "tibEMSStop";

    private ITasResolver tasResolver;
    private static final String TIBTRADE_APP_DEPLOY_FILE = "Tibco_Trade6_v12.ear.xml";
    public static final String TIBTRADE_EAR_FILENAME = "Tibco_Trade6_v12.ear";
    private String clientDir;
    public String traVersion;
    private String tibcoHomeDir;
    private String emsVersion;
    private String bwVersion;
    @SuppressWarnings("unused")
    private String tibEMSConfigDir;
    private String tibcoDomainName;
    private String dbRoleId;
    private String wasRoleId;
    private String emsRoleId;
    private String bwRoleId;
    private String agentInstallDir;
    private boolean setupAgent = false;
    public RunCommandFlowContext startCommandFlowContext;
    public RunCommandFlowContext stopCommandFlowContext;
    public RunCommandFlowContext startEMSCmdFlowContext;
    public RunCommandFlowContext stopEMSCmdFlowContext;

    public String getClientDir() {
        return clientDir;
    }

    public void setClientDir(String clientDir) {
        this.clientDir = clientDir;
    }

    public TibcoTradeAppRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.tasResolver = builder.tasResolver;
        this.clientDir = builder.clientDir;
        this.tibcoHomeDir = builder.tibcoHomeDir;
        this.traVersion = builder.traVersion;
        this.stopCommandFlowContext = builder.stopCommandFlowContext;
        this.startCommandFlowContext = builder.startCommandFlowContext;
        this.startEMSCmdFlowContext = builder.startEMSCmdFlowContext;
        this.stopEMSCmdFlowContext = builder.stopEMSCmdFlowContext;
        this.dbRoleId = builder.dbRoleId;
        this.bwRoleId = builder.bwRoleId;
        this.bwVersion = builder.bwVersion;
        this.emsVersion = builder.emsVersion;
        this.tibEMSConfigDir = builder.tibEMSConfigDir;
        this.wasRoleId = builder.wasRoleId;
        this.emsRoleId = builder.emsRoleId;
        this.setupAgent = builder.setupAgent;
        this.agentInstallDir = builder.agentInstallDir;
        this.tibcoDomainName = builder.tibcoDomainName;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
            startTibEMScmd(aaClient);
            deployTradeJarFixZip(aaClient, "com.ca.apm.binaries.tibco", "tibco-tradeapp-fix", "5.11.1-fixed", this.tibcoHomeDir);
            deployTradeAppZip(aaClient, "com.ca.apm.coda", "tibco-trade", "6.0", this.tibcoHomeDir);
            updateTradeAppProps(aaClient, this.tibcoHomeDir);
            installTibcoTradeApp(aaClient, this.tibcoHomeDir);
            updateAgentParameters(aaClient, agentInstallDir, setupAgent);
    }

    private void updateAgentParameters(IAutomationAgentClient aaClient, String agentInstallDir, boolean setupAgent) {

        FileModifierFlowContext context = null;
        List<String> agentArgs = new ArrayList<>();
        if(setupAgent) {
            String agentParams = "-javaagent:" + agentInstallDir.replaceAll("\\\\", "/") + "/wily/Agent.jar -Dcom.wily.introscope.agentProfile=" + agentInstallDir.replaceAll("\\\\", "/") + "/wily/core/config/IntroscopeAgent.profile";
                agentArgs.add("java.extended.properties=" + agentParams);
        }
        HashMap<String, String> mapArgs = new HashMap<>();
        //update needed properties in the app tra file
        String fileName = this.tibcoHomeDir + "\\tra\\domain\\" + this.tibcoDomainName + "\\application\\Tibco_Trade6\\Tibco_Trade6-Process_Archive.tra";
        agentArgs.add("bw.plugin.http.server.minProcessors=500");   // to change the threadpool of server
        agentArgs.add("bw.plugin.http.server.maxProcessors=500");
        // update heap sizes after deploying the server.
        mapArgs.put("java.heap.size.max=256M", "java.heap.size.max=512M");

        context = new FileModifierFlowContext.Builder()
                .append(fileName, agentArgs)
                .replace(fileName,mapArgs)
                .build();
        runFlow(aaClient, FileModifierFlow.class, context);
    }

    @SuppressWarnings("unused")
    private RunCommandFlowContext startTrade6App() {
        String tibBWBinDir = this.tibcoHomeDir + "\\tra\\" + this.traVersion + "\\bin";
        RunCommandFlowContext context;
        List<String> stopArgs = new ArrayList<>();
        stopArgs.add("-app");
        stopArgs.add("Tibco_Trade6");
        stopArgs.add("-domain");
        stopArgs.add(this.tibcoDomainName);
        stopArgs.add("-user");
        stopArgs.add("admin");
        stopArgs.add("-pw");
        stopArgs.add("admin");
        stopArgs.add("-start");
        context = new RunCommandFlowContext.Builder(tibBWBinDir + "\\AppManage.exe")
                .args(stopArgs)
                .workDir(tibBWBinDir)
                .doNotPrependWorkingDirectory()
                .build();
        return context;
    }

    private void startTibEMScmd(IAutomationAgentClient aaClient) {
        RunCommandFlowContext context = new RunCommandFlowContext.Builder(this.tibcoHomeDir + "/ems/"+ this.emsVersion +"/bin/tibemsd")
                .workDir(this.tibcoHomeDir + "/ems/"+ this.emsVersion +"/bin/")
                .terminateOnMatch("Server is active")
                .doNotPrependWorkingDirectory()
                .build();
        runCommandFlow(aaClient, context);
    }

    @SuppressWarnings("unused")
    private RunCommandFlowContext stopTrade6App(IAutomationAgentClient aaClient) {
        String tibBWBinDir = this.tibcoHomeDir + "\\tra\\" + this.traVersion + "\\bin";
        RunCommandFlowContext context;
        List<String> stopArgs = new ArrayList<>();
        stopArgs.add("-app");
        stopArgs.add("Tibco_Trade6");
        stopArgs.add("-domain");
        stopArgs.add(this.tibcoDomainName);
        stopArgs.add("-user");
        stopArgs.add("admin");
        stopArgs.add("-pw");
        stopArgs.add("admin");
        stopArgs.add("-stop");

        context = new RunCommandFlowContext.Builder(tibBWBinDir + "\\AppManage.exe")
                .args(stopArgs)
                .workDir(tibBWBinDir)
                .doNotPrependWorkingDirectory()
                .build();
        runCommandFlow(aaClient, context);
        return context;
    }

    private void installTibcoTradeApp(IAutomationAgentClient aaClient, String location) {
        try {
            Thread.sleep(30000);
            String tibBWBinDir = this.tibcoHomeDir + "\\tra\\" + this.traVersion + "\\bin";
            RunCommandFlowContext context;
            List<String> args = new ArrayList<>();
            args.add("-deploy");
            args.add("-ear");
            args.add(TIBTRADE_EAR_FILENAME);
            args.add("-deployConfig");
            args.add(TIBTRADE_APP_DEPLOY_FILE);
            args.add("-app");
            args.add("Tibco_Trade6");
            args.add("-domain");
            args.add(this.tibcoDomainName);
            args.add("-user");
            args.add("admin"); // the testbed is being installed with default user and pwd
            args.add("-pw");
            args.add("admin");

            context = new RunCommandFlowContext.Builder(tibBWBinDir + "\\AppManage.exe")
                    .args(args)
                    .workDir(tibBWBinDir)
                    .doNotPrependWorkingDirectory()
                    .build();
            runCommandFlow(aaClient, context);
        }
        catch (InterruptedException e){
            LOGGER.info("Exception while executing" + e);
        }
    }

    private void updateTradeAppProps(IAutomationAgentClient aaClient, String dir) {
        FileModifierFlowContext context = null;
        HashMap<String, String> replacePairs = new HashMap<>();
        String fileName = dir + "/tra/" + this.traVersion + "/bin/" + TIBTRADE_APP_DEPLOY_FILE;

        replacePairs.put("\\[BOOK_PATH\\]", pathResolver(dir + "/tra/" + this.traVersion + "/bin/BookStore.xml"));
        replacePairs.put("\\[DB_HOST\\]", tasResolver.getHostnameById(this.dbRoleId));
        replacePairs.put("\\[DB_PASSWORD\\]", "TRADE");
        replacePairs.put("\\[DB_SID\\]", "tradedb");
        replacePairs.put("\\[DB_USER\\]", "TRADE");
        replacePairs.put("\\[EMS_HOST\\]", tasResolver.getHostnameById(this.emsRoleId));
        replacePairs.put("\\[EP1_URL\\]", tasResolver.getHostnameById(this.wasRoleId) + ":9080");
        replacePairs.put("\\[FILE_PATH\\]", pathResolver("c:/sw/temp"));
        replacePairs.put("\\[BW_HOST\\]", tasResolver.getHostnameById(this.bwRoleId));
        replacePairs.put("\\[BW_VERSION\\]", this.bwVersion);
        replacePairs.put("\\[BW_PATH\\]", pathResolver(this.tibcoHomeDir + "/bw/" + this.bwVersion));

        context = new FileModifierFlowContext.Builder()
                .replace(fileName, replacePairs)
                .build();
        runFlow(aaClient, FileModifierFlow.class, context);
    }

    private void deployTradeJarFixZip(IAutomationAgentClient aaClient, String groupId, String artifactId, String version, String dir) {
        String tplcDir = dir + "/tpcl/" + this.traVersion + "/jdbc/";
        URL url = tasResolver.getArtifactUrl(new DefaultArtifact(groupId, artifactId, "zip", version));

        GenericFlowContext context = new GenericFlowContext.Builder()
                .artifactUrl(url)
                .destination(tplcDir)
                .build();
        runFlow(aaClient, GenericFlow.class, context);
    }

    private void deployTradeAppZip(IAutomationAgentClient aaClient, String groupId, String artifactId, String version, String location) {
        URL url = tasResolver.getArtifactUrl(new DefaultArtifact(groupId, artifactId, "zip", version));

        GenericFlowContext context = new GenericFlowContext.Builder()
                .artifactUrl(url)
                .destination(location + "/tra/" + this.traVersion + "/bin")
                .build();
        runFlow(aaClient, GenericFlow.class, context);
    }

    private String pathResolver(String dirPath){
        String resolvedPath = null;
        if(null == dirPath){
            return dirPath;
        } else {
            resolvedPath = StringUtils.replace(dirPath, "/", "\\");
            resolvedPath = StringUtils.replace(resolvedPath, "\\", "\\\\");
        }
        return resolvedPath;
    }

    public static class Builder extends BuilderBase<Builder, TibcoTradeAppRole>{
        private final String roleId;
        private final ITasResolver tasResolver;
        public String clientDir;
        public String tibcoHomeDir;
        public String traVersion;
        public String dbRoleId;
        public String bwRoleId;
        public String bwVersion;
        public String emsVersion;
        public String emsRoleId;
        public String wasRoleId;
        public String tibEMSConfigDir;
        public String tibcoDomainName;
        public boolean setupAgent;
        protected RunCommandFlowContext startCommandFlowContext;
        protected RunCommandFlowContext stopCommandFlowContext;
        protected RunCommandFlowContext startEMSCmdFlowContext;
        protected RunCommandFlowContext stopEMSCmdFlowContext;
        public String agentInstallDir;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }
        @Override
        protected TibcoTradeAppRole getInstance() {
            return new TibcoTradeAppRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public TibcoTradeAppRole build() {
            initClientLocation();
            initStartStopCmd();
            Args.notNull(tibcoHomeDir, "TPCL directory location");
            Args.notNull(dbRoleId, "dbrole ID for configuring app");
            Args.notNull(wasRoleId, "websphere role id");
            Args.notNull(tibEMSConfigDir, "tibco ems config dir");
            if(setupAgent){
                Args.notNull(agentInstallDir, "tibco pp agent install location");
            }
            return getInstance();
        }

        private void initStartStopCmd() {
            initDefaultVersionString();
            String tibBinLoc = this.tibcoHomeDir + "\\tra\\" + this.traVersion + "\\bin\\";
            String tibBWbinLoc = this.tibcoHomeDir + "\\bw\\" + this.bwVersion + "\\bin\\";
            String emsLoc = this.tibcoHomeDir + "/ems/"+ this.emsVersion +"/bin/";

            this.startCommandFlowContext = (new RunCommandFlowContext.Builder("bwengine.exe").args(constructTibStartArgs()).workDir(tibBWbinLoc).terminateOnMatch("Engine Tibco_Trade6-Process_Archive started").build());
            this.stopCommandFlowContext = (new RunCommandFlowContext.Builder("AppManage.exe").args(constructTibStopArgs("stop")).workDir(tibBinLoc).build());
            this.startEMSCmdFlowContext = (new RunCommandFlowContext.Builder("tibemsd").args(constructEmsStartArgs()).workDir(emsLoc).terminateOnMatch("Server is active").build());
            this.stopEMSCmdFlowContext = (new RunCommandFlowContext.Builder("taskkill").args(constructEmsStopArgs()).workDir("c:/Windows/System32/").build());

            this.getEnvProperties().add(TIB_START, this.startCommandFlowContext);
            this.getEnvProperties().add(TIB_STOP, this.stopCommandFlowContext);
            this.getEnvProperties().add(EMS_START, this.startEMSCmdFlowContext);
            this.getEnvProperties().add(EMS_STOP, this.stopEMSCmdFlowContext);
        }

        private Collection<String> constructEmsStopArgs() {
            List<String> args = new ArrayList<>();
            args.add("/F");
            args.add("/FI");
            args.add("\"IMAGENAME eq tibemsd.exe\"");
            return args;
        }

        public Collection<String> constructTibStopArgs(String cmd){
            List<String> stopArgs = new ArrayList<>();
            stopArgs.add("-app");
            stopArgs.add("Tibco_Trade6");
            stopArgs.add("-domain");
            stopArgs.add(this.tibcoDomainName);
            stopArgs.add("-user");
            stopArgs.add("admin");
            stopArgs.add("-pw");
            stopArgs.add("admin");
            stopArgs.add("-" + cmd);
            return stopArgs;
        }

        public Collection<String> constructTibStartArgs(){
            List<String> args = new ArrayList<>();
            args.add("--pid");
            args.add("--run");
            args.add("--propFile");
            args.add(this.tibcoHomeDir + "\\tra\\domain\\" + this.tibcoDomainName + "\\application\\Tibco_Trade6\\Tibco_Trade6-Process_Archive.tra");
            //C:/tibco/bw/5.8//bin/bwengine.exe, --pid, --run, --propFile, C:/tibco/tra/domain/APM/application/Tibco_Trade6//Tibco_Trade6-Process_Archive.tra
            return args;
        }

        public Collection<String> constructEmsStartArgs(){
            List<String> stopArgs = new ArrayList<>();
            stopArgs.add("-config");
            stopArgs.add(this.tibEMSConfigDir + "\\tibco\\cfgmgmt\\ems\\data\\tibemsd.conf");
            return stopArgs;
        }

        private void initDefaultVersionString() {
            if(this.bwVersion == null)
                this.bwVersion(TibcoSoftwareComponentVersions.TibcoBWWindowsx64v5_11_0.getVersion());
            if(this.traVersion == null)
                this.traVersion(TibcoSoftwareComponentVersions.TibcoTRAWindowsx64v5_8_0.getVersion());
            if(this.emsVersion == null)
                this.emsVersion(TibcoSoftwareComponentVersions.TibcoEMSWindowsx64v6_3_0.getVersion());
        }

        private void initClientLocation() {
            this.clientDir(TasBuilder.WIN_SOFTWARE_LOC + "/webapp/tibco/");
        }

        public TibcoTradeAppRole.Builder bwVersion(String bwVersion){
            this.bwVersion = bwVersion;
            return builder();
        }

        public TibcoTradeAppRole.Builder emsVersion(String emsVers){
            this.emsVersion = emsVers;
            return builder();
        }

        public TibcoTradeAppRole.Builder dbRole(String roleId){
            this.dbRoleId = roleId;
            return builder();
        }
        public TibcoTradeAppRole.Builder tibcoDomainName(String tibcoDomainName){
            this.tibcoDomainName = tibcoDomainName;
            return builder();
        }

        public TibcoTradeAppRole.Builder bwRole(String roleId){
            this.bwRoleId = roleId;
            return builder();
        }

        public TibcoTradeAppRole.Builder agentInstallDir(String agentInstalLoc){
            this.agentInstallDir = agentInstalLoc;
            return builder();
        }

        public TibcoTradeAppRole.Builder setupAgent(boolean setupAgentFlag){
            this.setupAgent = setupAgentFlag;
            return builder();
        }

        public TibcoTradeAppRole.Builder tibEMSConfigDir(String tibEMSConfigDir){
            this.tibEMSConfigDir = tibEMSConfigDir;
            return builder();
        }

        public TibcoTradeAppRole.Builder emsRole(String emsRoleId){
            this.emsRoleId = emsRoleId;
            return builder();
        }

        public TibcoTradeAppRole.Builder traVersion(String ver){
            this.traVersion = ver;
            return builder();
        }
        public TibcoTradeAppRole.Builder wasRole(String wasRole){
            this.wasRoleId = wasRole;
            return builder();
        }

        public TibcoTradeAppRole.Builder clientDir(String loc){
            this.clientDir = loc;
            return builder();
        }

        public TibcoTradeAppRole.Builder tibcoHomeDir(String dir){
            this.tibcoHomeDir = dir;
            return builder();
        }
    }

}
