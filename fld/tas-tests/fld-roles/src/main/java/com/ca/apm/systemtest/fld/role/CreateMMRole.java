/**
 * 
 */
package com.ca.apm.systemtest.fld.role;

import java.util.List;

import com.ca.apm.systemtest.fld.flow.mm.CreateMMFlow;
import com.ca.apm.systemtest.fld.flow.mm.CreateMMFlowContext;
import com.ca.apm.systemtest.fld.flow.mm.Dashboard;
import com.ca.apm.systemtest.fld.flow.mm.FullMetricSpecifier;
import com.ca.apm.systemtest.fld.flow.mm.ManagementModule;
import com.ca.apm.systemtest.fld.flow.mm.MetricGroup;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;

/**
 * @author keyja01
 *
 */
public class CreateMMRole extends AbstractRole {
    private String deployDir;
    private String filename;
    private ManagementModule mm;
    
    public static class Builder extends BuilderBase<Builder, CreateMMRole> {
        private String roleId = null;
        private ManagementModule mm;
        private String filename;
        private String deployDir;

        public Builder(String roleId, String managementModuleName) {
            this.roleId = roleId;
            mm = new ManagementModule();
            mm.setName(managementModuleName);
        }
        
        @Override
        public CreateMMRole build() {
            
            return getInstance();
        }
        
        /**
         * Create a new MetricGroup in the 
         * @param name
         * @param description
         * @return
         */
        public Builder addMetricGroup(String name, String description, List<FullMetricSpecifier> metricSpecs) {
            MetricGroup mg = new MetricGroup(name, description);
            mg.setFullMetricSpecs(metricSpecs);
            mm.addMetricGroup(mg);
            
            return this;
        }
        
        
        public Builder addDashboard(Dashboard db) {
            mm.addDashboard(db);
            return this;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected CreateMMRole getInstance() {
            return new CreateMMRole(this);
        }
        
        public Builder filename(String filename) {
            this.filename = filename;
            return this;
        }
        
        public Builder deployDir(String deployDir) {
            this.deployDir = deployDir;
            return this;
        }
    }
    
    private CreateMMRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.mm = builder.mm;
        this.deployDir = builder.deployDir;
        this.filename = builder.filename;
    }

    /* (non-Javadoc)
     * @see com.ca.tas.client.Deployable#deploy(com.ca.tas.client.IAutomationAgentClient)
     */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        CreateMMFlowContext ctx = CreateMMFlowContext.getInstance()
            .deployDir(deployDir).filename(filename).managementModule(mm)
            .build();
        runFlow(aaClient, CreateMMFlow.class, ctx);
    }

}
