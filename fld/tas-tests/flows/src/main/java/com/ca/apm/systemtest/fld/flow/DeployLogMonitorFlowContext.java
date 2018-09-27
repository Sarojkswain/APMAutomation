package com.ca.apm.systemtest.fld.flow;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Args;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.flow.AutowireCapable;
import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.property.AbstractEnvPropertySerializer;
import com.ca.tas.property.EnvPropSerializable;
import com.ca.tas.resolver.ITasResolver;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static com.ca.tas.artifact.IBuiltArtifact.TasExtension.JAR;


/**
 * @author haiva01
 */
public class DeployLogMonitorFlowContext implements IFlowContext, AutowireCapable,
    EnvPropSerializable<DeployLogMonitorFlowContext> {
    public static final String DATA = "DATA";
    private final transient Serializer envPropSerializer;

    Data data = new Data();

    protected DeployLogMonitorFlowContext() {
        envPropSerializer = new Serializer(this);
    }

    protected DeployLogMonitorFlowContext(Data data) {
        this();
        this.data = data;
    }

    protected DeployLogMonitorFlowContext(Builder builder) {
        this();
        data.logMonitorConfigSource = builder.logMonitorConfigSource;
        data.configFile = builder.configFile;
        data.vars = builder.vars;
        data.pidFile = builder.pidFile;
        data.tailerArtifactUrl = builder.tailerArtifactUrl.toString();
        data.targetDir = builder.targetDir;
        data.start = builder.start;
        data.maxMatchesPerPeriod = builder.maxMatchesPerPeriod;
        data.numberOfPreviousLines = builder.numberOfPreviousLines;
        data.emails = builder.emails.toArray(new String[builder.emails.size()]);
    }

    public LogMonitorConfigSource getLogMonitorConfigSource() {
        return data.logMonitorConfigSource;
    }

    public String getConfigFile() {
        return data.configFile;
    }

    public String getPidFile() {
        return data.pidFile;
    }

    public Map<String, String> getVars() {
        return data.vars;
    }

    public String getTailerArtifactUrl() {
        return data.tailerArtifactUrl;
    }

    public String getTargetDir() {
        return data.targetDir;
    }

    public boolean isStart() {
        return data.start;
    }

    public int getMaxMatchesPerPeriod() {
        return data.maxMatchesPerPeriod;
    }

    public int getNumberOfPreviousLines() {
        return data.numberOfPreviousLines;
    }

    public Collection<String> getEmails() {
        return Arrays.asList(data.emails);
    }

    public Data getData() {
        return data;
    }

    @NotNull
    @Override
    public Class<? extends IAutomationFlow> autowiredFlow() {
        return DeployLogMonitorFlow.class;
    }

    @Override
    public Map<String, String> serialize(String key) {
        return envPropSerializer.serialize(key);
    }

    @Override
    public DeployLogMonitorFlowContext deserialize(String key,
        Map<String, String> serializedData) {
        return envPropSerializer.deserialize(key, serializedData);
    }

    public enum LogMonitorConfigSource {
        ResourceFile,
        DiskFile
    }

    protected static class Data {
        public LogMonitorConfigSource logMonitorConfigSource;
        public String configFile;
        public String pidFile;
        public Map<String, String> vars;
        public String tailerArtifactUrl;
        public String targetDir;
        public boolean start;
        private int maxMatchesPerPeriod;
        private int numberOfPreviousLines;
        private String[] emails;
    }

    public static class Builder extends BuilderBase<DeployLogMonitorFlowContext.Builder,
        DeployLogMonitorFlowContext> {

        private LogMonitorConfigSource logMonitorConfigSource;
        private String configFile;
        private Map<String, String> vars = new LinkedHashMap<>(10);
        private String pidFile;
        private String tailerVersion;
        private String targetDir;
        private boolean start = true;
        private int maxMatchesPerPeriod = 10;
        private int numberOfPreviousLines = 5;
        private Collection<String> emails = new TreeSet<>();

        private ITasResolver tasResolver;
        private URL tailerArtifactUrl;

        public Builder(ITasResolver tasResolver) {
            this.tasResolver = tasResolver;

            tailerVersion = tasResolver.getDefaultVersion();
        }


        /**
         * @param file JSON config file for Tailer tool as on-disk file
         */
        public Builder configFile(String file) {
            logMonitorConfigSource = LogMonitorConfigSource.DiskFile;
            configFile = file;
            return builder();
        }

        /**
         * @param resource JSON config file for Tailer tool as resource file
         */
        public Builder configFileFromResource(String resource) {
            logMonitorConfigSource = LogMonitorConfigSource.ResourceFile;
            configFile = resource;
            return builder();
        }

        /**
         * @param vars variables values that will be used to fill in placeholders in JSON config
         *             file.
         */
        public Builder vars(Map<String, String> vars) {
            this.vars.putAll(vars);
            return builder();
        }

        /**
         * @param pidFile PID file path
         */
        public Builder pidFile(String pidFile) {
            this.pidFile = pidFile;
            return builder();
        }

        public Builder tailerVersion(String version) {
            this.tailerVersion = version;
            return builder();
        }

        public Builder nostart() {
            this.start = false;
            return builder();
        }

        public Builder maxMatchesPerPeriod(int maxMatchesPerPeriod) {
            this.maxMatchesPerPeriod = maxMatchesPerPeriod;
            return builder();
        }

        public Builder previousLines(int numberOfPreviousLines) {
            this.numberOfPreviousLines = numberOfPreviousLines;
            return builder();
        }

        public Builder emails(Collection<String> emails) {
            this.emails.addAll(emails);
            return builder();
        }

        public Builder email(String email) {
            this.emails.add(email);
            return builder();
        }

        @Override
        protected DeployLogMonitorFlowContext getInstance() {
            return new DeployLogMonitorFlowContext(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public DeployLogMonitorFlowContext build() {
            targetDir = getDeployBase() + "log-monitor/";
            if (StringUtils.isBlank(pidFile)) {
                pidFile = targetDir + "tailer.pid";
            }

            tailerArtifactUrl = tasResolver.getArtifactUrl(
                new com.ca.tas.artifact.TasArtifact.Builder("tailer")
                    .groupId("com.ca.apm.systemtest.fld")
                    .version(tailerVersion)
                    .classifier("jar-with-dependencies")
                    .extension(JAR)
                    .build());

            return getInstance();
        }
    }

    public static class LinuxBuilder extends Builder {
        public LinuxBuilder(ITasResolver tasResolver) {
            super(tasResolver);
        }

        @Override
        protected String getDeployBase() {
            return getLinuxDeployBase();
        }

        @Override
        protected LinuxBuilder builder() {
            return this;
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }
    }

    public static class Serializer extends
        AbstractEnvPropertySerializer<DeployLogMonitorFlowContext> {
        private static final Logger log = LoggerFactory.getLogger(Serializer.class);
        private DeployLogMonitorFlowContext flowContext;
        private Gson gson = new GsonBuilder().create();

        public Serializer(DeployLogMonitorFlowContext flowContext) {
            super(Serializer.class);
            this.flowContext = flowContext;
        }

        @Override
        public DeployLogMonitorFlowContext deserialize(String key,
            Map<String, String> serializedData) {
            log.debug("Serialized data: {}", serializedData);
            Map<String, String> deserializedMap = deserializeMapWithKey(key, serializedData);
            log.debug("Deserialized data: {}", deserializedMap);
            String jsonStr = deserializedMap.get(DATA);
            if (StringUtils.isBlank(jsonStr)) {
                throw new IllegalArgumentException("Empty deserialized data");
            }

            Data data = gson.fromJson(jsonStr, Data.class);
            if (data == null) {
                throw new IllegalArgumentException("JSON deserialization failure");
            }

            return new DeployLogMonitorFlowContext(data);
        }

        @Override
        public Map<String, String> serialize(String key) {
            Args.notNull(flowContext, "Flow context");

            Map<String, String> serializeMap = super.serialize(key);
            serializeMap.putAll(
                serializeMapWithKey(key,
                    Collections.singletonMap(DATA, gson.toJson(flowContext.getData()))));

            return serializeMap;
        }
    }
}
