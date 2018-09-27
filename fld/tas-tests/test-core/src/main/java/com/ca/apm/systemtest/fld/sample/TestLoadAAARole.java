/**
 * 
 */
package com.ca.apm.systemtest.fld.sample;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.systemtest.fld.role.loads.AbstractFldLoadRole;
import com.ca.apm.systemtest.fld.role.loads.FldLoadBuilderBase;
import com.ca.apm.systemtest.fld.sample.DeployTestLoadAAAFlow;
import com.ca.apm.systemtest.fld.sample.DeployTestLoadAAAFlowContext;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;

/**
 * @author keyja01
 *
 */
public class TestLoadAAARole extends AbstractFldLoadRole {
    private String installDir;
    public final static String START_AAA_FLOW_KEY = "startAAAFlow";
    public final static String STOP_AAA_FLOW_KEY = "stopAAAFlow";
    
    protected TestLoadAAARole(Builder builder) {
        super(builder.getRoleId(), builder.getEnvProperties());
        installDir = builder.installDir;
    }
    
    public static class Builder extends FldLoadBuilderBase<Builder, TestLoadAAARole> {
        private String installDir;
        
        public Builder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
            startLoadKey = START_AAA_FLOW_KEY;
            stopLoadKey = STOP_AAA_FLOW_KEY;
        }
        
        @Override
        protected TestLoadAAARole buildRole() {
            TestLoadAAARole testLoadAAA = getInstance();
            
            return testLoadAAA;
        }


        @Override
        protected RunCommandFlowContext.Builder createStartLoadFlowContextBuilder() {
            RunCommandFlowContext.Builder builder = new RunCommandFlowContext.Builder(getStartLoadCommand())
                .workDir(installDir);
            return builder;
        }
        
        public Builder installDir(String installDir) {
            this.installDir = getDeployBase() + getPathSeparator() + installDir;
            return this;
        }
        
        protected String getStartLoadCommand() {
            return "startLoadAAA.cmd";
        }
        
        protected String getStopLoadCommand() {
            return "stopLoadAAA.cmd";
        }

        @Override
        protected RunCommandFlowContext.Builder createStopLoadFlowContextBuilder() {
            RunCommandFlowContext.Builder builder = new RunCommandFlowContext.Builder(getStopLoadCommand())
                .workDir(installDir);
            return builder;
        }

        @Override
        protected TestLoadAAARole getInstance() {
            TestLoadAAARole role = new TestLoadAAARole(this);
            return role;
        }

        @Override
        protected Builder builder() {
            return this;
        }
        
    }

    /* (non-Javadoc)
     * @see com.ca.tas.role.Deployable#deploy(com.ca.tas.client.IAutomationAgentClient)
     */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        DeployTestLoadAAAFlowContext.Builder builder = new DeployTestLoadAAAFlowContext.Builder()
            .installDir(installDir);
        runFlow(aaClient, DeployTestLoadAAAFlow.class, builder.build());
    }
}
