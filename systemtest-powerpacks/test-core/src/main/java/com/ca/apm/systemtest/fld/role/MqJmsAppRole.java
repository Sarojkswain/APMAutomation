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

package com.ca.apm.systemtest.fld.role;

import com.ca.apm.automation.action.flow.agent.ApplicationServerType;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.systemtest.fld.flow.MQExplorerFlow;
import com.ca.apm.systemtest.fld.flow.MQExplorerFlowContext;
import com.ca.apm.systemtest.fld.flow.WASRegisterAgentFlow;
import com.ca.apm.systemtest.fld.flow.WASRegisterAgentFlowContext;
import com.ca.apm.tests.role.Websphere85Role;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.webapp.WebSphere8Role;
import org.apache.http.util.Args;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @Author rsssa02
 */
public class MqJmsAppRole extends AbstractRole {
    private static final Logger LOGGER = LoggerFactory.getLogger(TibcoTradeAppRole.class);
    private ITasResolver tasResolver;
    private String wasBinDir;
    private String profileName;
    private String nodeName;
    private String cellName;
    private String serverName;
    private String qmanagerName;
    private String creditReplyQ;
    private String creditRequestQ;
    private String replyQ;
    private String requestQ;
    private boolean setupAgent;
    private boolean wasIsAutostart;
    private String agentDirPath;
    private String wasInstallDir;
    private String wasRoleId;
    private MQExplorerFlowContext flowContext;
    private MQExplorerFlow flowBuilder;
    private IBMMQRole ibmmqRole;
    private final String propFilePath = "\\properties\\wsadmin.properties";
    private String propertyMDB = "com.ibm.websphere.management.application.dfltbndng.mdb.preferexisting=true";
    private final String deployProps = "deploy.properties";
    private final String createJmsResource = "create_jmsresources.jacl";

    private final boolean predeployed;


    MqJmsAppRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.tasResolver = builder.tasResolver;
        this.wasBinDir = builder.wasBinDir;
        this.profileName = builder.profileName;
        this.nodeName = builder.nodeName;
        this.cellName = builder.cellName;
        this.serverName = builder.serverName;
        this.ibmmqRole = builder.ibmmqRole;
        this.setupAgent = builder.setupAgent;
        this.wasIsAutostart = builder.wasIsAutostart;
        this.wasInstallDir = builder.wasInstallDir;
        this.wasRoleId = builder.wasRoleId;
        this.agentDirPath = builder.agentDirPath;
        this.creditReplyQ = builder.creditReplyQ;
        this.creditRequestQ = builder.creditRequestQ;
        this.qmanagerName = builder.qmanagerName;
        this.replyQ = builder.replyQ;
        this.requestQ = builder.requestQ;

        predeployed = builder.predeployed;
    }

    public boolean isPredeployed() {
        return predeployed;
    }

    public void deploy(IAutomationAgentClient aaClient) {
        if (!predeployed) {
            deployMqLoanAppArchive(aaClient, "com.ca.apm.coda", "ws-mqapp", "1.1");
            updateDeployProperties(aaClient);
            updateWsAdminPropertyFile(aaClient);
            if (wasIsAutostart) {
                commandLineStartStopWAS(aaClient, false);
            }
            commandLineStartStopWAS(aaClient, true);
            createJmsResourcesAdmin(aaClient);
            installMQLoanApp(aaClient);
            if (this.setupAgent) {
                registerJavaAgent(aaClient);
            }
            commandLineStartStopWAS(aaClient, wasIsAutostart);
        }
    }

    private void registerJavaAgent(IAutomationAgentClient aaClient) {
        String serverXMLPath = this.wasInstallDir + "\\profiles\\" + this.profileName + "\\config\\cells\\"
                + this.nodeName + "Cell\\nodes\\" + this.nodeName + "\\servers\\" + this.serverName;

        WASRegisterAgentFlowContext registerAgentFlowContext;
        registerAgentFlowContext = new WASRegisterAgentFlowContext.Builder()
                .agentPath(this.agentDirPath).serverType(ApplicationServerType.WEBSPHERE)
                .serverXmlFilePath(serverXMLPath).maxHeap("1024").initialHeap("850")
                .build();
        runFlow(aaClient, WASRegisterAgentFlow.class, registerAgentFlowContext);
    }

    private void commandLineStartStopWAS(IAutomationAgentClient aaClient, boolean start) {
        String execCmd = null;
        if (start)
            execCmd = "startServer.bat";
        else
            execCmd = "stopServer.bat";

        RunCommandFlowContext context = new RunCommandFlowContext.Builder(execCmd)
                .workDir(this.wasBinDir)
                .args(Arrays.asList(this.serverName)).ignoreErrors()
                .build();
        runCommandFlow(aaClient, context);
    }

    /**
     * to assign app to work with listener port binding following property needs to be set.
     * com.ibm.websphere.management.application.dfltbndng.mdb.preferexisting=false
     *
     * @param aaClient
     */
    private void updateWsAdminPropertyFile(IAutomationAgentClient aaClient) {
        FileModifierFlowContext updatePropContext;
        String propFilePath = wasInstallDir + "\\profiles\\" + this.profileName
                + "\\properties\\wsadmin.properties";

        List<String> propUpdateArgs = new ArrayList<>();
        propUpdateArgs.add(propertyMDB);

        updatePropContext = new FileModifierFlowContext.Builder()
                .append(propFilePath, propUpdateArgs)
                .build();
        runFlowToUpdate(updatePropContext, aaClient);
    }

    private void createJmsResourcesAdmin(IAutomationAgentClient aaClient) {
        RunCommandFlowContext context;

        ArrayList<String> args = new ArrayList<>();
        args.add("-f");
        args.add(createJmsResource);

        context = new RunCommandFlowContext.Builder("wsadmin.bat")
                .args(args)
                .workDir(this.wasBinDir)
                .build();
        runCommandFlow(aaClient, context);
    }

    private void installMQLoanApp(IAutomationAgentClient aaClient) {
        RunCommandFlowContext context;
        context = new RunCommandFlowContext.Builder("ws_ant.bat")
                .workDir(this.wasBinDir).ignoreErrors()
                .build();
        runCommandFlow(aaClient, context);
    }

    private void deployMqLoanAppArchive(IAutomationAgentClient aaClient, String groupId, String artifactId, String version) {
        URL url = tasResolver.getArtifactUrl(new DefaultArtifact(groupId, artifactId, "zip", version));

        GenericFlowContext context = new GenericFlowContext.Builder()
                .artifactUrl(url)
                .destination(this.wasBinDir)
                .build();
        GenericFlowContext context2 = new GenericFlowContext.Builder()
                .artifactUrl(url)
                .destination(wasInstallDir)
                .build();

        runFlow(aaClient, GenericFlow.class, context2);
        runFlow(aaClient, GenericFlow.class, context);
    }

    private void updateDeployProperties(IAutomationAgentClient aaClient) {
        String deployFilePath = this.wasBinDir + "\\" + this.deployProps;
        String createJmsResPath = this.wasBinDir + "\\" + this.createJmsResource;
        /*Map<String, Integer> portMap = new HashMap<>(this.flowContext.getMqPort());
        int mqPortTemp = portMap.get(this.qmanagerName);*/

        FileModifierFlowContext propsContext;
        FileModifierFlowContext jmsContext;

        HashMap<String, String> fileMap = new HashMap<>();
        fileMap.put("\\[WAS_HOST\\]", tasResolver.getHostnameById(wasRoleId));
        fileMap.put("\\[WAS_HOME\\]", wasInstallDir.replaceAll("\\\\", "/"));
        fileMap.put("\\[QName1\\]", this.creditReplyQ);
        fileMap.put("\\[QName2\\]", this.creditRequestQ);
        fileMap.put("\\[QName3\\]", this.replyQ);
        fileMap.put("\\[QName4\\]", this.requestQ);
        fileMap.put("\\[QCFName\\]", "QueueCF");
        fileMap.put("\\[WSQCFName\\]", "WebServicesReplyQCF");
        fileMap.put("\\[MQHOST\\]", tasResolver.getHostnameById(ibmmqRole.getRoleId()));
        fileMap.put("\\[MQPORT\\]", Integer.toString(ibmmqRole.getMqPort(this.qmanagerName)));
        fileMap.put("\\[NODE\\]Cell", this.cellName);
        fileMap.put("\\[NODE\\]", this.nodeName);
        fileMap.put("\\[CELL\\]", this.cellName);
        fileMap.put("\\[QMGRNAME\\]", this.qmanagerName);
        fileMap.put("\\[SOAPJMS_APP\\]", "mqppSoapJmsEAR");
        fileMap.put("\\[SOAPJMS_APP_CLIENT\\]", "mqppSoapJmsClientEAR");
        fileMap.put("\\[SOAPJMS_APP_CONTEXT_ROOT\\]", "mqppSoapJms");
        fileMap.put("\\[SOAPJMS_APP_CLIENT_CONTEXT_ROOT\\]", "mqppSoapJmsClient");

        propsContext = new FileModifierFlowContext.Builder()
                .replace(deployFilePath, fileMap)
                .build();

        jmsContext = new FileModifierFlowContext.Builder()
                .replace(createJmsResPath, fileMap)
                .build();

        runFlowToUpdate(jmsContext, aaClient);
        runFlowToUpdate(propsContext, aaClient);
    }

    private void runFlowToUpdate(FileModifierFlowContext context, IAutomationAgentClient aaClient) {
        runFlow(aaClient, FileModifierFlow.class, context);
    }

    public static class Builder extends BuilderBase<Builder, MqJmsAppRole> {
        private final String roleId;
        private final ITasResolver tasResolver;
        public String wasBinDir;
        public String nodeName;
        public String cellName;
        public String profileName;
        public String serverName;
        public String creditReplyQ;
        public String creditRequestQ;
        public String qmanagerName;
        public String replyQ;
        public String requestQ;
        public String agentDirPath;
        public boolean setupAgent;
        public boolean wasIsAutostart;
        public IBMMQRole ibmmqRole;
        public String wasInstallDir;
        public String wasRoleId;

        protected boolean predeployed;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        protected MqJmsAppRole getInstance() {
            return new MqJmsAppRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public MqJmsAppRole build() {
            initProperties();
            Args.notNull(this.nodeName, "WAS profile Node name");
            Args.notNull(this.setupAgent, "flag to setup wily params in server.xml");
            Args.notNull(this.profileName, "was profile name");
            Args.notNull(this.creditReplyQ, "creditReplySOJQueue name");
            Args.notNull(this.creditRequestQ, "creditRequestSOJQueue name");
            Args.notNull(this.replyQ, "ReplySOJQueue name ");
            Args.notNull(this.requestQ, "RequestSOJQueue name ");
            Args.notNull(this.ibmmqRole, "ibm mq role");
            Args.notNull(this.qmanagerName, "QManager name");
            Args.notNull(this.wasInstallDir, "wasInstallDir");
            Args.notNull(this.wasRoleId, "wasRoleId");
            if (setupAgent) {
                Args.notNull(this.agentDirPath, "agent parameters to setup wily");
            }
            return getInstance();
        }

        private void initProperties() {
            wasBinDir(wasInstallDir + "\\bin");
            if (this.cellName == null) {
                cellName(this.nodeName + "Cell");
            }
        }

        public Builder wasBinDir(String pathToBin) {
            this.wasBinDir = pathToBin;
            return this.builder();
        }

        public Builder setupAgent(boolean setupAgent) {
            this.setupAgent = setupAgent;
            return this.builder();
        }

        public Builder agentDirPath(String agentDirPath) {
            this.agentDirPath = agentDirPath;
            return this.builder();
        }

        public Builder profileName(String profileName) {
            this.profileName = profileName;
            return this.builder();
        }

        public Builder nodeName(String nodeName) {
            this.nodeName = nodeName;
            return this.builder();
        }

        public Builder cellName(String cellName) {
            this.cellName = cellName;
            return this.builder();
        }

        public Builder creditRequestQ(String creditRequestQ) {
            this.creditRequestQ = creditRequestQ;
            return this.builder();
        }

        public Builder creditReplyQ(String creditReplyQ) {
            this.creditReplyQ = creditReplyQ;
            return this.builder();
        }

        public Builder replyQ(String replyQ) {
            this.replyQ = replyQ;
            return this.builder();
        }

        public Builder requestQ(String requestQ) {
            this.requestQ = requestQ;
            return this.builder();
        }

        public Builder qmanagerName(String qmanagerName) {
            this.qmanagerName = qmanagerName;
            return this.builder();
        }

        public Builder serverName(String serverName) {
            this.serverName = serverName;
            return this.builder();
        }

        public Builder ibmmqRole(IBMMQRole ibmmqRole) {
            this.ibmmqRole = ibmmqRole;
            return this.builder();
        }

        public Builder wasIsAutostart(boolean wasIsAutostart) {
            this.wasIsAutostart = wasIsAutostart;
            return this.builder();
        }

        public Builder websphereRole(WebSphere8Role wasRole) {
            this.wasInstallDir = wasRole.getInstallDir();
            this.wasRoleId = wasRole.getRoleId();
            return this.builder();
        }

        public Builder websphereRole(Websphere85Role wasRole) {
            this.wasInstallDir = wasRole.getInstallDir();
            this.wasRoleId = wasRole.getRoleId();
            return this.builder();
        }

        public Builder predeployed(boolean predeployed) {
            this.predeployed = predeployed;
            return this.builder();
        }

    }
}
