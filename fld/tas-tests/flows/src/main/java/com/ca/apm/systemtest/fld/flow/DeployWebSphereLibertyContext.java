package com.ca.apm.systemtest.fld.flow;

import java.net.URL;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Args;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.flow.AutowireCapable;
import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.property.AbstractEnvPropertySerializer;
import com.ca.tas.property.EnvPropSerializable;
import com.ca.tas.resolver.ITasResolver;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

/**
 * @author haiva01
 */
public class DeployWebSphereLibertyContext implements IFlowContext, AutowireCapable,
    EnvPropSerializable<DeployWebSphereLibertyContext> {

    public static final String DATA = "DATA";
    private final transient Serializer envPropSerializer;
    Data data = new Data();

    protected DeployWebSphereLibertyContext() {
        envPropSerializer = new Serializer(this);
    }

    protected DeployWebSphereLibertyContext(Builder builder) {
        this();
        data.wlpArtifactUrl = builder.wlpArtifactUrl;
        data.destDir = builder.destDir;
    }

    protected DeployWebSphereLibertyContext(Data data) {
        this();
        this.data = data;
    }

    public String getWlpArtifactUrl() {
        return data.wlpArtifactUrl;
    }

    public String getDestDir() {
        return data.destDir;
    }

    @NotNull
    @Override
    public Class<? extends IAutomationFlow> autowiredFlow() {
        return DeployWebSphereLiberty.class;
    }

    @Override
    public Map<String, String> serialize(String key) {
        return envPropSerializer.serialize(key);
    }

    @Override
    public DeployWebSphereLibertyContext deserialize(String key,
        Map<String, String> serializedData) {
        return envPropSerializer.deserialize(key, serializedData);
    }


    protected static class Data {
        String wlpArtifactUrl;
        String destDir;
    }

    public static class Builder extends BuilderBase<Builder, DeployWebSphereLibertyContext> {
        private ITasResolver tasResolver;
        private String wlpArtifactUrl;
        private String destDir;

        public Builder(ITasResolver tasResolver) {
            this.tasResolver = tasResolver;
        }

        public Builder wlpArtifact(ITasArtifact artifact) {
            return wlpArtifact(tasResolver.getArtifactUrl(artifact));
        }

        public Builder wlpArtifact(URL wlpArtifactUrl) {
            this.wlpArtifactUrl = wlpArtifactUrl.toExternalForm();
            return builder();
        }

        public Builder destDir(String destDir) {
            this.destDir = destDir;
            return builder();
        }

        @Override
        protected DeployWebSphereLibertyContext getInstance() {
            return new DeployWebSphereLibertyContext(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public DeployWebSphereLibertyContext build() {
            Args.notBlank(wlpArtifactUrl, "WebSphere Liberty artifact must be specified.");
            destDir = defaultIfBlank(destDir, this.getDeployBase());

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
        AbstractEnvPropertySerializer<DeployWebSphereLibertyContext> {
        private static final Logger log = LoggerFactory.getLogger(Serializer.class);
        private DeployWebSphereLibertyContext flowContext;
        private Gson gson = new GsonBuilder().create();

        public Serializer(DeployWebSphereLibertyContext flowContext) {
            super(Serializer.class);
            this.flowContext = flowContext;
        }

        @Override
        public DeployWebSphereLibertyContext deserialize(String key,
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

            return new DeployWebSphereLibertyContext(data);
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
}
