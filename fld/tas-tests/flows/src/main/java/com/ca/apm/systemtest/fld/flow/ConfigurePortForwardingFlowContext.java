/**
 * 
 */
package com.ca.apm.systemtest.fld.flow;

import com.ca.apm.automation.action.flow.IFlowContext;

/**
 * @author keyja01
 *
 */
public class ConfigurePortForwardingFlowContext implements IFlowContext {
    protected int listenPort;
    protected int targetPort;
    protected String targetIpAddress;
    protected String workDir;


    public static Builder getBuilder() {
        BuilderFactory<ConfigurePortForwardingFlowContext, Builder> fact = new BuilderFactory<>();
        return fact.newBuilder(ConfigurePortForwardingFlowContext.class, Builder.class);
    }
    
    
    public interface Builder extends IGenericBuilder<ConfigurePortForwardingFlowContext> {
        public Builder listenPort(int listenPort);
        public Builder targetPort(int targetPort);
        public Builder targetIpAddress(String targetIpAddress);
        public Builder workDir(String workDir);
    }
}
