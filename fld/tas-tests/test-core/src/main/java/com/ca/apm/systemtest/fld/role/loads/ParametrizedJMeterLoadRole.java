/**
 * 
 */
package com.ca.apm.systemtest.fld.role.loads;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.util.StringUtils;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.systemtest.fld.role.JMeterRole;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;

/**
 * Role for launching JMeter load with additional Jmeter parameters support. 
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class ParametrizedJMeterLoadRole extends AbstractFldLoadRole {

    public final static String START_LOAD_FLOW_KEY = "startJMeterFlow";
    public final static String STOP_LOAD_FLOW_KEY = "stopJMeterFlow";
    public final static String JMETER_EXECUTABLE = "jmeter.bat"; 
    public final static String DEFAULT_SCRIPTS_FOLDER = "scripts";
    public final static String RESULTS_FOLDER = "results";
    
    protected ParametrizedJMeterLoadRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
    }
    
    /* (non-Javadoc)
     * @see com.ca.tas.role.Deployable#deploy(com.ca.tas.client.IAutomationAgentClient)
     */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
    }
    
    public static class Builder extends FldLoadBuilderBase<Builder, ParametrizedJMeterLoadRole> {
        private String script;
        private String resultFile;
        private String scriptsFolder = DEFAULT_SCRIPTS_FOLDER;
        
        private boolean isOutputToFile = false;
        private JMeterRole jmeterRole;

        private Map<String, String> jmeterParameters = new HashMap<>();
        
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
        
        public Builder jmeterParameter(String paramName, String paramValue) {
            jmeterParameters.put(paramName, paramValue);
            return this;
        }
        
        public Builder jmeterParametersMap(Map<String, String> params) {
            jmeterParameters.putAll(params);
            return this;
        }
        
        public Builder scriptsFolder(String scriptsFolder) {
            this.scriptsFolder = scriptsFolder;
            return this;
        }
        
        @Override
        protected ParametrizedJMeterLoadRole buildRole() {
            ParametrizedJMeterLoadRole role = getInstance();
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
            String scriptPath = Paths.get(installDir, scriptsFolder, script).toString();
            args.add(scriptPath);
            if (resultFile != null && !resultFile.isEmpty()) {
                args.add("-l");
                String resultPath = Paths.get(installDir, RESULTS_FOLDER, resultFile).toString();
                args.add(resultPath);
            }
            
            if (!jmeterParameters.isEmpty()) {
                for (Entry<String, String> jmeterPropEntry : jmeterParameters.entrySet()) {
                    args.add("-J" + jmeterPropEntry.getKey() + "=" + jmeterPropEntry.getValue());    
                }
            }
            //redirect output to file
            if (isOutputToFile) {
                args.add(">");
                String outputPath = Paths.get(installDir, script + ".log").toString();
                args.add(outputPath);
            }
            String binDir = Paths.get(installDir, "bin").toString();
            RunCommandFlowContext.Builder flow = new RunCommandFlowContext.Builder(JMETER_EXECUTABLE)
                .args(args)
                .workDir(binDir);
            return flow;
        }

        @Override
        protected RunCommandFlowContext.Builder createStopLoadFlowContextBuilder() {
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
        protected ParametrizedJMeterLoadRole getInstance() {
            ParametrizedJMeterLoadRole role = new ParametrizedJMeterLoadRole(this);
            return role;
        }
        
    }
}
