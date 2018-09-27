package com.ca.apm.tests.em.properties;

public class EmPropertyConstants {
    
    
    public static final String defaultlog4jMangerProp="log4j.logger.Manager=INFO, console, logfile";
    public static final String defaultlog4jSupportProp="log4j.logger.Manager.Support=INFO, supportlogfile";
    public static final String defaultlog4jPerformanceProp="log4j.logger.Manager.Performance=DEBUG, performance";
    public static final String defaultlog4jQueryLogProp= "log4j.logger.Manager.QueryLog=INFO, querylog";
    
    public static final String errorlog4jMangerProp="log4j.logger.Manager=ERROR, console, logfile";
    public static final String errorlog4jSupportProp="log4j.logger.Manager.Support=ERROR, supportlogfile";
    public static final String errorlog4jPerformanceProp="log4j.logger.Manager.Performance=ERROR, performance";
    public static final String errorlog4jQueryLogProp= "log4j.logger.Manager.QueryLog=ERROR, querylog";
    
    
    public static final String debuglog4jMangerProp="log4j.logger.Manager=DEBUG, console, logfile";
    public static final String debuglog4jSupportProp="log4j.logger.Manager.Support=DEBUG, supportlogfile";
    public static final String debuglog4jPerformanceProp="log4j.logger.Manager.Performance=DEBUG, performance";
    public static final String debuglog4jQueryLogProp= "log4j.logger.Manager.QueryLog=DEBUG, querylog";
    
    
    public static final String verboselog4jMangerProp="log4j.logger.Manager=VERBOSE#com.wily.util.feedback.Log4JSeverityLevel, console, logfile";
    public static final String verboselog4jSupportProp="log4j.logger.Manager.Support=VERBOSE#com.wily.util.feedback.Log4JSeverityLevel, supportlogfile";
    public static final String verboselog4jPerformanceProp="log4j.logger.Manager.Performance=VERBOSE#com.wily.util.feedback.Log4JSeverityLevel, performance";
    public static final String verboselog4jQueryLogProp= "log4j.logger.Manager.QueryLog=VERBOSE#com.wily.util.feedback.Log4JSeverityLevel, querylog";
    
    public static final String tracelog4jMangerProp="log4j.logger.Manager=TRACE#com.wily.util.feedback.Log4JSeverityLevel, console, logfile";
    public static final String tracelog4jSupportProp="log4j.logger.Manager.Support=TRACE#com.wily.util.feedback.Log4JSeverityLevel, supportlogfile";
    public static final String tracelog4jPerformanceProp="log4j.logger.Manager.Performance=TRACE#com.wily.util.feedback.Log4JSeverityLevel, performance";
    public static final String tracelog4jQueryLogProp= "log4j.logger.Manager.QueryLog=TRACE#com.wily.util.feedback.Log4JSeverityLevel, querylog";
    
    
    
    public static final String emLogDefultPath="log4j.appender.logfile.File=logs/IntroscopeEnterpriseManager.log";
    public static final String emPerfLogDefultPath="log4j.appender.performance.File=logs/perflog.txt";
    
    public static final String emLogErrorPath="log4j.appender.logfile.File=logs/IntroscopeEnterpriseManager_ERROR.log";
    public static final String emLogInfoPath="log4j.appender.logfile.File=logs/IntroscopeEnterpriseManager_INFO.log";
    public static final String emLogDebugPath="log4j.appender.logfile.File=logs/IntroscopeEnterpriseManager_DEBUG.log";
    public static final String emLogVerbosePath="log4j.appender.logfile.File=logs/IntroscopeEnterpriseManager_VERBOSE.log";
    public static final String emLogTracePath="log4j.appender.logfile.File=logs/IntroscopeEnterpriseManager_TRACE.log";

    public static final String clwLog="log4j.appender.logfile.File=logs/IntroscopeEnterpriseManager_CLW.log";
    
    public static final String defaultLogFileMaxSize="log4j.appender.logfile.MaxFileSize=200MB";
    public static final String defaultLogFileMaxBackupIndex="log4j.appender.logfile.MaxBackupIndex=4";
    
    public static final String customLogFileMaxSize="log4j.appender.logfile.MaxFileSize=20KB";
    public static final String customLogFileMaxBackupIndex="log4j.appender.logfile.MaxBackupIndex=3";
    
    public static final String defaultQueryLogDisableProp= "log4j.additivity.Manager.QueryLog=false";
    public static final String defaultEmPerformanceCompressed="introscope.enterprisemanager.performance.compressed=true";
    public static final String defaultEMPerflogDisabled="log4j.additivity.Manager.Performance=false";
    
    public static final String defaultEMSupportDisabled="log4j.additivity.Manager.Support=false";
    
    public static final String clusterEM2Port= "introscope.enterprisemanager.clustering.login.em2.port=";
    public static final String clusterEM3Port= "introscope.enterprisemanager.clustering.login.em3.port=";

    public static final String clusterEM1PublicKey="introscope.enterprisemanager.clustering.login.em1.publickey=config/internal/server/EM.public";
    public static final String clusterEM2PublicKey="introscope.enterprisemanager.clustering.login.em2.publickey=config/internal/server/EM.public";
    public static final String clusterEM3PublicKey="introscope.enterprisemanager.clustering.login.em3.publickey=config/internal/server/EM.public";

    public static final String defaultEMAgentAllowedProp="introscope.apm.agentcontrol.agent.allowed=true";
    public static final String defaultEMlistLookup="introscope.apm.agentcontrol.agent.emlistlookup.enable=true";
    public static final String defaultEMdisallowedConnLimit="introscope.enterprisemanager.agent.disallowed.connection.limit=0";

    public static final String defaultEMAgentAllowedPropFalse="introscope.apm.agentcontrol.agent.allowed=false";

}
