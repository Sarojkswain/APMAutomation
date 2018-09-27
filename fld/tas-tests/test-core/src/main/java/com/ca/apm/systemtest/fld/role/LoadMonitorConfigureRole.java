/**
 * 
 */
package com.ca.apm.systemtest.fld.role;

import org.springframework.util.Assert;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.systemtest.fld.flow.ConfigureTomcatForLoadMonitorFlow;
import com.ca.apm.systemtest.fld.flow.ConfigureTomcatForLoadMonitorFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.webapp.TomcatRole;

/**
 * @author keyja01
 *
 */
public class LoadMonitorConfigureRole extends AbstractRole {
    private ConfigureTomcatForLoadMonitorFlowContext.Builder configureFlow;
    private RunCommandFlowContext stopTomcatContext;
    private RunCommandFlowContext startTomcatContext;

    private LoadMonitorConfigureRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        configureFlow = builder.configureFlow;
        startTomcatContext = builder.tomcatRole.getStartCmdFlowContext();
        stopTomcatContext = builder.tomcatRole.getStopCmdFlowContext();
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        boolean stopped = false;
        try {
            runCommandFlow(aaClient, stopTomcatContext);
            stopped = true;
        } catch (Exception e) {
            // ignore - it might have already been stopped
        }
        runFlow(aaClient, ConfigureTomcatForLoadMonitorFlow.class, configureFlow.build());
        if (stopped) {
            // if we stopped it, we need to restart
            runCommandFlow(aaClient, startTomcatContext);
        }
    }

    
    public static final class Builder extends BuilderBase<Builder, LoadMonitorConfigureRole> {
        private String roleId;
        private ConfigureTomcatForLoadMonitorFlowContext.Builder configureFlow;
        private String markerDir;
        private String loadmonDir = "loadmon";
        private String loadmonFormatString = "set LOADMON=%s";
        private TomcatRole tomcatRole;
        
        public Builder(String roleId, ITasResolver resolver) {
            this.roleId = roleId;
            markerDir("markers");
        }
        
        public Builder tomcatRole(TomcatRole tomcatRole) {
            this.tomcatRole = tomcatRole;
            return this;
        }
        
        public Builder markerDir(String markerDir) {
            this.markerDir = markerDir;
            return this;
        }
        
        public Builder loadmonDir(String loadmonDir) {
            this.loadmonDir = loadmonDir;
            return this;
        }
        
        public Builder loadmonFormatString(String loadmonFormatString) {
            this.loadmonFormatString = loadmonFormatString;
            return this;
        }

        @Override
        public LoadMonitorConfigureRole build() {
            Assert.notNull(tomcatRole, "LoadMonitorConfigureRole: you must set tomcatRole (or fight the bear)");
            
            String loadmonBase = concatPaths(getDeployBase(), loadmonDir);
            configureFlow = ConfigureTomcatForLoadMonitorFlowContext.getBuilder()
                .markerDir(concatPaths(loadmonBase, markerDir))
                .loadmonDir(loadmonBase)
                .loadmonFormatString(loadmonFormatString)
                .environmentFile("bin/setenv.bat");
            
            LoadMonitorConfigureRole inst = getInstance();
            return inst;
        }

        @Override
        protected LoadMonitorConfigureRole getInstance() {
            return new LoadMonitorConfigureRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
        
    }
}
