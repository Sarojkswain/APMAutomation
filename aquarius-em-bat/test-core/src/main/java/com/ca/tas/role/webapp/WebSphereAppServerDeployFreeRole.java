package com.ca.tas.role.webapp;

import java.util.Map;

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.envproperty.RoleEnvironmentProperties;
import com.ca.tas.property.RolePropertyContainer;
import com.ca.tas.property.TestProperty;
import com.ca.tas.role.AbstractRole;

public class WebSphereAppServerDeployFreeRole extends AbstractRole {
    private final RolePropertyContainer propertyContainer;

    /**
     * Sets up the WebSphere role and defines its properties
     *
     * @param builder Builder object containing all necessary data
     */
    protected WebSphereAppServerDeployFreeRole(Builder builder) {
        super(builder.roleId);
        propertyContainer = builder.envPropertyContainer;
    }

    @Override
    public Map<String, String> getEnvProperties() {
        properties.putAll(new RoleEnvironmentProperties(getRoleId(), propertyContainer.getTestPropertiesAsProperties()));
        return properties;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        // TODO Auto-generated method stub
    }

    public static class Builder implements IBuilder<WebSphereAppServerDeployFreeRole> {

        //required
        private final String roleId;
        private final RolePropertyContainer envPropertyContainer = new RolePropertyContainer();


        public Builder(String roleId) {
            this.roleId = roleId;
        }

        public Builder wasInstallLocation(String pathToWasHome) {
            envPropertyContainer.add(new TestProperty<>("was7.home", pathToWasHome));
            return this;
        }

        public WebSphereAppServerDeployFreeRole build() {
            return new WebSphereAppServerDeployFreeRole(this);
        }
    }

}
