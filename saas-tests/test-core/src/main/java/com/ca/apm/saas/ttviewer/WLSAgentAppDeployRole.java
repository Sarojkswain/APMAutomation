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
package com.ca.apm.saas.ttviewer;

import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlow;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.webapp.JavaRole;

/**
 * @author banra06
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
    private JavaRole javaRole;
    private String agentVersion;
    private String emHost;

    public static final String WLS_START_ENV_KEY = "wlsstart";
    public static final String WLS_STOP_ENV_KEY = "wlsstop";

    private static final String SEPARATOR = "\\\\";
    private static final String DEPLOY_BASE = "C:" + SEPARATOR + "automation" + SEPARATOR
        + "deployed" + SEPARATOR;
    private static final String WLS12C_INSTALL_HOME = DEPLOY_BASE + "Oracle" + SEPARATOR
        + "Middleware12.1.3";
    private static final String PO_DOMAIN_HOME = DEPLOY_BASE + "webapp" + SEPARATOR
        + "pipeorgandomain";
    private static final String ALT_START_SCRIPT = "startWebLogicInWindow.cmd";

    private static final String DATABASE_JDBC_URL = "jdbc:oracle:thin:@jass6:1521:AUTO";
    private static final String DATABASE_DRIVER_NAME = "oracle.jdbc.xa.client.OracleXADataSource";
    private static final String DATABASE_USER_NAME = "AUTOMATION";
    private static final String DATABASE_JDBC_NAME = "jdbc:oracle:thin:@jass6:1521:AUTO";
    private static final String DATABASE_ENCRYPTED_USER_PASSWORD = "AUTOMATION";
    private static final String DATABASE_KEEPALIVE_QUERY = "SQL SELECT 1 FROM dual";
    private static final String START_MATCH_TEXT = "WLS started";

    protected WLSAgentAppDeployRole(Builder builder) {

        super(builder.roleId);
        this.roleId = builder.roleId;
        this.wlsRole = builder.wlsRole;
        this.tasResolver = builder.tasResolver;
        this.classifier = builder.classifier;
        this.serverPort = builder.serverPort;
        this.isLegacyMode = builder.isLegacyMode;
        this.isJassEnabled = builder.isJassEnabled;
        this.javaRole = builder.javaRole;
        this.agentVersion = builder.agentVersion;
        this.emHost = builder.emHost;
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
        updateCrossJVMTracing(aaClient);
        startPODomain(aaClient);
        createCustomPBD(aaClient);
    }

    private void createCustomPBD(IAutomationAgentClient aaClient) {

        Collection<String> createBatch =
            Arrays
                .asList(
                    "TurnOn: RecursorTracing",
                    "SetFlag: RecursorTracing",
                    "IdentifyClassAs: com.wily.tools.pipeorgan.extensions.Recursor RecursorTracing",
                    "TraceOneMethodIfFlagged: RecursorTracing doRecurse BlamePointTracer \"RecursorTest1|{classname}\"",
                    "TraceOneMethodIfFlagged: RecursorTracing doRecurse2 BlamePointTracer \"RecursorTest2|{classname}\"");

        FileModifierFlowContext BatchFile =
            new FileModifierFlowContext.Builder().create(
                PO_DOMAIN_HOME + "/wily/core/config/hotdeploy/custom.pbd", createBatch).build();
        runFlow(aaClient, FileModifierFlow.class, BatchFile);
    }

    private void startPODomain(IAutomationAgentClient aaClient) {
        RunCommandFlowContext ctx =
            new RunCommandFlowContext.Builder(ALT_START_SCRIPT).workDir(PO_DOMAIN_HOME)
                .doNotPrependWorkingDirectory().terminateOnMatch(START_MATCH_TEXT).build();
        runCommandFlow(aaClient, ctx);
        synchronized (this) {
            // FIXME - startWebLogic.cmd does not write any output that we can monitor
            // rewrite the start scripts to not redirect output. For now, just wait
            // up to 10 minutes for the server to have time to start. When it connects
            // on the configured port, consider the server as started
            waitForWebLogicStart();
        }
    }


    private void waitForWebLogicStart() {
        String wlsHost = tasResolver.getHostnameById(roleId);
        long timeout = System.currentTimeMillis() + 600000L;
        boolean started = false;
        while (!started && System.currentTimeMillis() < timeout) {
            try {
                Socket s = new Socket(wlsHost, Integer.parseInt(serverPort));
                s.close();
                break;
            } catch (Exception e) {
                // if no connection is made to WLS, an exception is thrown, which we don't care
                // about
            }

            try {
                synchronized (this) {
                    wait(10000L);
                }
            } catch (Exception e) {
                // do nothing
            }
        }
    }


    private void deployArtifacts(IAutomationAgentClient aaClient) {

        GenericFlowContext context = null;

        LOGGER.info("Deploying Artifacts...");

        // get po domain file
        context =
            new GenericFlowContext.Builder()
                .artifactUrl(
                    tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.binaries.weblogic",
                        "pipeorgandomain", "zip", "10.3"))).destination(codifyPath(PO_DOMAIN_HOME))
                .build();
        runFlow(aaClient, GenericFlow.class, context);

        // get po ejb3 jar file
        context =
            new GenericFlowContext.Builder()
                .notArchive()
                .artifactUrl(
                    tasResolver.getArtifactUrl(new DefaultArtifact(
                        "com.ca.apm.coda-projects.test-tools.pipeorgan", "pipeorgan_ear_ejb3",
                        "ear", tasResolver.getDefaultVersion())))
                .destination(codifyPath(PO_DOMAIN_HOME + "/applications/pipeorgan.wls.ejb3.ear"))
                .build();
        runFlow(aaClient, GenericFlow.class, context);

        // get po jar file
        context =
            new GenericFlowContext.Builder()
                .notArchive()
                .artifactUrl(
                    tasResolver.getArtifactUrl(new DefaultArtifact(
                        "com.ca.apm.coda-projects.test-tools.pipeorgan", "pipeorgan", "jar",
                        tasResolver.getDefaultVersion())))
                .destination(codifyPath(PO_DOMAIN_HOME + "/pipeorgan/pipeorgan.jar")).build();
        runFlow(aaClient, GenericFlow.class, context);

        // get qatestapp ear
        context =
            new GenericFlowContext.Builder()
                .notArchive()
                .artifactUrl(
                    tasResolver.getArtifactUrl(new DefaultArtifact(
                        "com.ca.apm.coda-projects.test-tools", "qatestapp", classifier, "ear",
                        tasResolver.getDefaultVersion())))
                .destination(codifyPath(PO_DOMAIN_HOME + "/applications/QATestApp.ear")).build();
        runFlow(aaClient, GenericFlow.class, context);

        LOGGER.info("Deploying Weblogic Agent.");

        String artifact = "agent-noinstaller-weblogic-windows";
        if (isLegacyMode) {
            artifact = "agent-legacy-noinstaller-weblogic-windows";
        }

        // get weblogic agent
        context =
            new GenericFlowContext.Builder()
                .artifactUrl(
                    tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.delivery", artifact,
                        "zip", agentVersion))).destination(codifyPath(PO_DOMAIN_HOME)).build();
        runFlow(aaClient, GenericFlow.class, context);

        if (isJassEnabled) {
            // get DITestAppJass agent
            context =
                new GenericFlowContext.Builder()
                    .notArchive()
                    .artifactUrl(
                        tasResolver.getArtifactUrl(new DefaultArtifact(
                            "com.ca.apm.coda-projects.test-tools", "ditestappjass", "dist", "war",
                            tasResolver.getDefaultVersion())))
                    .destination(codifyPath(PO_DOMAIN_HOME + "/autodeploy/DITestAppJass.war"))
                    .build();
            runFlow(aaClient, GenericFlow.class, context);
        }

    }

    private void updatePODomainConfig(IAutomationAgentClient aaClient) {

        FileModifierFlowContext context = null;
        Map<String, String> replacePairs = new HashMap<String, String>();

        replacePairs.put("\\[WEBLOGIC.SERVER.PORT\\]", serverPort);
        replacePairs.put("\\[WEBLOGIC.SERVER.HOST\\]", tasResolver.getHostnameById(wlsRole));

        String fileName = PO_DOMAIN_HOME + "/config/config.xml";
        // replacing values
        context = new FileModifierFlowContext.Builder().replace(fileName, replacePairs).build();
        runFlow(aaClient, FileModifierFlow.class, context);

    }

    private void updatePODomainJDBC(IAutomationAgentClient aaClient) {

        FileModifierFlowContext context = null;
        Map<String, String> replacePairs = new HashMap<String, String>();

        replacePairs.put("\\[DATABASE.JDBC.URL\\]", DATABASE_JDBC_URL);
        replacePairs.put("\\[DATABASE.DRIVER.NAME\\]", DATABASE_DRIVER_NAME);
        replacePairs.put("\\[DATABASE.USER.NAME\\]", DATABASE_USER_NAME);
        replacePairs.put("\\[DATABASE.JDBC.NAME\\]", DATABASE_JDBC_NAME);
        replacePairs
            .put("\\[DATABASE.ENCRYPTED.USER.PASSWORD\\]", DATABASE_ENCRYPTED_USER_PASSWORD);
        replacePairs.put("\\[DATABASE.KEEPALIVE.QUERY\\]", DATABASE_KEEPALIVE_QUERY);

        String fileName = PO_DOMAIN_HOME + "/config/jdbc/pipeorgan-jdbc.xml";

        // replacing values
        context = new FileModifierFlowContext.Builder().replace(fileName, replacePairs).build();
        runFlow(aaClient, FileModifierFlow.class, context);
    }

    private void updatePODomainstartRootWLSCMD(IAutomationAgentClient aaClient) {
        Map<String, String> replacePairs = new HashMap<>(1);

        replacePairs.put("\\[DOMAIN.HOME.DIR\\]", PO_DOMAIN_HOME);
        String fileName = PO_DOMAIN_HOME + "/startWebLogic.cmd";

        FileModifierFlowContext context =
            new FileModifierFlowContext.Builder().replace(fileName, replacePairs).build();
        runFlow(aaClient, FileModifierFlow.class, context);
    }

    private void updatePODomainsetDomainEnv(IAutomationAgentClient aaClient) {
        Map<String, String> replacePairs = new HashMap<>(10);

        replacePairs.put("\\[WLS.HOME\\]", WLS12C_INSTALL_HOME + "/wlserver");
        replacePairs.put("\\[JAVA.HOME\\]", codifyPath(javaRole.getInstallDir()));
        replacePairs.put("\\[DOMAIN.HOME.DIR\\]", PO_DOMAIN_HOME);
        replacePairs.put("-Xms\\[MIN.HEAP.SIZE\\]m -Xmx\\[MAX.HEAP.SIZE\\]m", "-Xms512m -Xmx752m");
        replacePairs.put("\\[PERM.SPACE.SIZE\\]", "50");
        replacePairs.put("\\[MAX.PERM.SPACE.SIZE\\]", "200");
        replacePairs.put("\\[RESULTS.OUTPUT.DIR\\]", PO_DOMAIN_HOME);
        replacePairs.put("set WLS_STDOUT_LOG",
            "REM Disable redirection to avoid filling disks with useless drivel.\n"
                + "REM set WLS_STDOUT_LOG");
        replacePairs.put("set WLS_STDERR_LOG",
            "REM Disable redirection to avoid filling disks with useless drivel.\n"
                + "REM set WLS_STDERR_LOG");

        String fileName = PO_DOMAIN_HOME + "/bin/setDomainEnv.cmd";
        FileModifierFlowContext context =
            new FileModifierFlowContext.Builder().replace(fileName, replacePairs).build();

        runFlow(aaClient, FileModifierFlow.class, context);
    }

    private void updatePODomainStartWLSCMD(IAutomationAgentClient aaClient) {

        FileModifierFlowContext context = null;
        Map<String, String> replacePairs = new HashMap<String, String>();

        replacePairs.put("\\[DOMAIN.HOME.DIR\\]", PO_DOMAIN_HOME);
        replacePairs.put("WILY_AGENT_ENABLED=false", "WILY_AGENT_ENABLED=true");
        replacePairs.put("\\[AGENT.JAVA.OPTIONS\\]", "-Dweblogic.TracingEnabled=true -javaagent\\:"
            + PO_DOMAIN_HOME + "/wily/Agent.jar" + " -Dcom.wily.introscope.agentProfile="
            + PO_DOMAIN_HOME + "/wily/core/config/IntroscopeAgent.profile");
        replacePairs.put("\\[AGENT.JAVA.CLASSPATH\\]", PO_DOMAIN_HOME
            + "/wily/common/WebAppSupport.jar");
        replacePairs.put("\\[WLS.LOG.OUTPUT\\]", PO_DOMAIN_HOME + "/WebLogicConsole.log");
        replacePairs.put("\\[OTHER.JAVA.OPTIONS\\]", "");
        replacePairs.put("\\[HEAPMONITOR.JAR\\]", "");

        String fileName = PO_DOMAIN_HOME + "/bin/startWebLogic.cmd";

        context = new FileModifierFlowContext.Builder().replace(fileName, replacePairs).build();
        runFlow(aaClient, FileModifierFlow.class, context);

        List<String> appendLines =
            Arrays.asList(new String[] {"START \"WLS Rulez\" startWebLogic.cmd",
                    "echo " + START_MATCH_TEXT});

        FileCreatorFlowContext createContext =
            new FileCreatorFlowContext.Builder().destinationFilename(ALT_START_SCRIPT)
                .destinationDir(PO_DOMAIN_HOME).fromData(appendLines).build();
        runFlow(aaClient, FileCreatorFlow.class, createContext);
    }

    private void updatePODomainStopWLSCMD(IAutomationAgentClient aaClient) {

        FileModifierFlowContext context = null;
        Map<String, String> replacePairs = new HashMap<String, String>();

        replacePairs.put("\\[DOMAIN.HOME.DIR\\]", PO_DOMAIN_HOME);
        replacePairs.put("\\[WEBLOGIC.SERVER.HOST\\]", tasResolver.getHostnameById(wlsRole));
        replacePairs.put("\\[WEBLOGIC.SERVER.PORT\\]", serverPort);
        replacePairs.put("'%SERVER_NAME%','Server'",
            "'%SERVER_NAME%','Server',ignoreSessions='true',force='true'");

        String fileName = PO_DOMAIN_HOME + "/bin/stopWebLogic.cmd";

        context = new FileModifierFlowContext.Builder().replace(fileName, replacePairs).build();
        runFlow(aaClient, FileModifierFlow.class, context);
    }

    private void updateCrossJVMTracing(IAutomationAgentClient aaClient) {

        FileModifierFlowContext context = null;
        Map<String, String> replacePairs = new HashMap<String, String>();
        String prop = "agentManager.url.1=" + emHost + ":5001";

        replacePairs.put("#introscope.agent.weblogic.crossjvm=true",
            "introscope.agent.weblogic.crossjvm=true");
        replacePairs.put("agentManager.url.1=localhost:5001", prop);

        String fileName = PO_DOMAIN_HOME + "/wily/core/config/IntroscopeAgent.profile";

        context = new FileModifierFlowContext.Builder().replace(fileName, replacePairs).build();
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
        protected JavaRole javaRole;
        protected String agentVersion;
        protected String emHost;

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

        public Builder javaRole(JavaRole javaRole) {
            this.javaRole = javaRole;
            return builder();
        }

        public Builder emHost(String emHost) {
            this.emHost = emHost;
            return builder();
        }

        public Builder agentVersion(String agentVersion) {
            this.agentVersion = agentVersion;
            return builder();
        }
    }
}
