/**
 * 
 */
package com.ca.apm.systemtest.fld.role;

import com.ca.apm.automation.action.flow.FlowConfig.FlowConfigBuilder;
import com.ca.apm.systemtest.fld.flow.mm.UpdateFLDMMFlow;
import com.ca.apm.systemtest.fld.flow.mm.UpdateFLDMMFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;

/**
 * @author keyja01
 *
 */
public class UpdateFLDMMRole extends AbstractRole {

    private String mmJarFile;
    private String emailAddresses;
    
    public UpdateFLDMMRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        mmJarFile = builder.mmJarFile;
        emailAddresses = builder.emails.toString();
    }
    
    public static class Builder extends BuilderBase<Builder, UpdateFLDMMRole> {
        private String roleId;
        private String mmJarFile;
        private StringBuffer emails = new StringBuffer();
        
        public Builder(String roleId) {
            this.roleId = roleId;
        }
        
        public Builder mmJarFile(String mmJarFile) {
            this.mmJarFile = mmJarFile;
            return this;
        }
        
        public Builder emailAddress(String addr) {
            if (emails.length() > 0) {
                emails.append(",");
            }
            emails.append(addr);
            return this;
        }

        @Override
        public UpdateFLDMMRole build() {
            // TODO Auto-generated method stub
            return getInstance();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected UpdateFLDMMRole getInstance() {
            return new UpdateFLDMMRole(this);
        }
        
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        UpdateFLDMMFlowContext.Builder builder = UpdateFLDMMFlowContext.getInstance()
            .emailAddresses(emailAddresses)
            .mmJarFile(mmJarFile);
        aaClient.runJavaFlow(new FlowConfigBuilder(UpdateFLDMMFlow.class, builder.build(), 
            getHostingMachine().getHostnameWithPort()));
    }
}
