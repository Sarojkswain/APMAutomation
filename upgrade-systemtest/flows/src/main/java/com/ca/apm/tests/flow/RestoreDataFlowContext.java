package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.flow.AutowireCapable;
import com.ca.apm.automation.action.flow.utility.SshUploadFlow;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.property.AbstractEnvPropertySerializer;
import com.ca.tas.property.EnvPropSerializable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.http.util.Args;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Collections;
import java.util.Map;

/**
 * Created by jirji01 on 6/27/2017.
 */
public class RestoreDataFlowContext implements AutowireCapable, EnvPropSerializable<RestoreDataFlowContext> {
    public static final String DATA = "DATA";

    private final transient RestoreDataFlowContext.Serializer envPropSerializer;
    public RestoreDataFlowContext.Data data = new RestoreDataFlowContext.Data();
    
    protected RestoreDataFlowContext() {
        this.envPropSerializer = new RestoreDataFlowContext.Serializer(this);
    }

    protected RestoreDataFlowContext(Data data) {
        this();
        this.data = data;
    }

    @NotNull
    @Override
    public Class<? extends IAutomationFlow> autowiredFlow() {
        return SshUploadFlow.class;
    }

    @Override
    public Map<String, String> serialize(String key) {
        return envPropSerializer.serialize(key);
    }

    @Override
    public RestoreDataFlowContext deserialize(String key, Map<String, String> serializedData) {
        return envPropSerializer.deserialize(key, serializedData);
    }

    public static class Serializer extends
            AbstractEnvPropertySerializer<RestoreDataFlowContext> {
        private static final Logger log = LoggerFactory
                .getLogger(RestoreDataFlowContext.Serializer.class);
        private RestoreDataFlowContext flowContext;
        private Gson gson = new GsonBuilder().create();

        public Serializer(RestoreDataFlowContext flowContext) {
            super(RestoreDataFlowContext.Serializer.class);
            this.flowContext = flowContext;
        }

        @Override
        public RestoreDataFlowContext deserialize(String key,
                                                Map<String, String> serializedData) {
            log.debug("Serialized data: {}", serializedData);
            Map<String, String> deserializedMap = deserializeMapWithKey(key, serializedData);
            log.debug("Deserialized data: {}", deserializedMap);
            String jsonStr = deserializedMap.get(DATA);
            if (StringUtils.isBlank(jsonStr)) {
                throw new IllegalArgumentException("Empty deserialized data");
            }

            RestoreDataFlowContext.Data
                    data = gson.fromJson(jsonStr, RestoreDataFlowContext.Data.class);
            if (data == null) {
                throw new IllegalArgumentException("JSON deserialization failure");
            }

            return new RestoreDataFlowContext(data);
        }

        @Override
        public Map<String, String> serialize(String key) {
            Args.notNull(flowContext, "Flow context");

            Map<String, String> serializeMap = super.serialize(key);
            serializeMap.putAll(
                    serializeMapWithKey(key,
                            Collections.singletonMap(DATA, gson.toJson(flowContext.data))));

            return serializeMap;
        }
    }

    public static class Builder extends BuilderBase<Builder,RestoreDataFlowContext> {

        Data data = new Data();

        @Override
        protected RestoreDataFlowContext getInstance() {
            return new RestoreDataFlowContext(data);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public RestoreDataFlowContext build() {

            // do checks

            return getInstance();
        }

        public Builder em(String dir) {
            data.em = dir;
            return builder();
        }

        public Builder smartstor(String dir) {
            data.smartstor = dir;
            return builder();
        }

        public Builder smartstorArchive(String dir) {
            data.smartstorArchive = dir;
            return builder();
        }

        public Builder smartstorMeta(String dir) {
            data.smartstorMeta = dir;
            return builder();
        }

        public Builder traces(String dir) {
            data.traces = dir;
            return builder();
        }

        public Builder baseLine(String path) {
            data.baseLine = path;
            return builder();
        }

        public Builder hammondData(String dir) {
            data.hammondData = dir;
            return builder();
        }

        public Builder sourceData(String path) {
            data.sourceData = path;
            return builder();
        }

        public void dbHost(String dbHost) {
            data.dbHost = dbHost;
        }

        public void dbVersion(String version) {
            data.dbVersion = version;
        }
    }

    public static class Data {
        public String em;
        public String smartstor;
        public String smartstorArchive;
        public String smartstorMeta;
        public String traces;
        public String baseLine;
        public String hammondData;
        public String sourceData;
        public String dbHost;
        public String dbVersion;
    }
}
