/**
 * 
 */
package com.ca.apm.systemtest.fld.role;

import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;

/**
 * Simple role to insert a configurable delay into a deployment
 * @author keyja01
 *
 */
public class DelayRole extends AbstractRole {
    private int delaySeconds;
    
    protected DelayRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        delaySeconds = builder.delaySeconds;
    }
    
    public static class Builder extends BuilderBase<Builder, DelayRole> {
        private String roleId;
        protected int delaySeconds = 60;
        
        public Builder(String roleId) {
            this.roleId = roleId;
        }
        
        public Builder delaySeconds(int s) {
            this.delaySeconds = s;
            return this;
        }

        @Override
        public DelayRole build() {
            DelayRole instance = getInstance();
            return instance;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected DelayRole getInstance() {
            return new DelayRole(this);
        }
        
    }
    

    /* (non-Javadoc)
     * @see com.ca.tas.role.Deployable#deploy(com.ca.tas.client.IAutomationAgentClient)
     */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        try {
            long ms = delaySeconds * 1000L;
            Thread.sleep(ms);
        } catch (Exception e) {
        }
    }

}
