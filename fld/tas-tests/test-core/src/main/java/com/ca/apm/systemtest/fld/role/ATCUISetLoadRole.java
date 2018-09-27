package com.ca.apm.systemtest.fld.role;

import com.ca.apm.systemtest.fld.flow.ConfigureATCUILoadFlowContext;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.flow.tess.TessConfigurer.RecordType;
import com.ca.tas.property.RolePropertyContainer;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * Role setup parameters for Assisted Triage load. 
 * 
 * @author filja01
 *
 */
public class ATCUISetLoadRole extends AbstractRole {
    public static String ATCUI_SET_LOAD_FLOW_CTX_KEY = "ATCUI_SET_LOAD_FLOW_CTX_KEY";
    
    private ConfigureATCUILoadFlowContext flowCtx;

    private ATCUISetLoadRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.flowCtx = builder.flowCtx;
    }

	@Override
	public void deploy(IAutomationAgentClient automationAgentClient) {
		;
	}

	public static class Builder extends BuilderBase<Builder, ATCUISetLoadRole> {
        private String roleId;
		private String webviewHost = "fldcoll11c.ca.com";
        private String user = "cemadmin";
        private String password = "quality";
        private String webviewPort = "8080";
        private Integer numberOfBrowsers = 3;
        
        private ConfigureATCUILoadFlowContext flowCtx;
        
        public Builder(String roleId, ITasResolver resolver) {
        	super();
        	this.roleId = roleId;
        }

        /**
         * Sets up the role to automatically run the configuration flow during the deploy phase.
         * 
         * @return
         */
        public Builder setWebviewHost(String webviewHost) {
			this.webviewHost = webviewHost;
			return this;
		}

		public Builder setUser(String user) {
			this.user = user;
			return this;
		}

		public Builder setPassword(String password) {
			this.password = password;
			return this;
		}

		public Builder setWebviewPort(String webviewPort) {
			this.webviewPort = webviewPort;
			return this;
		}
		
		public Builder setNumberOfBrowsers(Integer numberOfBrowsers) {
            this.numberOfBrowsers = numberOfBrowsers;
            return this;
        }
		
		@Override
		public ATCUISetLoadRole build() {
		    ConfigureATCUILoadFlowContext.Builder builder = ConfigureATCUILoadFlowContext.getBuilder();
            builder.user(user)
                .password(password)
                .webviewHost(webviewHost)
                .webviewPort(webviewPort)
                .numberOfBrowsers(numberOfBrowsers);
            
            flowCtx = builder.build();
            
            RolePropertyContainer props = getEnvProperties();
            props.add(ATCUI_SET_LOAD_FLOW_CTX_KEY, flowCtx);
            return getInstance();
		}

		@Override
		protected Builder builder() {
			return this;
		}

		@Override
		protected ATCUISetLoadRole getInstance() {
			return new ATCUISetLoadRole(this);
		}
		
	}
}
