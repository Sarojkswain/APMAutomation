/**
 * 
 */
package com.ca.apm.systemtest.fld.role.loads;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.systemtest.fld.role.JMeterRole;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;

/**
 * Role to run a jmeter script in an already deployed {@link JMeterRole}
 * @author keyja01
 *
 */
public class JMeterLoadRole extends AbstractFldLoadRole {
    public final static String START_LOAD_FLOW_KEY = "startJMeterFlow";
    public final static String STOP_LOAD_FLOW_KEY = "stopJMeterFlow";
    
    protected JMeterLoadRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
    }
    
    
    public static class Builder extends FldLoadBuilderBase<Builder, JMeterLoadRole> {
        private String hostParameterName = "host";
        private String portParameterName = "port";
        private String hostHeaderParameterName = "hostHeader";
        private String host;
        private Integer port;
        private String script;
        private String resultFile;
        private boolean isOutputToFile = false;
        private JMeterRole jmeterRole;
        private String hostHeader;

        public Builder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
            startLoadKey = START_LOAD_FLOW_KEY;
            stopLoadKey = STOP_LOAD_FLOW_KEY;
        }

        
        public Builder script(String script) {
            this.script = script;
            return this;
        }
        
        public Builder isOutputToFile(boolean isOutputToFile) {
            this.isOutputToFile = isOutputToFile;
            return this;
        }
        
        public Builder resultFile(String resultFile) {
            this.resultFile = resultFile;
            return this;
        }
        
        
        public Builder jmeter(JMeterRole jmeterRole) {
            assert jmeterRole != null;
            this.jmeterRole = jmeterRole;
            return this;
        }
        
        
        /**
         * The hostname of the remote app server
         * @param host
         * @return
         */
        public Builder host(String host) {
            assert host != null;
            this.host = host;
            return this;
        }
        
        
        /**
         * 
         * @param hostHeader
         * @return
         */
        public Builder hostHeader(String hostHeader) {
            assert hostHeader != null;
            this.hostHeader = hostHeader;
            return this;
        }
        
        /**
         * The port the remote app server is listening to
         * @param port
         * @return
         */
        public Builder port(Integer port) {
            assert port != null;
            this.port = port;
            return this;
        }
        
        
        /**
         * Sets the name of the parameter used for host in the JMeter script.  Defaults to "host"
         * @param hostParameterName
         * @return
         */
        public Builder hostParameterName(String hostParameterName) {
            assert hostParameterName != null;
            this.hostParameterName = hostParameterName;
            return this;
        }
        
        
        /**
         * Sets the name of the parameter used for port in the JMeter script. Defaults to "port"
         * @param portParamName
         * @return
         */
        public Builder portParameterName(String portParamName) {
            assert portParamName != null;
            this.portParameterName = portParamName;
            return this;
        }
        
        
        public Builder hostHeaderParameterName(String hostHeaderParameterName) {
            assert hostHeaderParameterName != null;
            this.hostHeaderParameterName = hostHeaderParameterName;
            return this;
        }
        
        
        @Override
        protected JMeterLoadRole buildRole() {
            JMeterLoadRole role = getInstance();
            return role;
        }

        @Override
        protected RunCommandFlowContext.Builder createStartLoadFlowContextBuilder() {
            List<String> args = new ArrayList<>();
            args.add("-n");
            args.add("-t");
            String installDir = ".";
            if (jmeterRole != null && !StringUtils.isEmpty(jmeterRole.getInstallDir())) {
                installDir = jmeterRole.getInstallDir();
            }
            String scriptPath = Paths.get(installDir, "scripts", script).toString();
            args.add(scriptPath);
            if (resultFile != null && !resultFile.isEmpty()) {
                args.add("-l");
                String resultPath = Paths.get(installDir, "results", resultFile).toString();
                args.add(resultPath);
            }
            if (host != null) {
                args.add("-J" + hostParameterName + "=" + host);
            }
            if (port != null) {
                args.add("-J" + portParameterName + "=" + port);
            }
            if (hostHeader != null) {
                args.add("-J" + hostHeaderParameterName + "=" + hostHeader);
            }
            //redirect output to file
            if (isOutputToFile) {
                args.add(">");
                String outputPath = Paths.get(installDir, script+".log").toString();
                args.add(outputPath);
            }
            String binDir = Paths.get(installDir, "bin").toString();
            RunCommandFlowContext.Builder flow = new RunCommandFlowContext.Builder("jmeter.bat")
                .args(args)
                .workDir(binDir)
                ;
            
            return flow;
        }

        @Override
        protected RunCommandFlowContext.Builder createStopLoadFlowContextBuilder() {
            //"wmic process where \"CommandLine like '%java%ApacheJMeter%' and not (CommandLine like '%wmic%')\" Call Terminate";
            String stopCommand = "wmic process where \"CommandLine like '%java%ApacheJMeter%' and not (CommandLine like '%wmic%')\" Call Terminate";
            if (!StringUtils.isEmpty(script)) {
                stopCommand = String.format("wmic process where \"CommandLine like "
                    + "'%%java%%ApacheJMeter%%%s%%' and not (CommandLine like '%%wmic%%')\" "
                    + "Call Terminate", script);
            }
            RunCommandFlowContext.Builder flow = new RunCommandFlowContext.Builder(stopCommand);
            return flow;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected JMeterLoadRole getInstance() {
            JMeterLoadRole role = new JMeterLoadRole(this);
            return role;
        }
        
    }
    

    /* (non-Javadoc)
     * @see com.ca.tas.role.Deployable#deploy(com.ca.tas.client.IAutomationAgentClient)
     */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        // this role doesn't actually need to do anything
    }

}
