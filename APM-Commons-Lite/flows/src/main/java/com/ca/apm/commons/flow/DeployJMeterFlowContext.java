package com.ca.apm.commons.flow;

import com.ca.apm.automation.action.flow.AutowireCapable;
import com.ca.tas.builder.BuilderBase;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author Boch, Tomas (bocto01@ca.com)
 */
public final class DeployJMeterFlowContext implements AutowireCapable {

    /**
     * The URL location of artifact representing distribution of Apache JMeter
     */
    private final URL jmeterBinariesArtifactURL;

    /**
     * The location of folder where the Apache JMeter should be installed
     */
    private final String jmeterInstallDir;

    /**
     * The location of home directory for JDK required by Tomcat to run
     */
    private final String jdkHomeDir;

    /**
     * JMeter startup command
     */
    private final String startCommand;
    /**
     * JMeter shutdown command
     */
    private final String stopCommand;

    private final String jmeterBinDir;

    private final String jmeterExtDir;

    private final String jmLaunch;

    private final List<String> startCommandParams;

    private final URL jmeterScriptsArchiveURL;

    private DeployJMeterFlowContext(Builder builder) {
        this.jmeterBinariesArtifactURL = builder.jmeterBinariesArtifactURL;
        this.jmeterScriptsArchiveURL = builder.jmeterScriptsArchiveURL;
        this.jmeterInstallDir = builder.jmeterInstallDir;
        this.jdkHomeDir = builder.jdkHomeDir;
        this.startCommand = builder.startCommand;
        this.stopCommand = builder.stopCommand;
        this.jmeterBinDir = builder.workDir;
        this.jmeterExtDir = builder.extDir;
        this.jmLaunch = builder.jmLaunch;
        this.startCommandParams = builder.buildStartCommand();
    }

    @NotNull
    @Override
    public Class<? extends DeployJMeterFlow> autowiredFlow() {
        return DeployJMeterFlow.class;
    }

    public URL getJMeterBinariesArtifactURL() {
        return jmeterBinariesArtifactURL;
    }

    public URL getJMeterScriptsArtifactURL() {
        return jmeterScriptsArchiveURL;
    }

    public String getJMeterInstallDir() {
        return jmeterInstallDir;
    }

    public String getJdkHomeDir() {
        return jdkHomeDir;
    }

    public String getStartCommand() {
        return startCommand;
    }

    public String getStopCommand() {
        return stopCommand;
    }

    @NotNull
    public String getJMeterBinDir() {
        return jmeterBinDir;
    }

    @NotNull
    public String getJMeterExtDir() {
        return jmeterExtDir;
    }

    public String getJmLaunch() {
        return jmLaunch;
    }

    public List<String> getStartCommandParams() {
        return Collections.unmodifiableList(startCommandParams);
    }

    public static class LinuxBuilder extends Builder {
        public static final String LINUX_START_COMMAND = "jmeter.sh";
        // TODO
        public static final String LINUX_STOP_COMMAND = "";

        public LinuxBuilder() {
            startCommand = LINUX_START_COMMAND;
            stopCommand = LINUX_STOP_COMMAND;
        }

        @Override
        protected LinuxBuilder builder() {
            return this;
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }

        @Override
        protected String getDeployBase() {
            return getLinuxDeployBase();
        }

        @Override
        protected String getJavaBase() {
            return getLinuxJavaBase();
        }

        @Override
        protected void initJmLaunch(String jmLaunch) {
            this.jmLaunch = jmLaunch;
        }
    }

    public static class Builder extends BuilderBase<Builder, DeployJMeterFlowContext> {
        public static final String WIN_START_COMMAND = "jmeter.bat";
        public static final String WIN_STOP_COMMAND =
            "wmic process where \"CommandLine like '%java%ApacheJMeter%' and not (CommandLine like '%wmic%')\" Call Terminate";
        protected static final String DEFAULT_WORK_DIR = "bin";
        protected static final String JMETER_LIB_DIR = "lib";
        protected static final String JMETER_EXT_DIR = "ext";
        protected static final String JAVA = "java.exe";

        protected String startCommand = WIN_START_COMMAND;
        protected String stopCommand = WIN_STOP_COMMAND;

        @Nullable
        protected URL jmeterBinariesArtifactURL;
        protected String jmeterInstallDir;
        protected String workDir;
        protected String extDir;
        protected String jmLaunch;
        protected String jdkHomeDir;
        protected String testPlan;
        protected String jmeterLogFile;
        protected String logFile;
        protected String outputFile;
        protected Map<String, String> jmeterProperties;
        private URL jmeterScriptsArchiveURL;

        public Builder() {
            super();
        }

        @Override
        public DeployJMeterFlowContext build() {
            initWorkDir(concatPaths(jmeterInstallDir, DEFAULT_WORK_DIR));
            initExtDir(concatPaths(jmeterInstallDir, JMETER_LIB_DIR, JMETER_EXT_DIR));
            initJmLaunch(concatPaths(jdkHomeDir, DEFAULT_WORK_DIR, JAVA));
            return getInstance();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected DeployJMeterFlowContext getInstance() {
            return new DeployJMeterFlowContext(this);
        }

        public Builder jmeterBinariesArtifactURL(URL jmeterBinariesArtifactURL) {
            this.jmeterBinariesArtifactURL = jmeterBinariesArtifactURL;
            return builder();
        }
        
        public Builder jmeterScriptsArchive(URL jmeterScriptsArchiveURL) {
            this.jmeterScriptsArchiveURL = jmeterScriptsArchiveURL;
            return this;
        }

        public Builder installDir(String jmeterInstallDir) {
            this.jmeterInstallDir = jmeterInstallDir;
            return builder();
        }

        protected void initWorkDir(String workDirPath) {
            workDir = workDirPath;
        }

        protected void initExtDir(String extDirPath) {
            extDir = extDirPath;
        }

        protected void initJmLaunch(String jmLaunch) {
            this.jmLaunch = '"' + jmLaunch + '"';
        }

        public Builder jdkHomeDir(String jdkHomeDir) {
            this.jdkHomeDir = jdkHomeDir;
            return builder();
        }

        public Builder testPlan(String testPlan) {
            this.testPlan = testPlan;
            return builder();
        }

        public Builder jmeterLogFile(String jmeterLogFile) {
            this.jmeterLogFile = jmeterLogFile;
            return builder();
        }
        
        public Builder outputFile(String outputFile) {
            this.outputFile = outputFile;
            return builder();
        }

        public Builder logFile(String logFile) {
            this.logFile = logFile;
            return builder();
        }

        public Builder jmeterProperties(Map<String, String> jmeterProperties) {
            this.jmeterProperties = jmeterProperties;
            return builder();
        }

        public List<String> buildStartCommand() {
            List<String> startCommandParams = new ArrayList<>(10);
            if (isNotBlank(testPlan)) {
                startCommandParams.add("-n");
                startCommandParams.add("-t");
                startCommandParams.add(testPlan);
                if (isNotBlank(jmeterLogFile)) {
                    startCommandParams.add("-j");
                    startCommandParams.add(jmeterLogFile);
                }
                if (isNotBlank(logFile)) {
                    startCommandParams.add("-l");
                    startCommandParams.add(logFile);
                }
                if (jmeterProperties != null) {
                    for (String key : jmeterProperties.keySet()) {
                        startCommandParams.add("-J" + key + "=" + jmeterProperties.get(key));
                    }
                }
                //redirect output to file
                if (isNotBlank(outputFile)) {
                    startCommandParams.add(">");
                    startCommandParams.add(outputFile);
                }
            }
            return startCommandParams;
        }
    }

}
