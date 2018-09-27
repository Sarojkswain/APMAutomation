/**
 * 
 */
package com.ca.apm.systemtest.fld.flow.agent;

import java.io.File;
import java.util.Map.Entry;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.utils.configuration.ConfigurationFile;
import com.ca.apm.automation.utils.configuration.ConfigurationFileFactory;

/**
 * @author keyja01
 *
 */
@Flow
public class ReconfigureAgentFlow implements IAutomationFlow {
    
    @FlowContext
    private ReconfigureAgentFlowContext ctx;
    

    /* (non-Javadoc)
     * @see com.ca.apm.automation.action.core.IAutomationFlow#run()
     */
    @Override
    public void run() throws Exception {
        ConfigurationFileFactory cff = new ConfigurationFileFactory();
        if (ctx.agentConfigFile == null) {
            return;
        }
        
        ConfigurationFile cf = cff.create(new File(ctx.agentConfigFile));
        if (ctx.agentName != null) {
            cf.addOrUpdate("introscope.agent.agentName", ctx.agentName);
        }
        if (ctx.properties != null) {
            for (Entry<String, String> entry: ctx.properties.entrySet()) {
                cf.addOrUpdate(entry.getKey(), entry.getValue());
            }
        }
    }

}
