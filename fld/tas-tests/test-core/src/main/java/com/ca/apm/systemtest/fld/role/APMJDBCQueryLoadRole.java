package com.ca.apm.systemtest.fld.role;

import com.ca.apm.systemtest.fld.flow.ConfigureAPMJDBCQueyLoadFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.property.RolePropertyContainer;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * Role setup parameters for APM JDBC Query load. 
 * 
 * @author filja01
 *
 */
public class APMJDBCQueryLoadRole extends AbstractRole {
    public static String APM_JDBC_QUERY_LOAD_FLOW_CTX_KEY = "APM_JDBC_QUERY_LOAD_FLOW_CTX_KEY";
    
    private ConfigureAPMJDBCQueyLoadFlowContext flowCtx;

    private APMJDBCQueryLoadRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.flowCtx = builder.flowCtx;
    }

	@Override
	public void deploy(IAutomationAgentClient automationAgentClient) {
		;
	}

	public static class Builder extends BuilderBase<Builder, APMJDBCQueryLoadRole> {
        private String roleId;
		private String apmServer = "fldmom01c.ca.com";
        
        private ConfigureAPMJDBCQueyLoadFlowContext flowCtx;
        
        public Builder(String roleId, ITasResolver resolver) {
        	super();
        	this.roleId = roleId;
        }

        /**
         * Sets up the role to automatically run the configuration flow during the deploy phase.
         * 
         * @return
         */
        public Builder setApmServer(String apmServer) {
			this.apmServer = apmServer;
			return this;
		}
		
		@Override
		public APMJDBCQueryLoadRole build() {
		    ConfigureAPMJDBCQueyLoadFlowContext.Builder builder = ConfigureAPMJDBCQueyLoadFlowContext.getBuilder();
            builder.apmServer(apmServer);
            
            flowCtx = builder.build();
            
            RolePropertyContainer props = getEnvProperties();
            props.add(APM_JDBC_QUERY_LOAD_FLOW_CTX_KEY, flowCtx);
            return getInstance();
		}

		@Override
		protected Builder builder() {
			return this;
		}

		@Override
		protected APMJDBCQueryLoadRole getInstance() {
			return new APMJDBCQueryLoadRole(this);
		}
		
	}
}
