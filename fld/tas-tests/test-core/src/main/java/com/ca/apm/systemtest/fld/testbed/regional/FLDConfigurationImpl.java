/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed.regional;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.ca.apm.systemtest.fld.flow.DeployLogMonitorFlowContext;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil;
import com.ca.apm.systemtest.fld.testbed.util.LogMonitorConfigurationSourceImpl;

/**
 * @author keyja01
 *
 */
public class FLDConfigurationImpl implements FLDConfiguration {
    private String emVersion;
    private String apiVersion;
    private boolean oracleMode;
    private String[] logMonitorEmails;
    private String reportEmail;
    private String domainConfigVersion;
    private String dbTargetReleaseVersion;
    private String backupHost;
    private String backupUser;
    private String backupPassword;
    private boolean skipBackup = true;
    private Map<String, String> machineTemplateOverrideMap;
    private final String fldConfigSmtpHost;
    private final String tessSmtpHost;
    private boolean dockerMode;
    private String agent2EmConnectionType;
    private boolean emNeedClientAuth;
    private boolean tomcatDockerDeploy;
    private boolean jbossDockerDeploy;
    private boolean websphereDockerDeploy;
    private boolean weblogicDockerDeploy;

    public FLDConfigurationImpl(InputStream in) throws Exception {
        Properties props = new Properties();
        props.load(in);
        
        emVersion = props.getProperty("fld.em.version");
        apiVersion = props.getProperty("fld.api.version");
        oracleMode = readBoolean(props, "fld.oracle");
        logMonitorEmails = StringUtils.commaDelimitedListToStringArray(props.getProperty("logmonitor.emails"));
        reportEmail = props.getProperty("report.email");
        domainConfigVersion = props.getProperty("domain.config.version");
        dbTargetReleaseVersion = props.getProperty("db.targetrelease.version");
        skipBackup = readBoolean(props, "backup.skip");
        backupHost = props.getProperty("backup.host");
        backupUser = props.getProperty("backup.user");
        backupPassword = props.getProperty("backup.pass");
        String machineTemplateMapProp = props.getProperty("machine.template.overrides");
        machineTemplateOverrideMap = parseRoleTemplateMap(machineTemplateMapProp);
        fldConfigSmtpHost = getPropOrDefault(props, "config.smtp.host", FLDConstants.DEFAULT_SMTP_HOST);
        tessSmtpHost = getPropOrDefault(props, "tess.smtp.host", FLDConstants.DEFAULT_SMTP_HOST);
        dockerMode = readBoolean(props, "fld.docker");
        weblogicDockerDeploy = readBoolean(props, "weblogic.docker.deploy");
        websphereDockerDeploy = readBoolean(props, "websphere.docker.deploy");
        jbossDockerDeploy = readBoolean(props, "jboss.docker.deploy");
        tomcatDockerDeploy = readBoolean(props, "tomcat.docker.deploy");

        Assert.notNull(emVersion);
        Assert.notEmpty(logMonitorEmails);
        Assert.notNull(reportEmail);
        Assert.notNull(domainConfigVersion);

        agent2EmConnectionType = props.getProperty("dotnetagent.agent2EmConnectionType");
        emNeedClientAuth = readBoolean(props, "dotnetagent.emNeedClientAuth");
    }
    
    
    private boolean readBoolean(Properties props, String key) {
        String val = props.getProperty(key);
        if (val != null) {
            return Boolean.valueOf(val);
        }
        return false;
    }
    
    
    private String getPropOrDefault(Properties props, String key, String defaultValue) {
        String val = props.getProperty(key);
        if (val == null || val.trim().length() == 0) {
            return defaultValue;
        }
        return val;
    }


    private Map<String, String> parseRoleTemplateMap(String src) {
        Map<String, String> map = new HashMap<>();
        StringTokenizer st = new StringTokenizer(src, ",");
        while (st.hasMoreElements()) {
            String token = st.nextToken();
            StringTokenizer st2 = new StringTokenizer(token, "=");
            if (st2.countTokens() != 2) {
                throw new IllegalArgumentException("Error while parsing role template map: invalid pair => " + token);
            }
            map.put(st2.nextToken().trim(), st2.nextToken().trim());
        }
        
        return map;
    }
    

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration#getReportEmail()
     */
    @Override
    public String getReportEmail() {
        return reportEmail;
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration#getDomainConfigVersion()
     */
    @Override
    public String getDomainConfigVersion() {
        return domainConfigVersion;
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration#getLogMonitorEmail()
     */
    @Override
    public String[] getLogMonitorEmail() {
        return logMonitorEmails;
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration#getEmVersion()
     */
    @Override
    public String getEmVersion() {
        return emVersion;
    }
    
    @Override
    public String getApiVersion() {
        return apiVersion;
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration#getMomLogMonitorConfiguration()
     */
    @Override
    public LogMonitorConfigurationSource getMomLogMonitorConfiguration() {
        return FLDTestbedUtil.getDefaultMomLogMonitorConfiguration();
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration#getCollectorLogMonitorConfiguration(java.lang.String)
     */
    @Override
    public LogMonitorConfigurationSource getCollectorLogMonitorConfiguration(String collector) {
        return new LogMonitorConfigurationSourceImpl(
            DeployLogMonitorFlowContext.LogMonitorConfigSource.ResourceFile,
            LOG_MONITOR_CONFIG_JSON, "emLogStream");
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration#getWebViewLogMonitorConfiguration()
     */
    @Override
    public LogMonitorConfigurationSource getWebViewLogMonitorConfiguration() {
        return new LogMonitorConfigurationSourceImpl(
            DeployLogMonitorFlowContext.LogMonitorConfigSource.ResourceFile,
            LOG_MONITOR_CONFIG_JSON, "wvLogStream");
    }

    @Override
    public String getBackupHost() {
        return backupHost;
    }

    @Override
    public String getBackupPassword() {
        return backupPassword;
    }

    @Override
    public String getBackupUser() {
        return backupUser;
    }

    @Override
    public boolean isSkipBackup() {
        return skipBackup;
    }

    @Override
    public Map<String, String> getMachineTemplateOverrides() {
        return machineTemplateOverrideMap;
    }


    @Override
    public String getFldConfigSmtpHost() {
        return fldConfigSmtpHost;
    }


    @Override
    public String getTessSmtpHost() {
        return tessSmtpHost;
    }

    @Override
    public boolean isDockerMode() {
        return dockerMode;
    }


    @Override
    public String getDbTargetReleaseVersion() {
        return dbTargetReleaseVersion;
    }


    @Override
    public String getAgent2EmConnectionType() {
        return agent2EmConnectionType;
    }

    @Override
    public boolean isEmNeedClientAuth() {
        return emNeedClientAuth;
    }


    @Override
    public boolean isWeblogicDockerDeploy() {
        return weblogicDockerDeploy;
    }


    @Override
    public boolean isWebsphereDockerDeploy() {
        return websphereDockerDeploy;
    }


    @Override
    public boolean isJBossDockerDeploy() {
        return jbossDockerDeploy;
    }


    @Override
    public boolean isTomcatDockerDeploy() {
        return tomcatDockerDeploy;
    }

    @Override
    public int getRunDuration(TimeUnit unit) {
        return (int) unit.convert(1, TimeUnit.DAYS);
    }


    @Override
    public boolean isOracleMode() {
        return oracleMode;
    }

}
