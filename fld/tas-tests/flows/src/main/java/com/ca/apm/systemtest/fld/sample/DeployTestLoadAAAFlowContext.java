/**
 * 
 */
package com.ca.apm.systemtest.fld.sample;

import org.apache.http.util.Args;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;

/**
 * @author keyja01
 *
 */
public class DeployTestLoadAAAFlowContext implements IFlowContext {
    private String installDir;

    protected DeployTestLoadAAAFlowContext(Builder builder) {
        installDir = builder.installDir;
    }
    
    public static class Builder extends BuilderBase<Builder, DeployTestLoadAAAFlowContext> {
        private String installDir;

        @Override
        public DeployTestLoadAAAFlowContext build() {
            DeployTestLoadAAAFlowContext ctx = getInstance();
            Args.notNull(ctx.installDir, "You must specify installDir");
            return ctx;
        }

        @Override
        protected DeployTestLoadAAAFlowContext getInstance() {
            return new DeployTestLoadAAAFlowContext(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
        
        public Builder installDir(String installDir) {
            this.installDir = installDir;
            return this;
        }
        
    }
    
    public String getInstallDir() {
        return installDir;
    }
}
