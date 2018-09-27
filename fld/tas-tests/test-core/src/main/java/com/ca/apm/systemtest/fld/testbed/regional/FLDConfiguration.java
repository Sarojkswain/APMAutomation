/**
 *
 */

package com.ca.apm.systemtest.fld.testbed.regional;

import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * @author KEYJA01
 */
public interface FLDConfiguration {
    public static final String LOG_MONITOR_CONFIG_JSON = "/com/ca/apm/systemtest/fld/testbed/devel/log-monitor-config.json";
    
    /**
     * @return
     */
    public Map<String, String> getMachineTemplateOverrides();
        
    /**
     * @return The email to be used for configuring regular reports
     */
    String getReportEmail();
    
    /**
     * Returns the version of the domain-config.xml import file to use 
     * @return
     */
    public String getDomainConfigVersion();
    
    /**
     * @return The DB Target release version number to be used for domain-config.xml import file
     */
    public String getDbTargetReleaseVersion();
    
    /**
     * Should the EM be backed up during cleanup phase
     * @return true if the backup should be skipped 
     */
    public boolean isSkipBackup();
    
    /**
     * Returns the host that backup archives should be sent to
     * @return
     */
    public String getBackupHost();
    
    /**
     * Returns the host that backup archives should be sent to
     * @return
     */
    public String getBackupPassword();
    
    /**
     * Returns the host that backup archives should be sent to
     * @return
     */
    public String getBackupUser();

    /**
     * @return The email addresses to which log monitor emails should be sent
     */
    String[] getLogMonitorEmail();

    /**
     * @return The version number to be used for APM components (EM, Agent, etc)
     */
    String getEmVersion();
    
    /**
     * @return The apiVersion number of EM
     */
    String getApiVersion();
    
    /**
     * @returns if the Oracle DB should be used for EM installation
     */
    boolean isOracleMode();
    
    /**
     * Log monitor configuration information for MOM.
     *
     * @return {@link LogMonitorConfigurationSource} instance.
     */
    LogMonitorConfigurationSource getMomLogMonitorConfiguration();

    /**
     * Log monitor configuration information for collector given by its name.
     *
     * @return {@link LogMonitorConfigurationSource} instance.
     * @param collector collector name
     */
    LogMonitorConfigurationSource getCollectorLogMonitorConfiguration(String collector);

    /**
     * Log monitor configuration information for web view.
     *
     * @return {@link LogMonitorConfigurationSource} instance.
     */
    LogMonitorConfigurationSource getWebViewLogMonitorConfiguration();
    
    /**
     * Returns the SMTP server used to send the FLD configuration email
     * @return
     */
    public String getFldConfigSmtpHost();
    
    /**
     * Returns the SMTP server used to configure TESS
     * @return
     */
    public String getTessSmtpHost();

    /**
     * Returns if the docker should be used for EM installaiton
     */
    public boolean isDockerMode();


    /**
     * Returns type of the agent -EM communication (socket, ssl, http, https)
     * @return
     */
    public String getAgent2EmConnectionType();

    /**
     * Returns if to configure EM to require client auth
     */
    public boolean isEmNeedClientAuth();

    /**
     * Whether to use Docker to deploy WLS
     */
    public boolean isWeblogicDockerDeploy();
    
    /**
     * Whether to use Docker to deploy WAS
     */
    public boolean isWebsphereDockerDeploy();
    
    /**
     * Whether to use Docker to deploy JBoss
     */
    public boolean isJBossDockerDeploy();
    
    /**
     * Whether to use Docker to deploy Tomcat
     */
    public boolean isTomcatDockerDeploy();

    int getRunDuration(TimeUnit minutes);
}
