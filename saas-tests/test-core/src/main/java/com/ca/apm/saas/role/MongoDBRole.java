/**
 * 
 */
package com.ca.apm.saas.role;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.redhat.YumRepositoryFlow;
import com.ca.apm.automation.action.flow.redhat.YumRepositoryFlowContext;
import com.ca.apm.automation.action.flow.utility.ConfigureFlow;
import com.ca.apm.automation.action.flow.utility.ConfigureFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.linux.YumInstallPackageRole;

/**
 * @author zheji01@ca.com
 *
 */
public class MongoDBRole extends AbstractRole
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBRole.class);

    /** Constant <code>MONGODB_START="mongodbStart"</code> */
    public static final String MONGODB_START = "mongodbStart";
    /** Constant <code>MONGODB_STOP="mongodbStop"</code> */
    public static final String MONGODB_STOP = "mongodbStop";

    private final YumRepositoryFlowContext yumRepositoryContext;
    private final YumInstallPackageRole yumInstallMongoDBRole;
    private final RunCommandFlowContext stopCommandContext;
    private final RunCommandFlowContext startCommandContext;
    private final ConfigureFlowContext configureMongoDBContext;
    private final boolean autoStart;

    /**
     * <p>Constructor for MongoDBRole.</p>
     *
     * @param builder Builder object containing all necessary data
     */
    protected MongoDBRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());

        yumRepositoryContext = builder.yumRepositoryContext;
        yumInstallMongoDBRole = builder.yumInstallMongoDBRole;
        startCommandContext = builder.startMongoDBContext;
        stopCommandContext = builder.stopMongoDBContext;
        configureMongoDBContext = builder.configureMongoDBContext;
        autoStart = builder.autoStart;
    }

    /** {@inheritDoc} */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        addMongoDBRepository(aaClient);
        installMongoDB(aaClient);
        configureMongoDB(aaClient);

        if (autoStart) {
            start(aaClient);
        }
    }

    /**
     * Linux only
     *
     * @param aaClient a {@link com.ca.tas.client.IAutomationAgentClient} object.
     */
    protected void addMongoDBRepository(IAutomationAgentClient aaClient) {
        if (yumRepositoryContext == null) {
            return;
        }

        runFlow(aaClient, YumRepositoryFlow.class, yumRepositoryContext);
    }

    /**
     * <p>installMongoDB.</p>
     *
     * @param aaClient a {@link com.ca.tas.client.IAutomationAgentClient} object.
     */
    protected void installMongoDB(IAutomationAgentClient aaClient) {
        if (yumInstallMongoDBRole == null) {
            return;
        }

        assert getHostingMachine() != null;

        getHostingMachine().addRole(yumInstallMongoDBRole);
        
        int maxAttempts = 5;
        int attempts = 0;
        boolean success= false;
        
		while (!success && attempts++ < maxAttempts) {
			try {
				LOGGER.info("install attempt: " + attempts);
				yumInstallMongoDBRole.deploy(aaClient);
				success = true;
			} catch (IllegalStateException e) {
				LOGGER.info("got exception while installing mongo db role: " + e.getMessage());
			}
		}
        
    }

    /**
     * <p>configureMongoDB.</p>
     *
     * @param aaClient a {@link com.ca.tas.client.IAutomationAgentClient} object.
     */
    protected void configureMongoDB(IAutomationAgentClient aaClient) {
        if (configureMongoDBContext == null) {
            return;
        }

        runFlow(aaClient, ConfigureFlow.class, configureMongoDBContext);
    }

    /**
     * <p>start.</p>
     *
     * @param aaClient a {@link com.ca.tas.client.IAutomationAgentClient} object.
     */
    public void start(IAutomationAgentClient aaClient) {
        runCommandFlow(aaClient, startCommandContext);
    }

    /**
     * <p>stop.</p>
     *
     * @param aaClient a {@link com.ca.tas.client.IAutomationAgentClient} object.
     */
    public void stop(IAutomationAgentClient aaClient) {
        runCommandFlow(aaClient, stopCommandContext);
    }

    /**
     * <p>isAutoStart.</p>
     *
     * @return a boolean.
     */
    public boolean isAutoStart() {
        return autoStart;
    }

    /**
     * <p>Getter for the field <code>yumRepositoryContext</code>.</p>
     *
     * @return a {@link com.ca.apm.automation.action.flow.redhat.YumRepositoryFlowContext} object.
     */
    public YumRepositoryFlowContext getYumRepositoryContext() {
        return yumRepositoryContext;
    }

    /**
     * <p>Getter for the field <code>startCommandContext</code>.</p>
     *
     * @return a {@link com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext} object.
     */
    public RunCommandFlowContext getStartCommandContext() {
        return startCommandContext;
    }

    /**
     * <p>Getter for the field <code>stopCommandContext</code>.</p>
     *
     * @return a {@link com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext} object.
     */
    public RunCommandFlowContext getStopCommandContext() {
        return stopCommandContext;
    }

    /**
     * <p>Getter for the field <code>yumInstallMongoDBRole</code>.</p>
     *
     * @return a {@link com.ca.tas.role.linux.YumInstallPackageRole} object.
     */
    public YumInstallPackageRole getYumInstallMongoDBRole() {
        return yumInstallMongoDBRole;
    }

    /**
     * Linux Builder responsible for holding all necessary properties to instantiate {@link MongoDBRole}
     */
    public static class LinuxBuilder extends Builder {

        private static final String YUM_INSTALL_ROLE_ID = "yumInstallRole";
        private static final String MONGODB_PACKAGE = "mongodb-org";
        private static final String LINUX_CONFIG_FILE = "/etc/mongod.conf";
        private static final String TIXCHANGE_CONFIG_FILE = "/opt/automation/deployed/tixchangeNode/server/config.json";
        private static final String MONGODB_DEAMON_CMD = "mongod";

        protected MongoDBYumRepository yumRepo;

        public LinuxBuilder(String roleId) {
            super(roleId);
        }

        @Override
        public MongoDBRole build() {
            //to get base builder verification
            super.build();

            initMongoDBRepo();
            initYumInstallRole();

            MongoDBRole role = getInstance();
            
            Args.notNull(role.yumRepositoryContext, "MongoDB yum repository for linux builder");
            Args.notNull(role.yumInstallMongoDBRole, "MongoDB yum install role for linux builder");

            return role;
        }

        protected void initMongoDBRepo() {
            if (yumRepo == null) {
                yumRepo = defaultMongoDBYumRepo();
            }
            yumRepositoryContext = new YumRepositoryFlowContext.Builder(yumRepo).build();
        }

        protected MongoDBYumRepository defaultMongoDBYumRepo() {
            return MongoDBYumRepository.v32;
        }

        protected void initYumInstallRole() {
            yumInstallMongoDBRole = new YumInstallPackageRole.Builder(YUM_INSTALL_ROLE_ID)
                .addPackage(MONGODB_PACKAGE)
                .build();
        }

        @Override
        protected void initStartStopCommands() {
            startMongoDBContext = getCommand("start", "mongodb-start");
            getEnvProperties().add(MONGODB_START, startMongoDBContext);
            stopMongoDBContext = getCommand("stop", "mongodb-stop");
            getEnvProperties().add(MONGODB_STOP, stopMongoDBContext);
        }

        protected RunCommandFlowContext getCommand(String command, String name) {
            return new RunCommandFlowContext.Builder("service").args(Arrays.asList(MONGODB_DEAMON_CMD, command)).name(name).build();
        }

        @Override
        protected void initConfigureFlow() {
            if (configurationProps.isEmpty()) {
                return;
            }
            configureMongoDBContext = new ConfigureFlowContext.Builder().configurationMap(LINUX_CONFIG_FILE, configurationProps).build();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        public Builder repoVersion(MongoDBYumRepository yumRepo) {
            this.yumRepo = yumRepo;

            return builder();
        }

        @Override
        protected String getDeployBase() {
            return getLinuxDeployBase();
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }
    }

    /**
     * Builder responsible for holding all necessary properties to instantiate {@link MongoDBRole}
     */
    public static class Builder extends BuilderBase<Builder, MongoDBRole> {

        //todo
        private final String roleId;
        protected final Map<String, String> configurationProps = new HashMap<>();

        protected YumInstallPackageRole yumInstallMongoDBRole;
        protected YumRepositoryFlowContext yumRepositoryContext;
        protected RunCommandFlowContext startMongoDBContext;
        protected RunCommandFlowContext stopMongoDBContext;
        protected ConfigureFlowContext configureMongoDBContext;
        protected boolean autoStart;

        public Builder(String roleId) {
            this.roleId = roleId;
        }

        @Override
        public MongoDBRole build() {
            initStartStopCommands();
            initConfigureFlow();

            return getInstance();
        }

        protected void initConfigureFlow() {
            //todo
        }

        protected void initStartStopCommands() {
            //todo
        }

        @Override
        protected MongoDBRole getInstance() {
            return new MongoDBRole(this);
        }

        public Builder autoStart() {
            autoStart = true;
            return builder();
        }

        public Builder configure(String key, String value) {
            configurationProps.put(key, value);

            return builder();
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }

}
