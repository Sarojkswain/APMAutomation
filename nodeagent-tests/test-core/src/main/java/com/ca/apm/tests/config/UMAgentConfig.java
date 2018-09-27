package com.ca.apm.tests.config;

import static com.ca.apm.tests.utils.CommonUtils.createFullPath;

import java.io.File;

import org.apache.http.util.Args;

import com.ca.apm.automation.common.AutomationConstants;
import com.ca.apm.automation.utils.configuration.TasConfigurationWrapper;

/**
 * 
 * @author Dhruv Mevada (mevdh01)
 *
 */
public class UMAgentConfig extends BaseAppConfig
{
    private static final String DEFAULT_LOG_FILE_NAME = "IntroscopeAgent.log";
    private static final String DEFAULT_PROFILE_NAME = "IntroscopeAgent.profile";
    public static final String LOG_PATH_PROPERTY = AutomationConstants.Agent.LOG_PATH_PROPERTY;
    public static final String DEFAULT_NODEJS_PBDNAME = "nodejs-common.pbd";
    public static final String DEFAULT_NODEJS_TOGGLES_PBDNAME = "nodejs-toggles.pbd";
    public static final String APM_IA_LINUX_SCRIPT_NAME = "APMIAgent.sh";
    
    private final String logFileDir;
    protected final String pbdPath;
    protected final String togglesPbdPath;
    
    public static final String LOG_LEVEL = AutomationConstants.Agent.LOG_LEVEL;
    public static final String PROBE_COLLECTOR_LOG_LEVEL = "log4j.logger.IntroscopeAgent.ProbeCollector";
    
    public UMAgentConfig(String home)
    {
        super(home, createFullPath(home.substring(0, home.length()), "core", "config", DEFAULT_PROFILE_NAME));
        this.logFileDir = createFullPath(home.substring(0, home.length()), "logs");
        this.logPath = createFullPath(logFileDir, DEFAULT_LOG_FILE_NAME);
        
        String extDirectivesDir = createFullPath(home, "extensions", "NodeExtension", "directives");
        this.pbdPath = createFullPath(extDirectivesDir, DEFAULT_NODEJS_PBDNAME);
        this.togglesPbdPath = createFullPath(extDirectivesDir, DEFAULT_NODEJS_TOGGLES_PBDNAME);
    }

    public void updateLogLevel(AgentLoggingLevel level) {
        super.updateProperty(LOG_LEVEL, level.getLevel() + ", console, logfile");
    }

    public void updateProbeCollectorLogLevel(AgentLoggingLevel level) {
        super.updateProperty(PROBE_COLLECTOR_LOG_LEVEL, level.getLevel() + ", console, logfile");
    }

    public void updateLogFilePath(String path) {
        Args.notNull(path, "UMAgent agent log file path");
        super.updateProperty(LOG_PATH_PROPERTY, path);
        setLogFilePath(path);
    }

    @Override
    public String getProperty(String key) {
        if (configFile instanceof TasConfigurationWrapper) {
            TasConfigurationWrapper config = (TasConfigurationWrapper) configFile;
            return String.valueOf(config.getProperty(key));
        }
        return null;
    }
    
    public void updateLogFileName(String name) {
        String path = logFileDir + File.separator + name;
        updateLogFilePath(path);
    }

    public String getPbdPath() {
        return pbdPath;
    }

    public String getTogglesPbdPath() {
        return togglesPbdPath;
    }
    
    public enum AgentLoggingLevel {
        ERROR("ERROR"), INFO("INFO"), DEBUG("DEBUG"), TRACE(
                "TRACE#com.wily.util.feedback.Log4JSeverityLevel"), VERBOSE(
                "VERBOSE#com.wily.util.feedback.Log4JSeverityLevel");

        private String level;

        AgentLoggingLevel(String level) {
            this.level = level;
        }

        public String getLevel() {
            return level;
        }

        @Override
        public String toString() {
            return getLevel();
        }
    }
}
   