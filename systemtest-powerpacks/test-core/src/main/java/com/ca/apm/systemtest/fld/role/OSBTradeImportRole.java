package com.ca.apm.systemtest.fld.role;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.systemtest.fld.flow.OrclSrvcBusFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.apache.http.util.Args;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.testng.IAnnotationTransformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author rsssa02
 */
public class OSBTradeImportRole extends AbstractRole {

    protected OrclSrvcBusFlowContext flowContext;
    public static final String DEPLOY_HOME = "C:/automation/deployed";
    public static final String TRADE_SOURCE_DOWNLOAD = DEPLOY_HOME + "/tradeapp";
    public static final String OSB_TRADE_RESOURCE_NAME = "osbtrade.jar";

    private String javaHome;
    private String wlsRoleID;
    private String wlsPort;

    public String getWlsServerHome() {
        return wlsServerHome;
    }

    public String getOsbHome() {
        return osbHome;
    }

    public String getJavaHome(){
        return javaHome;
    }

    public String getWlsRoleID(){
        return wlsRoleID;
    }

    public String getWlsPort(){
        return wlsPort;
    }

    private String wlsServerHome;
    private String osbHome;
    private ITasResolver tasResolver;

    public OSBTradeImportRole(Builder builder) {
        super(builder.roleId);
        this.wlsServerHome = builder.wlsServerHome;
        this.osbHome = builder.osbHome;
        this.tasResolver = builder.tasResolver;
        this.javaHome = builder.javaHome;
        this.wlsRoleID = builder.wlsRoleID;
        this.wlsPort = builder.wlsPort;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        deployOSBWebappToImport(aaClient);
        updateWSDLConfig(aaClient, TRADE_SOURCE_DOWNLOAD + "/ALSBSampleProject_Customization.xml");
        updateWSDLConfig(aaClient, TRADE_SOURCE_DOWNLOAD + "/Trade/Trade/Trade.WSDL");
        updateWSDLConfig(aaClient, TRADE_SOURCE_DOWNLOAD + "/Trade/Trade/BUSINESS/Business_Service_1.BusinessService");
        updateWSDLConfig(aaClient, TRADE_SOURCE_DOWNLOAD + "/Trade/Trade/BUSINESS/Business_Service_2.BusinessService");
        updateImportPropertiesFile(aaClient);
        bundleProjectAsJar(aaClient);
        uploadResourceToOSB(aaClient);
    }

    private void uploadResourceToOSB(IAutomationAgentClient aaClient) {
        List<String> argsCmd = new ArrayList<String>();

        RunCommandFlowContext context;
        //env classpath to run wlst command
        Map<String, String> env = new HashMap<>();
        env.put("CLASSPATH", wlsServerHome + "/server/lib/weblogic.jar;" + osbHome + "/modules/*;" + osbHome + "/lib/*");

        //argsCmd.add(javaHome + "/bin/jar");
        argsCmd.add("weblogic.WLST");
        argsCmd.add(TRADE_SOURCE_DOWNLOAD + "/import.py");
        argsCmd.add(TRADE_SOURCE_DOWNLOAD + "/import.properties");

        context = new RunCommandFlowContext.Builder(javaHome + "/bin/java")
                .args(argsCmd)
                .workDir(TRADE_SOURCE_DOWNLOAD)
                .doNotPrependWorkingDirectory()
                .environment(env)
                .build();
        runCommandFlowAsync(aaClient,context);
    }

    private void bundleProjectAsJar(IAutomationAgentClient aaClient) {
        RunCommandFlowContext context;
        List<String> argsCmd = new ArrayList<String>();
        //argsCmd.add(javaHome + "/bin/jar");
        argsCmd.add("cvf");
        argsCmd.add(OSB_TRADE_RESOURCE_NAME);
        argsCmd.add("-C");
        argsCmd.add(TRADE_SOURCE_DOWNLOAD + "/Trade/");
        argsCmd.add(".");

        context = new RunCommandFlowContext.Builder(javaHome + "/bin/jar")
                .args(argsCmd)
                .workDir(TRADE_SOURCE_DOWNLOAD)
                .doNotPrependWorkingDirectory()
                .build();
        runCommandFlowAsync(aaClient,context);
    }

    private void updateImportPropertiesFile(IAutomationAgentClient aaClient) {

        FileModifierFlowContext context;
        String fileName = TRADE_SOURCE_DOWNLOAD + "/import.properties";
        Map<String, String> replacePairs = new HashMap<String,String>();

        replacePairs.put("\\[OSB.PORT\\]",wlsPort);
        replacePairs.put("\\[DOMAIN.USER\\]","weblogic");
        replacePairs.put("\\[DOMAIN.PASSWORD\\]","welcome1");
        replacePairs.put("\\[IMPORT_JAR\\]",TRADE_SOURCE_DOWNLOAD + "/" + OSB_TRADE_RESOURCE_NAME);
        replacePairs.put("\\[CUST_XML\\]","ALSBSampleProject_Customization.xml");

        context = new FileModifierFlowContext.Builder()
                .replace(fileName, replacePairs)
                .build();
        runFlow(aaClient, FileModifierFlow.class, context);
    }

    private void deployOSBWebappToImport(IAutomationAgentClient aaClient) {
        GenericFlowContext context;
        context = new GenericFlowContext.Builder()
                .artifactUrl(tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.coda.wls-osb", "osbtradeapp", "zip", "11.1.1")))
                .destination(TRADE_SOURCE_DOWNLOAD)
                .build();
        runFlow(aaClient, GenericFlow.class, context);
    }

    private void updateWSDLConfig(IAutomationAgentClient aaClient, String fileName) {
        FileModifierFlowContext context;
        //String fileName = TRADE_SOURCE_DOWNLOAD + "/Trade/Trade/Trade.WSDL";
        Map<String, String> replacePairs = new HashMap<String,String>();

        replacePairs.put("\\[TRADE.URL\\]","http://" + tasResolver.getHostnameById(wlsRoleID) + ":7001/TradeBSL/Trade");

        context = new FileModifierFlowContext.Builder()
                .replace(fileName, replacePairs)
                .build();
        runFlow(aaClient, FileModifierFlow.class, context);

    }

    public static class Builder extends BuilderBase<OSBTradeImportRole.Builder, OSBTradeImportRole> {

        private final String roleId;
        private final ITasResolver tasResolver;
        protected String wlsServerHome;
        protected String osbHome;
        protected String wlsPort;
        protected String wlsRoleID;
        protected String javaHome;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        protected OSBTradeImportRole getInstance() {
            return new OSBTradeImportRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public OSBTradeImportRole build() {
            return getInstance();
        }

        public Builder wlsPort(String wlsPort) {
            Args.notBlank(wlsPort, "Server port");
            this.wlsPort = wlsPort;
            return builder();
        }

        public Builder osbHome(String osbHome){
            Args.notBlank(osbHome, "OSB home Dir");
            this.osbHome = osbHome;
            return builder();
        }

        public Builder wlsRoleID(String wlsRoleID){
            Args.notBlank(wlsRoleID, "role ID");
            this.wlsRoleID = wlsRoleID;
            return builder();
        }

        public Builder javaHome(String javaHome){
            Args.notBlank(javaHome, "Java Home");
            this.javaHome = javaHome;
            return builder();
        }

        public Builder wlsServerHome(String wlsServerHome){
            Args.notBlank(wlsServerHome, "weblogic home");
            this.wlsServerHome = wlsServerHome;
            return builder();
        }
    }
}
