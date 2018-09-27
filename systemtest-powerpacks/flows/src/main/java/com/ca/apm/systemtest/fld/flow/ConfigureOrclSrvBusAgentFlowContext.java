package com.ca.apm.systemtest.fld.flow;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.property.EnvPropSerializable;
import org.apache.http.util.Args;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @Author rsssa02
 */
public class ConfigureOrclSrvBusAgentFlowContext implements IFlowContext, EnvPropSerializable<ConfigureOrclSrvBusAgentFlowContext> {
    private final String agentJarPath;
    private final String profileFilePath;
    private final String domainDirRelativePath;
    private final String environmentFileRelativePath;
    private final String environmentFileBackupExtension;


    public String getAgentJarPath() {
        return agentJarPath;
    }

    public String getProfileFilePath() {
        return profileFilePath;
    }

    public String getEnvironmentFileBackupExtension() {
        return environmentFileBackupExtension;
    }

    public String getDomainDirRelativePath() {
        return domainDirRelativePath;
    }

    public String getEnvironmentFileRelativePath() {
        return environmentFileRelativePath;
    }

    private final transient ConfigureOrclSrvBusAgentFlowContextSerializer envPropSerializer;

    protected ConfigureOrclSrvBusAgentFlowContext(ConfigureOrclSrvBusAgentFlowContext.Builder builder) {
        this.agentJarPath = builder.agentJarPath;
        this.profileFilePath = builder.profileFilePath;
        this.domainDirRelativePath = builder.domainDirRelativePath;
        this.environmentFileBackupExtension = builder.environmentFileBackupExtension;
        this.environmentFileRelativePath = builder.environmentFileRelativePath;
        this.envPropSerializer = new ConfigureOrclSrvBusAgentFlowContextSerializer(this);
    }


    @Override
    public Map<String, String> serialize(String key) {
        return this.envPropSerializer.serialize(key);
    }

    @Override
    public ConfigureOrclSrvBusAgentFlowContext deserialize(String key, Map<String, String> serializedData) {
        return this.envPropSerializer.deserialize(key, serializedData);
    }

    public static class Builder extends BuilderBase<ConfigureOrclSrvBusAgentFlowContext.Builder, ConfigureOrclSrvBusAgentFlowContext> {
        private static final String DEFAULT_ENV_FILE_RELATIVE_PATH = "bin\\setDomainEnv.cmd";
        private static final String DEFAULT_ENV_BACKUP_EXTENSION_PATH = ".bak";
        private String agentJarPath;
        private String profileFilePath;
        protected String domainDirRelativePath;
        protected String environmentFileRelativePath;
        protected String environmentFileBackupExtension;

        public Builder(String var1, String var2, String var3) {
            this.agentJarPath = var1;
            this.profileFilePath = var2;
            this.domainDirRelativePath = var3;
            this.environmentFileRelativePath("bin\\setDomainEnv.cmd");
            this.environmentFileBackupExtension(".bak");
        }

        public Builder() {
        }

        public ConfigureOrclSrvBusAgentFlowContext build() {
            ConfigureOrclSrvBusAgentFlowContext context = this.getInstance();
            Args.notNull(context.agentJarPath, "javaAgentArgument");
            Args.notNull(context.domainDirRelativePath, "domainDirRelativePath");
            return context;
        }

        protected ConfigureOrclSrvBusAgentFlowContext.Builder environmentFileRelativePath(String value) {
            this.environmentFileRelativePath = value;
            return this.builder();
        }

        protected ConfigureOrclSrvBusAgentFlowContext.Builder environmentFileBackupExtension(String value) {
            this.environmentFileBackupExtension = value;
            return this.builder();
        }

        protected ConfigureOrclSrvBusAgentFlowContext.Builder builder() {
            return this;
        }

        protected ConfigureOrclSrvBusAgentFlowContext getInstance() {
            return new ConfigureOrclSrvBusAgentFlowContext(this);
        }

        public ConfigureOrclSrvBusAgentFlowContext.Builder javaAgentArgs(String value) {
            this.agentJarPath = value;
            return this.builder();
        }
        public ConfigureOrclSrvBusAgentFlowContext.Builder profileFileArgs(String value) {
            this.profileFilePath = value;
            return this.builder();
        }

     public ConfigureOrclSrvBusAgentFlowContext.Builder domainDirRelativePath(String value) {
            this.domainDirRelativePath = value;
            return this.builder();
        }
    }

}
