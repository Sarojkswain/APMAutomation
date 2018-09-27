package com.ca.apm.systemtest.fld.role;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.WebLogicRole;
import org.apache.commons.lang.StringUtils;
import org.apache.http.util.Args;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author rsssa02
 */
public class PPWLSStockTraderRole extends AbstractRole {

    public static final String EP_WEBAPP_START = "webappStart";
    public static final String EP_WEBAPP_STOP = "webappStop";

    public static final String DEPLOY_BASE = "c:\\automation\\deployed\\";
    public static final String DOMAIN_HOME = DEPLOY_BASE + "webapp\\stocktraderdomain";

    private static final Logger LOGGER = LoggerFactory.getLogger(PPWLSStockTraderRole.class);
    private static final String DATABASE_JDBC_URL = "jdbc:oracle:thin:@[HOST]:1521:tradedb";
    private static final String DATABASE_DRIVER_NAME = "oracle.jdbc.xa.client.OracleXADataSource";
    private static final String DATABASE_USER_NAME = "TRADE";
    private static final String DATABASE_JDBC_NAME = "tradedb";
    private static final String DATABASE_ENCRYPTED_USER_PASSWORD = "TRADE";
    private static final String DATABASE_KEEPALIVE_QUERY = "SQL SELECT 1 FROM dual";
    private static final String DB_MIN_POOL = "50";
    private static final String DB_MAX_POOL = "200";
    private static final String DATABASE_JNDI_NAME = "Trade";
    private ITasResolver tasResolver;
    private String roleId;
    private String dbRole;
    private String serverPort;
    private JavaRole javaRole;
    private WebLogicRole weblogicRole;
    private String domainHomeDir;
    private String startCmdExec;
    private String stopCmdExec;

    public PPWLSStockTraderRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.roleId = builder.roleId;
        this.serverPort = builder.serverPort;
        this.javaRole = builder.javaRole;
        this.dbRole = builder.dbRole;
        this.domainHomeDir = builder.domainHomeDir;
        this.startCmdExec = builder.startCmdExec;
        this.stopCmdExec = builder.stopCmdExec;
        this.weblogicRole = builder.webLogicRole;
        this.tasResolver = builder.tasResolver;
    }
    public String getDomainHomeDir(){
        return this.domainHomeDir;
    }
    public String getStartCmdExec(){
        return this.startCmdExec;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        deployArtifacts(aaClient);
        updatePODomainConfig(aaClient);
        updatePODomainJDBC(aaClient);
        updatePODomainsetDomainEnv(aaClient);
        updatePODomainstartRootWLSCMD(aaClient);
        updatePODomainStartWLSCMD(aaClient);
        updatePODomainStopWLSCMD(aaClient);

    }

    private void deployArtifacts(IAutomationAgentClient aaClient) {
        GenericFlowContext context;

        LOGGER.info("Installing domain for stocktrader app...");

        context = new GenericFlowContext.Builder()
                .artifactUrl(tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.binaries.weblogic", "stocktraderdomain", "zip", "10.3.3")))
                .destination(DOMAIN_HOME)
                .build();
        runFlow(aaClient, GenericFlow.class, context);

        context = new GenericFlowContext.Builder()
                .artifactUrl(tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.coda", "wls-stocktrader", "zip", "10.3")))
                .destination(DOMAIN_HOME + "\\applications")
                .build();
        runFlow(aaClient, GenericFlow.class, context);
    }

    private void updatePODomainConfig(IAutomationAgentClient aaClient) {

        FileModifierFlowContext context = null;
        Map<String,String> replacePairs = new HashMap<String,String>();

        replacePairs.put("\\[WEBLOGIC.SERVER.PORT\\]", serverPort);
        replacePairs.put("\\[WEBLOGIC.SERVER.HOST\\]", tasResolver.getHostnameById(weblogicRole.getRoleId()));

        String fileName =  DOMAIN_HOME + "/config/config.xml";
        // replacing values
        context = new FileModifierFlowContext.Builder()
                .replace(fileName, replacePairs)
                .build();
        runFlow(aaClient, FileModifierFlow.class, context);

    }

    private void updatePODomainJDBC(IAutomationAgentClient aaClient) {

        FileModifierFlowContext context = null;
        Map<String, String> replacePairs = new HashMap<String,String>();

        replacePairs.put("\\[DATABASE.JDBC.URL\\]", DATABASE_JDBC_URL.replace("[HOST]",tasResolver.getHostnameById(dbRole)));
        replacePairs.put("\\[DATABASE.DRIVER.NAME\\]", DATABASE_DRIVER_NAME);
        replacePairs.put("\\[DATABASE.USER.NAME\\]",DATABASE_USER_NAME);
        replacePairs.put("\\[DATABASE.JDBC.NAME\\]", DATABASE_JDBC_NAME);
        replacePairs.put("\\[DATABASE.ENCRYPTED.USER.PASSWORD\\]", DATABASE_ENCRYPTED_USER_PASSWORD);
        replacePairs.put("\\[DATABASE.KEEPALIVE.QUERY\\]", DATABASE_KEEPALIVE_QUERY);
        replacePairs.put("\\[DB.MIN.POOL\\]", DB_MIN_POOL);
        replacePairs.put("\\[DB.MAX.POOL\\]", DB_MAX_POOL);
        replacePairs.put("\\[DATABASE.JNDI.NAME\\]", DATABASE_JNDI_NAME);
        replacePairs.put("\\[DATABASE.HOST.NAME\\]", tasResolver.getHostnameById(dbRole));


        String fileName =  DOMAIN_HOME + "/config/jdbc/StockTrade-jdbc.xml";

        // replacing values
        context = new FileModifierFlowContext.Builder()
                .replace(fileName, replacePairs)
                .build();
        runFlow(aaClient, FileModifierFlow.class, context);
    }

    private void updatePODomainstartRootWLSCMD(IAutomationAgentClient aaClient) {

        FileModifierFlowContext context = null;
        Map<String, String> replacePairs = new HashMap<String,String>();

        replacePairs.put("\\[DOMAIN.HOME.DIR\\]",  pathResolver(DOMAIN_HOME) );

        String fileName =  DOMAIN_HOME + "/startWebLogic.cmd";

        context = new FileModifierFlowContext.Builder()
                .replace(fileName, replacePairs)
                .build();
        runFlow(aaClient, FileModifierFlow.class, context);
    }

    private void updatePODomainsetDomainEnv(IAutomationAgentClient aaClient) {

        FileModifierFlowContext context = null;
        Map<String, String> replacePairs = new HashMap<String,String>();

        replacePairs.put("\\[WLS.HOME\\]", pathResolver(weblogicRole.getInstallDir()));
        replacePairs.put("\\[JAVA.HOME\\]", pathResolver(javaRole.getInstallDir()));
        replacePairs.put("\\[DOMAIN.HOME.DIR\\]", pathResolver(DOMAIN_HOME) );
        replacePairs.put("-Xms\\[MIN.HEAP.SIZE\\]m -Xmx\\[MAX.HEAP.SIZE\\]m","-Xms512m -Xmx752m");
        replacePairs.put("\\[PERM.SPACE.SIZE\\]","50");
        replacePairs.put("\\[MAX.PERM.SPACE.SIZE\\]","200");
        replacePairs.put("\\[RESULTS.OUTPUT.DIR\\]", pathResolver(DOMAIN_HOME));

        String fileName =  DOMAIN_HOME + "/bin/setDomainEnv.cmd";

        context = new FileModifierFlowContext.Builder()
                .replace(fileName, replacePairs)
                .build();
        runFlow(aaClient, FileModifierFlow.class, context);
    }

    private void updatePODomainStartWLSCMD(IAutomationAgentClient aaClient) {

        FileModifierFlowContext context = null;
        Map<String, String> replacePairs = new HashMap<String,String>();

        replacePairs.put("\\[DOMAIN.HOME.DIR\\]",  pathResolver(DOMAIN_HOME) );
        replacePairs.put("\\[WLS.LOG.OUTPUT\\]",  pathResolver((DOMAIN_HOME) + "/WebLogicConsole.log"));
        replacePairs.put("\\[OTHER.JAVA.OPTIONS\\]","");
        replacePairs.put("\\[HEAPMONITOR.JAR\\]","");

        String fileName =  DOMAIN_HOME + "/bin/startWebLogic.cmd";

        context = new FileModifierFlowContext.Builder()
                .replace(fileName, replacePairs)
                .build();
        runFlow(aaClient, FileModifierFlow.class, context);
    }

    private void updatePODomainStopWLSCMD(IAutomationAgentClient aaClient) {

        FileModifierFlowContext context = null;
        Map<String, String> replacePairs = new HashMap<String,String>();

        replacePairs.put("\\[DOMAIN.HOME.DIR\\]",  pathResolver(DOMAIN_HOME) );
        replacePairs.put("\\[WEBLOGIC.SERVER.HOST\\]", tasResolver.getHostnameById(weblogicRole.getRoleId()));
        replacePairs.put("\\[WEBLOGIC.SERVER.PORT\\]", serverPort);
        replacePairs.put("'%SERVER_NAME%','Server'", "'%SERVER_NAME%','Server',ignoreSessions='true',force='true'");

        String fileName =  DOMAIN_HOME + "/bin/stopWebLogic.cmd";

        context = new FileModifierFlowContext.Builder()
                .replace(fileName, replacePairs)
                .build();
        runFlow(aaClient, FileModifierFlow.class, context);
    }

    private String pathResolver(String dirPath){
        String resolvedPath = null;
        if(null == dirPath){
            return dirPath;
        } else {
            resolvedPath = StringUtils.replace(dirPath, "/", "\\");
            resolvedPath = StringUtils.replace(resolvedPath, "\\", "\\\\");
            //LOGGER.info("resolving path..! "+ dirPath + " - to - " + resolvedPath);
//            LOGGER.info("resolving path..! "+ dirPath );
        }
        return resolvedPath;
    }

    public static class Builder extends BuilderBase<Builder, PPWLSStockTraderRole> {

        private final String roleId;
        private final ITasResolver tasResolver;
        protected String appserverDir;
        protected String serverName;
        protected String serverPort;
        protected String dbRole;
        protected String domainHomeDir = DOMAIN_HOME;
        protected RunCommandFlowContext startCommandFlowContext;
        protected RunCommandFlowContext stopCommandFlowContext;
        protected String startCmdExec;
        protected String stopCmdExec;

        protected WebLogicRole webLogicRole;
        protected JavaRole javaRole;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        protected PPWLSStockTraderRole getInstance() {
            return new PPWLSStockTraderRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public PPWLSStockTraderRole build() {
            initStartStopCommandFlow();
            Args.notNull(serverPort, "server port number");
            Args.notNull(webLogicRole, "weblogic role name");
            Args.notNull(javaRole, "jvm role name");
            Args.notNull(dbRole, "Oracle DB role name");
            return getInstance();
        }

        private void initStartStopCommandFlow() {
            if(startCmdExec == null) {
                this.startCmdExec(domainHomeDir + "/bin/startWebLogic.cmd");
            }
            if(stopCmdExec == null) {
                this.stopCmdExec(domainHomeDir + "/bin/stopWebLogic.cmd");
            }
            startCommandFlowContext = (new com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext.Builder(this.startCmdExec)).terminateOnMatch("Server started in RUNNING mode").build();
            getEnvProperties().add(EP_WEBAPP_START, startCommandFlowContext);
            stopCommandFlowContext = (new com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext.Builder(this.stopCmdExec)).build();
            getEnvProperties().add(EP_WEBAPP_STOP, stopCommandFlowContext);
        }

        public Builder startCmdExec(String cmdExec) {
            this.startCmdExec = cmdExec;
            return builder();
        }
        public Builder stopCmdExec(String cmdExec) {
            this.stopCmdExec = cmdExec;
            return builder();
        }

        public Builder appserverDir(String appserverDir) {
            this.appserverDir = appserverDir;
            return builder();
        }

        public Builder webLogicRole(WebLogicRole webLogicRole) {
            this.webLogicRole = webLogicRole;
            return builder();
        }

        public Builder dbRole(String dbRole){
            this.dbRole = dbRole;
            return builder();
        }

        public Builder serverName(String serverName) {
            this.serverName = serverName;
            return builder();
        }

        public Builder serverPort(String serverPort) {
            this.serverPort = serverPort;
            return builder();
        }

        public Builder javaRole(JavaRole javaRole) {
            this.javaRole = javaRole;
            return builder();
        }
    }
}
