package com.ca.apm.systemtest.fld.flow;

import com.ca.tas.property.AbstractEnvPropertySerializer;
import org.apache.http.util.Args;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author rsssa02
 */
public class ConfigureOrclSrvBusAgentFlowContextSerializer extends AbstractEnvPropertySerializer<ConfigureOrclSrvBusAgentFlowContext> {
        private static final Logger LOGGER = LoggerFactory.getLogger(ConfigureOrclSrvBusAgentFlowContextSerializer.class);

        private static final String AGENT_PATH = "agentPath";
        private static final String PROFILE_PATH = "profilePath";
        private static final String DOMAIN_PATH = "domainPath";
        private static final String ENV_FILE_PATH = "envFilePath";
        private static final String BKP_EXTENSION_ENV_FILE = "envFilePathBackupExt";
        private final ConfigureOrclSrvBusAgentFlowContext flowContext;

        public ConfigureOrclSrvBusAgentFlowContextSerializer(@Nullable ConfigureOrclSrvBusAgentFlowContext flowContext) {
            super(ConfigureOrclSrvBusAgentFlowContextSerializer.class);
            this.flowContext = flowContext;
        }

        public ConfigureOrclSrvBusAgentFlowContext deserialize(String key, Map<String, String> serializedData) {
            LOGGER.debug("Serialized data: {}", serializedData);
            Map deserializedMap = this.deserializeMapWithKey(key, serializedData);
            LOGGER.debug("Deserialized data: {}", deserializedMap);
            String agentPath = (String) deserializedMap.get(AGENT_PATH);
            String profilePath = (String) deserializedMap.get(PROFILE_PATH);
            String envFileBkpExt = (String) deserializedMap.get(BKP_EXTENSION_ENV_FILE);
            String envFile = (String) deserializedMap.get(ENV_FILE_PATH);
            String domainPath = (String) deserializedMap.get(DOMAIN_PATH);

            if (agentPath == null) {
                throw new IllegalArgumentException("Insufficient arguments in env property file: agentPath is missing.");
            }
            if (profilePath == null) {
                throw new IllegalArgumentException("Insufficient arguments in env property file: profilepath is missing.");
            }
            else {
                ConfigureOrclSrvBusAgentFlowContext.Builder builder = new ConfigureOrclSrvBusAgentFlowContext.Builder()
                        .javaAgentArgs(agentPath).profileFileArgs(profilePath).environmentFileBackupExtension(envFileBkpExt)
                        .domainDirRelativePath(domainPath).environmentFileRelativePath(envFile);
                return builder.build();
            }
        }

        public Map<String, String> serialize(String key) {
            Args.notNull(this.flowContext, "Flow context");
            HashMap customData = new HashMap();
            customData.put(AGENT_PATH, this.flowContext.getAgentJarPath());
            customData.put(PROFILE_PATH, this.flowContext.getProfileFilePath());
            customData.put(DOMAIN_PATH, this.flowContext.getDomainDirRelativePath());
            customData.put(ENV_FILE_PATH, this.flowContext.getEnvironmentFileRelativePath());
            customData.put(BKP_EXTENSION_ENV_FILE, this.flowContext.getEnvironmentFileBackupExtension());
            Map serializedData1 = super.serialize(key);
            serializedData1.putAll(this.serializeMapWithKey(key, customData));
            return serializedData1;
        }
}
