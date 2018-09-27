package com.ca.apm.systemtest.fld.role;

import com.ca.apm.automation.action.flow.utility.FileCreatorFlow;
import com.ca.tas.artifact.built.docker.ApmDbDockerImage;
import com.ca.tas.artifact.built.docker.ApmDockerImageBase;
import com.ca.tas.artifact.built.docker.ApmEmDockerImage;
import com.ca.tas.artifact.built.docker.ApmWvDockerImage;
import com.ca.tas.flow.docker.DockerCreateFlow;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.PhantomJSRole;
import com.ca.tas.role.TIMRole;
import com.ca.tas.role.docker.DockerCapable;
import com.ca.tas.role.docker.DockerRole;
import com.google.common.net.HostAndPort;
import org.apache.http.util.Args;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Docker EM role for FLD deployment
 *
 * Created by jirji01 on 1/17/2017.
 */
public class DockerEmRole extends AbstractRole implements DockerCapable {

    private final FileCreatorFlowContext laxConfigFileFlowContext;
    private final FileCreatorFlowContext introscopeConfigFileFlowContext;
    private final FileCreatorFlowContext agentConfigFileFlowContext;
    private final FileCreatorFlowContext tessDefaultConfigFileFlowContext;
    private final PhantomJSRole phantomJSRole;
    private final DockerRole dockerRole;
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerEmRole.class);

    private DockerEmRole(AbstractBuilder builder) {
        super(builder.roleId, builder.getEnvProperties());

        this.dockerRole = builder.dockerRole;

        this.laxConfigFileFlowContext = builder.laxConfigFileFlowContext;
        this.introscopeConfigFileFlowContext = builder.introscopeConfigFileFlowContext;
        this.agentConfigFileFlowContext = builder.agentConfigFileFlowContext;
        this.tessDefaultConfigFileFlowContext = builder.tessDefaultConfigFileFlowContext;

        this.phantomJSRole = builder.phantomJSRole;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        DockerEmRole.LOGGER.debug("Creating docker configuration files.");
        if (laxConfigFileFlowContext != null) {
            DockerEmRole.LOGGER.debug("Creating {} config file", laxConfigFileFlowContext.getDestinationPath());
            runFlow(aaClient, FileCreatorFlow.class, laxConfigFileFlowContext);
        }
        if (introscopeConfigFileFlowContext != null) {
            DockerEmRole.LOGGER.debug("Creating {} config file", introscopeConfigFileFlowContext.getDestinationPath());
            runFlow(aaClient, FileCreatorFlow.class, introscopeConfigFileFlowContext);
        }
        if (agentConfigFileFlowContext != null) {
            DockerEmRole.LOGGER.debug("Creating {} config file", agentConfigFileFlowContext.getDestinationPath());
            runFlow(aaClient, FileCreatorFlow.class, agentConfigFileFlowContext);
        }
        if (tessDefaultConfigFileFlowContext != null) {
            DockerEmRole.LOGGER.debug("Creating {} config file", tessDefaultConfigFileFlowContext.getDestinationPath());
            runFlow(aaClient, FileCreatorFlow.class, tessDefaultConfigFileFlowContext);
        }
    }

    @NotNull
    @Override
    public Collection<? extends IRole> dependentRoles() {
        Collection<IRole> dependentRoles = new ArrayList<>();

        dockerRole.after(this);
        dependentRoles.add(dockerRole);

        if (null != phantomJSRole) {
            DockerEmRole.LOGGER.debug("Phantom JS role is enabled");
            phantomJSRole.after(dockerRole);
            dependentRoles.add(phantomJSRole);
        }

        return dependentRoles;
    }



    @Override
    public String getName() {
        return this.dockerRole.getName();
    }

    public DockerRole getDockerRole() {
        return dockerRole;
    }

    public static class DbBuilder extends AbstractBuilder<DbBuilder> {

        public DbBuilder(String roleId, ITasResolver resolver) {
            super(roleId, resolver);
        }

        @Override
        public DbBuilder builder() {
            return this;
        }

        @Override
        void doBuild() {
            dockerBuilder.image(ApmDbDockerImage.IMAGE_NAME_APM_DB);

            if (!startDb) {
                dockerBuilder.noStart();
            }
        }
    }

    public static class WVBuilder extends AbstractBuilder<WVBuilder> {

        public WVBuilder(String roleId, ITasResolver resolver) {
            super(roleId, resolver);
            laxConfigFileName = "Introscope_WebView.lax";
            introscopeConfigFileName = "IntroscopeWebView.properties";
            agentConfigFileName = "IntroscopeAgent.profile";
        }

        void doBuild() {
            dockerBuilder
                    .image(ApmWvDockerImage.IMAGE_NAME_APM_WV)
                    .env("APM_CUSTOM_CONFIG_DIR", "/opt/ca/custom-config")
                    .volume(installDir + "/config", "/opt/ca/custom-config");

            if (!startEm) {
                dockerBuilder.noStart();
            }

            if (wvPort != null)
                introscopeProperties.put("introscope.webview.tcp.port", Integer.toString(wvPort));
            if (wvEmHost != null)
                introscopeProperties.put("introscope.webview.enterprisemanager.tcp.host", wvEmHost);
            if (emPort != null)
                introscopeProperties.put("introscope.webview.enterprisemanager.tcp.port", Integer.toString(emPort));
            if (emWebPort != null)
                introscopeProperties.put("introscope.webview.enterprisemanager.webserver.tcp.port", Integer.toString(emWebPort));
            if (wvEmHost != null)
                introscopeProperties.put("introscope.webview.enterprisemanager.rest.base", "http://" + wvEmHost + ":" + (emWebPort == null ? 8081 : emWebPort) + "/apm/appmap");

//            if (javaHeapSize != null) {
//                laxProperties.put("lax.nl.java.option.additional", "-Xms" + javaHeapSize +"m -Xmx" + javaHeapSize +"m -Djava.awt.headless=true " +
//                                "-Dorg.owasp.esapi.resources=./config/esapi -Dsun.java2d.noddraw=true " +
//                                "-Dorg.osgi.framework.bootdelegation=org.apache.xpath");
//            }
        }

        @Override
        public WVBuilder builder() {
            return this;
        }
    }

    public static class CollectorBuilder extends AbstractBuilder<CollectorBuilder> {

        public CollectorBuilder(String roleId, ITasResolver resolver) {
            super(roleId, resolver);
            laxConfigFileName = "Introscope_Enterprise_Manager.lax";
            introscopeConfigFileName = "IntroscopeEnterpriseManager.properties";
            tessDefaultConfigFileName = "tess-default.properties";
        }

        void doBuild() {
            Args.notBlank(dbHostname, "Database host name.");

            if (!startEm) {
                dockerBuilder.noStart();
            }

            dockerBuilder
                    .image(ApmEmDockerImage.IMAGE_NAME_APM_EM)
                    .env("APM_CUSTOM_CONFIG_DIR", "/opt/ca/custom-config")
                    .env("EM_DB_HOST", dbHostname)
                    .volume(installDir + "/logs", "/opt/ca/apm/logs")
                    .volume(installDir + "/config", "/opt/ca/custom-config")
                    .command("collector");

            if (emPort != null)
                introscopeProperties.put("introscope.enterprisemanager.port.channel1", Integer.toString(emPort));
            if (emWebPort != null)
                introscopeProperties.put("introscope.enterprisemanager.webserver.port", Integer.toString(emWebPort));
        }

        @Override
        protected CollectorBuilder builder() {
            return this;
        }

    }

    public static class MomBuilder extends AbstractBuilder<MomBuilder> {

        public MomBuilder(String roleId, ITasResolver resolver) {
            super(roleId, resolver);
            laxConfigFileName = "Introscope_Enterprise_Manager.lax";
            introscopeConfigFileName = "IntroscopeEnterpriseManager.properties";
        }

        void doBuild() {
            Args.notEmpty(collectors, "collectors");
            Args.check(collectors.size() <= 10, "maximum 10 collectors can be connected to MOM");
            Args.notBlank(dbHostname, "Database host name.");

//            if (javaHeapSize != null) {
//                laxProperties.put("lax.nl.java.option.additional", "-Xms%s" + javaHeapSize +"m -Xmx" + javaHeapSize +"m -Djava.awt.headless=true " +
//                                "-Dmail.mime.charset=UTF-8 -Dorg.owasp.esapi.resources=./config/esapi " +
//                                "-XX:+UseConcMarkSweepGC -XX:+UseParNewGC  -Xss512k");
//            }

            if (!startEm) {
                dockerBuilder.noStart();
            }

            HashMap<String, String> envs = new HashMap<>();
            int idx = 1;
            for (String collector : collectors) {
                envs.put("EM_COLLECTOR_HOST_" + idx, collector);
                idx++;
            }
            envs.put("APM_CUSTOM_CONFIG_DIR", "/opt/ca/custom-config");
            envs.put("EM_DB_HOST", dbHostname);

            dockerBuilder
                    .image(ApmEmDockerImage.IMAGE_NAME_APM_EM)
                    .envs(envs)
                    .volume(installDir + "/logs", "/opt/ca/apm/logs")
                    .volume(installDir + "/config", "/opt/ca/custom-config")
                    .volume(installDir + "/modules", "/transfer/modules")
                    .command("mom");

            if (emPort != null)
                introscopeProperties.put("introscope.enterprisemanager.port.channel1", Integer.toString(emPort));
            if (emWebPort != null)
                introscopeProperties.put("introscope.enterprisemanager.webserver.port", Integer.toString(emWebPort));
        }

        @Override
        public MomBuilder builder() {
            return this;
        }

    }
    
    public abstract static class AbstractBuilder<T extends AbstractBuilder<T>> extends BuilderBase<T, DockerEmRole> {

        private static final String PHANTOMJS_ROLE_SUFFIX = "phantomJS";
        private static final String CONFIGURE_TIM_SCRIPT = "/com/ca/tas/role/configureTimPhantomJs.js";

        public final String roleId;
        public final ITasResolver tasResolver;

        HostAndPort imageRegistry = ApmDockerImageBase.REGISTRY_SCX;
        String version = "99.99.sys-SNAPSHOT";
        ArrayList<String> collectors = new ArrayList<>();
        protected String installDir;
        HashMap<String, String> laxProperties = new HashMap<>();
        String laxConfigFileName;
        FileCreatorFlowContext laxConfigFileFlowContext;
        HashMap<String, String> introscopeProperties = new HashMap<>();
        String introscopeConfigFileName;
        FileCreatorFlowContext introscopeConfigFileFlowContext;
        HashMap<String, String> tessDefaultProperties = new HashMap<>();
        String tessDefaultConfigFileName;
        FileCreatorFlowContext tessDefaultConfigFileFlowContext;
        HashMap<String, String> agentProperties = new HashMap<>();
        String agentConfigFileName;
        FileCreatorFlowContext agentConfigFileFlowContext;

        Integer emWebPort;
        Integer emPort;

        String wvEmHost;
        Integer wvPort;
        boolean startDb = false;
        boolean startEm = true;
        String dbHostname;

        HashMap<String, String> transactionMonitors = new HashMap<>();

        DockerRole.LinuxBuilder dockerBuilder;

        PhantomJSRole phantomJSRole;
        DockerRole dockerRole;
        String exposeFolder;

        AbstractBuilder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            this.dockerBuilder = new DockerRole.LinuxBuilder("docker_" + roleId);
        }

        public T wvEmHost(String emHost) {
            this.wvEmHost = emHost;
            return builder();
        }

        public T wvPort(int port) {
            this.wvPort = port;
            return builder();
        }

        public T emPort(int port) {
            this.emPort = port;
            return builder();
        }

        public T emWebPort(int emWebPort) {
            this.emWebPort = emWebPort;
            return builder();
        }

        public T nostartEM() {
            this.startEm = false;
            return builder();
        }

        public T nostartWV() {
            this.startEm = false;
            return builder();
        }

        public T version(String version) {
            this.version = version;
            return builder();
        }

        public T imageRegistry(HostAndPort imageRegistry) {
            this.imageRegistry = imageRegistry;
            return builder();
        }
        
        public T installDir(String installDir) {
            this.installDir = installDir;
            return builder();
        }

        public T wvLaxNlClearJavaOption(Collection<String> options) {
            StringBuilder sb = new StringBuilder();
            for (String option : options) {
                sb.append(option);
                sb.append(' ');
            }
            return laxProperty("lax.nl.java.option.additional", sb.toString());
        }

        public T emLaxNlClearJavaOption(Collection<String> options) {
            StringBuilder sb = new StringBuilder();
            for (String option : options) {
                sb.append(option);
                sb.append(' ');
            }
            return laxProperty("lax.nl.java.option.additional", sb.toString());
        }

        public T laxProperty(String key, String value) {
            laxProperties.put(key, value);
            return builder();
        }

        public T configProperty(String key, String value) {
            introscopeProperties.put(key, value);
            return builder();
        }

        public T tessDefaultProperty(String key, String value) {
            tessDefaultProperties.put(key, value);
            return builder();
        }

//        public T dbuser(String username) {
//            this.dbUsername = username;
//            return builder();
//        }
//
//        public T dbpassword(String password) {
//            this.dbPassword = password;
//            return builder();
//        }
//
//        public T dbAdminUser(String adminUsername) {
//            this.dbAdminUsername = adminUsername;
//            return builder();
//        }
//
//        public T dbAdminPassword(String adminPassword) {
//            this.dbAdminPassword = adminPassword;
//            return builder();
//        }

        public T dbhost(String dbhost) {
            this.dbHostname = dbhost;
            return builder();
        }

        public T autostartApmSqlServer() {
            this.startDb = true;
            return builder();
        }

        public T emCollector(IRole collectorRole) {
            collectors.add(tasResolver.getHostnameById(collectorRole.getRoleId()));
            return builder();
        }

        public T tim(TIMRole tim) {
//            String timHostName = this.tasResolver.getHostnameById(tim.getRoleId());
//
//            try {
//                String timIPAddress = InetAddress.getByName(timHostName).getHostAddress();
//                this.transactionMonitors.put(timHostName, timIPAddress);
//            } catch (UnknownHostException var4) {
//                throw new IllegalStateException("Unable to resolve IP Address for TIM host " + timHostName + " (" + tim.getRoleId() + ")");
//            }

            return this.builder();
        }

        public T exposeFolder(String folderName) {
            this.exposeFolder = folderName;
            return this.builder();
        }

        abstract protected T builder();

        @Override
        protected DockerEmRole getInstance() {
            return new DockerEmRole(this);
        }

        public T agentProperty(String key, String value) {
            // INSTALL_DIR+"/product/webview/agent/wily/core/config/IntroscopeAgent.profile"
            agentProperties.put(key, value);
            return builder();
        }

        abstract void doBuild();

        @Override
        public DockerEmRole build() {

            doBuild();

            if (exposeFolder != null && !exposeFolder.isEmpty()) {
                dockerBuilder.volume(installDir + "/" + exposeFolder, "/opt/ca/exposed-" + exposeFolder);
//                dockerBuilder.customContainerCommand(Arrays.asList("mkdir", "-p", "/opt/ca/exposed-" + exposeFolder));
                dockerBuilder.customContainerCommand(Arrays.asList("/bin/bash", "-c", "cp -rf '/opt/ca/apm/" + exposeFolder + "/'* '/opt/ca/exposed-" + exposeFolder + "'"));
            }

            dockerRole = dockerBuilder
                .networkMode(DockerCreateFlow.DockerNetworkMode.HOST)
                .registry(imageRegistry)
                .version(version)
                .build();

            laxConfigFileFlowContext = prepareConfigFile(laxProperties, laxConfigFileName);
            introscopeConfigFileFlowContext = prepareConfigFile(introscopeProperties, introscopeConfigFileName);
            tessDefaultConfigFileFlowContext = prepareConfigFile(tessDefaultProperties, tessDefaultConfigFileName);
            agentConfigFileFlowContext = prepareConfigFile(agentProperties, agentConfigFileName);

            if (startEm && !transactionMonitors.isEmpty()) {
                PhantomJSRole.LinuxBuilder phantomJSRoleBuilder = new PhantomJSRole.LinuxBuilder(roleId + PHANTOMJS_ROLE_SUFFIX, tasResolver);

                for (String timHost : transactionMonitors.keySet()) {
                    String timIPAddress = transactionMonitors.get(timHost);
                    String emHost = tasResolver.getHostnameById(roleId);
                    String emWebPort = String.valueOf(AbstractBuilder.this.emWebPort);

                    phantomJSRoleBuilder.scripts(CONFIGURE_TIM_SCRIPT, emHost, emWebPort, timHost, timIPAddress);
                }

                phantomJSRole = phantomJSRoleBuilder.build();
            }

            return getInstance();
        }

        private FileCreatorFlowContext prepareConfigFile(HashMap<String, String> properties, String fileName) {
            Args.notNull(properties, "configuration data");

            FileCreatorFlowContext flowContext = null;
            if (!properties.isEmpty()) {
                Args.notBlank(fileName, "configuration file name");
                DockerEmRole.LOGGER.debug("Preparing config EM docker file {} context.", fileName);

                Collection<String> data = new ArrayList<>();

                for (Map.Entry<String, String> property : properties.entrySet()) {
                    String line = property.getKey() + '=' + property.getValue();
                    data.add(line);
                    DockerEmRole.LOGGER.debug("{} - {}", fileName, line);
                }

                flowContext = new FileCreatorFlowContext.Builder()
                    .destinationPath(installDir + "/config/" + fileName)
                    .fromData(data)
                    .build();
            }
            return flowContext;
        }
    }
}
