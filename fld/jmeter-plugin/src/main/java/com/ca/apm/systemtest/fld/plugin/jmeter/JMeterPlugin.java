package com.ca.apm.systemtest.fld.plugin.jmeter;

import java.util.Map;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.Plugin;

/**
 * FLD jMeter plugin interface.
 *
 * @author haiva01
 */
@PluginAnnotationComponent(pluginType = JMeterPlugin.PLUGIN)
public interface JMeterPlugin extends Plugin {
    String PLUGIN = "jmeterPlugin";
    String JMETER_STOPPING_PORT_KEY = "jmeterStoppingPort";
    String JMETER_PATH_KEY = "JmeterLogsPath";

    String JMETER_ARTIFACT_GROUP_ID = "com.ca.apm.binaries";
    String JMETER_ARTIFACT_ARTIFACT_ID = "apache-jmeter";
    String JMETER_ARTIFACT_DEFAULT_VERSION = "2.13";

    enum ScenarioType {
        BUILT_IN,
        URL
    }

    String createTempDir();
    
    String createTempDir(String parentDirPath, String prefix);
    
    void deleteTempDir();

    String downloadJMeter(String version);
    
    String downloadJmeterByUrl(String url);
    
    String unzipJMeterZip();

    ScenarioType getScenarioType();

    String getScenario();

    void setScenarioUrl(String url);

    void setBuiltinScenario(String scenario);

    String execute(Map<String, String> scenarioProperties);

    boolean isRunning(String jmeterHandle);

    boolean shutDown(int terminationPort);

    String getJmeterStoppingPort();

    String getLastResult(String jmeterHandle);

    String deployExtension(String jmeterDir, String artifactoryUrl, String groupId,
        String artifactId, String version, String classifier, String type);

    boolean checkIfJmeterIsInstalled();

    String getJmeterPath();
}
