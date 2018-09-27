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

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.automation.action.flow.agent.ApplicationServerType;
import com.ca.tas.builder.ExtendedBuilderBase;
import com.ca.tas.property.EnvPropSerializable;
import org.apache.http.util.Args;

import java.util.Map;

/**
 * Flow Context for installing NET StockTrader WebApp into IIS
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class RegisterJavaAgentFlowContext implements IFlowContext, EnvPropSerializable<RegisterJavaAgentFlowContext> {

    private final String agentPath;
    private final String agentVersion;

    private final ApplicationServerType serverType;
    private final String serverXmlFilePath;

    private final transient RegisterJavaAgentFlowContextSerializer envPropSerializer;

    protected RegisterJavaAgentFlowContext(RegisterJavaAgentFlowContext.Builder builder) {
        this.agentPath = builder.agentPath;
        this.agentVersion = builder.agentVersion;

        this.serverType = builder.serverType;
        this.serverXmlFilePath = builder.serverXmlFilePath;

        this.envPropSerializer = new RegisterJavaAgentFlowContextSerializer(this);
    }

    public String getAgentPath() {
        return agentPath;
    }

    public String getAgentVersion() {
        return agentVersion;
    }

    public ApplicationServerType getServerType() {
        return serverType;
    }

    public String getServerXmlFilePath() {
        return serverXmlFilePath;
    }

    @Override
    public RegisterJavaAgentFlowContext deserialize(String key, Map<String, String> serializedData) {
        return this.envPropSerializer.deserialize(key, serializedData);
    }

    @Override
    public Map<String, String> serialize(String key) {
        return this.envPropSerializer.serialize(key);
    }

    public static class Builder extends ExtendedBuilderBase<RegisterJavaAgentFlowContext.Builder, RegisterJavaAgentFlowContext> {

        protected String agentPath;
        protected String agentVersion;

        protected ApplicationServerType serverType;
        protected String serverXmlFilePath;


        public Builder() {

        }

        public RegisterJavaAgentFlowContext build() {
            RegisterJavaAgentFlowContext context = this.getInstance();
            Args.notNull(context.agentPath, "agentPath");
            Args.notNull(context.agentVersion, "agentVersion");

            Args.notNull(context.serverType, "serverType");
            Args.notNull(context.serverXmlFilePath, "serverXmlFilePath");

            return context;
        }

        protected RegisterJavaAgentFlowContext getInstance() {
            return new RegisterJavaAgentFlowContext(this);
        }

        public RegisterJavaAgentFlowContext.Builder agentPath(String agentPath) {
            this.agentPath = agentPath;
            return this.builder();
        }

        public RegisterJavaAgentFlowContext.Builder agentVersion(String agentVersion) {
            this.agentVersion = agentVersion;
            return this.builder();
        }

        public RegisterJavaAgentFlowContext.Builder serverType(ApplicationServerType serverType) {
            this.serverType = serverType;
            return this.builder();
        }

        public RegisterJavaAgentFlowContext.Builder serverXmlFilePath(String serverXmlFilePath) {
            this.serverXmlFilePath = serverXmlFilePath;
            return this.builder();
        }

        protected RegisterJavaAgentFlowContext.Builder builder() {
            return this;
        }
    }

}