package com.ca.apm.systemtest.fld.flow;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.flow.AutowireCapable;
import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.property.AbstractEnvPropertySerializer;
import com.ca.tas.property.EnvPropSerializable;
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

/**
 * Flow context for {@link ClwCleanupFlow}.
 *
 * @author haiva01
 */
public class ClwCleanupFlowContext implements IFlowContext, AutowireCapable,
    EnvPropSerializable<ClwCleanupFlowContext> {
    public static final String DATA = "DATA";
    private final transient Serializer envPropSerializer;

    Data data;

    protected ClwCleanupFlowContext() {
        data = new Data();
        envPropSerializer = new Serializer(this);
    }

    protected ClwCleanupFlowContext(Data data) {
        this();
        this.data = data;
    }

    protected ClwCleanupFlowContext(Builder builder) {
        this();
        data.dir = builder.dir;
        data.cleanupPeriod = builder.cleanupPeriodMillis;
    }

    protected Data getData() {
        return data;
    }

    public String getDir() {
        return data.dir;
    }

    public long getCleanupPeriod() {
        return data.cleanupPeriod;
    }

    private static class Data {
        private String dir;
        private long cleanupPeriod = 0;
    }

    @NotNull
    @Override
    public Class<? extends IAutomationFlow> autowiredFlow() {
        return ClwCleanupFlow.class;
    }

    @Override
    public Map<String, String> serialize(String key) {
        return envPropSerializer.serialize(key);
    }

    @Override
    public ClwCleanupFlowContext deserialize(String key, Map<String, String> serializedData) {
        return envPropSerializer.deserialize(key, serializedData);
    }

    public static class Builder extends BuilderBase<Builder, ClwCleanupFlowContext> {
        private String dir;
        protected long cleanupPeriod = 0;
        protected TimeUnit cleanupPeriodUnit;

        protected long cleanupPeriodMillis = 0;

        /**
         * This function specifies directory where the {@link ClwCleanupFlow} will look for files.
         * @param dir working directory for the {@link ClwCleanupFlow}
         */
        public Builder dir(String dir) {
            this.dir = dir;
            return builder();
        }

        /**
         * This function sets time between periodic cleanups of CLW's XML files.
         *
         * @param period   amount of time in units specified by second parameter
         * @param timeUnit time unit of the first parameter
         */
        public Builder cleanupPeriod(long period, TimeUnit timeUnit) {
            cleanupPeriod = period;
            cleanupPeriodUnit = timeUnit;
            return builder();
        }

        @Override
        protected ClwCleanupFlowContext getInstance() {
            return new ClwCleanupFlowContext(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public ClwCleanupFlowContext build() {
            if (cleanupPeriod > 0
                || cleanupPeriodUnit != null) {
                cleanupPeriodMillis = cleanupPeriodUnit.toMillis(cleanupPeriod);
            }

            return getInstance();
        }
    }

    public static class Serializer extends
        AbstractEnvPropertySerializer<ClwCleanupFlowContext> {
        private static final Logger log = LoggerFactory.getLogger(Serializer.class);
        private ClwCleanupFlowContext flowContext;
        private Gson gson = new GsonBuilder().create();

        public Serializer(ClwCleanupFlowContext flowContext) {
            super(Serializer.class);
            this.flowContext = flowContext;
        }

        @Override
        public ClwCleanupFlowContext deserialize(String key,
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

            return new ClwCleanupFlowContext(data);
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
