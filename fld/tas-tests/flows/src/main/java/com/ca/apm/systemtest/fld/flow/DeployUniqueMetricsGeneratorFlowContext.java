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

import static com.ca.tas.artifact.IBuiltArtifact.TasExtension.ZIP;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

/**
 * @author haiva01
 */
public class DeployUniqueMetricsGeneratorFlowContext implements IFlowContext, AutowireCapable,
    EnvPropSerializable<DeployUniqueMetricsGeneratorFlowContext> {
    private static final String DATA = "data";

    private final transient Serializer envPropSerializer;
    private Data data;

    protected DeployUniqueMetricsGeneratorFlowContext() {
        data = new Data();
        envPropSerializer = new Serializer(this);
    }

    protected DeployUniqueMetricsGeneratorFlowContext(Data data) {
        this();
        this.data = data;
    }

    protected DeployUniqueMetricsGeneratorFlowContext(Builder builder) {
        this();
        data.dir = builder.dir;
        data.umegArtifactUrl = builder.umegArtifactUrl;
    }

    public String getDir() {
        return data.dir;
    }

    public String getUmegArtifactUrl() {
        return data.umegArtifactUrl;
    }

    @NotNull
    @Override
    public Class<? extends IAutomationFlow> autowiredFlow() {
        return DeployUniqueMetricsGeneratorFlow.class;
    }

    @Override
    public Map<String, String> serialize(String key) {
        return envPropSerializer.serialize(key);
    }

    @Override
    public DeployUniqueMetricsGeneratorFlowContext deserialize(String key,
        Map<String, String> serializedData) {
        return envPropSerializer.deserialize(key, serializedData);
    }

    protected Data getData() {
        return data;
    }

    protected static class Data {
        private String dir;
        private String umegArtifactUrl;
    }

    public static class LinuxBuilder extends Builder {
        public LinuxBuilder(ITasResolver tasResolver) {
            super(tasResolver);
        }

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
        DeployUniqueMetricsGeneratorFlowContext> {

        protected ITasResolver tasResolver;
        protected String dir;
        protected String umegVersion;
        protected String umegArtifactUrl;

        public Builder(ITasResolver tasResolver) {
            this.tasResolver = tasResolver;
        }

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
         * This function sets UMeG tool version that will be deployed.
         *
         * @param umegVersion UMeG version
         */
        public Builder version(String umegVersion) {
            this.umegVersion = umegVersion;
            return builder();
        }

        @Override
        protected DeployUniqueMetricsGeneratorFlowContext getInstance() {
            return new DeployUniqueMetricsGeneratorFlowContext(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public DeployUniqueMetricsGeneratorFlowContext build() {
            dir = defaultIfBlank(dir, concatPaths(getDeployBase(), "umeg"));

            umegVersion = defaultIfBlank(umegVersion, tasResolver.getDefaultVersion());
            umegArtifactUrl = tasResolver.getArtifactUrl(
                new com.ca.tas.artifact.TasArtifact.Builder("umeg-dist")
                    .groupId("com.ca.apm.systemtest.fld")
                    .version(umegVersion)
                    .classifier("dist")
                    .extension(ZIP)
                    .build())
                .toString();

            return getInstance();
        }
    }

    public static class Serializer extends
        AbstractEnvPropertySerializer<DeployUniqueMetricsGeneratorFlowContext> {
        private static final Logger log = LoggerFactory.getLogger(Serializer.class);
        private DeployUniqueMetricsGeneratorFlowContext flowContext;
        private Gson gson = new GsonBuilder().create();

        public Serializer(DeployUniqueMetricsGeneratorFlowContext flowContext) {
            super(Serializer.class);
            this.flowContext = flowContext;
        }

        @Override
        public DeployUniqueMetricsGeneratorFlowContext deserialize(String key,
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

            return new DeployUniqueMetricsGeneratorFlowContext(data);
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
