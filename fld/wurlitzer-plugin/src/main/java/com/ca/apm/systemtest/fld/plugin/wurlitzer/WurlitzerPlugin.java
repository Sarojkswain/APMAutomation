package com.ca.apm.systemtest.fld.plugin.wurlitzer;

import java.io.File;
import java.net.URL;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.Plugin;

/**
 * FLD Wurlitzer plugin interface.
 *
 * @author haiva01
 */
@PluginAnnotationComponent(pluginType = "wurlitzerPlugin")
public interface WurlitzerPlugin extends Plugin {
    int getDurationHours();

    void setDurationHours(int durationHours);

    boolean isDebug();

    void setDebug(boolean debug);

    int getInitialEdgeSetDelayMinutes();

    void setInitialEdgeSetDelayMinutes(int initialEdgeSetDelayMinutes);

    String getEmHost();

    void setEmHost(String emHost);

    int getEmPort();

    void setEmPort(int emPort);

    void createTempDir();

    void deleteTempDir();

    File downloadWurlitzer(String version);

    File unzipWurlitzerZip();

    void setBuiltInScenario(String scenario);

    void setScenarioUrl(URL scenario);

    void setNoScenario(String scenario);

    void setBuiltTargetScenario(String scenario);

    String execute();

    void stop(String wurlitzerId);

    String executeBuildFileWithTarget(String buildFileName, String target);

    String executeBuildFileWithTarget(String subPath, String buildFileName, String target);

    String editBuildFile(String subPath, String buildFileName, String emHost, int emPort);

    void replaceContent(String searchForFile);
}
