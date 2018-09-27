package com.ca.tas.role;

import java.util.Map;

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.envproperty.RoleEnvironmentProperties;

public class ControllerDeployFreeRole extends AbstractRole {

	private final int jenkinsPort;
	
    public ControllerDeployFreeRole(Builder builder) {
        super(builder.roleId);
        this.jenkinsPort = builder.jenkinsPort;
    }

    @Override
    public Map<String, String> getEnvProperties() {
        //store controller type information into env type properties
        properties.put(RoleEnvironmentProperties.ROLE_TYPE_KEY, ControllerRole.class.getSimpleName());
        properties.put("port", String.valueOf(jenkinsPort));
        return properties;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        // TODO Auto-generated method stub
        
    }

    public static class Builder implements IBuilder<ControllerDeployFreeRole> {

        private final String roleId;
        private int jenkinsPort;
        
        public Builder(String roleId) {
            this.roleId = roleId;
        }

        public Builder jenkinsPort(int jenkinsPort) {
        	this.jenkinsPort = jenkinsPort;
        	return this;
        }
        
        @Override
        public ControllerDeployFreeRole build() {
            return new ControllerDeployFreeRole(this);
        }
    }

}
