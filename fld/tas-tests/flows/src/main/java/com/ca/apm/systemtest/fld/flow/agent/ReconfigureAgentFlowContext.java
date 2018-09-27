/**
 * 
 */
package com.ca.apm.systemtest.fld.flow.agent;

import java.util.Map;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.systemtest.fld.flow.BuilderFactory;
import com.ca.apm.systemtest.fld.flow.IGenericBuilder;

/**
 * @author keyja01
 *
 */
public class ReconfigureAgentFlowContext implements IFlowContext {
    protected String agentConfigFile;
    protected String agentName;
    protected Map<String, String> properties;
    
    
    public static Builder getBuilder() {
        BuilderFactory<ReconfigureAgentFlowContext, Builder> fact = new BuilderFactory<>();
        return fact.newBuilder(ReconfigureAgentFlowContext.class, Builder.class);
    }
    
    
    public interface Builder extends IGenericBuilder<ReconfigureAgentFlowContext> {
        public Builder agentConfigFile(String agentConfigFile);
        public Builder agentName(String agentName);
        public Builder properties(Map<String, String> properties);
    }
}
