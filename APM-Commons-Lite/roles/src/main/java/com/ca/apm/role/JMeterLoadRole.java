package com.ca.apm.role;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.commons.flow.StartJMeterFlow;
import com.ca.tas.annotation.TasDocRole;
import com.ca.tas.annotation.TasEnvironmentPropertyKey;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.type.Platform;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Args;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Role to run a jmeter script in an already deployed {@link JMeterRole}
 *
 * @author keyja01, haiva01
 */
@TasDocRole(platform = {Platform.WINDOWS})
public class JMeterLoadRole extends AbstractRole {
    @TasEnvironmentPropertyKey
    public final static String START_JMETER_FLOW_KEY = "startJMeterFlow";
    @TasEnvironmentPropertyKey
    public final static String STOP_JMETER_FLOW_KEY = "stopJMeterFlow";

    protected boolean autoStart;
    protected RunCommandFlowContext startJmeterContext;
    protected RunCommandFlowContext stopJmeterContext;

    protected JMeterLoadRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        autoStart = builder.autoStart;
        startJmeterContext = builder.startJmeterContext;
        stopJmeterContext = builder.stopJmeterContext;
    }

    public RunCommandFlowContext getStartJmeterContext() {
        return startJmeterContext;
    }

    public RunCommandFlowContext getStopJmeterContext() {
        return stopJmeterContext;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        if (autoStart) {
            runFlow(aaClient, StartJMeterFlow.class, startJmeterContext);
        }
    }

    public static class Builder extends BuilderBase<Builder, JMeterLoadRole> {
        protected RunCommandFlowContext startJmeterContext;
        protected RunCommandFlowContext stopJmeterContext;
        private String roleId;
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
        private boolean autoStart = false;
        private Map<String, String> jmeterProperties = new LinkedHashMap<>(5);

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
        }

        public Builder autoStart() {
            this.autoStart = true;
            return builder();
        }

        public Builder jmeterProperties(Map<String, String> jmeterProperties) {
            Args.notNull(jmeterProperties, "JMeter properties");
            this.jmeterProperties = new LinkedHashMap<>(jmeterProperties);
            return builder();
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
         *
         * @param host
         * @return
         */
        public Builder host(String host) {
            assert host != null;
            this.host = host;
            return this;
        }


        /**
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
         *
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
         *
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
         *
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

        protected RunCommandFlowContext.Builder createStartLoadFlowContextBuilder() {
            List<String> args = new ArrayList<>(32);
            args.add("/S");
            args.add("/C");
            args.add("\"");
            args.add("start");
            args.add("cmd");
            args.add("/S");
            args.add("/C");
            args.add("\"");
            args.add("call");
            args.add("jmeter.bat");
            args.add("-n");
            args.add("-t");
            String installDir = ".";
            if (jmeterRole != null && !StringUtils.isEmpty(jmeterRole.getInstallDir())) {
                installDir = jmeterRole.getInstallDir();
            }
            String scriptPath = concatPaths(installDir, "testplan", script);
            args.add(scriptPath);
            if (resultFile != null && !resultFile.isEmpty()) {
                args.add("-l");
                String resultPath = concatPaths(installDir, "results", resultFile);
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
            if (jmeterProperties != null) {
                for (String key : jmeterProperties.keySet()) {
                    args.add("-J" + key + "=" + jmeterProperties.get(key));
                }
            }
            //redirect output to file
            if (isOutputToFile) {
                args.add(">");
                String outputPath = concatPaths(installDir, script + ".log");
                args.add(outputPath);
                args.add("2>&1");
            }
            args.add("\"");
            args.add("\"");
            String binDir = concatPaths(installDir, "bin");
            return new RunCommandFlowContext.Builder("cmd")
                .doNotPrependWorkingDirectory()
                .args(args)
                .workDir(binDir);
        }

        protected RunCommandFlowContext.Builder createStopLoadFlowContextBuilder() {
            //"wmic process where \"CommandLine like '%java%ApacheJMeter%' and not (CommandLine
            // like '%wmic%')\" Call Terminate";
            String stopCommand
                = "wmic process where \"CommandLine like '%java%ApacheJMeter%'"
                + " and not (CommandLine like '%wmic%')\" Call Terminate";
            if (!StringUtils.isEmpty(script)) {
                stopCommand = String.format("wmic process where "
                    + "\"CommandLine like '%%java%%ApacheJMeter%%%s%%'"
                    + " and not (CommandLine like '%%wmic%%')\""
                    + " Call Terminate", script);
            }
            return new RunCommandFlowContext.Builder(stopCommand);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected JMeterLoadRole getInstance() {
            return new JMeterLoadRole(this);
        }

        @Override
        public JMeterLoadRole build() {
            getEnvProperties().add(START_JMETER_FLOW_KEY,
                startJmeterContext = createStartLoadFlowContextBuilder().build());
            getEnvProperties().add(STOP_JMETER_FLOW_KEY,
                stopJmeterContext = createStopLoadFlowContextBuilder().build());
            return getInstance();
        }
    }

}
