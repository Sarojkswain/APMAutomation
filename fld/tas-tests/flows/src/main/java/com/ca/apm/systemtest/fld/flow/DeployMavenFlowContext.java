package com.ca.apm.systemtest.fld.flow;

import java.io.File;
import java.net.MalformedURLException;
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
import com.ca.apm.systemtest.fld.common.ErrorUtils;
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
public class DeployMavenFlowContext implements IFlowContext, AutowireCapable,
    EnvPropSerializable<DeployMavenFlowContext> {

    public static final String DATA = "DATA";
    private static final Logger log = LoggerFactory.getLogger(DeployMavenFlowContext.class);
    private final transient Serializer envPropSerializer;
    protected Data data = new Data();

    protected DeployMavenFlowContext() {
        envPropSerializer = new Serializer(this);
    }

    protected DeployMavenFlowContext(Data data) {
        this();
        this.data = data;
    }

    protected DeployMavenFlowContext(Builder builder) {
        this();
        data.destDir = builder.destDir;
        data.mavenArtifactUrl = builder.mavenArtifactUrl;
        data.m2Home = builder.m2Home;
    }

    public static String fileNameFromUrl(String url) {
        try {
            URL mavenArtifactUrl = new URL(url);
            return new File(mavenArtifactUrl.getPath()).getName();
        } catch (MalformedURLException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to extract file name from URL {1}. Exception: {0}", url);
        }
    }

    public String getDestDir() {
        return data.destDir;
    }

    public String getMavenArtifactUrl() {
        return data.mavenArtifactUrl;
    }

    public String getM2Home() {
        return data.m2Home;
    }

    @NotNull
    @Override
    public Class<? extends IAutomationFlow> autowiredFlow() {
        return DeployMavenFlow.class;
    }

    @Override
    public Map<String, String> serialize(String key) {
        return envPropSerializer.serialize(key);
    }

    @Override
    public DeployMavenFlowContext deserialize(String key, Map<String, String> serializedData) {
        return envPropSerializer.deserialize(key, serializedData);
    }

    protected static class Data {
        protected String destDir;
        protected String mavenArtifactUrl;
        protected String m2Home;
    }

    public static class Builder extends BuilderBase<Builder, DeployMavenFlowContext> {
        private ITasResolver tasResolver;

        private String destDir;
        private String mavenArtifactUrl;
        private String m2Home;

        public Builder(ITasResolver tasResolver) {
            this.tasResolver = tasResolver;
        }

        public Builder destDir(String dir) {
            destDir = dir;
            return builder();
        }

        public Builder mavenArtifact(ITasArtifact mavenArtifact) {
            URL artifactUrl = tasResolver.getArtifactUrl(mavenArtifact);
            mavenArtifactUrl = artifactUrl.toExternalForm();
            return builder();
        }

        public Builder mavenArtifactUrl(URL mavenArtifactUrl) {
            this.mavenArtifactUrl = mavenArtifactUrl.toExternalForm();
            return builder();
        }

        @Override
        protected DeployMavenFlowContext getInstance() {
            return new DeployMavenFlowContext(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public DeployMavenFlowContext build() {
            Args.notBlank(mavenArtifactUrl, "Maven artifact URL must be specified.");

            destDir = defaultIfBlank(destDir, this.getDeployBase());
            String archiveFileName = fileNameFromUrl(mavenArtifactUrl);
            archiveFileName = StringUtils.removeEndIgnoreCase(archiveFileName, ".zip");
            archiveFileName = StringUtils.removeEndIgnoreCase(archiveFileName, ".tar.gz");
            archiveFileName = StringUtils.removeEndIgnoreCase(archiveFileName, "-bin");
            m2Home = concatPaths(destDir, archiveFileName);

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
        AbstractEnvPropertySerializer<DeployMavenFlowContext> {
        private static final Logger log = LoggerFactory.getLogger(Serializer.class);
        private DeployMavenFlowContext flowContext;
        private Gson gson = new GsonBuilder().create();

        public Serializer(DeployMavenFlowContext flowContext) {
            super(Serializer.class);
            this.flowContext = flowContext;
        }

        @Override
        public DeployMavenFlowContext deserialize(String key,
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

            return new DeployMavenFlowContext(data);
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
