/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 *
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.apm.tests.flow.agent;

import com.ca.apm.automation.action.flow.agent.ApplicationServerType;
import com.ca.tas.property.AbstractEnvPropertySerializer;
import org.apache.http.util.Args;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class RegisterJavaAgentFlowContextSerializer extends AbstractEnvPropertySerializer<RegisterJavaAgentFlowContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterJavaAgentFlowContextSerializer.class);
    private static final String AGENT_PATH = "agentPath";
    private static final String AGENT_VERSION = "agentVersion";
    private static final String SERVER_TYPE = "serverType";
    private static final String SERVER_XML_FILE_PATH = "serverXmlFilePath";
    private final RegisterJavaAgentFlowContext flowContext;

    public RegisterJavaAgentFlowContextSerializer(@Nullable RegisterJavaAgentFlowContext flowContext) {
        super(RegisterJavaAgentFlowContextSerializer.class);
        this.flowContext = flowContext;
    }

    public RegisterJavaAgentFlowContext deserialize(String key, Map<String, String> serializedData) {
        LOGGER.debug("Serialized data: {}", serializedData);
        Map deserializedMap = this.deserializeMapWithKey(key, serializedData);
        LOGGER.debug("Deserialized data: {}", deserializedMap);
        String agentPath = (String) deserializedMap.get(AGENT_PATH);
        String agentVersion = (String) deserializedMap.get(AGENT_VERSION);
        ApplicationServerType serverType = ApplicationServerType.valueOf((String) deserializedMap.get(SERVER_TYPE));
        String serverXmlFilePath = (String) deserializedMap.get(SERVER_XML_FILE_PATH);
        if (agentPath == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: agentPath is missing.");
        }
        if (agentVersion == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: agentVersion is missing.");
        }
        if (serverType == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: serverType is missing.");
        }
        if (serverXmlFilePath == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: serverXmlFilePath is missing.");
        } else {
            RegisterJavaAgentFlowContext.Builder builder = new RegisterJavaAgentFlowContext.Builder()
                    .agentPath(agentPath).agentVersion(agentVersion).serverType(serverType)
                    .serverXmlFilePath(serverXmlFilePath);
            return builder.build();
        }
    }

    public Map<String, String> serialize(String key) {
        Args.notNull(this.flowContext, "Flow context");
        HashMap customData = new HashMap();
        customData.put(AGENT_PATH, this.flowContext.getAgentPath());
        customData.put(AGENT_VERSION, this.flowContext.getAgentVersion());
        customData.put(SERVER_TYPE, this.flowContext.getServerType().name());
        customData.put(SERVER_XML_FILE_PATH, this.flowContext.getServerXmlFilePath());
        Map serializedData1 = super.serialize(key);
        serializedData1.putAll(this.serializeMapWithKey(key, customData));
        return serializedData1;
    }
}
