/**
 * 
 */
package com.ca.apm.systemtest.fld.role;

import static com.ca.apm.systemtest.fld.flow.DynamicInstrumentationFlowContext.getBuilder;

import com.ca.apm.systemtest.fld.flow.DynamicInstrumentationFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * @author KEYJA01
 *
 */
public class DynamicInstrumentationLoadRole extends AbstractRole {
    public static final String RUN_DI_FLOW = "run_di_flow";

    /**
     * @param roleId
     */
    private DynamicInstrumentationLoadRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
    }

    public static class Builder extends BuilderBase<Builder, DynamicInstrumentationLoadRole> {
        @SuppressWarnings("unused")
        private ITasResolver resolver;
        private String roleId;
        private DynamicInstrumentationFlowContext.Builder diFlowCtx = getBuilder();

        public Builder(String roleId, ITasResolver resolver) {
            this.roleId = roleId;
            this.resolver = resolver;
            
        }
        
        @Override
        public DynamicInstrumentationLoadRole build() {
            // TODO add the necessary properties
            
            getEnvProperties().add(RUN_DI_FLOW, diFlowCtx.build());
            
            return getInstance();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected DynamicInstrumentationLoadRole getInstance() {
            return new DynamicInstrumentationLoadRole(this);
        }
        
        public Builder em(String host) {
            return this.em(host, 5001, "cemadmin", "quality");
        }
        
        public Builder dynamicInstrumentationHome(String directory) {
            diFlowCtx.diHome(directory);
            return this;
        }
        
        public Builder em(String host, int port, String user, String password) {
            diFlowCtx.emHost(host).emUser(user).emPassword(password).emPort(port);
            
            return this;
        }
        
        public Builder agent(String agentName, String agentHost, int agentPort) {
            diFlowCtx.agentName(agentName).agentHost(agentHost).agentPort(agentPort);
            
            return this;
        }
        
        public Builder clwJar(String clwJar) {
            diFlowCtx.clwJar(clwJar);
            return this;
        }
        
        public Builder urlFormat(String urlFormat) {
            diFlowCtx.urlFormat(urlFormat);
            return this;
        }
        
        public Builder servlets(int numServlets) {
            diFlowCtx.servlets(numServlets);
            return this;
        }
        
        public Builder javaHome(String javaHome) {
            diFlowCtx.javaHome(javaHome);
            return this;
        }
    }

    /* (non-Javadoc)
     * @see com.ca.tas.role.Deployable#deploy(com.ca.tas.client.IAutomationAgentClient)
     */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
    }
}
