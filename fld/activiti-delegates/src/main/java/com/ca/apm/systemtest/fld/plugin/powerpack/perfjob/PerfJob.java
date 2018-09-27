/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.powerpack.perfjob;

import org.activiti.engine.delegate.JavaDelegate;

/**
 * @author keyja01
 *
 */
public interface PerfJob {
    JavaDelegate installAgent();
    JavaDelegate installAgentWithPP();
    JavaDelegate stopAppServer();
    JavaDelegate startAppServer();
    JavaDelegate uninstallAgent();

    JavaDelegate checkJmeterInstallationStatus();
    JavaDelegate downloadJmeter();
    JavaDelegate installJmeter();
    JavaDelegate runJmeterTests();
    JavaDelegate checkJmeterRunStatus();
    JavaDelegate killJmeter();

    JavaDelegate cleanUp();
    JavaDelegate moveTypePerfLogs();
    JavaDelegate moveJmxLogs();
    JavaDelegate moveJstatLogs();
    JavaDelegate moveJmeterLogs();
    JavaDelegate moveIntroscopeAgentLogs();
    JavaDelegate archiveLogs();
    JavaDelegate configureAgent();
    JavaDelegate unConfigureAgent();

    JavaDelegate monitor();
    JavaDelegate checkMonitoring();
    JavaDelegate stopMonitoring();

    JavaDelegate buildReport();
    JavaDelegate groupResults();
    
    JavaDelegate checkAgentConnectedEM();

}
