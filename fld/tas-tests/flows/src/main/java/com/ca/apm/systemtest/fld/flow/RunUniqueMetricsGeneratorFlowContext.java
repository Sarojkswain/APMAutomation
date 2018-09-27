package com.ca.apm.systemtest.fld.flow;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.flow.AutowireCapable;
import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.property.AbstractEnvPropertySerializer;
import com.ca.tas.property.EnvPropSerializable;
import com.ca.tas.resolver.ITasResolver;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Args;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

/**
 * This flow context is used by {@link RunUniqueMetricsGeneratorFlow}.
 *
 * @author haiva01
 */
public class RunUniqueMetricsGeneratorFlowContext implements IFlowContext, AutowireCapable,
    EnvPropSerializable<RunUniqueMetricsGeneratorFlowContext> {
    private static final String DATA = "data";
    private final transient Serializer envPropSerializer;
    Data data;

    protected RunUniqueMetricsGeneratorFlowContext() {
        data = new Data();
        envPropSerializer = new Serializer(this);
    }

    protected RunUniqueMetricsGeneratorFlowContext(Data data) {
        this();
        this.data = data;
    }

    protected RunUniqueMetricsGeneratorFlowContext(Builder builder) {
        this();
        data.dir = builder.dir;
        data.rate = builder.rate;
        data.runningTimeSecs = builder.runningTimeSecs;
        data.uniqueString = builder.uniqueString;
        data.emUrl = builder.emUrl;
        data.agentName = builder.agentName;
        data.customProcessName = builder.customProcessName;
        data.pidFile = builder.pidFile;
    }

    public String getDir() {
        return data.dir;
    }

    public long getRate() {
        return data.rate;
    }

    public long getRunningTimeSecs() {
        return data.runningTimeSecs;
    }

    public String getUniqueString() {
        return data.uniqueString;
    }

    public String getEmUrl() {
        return data.emUrl;
    }

    public String getAgentName() {
        return data.agentName;
    }

    public String getCustomProcessName() {
        return data.customProcessName;
    }

    public String getPidFile() {
        return data.pidFile;
    }

    @NotNull
    @Override
    public Class<? extends IAutomationFlow> autowiredFlow() {
        return RunUniqueMetricsGeneratorFlow.class;
    }

    @Override
    public Map<String, String> serialize(String key) {
        return envPropSerializer.serialize(key);
    }

    @Override
    public RunUniqueMetricsGeneratorFlowContext deserialize(String key,
        Map<String, String> serializedData) {
        return envPropSerializer.deserialize(key, serializedData);
    }

    protected Data getData() {
        return data;
    }

    protected static class Data {
        private String dir;
        private long runningTimeSecs;
        private long rate;
        private String uniqueString;
        private String emUrl;
        private String agentName;
        private String customProcessName;
        private String pidFile;

    }

    public static class LinuxBuilder extends Builder {
        @Override
        protected LinuxBuilder builder() {
            return this;
        }

        @Override
        protected String getDeployBase() {
            return getLinuxDeployBase();
        }

        @Override
        protected String getJavaBase() {
            return getLinuxJavaBase();
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }
    }

    public static class Builder extends BuilderBase<Builder,
        RunUniqueMetricsGeneratorFlowContext> {
        private static final Logger log = LoggerFactory.getLogger(Builder.class);

        protected ITasResolver tasResolver;
        protected String dir;
        protected long runningTimeSecs = -1;
        protected long rate = -1;
        protected String uniqueString;
        protected String emUrl;
        protected String agentName;
        protected String customProcessName;
        protected String pidFile;

        /**
         * This function sets directory where the UMeG tool will be extracted.
         *
         * @param dir directory
         */
        public Builder dir(String dir) {
            this.dir = dir;
            return builder();
        }

        /**
         * This function sets limited running time for UMeG specified in seconds.
         *
         * @param seconds amount of running time in seconds
         */
        public Builder runningTime(long seconds) {
            this.runningTimeSecs = seconds;
            return builder();
        }

        /**
         * This function sets metrics generation rate in metrics per second.
         *
         * @param rate metrics per second rate value
         */
        public Builder rate(long rate) {
            this.rate = rate;
            return builder();
        }

        /**
         * This function is convenience function to compute both running time and rate of
         * generation of metrics by specifying target amount of metrics after given time.
         *
         * @param metrics  target amount of metrics
         * @param time     duration after which target amount of metrics should be generated
         * @param timeUnit time duration unit
         */
        public Builder generate(long metrics, long time, TimeUnit timeUnit) {
            this.runningTimeSecs = timeUnit.toSeconds(time);
            this.rate = (long) Math.ceil((double) metrics / runningTimeSecs);
            return builder();
        }

        /**
         * This function sets unique string that will be used to make individual runs of the UMeG
         * tool unique. If it is not specified, one is automatically generated.
         *
         * @param str unique string
         */
        public Builder uniqueString(String str) {
            this.uniqueString = str;
            return builder();
        }

        /**
         * This function sets EM URL which will be used as value for <code>agentManager
         * .url.1</code> property.
         *
         * @param emUrl EM URL
         */
        public Builder emUrl(String emUrl) {
            this.emUrl = emUrl;
            return builder();
        }

        /**
         * This function sets agent's name which will be used as value for <code>com.wily
         * .introscope.agent.agentName</code> property. The default value is "UMeGAgent".
         *
         * @param agentName agent name
         */
        public Builder agentName(String agentName) {
            this.agentName = agentName;
            return builder();
        }

        /**
         * This function sets custom process name which will be used as value for
         * <code>introscope.agent.customProcessName</code>. The default value is "UMeG".
         *
         * @param customProcessName custom process name
         */
        public Builder customProcessName(String customProcessName) {
            this.customProcessName = customProcessName;
            return builder();
        }

        /**
         * This function sets PID file path.
         *
         * @param pidFile PID file path
         */
        public Builder pidFile(String pidFile) {
            this.pidFile = pidFile;
            return builder();
        }

        @Override
        protected RunUniqueMetricsGeneratorFlowContext getInstance() {
            return new RunUniqueMetricsGeneratorFlowContext(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public RunUniqueMetricsGeneratorFlowContext build() {
            Args.positive(rate, "metrics generation rate must be specified");

            dir = defaultIfBlank(dir, concatPaths(getDeployBase(), "umeg"));
            agentName = defaultIfBlank(agentName, "UMeGAgent");
            customProcessName = defaultIfBlank(customProcessName, "UMeG");
            return getInstance();
        }
    }

    public static class Serializer extends
        AbstractEnvPropertySerializer<RunUniqueMetricsGeneratorFlowContext> {
        private static final Logger log = LoggerFactory
            .getLogger(RunUniqueMetricsGeneratorFlowContext.Serializer.class);
        private RunUniqueMetricsGeneratorFlowContext flowContext;
        private Gson gson = new GsonBuilder().create();

        public Serializer(RunUniqueMetricsGeneratorFlowContext flowContext) {
            super(RunUniqueMetricsGeneratorFlowContext.Serializer.class);
            this.flowContext = flowContext;
        }

        @Override
        public RunUniqueMetricsGeneratorFlowContext deserialize(String key,
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

            return new RunUniqueMetricsGeneratorFlowContext(data);
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
