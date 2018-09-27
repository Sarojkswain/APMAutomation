package com.ca.tas.role;

import java.util.Map;

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.tas.client.IAutomationAgentClient;

public class QCUploadToolRole extends AbstractRole {

    private String javaPath;
    
    private QCUploadToolRole(Builder builder) {
        super(builder.roleId);
        this.javaPath = builder.javaPath;
    }

    @Override
    public void deploy(IAutomationAgentClient automationAgentClient) {
        
    }

    
    /* (non-Javadoc)
     * @see com.ca.tas.role.AbstractRole#getEnvProperties()
     */
    @Override
    public Map<String, String> getEnvProperties() {
        Map<String, String> envProps = super.getEnvProperties();
        if (javaPath != null) {
            envProps.put("qcuploadtool.java.home", javaPath);
        }
        return envProps;
    }

    public static class Builder implements IBuilder<QCUploadToolRole> {

        private String roleId;
        private String javaPath;
        
        public Builder(String roleId) {
            this.roleId = roleId;
        }
        
        public Builder setJavaPath(String javaPath) {
            this.javaPath = javaPath;
            return this;
        }
        
        @Override
        public QCUploadToolRole build() {
            return new QCUploadToolRole(this);
        }
        
    }
}
