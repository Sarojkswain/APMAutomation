/**
 * 
 */
package com.ca.apm.systemtest.fld.role.loads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.net.HostAndPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;

/**
 * Given a {@link WurlitzerBaseRole}, this role executes a specific scenario file targeted to a specific EM
 * @author keyja01
 *
 */
public class WurlitzerLoadRole extends AbstractFldLoadRole {
    public static final Logger logger = LoggerFactory.getLogger(WurlitzerLoadRole.class);
    public final static String START_WURLITZER_FLOW_KEY = "startWurlitzerFlow";
    public final static String STOP_WURLITZER_FLOW_KEY = "stopWurlitzerFlow";
    
    protected WurlitzerLoadRole(Builder builder) {
        super(builder.getRoleId(), builder.getEnvProperties());
    }
    
    
    public static class Builder extends FldLoadBuilderBase<Builder, WurlitzerLoadRole> {
        private String installDir;
        private WurlitzerBaseRole wurlitzerBase;
        private EmRole emRole;
        private HostAndPort hostAndPort;
        private String buildFileLocation;
        private String target;
        private String logFileName = "wurlitzer.log";
        
        public Builder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
            startLoadKey = START_WURLITZER_FLOW_KEY;
            stopLoadKey = STOP_WURLITZER_FLOW_KEY;
        }

        @Override
        protected WurlitzerLoadRole buildRole() {
            WurlitzerLoadRole role = getInstance();
            return role;
        }
        
        public Builder wurlitzerBaseRole(WurlitzerBaseRole wurlitzerBase) {
            
            this.wurlitzerBase = wurlitzerBase;
            return this;
        }
        
        public Builder logFile(String logFile) {
            this.logFileName = logFile;
            return this;
        }
        
        public  Builder emRole(EmRole emRole) {
            this.emRole = emRole;
            return this;
        }

        public Builder overrideEM(HostAndPort hostAndPort) {
            this.hostAndPort = hostAndPort;
            return this;
        }
        
        public Builder target(String target) {
            this.target = target;
            return this;
        }
        
        
        public Builder buildFileLocation(String buildFileLocation) {
            this.buildFileLocation = buildFileLocation;
            return this;
        }
        
        public Builder installDir(String installDir) {
            this.installDir = installDir;
            return this;
        }

        @Override
        protected RunCommandFlowContext.Builder createStartLoadFlowContextBuilder() {
            Map<String, String> env = new HashMap<>();

            String host = null;
            int port = 0;

            if (hostAndPort != null) {
                host = hostAndPort.getHostText();
                port = hostAndPort.getPort();
            } else if (emRole != null) {
                host = tasResolver.getHostnameById(emRole.getRoleId());
                port = emRole.getEmPort();
            }

            if (host != null) {
                env.put("WURLITZER_EM_HOST", host.trim());
            }
            if (port != 0) {
                env.put("WURLITZER_EM_PORT", Integer.toString(port));
            }
            env.put("ANT_OPTS", "-Xmx1536m");
            
            
            List<String> args = new ArrayList<>();
            args.add("-f");
            String wurlitzerDir = wurlitzerBase.getDeployDir();
            String scriptsDir = wurlitzerDir + "\\scripts\\";
            if (buildFileLocation != null) {
                args.add(scriptsDir + buildFileLocation);
            }
            logger.info("Using buildFileLocation: " + buildFileLocation);
            args.add(target);
            args.add(">");
            args.add(wurlitzerDir + "\\" + host.trim() + "_" + logFileName);
            
            if (installDir == null) {
                installDir = "wurlitzerInstall";
            }
            logger.info("Using installDir: " + installDir);
            
            RunCommandFlowContext.Builder builder = new RunCommandFlowContext.Builder("ant")
                .environment(env)
                .args(args)
                .terminateOnMatch("CONNECTED to");
            
            return builder;
        }

        @Override
        protected RunCommandFlowContext.Builder createStopLoadFlowContextBuilder() {
            // for now, we are not stopping the load
            List<String> args = new ArrayList<>();
            args.add("/c");
            args.add("dir.exe");
            args.add("c:\\");
            RunCommandFlowContext.Builder builder = new RunCommandFlowContext.Builder("cmd.exe")
                .doNotPrependWorkingDirectory()
                .args(args)
                .terminateOnMatch("");
            return builder;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected WurlitzerLoadRole getInstance() {
            WurlitzerLoadRole role = new WurlitzerLoadRole(this);
            return role;
        }
        
    }
    

    /* (non-Javadoc)
     * @see com.ca.tas.role.Deployable#deploy(com.ca.tas.client.IAutomationAgentClient)
     */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
//        aaClient.runJavaFlow(new FlowConfigBuilder(RunCommandFlow.class, startLoadContext, getHostWithPort()));
    }

}
