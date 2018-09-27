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
import com.ca.tas.builder.ExtendedBuilderBase;
import com.ca.tas.property.EnvPropSerializable;
import org.apache.http.util.Args;

import java.util.Map;

/**
 * Flow Context for installing NET StockTrader WebApp into IIS
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class RegisterNetAgentFlowContext implements IFlowContext, EnvPropSerializable<RegisterNetAgentFlowContext> {

    private final String agentPath;
    private final String agentVersion;
    private final String agentDllVersion;
    private final String gacutilPath;

    private final transient RegisterNetAgentFlowContextSerializer envPropSerializer;

    protected RegisterNetAgentFlowContext(RegisterNetAgentFlowContext.Builder builder) {
        this.agentPath = builder.agentPath;
        this.agentVersion = builder.agentVersion;
        this.agentDllVersion = builder.agentDllVersion;
        this.gacutilPath = builder.gacutilPath;

        this.envPropSerializer = new RegisterNetAgentFlowContextSerializer(this);
    }

    public String getAgentPath() {
        return agentPath;
    }

    public String getAgentVersion() {
        return agentVersion;
    }

    public String getAgentDllVersion() {
        return agentDllVersion;
    }

    public String getGacutilPath() {
        return gacutilPath;
    }

    @Override
    public RegisterNetAgentFlowContext deserialize(String key, Map<String, String> serializedData) {
        return this.envPropSerializer.deserialize(key, serializedData);
    }

    @Override
    public Map<String, String> serialize(String key) {
        return this.envPropSerializer.serialize(key);
    }

    public static class Builder extends ExtendedBuilderBase<RegisterNetAgentFlowContext.Builder, RegisterNetAgentFlowContext> {

        protected String agentPath;
        protected String agentVersion;
        protected String agentDllVersion;
        protected String gacutilPath;

        public Builder() {

        }

        public RegisterNetAgentFlowContext build() {
            RegisterNetAgentFlowContext context = this.getInstance();
            Args.notNull(context.agentPath, "agentPath");
            Args.notNull(context.agentVersion, "agentVersion");
            Args.notNull(context.gacutilPath, "gacutilPath");

            return context;
        }

        protected RegisterNetAgentFlowContext getInstance() {
            return new RegisterNetAgentFlowContext(this);
        }

        public RegisterNetAgentFlowContext.Builder agentPath(String agentPath) {
            this.agentPath = agentPath;
            return this.builder();
        }

        public RegisterNetAgentFlowContext.Builder agentVersion(String agentVersion) {
            this.agentVersion = agentVersion;
            return this.builder();
        }

        public RegisterNetAgentFlowContext.Builder agentDllVersion(String agentDllVersion) {
            this.agentDllVersion = agentDllVersion;
            return this.builder();
        }

        public RegisterNetAgentFlowContext.Builder gacutilPath(String gacutilPath) {
            this.gacutilPath = gacutilPath;
            return this.builder();
        }

        protected RegisterNetAgentFlowContext.Builder builder() {
            return this;
        }
    }

}