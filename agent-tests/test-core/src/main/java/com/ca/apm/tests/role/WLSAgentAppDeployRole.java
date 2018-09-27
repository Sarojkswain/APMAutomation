/*
 * Copyright (c) 2016 CA.  All rights reserved.
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
package com.ca.apm.tests.role;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.tests.flow.AccAgentDownloadFlow;
import com.ca.apm.tests.flow.AccAgentDownloadFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * @author ahmal01, kurma05
 */
public class WLSAgentAppDeployRole extends AbstractRole {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(WLSAgentAppDeployRole.class);
    private ITasResolver tasResolver;
    private String roleId;
    private String wlsRole;
    private boolean isLegacyMode;
    private boolean isJassEnabled;
    private String classifier;
    private String serverPort;
    private String javaHome;
    private String agentVersion;
    private String accServerUrl;
    private String accPackageName;
    private String accPackageOsName;  
    private boolean isNoRedefEnabled;
    private static final String SEPARATOR = "\\\\";
    private static final String DEPLOY_BASE = "C:" + SEPARATOR + "automation" + SEPARATOR + "deployed" + SEPARATOR;
    private static final String DEFAULT_INSTALL_HOME = DEPLOY_BASE + "Oracle" + SEPARATOR + "Middleware12.1.3";
    private static final String PO_DOMAIN_HOME = DEPLOY_BASE + "webapp" + SEPARATOR + "pipeorgandomain";    
    private static final String DATABASE_JDBC_URL = "jdbc:oracle:thin:@jass6:1521:AUTO";
    private static final String DATABASE_DRIVER_NAME = "oracle.jdbc.xa.client.OracleXADataSource";
    private static final String DATABASE_USER_NAME = "AUTOMATION";
    private static final String DATABASE_JDBC_NAME = "jdbc:oracle:thin:@jass6:1521:AUTO";
    private static final String DATABASE_ENCRYPTED_USER_PASSWORD = "AUTOMATION";
    private static final String DATABASE_KEEPALIVE_QUERY = "SQL SELECT 1 FROM dual";
    private String installHome;
 
    protected WLSAgentAppDeployRole(Builder builder) {
        
        super(builder.roleId);
        this.roleId = builder.roleId;
        this.wlsRole = builder.wlsRole;
        this.tasResolver = builder.tasResolver;
        this.classifier = builder.classifier;
        this.serverPort = builder.serverPort;
        this.isLegacyMode = builder.isLegacyMode;
        this.isJassEnabled = builder.isJassEnabled;
        this.javaHome = builder.javaHome;
        this.agentVersion = builder.agentVersion;
        this.installHome = builder.installHome;
        this.isNoRedefEnabled = builder.isNoRedefEnabled;
        this.accServerUrl = builder.accServerUrl;
        this.accPackageName = builder.accPackageName;
        this.accPackageOsName = builder.accPackageOsName;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        
        deployArtifacts(aaClient);
        updatePODomainConfig(aaClient);
        updatePODomainJDBC(aaClient);
        updatePODomainstartRootWLSCMD(aaClient);
        updatePODomainsetDomainEnv(aaClient);
        updatePODomainStartWLSCMD(aaClient);
        updatePODomainStopWLSCMD(aaClient);        
    }
   
    private void deployArtifacts(IAutomationAgentClient aaClient) {
     
        GenericFlowContext context = null;
        
        LOGGER.info("Deploying Artifacts...");
        
        //get po domain file
        context = new GenericFlowContext.Builder()
           .artifactUrl(tasResolver.getArtifactUrl(new DefaultArtifact(
               "com.ca.apm.binaries.weblogic", "pipeorgandomain", "nostartupclass", "zip", "10.3")))
           .destination(codifyPath(PO_DOMAIN_HOME))
           .build();  
        runFlow(aaClient, GenericFlow.class, context);

        //get po ejb3 jar file
        context = new GenericFlowContext.Builder()
           .notArchive()
           .artifactUrl(tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.coda-projects.test-tools.pipeorgan", "pipeorgan_ear_ejb3", "ear", tasResolver.getDefaultVersion())))
           .destination(codifyPath(PO_DOMAIN_HOME + "/applications/pipeorgan.wls.ejb3.ear"))
           .build();  
        runFlow(aaClient, GenericFlow.class, context);

        //get po jar files
        context = new GenericFlowContext.Builder()
            .notArchive()
            .artifactUrl(tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.coda-projects.test-tools.pipeorgan", "pipeorgan", "jar", tasResolver.getDefaultVersion())))
            .destination(codifyPath(PO_DOMAIN_HOME + "/pipeorgan/pipeorgan.jar"))
            .build();  
        runFlow(aaClient, GenericFlow.class, context);
        
        if(classifier.contains("jvm6")) {            
            //get ejb3 jar
            context = new GenericFlowContext.Builder()
                .notArchive()
                .artifactUrl(tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.coda-projects.test-tools.pipeorgan", 
                    "pipeorgan_ejb3", "jar", tasResolver.getDefaultVersion())))
                .destination(codifyPath(PO_DOMAIN_HOME + "/pipeorgan/pipeorgan_ejb3.jar"))
                .build();  
            runFlow(aaClient, GenericFlow.class, context);
        
            //get ejb2 jar        
            context = new GenericFlowContext.Builder()
                .notArchive()
                .artifactUrl(tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.coda-projects.test-tools.pipeorgan", 
                   "pipeorgan_ejb", "jar", tasResolver.getDefaultVersion())))
                .destination(codifyPath(PO_DOMAIN_HOME + "/pipeorgan/pipeorgan_ejb.jar"))
                .build();  
            runFlow(aaClient, GenericFlow.class, context);
        }
        
        //get qatestapp ear
        context = new GenericFlowContext.Builder()    
            .notArchive()
            .artifactUrl(tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.coda-projects.test-tools", 
                "qatestapp", classifier, "ear", tasResolver.getDefaultVersion())))
            .destination(codifyPath(PO_DOMAIN_HOME + "/applications/QATestApp.ear"))
            .build();  
        runFlow(aaClient, GenericFlow.class, context);
        
        LOGGER.info("Deploying Weblogic Agent.");
        
        String artifact = "agent-noinstaller-weblogic-windows";
        if(isLegacyMode) {
            artifact = "agent-legacy-noinstaller-weblogic-windows";
        }
        
        //get weblogic agent         
        if (accPackageName != null) {
            deployAccArtifact(aaClient);
        }
        else {
            context = new GenericFlowContext.Builder()    
                .artifactUrl(tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.delivery", artifact, "zip", agentVersion)))
                .destination(codifyPath(PO_DOMAIN_HOME))
                .build();  
            runFlow(aaClient, GenericFlow.class, context);
        }
        
        if (isJassEnabled){
            //get DITestAppJass app (deploy it to temp dir 'jass_apps' to be used by DI tests only) 
            context = new GenericFlowContext.Builder()    
                .notArchive()
                .artifactUrl(tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.coda-projects.test-tools", "ditestappjass", "dist", "war", tasResolver.getDefaultVersion())))
                .destination(codifyPath(PO_DOMAIN_HOME + "/jass_apps/DITestAppJass.war"))
                .build();  
            runFlow(aaClient, GenericFlow.class, context);
            
            //get ejbtestappjass app (deploy it to temp dir 'jass_apps' to be used by EJB tests only) 
            context = new GenericFlowContext.Builder()    
                .notArchive()
                .artifactUrl(tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.coda-projects.test-tools.ejbtestappjass", "ejbtestappjass-ear", "ear", tasResolver.getDefaultVersion())))
                .destination(codifyPath(PO_DOMAIN_HOME + "/jass_apps/ejbtestappjass.ear"))
                .build();  
            runFlow(aaClient, GenericFlow.class, context);
        }           
    }
    
    private void deployAccArtifact(IAutomationAgentClient aaClient) {
        
        //get agent 
        AccAgentDownloadFlowContext downloadContext = new AccAgentDownloadFlowContext.Builder()
            .packageName(accPackageName)
            .osName(accPackageOsName)
            .accServerUrl(accServerUrl)
            .installDir(codifyPath(PO_DOMAIN_HOME))
            .build(); 
        
        runFlow(aaClient, AccAgentDownloadFlow.class, downloadContext);
    }

    private void updatePODomainConfig(IAutomationAgentClient aaClient) {
        
        FileModifierFlowContext context = null;
        Map<String,String> replacePairs = new HashMap<String,String>();
        
        replacePairs.put("\\[WEBLOGIC.SERVER.PORT\\]", serverPort);
        replacePairs.put("\\[WEBLOGIC.SERVER.HOST\\]", tasResolver.getHostnameById(wlsRole));
        
        String fileName = PO_DOMAIN_HOME + "/config/config.xml";
        // replacing values
        context = new FileModifierFlowContext.Builder()
            .replace(fileName, replacePairs)
            .build();
        runFlow(aaClient, FileModifierFlow.class, context);        
    }
    
    private void updatePODomainJDBC(IAutomationAgentClient aaClient) {

        FileModifierFlowContext context = null;
        Map<String, String> replacePairs = new HashMap<String,String>();
        
        replacePairs.put("\\[DATABASE.JDBC.URL\\]", DATABASE_JDBC_URL);
        replacePairs.put("\\[DATABASE.DRIVER.NAME\\]", DATABASE_DRIVER_NAME);
        replacePairs.put("\\[DATABASE.USER.NAME\\]",DATABASE_USER_NAME);
        replacePairs.put("\\[DATABASE.JDBC.NAME\\]", DATABASE_JDBC_NAME);
        replacePairs.put("\\[DATABASE.ENCRYPTED.USER.PASSWORD\\]", DATABASE_ENCRYPTED_USER_PASSWORD);
        replacePairs.put("\\[DATABASE.KEEPALIVE.QUERY\\]", DATABASE_KEEPALIVE_QUERY);
        
        String fileName = PO_DOMAIN_HOME + "/config/jdbc/pipeorgan-jdbc.xml";
        
        // replacing values
        context = new FileModifierFlowContext.Builder()
            .replace(fileName, replacePairs)
            .build();
        runFlow(aaClient, FileModifierFlow.class, context);
    }
    
    private void updatePODomainstartRootWLSCMD(IAutomationAgentClient aaClient) {

        FileModifierFlowContext context = null;
        Map<String, String> replacePairs = new HashMap<String,String>();
        
        replacePairs.put("\\[DOMAIN.HOME.DIR\\]", PO_DOMAIN_HOME );
        
        String fileName = PO_DOMAIN_HOME + "/startWebLogic.cmd";
    
        context = new FileModifierFlowContext.Builder()
        .replace(fileName, replacePairs)
        .build();
        runFlow(aaClient, FileModifierFlow.class, context);
    }
    
    private void updatePODomainsetDomainEnv(IAutomationAgentClient aaClient) {

        FileModifierFlowContext context = null;
        Map<String, String> replacePairs = new HashMap<String,String>();
        
        replacePairs.put("\\[WLS.HOME\\]", installHome + "/wlserver");
        replacePairs.put("\\[JAVA.HOME\\]", codifyPath(javaHome));
        replacePairs.put("\\[DOMAIN.HOME.DIR\\]", PO_DOMAIN_HOME );
        replacePairs.put("-Xms\\[MIN.HEAP.SIZE\\]m -Xmx\\[MAX.HEAP.SIZE\\]m","-Xms512m -Xmx752m");
        replacePairs.put("\\[PERM.SPACE.SIZE\\]","50");
        replacePairs.put("\\[MAX.PERM.SPACE.SIZE\\]","200");
        replacePairs.put("\\[RESULTS.OUTPUT.DIR\\]",PO_DOMAIN_HOME);
        
        String fileName = PO_DOMAIN_HOME + "/bin/setDomainEnv.cmd";
  
        context = new FileModifierFlowContext.Builder()
        .replace(fileName, replacePairs)
        .build();
        runFlow(aaClient, FileModifierFlow.class, context);
    }
    
    private void updatePODomainStartWLSCMD(IAutomationAgentClient aaClient) {

        FileModifierFlowContext context = null;
        Map<String, String> replacePairs = new HashMap<String,String>();
        
        String agentJar = "Agent.jar";
        String agentProfile = "IntroscopeAgent.profile";
        
        if(isNoRedefEnabled) {
            agentJar = "AgentNoRedefNoRetrans.jar";
            if (accPackageName == null) { //use noredef profile for non-acc package only
                agentProfile = "IntroscopeAgent.NoRedef.profile";
            }
        }
        
        replacePairs.put("\\[DOMAIN.HOME.DIR\\]", PO_DOMAIN_HOME );
        replacePairs.put("WILY_AGENT_ENABLED=false", "WILY_AGENT_ENABLED=true");
        replacePairs.put("\\[AGENT.JAVA.OPTIONS\\]","-javaagent\\:"
                        + PO_DOMAIN_HOME + "/wily/" + agentJar + " -Dcom.wily.introscope.agentProfile=" 
                        + PO_DOMAIN_HOME + "/wily/core/config/" + agentProfile);
        replacePairs.put("\\[WLS.LOG.OUTPUT\\]", PO_DOMAIN_HOME + "/WebLogicConsole.log");
        replacePairs.put("\\[OTHER.JAVA.OPTIONS\\]",""); 
        replacePairs.put("\\[HEAPMONITOR.JAR\\]",""); 
        
        String classpath = PO_DOMAIN_HOME;
        if(classifier.contains("jvm6")) {
            classpath += ";" + 
                PO_DOMAIN_HOME + "/pipeorgan/pipeorgan.jar;" +
                PO_DOMAIN_HOME + "/pipeorgan/pipeorgan_ejb3.jar;" +
                PO_DOMAIN_HOME + "/pipeorgan/pipeorgan_ejb.jar";
        }       
        replacePairs.put("\\[AGENT.JAVA.CLASSPATH\\]", classpath);
        
        String fileName = PO_DOMAIN_HOME + "/bin/startWebLogic.cmd";
        
        context = new FileModifierFlowContext.Builder()
            .replace(fileName, replacePairs)
            .build();
        runFlow(aaClient, FileModifierFlow.class, context);
    }

    private void updatePODomainStopWLSCMD(IAutomationAgentClient aaClient) {

        FileModifierFlowContext context = null;
        Map<String, String> replacePairs = new HashMap<String,String>();
        
        replacePairs.put("\\[DOMAIN.HOME.DIR\\]", PO_DOMAIN_HOME );
        replacePairs.put("\\[WEBLOGIC.SERVER.HOST\\]", tasResolver.getHostnameById(wlsRole));
        replacePairs.put("\\[WEBLOGIC.SERVER.PORT\\]", serverPort);
        replacePairs.put("'%SERVER_NAME%','Server'", "'%SERVER_NAME%','Server',ignoreSessions='true',force='true'");

        String fileName = PO_DOMAIN_HOME + "/bin/stopWebLogic.cmd";
        
        context = new FileModifierFlowContext.Builder()
        .replace(fileName, replacePairs)
        .build();
        runFlow(aaClient, FileModifierFlow.class, context);
    }

    @NotNull
    protected String codifyPath(String path) {
        return FilenameUtils.separatorsToUnix(path);
    }
    
    public static class Builder extends BuilderBase<Builder, WLSAgentAppDeployRole> {

        private final String roleId;
        private final ITasResolver tasResolver;
        protected String appserverDir;
        protected String wlsRole;
        protected String serverName;
        protected String classifier;
        protected String serverPort;
        protected boolean isLegacyMode;
        protected boolean isJassEnabled;
        protected String javaHome;
        protected String agentVersion;
        protected String installHome = DEFAULT_INSTALL_HOME;
        protected boolean isNoRedefEnabled = false;
        // for pulling acc package
        private String accServerUrl;
        private String accPackageName;
        private String accPackageOsName;
        
        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public WLSAgentAppDeployRole build() {
            return getInstance();
        }

        @Override
        protected WLSAgentAppDeployRole getInstance() {
            return new WLSAgentAppDeployRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
        
        public Builder appserverDir(String appserverDir) {
            this.appserverDir = appserverDir;
            return builder();
        }
        
        public Builder wlsRole(String wlsRole) {
            this.wlsRole = wlsRole;
            return builder();
        }
        
        public Builder serverName(String serverName) {
            this.serverName = serverName;
            return builder();
        }
        
        public Builder classifier(String classifier) {
            this.classifier = classifier;
            return builder();
        }
        
        public Builder serverPort(String serverPort) {
            this.serverPort = serverPort;
            return builder();
        }
        
        public Builder isLegacyMode(boolean isLegacyMode) {
            this.isLegacyMode = isLegacyMode;
            return builder();
        }

        public Builder isJassEnabled(boolean isJassEnabled) {
            this.isJassEnabled = isJassEnabled;
            return builder();
        }

        public Builder javaHome(String javaHome) {
            this.javaHome = javaHome;
            return builder();
        }
        
        public Builder agentVersion(String agentVersion) {
            this.agentVersion = agentVersion;
            return builder();
        }
        
        public Builder installHome(String installHome) {
            this.installHome = installHome;
            return builder();
        }
        
        public Builder accServerUrl(String url) {
            this.accServerUrl = url;
            return builder();
        }
        
        public Builder accPackageName(String accPackageName) {
            this.accPackageName = accPackageName;
            return builder();
        }
        
        public Builder accPackageOsName(String accPackageOsName) {
            this.accPackageOsName = accPackageOsName;
            return builder();
        }
        
        public Builder isNoRedefEnabled(boolean isNoRedefEnabled) {
            this.isNoRedefEnabled = isNoRedefEnabled;
            return builder();
        }
    }
}