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

import java.util.Collections;
import java.util.Map;

/**
 * Created by jirji01 on 6/27/2017.
 */
public class RestorePermissionsFlowContext implements AutowireCapable, EnvPropSerializable<RestorePermissionsFlowContext> {
    public static final String DATA = "DATA";

    private final transient RestorePermissionsFlowContext.Serializer envPropSerializer;
    public RestorePermissionsFlowContext.Data data = new RestorePermissionsFlowContext.Data();

    protected RestorePermissionsFlowContext() {
        this.envPropSerializer = new RestorePermissionsFlowContext.Serializer(this);
    }

    protected RestorePermissionsFlowContext(Data data) {
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
    public RestorePermissionsFlowContext deserialize(String key, Map<String, String> serializedData) {
        return envPropSerializer.deserialize(key, serializedData);
    }

    public static class Serializer extends
            AbstractEnvPropertySerializer<RestorePermissionsFlowContext> {
        private static final Logger log = LoggerFactory
                .getLogger(RestorePermissionsFlowContext.Serializer.class);
        private RestorePermissionsFlowContext flowContext;
        private Gson gson = new GsonBuilder().create();

        public Serializer(RestorePermissionsFlowContext flowContext) {
            super(RestorePermissionsFlowContext.Serializer.class);
            this.flowContext = flowContext;
        }

        @Override
        public RestorePermissionsFlowContext deserialize(String key,
                                                         Map<String, String> serializedData) {
            log.debug("Serialized data: {}", serializedData);
            Map<String, String> deserializedMap = deserializeMapWithKey(key, serializedData);
            log.debug("Deserialized data: {}", deserializedMap);
            String jsonStr = deserializedMap.get(DATA);
            if (StringUtils.isBlank(jsonStr)) {
                throw new IllegalArgumentException("Empty deserialized data");
            }

            RestorePermissionsFlowContext.Data
                    data = gson.fromJson(jsonStr, RestorePermissionsFlowContext.Data.class);
            if (data == null) {
                throw new IllegalArgumentException("JSON deserialization failure");
            }

            return new RestorePermissionsFlowContext(data);
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

    public static class Builder extends BuilderBase<Builder,RestorePermissionsFlowContext> {

        Data data = new Data();

        @Override
        protected RestorePermissionsFlowContext getInstance() {
            return new RestorePermissionsFlowContext(data);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public RestorePermissionsFlowContext build() {

            // do checks

            return getInstance();
        }

        public Builder baseFolder(String dir) {
            data.baseFolder = dir;
            return builder();
        }

        public Builder emFileListing(String file) {
            data.emFileListing = file;
            return builder();
        }
    }

    public static class Data {
        public String baseFolder;
        public String emFileListing;
    }
}
