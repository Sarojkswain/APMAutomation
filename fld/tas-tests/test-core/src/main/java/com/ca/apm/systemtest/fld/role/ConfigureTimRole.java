/**
 * 
 */
package com.ca.apm.systemtest.fld.role;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.ca.apm.systemtest.fld.flow.ConfigureTimFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;

/**
 * @author keyja01
 *
 */
public class ConfigureTimRole extends AbstractRole {
    private ConfigureTimFlowContext configureTimFlowContext;
    
    private ConfigureTimRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        configureTimFlowContext = builder.ctx;
    }
    
    public static class Builder extends BuilderBase<Builder, ConfigureTimRole> {
        private String roleId;
        private String hostname;
        private HashSet<String> requiredInterfaces = new HashSet<>();
        private HashSet<String> disallowedInterfaces = new HashSet<>();
        private String username = "admin";
        private String password = "quality";
        private int port = 80;
        private ConfigureTimFlowContext ctx;
        private final Map<String, String> additionalProperties = new HashMap<>();
        
        public Builder(String roleId) {
            this.roleId = roleId;
        }

        @Override
        public ConfigureTimRole build() {
            ctx = ConfigureTimFlowContext.getBuilder()
                .hostname(hostname)
                .port(port)
                .username(username)
                .password(password)
                .checkIfs(requiredInterfaces.toArray(new String[0]))
                .uncheckIfs(disallowedInterfaces.toArray(new String[0]))
                .additionalProperties(additionalProperties)
                .build();
            return getInstance();
        }
        
        public Builder username(String username) {
            this.username = username;
            return this;
        }
        
        public Builder password(String password) {
            this.password = password;
            return this;
        }
        
        public Builder port(int port) {
            this.port = port;
            return this;
        }
        
        public Builder timHostname(String hostname) {
            this.hostname = hostname;
            return this;
        }
        
        public Builder requiredInterface(String requiredInterface) {
            this.requiredInterfaces.add(requiredInterface);
            return this;
        }
        
        public Builder disallowedInterface(String disallowedInterface) {
            this.disallowedInterfaces.add(disallowedInterface);
            return this;
        }
        
        public Builder additionalProperty(String key, String value) {
            additionalProperties.put(key, value);
            return this;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected ConfigureTimRole getInstance() {
            return new ConfigureTimRole(this);
        }
        
    }
    

    /* (non-Javadoc)
     * @see com.ca.tas.role.Deployable#deploy(com.ca.tas.client.IAutomationAgentClient)
     */
    @Override
    public void deploy(IAutomationAgentClient aa) {
        runFlow(aa, configureTimFlowContext);
    }

}
