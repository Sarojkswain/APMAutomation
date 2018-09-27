package com.ca.apm.systemtest.fld.role;

import com.ca.apm.systemtest.fld.flow.RunRecordingSessionFlow;
import com.ca.apm.systemtest.fld.flow.ConfigureRecordingSessionFlowContext;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.flow.tess.TessConfigurer.RecordType;
import com.ca.tas.property.RolePropertyContainer;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * Role to provide special context to a machine on which recording sessions will be running using Selenium web driver.
 * By default, record type is Agent but it can be changed to TIM using specific role builder's setter. 
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class RecordingSessionRole extends AbstractRole {
    public static String RUN_RECORDING_SESSION_FLOW_CTX_KEY = "RUN_RECORDING_SESSION_FLOW_CTX_KEY";
    
    private boolean autostart;
    private ConfigureRecordingSessionFlowContext flowCtx;

    private RecordingSessionRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.autostart = builder.autostart;
        this.flowCtx = builder.flowCtx;
    }

	@Override
	public void deploy(IAutomationAgentClient automationAgentClient) {
		if (autostart) {
			runFlow(automationAgentClient, RunRecordingSessionFlow.class, flowCtx);	
		}
		
	}

	public static class Builder extends BuilderBase<Builder, RecordingSessionRole> {
        private String roleId;
		private String tessHost;
        private String tessUser = "cemadmin";
        private String tessPassword = "quality";
        private String clientIP;
        private int tessPort = 8081;
        private boolean autostart = false;
        private int recordingDurationMillis = FLDLoadConstants.DEFAULT_AGENT_RECORDING_SESSION_DURATION_IN_MILLIS;
        private RecordType recordType = RecordType.Agent;
        
        private ConfigureRecordingSessionFlowContext flowCtx;
        
        public Builder(String roleId, ITasResolver resolver) {
        	super();
        	this.roleId = roleId;
        }

        /**
         * Sets up the role to automatically run the configuration flow during the deploy phase.
         * 
         * @return
         */
        public Builder setAutostart() {
            this.autostart = true;
            return this;
        }

		public Builder setTessHost(String tessHost) {
			this.tessHost = tessHost;
			return this;
		}

		public Builder setTessUser(String tessUser) {
			this.tessUser = tessUser;
			return this;
		}

		public Builder setTessPassword(String tessPassword) {
			this.tessPassword = tessPassword;
			return this;
		}

		public Builder setTessPort(int tessPort) {
			this.tessPort = tessPort;
			return this;
		}

		public Builder setClientIP(String clientIP) {
			this.clientIP = clientIP;
			return this;
		}

		public Builder setRecordingDurationMillis(int recordingDurationMillis) {
			this.recordingDurationMillis = recordingDurationMillis;
			return this;
		}

		public Builder setTIMRecording() {
			this.recordType = RecordType.TIM;
			return this;
		}
		
		@Override
		public RecordingSessionRole build() {
			ConfigureRecordingSessionFlowContext.Builder builder = ConfigureRecordingSessionFlowContext.getBuilder();
            builder.tessUser(tessUser)
                .tessPassword(tessPassword)
                .tessHostname(tessHost)
                .tessPort(tessPort)
                .recordingDurationMillis(recordingDurationMillis)
                .clientIP(clientIP)
                .recordType(recordType);
            
            flowCtx = builder.build();
            
            RolePropertyContainer props = getEnvProperties();
            props.add(RUN_RECORDING_SESSION_FLOW_CTX_KEY, flowCtx);
            return getInstance();
		}

		@Override
		protected Builder builder() {
			return this;
		}

		@Override
		protected RecordingSessionRole getInstance() {
			return new RecordingSessionRole(this);
		}
		
	}
}
