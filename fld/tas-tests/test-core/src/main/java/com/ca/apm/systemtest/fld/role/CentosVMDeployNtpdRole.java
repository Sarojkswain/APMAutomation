/**
 * 
 */
package com.ca.apm.systemtest.fld.role;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;

/**
 * Deploys and configures NTPD on a Centos VM - will work on physical machines as well, but
 * you probably don't want to mess with their configuration
 * 
 * @author KEYJA01
 *
 */
public class CentosVMDeployNtpdRole extends AbstractRole {
    private ArrayList<String> ntpServers;
    private RunCommandFlowContext initInstallNtpCtx;
    private RunCommandFlowContext startNtpdCtx;
    private FileModifierFlowContext editNtpConfCtx;

    public CentosVMDeployNtpdRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.ntpServers = builder.ntpServers;
        this.initInstallNtpCtx = builder.initInstallNtpCtx;
        this.startNtpdCtx = builder.startNtpdCtx;
        this.editNtpConfCtx = builder.editNtpConfCtx;
    }

    public static class Builder extends BuilderBase<Builder, CentosVMDeployNtpdRole> {
        private String roleId;
        private ArrayList<String> ntpServers = new ArrayList<>();
        private RunCommandFlowContext initInstallNtpCtx;
        private RunCommandFlowContext startNtpdCtx;
        private FileModifierFlowContext editNtpConfCtx;
        
        public Builder(String roleId) {
            this.roleId = roleId;
        }
        
        public Builder ntpServer(String ntpServer) {
            ntpServers.add(ntpServer);
            return this;
        }
        
        @Override
        public CentosVMDeployNtpdRole build() {
            initInstallNtp();
            initEditNtpConf();
            initRestartNtpd();
            return getInstance();
        }
        
        private void initRestartNtpd() {
            // ensures that ntpd is restarted and running with the new configuration
            RunCommandFlowContext.Builder builder = new RunCommandFlowContext.Builder("/sbin/service")
                .args(Arrays.asList("ntpd", "restart"))
                .doNotPrependWorkingDirectory()
                ;
            this.startNtpdCtx = builder.build();
        }

        private void initEditNtpConf() {
            String confFile = "/etc/ntp.conf";
            Map<String, String> replacePairs = new HashMap<>();
            for (int i = 0; i < 10; i++) {
                replacePairs.put("server " + i, "#server " + i);
            }
            Collection<String> values = new ArrayList<>();
            for (String ntpServer: ntpServers) {
                values.add("server " + ntpServer + " iburst");
            }
            values.add("minpoll 6");
            values.add("maxpoll 8");
            FileModifierFlowContext.Builder builder = new FileModifierFlowContext.Builder()
                .replace(confFile, replacePairs)
                .append(confFile, values)
                ;
            
            this.editNtpConfCtx = builder.build();
        }

        private void initInstallNtp() {
            RunCommandFlowContext.Builder builder = new RunCommandFlowContext.Builder("yum")
                .args(Arrays.asList("-y", "install", "ntp"))
                .doNotPrependWorkingDirectory()
                ;
            this.initInstallNtpCtx = builder.build();
        }
        

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected CentosVMDeployNtpdRole getInstance() {
            return new CentosVMDeployNtpdRole(this);
        }
        
    }

    
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        // "yum install ntp"
        runFlow(aaClient, initInstallNtpCtx);
        
        // vi /etc/ntp.conf
        runFlow(aaClient, editNtpConfCtx);
        
        // service ntpd start
        runFlow(aaClient, startNtpdCtx);
    }
    
}
