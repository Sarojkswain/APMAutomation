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

package com.ca.apm.powerpack.sysview.tests.role;

import com.ca.apm.automation.action.flow.FlowConfig.FlowConfigBuilder;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.mainframe.ConfigureEncodedFlow;
import com.ca.apm.automation.action.flow.mainframe.ConfigureEncodedFlowContext;
import com.ca.apm.automation.action.flow.mainframe.ControlMvsTaskFlow;
import com.ca.apm.automation.action.flow.mainframe.ControlMvsTaskFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.automation.action.test.PortUtils;
import com.ca.apm.automation.utils.CommonUtils;
import com.ca.apm.automation.utils.mainframe.MvsTask;
import com.ca.apm.automation.utils.mainframe.MvsTask.State;
import com.ca.apm.powerpack.sysview.tests.role.SysviewRole.SysviewConfig;
import com.ca.tas.annotation.TasEnvironmentProperty;
import com.ca.tas.annotation.TasResource;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.EmRole;

import org.apache.http.util.Args;
import org.eclipse.aether.artifact.DefaultArtifact;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;

/**
 * Deployment and configuration of CE-APM agent.
 */
public class CeapmRole extends AbstractRole {

    /** Default configuration */
    public static final CeapmConfig CEAPM_DEFAULT = CeapmConfig.POX;

    private String jobName;

    /** Parameters to be sent to started task on mainframe. Example: Java version parameters */
    private Map<String, String> taskParms;

    private static final String CEAPM_AGENT_TAR = "ceapm-agent.tar";

    /** TAS property that provides CEAPM home directory. */
    public static final String CEAPM_HOME_PROPERTY = "ceapm.home";
    /** TAS property that contains started task name. */
    public static final String CEAPM_START_TASK_PROPERTY = "ceapm.start.task";
    /** TAS property that contains task start parameters. */
    public static final String CEAPM_START_PARAMS_PROPERTY = "ceapm.start.parameters";
    /** TAS property that contains SMF port. */
    public static final String CEAPM_SMF_PORT_PROPERTY = "ceapm.smf.port";

    private URL agentUrl;
    private String installLocation;

    private ConfigureEncodedFlowContext configCtx;

    private int smfPort;

    protected CeapmRole(Builder builder) {
        super(builder.roleId);
    }

    /** last component of extracted agent path */
    private static final String CEAPM_HOME = "/Cross-Enterprise_APM/";

    public static final String CEAPM_PROPERTIES_ENCODING = "Cp1047";
    public static final String CEAPM_PROPERTIES_FILE =
        "config/Cross-Enterprise_APM_Dynamic.properties";
    public static final String CEAPM_PROFILE_FILE =
        "config/Introscope_Cross-Enterprise_APM.profile";

    public static final String CEAPM_PROCESS_NAME_DEFAULT = "Cross-Enterprise APM Process";
    public static final String CEAPM_AGENT_NAME_DEFAULT = "Cross-Enterprise APM Agent";

    /** Last GA version. */
    public static final String CEAPM_GA_VERSION = "10.2.0.16";

    /** Java for last GA version. */
    public static final CeapmJavaConfig CEAPM_GA_JAVA_VERSION = CeapmJavaConfig.JVM7;

    /** Minimal metric polling interval [s]. */
    public static final int CEAPM_MIN_UPDATE_VALUE = 15;

    public static final String UPDATE_INTERVAL_PROPERTY = "SYSVIEW.update.interval";

    /** Default Java version. */
    private static final CeapmJavaConfig DEFAULT_JAVA_VERSION = CeapmJavaConfig.JVM7;
    /** Currently supported Java versions. */
    public static final CeapmJavaConfig[] SUPPORTED_JAVA_VERSIONS = {CeapmJavaConfig.JVM6,
            CeapmJavaConfig.JVM7, CeapmJavaConfig.JVM8};
    static {
        assert Arrays.asList(SUPPORTED_JAVA_VERSIONS).contains(DEFAULT_JAVA_VERSION);
    }

    /**
     * Deploy CE-APM agent and update configuration files.
     *
     * @param aaClient Automation client to run flows on automation agent
     */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        // Stop the task in case its still running from a previous execution
        stopTask(aaClient);

        // Make sure the intended work directory exists
        runCommand("mkdir -p " + installLocation, aaClient);

        // Target directory cleanup
        runCommand("rm -r * ; exit 0", aaClient);

        // Install package downloaded from artifactory
        downloadAgent(aaClient);

        // Unpack it
        runCommand("tar -xfo " + CEAPM_AGENT_TAR, aaClient);

        // Basic agent configuration (.profile and .properties)
        updateConfig(aaClient);

        // Give everyone access to the agent files (this is for debugging purposes)
        runCommand("chmod -R 777 *", aaClient);
    }

    /**
     * Run shell command in installation directory. Directory must exist otherwise flow ends with
     * IOException.
     *
     * @param command USS shell command
     * @param aaClient Automation client to run flows on automation agent
     */
    protected void runCommand(String command, IAutomationAgentClient aaClient) {
        String[] args = {"-c", command};

        RunCommandFlowContext cmdCtx =
            new RunCommandFlowContext.Builder("sh").workDir(installLocation)
                .doNotPrependWorkingDirectory().args(Arrays.asList(args)).name(getRoleId()).build();

        aaClient
            .runJavaFlow(new FlowConfigBuilder(RunCommandFlow.class, cmdCtx, getHostWithPort()));
    }

    /**
     * Download CE-APM agent.
     *
     * @param aaClient Automation client to run flows on automation agent
     */
    protected void downloadAgent(IAutomationAgentClient aaClient) {
        GenericFlowContext ctx =
            new GenericFlowContext.Builder().artifactUrl(agentUrl)
                .destination(installLocation + "/" + CEAPM_AGENT_TAR).notArchive().build();

        runFlow(aaClient, GenericFlow.class, ctx);
    }

    /**
     * Update profile and properties configuration files.
     *
     * @param aaClient Automation client to run flows on automation agent
     */
    protected void updateConfig(IAutomationAgentClient aaClient) {
        runFlow(aaClient, ConfigureEncodedFlow.class, configCtx);
    }

    /**
     * Stop the started task that runs the agent.
     *
     * @param aaClient Automation client to run flows on automation agent
     */
    protected void stopTask(IAutomationAgentClient aaClient) {
        ControlMvsTaskFlowContext stopTask =
            new ControlMvsTaskFlowContext.Builder(jobName, MvsTask.State.STOPPED).build();

        runFlow(aaClient, ControlMvsTaskFlow.class, stopTask);
    }

    /**
     * Start the started task that runs the agent.
     *
     * @param aaClient Automation client to run flows on automation agent
     */
    protected void startTask(IAutomationAgentClient aaClient) {
        ControlMvsTaskFlowContext.Builder ctx =
            new ControlMvsTaskFlowContext.Builder(jobName, MvsTask.State.RUNNING)
                .restartIfRunning();
        if (!taskParms.isEmpty()) {
            ctx.taskParms(taskParms);
        }

        runFlow(aaClient, ControlMvsTaskFlow.class, ctx.build());
    }

    @TasResource(value = "logs", regExp = ".*")
    public String getCeapmLogDir() {
        return getCeapmHomeDir() + "logs";
    }

    @TasResource(value = "config", regExp = ".*")
    public String getCeapmConfigDir() {
        return getCeapmHomeDir() + "config";
    }

    @TasEnvironmentProperty(CEAPM_HOME_PROPERTY)
    public String getCeapmHomeDir() {
        return installLocation + CEAPM_HOME;
    }

    @TasEnvironmentProperty(CEAPM_START_TASK_PROPERTY)
    public String getTaskName() {
        return jobName;
    }

    @TasEnvironmentProperty(CEAPM_START_PARAMS_PROPERTY)
    public Map<String, String> getTaskStartParms() {
        return taskParms;
    }

    @TasEnvironmentProperty(CEAPM_SMF_PORT_PROPERTY)
    public int getSmfPort() {
        return smfPort;
    }

    /**
     * Start CEAPM agent task defined in testbed locally. Uses task parameters that can
     * be optionally extended/overridden.
     *
     * @param envProperties Testbed environment properties.
     * @param roleId CEAPM role ID.
     * @param additionalParms Additional or overridden parameters to pass to the started task.
     * @param verify Verify the agent is fully started.
     */
    public static void startAgent(EnvironmentPropertyContext envProperties, String roleId,
        @Nullable Map<String, String> additionalParms, boolean verify)  {
        Args.notBlank(roleId, "role ID");
        Args.notNull(envProperties, "environment properties");
        String task = envProperties.getRolePropertyById(roleId, CEAPM_START_TASK_PROPERTY);
        Args.notBlank(task, "task");
        Map<String, String> parms = new LinkedHashMap<>();
        Map<String, String> baseParms =
            envProperties.getRolePropertyMapById(roleId, CEAPM_START_PARAMS_PROPERTY);
        Args.notNull(baseParms, "base task parms");
        parms.putAll(baseParms);
        if (additionalParms != null) {
            parms.putAll(additionalParms);
        }

        try {
            MvsTask.start(task, null, parms, false, true, MvsTask.DEFAULT_TIMEOUT);
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException("Failed to start CEAPM agent", e);
        }
        if (verify) {
            waitUntilAgentRunning(envProperties, roleId);
        }
    }

    /**
     * Start CEAPM agent task defined in testbed remotely. Uses task parameters that can
     * be optionally extended/overridden. Restarts the task if already running.
     *
     * @param aaClient Automation agent instance for the test.
     * @param envProperties Testbed environment properties.
     * @param roleId CEAPM role ID.
     * @param additionalParms Additional or overridden parameters to pass to the started task.
     * @param verify Verify the agent is fully started.
     */
    public static void startAgent(IAutomationAgentClient aaClient,
        EnvironmentPropertyContext envProperties, String roleId,
        @Nullable Map<String, String> additionalParms, boolean verify) {
        Args.notBlank(roleId, "role ID");
        Args.notNull(aaClient, "automation agent");
        String task = envProperties.getRolePropertyById(roleId, CEAPM_START_TASK_PROPERTY);
        Args.notBlank(task, "task");
        Map<String, String> parms = getAgentParameters(envProperties, roleId, additionalParms);

        ControlMvsTaskFlowContext flowContext =
            new ControlMvsTaskFlowContext.Builder(task, State.RUNNING).restartIfRunning()
                .taskParms(parms).build();
        String hostWithPort = envProperties.getMachineHostnameWithPortByRoleId(roleId);
        aaClient.runJavaFlow(new FlowConfigBuilder(ControlMvsTaskFlow.class, flowContext)
            .hostname(hostWithPort));
        if (verify) {
            waitUntilAgentRunning(envProperties, roleId);
        }
    }

    /**
     * Wait until CEAPM agent defined in testbed is running.
     *
     * @param envProperties Testbed environment properties.
     * @param roleId CEAPM role ID.
     */
    public static void waitUntilAgentRunning(EnvironmentPropertyContext envProperties, String roleId) {
        int smfPort =
            Integer.parseInt(envProperties.getRolePropertyById(roleId, CEAPM_SMF_PORT_PROPERTY));
        new PortUtils().waitTillRemotePortIsBusyInSec(
            envProperties.getMachineHostnameByRoleId(roleId), smfPort, 60);
    }

    /**
     * Stop CEAPM agent task defined in testbed.
     *
     * @param envProperties Testbed environment properties.
     * @param roleId CEAPM role ID.
     * @throws IOException When querying of the task status fails unexpectedly.
     * @throws TimeoutException If the process of stopping the task times out.
     */
    public static void stopAgent(EnvironmentPropertyContext envProperties, String roleId)
        throws IOException, TimeoutException {
        Args.notBlank(roleId, "role ID");
        Args.notNull(envProperties, "environment properties");
        String task = envProperties.getRolePropertyById(roleId, CEAPM_START_TASK_PROPERTY);
        Args.notBlank(task, "task");

        MvsTask.stop(task, null, MvsTask.DEFAULT_TIMEOUT);
    }

    /**
     * Stop CEAPM agent task defined in testbed remotely.
     *
     * @param aaClient Automation agent instance for the test.
     * @param envProperties Testbed environment properties.
     * @param roleId CEAPM role ID.
     * @throws IOException When querying of the task status fails unexpectedly.
     * @throws TimeoutException If the process of stopping the task times out.
     */
    public static void stopAgent(IAutomationAgentClient aaClient,
        EnvironmentPropertyContext envProperties, String roleId) {
        Args.notBlank(roleId, "role ID");
        Args.notNull(aaClient, "automation agent");
        String task = envProperties.getRolePropertyById(roleId, CEAPM_START_TASK_PROPERTY);
        Args.notBlank(task, "task");

        ControlMvsTaskFlowContext flowContext =
            new ControlMvsTaskFlowContext.Builder(task, State.STOPPED).build();
        String hostWithPort = envProperties.getMachineHostnameWithPortByRoleId(roleId);
        aaClient.runJavaFlow(new FlowConfigBuilder(ControlMvsTaskFlow.class, flowContext)
            .hostname(hostWithPort));
    }

    /**
     * Get CEAPM task start parameters that force use of specific Java version.
     *
     * @param javaConfig Java version used to start the task.
     * @return map of task parameters
     */
    public static Map<String, String> getAgentJavaParameters(CeapmJavaConfig javaConfig) {
        Map<String, String> taskParms = new LinkedHashMap<String, String>();
        taskParms.put("VERSION", Integer.toString(javaConfig.getVersion()));
        taskParms.put("RELEASE", Integer.toString(javaConfig.getRelease()));
        taskParms.put("MINOR", Integer.toString(javaConfig.getMinor()));
        taskParms.put("PGMSUFF", javaConfig.getPgmSuffix());
        taskParms.put("USSPATH", javaConfig.getUssDir());
        return Collections.unmodifiableMap(taskParms);
    }

    /**
     * Get CEAPM task start parameters. You can provide optional override.
     *
     * @param envProperties Testbed environment properties.
     * @param roleId CEAPM role ID.
     * @param additionalParms Additional or overridden parameters to pass to the started task.
     * @return map of task parameters
     */
    public static Map<String, String> getAgentParameters(EnvironmentPropertyContext envProperties,
        String roleId, Map<String, String> additionalParms) {
        Map<String, String> parms = new LinkedHashMap<>();
        Map<String, String> baseParms =
            envProperties.getRolePropertyMapById(roleId, CEAPM_START_PARAMS_PROPERTY);
        Args.notNull(baseParms, "base task parms");
        parms.putAll(baseParms);
        if (additionalParms != null) {
            parms.putAll(additionalParms);
        }
        return Collections.unmodifiableMap(parms);
    }

    /**
     * Cleanup role for {@link CeapmRole}.
     */
    public static class Cleanup extends AbstractRole {
        final CeapmRole deployedRole;

        /**
         * Constructor.
         *
         * @param roleId Role identification.
         * @param deployedRole The deployed role instance to be cleaned up.
         */
        public Cleanup(String roleId, CeapmRole deployedRole) {
            super(roleId);
            this.deployedRole = deployedRole;
        }

        @Override
        public void deploy(IAutomationAgentClient aaClient) {
            ControlMvsTaskFlowContext stopTask =
                new ControlMvsTaskFlowContext.Builder(deployedRole.getTaskName(),
                    MvsTask.State.STOPPED).timeout(120).build();
            runFlow(aaClient, ControlMvsTaskFlow.class, stopTask);
        }
    }

    /**
     * Builder used to construct the CEAPM role.
     */
    public static class Builder extends BuilderBase<Builder, CeapmRole> {
        // Configuration property keys
        private static final String AGENT_NAME_P = "introscope.agent.agentName";
        private static final String PROCESS_NAME_P = "introscope.agent.customProcessName";
        private static final String EM_HOST_P =
            "introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT";
        private static final String EM_PORT_P =
            "introscope.agent.enterprisemanager.transport.tcp.port.DEFAULT";
        private static final String EM_P = "agentManager.url.1";
        private static final String SMF_PORT_P = "ppz.smf.socket.port";
        private static final String LOG_BACKUPS_P = "log4j.appender.logfile.MaxBackupIndex";
        private static final String LOG_SIZE_P = "log4j.appender.logfile.MaxFileSize";
        private static final String LOG_ROOT_LOGGER_P = "log4j.rootLogger";
        private static final String LOG_ISCOPE_LOGGER_P = "log4j.logger.IntroscopeAgent";
        private static final String LICENSE_AGREEMENT_P =
            "CA.Cross-Enterprise.APM.I.Read.And.Accept.End.User.License.Agreement";
        private static final String JOBNAME_P = "SYSVIEW.connection.jobname";
        private static final String DB2_COLLECT_P = "Insight.metrics.collect";
        private static final String DB2_HOST_P = "Insight.connection.hostname";
        private static final String DB2_PORT_P = "Insight.connection.port";
        private static final String DB2_DIRECTOR_P = "Insight.director";
        private static final String DB2_PASS_SUPP_P = "Insight.passticket.support";
        private static final String DB2_PASS_APPL_P = "Insight.passticket.appl";
        private static final String DB2_USER_P = "Insight.username";

        private final String roleId;
        private final ITasResolver tasResolver;

        private String version;

        private final HashMap<String, String> ceapmPropertiesModifications = new HashMap<>();
        private final HashMap<String, String> introscopeProfileModifications = new HashMap<>();

        private String jobName = CEAPM_DEFAULT.getTaskName();
        private Map<String, String> taskParms = new LinkedHashMap<>();

        private String installLocation = CEAPM_DEFAULT.getPath();

        private int smfPort = CEAPM_DEFAULT.getSmfPort();

        private String emHost = "czprcorvus-em1";
        private int emPort = 5001;

        /**
         * Flag that forbids change of version after we already translated some of the properties
         * according to it.
         */
        private boolean versionDirty = false;

        /**
         * Constructor.
         *
         * @param roleId Role identification
         * @param tasResolver TAS URL and hostname resolver
         */
        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            version = tasResolver.getDefaultVersion();
            setProperty(LICENSE_AGREEMENT_P, "yes");
            // limit log size by default to prevent excessive disk usage
            // use logfile appender, because console may end up redirected to unlimited file
            setProfile(LOG_SIZE_P, "10MB");
            setProfile(LOG_BACKUPS_P, "3");
            setProfile(LOG_ROOT_LOGGER_P, "INFO,logfile");
            setProfile(LOG_ISCOPE_LOGGER_P, "INFO,logfile");
            configJavaVersionParameters(DEFAULT_JAVA_VERSION);
        }

        /**
         * Constructor using default configuration.
         *
         * @param config CEAPM configuration
         * @param tasResolver TAS URL and hostname resolver
         */
        public Builder(CeapmConfig config, ITasResolver tasResolver) {
            this(config.getRole(), tasResolver);
            configBasic(config);
        }

        /**
         * Collect all parameters to produce a CeapmRole object.
         *
         * @return CE-APM agent role object
         */
        @Override
        public CeapmRole build() {
            CeapmRole role = getInstance();
            role.jobName = jobName;
            role.taskParms = taskParms;
            role.agentUrl =
                tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.powerpack.sysview",
                    "cross-enterprise-agent-mf-dist", "ebcdic", "tar", version));
            role.installLocation = installLocation;
            role.configCtx =
                createConfigFlowContext(role.getCeapmHomeDir(), introscopeProfileModifications,
                    ceapmPropertiesModifications);
            role.smfPort = smfPort;

            // Set EM host and port using version specific properties
            if (versionCompare(version, "10.2") >= 0) {
                setProfile(EM_P, emHost + ":" + emPort);
            } else {
                setProfile(EM_HOST_P, emHost);
                setProfile(EM_PORT_P, Integer.toString(emPort));
            }

            return role;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected CeapmRole getInstance() {
            return new CeapmRole(this);
        }

        /**
         * Create configuration flow context to configure CEAPM agent.
         *
         * @param agentHomePath Agent home path
         * @param profileChanges Map of property changes for profile file or {@code null}
         * @param propertiesChanges Map of property changes for property file or {@code null}
         * @return Created flow context.
         */
        protected static ConfigureEncodedFlowContext createConfigFlowContext(String agentHomePath,
            Map<String, String> profileChanges, Map<String, String> propertiesChanges) {
            ConfigureEncodedFlowContext.Builder builder =
                new ConfigureEncodedFlowContext.Builder().encoding(CEAPM_PROPERTIES_ENCODING);
            if (profileChanges != null) {
                builder.configurationMap(agentHomePath + CEAPM_PROFILE_FILE, profileChanges);
            }
            if (propertiesChanges != null) {
                builder.configurationMap(agentHomePath + CEAPM_PROPERTIES_FILE, propertiesChanges);
            }
            return builder.build();
        }

        /**
         * Set the started task job name.
         *
         * @param jobName Job name.
         * @return Builder instance the method was called on.
         */
        public Builder jobName(String jobName) {
            Args.notBlank(jobName, "jobname");
            this.jobName = jobName;
            return builder();
        }

        /**
         * Set CE-APM version to deploy.
         *
         * @param version CE-APM agent artifact version
         * @return Builder instance the method was called on.
         * @exception IllegalStateException When DB2 options were already configured, exception
         *            is thrown to prevent use of properties from mismatching release.
         */
        public Builder version(String version) throws IllegalStateException {
            Args.notBlank(version, "agent artifact version");
            if (versionDirty) {
                throw new IllegalStateException(
                    "You must set version before configuring DB2 options");
            }
            this.version = version;
            return builder();
        }

        /**
         * Set installation location
         *
         * @param location target location for CE-APM agent deployment
         * @return Builder instance the method was called on.
         */
        public Builder installLocation(String location) {
            Args.notBlank(location, "installation location");
            installLocation = location;
            return builder();
        }

        /**
         * Configure Sysview used by agent
         *
         * @param sysviewJobName Sysview jobname
         * @return Builder instance the method was called on.
         */
        public Builder configSysviewJob(String sysviewJobName) {
            Args.notNull(sysviewJobName, "Sysview jobname");
            setProperty(JOBNAME_P, sysviewJobName);
            return builder();
        }

        /**
         * Configure metric polling interval.
         *
         * @param interval Metric polling interval [s].
         * @return Builder instance the method was called on.
         */
        public Builder configUpdateInterval(int interval) {
            Args.check(0 < interval, "polling interval must be positive");
            setProperty(UPDATE_INTERVAL_PROPERTY, Integer.toString(interval));
            return builder();
        }

        /**
         * Configure EM host
         *
         * @param emHost Enterprise Manager host
         * @return Builder instance the method was called on.
         */
        public Builder configEmHost(String emHost) {
            Args.notBlank(emHost, "EM host");
            this.emHost = emHost;
            return builder();
        }

        /**
         * Configure EM port
         *
         * @param emPort Enterprise Manager port
         * @return Builder instance the method was called on.
         */
        public Builder configEmPort(int emPort) {
            Args.check(0 < emPort && emPort < 65536, "emPort must be a valid port number");
            this.emPort = emPort;
            return builder();
        }

        /**
         * Configure SMF port
         *
         * @param smfPort Sysview SMF port
         * @return Builder instance the method was called on.
         */
        public Builder configSmfPort(int smfPort) {
            Args.check(0 < smfPort && smfPort < 65536, "smfPort must be a valid port number");
            setProfile(SMF_PORT_P, Integer.toString(smfPort));
            this.smfPort = smfPort;
            return builder();
        }

        /**
         * Configure agent name.
         *
         * @param name New agent name
         * @return Builder instance the method was called on.
         */
        public Builder configAgentName(String name) {
            Args.notBlank(name, "agent name");
            Args.check(name.startsWith(CEAPM_AGENT_NAME_DEFAULT), "process name must start with "
                + CEAPM_AGENT_NAME_DEFAULT);
            setProfile(AGENT_NAME_P, name);
            return builder();
        }

        /**
         * Configure process name.
         *
         * @param name New process name.
         * @return Builder instance the method was called on.
         */
        public Builder configProcessName(String name) {
            Args.notBlank(name, "process name suffix");
            Args.check(name.startsWith(CEAPM_PROCESS_NAME_DEFAULT), "process name must start with "
                + CEAPM_PROCESS_NAME_DEFAULT);
            setProfile(PROCESS_NAME_P, name);
            return builder();
        }

        /**
         * Enable collection of DB2 metrics. Configure connection and passticket authentication.
         *
         * @param host Xnet host
         * @param port Xnet port
         * @param appl Xnet passticket applid
         * @param user Xnet user name
         * @return the Builder instance the method was called on.
         */
        public Builder configDb2UsingPassticket(String host, int port, String appl, String user) {
            Args.notBlank(host, "host");
            Args.check(0 < port && port < 65536, "port must be a valid port number");
            Args.notBlank(appl, "applid");
            Args.notBlank(user, "user");

            setDb2Property(DB2_COLLECT_P, "yes");
            setDb2Property(DB2_HOST_P, host);
            setDb2Property(DB2_PORT_P, Integer.toString(port));
            setDb2Property(DB2_PASS_SUPP_P, "yes");
            setDb2Property(DB2_PASS_APPL_P, appl);
            setDb2Property(DB2_USER_P, user);

            return builder();
        }

        /**
         * Enable collection of DB2 metrics. Configure connection and passticket authentication
         * using default Xnet instance.
         *
         * @param user Xnet user name
         * @return Builder instance the method was called on.
         */
        public Builder configDb2UsingPassticket(String user) {
            // TODO while SysviewDb2Role stores connection details in SYSVDB2_PROPERTIES_FILE
            // CeapmRole never uses this information.
            return configDb2UsingPassticket("ca31", 6600, "CATICKET", user);
        }

        /**
         * Configure Xnet director ID. Use unique one in case two agents connect to the
         * same Xnet.
         *
         * @param director Director ID.
         * @return Builder instance the method was called on.
         */
        public Builder configDb2Director(String director) {
            Args.notBlank(director, "director");
            setDb2Property(DB2_DIRECTOR_P, director);
            return builder();
        }

        /**
         * Set DB2 property using version-specific key. This is as generalization of Insight
         * properties that were renamed to SYSVDB2 in release 10.2.
         *
         * @param key Property key using the old name.
         * @param value Property value.
         */
        private void setDb2Property(String key, String value) {
            if (versionCompare(version, "10.2") < 0) {
                setProperty(key, value);
            } else {
                setProperty(
                    key.replace("Insight.DB2.", "SYSVDB2.").replace("Insight.", "SYSVDB2."), value);
            }
            versionDirty = true;
        }

        /**
         * Compare two version strings (decimal numbers separated with dots). Version X.Y
         * is considered equal to X.Y.0. Non-numeric characters are ignored.
         * TODO replace with org.apache.maven.artifact.versioning.ComparableVersion
         *
         * @param ver1 First version
         * @param ver2 Second version
         * @return 0 for equal versions, negative when v1 is less than v2, positive otherwise
         */
        private int versionCompare(String ver1, String ver2) {
            String[] parts1 = ver1.replaceAll("[^0-9.]", "").split("\\.");
            String[] parts2 = ver2.replaceAll("[^0-9.]", "").split("\\.");
            int max = Math.max(parts1.length, parts2.length);
            for (int i = 0; i < max; i++) {
                int p1 = 0;
                int p2 = 0;
                if (i < parts1.length && parts1[i].length() > 0) {
                    p1 = Integer.parseInt(parts1[i]);
                }
                if (i < parts2.length && parts2[i].length() > 0) {
                    p2 = Integer.parseInt(parts2[i]);
                }
                if (p1 != p2) {
                    return p1 - p2;
                }
            }
            return 0;
        }

        /**
         * Configure property in CEAPM properties file.
         *
         * @param key Property key.
         * @param value Property value.
         * @return Builder instance the method was called on.
         */
        public Builder setProperty(String key, String value) {
            Args.notBlank(key, "key");
            Args.notNull(value, "value");
            ceapmPropertiesModifications.put(key, value);
            return builder();
        }

        /**
         * Configure property in Introscope profile file.
         *
         * @param key Property key.
         * @param value Property value.
         * @return Builder instance the method was called on.
         */
        public Builder setProfile(String key, String value) {
            Args.notBlank(key, "key");
            Args.notNull(value, "value");
            introscopeProfileModifications.put(key, value);
            return builder();
        }

        /**
         * Configure properties in CEAPM properties file.
         *
         * @param props Map containing key property key-value pairs. {@code null} values denote
         *        key deletion.
         * @return Builder instance the method was called on.
         */
        public Builder configProperties(Map<String, String> props) {
            Args.notNull(props, "property map");
            for (Entry<String, String> entry : props.entrySet()) {
                setProperty(entry.getKey(), entry.getValue());
            }
            return builder();
        }

        /**
         * Configure properties in Introscope profile file.
         *
         * @param props Map containing key property key-value pairs. {@code null} values denote
         *        key deletion.
         * @return Builder instance the method was called on.
         */
        public Builder configProfile(Map<String, String> props) {
            Args.notNull(props, "property map");
            for (Entry<String, String> entry : props.entrySet()) {
                setProfile(entry.getKey(), entry.getValue());
            }
            return builder();
        }

        /**
         * Configure JVM version parameters string.
         *
         * @param javaConfig version of Java that will be used to run the agent.
         *
         * @return Builder instance the method was called on.
         */
        public Builder configJavaVersionParameters(CeapmJavaConfig javaConfig) {
            taskParms.putAll(getAgentJavaParameters(javaConfig));
            return builder();
        }

        /**
         * Configure agent basics using a configuration set.
         *
         * @param config Configuration set to use.
         * @return Builder instance the method was called on.
         */
        public Builder configBasic(CeapmConfig config) {
            Args.notNull(config, "config");
            installLocation(config.getPath());
            jobName(config.getTaskName());
            configSmfPort(config.getSmfPort());
            configAgentName(config.getAgentName());
            return builder();
        }

        /**
         * Configure agent connection to Sysview XAPI.
         *
         * @param config Sysview configuration set to use.
         * @return Builder instance the method was called on.
         */
        public Builder configSysviewXapi(SysviewConfig config) {
            configSysviewJob(config.getUserTask());
            // TODO JCL, ENV and jobname must be configured consistently.
            return builder();
        }

        /**
         * Configure DB2 with passticket security based on a CEAPM configuration set.
         *
         * @param config Configuration set to use.
         * @return Builder instance the method was called on.
         */
        public Builder configDb2(CeapmConfig config) {
            Args.notNull(config, "config");
            configDb2UsingPassticket(config.getOwner());
            configDb2Director(config.getDb2Director());
            return builder();
        }

        /**
         * Configure EM connection.
         *
         * @param emRole EM role to connect to.
         * @return Builder instance the method was called on.
         */
        public Builder configEm(EmRole emRole) {
            Args.notNull(emRole, "config");
            configEmHost(tasResolver.getHostnameById(emRole.getRoleId()));
            configEmPort(emRole.getEmPort());
            return builder();
        }

        /**
         * Configure debug logging level for specific class logger(s).
         *
         * @param classes The class(es) to debug.
         * @return Builder instance the method was called on.
         */
        public Builder debugLog(Class<?>... classes) {
            for (Class<?> clazz : classes) {
                setProfile("log4j.logger." + clazz.getName(), "DEBUG");
            }
            return builder();
        }
    }

    /**
     * This enum holds parameter values for the last versions of JVM installation within particular
     * release.
     * Each Java version has three parameters: version, release and minor.
     *
     * Program suffix (pgmSuffix) is part of JCL program name to execute.
     * Example:
     * //JAVAJVM EXEC PGM=JVMLDM80 ("80" is pgmSuffix)
     *
     * USS path is folder name on USS.
     * Example:
     * /sys/java31bt/v8r0m0/usr/lpp/java/J8.0 ("J8.0" is ussDir)
     *
     * Syntax would be:
     * START <jcl>,JOBNAME=<task>,VERSION=<JavaVersionNumber>,RELEASE=<JavaReleaseNumber>,
     * MINOR=<JavaMinorNumber>,PGMSUFF=<ProgramNumber>,USSPATH=<USSJavaFolderName>
     */
    public enum CeapmJavaConfig {
        JVM6_0_0(6, 0, 0, "60", "J6.0"),
        JVM6_0_1(6, 0, 1, "61", "J6.0.1"),
        JVM7_0_0(7, 0, 0, "70", "J7.0"),
        JVM7_1_0(7, 1, 0, "71", "J7.1"),
        JVM8_0_0(8, 0, 0, "80", "J8.0"),
        ;

        public static final CeapmJavaConfig JVM6 = JVM6_0_1;
        public static final CeapmJavaConfig JVM7 = JVM7_1_0;
        public static final CeapmJavaConfig JVM8 = JVM8_0_0;

        private final int version;
        private final int release;
        private final int minor;
        private final int bitness = 31; // Hard-coded for now as we only support 31-bit Java
        private final String pgmSuffix;
        private final String ussDir;

        CeapmJavaConfig(int version, int release, int minor, String pgmSuffix, String ussDir) {
            this.version = version;
            this.release = release;
            this.minor = minor;
            this.pgmSuffix = pgmSuffix;
            this.ussDir = ussDir;
        }

        public int getVersion() {
            return version;
        }

        public int getRelease() {
            return release;
        }

        public int getMinor() {
            return minor;
        }

        public int getBitness() {
            return bitness;
        }

        public String getPgmSuffix() {
            return pgmSuffix;
        }

        public String getUssDir() {
            return ussDir;
        }

        /**
         * Returns the full path to the Java installation as structured on CA mainframe systems.
         *
         * @return Full path to the Java installation.
         */
        public String getUssPath() {
            // TODO /sys/java31bt/vXrYmZ/usr/lpp/java/JX.Y.Z is same as /usr/lpp/java/JX.Y.Z
            return MessageFormat.format("/sys/java{0}bt/v{1}r{2}m{3}/usr/lpp/java/{4}",
                getBitness(), getVersion(), getRelease(), getMinor(), getUssDir());
        }

        @Override
        public String toString() {
            return MessageFormat.format("Java {0}.{1}.{2} ({3}-bit)", getVersion(), getRelease(),
                getMinor(), getBitness());
        }
    }

    /**
     * This class holds "default" CEAPM configurations.
     */
    public enum CeapmConfig {
        POX("WILYZPOX", "/CA31/u/users/wily/test/ceapm_role", "podja02", 15033),
        GA("WILYZBIG", "/CA31/u/users/wily/test/ceapm_ga", "bisvo01", 15033),
        NEW("WILYZBIN", "/CA31/u/users/wily/test/ceapm_new", "bisvo01", 15034),
        DEMO1("WILYZBID", "/CA31/u/users/wily/test/ceapm_demo1", "bisvo01", 15041),
        DEMO2("WILYZBIE", "/CA31/u/users/wily/test/ceapm_demo2", "bisvo01", 15042),
        LONG1("WILYZBIK", "/CA31/u/users/wily/test/ceapm_long1", "bisvo01", 15045),
        LONG2("WILYZBIL", "/CA31/u/users/wily/test/ceapm_long2", "bisvo01", 15046),

        // monitored by Sysview 14.1 Wily instance
        // TODO remove once the JCL/ENV are configurable
        GA_14_1("WILYZBIH", "/CA31/u/users/wily/test/ceapm_ga", "bisvo01", 15033),
        NEW_14_1("WILYZBIO", "/CA31/u/users/wily/test/ceapm_new", "bisvo01", 15034),

        TEST0031("WILYZT00", "/u/users/wily/test/00", "bisvo01", 15042),
        TEST0111("WILYZT01", "/u/users/wily/test/01", "bisvo01", 15033),

        // Do not use any of the entries below unless explicitly authorized by their respective
        // owners
        BI4("WILYZBI4", "/CA31/u/users/wily/bisvo01/4", "bisvo01", 15032),
        BI5("WILYZBI5", "/CA31/u/users/wily/bisvo01/5", "bisvo01", 15031),
        BI7("WILYZBI7", "/CA31/u/users/wily/bisvo01/7", "bisvo01", 15031),
        DZ7("WILYZDZ7", "/CA31/u/users/wily/wilyb/dzuro02/APM99.MF.7", "dzuro02", 15037),
        DZ6("WILYZDZ6", "/CA31/u/users/wily/wilyb/dzuro02/APM99.MF.6", "dzuro02", 15038),
        HEX("WILYZHEX", "/CA31/u/users/wily/wilyb/hecka01/agentx", "hecka01", 15035),
        HEY("WILYZHEY", "/CA31/u/users/wily/wilyb/hecka01/agenty", "hecka01", 15036),
        PODJA02_0("WILYZPO0", "/CA31/u/users/wily/podja02/stage/auto_0", "podja02", 15039),
        PODJA02_1("WILYZPO1", "/CA31/u/users/wily/podja02/stage/auto_1", "podja02", 15040),
        ;

        private final String taskName;
        private final int smfPort;
        private final String path;
        // owner is intended to be usable for XNET passticket credentials
        private final String owner;

        private CeapmConfig(String taskName, String path, String owner, int smfPort) {
            this.taskName = taskName;
            this.path = path;
            this.owner = owner;
            this.smfPort = smfPort;
        }

        public String getAgentName() {
            return CeapmRole.CEAPM_AGENT_NAME_DEFAULT + " " + name();
        }

        public String getProcessName() {
            return CeapmRole.CEAPM_PROCESS_NAME_DEFAULT + " " + name();
        }

        public String getOwner() {
            return owner;
        }

        public String getDb2Director() {
            final String full = (owner + "-" + name());
            // trim to maximal director length
            return full.substring(0, Math.min(16, full.length()));
        }

        public int getSmfPort() {
            return smfPort;
        }

        public String getTaskName() {
            return taskName;
        }

        public String getPath() {
            return path;
        }

        public String getRole() {
            return "ceapm" + CommonUtils.constantToCamelCase(name()) + "Role";
        }
    }
}
