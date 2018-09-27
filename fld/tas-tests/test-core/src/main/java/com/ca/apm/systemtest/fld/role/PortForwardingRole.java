/**
 * 
 */
package com.ca.apm.systemtest.fld.role;

import com.ca.apm.systemtest.fld.flow.ConfigurePortForwardingFlow;
import com.ca.apm.systemtest.fld.flow.ConfigurePortForwardingFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;

/**
 * @author keyja01
 *
 */
public class PortForwardingRole extends AbstractRole {
    private int listenPort;
    private String targetIpAddress;
    private int targetPort;
    private String workDir;

    public PortForwardingRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.listenPort = builder.listenPort;
        this.targetIpAddress = builder.targetIpAddress;
        this.targetPort = builder.targetPort;
        this.workDir = builder.workDir;
    }
    
    public int getListenPort() {
        return this.listenPort;
    }
    
    public String getTargetIpAddress() {
        return this.targetIpAddress;
    }
    
    public int getTargetPort() {
        return this.targetPort;
    }

    public static class Builder extends BuilderBase<Builder, PortForwardingRole> {
        public String workDir;
        public int targetPort;
        public String targetIpAddress;
        public int listenPort;
        private String roleId;
        
        public Builder(String roleId) {
            this.roleId = roleId;
        }
        
        public Builder workDir(String workDir) {
            this.workDir = concatPaths(getDeployBase(), "tunnels", workDir);
            return this;
        }
        
        public Builder listenPort(int port) {
            this.listenPort = port;
            return this;
        }
        
        public Builder targetPort(int port) {
            this.targetPort = port;
            return this;
        }
        
        public Builder targetIpAddress(String addr) {
            this.targetIpAddress = addr;
            return this;
        }

        @Override
        public PortForwardingRole build() {
            return getInstance();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected PortForwardingRole getInstance() {
            return new PortForwardingRole(this);
        }
        
        @Override
        protected String getDeployBase() {
            return getLinuxDeployBase();
        }
        
        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }
        
    }
    
    
    /* (non-Javadoc)
     * @see com.ca.tas.role.Deployable#deploy(com.ca.tas.client.IAutomationAgentClient)
     */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        ConfigurePortForwardingFlowContext ctx = ConfigurePortForwardingFlowContext.getBuilder()
            .listenPort(listenPort)
            .targetIpAddress(targetIpAddress)
            .targetPort(targetPort)
            .workDir(workDir)
            .build();
        
        runFlow(aaClient, ConfigurePortForwardingFlow.class, ctx);
    }

}
